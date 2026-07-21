# Livelihood Platform — API Specs (Concise, engineer-facing)

**Purpose:** Presentation-ready API map for Phase 1, de-duplicated and easy to scan.  
**Source of truth:** `LIVELIHOOD_API_SPECS.md` (full samples + OpenAPI + DB annex) and `LIVELIHOOD_PLATFORM_CHANGES.md` §12.  
**Tenant model:** single tenant in fork: `tenantId = "livelihood"`.

---

## 1. How to read this document

- The **full spec** (request/response examples, OpenAPI, DB annex) is in `LIVELIHOOD_API_SPECS.md`.
- “**Modify**” means “same route, adapted payload/validation/filters/workflow bindings/config”. It does **not** automatically mean a large refactor.

---

## 2. Cross-service contract invariants (Phase 1)

- **Incident requires** `facilityId + assetId` and must validate asset.facilityId = facilityId.
- **Asset list for issue UI** is always fetched by **facility** via `criteria.facilityID`.
- **Auto-assignment** on incident create derives assignee from **asset → vendor** mapping.
- **Auth**: bearer token (employee-style in DIGIT). OTP/QR flows are front-end orchestration using existing backbone services.

---

## 3. API surface area (by service)

### 3.1 Facility service (`/facility-service`)


| Capability                           | Method + route                                 | Notes                                                         |
| ------------------------------------ | ---------------------------------------------- | ------------------------------------------------------------- |
| Create facility                      | `POST /v2/facility/create`                     | Modified for Livelihood facility attributes + manager contact |
| Update facility                      | `POST /v2/facility/update`                     | Modified                                                      |
| Search facility                      | `GET /v2/facility/search`                      | Modified filters: geography + projectId + programType         |
| Bulk search                          | `POST /v2/facility/_bulk-search`               | Modified                                                      |
| Bulk search (with boundary)          | `POST /v2/facility/_bulk-search-with-boundary` | Modified                                                      |
| Update block mapping                 | `POST /v2/facility/update-block`               | Conditional / if block remap is supported                     |
| Resolve by manager mobile (optional) | `POST /v2/facility/_resolve-by-manager-mobile` | Optional bootstrap helper; **not** a link API                 |


**Request envelope:** `FacilityCreateUpdateRequest` RequestInfo + facility.  
**Security:** bearer auth.

---

### 3.2 Asset registry (`/asset-registry`)


| Capability                          | Method + route                              | Notes                                               |
| ----------------------------------- | ------------------------------------------- | --------------------------------------------------- |
| Create asset                        | `POST /v1/asset/_create`                    | Modified: require facilityId + vendor mapping       |
| Update asset                        | `POST /v1/asset/_update`                    | Modified                                            |
| Search assets                       | `POST /v1/asset/_search?offset=&limit=`     | Modified: support/standardize `criteria.facilityID` |
| Bulk create assets                  | `POST /v1/asset/bulk/_create`               | Modified                                            |
| Asset workflow update (conditional) | `POST /v1/asset/workflow/{assetID}/_update` | Only if asset workflow is kept                      |
| QR resolve (recommended)            | `POST /v1/asset/qr/_resolve`                | New; typically unauthenticated                      |
| Item code search (conditional)      | `POST /v1/item-code/_search`                | Only if item codes are not MDMS-only                |


**Key request envelope:** `assetDetail.Asset` (single create), `assetDetails[].Asset` (bulk), `AssetSearchRequest`.

---

### 3.3 Vendor registry (`/vendor`)


| Capability                           | Method + route                              | Notes                               |
| ------------------------------------ | ------------------------------------------- | ----------------------------------- |
| Create vendor org                    | `POST /organisation/v1/_create`             | Modified validations for Livelihood |
| Create org service                   | `POST /v1/_create`                          | Modified offerings model            |
| Update org service                   | `POST /v1/_update`                          | Modified                            |
| Map item code → vendor (conditional) | `POST /organisation/v1/item-code/_map`      | Only if not MDMS-only               |
| Vendors by facility (recommended)    | `POST /organisation/v1/vendors-by-facility` | Helper for POC “assign vendor” UI   |


---

### 3.4 Project (`/project`)


| Capability                              | Method + route                            | Notes                                               |
| --------------------------------------- | ----------------------------------------- | --------------------------------------------------- |
| Create project                          | `POST /v1/_create`                        | Modified: justificationCode + multi-state geography |
| Update project                          | `POST /v1/_update`                        | Modified: justificationCode immutability rules      |
| Search projects                         | `POST /v1/_search`                        | Modified filters                                    |
| Link facility to project                | `POST /facility/v1/_create`               | Modified validations                                |
| Create from justification (recommended) | `POST /v1/_create-from-justification`     | New                                                 |
| Map-by-justification (recommended)      | `POST /facility/v1/_map-by-justification` | New                                                 |


---

### 3.5 Incident management (`/im-services`)


| Capability                    | Method + route                                  | Notes                                                    |
| ----------------------------- | ----------------------------------------------- | -------------------------------------------------------- |
| Create incident               | `POST /v2/request/_create`                      | Modified: `facilityId + assetId`, auto-assign vendor     |
| Update incident (all actions) | `POST /v2/request/_update`                      | Modified: workflow-driven actions via single route       |
| Search incidents              | `POST /v2/request/_search`                      | Modified: role-scoped filters (manager vs vendor vs POC) |
| Count incidents               | `POST /v2/request/_count`                       | Modified                                                 |
| Plain search                  | `POST /v2/request/_plainsearch`                 | Modified                                                 |
| Sync boundary by facility     | `POST /v2/request/_update-boundary-by-facility` | Modified / keep in sync when facility boundary changes   |


**Request envelopes:** `IncidentCreateRequest`, `IncidentUpdateRequest`.  
**Security:** bearer auth.

---

## 4. Minimal payload expectations (what engineers must implement)

### 4.1 Asset search for issue UI

- Use `POST /asset-registry/v1/asset/_search` with `criteria.facilityID`.
- Pagination via `offset`, `limit` query params.

### 4.2 Incident create

Must include (at minimum):

- `tenantId: "livelihood"`
- `Incident.facilityId`
- `Incident.assetId`
- `Incident.incidentType` / `incidentSubType` (MDMS-driven)
- `Workflow.action: "AUTO_ASSIGN"` (or equivalent start transition)

Backend must:

- validate asset↔facility
- resolve vendor from asset
- set assignee + status `PENDING_FOR_RESOLUTION`

### 4.3 Incident update (workflow actions)

Single endpoint handles:

- vendor: `RESOLVE`, `OUT_OF_SCOPE`, `OUT_OF_WARRANTY` (+ quotation doc), `DECLINE`
- POC: `REASSIGN`, `ASSIGN_VENDOR`, `DECLINE_POC`
- manager: `REOPEN` (within 72h)
- system: `AUTO_CLOSE`

Quotation upload is via filestore; incident update references `fileStoreId` in documents.

---

## 5. Where to get the full spec

- **Full API + samples + DB annex**: `LIVELIHOOD_API_SPECS.md` (§3 request/response examples, §5–§6 DB).
- **Platform change context**: `LIVELIHOOD_PLATFORM_CHANGES.md` §12 (inventory + modify notes).
- **Workflow semantics**: `LIVELIHOOD_LLD_WORKFLOW_AND_SLA.md` / `..._CONCISE.md`.

---

## 6. Database changes — existing vs required (summary)

Mirrors `LIVELIHOOD_API_SPECS.md` §5–§6. Legend: **✓** exists | **+** add column/table | **JSONB** interim in existing JSONB | **WF** / **HRMS** external.

### 6.1 P0 gaps (APIs not fully workable without these)


| Table            | Add                     | API field          | Service        |
| ---------------- | ----------------------- | ------------------ | -------------- |
| `asset`          | `vendor_id` VARCHAR(64) | `Asset.vendorId`   | asset-registry |
| `asset`          | `item_code` VARCHAR(64) | `Asset.itemCode`   | asset-registry |
| `eg_incident_v2` | `asset_id` VARCHAR(64)  | `Incident.assetId` | im-services    |


Also: update `im-services-persister.yml` + Java models for `asset_id`; add indexes on `(tenant_id, facility_id, vendor_id)` and `(tenant_id, item_code)` on `asset`.

**Interim (pre-DDL):** `vendorId`, `itemCode`, `isSolar` → `asset.additional_details`; `createdOnBehalf`, `entryChannel` → `eg_incident_v2.additionaldetails`.

### 6.2 Already aligned (no DDL for these API fields)


| API field                                                     | Table.column                                                |
| ------------------------------------------------------------- | ----------------------------------------------------------- |
| `Asset.facilityID`                                            | `asset.facility_id` ✓                                       |
| `Asset.serialNumber`, `modelNumber`, `brandID`, `assetTypeID` | `asset.`* ✓                                                 |
| `Incident.facilityId`                                         | `eg_incident_v2.facilityid` ✓                               |
| `facility.facilityPocPhone/Name/Email`                        | `facility.facility_poc_*` ✓                                 |
| `facility.boundaryCode`                                       | `facility.boundary_code` ✓                                  |
| Project–facility link                                         | `PROJECT_FACILITY.projectId`, `facilityId` ✓                |
| Vendor org master                                             | `eg_org` (+ related) ✓                                      |
| Workflow assignee                                             | `egov_wf_process_*` (WF) ✓                                  |
| Facility manager user                                         | HRMS `COMPLAINANT` on facility boundary (not a facility FK) |


### 6.3 P1 / optional DDL


| Table      | Add                                 | Purpose                                         |
| ---------- | ----------------------------------- | ----------------------------------------------- |
| `asset`    | `is_solar` BOOLEAN                  | QC form routing (`isSolar`)                     |
| `asset`    | `qr_token` VARCHAR                  | QR resolve (skip if QR encodes `asset_id` only) |
| `project`  | `justification_code` VARCHAR        | or keep in `additionalDetails` JSONB            |
| `facility` | `livelihood_ready` BOOLEAN          | gate HRMS provision                             |
| —          | `livelihood_item_code_vendor` table | only if item codes are **not** MDMS-only        |


### 6.4 No new tables needed

`qr/_resolve`, `_resolve-by-manager-mobile`, `vendors-by-facility` (query assets), `_create-from-justification`, workflow actions — orchestration / compute only.

### 6.5 Minimum migration checklist

1. `ALTER TABLE asset ADD vendor_id, item_code` (+ indexes) — asset-registry
2. `ALTER TABLE eg_incident_v2 ADD asset_id` (+ index) — im-services
3. Persister YAML + entity/DTO updates — both services
4. Optional: `is_solar`, `qr_token`, `justification_code`, item-code mapping table or MDMS-only

### 6.6 API ↔ DB quick reference


| API object field            | Table.column                                        | Status                    |
| --------------------------- | --------------------------------------------------- | ------------------------- |
| `Asset.vendorId`            | `asset.vendor_id`                                   | **+ add**                 |
| `Asset.itemCode`            | `asset.item_code`                                   | **+ add**                 |
| `Incident.assetId`          | `eg_incident_v2.asset_id`                           | **+ add**                 |
| `Incident.createdOnBehalf`  | `eg_incident_v2.additionaldetails`                  | JSONB interim             |
| `project.justificationCode` | `project.additionaldetails` or `justification_code` | interim / optional column |
| Item code master            | MDMS or mapping table                               | design choice             |


Full column lists and migration notes: `LIVELIHOOD_API_SPECS.md` §5.