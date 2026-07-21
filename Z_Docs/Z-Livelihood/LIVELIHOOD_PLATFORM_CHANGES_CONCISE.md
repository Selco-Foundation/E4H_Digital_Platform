# Livelihood Platform — Changes Relative to E4H (De-duplicated)

**Purpose:** A single, readable reference for engineers that **removes repetition** from the original `LIVELIHOOD_PLATFORM_CHANGES.md` while keeping the same implementation-significant content.  
**Primary source:** `LIVELIHOOD_PLATFORM_CHANGES.md` (v2.4 draft).  
**Related sources:** `LIVELIHOOD_LLD_WORKFLOW_AND_SLA.md` (workflow semantics) and `LIVELIHOOD_API_SPECS.md` (OpenAPI bundle).  
**Scope note:** Installation track requirements are included for completeness even if implemented later (Phase 2).

---

## 1. Non-negotiable invariants (Phase 1)

These are the highest-signal decisions that constrain the whole design.

### 1.1 Entity model (core)

- **No separate “end user” entity**
  - Requirement PDFs use “end user” to mean the **person** operating the facility.
  - In DIGIT terms this is only the **facility manager**: HRMS `COMPLAINANT` user, **1:1 with a facility**.
  - Do **not** model `endUserId`, do **not** create project↔user membership entities, and do **not** attach assets to users.
- **Facility is the program site, under project**
  - Program membership is: `**Project` → `ProjectFacility` → `Facility`**.
  - Program oversight and scoping happens at **facilities linked to a project**, not “users mapped to a project”.
- **Assets are under facility**
  - Each **facility** has **many assets** (`asset.facilityId`).
  - Tickets must carry `**facilityId` + `assetId`** and validate asset.facilityId = facilityId.
- **Asset-level vendor mapping**
  - **Each asset** has one vendor; different assets in the same facility may map to different vendors.
  - **Ticket assignment is always derived from the selected asset** (not facility-default vendor, not CRM manual assignment).

### 1.2 Auth and tenancy

- **Auth is inherited**: `egov-user` + `egov-otp` (no new auth stack).
- **Separate deployment environment** for Livelihood (DB + services), and a **new frontend**.
- **Naming note:** `health-facility-registry` is recommended to be renamed to `facility-registry` (service remains the facility registry).

---

## 2. What changes vs E4H (by track)

### 2.1 Issue / support track (Phase 1)


| Topic              | E4H (typical)                      | Livelihood requirement                                           |
| ------------------ | ---------------------------------- | ---------------------------------------------------------------- |
| **Complainant**    | Facility / health patterns         | **Facility manager** (`COMPLAINANT`, 1:1 facility)               |
| **Assignment**     | Facility/vendor mapping + CRM step | **Auto-assign vendor on create from `assetId`**                  |
| **Oversight role** | CRM / health roles                 | **Program POC** (state-scoped)                                   |
| **Entry channels** | Platform + health integrations     | Direct platform + pilot IVR/WhatsApp (POC manual create)         |
| **Workflow**       | E4H incident workflow              | Livelihood states/actions (see §3)                               |
| **Reopen/close**   | E4H patterns                       | **72h reopen window**, else **auto close**                       |
| **SLA**            | E4H incident SLAs                  | **7d vendor**, **3d POC (OOS)**, **14d OOW**, **72h auto-close** |
| **Languages (v1)** | As configured                      | **English + Kannada**                                            |


### 2.2 Installation track (Phase 2 requirements captured here)


| Topic                 | E4H (typical)        | Livelihood requirement                                              |
| --------------------- | -------------------- | ------------------------------------------------------------------- |
| **Project geography** | Single-state project | **Multi-state** projects under a **justification code**             |
| **Project driver**    | Manual project setup | **Justification code** drives auto-mapping of facilities to project |
| **Assets per site**   | Small count          | **Many assets per facility**                                        |
| **Vendor importance** | Less central         | **Central** (item code ↔ vendor master)                             |
| **Field plan unit**   | Facility             | **Facility × Vendor**                                               |
| **Roles**             | Mostly internal      | Vendor Field Staff/Supervisor + **SELCO Installation Reviewer**     |
| **QC forms**          | Solar-focused        | **Item-code driven** (solar vs non-solar)                           |
| **Letters**           | None                 | **Acknowledgment (OTP)** + **Handover**                             |
| **AMC/RMS**           | Present in E4H       | **Not required** in Livelihood Phase 1                              |


---

## 3. Issue workflow summary (Phase 1)

This section is the “single slide” view engineers typically want for implementation alignment.

### 3.1 Statuses (user-facing)

- `PENDING_FOR_RESOLUTION` (vendor working; vendor assigned automatically)
- `OUT_OF_SCOPE_PENDING_POC` (vendor marked out-of-scope; POC must act)
- `OUT_OF_SCOPE_PENDING_VENDOR` (POC reassigned; vendor working)
- `OUT_OF_WARRANTY_PENDING_VENDOR` (quotation uploaded; off-platform decision; reminders)
- `RESOLVED` (72h reopen window)
- `CLOSED_AFTER_RESOLUTION` (system auto-close after 72h if no reopen)
- `CLOSED_AFTER_DECLINE` (POC decline or OOW rejection)

### 3.2 Actions (technical)

Core action codes (indicative): `AUTO_ASSIGN`, `RESOLVE`, `OUT_OF_SCOPE`, `OUT_OF_WARRANTY`, `DECLINE`, `REASSIGN`, `ASSIGN_VENDOR`, `DECLINE_POC`, `REOPEN`, `AUTO_CLOSE`.

### 3.3 SLA timers


| Situation         | Timer                                      | Start                                    |
| ----------------- | ------------------------------------------ | ---------------------------------------- |
| Vendor working    | **7 days**                                 | Create / reopen / POC reassign to vendor |
| POC working (OOS) | **3 days**                                 | Vendor marks OOS                         |
| OOW window        | **14 days** (+ reminder at day 7 and T-2d) | Vendor uploads quotation                 |
| Resolved → close  | **72 hours**                               | Vendor resolves                          |


**Escalation mechanism:** workflow auto-escalation cron calls `egov-workflow-v2` `/auto/{businessService}/_escalate`, and `im-services` consumes `im-auto-escalation` to apply Livelihood-specific side effects (do **not** reuse E4H “blind close” behaviour).

---

## 4. Service inventory (Phase 1)

### 4.1 Legend


| Status            | Meaning                                                              |
| ----------------- | -------------------------------------------------------------------- |
| **Reuse (minor)** | Deploy/configure; small or no code changes                           |
| **Reuse (major)** | Keep service, but schema/APIs/workflow/rules need Livelihood changes |
| **Optional**      | Include only if UI/reporting needs it                                |
| **Not required**  | Omit from Livelihood Phase 1                                         |
| **External**      | DIGIT backbone service (not forked here)                             |


### 4.2 External backbone (shared, not forked)


| Service             | Handles                        | Livelihood                    |
| ------------------- | ------------------------------ | ----------------------------- |
| `egov-user`         | Login, OAuth tokens, user CRUD | **Required**                  |
| `egov-otp`          | OTP send/validate              | **Required**                  |
| `egov-localization` | EN/Kannada messages            | **Required**                  |
| `egov-persister`    | Kafka→DB async writes          | **Required** where applicable |
| `egov-searcher`     | Search/inbox indices           | **Optional**                  |


### 4.3 Core services (forked/modified)


| Service                                        | Status        | Why it matters for Livelihood                                        |
| ---------------------------------------------- | ------------- | -------------------------------------------------------------------- |
| `boundary-service`                             | Reuse (minor) | Geography master; POC state scope; projects                          |
| `egov-mdms-service-v2`                         | Reuse (major) | Issue types, SLA config, AutoEscalation rules, templates             |
| `egov-workflow-v2`                             | Reuse (major) | Livelihood incident business service + transitions + auto-escalation |
| `egov-idgen`                                   | Reuse (minor) | IDs for facility/asset/ticket/doc                                    |
| `egov-filestore`                               | Reuse (minor) | Evidence, quotations, letters                                        |
| `egov-notification-sms`                        | Reuse (minor) | Ticket SMS matrix                                                    |
| `health-facility-registry` (facility registry) | Reuse (major) | Facility master + facility boundary + manager contact/provisioning   |
| `zuul`                                         | Not required  | Gateway handled by deployment                                        |


### 4.4 Domain services (forked/modified)


| Service                  | Status                 | Why it matters for Livelihood                                  |
| ------------------------ | ---------------------- | -------------------------------------------------------------- |
| `project`                | Reuse (major)          | Justification code, multi-state, ProjectFacility links         |
| `field-planner`          | Reuse (major)          | Field plans become **facility × vendor** (install track)       |
| `field-planner-activity` | Reuse (major)          | QC forms item-code driven; OTP acknowledgment; handover        |
| `vendor-registry`        | Reuse (major)          | Vendor master; org users; optional helpers                     |
| `asset-registry`         | Reuse (major)          | Many assets per facility; vendor per asset; QR; warranty       |
| `ingestion-service`      | Reuse (major)          | Bulk load facilities (with manager cols), assets, vendors      |
| `egov-hrms`              | Reuse (major)          | POC/vendor staff/reviewers + facility managers (`COMPLAINANT`) |
| `im-services`            | Reuse (major)          | Asset-level tickets, auto-assign, workflow actions, SLA        |
| `inbox`                  | Optional               | Only if web UI uses DIGIT inbox                                |
| `processor-services`     | Optional               | Only if video pipeline is retained                             |
| `im-services-analytics`  | Not required (Phase 1) | E4H reporting not reused as-is for Livelihood                  |
| `rms-service`            | Not required (Phase 1) | No telemetry→ticket for Livelihood MVP                         |
| `amc-scheduler-service`  | Not required (Phase 1) | AMC is out of scope                                            |


### 4.5 Automation and crons


| Component                                    | Status         | Livelihood adaptation                                          |
| -------------------------------------------- | -------------- | -------------------------------------------------------------- |
| `automation-cronjob` (`cronJobAPIConfig.py`) | Reuse (extend) | Add Livelihood business service to call `/auto/{bs}/_escalate` |
| `im-services` `NotificationConsumer`         | Reuse (major)  | Handle Livelihood escalation/auto-close actions correctly      |
| Daily/weekly escalation emails               | Optional       | Only if Livelihood wants email reporting jobs                  |


**Important:** AMC/RMS crons do **not** close tickets. Livelihood automation is **workflow + IM + escalation cron**.

---

## 5. Service responsibility matrix (who owns what)

### 5.1 Master data and registry (Phase 1)


| Capability                            | Owning service                  | Notes                                                 |
| ------------------------------------- | ------------------------------- | ----------------------------------------------------- |
| States/districts/blocks               | `boundary-service` + MDMS       | Loaded via ingestion when required                    |
| Facility                              | facility registry               | Premises, geography, facility contact                 |
| Facility manager                      | facility registry + `egov-hrms` | **1:1** manager per facility boundary; mobile for OTP |
| Facility ↔ project                    | `project`                       | `ProjectFacility` links                               |
| Vendor master                         | `vendor-registry`               | Org users provisioned via HRMS path                   |
| Asset master                          | `asset-registry`                | `facilityId` required; `vendorId` on asset            |
| Config masters (issues/SLA/templates) | `egov-mdms-service-v2`          | Tenant/module scoped                                  |


### 5.2 Issue lifecycle (Phase 1)


| Capability                  | Owning service                           | Notes                                               |
| --------------------------- | ---------------------------------------- | --------------------------------------------------- |
| Create/update/search ticket | `im-services`                            | Must include `facilityId + assetId`                 |
| List assets for issue UI    | `asset-registry`                         | `POST /v1/asset/_search` with `criteria.facilityID` |
| Workflow engine             | `egov-workflow-v2`                       | Business service e.g. `LivelihoodIncident`          |
| Auto-assign on create       | `im-services`                            | Derive vendor from asset                            |
| SLA compute                 | `im-services` + MDMS                     | 7d/3d/14d/72h semantics                             |
| SLA breach / auto-close     | workflow + cron + `im-services` consumer | AutoEscalation + Kafka consumer                     |
| Evidence/quotation docs     | `egov-filestore`                         | Store `fileStoreId` refs                            |


### 5.3 Installation lifecycle (Phase 2 requirements)

Keep the install track owned primarily by: `project`, `field-planner`, `field-planner-activity`, `asset-registry`, `egov-otp`, `egov-filestore`.

---

## 6. End-to-end flows (touchpoints)

### 6.1 Installation happy path (Phase 2)

```text
ingestion-service → facilities (manager contact columns) + assets (asset.facilityId)
       ↓
facility registry, asset-registry, vendor-registry
       ↓
project (justification code, multi-state) → project/facility links
       ↓
field-planner (plans per facility × vendor)
       ↓
field-planner-activity (QC per item code → approve)
       ↓
egov-otp + filestore (acknowledgment) → handover
       ↓
asset-registry (serial, warranty, QR — still under facilityId)
```

### 6.2 Issue happy path (Phase 1)

```text
Facility manager / POC → egov-user (auth)
       ↓
Resolve facility (manager’s facility or POC-selected facility)
       ↓
asset-registry _search (criteria.facilityID) → select asset
       ↓
im-services _create (facilityId + assetId → asset.vendor → auto-assign)
       ↓
workflow transitions (vendor/POC/manager/system)
       ↓
notifications (SMS/email) + escalations (auto/_escalate + consumer)
```

---

## 7. Implementation priority (highest effort first)

1. `im-services` + workflow + MDMS: auto-assign, Livelihood states, SLAs, reopen/auto-close, escalation consumer logic.
2. `asset-registry`: multi-asset per facility, vendor link, `_search` by `facilityID`, QR/warranty.
3. facility registry + `egov-hrms`: facility boundary + **1:1** manager provisioning (`COMPLAINANT`).
4. `project`: multi-state + justification-code mapping.
5. `ingestion-service`: Livelihood templates/validators (facility includes manager columns).
6. Cron/AutoEscalation: add Livelihood business service rules.

---

## 8. Out of scope (Phase 1)


| Item                                       | Reason                                        |
| ------------------------------------------ | --------------------------------------------- |
| `amc-scheduler-service` + visit scheduling | AMC deferred                                  |
| `rms-service` + RMS crons                  | RMS creates tickets for health telemetry only |
| `im-services-analytics` (as-is)            | E4H-specific reporting; Livelihood later      |
| ERP real-time integration                  | Future phase                                  |
| Native IVR/WhatsApp DIGIT integration      | Pilot: POC manual create                      |


---

## 9. Deployment view (minimal Phase 1 backend set)

**Required (with modifications):**  
`health-facility-registry` (facility registry), `project`, `vendor-registry`, `asset-registry`, `ingestion-service`, `egov-hrms`, `im-services`, `egov-mdms-service-v2`, `egov-workflow-v2`, `boundary-service`, `egov-idgen`, `egov-filestore`, `egov-notification-sms`

**External backbone (required):** `egov-user`, `egov-otp`, `egov-localization`, `egov-persister`

**Optional:** `inbox`, `processor-services`, reporting email jobs

**Exclude Phase 1:** `rms-service`, `amc-scheduler-service`, `im-services-analytics`

---

## 10. API inventory 

- **OpenAPI bundle (source-of-truth):** `LIVELIHOOD_API_SPECS.md`
- **Human-readable API map:** `LIVELIHOOD_API_SPECS_CONCISE.md`

---

## 11. Open design decisions 

---

1. **Tenant strategy**: separate tenant vs shared tenant with module flags (if fork is “only tenant”, align across all services).
2. **Item code vs asset**: single entity vs two linked entities (both still keyed by `facilityId`).
3. **Asset↔vendor mapping source**: asset-registry primary vs MDMS-only.
4. **72h auto-close**: implement via MDMS AutoEscalation + existing consumer vs dedicated cron; keep semantics identical.
5. **Workflow business service naming**: single `LivelihoodIncident` vs priority variants.
6. **Inbox/search strategy**: DIGIT inbox module vs service `_search` APIs only.

