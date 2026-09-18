# Ingestion Service changelog

Derived from commit history on `backend/e4h-services/ingestion-service` (`main` branch), grouped by month.

## 2026-06
- Mapped Vendor not reflecting correctly in Kibana and Admin module for Maharashtra #2592 (#2598) (#2604)
- Vendor mapping facility(2449), Fix issue Facility Name Leading Space Not Trimmed #2488, State Name Display Formatting Issue #2489, Get only active values from mdms for facility ingestion, Recreated User Remains Inactive After Deletion #2515, 504 issue #2517 (#2518)

## 2026-05
- Script to Sync Kibana Code from NIN ID when HFR ID is empty or NULL,Sync code from poc_username for Anganwadi facility. Implement Migration for missing boundary facility (PROD). Update district for facility.Validation/checks for duplicate boundary creation (#2467)
- Anganwadi onboard,  Pause RMS UI Refactors, Arunachal Pradesh state info addition (#2442)
- Fix bulk boundary creation: Handle duplicates rows (#2410) (#2411)
- Search in same boundary error and NULL check (#2409)

## 2026-04
- Phase two prod deployment (#2356)
- Prod release (#2320)

## 2026-03
- Ingestion fix (#2249)
- Facility name localization (#2245)
- Bulk facility ingestion without boundary code (#2234)
- Fixed the response time for facility selection template generation (#2211)
- Amc module staging deployment:  Fix issue on create AMC configuration (#2169)
- Optimize ingestion service for project facility download (#2168)

## 2026-02
- Add sorting option on activity facility search, Optimize fieldplan facility search (#2099)
- Admin UI backend implementation (#2050)
- Admin UI Backend staging (#2035)

## 2026-01
- Admin UI functionality (#1879) (#2003)
- Added fix for excel dropdown issue (#1894) (#1978)

## 2025-12
- [Fix]: AMC Facility Data Validation (#1820)
- Modified the request info for visit cronjob and payload, fixed issue on ingestion service for AMC (#1818)
- Modified the asset amc template flow (#1720)

## 2025-11
- Amc ingestion: Apis to download and upload template (#1678)
- Staging field planner (#1629)

## 2025-10
- Staging field planner (#1501)
- Staging field planner (#1495)

## 2025-09
- Fix project creation (#1466)
- Project creation (#1459)

## 2025-08
- Release v2.1 (#1247)

## 2025-07
- Update incident api (#1169)
- Update incident api (#1109)
- Legacy ticket ingestion (#1025)

## 2025-06
- Organize services
