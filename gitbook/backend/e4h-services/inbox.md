# Inbox

## Purpose

The inbox service aggregates workflow and service data so applications can display paginated inbox results.

## Source location

- Service path: `backend/e4h-services/inbox`
- README: `backend/e4h-services/inbox/README.md`
- Local setup: `backend/e4h-services/inbox/LOCALSETUP.md`
- Changelog: `backend/e4h-services/inbox/CHANGELOG.md`

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

- `_search`: searches inbox application data based on provided criteria.

## Operational notes

Inbox behavior depends on workflow configuration, user roles, and per-service inbox configuration. When a new service needs inbox support, add the service configuration and test role-specific search results.
