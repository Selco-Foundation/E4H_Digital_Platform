# Inbox

## Purpose

The inbox service aggregates workflow and service data so applications can display paginated inbox results.

## Source location

- Service path: `backend/e4h-services/inbox`
- README: [`backend/e4h-services/inbox/README.md`](https://github.com/Selco-Foundation/E4H_Digital_Platform/blob/add-gitbook-docs/backend/e4h-services/inbox/README.md)
- OpenAPI spec: [`backend/e4h-services/inbox/openapi.json`](https://github.com/Selco-Foundation/E4H_Digital_Platform/blob/add-gitbook-docs/backend/e4h-services/inbox/openapi.json)
- Local setup: `backend/e4h-services/inbox/LOCALSETUP.md`
- Changelog: [`backend/e4h-services/inbox/CHANGELOG.md`](https://github.com/Selco-Foundation/E4H_Digital_Platform/blob/add-gitbook-docs/backend/e4h-services/inbox/CHANGELOG.md)

## Responsibilities

- Aggregates municipal or domain service data with workflow data.
- Supports complex search criteria.
- Returns application and workflow data for inbox screens.
- Returns total count for matching search criteria.

## Dependencies

The README lists:

- `workflow-v2`
- `user-service`
- `egov-searcher`
- The domain service for which inbox configuration is added.

## API surface

Base context path: `/inbox`. Two API generations exist side by side: v1 (`InboxController`) and v2
(`InboxV2Controller`). The complete contract, including request/response schemas and examples, is in
`backend/e4h-services/inbox/openapi.json`.

### v1

- `POST /v1/_search` — search inbox application data (workflow + module data) based on criteria.
- `POST /v1/dss/_search` — aggregate metric data for DSS charts.
- `POST /v1/elastic/_search` — placeholder Elasticsearch-backed search endpoint; not wired up, currently returns a null body.

### v2

- `POST /v2/_search` — generic ES + workflow-driven inbox search.
- `POST /v2/project/_search` — inbox search returning project-module-shaped results.
- `POST /v2/_getFields` — fetch a specific set of fields from the configured ES index for matching documents.

## Operational notes

Inbox behavior depends on workflow configuration, user roles, and per-service inbox configuration. When a new service needs inbox support, add the service configuration and test role-specific search results.
