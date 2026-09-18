# Changelog
All notable changes to this module will be documented in this file.

## 1.2.0 - 2025-08-11
- Added fix for login report and addressed review comments
- Added changelog entries; Release v2.1

## 2025-07-25
- Hot Fix on prod
- User login report
- Fixed timestamp issue, with a follow-up timestamp fix

## 2025-07-08
- Fixed multiple document download URLs
- V2.0.0 Prod Deployment
- Fixed business service lookup for update calls; fixed import
- Legacy ticket ingestion

## 2025-06-30
- Bugfix inbox; Assign to me; fixed image upload issue on IM Services/processor-service
- Added new localized fields for dashboard; new index fields staging
- Fixed overallSLA field and lastActionTakenBy; added new audit index fields
- Updated Incident models for ingestion; refactored DB migration
- Added migrationId, legacyId, filedDate fields to the row mapper
- Quickfix to comments in audit report and ticket details report; filestore download endpoint and URL fixes
- Added mapped vendor name index field; fixed wrong sendback reason error

## 2025-06-09
- Added system functional field for incidents and included it in the search query
- Fixed priority-based business service lookup from ticket type/subtype; fixed PMDMS object matching
- Fixed autoEscalation for im-service; fixed VideoQualityProcessor; removed unused VideoQualityFactory/Settings
- Add totalSLA logic in im-services
- Rollback consumer for auto escalation

## 2025-05-26
- Renamed/moved service directories from municipal-services to e4h-services, creating vendor-registry service
- Optel changes; fixed bug related to video upload; supervisors ingestion
- Fixed hardcoded URL for SMS notification; fixed hardcoded tenant in enrichment service; fixed SMS notification locale to always be en_IN

## 2025-04-16
- Validate-assignees placeholder feature, with a follow-up fix and later revert of the assignee condition
- Fixed logger NPE on create; updated indexer flow; fixed UAT indexer async flow by removing externalURIMapping
- Fixed deployment issues on dev; removed unwanted notification action
- Added code to process video; Feedback Survey Implementation follow-up

## 2025-03-28
- CPU percentage limit on ffmpeg processes, with follow-up fixes; moved video processing to foreground for proper completion
- Feedback survey implementation
- Null-check fix for NPE on send back reason

## 2025-03-17
- feature-186: ffmpeg POC and implementation
- feature-187: ffmpeg/HLS-based video processing, with multiple bug-fix and optimisation patches
- Release/1.1

## 2025-02-19
- feature-67: code setup; feature-204: video optimization, validation and upload
- Image build workflow for im-services; fixed cyclic dependency
- feature-56: fix for video upload
- feature-63: Sendback SMS template; feature-64: log sendback action

## 2025-01-23
- Added IMEscalationRequest model and related IMService updates (Phase 2 commit)
- Added, then later removed, an Incident Management API Specification doc

## 2024-07-04
- Initial consolidation of the existing 1.1.8 codebase into the new monorepo (Phase 1 commit), followed by workflow ProcessInstance model enhancements and search criteria/config updates
- Added DB migration scripts for incident alter and reporter-type tables
- Fixed block and district case-sensitivity issue in EnrichmentService

## 1.1.8 - 2023-08-10

- Central Instance Library Integration
- 
## 1.1.7 - 2023-02-01

- Transition from 1.1.7-beta version to 1.1.7 version

## 1.1.7-beta - 2022-11-03

- Incorporated privacy decryption for notification flow

## 1.1.6 - 2022-08-03
- Added channel based notification

## 1.1.4 - 2022-01-13
- Updated to log4j2 version 2.17.1

## 1.1.3 - 2021-07-23
- Fixed HRMS multi-tenant department validation

## 1.1.2 - 2021-05-11
- Fixed security issue of untrusted data pass as user input.

## 1.1.1 - 2021-02-26
- Updated domain name in application.properties.
- Fixed security issue for throwable statement.

## 1.1.0 - 2020-01-15
- IM v2 API integration with IM UI/UX revamp

## 1.0.0 - 2020-09-01
- Baseline version released
