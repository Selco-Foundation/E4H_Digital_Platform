# AMC Scheduler Service

Manages Annual Maintenance Contract (AMC) configurations for facility assets and the scheduled maintenance visits generated from them. It tracks per-asset AMC coverage windows, generates recurring visit schedules against an AMC configuration, and drives each visit through a workflow (assignment, technician OTP verification, completion) integrated with `egov-workflow-v2`.

## Service Dependencies

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

## API Endpoints

Base path: `/asset-amc` (`server.servlet.context-path`)

### AMC Configuration — `/v1/configuration`
- `POST /v1/configuration/_create` — Create a new AMC configuration (vendor + facility + asset types + duration/frequency).
- `POST /v1/configuration/_update` — Update an existing AMC configuration.
- `POST /v1/configuration/_search` — Search AMC configurations.

### Asset AMC — `/v1/asset`
- `POST /v1/asset/_create` — Link an asset to an AMC configuration and set its coverage window/status.
- `POST /v1/asset/_update` — Update an asset-AMC record.
- `POST /v1/asset/_search` — Search asset-AMC records.

### Scheduled Visit — `/v1/visit`
- `POST /v1/visit/_create` — Create scheduled visit(s).
- `POST /v1/visit/configuration/_generate` — Generate the recurring set of scheduled visits for an AMC configuration.
- `POST /v1/visit/_update` — Bulk update scheduled visits.
- `POST /v1/visit/workflow/_update` — Submit a visit report and transition the visit through workflow.
- `POST /v1/visit/_search` — Search scheduled visits; also enriches results with related workflow process instances and visit transactions.
- `POST /v1/visit/_resend_otp` — Resend the technician completion OTP for a visit.

## Events

No `@KafkaListener` consumers are implemented — `FieldPlannerConsumer` exists in `org.egov.amc.consumer` but is an empty placeholder component with no listener methods. All persistence is published asynchronously through the common `Producer`/`GenericRepository` to:

| Config key | Topic |
|---|---|
| `amc.configuration.create.topic` | `save-amc-configuration` |
| `amc.configuration.update.topic` | `update-amc-configuration` |
| `asset.amc.create.topic` | `save-asset-amc` |
| `asset.amc.update.topic` | `update-asset-amc` |
| `scheduled.visit.create.topic` | `save-scheduled-visit` |
| `scheduled.visit.update.topic` | `update-scheduled-visit` |
| `visit.management.transaction.kafka.create.topic` | `visit-transaction-create` |
| `kafka.topics.notification.sms` | `egov.core.notification.sms` (OTP / completion SMS) |
| `egov.kafka.notification.email.topic` | `egov.core.notification.email` |

Persister mapping: `src/main/resources/amc-persister.yml`.

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

Flyway migrations: `src/main/resources/db/migration/main/` — currently `V20251114180100__amc_create_ddl.sql`, `V20251203180100__amc_update_ddl.sql`, `V20260424120000__add_facility_name_to_scheduled_visits.sql` (3 files; the prior platform assessment referenced "3 more" after the create DDL, but only 2 follow-on migrations exist today).

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
- `ScheduledVisitController#updateScheduledVisit` (`POST /v1/visit/workflow/_update`) submits a visit report and drives the workflow transition; `POST /v1/visit/_search` enriches each result with its `ProcessInstance` history and linked `visit_transaction` rows.

## Local Setup

Basic build/run (Java 17, Maven, Spring Boot 3.2.2):

1. Ensure Postgres is reachable and update `spring.datasource.*` / `spring.flyway.*` in `src/main/resources/application.properties` if not using local defaults (`localhost:5432/mydb`).
2. Ensure Redis is reachable (`spring.redis.host`/`spring.redis.port`, default `localhost:6379`) — used for response caching.
3. Ensure Kafka is reachable (`kafka.config.bootstrap_server_config`, default `localhost:9092`).
4. Point the dependent service hosts at reachable instances: `egov.idgen.host`, `egov.mdms.host`, `egov.household.host`, `egov.facility.host`, `egov.location.host`/`egov.boundary.host` (boundary), `egov.product.host`, `egov.localization.host`, `egov.workflow.host`, `egov.hrms.host`, `egov.asset.host`, `egov.vendor.host`, `egov.otp.host`, `egov.enc.host`.
5. Build:
   ```bash
   mvn clean install
   ```
6. Run:
   ```bash
   mvn spring-boot:run
   ```
   or run the packaged jar:
   ```bash
   java -jar target/amc-service-1.1.6.jar
   ```
7. Service listens on port `8095` under context path `/asset-amc`.
