# Field Planner Activity

## Purpose

The field planner activity service supports activity-level execution for field planning workflows.

## Source location

- Service path: [`backend/e4h-services/field-planner-activity`](https://github.com/Selco-Foundation/E4H_Digital_Platform/tree/add-gitbook-docs/backend/e4h-services/field-planner-activity)
- README: [`backend/e4h-services/field-planner-activity/README.md`](https://github.com/Selco-Foundation/E4H_Digital_Platform/blob/add-gitbook-docs/backend/e4h-services/field-planner-activity/README.md)
- OpenAPI spec: [`backend/e4h-services/field-planner-activity/openapi.json`](https://github.com/Selco-Foundation/E4H_Digital_Platform/blob/add-gitbook-docs/backend/e4h-services/field-planner-activity/openapi.json)
- Changelog: [`backend/e4h-services/field-planner-activity/CHANGELOG.md`](https://github.com/Selco-Foundation/E4H_Digital_Platform/blob/add-gitbook-docs/backend/e4h-services/field-planner-activity/CHANGELOG.md)
- ERD: [`backend/e4h-services/field-planner-activity/ERD.md`](https://github.com/Selco-Foundation/E4H_Digital_Platform/blob/add-gitbook-docs/backend/e4h-services/field-planner-activity/ERD.md)

## Responsibilities

- Manages field activities carried out at a facility: creation, assignment, and search of Activity-Facility records (`/v1/activities`).
- Generates and manages Bill of Materials (BOM) for facility installations, including BOM PDF generation and persistence to the filestore (`/v1/bom`).
- Assigns activities and field staff to facilities, and drives the facility installation workflow through egov-workflow-v2 (business service `FACILITY_INSTALLATION`).
- Tracks per-facility transactions and comments, and auto-progresses newly created activity facilities through initial workflow states (`SCHEDULED`, then `ASSIGN_FIELD_STAFF`) via a Kafka consumer on the persister ack topic.
- Provides backend support for the mobile activity-facility, BOM, and workflow flows listed below.

## Related mobile source areas

- `mobile/lib/blocs/activity_facility`
- `mobile/lib/blocs/activity_facility_bom`
- `mobile/lib/blocs/selected_activity_facility`
- `mobile/lib/repositories/activity_facility_repo.dart`
- `mobile/lib/repositories/activity_facility_workflow_repo.dart`

## API surface

Context path: `/activity`. Full request/response schemas are in the OpenAPI spec at `backend/e4h-services/field-planner-activity/openapi.json`.

Activity:
- `POST /v1/activities/_create` — create one or more Activities.

Activity Facility:
- `POST /v1/activities/_update` — update Activity Facility record(s).
- `POST /v1/activities/_delete` — soft-delete Activity Facility record(s).
- `POST /v1/activities/_search` — search Activity Facilities, enriched with transactions, comments, and workflow state.
- `POST /v1/activities/_assign-staff` — assign field staff/vendor to Activity Facility(s).
- `POST /v1/activities/test_update_activity` — debug/test endpoint that publishes an Activity Facility payload onto the internal Kafka audit topic.

Activity Assignment:
- `POST /v1/activities/_assign-activity` — assign Activity/Field-Plan(s) to user(s).
- `POST /v1/activities/assignment/_update` — update Activity Assignment(s).
- `POST /v1/activities/assignment/_search` — search Activity Assignments.
- `POST /v1/activities/_unassign-activity` — unassign Activity Assignment(s).

Facility Workflow:
- `POST /v1/activities/workflow/update` — apply a workflow action (approve/reject/etc.) to a single Activity Facility.
- `POST /v1/activities/bulk/workflow/update` — apply a workflow action in bulk to multiple/all matching Activity Facilities.

Activity Facility Staff:
- `POST /v1/activities/staff/v1/_create` — create Activity Facility Staff linkage(s).
- `POST /v1/activities/staff/v1/_update` — update Activity Facility Staff linkage(s).
- `POST /v1/activities/staff/v1/_delete` — soft-delete Activity Facility Staff linkage(s).

BOM:
- `POST /v1/bom/_create` — create one or more Bill of Material records.
- `POST /v1/bom/_update` — update Bill of Material record(s).
- `POST /v1/bom/_search` — search Bill of Material records.
- `POST /v1/bom/_generate_pdf` — generate a BOM PDF and stream it back.
- `POST /v1/bom/_save_pdf` — generate a BOM PDF and persist it to the filestore.

Health:
- `GET /health` — service liveness/readiness check.

## Operational notes

Use this service page as the activity-planning index. Full request/response schemas and behavioral detail (Kafka events, configuration, database) are in the service README; update this page when the service contract changes.
