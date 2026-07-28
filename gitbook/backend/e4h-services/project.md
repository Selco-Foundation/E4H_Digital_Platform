# Project Service

## Purpose

The project service manages project registry data for health campaign style workflows on the DIGIT platform.

## Source location

- Service path: `backend/e4h-services/project`
- README: `backend/e4h-services/project/README.md`
- Local setup: no dedicated `LOCALSETUP.md`; build and run with Maven (`mvn clean install`, `mvn spring-boot:run`)
- Changelog: `backend/e4h-services/project/CHANGELOG.md`
- OpenAPI spec: `backend/e4h-services/project/openapi.json`

## Responsibilities

- Creates, updates, deletes, and searches projects.
- Manages project beneficiaries.
- Manages project tasks.
- Links project staff users to projects for specific time periods.
- Supports bulk operations for beneficiaries, tasks, and staff.

## Documented dependencies

- IDGen service.
- Facility service.
- Household service.
- Product service.

## API surface

Base path:

- `/project`

Full endpoint list (48 endpoints) extracted from the OpenAPI spec at `backend/e4h-services/project/openapi.json`.

### Project

- POST `/project/v1/_create` — Create projects
- POST `/project/v1/_search` — Search projects (v1)
- POST `/project/v2/_search` — Search projects with workflow status (v2)
- POST `/project/v1/_update` — Update projects
- POST `/project/v1/project/workflow/update` — Update a project's workflow status
- POST `/project/v1/project/bulk/workflow/update` — Bulk approve/update project workflows
- POST `/project/v1/fetchProjectsByFacilities` — Fetch project id/name by facility ids

### Project Beneficiary

- POST `/project/beneficiary/v1/_create` — Create a project beneficiary
- POST `/project/beneficiary/v1/_search` — Search project beneficiaries
- POST `/project/beneficiary/v1/_update` — Update a project beneficiary
- POST `/project/beneficiary/v1/_delete` — Delete a project beneficiary
- POST `/project/beneficiary/v1/bulk/_create` — Bulk create project beneficiaries
- POST `/project/beneficiary/v1/bulk/_update` — Bulk update project beneficiaries
- POST `/project/beneficiary/v1/bulk/_delete` — Bulk delete project beneficiaries

### Project Task

- POST `/project/task/v1/_create` — Create a project task
- POST `/project/task/v1/_search` — Search project tasks
- POST `/project/task/v1/_update` — Update a project task
- POST `/project/task/v1/_delete` — Delete a project task
- POST `/project/task/v1/bulk/_create` — Bulk create project tasks
- POST `/project/task/v1/bulk/_update` — Bulk update project tasks
- POST `/project/task/v1/bulk/_delete` — Bulk delete project tasks

### Project Staff

- POST `/project/staff/v1/_create` — Create a project-staff linkage
- POST `/project/staff/v1/_search` — Search project-staff linkages
- POST `/project/staff/v1/_update` — Update a project-staff linkage
- POST `/project/staff/v1/_delete` — Delete a project-staff linkage
- POST `/project/staff/v1/bulk/_create` — Bulk create project-staff linkages
- POST `/project/staff/v1/bulk/_update` — Bulk update project-staff linkages
- POST `/project/staff/v1/bulk/_delete` — Bulk delete project-staff linkages

### Project Facility

- POST `/project/facility/v1/_create` — Create a project-facility linkage
- POST `/project/facility/v1/_search` — Search project-facility linkages
- POST `/project/facility/v1/_update` — Update a project-facility linkage
- POST `/project/facility/v1/_delete` — Delete a project-facility linkage
- POST `/project/facility/v1/bulk/_create` — Bulk create project-facility linkages
- POST `/project/facility/v1/bulk/_update` — Bulk update project-facility linkages
- POST `/project/facility/v1/bulk/_delete` — Bulk delete project-facility linkages

### Project Resource

- POST `/project/resource/v1/_create` — Create a project resource linkage
- POST `/project/resource/v1/_search` — Search project resource linkages
- POST `/project/resource/v1/_update` — Update a project resource linkage
- POST `/project/resource/v1/_delete` — Delete a project resource linkage
- POST `/project/resource/v1/bulk/_create` — Bulk create project resource linkages
- POST `/project/resource/v1/bulk/_update` — Bulk update project resource linkages
- POST `/project/resource/v1/bulk/_delete` — Bulk delete project resource linkages

### User Action

- POST `/project/user-action/v1/_create` — Bulk create user actions
- POST `/project/user-action/v1/_search` — Search user actions
- POST `/project/user-action/v1/_update` — Bulk update user actions

### Location Capture

- POST `/project/user-location/v1/_create` — Bulk create location-capture records
- POST `/project/user-location/v1/_search` — Search location-capture records

### Bandwidth

- POST `/project/check/bandwidth` — Check client/server bandwidth

The complete, authoritative spec (schemas, request/response bodies, error shapes) lives at `backend/e4h-services/project/openapi.json`.

## API reference

- Project API: `docs/project-service/project-v1.api.yaml`

## Operational notes

Project data can drive field planning, scheduled visits, and staff assignment workflows. Review downstream mobile and frontend flows when project contracts change.
