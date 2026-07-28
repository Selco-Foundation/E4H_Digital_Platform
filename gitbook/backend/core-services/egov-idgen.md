# eGov IDGen

## Purpose

The IDGen service generates IDs based on requested ID formats.

## Source location

- Service path: `backend/core-services/egov-idgen`
- README: `backend/core-services/egov-idgen/README.md`
- Local setup: `backend/core-services/egov-idgen/LOCALSETUP.md`
- Changelog: `backend/core-services/egov-idgen/CHANGELOG.md`

## Responsibilities

- Exposes a REST API that accepts ID generation requests.
- Generates IDs using configured formats.
- Supports services that need stable application numbers, entity IDs, ticket IDs, or similar identifiers.

## Dependencies

- `egov-mdms-service`

## API surface

The README documents:

- `id/v1/_genearte`

The spelling above appears in the source README. Confirm the actual endpoint from code or API contracts before using it in integrations.

## Development notes

The service is a Spring Boot application. The README notes that Lombok support should be enabled in the IDE.

## Operational notes

When adding a service that needs generated IDs, document the required ID format configuration and verify that the format is present in MDMS or the relevant configuration source.
