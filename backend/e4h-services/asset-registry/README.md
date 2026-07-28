# Asset Registry

Asset Registry is the system of record for physical solar assets (inverters, batteries, panels, and related equipment) installed at facilities. It manages asset creation, search, and lifecycle metadata (warranty, operational status, AMC), and is intended to drive an approval/installation workflow via DIGIT Workflow v2. Several endpoints (bulk create, AMC, workflow update) are scaffolded but not yet implemented — see [Workflow](#workflow) and the endpoint table below.

## Service Dependencies

- **egov-user** — user create/search/update (`egov.user.host`)
- **egov-idgen** — asset ID and document ID generation (`ASSET-[SEQ_ASSET_ID]`, `DOCUMENT-[SEQ_DOCUMENT_ID]`)
- **egov-mdms-service** — master data validation (module `asset-registry`: `AssetTypeSchema`, `BrandSchema`, `SystemSchema`, `WarrantyDurationSchema`, `AssetCountSchema`)
- **egov-workflow-v2** — business service / process instance search and transition (client code, `WorkflowUtil.java`, exists but is not wired into the controller yet)
- **facility-service** — facility validation (`egov.facility.search.path`)
- **field-planner / activity facility service** — activity-facility validation (`egov.activity.facility.search.path`)
- **egov-hrms** — employee search (`egov.hrms.search.endpoint`)
- **egov-url-shortening** — URL shortening for notifications
- **egov-persister** — consumes `save-asset` / `update-asset` Kafka topics and performs the actual DB writes (see `asset-persister.yml`)
- **Postgres** — via Spring JDBC + Flyway
- **Kafka** — producer for asset create/update events; SMS notification topic referenced in config but not observed being published from this service's code

## API Endpoints

All routes are under context path `/asset-registry` (see `server.servlet.context-path`), defined in `V1ApiController.java`.

### Asset
- `POST /v1/asset/_create` — create a single asset (validates via MDMS/facility, generates IDs, pushes to Kafka)
- `POST /v1/asset/_update` — update an existing asset (validates existence, pushes to Kafka)
- `POST /v1/asset/_search` — search assets by tenant, asset ID, type, status, facility, activity facility, operational flag, serial number, brand, model (paginated)
- `POST /v1/asset/bulk/_create` — bulk create assets — **stub, returns HTTP 501 Not Implemented**

### Asset Workflow
- `POST /v1/asset/workflow/{assetID}/_update` — update asset workflow status — **stub, returns HTTP 501 Not Implemented**

### Asset AMC (Annual Maintenance Contract)
- `POST /v1/asset/amc/_create` — create AMC contract — **stub, returns HTTP 501 Not Implemented**
- `POST /v1/asset/amc/_update` — update AMC contract — **stub, returns HTTP 501 Not Implemented**
- `GET /v1/asset/amc/_search` — search AMC records by asset ID / contract number — **stub, returns HTTP 501 Not Implemented**
- `POST /v1/asset/amc/visit/_create` — log an AMC visit — **stub, returns HTTP 501 Not Implemented**
- `POST /v1/asset/amc/visit/{visitID}/_update` — update an AMC visit — **stub, returns HTTP 501 Not Implemented**
- `GET /v1/asset/amc/visit/_search` — search AMC visits by asset/facility/visit date — **stub, returns HTTP 501 Not Implemented**

OpenAPI spec: [`docs/asset-registry/asset-registry-1.0.0.yaml`](../../../docs/asset-registry/asset-registry-1.0.0.yaml) (repo root); a generated `openapi.json` also ships in this service directory.

## Events

Kafka producer (`kafka/Producer.java` wraps `CustomKafkaTemplate`), topics configured via `Configuration.java`:

| Event | Config key | Default topic | Consumed by |
|---|---|---|---|
| Asset created | `asset.create.topic` | `save-asset` | egov-persister (`asset-persister.yml`) — inserts into `asset` and `asset_documents` |
| Asset updated | `asset.update.topic` | `update-asset` | egov-persister (`asset-persister.yml`) — updates `asset` |

A `Consumer.java` component exists with a `@KafkaListener` commented out (topic placeholder `kafka.topics.consumer` / `service-consumer-topic`) — not active.

## Configuration

Non-secret config lives in `src/main/resources/application.properties` and is bound via `config/Configuration.java`:

- `server.servlet.context-path=/asset-registry`, `server.port=8083`
- Flyway: `spring.flyway.locations=classpath:/db/migration/main`, `spring.flyway.enabled=true`
- MDMS: `egov.mdms.host`, `egov.mdms.search.endpoint`
- User: `egov.user.host`, `egov.user.context.path`, `egov.user.create.path`, `egov.user.search.path`, `egov.user.update.path`
- Idgen: `egov.idgen.host`, `egov.idgen.path`
- Workflow: `is.workflow.enabled`, `egov.workflow.host`, `egov.workflow.transition.path`, `egov.workflow.businessservice.search.path`, `egov.workflow.processinstance.search.path`
- Facility: `egov.facility.host`, `egov.facility.search.path`
- Activity facility: `egov.activity.facility.host`, `egov.activity.facility.search.path`
- HRMS: `egov.hrms.host`, `egov.hrms.search.endpoint`
- URL shortener: `egov.url.shortner.host`, `egov.url.shortner.endpoint`
- Kafka topics: `asset.create.topic`, `asset.update.topic`, `egov.sms.notification.topic`
- OpenTelemetry: `otel.service.name=asset-registry`, `otel.traces.exporter=otlp`, `otel.exporter.otlp.endpoint`

Database credentials and Kafka bootstrap servers are also in `application.properties` but are local-dev defaults, not secrets used in deployed environments.

## Database

Flyway migrations: `src/main/resources/db/migration/main/`

- `V20250520141800__asset-service_ddl.sql` — creates `asset` (PK `asset_id VARCHAR`) and `asset_documents` (PK `id VARCHAR`, FK to `asset`)
- `V20250618141800__asset_document_column_add_ddl.sql` — adds `latitude`/`longitude` to `asset_documents`
- `V20250625141800__asset_column_add_ddl.sql` — adds `is_operational` to `asset`
- `V20250702141800__asset_column_update_ddl.sql` — relaxes several `NOT NULL` constraints on `asset`
- `V20251017141800__asset_column_add_ddl.sql` — adds `activity_facility_id` to `asset`
- `V20260109141800__asset_isoperational_update_ddl.sql` — backfills `is_operational=false` where null

Conventions:
- Audit columns `created_by`, `created_time`, `last_modified_by`, `last_modified_time` (epoch millis) on `asset`; `asset_documents` uses `created_by`/`created_time`/`updated_by`/`updated_time`
- Soft delete via `is_active BOOLEAN DEFAULT TRUE` on `asset` (no deletion endpoint currently exposed)
- Workflow status tracked in `wf_status VARCHAR` on `asset`
- Primary keys are `VARCHAR` (idgen-issued IDs), not UUID
- `asset_details` / `additional_details` are `JSONB` for asset-type-specific attributes (inverter/battery/panel details)

## Workflow

`is.workflow.enabled=true` is set in config and a full workflow client (`util/WorkflowUtil.java`) exists — it can fetch business services and call `egov-workflow-v2`'s `_transition` API and populate `wf_status`. However, it is **not called anywhere** in `AssetService` or `V1ApiController`. The `POST /v1/asset/workflow/{assetID}/_update` controller method only logs `"Update asset workflow endpoint called but not implemented"` and returns HTTP 501. Net effect: workflow integration is built but not wired up; `wf_status` is set only via whatever value is passed on asset create/update.

## Local Setup

No `LOCALSETUP.md` exists for this service. Basic build/run (Java 17, Maven, Spring Boot 3.4.5):

1. Ensure Postgres is reachable and update `spring.datasource.*` / `spring.flyway.*` in `src/main/resources/application.properties` if not using local defaults (`localhost:5432/mydb`).
2. Ensure Kafka is reachable (`kafka.config.bootstrap_server_config`, default `localhost:9092`) and that egov-persister is configured with `asset-persister.yml` if you need writes to actually land in the DB.
3. Point the dependent service hosts (`egov.mdms.host`, `egov.facility.host`, `egov.activity.facility.host`, `egov.idgen.host`, `egov.workflow.host`, `egov.hrms.host`, `egov.user.host`) at reachable instances.
4. Build:
   ```
   mvn clean install
   ```
5. Run:
   ```
   mvn spring-boot:run
   ```
   or run the packaged jar:
   ```
   java -jar target/asset_registry-1.0.0.jar
   ```
6. Service listens on port `8083` under context path `/asset-registry`; actuator endpoints are exposed at that same base path (e.g. health check at `/asset-registry/health`, per `management.endpoints.web.base-path`).
