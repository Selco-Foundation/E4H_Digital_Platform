"""
Align the CRM Incident.ServiceDefs of an environment (DEV, UAT, ...) with CRM.Issue.type.subtype.xlsx,
through ServiceDefs.CRM.mapping.json (one row per issue type / sub type of the file).

The ServiceDefs differ between environments (ids, missing entries): the current entries are read from
MDMS, and each row names its entry by menuPath + serviceCode (case-insensitive), never by id (devId is
only the DEV id, for reference). When several entries share the same menuPath + serviceCode (e.g.
INVERTER/Other), the one with the row's name, then priority, then not deprecated, then the lowest id is used.

For each row:
  - action "ensure":    the entry is kept offered - deprecated=false, active=true, isActive=true. When it is
                        missing, it is created (channel CRM, next free id of the environment) with the row's
                        name and priority, and its localization message SERVICEDEFS.<SERVICECODE> = name.
  - action "deprecate": an entry removed from the file gets deprecated=true (nothing to do when missing).
  - --deprecate-others: every other CRM entry still offered (not in the mapping, not channel HF) gets
                        deprecated=true. Off by default.

Usage:
    AUTH_TOKEN=<token> USER_UUID=<uuid> [USER_ID=<id>] python3 sync_crm_servicedefs.py [--env dev|uat] \
        [--dry-run] [--deprecate-others] [--skip-localization]

    --env dev   MDMS http://localhost:8285, localization http://localhost:8088 (default)
    --env uat   MDMS http://localhost:8286, no default localization URL: set LOCALIZATION_URL or pass
                --skip-localization
    MDMS_URL / LOCALIZATION_URL override the URLs of the environment.

--dry-run reads MDMS (a token of the environment is still needed) and prints every change without writing.
The script can be re-run: an entry already in the expected state is left alone.
"""
import json
import os
import sys
import urllib.error
import urllib.request

HERE = os.path.dirname(os.path.abspath(__file__))
TENANT_ID = "in"
SCHEMA = "Incident.ServiceDefs"

ENVIRONMENTS = {
    "dev": {"mdms": "http://localhost:8285", "localization": "http://localhost:8088"},
    "uat": {"mdms": "http://localhost:8286", "localization": None},
}


def option(name, default=None):
    if name in sys.argv:
        index = sys.argv.index(name)
        if index + 1 < len(sys.argv):
            return sys.argv[index + 1]
    return default


ENV = option("--env", "dev").lower()
if ENV not in ENVIRONMENTS:
    print(f"Unknown --env {ENV}: expected one of {', '.join(ENVIRONMENTS)}")
    sys.exit(2)
MDMS_URL = os.environ.get("MDMS_URL", ENVIRONMENTS[ENV]["mdms"]).rstrip("/")
LOCALIZATION_URL = (os.environ.get("LOCALIZATION_URL") or ENVIRONMENTS[ENV]["localization"] or "").rstrip("/")
DRY_RUN = "--dry-run" in sys.argv
DEPRECATE_OTHERS = "--deprecate-others" in sys.argv
SKIP_LOCALIZATION = "--skip-localization" in sys.argv

AUTH_TOKEN = os.environ.get("AUTH_TOKEN", "").strip()
USER_UUID = os.environ.get("USER_UUID", "").strip()
USER_ID = os.environ.get("USER_ID", "").strip()

USER_INFO = {"uuid": USER_UUID, "tenantId": TENANT_ID, "type": "EMPLOYEE"}
if USER_ID:
    USER_INFO["id"] = int(USER_ID) if USER_ID.isdigit() else USER_ID
REQUEST_INFO = {"apiId": "Rainmaker", "authToken": AUTH_TOKEN, "userInfo": USER_INFO, "msgId": "sync-crm-servicedefs|en_IN"}

# Defaults of a created entry, as on the existing CRM entries
NEW_ENTRY_DEFAULTS = {"slaHours": 264, "department": "O&M", "active": True, "deprecated": False, "channel": "CRM"}


def post(url, body, write=True):
    """POSTs a request. Returns the parsed response, or None on failure. Writes are skipped in dry-run."""
    if DRY_RUN and write:
        return {}
    request = urllib.request.Request(url, data=json.dumps(body).encode(), headers={"Content-Type": "application/json"})
    try:
        with urllib.request.urlopen(request) as response:
            return json.loads(response.read() or b"{}")
    except urllib.error.HTTPError as e:
        print(f"  FAILED {e.code} {url}: {e.read().decode(errors='replace')[:300]}")
        return None
    except urllib.error.URLError as e:
        print(f"  FAILED {url}: {e.reason}")
        return None


def norm(value):
    return str(value or "").strip().lower()


def fetch_service_defs():
    response = post(f"{MDMS_URL}/egov-mdms-service/v2/_search",
                    {"RequestInfo": REQUEST_INFO,
                     "MdmsCriteria": {"tenantId": TENANT_ID, "schemaCode": SCHEMA, "limit": 2000, "offset": 0}},
                    write=False)
    if response is None:
        sys.exit(1)
    return response.get("mdms") or []


def find(by_key, row):
    """The environment's entry for a row, or None when it has none."""
    candidates = by_key.get((norm(row["menuPath"]), norm(row["serviceCode"])))
    if not candidates:
        return None
    return min(candidates, key=lambda r: (
        norm(r["data"].get("name")) != norm(row["name"]),
        norm(r["data"].get("priority")) != norm(row["priority"]),
        bool(r["data"].get("deprecated")),
        r["data"].get("id") or 0,
    ))


def describe(record):
    data = record["data"]
    return f"{data.get('id')} {data.get('menuPath')}/{data.get('serviceCode')}"


def update(record, changes, label):
    print(f"  update {label}: {changes}")
    record["data"].update({k: v for k, v in changes.items() if k != "isActive"})
    if "isActive" in changes:
        record["isActive"] = changes["isActive"]
    return post(f"{MDMS_URL}/egov-mdms-service/v2/_update/{SCHEMA}", {"RequestInfo": REQUEST_INFO, "Mdms": record}) is not None


def main():
    missing = [name for name, value in (("AUTH_TOKEN", AUTH_TOKEN), ("USER_UUID", USER_UUID)) if not value]
    if missing:
        print(f"Missing environment variable(s): {', '.join(missing)} - see the usage at the top of this script")
        sys.exit(2)
    if not LOCALIZATION_URL and not SKIP_LOCALIZATION:
        print(f"No localization URL for --env {ENV}: set LOCALIZATION_URL or pass --skip-localization")
        sys.exit(2)

    with open(os.path.join(HERE, "ServiceDefs.CRM.mapping.json")) as f:
        mapping = json.load(f)
    records = fetch_service_defs()
    by_key = {}
    for record in records:
        by_key.setdefault((norm(record["data"].get("menuPath")), norm(record["data"].get("serviceCode"))), []).append(record)
    next_id = max([r["data"].get("id") or 0 for r in records] or [0]) + 1
    print(f"[{ENV}] {MDMS_URL}: {len(records)} {SCHEMA} records read{' (dry-run: nothing is written)' if DRY_RUN else ''}")

    failures, kept, new_messages = 0, set(), []

    print("Entries of the CRM file")
    for row in mapping:
        label = f"{row['issueType']} / {row['subType']}"
        record = find(by_key, row)

        if row["action"] == "deprecate":
            if record is None:
                print(f"  not found, nothing to deprecate: {label}")
            elif not record["data"].get("deprecated") and not update(record, {"deprecated": True}, f"{label} (removed) -> {describe(record)}"):
                failures += 1
            continue

        if record is not None:
            kept.add(id(record))
            changes = {k: v for k, v in (("deprecated", False), ("active", True)) if record["data"].get(k) != v}
            if not record.get("isActive"):
                changes["isActive"] = True
            if changes and not update(record, changes, f"{label} -> {describe(record)}"):
                failures += 1
            continue

        data = {"id": next_id, "serviceCode": row["serviceCode"], "name": row["name"], "menuPath": row["menuPath"],
                "priority": row["priority"], "keywords": row["name"].lower(), **NEW_ENTRY_DEFAULTS}
        print(f"  create {label} -> {next_id} {row['menuPath']}/{row['serviceCode']} ({row['priority']})")
        body = {"RequestInfo": REQUEST_INFO, "Mdms": {"tenantId": TENANT_ID, "schemaCode": SCHEMA, "data": data, "isActive": True}}
        if post(f"{MDMS_URL}/egov-mdms-service/v2/_create/{SCHEMA}", body) is None:
            failures += 1
        else:
            new_messages.append({"code": f"SERVICEDEFS.{row['serviceCode'].upper()}", "message": row["name"],
                                 "module": "rainmaker-im", "locale": "en_IN"})
        next_id += 1

    others = [r for r in records
              if id(r) not in kept and r["data"].get("channel") != "HF"
              and r.get("isActive") and r["data"].get("active") and not r["data"].get("deprecated")]
    print(f"CRM entries still offered but not in the file: {len(others)}")
    for record in sorted(others, key=lambda r: (str(r["data"].get("menuPath")), r["data"].get("id") or 0)):
        if DEPRECATE_OTHERS:
            if not update(record, {"deprecated": True}, describe(record)):
                failures += 1
        else:
            print(f"  kept as is (use --deprecate-others): {describe(record)}")

    if new_messages and not SKIP_LOCALIZATION:
        print(f"Localization of the {len(new_messages)} created entries ({LOCALIZATION_URL})")
        for message in new_messages:
            print(f"  {message['code']} = {message['message']}")
        body = {"RequestInfo": REQUEST_INFO, "tenantId": TENANT_ID, "messages": new_messages}
        if post(f"{LOCALIZATION_URL}/localization/messages/v1/_upsert", body) is None:
            failures += 1

    print("Done" if failures == 0 else f"Done with {failures} failure(s)")
    sys.exit(1 if failures else 0)


if __name__ == "__main__":
    main()
