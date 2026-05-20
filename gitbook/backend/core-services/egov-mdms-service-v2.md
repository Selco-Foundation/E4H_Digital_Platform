# eGov MDMS Service v2

## Purpose

MDMS is the master-data management service used to serve configuration and reference data to backend services, frontend applications, and mobile workflows.

## Source location

- Service path: `backend/core-services/egov-mdms-service-v2`
- Build file: `backend/core-services/egov-mdms-service-v2/pom.xml`

## Responsibilities

- Serves master data used by services and UIs.
- Supports configurable behavior without hardcoding every reference value in application code.
- Acts as a dependency for services such as IDGen, Workflow, HRMS, IM Services, and mobile/frontend form flows.

## Related repository assets

Master-data schemas are stored in:

- `docs/asset-registry/master-data-schema`
- `docs/facility-registry/master-data-schema`

## Operational notes

When adding or changing master data, update the schema, service behavior, UI behavior, and any deployment data together. Incorrect MDMS data often appears as missing dropdown values, failed validation, broken workflow actions, or failed ID generation.
