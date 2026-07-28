# Field Planner

Field Planner manages the lifecycle of field plans for installation campaigns: creating and searching field plans, linking/unlinking facilities to a plan, and (via a downstream field-plan-activity service) tracking per-facility activity execution and staff assignment. It auto-generates plan names from state/activity/year, enforces cascading update rules on plan dates and geography, and drives email notifications to assigned staff.

## Service Dependencies
- Idgen Service (`egov.idgen.host`) - ID generation for facility linkage
- MDMS Service (`egov.mdms.host`) - Activities and StateInfo master data
- Project Service (`egov.project.host`) - project linkage
- Household Service (`egov.household.host`)
- Facility Service (`egov.facility.host`, v1 and v2 search)
- Boundary Service (`egov.boundary.host`) - geography scope validation
- HRMS Service (`egov.hrms.host`) - employee lookup for assignment emails
- Field Plan Activity Service (`egov.fieldplan.activity.host`) - activity assignment search/update and facility-activity creation
- Workflow Service (`egov.workflow.host`) - configured but not currently invoked from application code (see Workflow section)
- Redis - response caching
- Kafka - persister and notification events

## API Endpoints
Base path: `/field-planner`, controller path: `/v1/field-plans`

Field Plan:
- `POST /v1/field-plans/_create` — Create one or more field plans (auto-generates name, enriches audit fields, pushes to Kafka)
- `POST /v1/field-plans/_update` — Update field plan(s); enforces cascading-update rules on dates/geography and triggers facility-activity creation when status becomes `SCHEDULED`
- `POST /v1/field-plans/_search` — Search field plans with pagination, date range and `includeDeleted`/`lastChangedSince` filters

Field Plan Facility:
- `POST /v1/field-plans/facility/_create` — Link a facility to a field plan (synchronous)
- `POST /v1/field-plans/facility/bulk/_create` — Bulk link facilities to a field plan (pushed to Kafka, consumed asynchronously)
- `POST /v1/field-plans/facility/_search` — Search field-plan-to-facility links
- `POST /v1/field-plans/facility/_unassign` — Unassign (soft-delete) a single facility link (synchronous)
- `POST /v1/field-plans/facility/bulk/_unassign` — Bulk unassign facility links (pushed to Kafka, consumed asynchronously)

Health:
- `GET /health` — Liveness/readiness check

## Events

Producers (topic values from `application.properties`, config key in parens):
- `save-field-plan` (`fieldPlan.management.system.kafka.create.topic`) — field plan create, persisted by a persister listening on this topic
- `update-fieldplan` (`fieldPlan.kafka.update.topic`) — field plan update
- `save-fieldplan-facility-topic` (`fieldPlan.facility.kafka.create.topic`) — single facility link create
- `delete-fieldplan-facility-topic` (`fieldPlan.facility.kafka.unassign.topic`) — single facility link unassign
- `egov.core.notification.email` (`egov.kafka.notification.email.topic`) — activity assignment email notifications

Consumers (`FieldPlanFacilityConsumer`):
- `save-fieldplan-facility-bulk-topic` (`fieldPlan.facility.consumer.bulk.create.topic`) — bulk facility link create
- `delete-fieldplan-facility-bulk-topic` (`fieldPlan.facility.consumer.bulk.unassign.topic`) — bulk facility link unassign

Note: `FieldPlannerConsumer.java` exists as an empty `@Component` with no `@KafkaListener` methods — dead scaffold, no active listeners.

## Configuration
Defined in `FieldPlannerConfiguration.java`, sourced from `src/main/resources/application.properties` (non-secret keys only):
- Service hosts/paths: `egov.product.host`, `egov.household.host`, `egov.project.host`, `egov.search.project.url`, `egov.facility.host`, `egov.search.facility.url`, `egov.v2.search.facility.url`, `egov.fieldplan.activity.host` + activity search/update/create URLs, `egov.hrms.host`, `egov.hrms.search.url`, `egov.mdms.host`, `egov.mdms.search.endpoint`, `egov.boundary.host`, `egov.boundary.search.url`
- Pagination: `project.search.max.limit`, `project.default.offset`, `project.default.limit`, `search.api.limit`
- Idgen format: `fieldplan.facility.idgen.id.format`
- Validation: `egov.user.id.validator`, `project.document.id.verification.required`, `egov.location.hierarchy.type`
- Workflow (present but unused by app code today): `egov.workflow.host`, `egov.workflow.transition.path`, `egov.workflow.search.path`, `egov.workflow.module.name`, `egov.workflow.business.service`
- Email template keys: `email.activity.assignment.subject`, `email.activity.assignment.body` (default password key exists but the value must be treated as a secret and rotated outside this repo)
- Kafka topics: see Events section above

## Database
Flyway migrations live under `src/main/resources/db/migration/main` (`spring.flyway.locations=classpath:/db/migration/main`; disabled by default via `spring.flyway.enabled=false` in local `application.properties`).

Key tables (`V20250901180100__fieldPlanner_create_ddl.sql` plus follow-on migrations):
- `field_plans` — plan header: `project_id`, `start_date`/`end_date` (with `valid_date_range` check), `geography_scope`, `selected_activities`, `status`, `created_by`
- `field_plan_facilities` — links a field plan to a facility (unique on `tenantid`+`field_plan_id`+`facility_id`)
- `activities` — master list of field activities (`code`, `default_conditions`, `required_roles`, `sequence_order`); seeded via `V20260331120000__insert_installation_activity.sql`
- `activity_assignments` — assigns an activity/role to a staff member per field plan (`role` jsonb, `emailsent`, `poc_number`)
- `facility_activities` — per-facility execution state for an activity within a field plan (`status`: SCHEDULED/ACTIVE/COMPLETED/CANCELLED, `conditions_met`)

Conventions: VARCHAR primary keys (not UUID type), `created_time`/`last_modified_time` as epoch-millis BIGINT, `additional_details` JSONB catch-all, soft delete via `isdeleted` boolean (column was renamed from `is_deleted` to `isdeleted` on `field_plan_facilities` in `V20250924180100`, so naming is inconsistent across tables).

## Workflow
`egov.workflow.host`, `egov.workflow.transition.path`, `egov.workflow.search.path`, `egov.workflow.module.name` and `egov.workflow.business.service` (`FACILITY_INSTALLATION`) are defined in `FieldPlannerConfiguration.java` and `application.properties`, but no class in this service currently calls the workflow-v2 `_transition`/`_search` endpoints — there is no dedicated `WorkflowService`, and `field_plans.status`/`facility_activities.status` are set directly by application code (e.g. `SCHEDULED`, `ACTIVE`, `COMPLETED`) rather than through workflow transitions.

## Local Setup
See [LOCALSETUP.md](./LOCALSETUP.md)
