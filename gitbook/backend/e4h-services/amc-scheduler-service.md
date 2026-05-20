# AMC Scheduler Service

## Purpose

The AMC scheduler service supports scheduled field or maintenance operations. Its README currently uses project-service style documentation, so this page records the repo-backed facts and the likely operational role conservatively.

## Source location

- Service path: `backend/e4h-services/amc-scheduler-service`
- README: `backend/e4h-services/amc-scheduler-service/README.md`
- Local setup: `backend/e4h-services/amc-scheduler-service/LOCALSETUP.md`
- Changelog: `backend/e4h-services/amc-scheduler-service/CHANGELOG.md`

## Responsibilities

- Supports scheduled operational workflows related to AMC or field work.
- Uses project-style domain concepts such as projects, beneficiaries, tasks, and staff in the existing README source material.
- Participates in backend scheduling or workflow orchestration where configured.

## Documented dependencies

The README lists:

- IDGen service.
- Facility service.
- Household service.
- Product service.

## API surface from README

The README describes project-style endpoints under `/project`, including create, update, search, delete, and bulk operations for:

- Projects.
- Project beneficiaries.
- Project tasks.
- Project staff.

Confirm the actual endpoints from the service code before integrating, because the README appears shared with project-style services.

## Operational notes

When documenting AMC behavior further, distinguish scheduled job behavior from project registry behavior and link the relevant cron or scheduler configuration.
