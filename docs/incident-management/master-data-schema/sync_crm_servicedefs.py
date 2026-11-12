"""
DEV ENVIRONMENT ONLY - ServiceDefs.CRM.mapping.json references the Incident.ServiceDefs ids of DEV (MDMS on
port 8285). The ServiceDefs differ between environments: do not run it against UAT or another environment.

Align the CRM Incident.ServiceDefs with CRM.Issue.type.subtype.xlsx, through ServiceDefs.CRM.mapping.json
(one row per issue type / sub type of the file):
  - action "reuse":  the existing entry (data.id) is kept offered: deprecated=false, active=true, isActive=true
  - action "create": a new entry is created (channel CRM, next free id), with its localization message
                     SERVICEDEFS.<SERVICECODE> = name
  - --deprecate-others: every other CRM entry still offered (not in the mapping, not channel HF) gets
                     deprecated=true. Off by default.

Usage:
    MDMS_URL=http://localhost:8285 LOCALIZATION_URL=http://localhost:8088 \
    AUTH_TOKEN=<token> USER_UUID=<uuid> [USER_ID=<id>] \
        python3 sync_crm_servicedefs.py [--dry-run] [--deprecate-others] [--skip-localization]

--dry-run reads MDMS (a token is still needed) and prints every change without writing anything.
The script can be re-run: an entry already in the expected state is left alone, and an entry to create
that already exists (same menuPath and serviceCode) is not created again.
"""
import json
import os
import sys
import urllib.error
import urllib.request

HERE = os.path.dirname(os.path.abspath(__file__))
TENANT_ID = "in"
SCHEMA = "Incident.ServiceDefs"
MDMS_URL = os.environ.get("MDMS_URL", "http://localhost:8285").rstrip("/")
LOCALIZATION_URL = os.environ.get("LOCALIZATION_URL", "http://localhost:8088").rstrip("/")
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


def key(menu_path, service_code):
    return (str(menu_path).strip().lower(), str(service_code).strip().lower())


def fetch_service_defs():
    response = post(f"{MDMS_URL}/egov-mdms-service/v2/_search",
                    {"RequestInfo": REQUEST_INFO,
                     "MdmsCriteria": {"tenantId": TENANT_ID, "schemaCode": SCHEMA, "limit": 2000, "offset": 0}},
                    write=False)
    if response is None:
        sys.exit(1)
    return response.get("mdms") or []


def update(record, changes, label):
    print(f"  update {label}: {changes}")
    record["data"].update({k: v for k, v in changes.items() if k != "isActive"})
    if "isActive" in changes:
        record["isActive"] = changes["isActive"]
    ok = post(f"{MDMS_URL}/egov-mdms-service/v2/_update/{SCHEMA}", {"RequestInfo": REQUEST_INFO, "Mdms": record})
    return ok is not None


def main():
    missing = [name for name, value in (("AUTH_TOKEN", AUTH_TOKEN), ("USER_UUID", USER_UUID)) if not value]
    if missing:
        print(f"Missing environment variable(s): {', '.join(missing)} - see the usage at the top of this script")
        sys.exit(2)

    with open(os.path.join(HERE, "ServiceDefs.CRM.mapping.json")) as f:
        mapping = json.load(f)
    records = fetch_service_defs()
    by_id = {r["data"].get("id"): r for r in records}
    by_key = {key(r["data"].get("menuPath"), r["data"].get("serviceCode")): r for r in records}
    next_id = max((r["data"].get("id") or 0) for r in records) + 1
    print(f"{len(records)} {SCHEMA} records read{' (dry-run: nothing is written)' if DRY_RUN else ''}")

    failures, kept_ids, new_messages = 0, set(), []

    print("Entries of the CRM file")
    for row in mapping:
        label = f"{row['issueType']} / {row['subType']}"
        if row["action"] == "reuse":
            record = by_id.get(row["id"])
            if record is None:
                print(f"  MISSING id {row['id']} for {label}")
                failures += 1
                continue
            kept_ids.add(row["id"])
            data = record["data"]
            changes = {k: v for k, v in (("deprecated", False), ("active", True)) if data.get(k) != v}
            if not record.get("isActive"):
                changes["isActive"] = True
            if changes and not update(record, changes, f"{label} -> {data['id']} {data['serviceCode']}"):
                failures += 1
            continue

        existing = by_key.get(key(row["menuPath"], row["serviceCode"]))
        if existing is not None:
            kept_ids.add(existing["data"]["id"])
            print(f"  already created: {label} -> {existing['data']['id']} {row['serviceCode']}")
            continue
        data = {"id": next_id, "serviceCode": row["serviceCode"], "name": row["name"], "menuPath": row["menuPath"],
                "priority": row["priority"], "keywords": row["name"].lower(), **NEW_ENTRY_DEFAULTS}
        print(f"  create {label} -> {next_id} {row['menuPath']}/{row['serviceCode']} ({row['priority']})")
        body = {"RequestInfo": REQUEST_INFO, "Mdms": {"tenantId": TENANT_ID, "schemaCode": SCHEMA, "data": data, "isActive": True}}
        if post(f"{MDMS_URL}/egov-mdms-service/v2/_create/{SCHEMA}", body) is None:
            failures += 1
        else:
            kept_ids.add(next_id)
            new_messages.append({"code": f"SERVICEDEFS.{row['serviceCode'].upper()}", "message": row["name"],
                                 "module": "rainmaker-im", "locale": "en_IN"})
        next_id += 1

    others = [r for r in records
              if r["data"].get("id") not in kept_ids and r["data"].get("channel") != "HF"
              and r.get("isActive") and r["data"].get("active") and not r["data"].get("deprecated")]
    print(f"CRM entries still offered but not in the file: {len(others)}")
    for record in sorted(others, key=lambda r: (str(r["data"].get("menuPath")), r["data"].get("id"))):
        data = record["data"]
        label = f"{data['id']} {data.get('menuPath')}/{data.get('serviceCode')}"
        if DEPRECATE_OTHERS:
            if not update(record, {"deprecated": True}, label):
                failures += 1
        else:
            print(f"  kept as is (use --deprecate-others): {label}")

    if new_messages and not SKIP_LOCALIZATION:
        print(f"Localization of the {len(new_messages)} created entries")
        for message in new_messages:
            print(f"  {message['code']} = {message['message']}")
        body = {"RequestInfo": REQUEST_INFO, "tenantId": TENANT_ID, "messages": new_messages}
        if post(f"{LOCALIZATION_URL}/localization/messages/v1/_upsert", body) is None:
            failures += 1

    print("Done" if failures == 0 else f"Done with {failures} failure(s)")
    sys.exit(1 if failures else 0)


if __name__ == "__main__":
    main()
