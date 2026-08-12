# APIs Involved: Multiple AMCs per Facility

Companion to [LLD_Multiple_AMC_Per_Facility.md](./LLD_Multiple_AMC_Per_Facility.md). Lists every API touched by the change and the update required in each, grouped by service.

---

## 1. amc-scheduler-service

| API | Today | Update Needed |
| --- | --- | --- |
| `POST /asset-amc/v1/configuration/_create` | Creates an `amc_configuration` (facility+project+vendor contract). `AmcConfigurationValidator` checks project/facility existence, mandatory fields, date ranges — no duplicate-tuple check. | **Yes.** Add a pre-check in `AmcConfigurationValidator` for an existing `(facilityId, projectId, vendorId)` tuple before insert, so a duplicate submission fails as a clean 4xx instead of a raw DB unique-violation (`ux_amc_configuration_unique_installation`). No change to allow overlapping `asset_types` across sibling configs — that stays permitted. (§3.3) |
| `POST /asset-amc/v1/configuration/_search` | Search configs by `facilityIds`/`projectIds`/`status`. | No contract change. Used as-is by both the legacy fallback matching and the new field-plan flow. |
| `POST /asset-amc/v1/asset/_create` | Creates `asset_amc` link(s) — the API that maps an asset to an AMC. `AssetAmcValidator` only checks that `assetId`/`amcConfigurationId` exist. | **Yes.** Before creating a row with `status = ACTIVE`, query for an existing row on the same `asset_id` with `status IN ('ACTIVE','UNDER_MAINTENANCE')`. Reject (`"Asset already has an active AMC"`) unless an explicit "supersede" flag is passed, in which case atomically expire the old row before activating the new one. Payload shape is unchanged — only server-side validation is added, backed by the new partial unique index `ux_asset_amc_one_active_per_asset` at the DB layer. (§3.1, §3.2, §4.3) |
| `POST /asset-amc/v1/asset/_update` | Update an existing `asset_amc` link (e.g. status change). | No explicit change called out, but should be checked against the same "one currently-mapped row per asset" invariant if a status update could transition a row into `ACTIVE`/`UNDER_MAINTENANCE`. |
| `POST /asset-amc/v1/asset/_search` | Search asset-AMC links. | No contract change. Becomes more heavily used — the legacy fallback and the new ingest flow both call it (filtered by `assetIds` + `status=ACTIVE,UNDER_MAINTENANCE`) to check for existing currently-mapped links before linking. |
| `POST /asset-amc/v1/visit/_create`, `/configuration/_generate`, `/workflow/_update`, `/_search`, `/_update` | Scheduled-visit lifecycle, scoped by `amc_configuration_id`. | No change needed. Fixing the matching/validation layer upstream (one-active-link-per-asset) removes the duplicate-visit-schedule side effect without touching `generateScheduledVisits` itself. (§2.6, §3.4) |
| `POST /asset-amc/v1/fieldPlanEligibility/_search` *(new)* | Does not exist today. There is, however, an existing `AMCSchedulerServiceClient.search_amc_configurations` call (used by ingestion-service's current template endpoint, see below) that fetches existing AMC config data per facility — but only at facility granularity, with no field-plan/asset-linkage awareness. | **New API**, additive alongside the existing per-facility config lookup above (that one isn't replaced — it's still needed for the config-level `AMC-Frequency`/`AMC-Duration` pre-fill). Input: `tenantId` + `projectId`. Queries the new `installation_active_amc_map` read-model table with `GROUP BY facility_id, field_plan_id, field_plan_name HAVING COUNT(*) FILTER (WHERE amc_configuration_id IS NOT NULL) = 0`. Returns facility/field-plan pairs with no currently-mapped assets — i.e. eligible for a new AMC at field-plan granularity. Called by ingestion-service's template-generation endpoint (below) as a second lookup alongside its existing config search. (§3.4 Step 0) |

---

## 2. ingestion-service

| API | Today | Update Needed |
| --- | --- | --- |
| `POST /ingestion-service/template/amcConfigurationTemplate` | **Already exists and is not static** — corrects the LLD's §2.8/§3.4 framing of this as a net-new `_generate` endpoint. Implemented in `template_generation.py:797-1114` (`get_amc_configuration_template`), routed via `ingestion-service/app/api/routes.py:9`. Takes `RequestInfo`, `boundary_data`, `project_id`. Dynamically fetches real facilities for the given boundary/project via `FacilityServiceClient.bulk_search_facility_with_boundary`, then calls amc-scheduler-service's config search **per facility** to pre-fill `AMC-Frequency`/`AMC-Duration` for facilities that already have a config, locks those cells, and adds a `BoundaryCodes` sheet + dropdown validation. Already wired to the frontend "Download Template" button (`pm/src/pages/employee/CreateAMC.js:130` → `PMService.downloadAMCFacilityDataTemplate` → `pm/src/services/Ingestion.js:103-119`). | **Extend, don't rebuild.** Granularity today is one row per **facility**; needs to become one row per **(facility, installation plan/field-plan)**. Required changes: (1) add a call to the new `fieldPlanEligibility/_search` API to get eligible `(facility, field_plan)` pairs instead of (or alongside) the existing per-facility config lookup; (2) add `Installation Plan Name` (hidden `field_plan_id`), `Assets`, and `Yes/No` columns, pre-checked `Yes`; (3) keep the existing frequency/duration pre-fill + cell-locking + `BoundaryCodes` sheet behavior — that part is already correct and should be preserved. (§3.4 Step 0) |
| `POST /amcConfigurationValidateData` | Validates the uploaded Excel: facility/vendor/frequency/duration columns, dedupes by `config_key = (facility_id, project_id)`. Does not touch `asset_amc`. | **Yes.** Sheet gains three columns: `Installation Plan Name`, `Assets`, `Yes/No`. Skip rows where `Yes/No = No` entirely. For every remaining row, resolve the field plan's installed assets (`asset.activity_facility_id → facility_activities.id`, `facility_activities.field_plan_id = row.field_plan_id`) and check for existing currently-mapped `asset_amc` rows (`status IN ('ACTIVE','UNDER_MAINTENANCE')`). If **any** row conflicts, reject the **entire sheet**, naming the offending `field_plan_id`(s) — no partial apply. This is a live check against `asset_amc`, not the Step-0 read-model cache, to catch the race window between template generation and upload. (§3.4 Validation step) |
| `POST /amcConfigurationBulkIngest` | Creates `amc_configuration` rows only, keyed by `config_key = (facility_id, project_id)`; one project + one vendor per upload; never calls the asset-linking API. | **Yes.** Same `Yes/No` skip as validation. After creating/reusing the `amc_configuration` for `(facility_id, project_id, vendor_id)` (unchanged `config_key` logic), resolve each row's `field_plan_id` to its assets via the same join and bulk-create `asset_amc` links from exactly that batch to that config. Two rows for the same facility with different `field_plan_id`s under the same project contribute two separate asset batches to one shared config. **Open decision:** whether this resolution/linking logic runs in ingestion-service (Python, calling out to field-planner-activity + asset-registry) or is delegated to amc-scheduler-service (Java) via `field_plan_id` passed through existing payloads. (§3.4 Ingest step) |

**Optional/related (not required for requirement 3 itself):**
- Per-row vendor/project instead of one-per-upload (removes today's hard error on >1 vendor per batch).
- Duplicate-check key updated to `(facility_id, project_id, vendor_id)`.
- Relevant only if bulk-creating multiple sibling AMCs (different vendors/projects) per facility concurrently. (§3.5)

---

## 3. field-planner-activity

| API / Method | Today | Update Needed |
| --- | --- | --- |
| `AmcSchedulerService.processInstallationCompletion` *(internal, no public API)* | Fires on installation completion. Fetches all active configs for the project+facility (any vendor), matches installed assets to configs purely by `asset_types`, independently per config — no check for assets already actively linked, so one asset can match multiple overlapping configs. Only caller of `asset-amc/v1/asset/_create` today. | **Yes (kept as legacy fallback only).** Once `field_plan_id`-scoped linking is in place, this path is only exercised for configs created via the single, non-bulk `configuration/_create` API (which has no `field_plan_id` concept). Required changes: <br>1. Before matching, exclude assets that already have a currently-mapped `asset_amc` link (search `/asset-amc/v1/asset/_search` by `assetIds` + `status=ACTIVE,UNDER_MAINTENANCE`).<br>2. Auto-link only when **exactly one** active config matches an asset's type; if zero or multiple match, leave the asset unlinked (no manual-assignment UI exists to resolve ambiguity). (§3.4 Legacy fallback) |
| `ActivityFacility` search (used internally via `field_plan_id` join) | Used internally by `processInstallationCompletion`. | No public contract change, but the same join path (`ActivityFacility.fieldPlanId`) is newly relied upon by ingestion-service's validate/ingest steps to resolve a `field_plan_id` to its installed assets. If this resolution is done in ingestion-service (per the open decision above), ingestion-service would need a callable search API here for `ActivityFacility`/assets by `field_plan_id` — not just internal use. |

---

## 4. health-facility-registry

| API | Today | Update Needed |
| --- | --- | --- |
| `POST /_bulk-search` (facility) | Existing bulk facility search by `facilityIds`. | No contract change. Newly used by the template-generation endpoint (§3.4 Step 0) to fetch `NIN/HFR ID`/`BoundaryCode`/`Health Facility Name` for all distinct facilities in one call instead of one call per facility. |

---

## 5. asset-registry

| API | Today | Update Needed |
| --- | --- | --- |
| Asset search by `activityFacilityId` | Only accepts a single `activityFacilityId` per call — no batched variant. | No change mandated by this LLD, but called out as a scaling limitation: a live (non-cached) eligibility resolution would need this to be batchable across many `activityFacilityId`s. This is the reason the new `installation_active_amc_map` read-model exists instead of a live join at request time. (§3.4 "Why this can't be one live SQL query") |

---

## 6. New backing table (not an API, but new data surface)

`installation_active_amc_map` — owned by **amc-scheduler-service**. Not exposed directly; only reachable via the new `POST /asset-amc/v1/fieldPlanEligibility/_search` API above. Populated by:
- A periodic sync job (or installation-completion listener) pulling static identity columns (`field_plan_id`, `field_plan_name`, `facility_id`, `asset_id`, `tenant_id`, `project_id`) from field-planner-activity + asset-registry.
- Live updates to `amc_configuration_id` in the same write path as `asset_amc` (e.g. alongside the existing Kafka "save-asset-amc" persister flow), since this column must stay fresh for correctness. (§3.4)

---

## 7. Summary table — API-level changes only

| API | Service | Change |
| --- | --- | --- |
| `POST /asset-amc/v1/configuration/_create` | amc-scheduler-service | Add duplicate-tuple pre-check (4xx instead of DB error) |
| `POST /asset-amc/v1/asset/_create` | amc-scheduler-service | Add one-active-AMC-per-asset validation + supersede flow |
| `POST /asset-amc/v1/asset/_update` | amc-scheduler-service | Verify same invariant holds on status transitions |
| `POST /asset-amc/v1/fieldPlanEligibility/_search` | amc-scheduler-service | **New** — field-plan eligibility query, additive to existing per-facility config search |
| `POST /ingestion-service/template/amcConfigurationTemplate` | ingestion-service | **Extend existing endpoint** (already dynamic, not static) to add field-plan/asset/Yes-No columns and per-field-plan granularity |
| `POST /amcConfigurationValidateData` | ingestion-service | Add field-plan columns, live conflict check, whole-sheet rejection |
| `POST /amcConfigurationBulkIngest` | ingestion-service | Add field-plan-scoped `asset_amc` bulk-linking |
| `AmcSchedulerService.processInstallationCompletion` (internal) | field-planner-activity | Exclude already-linked assets; auto-link only on unambiguous match |
| `POST /asset-amc/v1/configuration/_search`, `/asset/_search`, visit APIs | amc-scheduler-service | No contract change (used as-is, more heavily) |
| `POST /_bulk-search` (facility) | health-facility-registry | No contract change (newly reused) |
| Asset search by `activityFacilityId` | asset-registry | No change (scaling limitation noted, not addressed) |
