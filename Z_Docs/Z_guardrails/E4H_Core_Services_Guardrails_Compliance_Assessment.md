# E4H Core Services — Design Guardrails Compliance Assessment

**Reference document:** `C2 Selco_E4H_Design_Guardrails_v1.txt` (v1.0, June 2026)  
**Related:** [E4H_Design_Guardrails_Compliance_Assessment.md](./E4H_Design_Guardrails_Compliance_Assessment.md) (e4h-services, frontend, mobile, migrations)  
**Codebase path:** `backend/core-services/`  
**Assessment date:** June 26, 2026  
**Scope:** Seven core services (source only; `target/` excluded)

---

## Compliance Legend

| Rating | Meaning |
|--------|---------|
| **To a Great Extent** | Guardrail implemented consistently; gaps minor or absent |
| **Partially** | Pattern incomplete, inconsistent, or mixed with anti-patterns |
| **Not Followed at All** | Guardrail absent, contradicted, or not applicable with no compensating pattern |

**Improvement scope:** Each service was compared to upstream DIGIT. Gaps are labelled by origin to clarify **what can be improved in the current E4H adaptation** vs what **already existed in DIGIT upstream**:

| Scope label | Meaning |
|-------------|---------|
| **Pre-existing in DIGIT** | Gap was present in upstream DIGIT before E4H changes. Improvement is optional unless a platform-wide uplift is planned. |
| **Introduced in E4H adaptation** | Gap was introduced or worsened during SELCO/E4H changes. In scope for adaptation improvement work. |
| **Full adaptation scope** | Service was heavily rewritten for E4H — most gaps fall within the adaptation improvement backlog. |
| **No new gaps from adaptation** | E4H changes did not add guardrail violations; any remaining gaps are pre-existing in DIGIT. |

**P/NF rows:** Each gap includes a **How to improve** action.

## How to Read This Document

1. **2 of 7 services** have the most adaptation-scope improvements (boundary-service, health-facility-registry) — the rest are largely pre-existing DIGIT patterns or already aligned.
2. Per-service sections show **gaps first**; compliant items are collapsed.

---

## Improvement Summary — Core Services

### The bottom line

| Metric | Value |
|--------|-------|
| **Services with adaptation-scope improvements** | **2 of 7** (boundary-service, health-facility-registry) |
| **Services with no new gaps from adaptation** | **5 of 7** (filestore, idgen, mdms-v2, notification-sms, workflow-v2) |
| **Highest-impact single fix** | Restore Kafka persister in boundary-service |

### Improvement summary by service

| Service | Adaptation scope | GE | P | NF | Gaps from E4H adaptation |
|---------|------------------|----|----|-----|------------------------|
| **boundary-service** | **Adaptation fix — P0** | 13 | 4 | 1 | G2, G8, G13 (persister write path changed) |
| **health-facility-registry** | **Full adaptation scope — P1** | 5 | 10 | 3 | G1, G2, G5, G8–G11, G14, G17, G18 |
| **egov-workflow-v2** | No new gaps | 9 | 7 | 2 | None — IM extensions only |
| **egov-mdms-service-v2** | No new gaps | 6 | 8 | 4 | None — +1 migration is improvement |
| **egov-notification-sms** | No new gaps | 1 | 4 | 11 | None — G13 improved vs upstream |
| **egov-filestore** | Pre-existing in DIGIT | 1 | 4 | 11 | None — structural gaps pre-date E4H |
| **egov-idgen** | Pre-existing in DIGIT | 0 | 6 | 10 | None — unchanged from upstream |

### What to fix first

| Order | Service | What | Why |
|-------|---------|------|-----|
| 1 | boundary-service | Restore `producer.push()` for create/delete | Write path changed during adaptation — upstream used Kafka persister |
| 2 | health-facility-registry | Enable workflow v2 | Workflow disabled (`is.workflow.enabled=false`) |
| 3 | health-facility-registry | Add enricher + standardize audit fields | Missing layer; non-standard `created_at` names |
| 4 | health-facility-registry | Remove direct JDBC for boundary_code | Mixed write path breaks persister pattern |
| 5 | health-facility-registry | ErrorConstants + QueryBuilder cleanup | Inline SQL and ad-hoc errors |

---

## Services in Scope

| # | Service | Path | Upstream DIGIT source |
|---|---------|------|------------------------|
| 1 | boundary-service | `backend/core-services/boundary-service` | `egovernments/Digit-Core` → `core-services/boundary-service` |
| 2 | egov-filestore | `backend/core-services/egov-filestore` | `egovernments/core-services` → `egov-filestore` |
| 3 | egov-idgen | `backend/core-services/egov-idgen` | `egovernments/core-services` → `egov-idgen` |
| 4 | egov-mdms-service-v2 | `backend/core-services/egov-mdms-service-v2` | `egovernments/Digit-Core` → `core-services/mdms-v2` |
| 5 | egov-notification-sms | `backend/core-services/egov-notification-sms` | `egovernments/core-services` → `egov-notification-sms` |
| 6 | egov-workflow-v2 | `backend/core-services/egov-workflow-v2` | `egovernments/core-services` → `egov-workflow-v2` |
| 7 | health-facility-registry | `backend/core-services/health-facility-registry` | `egovernments/health-campaign-services` → `health-services/facility` |

---

## Guardrail Reference (G1–G18)

| ID | Guardrail | Source |
|----|-----------|--------|
| G1 | Layered architecture (Controller → Service → Validator → Enricher → Repository → QueryBuilder) | §18 |
| G2 | Kafka persister writes (no direct DB writes from service layer) | §5.5, §22.3 |
| G3 | Flyway versioned migrations | §5.1 |
| G4 | MDMS / config-over-code | §3, §5.2 |
| G5 | Workflow v2 integration | §3, §5.3 |
| G6 | OpenAPI specification | §3, §5.4, §16 |
| G7 | Elasticsearch / indexer read models | §6, §12.1 |
| G8 | QueryBuilder pattern (no inline SQL) | §22 |
| G9 | Structured errors (paired code + message) | §19 |
| G10 | Audit fields in DDL and enricher | §5.1, §21.2 |
| G11 | UUID primary keys | §5.1 |
| G12 | Configuration externalization | §24 |
| G13 | Kafka producer pattern | §23 |
| G14 | RBAC / ABAC | §3, §9 |
| G15 | PII field-level encryption | §9 |
| G16 | Observability (TracerConfiguration) | §11 |
| G17 | Naming conventions | §17 |
| G18 | Postman / API documentation | §27 |

---

## Per-Service Assessment

*Services 2–6 (filestore, idgen, mdms-v2, notification-sms, workflow-v2) have **no new guardrail gaps from E4H adaptation** — see brief notes below.*

---

### 1. boundary-service

**Path:** `backend/core-services/boundary-service`  
**Role:** Boundary, hierarchy, and relationship master data  
**Overall alignment:** Medium-High  
**Improvement scope:** **Introduced in E4H adaptation** — write path changed from upstream Kafka persister to direct JDBC


> **Compliance snapshot:** 8/18 fully met · **3 gaps from E4H adaptation** (G2, G8, G13)

#### Improvements from E4H adaptation (3 items)

| Guardrail | Rating | Scope | Issue | How to improve |
|-----------|--------|-------|-------|----------------|
| **G2** Persister writes | **P → NF** | Introduced in adaptation | JDBC INSERT/DELETE in `BoundaryRepositoryImpl` (upstream used Kafka) | Restore `producer.push(persisterTopic, request)` in `create()`; route deletes through persister |
| **G8** QueryBuilder | **P** | Introduced in adaptation | Inline SQL added in E4H `create()` path | Remove INSERT SQL from repository; writes go via persister only |
| **G13** Kafka producer | **GE*** | Introduced in adaptation | *Broken for entity create* — hierarchy/relationship still use Kafka | Same fix as G2 — restore producer for boundary entity create/delete |

#### Pre-existing in DIGIT upstream (outside adaptation scope)

<details>
<summary>Pre-existing in DIGIT (7 items — click to expand)</summary>

| Guardrail | Rating | Scope | Notes |
|-----------|--------|-------|-------|
| G4 MDMS | P | Pre-existing in DIGIT | E4H added MDMSUtils — minor adaptation addition |
| G5 Workflow v2 | NF | Pre-existing in DIGIT | Not in upstream boundary-service |
| G6 OpenAPI | P | Pre-existing in DIGIT | Same as upstream |
| G7 Elasticsearch | NF | Pre-existing in DIGIT | Same as upstream |
| G11 UUID PKs | P | Pre-existing in DIGIT | Same as upstream |
| G14 RBAC/ABAC | NF | Pre-existing in DIGIT | Same as upstream |
| G15 PII encryption | NF | Pre-existing in DIGIT | Same as upstream |
| G17 Naming | P | Pre-existing in DIGIT | Same as upstream |

</details>

<details>
<summary>✓ Compliant items (8 — click to expand)</summary>

| Guardrail | Notes |
|-----------|-------|
| G1 Layered architecture | Full stack preserved |
| G3 Flyway | Identical 3 migrations to upstream |
| G9 Structured errors | ErrorCodes in place |
| G10 Audit fields | Standard audit columns |
| G12 Config external | Externalised |
| G16 Observability | TracerConfiguration present |
| G18 API docs | Postman with SELCO examples |

</details>


**Changes during E4H adaptation:** `BoundaryRepositoryImpl` JDBC create/delete, `BoundaryService.deleteBoundary()`, `getAllBoundaries`, `MDMSUtils`, `FlatBoundaryResponse` models.

**Recommended improvement:** Restore upstream `producer.push()` for create; persister-based delete.

---

### 2. egov-filestore

**Path:** `backend/core-services/egov-filestore`  
**Role:** File and blob storage (legacy DIGIT)  
**Overall alignment:** Low  
**Improvement scope:** **Pre-existing in DIGIT** — no new gaps from E4H adaptation

**Changes during E4H adaptation:** `HLSStorageService`, `CloudFileManagerV2`, `MinioRepositoryV2`; `fixed.bucketname=selco-dev`.

**Pre-existing in DIGIT upstream:** JPA-direct writes, no QueryBuilder, no OpenAPI, `bigserial` PKs, no Kafka persister, no MDMS.

**Conclusion:** Improvement scope is limited to optional platform-wide uplift; adaptation did not introduce new violations.

---

### 3. egov-idgen

**Path:** `backend/core-services/egov-idgen`  
**Role:** ID and sequence generation (legacy DIGIT)  
**Overall alignment:** Low  
**Improvement scope:** **Pre-existing in DIGIT** — essentially unchanged from upstream

**Pre-existing in DIGIT upstream:** Thin architecture, direct JDBC, no persister, no audit columns, `bigserial` PKs, no OpenAPI.

**Conclusion:** No new guardrail gaps from E4H adaptation.

---

### 4. egov-mdms-service-v2

**Path:** `backend/core-services/egov-mdms-service-v2`  
**Role:** Master Data Management Service v2  
**Overall alignment:** Medium-High  
**Improvement scope:** **No new gaps from adaptation**

**Changes during E4H adaptation:** `V20250403133900__add_sequence_mdms_data.sql` (UUID default) — improvement, not a new violation. Minor controller/repository tweaks.

**Pre-existing in DIGIT upstream:** Tracer not wired in source, no RBAC, no standalone OpenAPI — same as upstream.

**Conclusion:** E4H adaptation did not introduce guardrail violations.

---

### 5. egov-notification-sms

**Path:** `backend/core-services/egov-notification-sms`  
**Role:** SMS notification gateway (stateless)  
**Overall alignment:** Low (many guardrails N/A — no database)  
**Improvement scope:** **No new gaps from adaptation**

**Changes during E4H adaptation:** SMS bounce callback (`CallbackAPI`, `ReportListener`); `sms.senderid=SELCOF`. Kafka producer (G13) **improved** vs upstream.

**Pre-existing in DIGIT upstream:** No DB, no Flyway, no RBAC — by design in DIGIT.

**Note:** Only explicit DLQ in platform: `notification-sms-deadletter` in application.properties.

---

### 6. egov-workflow-v2

**Path:** `backend/core-services/egov-workflow-v2`  
**Role:** Platform workflow engine  
**Overall alignment:** High  
**Improvement scope:** **No new gaps from adaptation** — domain extensions only

**Changes during E4H adaptation (+3 migrations):** `isActive`, `triggerparallelworkflows`, soft-delete CLOSED-after-REJECT fix; `ImServiceClient`, `IMEscalation*` models, `ElasticSearchClient`, IM indexer topics.

**Pre-existing in DIGIT upstream:** Ad-hoc `CustomException` (no central ErrorCodes), CASCADE in legacy DDL, no PII encryption — pre-existing DIGIT patterns.

**Conclusion:** Core engine aligned with upstream. IM integration is domain extension work, not a new guardrail gap from adaptation.

---

### 7. health-facility-registry

**Path:** `backend/core-services/health-facility-registry`  
**Upstream ancestor:** HCM `health-services/facility` (`org.egov.facility`)  
**Role:** Health facility registration and management  
**Overall alignment:** Medium  
**Improvement scope:** **Full adaptation scope** — heavily rewritten for SELCO/E4H


> **Compliance snapshot:** 4/18 fully met · **14 gaps in adaptation scope**

#### Priority 1 — Architecture

| Guardrail | Rating | Scope | Issue | How to improve |
|-----------|--------|-------|-------|----------------|
| **G5** Workflow v2 | **NF** | Full adaptation scope | `is.workflow.enabled=false` | Enable workflow v2; wire WorkflowService on create/approve |
| **G2** Persister writes | **P** | Full adaptation scope | Direct JDBC for `boundary_code` update | Route through persister topic in `facility-persister.yml` |
| **G1** Layered architecture | **P** | Full adaptation scope | No enricher layer | Add `FacilityEnrichment.java`; move validators to `validator/` package |
| **G8** QueryBuilder | **P** | Full adaptation scope | Inline SQL in `FacilityService` | Extract to `FacilityQueryBuilder` |

#### Priority 2 — Standards alignment

| Guardrail | Rating | Scope | Issue | How to improve |
|-----------|--------|-------|-------|----------------|
| **G10** Audit fields | **P** | Full adaptation scope | `created_at`/`updated_at` non-standard | Migration to `createdBy`/`createdTime`/`lastModified*` |
| **G9** Structured errors | **P** | Full adaptation scope | Ad-hoc throws mixed with constants | Create `ErrorConstants.java` with paired codes |
| **G17** Naming | **P** | Full adaptation scope | Package deviates from `org.egov.facility` | Align package and audit field naming |
| **G18** API docs | **NF** | Full adaptation scope | No Postman in service | Add Postman + move OpenAPI to `docs/` |

#### Priority 3 — Hardening

| Guardrail | Rating | Scope | Issue | How to improve |
|-----------|--------|-------|-------|----------------|
| **G6** OpenAPI | **P** | Full adaptation scope | Spec at repo root only | Publish under `docs/health-facility-registry/` |
| **G7** Elasticsearch | **P** | Full adaptation scope | Indexer present; no reconciliation doc | Add ES reconciliation runbook |
| **G11** UUID PKs | **P** | Full adaptation scope | VARCHAR PK | UUID migration for `facility.id` |
| **G12** Config external | **P** | Full adaptation scope | Hardcoded role lists | Move role lists to MDMS |
| **G14** RBAC/ABAC | **P** | Full adaptation scope | Ad-hoc role gates only | Document API-to-role map |
| **G15** PII encryption | **P** | Full adaptation scope | Partial POC encryption | Extend to all POC contact fields |

<details>
<summary>✓ Compliant items (4 — click to expand)</summary>

| Guardrail | Status | Notes |
|-----------|--------|-------|
| **G3** Flyway | GE | 5 new E4H migrations (2025–2026) |
| **G4** MDMS | GE | Minor hardcoded status strings |
| **G13** Kafka producer | GE | `facility-persister.yml` preserved |
| **G16** Observability | GE | Preserved |

</details>


**Schema changes during adaptation:** `facility_category`, `is_onm_ready`, POC fields, `hfr_id`, `nin_id`, `boundary_code` indexes.

**Recommended improvements:** Enable workflow v2; add enricher layer; route `boundary_code` through persister; standardize audit field names (`createdBy`/`createdTime`).

---

## Database Migrations (core-services)

Migration guardrails: MG1 Flyway · MG2 Additive only · MG3 Idempotent · MG4 Audit fields · MG5 No CASCADE · MG6 UUID PKs · MG7 ES migrations · MG8 Reconciliation docs · MG9 Rollback docs · MG10 Java transforms

### boundary-service

| Guardrail | Rating | Scope | Notes |
|-----------|--------|-------|-------|
| MG1 Flyway | GE | Pre-existing in DIGIT | 3 migrations identical to Digit-Core upstream |
| MG2 Additive only | GE | Pre-existing in DIGIT | No destructive alters |
| MG4 Audit fields | GE | Pre-existing in DIGIT | Standard audit columns in DDL |
| MG3 Idempotent | NF | Pre-existing in DIGIT | No `IF NOT EXISTS` |
| MG6 UUID PKs | NF | Pre-existing in DIGIT | VARCHAR PKs |

**Improvement scope:** No new migration gaps from adaptation — migrations unchanged from upstream.

---

### egov-filestore, egov-idgen

| Service | Scope | Notes |
|---------|-------|-------|
| egov-filestore | Pre-existing in DIGIT | 4 migrations unchanged; JPA-era DDL |
| egov-idgen | Pre-existing in DIGIT | 58 migrations unchanged; `bigserial` PKs, no audit columns |

**Improvement scope:** Pre-existing in DIGIT — no new migration gaps from adaptation.

---

### egov-mdms-service-v2

| Guardrail | Rating | Scope | Notes |
|-----------|--------|-------|-------|
| MG1 Flyway | GE | Adaptation addition | 3 base + 1 E4H migration (`V20250403133900` UUID default) |
| MG2 Additive only | GE | Adaptation addition | E4H migration is additive |
| MG4 Audit fields | GE | Pre-existing in DIGIT | |
| MG6 UUID PKs | P | Adaptation improvement | E4H migration adds DB-level UUID default |

**Improvement scope:** Adaptation migration is compliant; no new violations.

---

### egov-workflow-v2

| Guardrail | Rating | Scope | Notes |
|-----------|--------|-------|-------|
| MG1 Flyway | GE | Adaptation addition | 11 upstream + 3 E4H migrations (2025) |
| MG2 Additive only | P | Pre-existing in DIGIT | Some renames in legacy DDL |
| MG5 Soft delete | P | Pre-existing in DIGIT | CASCADE in create DDL; E4H added soft-delete data fix |
| MG4 Audit fields | GE | Pre-existing in DIGIT | |

**Improvement scope:** +3 E4H migrations are additive; legacy CASCADE is pre-existing in DIGIT.

---

### health-facility-registry

| Guardrail | Rating | Scope | Notes |
|-----------|--------|-------|-------|
| MG1 Flyway | GE | Full adaptation scope | All 5 migrations from E4H adaptation (Aug 2025 – May 2026) |
| MG2 Additive only | GE | Full adaptation scope | Additive alters for new columns |
| MG4 Audit fields | P | Full adaptation scope | Uses `created_at`/`updated_at` not platform standard |
| MG5 Soft delete | GE | Full adaptation scope | `is_active`; FK uses `ON DELETE SET NULL` |
| MG6 UUID PKs | NF | Full adaptation scope | VARCHAR PK on `facility.id` |
| MG8–MG9 | NF | Full adaptation scope | No reconciliation or rollback docs |

**Improvement scope:** Full adaptation scope — audit field naming should align with platform standard.

---

## Platform Findings (core-services only)

**Strengths**
- `egov-workflow-v2` and `egov-mdms-service-v2` are well-aligned with DIGIT upstream
- `boundary-service` has strong layering and error handling (adaptation write-path change is isolated)
- E4H adaptation improved notification SMS (Kafka producer) and workflow (ES client)

**Highest adaptation-scope improvements**
1. **boundary-service** — restore persister write path changed during adaptation
2. **health-facility-registry** — close architectural gaps from full service rewrite

**Pre-existing in DIGIT (optional platform uplift)**
- `egov-filestore`, `egov-idgen` — legacy infra patterns
- RBAC, UUID PKs, standalone OpenAPI on services where upstream had same gaps

---

## Methodology

1. Read `C2 Selco_E4H_Design_Guardrails_v1.txt` and mapped G1–G18 and MG1–MG10 criteria.
2. Compared each service against upstream DIGIT (`egovernments/core-services`, `egovernments/Digit-Core`, `egovernments/health-campaign-services`).
3. Classified each gap by **improvement scope**: *Pre-existing in DIGIT*, *Introduced in E4H adaptation*, or *Full adaptation scope* (health-facility rewrite).
4. Excluded `target/` compiled output.

---

*Generated from codebase analysis against Selco E4H Design Guardrails v1.0.*
