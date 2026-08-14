# eGov HRMS

## Purpose

HRMS manages employees enrolled onto the system, including their assignments, jurisdictions, service history, educational details, departmental tests, and (de)activation history. It is treated as a superset/companion of `egov-user`: every employee created through HRMS is also created as a user in `egov-user`, using the employee code as the login username.

## Source location

- Service path: [`backend/e4h-services/egov-hrms`](https://github.com/Selco-Foundation/E4H_Digital_Platform/tree/add-gitbook-docs/backend/e4h-services/egov-hrms)
- README: [`backend/e4h-services/egov-hrms/README.md`](https://github.com/Selco-Foundation/E4H_Digital_Platform/blob/add-gitbook-docs/backend/e4h-services/egov-hrms/README.md)
- OpenAPI spec: [`backend/e4h-services/egov-hrms/openapi.json`](https://github.com/Selco-Foundation/E4H_Digital_Platform/blob/add-gitbook-docs/backend/e4h-services/egov-hrms/openapi.json)
- Changelog: [`backend/e4h-services/egov-hrms/CHANGELOG.md`](https://github.com/Selco-Foundation/E4H_Digital_Platform/blob/add-gitbook-docs/backend/e4h-services/egov-hrms/CHANGELOG.md)
- ERD: [`backend/e4h-services/egov-hrms/ERD.md`](https://github.com/Selco-Foundation/E4H_Digital_Platform/blob/add-gitbook-docs/backend/e4h-services/egov-hrms/ERD.md)

## Responsibilities

- Manages employee records.
- Captures assignments, departments, designations, and reporting relationships.
- Captures jurisdictions.
- Captures service history and educational details.
- Creates a corresponding user in `egov-user` for every employee, using the employee code as the login username, and keeps that user in sync on employee updates.

## Dependencies

The README lists:

- `egov-user`
- `egov-localization`
- `egov-idgen`
- `egov-mdms`
- `egov-filestore`
- `egov-boundary-service`
- `egov-otp`

## Key domain concepts

- Assignments: employee designations for a period of time.
- Jurisdictions: administrative or boundary scope for the employee.
- Service history: professional experience records.
- Educational details: degree and qualification information.
- Departmental tests: additional employee qualification information.
- Deactivation/reactivation: employee lifecycle is tracked via status flags plus deactivation/reactivation detail records, not row deletion.

## API surface

BasePath: `/egov-hrms/employees`

- `POST /employees/_create` — Create one or more employees, each with an embedded `egov-user` account. Validates for duplicate mobile number/username, MDMS-driven codes (employee type, department, designation, role, qualification, stream, departmental test), and data-sanity rules (exactly one current assignment, non-overlapping assignment/service periods, dates not before date of birth) before persisting.
- `POST /employees/_update` — Update one or more existing employees (each must include its existing `id`, `uuid`, and unchanged `code`). Previously-created jurisdictions, assignments, service history, education, departmental tests, documents, and deactivation details can only be appended to or modified, not removed. Deactivating requires deactivation details with an MDMS-validated reason and an effective date equal to the current date; reactivating requires reactivation details whose effective date falls between the prior deactivation and now. Also propagates changes to the corresponding `egov-user` account.
- `POST /employees/_search` — Search employees by any combination of query parameters (codes, names, departments, designations, roles, ids, uuids, employee statuses/types, positions, phone, tenantId, boundary codes, etc.); the JSON body carries only a `RequestInfo`. An open search (no criteria) requires the caller's role to be in `open.search.enabled.roles`. CITIZEN-type users cannot search by `ids`; `asOnDate` requires `departments`+`designations`; `roles`, `phone`, or `names` require `tenantId`.
- `POST /employees/_count` — Get the total employee count for a given `tenantId`. Unlike the other three endpoints, the JSON request body here is a bare `RequestInfo` object (its fields at the top level) rather than wrapped under a `RequestInfo` key.

The full OpenAPI 3 spec, including request/response schemas, lives at `backend/e4h-services/egov-hrms/openapi.json`.

## Operational notes

HRMS data controls who can act in employee workflows and can affect inbox visibility, workflow role checks, and staff assignment flows. Keep HRMS, user-service, MDMS, and workflow configuration aligned.
