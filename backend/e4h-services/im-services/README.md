# IM Services

Incident/ticket management service for the E4H platform, derived from DIGIT's PGR (public grievance redressal) module. It lets citizens/CRM raise and track incidents (device faults, theft, installation issues, etc.) against facilities, drives the ticket through a workflow lifecycle (assignment, resolution, escalation, reopen), and supports bulk/legacy migration paths, video evidence upload, and theft-ticket SMS alerts.

## Service Dependencies
- egov-user (auth/user resolution)
- egov-idgen (incident ID generation)
- egov-mdms (business/status master data)
- egov-hrms (employee/role lookup for assignment)
- egov-workflow-v2 (state machine transitions)
- egov-localization
- egov-notification-sms (rate/reopen/theft SMS events)
- egov-url-shortening (SMS links)
- egov-filestore (video/image storage)
- facility-service (facility details/search)
- boundary-service (boundary relationships)
- rms-service (ticket status update webhook)
- egov-persister (workflow transition + migration batch persistence via Kafka)

Note: the previous README also listed `egov-notification-mail`; no mail integration exists in current config/code, so it has been dropped.

## API Endpoints

All incident endpoints are served under `/v2` (context path `/im-services`), implemented in `RequestsApiController`:

- `POST /v2/request/_create` — create a new incident + workflow
- `POST /v2/request/_search` — search incidents (with workflow enrichment)
- `POST /v2/request/_plainsearch` — search incidents without workflow enrichment
- `POST /v2/request/_update` — update an incident (drives workflow transition)
- `POST /v2/request/_update-boundary-by-facility` — bulk-update `boundarycode` on `eg_incident_v2` for all incidents tied to a facility (used when a facility's block/boundary changes)
- `POST /v2/request/migration/_update` — legacy migration-path update
- `POST /v2/request/migration/v2/_update` — v2 migration update (theft status)
- `POST /v2/request/_count` — count incidents matching criteria
- `POST /v2/theft-notification` — scan `PENDINGFORASSIGNMENT_THEFT` tickets past the MDMS-configured threshold and SMS the CRM; callable by cron or manually

Storage (`StorageController`, `/v2/video`):
- `POST /v2/video/upload` — upload video/image files to filestore, kick off async master-file processing via Kafka

User login reporting (`UserLoginReportController`):
- `POST /user/login/_report` — record a citizen/complaint-resolver login event

Migration (`MigrationController`, gated behind `migration.enabled=true`):
- `POST /migration/_transform` — transform legacy v1 PGR contract payloads

Mock/dev-only (`MockController`, not for production use):
- `POST /mock/requests/_create`, `POST /mock/requests/_search`, `POST /mock/requests/_update` — return canned data from bundled `mockData.json`
- `POST /mock/requests/_test` — live HRMS department lookup for given UUIDs

An OpenAPI description reflecting current controller behavior is checked in at `openapi.json` (root of this service). It explicitly notes that `src/main/resources/swagger-contract.yml` and `contractForCodeGen.yml` describe an older v1 PGR-style contract that no longer matches the live `/v2/...` endpoints — treat the controllers/`openapi.json` as the source of truth.

## Events

**Kafka producers** (via `producer/Producer.java`, tenant-scoped topic names):
- `im.kafka.create.topic` (`save-im-request`) — incident create
- `im.kafka.update.topic` (`update-im-request`) — incident update
- `im.kafka.update.migration.topic` (`update-im-request-migration`) — migration update
- `im.kafka.save.report.topic` (`save-login-report`) — login report
- `im.kafka.migration.persister.topic` (`save-im-request-batch`) — batch migration persister
- `im.kafka.process.video.topic` (`process-im-video-request`) — async video/master-file processing
- `im.save.rms.inactive.topic` / `im.delete.rms.inactive.topic` — RMS inactive-incident bookkeeping
- `persister.save.transition.wf.topic` / `persister.save.transition.wf.migration.topic` — workflow transition persistence (normal and migration batches)

**Kafka consumers** (`consumer/NotificationConsumer.java`, `consumer/MigrationConsumer.java`) — correcting the prior "NA":
- `im.kafka.create.topic` + `im.kafka.update.topic` — triggers citizen/employee SMS notifications
- `persister.auto.escalation.topic` (`im-auto-escalation`) — auto-escalation notifications
- `im.kafka.migration.topic` (`im-migration`) — legacy migration ingestion

**Indexer topics** (config keys only; consumed by the egov-indexer, not this service):
- `im.kafka.create.topic.indexer` (`save-im-request-indexer`)
- `im.kafka.update.topic.indexer` (`update-im-request-indexer`)
- `im.audit.kafka.create.topic.indexer` (`save-im-audit-request-indexer`)
- `im.kafka.save.topic.indexer` (`save-user-login-report-indexer`)

## Configuration

Non-secret config lives in `src/main/resources/application.properties`, bound in `config/IMConfiguration.java`. Key properties:

- Business/search: `im.complain.idle.time`, `im.default.offset`, `im.default.limit`, `im.search.max.limit`, `im.business.codes`, `im.business.level.sla`, `citizen.allowed.search.params`, `employee.allowed.search.params`, `workflow.ticket.open.statuses`, `allowed.source`
- Workflow: `is.workflow.enabled`, `egov.workflow.host`, `egov.workflow.transition.path`, `egov.workflow.businessservice.search.path`, `egov.workflow.processinstance.search.path`
- ID generation: `egov.idgen.host`, `egov.idgen.im.IncidentId.name`, `egov.idgen.im.IncidentId.format`
- Downstream hosts: `egov.user.host`, `egov.mdms.host`, `egov.hrms.host`, `egov.localization.host`, `egov.facility.host`, `egov.boundary.host`, `egov.rms.host`, `egov.filestore.host`, `egov.url.shortner.host`
- Notification: `notification.sms.enabled`, `egov.user.event.notification.enabled`, `kafka.topics.notification.sms`, `mseva.mobile.app.download.link`, `egov.im.events.rate.link`, `egov.im.events.reopen.link`
- Theft notification: `im.theft.notification.cron` (default `0 0 9 * * ?`), `im.theft.notification.crm.mobile`, `im.theft.notification.tenantid`
- Video/file handling: `allowed.formats.map`, `video.max.size`, `video.list.size`, `image.max.size`, `file.list.size`, `ffprobe.path`, `ffmpeg.cpulimitpercentage`
- Central-instance/multi-tenant: `state.level.tenantid.length`, `is.environment.central.instance`, `digit.ui.tenant`, `egov.ui.app.host.map`
- Flyway: `flyway.locations=classpath:/db/migration/main`, `flyway.outOfOrder=true`
- Observability: `otel.service.name`, `otel.exporter.otlp.endpoint` (via `TracerConfiguration`)

Migration mode is gated by `migration.enabled` (default `false`), which conditionally enables `MigrationController`.

## Database

Flyway migrations live in `src/main/resources/db/migration/main/` (SQL, 30+ files) plus Java-based data migrations in `src/main/java/db/migration/main/` (used for ES reindex/backfill and cross-table data migrations, e.g. `V20251114123000__update_es_incident_tenant_id_data.java`, `V20260420120000__migrate_login_report_es_hrms_facility.java`).

Key tables:
- `eg_incident_v2` — core incident record (`id` string-format identifier, `varchar(64)`, PK is composite `(tenantId, incidentId)`, `applicationStatus`, `assigner`/`assignee`, `additionalDetails` JSONB, audit columns)
- `eg_incident_address_v2` — incident address (district/block/PHC type/subtype), FK to `eg_incident_v2.id`
- `im_services_priority` — SLA priority lookup used by `SLAService`
- `facility_rms_inactive_incident` — tracks incidents made inactive via RMS
- `facility_tenant_id_map` — facility-to-tenant mapping used in multi-tenant migrations

Audit convention: every write enriches `createdBy`/`createdTime`/`lastModifiedBy`/`lastModifiedTime` via `EnrichmentService` + `IMUtils.getAuditDetails`. There is no soft-delete (`isDeleted`) column on incident tables; deactivation elsewhere (e.g. vendor merges) uses an explicit `is_active` flag instead.

Primary key note: incident/address IDs are `VARCHAR`, not native `UUID` (see Guardrails Compliance below).

## Workflow

`WorkflowService` is injected into `IMService` and drives every state transition:
- `getBusinessService` resolves the applicable workflow business service (`Incident`, or a priority-specific variant `Incident_High`/`Incident_Medium`/`Incident_Low`) by calling `egov-workflow-v2`'s businessservice search.
- `updateWorkflowStatus` (called from `IMService.create`/`update`/`migrationUpdate`) builds a `ProcessInstance` from the incoming `Workflow` payload, calls `egov-workflow-v2`'s `_transition` API, and writes the resulting `applicationStatus` back onto the `Incident`.
- Role-based reassignment (`reassignWorkflow`) resolves the next assignee via HRMS lookup for actions like `RESOLVE`, `REJECT`, `MARK_OUT_OF_SCOPE`, and the out-of-warranty/tech-POC approval path.
- `enrichWorkflow` (used on search) batches process-instance lookups per tenant and attaches the current `Workflow` state to each `IncidentWrapper`.
- `enrichTotalSla` pulls `BusinessHours` from MDMS and computes elapsed/remaining SLA plus first-round resolved/declined timestamps, stored on `IndexView` for the ES index.

## Local Setup

Basic build/run (Java 17, Maven, Spring Boot 3.2.2):

1. Ensure Postgres is reachable and update `spring.datasource.*` / `flyway.*` in `src/main/resources/application.properties` if not using local defaults (`localhost:5432/mydb`).
2. Ensure Kafka is reachable (`kafka.config.bootstrap_server_config`, default `localhost:9092`).
3. Install `ffmpeg`/`ffprobe` and reachable on the host (or set `ffprobe.path`, default `/usr/bin/ffprobe`) — required for incident video/image upload validation.
4. Point the dependent service hosts at reachable instances: `egov.localization.host`, `egov.mdms.host`, `egov.hrms.host`, `egov.facility.host`, `egov.boundary.host`, `egov.user.host`, `egov.idgen.host`, `egov.workflow.host`, `egov.url.shortner.host`, `egov.filestore.host`, `egov.rms.host` (ticket status update webhook).
5. Set `egov.internal.microservice.user.uuid` to a valid system user UUID.
6. Build:
   ```bash
   mvn clean install
   ```
7. Run:
   ```bash
   mvn spring-boot:run
   ```
   or run the packaged jar:
   ```bash
   java -jar target/im-services-1.2.0.jar
   ```
8. Service listens on port `8880` under context path `/im-services`.
