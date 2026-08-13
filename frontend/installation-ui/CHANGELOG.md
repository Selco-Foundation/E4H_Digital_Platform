# Changelog

All notable changes to this module will be documented in this file.

## 2026-07-16
- Updated justification code validation to also accept codes prefixed with `SFJ-`, in addition to the existing `JUS-` prefix.

## 2026-06-30
- Production release (iteration 31).
- Added Installation Completion Certificate and Asset Handover Document generation to the Installation Reviewer UI.
- Production release (June 23).
- RMS deployment (June 30).

## 2026-06-03
- Added Anganwadi facility onboarding flow, refactored the Pause RMS UI, and added Arunachal Pradesh state information.
- Synced facility codes from Kibana/NIN/HFR IDs and migrated missing boundary facility data as part of a production data cleanup.
- Added vendor-to-facility mapping; fixed facility name leading-space trimming, state name display formatting, inactive-user-after-deletion bug, a recurring 504 issue, and filtered ingestion to use only active MDMS values.

## 2026-04-27
- Production release.
- Fixed profile update page UI validations.
- Phase two production deployment.

## 2026-03-27
- Fixed backend error responses not being surfaced in UI toast notifications.
- Renamed the "Inverter" label to "Inverter or Charge Controller" on AMC Reviewer screens.
- Refactored the Organization Admin UI for user jurisdiction handling.

## 2026-02-18
- Increased the asset search result limit on the facility details page.
- Added the Admin UI frontend.
- Added an "Is O&M Ready" toggle for bulk facility additions.
- Fixed facility boundary incorrectly displaying as a block on the facility details page.
- Fixed asset search pagination on the facility admin UI.
- Fixed issues in the Boundary & Organization Admin UI.

## 2025-12-23
- Added the AMC (Annual Maintenance Contract) Reviewer UI.
- Fixed a bug allowing AMC creation forms to be submitted without a required file upload.
- Fixed AMC facility data validation.
- Added user management for Field Plan installation activities.

## 2025-11-13
- Fixed PO number display and a custom dropdown issue on the Activity Details page.
- Migrated the Installation Reviewer module to updated backend APIs.
- Fixed a stylesheet truncation issue caused by the unpkg CDN.

## 2025-10-13
- Added the Project creation flow and removed unused sandbox/workbench build packages.
- Implemented the Project Manager UI for Field Plan creation.
- Added Bill of Materials (BOM) support to the Installation Reviewer.
- Fixed issues in the Field Plan creation UI.

## 2025-09-01
- Refactored the Installation Reviewer module structure.
- Implemented approve and reject functionality for installation reviews.
- Refactored the Installation Reviewer UI.
- Added QC module hooks and services for facility, boundary, field plan, and asset data (v2.0.4).

## 1.0.0 - 2025-08-11
- Base version: Installation UI micro-frontend added to the platform, including workbench/sandbox build setup, component registry, and initial customisations.
- Follow-up patch cleaning up the initial build/workbench setup.
