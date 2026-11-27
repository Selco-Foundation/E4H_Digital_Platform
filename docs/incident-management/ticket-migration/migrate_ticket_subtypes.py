"""
Moves existing tickets (eg_incident_v2) from the old issue types / sub types to the new ones, following
ticket_migration_mapping.json (built from Ticket Migration.xlsx by build_mapping.py):
  - reportertype 'HCR' tickets go to the facility user sub types (Incident.ServiceDefs with channel HF)
  - reportertype 'CRM' tickets go to the CRM sub types (channel CRM, else an offered entry without channel)

The mapping is written with labels. They are turned into codes against the Incident.ServiceDefs of the
environment the script runs on, since ids and codes differ between DEV and UAT:
  - incidenttype    = the ServiceDef menuPath
  - incidentsubtype = the ServiceDef serviceCode
A label matches a ServiceDef by its localized message (SERVICEDEFS.<CODE> in rainmaker-im / rainmaker-common,
what the UI shows), its name or its code. Labels that still do not match are given their code in
ticket_migration_overrides.json.

Prerequisite: the new ServiceDefs (HF and CRM) already exist in the environment's MDMS. A target that is
not found is reported as UNRESOLVED_TARGET and its tickets are left alone.

Modes:
  (default)      dry run: writes the report and the SQL it would run, changes nothing
  --apply        runs that SQL with psql in one transaction (backup table + UPDATE), then updates
                 Elasticsearch for the migrated tickets
  --es-only RUN  only (re)does the Elasticsearch update of an earlier run, from the backup table

Inputs (environment variables):
  MDMS_URL          e.g. http://localhost:8285            (or --servicedefs-file <MDMS export .json>)
  AUTH_TOKEN        token for MDMS / localization searches, when the environment needs one
  DATABASE_URL      postgresql://user:pass@host:5432/db   (or --tickets-csv <export> for a dry run)
  LOCALIZATION_URL  e.g. http://localhost:8098: localized labels, used to match the workbook labels and
                    written to Elasticsearch       (or --localization-file <_search response .json>)
  EGOV_ES_HOST, EGOV_ES_USERNAME, EGOV_ES_PASSWORD   Elasticsearch, for --apply / --es-only
Options:
  --skip-es          with --apply: database only
  --out-dir <dir>    where the report and SQL files go (default: ./out)

The script can be re-run: a migrated ticket is already on its target and is reported as ALREADY_NEW.
"""
import base64
import csv
import json
import os
import re
import ssl
import subprocess
import sys
import time
import urllib.error
import urllib.request
from collections import defaultdict

HERE = os.path.dirname(os.path.abspath(__file__))
TENANT_ID = "in"
SCHEMA = "Incident.ServiceDefs"
REPORTER_CHANNELS = {"HCR": "HF", "CRM": "CRM"}
BACKUP_TABLE = "eg_incident_v2_subtype_migration_bkp"
ES_INDICES = ["computed-sla-im-services-write", "audit-computed-sla-im-services-write"]
ES_BATCH = 500
# Searched in this order: a code found in the first module wins.
LOCALIZATION_MODULES = ["rainmaker-im", "rainmaker-common"]


def arg_value(name, default=None):
    if name in sys.argv:
        i = sys.argv.index(name)
        if i + 1 < len(sys.argv):
            return sys.argv[i + 1]
        sys.exit(f"{name} needs a value")
    return default


APPLY = "--apply" in sys.argv
ES_ONLY_RUN = arg_value("--es-only")
SKIP_ES = "--skip-es" in sys.argv
SERVICEDEFS_FILE = arg_value("--servicedefs-file")
TICKETS_CSV = arg_value("--tickets-csv")
LOCALIZATION_FILE = arg_value("--localization-file")
OUT_DIR = arg_value("--out-dir", os.path.join(os.getcwd(), "out"))
MAPPING_FILE = arg_value("--mapping", os.path.join(HERE, "ticket_migration_mapping.json"))
OVERRIDES_FILE = arg_value("--overrides", os.path.join(HERE, "ticket_migration_overrides.json"))

MDMS_URL = os.environ.get("MDMS_URL", "").rstrip("/")
AUTH_TOKEN = os.environ.get("AUTH_TOKEN", "").strip()
DATABASE_URL = os.environ.get("DATABASE_URL", "").strip()
LOCALIZATION_URL = os.environ.get("LOCALIZATION_URL", "").rstrip("/")
ES_HOST = os.environ.get("EGOV_ES_HOST", "").rstrip("/")
ES_USERNAME = os.environ.get("EGOV_ES_USERNAME", "")
ES_PASSWORD = os.environ.get("EGOV_ES_PASSWORD", "")


def norm(text):
    """Label key: lower case, letters and digits only ("Burnt (or) Damaged" -> "burntordamaged")."""
    return re.sub(r"[^a-z0-9]", "", (text or "").lower())


def sql_literal(value):
    return "'" + str(value).replace("'", "''") + "'"


# ---------------------------------------------------------------------------------------------- HTTP

def post_json(url, body, headers=None, insecure=False):
    data = json.dumps(body).encode()
    request = urllib.request.Request(url, data=data, headers={"Content-Type": "application/json", **(headers or {})})
    context = ssl._create_unverified_context() if insecure else None
    with urllib.request.urlopen(request, context=context, timeout=300) as response:
        return json.loads(response.read() or b"{}")


def request_info():
    return {"apiId": "Rainmaker", "authToken": AUTH_TOKEN, "msgId": "migrate-ticket-subtypes|en_IN"}


# --------------------------------------------------------------------------------------- ServiceDefs

def load_servicedefs():
    """Every Incident.ServiceDef, deprecated and inactive ones included (old tickets still use them)."""
    if SERVICEDEFS_FILE:
        with open(SERVICEDEFS_FILE) as f:
            records = json.load(f)
        if isinstance(records, dict):
            records = records.get("mdms", [])
    else:
        if not MDMS_URL:
            sys.exit("Set MDMS_URL or pass --servicedefs-file")
        records, offset = [], 0
        while True:
            body = {"RequestInfo": request_info(),
                    "MdmsCriteria": {"tenantId": TENANT_ID, "schemaCode": SCHEMA, "limit": 500, "offset": offset}}
            page = post_json(f"{MDMS_URL}/egov-mdms-service/v2/_search", body).get("mdms") or []
            records += page
            if len(page) < 500:
                break
            offset += 500
    defs = []
    for record in records:
        data = record.get("data") or {}
        if not data.get("serviceCode") or not data.get("menuPath"):
            continue
        defs.append({
            "menuPath": data["menuPath"],
            "serviceCode": data["serviceCode"],
            "name": data.get("name") or "",
            "channel": data.get("channel"),
            "componentCode": data.get("componentCode"),
            "offered": bool(data.get("active", True)) and not data.get("deprecated", False)
                       and record.get("isActive", True) is not False,
        })
    print(f"Loaded {len(defs)} ServiceDefs from {SERVICEDEFS_FILE or MDMS_URL}")
    return defs


def load_localization():
    """{localization code: message} of the en_IN SERVICEDEFS.* messages, {} when no source is given."""
    if LOCALIZATION_FILE:
        with open(LOCALIZATION_FILE) as f:
            messages = json.load(f).get("messages") or []
        source = LOCALIZATION_FILE
    elif LOCALIZATION_URL:
        url = (f"{LOCALIZATION_URL}/localization/messages/v1/_search?locale=en_IN&tenantId={TENANT_ID}"
               f"&module={'%2C'.join(LOCALIZATION_MODULES)}")
        try:
            messages = post_json(url, {"RequestInfo": request_info()}).get("messages") or []
        except (urllib.error.URLError, ValueError) as e:
            sys.exit(f"Localization search failed ({url}): {e}")
        source = LOCALIZATION_URL
    else:
        print("No localization (LOCALIZATION_URL / --localization-file): labels matched on names and codes only")
        return {}
    rank = {m: i for i, m in enumerate(LOCALIZATION_MODULES)}
    messages = sorted(messages, key=lambda m: rank.get(m.get("module"), len(rank)))
    localized = {}
    for m in messages:
        code, message = m.get("code") or "", m.get("message")
        if code.startswith("SERVICEDEFS.") and message and code not in localized:
            localized[code] = message
    print(f"Loaded {len(localized)} SERVICEDEFS.* messages from {source}")
    return localized


def localization_key(code):
    """Key im-services localizes a type or sub type with (LocalizationService: "SERVICEDEFS." + code upper)."""
    return "SERVICEDEFS." + (code or "").upper()


def add_match_keys(defs, localized):
    """Every label a ServiceDef can be recognized by: localized message, name, code (and for its type:
    localized menuPath, menuPath, componentCode)."""
    for d in defs:
        d["label"] = localized.get(localization_key(d["serviceCode"])) or d["name"]
        d["typeLabel"] = localized.get(localization_key(d["menuPath"])) or d["menuPath"]
        d["labelKeys"] = {norm(d["name"]), norm(d["serviceCode"]),
                          norm(localized.get(localization_key(d["serviceCode"])))} - {""}
        d["typeKeys"] = {norm(d["menuPath"]), norm(d["componentCode"]),
                         norm(localized.get(localization_key(d["menuPath"])))} - {""}


# ------------------------------------------------------------------------------------------- Mapping

class Resolver:
    """Turns the labels of the mapping into ServiceDef codes."""

    def __init__(self, defs, overrides):
        self.defs = defs
        self.type_aliases = {rt: {norm(k): {norm(v) for v in (vs if isinstance(vs, list) else [vs])}
                                  for k, vs in aliases.items()}
                             for rt, aliases in overrides.get("typeAliases", {}).items()}
        self.source_overrides = {(norm(o["oldType"]), norm(o["oldSubType"])): o["codes"]
                                 for o in overrides.get("source", [])}
        self.target_overrides = {(o["reporterType"], norm(o["newType"]), norm(o["newSubType"])): o["serviceCode"]
                                 for o in overrides.get("target", [])}

    def _type_keys(self, reporter_type, label):
        return {norm(label)} | self.type_aliases.get(reporter_type, {}).get(norm(label), set())

    def source_codes(self, reporter_type, old_type, old_sub):
        """Old (menuPath, serviceCode) pairs a label stands for.

        The entries of that type outside the facility user list (HF) whose name or code matches the label,
        plus the codes the overrides add for it. An override code is taken even when this environment's
        MDMS no longer lists it: old tickets can carry codes that were removed since.
        """
        type_keys = self._type_keys(reporter_type, old_type)
        candidates = [d for d in self.defs if d["channel"] != "HF" and d["typeKeys"] & type_keys]
        key = norm(old_sub)
        pairs = {(d["menuPath"], d["serviceCode"]) for d in candidates if key in d["labelKeys"]}
        extra = self.source_overrides.get((norm(old_type), key), [])
        for menu_path in {d["menuPath"] for d in candidates}:
            pairs |= {(menu_path, code) for code in extra}
        return sorted(pairs)

    def target(self, reporter_type, new_type, new_sub):
        """The (menuPath, serviceCode, name) a ticket moves to, or (None, reason)."""
        channel = REPORTER_CHANNELS[reporter_type]
        type_keys = self._type_keys(reporter_type, new_type)

        def type_matches(d):
            return bool(d["typeKeys"] & type_keys)

        # Entries of the reporter's channel first; CRM may also reuse an offered entry without channel.
        pools = [[d for d in self.defs if d["offered"] and d["channel"] == channel and type_matches(d)]]
        if channel == "CRM":
            pools.append([d for d in self.defs if d["offered"] and not d["channel"] and type_matches(d)])

        override = self.target_overrides.get((reporter_type, norm(new_type), norm(new_sub)))
        for pool in pools:
            if override:
                found = [d for d in pool if d["serviceCode"] == override]
            else:
                found = [d for d in pool if norm(new_sub) in d["labelKeys"]]
            distinct = {(d["menuPath"], d["serviceCode"]) for d in found}
            if len(distinct) == 1:
                d = found[0]
                return (d["menuPath"], d["serviceCode"], d["name"]), None
            if len(distinct) > 1:
                return None, "ambiguous: " + ", ".join(f"{m}/{c}" for m, c in sorted(distinct))
        return None, f"no offered {channel} ServiceDef labelled '{new_sub}' under '{new_type}'"


def build_rules(mapping, resolver):
    """{(reporterType, MENUPATH, serviceCode): rule} for every old code of the mapping."""
    rules, problems = {}, []
    for row in mapping:
        rt = row["reporterType"]
        sources = resolver.source_codes(rt, row["oldType"], row["oldSubType"])
        label = f"{rt} | {row['oldType']} / {row['oldSubType']} -> {row['newType']} / {row['newSubType']}"
        if not sources:
            problems.append(("UNRESOLVED_SOURCE", label, "no old ServiceDef matches the label: add its codes in the overrides"))
            continue
        if row.get("action") == "skip":
            rule = {"status": "SKIP_REMOVE", "row": row}
        else:
            target, reason = resolver.target(rt, row["newType"], row["newSubType"])
            rule = {"status": "MIGRATE", "row": row, "target": target} if target else \
                {"status": "UNRESOLVED_TARGET", "row": row, "reason": reason}
            if not target:
                problems.append(("UNRESOLVED_TARGET", label, reason))
        for menu_path, code in sources:
            key = (rt, menu_path.upper(), code)
            previous = rules.get(key)
            if previous and previous.get("target") != rule.get("target"):
                problems.append(("CONFLICT", label, f"{menu_path}/{code} is also mapped by "
                                 f"{previous['row']['oldType']} / {previous['row']['oldSubType']}"))
                rules[key] = {"status": "CONFLICT", "row": row,
                              "reason": f"{menu_path}/{code} has two different targets in the mapping"}
                continue
            rules[key] = rule
    return rules, problems


# ------------------------------------------------------------------------------------------- Tickets

def psql(sql, single_transaction=False, file=None):
    if not DATABASE_URL:
        sys.exit("Set DATABASE_URL (or pass --tickets-csv for a dry run)")
    cmd = ["psql", DATABASE_URL, "-v", "ON_ERROR_STOP=1", "-X", "-q"]
    if single_transaction:
        cmd.append("-1")
    cmd += ["-f", file] if file else ["-c", sql]
    result = subprocess.run(cmd, capture_output=True, text=True)
    if result.returncode != 0:
        sys.exit(f"psql failed:\n{result.stderr}")
    return result.stdout


def psql_rows(query):
    out = psql(f"COPY ({query}) TO STDOUT WITH CSV HEADER")
    return list(csv.DictReader(out.splitlines()))


def load_ticket_groups():
    """[(reportertype, incidenttype, incidentsubtype, count)] of the HCR and CRM tickets."""
    reporters = tuple(REPORTER_CHANNELS)
    if TICKETS_CSV:
        counts = defaultdict(int)
        with open(TICKETS_CSV, newline="") as f:
            for row in csv.DictReader(f):
                if row.get("reportertype") in reporters:
                    counts[(row["reportertype"], row["incidenttype"], row["incidentsubtype"])] += 1
        groups = [(*k, n) for k, n in counts.items()]
    else:
        rows = psql_rows("SELECT reportertype, incidenttype, incidentsubtype, count(*) AS n FROM eg_incident_v2 "
                         f"WHERE reportertype IN {reporters} GROUP BY 1, 2, 3")
        groups = [(r["reportertype"], r["incidenttype"], r["incidentsubtype"], int(r["n"])) for r in rows]
    return sorted(groups, key=lambda g: (g[0], (g[1] or "").upper(), g[2] or ""))


def classify(groups, rules, defs):
    channel_targets = {rt: {(d["menuPath"].upper(), d["serviceCode"]) for d in defs
                            if d["offered"] and d["channel"] == ch}
                       for rt, ch in REPORTER_CHANNELS.items()}
    mapped_targets = defaultdict(set)
    for (rt, _, _), rule in rules.items():
        if rule.get("target"):
            mapped_targets[rt].add((rule["target"][0].upper(), rule["target"][1]))

    report = []
    for rt, inc_type, sub_type, count in groups:
        current = ((inc_type or "").upper(), sub_type)
        rule = rules.get((rt, *current))
        entry = {"reportertype": rt, "incidenttype": inc_type, "incidentsubtype": sub_type, "tickets": count,
                 "status": "", "new_incidenttype": "", "new_incidentsubtype": "", "mapping_row": "", "detail": ""}
        if rule:
            row = rule["row"]
            entry["mapping_row"] = f"{row['oldType']} / {row['oldSubType']} -> {row['newType']} / {row['newSubType']}"
        if rule and rule["status"] == "MIGRATE":
            target = rule["target"]
            entry["new_incidenttype"], entry["new_incidentsubtype"] = target[0], target[1]
            if (target[0].upper(), target[1]) == current and target[0] == inc_type:
                entry["status"] = "ALREADY_NEW"
            else:
                entry["status"] = "MIGRATE"
        elif rule:
            entry["status"] = rule["status"]
            entry["detail"] = rule.get("reason", "")
        elif current in channel_targets[rt] or current in mapped_targets[rt]:
            entry["status"] = "ALREADY_NEW"
        else:
            entry["status"] = "UNMAPPED"
            entry["detail"] = "old code not in Ticket Migration.xlsx (or its label does not match MDMS)"
        report.append(entry)
    return report


# ----------------------------------------------------------------------------------------------- SQL

def migration_sql(report, run_id):
    now_ms = int(time.time() * 1000)
    lines = [
        f"-- Ticket issue type / sub type migration, run {run_id}",
        f"CREATE TABLE IF NOT EXISTS {BACKUP_TABLE} (",
        "    run_id VARCHAR(64) NOT NULL,",
        "    id VARCHAR(64) NOT NULL,",
        "    incidentid VARCHAR(64),",
        "    reportertype VARCHAR(64),",
        "    old_incidenttype VARCHAR(256),",
        "    old_incidentsubtype VARCHAR(256),",
        "    new_incidenttype VARCHAR(256),",
        "    new_incidentsubtype VARCHAR(256),",
        "    migrated_at BIGINT NOT NULL",
        ");",
    ]
    for e in report:
        if e["status"] != "MIGRATE":
            continue
        where = (f"reportertype = {sql_literal(e['reportertype'])} "
                 f"AND incidenttype = {sql_literal(e['incidenttype'])} "
                 f"AND incidentsubtype = {sql_literal(e['incidentsubtype'])}")
        new_type, new_sub = sql_literal(e["new_incidenttype"]), sql_literal(e["new_incidentsubtype"])
        lines += [
            "",
            f"-- {e['reportertype']} {e['incidenttype']}/{e['incidentsubtype']} -> "
            f"{e['new_incidenttype']}/{e['new_incidentsubtype']} ({e['tickets']} tickets in the report)",
            f"INSERT INTO {BACKUP_TABLE} SELECT {sql_literal(run_id)}, id, incidentid, reportertype, incidenttype, "
            f"incidentsubtype, {new_type}, {new_sub}, {now_ms} FROM eg_incident_v2 WHERE {where};",
            f"UPDATE eg_incident_v2 SET incidenttype = {new_type}, incidentsubtype = {new_sub} WHERE {where};",
        ]
    return "\n".join(lines) + "\n"


def rollback_sql(run_id):
    return (f"-- Puts back the issue type / sub type of the tickets migrated by run {run_id}\n"
            f"UPDATE eg_incident_v2 i SET incidenttype = b.old_incidenttype, incidentsubtype = b.old_incidentsubtype\n"
            f"FROM {BACKUP_TABLE} b WHERE b.run_id = {sql_literal(run_id)} AND i.id = b.id;\n")


# ------------------------------------------------------------------------------------- Elasticsearch

def localized_labels(codes, defs):
    """{type or sub type code: label written to Elasticsearch}: the localized message, else the ServiceDef
    name (sub type) or the menuPath (type), as add_match_keys set them."""
    labels = {d["menuPath"]: d["typeLabel"] for d in defs}
    labels.update({d["serviceCode"]: d["label"] for d in defs})
    return {code: labels.get(code, code) for code in codes}


def update_elasticsearch(run_id, defs):
    if not ES_HOST:
        sys.exit("Set EGOV_ES_HOST (or use --skip-es)")
    rows = psql_rows(f"SELECT incidentid, new_incidenttype, new_incidentsubtype FROM {BACKUP_TABLE} "
                     f"WHERE run_id = {sql_literal(run_id)}")
    by_target = defaultdict(list)
    for r in rows:
        by_target[(r["new_incidenttype"], r["new_incidentsubtype"])].append(r["incidentid"])
    labels = localized_labels({c for t in by_target for c in t}, defs)
    headers = {}
    if ES_USERNAME:
        headers["Authorization"] = "Basic " + base64.b64encode(f"{ES_USERNAME}:{ES_PASSWORD}".encode()).decode()
    script = ("if (ctx._source.Data == null) { ctx._source.Data = [:]; } "
              "if (ctx._source.Data.incident == null) { ctx._source.Data.incident = [:]; } "
              "ctx._source.Data.incident.incidentType = params.type; "
              "ctx._source.Data.incident.incidentSubType = params.subType; "
              "ctx._source.Data.incident.incidentType_localized = params.typeLabel; "
              "ctx._source.Data.incident.incidentSubType_localized = params.subTypeLabel; "
              "if (ctx._source.Data.incidentType != null) { ctx._source.Data.incidentType = params.type; } "
              "if (ctx._source.Data.incidentSubType != null) { ctx._source.Data.incidentSubType = params.subType; } "
              "if (ctx._source.Data.indexView == null) { ctx._source.Data.indexView = [:]; } "
              "ctx._source.Data.indexView.incidentType_localized = params.typeLabel; "
              "ctx._source.Data.indexView.incidentSubType_localized = params.subTypeLabel;")
    for index in ES_INDICES:
        updated = 0
        for (new_type, new_sub), ids in by_target.items():
            for i in range(0, len(ids), ES_BATCH):
                batch = ids[i:i + ES_BATCH]
                body = {
                    "script": {"lang": "painless", "source": script,
                               "params": {"type": new_type, "subType": new_sub,
                                          "typeLabel": labels.get(new_type, new_type),
                                          "subTypeLabel": labels.get(new_sub, new_sub)}},
                    "query": {"bool": {"should": [{"terms": {"Data.incident.incidentId.keyword": batch}},
                                                  {"terms": {"Data.incident.incidentId": batch}}],
                                       "minimum_should_match": 1}},
                }
                url = f"{ES_HOST}/{index}/_update_by_query?conflicts=proceed&refresh=true"
                result = post_json(url, body, headers=headers, insecure=True)
                updated += result.get("updated", 0)
                if result.get("failures"):
                    print(f"  {index}: failures {json.dumps(result['failures'])[:500]}")
        print(f"  {index}: {updated} documents updated")


# ---------------------------------------------------------------------------------------------- Main

def write_report(report, path):
    fields = ["reportertype", "incidenttype", "incidentsubtype", "tickets", "status",
              "new_incidenttype", "new_incidentsubtype", "mapping_row", "detail"]
    with open(path, "w", newline="") as f:
        writer = csv.DictWriter(f, fieldnames=fields)
        writer.writeheader()
        writer.writerows(report)


def main():
    defs = load_servicedefs()
    add_match_keys(defs, load_localization())
    if ES_ONLY_RUN:
        update_elasticsearch(ES_ONLY_RUN, defs)
        return

    with open(MAPPING_FILE) as f:
        mapping = json.load(f)
    overrides = {}
    if os.path.exists(OVERRIDES_FILE):
        with open(OVERRIDES_FILE) as f:
            overrides = json.load(f)

    rules, problems = build_rules(mapping, Resolver(defs, overrides))
    groups = load_ticket_groups()
    report = classify(groups, rules, defs)

    run_id = time.strftime("%Y%m%d_%H%M%S")
    os.makedirs(OUT_DIR, exist_ok=True)
    report_path = os.path.join(OUT_DIR, f"report_{run_id}.csv")
    problems_path = os.path.join(OUT_DIR, f"mapping_problems_{run_id}.csv")
    sql_path = os.path.join(OUT_DIR, f"migration_{run_id}.sql")
    rollback_path = os.path.join(OUT_DIR, f"rollback_{run_id}.sql")
    write_report(report, report_path)
    with open(problems_path, "w", newline="") as f:
        writer = csv.writer(f)
        writer.writerow(["problem", "mapping_row", "detail"])
        writer.writerows(problems)
    with open(sql_path, "w") as f:
        f.write(migration_sql(report, run_id))
    with open(rollback_path, "w") as f:
        f.write(rollback_sql(run_id))

    totals = defaultdict(lambda: [0, 0])
    for e in report:
        totals[(e["reportertype"], e["status"])][0] += 1
        totals[(e["reportertype"], e["status"])][1] += e["tickets"]
    print("\nTickets by status (groups / tickets):")
    for (rt, status), (n_groups, n_tickets) in sorted(totals.items()):
        print(f"  {rt:4} {status:18} {n_groups:5} groups {n_tickets:7} tickets")
    print(f"\nMapping rows that did not resolve: {len(problems)}  (see {problems_path})")
    print(f"Report:   {report_path}")
    print(f"SQL:      {sql_path}")
    print(f"Rollback: {rollback_path}")

    if not APPLY:
        print("\nDry run: nothing changed. Re-run with --apply to migrate.")
        return
    if TICKETS_CSV:
        sys.exit("--apply reads the tickets from the database: drop --tickets-csv")
    print("\nApplying the SQL in one transaction...")
    psql(None, single_transaction=True, file=sql_path)
    migrated = psql_rows(f"SELECT count(*) AS n FROM {BACKUP_TABLE} WHERE run_id = {sql_literal(run_id)}")[0]["n"]
    print(f"  {migrated} tickets migrated (backup in {BACKUP_TABLE}, run_id {run_id})")
    if SKIP_ES:
        print(f"Elasticsearch skipped. Run later with: --es-only {run_id}")
    else:
        print("Updating Elasticsearch...")
        update_elasticsearch(run_id, defs)


if __name__ == "__main__":
    main()
