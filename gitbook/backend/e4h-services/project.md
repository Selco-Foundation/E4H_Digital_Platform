# Project Service

## Purpose

The project service manages project registry data for health campaign style workflows on the DIGIT platform.

## Source location

- Service path: `backend/e4h-services/project`
- README: `backend/e4h-services/project/README.md`
- Local setup: `backend/e4h-services/project/LOCALSETUP.md`
- Changelog: `backend/e4h-services/project/CHANGELOG.md`

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

The README documents endpoints for:

- `/project/v1/_create`, `_update`, `_search`
- `/project/beneficiary/v1/*`
- `/project/task/v1/*`
- `/project/staff/v1/*`
- Bulk create, update, and delete operations for beneficiaries, tasks, and staff.

## API reference

- Project API: `docs/project-service/project-v1.api.yaml`

## Operational notes

Project data can drive field planning, scheduled visits, and staff assignment workflows. Review downstream mobile and frontend flows when project contracts change.
