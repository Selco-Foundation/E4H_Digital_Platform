# Micro UI changelog

Derived from commit history on `frontend/micro-ui` (`main` branch), grouped by month.

## 2026-06
- Rms deployment 30 jun (#2717)
- Vendor mapping facility(2449), Fix issue Facility Name Leading Space Not Trimmed #2488, State Name Display Formatting Issue #2489, Get only active values from mdms for facility ingestion, Recreated User Remains Inactive After Deletion #2515, 504 issue #2517 (#2518)

## 2026-05
- Script to Sync Kibana Code from NIN ID when HFR ID is empty or NULL,Sync code from poc_username for Anganwadi facility. Implement Migration for missing boundary facility (PROD). Update district for facility.Validation/checks for duplicate boundary creation (#2467)
- Anganwadi onboard,  Pause RMS UI Refactors, Arunachal Pradesh state info addition (#2442)
- Fixed Tech POC user role check (#2402)
- Prod release (#2374)

## 2026-04
- Fixed profile update page UI validations (#2321)
- Prod release (#2320)

## 2026-03
- [FIX]: OOS State Spoc actions validations and spare part change assignee issue (#2181)
- [REFACTOR]: Made assigned to me the default filter for Tech POC user in Inbox page (#2163)
- [FIX]: RMS Ticket Reopen UI (#2154)

## 2026-02
- Added theft conditional checks (#2083) (#2153)
- [FEATURE]: SeM 3.0 UI (#2144)

## 2026-01
- Quickfix access employee (#1984)
- Added pgr access for user role EMPLOYEE (#1976)

## 2025-12
- [FIX]: Added conditional render of crm help line number for mobile side nav (#1809)
- [FIX]: Fixed incorrect crm mobile number in mobile side nav (#1807)
- Fixed inbox search failure in create complaint page (#1778)
- Migration Saura eMitra Prod to Asset management backend (#1767)
- [FIX]: Facility Migration fix, UI Global Config refactors, Multi Level Jurisdictions allowance (#1773)
- [FIX]: Changed rate option to be allowed based on username (#1723)
- Fixed workflow comments showing up twice in time line (#1759)
- [FIX]: Fixed RMS ticket comment coming in wrong format (#1746)
- Saura eMitra asset integration (#1712)

## 2025-11
- Staging merge (#1595)

## 2025-10
- [FEATURE]: Potential Duplicate Ticket UI (#1545)
- [FIX]: Missing sla value on UI for hcr (#1524)
- Revert "Fixed casing issue for health care center type in ticket details page (#1145)" (#1488) (#1489) (#1516)
- Revert "Fixed casing issue for health care center type in ticket details page (#1145)" (#1488) (#1489)

## 2025-09
- DevOps: Injestion of Measurement ID (#1478) (#1480)
- Project creation (#1459)
- Filter fix (#1369)
- Filter-reordering (#1361)
- Release v2.0.4 (#1343)
- Service defs caching changes (#1346)

## 2025-08
- GA4 bug fixes and Facility name added (#1331) (#1335)
- Ga4 implementation (#1313)
- V2.0.3 patch (#1205)
- Fixed logo overlap with tenants dropdown in mobile view (#1256) (#1257)
- Icon size changed (#1251)
- Release v2.1 (#1247)
- Pwa icon changes staging (#1225)
- Revert/sla overdue (#1219)
- Implement PWA (#1217)

## 2025-07
- Sprint 10 UI changes (#1180)
- User login report (#1176)
- Fix/UI fixes and features sprint 10 (#1148)
- Fixed incorrect error message on file upload (Issue#335) (#1026)
- Hotfix V2.0.0 (#984)
- USE yarn lock, it was made to be used, not deleted
- V2.0.0 Prod Deployment (#950)
- Video player fallback link styling changes (#969)

## 2025-06
- Remove 1440p calls and show video processing message with download (#947)
- Show filed date in complaint details (#943)
- Fix/UI fixes prod (#941)
- Fix state management issues on Image Upload (#898)
- Fix image videoissue (#888)
- Fill colour for play pause buttons (#893)
- Removed Helper text in create ticket page (#889) (#892)
- Fix image video issue (#885)
- Fixes Image and Video upload issue after the ticket has been created (#884)
- Fixes the multiple call issue on the Inbox (#881)
- Mandated commit and file attaching according to the action selected. Extra actions for send back removed. (#851)
- Fixed missing status filters by changing name of business service to be retrieved from WorkflowService (#858) (#859)
- Adding complaint facilitator roles to pgr access (#844)
- Fixing hcr inbox fail issue (#831)
- Updating business service from workflow response to get proper roles for vendor list (#829)
- Fixed decline action (#815)
- Displaying role based sla in inbox (#804)

## 2025-05
- Displaying systemFunctional field in ticket details (#765)
- Adding new popup functionality (#766)
- Removing gender field from profile (#745) (#746)
- Removing gender field from profile (#745)
- Adding system functional field in ticket creation (#725)
- Bug fix (#695)
- Form submit on upload click bug fix (#692)
- #436, #437 Bug Fixes (#686)
- Fixing crm toll number in mobile screen (#675) (#677)
- Timeout added on upload axios (#676)
- Fixing crm toll number in mobile screen (#675)
- Upgrading css version (#674)
- CSS version change (#667)
- Tickets #436 & #437 (#659)
- Fixed the sla count to be blank for closed tickets (#648)
- UI upgrade dev (#612)
- Bug fix for slow upload issue (#602)

## 2025-04
- Docker file for other state images (#541)
- Remove probably unnecessary steps in docker file for UI (#540)
- Sending assignee to fix nearing sla (#538) (#539)
- Sending assignee to fix nearing sla (#538)
- Hotfix image size to 10mb (#533)
- Fixinf scroll issue for change city dropdown (#522) (#525)
- Fixinf scroll issue for change city dropdown (#522)
- Refactor logo rendering: Dynamically map logos from globalConfig (#520) (#521)
- Refactor logo rendering: Dynamically map logos from globalConfig (#520)
- Conditionally render banner logos based on config values (#508)
- Feedback Survey Implementation (#491)
- Fixing logic to not show rate as a Take Action option (#494)
- Added feedback edge cases (#492)
- Fixed feedback display issue (#485)
- Fixing redirection to same screen after login (#484)
- Fixed feedback minor issues (#483)
- Will send assignee null for sendback (#472)
- Gujarat UI image (#461) (#462)
- Gujarat UI image (#461)
- DevOps 446 (#447)

## 2025-03
- Revert redirection changes (#429)
- Feedback survey implementation (#377)
- Hotfix 420 (#424)
- Disabling video upload (#417)
- Reading helpline number, logo and bg urls from config (#415) (#416)
- Reading helpline number, logo and bg urls from config (#415)
- TenantId determination fix multi-tenant (#413)
- Fixed file type validation issue (#404) (#406)
- Fixed file type validation issue (#404)
- Hotfix - loader changes, proper error display for file uploads, fix ticket assigning with only whitespaces (#401)
- Fixed ticket assigning with only white spaces. (#378)
- Updated file upload errors to display filename (#389)
- Removed digit-ui css (#388)
- Upgraded css version including loader css (#385)
- Added loader for file uploads (#384)
- Multi tenant image  (#383)
- Multi tenant image  (#381)
- Fixed logo alignment (#367) (#368)
- Fixed logo alignment (#367)
- Release/1.1 (#361)
- NearingSLA filter (#284)
- Added Others reject option and made comments optional for rest of the options (#321)
- Added deprecated key to filter required defs (#320)
- Master file url domain fix (#310)
- Upgraded CSS (#309)
- Build failure fix (#308)
- Added React Player and implemented ABR streaming using hls.js (#306)

## 2025-02
- Implemented Forgot Password Popup (#269)
- Fixed health center filter issue (#258)
- Fixed position of submit button when keyboard appears (#251)
- Updated css version (#250)
- Revert "test css import (#243)"
- Deleted import (#246)
- Fixed dropdown issue (#242)
- Test css import (#243)
- Fixed video upload bugs (#240)
- Added functionality to upload video files (#216)
- Hash bundle output for cache-busting (#228)
- Added sendback reasons dropdown options along with sub reasons (#200)
- Image build (#214)
- Added babel plugin for optional chaining (#212)
- Code Cleanup (#201)
- Fixed upload file name tag wrapping issue (#197)
- Fixed crashing on pressing enter button (#195)
- Fixed dropdown issue to empty dependent fields on district change (#170)
- Fixed health care center filter (#172)

## 2025-01
- Backend and frontend code

## 2024-07
- Create package.json
- Logo fix
- Final frontend commit

## 2024-06
- Code merge

## 2024-05
- Pre UAT Commit
- Phase 1 commit
