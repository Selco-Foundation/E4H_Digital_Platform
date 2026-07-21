# Livelihood Platform — Changes Relative to E4H

**Document purpose:** Single reference for forking/extending the E4H backend for the Livelihood program.  
**Base platform:** E4H (Energy for Health) on DIGIT.  
**Sources:** Livelihood Installation App Requirements, Issue Creation Platform Requirements, and E4H codebase review.  
**Version:** v2.4 (Draft)

DEV OPS: 

1. We will fork the current E4H git branch, and modify the relevant services to cater to the Livelihood application
2. A completely separate environment will be used to deploy the Livelihood application and DB
3. A  new frontend is to be deployed, created from scratch

---

## 1. Executive summary (Phase 1)

**Entity rules (confirmed):**

1. **No separate “end user” entity** — requirement PDFs use “end user” for the **person**; in DIGIT that person is only the **facility manager** (`COMPLAINANT` HRMS user, 1:1 with a facility). Do not model `endUserId`, project↔user links, or user-owned assets.
2. **Facility sits under project** — program membership is `**Project` → `ProjectFacility` → `Facility`**. Field plans, install QC, and POC “sites in program” use facilities linked to the project, not users linked to the project.
3. **Assets sit under facility** — each **facility** has **multiple assets** (`asset.facilityId`). Tickets use `facilityId` + `assetId`; the facility manager acts on behalf of the facility, not as owner of asset rows.

Livelihood reuses the **same DIGIT instance, auth, and core patterns** as E4H. Work splits into two tracks:


| Track                | Primary existing services                                                                                                                  | Nature of change                                                   |
| -------------------- | ------------------------------------------------------------------------------------------------------------------------------------------ | ------------------------------------------------------------------ |
| **Installation**     | `project`, `field-planner`, `field-planner-activity`, `health-facility-registry`, `vendor-registry`, `asset-registry`, `ingestion-service` | Extend entity model and field-plan unit (facility × vendor)        |
| **Issues / support** | `im-services`, `egov-workflow-v2`, `egov-mdms-service-v2`                                                                                  | Asset-level auto-assignment, new workflow states/SLAs, POC scoping |


**Auth is inherited** (`egov-user`, `egov-otp`) — not rebuilt in HRMS or a new auth service.

**AMC and RMS are not required** for Livelihood phase 1. Ticket status automation uses **workflow + IM + cron jobs**, not AMC/RMS.

---

## 2. Unified entity model (Phase 1)

### 2.0 Key principles


| Principle                                   | Meaning                                                                                                                                                                                                           |
| ------------------------------------------- | ----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| **Facility under project**                  | A livelihood **site** is a **facility** linked to a **project** via `project/facility` (`ProjectFacility`). Justification code drives which facilities join the project—not a parallel “end user ↔ project” link. |
| **Facility is the asset container**         | All machines / item codes / assets belong to a **facility** via `facilityId`. There is no `endUserId` on asset, project, or field-plan records.                                                                   |
| **Facility manager (= “end user” in PDFs)** | The farmer/producer is the **facility manager** only: contact on facility, HRMS `COMPLAINANT`, login/OTP/complaints. Not a separate registry object.                                                              |
| **Manager ↔ facility (1:1)**                | **Exactly one** facility manager per facility (hard rule). Scope is the **facility** (and its project link), not “all users in a justification batch.”                                                            |
| **Vendors attach to assets**                | Each asset at a facility has one **vendor**; field plans use **facility × vendor**; tickets use **asset → vendor**.                                                                                               |


**PDF glossary → implementation**


| Requirement wording              | Implementation                                                                   |
| -------------------------------- | -------------------------------------------------------------------------------- |
| “End user” / “mapped to project” | **Facility** in `ProjectFacility`; manager provisioned on **facility** create    |
| “End user has item codes”        | **Facility** has item codes/assets (`facilityId` on asset / field-plan grouping) |
| “End user raises issue”          | **Facility manager** raises for `facilityId` + selected `assetId`                |


### 2.1 Mapping: requirements → E4H concepts


| Livelihood concept   | E4H equivalent                                                                                | Notes                                                                                    |
| -------------------- | --------------------------------------------------------------------------------------------- | ---------------------------------------------------------------------------------------- |
| **Facility manager** | HRMS/`egov-user` with `**COMPLAINANT`** + jurisdiction on **facility boundary** (E4H pattern) | **1:1** with facility; mobile + QR/OTP login; **not** linked to project directly         |
| Site / premises      | **Facility** (`health-facility-registry`)                                                     | Under **project** via `ProjectFacility`; holds geography, contact, and **all assets**    |
| Program membership   | **Project** + **ProjectFacility**                                                             | POC works on **facilities in project**; no `ProjectEndUser` or user-level project link   |
| Machine / solution   | **Item code** (install) / **Asset** (issues)                                                  | **Many per facility**; mapped to `facilityId` only; solar flag on item code              |
| Supplier             | **Vendor** (`vendor-registry`)                                                                | One vendor per item code / asset                                                         |
| Program grouping     | **Project** (`project`)                                                                       | Driven by **justification code**; links **facilities** (not asset lists on users)        |
| Field work unit      | **Field plan**                                                                                | **Per facility × vendor** (assets at that facility from that vendor grouped on the plan) |
| Support ticket       | **Incident** (`im-services`)                                                                  | `facilityId` for context + **assetId** for the affected machine → auto-assign vendor     |


### 2.2 Relationship diagram

```text
Justification Code
       │
       ▼
   Project (multi-state: states / districts / blocks)
       │
       └──► ProjectFacility ──► Facility (site / premises)     ◄── program scope is HERE
                                    │
                    ┌───────────────┼───────────────┐
                    │               │               │
            Facility manager    Asset / Item code   Field plan(s)
            (1:1; COMPLAINANT;  (many; facilityId  (per Facility
             NOT on project)      only)              × Vendor)
                    │                   └──► Vendor (one per asset)
                    │
                    └──► Login / OTP / complaints for this facility only

Issue ticket: facilityId + assetId (facility must be the asset’s facility; typically in POC’s project list) → auto-assign asset’s vendor
```

### 2.3 Worked examples

**Installation:** 1 justification code → 1 project → **3 facilities** (each with a facility manager) → each **facility** has 2 item codes/assets from 2 vendors → **6 field plans** (3 facilities × 2 vendors). Item codes are on the facility, not on the manager user record.

**Issues:** Facility “Patil Dairy” has roti machine (Vendor A) and solar unit (Vendor B), both registered with `facilityId = Patil Dairy`. The **facility manager** logs in, sees **the facility’s assets**, selects the roti machine → ticket gets Vendor A. Assignment never uses a single facility-wide vendor default.

---

## 3. Changes vs existing E4H (Phase 1)

### 3.1 Installation track (Phase 2)


| Aspect                 | E4H (current)             | Livelihood (required)                                                                                     |
| ---------------------- | ------------------------- | --------------------------------------------------------------------------------------------------------- |
| Project geography      | Single state per project  | **Multi-state** under one justification code                                                              |
| Project driver         | Project setup             | **Justification code** auto-maps **facilities** to project (`ProjectFacility`); managers stay on facility |
| Site / operator        | Health facility (as site) | **Facility** under project; **facility manager** is the operator (not a separate program entity)          |
| Assets per site        | ~One solar system         | **Many assets per facility** (not per user)                                                               |
| Vendor in install flow | Not central               | **Central**; item code ↔ vendor master                                                                    |
| Field plan unit        | Per **facility**          | Per **facility × vendor**                                                                                 |
| Field plan roles       | Internal only             | **Vendor** Field Staff / Supervisor + **SELCO** Installation Reviewer                                     |
| QC forms               | Solar-focused             | **Item-code driven**: full solar form if solar flag; else lightweight (serial, warranty, evidence)        |
| Letters                | None                      | **Acknowledgment** (OTP-verified) + **Handover** (separate templates)                                     |
| Notifications          | Limited                   | Email on role assignment; email to **procurement** on install + handover complete                         |
| AMC                    | Used in E4H               | **Out of scope** phase 1                                                                                  |


### 3.2 Issue / support track (Phase 1)


| Aspect         | E4H (current)                      | Livelihood (required)                                                        |
| -------------- | ---------------------------------- | ---------------------------------------------------------------------------- |
| Complainant    | Health facility                    | **Facility manager** (`COMPLAINANT`), acting for a **facility** in a project |
| Assignment     | Facility → vendor; **CRM assigns** | **Asset → vendor**; **auto-assign** on create                                |
| Oversight      | CRM                                | **Program POC** (state-scoped, like CRM)                                     |
| Entry channels | Platform                           | IVR / WhatsApp (POC creates in pilot) + direct platform + QR/OTP             |
| Vendor actions | Resolve-focused                    | Resolve, **Out of Scope**, **Out of Warranty** (quotation, 14d window)       |
| POC actions    | Reassign                           | Reassign same vendor, assign different vendor, **decline**                   |
| Reopen         | Existing patterns                  | **72 hours** after resolve → else **Closed After Resolution**                |
| SLAs           | E4H incident SLAs                  | 7d vendor, 3d POC (out of scope), 14d warranty quotation                     |
| Languages      | As configured                      | **English + Kannada** (v1)                                                   |


### 3.3 Auth and users (Phase 1)


| Aspect            | E4H (current)                                                         | Livelihood                                                                                                                                      |
| ----------------- | --------------------------------------------------------------------- | ----------------------------------------------------------------------------------------------------------------------------------------------- |
| Login             | `egov-user` + `egov-otp`                                              | **Same** (inherited)                                                                                                                            |
| Employee users    | `egov-hrms` creates employee → `egov-user`                            | **Same** for vendor staff, POC, reviewers                                                                                                       |
| Facility manager  | HRMS employee + `COMPLAINANT`; jurisdiction = `facility.boundaryCode` | **Reuse E4H pattern** on facility create/ONM; **not** project-scoped; **1:1** per facility(Explore the role of citizen as the facility manager) |
| QR + OTP on asset | Not in E4H IM                                                         | QR → asset/facility context; OTP via `egov-otp` to manager **mobile** (same HRMS user)                                                          |
| Manager login     | Username/password (facility id / HFR)                                 | **Also** mobile OTP and QR-first flows (Livelihood UI); credentials optional per tenant                                                         |


### 3.3.1 Facility manager provisioning and login (Option 1)

```text
Facility create → boundaryCode = {block}_{facilityId}//(data created via CSV manually)
       ↓
HRMS employee (COMPLAINANT) + jurisdiction.boundary = facility.boundaryCode
       ↓
egov-user (mobile on record) ← OTP / QR / credentials login
       ↓
Session scoped to one facility (1:1) → list assets by facilityId → raise incident
```

- **No** `POST /v2/facility/manager/_link` — jurisdiction on the facility boundary is the binding.
- **Uniqueness:** one active `COMPLAINANT` per facility boundary; one manager mobile should not map to multiple facilities.
- **QR flow:** `asset-registry` `qr/_resolve` → `facilityId` / `assetId` → OTP to manager mobile → verify user’s jurisdiction matches resolved facility boundary.

### 3.4 What does not change for E4H health (Phase 1)

Per requirements: **no regression** to existing E4H health installation and incident flows. Use **tenant/module/program segregation** (configuration), not breaking shared services.

---

## 4. Service inventory (Phase 1)

### 4.1 Legend


| Status            | Meaning                                                                |
| ----------------- | ---------------------------------------------------------------------- |
| **Reuse (minor)** | Deploy and configure; small or no code changes                         |
| **Reuse (major)** | Keep service; schema, APIs, workflow, or rules need Livelihood changes |
| **Not required**  | Omit from Livelihood phase 1 deployment                                |
| **External**      | Not in this repo; required from DIGIT backbone                         |


---

### 4.2 External platform services (shared, not forked)


| Service               | Handles                                                                 | Livelihood                                              |
| --------------------- | ----------------------------------------------------------------------- | ------------------------------------------------------- |
| **egov-user**         | Login, OAuth tokens, roles, user CRUD                                   | **Required** — same as E4H                              |
| **egov-otp**          | Send/validate OTP (citizen login, password reset, future asset QR flow) | **Required**                                            |
| **egov-localization** | EN / Kannada messages                                                   | **Required**                                            |
| **egov-persister**    | Kafka → DB for async writes                                             | **Required** where services use Kafka persister pattern |
| **egov-searcher**     | Inbox/search indices (if inbox UI used)                                 | **Optional**                                            |


---

### 4.3 Core services (`backend/core-services/`)


| Service                      | Status        | Handles today                                       | Livelihood role                                                                                 | Reason to include / modify                                  |
| ---------------------------- | ------------- | --------------------------------------------------- | ----------------------------------------------------------------------------------------------- | ----------------------------------------------------------- |
| **boundary-service**         | Reuse (minor) | State / district / block boundaries                 | Geography master for projects and POC state scope                                               | Required by install doc; already used                       |
| **egov-mdms-service-v2**     | Reuse (major) | Master data, schemas, config                        | Item codes, issue types/subtypes per asset, SLA hours, AutoEscalation rules, letter templates   | All configurable business rules                             |
| **egov-workflow-v2**         | Reuse (major) | Process instances, transitions, auto-escalation API | Livelihood incident business service(s): new states and actions                                 | Resolve */ out_of_scope / out_of_warranty /* reopen / close |
| **egov-idgen**               | Reuse (minor) | IDs for entities                                    | Project, facility, asset, ticket, document IDs                                                  | Standard DIGIT pattern                                      |
| **egov-filestore**           | Reuse (minor) | File upload/storage                                 | Photos, videos, quotations, acknowledgment/handover PDFs                                        | Required for evidence and letters                           |
| **egov-notification-sms**    | Reuse (minor) | SMS                                                 | Ticket lifecycle SMS matrix per issue doc                                                       | Notifications                                               |
| **health-facility-registry** | Reuse (major) | Health facility registry                            | **Facility** + per-facility **boundary**; manager contact; HRMS user provision (reuse POC flow) | Central site entity; assets hang off facility               |
| **zuul**                     | Not required  | Stub in repo only                                   | N/A                                                                                             | Gateway lives in deployment, not this folder                |


---

### 4.4 E4H domain services (`backend/e4h-services/`)


| Service                    | Status                 | Handles today                                                     | Livelihood role                                                                                              | Reason to include / modify                              |
| -------------------------- | ---------------------- | ----------------------------------------------------------------- | ------------------------------------------------------------------------------------------------------------ | ------------------------------------------------------- |
| **project**                | Reuse (major)          | Project CRUD, **project↔facility**, staff, tasks, beneficiaries   | Project from **justification code**, **multi-state**, map facilities                                         | Core program container; `justificationCode` requirement |
| **field-planner**          | Reuse (major)          | Field plans, geography, role assignment emails                    | Plans at **facility × vendor**; vendor on plan; role emails                                                  | Install doc field-plan unit change                      |
| **field-planner-activity** | Reuse (major)          | Installation QC, facility activities, BOM/PDF, vendor org linkage | Item-code forms, approval chain, **OTP acknowledgment**, handover generation                                 | Closest to Installation QC app backend                  |
| **vendor-registry**        | Reuse (major)          | Organisation/vendor master, org users → HRMS                      | Vendor master, item-code mapping (or coordinate with asset-registry)                                         | Vendor-centric install and issue assignment             |
| **asset-registry**         | Reuse (major)          | Asset registry, AMC linkage                                       | **Many assets per `facilityId`** (no direct end-user FK), vendor per asset, serial/warranty/solar, **QR**    | Issue auto-assign and install evidence                  |
| **ingestion-service**      | Reuse (major)          | Python bulk Excel ingestion                                       | Facilities **with manager columns** (same ingest as facility), assets, vendors, boundaries                   | Master data before project creation                     |
| **egov-hrms**              | Reuse (major)          | Employee registry → `egov-user`                                   | Vendor staff, POC (state jurisdiction), **facility managers** (`COMPLAINANT` + facility boundary), reviewers | Manager = 1 HRMS user per facility boundary             |
| **im-services**            | Reuse (major)          | Incidents, workflow integration, notifications, SLA               | **Asset-level tickets**, auto-assign vendor, new statuses, POC filters, reopen/close rules                   | Core issue platform (Saura eMitra)                      |
| **inbox**                  | Optional               | Aggregates workflow + service for list UIs                        | POC/vendor inbox if web uses DIGIT inbox pattern                                                             | Reuse only if UI depends on it                          |
| **processor-services**     | Optional               | Kafka video processing for IM                                     | Only if heavy video pipeline retained                                                                        | Not required for Livelihood MVP                         |
| **im-services-analytics**  | Not required (phase 1) | E4H escalation emails, weekly DRE reports                         | Replace with Livelihood-specific reporting later if needed                                                   | E4H-specific; not issue MVP                             |
| **rms-service**            | Not required (phase 1) | Remote monitoring → **create** health tickets                     | N/A currently for Livelihood                                                                                 | Telemetry auto-tickets for health solar only            |
| **amc-scheduler-service**  | Not required (phase 1) | AMC config, scheduled visits, visit OTP                           | Deferred per install doc                                                                                     | AMC out of scope                                        |
| **project** (partial APIs) | Partial                | Beneficiary, household-style task APIs                            | Likely **unused** unless repurposed                                                                          | Health-campaign APIs; facility is better fit            |


---

### 4.5 Automation and crons (`backend/docs/cron/`, K8s YAML)


| Component                                      | Status         | Handles                                                                                     | Livelihood role                                                                |
| ---------------------------------------------- | -------------- | ------------------------------------------------------------------------------------------- | ------------------------------------------------------------------------------ |
| **automation-cronjob** (`cronJobAPIConfig.py`) | Reuse (extend) | Calls `egov-workflow-v2` `/egov-wf/auto/{businessService}/_escalate` for Incident* services | Configure **Livelihood** business service names and MDMS AutoEscalation        |
| **im-services** `NotificationConsumer`         | Reuse (major)  | Consumes `im-auto-escalation`; applies workflow actions on SLA breach                       | Extend actions for Livelihood states (e.g. auto-close after 72h if configured) |
| **daily / weekly escalation crons**            | Optional       | Call `im-services-analytics` for **email reports**                                          | New Livelihood analytics or extend later                                       |
| **visit-scheduling-cron**                      | Not required   | Calls **amc-scheduler-service**                                                             | AMC out of scope                                                               |
| **rms-*-cron**                                 | Not required   | RMS rule engine, mapping, pause expiry                                                      | No RMS in Livelihood phase 1                                                   |
| **theft-notification cron**                    | Optional       | E4H-specific IM endpoint                                                                    | Only if Livelihood needs analogous alerts                                      |


**Important:** AMC and RMS crons do **not** move tickets to Resolved/Closed. Status automation is **workflow + IM + escalation cron**, not AMC/RMS.

---

### 4.6 Frontends and mobile (reference only)


| App                                         | Role in Livelihood                                                        |
| ------------------------------------------- | ------------------------------------------------------------------------- |
| **Web portal** (micro-ui / installation-ui) | Project setup, field plans, master data, letter templates, POC dashboards |
| **Installation QC mobile**                  | Field Staff / Supervisor QC; reuse auth (`user/oauth/token`)              |
| **Livelihood issue UI**                     | Facility manager / vendor / POC; same auth; “my facility’s assets”        |


Backend doc scope: services above; UI forks share auth proxy (`/user`, `/user-otp`).

---

## 5. Service responsibility matrix (who handles what) (Phase 1)

### 5.1 Master data and registry


| Capability                                         | Owning service                                                 | Notes                                                                                  |
| -------------------------------------------------- | -------------------------------------------------------------- | -------------------------------------------------------------------------------------- |
| States, districts, blocks                          | **boundary-service** + MDMS                                    | Ingestion may load via **ingestion-service**                                           |
| Facility (site)                                    | **health-facility-registry**                                   | Premises, geography, facility-level contact                                            |
| Facility manager                                   | **health-facility-registry** + **egov-hrms**                   | **1:1** with facility; provisioned with facility boundary jurisdiction; mobile for OTP |
| Facility ↔ project                                 | **project** (`project/facility/v1/*`)                          | **ProjectFacility** links; POC/install scope; justification code selects facilities    |
| Manager ↔ facility mapping                         | **health-facility-registry** (or facility `additionalDetails`) | One manager per facility; **no** manager↔project API                                   |
| Vendor / organisation                              | **vendor-registry**                                            | Org users created via HRMS path                                                        |
| Asset / item code                                  | **asset-registry** (+ MDMS)                                    | `**facilityId` required**; `vendorId`; no direct link to facility manager user id      |
| Item code ↔ vendor mapping                         | **asset-registry** and/or **vendor-registry** + MDMS           | Per asset at a facility                                                                |
| Bulk load (Excel)                                  | **ingestion-service**                                          | Facilities (+ manager on same row/sheet) → assets → vendors                            |
| Configurable masters (issue types, SLA, templates) | **egov-mdms-service-v2**                                       | Per-tenant Livelihood module                                                           |


### 5.2 Installation lifecycle (Phase 2)


| Capability                                       | Owning service                                                               | Notes                                       |
| ------------------------------------------------ | ---------------------------------------------------------------------------- | ------------------------------------------- |
| Create project (justification code, multi-state) | **project**                                                                  | Auto-map facilities from justification code |
| Link facility ↔ project                          | **project** (`project/facility/`*)                                           | Existing ProjectFacility pattern            |
| Create field plan (facility × vendor)            | **field-planner**                                                            | **Major change** from per-facility plan     |
| Assign Field Staff / Supervisor / Reviewer       | **field-planner** + **field-planner-activity**                               | Vendor + SELCO roles; assignment emails     |
| Installation QC submit / approve                 | **field-planner-activity**                                                   | Item-code-driven forms                      |
| BOM / install documents (if retained)            | **field-planner-activity**                                                   | Reuse or simplify for non-solar             |
| Post-install asset registration                  | **asset-registry** (+ call from field-planner-activity)                      | Serial, warranty, vendor                    |
| Acknowledgment letter + OTP                      | **field-planner-activity** or new module + **egov-otp** + **egov-filestore** | OTP via existing OTP service                |
| Handover letter                                  | Same + **filestore**                                                         | Admin templates in MDMS / web               |
| Procurement / role assignment email              | **field-planner** (existing email patterns) + notification service           | Extend templates                            |


### 5.3 Issue / ticket lifecycle (Phase 1)


| Capability                                       | Owning service                                                                     | Notes                                                                     |
| ------------------------------------------------ | ---------------------------------------------------------------------------------- | ------------------------------------------------------------------------- |
| Create / update / search ticket                  | **im-services**                                                                    | `facilityId` + **assetId**; validate asset belongs to facility            |
| List assets at facility (issue UI)               | **asset-registry** `POST /v1/asset/_search`                                        | `criteria.facilityID`                                                     |
| Workflow state machine                           | **egov-workflow-v2**                                                               | New business service e.g. `LivelihoodIncident`                            |
| Auto-assign vendor on create                     | **im-services**                                                                    | Read **asset**→vendor (assets scoped by facility); transition to Assigned |
| Vendor: Resolve / Out of Scope / Out of Warranty | **im-services** + workflow                                                         | New actions and validations                                               |
| POC: reassign, decline, raise on behalf          | **im-services** + workflow                                                         | State-scoped search                                                       |
| SLA calculation                                  | **im-services** (`SLAService`) + MDMS                                              | 7d / 3d / 14d per issue doc                                               |
| SMS notifications                                | **egov-notification-sms** + **im-services** `NotificationService`                  | Event matrix from requirements                                            |
| 72h reopen → auto close                          | **egov-workflow-v2** AutoEscalation + **im-services** consumer and/or **new cron** | May need new MDMS rules (not in AMC/RMS)                                  |
| SLA breach → POC email                           | **cron** → workflow `_escalate` and/or analytics-style email job                   | Extend `cronJobAPIConfig.py`                                              |
| File evidence / quotation                        | **egov-filestore**                                                                 | Linked on incident documents                                              |
| IVR / WhatsApp (pilot)                           | External + **im-services** (POC manual create)                                     | Phase 1: POC creates ticket in platform                                   |


### 5.4 Users and auth


| Capability                         | Owning service                | Notes                                                                                                 |
| ---------------------------------- | ----------------------------- | ----------------------------------------------------------------------------------------------------- |
| Login, token, refresh              | **egov-user**                 | Unchanged                                                                                             |
| OTP send/validate                  | **egov-otp**                  | Citizen login; acknowledgment; asset QR (future)                                                      |
| Employee / vendor user record      | **egov-hrms** → **egov-user** | **vendor-registry** triggers HRMS create                                                              |
| Facility manager                   | **egov-hrms** → **egov-user** | `COMPLAINANT` + jurisdiction on **facility `boundaryCode`**; mobile for OTP/QR; **1:1** with facility |
| Jurisdiction / state scope for POC | **egov-hrms** + UI session    | State/district/block jurisdiction (not facility boundary)                                             |


---

## 6. End-to-end flows (service touchpoints) (Phase 1)

### 6.1 Installation (happy path) (Phase 2)

```text
ingestion-service → facilities (manager contact on facility ingest → HRMS `COMPLAINANT` when ONM-ready) + assets (each asset.facilityId)
       ↓
health-facility-registry, asset-registry, vendor-registry
       ↓
project (justification code, multi-state) → project/facility
       ↓
field-planner (per facility × vendor; assets at facility from that vendor)
       ↓
field-planner-activity (QC per item code at facility → approve)
       ↓
egov-otp + filestore (acknowledgment to facility manager) → handover
       ↓
asset-registry (serial, warranty, QR — still under same facilityId)
```

### 6.2 Issue (happy path) (Phase 1)

```text
Facility manager / POC → egov-user (auth)
       ↓
Resolve facility (manager’s facility or POC-selected facility)
       ↓
asset-registry `_search` (`criteria.facilityID`) → user selects asset
       ↓
im-services (_create: facilityId + assetId → asset.vendor → auto-assign)
       ↓
egov-workflow-v2 (state: Assigned → … → Resolved)
       ↓
egov-notification-sms (SMS to vendor, POC, facility manager)
       ↓
[Optional cron] workflow _escalate → im-services (72h close, SLA breach)
```

---

## 7. Highest-effort modifications (implementation priority) (Phase 1)

1. **im-services** + **egov-workflow-v2** + MDMS — asset auto-assign, Livelihood states, SLAs, reopen/close.
2. **field-planner** + **field-planner-activity** — facility×vendor, item-code forms, letters/OTP.
3. **asset-registry** — multi-asset, vendor link, QR/warranty; `**_search` by `facilityID`** for issue/install UIs.
4. **health-facility-registry** + **egov-hrms** — facility boundary + **1:1** manager provision (`COMPLAINANT`); assets remain on facility.
5. **project** — multi-state, justification-code mapping.
6. **ingestion-service** — Livelihood templates and validators.
7. **Cron / AutoEscalation** — Livelihood business service rules (not AMC/RMS).

---

## 8. Out of scope (phase 1) (Phase 2)


| Item                                              | Reason                                                              |
| ------------------------------------------------- | ------------------------------------------------------------------- |
| **amc-scheduler-service** + visit-scheduling cron | AMC deferred in install requirements                                |
| **rms-service** + RMS crons                       | Health remote monitoring only; creates tickets, does not close them |
| **im-services-analytics** (as-is)                 | E4H reporting; Livelihood dashboards later                          |
| ERP real-time integration                         | Future phase per both docs                                          |
| IVR/WhatsApp native DIGIT integration             | Pilot: POC manual ticket creation                                   |


---

## 9. Deployment view (minimal Livelihood backend set) (Phase 1)

**Required with modifications:**  
`health-facility-registry`, `project`, `field-planner`, `field-planner-activity`, `vendor-registry`, `asset-registry`, `ingestion-service`, `egov-hrms`, `im-services`, `egov-mdms-service-v2`, `egov-workflow-v2`, `boundary-service`, `egov-idgen`, `egov-filestore`, `egov-notification-sms`

**Plus external:** `egov-user`, `egov-otp`, `egov-localization`, `egov-persister`

**Optional:** `inbox`, `processor-services`, escalation email jobs

**Exclude phase 1:** `rms-service`, `amc-scheduler-service`, `im-services-analytics`

---

## 10. Open design decisions (resolve during implementation) (Phase 2)

1. **Tenant strategy:** Separate Livelihood tenant vs shared `in` with module flags.
2. ~~**Facility manager storage**~~ **Resolved:** Reuse E4H — facility POC contact on facility record + one HRMS user per facility with `jurisdiction.boundary = facility.boundaryCode` (`COMPLAINANT`). **No** separate link API. Login: mobile OTP, QR, and/or credentials.
3. ~~**End user vs facility vs project**~~ **Resolved:** **No end-user entity.** Person = **facility manager** (1:1 facility). Program scope = **facility under project** (`ProjectFacility`). Assets under `facilityId` only.
4. **Item code vs asset:** Single entity with lifecycle stages or two linked entities (both still keyed by `facilityId`).
5. **Asset↔vendor mapping store:** `asset-registry` primary vs MDMS-only.
6. **72-hour auto-close:** MDMS AutoEscalation + existing consumer vs dedicated cron job.
7. **Business service name(s):** One `LivelihoodIncident` vs priority variants like E4H `Incident_Low/Medium/High`.
8. **Inbox:** Keep DIGIT inbox vs IM search APIs only for POC/vendor lists.

---

## 11. Document references (Phase 1)


| Document                                                         | Topic                                     |
| ---------------------------------------------------------------- | ----------------------------------------- |
| Livelihood Installation App Requirements (PDF)                   | Install track, field plans, letters       |
| Issue Creation Platform Requirements (PDF)                       | IM workflow, channels, SLAs               |
| `backend/e4h-services/rms-service/README.md`                     | RMS scope (create tickets from telemetry) |
| `backend/docs/cron/cronJobAPIConfig.py`                          | SLA auto-escalation cron                  |
| `backend/e4h-services/im-services/.../NotificationConsumer.java` | IM consumer for auto-escalation topic     |


---

## 12. API inventory — reuse (with modification) vs new (Phase 1)

API paths below are taken from the current E4H codebase. Full URL = gateway host + **context path** + route (e.g. `POST {host}/facility-service/v2/facility/create`).

### 12.0 Legend


| Tag        | Meaning                                                      |
| ---------- | ------------------------------------------------------------ |
| **Reuse**  | Same endpoint; tenant/MDMS/config only                       |
| **Modify** | Same route; extend payload, validation, filters, or workflow |
| **New**    | New route or new business capability                         |
| **N/A**    | Not used for Livelihood phase 1                              |


**How to read “Modify” in this section (brief):** “Modify” means *same route*, but Livelihood adds/changes one or more of: **payload fields**, **validations**, **search filters**, **role/jurisdiction access rules**, **workflow/state bindings**, or **MDMS-driven behaviour**. Each sub-section below includes short “Modify notes”.

---

### 12.1 External platform (not in this repo)


| API                                 | Method | Livelihood use                    | Tag                                                |
| ----------------------------------- | ------ | --------------------------------- | -------------------------------------------------- |
| `/user/oauth/token`                 | POST   | Employee & facility manager login | **Reuse**                                          |
| `/user-otp/v1/_send`                | POST   | OTP login; acknowledgment OTP     | **Reuse** / **Modify** (facility-manager's mobile) |
| `/user/password/nologin/_update`    | POST   | Forgot password                   | **Reuse**                                          |
| `/user/_search`                     | POST   | Resolve users (POC, manager)      | **Reuse**                                          |
| `/user/_logout`                     | POST   | Logout                            | **Reuse**                                          |
| `/localization/messages/v1/_search` | POST   | EN / Kannada                      | **Reuse**                                          |


**Modify notes (brief):**

- `**/user-otp/v1/_send`**: Ensure OTP flow supports **facility manager** (Option 1 = HRMS `COMPLAINANT` user with mobile). In practice this is usually **tenant/config** (allowed user types/roles, rate limits) plus UX: allow OTP send for QR-first and direct-mobile flows.

---

### 12.2 `health-facility-registry` — context path `/facility-service`

#### Existing APIs (reuse / modify)


| API                                       | Method | E4H today                | Livelihood                                                                   | Tag                    |
| ----------------------------------------- | ------ | ------------------------ | ---------------------------------------------------------------------------- | ---------------------- |
| `/v2/facility/create`                     | POST   | Create health facility   | Create **Livelihood facility** (site); facility manager fields; program type | **Modify**             |
| `/v2/facility/update`                     | POST   | Update facility          | Manager contact, status, Livelihood attributes                               | **Modify**             |
| `/v2/facility/search`                     | GET    | Search facilities        | Filter by project/state/program                                              | **Modify**             |
| `/v2/facility/_bulk-search`               | POST   | Bulk search              | Ingestion / project mapping                                                  | **Modify**             |
| `/v2/facility/_bulk-search-with-boundary` | POST   | Search with boundary     | POC state-scoped lists                                                       | **Modify**             |
| `/v2/facility/update-block`               | POST   | Block update             | If Livelihood uses block hierarchy                                           | **Reuse** / **Modify** |
| `/v2/facility/assessment/create`          | POST   | Health assessment        | Likely **N/A** v1                                                            | **N/A**                |
| `/v2/facility/assessment/_update`         | POST   | Health assessment update | Likely **N/A** v1                                                            | **N/A**                |
| `/v2/facility/summary`                    | GET    | Assessment summary       | Likely **N/A** v1                                                            | **N/A**                |
| `/v2/facility/migrate_data`               | GET    | Migration utility        | **N/A**                                                                      | **N/A**                |


**Modify notes (brief):**

- `**/v2/facility/create`**: Add/validate **Livelihood facility attributes**; persist manager contact fields; create **facility boundary** (`{blockBoundaryCode}_{facilityId}`) and localization; trigger **HRMS `COMPLAINANT`** provisioning when facility becomes ONM-ready / Livelihood-ready.
- `**/v2/facility/update**`: Allow updating manager contact/status; ensure boundary stays consistent; optionally (re)provision HRMS user when transitioning to ONM-ready.
- `**/v2/facility/search**`: Add filters needed by Livelihood ops/POC flows (e.g., program type, project, state/district/block); ensure performance via boundary indexes.
- `**/v2/facility/_bulk-search` / `_bulk-search-with-boundary**`: Extend criteria to support ingestion and **POC state-scoped** facility lists; return `boundaryCode` and manager contact fields reliably.
- `**/v2/facility/update-block`**: If used, ensure block re-map updates facility boundary relationships and triggers **incident boundary sync** (`im-services /_update-boundary-by-facility`) where required.

#### Facility manager binding (no separate link API)

**Design (Option 1 — confirmed):** **1 facility : 1 manager.** Binding is implicit:

1. `**POST /v2/facility/create`** (modify) — Creates facility-level boundary `{blockBoundaryCode}_{facilityId}` and sets `facility.boundaryCode` (existing E4H behaviour).
2. **HRMS user provision** (reuse) — On facility ONM-ready / Livelihood ingest: `POST /egov-hrms/employees/_create` with manager name, **mobile**, roles `COMPLAINANT` + `EMPLOYEE`, and `jurisdictions[].boundary = facility.boundaryCode`, `boundaryType = Facility` (same as `HRMSService.createFacilityPOCEmployee` today).
3. **Login** — `egov-user` + `egov-otp` to manager mobile; optional QR via asset-registry `qr/_resolve` then OTP. No `manager/_link` route.

**Optional NEW (bootstrap only, not linking):**


| API                                       | Method | Purpose                                                                |
| ----------------------------------------- | ------ | ---------------------------------------------------------------------- |
| `/v2/facility/_resolve-by-manager-mobile` | POST   | After OTP: mobile → single facility (1:1) + `boundaryCode` for session |


**Assets for ticket UI:** `POST /asset-registry/v1/asset/_search` with `criteria.facilityID` (not on facility-service).

**Reuse for manager/facility lookup:** `GET /v2/facility/search` (by boundary or facility id); `POST /egov-hrms/employees/_search` (`phone`, `roles=COMPLAINANT`, boundary).

---

### 12.3 `asset-registry` — context path `/asset-registry`

#### Existing APIs (reuse / modify)


| API                                                             | Method   | E4H today      | Livelihood                                                                                                       | Tag        |
| --------------------------------------------------------------- | -------- | -------------- | ---------------------------------------------------------------------------------------------------------------- | ---------- |
| `/v1/asset/_create`                                             | POST     | Create asset   | `**facilityId`**, `**vendorId`**, item code, solar flag, serial, warranty                                        | **Modify** |
| `/v1/asset/_update`                                             | POST     | Update asset   | Warranty, QR, install status                                                                                     | **Modify** |
| `/v1/asset/_search`                                             | POST     | Search assets  | **Same endpoint**; add/standardize `criteria.facilityID` filter for manager/issue UI; keep vendor/serial filters | **Modify** |
| `/v1/asset/bulk/_create`                                        | POST     | Bulk create    | Bulk ingest assets per facility                                                                                  | **Modify** |
| `/v1/asset/workflow/{assetID}/_update`                          | POST     | Asset workflow | Install QC state if used                                                                                         | **Modify** |
| `/v1/asset/amc/_create`, `/amc/_search`, `/amc/_update`         | POST/GET | AMC on asset   | **N/A**                                                                                                          | **N/A**    |
| `/v1/asset/amc/visit/_create`, `/_search`, `/{visitID}/_update` | POST/GET | AMC visits     | **N/A**                                                                                                          | **N/A**    |


**Modify notes (brief):**

- `**/v1/asset/_create`**: Require `facilityId` and `vendorId` for Livelihood; validate facility exists; set warranty/QR fields; enforce item-code/solar rules where applicable.
- `**/v1/asset/_update`**: Allow updates for warranty, QR assignment, install status/workflow status; enforce that `facilityId` is immutable.
- `**/v1/asset/_search`**: Support/encourage `criteria.facilityID` as the **primary** filter for “my facility assets”; add/validate access controls (manager jurisdiction = facility boundary; POC state scope); keep vendor/serial filters for ops.
- `**/v1/asset/bulk/_create`**: Extend ingestion validations for facilityId/vendorId/itemCode; partial-failure strategy and error reporting.
- `**/v1/asset/workflow/{assetID}/_update`**: If retained, align asset workflow statuses to installation QC (solar vs non-solar) and expose only relevant transitions.

#### Asset search: add `facilityID` as a criterion (reuse — no new route)

Use existing `**POST /v1/asset/_search`**. Add `**criteria.facilityID`** (asset-registry field name; value = facility `facilityId`) as the primary filter for “assets at my facility”.

```json
{
  "RequestInfo": { "apiId": "livelihood-web", "authToken": "Bearer <token>" },
  "criteria": {
    "tenantId": "in.livelihood",
    "facilityID": "fac/2025/41923"
  }
}
```

Query params: `offset`, `limit`. UI (facility manager raise-issue, POC on-behalf asset picker) and **im-services** call asset-registry `_search` with `criteria.facilityID`.

**Implementation notes (modify):** Enforce `facilityID` required for Livelihood tenant when listing for ticket create; optional `isOperational` / `wfStatus` filters; validate caller may access that facility (manager jurisdiction or POC state scope).

#### Other new APIs (recommended)


| API                     | Method | Purpose                                                   |
| ----------------------- | ------ | --------------------------------------------------------- |
| `/v1/item-code/_search` | POST   | Item code master + vendor + solar flag (if not MDMS-only) |
| `/v1/asset/qr/_resolve` | POST   | QR scan → `assetId` + `facilityId` (login / raise issue)  |


---

### 12.4 `vendor-registry` — context path `/vendor`

#### Existing APIs (reuse / modify)


| API                                         | Method | Livelihood                                 | Tag                    |
| ------------------------------------------- | ------ | ------------------------------------------ | ---------------------- |
| `/organisation/v1/_create`                  | POST   | Livelihood vendors                         | **Reuse** / **Modify** |
| `/organisation/v1/_search`                  | POST   | Search for field plans / assignment        | **Reuse**              |
| `/organisation/v1/_update`                  | POST   | Update vendor                              | **Reuse**              |
| `/organisation/v1/user/_create`             | POST   | Org user → HRMS (Field Staff / Supervisor) | **Reuse**              |
| `/organisation/v1/user/_search`             | POST   | Search org users                           | **Reuse**              |
| `/organisation/v1/user/_update`             | POST   | Update org user                            | **Reuse**              |
| `/organisation/v1/user/_delete`             | POST   | Deactivate org user                        | **Reuse**              |
| `/v1/_create`, `/v1/_search`, `/v1/_update` | POST   | Org services                               | **Reuse** / **Modify** |


**Modify notes (brief):**

- `**/organisation/v1/_create`**: Validate Livelihood-required vendor attributes (e.g., item-code/service coverage) and ensure org-user provisioning hooks work for vendor staff.
- `**/v1/_create` / `_update` (org services)**: Extend payload/masters to represent Livelihood service offerings that align to item codes/assets where required for assignment/reassign rules.

#### New APIs (recommended)


| API                                    | Method | Purpose                                                  |
| -------------------------------------- | ------ | -------------------------------------------------------- |
| `/organisation/v1/item-code/_map`      | POST   | Map **item code → vendor** (if not MDMS-only)            |
| `/organisation/v1/vendors-by-facility` | POST   | Distinct vendors for assets at a facility (POC reassign) |


---

### 12.5 `project` — context path `/project`

#### Existing APIs (reuse / modify)


| API                                                        | Method | Livelihood                                                                                  | Tag                    |
| ---------------------------------------------------------- | ------ | ------------------------------------------------------------------------------------------- | ---------------------- |
| `/v1/_create`                                              | POST   | **Multi-state**, **justification code**                                                     | **Modify**             |
| `/v1/_update`                                              | POST   | Update project geography / justification                                                    | **Modify**             |
| `/v1/_search`                                              | POST   | Search Livelihood projects (by project fields; **not** a facility justification search API) | **Modify**             |
| `/facility/v1/_create`                                     | POST   | Link facility to project                                                                    | **Modify**             |
| `/facility/v1/bulk/_create`                                | POST   | Bulk link facilities                                                                        | **Reuse** / **Modify** |
| `/facility/v1/_search`                                     | POST   | List facilities in project                                                                  | **Reuse**              |
| `/facility/v1/_update`, `/_delete`, bulk variants          | POST   | Maintain project–facility links                                                             | **Reuse**              |
| `/staff/v1/_create`, `_search`, `_update`, `_delete`, bulk | POST   | Project staff (SELCO reviewers)                                                             | **Reuse** / **Modify** |
| `/beneficiary/v1/`*                                        | POST   | Health campaign beneficiaries                                                               | **N/A**                |
| `/task/v1/`*, `/resource/v1/`*                             | POST   | Campaign tasks/resources                                                                    | **N/A**                |
| `/user-action/v1/`*, `/user-location/v1/`*                 | POST   | Tracking                                                                                    | **Optional**           |
| `/check/bandwidth`                                         | POST   | Bandwidth check                                                                             | **Optional**           |


**Modify notes (brief):**

- `**/v1/_create`**: Accept `justificationCode` and **multi-state** geography; validate facility lists/project geography consistency.
- `**/v1/_update`**: Enforce `justificationCode` immutability; allow controlled geography updates.
- `**/v1/_search`**: Add filters for Livelihood program views (e.g., by justificationCode/geography/project type) and tune pagination for ops.
- `**/facility/v1/_create` / `bulk/_create`**: Link facilities to projects with dedupe/validation (facility exists, tenant consistency).
- `**/staff/v1/`***: Ensure staff roles and scoping fit Livelihood install operations (vendor + SELCO reviewer patterns).

#### New APIs (recommended)


| API                                  | Method | Purpose                                                           |
| ------------------------------------ | ------ | ----------------------------------------------------------------- |
| `/v1/_create-from-justification`     | POST   | Create project + auto **ProjectFacility** from justification code |
| `/facility/v1/_map-by-justification` | POST   | Attach all facilities for justification code to project           |


---

### 12.6 `field-planner` — context path `/field-planner (Phase 2)`

#### Existing APIs (reuse / modify)


| API                                                    | Method | Livelihood                                     | Tag        |
| ------------------------------------------------------ | ------ | ---------------------------------------------- | ---------- |
| `/v1/field-plans/_create`                              | POST   | Plan per **facility × vendor**; vendor on plan | **Modify** |
| `/v1/field-plans/_update`                              | POST   | Update plan, roles, status                     | **Modify** |
| `/v1/field-plans/_search`                              | POST   | Search by vendor user, facility, project       | **Modify** |
| `/v1/field-plans/facility/_create`                     | POST   | Link facility to plan                          | **Modify** |
| `/v1/field-plans/facility/bulk/_create`                | POST   | Bulk facility×vendor links                     | **Modify** |
| `/v1/field-plans/facility/_search`                     | POST   | Search plan–facility                           | **Modify** |
| `/v1/field-plans/facility/_unassign`, `bulk/_unassign` | POST   | Unassign facility from plan                    | **Reuse**  |


**Modify notes (brief):**

- **Core change:** field plan is **facility × vendor** (not only facility). This affects create/search criteria, uniqueness keys, and validations.
- `**/v1/field-plans/_create`**: Include vendor on plan; validate vendor covers item codes/assets at the facility.
- `**/v1/field-plans/_search`**: Add/ensure filters for vendor user, facility, project with mobile-friendly pagination.
- Facility link APIs: persist facility×vendor links; prevent cross-tenant and duplicate link cases.

#### New APIs (recommended)


| API                                       | Method | Purpose                                                       |
| ----------------------------------------- | ------ | ------------------------------------------------------------- |
| `/v1/field-plans/_generate`               | POST   | Generate all **facility × vendor** plans for a project        |
| `/v1/field-plans/{id}/roles/_assign`      | POST   | Assign Field Staff, Supervisor, Installation Reviewer + email |
| `/v1/field-plans/{id}/item-codes/_search` | POST   | Item codes/assets on plan (facility + vendor)                 |


---

### 12.7 `field-planner-activity` — context path `/activity(Phase 2)`

#### Existing APIs (reuse / modify)


| API                                                                   | Method | Livelihood                                         | Tag          |
| --------------------------------------------------------------------- | ------ | -------------------------------------------------- | ------------ |
| `/v1/activities/_create`                                              | POST   | Activity per install step; **item-code form type** | **Modify**   |
| `/v1/activities/_update`                                              | POST   | QC data (solar vs non-solar)                       | **Modify**   |
| `/v1/activities/_search`                                              | POST   | Field Staff mobile list                            | **Modify**   |
| `/v1/activities/_delete`                                              | POST   | Delete activity                                    | **Reuse**    |
| `/v1/activities/_assign-activity`                                     | POST   | Assign activity                                    | **Modify**   |
| `/v1/activities/assignment/_update`, `/_search`                       | POST   | Assignment maintenance                             | **Modify**   |
| `/v1/activities/_unassign-activity`                                   | POST   | Unassign                                           | **Reuse**    |
| `/v1/activities/_assign-staff`                                        | POST   | Assign vendor field staff                          | **Modify**   |
| `/v1/activities/staff/v1/_create`, `_update`, `_delete`               | POST   | Staff on activity                                  | **Modify**   |
| `/v1/activities/workflow/update`, `bulk/workflow/update`              | POST   | Submit → Supervisor → Reviewer                     | **Modify**   |
| `/v1/bom/_create`, `_update`, `_search`, `_generate_pdf`, `_save_pdf` | POST   | BOM / PDF                                          | **Optional** |


**Modify notes (brief):**

- **Item-code driven QC:** Payload and validations differ for solar vs non-solar; drive form selection via item code master.
- Workflow chain remains: submit → supervisor → reviewer; update role checks and transitions for vendor/SELCO actors.

#### New APIs (recommended)


| API                                    | Method | Purpose                                          |
| -------------------------------------- | ------ | ------------------------------------------------ |
| `/v1/activities/_submit-qc`            | POST   | Submit QC for an **item code** at facility       |
| `/v1/acknowledgment/_send-otp`         | POST   | OTP to **facility manager**                      |
| `/v1/acknowledgment/_verify`           | POST   | Verify OTP; finalize acknowledgment              |
| `/v1/acknowledgment/_generate-letter`  | POST   | Acknowledgment PDF → filestore                   |
| `/v1/handover/_generate-letter`        | POST   | Handover PDF                                     |
| `/v1/installation/_notify-procurement` | POST   | Email procurement on install + handover complete |


---

### 12.8 `im-services` — context path `/im-services`

#### Existing APIs (reuse / modify)


| API                                        | Method | Livelihood                                                                                       | Tag                    |
| ------------------------------------------ | ------ | ------------------------------------------------------------------------------------------------ | ---------------------- |
| `/v2/request/_create`                      | POST   | `**facilityId` + `assetId`**; **auto-assign vendor** from asset                                  | **Modify**             |
| `/v2/request/_update`                      | POST   | All workflow actions: resolve, **OOS**, **OOW** (+ quotation doc), **REOPEN** (72h), POC actions | **Modify**             |
| `/v2/request/_search`                      | POST   | Manager: own facility; POC: **state** filter                                                     | **Modify**             |
| `/v2/request/_count`                       | POST   | Count with same filters                                                                          | **Modify**             |
| `/v2/request/_plainsearch`                 | POST   | Plain search                                                                                     | **Modify**             |
| `/v2/request/_update-boundary-by-facility` | POST   | Sync boundary from facility                                                                      | **Reuse** / **Modify** |
| `/v2/video/`*                              | POST   | Video upload                                                                                     | **Optional**           |
| `/user/login/_report`                      | POST   | Login audit                                                                                      | **Reuse**              |
| `/v2/theft-notification`                   | POST   | E4H theft SMS cron                                                                               | **N/A**                |
| `/migration/`*, `/mock/`*                  | POST   | Ops / test only                                                                                  | **N/A**                |


**Modify notes (brief):**

- `**/v2/request/_create`**: Require `facilityId` + `assetId`; validate `asset.facilityId == facilityId`; auto-assign vendor from asset; start workflow on `LivelihoodIncident`. **Single create endpoint** (no `/_create-on-behalf`): **facility manager** self-serve when `requestInfo.userInfo` has `COMPLAINANT` and jurisdiction matches `facilityId`; **Program POC** raise-on-behalf on the **same** path with `createdOnBehalf: true`, `entryChannel: POC_MANUAL` (or `IVR_WHATSAPP`), role `LIVELIHOOD_POC`, complainant/reporter resolved from facility’s `COMPLAINANT` HRMS user (not the POC’s uuid), and “raised on your behalf” SMS to the facility manager.
- `**/v2/request/_update`**: Single transition API (E4H `requestsUpdatePost` → workflow). **Out-of-warranty quotation:** vendor uploads file to **egov-filestore**, then `_update` with `workflow.action: OUT_OF_WARRANTY` and `verificationDocuments` (`documentType: QUOTATION`, `fileStoreId`) — no `/quotation/_upload`. **Reopen:** facility manager `_update` with `workflow.action: REOPEN` within 72h of `RESOLVED` — no `/reopen` route. Also: resolve, OOS, decline, POC reassign/decline; validations per action.
- `**/v2/request/_search` / `_count` / `_plainsearch`**: Add role-based filters (manager=single facility, vendor=assigned, POC=state scope) and support new Livelihood statuses.
- `**/v2/request/_update-boundary-by-facility`**: Keep incidents in sync when facility boundary changes (search/escalation correctness).

#### New APIs (recommended)

**None** on `im-services` for Livelihood workflow actions — reuse `**POST /v2/request/_update`** only (same as E4H).

---

### 12.9 `egov-workflow-v2` — context path `/egov-workflow-v2/egov-wf`

#### Existing APIs (reuse / modify)


| API                                                   | Method | Livelihood                                        | Tag       |
| ----------------------------------------------------- | ------ | ------------------------------------------------- | --------- |
| `/process/_transition`                                | POST   | Livelihood actions on new business service        | **Reuse** |
| `/process/_search`                                    | POST   | Process history                                   | **Reuse** |
| `/process/_count`, `_statuscount`, `_nearingslacount` | POST   | Dashboard metrics                                 | **Reuse** |
| `/businessservice/_search`                            | POST   | Load `LivelihoodIncident` (or similar) definition | **Reuse** |
| `/businessservice/_create`, `_update`                 | POST   | Define / update workflow                          | **Reuse** |
| `/auto/{businessService}/_escalate`                   | POST   | SLA cron (7d / 3d / 14d / 72h close)              | **Reuse** |
| `/escalate/_search`                                   | POST   | Escalated queue for POC                           | **Reuse** |
| `/migration/_update`, `v2/migration/_update`          | POST   | Data migration                                    | **N/A**   |


**Modify notes (brief):**

- Add/configure business service `LivelihoodIncident` (states/actions/roles).
- `/auto/{businessService}/_escalate`: add AutoEscalation rules for Livelihood SLA timers (7d/3d/14d/72h) and publish to Kafka as per existing pattern.
- Count/statuscount/nearingslacount: include Livelihood statuses/state groupings for dashboards.

#### New APIs

Prefer **new MDMS `Workflow.AutoEscalation` rows** and a new business service name rather than new workflow routes.

---

### 12.10 `egov-mdms-service-v2` — context path `/mdms-v2`

#### Existing APIs (reuse / modify)


| API                                       | Method | Livelihood                            | Tag                    |
| ----------------------------------------- | ------ | ------------------------------------- | ---------------------- |
| `/v1/_search`                             | POST   | Masters (issue types, SLA, templates) | **Reuse** / **Modify** |
| `/v2/_create`                             | POST   | Schema-based master create            | **Modify**             |
| `/v2/_search`                             | POST   | Search Livelihood masters             | **Modify**             |
| `/v2/_update/{schemaCode}`                | POST   | Update master data                    | **Modify**             |
| `schema/v1/_create`, `_search`, `_update` | POST   | Schema definitions                    | **Modify**             |


**Modify notes (brief):**

- Add Livelihood MDMS data modules: item codes, issue types per asset type, SLA matrix, templates, `Workflow.AutoEscalation` rows.
- Schema and v2 CRUD endpoints: add schema codes/namespaces for Livelihood; role-gate who can update.

#### New (typically data modules, not new routes)

- `Livelihood.ItemCode`, issue types per asset type, letter templates, `Workflow.AutoEscalation` for Livelihood incident service.

---

### 12.11 `boundary-service` — context path `/boundary-service`

#### Existing APIs (reuse / modify)


| API                                                                              | Method | Livelihood              | Tag       |
| -------------------------------------------------------------------------------- | ------ | ----------------------- | --------- |
| `/boundary/_create`, `_search`, `v2/_search`, `_update`, `_delete`               | POST   | Geography entities      | **Reuse** |
| `/boundary-relationships/_create`, `_search`, `v2/_search`, `_update`, `_delete` | POST   | Hierarchy relationships | **Reuse** |
| `/boundary-hierarchy-definition/_create`, `_search`                              | POST   | Hierarchy definitions   | **Reuse** |


#### New APIs

Usually **none** — reuse existing + ingestion.

---

### 12.12 `egov-hrms` — context path `/egov-hrms`

#### Existing APIs (reuse / modify)


| API                  | Method | Livelihood                     | Tag                    |
| -------------------- | ------ | ------------------------------ | ---------------------- |
| `/employees/_create` | POST   | Vendor staff, POC, reviewers   | **Reuse** / **Modify** |
| `/employees/_update` | POST   | Update employee                | **Reuse**              |
| `/employees/_search` | POST   | Jurisdiction; post-login (web) | **Modify**             |
| `/employees/_count`  | POST   | Count                          | **Reuse**              |


**Modify notes (brief):**

- `/employees/_create`: facility manager as HRMS employee with role `COMPLAINANT` and jurisdiction on facility `boundaryCode` (Option 1), plus vendor/POC/reviewer creation as-is.
- `/employees/_search`: support filtering by `roles=COMPLAINANT` + phone/jurisdiction boundary (used in enrichment/provisioning and validation).

#### New APIs

**Reuse** — facility manager provisioned via `**/employees/_create`** (same as health facility POC): `COMPLAINANT` role, jurisdiction on facility boundary, mobile on user record.

---

### 12.13 `ingestion-service` — prefix `/ingestion-service`

#### Existing APIs (reuse / modify)


| API                                                                   | Method | Livelihood                            | Tag                                                                                    |
| --------------------------------------------------------------------- | ------ | ------------------------------------- | -------------------------------------------------------------------------------------- |
| `POST /ingestion-service/ingest/vendors`                              | POST   | Vendor Excel                          | **Reuse**                                                                              |
| `POST /ingestion-service/ingest/boundaries`                           | POST   | Boundary Excel                        | **Reuse**                                                                              |
| `POST /ingestion-service/ingest/facilities`                           | POST   | Facility Excel                        | **Modify** (facility + **manager** columns on same template; HRMS provision on create) |
| `POST /ingestion-service/ingest/createFacilityAndUpdateProject`       | POST   | Validate + link facilities to project | **Modify** (manager fields in facility sheet)                                          |
| `POST /ingestion-service/ingest/addFacilitiesValidateData`            | POST   | Validate facilities                   | **Modify**                                                                             |
| `POST /ingestion-service/template/facilityIngestionTemplate`*         | POST   | Facility templates                    | **Modify**                                                                             |
| `POST /ingestion-service/template/boundaryIngestionTemplate`          | POST   | Boundary template                     | **Reuse**                                                                              |
| `POST /ingestion-service/template/fieldplanFacilityIngestionTemplate` | POST   | Field plan template                   | **Modify**                                                                             |
| AMC / workstream / `facilityQRGeneration` (E4H)                       | POST   | E4H-specific flows                    | **N/A** / **Modify**                                                                   |


**Modify notes (brief):**

- **Facility ingestion** (`ingest/facilities`, `addFacilitiesValidateData`, `createFacilityAndUpdateProject`, facility templates): extend MDMS **FacilityIngestionSchema** with manager name/mobile (and Livelihood fields). **No** separate `ingest/facility-managers` route — manager is loaded **with** each facility row; on facility create/ONM-ready, trigger existing HRMS `COMPLAINANT` provision (Option 1).
- Field plan ingestion/template: change unit to facility×vendor and map item codes/assets accordingly.

#### New APIs (recommended)


| API                                                    | Method | Purpose                                       |
| ------------------------------------------------------ | ------ | --------------------------------------------- |
| `POST /ingestion-service/ingest/assets`                | POST   | Bulk assets (`facilityId`, vendor, item code) |
| `POST /ingestion-service/ingest/item-codes`            | POST   | Item code ↔ vendor master                     |
| `POST /ingestion-service/ingest/justification-mapping` | POST   | Justification code → facilities               |


---

### 12.14 Supporting core services


| Service                   | Context path      | Reuse APIs                                           | New for Livelihood                          |
| ------------------------- | ----------------- | ---------------------------------------------------- | ------------------------------------------- |
| **egov-idgen**            | `/egov-idgen`     | `POST /id/_generate`                                 | **Reuse** (new id names in config)          |
| **egov-filestore**        | `/filestore`      | `POST /v1/files`, `GET /v1/files/url`, `/id`, `/tag` | **Reuse** (photos, quotations, letters)     |
| **egov-notification-sms** | (deploy-specific) | SMS via configured topics                            | **Modify** (Livelihood notification matrix) |
| **inbox** (optional)      | `/inbox`          | `POST /v2/_search`                                   | **Modify** (Livelihood module config)       |


---

### 12.15 Services not in Livelihood phase 1


| Service                   | Representative APIs                         | Tag                           |
| ------------------------- | ------------------------------------------- | ----------------------------- |
| **rms-service**           | Rule engine, telemetry → create IM ticket   | **N/A**                       |
| **amc-scheduler-service** | `/asset-amc/v1/configuration/`*, `/visit/`* | **N/A**                       |
| **im-services-analytics** | `/v1/escalation-emails/daily`, `/weekly`    | **Optional** (reporting only) |
| **processor-services**    | Kafka video consumer                        | **Optional**                  |


---

---

Note: 1. the 'health-facility-registry' should be renamed as 'facility-registry'

```
       2. Modify tags in the APIs need not specifically mean and overhaul of the API, a slight adaptation in the api code may do just fine.
```

