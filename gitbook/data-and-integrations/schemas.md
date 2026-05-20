# Schemas

Schemas define API payloads, master-data shapes, ingestion contracts, and database setup files.

## JSON schema directories

- `docs/asset-registry/master-data-schema`
- `docs/facility-registry/master-data-schema`
- `docs/ingestion/schema`

## SQL schema files

- `docs/asset-registry/schema/V1__create_asset_registry_schema.sql`
- `docs/facility-registry/schema/V1__create_facility_registry_schema.sql`

## API schema files

- `docs/asset-registry/asset-registry-1.0.0.yaml`
- `docs/facility-registry/facility-v2-api.yaml`
- `docs/project-service/project-v1.api.yaml`

## Update guidance

When a schema changes:

1. Update the source schema file.
2. Update affected backend services.
3. Update frontend or mobile models if the payload is user-facing.
4. Update this GitBook page if the change adds or removes a schema area.
