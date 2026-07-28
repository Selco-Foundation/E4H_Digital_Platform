# Asset Registry

## Purpose

The asset registry service manages E4H asset data and installation workflow contracts.

## Source location

- Service path: `backend/e4h-services/asset-registry`
- README: `backend/e4h-services/asset-registry/README.md`
- Build file: `backend/e4h-services/asset-registry/pom.xml`

## Responsibilities

- Provides asset registry APIs.
- Stores and validates asset-related domain data.
- Supports asset installation workflows used by frontend and mobile flows.
- Uses master data such as asset type, brand, system, asset count, and warranty duration.

## API and schema references

- API specification: `docs/asset-registry/asset-registry-1.0.0.yaml`
- Master-data schemas: `docs/asset-registry/master-data-schema`
- SQL schema: `docs/asset-registry/schema/V1__create_asset_registry_schema.sql`
- Workflow definition: `docs/asset-registry/workflows/AssetInstallationWorkflow.json`

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
