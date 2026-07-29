# IM Services changelog

Derived from commit history on `backend/e4h-services/im-services` (`main` branch), grouped by month.

## 2026-07
- Updated tech poc sms to be state specific (#2787)
- Rms bug fixes 1 jul (#2728)
- Fixed Assign To issue for State SPOC (#2725) (#2730)

## 2026-06
- Rms deployment 30 jun (#2717)
- Prod release iteration 31 (#2577)
- Fix merge vendors (#2543)

## 2026-05
- Anganwadi onboard,  Pause RMS UI Refactors, Arunachal Pradesh state info addition (#2442)
- Fix im service prod issue when updating old tickets (#2400)
- Prod release (#2374)

## 2026-04
- Removed tenant.tenants use for login report (#2336) (#2352)

## 2026-03
- Resolve conflicts (#2254)
- Added migration to add incidenttype in im service priority table (#2215) (#2216)
- Pause rms creating tickets staging (#2180)
- Made some minor changes to fix warranty status (#2174)
- Im-services uninstallation and reinstallation (#1997, #1999) (#2149). Update business service, migrate process instance (#2151)
- Added M&E dashboard changes for avg turn around time (#2162) (#2166)
- Added changes related to warranty status (#2155)

## 2025-12
- RMS Integration (#1611)
- Added searchOnlyInBoundary and isActive filter for user search (#1835)
- [FIX]: Facility Migration - POC Contact Number (#1782)
- Renamed MDMS migration files to run it prior to employee migration (#1777)
- Migration Saura eMitra Prod to Asset management backend (#1767)
- [FIX]: Facility Migration fix, UI Global Config refactors, Multi Level Jurisdictions allowance (#1773)
- Implemented code to pick district and block names from localization (#1754)
- Mirgation fixes: remove phone number duplicate check in hrms, migration file for mdms data (#1740)
- [FIX]: IM Services Asset management UAT migration (#1734)
- Migrate deduplication and inbox search (#1687) (#1725)
- Saura eMitra asset integration (#1712)
- Pushing RMS ans reporterType if the user has the role of RMS. (#1715)

## 2025-11
- Java image update (#1664)
- Update image (#1610)
- Fix jdk workflow (#1608)
- Update java images in Dockerfile (#1607)
- Staging field planner (#1601)
- Staging merge (#1595)

## 2025-10
- Updated process instances method to make processinstances mutable. (#1548)
- Updated process instances method to make processinstances mutable. (#1547)
- Staging field planner (#1544)
- Feat(im-services): removed the redundant check for empty processInstances in computeTotalSlaRemaining (#1542)
- Priority table uat (#1520)
- Feat(im-services): updated phc-subtype values so as to make the uniform (#1512)
- Total sla remaining (#1502)
- Fix(im-services): added incident type and subtype to the ticket KA-RCH-6365828349-0002 (#1487)

## 2025-09
- FIX: add migration to correct spelling of subtype 'OtherFan' (#1425)
- FIX: add migration to correct spelling of subtype 'OtherFan' (#1421)
- Deletion of Dummy tickets (#1411)
- Delete 8 tickets for dummy health center in production (#1410)
- Fixed double addition of current state (#1397)
- Removed the if condition for closed tickets (#1396)
- Implement Java Based Migrations (#1384) (#1395)
- Total sla staging (#1389)
- Revert "delete 8 tickets for dummy health center in production (#1357)" (#1373)
- Need to push only one reject reason (latest) rather than an array to index (#1371)
- Delete 8 tickets for dummy health center in production (#1357)
- Need to push only one reject reason (latest) rather than an array to index (#1356)
- Release v2.0.4 (#1343)

## 2025-08
- Staging soft delete and migration changes (#1274)
- V2.0.3 patch (#1205)
- Release v2.1 (#1247)
- Added change logs (#1237)
- Added fix for login report (#1229)
- Added fix for login report and addressed the comments (#1228)

## 2025-07
- The timestamp change (#1178)
- Fixed timestamp issue (#1177)
- User login report (#1176)
- Hot Fix on prod (#1168)
- Legacy ticket ingestion (#1025)
- Fix import (#992)
- Fixed business service for update call (#990)
- Fixed business service for update call (#989)
- V2.0.0 Prod Deployment (#950)
- Fixed multiple document download urls (#971)

## 2025-06
- Fixed wrong sendback reason error (#966)
- Added a new field mapped vendor name to index. (#961) (#962)
- Url fix (#960)
- Update filestore download endpoint (#958)
- Quickfix to comments in audit report and ticket details report (#957)
- Quickfix to comments in audit report (#951)
- Added migrationId, legacyId, filedDate fields in the row mapper. (#949)
- Refactor dbmigration of im-services (#937)
- Update Incident Models for ingestion (#934)
- Added new fields for audit index. (#918) (#922)
- Fixing overall sla (#919)
- Fixing lastActionTakenBy (#917)
- Fixing overallsla field (#915)
- New index fields staging (#912)
- Adding new localized fields for dashboard (#891) (#895)
- Fix the Image upload issue on IM Services and processor-service (#894)
- Assign to me (#880)
- Bugfix inbox (#869)
- Rollback consumer for autoescalation (#826)
- Add totalSLA logic in im-services (#812)
- Fix VideoQualityProcessor.java (#810)
- Fixed autoEscalation for im-service (#793)
- Fixed pmdms object matching (#788)
- Getting priority based business service from ticket type and subtype (#785)
- Fixed hardcoded tenant in enrichment service (#783)
- Added system functional to search query (#781)

## 2025-05
- Changed the name of DB migration file. (#767)
- Added a new field system Functiona for incident. (#764)
- Supervisors ingestion (#679)
- Fixed bug related to video upload. (#650)
- Optel changes (#605)

## 2025-04
- Renaming municipal service and moving directories to e4h-services and creating vendor-registry service (#519)
