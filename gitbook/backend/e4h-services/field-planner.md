# Field Planner

## Purpose

The field planner service supports planning field work through project-style structures such as projects, tasks, beneficiaries, and staff assignments.

## Source location

- Service path: `backend/e4h-services/field-planner`
- README: `backend/e4h-services/field-planner/README.md`
- Local setup: `backend/e4h-services/field-planner/LOCALSETUP.md`
- Changelog: `backend/e4h-services/field-planner/CHANGELOG.md`

## Responsibilities

- Supports field planning operations.
- Manages project-like field structures.
- Links staff users to project or field work for a defined time period.
- Supports task creation, updates, search, deletion, and bulk operations as described in the README.

## Documented dependencies

- IDGen service.
- Facility service.
- Household service.
- Product service.

## API surface from README

The README describes `/project` endpoints for:

- Project create, update, and search.
- Project beneficiary create, update, search, delete, and bulk operations.
- Project task create, update, search, delete, and bulk operations.
- Project staff create, update, search, delete, and bulk operations.

## Operational notes

Field planner behavior is likely consumed by mobile scheduled visit and activity flows. When changing planning APIs, review mobile repositories and models related to activity facilities and scheduled visits.
