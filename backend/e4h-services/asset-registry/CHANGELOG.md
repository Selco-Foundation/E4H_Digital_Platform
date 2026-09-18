
All notable changes to this module will be documented in this file.

## 2026-07-28
- Added an OpenAPI spec (openapi.json) and refreshed the README with the full endpoint list for API documentation.

## 2026-05-25
- Removed the battery type validation, keeping only a null check.

## 2026-02-16 - Admin UI backend implementation
- Fixed an issue with the update user org endpoint (mobile number handling).
- Removed the model number uniqueness check on asset creation.
- Added facility address and boundary details to the facility activity search response.
- Fixed an issue with facility search.

## 2026-01-29 - Admin UI functionality
- Extended the asset service and search criteria to support Admin UI needs, including a new `isOperational` field and DB migration.
- Fixed issues with the update user org endpoint and jurisdiction handling.
- Fixed facility search sorting and facility activities issues.

## 2025-11-21 - AMC module integration
- Minor cleanup to the asset search request model as part of the AMC (Annual Maintenance Contract) module work.

## 2025-10-28 to 2025-11-10 - Field planner integration
- Added configuration, error constants, and facility utility support for activity-facility search.
- Added ActivityFacilitySearchCriteria/Request and FacilityStatusResponse models.
- Added a DB migration for a new asset column.
- Fixed the asset validation flow, the asset row mapper's facility count, and asset start/end date validation.

## 2025-10-13
- Skipped warranty validation when the warranty value is null or zero.

## 2025-10-12
- Updated AssetConstants and the asset validator.

## v2.1 / v2.0.3 patch - 2025-08-11 to 2025-08-14
- Added document upload support (Document model, DocumentRowMapper) and a GeoLocation model.
- Extended AssetRepository and AssetService, and updated related controller, configuration, and persister settings.
- Added DB migrations for a new document column and asset column updates.

## V2.0.0 - 2025-07-03
- Rolled the Asset Registry module (asset creation, search, update, workflow, AMC visit tracking, validator, DB migration, Kafka persister config) into the platform's v2.0.0 production release.

## 1.0.0 - 2025-05-08 to 2025-06-05
- Base version: initial Asset Registry service with create and search asset APIs, workflow support, and facility-service integration.
- Added telemetry.
- Changed Java version from 21 to 17.
- Removed unused test folder.
- Fixed asset registry and facility-service integration bugs.
