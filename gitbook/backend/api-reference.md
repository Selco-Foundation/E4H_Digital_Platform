# API reference

The repository stores API specifications and data contracts under `docs`. This page indexes the major API references instead of duplicating them.

## OpenAPI specifications

| Area | File |
| --- | --- |
| Asset registry | `docs/asset-registry/asset-registry-1.0.0.yaml` |
| Facility registry | `docs/facility-registry/facility-v2-api.yaml` |
| Project service | `docs/project-service/project-v1.api.yaml` |

E4H service OpenAPI specs (see [E4H services](e4h-services.md) for per-service documentation):

| Service | File |
| --- | --- |
| AMC Scheduler Service | `backend/e4h-services/amc-scheduler-service/openapi.json` |
| Asset Registry | `backend/e4h-services/asset-registry/openapi.json` |
| eGov HRMS | `backend/e4h-services/egov-hrms/openapi.json` |
| Field Planner | `backend/e4h-services/field-planner/openapi.json` |
| Field Planner Activity | `backend/e4h-services/field-planner-activity/openapi.json` |
| IM Services | `backend/e4h-services/im-services/openapi.json` |
| IM Services Analytics | `backend/e4h-services/im-services-analytics/openapi.json` |
| Inbox | `backend/e4h-services/inbox/openapi.json` |
| Ingestion Service | `backend/e4h-services/ingestion-service/openapi.json` |
| Project Service | `backend/e4h-services/project/openapi.json` |
| RMS Service | `backend/e4h-services/rms-service/openapi.json` |
| Vendor Registry | `backend/e4h-services/vendor-registry/openapi.json` |

Processor Services has no OpenAPI spec.

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
