# Vendor Registry changelog

Derived from commit history on `backend/e4h-services/vendor-registry` (`main` branch), grouped by month.

## 2026-06
- Write migration to fill missing boundary details(block, district, state) on Kibana, Fixed issue on vendor mapping, Add some facility fields on visit search response (#2556)
- Vendor mapping facility(2449), Fix issue Facility Name Leading Space Not Trimmed #2488, State Name Display Formatting Issue #2489, Get only active values from mdms for facility ingestion, Recreated User Remains Inactive After Deletion #2515, 504 issue #2517 (#2518)

## 2026-05
- Add default role EMPLOYEE when creating a vendor user (#2412) (#2413)
- Prod release (#2374)

## 2026-04
- Prod release (#2320)

## 2026-03
- Fixed the code for facility onm readiness (#2241)
- Amc, admin and reviewer related changes (#2213) (#2235)

## 2026-02
- Admin UI backend implementation (#2050)

## 2026-01
- Admin UI functionality (#1879) (#2003)

## 2025-10
- Staging field planner (#1495)

## 2025-08
- Release v2.1 (#1247)

## 2025-06
- Fixed vendor registry issues. (#808)

## 2025-05
- Fixed vendor registry issues. (#693)
- Modify opentelemetry in pom (#685)
- Remove opentelemetry alpha dependency (#684)
- More fixes for vendor.. (#683)
- Ignore telemetry test (#682)
- Added more exclusions to disable telemetry config for unit tests (#681)
- Fix for vendor build failure (#665)
- Added telemetry (#651)

## 2025-04
- Boundary ingestion function (#573)
- Fixed test cases (#567)
- Added new fields and updated validations (#542)
- Vendor service (#530)
- Renaming municipal service and moving directories to e4h-services and creating vendor-registry service (#519)
