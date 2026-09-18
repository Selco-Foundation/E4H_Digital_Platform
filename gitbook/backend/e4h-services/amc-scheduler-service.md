# AMC Scheduler Service

## Purpose

Manages Annual Maintenance Contract (AMC) configurations for facility assets and the scheduled maintenance visits generated from them. It tracks per-asset AMC coverage windows, generates recurring visit schedules against an AMC configuration, and drives each visit through a workflow (assignment, technician OTP verification, completion) integrated with `egov-workflow-v2`.

## Source location

- Service path: [`backend/e4h-services/amc-scheduler-service`](https://github.com/Selco-Foundation/E4H_Digital_Platform/tree/add-gitbook-docs/backend/e4h-services/amc-scheduler-service)
- README: [`backend/e4h-services/amc-scheduler-service/README.md`](https://github.com/Selco-Foundation/E4H_Digital_Platform/blob/add-gitbook-docs/backend/e4h-services/amc-scheduler-service/README.md)
- OpenAPI spec: [`backend/e4h-services/amc-scheduler-service/openapi.json`](https://github.com/Selco-Foundation/E4H_Digital_Platform/blob/add-gitbook-docs/backend/e4h-services/amc-scheduler-service/openapi.json)
- Changelog: [`backend/e4h-services/amc-scheduler-service/CHANGELOG.md`](https://github.com/Selco-Foundation/E4H_Digital_Platform/blob/add-gitbook-docs/backend/e4h-services/amc-scheduler-service/CHANGELOG.md)
- ERD: [`backend/e4h-services/amc-scheduler-service/ERD.md`](https://github.com/Selco-Foundation/E4H_Digital_Platform/blob/add-gitbook-docs/backend/e4h-services/amc-scheduler-service/ERD.md)

## Responsibilities

- Maintains AMC configurations (vendor + facility + asset types + duration/frequency) and links individual assets to a configuration with a coverage window and status (ACTIVE/EXPIRED/UNDER_MAINTENANCE/INACTIVE).
- Generates the recurring set of scheduled maintenance visits for an AMC configuration.
- Drives each scheduled visit through a workflow (assignment, technician OTP verification, visit-report submission, completion) via `egov-workflow-v2`.
- Enriches visit search results with related workflow process-instance history and visit transactions.

## Service dependencies

- Idgen Service (`egov.idgen.host`)
- MDMS Service (`egov.mdms.host`)
- Facility Service (`egov.facility.host`)
- Project Service (`egov.project.host`)
- Household Service (`egov.household.host`)
- Asset Registry (`egov.asset.host`)
- Boundary Service (`egov.boundary.host`)
- Workflow v2 (`egov.workflow.host`)
- HRMS (`egov.hrms.host`)
- Vendor Service (`egov.vendor.host`)
- OTP Service (`egov.otp.host`)
- Encryption Service, for facility POC phone decryption (`egov.enc.host`)

## Configuration

Key non-secret properties, all in `src/main/resources/application.properties`, most surfaced via `org.egov.amc.config.AMCServiceConfiguration`:

- `server.servlet.context-path=/asset-amc`, `server.port=8095`
- Flyway: `spring.flyway.locations=classpath:/db/migration/main`
- MDMS: `egov.mdms.host`, `egov.mdms.search.endpoint`, `egov.mdms.master.name`, `egov.mdms.module.name`
- Workflow: `egov.workflow.host`, `egov.workflow.transition.path`, `egov.workflow.search.path`, `egov.workflow.module.name`, `egov.workflow.business.service=AMC_VISIT`
- OTP: `egov.otp.host`, `egov.otp.create.url`, `egov.otp.validate.url`, `egov.otp.bypass.validation`, `amc.otp.sms.message.template`
- Search limits: `search.api.limit`, `project.default.offset`, `project.default.limit`, `project.search.max.limit`
- Boundary: `egov.boundary.hierarchy.type=SELCO`

## Database

Flyway migrations: `src/main/resources/db/migration/main/` — currently `V20251114180100__amc_create_ddl.sql`, `V20251203180100__amc_update_ddl.sql`, `V20260424120000__add_facility_name_to_scheduled_visits.sql`.

Key tables:
- `amc_configuration` — AMC terms per tenant/facility/project/vendor (unique on that tuple), duration & visit frequency, coverage window, status.
- `amc_configuration_assignments` — users assigned to an AMC configuration.
- `asset_amc` — per-asset AMC coverage linked to an `amc_configuration`, with status (ACTIVE/EXPIRED/UNDER_MAINTENANCE/INACTIVE) and legacy-asset flag.
- `scheduled_visits` — generated visits per `amc_configuration`, unique per `(amc_configuration_id, visit_number)`, holds `visit_report` JSONB and status.
- `scheduled_visit_assignments` — users assigned to a scheduled visit.
- `visit_transaction` — workflow process-instance / visit-report history per visit.

All tables use `created_by` / `created_time` / `last_modified_by` / `last_modified_time` audit columns. There is no `is_deleted` column on these tables — soft delete is not implemented at the DDL level. Primary keys are `VARCHAR`, not `UUID`.

## Workflow

`VisitWorkflowService` integrates scheduled visits with `egov-workflow-v2`:
- `transitionWorkflow(...)` builds a `ProcessInstance` (business ID = visit ID, module = `egov.workflow.module.name`, business service = `egov.workflow.business.service` = `AMC_VISIT`) and posts it to `egov.workflow.host` + `egov.workflow.transition.path`.
- `getProcessInstanceById(...)` queries `egov.workflow.search.path` for the process-instance history of a visit, used to enrich search results.
- `POST /v1/visit/workflow/_update` submits a visit report and drives the workflow transition; `POST /v1/visit/_search` enriches each result with its `ProcessInstance` history and linked `visit_transaction` rows.

## API surface

Base path: `/asset-amc` (`server.servlet.context-path`). Authentication is not via an `Authorization` header — every request carries a `RequestInfo` object, and the Bearer JWT from egov-user's login goes in `RequestInfo.authToken` (with `RequestInfo.userInfo`) on every call.

Full spec: `backend/e4h-services/amc-scheduler-service/openapi.json`.

### AMC Configuration — `/v1/configuration`
- `POST /v1/configuration/_create` — Create AMC configuration(s). At least one `AmcConfiguration` entry is required.
- `POST /v1/configuration/_update` — Update one or more existing AMC configurations identified by their id, including assignments.
- `POST /v1/configuration/_search` — Search AMC configurations by tenantId, ids, vendorIds, facilityIds, projectIds, statuses, activeOnDate, date range, assignedUsers and createdBy, with pagination.

### Asset AMC — `/v1/asset`
- `POST /v1/asset/_create` — Create Asset-AMC records; capture details of asset(s) covered under an AMC configuration. At least one entry is required.
- `POST /v1/asset/_update` — Update one or more existing Asset-AMC records identified by their id.
- `POST /v1/asset/_search` — Search Asset-AMC records by tenantId, ids, assetIds, amcConfigurationIds, statuses and date ranges, with pagination.

### Scheduled Visit — `/v1/visit`
- `POST /v1/visit/_create` — Create scheduled visit(s). At least one `ScheduledVisit` entry is required.
- `POST /v1/visit/configuration/_generate` — Generate `ScheduledVisit` records for an AMC configuration based on its duration and visit frequency, optionally scoped to a generation date window and optionally regenerating existing visits.
- `POST /v1/visit/_update` — Bulk-update one or more existing scheduled visits identified by their id.
- `POST /v1/visit/workflow/_update` — Submit/update the visit report for a scheduled visit (e.g. after OTP verification) and apply the supplied workflow action/transition.
- `POST /v1/visit/_search` — Search scheduled visits by tenantId, ids, amcConfigurationIds, facilityIds, projectsIds, statuses, date ranges, visitNumbers and assignedUsers, with pagination; each result is enriched with its workflow `ProcessInstance` history and payment/OTP transactions.
- `POST /v1/visit/_resend_otp` — Regenerate and resend the OTP used to verify visit-report submission for a given visitId, sent to the facility POC's mobile number.
