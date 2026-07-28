# Master data

Master data supports shared platform configuration, registries, UI behavior, and validation.

## Asset registry master data

The asset registry includes schemas for:

- Asset count.
- Asset type.
- Brand.
- System.
- Warranty duration.

Files live under `docs/asset-registry/master-data-schema`.

## Facility registry master data

The facility registry includes schemas for:

- Facility category.
- Facility ownership.
- Facility region.
- Facility type.
- Solar solution design type.

Files live under `docs/facility-registry/master-data-schema`.

## Usage guidance

Use master-data schemas when validating backend changes, UI forms, mobile models, ingestion payloads, and deployment configuration.

When adding master data, keep the schema, service behavior, UI behavior, and documentation aligned.
