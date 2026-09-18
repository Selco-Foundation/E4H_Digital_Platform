# Changelog

All notable changes to this module will be documented in this file.

## 2026-07-28
- Added logic to handle CO2 calculation for facilities with missing data and introduced the program CO2 dashboard
- Quick-fixed a Kibana issue affecting Anganwadi data
- Modified the analytics index payload
- Brought in API documentation from the API doc branch and updated the e4h-services GitBook pages

## 2026-06-30
- RMS deployment iteration (facility/vendor data updates)

## 2026-06-11 - Prod release iteration 31
- Bundled fixes: vendor-to-facility mapping, trimmed leading spaces in facility names, corrected state name display formatting, restricted MDMS facility ingestion to active values only, fixed recreated users remaining inactive after deletion, and resolved a recurring 504 error

## 2026-04-28
- Hotfix for a facility update issue

## 2026-03-27
- Reinstalled/updated the im-services integration, including business service updates and process instance migration
- Rolled out Saura eMitra 3.0
- Updated the open-tickets query to correctly exclude closed-state tickets and stabilized open-status handling in staging

## 2025-12-19
- Migrated PHC (Primary Health Centre) count data to key off facilityId instead of tenantId

## 2025-12-12
- Reworked the user search flow
- Ran the deduplication migration in staging
- Adjusted the escalation flow to account for post-migration data
- Migrated the Saura eMitra integration from production into the asset-management backend

## 2025-12-04
- Integrated Saura eMitra asset data
- Fixed PHC count and remaining-SLA count issues surfaced after the integration

## 2025-11-17
- Added handling for health-facility type and age-bucket flow in the analytics logic

## 2025-11-03
- Overhauled escalation email templates: ticket logic, level-one ticket handling, breached-ticket counts, weekly template layout (including arrow icons and a new color scheme), and renamed "operational lead" to "SPM" in templates

## 2025-10-24
- Introduced a scheduled cron job for the escalation matrix
- Fixed workflow state handling, escalation flow, index naming, and ticket-fetching logic; added a missing supporting file and environment-specific URL configuration

## 2025-10-17
- Corrected total SLA remaining calculation and related index-name issues
- Adjusted the priority table for UAT

## 2025-09-15
- Updated the computed-SLA query for im-services and the search indexer
- Included closed tickets in the total SLA staging computation

## 2025-09-01 - v2.0.4
- Released v2.0.4, consolidating a series of PHC count fixes (facility PHC counts, indexing, and computation corrections)

## 2025-08-11 to 2025-08-14 - v2.1 / v2.0.3
- Released v2.1
- Backported a v2.0.3 patch

## 2025-07-11
- Fixed SLA computation issues for the Incident business service and corrected remaining/defined total SLA values

## 2025-07-03 - v2.0.0
- V2.0.0 production deployment

## 2025-06-06
- Added a computation endpoint for priority-based SLA
- Removed the unused db-migration module

## 2025-04-22 - 1.0.0 (Base version)
- Initial version of the analytics/escalation service, established as part of renaming the municipal service and reorganizing directories under e4h-services
