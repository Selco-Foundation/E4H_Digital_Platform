# IM Services Analytics

Analytics service for the Incident Management (IM) module. It computes SLA remaining/breach state for open incident tickets, drives the daily/weekly SLA-breach escalation email workflow, and generates weekly functional-status reports. It reads incident data that is indexed into Elasticsearch by other services (it does not own the incident data). It also hosts a separate, unrelated CO2/carbon-emission calculation batch job that runs on the same deployable.

## Service Dependencies

- **Elasticsearch** — primary data source: `computed-sla-im-services-write` (current incident/SLA index), legacy `im-services` index, `phc-master-list-new-2` (health facility master), and CO2 indices (see Configuration).
- **PostgreSQL** — read-only JDBC queries against incident aggregation tables via `IncidentRepository`/`IncidentQueryBuilder` (no Flyway-managed schema owned by this service).
- **egov-mdms-service** — escalation recipients/levels, SLA thresholds, active tenants (`EscalationMasterDataService`, `MdmsUtil`).
- **egov-workflow-v2** — read-only process-instance history lookup (`WorkflowService`) used to find the current assignment cycle for a ticket.
- **egov-user**, **egov-hrms** — resolve users/employees by role and boundary for escalation recipients.
- **egov-filestore** — upload/download generated escalation and weekly-report CSVs.
- **egov-notification-mail** (via Kafka) — delivery of escalation/weekly-report emails.
- **egov-localization** — boundary/state display names for CO2 dashboard and reports.
- **facility-service**, **project-service**, **rms-service** — CO2 module: facility bulk-search, project-by-facility lookup, and monthly consumption data (`FacilityRegistryClient`, `ProjectCo2Client`, `RmsConsumptionClient`).
- **egov-tracer** — `TracerConfiguration` imported for distributed tracing/MDC.

## API Endpoints

This is primarily an event/cron-triggered service, but it does expose REST endpoints (base context path `/im-services-analytics`):

| Method | Path | Purpose |
|---|---|---|
| POST | `/v1/computeSLA` | Compute/update SLA remaining and breach status for tickets (`IMAnalyticsController`) |
| GET | `/v1/update_phc` | Trigger a one-off PHC aggregation update script |
| POST | `/v1/test_update_phc` | Test hook that publishes a dummy incident to a Kafka topic |
| POST | `/v1/escalation-emails/daily` | Run the daily SLA-breach escalation email job (`EscalationController`) |
| POST | `/v1/escalation-emails/weekly` | Run the weekly DRE system report email job |
| GET | `/v1/escalation-emails/health` | Health check |
| POST | `/v1/carbon/trigger` | Trigger the CO2/carbon-emission monthly batch calculation (`CarbonEmissionTriggerController`), intended to be called by a Kubernetes CronJob |

The `/computeSLA`, `/daily`, `/weekly`, and `/carbon/trigger` endpoints are designed to be invoked by external schedulers (cron/CronJob), not by end-user clients.

## Events

**Consumed:**
- Configurable topic `kafka.topics.consumer` (default `save-hrms-employee`) — generic `EventListener` (`MessageListener<String,String>`) that forwards the payload to `UpdateService.updateEsDoc(topic, payload)` to (re)index documents into Elasticsearch.
- `kafka.topics.carbon-emission-calculate` (default `carbon-emission-calculate`) — `CarbonEmissionListener` (`@KafkaListener`), consumer group `${spring.kafka.consumer.group-id}-carbon`; triggers `CarbonEmissionBatchService.process(...)` for a given month/year.

**Produced:**
- `KafkaProducerService.sendIncident(topic, incident)` publishes via a raw `KafkaTemplate<String,Object>` (used by the `/test_update_phc` test endpoint; not on the main escalation path).
- `EscalationController` publishes escalation/weekly-report email requests directly via `KafkaTemplate` to `egov.kafka.notification.email.topic` (default `egov.core.notification.email`).
- `EscalationStatusService` publishes success/failure status events to `egov.kafka.escalation.status.topic` (default `escalation-notification-email-status`).
- CO2 batch flow publishes to `kafka.topics.co2-monthly-facility-indexer` and `kafka.topics.co2-monthly-projection-indexer` to index computed CO2 documents.

There is no dedicated "thin producer" wrapper — both `KafkaProducerService` and the controllers/services use `KafkaTemplate` directly.

## Configuration

Key non-secret properties (`src/main/resources/application.properties`); secrets are all env-overridable (`DB_PASSWORD`, `ES_PASSWORD`, etc.) and not reproduced here:

- `server.servlet.context-path=/im-services-analytics`, `server.port=8099`
- `spring.datasource.url` — Postgres connection for read-only JDBC incident queries
- `spring.kafka.consumer.group-id=im-services-analytics-grp`, `kafka.topics.consumer`, `kafka.topics.carbon-emission-calculate`
- `egov.infra.indexer.host`, `egov.indexer.es.host.name` / `.port.no` — Elasticsearch endpoint
- `es.index.computed.sla.im.services=computed-sla-im-services-write` — primary SLA/incident ES index
- `egov.update.index.path=computed-sla-im-services-write/_update/`
- `incident.kafka.update.topic.indexer=save-phc-master-list-indexer`, `php.kafka.topic.indexer=health-facility-index-v0001`
- `egov.mdms.host` / `egov.mdms.search.endpoint` — MDMS
- `egov.workflow.host` / `egov.workflow.processinstance.search.path` — workflow v2 (read-only)
- `egov.user.host`, `egov.hrms.host` — user/employee lookups for escalation recipients
- `egov.filestore.host`, `egov.filestore.baseUrl`, upload/download endpoints — CSV storage
- `egov.kafka.escalation.status.topic`, `egov.kafka.notification.email.topic`
- `kibana.dashboard.url`, `saura.emitra.base.url` — links embedded in report emails
- CO2 module: `co2.es.index.actual`, `co2.es.index.projection`, `co2.growth.rate`, `co2.lifecycle.years`, `co2.batch.facility.size`, `egov.rms.*`, `egov.facility.*`, `egov.project.*`
- Flyway config block is present but fully commented out / disabled — this service does not run migrations.

## Data Model

This service does not own a database schema — no Flyway migrations exist under `src/main/resources/db/migration`. Instead it reads:

- **Elasticsearch indices**: `computed-sla-im-services-write` (current incident/SLA documents, read/updated by `ElasticSearchClient` and `UpdateService`), legacy `im-services` index, `phc-master-list-new-2` (health facility master data), and CO2 indices (`co2-monthly-facility-index-write`, `co2-monthly-projection-facility-index-write`).
- **PostgreSQL (read-only)**: incident status/system-functional aggregation queries built by `IncidentQueryBuilder` and executed via `JdbcTemplate` in `IncidentRepository` — no writes.
- **MDMS master data**: escalation recipients, escalation levels/SLA thresholds, active tenants (via `MdmsUtil` / `EscalationMasterDataService`).
- In-memory domain models under `web/models` (e.g. `Incident`, `EscalationTicket`, `EscalationRecipient`, `WeeklyReportData`) are DTOs constructed from the above sources, not persisted entities.

## Workflow

`WorkflowService` only performs a read: it calls egov-workflow-v2's `_search` endpoint (`egov.workflow.processinstance.search.path`) to fetch process-instance history for an incident and determine the latest "assignment cycle" (from the last `PENDING_FOR_ASSIGNMENT` state onward). It does not create or transition workflow process instances — escalation state changes are represented purely in Elasticsearch (`ElasticsearchEscalationService.updateEscalationsForTickets`) and MDMS-defined escalation levels, not in the workflow engine.

## Local Setup

No `LOCALSETUP.md` exists for this service. Basic build/run steps, based on `pom.xml`:

```bash
# from backend/e4h-services/im-services-analytics
mvn clean install -DskipTests

# run the built jar
java -jar target/im-services-analytics-1.0.0.jar
```

Requires Java 17 and a running Postgres instance, Kafka broker, Elasticsearch cluster, and the dependent eGov services listed above (MDMS, workflow-v2, user, HRMS, filestore) reachable at the hosts configured in `application.properties`. Override secrets/hosts via environment variables (`DB_URL`, `DB_USERNAME`, `DB_PASSWORD`, `ES_PASSWORD`, `RMS_HOST`, `FACILITY_HOST`, `PROJECT_HOST`, `EGOV_LOCALIZATION_HOST`, etc.) rather than editing the properties file directly. The service listens on port `8099` under context path `/im-services-analytics`.
