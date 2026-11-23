# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## What this is

Backend for SELCO Foundation's **Energy for Health (E4H)** platform. It manages solar installations at health facilities across their whole life: facility registration, field assessment and planning, installation, asset tracking, maintenance (AMC), incident/complaint handling ("Saura eMitra"), remote monitoring (RMS), and analytics.

It is built on the **eGov DIGIT** microservice framework. Every service is an independent Spring Boot app with its own `pom.xml`. There is **no parent/aggregator POM**, so each service is built and run on its own.

```
backend/
  core-services/      DIGIT platform services (forked and customised for E4H)
  e4h-services/       E4H domain services
  business-services/  empty (HRMS lives in e4h-services/egov-hrms)
  docs/               Kubernetes CronJob YAMLs, Python cron runners (docs/cron/), API/handover docs
```

## Services

### core-services (DIGIT platform)

| Service | Purpose |
|---|---|
| `egov-user` | User accounts, login/OTP, auth tokens. |
| `egov-accesscontrol` | Role → API action mapping; authorises requests. |
| `egov-workflow-v2` | Generic state machine. Business services define states, actions and the roles allowed for each action. |
| `egov-mdms-service-v2` | Master data (tenants, roles, priorities, categories, escalation config, etc.) served at runtime. |
| `egov-idgen` | Generates formatted business IDs (complaint numbers, asset IDs, …). |
| `egov-filestore` | File/document upload and retrieval. |
| `egov-notification-sms` | Sends SMS from messages on a Kafka topic. |
| `boundary-service` | Administrative boundary hierarchy (state → district → block …) and boundary relationships. |
| `health-facility-registry` | Health facility (PHC/CHC, etc.) master. Handles create/approve/reject, boundary mapping, mapped vendor, assessment data, and Kibana/AMC index sync. |

### DIGIT services used but not in this repo

These run as stock DIGIT deployments. Their code is not here, but services here call them or publish to them.

| Service | Purpose / how it's used here |
|---|---|
| `egov-persister` | Consumes Kafka topics and writes to Postgres using YAML mappings. The mappings for each service live in that service's `src/main/resources` (e.g. `im-services-persister.yml`, `asset-persister.yml`, `facility-persister.yml`, `egov-hrms/.../config/hrms-employee-persister.yml`). Changing a persisted model means changing this YAML. |
| `egov-indexer` | Consumes `*-indexer` Kafka topics and writes to Elasticsearch for search and Kibana dashboards. (`bulk-indexer` in this repo handles high-volume batch indexing separately.) |
| `egov-localization` | Translated UI and notification messages by locale and module. |
| `egov-enc-service` | Encrypts and decrypts PII such as phone numbers. |
| `egov-url-shortening` | Short links used in SMS notifications. |

### e4h-services (E4H domain)

| Service | Purpose |
|---|---|
| `im-services` | **Incident management**: raise, assign, resolve and close complaints on installed systems. Derived from DIGIT PGR. Uses workflow-v2 for state, HRMS for assignees, and handles SLA, notifications, video/media upload and RMS-generated incidents. |
| `im-services-analytics` | Reporting on incidents: daily/weekly escalation emails to state and program roles, CO2 emission calculation, user-login analytics, CSV exports, Kibana dashboards. Reads Postgres and Elasticsearch. Package `org.selco.e4h`. |
| `rms-service` | **Remote Monitoring System**: pulls telemetry (solar, inverter, battery, grid) from vendor RMS APIs, runs anomaly rules, dedupes via `active_alerts`, and auto-creates tickets in im-services. |
| `asset-registry` | Installed assets (equipment) at facilities: create/search/update, bulk create, asset workflow, AMC contracts and AMC visits on assets. |
| `amc-scheduler-service` | Annual Maintenance Contract planning: AMC configuration, AMC plans, scheduled maintenance visits (with OTP confirmation), visit workflow, AMC summary indexing. |
| `field-planner` | Field planning before installation: facility assessments, field plan templates, field plans (create/complete/hand-off), facility assignment to plans, ICC reports. |
| `field-planner-activity` | Execution of a field plan: activities and staff assignment, BOM (bill of materials), activity workflow, installation reports and PDF generation. |
| `project` | DIGIT project registry adapted for E4H: projects and the facilities, staff, resources and beneficiaries attached to them. |
| `vendor-registry` | Organisation registry for vendors/contractors: contact details, tax IDs, areas of work. |
| `egov-hrms` | Employee management (assignments, jurisdictions). Every employee is also created in egov-user. Used to find assignees and role holders. |
| `inbox` | Aggregated, paginated work-list across a module's records and their workflow state (backs the UI inbox). |
| `processor-services` | Kafka consumers for heavy async work, mainly video processing (FFmpeg quality conversion and upload). |
| `bulk-indexer` | Shape-agnostic Kafka → Elasticsearch `_bulk` writer. Producers send final ES documents and name the target index. Package `org.selco.e4h`. |
| `translator-service` | Translates word lists between languages, including from uploaded spreadsheets. Pluggable providers (mock, Gemini). Used for localization. |
| `ingestion-service` | **Python** (FastAPI, uv). Excel-driven bulk ingestion for Asset Management (boundaries, facilities, assets, QR codes). |

### Scheduled jobs

Jobs are not scheduled inside the services. Kubernetes CronJobs (`backend/docs/*-cronjob.yaml`, `rms-*-cron.yaml`) run Python scripts in `backend/docs/cron/`. Each script `POST`s to a service endpoint with a synthetic SYSTEM `RequestInfo`. Jobs: daily/weekly escalation, SLA, RMS rule engine, RMS mapping sync/validate/pause-expiry, visit scheduling, carbon emission, theft notification, user analytics report.

## Build & test

Run Maven per service (from the service directory, or with `-f`):

```bash
mvn -B -f backend/e4h-services/im-services/pom.xml package        # build + tests
mvn -f <service>/pom.xml clean compile -DskipTests
mvn -f <service>/pom.xml test -Dtest=ClassName                     # single test class
mvn -f <service>/pom.xml test -Dtest=ClassName#methodName          # single test method
mvn -f <service>/pom.xml spring-boot:run
```

Java/Spring versions differ per service. Check the service's `pom.xml` before using language or API features:
- Most services: Java 17, Spring Boot 3.2.x–3.4.x (`jakarta.*`).
- **Legacy Java 8 / `javax.*`**: `core-services/egov-user` (Boot 1.5), `egov-workflow-v2`, `egov-idgen`, `egov-notification-sms`, `e4h-services/inbox` (Boot 2.2).
- `translator-service`: Java 21.

Test coverage is uneven. Some services (inbox, egov-hrms, bulk-indexer, mdms-v2) have no tests. Postman collections (in `src/main/resources`, `src/test/resources/postman`, or `postman/`) are the main API test artifacts.

Ingestion service:
```bash
cd backend/e4h-services/ingestion-service
uv sync
uv run -m app.main      # needs .env and kubectl port-forwards
```

## Running locally

Services depend on other DIGIT services and on Postgres and Kafka; some also need Elasticsearch. Follow each service's `LOCALSETUP.md`:
1. Run `kubectl port-forward -n egov <dependency> <port>:8080` for each dependency.
2. In `application.properties`, point the `*.host` properties (`egov.mdms.host`, `egov.user.host`, `egov.workflow.host`, …) and `spring.datasource.url` / `spring.flyway.url` at them.

## Architecture conventions (DIGIT pattern)

- **Request/response envelope**: every API is a `POST` with a `RequestInfo` wrapper (auth token + `userInfo`) and returns `ResponseInfo`. Endpoints are named `_create`, `_update` and `_search`.
- **Layering**: `web/controllers` → `validator` → `service` (enrichment, workflow call, business logic) → `producer` (Kafka). Reads go through `repository` with a `querybuilder` and a `rowmapper`.
- **Writes are async via egov-persister**: services usually don't write to the database directly. They publish to a Kafka topic, and the persister writes using a YAML mapping (e.g. `im-services/src/main/resources/im-services-persister.yml`, topics `save-im-request` / `update-im-request`). `*-indexer` topics feed Elasticsearch. When you add a DB column, update the Flyway migration, persister YAML, row mapper and query builder together.
- **State transitions** go through **egov-workflow-v2** (business service config, e.g. `im-services/src/main/resources/workflowConfig.json`). Roles and actions come from workflow and MDMS. Access control comes from egov-accesscontrol.
- **Master data** comes from **MDMS v2** at runtime. Don't hard-code values that MDMS provides.
- **DB migrations**: Flyway, in `src/main/resources/db/migration/main` (SQL). im-services also has Java migrations in `src/main/java/db/migration/main`. Name files `V<yyyyMMddHHmmss>__desc`. In deployment they run with `outOfOrder=true` from a separate `<service>-db` image (`src/main/resources/db/Dockerfile` + `migrate.sh`), so a new migration must not depend on strict version ordering.
