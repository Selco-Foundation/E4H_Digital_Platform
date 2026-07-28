# Field Planner

## Purpose

Field Planner manages the lifecycle of field plans for installation campaigns: creating and searching field plans, linking/unlinking facilities to a plan, and (via a downstream field-plan-activity service) tracking per-facility activity execution and staff assignment. It auto-generates plan names from state/activity/year, enforces cascading update rules on plan dates and geography, and drives email notifications to assigned staff.

## Source location

- Service path: `backend/e4h-services/field-planner`
- README: `backend/e4h-services/field-planner/README.md`
- OpenAPI spec: `backend/e4h-services/field-planner/openapi.json`
- Local setup: `backend/e4h-services/field-planner/LOCALSETUP.md`
- Changelog: `backend/e4h-services/field-planner/CHANGELOG.md`

## Responsibilities

- Creates and updates field plans, auto-generating plan names and enforcing cascading rules on dates/geography.
- Searches field plans with pagination and date-range/change filters.
- Links and unlinks facilities to field plans, individually and in bulk (bulk operations pushed to Kafka and processed asynchronously).
- Tracks per-facility activity execution and staff assignment via the downstream Field Plan Activity Service, including email notifications to assigned staff.

## Documented dependencies

- Idgen Service — ID generation for facility linkage.
- MDMS Service — activities and state-info master data.
- Project Service — project linkage.
- Household Service.
- Facility Service — v1 and v2 search.
- Boundary Service — geography scope validation.
- HRMS Service — employee lookup for assignment emails.
- Field Plan Activity Service — activity assignment search/update and facility-activity creation.
- Workflow Service — configured but not currently invoked from application code.
- Redis — response caching.
- Kafka — persister and notification events.

## API surface

Base path: `/field-planner`, controller path: `/v1/field-plans`. The complete, authoritative endpoint list is in the OpenAPI spec at `backend/e4h-services/field-planner/openapi.json`.

**Field Plan**

- `POST /v1/field-plans/_create` — Create one or more Field Plans.
- `POST /v1/field-plans/_update` — Update one or more existing Field Plans.
- `POST /v1/field-plans/_search` — Search Field Plans.

**Field Plan Facility**

- `POST /v1/field-plans/facility/_create` — Assign a facility to a Field Plan.
- `POST /v1/field-plans/facility/bulk/_create` — Assign facilities to Field Plans in bulk.
- `POST /v1/field-plans/facility/_search` — Search Field Plan - Facility linkages.
- `POST /v1/field-plans/facility/_unassign` — Unassign a facility from a Field Plan.
- `POST /v1/field-plans/facility/bulk/_unassign` — Unassign facilities from Field Plans in bulk.

**Health**

- `GET /health` — Service health check.

## Operational notes

Field planner behavior is likely consumed by mobile scheduled visit and activity flows. When changing planning APIs, review mobile repositories and models related to activity facilities and scheduled visits.
