# Changelog

All notable changes to this module will be documented in this file.

## 2026-05 to 2026-06 (latest work) - 2026-06-30
- Cut a production release and fixed the Tech POC user role check.
- Onboarded Anganwadi facilities, paused/refactored parts of the RMS UI, and added Arunachal Pradesh state info.
- Added scripts to sync Kibana codes from NIN ID/POC username and migrate missing boundary facility data.
- Fixed vendor-to-facility mapping, facility name leading-space trimming, state name display formatting, MDMS active-value filtering, a recreated-user-remains-inactive bug, and a 504 error.
- Deployed the RMS module update.

## Saura eMitra 3.0 / Prod release - 2026-04-16
- Shipped the Saura eMitra 3.0 release.
- Re-applied the theft conditional checks and the CRM mobile-number fix (carried over from earlier reverts).
- Cut a production release.
- Fixed profile-update page validation issues.

## SeM 3.0 UI - 2026-03-10
- Built the SeM (Solar eMitra) 3.0 UI.
- Added conditional checks for theft-related flows.
- Fixed the RMS ticket reopen UI.
- Made "assigned to me" the default inbox filter for Tech POC users.
- Fixed OOS-state SPOC action validations and a spare-part change-assignee issue.

## Access control updates - 2026-01-28
- Granted PGR access to the EMPLOYEE user role, with a quick follow-up fix.
- Made HRMS search case-insensitive and extended PGR access to the VIEWER role.

## Saura eMitra asset integration - 2025-12-17
- Merged staging changes and integrated Saura eMitra assets into the platform.
- Fixed RMS ticket comments rendering in the wrong format and appearing twice in the timeline.
- Restricted the "rate" action based on username.
- Fixed a facility migration issue, refactored the UI global config, and allowed multi-level jurisdictions.
- Migrated Saura eMitra data from production into the asset-management backend.
- Fixed inbox search failing on the create-complaint page.
- Fixed the CRM helpline number shown in the mobile side navigation (two follow-up rounds).

## Duplicate-ticket detection - 2025-10-24
- Added a project-creation flow.
- Injected the analytics measurement ID via DevOps config.
- Reverted a health-care-center-type casing fix that had caused a regression.
- Fixed a missing SLA value for HCR tickets.
- Added a Potential Duplicate Ticket UI to flag likely duplicate complaints.

## GA4 analytics & Release v2.0.4 - 2025-09-04
- Implemented GA4 analytics; fixed GA4 bugs and added facility-name tracking.
- Added caching for service definitions.
- Tagged as Release v2.0.4.
- Reordered and fixed inbox filters.

## v2.0.3 patch - 2025-08-14
- Shipped the v2.0.3 patch release.

## Release v2.1 (PWA) - 2025-08-13
- Implemented Progressive Web App (PWA) support, including icon changes for staging.
- Reverted an SLA-overdue change.
- Tagged as Release v2.1; adjusted the PWA icon size.
- Fixed the logo overlapping the tenants dropdown in mobile view.

## Sprint 10 - 2025-07-28
- Delivered a batch of Sprint 10 UI fixes and features.
- Added a user login report.
- Applied further Sprint 10 UI changes.

## V2.0.0 - 2025-07-08
- Restyled the video player fallback link.
- Tagged and deployed V2.0.0 to production.
- Restored the yarn.lock file after it had been mistakenly deleted.
- Shipped a V2.0.0 hotfix and fixed an incorrect file-upload error message.

## 2025-06 batch - 2025-06-30
- Added role-based SLA display in the inbox; fixed the decline action.
- Fixed business-service/role resolution for the vendor list and an HCR inbox failure.
- Added the complaint facilitator role to PGR access; fixed missing status filters by sourcing business service from WorkflowService.
- Made comment/attachment mandatory depending on the selected workflow action and trimmed extra send-back actions.
- Fixed duplicate inbox calls and repeated image/video upload failures after ticket creation (several follow-up rounds).
- Removed redundant helper text on the create-ticket page; restyled the video play/pause button fill.
- Fixed image-upload state-management issues; rolled up a batch of production UI fixes.
- Showed the filed date on complaint details; removed 1440p video calls in favor of a "processing" message with a download link.

## 2025-05 batch - 2025-05-29
- Fixed a slow file-upload issue and shipped a general UI upgrade.
- Fixed SLA count incorrectly showing for closed tickets.
- Bundled fixes for tickets #436/#437; bumped the CSS bundle version twice.
- Fixed the CRM helpline number display on mobile and added an upload timeout.
- Fixed a bug where clicking upload triggered form submission.
- Added a "system functional" field to ticket creation and detail views.
- Removed the gender field from the user profile.
- Added new popup functionality.

## 2025-04 batch - 2025-04-26
- Added a DevOps pipeline change and a Gujarat-specific UI image build.
- Fixed send-back to send a null assignee; fixed redirect-to-same-screen after login.
- Fixed several feedback-survey issues (minor bugs, display issue, edge cases) and stopped "rate" from showing as a Take Action option.
- Made banner logos render conditionally and refactored logo rendering to map dynamically from global config.
- Fixed a scroll issue on the change-city dropdown.
- Raised the upload size limit to 10MB and fixed nearing-SLA assignee handling.
- Refactored the UI Docker build and added Docker images for additional state deployments.

## Post-1.1 stabilization - 2025-03-28
- Added multi-tenant logo/image support, then removed the shared digit-ui CSS in favor of the app's own styles.
- Added an upload loader and upgraded CSS to match.
- Improved file-upload error messages to show the actual filename; fixed ticket assignment accepting whitespace-only values.
- Rolled the loader, upload-error, and whitespace-assignment fixes into a hotfix; also fixed file-type validation.
- Fixed tenant ID resolution for multi-tenant deployments.
- Moved helpline number, logo, and background URLs to be read from config instead of being hardcoded.
- Temporarily disabled video upload, then shipped a further hotfix.
- Implemented the feedback survey feature; reverted a redirection change that had caused issues.

## Release/1.1 - 2025-03-17
- Added React Player with hls.js for adaptive bitrate (ABR) video streaming; fixed the resulting build failure.
- Upgraded the CSS bundle; fixed master file URL domain handling.
- Added a "deprecated" key to filter out required field definitions.
- Added an "Others" reject reason and made comments optional for the remaining reject reasons.
- Added a nearingSLA filter.
- Tagged as Release/1.1; fixed a logo alignment regression introduced with it.

## 2025-02 bug-fix batch - 2025-02-28
- Fixed the health care center filter and dropdown fields not resetting dependent fields on district change.
- Fixed an app crash on pressing Enter and a file-upload filename tag wrapping issue.
- Did a code cleanup pass; added a Babel plugin for optional-chaining support.
- Hashed the bundle output for cache-busting on deploys; fixed the resulting image build.
- Added a sendback reasons dropdown with sub-reasons.
- Added video file upload support and fixed the related upload bugs.
- Fixed a dropdown issue and the submit button position when the mobile keyboard appears.
- Fixed the health center filter issue (follow-up) and updated the CSS bundle version.
- Implemented a Forgot Password popup.

## Development resumes - 2025-01-21
- Large combined backend/frontend commit resuming active development after the mid-2024 pause.

## 1.8.0 (Base) - 2024-07-05
- Initial build-out of the micro-ui app (Phase 1 build, pre-UAT commit, code merge, final frontend commit).
- Fixed the logo; added the app's root package.json with the version set to 1.8.0.
