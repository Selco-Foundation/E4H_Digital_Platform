"""
Upsert the localization messages of the facility user Incident.ServiceDefs entries (SEM 4.0 Ticket
Reporting): one message per entry of ServiceDefs.HF.data.json, with
    code    = SERVICEDEFS.<serviceCode>
    message = name
plus the issue types they introduce (NEW_ISSUE_TYPES), e.g. SERVICEDEFS.CALL BACK.

Usage:
    LOCALIZATION_URL=http://localhost:8088 AUTH_TOKEN=<token> USER_UUID=<uuid> [USER_ID=<id>] \
        python3 seed_servicedefs_localization.py [--dry-run]

Options / environment:
    --dry-run   prints the messages without calling the localization service
    LOCALE      default en_IN
    MODULE      default rainmaker-im

The upsert creates missing messages and overwrites the text of existing ones, so the script can be re-run.
"""
import json
import os
import sys
import urllib.error
import urllib.request

HERE = os.path.dirname(os.path.abspath(__file__))
TENANT_ID = "in"
LOCALIZATION_URL = os.environ.get("LOCALIZATION_URL", "http://localhost:8088").rstrip("/")
LOCALE = os.environ.get("LOCALE", "en_IN")
MODULE = os.environ.get("MODULE", "rainmaker-im")
DRY_RUN = "--dry-run" in sys.argv

AUTH_TOKEN = os.environ.get("AUTH_TOKEN", "").strip()
USER_UUID = os.environ.get("USER_UUID", "").strip()
USER_ID = os.environ.get("USER_ID", "").strip()

USER_INFO = {"uuid": USER_UUID, "tenantId": TENANT_ID, "type": "EMPLOYEE"}
if USER_ID:
    USER_INFO["id"] = int(USER_ID) if USER_ID.isdigit() else USER_ID


# Issue types (menuPath) introduced by the HF entries: the ticket type is shown as
# SERVICEDEFS.<incidentType upper-cased> (LocalizationService). Every other HF menuPath (INVERTER,
# PANEL, Theft, Uninstall, ...) is an existing CRM type, already localized.
NEW_ISSUE_TYPES = {"Call Back": "Call Back"}


def message(code, text):
    return {"code": code, "message": text, "module": MODULE, "locale": LOCALE}


def build_messages():
    with open(os.path.join(HERE, "ServiceDefs.HF.data.json")) as f:
        service_defs = json.load(f)
    messages = [message(f"SERVICEDEFS.{entry['serviceCode'].upper()}", entry["name"].strip()) for entry in service_defs]
    messages += [message(f"SERVICEDEFS.{menu_path.upper()}", text) for menu_path, text in NEW_ISSUE_TYPES.items()]
    return messages


def main():
    messages = build_messages()
    for message in messages:
        print(f"  {message['code']} = {message['message']}")
    if DRY_RUN:
        print(f"[dry-run] {len(messages)} message(s), module {MODULE}, locale {LOCALE}")
        return

    missing = [name for name, value in (("AUTH_TOKEN", AUTH_TOKEN), ("USER_UUID", USER_UUID)) if not value]
    if missing:
        print(f"Missing environment variable(s): {', '.join(missing)} - see the usage at the top of this script")
        sys.exit(2)

    body = {
        "RequestInfo": {"apiId": "Rainmaker", "authToken": AUTH_TOKEN, "userInfo": USER_INFO},
        "tenantId": TENANT_ID,
        "messages": messages,
    }
    request = urllib.request.Request(
        LOCALIZATION_URL + "/localization/messages/v1/_upsert",
        data=json.dumps(body).encode(),
        headers={"Content-Type": "application/json"},
    )
    try:
        with urllib.request.urlopen(request) as response:
            saved = (json.loads(response.read() or b"{}").get("messages") or [])
            print(f"Done: {len(saved)} message(s) upserted, module {MODULE}, locale {LOCALE}")
    except urllib.error.HTTPError as e:
        print(f"FAILED {e.code}: {e.read().decode(errors='replace')[:500]}")
        sys.exit(1)


if __name__ == "__main__":
    main()
