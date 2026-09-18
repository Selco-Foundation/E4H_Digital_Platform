# Vendor Registry

## Purpose

The vendor registry stores organisation records such as vendors, contractors, and community-based organisations.

The service still identifies internally as "Organisation" in most code (`org.egov`, `OrganisationApiController`, `eg_org*` tables); `vendor-registry` is its e4h module name (`server.contextPath=/vendor`, OpenAPI title "Vendor Registry Service").

## Source location

- Service path: [`backend/e4h-services/vendor-registry`](https://github.com/Selco-Foundation/E4H_Digital_Platform/tree/add-gitbook-docs/backend/e4h-services/vendor-registry)
- README: [`backend/e4h-services/vendor-registry/README.md`](https://github.com/Selco-Foundation/E4H_Digital_Platform/blob/add-gitbook-docs/backend/e4h-services/vendor-registry/README.md)
- OpenAPI spec: [`backend/e4h-services/vendor-registry/openapi.json`](https://github.com/Selco-Foundation/E4H_Digital_Platform/blob/add-gitbook-docs/backend/e4h-services/vendor-registry/openapi.json)
- Changelog: [`backend/e4h-services/vendor-registry/CHANGELOG.md`](https://github.com/Selco-Foundation/E4H_Digital_Platform/blob/add-gitbook-docs/backend/e4h-services/vendor-registry/CHANGELOG.md)
- ERD: [`backend/e4h-services/vendor-registry/ERD.md`](https://github.com/Selco-Foundation/E4H_Digital_Platform/blob/add-gitbook-docs/backend/e4h-services/vendor-registry/ERD.md)

## Responsibilities

- Creates, updates, and searches organisation entities.
- Stores organisation details and contact details.
- Stores tax identifiers and classifications.
- Links organisations to functional areas where they operate.
- Links organisations to Individual/HRMS/User records.
- Sends SMS notifications on organisation create/update.

## Dependencies

The README lists:

- DIGIT backbone services (User, Localization, MDMS, Workflow v2, Boundary/Location).
- Persister.
- Indexer.
- IDGen.
- Individual.
- HRMS (employee sync for org users).
- egov-enc-service (PII encryption of POC mobile numbers).
- facility-service, field-planner-activity (org/facility and field-plan integrations).

## API surface

The full OpenAPI 3 spec lives at `backend/e4h-services/vendor-registry/openapi.json`. The README also links to the upstream Organisation Registry API specification:

- `https://raw.githubusercontent.com/egovernments/DIGIT-Specs/master/Domain%20Services/Works/Organisation-V1.0.0.yaml`

All active endpoints are served by `OrganisationApiController`. A second controller, `OrgServicesApiController` (mapped under `/v1`), backs the workflow-oriented surface; per the spec its search endpoint is a stub (see below).

### Organisation (`/organisation/v1`)

- `POST /organisation/v1/_create` — create one or more organisations (no workflow)
- `POST /organisation/v1/_search` — search organisations
- `POST /organisation/v1/_update` — update one or more organisations (no workflow)

### Organisation User (`/organisation/v1/user`)

- `POST /organisation/v1/user/_create` — create organisation user (link a user to an organisation)
- `POST /organisation/v1/user/_search` — search organisation users
- `POST /organisation/v1/user/_update` — update organisation user
- `POST /organisation/v1/user/_delete` — delete organisation user

### Organisation Services / Workflow (`/v1`)

- `POST /v1/_create` — create organisation (with workflow)
- `POST /v1/_search` — search organisations (workflow controller stub; always returns an empty list and zero count regardless of criteria — use `POST /organisation/v1/_search` instead)
- `POST /v1/_update` — update organisation (with workflow)

## Operational notes

Vendor data may be consumed by ingestion, facility/asset operations, and field workflows. Keep organisation schema and integration expectations aligned when onboarding new vendor categories.
