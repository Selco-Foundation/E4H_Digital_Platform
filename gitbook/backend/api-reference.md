# API reference

The repository stores API specifications and data contracts under `docs`. This page indexes the major API references instead of duplicating them.

## OpenAPI specifications

| Area | File |
| --- | --- |
| Asset registry | `docs/asset-registry/asset-registry-1.0.0.yaml` |
| Facility registry | `docs/facility-registry/facility-v2-api.yaml` |
| Project service | `docs/project-service/project-v1.api.yaml` |

## JSON schemas

| Area | Directory |
| --- | --- |
| Asset registry master data | `docs/asset-registry/master-data-schema` |
| Facility registry master data | `docs/facility-registry/master-data-schema` |
| Ingestion schemas | `docs/ingestion/schema` |

## SQL schemas

| Area | File |
| --- | --- |
| Asset registry | `docs/asset-registry/schema/V1__create_asset_registry_schema.sql` |
| Facility registry | `docs/facility-registry/schema/V1__create_facility_registry_schema.sql` |

## Sequence diagrams

| Area | Directory |
| --- | --- |
| Facility registry | `docs/facility-registry/sequence-diagrams` |
| Ingestion | `docs/ingestion/sequence-diagrams` |
| UI flows | `docs/ui-sequence-diagrams` |

## How to update API docs

1. Update the source OpenAPI, schema, SQL, or diagram file.
2. Update this page if a new contract area is added.
3. Update service-specific documentation when API behavior changes.
