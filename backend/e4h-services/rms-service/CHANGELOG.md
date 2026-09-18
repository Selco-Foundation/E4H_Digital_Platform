# Changelog

All notable changes to this module will be documented in this file.

## 2026-07-25
- Added the CO2 program dashboard.

## 2026-07-20
- Modified the search index payload.

## 2026-07-02
- Bug fixes for the RMS flow.

## 2026-06-30
- Added a CO2 archetype CHC seed migration and simplified the payload generator.
- Updated and quick-fixed the RMS system user used when creating tickets.

## 2026-06-11
- Refactored the CO2 el-measure consumption service and dashboard API client.
- Removed LLD documentation files from the service module (relocated elsewhere).

## 2026-06-03
- Facility ingestion and mapping fixes: trimmed leading spaces from facility names, fixed state name display formatting, restricted MDMS lookups to active values only, fixed recreated users staying inactive after deletion, and fixed a 504 timeout issue.

## 2026-05-05
- Added a ticket-pause feature: facility eligibility sync service, MDMS-driven district configuration gating, ticket pause repository/service, and audit event publishing for paused tickets.

## 2026-04-16
- Added checks to block ticket creation under certain conditions.

## 2026-03-30
- Added a new RMS schema migration and updated payload generation and orchestrator logic.

## 2026-03-10 to 2026-03-18
- Paused RMS from creating new tickets when there are already open tickets of issue type RMS or Theft for a facility.
- Fixed an issue with the facility bulk search used by the ticket-pause check.

## 2026-02-24
- Fixed the RMS database migration file and adjusted application properties.
- Onm-ready changes, including a minor update-URL fix.
- Fixed a user account ID issue in payload generation.

## 2026-02-23
- Initial release of the RMS schema for the Karnataka state rollout, including base DB migrations and data collector tests.
- Fixed the user UUID used by the RMS service user.

## 2026-02-19
- Added a check to validate that boundaries belong to Karnataka before creating a ticket.

## 2026-02-13
- Added new RMS alert subtypes and filtered out uninstalled facilities from processing.

## 2026-01-29
- Set the alert subtype for poor health status.

## 2026-01-21
- Improved the pagination flow used when collecting RMS data.

## 2025-12-31
- Fixed the alert subtype and inverter rules.

## 1.0.0 - 2025-12-29
- Base version: initial RMS (Remote Monitoring System) integration, including alert ingestion and ticket creation.
- Added follow-up fixes to the RMS flow.
