# Changelog

All notable feature work, feature updates, and significant bug fixes on the `main` branch, grouped by calendar month. Each entry is written from the actual PR content (squashed sub-commit messages and, where those were thin, `git show --stat` on the underlying commits) rather than restated commit titles — so entries describe *what* changed technically and *why*, not just that "a bug was fixed."

Routine maintenance is intentionally omitted throughout: CI/build/Dockerfile-only changes, dependency bumps, typo/CSS-only fixes, revert-and-reapply pairs that cancel out, README-only updates, and duplicate hotfix cherry-picks that landed the same fix twice under different PR numbers (merged into one entry where recognized). Months with no commits on `main` (2024-08, 2024-09, 2024-11, 2024-12) are omitted.

---

## July 2026

### Added
- **CO2 dashboard for facilities** — Built out the carbon-emission dashboard end to end: `CarbonEmissionBatchService` now computes CO2 for all existing facilities, `FacilityKibanaMapper`/`FacilityKibanaIndex` gained facility category, vendor details, and project name fields, and AMC scheduled visits are pushed into a new Elastic index (with localization support via `LocalizationUtil` and a backfill script to populate historical data). Device identity resolution for CO2 calculation was hardened to fall back HFR → NIN → name regardless of facility type. (#2850, #2836, #2844, #2848, #2826)
- **Installation Completion Certificate & Asset Handover Document surfaced in Reviewer UI** (carried in from June work, finalized/redeployed in the July release train). (#2591)

### Changed
- **Justification code validation widened** — `create`/`update` project API validation now accepts justification codes prefixed `SFJ-` in addition to `JUS-`, and a follow-up fix removed a regex matcher error that was rejecting otherwise-valid codes. (#2789, #2720, #2726)
- **AMC expiration flow** — Implemented the AMC expiration workflow with a guard that blocks scheduling a run less than one month after "today", plus corresponding cron-job updates for AMC run scheduling. (#2617, #2606, #2612)
- **Tech POC SMS made state-specific** rather than a single global template. (#2787)

### Fixed
- **Kibana anganwadi facility-category indexing** — Fixed the health-facility index update flow so anganwadi facilities keep the correct `facility_category` on Kibana instead of falling back/being overwritten incorrectly. (#2791)
- **AMC visit index payload / center-id resolution** — Reworked the logic used to fetch the center id feeding the AMC index payload (iterated across several attempts) and added a flow to store `totalSolarEnergyGeneratedInKwh` per visit. (#2756, #2814)
- **Username sync on facility update** — The facility-update API now also updates the linked NIN/HFR id, and a new API was added to update usernames directly; fixed a `RequestInfo` bug and an `is_active` regression introduced in the same change. (#2750, #2810)
- **RMS bug batch** — Corrected the open-tickets query and the boundary search used by facility service, cleaned up duplicated/renamed RMS migration files, and paused automatic RMS reverse-voltage ticket creation (deemed too noisy/unreliable to auto-generate). (#2728, #2430)
- **State SPOC ticket assignment** — Tickets assigned to a State SPOC now surface correctly under "Assigned to Me" instead of only in "All Tickets"; also fixed a notification-service NPE and an SMS-sending issue, and added an SMS template for RMS ticket assignment to vendor by Tech POC. (#2725, #2730)

---

## June 2026

### Added
- **Vendor Organization Mapping** — New service-layer flow to map vendor organizations onto facilities on Kibana/admin, including an async path so syncing mapped vendors to facilities doesn't block the request thread; also mints a system user for facility updates. Fixed follow-up issues where vendor name still showed on Kibana after a health facility was removed from a vendor, and where updating an org user left a stale vendor username indexed. (#2556, #2482, #2555)
- **CO2 dashboard groundwork** — Added a reference table and MDMS-backed reference APIs for carbon emission, an Elmeasure monthly-consumption API, a CO2 batch-calculation + trigger API, and a Kafka-driven ES indexer; also added a cron job and handling for future financial years not yet in reference data. (#2556, #2451)
- **Installation Completion Certificate & Asset Handover Document** added to the Installation Reviewer UI, alongside the POC number surfaced on the search-activity response endpoint. (#2591, #2589, #2590)
- **RMS pause/activation alerts** — Added notification alerts when a facility's RMS pause is modified or the facility is reactivated, and made date formatting consistent across the Pause RMS pages. (#2454)
- **Financial year/month support** — Added logic across facility/AMC services to reason about financial year and month (used by the CO2 dashboard's future-FY handling). (#2507)

### Changed
- **Project naming convention overhaul** — Reworked project name generation to a new format, with a migration to rename existing projects' names and fixes for a resulting facility-count discrepancy and stale project/field-plan names being shown on create/update response pages. (#2538, #2567)
- **PO number & vendor display fixes** — Corrected PO number format validation, fixed vendor-name display, and ensured all `pocNumber`s are verified identical across Activity Assignments in a list (with clearer error text) before allowing certain actions; assigned-to now shows staff vendor name. (#2607, #2608, #2613, #2618, #2619, #2621)
- **Facility bulk search now filters inactive facilities**, and when `is_active` is false the facility's `onm_ready` flag is also forced false so it stops being treated as monitorable. (#2630)
- **RMS reactivation logic** — If `onmReady=true`, an existing (deactivated) user is now reactivated instead of a duplicate being created; if `onmReady=false`, the corresponding Elastic record is deleted and the HCR user deactivated. If `onmReady` derives false from open tickets, `solarPanelStatus` is deduced accordingly. (#2623, #2624)
- **Removed hardcoded Karnataka filter** from RMS logic and revised the CO2/consumption calculation formula. (#2609, #2632)
- **AMC visit OTP SMS ("Saura eMitra 3.0")** — Implemented SMS delivery for AMC visit OTPs, iterated through a couple of revert/re-add cycles before landing on the final template and routing for Tech POC → Vendor ticket assignment SMS. (#2566, #2638)

### Fixed
- **Vendor mapping for Maharashtra in Kibana/Admin** — Fixed a bug in updating a vendor user's jurisdiction when the user had more than 1000 jurisdictions mapped, which previously left the mapped vendor showing incorrectly. (#2592, #2598, #2604)
- **Kibana boundary backfill migration** — Wrote a migration to fill in missing block/district/state boundary details on Kibana for existing records, plus fixes to facility bulk search, epoch-time handling, and localization for state/district/block values. (#2556)
- **Facility ingestion data hygiene** — Trimmed leading spaces from facility names, collapsed multiple internal spaces to one, fixed a float-column parsing issue in the ingestion service, formatted state-name display consistently, and restricted MDMS lookups to active values only. (#2488, #2489, #2515, #2517, #2529)
- **Recreated user stays active** — When a deleted user is recreated with the same username, the new account is now created Active instead of remaining inactive. (#2515)
- **RMS user assignment quickfix** — Corrected the RMS user referenced by rms-service (landed twice due to a redeploy). (#2724, #2723)
- **Duplicate vendor merge** — Merged duplicate ERES and Mangaal vendor org records that had been created separately. (#2543)
- **Old RMS ticket migration** — Migrated old RMS ticket process instances onto the new workflow so legacy tickets aren't stuck on a deprecated process definition. (#2544)
- **Mizoram OTP issue** — Fixed an OTP-delivery bug specific to Mizoram state accounts. (#2527)
- **Boundary/localization** — Fixed extra whitespace being persisted into localization strings during boundary creation, and added a delay after organization user create/update to absorb Kafka propagation lag. (#2516, #2520)

---

## May 2026

### Added
- **Anganwadi facility category & onboarding** — Introduced `facility_category` (HEALTH vs ANGANWADI) across facility search, ingestion (Excel upload), and the indexer; one of HFR ID or NIN ID is now required only when category is HEALTH. Added handling for the "AW" (anganwadi worker) column and username during ingestion, and a Facility Admin UI flow (POC Username field, conditional detail rendering by category, validations) for onboarding anganwadi facilities directly. (#2395, #2414, #2424, #2428, #2434, #2442)
- **Forgot Password self-service (Web UI)** — Added forgot-password screens to the incident-management web UI with 4-digit OTP, refactored error messaging on failure, and restyled pre-login screens. (#2415)
- **Pause RMS UI refactor** — Reworked the Pause RMS Auto Ticket Creation UI, restricted RMS route visibility to CRM role, and fixed a bug where the Disable button stayed disabled even after all required fields were filled. (#2435, #2440)
- **CRM-driven RMS pause/resume** — Implemented a full workflow letting CRMs pause/resume RMS ticket creation for facilities within their jurisdiction: UI to select and block facilities, a "paused facilities" list view, boundary-filter driven search, and pause-duration validation, wired to new pause-RMS APIs including a cron job that auto-resumes facilities once the pause period elapses. (#2387, #2388, #2357, #2362, #2363, #2364, #2365)
- **Default EMPLOYEE role on vendor user creation** — Backend now assigns the EMPLOYEE role by default whenever a vendor user is created or updated, rather than relying on the UI to set it. (#2412, #2413)

### Changed
- **Facility block update endpoint** — Added a dedicated facility-update endpoint for changing a facility's block, plus a migration to correct facility sub-types and a fix for HF facility counts being wrong after such moves. (#2344, #2362)
- **IM user migration to vendor registry** — Migrated existing incident-management users into the vendor registry model, including tenant/accountId migration for old tickets, and deactivated vendor users automatically when removed from their vendor. (#2347, #2351, #2345, #2367)
- **Boundary creation validation** — Made duplicate checks mandatory during district/state creation, added validation against duplicate boundary creation generally, and fixed bulk boundary upload to report an accurate error count and surface it on the UI (also fixed duplicate-row handling in bulk boundary creation). (#2459, #2469, #2479, #2410, #2411)
- **Battery type validation relaxed** to a null check only, removing an overly strict enum-style check that was rejecting valid data. (#2465, #2466)

### Fixed
- **No duplicate facility with existing username for Anganwadi** — Prevented creating a new anganwadi facility that collides with an existing username. (#2452)
- **Kibana identity backfill (HFR → NIN → poc_username)** — Script to sync Kibana's "code" field from NIN ID when HFR ID is empty/null, falling back further to `poc_username` for anganwadi facilities; paired with a production migration to fill in missing facility-boundary relationships and update district for Meghalaya facilities. (#2446, #2464, #2455, #2467)
- **Tech POC role check** and **inactive employees in assignee dropdown** — Fixed the ticket-details assignee dropdown populating inactive employees, and corrected an incorrect Tech POC role check. (#2475, #2402)
- **Old ticket update failures (401/400)** — Fixed im-service errors when updating status on tickets created under the pre-migration tenant/account scheme. (#2400, #2367)
- **POC user designation fix** and **stale project-name display on create/update response pages**. (#2443, #2445, #2568, #2569, #2570)

---

## April 2026

Routine-maintenance-heavy month, but several substantive items landed:

### Added
- **Custom UI Core Module fork** — Forked a local copy of the DIGIT UI core module to allow platform-specific customization: removed the language-selection screen, fixed the login screen's logo/branding and top-nav logos, and resolved several login/password-field bugs (password update not applying, entered password clearing on visibility toggle, stale nav items under different context paths). This underlies the later forgot/change-password self-service work. (#2276)
- **Change password functionality** implemented in both the Incident Management UI and the Saura eMitra web app, and made accessible on laptop screen sizes (previously hidden by a layout bug). (#2296, #2306, #2304, #2305)
- **User role groups** — Introduced grouped user-role management so role assignment is organized/readable instead of a flat list. (#2291, #2292)
- **AMC Reviewer UI moved to MDMS config** — AMC Reviewer form configuration and rejection reasons are now driven by MDMS data instead of hardcoded UI, matching a similar move already done for the Installation Reviewer's rejection reasons; also populated installation images alongside the installation completion report in the Reviewer UI. (#2343, #2355, #2342, #2354)

### Changed
- **AMC/facility template and ingestion fixes** — Vendor code is now mandatory during facility ingestion and the "Vendor" column was removed from the AMC template (superseded by vendor-code driven mapping); also fixed NIN/HFR ID handling in the AMC template and a duplicate-row issue in the AMC config template. (#2273, #2320)
- **Same-mobile-number user creation** — Reworked the user-create API so a second user with the same mobile number updates the existing HRMS user rather than erroring; fixed default-assignment and existing-assignment checks, a deleted-user-not-updating bug, and a jurisdiction bug specific to this same-mobile flow. (#2244, #2294, #2311, #2312)
- **Bulk upload/download performance** — Reduced response time for facility template download/upload and fixed large-data upload plus AMC download issues; added bulk APIs for project-facility linking used by field plan and AMC bulk flows. (#2275, #2293)
- **AMC vendor migration** — Migrated AMC vendor org sub-types and their users over to the "Installation Vendor" subtype/model, consolidating what had been two separate vendor concepts. (#2317, #2356)

### Fixed
- **HF (health facility) update regressions** — Multiple rounds of fixes to health-facility update: localization issue when updating HF name, corrected data pushed to the health-facility Kibana index on facility creation, and issues when POC contact details are updated or tenant Id is changed. (#2317, #2359)
- **Facility details migration data** — Fixed incorrect data produced by the facility-details migration in production, requiring a follow-up correction pass. (#2277, #2280)
- **Login-report index** — Removed a dependency on `tenant.tenants` for the login report, added explicit state-name resolution, and added/iterated a migration to backfill the login-report index accordingly. (#2336, #2352)
- **OOW total cost changing via mouse scroll** on a numeric input, and **profile-update page UI validation** bugs. (#2302, #2303, #2321)
- **Ticket-creation blocking checks** added to prevent invalid tickets from being created in certain states. (#2274)

---

## March 2026

### Added
- **AMC module and Field Planner rollout to main** — a large merge brought the AMC (Annual Maintenance Contract) module onto `main`: new Field Planner and Field-Planner-Activity microservices, a BOM (Bill of Materials) service with PDF/document generation, an AMC service with configuration, scheduled-visit, and OTP-based visit-completion endpoints (with resend-OTP), organisation-user management, activity-facility assignment with reviewer/SPOC linkage, and download/upload templates for field-plan facilities. Includes workflow wiring for AMC_REVIEWER/AMC_STAFF-to-project linkage and inbox search by jurisdiction/boundary. (#2254, #2256)
- **RMS integration for Karnataka rollout** (part of the same release) — RMS (Remote Monitoring System) alert triggers wired into ticket creation: panel-data, high-voltage, deep-discharge, zero battery-voltage, grid-level, and inverter-no-signal triggers each generate incident tickets with SLA/priority and comment payloads, deduplicated against `eg_incident_v2` (open/closed check) so repeat alerts don't spawn duplicate tickets, and a cron/db-migration to keep ticket status in sync once resolved/closed.
- **ORG Admin UI — User Jurisdiction management** — added country- and facility-level jurisdiction assignment to the Org User form, a new Jurisdiction table component, and an undo action to restore a removed jurisdiction without closing the edit modal; reworked Org Users table and user-form styling. (#2239, #2252)
- **SeM 3.0 UI — OOW/OOS/SPC/RMS ticket-flow actions** — new complaint-details actions for the full SeM ticket lifecycle: an out-of-warranty (OOW) vendor questionnaire, mark-out-of-scope (OOS) action, status-update and submit-revision actions, spare-part-change (SPC) actions, and RMS-specific ticket actions, plus a Tech POC inbox assignee filter. Follow-up fixes covered blank SLA on new business states, duplicate creation-documents in the ticket timeline, infinite re-renders on the details page, per-field validation limits on OOW/SPC vendor responses, and restrictions on ticket-type/issue-subtype selection for uninstalled facilities. (#2081, #2089, #2132, #2144, #2212)
- **RMS ticket reopen UI** — implemented a dedicated "Reopen RMS" action with its own reopen-reason field (previously missing for RMS tickets), and fixed OOS-state SPOC action validation plus an issue where the spare-part-change action reassigned the wrong assignee; also fixed two reopen actions incorrectly appearing on already-closed tickets. (#2154, #2181)
- **Is-ONM-Ready toggle / facility ONM readiness** — added facility "ONM readiness" logic in the HRMS utility/facility-activity flow, including linked-users lookup for a facility activity, an `isInstallationQcApprover` role check when resolving assigned users, and safeguards so a user can't be deleted while activities are still assigned to them. (#2241, #2213, #2235)
- **Pause RMS ticket creation when open tickets exist** — RMS-service now skips creating a new ticket for a facility if it already has an open ticket of type RMS or Theft, avoiding duplicate/noisy alerts; includes a facility bulk-search fix so the open-ticket check resolves correctly. (#2170, #2180, #2218)
- **Theft workflow conditional checks + SMS notification** — added conditional UI logic for theft-type tickets (dynamic incident-creation actions per ticket type, `FIR_DOCUMENT` as a required document type, uppercase-normalized theft-type checks), and a new SMS notification to CRM users on theft incidents, run via a dedicated cron job. (#2083, #2153, #2220, #2084, #2217)
- **Im-services uninstallation and reinstallation workflow** — new process-instance states/transitions for uninstalling and reinstalling a facility's incident-management service, with a migration to move existing process instances onto the new workflow business service, Kibana/Elasticsearch reindexing of current process instances, and handling for `PENDINGFORASSIGNMENT_THEFT` / `PENDING_RESOLUTION_SPARE_PART_NEEDED` application-status transitions. (#1997, #1999, #2149, #2151, #2219)
- **Bulk facility ingestion without boundary code** — facility ingestion no longer requires a `boundaryCode` column; the ingestion service now fetches all boundaries and their translations and derives the facility's boundary code from the State/District/Block columns in the uploaded sheet, with null-checks and same-boundary duplicate detection. (#2234)
- **Facility name localization** — facility names are now run through the localization service during ingestion/display; shipped alongside a related ingestion change that removed the "include in project" column and made HFR/NIN ID mandatory on the facility template (later partially reverted and reapplied while stabilizing). (#2245, #2240, #2249)
- **M&E dashboard average turnaround-time metric** — added a `calculateBusinessDurationForAllStates` computation and a new migration so the M&E dashboard can report average turnaround time per ticket state; extends business-hours utilities for business-hours-aware duration math. (#2162, #2166)

### Changed
- **Backend error responses now surfaced in UI toasts** — org-user create/update/delete, facility create/update, AMC Reviewer screens, Installation Reviewer screens, and project/field-plan create/update/workflow calls previously swallowed backend error bodies, showing only a generic failure toast; the actual backend message is now populated. Also cleaned up unused imports/variables across the PM, QC, org, FA and AMC frontend modules and fixed a missing `await` on the delete-organization-user API call. (#2236, #2238)
- **Tech POC inbox default filter** — the Inbox page now defaults to "assigned to me" for Tech POC users instead of showing all tickets; fixed the IM summary card not reflecting ticket totals under the new default and an assignee-filter persistence bug. (#2163)
- **Ingestion performance for facility templates** — facility-selection template generation was slow due to per-facility lookups; replaced with a bulk facility search and reduced Excel-generation work, cutting response time significantly. Project-facility download template generation was similarly optimized, adding column auto-fit. (#2211, #2168)
- **Warranty status handling** — added a `WarrantyStatus` model and Elasticsearch migration so incidents carry a computed warranty status, with corresponding row-mapper/enrichment-service changes, localization strings for the new status values, and a follow-up validator fix. (#2155, #2174)
- **Open-ticket status definition** — the "open tickets" query used for RMS pause-check and dashboards was changed from an explicit allow-list of statuses to "anything not in a closed state," fixing cases where new/renamed statuses were being missed as "open." (#2247, #2251)
- **AMC Reviewer label change** — "Inverter" relabeled to "Inverter or Charge Controller" across AMC Reviewer screens to match field terminology. (#2227)
- **HRMS/PGR access refinements** (part of the same release) — HRMS username search made case-sensitive; added `searchOnlyInBoundary` and `isActive` filters to user search; removed the mobile-number duplicate-check validation on HRMS update calls (only applied to create); and PGR access extended to the EMPLOYEE role and complaint-facilitator roles.

### Fixed
- **Facility search during facility update** — `FacilityService` was searching with incomplete criteria during update calls, causing update-time facility lookups to miss or mismatch records; the search query construction was corrected. (#2224)
- **HRMS "current owner" resolution issue** — fixed incorrect current-owner attribution used when resolving assignees for incidents/activities. (#2223)
- **Incorrect CRM mobile number in mobile side nav** — the citizen-facing mobile sidebar showed the wrong CRM helpline number; fixed and made the helpline number render conditionally rather than unconditionally. (#1812, #2228)
- **AMC configuration assignment on ingestion** — fixed AMC configuration creation so AMC staff and AMC reviewer are added to the configuration assignment regardless of which organisation was selected in the uploaded Excel sheet (previously only applied for a subset of organisations).
- **Incident priority migration** — added a migration to store `incidentType` on the priority table and fixed a duplication issue it introduced in priority lookups. (#2215, #2216)

---

## February 2026

### Added
- **RMS (Remote Monitoring System) production rollout for Karnataka** — landed the `rms-service` as a new backend module (~60 files, ~7500 lines): `RuleEngineService` evaluates alerts (deep-discharge, overcharge/poor-health, high-voltage, grid-level, inverter-no-signal, battery-voltage-zero) against facility telemetry and triggers ticket creation; `PayloadGenerator` builds im-services incident payloads from RMS alerts including boundary/district/block mapping; `DeduplicationManager` checks the `eg_incident_v2` table to avoid re-opening tickets that are already open/closed for the same facility+alert type; `SauraEmitraConnector` and `TicketStatusUpdateService` sync ticket status back once resolved/closed. Includes its own Flyway migrations and a cron-driven orchestrator. (#2107)
- **Karnataka-only boundary validation for RMS ticket creation** — `PayloadGenerator` now rejects ticket creation (returns null, skips) when a facility's `boundaryCode` is null/empty or does not start with `India_Karnataka`, replacing a previous hardcoded fallback boundary code — a hard gate to keep the initial production release scoped to Karnataka only. (#2098)
- **RMS new alert subtypes and uninstalled-facility filtering** — added `BatteryDisconnected` and `RunningOnGrid` subtypes, and the facility/data-collector clients now filter out uninstalled facilities before alerts are processed, preventing spurious tickets for facilities not yet live. (#2082)
- **SeM 3.0 UI — OOW/OOS and SPC/RMS ticket flows** — new UI actions for the "mark Out-Of-Warranty", "mark Out-Of-Scope", vendor questionnaire on OOW pending, submit-revision, and Spare-Part-Needed (SPC) flows; added an inbox assignee filter for Tech POC, blocking-UI while awaiting workflow API responses, per-action document-limit and individual field validation for OOW/SPC vendor responses, and restricted ticket-type/issue-subtype/system-functional selection for uninstalled or already-installed facilities depending on ticket type. (#2144, #2081, #2089, #2132)
- **Theft ticket conditional checks** — `Complaint.js` now makes the incident-creation action dynamic based on ticket type, adds `FIR_DOCUMENT` as a required document type specifically for theft-type tickets, and normalizes the theft-type check to be case-insensitive. (#2083, #2153)
- **Admin UI — Facility, Boundary and Organization Admin frontend modules** — new Facility Admin module with facility listing/create/edit, bulk facility upload with template download, an Activities/Assets/AMC tabbed details page, and boundary-filtered geography info; new standalone boundary-creation flow (state/district/block boundaries, custom relationships) later refactored into a modal; new Organization Admin module with platform vs vendor organization tables, organization details page, and CRUD for organization users (create/update/delete, POC management, role-restricted by org sub-type). (#2030, #1884, #1889, #2002, #2024)
- **Admin UI backend** — new/updated APIs across Facility, Vendor, User-Vendor, Asset, AMC-configuration, and Field-Planner registries: asset search now takes `facilityID` plus optional `assetType`/`assetStatus`/an array of `serialNumbers`; facility search returns full boundary detail and can filter on country/state/district/block; new Create-User-and-Add-to-Organization, Edit-Organization-User, Edit-Organization-POC-User and Remove-User-from-Organization endpoints; ingestion service accepts a `facilities_onm_ready` query param on bulk facility ingestion; auto-creates State info in MDMS when a state boundary code is missing; paginated boundaries API; facility admin role added as project manager. (#2134, #2050, #2035)
- **"Is ONM Ready" toggle for bulk facility add** — bulk facility-upload page gained an explicit ONM-ready toggle, and installation reviewers no longer see facility boundary incorrectly rendered under the "block" boundary column. (#2048)
- **Default-OTP bypass for AMC scheduled-visit validation** — `ScheduledVisitService` gained a config-driven bypass path — when enabled, the visit's OTP reference is compared directly against a configured default OTP string instead of calling the real OTP-validation service, intended for staging/demo environments. (#2057, #2058)

### Changed
- **RMS operational fixes** — removed timeout from the RMS rule-engine cron job so long-running scans aren't killed mid-run; corrected the reporter user/UUID used when RMS-generated tickets are created; minor URL fixes tied to the ONM-ready rollout and SYSTEM_USER role added to the allowed roles for ONM-ready facility activity updates. (#2070, #2115, #2118, #2135, #2145)
- **Field Planner / Activity search optimization** — activity query builder gained a sort-direction option, and the ingestion-service's fieldplan facility endpoints/excel template generation were reworked for performance. (#2099)

### Fixed
- Asset search pagination on the Facility Admin UI was returning incorrect pages — hook fixed to respect page/size params correctly. (#2055)
- Facility boundary was incorrectly displayed as "block" in the facility details page — fixed to read the correct boundary field. (#2049)
- Boundary and Organization Admin UI: null fields no longer shown, fixed submit-bar overlapping selected options, and fixed boundary search pagination that was returning incorrect result pages for a given offset. (#2091, #2092)

---

## January 2026

### Added
- **Admin UI backend — initial landing** — first arrival of the Facility/Vendor/User-Vendor/Asset/AMC-configuration/Field-Planner registry APIs described above — facility search with boundary/country/state/district/block filters, asset search with array-based `assetType`/`serialNumbers` filters and an `isOperational` flag, org-user CRUD endpoints, ingestion service `facilities_onm_ready` support, auto-creation of MDMS State info when a boundary code is missing, and the "isONM ready" flag being set (and the facility pushed to Kibana if not already indexed) once a facility's installation is approved. (#2020, #1879, #2007, #1811)
- **Improved RMS data-collection pagination** — the data collector now reads the panel-graph API's actual pagination block (page, totalPages, totalRecords) to decide whether to fetch another page, replacing the previous heuristic of stopping once a page returned fewer than `pageSize` facilities — which could under- or over-fetch when the upstream API changed page sizing. (#1893)
- **RMS alert-subtype fix for poor battery health** — `RuleEngineService` now maps a `batteryHealthInfo` value of `poorHealth` (in addition to `overcharge`) to the `OVERCHARGING` alert subtype, so poor-health readings generate the correct ticket subtype instead of falling through to the default `DEEP_DISCHARGING`. (#2017)

### Changed
- **HRMS username/employee-code search made case-sensitive** — dropped the lowercase comparison and now matches employee codes/usernames with exact case, changing search behavior for callers relying on the previous case-insensitive lookup. (#1993)
- **HRMS mobile-number duplicate-check relaxed for updates** — the employee validator no longer rejects an employee update purely because the mobile number matches another existing record, addressing false-positive duplicate errors when editing an existing employee's own record. (#1911, #1895)
- **PGR access role expansion** — `pgrAccess` in the shared UI library was extended to grant PGR (grievance) access to the EMPLOYEE user role and then further generalized to the VIEWER role, with the access-check function simplified/refactored. (#1996, #1984, #1976)
- **Ingestion-service excel dropdown fix** — reworked the dropdown-generation logic and added a dedicated logging module, fixing an issue where MDMS-driven dropdown values in the bulk-upload Excel templates were not populating/validating correctly. (#1894, #1978)
- **Increased asset-search result limit on facility details page** — raised the page size used when fetching a facility's assets, fixing truncated asset lists for facilities with many assets. (#1982)

---

## December 2025

### Added
- **AMC module: Field Planner microservice** — A brand-new `field-planner` microservice was built out over the month: APIs for field plan creation/search/update, Field Plan Facility creation, and a separate Field Planner Activity service with activity-facility update/unassign endpoints. Bill of Materials (BOM) support was added with document-list attachments and PDF generation, plus download/upload Excel templates for field plans and facilities, ingestion for field plans, and dedicated egov-workflow business services for both Field-Planner and Field-Planner-Activity. (#1674, #1689, #1695, #1697, #1711, #1817)
- **AMC module: Activity Facility User & notification flow** — When a field plan moves to `SCHEDULED`, the system now auto-creates Activity Facility records for all linked facilities and populates a new `activity_facility_user` table so Reviewer and SPOC users can see every facility belonging to their field plan; users receive an email with default credentials and an app link at that point. Deduplication tickets are handled on the backend, and bulk workflow-update support was added for activity facilities.
- **AMC service: scheduled visits with OTP** — New `asset-amc` service endpoints implement AMC configuration and scheduled-visit workflows, including OTP generation/validation and resend-OTP for visit completion, duplicate-assignment guarding on schedule-visit/AMC-configuration creation, and linking the AMC_REVIEWER/AMC_STAFF roles to the project so reviewers can see it from the UI. A cron job hits a visit-update endpoint for all visits nearing their scheduled date to auto-transition state, and an "installation completion" side effect sets `installationDate` to `lastModifiedTime`.
- **AMC ingestion templates** — Added download/upload ingestion endpoints and Excel templates for `amc-asset` data, later refined for field name corrections and validation flow. (#1678, #1720, #1815)
- **AMC Reviewer UI** — New frontend module with AMC setup screens, primary reviewer screens, visit-details pages, boundary-based localization, role-based filtering during user assignment, rejection-reason management, and role-restricted access to the module. (#1743)
- **Field Plan Manager role provisioning** — Field Plan Manager can now create Field Staff and Field Supervisor roles directly inside a Field Plan. (#1830, #1831)
- **Field Plan installation-activity user management** — Role filtering was added to the field plan activity-details page and to activity-assignment/user-search so only appropriate roles surface for assignment. (#1832)
- **RMS Integration (rms-service microservice)** — The full `rms-service` module landed on main (58 files, ~7300 lines): `RuleEngineService` implements trigger rules for panel data, high voltage, battery-voltage-at-zero, deep discharge, and grid-level events, creating two tickets per trigger; `DataCollectorService` and `SauraEmitraConnector` pull inverter/panel telemetry; `DeduplicationManager` checks `eg_incident_v2` to avoid opening duplicate tickets for an already-open/closed alert; `PayloadGenerator`/`RMSOrchestratorService` build IM-service ticket payloads with facility/asset boundary codes; scheduler cron jobs sync/validate center-to-HFR ID mappings; and a status-update service in im-services closes tickets back out to RMS once resolved.
- **HRMS user-search filters** — Added `searchOnlyInBoundary` and `isActive` query filters to user search so callers can restrict lookups to a boundary and to active employees only, with a null-check follow-up fix. (#1835, #1842)

### Changed
- **Multi-tenant migration: Health Facilities, Boundaries, Incidents (Saura eMitra → in-tenant)** — Large migration effort moving facilities, incidents, employees, documents, MDMS, and localization data into the `in` tenant: `eg_incident_v2` gained a `facilityId` foreign key and boundary-code population; facility boundary codes are now generated from block code + facility ID; retry/dedup logic was added to MDMS and localization migration calls; idgen/ticket-number generation was switched to derive its format from the ticket's boundary; and the Incident Management UI was unified (single login screen, boundary-based facility/ticket-creation flows, jurisdiction-restricted inbox search). (#1627, #1606, #1650, #1668, #1670, #1690, #1698, #1699, #1700, #1679)
- **Inbox search jurisdiction/boundary scoping** — Inbox and deduplication search were migrated off raw `tenantId` onto boundary code, with follow-on fixes for total-SLA-remaining computation, the PHC-count dashboard, and the escalation-matrix weekly email, plus a CodeQL fix in the inbox service. (#1687, #1725, #1748, #1775, #1776)
- **User search returns empty list instead of erroring** when the requested user isn't found within the caller's boundary, rather than surfacing a failure or unfiltered result. (#1736)
- **PHC count dashboard migrated to facilityId** — reworks the PHC dashboard aggregation to key off `facilityId` and boundary code rather than tenant, alongside continued AMC-module work merged in the same release (174 files / ~13.5k lines touched). (#1817, #1821)
- **Global config / jurisdiction refactors** — State boundary info is now read from global config instead of being hardcoded; UI restrictions on boundary levels within employee jurisdictions were removed and replaced with an explicit state filter and jurisdiction search criteria; duplicate boundaries returned by the boundary-relationship search API are now de-duplicated; and a mapping-table gap caused by duplicate HFR/NIN IDs was fixed. (#1773)
- **Escalation flow after migrations** — Fixed the weekly escalation CSV export, corrected user-fetch logic for country-level escalation, and fixed the filestore download URL/arrow rendering for non-functional-status tickets. (#1750)
- **District/block names sourced from localization service** instead of raw codes. (#1754)
- **RMS reporter type on ticket creation** — the enrichment service now pushes `RMS` as the `reporterType` when the creating user holds the RMS role, so RMS-originated tickets are attributable. (#1715)
- **Migration hygiene fixes** — Removed the phone-number duplicate-uniqueness check in HRMS to unblock migrated users with shared numbers, added a migration for workflow auto-escalation data, and renamed the MDMS data migration file so it runs before the employee migration (ordering bug). (#1740, #1777)

### Fixed
- **AMC rule-engine and alert fixes** — corrected alert-subtype classification and inverter rule logic across the RMS alert repository/rule engine; a follow-up patched payload-building edge cases. (#1852, #1844)
- **RMS ticket comment format** — Fixed RMS-sourced ticket comments rendering in the wrong format on the complaint-details timeline, and a related fix stopped workflow comments from appearing twice in the timeline. (#1746, #1759)
- **Facility Migration – POC contact number** — Added a targeted migration to fix incorrectly migrated point-of-contact phone numbers on facilities, plus a companion fix to the facility update API and its persister config. (#1782)
- **AMC facility data validation** — Fixed the AMC create form allowing progression without a valid file upload, fixed a loader that was blocking toast notifications, and tightened the asset-AMC validation flow; a related fix stopped the create-AMC form from being submittable with no file at all. (#1820, #1804)
- **AMC ingestion/cron request-info fix** — Fixed the request-info payload used by the asset-AMC ingestion service and the visit-reminder cron job. (#1818)
- **Inbox search failure on create-complaint page** — Fixed a failure in inbox search triggered specifically from the create-complaint flow. (#1778)
- **Rate option gating** — Changed the "rate" action on complaint details to be permitted based on the acting username rather than role alone, and stopped it from being incorrectly offered as a takeable action. (#1723)
- **Mobile CRM helpline number** — Fixed an incorrect CRM mobile number shown in the mobile side navigation and made its rendering conditional so it only appears when configured. (#1807, #1809)
- **Remaining-SLA count** — Fixed an issue where remaining-SLA computation produced incorrect (including negative) totals after the boundary-code migration. (#1735)
- **IM Services asset-management UAT migration fixes** — Fixed missing log context and HRMS search behavior during facility migration, and fixed missing state/health-facility localizations in the Elasticsearch indices. (#1734)
- **AMC total-count and duplicate-assignment fixes** — Fixed incorrect total counts on AMC-configuration and schedule-visit search, and added checks to prevent duplicate assignments when creating schedule visits or AMC configurations.

---

## November 2025

### Added
- **AMC (Annual Maintenance Contract) module goes live end-to-end** — The `asset-amc` microservice was built out with a workflow-driven schedule-visit lifecycle, an `amc-persister` for indexing, and a schedule-visit endpoint extended with OTP generation/verification (resend-OTP flow) so field engineers must confirm an OTP before a visit can be marked complete. A cron job periodically calls a visit-update endpoint for every visit nearing its scheduled date, auto-transitioning visit state instead of relying on manual polling. On installation completion, the asset-amc service now reacts as a side effect: it looks up the AMC record via a corrected search API URL, aligns field names, and sets the AMC installation date from the asset's last-modified time rather than a separate input. Ingestion endpoints were added for amc-asset supporting template download/upload for bulk AMC asset data.
- **Field Planner microservice reaches feature completeness** — Built from scratch: field-plan CRUD and search, per-facility field-plan-facility assignment/unassignment, a dedicated Field-Planner-Activity microservice, workflow state machines for both, an Activity Facility User table (auto-populated with Reviewer/SPOC users once a field plan moves to SCHEDULED, with default-password credential emails), and organisation-user create/search endpoints. Bill of Materials (BOM) support was added on the backend: SQL migration, create/search BOM APIs with a document list, PDF generation for BOM, and a "save BOM PDF to filestore" endpoint, plus multi-BOM form support wired into the reviewer installation app. Facility upload/download templates, filters, bulk workflow transitions for activity facilities, and an ingestion pipeline for field plans were also added. This module was repeatedly re-merged across several "Staging field planner" and "Amc module" PRs as the branch was progressively promoted through environments — functionally one continuous build-out. (#1652, #1635, #1632, #1631, #1629, #1601, #1695, #1689, #1674)
- **Deduplication handling on the backend** — When a field-plan facility is created, the backend now checks for and handles potential-duplicate tickets against existing records rather than silently creating new ones.
- **HF type and age-bucket escalation flow** — Added new escalation logic keyed on health-facility type and ticket age bucket, initially implemented with a scheduled annotation and then reworked to run via an external Kubernetes CronJob instead, for better operational control over the analytics scheduler.

### Changed
- **Installation Reviewer API migration/refactor** — Migrated the Field Plans list, Facility list, and Facility Details pages from legacy APIs to the new field-planner/activity services; migrated workflow-update and bulk-workflow-approve calls; restructured the UI service layer; and changed asset reads to be keyed by activity-facility ID instead of the old facility ID. Bundled in earlier PM-module UI bug fixes for project creation, field-plan creation, and activity-assignment deletion logic. (#1630)
- **Operational Lead role renamed to SPM** across role checks and workflow assignment logic, requiring corresponding role-check updates wherever the old role name was referenced. (#1596)

### Fixed
- **SLA computation corrections continued from October's rework** were carried into November's staging merges: total-SLA-remaining logic, business-hours-elapsed calculation, and priority-table-driven SLA lookups were re-validated and folded into the escalation/weekly-report pipeline.
- **Stylesheet truncation due to unpkg CDN bundling** — Local/staging builds were losing part of their CSS because of how the unpkg-based stylesheet bundling truncated large files; fixed the bundling approach and adjusted local setup styling accordingly. (#1653, #1654)
- **Field Plan Facility count bug on the Installation Reviewer QC App** — counts of assigned/unassigned facilities were wrong after unassign operations; fixed alongside a fix for the unassign-facilities count and the asset start/end-date validation issue.
- **Visit-cronjob RequestInfo fix** — the AMC visit-scheduling cron job was sending a malformed/incomplete RequestInfo on its update calls; corrected so scheduled cron-triggered update calls authenticate/authorize correctly.
- **Escalation weekly-report template fixes** — several passes fixing arrow/direction indicators, color scheme, and breached-ticket counts in the escalation email templates.

---

## October 2025

### Added
- **Potential Duplicate Ticket UI** — New alert card surfaced during ticket creation when the system detects a probable duplicate, plus a "potential duplicate" tag shown in the inbox list so agents can spot likely dupes before triage; a follow-up fix corrected the popup action button coloring. (#1545, #1539, #1541, #1543)
- **Escalation matrix cron job** — Added a full escalation subsystem to im-services-analytics: a scheduler, SLA-breach-detection service, Elasticsearch escalation service, weekly-report/weekly-report-email services, a CSV-generation service, and a large escalation controller — computes SLA breaches from the Elastic index, generates CSV/email reports, and dispatches escalation notifications on a schedule rather than on-demand. (#1434, #1526)
- **Priority table replaces MDMS for SLA priority lookup** — Added a priority repository/query-builder/row-mapper backed by a new `im_services_priority` table, with the priority-SLA service now querying this table instead of MDMS and falling back to a default priority when no row matches. im-services-analytics was updated to read priority from the search index rather than recomputing it. (#1520)
- **Total SLA Remaining logic** — Implemented new `computeTotalSlaRemaining` logic replacing the old business-hours-util-based computation; follow-up fixes corrected an incorrect remaining-SLA value, a wrong Elastic index name, and removed a redundant empty-processInstances guard that was short-circuiting the calculation. (#1502, #1503, #1504, #1523, #1542)
- **Installation Reviewer BOM completion report UI** — Reviewers can now view supporting documents and the BOM completion report attached to a project; reject/flag-for-QC actions now attach the relevant review documents; fixed asset images/videos not being visible after field-staff submission; refactored how support documents and asset media are read off the workflow object. (#1500, #1484, #1490)
- **Project Manager UI — Field Plan Creation** — Integrated field-plan search/create/update APIs into the Project Field Plans page, added the Activity Details page UI with MDMS/Organization API integration, and wired up activity assign/search/update/delete APIs plus a project-status-update API; edits are restricted once a field plan leaves draft status. (#1485, #1492)

### Changed
- **Workflow state-transition fixes** — Corrected naming and flow bugs in the workflow state machine that were producing incorrect state transitions. (#1536, #1533, #1534)
- **Level-one ticket / escalation flow logic** — Reworked how the first-level escalation ticket and its associated email templates are selected, and fixed a duplicate-ticket-generation issue in that path. (#1537, #1580, #1583)
- **AssetConstants / asset validator changes** — Modified asset constant field names and the asset validator; added a rule to skip warranty validation when the warranty value is null or zero; normalized `phc-subtype` values for consistency. (#1506, #1508, #1511, #1512)
- **Field Plan Creation UI robustness** — Fixed submitting the field-plan creation form when assignment entries had been deleted mid-flow, refactored form navigation (including a back-navigation confirmation alert), refactored the field-plan-facility data API calls, and added a loading indicator during the API round-trip. (#1505, #1493)
- **Activity Details PO Number / dropdown fixes** — Fixed the PO-number input re-rendering on every keystroke (losing focus/cursor), fixed the custom dropdown's displayed value not matching the selected option, and corrected activity-assignment ordering. (#1522, #1509, #1513)

### Fixed
- **Missing SLA value on the HCR ticket-details UI** — SLA remaining/total fields were blank for certain HCR ticket types; fixed alongside the broader total-SLA-remaining backend rework landing the same week. (#1524)
- **Breached-ticket count wrong in weekly escalation report** — The weekly report undercounted/miscounted tickets that had breached SLA; fixed as part of the same escalation-template cleanup pass that also corrected arrow indicators and color scheme in the report emails. (#1586, #1587, #1589, #1590, #1591, #1592)
- **Reverted a prior "Fixed casing issue for health care center type" change** — The earlier casing fix caused a regression and was reverted, indicating the original fix needs to be redone more carefully rather than silently dropped. (#1145, #1488, #1489, #1516)
- **Missing incident type/subtype on a specific ticket** — One-off data-correction fix for a ticket that was missing its classification fields, applied directly rather than via a generic backfill. (#1487)

---

## September 2025

### Added
- **Project Manager UI — project creation and field-plan workflow** — New Project Manager module for drafting and managing solar-installation projects, including auto-generated project names (with duplicate-name handling for bulk creation), draft project creation, a facility-data upload/validate/download flow, and a field-plan creation flow with start/end date restrictions. A follow-up fix added facility unlinking when a new template is uploaded and validation on MDMS dropdown values in the ingestion service, plus a missing-import bugfix. The same release also folded in earlier installation-ui module cleanup (removed redundant core modules/libraries, upgraded design-system package versions) needed to unblock the QC/PM build. (#1459, #1466)
- **Java-based DB migration framework** — Migrations for im-services now ship as versioned Java/Flyway-style migration classes alongside the existing SQL scripts, rather than hand-written SQL only — laying groundwork for more complex, logic-bearing migrations like the dummy-ticket cleanup below. (#1384, #1395)
- **Measurement ID templating for GA4 across state UIs** — Replaced the per-state GA4 measurement ID with a templated substitution applied uniformly in the CI workflows for assam, gujarat, maharastra, manipur, meghalaya, mizoram, nagaland, odisha, and sikkim UI builds, and added the corresponding nginx directive so each tenant build gets its own GA4 ID at image-build time. (#1478, #1480)

### Changed
- **SLA computation overhaul in im-services-analytics** — Reworked total-SLA calculation to use the correct business-hours-elapsed logic instead of raw wall-clock time, adding a workflow service and expanding the priority-SLA/service-request models to pull workflow history needed for the computation; also fixed a previously negative total-SLA count and resolved SonarQube findings. (#1389)
- **Installation Reviewer UI continued refactor** — Iterated on the facility-details page (localization of audit trail and rejection-reason modals, styling), fixed side-checkbox selection and blank asset-detail values, made the QC card, field-plan list, facility list, and facility-details pages responsive, and fixed font/localization glitches in rejection-reason modals. (#1333, #1288, #1307, #1332, #1339)
- **Service definition (MDMS) caching** — the service-elements library was changed to cache service definitions on the client rather than re-fetching them on every call, reducing redundant MDMS lookups. (#1346)
- **computed-sla-im-services indexer** — Iteratively patched error handling while updating the computed-SLA index, fixing two rounds of indexing errors surfaced after the SLA logic change above. (#1406, #1407, #1408)

### Fixed
- **PHC count discrepancy** — Latest round of the recurring "Fix phc issue" series — patched the incident service alongside the standalone PHC-count-update script, with a final commit specifically fixing a logging bug in the count script itself. (#1345)
- **Legacy/dummy ticket cleanup, take two** — Re-deleted 8 dummy health-center tickets in production via a proper Java migration after the first manual deletion (done directly against prod) had to be reverted because it wasn't going through the migration pipeline. (#1410, #1411, #1357, #1373)
- **Reject-reason array-vs-single-value indexing bug** — Only the latest rejection reason is now pushed to the Elasticsearch index instead of the full historical array, fixing incorrect/stale reject-reason display in Kibana dashboards; the fix had to be reapplied a second time after a regression. (#1356, #1371)
- **Closed-ticket handling in inbox/SLA queries** — Removed an if-condition that was excluding closed tickets from a query, fixed a bug causing the current workflow state to be counted twice in aggregations, and updated a separate query to explicitly include closed tickets in its result set — three related edge cases in how closed tickets flow through indexing/reporting. (#1396, #1397, #1399)
- **Issue subtype spelling correction** — Added a migration correcting a misspelled `OtherFan` issue subtype value both in the live incident table and retroactively in the audit-logs table, so historical records display the corrected spelling too. (#1421, #1425)
- **Filter fixes** — Fixed filter ordering and a related filter-application bug in the ticket listing/inbox filters. (#1361, #1369)

---

## August 2025

### Added
- **PWA compliance** — Implemented full Progressive Web App support for the citizen/employee micro-UI — web manifest, Android/iOS launcher icons at all required resolutions, and service worker registration — enabling "add to home screen" and offline-shell behavior. Icon assets were swapped out shortly after in favor of new PWA icon designs, and icon sizing was corrected again. (#1217, #1225, #1251)
- **GA4 analytics integration** — Added Google Analytics 4 event tracking for login, page views, filter usage, and video play/pause interactions on the ticket-detail page, wired through a new Analytics utility. Env-specific GA tag configuration was split out so each deployment environment reports to its own GA4 property, and a follow-up fixed several tracking bugs and added facility name and state name as tracked dimensions. (#1273, #1313, #1315, #1331, #1335)
- **Installation Reviewer approve/reject workflow** — Added approval, rejection, and "flag for QC" actions on the installation-details page, including an audit trail with rejection reasons, common-asset images/videos in the details view, and a bulk workflow-update API integration so a reviewer can approve/reject multiple facilities at once; query hooks were refactored to properly invalidate cached facility data after an action. (#1289, #1278)
- **Project inbox search index / project search v2** — Built a dedicated Elasticsearch indexer for projects and implemented inbox-search-v2 support for the project business object, including state/district/boundary-code fields, `slaHoursRemaining` and `escalations` as filterable fields, and an escalation-matrix filter. Added a companion aggregation endpoint in im-services-analytics that counts incidents by status via a new Kafka listener, and added facility/state/district counts to the PHC master-list index. (#1286, #1275, #1249, #1269, #1284)
- **CLOSEDAFTERREJECTION workflow state** — Added a new terminal business-service state for tickets closed after rejection, backed by a soft-delete SQL migration and a Java migration that patches the existing business-service workflow definition to include the new state/transitions. (#1274, #1262, #1267, #1268)

### Changed
- **Installation UI refactor** — Reworked "no records found" messaging, made the search filter trigger on Enter, fixed a facility count discrepancy in field-plan info, and swapped the web styling library for a local package build — the local-styling-library change was reverted a week later after it caused regressions. (#1279, #1250, #1261)

### Fixed
- **PHC count discrepancy (recurring)** — First appearance of the "Implement script to update phc count in existing phc" fix — a standalone script plus service/indexer patches to recompute and correct the health-facility count stored against existing PHC records, re-run and refined four times over the month as new edge cases in the count logic surfaced. (#1308, #1314, #1317, #1342)
- **User login report** — Reworked the user service to write a separate login-report entry per login event (instead of overwriting one row) and fixed health-center/block/CRM-role attribution logic used when generating the report. (#1228, #1229)
- **SLA blank-in-inbox regression** — A prior fix for SLA showing blank in the inbox page had introduced a different bug; it was reverted and replaced with a corrected fix that only touches SLA-remaining calculation without breaking the inbox render path — the revert/reapply had to happen twice across the month. (#1219, #1220)
- **Mobile-view layout fixes** — Fixed the tenant logo overlapping the tenant-selection dropdown on small screens via a one-line style fix, and corrected PWA icon sizing. (#1256, #1257, #1251)
- **DB cleanup script** — Added a one-off script to reconcile database rows with the corresponding Elasticsearch index after the PHC-count and dummy-ticket fixes left the two out of sync. (#1338)

---

## July 2025

### Added
- **Legacy ticket ingestion** — New ingestion-service API accepting legacy ticket records, mapping old issue type/subtype to new taxonomy (falling back to the original type/subtype when no mapping exists), tenant-specific CRM/profile-name handling (Mizoram and others), duplicate-record detection, comments truncated to 256 chars, and `NONFUNCTIONAL` renamed to `NON_FUNCTIONAL`. (#1025)
- **`triggerParallelWorkflows` column** added to the `ef_state` table via migration — the DB-level enabler for the workflow-v2 engine to fire parallel workflow transitions from a single state. (#982)
- **User login report** — new controller/model and user-service logic recording a distinct login entry per user with health-center/block context and a CRM-role check. (#1176, #1119, #1124, #1149, #1158, #1160)
- **Filestore signed URLs** — storage service/controller/repository extended to mint signed URLs for Minio-backed artifacts, plus a 404 fallback page. (#970)
- **Sprint 10 inbox filters** — district, block, and system-functional filters added to the inbox filter component/hooks, and the health-care-centre filter reworked; also fixed SLA rendering blank in the inbox list. (#1180, #1172)

### Changed
- **Ticket update/decline mapping** reworked across two iterations: tenant ID added to the input payload, unused variables removed, and duplicate tickets now get auto-declined specifically for the Nagaland tenant. (#1109, #1169)
- **SLA computation** patched repeatedly for the "Incident" business service — fixing remaining-SLA and defined-total-SLA values that were being computed against the wrong business service. (#987, #988, #989, #990, #1038, #1053, #1057)
- **V2.0.0 prod deployment** merged in the video upload & streaming pipeline, the updated SLA feature, and dashboard reporting, while also folding previously separate services (boundary-service, asset-registry, facility-registry, im-services-analytics, processor-service, project-service, vendor-registry) and their CI workflows into the main tree. (#950)
- **OpenTelemetry/tracer cleanup** — OTel config stripped from workflow-v2's build file, briefly re-added/downgraded, then removed again as the tracer dependency was destabilizing builds. (#972)
- **Sprint 10 UI fixes** — side-navbar alignment/overflow issues, upload-button alignment, localization for health-care-centre names in complaint details, CRM/state-manager healthcare dropdowns populated correctly, breadcrumbs removed from profile page, resolve-action attachments restricted to jpeg/png, HCC-type casing corrected on ticket details, and auto-login via query params added. (#1148)
- **ffmpeg/Dockerfile refactor** for processor-service to address recurring ffmpeg processing failures.

### Fixed
- **Multi-document filestore download URLs** — enrichment-service/index-view simplified to stop generating duplicate/incorrect signed URLs when a ticket had multiple attached documents. (#971)
- **File-upload error messaging** — corrected the wrong error text shown on unsupported/oversized uploads. (#1026)
- **Video player fallback link styling** — CSS fix for the fallback-download link. (#969)
- **Workflow transition / send-back regression chain** — a null-check change in the enrichment service broke send-back and was reverted, then correctly reapplied in the transition service with its test temporarily skipped while the fix stabilized. (#993, #995, #996, #997, #998, #999, #1000)
- **Hot fix on prod** — added an endpoint to bulk-update legacy subtypes in the DB and wired workflow to update old process instances; several attempts to push `systemFunctional` field updates through an indexer topic broke inbox search v2 and were reverted, landing on a safer migration-based fix. (#1168)

---

## June 2025

### Added
- **Priority-based SLA computation endpoint** — new im-services-analytics controller/priority-SLA service computing SLA against configured business hours per priority; iterated through several fixes for priority assignment and a negative-totalSLA bug. (#768)
- **Priority-aware business service routing** — the workflow/IM service now selects the workflow business service from ticket type+subtype priority via a new priority enum, replacing a single hardcoded business service. (#785)
- **totalSLA computation in im-services** — new business-hours utility; the workflow service computes total SLA from MDMS business-hours config, and the workflow State model gained SLA fields. (#812)
- **Kibana/audit index enrichment** — progressively added fields for reporting: audit-index assignee/assigner fields, new localized dashboard fields via a new localization service, `migrationId`/`legacyId`/`filedDate` in the row mapper, mapped-vendor-name field, and a broader overall-SLA/"new kibana fields" rework of the enrichment/workflow/index-view services. (#918, #922, #891, #895, #949, #961, #962, #912)
- **"Assign to me" fix for SPM role** plus tenantId correction for the complaint-facilitator role, touching notification/workflow tenant scoping. (#880)
- **Reopen-after-resolution** action for CRM users with an extra reopen reason, plus a 50MB cap on uploaded video size. (#941)
- **Filed date** now surfaced in the complaint-details UI. (#943)

### Changed
- **Video upload/HLS pipeline hardening** — the recurring "multiple call" bug on image/video attach after ticket creation was fixed across several passes, Kafka properties updated to stop out-of-order/duplicate message delivery, async branches removed from the storage/video service because Kafka was delivering messages out of order, the video consumer hardened against processing/streaming failures, the unused video-quality-processor class deleted, the Dockerfile updated to install ffmpeg/ffprobe/cpulimit, and 1440p transcoding calls removed in favor of a "processing — download instead" fallback message/UI. (#884, #885, #888, #883, #887, #924, #810, #920, #947)
- **Filestore/Minio hardening** — image-upload state-management fixes in the create-complaint flow, the Minio client/repository/properties reworked, folder-name formatting fixed, and the filestore download endpoint updated then patched again after it surfaced a wrong sendback-reason error. (#896, #898, #873, #946, #957, #958, #960, #966)
- **Inbox query/role rework** — the nearing-SLA query builder was rewritten twice after date-math/status regressions; business-service name is now read from the workflow-service response instead of a hardcoded string so status filters and vendor-list roles resolve correctly; assignee/assigner roles trimmed from the process-instance payload to fix HCR inbox failures; role-based SLA now displayed per role in inbox; duplicate inbox API calls fixed. (#800, #833, #829, #858, #859, #805, #831, #869, #804, #881)
- **Workflow-v2/PGR access** — jsonmapper error fixed by adding fields to inbox's workflow State model; autoescalation consumer logic changed then rolled back after regressions; complaint-facilitator roles added to PGR access control; mandatory commit/attachment enforcement now tied to the selected workflow action, with dead inactive-sendback-option code removed; hardcoded tenant removed from the enrichment service; `systemFunctional` added to the IM search query; decline-action logic fixed. (#814, #793, #826, #844, #851, #783, #781, #815)
- **db-migration housekeeping** for im-services — the legacy-fields migration script renamed to a new timestamped filename to fix ordering conflicts. (#934, #937)

### Fixed
- **Vendor registry** — the service-request repository was simplified to resolve vendor registry lookup issues. (#808)
- **Facility-service integration** — call bugs fixed in asset-registry's facility-linkage utility. (#802)
- **Checkbox tap-area misalignment** in the complaint UI. (#933)

---

## May 2025

### Added
- **Project Service** — brand-new Spring Boot service modeled on DIGIT's Health Campaign project domain — ships repositories/services for project, project-beneficiary, project-facility, project-resource, project-staff, and project-task, plus a location-capture repository/service and a user-action consumer, wired through Kafka consumers for beneficiaries, facilities, resources, staff, and tasks. Includes its own GitHub Actions deploy workflow and query builders for project addressing and targets. (#661)
- **Asset Registry service** — new service for tracking solar-installation hardware (panels, inverters, batteries) at facilities. Initial commit lays down the API controller, MDMS/Idgen/Workflow/UserUtil helpers, and model classes (Asset, AssetAMC, AssetAMCVisit, PanelDetails, InverterDetails, BatteryDetails, bulk-create request/response). Follow-up commits add the actual asset service, repository, row mapper, and facility-linkage utility to implement create and search APIs against facility data, plus DB migration wiring in the CI workflow; an actuator health endpoint and a dedicated deploy workflow followed. (#598, #720, #749, #757, #690, #669)
- **Facility Registry rewrite** — the service (created in April) is substantially reworked — adds a boundary validator, facility MDMS validator, facility query DAO, idgen util, MDMS util, and a service-request repository, replacing the earlier ad-hoc controller/service split with a cleaner API controller. Adds facility-code regeneration, uniqueness constraints on facility name + boundary code, update/getSummary/search APIs, and n+1 query cleanup. Earlier in the month, update/getSummary/search endpoints had already been added against refreshed MDMS master data, and a total-count field was added to facility search. The CI/CD workflow for the service also landed this month. (#709, #619, #760, #644)
- **Facility/asset/vendor/project ingestion (bulk onboarding via Excel)** — the Python ingestion-service gains a facility-selection template generator, a "supervisors" ingestion flow tying facilities to HRMS supervisors and projects, and a project ingestion script. A rework reconnected the facility ingestion client to talk to the real Facility Registry instead of an earlier mock stub (which was deleted), and fixed template generation/ingestion endpoints. A "project facility selection" ingestion script was added, the field-plan-to-staff flow in facility ingestion was fixed, and MDMS-driven dropdown validation plus cell-locking were added to the generated Excel templates so unsupported values can't be entered. (#687, #679, #645, #716, #617, #691, #762, #756, #696, #724)
- **`systemFunctional` field on incidents** — new DB column and incident-model field capturing whether the reporting system/equipment is still functional; surfaced in ticket creation and in ticket details/inbox. (#764, #765, #725)
- New popup functionality added to the citizen/employee UI. (#766)

### Changed
- **Elasticsearch inbox query rework** — query restructured to fix a memory hold-up under load, targeting the V2 inbox query builder used for ticket search/SLA display. (#595)
- **Boundary Service pagination** — the boundary service/controller extended to accept `boundaryType` on the paginated relationship API, and the paginated boundary re-ingestion script fixed so it can be safely re-run without duplicating data. (#610, #616)
- Facility service hardened with additional field/boundary validations and a cron-driven consistency job for facility/boundary cross-checks. (#647)
- Build fixes for vendor-registry (repeated build failures) and Java-version/OpenTelemetry dependency cleanup across services to stop CI breakage introduced by the e4h-services restructuring. (#665, #693, #682, #683, #684, #685, #694)
- UI upload flow reworked for two tracked bugs: upload/validation issues fixed in the file-upload components. (#659, #686)
- Upload axios timeout added to prevent hangs on large uploads; slow-upload bug fixed with null-safe token extraction. (#676, #602)
- Gender field removed from citizen/employee profile screens. (#745, #746)
- SMS notification locale hardcoded to `en_IN` regardless of tenant locale, fixing incorrectly localized SMS. (#747)

### Fixed
- **Nearing-SLA query miscalculation** — the inbox query builder's nearing-SLA condition fixed for Odisha tenant data; this follows on from the April SLA-query rewrite. (#721)
- **Vendor list role-resolution / vendor-registry issues** — vendor-registry build file and properties corrected after the service split, plus vendor build failures fixed. (#690, #693, #665)
- Partial-search bug in the V2 inbox query fixed as a UAT hotfix. (#618, #620)
- Boundary-service ingestion bugs fixed in the Python ingestion service — corrected boundary code handling and the vendor data processor factory. (#609, #611)
- Hardcoded tenantId values removed/fixed in several places: the enrichment service, a non-state-tenant condition added to a search/enrichment path, and the auto-escalation job — part of an ongoing multi-tenant cleanup. (#730, #722, #643)
- Facility/MDMS mismatch fixed after MDMS master data changes affected facility validation. (#678)
- Toll-number display bug fixed in the CRM mobile screen. (#675, #677)

---

## April 2025

### Added
- **Health Facility Registry service** — first commit of a new Spring Boot service — controller, service, repository, Kafka consumer/producer, and the full facility domain model (Facility, FacilityAddress, FacilityAssessment, create/update request/response, summary) plus the initial DB migration and persister config. This is the foundation that May's facility work (registry rewrite, code regeneration, search/update APIs) builds on. (#543)
- **Ingestion Service** — new standalone Python service for bulk data onboarding via Excel — file-ingestion endpoint, RBAC validator decorator, Excel data loader/writer, boundary-code/identifier/pattern/required-field validators, an MDMS client, an organization-service client, and a vendor-data-processor factory/processor pair for turning spreadsheet rows into vendor records. This is the service that May's facility/project/supervisor ingestion features extend. (#568, #574)
- **Boundary ingestion function** — adds boundary-excel data-loading and extends file-ingestion/RBAC validation so boundary hierarchy data can be bulk-loaded through the ingestion service; also adds an organisation-service validator check. (#573)
- **Vendor Registry service** carved out of the old municipal "organisation" service — the repo-wide rename/restructure commit moves `municipal-services` to `e4h-services` and splits a standalone vendor-registry service out of what had been a shared organisation/municipal service; a follow-up adds its CI/CD workflow and a Dockerfile fix. (#519, #530)
- **Boundary Service v2 with paginated APIs** — full new service — controllers for boundary, boundary-relationship, and hierarchy-definition, repositories/row-mappers/query-builders for boundary entities, hierarchy types, and relationships, plus enrichment and validators for entities, hierarchy, and relationships. Adds its own GitHub Actions workflow. Replaces ad-hoc boundary lookups with a proper paginated relationship API that May's fixes build on. (#524)
- **MDMS v2 service** — new master-data service alongside the legacy MDMS service, adding a v2 controller, schema-definition controller, schema-definition repository/query-builder/row-mapper, and v2 request/response/criteria models, backed by a Redis-wrapped data cache. A follow-up fixes the v2 API's naming convention to match REST conventions. (#449, #456)
- **Processor Service** — video/image transcoding split out of im-services into its own microservice — moves the ffmpeg executor/command-generator and reimplements the video service, video-quality processor, storage service, and video-uploader service behind new implementation classes, a Kafka-listener video consumer, and storage/video utility helpers; im-services' own copies of this logic are stripped down to thin delegators. Gets its own CI workflow. (#471)
- **Feedback Survey feature** — citizen-facing post-resolution rating flow — new feedback and select-rating employee pages, a rating field threaded through the workflow model, UI routes/constants for feedback, and accessibility/edge-case fixes (blocking "Rate" from showing as a Take-Action option once already rated, fixing redirect-after-login, fixing feedback display and null-assignee-on-sendback bugs). (#491, #377, #472, #483, #484, #485, #492, #494)

### Changed
- **Repo restructuring** — `municipal-services` renamed/moved to `e4h-services` across the tree (large rename-only diff touching HRMS, im-services-analytics, and other modules) to reflect the platform's shift from generic municipal services toward health-facility/energy-asset domain services; vendor-registry is split out as part of the same change. (#519)
- **Organisation service validations** — new fields and stricter validation added to organisation address/function query builders and row mappers, plus MDMS-driven constant lookups. (#542)
- **Boundary paginated API reshaped to match actual requirement** — removes an earlier, unused paginated call path and reworks the boundary service/controller to return a flat boundary response. (#531)
- **Inbox SLA query rework** — SLA computation rewritten and dead/buggy code removed — precursor to May's further nearing-SLA fixes. (#526)
- **Nearing-SLA assignee condition** — the inbox query builder extended to filter nearing-SLA tickets by assignee so tickets aren't surfaced to the wrong queue. (#537, #538, #539)
- **Vendor-tenant ticket scoping** — vendor-type users are now limited to only see tickets for their assigned tenant, closing a data-leak/over-broad-visibility gap. (#443)
- UI: dynamic logo/banner rendering driven from global config instead of hardcoded per-state assets, and a scroll-issue fix for the change-city dropdown. (#520, #521, #508, #522, #525)
- Image upload size limit raised to 10MB; Gujarat-specific UI Docker image added; Dockerfile trimmed of unnecessary UI build steps and a docker file added for other state images. (#533, #461, #462, #540, #541)

### Fixed
- **Assignee validation churn** — a placeholder "validate assignees" check and a follow-up fix requiring assignees for certain actions were added, then fully reverted a few days later together with an NPE fix — the revert explicitly reverts three prior commits, indicating the assignee-required validation caused regressions in production ticket workflows. (#458, #459, #467, #464)
- **Indexer async flow** — fixed by removing an externalURIMapping step that was breaking the async indexing flow; a related unwanted notification action removed. (#468, #473, #470)
- Deployment issues on dev fixed after the mdms-v2/boundary-service/vendor-registry additions changed the service topology. (#469)
- `sendback` action was sending a non-null assignee where null was expected, corrected. (#472)
- Login redirect bug fixed — users were being redirected back to the same screen after login instead of proceeding; feedback page display and accessibility issues fixed. (#484, #485, #492)

---

## March 2025

### Added
- **HLS re-implementation and retry hardening for the filestore** — the HLS storage service, artifact mapper, Minio repository, and Minio client facade were reworked to add a retry mechanism around EOF exceptions from MinIO and to consistently use try-with-resources so connections close gracefully — closing out a month of "fix-186"/"patch-186" churn (s3 path resolution, HLS directory patching, download-path fixes to save originals and renditions into separate folders) that had been landing since early March. (#373, #282, #286, #290, #293, #294, #295, #296)
- **Video-upload polish inside the in-repo filestore** — only master (source) files are now persisted to the artifact DB rather than every transcoded rendition, a hack was added to let `.mp4` uploads masquerade as QuickTime containers so iOS-recorded clips are accepted, and the get-HLS endpoint was switched to take the file id as a request param. (#297, #298)
- **React Player + hls.js adaptive bitrate playback** — the frontend evidence viewer was rewired to embed React Player backed by hls.js so uploaded videos stream as adaptive-bitrate HLS instead of a single rendition; this is the UI counterpart to the HLS master-playlist work landed on the backend in February/March. (#306)
- **End-to-end async video processing pipeline** — a video-quality processor and video-uploader service were introduced and the video/storage services refactored around them so quality renditions process asynchronously; iterated through several UX tunings — pushing the original chunk alongside the master file for a faster perceived response, then reverting to chunk-by-chunk processing and finally returning the response as soon as the master file exists instead of waiting on the original chunk. Async thread-pool sizing was adjusted twice after discovering the target dev box only has 1–2 vCPUs. (#312–#340, #358, #359, #339, #350)
- **CPU-limited ffmpeg transcoding** — the ffmpeg command generator now wraps ffmpeg invocations with `cpulimit` so transcoding can't starve the host; the wrapping command and its argument order were fixed twice more, and video processing was moved back to the foreground so the CPU-limited process can be tracked to completion. (#387, #410, #411, #412)
- **Feedback survey with star rating** — the workflow/process-instance model gained a rating field, persisted by the workflow service, and the citizen-facing feedback page's accessibility/visibility rules were updated to only surface the survey link at the appropriate workflow state. (#377, #370)
- **Multi-tenant/multi-state image builds** — added dedicated GitHub Actions workflows and multi-tenant Dockerfile/nginx config so each state gets its own branded frontend image; helpline number, logo, and background image URLs are now read from per-tenant config instead of being hardcoded. (#381, #383, #422, #424, #415, #416)

### Changed
- **Multipart upload path hardening** — im-services' multipart temp storage now points at an absolute path rather than a relative one, fixing uploads failing depending on process working directory. (#336)
- **nearingSLA filter** — the V2 inbox query builder gained a nearing-SLA query mode; a follow-up fixed the underlying Elasticsearch SLA query, and the frontend filter UI plus its SLA-remaining calculation were corrected. (#273, #275, #280, #284)
- Ticket sendback: added an "Others" rejection option and made comments optional for the remaining reject reasons; a null-pointer fix was applied when a ticket had no sendback reason. (#321, #427)

### Fixed
- **File upload error UX** — uploads now show a loader while in flight, validation errors display the offending filename instead of a generic message, and a file-type validation regression was fixed twice. (#401, #404, #406)
- **Ticket assignment whitespace bug** — assigning a ticket to a user whose name was only whitespace no longer succeeds silently/incorrectly. (#378)
- **Content-type detection on dev** — content-type was coming back null in the dev environment; resolved by deriving it from the multipart file instead of trusting the client-supplied header. (#376)
- **tenantId resolution in multi-tenant HLS playback** — the HLS player was determining the wrong tenant id when constructing HLS URLs across state-specific deployments. (#413)
- Video upload was temporarily disabled in production partway through the month while the ffmpeg/CPU-limit work above stabilized, then multi-tenant redirection changes were reverted as a late hotfix. (#417, #429)

---

## February 2025

### Added
- **In-repo filestore rebuilt from scratch, replacing the external egov-filestore dependency** — The service was added to the repo, deleted once and re-added clean with its own storage service, artifact repository, and Azure Blob/Minio repository backends; the storage controller/artifact repository/Minio repository were then substantially rewritten (500+ line diffs) across several follow-ups to stabilize upload/download flows. (#247, #249, #260, #261, #268)
- **ffmpeg-based video transcoding foundation** — new ffmpeg-command-generator and ffmpeg-service classes were introduced, video-quality config and directory utilities added, and the video service cut down from a monolith to delegate to them — the first working end-to-end transcode-to-multiple-qualities pipeline, refactored for async execution. (#270)
- **Video upload support in im-services** — a storage validator, storage controller, and storage utility were added to validate and integrate video uploads with the filestore; supported formats were extended to mp4/mov/wmv, a video size limit was added, and uploads were capped at 5 files per submission. The frontend upload components were substantially rewritten to drive multi-file video upload, with playback wired into the complaint timeline and related bugs fixed shortly after. (#209, #216, #177, #178, #196, #240)
- **HRMS integration removing the boundary-data dependency** — A new HRMS service was vendored into the business-services layer, and employee create/update no longer validates against static boundary/jurisdiction data — addressing a hard dependency that previously required boundary master data to be seeded before HRMS could be used. A dedicated image-build workflow followed. (#227, #230)
- **Sendback reasons with sub-reasons and SMS template** — the complaint components gained a two-level reason/sub-reason dropdown for sending a ticket back; a sendback-reason model and notification utility were added on the backend with a configurable SMS template stored in the DB, and sendback actions are now logged. (#200, #238, #239)
- Forgot-password popup implemented on the citizen/employee login flow. (#269)
- Per-service image-build GitHub Actions workflows for im-services and HRMS. (#214, #215, #217)

### Changed
- **Java 8 → 17 and javax → jakarta migration** — im-services' build file and all `javax.*` imports (validation annotations, servlet APIs) across controllers/models were migrated to `jakarta.*`; Spring's circular-reference restriction was relaxed and the Flyway Docker/migration scripts updated to match, with SonarQube exclusions added for generated/duplicate code. (#159)
- Postgres JDBC driver updated for the inbox module. (#263)
- Repo cleanup: removed unused core-services modules and stale root-level project files no longer relevant after the filestore/HRMS vendoring; a resulting cyclic dependency between modules was fixed. (#201, #221)
- Frontend bundle output is now content-hashed for cache-busting on deploy. (#228)

### Fixed
- Health-center dropdown filter fixed, and district-change no longer leaves stale values in dependent dropdowns. (#172, #170, #258)
- Crash fixed when pressing Enter on certain forms, and long uploaded filenames no longer break the file-name tag layout. (#195, #197)

---

## January 2025

Only two substantive commits landed on `main` in January; the rest of the month's history is documentation and merge commits with no code changes.

### Added
- **Initial IM escalation model and inbox wiring** — Escalation-instance/request models were added to both the workflow and im-services modules, the escalation service extended, and the inbox query builder/service updated to surface escalation state; notification consumer/service were extended for escalation notifications, and the frontend build config was updated accordingly.

### Changed
- Minor im-services configuration additions laying groundwork for later filestore/video work.

---

## October 2024
Only one commit all month, and it's a repo-hygiene change: "Update issue templates." No code changes.

---

## July 2024

### Added
- CI: SonarQube static code analysis wired up for the repository — the first automated code-analysis gate in the project.
- API documentation added for the incident-management service.

### Changed
- Frontend fixes and cleanup: logo fix, escalation query update, a block/district case-handling bug fix, and two rounds of frontend stabilization closing out this iteration of the incident-management frontend.
- Misc scaffolding: `package.json` added and utility helpers added.

### Notes
July was a quiet stabilization month on top of the Phase-1 import — small frontend bug fixes and the first CI wiring, no new services.

---

## June 2024

### Added
- New service: **im-services-analytics**, a Kafka consumer for incident-management — the first E4H-specific backend service (as opposed to imported/customized DIGIT code). It consists of a main entry point, Kafka consumer config/listener, and an update-service/update-utils pair to apply updates, plus supporting config/Docker/migration scaffolding.
- The consumer's payload mapping was reworked to match upstream contracts, and its service layer was subsequently modified.
- The remainder of the month (roughly a dozen commits between Jun 24–26) was one continuous PR-review cycle on this same consumer — repeated config tweaks, utility fixes, and a long back-and-forth of README updates, largely driven by automated review comments — merged as PR #5 "Added im-services-analytics consumer."
- A "code merge" commit folded in other in-flight work ahead of the consumer's introduction.

### Notes
June is essentially a single feature arc: standing up im-services-analytics and iterating it through code review until merge.

---

## May 2024

### Added
- **Phase 1 commit** — the big-bang import of the full DIGIT core monorepo: business-services, core-services, municipal-services, pgr-services, plus the micro-UI frontend. Roughly 6,000 files and 518,000+ lines added, bringing in billing-service, collection-services, DSS/analytics dashboards, HRMS, PGR, MDMS, and the full micro-UI frontend as a coherent DIGIT platform baseline (superseding the narrower HRMS/PGR-only customization from April).

### Changed
- "pre UAT Commit" — post-import cleanup/tidy-up touching ~211 files across the frontend (App shell, index.html, PGR inbox filter, etc.) to prepare the imported platform for UAT.
- A one-line config fix to the im-services application properties.

### Notes
May is dominated by a single event: replacing the earlier hand-picked HRMS/PGR customization with a full, current DIGIT monorepo import, then smoothing it out for UAT.

---

## April 2024

### Added
- **Initial commit** — repo bootstrap: `.gitignore`, `LICENSE`, and a two-line `README.md`. Nothing else.
- **"2.9 code of HRMS and PGR for customization"** (merged as PR #1) — the platform's first real code: a standalone `egov-hrms` module (DIGIT's HRMS service, version 2.9) added at the repo root, complete with models, repositories, services, Kafka consumer/producer, DB migrations, and config — intended as the base to customize for E4H's HR/incident needs.

### Changed
- **"Added frontend code and reorganized folder structures"** (merged as PR #2) — pure restructuring: moved `egov-hrms` under `backend/egov-hrms` and introduced the `backend/` vs `frontend/` split that the rest of the repo's history follows (file contents unchanged, only paths moved).

### Notes
April is the platform's true origin: an empty repo, followed same-day by importing a single customized DIGIT service (HRMS/PGR) and reorganizing it into the `backend/`/`frontend/` layout that later imports (Phase 1 in May) would build on.

---

## Notes on scope

- Version numbers referenced in commit history (v1.1, v2.0.0, v2.0.3, v2.0.4, v2.1) correspond to tagged production releases; see individual module `CHANGELOG.md` files under `backend/*/` for line-level detail inherited from upstream DIGIT modules.
- State-specific rollout branches (Karnataka, Maharashtra, Odisha, Gujarat, Meghalaya, Nagaland, Assam, Manipur, Mizoram, Sikkim, Arunachal Pradesh) are folded into the thematic entries above rather than listed per-state, since the underlying feature is almost always shared across states.
- Some features (RMS, AMC, Admin UI, Field Plan) were built incrementally over several months and were sometimes merged into `main` in a single large release commit that bundled weeks of branch work — in those cases the same release may be described from different angles across two adjacent monthly sections (e.g. the underlying branch work vs. the `main` merge date) rather than duplicated verbatim.
- PR numbers are quoted directly from commit messages; where a fix visibly landed twice under two PR numbers (a hotfix followed by its backport/reapply), both are listed on the same entry rather than duplicated as separate lines.
