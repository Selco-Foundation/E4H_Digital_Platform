# Changelog

All notable repository-wide changes are documented here, one entry per git release tag (`git tag`), newest first. Each entry is derived directly from the commit(s) included in that tag — for tags backed by many commits, the entry summarizes the major themes rather than listing every commit. For changes to a specific backend/frontend service, see that service's own `CHANGELOG.md`.

## Unreleased

- GitBook documentation overhaul: root README rewrite, architecture/module/roles pages, per-service GitBook pages, API docs, LLDs, and operations pages; added and corrected several service-level `README.md` "Local Setup" sections and `CHANGELOG.md` files.

## v3.14.38 - 2026-08-11
- Mobile numbers are now encrypted on every write call.

## v3.14.37 - 2026-08-05
- New field planner production deployment.

## v3.13.37 - 2026-08-03
- Project name lookup in MDMS now uses boundary code instead of state name.

## v3.13.36 - 2026-07-27
- Fixed CO2 calculation for facilities with missing data.

## v3.13.35 - 2026-07-25
- Added the program-level CO2 emissions dashboard.

## v3.12.35 - 2026-07-20
- Updated the index payload; fixed a Kibana Anganwadi display issue.

## v3.11.35 - 2026-07-17
- Username update (staging).

## v3.10.35 - 2026-07-16
- Justification code validation now accepts codes prefixed `SFJ-` alongside `JUS-`.

## v3.10.34 - 2026-07-16
- Tech POC SMS notifications made state-specific.

## v3.9.34 - 2026-07-08
- AMC expiration handling.

## v3.8.34 - 2026-07-02
- RMS bug fixes.

## v3.8.33 - 2026-07-01
- Fixed "Assign to me" default filter for State SPOC.

## v3.8.32 - 2026-06-30
- Quick fix for the RMS system user.

## v3.8.31 - 2026-06-30
- Updated the RMS system-user configuration.

## v3.8.30 - 2026-06-30
- RMS deployment.

## v3.7.30 - 2026-06-23
- Production release.

## v3.7.29 - 2026-06-17
- Fixed vendor mapping display in Kibana and the Admin module for Maharashtra.

## v3.7.28 - 2026-06-16
- Added Installation Completion Certificate and Asset Handover Document to the Installation Reviewer UI.

## v3.7.27 - 2026-06-12
- When a facility is marked not O&M-ready, its HCR user is deactivated and removed from the search index.

## v3.7.26 - 2026-06-11
- Production release.

## v3.6.26 - 2026-06-09
- Backfilled missing boundary details (block/district/state) in Kibana; fixed a vendor-mapping issue; added facility fields to the visit search response.

## v3.6.25 - 2026-06-04
- Fixed vendor merge logic.

## v3.6.24 - 2026-06-03
- Vendor-facility mapping fixes, facility name/state-name display fixes, active-only MDMS filtering for facility ingestion, fixed users staying inactive after re-creation, fixed a 504 timeout issue.

## v3.5.24 - 2026-05-25
- Synced facility codes from NIN ID / Anganwadi POC username in Kibana; migration for facilities with missing boundary data; duplicate-boundary-creation validation.

## v3.5.23 - 2026-05-25
- Relaxed battery-type validation to a null check only.

## v3.5.22 - 2026-05-20
- Prevented duplicate facility creation against an existing username for Anganwadi facilities.

## v3.5.21 - 2026-05-20
- Fixed POC user designation.

## v3.5.20 - 2026-05-19
- Anganwadi onboarding; RMS pause UI refactors; Arunachal Pradesh state info added.

## v3.4.20 - 2026-05-18
- Fixed a facility update issue.

## v3.4.19 - 2026-05-13
- Default `EMPLOYEE` role now assigned when creating a vendor user.

## v3.4.18 - 2026-05-12
- Fixed a same-boundary search error and added a null check; handled duplicate rows in bulk boundary creation.

## v3.4.17 - 2026-05-11
- Fixed Tech POC role check; fixed a production issue updating old tickets.

## v3.4.16 - 2026-05-05
- Renamed a migration file.

## v3.4.15 - 2026-05-05
- Production release.

## v3.3.15 - 2026-04-28
- Hotfix for a facility update issue.

## v3.3.14 - 2026-04-27
- Phase-two production deployment.

## v3.3.13 - 2026-04-24
- Removed reliance on `tenant.tenants` for the login report.

## v3.3.12 - 2026-04-16
- Fixed profile update page UI validations.

## v3.3.11 - 2026-04-16
- Production release.

## v3.3.10 - 2026-04-09
- Fixed a facility-details migration data issue.

## v3.3.9 - 2026-03-31
- Added a migration for default installation activity.

## v3.3.8 - 2026-03-31
- Fixed a project-service build issue.

## v3.3.7 - 2026-03-31

A large squashed release (~528 commits) marking the platform's expansion from the original incident-management app into the current E4H services suite. Highlights:
- Split services out into `backend/e4h-services`: asset registry, vendor registry, ingestion service, project service, field planner.
- Added the AMC (annual maintenance contract) module end to end, including the AMC Reviewer and Field Plan Installation Activity UI.
- Added RMS (Remote Monitoring System) integration and the RMS rule engine.
- Added the Admin UI (organization, boundary, and facility administration).
- Added video/image upload with FFmpeg processing (`processor-services`), including HLS streaming support.
- Added `egov-filestore` to the repo.
- MDMS v2 integration and boundary v2 (paginated) API integration.
- SLA computation, escalation matrix, and audit/index reporting improvements across `im-services`.
- Assorted UI features (feedback survey, multi-tenant theming, PWA support, GA4 analytics) and fixes.

## v3.2.7 - 2026-03-24
- Added the theft-notification cron job Dockerfile; fixed an incorrect CRM mobile number shown in the mobile side nav.

## v3.2.6 - 2026-03-19
- Fixed a "current owner" issue.

## v3.2.5 - 2026-03-18
- Added theft-related conditional checks.

## v3.2.4 - 2026-03-18
- IM Services uninstallation/reinstallation workflow.

## v3.2.3 - 2026-03-18
- Added the ability to pause RMS's automatic ticket creation (staging).

## v3.2.2 - 2026-03-18
- Saura eMitra 3.0.

## v3.1.2 - 2026-03-17
- Dashboard release.

## v3.0.2b - 2026-02-24
- Migration fix.

## v3.0.2a - 2026-02-24
- Migration file fix.

## v3.0.2 - 2026-02-24
- Fixed `application.properties` and a migration file.

## v3.0.1 - 2026-02-23
- Updated the RMS system-user UUID.

## v3.0.0 - 2026-02-23
- RMS release for Karnataka state.

## v2.2.8 - 2026-01-28
- Made HRMS search case-sensitive; granted the `VIEWER` role Postgres access.

## v2.2.7 - 2026-01-22
- Removed duplicate mobile-number validation from HRMS update calls.

## v2.2.6 - 2025-12-24
- Added `searchOnlyInBoundary` and `isActive` filters for user search.

## v2.2.5 - 2025-12-19
- Migrated PHC count data to key off `facilityId` instead of `tenantId`.

## v2.2.4 - 2025-12-13
- Fixed facility migration POC contact number.

## v2.2.3 - 2025-12-13
- Fixed inbox search failure on the create-complaint page.

## v2.2.2 - 2025-12-12
- Renamed MDMS migration files so they run before the employee migration.

## v2.2.1 - 2025-12-12
- Migrated Saura eMitra to production on the asset-management backend.

## v2.1.1 - 2025-11-17
- Added HF-type/age-bucket flow; Java base image update.

## v2.1.0 - 2025-11-03
- Staging merge.

## v2.0.11 - 2025-10-14
- Reverted a health-care-center-type casing fix that had regressed the ticket details page.

## v2.0.10 - 2025-10-13
- Standardized PHC-subtype value casing.

## v2.0.9 - 2025-10-06
- Fixed incident type/subtype on a specific production ticket.

## v2.0.8 - 2025-09-18
- Fixed spelling of the `OtherFan` subtype via migration.

## v2.0.7 - 2025-09-15
- Deleted dummy tickets.

## v2.0.6 - 2025-09-05
- Reverted the dummy-ticket deletion from v2.0.5; changed reject-reason indexing to push only the latest reason.

## v2.0.5 - 2025-09-04
- Deleted dummy health-center tickets from production; search filter fix.

## v2.0.4 - 2025-09-01
- Production release.

## v2.0.3 - 2025-08-14
- Patch release.

## v2.0.2 - 2025-07-23
- Production hotfix.

## v2.0.1 - 2025-07-22
- SLA computation and workflow-transition fixes across `im-services`; Maharashtra UI image build.

## v2.0.0 - 2025-07-03
- Production deployment covering: feedback survey, logo/theming refactor, SLA query fixes and Elasticsearch performance work, CRM toll-number display fix, workflow tagging, and role trimming on process instances.

## v1.1.2 - 2025-05-26
- Fixed SMS notification locale; removed the gender field from the profile page.

## v1.1.1 - 2025-05-14
- Logo rendering refactor; Docker build cleanup for state UI images; search query fixes.

## v1.1.0 - 2025-04-17
- Early platform release covering: multi-tenant UI theming, video upload with FFmpeg processing, workflow changes, ticket-assignment fixes, and initial state-specific UI images (Gujarat and others).

## v1.0 - 2024-10-04
- First tagged release, capturing everything merged since the initial commit (2024-04-16): base backend/frontend code, HRMS and PGR customization, and initial folder reorganization.
