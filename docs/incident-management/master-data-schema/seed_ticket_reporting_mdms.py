"""
Seed MDMS v2 for SEM 4.0 Ticket Reporting (facility user tickets).

Steps:
  1. Incident.Component: creates the schema, then one record per entry of Component.data.json.
  2. Incident.ServiceDefs: checks that the existing schema accepts the fields used by the facility
     user entries (componentCode, channel, order - see ServiceDefs.HF.schema-properties.json), then
     creates one record per entry of ServiceDefs.HF.data.json. Each entry is an observation:
       componentCode -> Incident.Component code, used by the frontend to list a component's observations
       menuPath      -> incidentType stored on the ticket (existing CRM type: INVERTER, PANEL, Theft, ...)
       serviceCode   -> incidentSubType stored on the ticket
       priority      -> the ticket takes the highest priority among its observations
  3. Incident.Observation (replaced by the ServiceDefs entries): every record still active is
     deactivated. Skip it with --keep-observations.

Usage:
    MDMS_URL=http://localhost:8285 AUTH_TOKEN=<token> USER_UUID=<uuid> [USER_ID=<id>] \
        python3 seed_ticket_reporting_mdms.py [--dry-run] [--keep-observations]

AUTH_TOKEN and USER_UUID are required: MDMS stamps the audit details of every schema and record
with that user, and rejects the request when the uuid is missing.

An already existing schema or record (duplicate) is reported and skipped, so the script can be
re-run after a partial failure.
"""
import json
import os
import sys
import urllib.error
import urllib.request

HERE = os.path.dirname(os.path.abspath(__file__))
TENANT_ID = "in"
MDMS_URL = os.environ.get("MDMS_URL", "http://localhost:8285").rstrip("/")
DRY_RUN = "--dry-run" in sys.argv
KEEP_OBSERVATIONS = "--keep-observations" in sys.argv

AUTH_TOKEN = os.environ.get("AUTH_TOKEN", "").strip()
USER_UUID = os.environ.get("USER_UUID", "").strip()
USER_ID = os.environ.get("USER_ID", "").strip()

USER_INFO = {"uuid": USER_UUID, "tenantId": TENANT_ID, "type": "EMPLOYEE"}
if USER_ID:
    USER_INFO["id"] = int(USER_ID) if USER_ID.isdigit() else USER_ID

REQUEST_INFO = {
    "apiId": "Rainmaker",
    "authToken": AUTH_TOKEN,
    "userInfo": USER_INFO,
    "msgId": "seed-ticket-reporting|en_IN",
}

SERVICE_DEFS = "Incident.ServiceDefs"


def load(name):
    with open(os.path.join(HERE, name)) as f:
        return json.load(f)


def call(path, body, read_only=False):
    """POSTs to MDMS. Returns (ok, parsed response or None)."""
    if DRY_RUN and not read_only:
        print(f"  [dry-run] POST {path}")
        return True, None
    request = urllib.request.Request(
        MDMS_URL + path, data=json.dumps(body).encode(), headers={"Content-Type": "application/json"}
    )
    try:
        with urllib.request.urlopen(request) as response:
            return 200 <= response.status < 300, json.loads(response.read() or b"null")
    except urllib.error.HTTPError as e:
        detail = e.read().decode(errors="replace")
        if "DUPLICATE" in detail.upper() or "already exists" in detail.lower():
            print(f"  already exists, skipped: {path}")
            return True, None
        print(f"  FAILED {e.code} {path}: {detail[:300]}")
        return False, None


def create_records(schema_code, records, label):
    failures = 0
    for record in records:
        body = {
            "RequestInfo": REQUEST_INFO,
            "Mdms": {"tenantId": TENANT_ID, "schemaCode": schema_code, "data": record, "isActive": True},
        }
        print(f"  {schema_code} {record[label]}")
        ok, _ = call(f"/egov-mdms-service/v2/_create/{schema_code}", body)
        failures += 0 if ok else 1
    return failures


def seed_component():
    print("Schema Incident.Component")
    ok, _ = call("/egov-mdms-service/schema/v1/_create",
                 {"RequestInfo": REQUEST_INFO, "SchemaDefinition": load("Component.schema.json")})
    if not ok:
        return 1
    return create_records("Incident.Component", load("Component.data.json"), "code")


def service_defs_schema_accepts_hf_fields():
    """The ServiceDefs schema already exists and cannot be updated through the API: when it rejects
    unknown properties, the new ones must be added in the database first."""
    if DRY_RUN:
        return True
    ok, response = call("/egov-mdms-service/schema/v1/_search",
                        {"RequestInfo": REQUEST_INFO, "SchemaDefCriteria": {"tenantId": TENANT_ID, "codes": [SERVICE_DEFS]}},
                        read_only=True)
    schemas = (response or {}).get("SchemaDefinitions") or []
    if not ok or not schemas:
        print(f"  Schema {SERVICE_DEFS} not found for tenant {TENANT_ID}")
        return False
    definition = schemas[0].get("definition") or {}
    missing = [name for name in load("ServiceDefs.HF.schema-properties.json")
               if name not in (definition.get("properties") or {})]
    if missing and definition.get("additionalProperties") is False:
        properties = json.dumps(load("ServiceDefs.HF.schema-properties.json"))
        print(f"  Schema {SERVICE_DEFS} rejects the properties {missing}. Add them first, then re-run:")
        print(f"    UPDATE eg_mdms_schema_definitions SET definition = jsonb_set(definition, '{{properties}}',"
              f" (definition->'properties') || '{properties}'::jsonb)"
              f" WHERE code = '{SERVICE_DEFS}' AND tenantid = '{TENANT_ID}';")
        return False
    if missing:
        print(f"  Schema {SERVICE_DEFS} does not declare {missing} but accepts additional properties - continuing")
    return True


def seed_service_defs():
    print(f"Schema {SERVICE_DEFS} (existing)")
    if not service_defs_schema_accepts_hf_fields():
        return 1
    return create_records(SERVICE_DEFS, load("ServiceDefs.HF.data.json"), "serviceCode")


def deactivate_observations():
    print("Incident.Observation (replaced by the ServiceDefs entries)")
    if DRY_RUN:
        print("  [dry-run] deactivate every active Incident.Observation record")
        return 0
    ok, response = call("/egov-mdms-service/v2/_search",
                        {"RequestInfo": REQUEST_INFO,
                         "MdmsCriteria": {"tenantId": TENANT_ID, "schemaCode": "Incident.Observation", "limit": 500}},
                        read_only=True)
    records = [r for r in ((response or {}).get("mdms") or []) if r.get("isActive")]
    if not records:
        print("  nothing to deactivate")
        return 0
    failures = 0
    for record in records:
        record["isActive"] = False
        print(f"  deactivate {record.get('uniqueIdentifier')}")
        ok, _ = call("/egov-mdms-service/v2/_update/Incident.Observation", {"RequestInfo": REQUEST_INFO, "Mdms": record})
        failures += 0 if ok else 1
    return failures


def main():
    missing = [name for name, value in (("AUTH_TOKEN", AUTH_TOKEN), ("USER_UUID", USER_UUID)) if not value]
    if missing and not DRY_RUN:
        print(f"Missing environment variable(s): {', '.join(missing)} - see the usage at the top of this script")
        sys.exit(2)
    failures = seed_component() + seed_service_defs()
    if not KEEP_OBSERVATIONS:
        failures += deactivate_observations()
    print("Done" if failures == 0 else f"Done with {failures} failure(s)")
    sys.exit(1 if failures else 0)


if __name__ == "__main__":
    main()
