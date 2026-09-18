#!/usr/bin/env python3
"""
Reconcile incident status between Elasticsearch and the im-services database.

Walks every ticket in the incident index, asks im-services `/request/_search` for the
same ticket's current status, and where the two disagree calls `/request/_reindex` to
re-publish the database row to the indexer. Reindexing goes through Kafka and is
asynchronous, so after each reindex the document is re-read from Elasticsearch until it
reflects the database status or the verification window expires. The script never writes
to Elasticsearch itself — the indexer owns that.

Usage:
    export ES_HOST=https://es.example:9200 ES_USERNAME=elastic ES_PASSWORD=...
    export IM_SERVICES_URL=https://host/im-services IM_AUTH_TOKEN=...
    python3 reindex_status_sync.py --tenant-id pb.amritsar --report /tmp/drift.csv

Every run applies: mismatched tickets are reindexed. Use --dry-run to only report.
"""

import argparse
import csv
import json
import logging
import os
import sys
import threading
import time
from concurrent.futures import FIRST_COMPLETED, ThreadPoolExecutor, wait
from dataclasses import dataclass, field
from typing import Any, Dict, Iterator, List, Optional

import requests
import urllib3
from requests.adapters import HTTPAdapter

# The secured ES8 cluster serves a self-signed certificate. Both the ES Flyway migrations
# in im-services and im-services-analytics trust it unconditionally; this mirrors that
# rather than inventing a different trust story for a one-off reconciliation script.
urllib3.disable_warnings(urllib3.exceptions.InsecureRequestWarning)

# Matches IncidentIndexRepository.INCIDENT_ID_SORT_FIELD — one document per incident, so
# the incident id is both unique and stable, which is what search_after needs.
INCIDENT_ID_SORT_FIELD = "Data.incident.incidentId.keyword"

DEFAULT_ES_INDEX = "computed-sla-im-services-write"

# Thrown by WorkflowService when the workflow engine holds no process instance for an
# incident. Both /request/_search and /request/_reindex fail on it, so a ticket in this
# state can be reported but not reconciled.
WORKFLOW_NOT_FOUND = "WORKFLOW_NOT_FOUND"

log = logging.getLogger("reindex-status-sync")


# --------------------------------------------------------------------------------------
# Config
# --------------------------------------------------------------------------------------


@dataclass
class Config:
    es_host: str
    es_index: str
    es_username: str
    es_password: str
    im_base_url: str
    request_info: Dict[str, Any]
    tenant_id: Optional[str]
    page_size: int
    workers: int
    max_tickets: Optional[int]
    incident_ids: Optional[List[str]]
    verify_attempts: int
    verify_delay: float
    dry_run: bool
    report_path: Optional[str]
    connect_timeout: float
    read_timeout: float

    @property
    def timeout(self):
        return (self.connect_timeout, self.read_timeout)


def build_request_info(args) -> Dict[str, Any]:
    """RequestInfo sent on every im-services call.

    ServiceRequestValidator.validateSearchParam reads userInfo.type to pick the allowed
    search-parameter set, so userInfo must be populated — an authToken alone is not
    enough unless the gateway enriches it.
    """
    if args.request_info_file:
        with open(args.request_info_file) as fh:
            payload = json.load(fh)
        # Accept either a bare RequestInfo or a wrapper containing one.
        return payload.get("RequestInfo", payload)

    auth_token = args.auth_token or os.getenv("IM_AUTH_TOKEN", "")
    user_type = os.getenv("IM_USER_TYPE", "SYSTEM")
    user_uuid = os.getenv("IM_USER_UUID", "")
    user_name = os.getenv("IM_USER_NAME", "reindex-status-sync")
    user_tenant = os.getenv("IM_USER_TENANT", args.tenant_id or "")
    roles_raw = os.getenv("IM_USER_ROLES", "")

    roles = []
    for code in [r.strip() for r in roles_raw.split(",") if r.strip()]:
        roles.append({"code": code, "name": code, "tenantId": user_tenant})

    return {
        "apiId": "reindex-status-sync",
        "ver": ".01",
        "ts": int(time.time() * 1000),
        "action": "_search",
        "did": "1",
        "key": "",
        "msgId": "reindex-status-sync|en_IN",
        "authToken": auth_token,
        "userInfo": {
                "id": 14347,
                "uuid": "6b4fc7e4-6959-435f-b4ea-66df2a9d4b08",
                "userName": "CRONJOB",
                "password": None,
                "salutation": None,
                "name": "CRONJOB",
                "gender": "MALE",
                "mobileNumber": "9449594044",
                "emailId": None,
                "altContactNumber": None,
                "pan": None,
                "aadhaarNumber": None,
                "permanentAddress": None,
                "permanentCity": None,
                "permanentPinCode": None,
                "correspondenceCity": None,
                "correspondencePinCode": None,
                "correspondenceAddress": None,
                "active": True,
                "dob": None,
                "pwdExpiryDate": 1772697081000,
                "locale": None,
                "type": "EMPLOYEE",
                "signature": None,
                "accountLocked": False,
                "roles": [
                    {
                        "name": "System user",
                        "code": "SYSTEM",
                        "description": None,
                        "tenantId": "in"
                    }
                ],
                "fatherOrHusbandName": "Mathihalli",
                "relationship": "FATHER",
                "bloodGroup": None,
                "identificationMark": None,
                "photo": None,
                "createdBy": "0",
                "createdDate": 1764921081000,
                "lastModifiedBy": "0",
                "lastModifiedDate": 1764921081000,
                "otpReference": None,
                "tenantId": "in"
            },
    }


# --------------------------------------------------------------------------------------
# Elasticsearch
# --------------------------------------------------------------------------------------


class EsClient:
    def __init__(self, cfg: Config):
        self.cfg = cfg
        self.host = cfg.es_host.rstrip("/")
        self.session = requests.Session()
        self.session.verify = False
        self.session.mount("https://", HTTPAdapter(pool_maxsize=cfg.workers + 4))
        self.session.mount("http://", HTTPAdapter(pool_maxsize=cfg.workers + 4))
        if cfg.es_username:
            self.session.auth = (cfg.es_username, cfg.es_password)
        self.search_url = f"{self.host}/{cfg.es_index}/_search"

    def _search(self, body: Dict[str, Any]) -> Dict[str, Any]:
        resp = self.session.post(self.search_url, json=body, timeout=self.cfg.timeout)
        resp.raise_for_status()
        return resp.json()

    def iter_tickets(self) -> Iterator[Dict[str, Any]]:
        """Walk the whole index with search_after.

        from/size is not usable here: Elasticsearch refuses it past
        index.max_result_window and it shifts under concurrent writes. Documents with no
        indexed incident id are filtered out — they have no sort value to resume from,
        and a document with no incident id is not a ticket.
        """
        search_after = None
        emitted = 0

        while True:
            filters: List[Dict[str, Any]] = [
                {"exists": {"field": INCIDENT_ID_SORT_FIELD}}
            ]
            if self.cfg.tenant_id:
                filters.append(
                    {"term": {"Data.incident.tenantId.keyword": self.cfg.tenant_id}}
                )
            if self.cfg.incident_ids:
                filters.append(
                    {"terms": {INCIDENT_ID_SORT_FIELD: self.cfg.incident_ids}}
                )

            body: Dict[str, Any] = {
                "size": self.cfg.page_size,
                "query": {"bool": {"filter": filters}},
                "_source": [
                    "Data.incident.incidentId",
                    "Data.incident.applicationStatus",
                    "Data.incident.tenantId",
                    "Data.tenantId",
                ],
                "sort": [{INCIDENT_ID_SORT_FIELD: {"order": "asc"}}],
            }
            if search_after:
                body["search_after"] = search_after

            hits = self._search(body).get("hits", {}).get("hits", [])
            if not hits:
                return

            for hit in hits:
                data = (hit.get("_source") or {}).get("Data") or {}
                incident = data.get("incident") or {}
                incident_id = incident.get("incidentId")
                if not incident_id:
                    continue
                yield {
                    "doc_id": hit.get("_id"),
                    "incident_id": incident_id,
                    "es_status": incident.get("applicationStatus"),
                    "tenant_id": incident.get("tenantId") or data.get("tenantId"),
                }
                emitted += 1
                if self.cfg.max_tickets and emitted >= self.cfg.max_tickets:
                    return

            search_after = hits[-1].get("sort")
            if not search_after:
                log.warning("Page returned no sort values; stopping the walk early")
                return

    def fetch_status(self, incident_id: str) -> Optional[str]:
        """Current indexed status for one incident, or None if it has no document."""
        body = {
            "size": 1,
            "query": {"term": {INCIDENT_ID_SORT_FIELD: incident_id}},
            "_source": ["Data.incident.applicationStatus"],
        }
        hits = self._search(body).get("hits", {}).get("hits", [])
        if not hits:
            return None
        source = (hits[0].get("_source") or {}).get("Data") or {}
        return (source.get("incident") or {}).get("applicationStatus")


# --------------------------------------------------------------------------------------
# im-services
# --------------------------------------------------------------------------------------


class ImServiceError(Exception):
    """An error response from im-services, carrying the eGov error code.

    The service answers failures with an `Errors` array rather than a plain status, and
    the code in it is the only way to tell a recoverable condition apart from a real
    fault — requests' bare "400 Client Error" hides it.
    """

    def __init__(self, status: int, code: str, message: str, body: str):
        super().__init__(f"HTTP {status} {code}: {message}" if code else f"HTTP {status}: {body[:300]}")
        self.status = status
        self.code = code
        self.message = message
        self.body = body


def _raise_for_egov_error(resp) -> Dict[str, Any]:
    """Return the parsed body, or raise ImServiceError carrying the eGov error code."""
    try:
        payload = resp.json()
    except ValueError:
        payload = None

    errors = (payload or {}).get("Errors") or []
    if errors:
        first = errors[0] or {}
        raise ImServiceError(resp.status_code, first.get("code") or "",
                             first.get("message") or "", resp.text)
    if not resp.ok:
        raise ImServiceError(resp.status_code, "", "", resp.text)
    return payload or {}


class ImServicesClient:
    def __init__(self, cfg: Config):
        self.cfg = cfg
        self.base = cfg.im_base_url.rstrip("/")
        self.session = requests.Session()
        self.session.verify = False
        self.session.mount("https://", HTTPAdapter(pool_maxsize=cfg.workers + 4))
        self.session.mount("http://", HTTPAdapter(pool_maxsize=cfg.workers + 4))

    def search_status(self, tenant_id: str, incident_id: str) -> Optional[str]:
        """Status from the service (database) side, or None if the ticket is unknown.

        The search criteria are bound with @ModelAttribute, so they travel as query
        params; only RequestInfo goes in the body. tenantId is mandatory whenever
        incidentId is supplied.
        """
        url = f"{self.base}/v2/request/_search"
        params = {"tenantId": tenant_id, "incidentId": incident_id}
        resp = self.session.post(
            url,
            params=params,
            json={"RequestInfo": self.cfg.request_info},
            timeout=self.cfg.timeout,
        )
        payload = _raise_for_egov_error(resp)
        wrappers = payload.get("IncidentWrappers") or []
        if not wrappers:
            return None
        return (wrappers[0].get("incident") or {}).get("applicationStatus")

    def reindex(self, tenant_id: str, incident_id: str) -> None:
        """Re-publish the incident's current database state to the indexer topic."""
        url = f"{self.base}/v2/request/_reindex"
        payload = {
            "RequestInfo": {**self.cfg.request_info, "action": "_reindex"},
            "tenantId": tenant_id,
            "incidentId": incident_id,
        }
        resp = self.session.post(url, json=payload, timeout=self.cfg.timeout)
        _raise_for_egov_error(resp)


# --------------------------------------------------------------------------------------
# Reconciliation
# --------------------------------------------------------------------------------------


@dataclass
class Counters:
    scanned: int = 0
    in_sync: int = 0
    missing_in_service: int = 0
    missing_tenant: int = 0
    workflow_missing: int = 0
    mismatched: int = 0
    reindexed: int = 0
    reindex_failed: int = 0
    verified: int = 0
    unverified: int = 0
    errors: int = 0
    _lock: threading.Lock = field(default_factory=threading.Lock, repr=False)

    def bump(self, name: str, amount: int = 1) -> None:
        with self._lock:
            setattr(self, name, getattr(self, name) + amount)


def normalise(status: Optional[str]) -> str:
    return (status or "").strip().upper()


def reconcile_one(ticket, es: EsClient, im: ImServicesClient, cfg: Config, counters: Counters):
    """Compare one ticket, reindex on drift, then verify the index caught up.

    Returns a report row, or None when the ticket is already in sync.
    """
    incident_id = ticket["incident_id"]
    tenant_id = ticket["tenant_id"]
    es_status = ticket["es_status"]

    row = {
        "incidentId": incident_id,
        "tenantId": tenant_id or "",
        "esStatus": es_status or "",
        "serviceStatus": "",
        "action": "",
        "finalEsStatus": "",
        "detail": "",
    }

    if not tenant_id:
        # /request/_reindex requires a tenantId and the indexed document carries none.
        counters.bump("missing_tenant")
        row["action"] = "SKIPPED_NO_TENANT"
        return row

    try:
        service_status = im.search_status(tenant_id, incident_id)
    except ImServiceError as exc:
        if exc.code == WORKFLOW_NOT_FOUND:
            # The incident row exists but the workflow engine holds no process instance
            # for it, and IMService.search enriches workflow for every hit before it
            # returns. Its status is therefore unreadable through this API — and
            # /request/_reindex would fail identically, since it calls the same search
            # and then getLatestProcessInstance. Nothing to do but report it.
            counters.bump("workflow_missing")
            row["action"] = "NO_WORKFLOW"
            row["detail"] = exc.message
            log.warning("incidentId=%s has no workflow process instance; cannot compare or reindex",
                        incident_id)
            return row
        counters.bump("errors")
        row["action"] = "SEARCH_FAILED"
        row["detail"] = f"{exc.code or exc.status}: {exc.message or exc.body[:200]}"
        log.error("Search failed for incidentId=%s: %s", incident_id, exc)
        return row
    except Exception as exc:
        counters.bump("errors")
        row["action"] = "SEARCH_FAILED"
        row["detail"] = str(exc)
        log.error("Search failed for incidentId=%s: %s", incident_id, exc)
        return row

    row["serviceStatus"] = service_status or ""

    if service_status is None:
        # Indexed but not in the database — reindexing cannot fix this, so only report it.
        counters.bump("missing_in_service")
        row["action"] = "NOT_FOUND_IN_SERVICE"
        return row

    if normalise(service_status) == normalise(es_status):
        counters.bump("in_sync")
        return None

    counters.bump("mismatched")
    log.info(
        "Drift on incidentId=%s tenantId=%s: index=%r service=%r",
        incident_id, tenant_id, es_status, service_status,
    )

    if cfg.dry_run:
        row["action"] = "DRY_RUN"
        return row

    try:
        im.reindex(tenant_id, incident_id)
        counters.bump("reindexed")
    except ImServiceError as exc:
        counters.bump("reindex_failed")
        row["action"] = "REINDEX_FAILED"
        row["detail"] = f"{exc.code or exc.status}: {exc.message or exc.body[:200]}"
        log.error("Reindex failed for incidentId=%s: %s", incident_id, exc)
        return row
    except Exception as exc:
        counters.bump("reindex_failed")
        row["action"] = "REINDEX_FAILED"
        row["detail"] = str(exc)
        log.error("Reindex failed for incidentId=%s: %s", incident_id, exc)
        return row

    # Reindex publishes to Kafka; the indexer writes the document some time later. Poll
    # rather than assume, so the run reports what actually landed in the index.
    final_status = es_status
    for attempt in range(cfg.verify_attempts):
        time.sleep(cfg.verify_delay)
        try:
            final_status = es.fetch_status(incident_id)
        except Exception as exc:
            log.warning("Verification read failed for incidentId=%s: %s", incident_id, exc)
            continue
        if normalise(final_status) == normalise(service_status):
            counters.bump("verified")
            row["action"] = "REINDEXED_VERIFIED"
            row["finalEsStatus"] = final_status or ""
            row["detail"] = f"verified after {attempt + 1} read(s)"
            return row

    counters.bump("unverified")
    row["action"] = "REINDEXED_UNVERIFIED"
    row["finalEsStatus"] = final_status or ""
    row["detail"] = f"index still stale after {cfg.verify_attempts} read(s)"
    log.warning(
        "incidentId=%s reindexed but the index still reads %r (expected %r)",
        incident_id, final_status, service_status,
    )
    return row


def run(cfg: Config) -> Counters:
    es = EsClient(cfg)
    im = ImServicesClient(cfg)
    counters = Counters()
    rows: List[Dict[str, str]] = []
    rows_lock = threading.Lock()

    started = time.time()
    log.info(
        "Walking index %s on %s (tenantId=%s, workers=%d, dry_run=%s)",
        cfg.es_index, cfg.es_host, cfg.tenant_id or "<all>", cfg.workers, cfg.dry_run,
    )

    with ThreadPoolExecutor(max_workers=cfg.workers) as pool:
        pending = set()
        for ticket in es.iter_tickets():
            counters.bump("scanned")
            pending.add(pool.submit(reconcile_one, ticket, es, im, cfg, counters))

            # Keep the in-flight set bounded so a large index does not queue every
            # ticket in memory before the first one completes.
            if len(pending) >= cfg.workers * 4:
                done, pending = wait(pending, return_when=FIRST_COMPLETED)
                _collect(done, rows, rows_lock, counters)

            if counters.scanned % 500 == 0:
                log.info("Scanned %d tickets so far", counters.scanned)

        done, _ = wait(pending)
        _collect(done, rows, rows_lock, counters)

    elapsed = time.time() - started
    _summarise(counters, elapsed)

    if cfg.report_path and rows:
        _write_report(cfg.report_path, rows)
        log.info("Wrote %d report rows to %s", len(rows), cfg.report_path)

    return counters


def _collect(done, rows, rows_lock, counters) -> None:
    """Drain finished futures into the report rows."""
    for future in done:
        try:
            row = future.result()
        except Exception as exc:  # defensive: reconcile_one handles its own failures
            counters.bump("errors")
            log.error("Worker raised: %s", exc)
            continue
        if row:
            with rows_lock:
                rows.append(row)


def _write_report(path: str, rows: List[Dict[str, str]]) -> None:
    fieldnames = ["incidentId", "tenantId", "esStatus", "serviceStatus",
                  "action", "finalEsStatus", "detail"]
    with open(path, "w", newline="") as fh:
        writer = csv.DictWriter(fh, fieldnames=fieldnames)
        writer.writeheader()
        writer.writerows(rows)


def _summarise(c: Counters, elapsed: float) -> None:
    log.info("=" * 62)
    log.info("Reconciliation summary")
    log.info("  tickets scanned in index      : %d", c.scanned)
    log.info("  already in sync               : %d", c.in_sync)
    log.info("  status mismatched             : %d", c.mismatched)
    log.info("  reindex calls succeeded       : %d", c.reindexed)
    log.info("  reindex calls failed          : %d", c.reindex_failed)
    log.info("  index verified updated        : %d", c.verified)
    log.info("  reindexed but still stale     : %d", c.unverified)
    log.info("  not found by im-services      : %d", c.missing_in_service)
    log.info("  no workflow process instance  : %d", c.workflow_missing)
    log.info("  skipped (no tenantId indexed) : %d", c.missing_tenant)
    log.info("  search/transport errors       : %d", c.errors)
    log.info("  elapsed                       : %.1fs", elapsed)
    log.info("=" * 62)


# --------------------------------------------------------------------------------------
# Entry point
# --------------------------------------------------------------------------------------


def parse_args(argv):
    p = argparse.ArgumentParser(
        description="Reconcile incident status between Elasticsearch and im-services.",
        formatter_class=argparse.ArgumentDefaultsHelpFormatter,
    )
    p.add_argument("--es-host", default=os.getenv("ES_HOST", "https://localhost:9200"))
    p.add_argument("--es-index", default=os.getenv("ES_INDEX", DEFAULT_ES_INDEX))
    p.add_argument("--es-username", default=os.getenv("ES_USERNAME", "elastic"))
    p.add_argument("--es-password", default=os.getenv("ES_PASSWORD", "9lwuatHbJh6Hseo987Hm8TEl"))
p.add_argument("--im-url", default=os.getenv("IM_SERVICES_URL", "http://localhost:8081/") +"im-services"),
                   help="Base URL up to and including the /im-services context path")
    p.add_argument("--auth-token", default=None, help="Overrides IM_AUTH_TOKEN")
    p.add_argument("--request-info-file", default=None,
                   help="JSON file holding the full RequestInfo to send (overrides env-built one)")
    p.add_argument("--tenant-id", default=None, help="Restrict the walk to one tenant")
    p.add_argument("--incident-ids-file", default=None,
                   help="File with one incidentId per line; only these are checked")
    p.add_argument("--page-size", type=int, default=500, help="Elasticsearch page size")
    p.add_argument("--workers", type=int, default=8, help="Concurrent tickets in flight")
    p.add_argument("--max-tickets", type=int, default=None, help="Stop after this many tickets")
    p.add_argument("--verify-attempts", type=int, default=5,
                   help="Re-reads of the index after a reindex before giving up")
    p.add_argument("--verify-delay", type=float, default=2.0,
                   help="Seconds between verification reads")
    p.add_argument("--connect-timeout", type=float, default=5.0)
    p.add_argument("--read-timeout", type=float, default=60.0)
    p.add_argument("--dry-run", action="store_true",
                   help="Report drift without calling /request/_reindex")
    p.add_argument("--report", default=None, help="Write a CSV of every non-in-sync ticket here")
    p.add_argument("--log-level", default="INFO")
    return p.parse_args(argv)


def main(argv=None):
    args = parse_args(argv or sys.argv[1:])
    logging.basicConfig(
        level=getattr(logging, args.log_level.upper(), logging.INFO),
        format="%(asctime)s %(levelname)-5s %(message)s",
    )

    incident_ids = None
    if args.incident_ids_file:
        with open(args.incident_ids_file) as fh:
            incident_ids = [line.strip() for line in fh if line.strip()]
        log.info("Restricting the run to %d incident ids", len(incident_ids))

    request_info = build_request_info(args)
    if not request_info.get("authToken"):
        log.warning("No authToken set — im-services calls will likely be rejected")

    cfg = Config(
        es_host=args.es_host,
        es_index=args.es_index,
        es_username=args.es_username,
        es_password=args.es_password,
        im_base_url=args.im_url,
        request_info=request_info,
        tenant_id=args.tenant_id,
        page_size=args.page_size,
        workers=max(1, args.workers),
        max_tickets=args.max_tickets,
        incident_ids=incident_ids,
        verify_attempts=args.verify_attempts,
        verify_delay=args.verify_delay,
        dry_run=args.dry_run,
        report_path=args.report,
        connect_timeout=args.connect_timeout,
        read_timeout=args.read_timeout,
    )

    counters = run(cfg)

    # Non-zero exit when the run could not fully reconcile, so a scheduled invocation
    # surfaces as a failure instead of passing silently.
    if counters.reindex_failed or counters.unverified or counters.errors:
        return 1
    return 0


if __name__ == "__main__":
    sys.exit(main())
