# Ingestion Service

## Purpose

A FastAPI service (Python, uv-managed) that bulk-loads Excel-based master and transactional data into the E4H platform. Users upload spreadsheets of vendors, boundaries, facilities, projects, AMC configurations, and legacy support tickets; the service validates each row against MDMS schemas and downstream services, then creates or updates records in facility-service, project-service, organization-service, HRMS, and im-services (incidents) via their REST APIs. It also generates the pre-filled Excel templates (with boundary codes, dropdowns, facility QR codes) that these uploads are built from.

## Source location

- Service path: `backend/e4h-services/ingestion-service`
- README: `backend/e4h-services/ingestion-service/README.md`
- TODO: `backend/e4h-services/ingestion-service/TODO.md`
- OpenAPI spec: `backend/e4h-services/ingestion-service/openapi.json`

## Responsibilities

- Runs ingestion processes for asset-management workflows.
- Uses repository schemas and sequence diagrams for ingestion contracts.
- Connects to relevant services during execution.

## Service dependencies

Called synchronously (HTTP) via thin client wrappers in `app/utils/`:

- MDMS (`mdms_client.py`) — schema/column definitions, master data lookups (tenants, blocks)
- Facility service (`facility_service_client.py`) — facility search/create, bulk search by boundary
- Organization/vendor service (`organization_service_client.py`) — vendor creation, vendor code lookups
- Project service (`project_service_client.py`) — project/work-stream/staff creation, project-facility linking, workflow transitions
- HRMS (`hrms_service_client.py`) — employee/user search and creation
- Boundary service (`boundary_service_client.py`) — boundary code verification/search
- Localization service (`localization_service_client.py`) — resolving boundary codes from State/District/Block names
- Field Plan service (`fieldplan_service_client.py`) and Field Plan Activity service (`fieldplan_activity_service_client.py`) — field-plan facility linking/unlinking and activity cleanup
- AMC Scheduler service (`amc_scheduler_service_client.py`) — reading/writing AMC configurations
- IM services (called directly via `requests` in `file_ingestion.py`) — legacy incident/ticket creation

Direct dependencies: PostgreSQL (read-only lookups against `eg_hrms_employee` and `eg_user` for legacy ticket migration and facility QR autologin, via `psycopg2`) and Kafka (see Events below).

## Runtime and setup

The README describes the service as a `uv` managed Python project.

Typical run flow:

1. Install `uv`.
2. Run `uv sync` from the project root that contains `uv.lock`.
3. Ensure `.env` contains the correct values.
4. Port-forward relevant services from Kubernetes.
5. Run `uv run -m app.main` from the root folder.

## Schema references

- Boundary ingestion schema: `docs/ingestion/schema/BoundaryIngestionSchema.json`
- Facility ingestion schema: `docs/ingestion/schema/FacilityIngestionSchema.json`
- Vendor ingestion schema: `docs/ingestion/schema/VendorIngestionSchema.json`

## Sequence diagrams

- Boundary ingestion: `docs/ingestion/sequence-diagrams/boundary-ingestion-seq.txt`
- Facility ingestion: `docs/ingestion/sequence-diagrams/facility-ingestion-seq.txt`
- Vendor ingestion: `docs/ingestion/sequence-diagrams/vendor-ingestion-seq.txt`

## API surface

All routes are prefixed with `/ingestion-service`. The full, authoritative spec (request/response schemas, auth requirements per endpoint) is at `backend/e4h-services/ingestion-service/openapi.json`.

### Ingest (`/ingestion-service/ingest`)

- `POST /vendors` — upload and process a vendor + boundary Excel workbook, creating vendors in the organization service
- `POST /boundaries` — upload and process a boundary Excel sheet
- `POST /addFacilitiesValidateData` — validate a bulk "add facility" Excel file before processing
- `POST /facilities` — upload and process a facility Excel file, bulk-creating facilities
- `POST /workStreamWithFacilities` — create a work-stream project from a Field Plan project and link its facilities
- `POST /facilityWithStaff` — upload facility+staff Excel, creating staff users and project-staff links
- `POST /facilityWithSupervisors` — upload facility+supervisor Excel, creating supervisor users and project-staff links
- `POST /facilityWithSupervisorUpdateWorkflowState` — same as above plus advancing the work-stream workflow state
- `POST /projects` — upload and process a project Excel file, creating projects (and SPOC users/SMS notification for Field Plan projects)
- `POST /facilitySelection` — link selected facilities (from a facility-selection workbook) to a project
- `POST /legacy_ticket_ingestion` — migrate legacy support tickets into im-services incidents
- `POST /check_duplicates` — flag duplicate rows in a legacy ticket workbook against the database
- `POST /flag_for_qc` — flag facility rows for quality-control review
- `POST /incidents/dataUpdate` — bulk-update incident data from an Excel file
- `POST /facilitiesValidateData` — validate a facility Excel file before processing (project-scoped)
- `POST /fieldPlanfacilitiesValidateData` — validate a Field Plan facility Excel file before processing
- `POST /createFacilityAndUpdateProject` — create facilities from validated rows and attach them to a project
- `POST /createFieldPlanFacility` — create Field Plan facilities from validated rows
- `POST /amcConfigurationValidateData` — validate an AMC configuration Excel file before processing
- `POST /amcConfigurationBulkIngest` — bulk-create AMC configurations from a validated template

### Template (`/ingestion-service/template`)

- `POST /facilityIngestionTemplateWithData` — generate a facility ingestion template pre-filled with schema, existing facility data, and boundary codes
- `POST /boundaryIngestionTemplate` — generate an empty boundary ingestion template
- `POST /fieldplanFacilityIngestionTemplate` — generate a Field Plan facility ingestion template (also unlinks facilities/activities no longer mapped to the project)
- `POST /facilityIngestion` — generate a facility ingestion template with schema, all boundaries, and vendor codes
- `POST /facilityWithStaff` — generate a facility+staff template for a given parent project
- `POST /facilityWithSupervisors` — generate a facility+supervisor template for a given parent project
- `GET /facilitySelection` — generate a facility selection template scoped to given boundary codes / project
- `POST /facilityQRGeneration` — generate a zip of per-facility login QR codes
- `POST /amcConfigurationTemplate` — generate an AMC configuration template pre-filled with facility asset metadata

### Health (`/ingestion-service/health`)

- `GET /ingestion-service/health` — liveness check (`fastapi_health`, always returns healthy)

## Events

- **Producer**: `app/producer/producer.py` wraps `kafka-python`'s `KafkaProducer`. It is only invoked in one place — `POST /ingestion-service/ingest/projects` — to send an SMS notification event after a Field Plan SPOC user is assigned staff.
- The bootstrap server (`localhost:9092`) and the topic name (`egov.core.notification.sms`) are hardcoded at the call site rather than read from configuration.
- No Kafka consumer exists in this service.
- Facility/vendor/project/incident writes are not published as domain events for a persister to consume; they call the owning service's REST create/search APIs directly (synchronous writes).

## Operational notes

Ingestion jobs usually require correct environment variables, service access, and schema compatibility. Validate sample payloads against the schema before running against shared environments.
