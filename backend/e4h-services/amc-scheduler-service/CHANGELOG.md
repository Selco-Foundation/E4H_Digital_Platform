# Changelog

All notable changes to this module will be documented in this file.

## 1.1.6 - 2026-08-13
- Rewrote the root README and consolidated local setup instructions into it, removing the separate LOCALSETUP.md.

## 1.1.6 - 2026-07-28
- Brought in generated API documentation (OpenAPI spec) and updated the README/GitBook pages accordingly.

## 1.1.6 - 2026-07-25
- Added support for the CO2 program dashboard, including a localization utility and a reindex endpoint on scheduled visits.

## 1.1.6 - 2026-07-08
- Added AMC expiration handling to the scheduled visit service.

## 1.1.6 - 2026-06-09
- Added a migration to backfill missing boundary details (block, district, state) for Kibana reporting.
- Fixed vendor mapping issues and added facility fields to the visit search response.
- Added an encryption/decryption utility for facility POC phone numbers.

## 1.1.6 - 2026-06-03
- Fixed vendor mapping and facility-related issues in the scheduled visit service (facility name trimming, state name formatting, active-only MDMS values, inactive recreated users).

## 1.1.6 - 2026-04-27
- Added facility name to scheduled visits (with accompanying DB migration).
- Added SMS request support and updated the resend-OTP flow.

## 1.1.6 - 2026-04-16
- Added a facility bulk search API and criteria model.
- Enhanced AMC configuration validation ahead of the production release.

## 1.1.6 - 2026-02-09
- Added the ability to bypass OTP validation.

## 1.1.6 - 2026-02-06
- Added support for using a default OTP value during validation (for non-production environments).

## 1.1.6 - 2026-01-29
- Added Admin UI functionality: organisation search/address models and organisation lookup for AMC configuration.

## 1.1.6 - 2025-12-16
- Fixed a staging deployment issue caused by an incorrectly named database migration file.

## 1.1.6 - 2025-12-13
- Added boundary-based inbox search enhancement for scheduled visits, including new Boundary and Role models.

## 1.1.6 - 2025-12-09
- Enhanced AMC configuration and scheduled visit services and query builders, with an accompanying DB migration.

## 1.1.6 - 2025-12-01
- Refactored scheduled visit service logic.

## 1.1.6 - 2025-11-27
- Added a scheduled cron job to auto-trigger visit updates as visits approach their scheduled date.
- Minor fix to the scheduled visit service.

## 1.1.6 - 2025-11-26
- Added OTP-based technician verification for scheduled visit assignment.

## 1.1.6 - 2025-11-21
- Initial release of the AMC (Annual Maintenance Contract) scheduler service: AMC configuration, asset AMC, and scheduled visit APIs, with workflow and MDMS integration.
