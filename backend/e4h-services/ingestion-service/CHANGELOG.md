# Changelog

All notable changes to this module will be documented in this file.

## 2026-07-28
- Brought in API documentation from the api_doc_branch and updated the e4h-services GitBook pages

## 2026-06-17
- Fixed mapped vendor not reflecting correctly in Kibana and the Admin module for Maharashtra

## 2026-06-03
- Added vendor mapping for facilities
- Fixed facility name leading space not being trimmed
- Fixed state name display formatting issue
- Restricted facility ingestion to only active values from MDMS
- Fixed recreated users remaining inactive after deletion
- Fixed a 504 timeout issue

## 2026-05-25
- Added script to sync Kibana code from NIN ID when HFR ID is empty or null
- Synced code from poc_username for Anganwadi facility
- Implemented migration for missing boundary facility data (prod)
- Updated district field for facility
- Added validation/checks for duplicate boundary creation

## 2026-05-19
- Added Anganwadi onboarding support
- Paused RMS UI refactors
- Added Arunachal Pradesh state info

## 2026-05-12
- Fixed search-in-same-boundary error and added NULL checks
- Fixed bulk boundary creation to handle duplicate rows

## 2026-04-27
- Phase two production deployment

## 2026-04-16
- Production release

## 2026-03-27
- Ingestion fix

## 2026-03-26
- Added facility name localization

## 2026-03-24
- Added support for bulk facility ingestion without requiring a boundary code

## 2026-03-18
- Improved response time for facility selection template generation

## 2026-03-06
- Optimized ingestion service for project facility download
- Fixed an issue creating AMC configuration during AMC module staging deployment

## 2026-02-19
- Added sorting option on activity facility search and optimized field plan facility search

## 2026-02-16
- Implemented Admin UI backend

## 2026-02-02
- Admin UI backend staging updates

## 2026-01-29
- Added Admin UI functionality

## 2026-01-28
- Fixed Excel dropdown issue

## 2025-12-19
- Fixed AMC facility data validation

## 2025-12-18
- Modified request info for the visit cronjob and payload; fixed an AMC ingestion issue

## 2025-12-03
- Modified the asset AMC template flow

## 2025-11-27
- Added AMC ingestion APIs to download and upload templates

## 2025-11-07
- Staging updates for field planner

## 2025-10-10
- Staging updates for field planner

## 2025-10-07
- Staging updates for field planner

## 2025-09-25
- Added project creation support
- Fixed project creation issue

## 2025-08-14
- v2.0.3 patch

## 2025-08-11
- Release v2.1

## 2025-07-23
- Updated incident API

## 2025-07-18
- Updated incident API

## 2025-07-08
- Added legacy ticket ingestion

## 2025-07-03
- V2.0.0 production deployment

## 2025-06-06 - Initial version
- Initial import of the ingestion service: file ingestion and template generation APIs, boundary/facility Excel data loaders and validators, RBAC decorator, and project service
