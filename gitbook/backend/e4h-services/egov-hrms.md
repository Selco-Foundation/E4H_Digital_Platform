# eGov HRMS

## Purpose

HRMS manages employees enrolled in the system and exposes APIs to create, update, and search employee records.

## Source location

- Service path: `backend/e4h-services/egov-hrms`
- README: `backend/e4h-services/egov-hrms/README.md`
- Local setup: `backend/e4h-services/egov-hrms/LOCALSETUP.md`
- Changelog: `backend/e4h-services/egov-hrms/CHANGELOG.md`

## Responsibilities

- Manages employee records.
- Captures assignments, departments, designations, and reporting relationships.
- Captures jurisdictions.
- Captures service history and educational details.
- Creates or links employee records with `egov-user`.

## Dependencies

The README lists:

- `egov-user`
- `egov-localization`
- `egov-idgen`
- `egov-mdms`
- `egov-filestore`

## Key domain concepts

- Assignments: employee designations for a period of time.
- Jurisdictions: administrative or boundary scope for the employee.
- Service history: professional experience records.
- Educational details: degree and qualification information.
- Departmental tests: additional employee qualification information.

## Operational notes

HRMS data controls who can act in employee workflows and can affect inbox visibility, workflow role checks, and staff assignment flows. Keep HRMS, user-service, MDMS, and workflow configuration aligned.
