# Ingestion Service

A FastAPI service (Python, uv-managed) that bulk-loads Excel-based master and transactional data into the E4H platform. Users upload spreadsheets of vendors, boundaries, facilities, projects, AMC configurations, and legacy support tickets; the service validates each row against MDMS schemas and downstream services, then creates or updates records in facility-service, project-service, organization-service, HRMS, and im-services (incidents) via their REST APIs. It also generates the pre-filled Excel templates (with boundary codes, dropdowns, facility QR codes) that these uploads are built from.

## Service Dependencies

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

Direct dependencies:
- PostgreSQL — read-only lookups against `eg_hrms_employee` and `eg_user` (legacy ticket migration, facility QR autologin), via `psycopg2`
- Kafka — see Events below

## API Endpoints

All routes are prefixed with `/ingestion-service`.

**Ingest** (`/ingestion-service/ingest`, `app/api/endpoints/file_ingestion.py`)
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
- `POST /facilitiesValidateData` — validate a facility Excel file before processing
- `POST /fieldPlanfacilitiesValidateData` — validate a Field Plan facility Excel file before processing
- `POST /createFacilityAndUpdateProject` — create facilities from validated rows and attach them to a project
- `POST /createFieldPlanFacility` — create Field Plan facilities from validated rows
- `POST /amcConfigurationValidateData` — validate an AMC configuration Excel file before processing
- `POST /amcConfigurationBulkIngest` — bulk-create AMC configurations from a validated template

**Template** (`/ingestion-service/template`, `app/api/endpoints/template_generation.py`)
- `POST /facilityIngestionTemplateWithData` — generate a facility ingestion template pre-filled with schema, existing facility data, and boundary codes
- `POST /boundaryIngestionTemplate` — generate an empty boundary ingestion template
- `POST /fieldplanFacilityIngestionTemplate` — generate a Field Plan facility ingestion template (also unlinks facilities/activities no longer mapped to the project)
- `POST /facilityIngestion` — generate a facility ingestion template with schema, all boundaries, and vendor codes
- `POST /facilityWithStaff` — generate a facility+staff template for a given parent project
- `POST /facilityWithSupervisors` — generate a facility+supervisor template for a given parent project
- `GET /facilitySelection` — generate a facility selection template scoped to given boundary codes / project
- `POST /facilityQRGeneration` — generate a zip of per-facility login QR codes
- `POST /amcConfigurationTemplate` — generate an AMC configuration template pre-filled with facility asset metadata

**Health** (`/ingestion-service/health`, `app/api/endpoints/health_check.py`)
- `GET /ingestion-service/health` — liveness check (`fastapi_health`, always returns healthy)

## Configuration

Read from environment variables (via `.env` / `python-dotenv`); only key names are listed, no values:

- `MDMS_URL`, `VENDOR_SERVICE_URL`, `PROJECT_SERVICE_URL`, `FIELDPLAN_SERVICE_URL`, `FIELDPLAN_ACTIVITY_SERVICE_URL`, `FACILITY_SERVICE_URL`, `HRMS_SERVICE_URL`, `IM_SERVICES_URL`, `AMC_SCHEDULER_SERVICE_URL`, `LOCALIZATION_SERVICE_URL`, `BOUNDARY_SERVICE_URL` — downstream service base URLs
- `DB_HOST`, `DB_PORT`, `DB_NAME`, `DB_USER`, `DB_PASSWORD` — PostgreSQL connection for the direct read-only queries described above
- `ENVIRONMENT` — selects the active entry (e.g. uat) in `app/config/tenant_creator_mapping.json` and `app/config/user_profiles.json`
- `TIME_OUT` — present in `.env` but not read anywhere in `app/` at the time of writing

## Events

- **Producer**: `app/producer/producer.py` wraps `kafka-python`'s `KafkaProducer`. It is only invoked in one place — `POST /ingestion-service/ingest/projects` — to send an SMS notification event after a Field Plan SPOC user is assigned staff.
- The bootstrap server (`localhost:9092`) and the topic name (`egov.core.notification.sms`) are hardcoded at the call site rather than read from configuration.
- No Kafka consumer exists in this service.
- Facility/vendor/project/incident writes are not published as domain events for a persister to consume; they call the owning service's REST create/search APIs directly (synchronous writes), and one TODO item (below) notes a case where this indirection already causes silent failures downstream.

## Architecture Notes

The platform's language-agnostic layering convention (guardrails §18) is validate → enrich → persist → respond, implemented as Controller/Service/Validator/Enricher/Repository components. This service partially follows that shape and is thinner in places, which is expected for a Python utility service — the guardrails document frames Part III conventions as recommended practice adapted per language, not a strict mandate.

- **Boundary and vendor ingestion** (`app/processor/boundary_data_processor.py`, `vendor_data_processor.py`, plus their `factory/` classes) come closest to the convention: a factory builds a processor per request, and the processor delegates to dedicated validators (`app/ingest/boundary_code_validator.py`, `boundary_hierarchy_validator.py`, `identifier_validator.py`, `pattern_validator.py`, `required_field_validator.py`) before writing output via `excel_data_writer.py` / `boundary_excel_data_loader.py` — a recognizable validate → process → respond split, even without `Enricher`/`Repository`-named classes.
- **Facility, project, and incident ingestion** (`app/api/endpoints/file_ingestion.py`) are thinner: validation, downstream API calls, and response-building are inlined in the route handler function rather than split into separate Service/Validator classes. This is a script-like shape common to data-migration/bulk-upload tools and is acceptable under the guardrails' language-agnostic framing, but it means a single file (`file_ingestion.py`, ~3,300 lines) carries logic that on the Java services would be spread across Controller, Service, and Validator classes.
- **Data access to other services** goes through `app/utils/*_service_client.py`, which functions like a Repository/gateway layer (one client class per owning service) rather than direct DB access — this does align with keeping data access out of the "persist" step being ad hoc.
- **Direct DB reads** (`eg_hrms_employee`, `eg_user`) in the legacy ticket and facility-QR flows bypass any repository abstraction — raw `psycopg2` connections and inline SQL live in the endpoint file.
- **Configuration** is centralized reasonably well (all URLs loaded once from env near the top of each endpoint module), though it is duplicated between `file_ingestion.py` and `template_generation.py` rather than living in one shared config component (guardrails §24 recommends a single config component per service).

## Local Setup

1. Install [uv](https://docs.astral.sh/uv/).
2. Run `uv sync` from this folder (the folder with `uv.lock`).
3. Ensure `.env` has the correct values for the URLs and DB credentials listed under Configuration.
4. Port-forward the relevant downstream services from kubectl.
5. Run `uv run -m app.main` from this folder (serves on `0.0.0.0:8080`).

