# Field Planner Activity

Manages field activities carried out at a facility (create/assign/search), the Bill of Materials (BOM) generated for a facility installation, and assignment of activities/staff to facilities. It also drives the facility installation workflow (via egov-workflow-v2), tracks per-facility transactions/comments, and generates BOM PDFs.

## Service Dependencies

- egov-idgen (facility/field-plan ID generation)
- egov-mdms-service (activities, BOM form, tenant, state-info masters)
- field-planner service (`egov.fieldplan.host`, field plan / field-plan-facility search)
- facility-service (v1 and v2 search/update)
- boundary-service
- pdf-service (BOM PDF create/create-and-save)
- egov-workflow-v2 (facility installation workflow transitions/search)
- egov-hrms (employee search)
- vendor/organisation service (org user search/update, organisation search)
- asset-registry (asset search/update)
- asset-amc / AMC scheduler service (asset create, configuration search, visit generation)
- Kafka (persister topic `process-audit-records`, notification email topic)

## API Endpoints

Context path: `/activity`

### Activity (`/v1/activities`)
- `POST /v1/activities/_create` — create activities (bulk)
- `POST /v1/activities/_update` — update activity-facility records
- `POST /v1/activities/_delete` — soft-delete activity-facility records
- `POST /v1/activities/_search` — search activity-facilities, enriched with transactions, comments and workflow process instances
- `POST /v1/activities/_assign-activity` — create activity assignment (link activity/facility to field plan)
- `POST /v1/activities/assignment/_update` — update activity assignment
- `POST /v1/activities/assignment/_search` — search activity assignments
- `POST /v1/activities/_unassign-activity` — unassign an activity assignment
- `POST /v1/activities/_assign-staff` — assign staff to an activity facility
- `POST /v1/activities/workflow/update` — transition workflow state for a single activity facility
- `POST /v1/activities/bulk/workflow/update` — bulk workflow transition for activity facilities
- `POST /v1/activities/staff/v1/_create` — link staff users to an activity facility
- `POST /v1/activities/staff/v1/_update` — update activity-facility staff linkage
- `POST /v1/activities/staff/v1/_delete` — soft-delete activity-facility staff linkage
- `POST /v1/activities/test_update_activity` — dev/debug endpoint that publishes a dummy record to `process-audit-records`

### BOM (`/v1/bom`)
- `POST /v1/bom/_create` — create Bill of Material
- `POST /v1/bom/_update` — update Bill of Material
- `POST /v1/bom/_search` — search Bill of Material
- `POST /v1/bom/_generate_pdf` — generate BOM PDF (returned inline, not persisted)
- `POST /v1/bom/_save_pdf` — generate BOM PDF and save to filestore, returns `filestoreId`

### Health
- `GET /health` — liveness/readiness status

## Events

### Kafka Producers (config key -> topic, from `application.properties`)
- `activity.kafka.create.topic` -> `save-activity-topic`
- `activity.facility.kafka.create.topic` -> `save-activity-facility-topic`
- `activity.facility.kafka.update.topic` -> `update-activity-facility`
- `activity.facility.kafka.delete.topic` -> `delete-activity-facility`
- `activity.assignment.kafka.create.topic` -> `save-activity-assignment-topic`
- `activity.assignment.kafka.update.topic` -> `update-activity-assignment`
- `activity.assignment.kafka.unassign.topic` -> `unassign-activity-assignment-topic`
- `bom.kafka.create.topic` -> `save-bom-topic`
- `bom.kafka.update.topic` -> `update-bom-topic`
- `facility.user.kafka.create.topic` -> `save-facility-user-topic`
- `facility.user.kafka.update.topic` -> `update-facility-user-topic`
- `facility.management.transaction.kafka.create.topic` -> `facility-transaction-create`
- `facility.management.comment.kafka.create.topic` -> `facility-comment-create`
- `persister.kafka.create.topic` -> `process-audit-records` (also consumed back, see below)

### Kafka Consumers
- `activity.assignment.consumer.bulk.create.topic` (`save-activity-assignment-bulk-topic`) — `ActivityAssignmentConsumer.bulkCreate`, bulk-creates activity assignments
- `persister.kafka.create.topic` (`process-audit-records`) — `ActivityAssignmentConsumer.updateActivityFacilityWorkflowStatus`; filters for records tagged with the `activity.facility.kafka.create.topic` name and, on match, drives the newly created activity facility through the workflow (`SCHEDULED` then `ASSIGN_FIELD_STAFF`)
- `activity.facility.consumer.bulk.create.topic` (`save-activity-facility-bulk-topic`) — defined in config; not wired to a `@KafkaListener` in current code (verify before relying on it)

## Configuration

Key non-secret properties (`src/main/resources/application.properties`, bound in `ActivityConfiguration.java`):
- `server.servlet.context-path=/activity`, `server.port=8090`
- `spring.flyway.locations=classpath:/db/migration/main`
- Search/paging: `search.api.limit`, `project.default.offset`, `project.default.limit`, `project.search.max.limit`
- MDMS: `egov.mdms.host`, `egov.mdms.search.endpoint`, `project.mdms.module` (`HCM-PROJECT-TYPES`), `task.mdms.module` (`HCM-TASK-QUANTITY-VALIDATION`)
- Workflow: `egov.workflow.host`, `egov.workflow.transition.path`, `egov.workflow.search.path`, `egov.workflow.business.service` (`FACILITY_INSTALLATION`), `egov.workflow.module.name`
- BOM system-type MDMS keys: `egov.off.grid.single.phase.key`, `egov.off.grid.three.phase.key`, `egov.hybrid.single.phase.key`, `egov.hybrid.three.phase.key`, `egov.dc.system.key`
- Downstream hosts: `egov.fieldplan.host`, `egov.facility.host`, `egov.pdf.host`, `egov.boundary.host`, `egov.hrms.host`, `egov.vendor.user.host`, `egov.asset.host`, `egov.amc.scheduler.host`
- `egov.user.id.validator` — toggles user-id validation against `egov-user` vs `individual` service
- Email: `email.activity.assignment.subject`, `email.activity.assignment.body`

## Database

Flyway migrations: `src/main/resources/db/migration/main/`
- `V20250919180100__bom_create_ddl.sql` — `bom` table (VARCHAR PK, `data`/`additional_details` JSONB)
- `V20250924180100__bom_document_create_ddl.sql` — `bom_document`
- `V20251015163200__activity_facility_transaction_comment_create.sql` — `activity_facility_transaction`, `activity_facility_transaction_comment` (FK to `facility_activities`)
- `V20251017141800__bom_column_add_ddl.sql` — adds `activity_facility_id` to `bom`
- `V20251030135800__activity_facility_assigned_users_ddl.sql` — `activity_facility_users` (isDeleted soft-delete, audit columns)

Other tables referenced by the query builders (`activities`, `facility_activities`, `activity_assignments`, `field_plans`) are **not** created by any migration in this repo — they are pre-existing/shared tables, so this service's Flyway history is incomplete on its own. Verify against the actual DB schema before assuming this migration set is sufficient to stand the service up from scratch.

Conventions: all primary keys are VARCHAR (not UUID); audit columns (`createdBy`/`createdTime`/`lastModifiedBy`/`lastModifiedTime`) and a boolean soft-delete flag (`isDeleted`/`is_active`) are used consistently, enriched via `ActivityEnrichment.java` and related enrichment classes.

## Workflow

`FacilityWorkflowService` wraps egov-workflow-v2: `transitionWorkflow(...)` posts a `ProcessInstanceRequest` (module `Workflow`, business service `FACILITY_INSTALLATION`) to `egov.workflow.transition.path`, and `getProcessInstanceById(...)` fetches process-instance history from `egov.workflow.search.path` for a given activity-facility id (used to enrich search results with status history).

`ActivityAssignmentConsumer` reacts to the persister ack topic (`process-audit-records`): when it sees an ack for a newly created activity facility (matching `activity.facility.kafka.create.topic`), it drives two automatic workflow transitions in sequence — `SCHEDULED`, then `ASSIGN_FIELD_STAFF` — via `activityService.updateBulkActivityFacilityWorkflow(...)`, so a facility becomes visible to field staff on the APK without a manual action.

## Local Setup

See [LOCALSETUP.md](./LOCALSETUP.md)
