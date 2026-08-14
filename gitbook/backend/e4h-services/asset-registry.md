# Asset Registry

## Purpose

The asset registry service manages E4H asset data and installation workflow contracts.

## Source location

- Service path: [`backend/e4h-services/asset-registry`](https://github.com/Selco-Foundation/E4H_Digital_Platform/tree/add-gitbook-docs/backend/e4h-services/asset-registry)
- README: [`backend/e4h-services/asset-registry/README.md`](https://github.com/Selco-Foundation/E4H_Digital_Platform/blob/add-gitbook-docs/backend/e4h-services/asset-registry/README.md)
- Build file: [`backend/e4h-services/asset-registry/pom.xml`](https://github.com/Selco-Foundation/E4H_Digital_Platform/blob/add-gitbook-docs/backend/e4h-services/asset-registry/pom.xml)
- OpenAPI spec: [`backend/e4h-services/asset-registry/openapi.json`](https://github.com/Selco-Foundation/E4H_Digital_Platform/blob/add-gitbook-docs/backend/e4h-services/asset-registry/openapi.json)
- ERD: [`backend/e4h-services/asset-registry/ERD.md`](https://github.com/Selco-Foundation/E4H_Digital_Platform/blob/add-gitbook-docs/backend/e4h-services/asset-registry/ERD.md)

## Responsibilities

- Provides asset registry APIs.
- Stores and validates asset-related domain data.
- Supports asset installation workflows used by frontend and mobile flows.
- Uses master data such as asset type, brand, system, asset count, and warranty duration.

## API and schema references

- API specification (YAML): `docs/asset-registry/asset-registry-1.0.0.yaml`
- Full OpenAPI spec (JSON): `backend/e4h-services/asset-registry/openapi.json`
- Master-data schemas: `docs/asset-registry/master-data-schema`
- SQL schema: `docs/asset-registry/schema/V1__create_asset_registry_schema.sql`
- Workflow definition: `docs/asset-registry/workflows/AssetInstallationWorkflow.json`

### API surface

All endpoints are under context path `/asset-registry`. Requests do not use an `Authorization` header; each request body carries a `RequestInfo` object whose `authToken` field holds the Bearer JWT obtained from egov-user's OAuth login, and `RequestInfo.userInfo` identifies the calling user.

Several endpoints below are stubbed and currently always return `501 Not Implemented` (bulk create, all AMC and AMC visit endpoints, workflow update). They are documented here because they are live, routable endpoints. The complete, authoritative spec is at `backend/e4h-services/asset-registry/openapi.json`.

#### Asset
- `POST /v1/asset/_create` — Validates and persists a new asset (inverter, panel, battery, etc.) against MDMS master data, checks for duplicates (assetTypeID + serialNumber + brandID + modelNumber), generates an assetId, and raises a create event on the `save-asset` Kafka topic.
- `POST /v1/asset/_update` — Validates and updates an existing asset identified by the `assetID` query parameter; re-validates MDMS masters and duplicates before persisting.
- `POST /v1/asset/_search` — Searches assets by tenantId (mandatory) plus optional filters (assetID, facilityID, assetType, serialNumber, modelNumber, brandID, wfStatus, isOperational, activityFacilityID), paginated via `offset`/`limit`; also returns each matched asset's attached documents.

#### Asset Bulk
- `POST /v1/asset/bulk/_create` — Bulk create assets. **Stub — returns HTTP 501 Not Implemented.**

#### Asset Workflow
- `POST /v1/asset/workflow/{assetID}/_update` — Transition an asset's workflow status (e.g. CREATED -> VERIFIED -> ACTIVE) via egov-workflow-v2, optionally attaching verification documents and assignees. **Stub — returns HTTP 501 Not Implemented.**

#### Asset AMC
- `POST /v1/asset/amc/_create` — Create an Annual Maintenance Contract record for an asset. **Stub — returns HTTP 501 Not Implemented.**
- `POST /v1/asset/amc/_update` — Update an existing AMC contract. **Stub — returns HTTP 501 Not Implemented.**
- `GET /v1/asset/amc/_search` — Filter AMC records by asset or contract number. **Stub — returns HTTP 501 Not Implemented.**

#### Asset AMC Visit
- `POST /v1/asset/amc/visit/_create` — Log a completed or scheduled AMC visit for an asset. **Stub — returns HTTP 501 Not Implemented.**
- `POST /v1/asset/amc/visit/{visitID}/_update` — Update an existing AMC visit record. **Stub — returns HTTP 501 Not Implemented.**
- `GET /v1/asset/amc/visit/_search` — Filter AMC visits by asset, facility, or visit date. **Stub — returns HTTP 501 Not Implemented.**

## Related mobile/frontend flows

Asset registry data is used by:

- Asset type selection.
- Asset count entry.
- Asset specification entry.
- Asset submission.
- Installation image upload and review.
- Asset summary views.

## Operational notes

Asset registry changes often affect backend contracts, mobile models, frontend forms, master data, SQL migrations, and workflow actions. Update all related layers together.
