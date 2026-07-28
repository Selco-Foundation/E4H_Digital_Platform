# IM Services Analytics

## Purpose

IM Services Analytics is the analytics service for the Incident Management (IM) module. It computes SLA remaining/breach state for open incident tickets, drives the daily/weekly SLA-breach escalation email workflow, and generates weekly functional-status reports. It reads incident data that is indexed into Elasticsearch by other services — it does not own the incident data. It also hosts a separate, unrelated CO2/carbon-emission calculation batch job that runs on the same deployable.

## Source location

- Service path: `backend/e4h-services/im-services-analytics`
- README: `backend/e4h-services/im-services-analytics/README.md`
- Build file: `backend/e4h-services/im-services-analytics/pom.xml`
- OpenAPI spec: `backend/e4h-services/im-services-analytics/openapi.json`

## Responsibilities

- Computes/updates SLA remaining and breach status for incident tickets (`PrioritySLAService`).
- Runs the daily and weekly SLA-breach escalation email jobs, including recipient resolution against MDMS, per-level CSV generation/upload to filestore, and email dispatch via Kafka.
- Generates weekly functional-status (DRE) reports.
- Runs a one-off PHC (Primary Health Center) status aggregation script.
- Hosts a separate CO2/carbon-emission monthly batch calculation, triggered via HTTP (for Kubernetes CronJob use) or Kafka.
- Is primarily an event/cron-triggered service, but also exposes REST endpoints under context path `/im-services-analytics`.

## API surface

All endpoints are served under the `/im-services-analytics` context path. Full request/response schemas are in the OpenAPI spec at `backend/e4h-services/im-services-analytics/openapi.json`.

IM Analytics:

- `POST /im-services-analytics/v1/computeSLA`: computes/updates SLA remaining and breach status for tickets in the tenant given in the request body; optional `transform` and `closedtickets` query params control whether records are re-mapped and whether closed tickets are included.
- `GET /im-services-analytics/v1/update_phc`: triggers a one-off PHC (Primary Health Center) status aggregation script.
- `POST /im-services-analytics/v1/test_update_phc`: test/utility endpoint that publishes a supplied incident onto the `process-audit-records` Kafka topic to exercise the incident-audit indexing pipeline.

Carbon Emission:

- `POST /im-services-analytics/v1/carbon/trigger`: triggers the CO2/carbon-emission monthly batch calculation for a tenant/month/year (all optional query params — defaults to the last completed month and the configured default tenant); intended to be called by a Kubernetes CronJob.

Escalation:

- `POST /im-services-analytics/v1/escalation-emails/daily`: runs the daily SLA-breach escalation email job — resolves recipients from MDMS, queries breach tickets per escalation level, uploads per-level CSVs to filestore, updates Elasticsearch escalation state, and publishes escalation emails via Kafka.
- `POST /im-services-analytics/v1/escalation-emails/weekly`: runs the weekly SLA/DRE system report email job — builds one consolidated CSV across mapped states and sends one consolidated report email per recipient via Kafka.
- `GET /im-services-analytics/v1/escalation-emails/health`: liveness check for the escalation email endpoints; always returns HTTP 200 with a fixed plain-text body.

## Runtime notes

The service listens on port `8099` under context path `/im-services-analytics`. Endpoints such as `/computeSLA`, the escalation-email endpoints, and `/carbon/trigger` are designed to be invoked by external schedulers (cron/CronJob), not by end-user clients. None of the endpoints enforce authentication themselves — where a `RequestInfo` is present in the request body, it is forwarded downstream (egov-user/HRMS, MDMS, workflow, filestore) for those services to authenticate/authorize.

## Operational notes

Use this service for dashboard, reporting, or analytics flows related to incident management — SLA breach status, escalation emails, and weekly functional-status reports. Keep reporting assumptions aligned with IM Services status, workflow, and ticket data. This service does not own a database schema; it reads Elasticsearch indices, read-only Postgres aggregation queries, and MDMS master data maintained elsewhere.
