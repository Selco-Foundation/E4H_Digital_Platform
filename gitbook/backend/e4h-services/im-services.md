# IM Services

## Purpose

Incident/ticket management service for the E4H platform, derived from DIGIT's PGR (public grievance redressal) module. It lets citizens/CRM raise and track incidents (device faults, theft, installation issues, etc.) against facilities, drives the ticket through a workflow lifecycle (assignment, resolution, escalation, reopen), and supports bulk/legacy migration paths, video evidence upload, and theft-ticket SMS alerts.

## Source location

- Service path: [`backend/e4h-services/im-services`](https://github.com/Selco-Foundation/E4H_Digital_Platform/tree/add-gitbook-docs/backend/e4h-services/im-services)
- README: [`backend/e4h-services/im-services/README.md`](https://github.com/Selco-Foundation/E4H_Digital_Platform/blob/add-gitbook-docs/backend/e4h-services/im-services/README.md)
- OpenAPI spec: [`backend/e4h-services/im-services/openapi.json`](https://github.com/Selco-Foundation/E4H_Digital_Platform/blob/add-gitbook-docs/backend/e4h-services/im-services/openapi.json)
- Changelog: [`backend/e4h-services/im-services/CHANGELOG.md`](https://github.com/Selco-Foundation/E4H_Digital_Platform/blob/add-gitbook-docs/backend/e4h-services/im-services/CHANGELOG.md)

## Responsibilities

- Creates complaints or tickets.
- Updates complaint details and workflow actions.
- Searches complaints using predefined parameters, with or without workflow enrichment.
- Bulk-updates an incident's boundary code when the tied facility's block/boundary changes.
- Sends notifications when complaint status changes, including theft-ticket SMS alerts to the CRM.
- Integrates with workflow for status transitions, including role-based reassignment.
- Supports legacy/v2 migration update paths and legacy v1 contract transformation.
- Accepts video/image evidence uploads to filestore with async processing.
- Records citizen/complaint-resolver login events.
- Persists create and update requests through Kafka producer topics.

## Dependencies

The README lists:

- `egov-user`
- `egov-idgen`
- `egov-mdms`
- `egov-hrms`
- `egov-workflow-v2`
- `egov-localization`
- `egov-notification-sms`
- `egov-url-shortening`
- `egov-filestore`
- `facility-service`
- `boundary-service`
- `rms-service`
- `egov-persister`

Note: an earlier README revision also listed `egov-notification-mail`; no mail integration exists in current config/code, so it has been dropped from this list.

## API surface

Server context path: `/im-services`. Full machine-readable spec: `backend/e4h-services/im-services/openapi.json`. The list below is the complete endpoint set extracted from that spec.

Note: two legacy files in this repo, `src/main/resources/swagger-contract.yml` and `contractForCodeGen.yml`, describe an older v1 PGR-style contract (`/requests/_search`, `/requests/_create`, `/requests/_update`, no version/mock prefix) that no longer matches the endpoints below. The controllers and `openapi.json` are the source of truth.

### Incidents (`RequestsApiController`)

- `POST /v2/request/_create` — create a new incident
- `POST /v2/request/_search` — search incidents (with workflow enrichment)
- `POST /v2/request/_plainsearch` — search incidents without workflow enrichment
- `POST /v2/request/_update` — update an incident
- `POST /v2/request/_update-boundary-by-facility` — bulk-update incident boundary code by facility
- `POST /v2/request/migration/_update` — update an incident (legacy migration path)
- `POST /v2/request/migration/v2/_update` — migration v2: update theft status
- `POST /v2/request/_count` — count incidents
- `POST /v2/theft-notification` — trigger theft notifications

### Storage (`StorageController`)

- `POST /v2/video/upload` — upload video/file(s) to storage

### User Login Report (`UserLoginReportController`)

- `POST /user/login/_report` — record a user login report

### Migration (`MigrationController`, gated behind `migration.enabled=true`)

- `POST /migration/_transform` — transform legacy v1 service data to v2

### Mock/Dev (`MockController`, not for production use)

- `POST /mock/requests/_create` — canned create response
- `POST /mock/requests/_search` — canned search response
- `POST /mock/requests/_update` — canned update response
- `POST /mock/requests/_test` — live HRMS department lookup for given UUIDs

## Kafka producers

The README documents (via `producer/Producer.java`, tenant-scoped topic names):

- `im.kafka.create.topic` (`save-im-request`): incident create.
- `im.kafka.update.topic` (`update-im-request`): incident update.
- `im.kafka.update.migration.topic` (`update-im-request-migration`): migration update.
- `im.kafka.save.report.topic` (`save-login-report`): login report.
- `im.kafka.migration.persister.topic` (`save-im-request-batch`): batch migration persister.
- `im.kafka.process.video.topic` (`process-im-video-request`): async video/master-file processing.
- `im.save.rms.inactive.topic` / `im.delete.rms.inactive.topic`: RMS inactive-incident bookkeeping.
- `persister.save.transition.wf.topic` / `persister.save.transition.wf.migration.topic`: workflow transition persistence (normal and migration batches).

## Operational notes

IM Services are central to RMS ticket generation and user-reported issue flows. When IM contracts change, review RMS payload generation, inbox aggregation, notification behavior, and frontend/mobile ticket views.
