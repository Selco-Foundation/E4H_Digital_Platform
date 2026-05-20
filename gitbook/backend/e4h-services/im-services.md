# IM Services

## Purpose

IM Services provide incident-management functionality for raising, tracking, updating, and notifying users about complaints or tickets.

## Source location

- Service path: `backend/e4h-services/im-services`
- README: `backend/e4h-services/im-services/README.md`
- Local setup: `backend/e4h-services/im-services/LOCALSETUP.md`
- Changelog: `backend/e4h-services/im-services/CHANGELOG.md`

## Responsibilities

- Creates complaints or tickets.
- Updates complaint details and workflow actions.
- Searches complaints using predefined parameters.
- Sends notifications when complaint status changes.
- Integrates with workflow for status transitions.
- Persists create and update requests through Kafka producer topics.

## Dependencies

The README lists:

- `egov-user`
- `egov-localization`
- `egov-idgen`
- `egov-mdms`
- `egov-persister`
- `egov-notification-sms`
- `egov-notification-mail`
- `egov-hrms`
- `egov-workflow-v2`
- `egov-url-shortening`

## API surface

Base path from README:

- `/im-services/v2/[API endpoint]`

Documented operations:

- `POST /_create`: create or raise a complaint.
- `POST /_update`: update complaint details or perform workflow actions.
- `POST /_search`: search complaints.
- Count/search support.
- Video upload support through filestore.

## Kafka producers

The README documents:

- `save-im-request`: create new complaint.
- `update-im-request`: update existing complaint.

## Operational notes

IM Services are central to RMS ticket generation and user-reported issue flows. When IM contracts change, review RMS payload generation, inbox aggregation, notification behavior, and frontend/mobile ticket views.
