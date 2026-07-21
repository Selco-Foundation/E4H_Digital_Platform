# E4H Design Guardrails — Compliance Assessment

**Reference document:** `C2 Selco_E4H_Design_Guardrails_v1.txt` (v1.0, June 2026)  
**Codebase assessed:** `/home/beehyv/Downloads/E4H_Digital_Platform-staging`  
**Assessment date:** June 26, 2026  
**Scope:** `backend/e4h-services/`, frontend (`frontend/`), mobile (`mobile/`), and e4h-service migrations.  
**Core services** (`backend/core-services/`) are assessed separately in [E4H_Core_Services_Guardrails_Compliance_Assessment.md](./E4H_Core_Services_Guardrails_Compliance_Assessment.md).  
Source only; `target/` excluded.

---

## Compliance Legend


| Rating                  | Meaning                                                                                     |
| ----------------------- | ------------------------------------------------------------------------------------------- |
| **To a Great Extent**   | Guardrail is implemented consistently in this service; gaps are minor or absent             |
| **Partially**           | Pattern exists but is incomplete, inconsistent, or mixed with anti-patterns                 |
| **Not Followed at All** | Guardrail is absent, contradicted, or not applicable by design with no compensating pattern |


**Abbreviations used in summary tables:** GE = To a Great Extent · P = Partially · NF = Not Followed at All · N/A = Not applicable for this service type

**P/NF rows:** Each gap includes a concrete **How to improve** action.

## How to Read This Document

1. **Start with the Executive Summary** below — it summarizes improvement gaps across backend, frontend, mobile, and migrations.
2. **Per-service sections** show **gaps only** up front; compliant items are collapsed under `<details>`.
3. **Core services** are in the [separate core-services doc](./E4H_Core_Services_Guardrails_Compliance_Assessment.md) — only 2 of 7 need E4H work.

### Document map


| Part                                                              | What you'll find                                       |
| ----------------------------------------------------------------- | ------------------------------------------------------ |
| [Executive Summary](#executive-summary--how-much-can-be-improved) | Platform-wide gaps, recurring fixes                    |
| [Part I — Backend](#part-i--backend-services-e4h-services)        | 12 e4h-services with gap tables + compliance snapshots |
| [Part II — Frontend](#part-ii--frontend-modules)                  | installation-ui + micro-ui modules                     |
| [Part III — Mobile](#part-iii--mobile-app)                        | Flutter app areas                                      |
| [Part IV — Migrations](#part-iv--database-migrations)             | Flyway / schema guardrails                             |


---

## Executive Summary — How Much Can Be Improved?

### Platform snapshot


| Area                                      | Items assessed              | Fully met | Gaps (P + NF) | Biggest lever                                           |
| ----------------------------------------- | --------------------------- | --------- | ------------- | ------------------------------------------------------- |
| **Backend** (12 e4h-services)             | 216 guardrail checks        | 93 (43%)  | 123 (57%)     | rms-service refactor; platform-wide OpenAPI + RBAC      |
| **Frontend** (installation-ui + micro-ui) | 12 guardrails × ~15 modules | ~30%      | ~70%          | Inbox v2 + Digit.Hooks in fa/pm/qc/amc/org              |
| **Mobile** (Flutter app)                  | 10 guardrails × 7 areas     | ~45%      | ~55%          | Accessibility (Semantics); MDMS-driven workflow filters |
| **Migrations** (e4h-services)             | 10 guardrails × 9 services  | ~35%      | ~65%          | UUID PKs, rollback docs, remove CASCADE                 |


### Backend — improvement summary by service


| Service                    | GE  | P   | NF  | Top priority fix                         |
| -------------------------- | --- | --- | --- | ---------------------------------------- |
| **project**                | 9   | 6   | 3   | Add RBAC map + ErrorConstants            |
| **field-planner-activity** | 10  | 3   | 5   | UUID PKs + ErrorConstants                |
| **field-planner**          | 9   | 4   | 5   | UUID PKs + dedicated WorkflowService     |
| **amc-scheduler-service**  | 10  | 3   | 5   | OpenAPI + RBAC + UUID PKs                |
| **im-services**            | 9   | 6   | 3   | ErrorConstants + remove JDBC exception   |
| **vendor-registry**        | 9   | 5   | 4   | Wire WorkflowUtil + ErrorConstants       |
| **egov-hrms**              | 8   | 5   | 5   | Workflow v2 + enricher + PII encryption  |
| **asset-registry**         | 6   | 6   | 6   | Enricher + QueryBuilder + workflow       |
| **inbox**                  | 6   | 4   | 8   | ErrorConstants + BFF architecture gaps   |
| **im-services-analytics**  | 4   | 7   | 7   | ErrorConstants + Flyway baseline         |
| **rms-service**            | 4   | 4   | 10  | Full layered refactor + persister writes |
| **processor-services**     | 3   | 1   | 14  | Worker architecture + Kafka event docs   |


### Recurring gaps


| Gap                             | Affects                  | Fix pattern                                       |
| ------------------------------- | ------------------------ | ------------------------------------------------- |
| No OpenAPI (G6)                 | 6 of 12 backend services | Generate from controllers; publish under docs/    |
| No RBAC (G14)                   | 8 of 12 backend services | Role-action map + Validator checks                |
| VARCHAR PKs (G11)               | 9 of 12 backend services | Platform-wide UUID migration programme            |
| No ErrorConstants (G9)          | 9 of 12 backend services | Single PR template; copy from asset-registry      |
| Custom tables not Inbox v2 (F6) | All E4H UI modules       | Copy workbench InboxSearchComposer setup          |
| Zero accessibility (M6)         | Entire mobile app        | Add Semantics wrapper utility; apply page by page |


---

---

## Services Assessed (e4h-services)


| #   | Service                | Path                                          | Type                            |
| --- | ---------------------- | --------------------------------------------- | ------------------------------- |
| 1   | amc-scheduler-service  | `backend/e4h-services/amc-scheduler-service`  | AMC contracts & visits          |
| 2   | asset-registry         | `backend/e4h-services/asset-registry`         | Asset lifecycle                 |
| 3   | egov-hrms              | `backend/e4h-services/egov-hrms`              | Employee/HRMS                   |
| 4   | field-planner          | `backend/e4h-services/field-planner`          | Field planning                  |
| 5   | field-planner-activity | `backend/e4h-services/field-planner-activity` | Field activity/BOM              |
| 6   | im-services            | `backend/e4h-services/im-services`            | Incident/ticket management      |
| 7   | im-services-analytics  | `backend/e4h-services/im-services-analytics`  | IM analytics & SLA              |
| 8   | inbox                  | `backend/e4h-services/inbox`                  | Cross-module inbox aggregator   |
| 9   | processor-services     | `backend/e4h-services/processor-services`     | Video/media processing worker   |
| 10  | project                | `backend/e4h-services/project`                | Project/beneficiary/tasks       |
| 11  | rms-service            | `backend/e4h-services/rms-service`            | RMS integration & orchestration |
| 12  | vendor-registry        | `backend/e4h-services/vendor-registry`        | Organisation/vendor registry    |


**Core services (7):** See [E4H_Core_Services_Guardrails_Compliance_Assessment.md](./E4H_Core_Services_Guardrails_Compliance_Assessment.md).

---

# Part I — Backend Services (e4h-services)

Backend guardrail reference (G1–G18  )

Each service is rated against these 18 backend guardrails:


| ID  | Guardrail                                                                                      | Source Sections |
| --- | ---------------------------------------------------------------------------------------------- | --------------- |
| G1  | Layered architecture (Controller → Service → Validator → Enricher → Repository → QueryBuilder) | §18             |
| G2  | Kafka persister writes (no direct DB writes from service layer)                                | §5.5, §22.3     |
| G3  | Flyway versioned migrations                                                                    | §5.1            |
| G4  | MDMS / config-over-code (no hardcoded business enums)                                          | §3, §5.2        |
| G5  | Workflow v2 integration                                                                        | §3, §5.3        |
| G6  | OpenAPI specification                                                                          | §3, §5.4, §16   |
| G7  | Elasticsearch / indexer read models                                                            | §6, §12.1       |
| G8  | QueryBuilder pattern (no inline SQL in repositories/services)                                  | §22             |
| G9  | Structured errors (paired code + message constants)                                            | §19             |
| G10 | Audit fields in DDL and enricher                                                               | §5.1, §21.2     |
| G11 | UUID primary keys                                                                              | §5.1            |
| G12 | Configuration externalization                                                                  | §24             |
| G13 | Kafka producer pattern                                                                         | §23             |
| G14 | RBAC / ABAC                                                                                    | §3, §9          |
| G15 | PII field-level encryption                                                                     | §9              |
| G16 | Observability (TracerConfiguration, structured logging)                                        | §11             |
| G17 | Naming conventions (`*Service`, `*Validator`, `*QueryBuilder`)                                 | §17             |
| G18 | Postman / API documentation artefacts                                                          | §27             |


---

## e4h-services Scorecard (at a glance)


| Service                    | GE  | P   | NF  | Alignment                                            | One-line summary |
| -------------------------- | --- | --- | --- | ---------------------------------------------------- | ---------------- |
| **project**                | 9   | 6   | 3   | Reference impl — gaps are RBAC, UUID, ErrorConstants |                  |
| **field-planner-activity** | 10  | 3   | 5   | Strongest workflow integration                       |                  |
| **field-planner**          | 9   | 4   | 5   | Needs dedicated WorkflowService                      |                  |
| **amc-scheduler-service**  | 10  | 3   | 5   | Missing OpenAPI + RBAC                               |                  |
| **im-services**            | 9   | 6   | 3   | Fix ErrorConstants + MDMS statuses                   |                  |
| **vendor-registry**        | 9   | 5   | 4   | Best PII encryption; wire workflow                   |                  |
| **egov-hrms**              | 8   | 5   | 5   | Add enricher + PII encryption                        |                  |
| **asset-registry**         | 6   | 6   | 6   | Needs Enricher + QueryBuilder                        |                  |
| **inbox**                  | 6   | 4   | 8   | BFF — many NF are N/A by design                      |                  |
| **im-services-analytics**  | 4   | 7   | 7   | Read-only analytics service                          |                  |
| **rms-service**            | 4   | 4   | 10  | **Critical refactor target**                         |                  |
| **processor-services**     | 3   | 1   | 14  | Worker — most NF are N/A                             |                  |


---

## Per-Service Detailed Assessment

---

### 1. amc-scheduler-service

**Path:** `backend/e4h-services/amc-scheduler-service`  
**Role:** AMC contracts and scheduled visits  
**Overall alignment:** High

> **Compliance snapshot:** 10/18 fully met · 3 partial · 5 not met

#### Gaps to address


| Guardrail                | Rating | Issue                                                                             | How to improve                                              |
| ------------------------ | ------ | --------------------------------------------------------------------------------- | ----------------------------------------------------------- |
| **G4** MDMS              | **P**  | `MDMSUtils.java` + hardcoded `AmcConstants.java`, `Constants.java`                | Move hardcoded constants to MDMS master data                |
| **G6** OpenAPI           | **NF** | No spec in service or `docs/`                                                     | Add OpenAPI spec under docs//; generate from controllers    |
| **G7** Elasticsearch     | **NF** | None                                                                              | N/A                                                         |
| **G9** Structured errors | **P**  | Inline `CustomException("MDMS_ERROR", ...)` in MDMSUtils; no ErrorConstants class | Create ErrorConstants.java with paired code + message       |
| **G11** UUID PKs         | **NF** | `id VARCHAR PRIMARY KEY` in DDL                                                   | Flyway migration: ALTER COLUMN id TYPE UUID                 |
| **G14** RBAC/ABAC        | **NF** | No role checks                                                                    | Add API-to-role map; enforce in Validator layer             |
| **G15** PII encryption   | **NF** | None                                                                              | N/A                                                         |
| **G18** API docs         | **P**  | Borrowed `Project Service.postman_collection 6.json` in resources                 | Add Postman collection with create/search/workflow examples |


✓ Compliant guardrails (10 items  )


| Guardrail                   | Status | Evidence                                                                              |
| --------------------------- | ------ | ------------------------------------------------------------------------------------- |
| **G1** Layered architecture | GE     | Full stack: controllers, services, validators, enrichers, repositories, querybuilders |
| **G2** Persister writes     | GE     | `AssetAmcRepository` extends `GenericRepository` + common `Producer`                  |
| **G3** Flyway               | GE     | `V20251114180100__amc_create_ddl.sql` (+ 3 migrations)                                |
| **G5** Workflow v2          | GE     | `VisitWorkflowService.java`                                                           |
| **G8** QueryBuilder         | GE     | `AssetAmcQueryBuilder`, `ScheduledVisitQueryBuilder`, `AmcConfigurationQueryBuilder`  |
| **G10** Audit fields        | GE     | DDL + `AssetAmcEnrichment.java`                                                       |
| **G12** Config external     | GE     | `AMCServiceConfiguration.java`                                                        |
| **G13** Kafka producer      | GE     | Common `Producer` via `GenericRepository`                                             |
| **G16** Observability       | GE     | `TracerConfiguration` in `MainConfiguration`                                          |
| **G17** Naming              | GE     | `org.egov.amc.`* follows conventions                                                  |


**Key gaps:** No OpenAPI; no RBAC; no UUID PKs.

---

### 2. asset-registry

**Path:** `backend/e4h-services/asset-registry`  
**Role:** Asset lifecycle management  
**Overall alignment:** Medium

> **Compliance snapshot:** 6/18 fully met · 6 partial · 6 not met

#### Gaps to address


| Guardrail                   | Rating | Issue                                                                                         | How to improve                                                    |
| --------------------------- | ------ | --------------------------------------------------------------------------------------------- | ----------------------------------------------------------------- |
| **G1** Layered architecture | **P**  | Has Controller/Service/Validator/Repository; **no Enricher** (audit inline in service); **... | Add Enricher + QueryBuilder layers (copy project service pattern) |
| **G2** Persister writes     | **P**  | Creates via Kafka (`AssetRepository` → `Producer`); reads via inline JDBC in `AssetService... | Route all writes through Kafka persister; remove direct JDBC      |
| **G4** MDMS                 | **P**  | `MdmsUtil.java`, `AssetValidator` MDMS validation; hardcoded `ServiceConstants.java`          | Move hardcoded constants to MDMS master data                      |
| **G5** Workflow v2          | **P**  | `WorkflowUtil.java` exists; `wf_status` column; workflow endpoint stub logs "not implement... | Enable workflow v2 and wire WorkflowService on create/update      |
| **G6** OpenAPI              | **P**  | `docs/asset-registry/asset-registry-1.0.0.yaml` at repo root                                  | Add OpenAPI spec under docs//; generate from controllers          |
| **G7** Elasticsearch        | **NF** | None                                                                                          | N/A                                                               |
| **G8** QueryBuilder         | **NF** | Inline SQL in `AssetService.searchAssets()` (`StringBuilder` query)                           | Extract inline SQL into dedicated QueryBuilder class              |
| **G10** Audit fields        | **P**  | DDL has audit columns; enrichment inline in `AssetService.createAsset()`                      | Move audit field population to dedicated Enricher                 |
| **G11** UUID PKs            | **NF** | `asset_id VARCHAR PRIMARY KEY`                                                                | Flyway migration: ALTER COLUMN id TYPE UUID                       |
| **G14** RBAC/ABAC           | **NF** | No role checks                                                                                | Add API-to-role map; enforce in Validator layer                   |
| **G15** PII encryption      | **NF** | None                                                                                          | N/A                                                               |
| **G18** API docs            | **NF** | No Postman in service                                                                         | Add Postman collection with create/search/workflow examples       |


✓ Compliant guardrails (6 items  )


| Guardrail                | Status | Evidence                                                  |
| ------------------------ | ------ | --------------------------------------------------------- |
| **G3** Flyway            | GE     | `V20250520141800__asset-service_ddl.sql` (+ 5 migrations) |
| **G9** Structured errors | GE     | Paired constants in `util/ErrorConstants.java`            |
| **G12** Config external  | GE     | `config/Configuration.java`                               |
| **G13** Kafka producer   | GE     | `kafka/Producer.java` wrapping `CustomKafkaTemplate`      |
| **G16** Observability    | GE     | `TracerConfiguration` in `MainConfiguration`              |
| **G17** Naming           | GE     | `org.egov.asset.`*; minor `V1ApiController` deviation     |


**Key gaps:** Inline SQL for search; no Enricher/QueryBuilder; workflow endpoint not implemented.

---

### 3. egov-hrms

**Path:** `backend/e4h-services/egov-hrms`  
**Role:** Employee and HRMS management  
**Overall alignment:** Medium

> **Compliance snapshot:** 8/18 fully met · 5 partial · 5 not met

#### Gaps to address


| Guardrail                   | Rating | Issue                                                                                         | How to improve                                                            |
| --------------------------- | ------ | --------------------------------------------------------------------------------------------- | ------------------------------------------------------------------------- |
| **G1** Layered architecture | **P**  | Controller/Service/Validator/Repository/QueryBuilder; **no Enricher** — `enrichCreateReque... | Add Enricher + QueryBuilder layers (copy project service pattern)         |
| **G4** MDMS                 | **P**  | `MDMSService.java` + hardcoded `HRMSConstants.java`                                           | Move hardcoded constants to MDMS master data                              |
| **G5** Workflow v2          | **NF** | No workflow integration                                                                       | Enable workflow v2 and wire WorkflowService on create/update              |
| **G6** OpenAPI              | **NF** | None                                                                                          | N/A                                                                       |
| **G7** Elasticsearch        | **NF** | None                                                                                          | N/A                                                                       |
| **G10** Audit fields        | **P**  | DDL audit columns; enrichment inline in service                                               | Move audit field population to dedicated Enricher                         |
| **G11** UUID PKs            | **P**  | Column named `uuid` but `CHARACTER VARYING(1024)`, not native UUID type                       | Flyway migration: ALTER COLUMN id TYPE UUID                               |
| **G14** RBAC/ABAC           | **P**  | Role validation in `EmployeeValidator` (`ERR_HRMS_INVALID_ROLE`)                              | Add API-to-role map; enforce in Validator layer                           |
| **G15** PII encryption      | **NF** | Employee PII stored without service-level encryption                                          | Encrypt PII fields via EncryptionDecryptionUtil (vendor-registry pattern) |
| **G18** API docs            | **NF** | None                                                                                          | N/A                                                                       |


✓ Compliant guardrails (8 items  )


| Guardrail                | Status | Evidence                                                                          |
| ------------------------ | ------ | --------------------------------------------------------------------------------- |
| **G2** Persister writes  | GE     | `EmployeeService` → `HRMSProducer.push()` on create/update; no direct JDBC writes |
| **G3** Flyway            | GE     | `V20190122152236__create_hrms_employee_table_ddl.sql` (+ 11 migrations)           |
| **G8** QueryBuilder      | GE     | `EmployeeQueryBuilder.java`                                                       |
| **G9** Structured errors | GE     | Paired code+msg in `utils/ErrorConstants.java`                                    |
| **G12** Config external  | GE     | `PropertiesManager.java`                                                          |
| **G13** Kafka producer   | GE     | `producer/HRMSProducer.java`                                                      |
| **G16** Observability    | GE     | `TracerConfiguration` in `EgovEmployeeApplication`                                |
| **G17** Naming           | GE     | `org.egov.hrms.`*                                                                 |


**Key gaps:** No workflow; no PII encryption for employee data; no OpenAPI.

---

### 4. field-planner

**Path:** `backend/e4h-services/field-planner`  
**Role:** Field planning and scheduling  
**Overall alignment:** High

> **Compliance snapshot:** 9/18 fully met · 4 partial · 5 not met

#### Gaps to address


| Guardrail                | Rating | Issue                                                         | How to improve                                               |
| ------------------------ | ------ | ------------------------------------------------------------- | ------------------------------------------------------------ |
| **G4** MDMS              | **P**  | `MDMSUtils.java` + `FieldPlannerConstants.java`               | Move hardcoded constants to MDMS master data                 |
| **G5** Workflow v2       | **P**  | Workflow URLs in config; no dedicated `WorkflowService` class | Enable workflow v2 and wire WorkflowService on create/update |
| **G6** OpenAPI           | **NF** | None                                                          | N/A                                                          |
| **G7** Elasticsearch     | **NF** | None                                                          | N/A                                                          |
| **G9** Structured errors | **P**  | Inline `CustomException` in MDMSUtils                         | Create ErrorConstants.java with paired code + message        |
| **G11** UUID PKs         | **NF** | VARCHAR PKs in DDL                                            | Flyway migration: ALTER COLUMN id TYPE UUID                  |
| **G14** RBAC/ABAC        | **NF** | No role checks                                                | Add API-to-role map; enforce in Validator layer              |
| **G15** PII encryption   | **NF** | None                                                          | N/A                                                          |
| **G18** API docs         | **P**  | Borrowed Project Postman collection                           | Add Postman collection with create/search/workflow examples  |


✓ Compliant guardrails (9 items)


| Guardrail                   | Status | Evidence                                                                  |
| --------------------------- | ------ | ------------------------------------------------------------------------- |
| **G1** Layered architecture | GE     | Full stack including `FieldPlannerEnrichment`, `FieldPlannerQueryBuilder` |
| **G2** Persister writes     | GE     | `FieldPlannerRepository` extends `GenericRepository` + common `Producer`  |
| **G3** Flyway               | GE     | `V20250901180100__fieldPlanner_create_ddl.sql` (+ 7 migrations)           |
| **G8** QueryBuilder         | GE     | `FieldPlannerQueryBuilder.java`                                           |
| **G10** Audit fields        | GE     | DDL + `FieldPlannerEnrichment.java`                                       |
| **G12** Config external     | GE     | `FieldPlannerConfiguration.java`                                          |
| **G13** Kafka producer      | GE     | Common `Producer` + notification via `FieldPlannerServiceUtil`            |
| **G16** Observability       | GE     | `TracerConfiguration` in `MainConfiguration`                              |
| **G17** Naming              | GE     | `org.egov.field_planner.`*                                                |


---

### 5. field-planner-activity

**Path:** `backend/e4h-services/field-planner-activity`  
**Role:** Field activity, BOM, and assignment  
**Overall alignment:** High

> **Compliance snapshot:** 10/18 fully met · 3 partial · 5 not met

#### Gaps to address


| Guardrail                | Rating | Issue                                           | How to improve                                              |
| ------------------------ | ------ | ----------------------------------------------- | ----------------------------------------------------------- |
| **G4** MDMS              | **P**  | `MDMSUtils.java` + `ActivityConstants.java`     | Move hardcoded constants to MDMS master data                |
| **G6** OpenAPI           | **NF** | None                                            | N/A                                                         |
| **G7** Elasticsearch     | **NF** | None                                            | N/A                                                         |
| **G9** Structured errors | **P**  | Inline `CustomException` in validators/services | Create ErrorConstants.java with paired code + message       |
| **G11** UUID PKs         | **NF** | VARCHAR PKs                                     | Flyway migration: ALTER COLUMN id TYPE UUID                 |
| **G14** RBAC/ABAC        | **NF** | None                                            | N/A                                                         |
| **G15** PII encryption   | **NF** | None                                            | N/A                                                         |
| **G18** API docs         | **P**  | Borrowed Project Postman collection             | Add Postman collection with create/search/workflow examples |


✓ Compliant guardrails (10 items  )


| Guardrail                   | Status | Evidence                                                                                            |
| --------------------------- | ------ | --------------------------------------------------------------------------------------------------- |
| **G1** Layered architecture | GE     | Full stack: Activity/BOM controllers, services, validators, enrichers, repositories, querybuilders  |
| **G2** Persister writes     | GE     | Repositories extend `GenericRepository` + `Producer`                                                |
| **G3** Flyway               | GE     | `V20250919180100__bom_create_ddl.sql` (+ 4 migrations)                                              |
| **G5** Workflow v2          | GE     | `FacilityWorkflowService.java`; transitions in `ActivityAssignmentConsumer`                         |
| **G8** QueryBuilder         | GE     | `ActivityQueryBuilder`, `BomQueryBuilder`, `ActivityAssignmentQueryBuilder`, `DocumentQueryBuilder` |
| **G10** Audit fields        | GE     | DDL + `ActivityEnrichment.java`                                                                     |
| **G12** Config external     | GE     | `ActivityConfiguration.java`                                                                        |
| **G13** Kafka producer      | GE     | Common `Producer` + audit topic publishing                                                          |
| **G16** Observability       | GE     | `TracerConfiguration` in `MainConfiguration`                                                        |
| **G17** Naming              | GE     | `org.egov.activity.`*                                                                               |


---

### 6. im-services

**Path:** `backend/e4h-services/im-services`  
**Role:** Incident and ticket management (PGR/IM)  
**Overall alignment:** Medium-High

> **Compliance snapshot:** 9/18 fully met · 6 partial · 3 not met

#### Gaps to address


| Guardrail                | Rating | Issue                                                                                         | How to improve                                               |
| ------------------------ | ------ | --------------------------------------------------------------------------------------------- | ------------------------------------------------------------ |
| **G2** Persister writes  | **P**  | Primary writes via Kafka (`Producer`); exception: direct `jdbcTemplate.update` in `IMRepos... | Route all writes through Kafka persister; remove direct JDBC |
| **G4** MDMS              | **P**  | `MDMSUtils.java` + hardcoded `IMConstants.java` (80+ status/business strings)                 | Move hardcoded constants to MDMS master data                 |
| **G6** OpenAPI           | **P**  | `swagger-contract.yml`, `contractForCodeGen.yml` in resources                                 | Add OpenAPI spec under docs//; generate from controllers     |
| **G7** Elasticsearch     | **P**  | Indexer topics in config; ES data migrations as Java Flyway classes                           | Add indexer topic + ES index mapping for search/read models  |
| **G9** Structured errors | **NF** | No `ErrorConstants`; inline `CustomException` strings throughout                              | Create ErrorConstants.java with paired code + message        |
| **G11** UUID PKs         | **NF** | VARCHAR incident IDs                                                                          | Flyway migration: ALTER COLUMN id TYPE UUID                  |
| **G14** RBAC/ABAC        | **P**  | Ad-hoc role checks in `IMService`, `UserService`                                              | Add API-to-role map; enforce in Validator layer              |
| **G15** PII encryption   | **NF** | None                                                                                          | N/A                                                          |
| **G18** API docs         | **P**  | `Selco.postman_collection.json`                                                               | Add Postman collection with create/search/workflow examples  |


✓ Compliant guardrails (9 items  )


| Guardrail                   | Status | Evidence                                                                                             |
| --------------------------- | ------ | ---------------------------------------------------------------------------------------------------- |
| **G1** Layered architecture | GE     | `RequestsApiController` → `IMService` → `ServiceRequestValidator` → `EnrichmentService` → `IMReposit |
| **G3** Flyway               | GE     | 30+ SQL migrations + Java ES migrations in `db/migration/main/`                                      |
| **G5** Workflow v2          | GE     | `WorkflowService.java` integrated in `IMService`                                                     |
| **G8** QueryBuilder         | GE     | `IMQueryBuilder`, `IMPriorityQueryBuilder`                                                           |
| **G10** Audit fields        | GE     | `EnrichmentService` + incident DDL                                                                   |
| **G12** Config external     | GE     | `IMConfiguration.java` (extensive topic/URL config)                                                  |
| **G13** Kafka producer      | GE     | `producer/Producer.java` with tenant-scoped push                                                     |
| **G16** Observability       | GE     | `TracerConfiguration`; OTel Kafka in properties                                                      |
| **G17** Naming              | GE     | `org.egov.im.`*                                                                                      |


**Key gaps:** Hardcoded status constants; no structured ErrorConstants; custom `applicationStatus` column alongside WF v2.

---

### 7. im-services-analytics

**Path:** `backend/e4h-services/im-services-analytics`  
**Role:** IM analytics, SLA breach detection, escalation  
**Overall alignment:** Medium-Low

> **Compliance snapshot:** 4/18 fully met · 7 partial · 7 not met

#### Gaps to address


| Guardrail                   | Rating | Issue                                                                           | How to improve                                                    |
| --------------------------- | ------ | ------------------------------------------------------------------------------- | ----------------------------------------------------------------- |
| **G1** Layered architecture | **P**  | Controllers + services + `IncidentRepository`; no Validator or Enricher         | Add Enricher + QueryBuilder layers (copy project service pattern) |
| **G2** Persister writes     | **P**  | Kafka consumer (`EventListener`); reads via JDBC; not a persister write service | Route all writes through Kafka persister; remove direct JDBC      |
| **G3** Flyway               | **NF** | No migrations (reads external IM data)                                          | Add Flyway migrations under src/main/resources/db/migration/      |
| **G4** MDMS                 | **P**  | `MdmsUtil.java` + `IMConstants.java`                                            | Move hardcoded constants to MDMS master data                      |
| **G5** Workflow v2          | **P**  | `WorkflowService` for escalation only                                           | Enable workflow v2 and wire WorkflowService on create/update      |
| **G6** OpenAPI              | **NF** | None                                                                            | N/A                                                               |
| **G9** Structured errors    | **NF** | `ServiceConstants.java` has no error code/message pairs                         | Create ErrorConstants.java with paired code + message             |
| **G10** Audit fields        | **P**  | `AuditDetails` model only; no owned DDL/enricher                                | Move audit field population to dedicated Enricher                 |
| **G11** UUID PKs            | **NF** | N/A (reads external data)                                                       | Flyway migration: ALTER COLUMN id TYPE UUID                       |
| **G13** Kafka producer      | **P**  | `KafkaProducerService` uses raw `KafkaTemplate`, not thin Producer wrapper      | Use thin Producer wrapper; route entity persistence via persister |
| **G14** RBAC/ABAC           | **NF** | None                                                                            | N/A                                                               |
| **G15** PII encryption      | **NF** | None                                                                            | N/A                                                               |
| **G17** Naming              | **P**  | Package `org.selco.e4h.`* deviates from `org.egov.`*                            | Align package and class naming to org.egov..*                     |
| **G18** API docs            | **NF** | None                                                                            | N/A                                                               |


✓ Compliant guardrails (4 items  )


| Guardrail               | Status | Evidence                                                                             |
| ----------------------- | ------ | ------------------------------------------------------------------------------------ |
| **G7** Elasticsearch    | GE     | `ElasticSearchClient`, `ElasticsearchEscalationService`, `SLABreachDetectionService` |
| **G8** QueryBuilder     | GE     | `IncidentQueryBuilder.java`                                                          |
| **G12** Config external | GE     | `ConsumerConfiguration.java`                                                         |
| **G16** Observability   | GE     | `TracerConfiguration` imported                                                       |


**Note:** Analytics/read-only service; many write-path guardrails are N/A by design.

---

### 8. inbox

**Path:** `backend/e4h-services/inbox`  
**Role:** Cross-module inbox aggregator (BFF-like)  
**Overall alignment:** Medium (aggregator; many guardrails N/A)

> **Compliance snapshot:** 6/18 fully met · 4 partial · 8 not met

#### Gaps to address


| Guardrail                   | Rating | Issue                                                                                         | How to improve                                                    |
| --------------------------- | ------ | --------------------------------------------------------------------------------------------- | ----------------------------------------------------------------- |
| **G1** Layered architecture | **P**  | Controllers + `InboxService`/`InboxServiceV2` + validators + ES repository; no Enricher (a... | Add Enricher + QueryBuilder layers (copy project service pattern) |
| **G2** Persister writes     | **NF** | No owned DB; `DataSourceAutoConfiguration` excluded                                           | Route all writes through Kafka persister; remove direct JDBC      |
| **G3** Flyway               | **NF** | No migrations                                                                                 | Add Flyway migrations under src/main/resources/db/migration/      |
| **G4** MDMS                 | **P**  | `MDMSUtil.java` + hardcoded module constants (`FSMConstants`, `BpaConstants`, etc.)           | Move hardcoded constants to MDMS master data                      |
| **G6** OpenAPI              | **NF** | None                                                                                          | N/A                                                               |
| **G9** Structured errors    | **P**  | `ErrorConstants.java` — codes only, no message pairs                                          | Create ErrorConstants.java with paired code + message             |
| **G10** Audit fields        | **NF** | Read-only aggregator                                                                          | Move audit field population to dedicated Enricher                 |
| **G11** UUID PKs            | **NF** | N/A                                                                                           | N/A                                                               |
| **G13** Kafka producer      | **NF** | No persistence producer                                                                       | Use thin Producer wrapper; route entity persistence via persister |
| **G14** RBAC/ABAC           | **P**  | Role-based filtering in `InboxService`, module filter services (`getRoles()`)                 | Add API-to-role map; enforce in Validator layer                   |
| **G15** PII encryption      | **NF** | None                                                                                          | N/A                                                               |
| **G18** API docs            | **NF** | None                                                                                          | N/A                                                               |


✓ Compliant guardrails (6 items  )


| Guardrail               | Status | Evidence                                                                                       |
| ----------------------- | ------ | ---------------------------------------------------------------------------------------------- |
| **G5** Workflow v2      | GE     | `WorkflowService.java`; V2 inbox in `InboxServiceV2`                                           |
| **G7** Elasticsearch    | GE     | Core read path: `ElasticSearchService`, `ElasticSearchRepository`, `ElasticSearchQueryBuilder` |
| **G8** QueryBuilder     | GE     | `InboxQueryBuilder`, `ElasticSearchQueryBuilder`                                               |
| **G12** Config external | GE     | `InboxConfiguration.java`                                                                      |
| **G16** Observability   | GE     | `TracerConfiguration` in `InboxApplication`                                                    |
| **G17** Naming          | GE     | `org.egov.inbox.`* (DIGIT legacy)                                                              |


**Note:** Functions as read-only BFF/aggregator per guardrail §7; write-path guardrails intentionally N/A.

---

### 9. processor-services

**Path:** `backend/e4h-services/processor-services`  
**Role:** Video/media processing Kafka worker  
**Overall alignment:** Low (worker service; most guardrails N/A)

> **Compliance snapshot:** 3/18 fully met · 1 partial · 14 not met

#### Gaps to address


| Guardrail                   | Rating | Issue                                                                                    | How to improve                                                    |
| --------------------------- | ------ | ---------------------------------------------------------------------------------------- | ----------------------------------------------------------------- |
| **G1** Layered architecture | **NF** | `VideoConsumer` → video services only; no Controller, Validator, Enricher, DB Repository | Add Enricher + QueryBuilder layers (copy project service pattern) |
| **G2** Persister writes     | **NF** | Worker; no DB ownership                                                                  | Route all writes through Kafka persister; remove direct JDBC      |
| **G3** Flyway               | **NF** | No migrations                                                                            | Add Flyway migrations under src/main/resources/db/migration/      |
| **G4** MDMS                 | **NF** | None                                                                                     | N/A                                                               |
| **G5** Workflow v2          | **NF** | None                                                                                     | N/A                                                               |
| **G6** OpenAPI              | **NF** | None                                                                                     | N/A                                                               |
| **G7** Elasticsearch        | **NF** | None                                                                                     | N/A                                                               |
| **G8** QueryBuilder         | **NF** | N/A                                                                                      | N/A                                                               |
| **G9** Structured errors    | **NF** | None                                                                                     | N/A                                                               |
| **G10** Audit fields        | **NF** | N/A                                                                                      | N/A                                                               |
| **G11** UUID PKs            | **NF** | N/A                                                                                      | N/A                                                               |
| **G13** Kafka producer      | **P**  | Kafka consumer only (`VideoConsumer`); OTel Kafka in properties                          | Use thin Producer wrapper; route entity persistence via persister |
| **G14** RBAC/ABAC           | **NF** | N/A                                                                                      | N/A                                                               |
| **G15** PII encryption      | **NF** | N/A                                                                                      | N/A                                                               |
| **G18** API docs            | **NF** | None                                                                                     | N/A                                                               |


✓ Compliant guardrails (3 items  )


| Guardrail               | Status | Evidence                                          |
| ----------------------- | ------ | ------------------------------------------------- |
| **G12** Config external | GE     | `ProcessorConfiguration.java`                     |
| **G16** Observability   | GE     | `TracerConfiguration` in `ProcessorConfiguration` |
| **G17** Naming          | GE     | `org.egov.processor.`*                            |


**Note:** Narrow-purpose worker; E4H layered architecture guardrails largely not applicable.

---

### 10. project

**Path:** `backend/e4h-services/project`  
**Role:** Project, beneficiary, task, and resource management  
**Overall alignment:** High — reference E4H implementation

> **Compliance snapshot:** 9/18 fully met · 6 partial · 3 not met

#### Gaps to address


| Guardrail                | Rating | Issue                                                                                         | How to improve                                              |
| ------------------------ | ------ | --------------------------------------------------------------------------------------------- | ----------------------------------------------------------- |
| **G4** MDMS              | **P**  | `MDMSUtils.java` + `ProjectConstants.java`                                                    | Move hardcoded constants to MDMS master data                |
| **G6** OpenAPI           | **P**  | `docs/project-service/project-v1.api.yaml` at repo root                                       | Add OpenAPI spec under docs//; generate from controllers    |
| **G7** Elasticsearch     | **P**  | Search-index DDL migrations; indexer topics in `ProjectConfiguration`                         | Add indexer topic + ES index mapping for search/read models |
| **G8** QueryBuilder      | **P**  | Dedicated builders for address/document/target; core entities use shared `SelectQueryBuild... | Extract inline SQL into dedicated QueryBuilder class        |
| **G9** Structured errors | **P**  | `Constants.VALIDATION_ERROR`; no dedicated paired ErrorConstants class                        | Create ErrorConstants.java with paired code + message       |
| **G11** UUID PKs         | **NF** | `id character varying(64)` in DDL                                                             | Flyway migration: ALTER COLUMN id TYPE UUID                 |
| **G14** RBAC/ABAC        | **NF** | No formal RBAC                                                                                | Add API-to-role map; enforce in Validator layer             |
| **G15** PII encryption   | **NF** | None                                                                                          | N/A                                                         |
| **G18** API docs         | **P**  | `Project Service.postman_collection 6.json`                                                   | Add Postman collection with create/search/workflow examples |


✓ Compliant guardrails (9 items  )


| Guardrail                   | Status | Evidence                                                                                   |
| --------------------------- | ------ | ------------------------------------------------------------------------------------------ |
| **G1** Layered architecture | GE     | Reference stack: controllers, services, validators, enrichers, repositories, querybuilders |
| **G2** Persister writes     | GE     | All entity repos extend `GenericRepository` + common `Producer`                            |
| **G3** Flyway               | GE     | `V20221202180100__project_create_ddl.sql` (+ 40 migrations)                                |
| **G5** Workflow v2          | GE     | `ProjectWorkflowService.java` used in `ProjectService`, `ProjectApiController`             |
| **G10** Audit fields        | GE     | DDL + enrichers (`ProjectEnrichment.java`)                                                 |
| **G12** Config external     | GE     | `ProjectConfiguration.java`                                                                |
| **G13** Kafka producer      | GE     | Common `Producer` via repositories                                                         |
| **G16** Observability       | GE     | `TracerConfiguration` in `MainConfiguration`                                               |
| **G17** Naming              | GE     | `org.egov.project.`*                                                                       |


**Key gaps:** No RBAC; no UUID PKs; no dedicated ErrorConstants.

---

### 11. rms-service

**Path:** `backend/e4h-services/rms-service`  
**Role:** RMS integration, alert orchestration, ticket pause  
**Overall alignment:** Low

> **Compliance snapshot:** 4/18 fully met · 4 partial · 10 not met

#### Gaps to address


| Guardrail                   | Rating | Issue                                                                                         | How to improve                                                    |
| --------------------------- | ------ | --------------------------------------------------------------------------------------------- | ----------------------------------------------------------------- |
| **G1** Layered architecture | **NF** | `RMSController` → orchestration services → JDBC repositories; no Validator, Enricher, Quer... | Add Enricher + QueryBuilder layers (copy project service pattern) |
| **G2** Persister writes     | **NF** | Direct JDBC in `AlertRepository`, `TicketPauseRepository`, `CenterIdMappingRepository`        | Route all writes through Kafka persister; remove direct JDBC      |
| **G4** MDMS                 | **P**  | `MDMSDistrictConfigService.java`; alert types hardcoded in Java                               | Move hardcoded constants to MDMS master data                      |
| **G5** Workflow v2          | **NF** | Creates IM tickets via REST; no WF v2 client                                                  | Enable workflow v2 and wire WorkflowService on create/update      |
| **G6** OpenAPI              | **NF** | None (cron YAMLs in `backend/docs/` only)                                                     | Add OpenAPI spec under docs//; generate from controllers          |
| **G7** Elasticsearch        | **P**  | Audit/indexer publishing via `TicketPauseAuditEventPublisher`                                 | Add indexer topic + ES index mapping for search/read models       |
| **G8** QueryBuilder         | **NF** | Inline SQL in repositories (e.g. `TicketPauseRepository`)                                     | Extract inline SQL into dedicated QueryBuilder class              |
| **G9** Structured errors    | **NF** | `log.error(...)` only; no ErrorConstants                                                      | Create ErrorConstants.java with paired code + message             |
| **G10** Audit fields        | **P**  | Non-standard `created_at`/`updated_at` in DDL                                                 | Move audit field population to dedicated Enricher                 |
| **G11** UUID PKs            | **NF** | `id VARCHAR(255) PRIMARY KEY`                                                                 | Flyway migration: ALTER COLUMN id TYPE UUID                       |
| **G13** Kafka producer      | **P**  | `Producer.java` for audit events only, not entity persistence                                 | Use thin Producer wrapper; route entity persistence via persister |
| **G14** RBAC/ABAC           | **NF** | None                                                                                          | N/A                                                               |
| **G15** PII encryption      | **NF** | None                                                                                          | N/A                                                               |
| **G18** API docs            | **NF** | None                                                                                          | N/A                                                               |


✓ Compliant guardrails (4 items  )


| Guardrail               | Status | Evidence                                     |
| ----------------------- | ------ | -------------------------------------------- |
| **G3** Flyway           | GE     | `V1__create_rms_schema.sql` (+ V2–V5)        |
| **G12** Config external | GE     | `RMSConfiguration.java` (extensive `@Value`) |
| **G16** Observability   | GE     | `TracerConfiguration` in `RMSConfiguration`  |
| **G17** Naming          | GE     | `org.egov.rms.`*                             |


**Additional concern:** `RestTemplateSslUtils.restTemplateAcceptingAllCerts()` disables certificate validation.

**Key gaps:** Does not follow E4H layered architecture or persister write pattern; highest-priority remediation target.

---

### 12. vendor-registry

**Path:** `backend/e4h-services/vendor-registry`  
**Role:** Organisation and vendor registration  
**Overall alignment:** Medium-High

> **Compliance snapshot:** 8/18 fully met · 5 partial · 5 not met

#### Gaps to address


| Guardrail                | Rating | Issue                                                                                         | How to improve                                               |
| ------------------------ | ------ | --------------------------------------------------------------------------------------------- | ------------------------------------------------------------ |
| **G2** Persister writes  | **P**  | Org CRUD via Kafka (`OrganizationProducer`); mixed direct JDBC in `OrganisationUserReposit... | Route all writes through Kafka persister; remove direct JDBC |
| **G4** MDMS              | **P**  | `MDMSUtil.java` + `OrganisationConstant.java`                                                 | Move hardcoded constants to MDMS master data                 |
| **G5** Workflow v2       | **P**  | `WorkflowUtil.java` + `wf_status` in DDL; not injected in `OrganisationService`               | Enable workflow v2 and wire WorkflowService on create/update |
| **G6** OpenAPI           | **NF** | `Organisation_V1.0_OAS3_final.yaml` at service root (outside `src/`); no entry in `docs/`     | Add OpenAPI spec under docs//; generate from controllers     |
| **G7** Elasticsearch     | **NF** | None                                                                                          | N/A                                                          |
| **G9** Structured errors | **NF** | Inline `CustomException` in validators                                                        | Create ErrorConstants.java with paired code + message        |
| **G11** UUID PKs         | **NF** | `id character varying(256)`                                                                   | Flyway migration: ALTER COLUMN id TYPE UUID                  |
| **G14** RBAC/ABAC        | **P**  | Role validation in `OrganisationUserServiceValidator`; `ORG_ADMIN` constant                   | Add API-to-role map; enforce in Validator layer              |
| **G17** Naming           | **P**  | Root package `org.egov` (not `org.egov.vendor`); otherwise consistent                         | Align package and class naming to org.egov..*                |
| **G18** API docs         | **NF** | OAS at service root only                                                                      | Add Postman collection with create/search/workflow examples  |


✓ Compliant guardrails (8 items  )


| Guardrail                   | Status | Evidence                                                                                             |
| --------------------------- | ------ | ---------------------------------------------------------------------------------------------------- |
| **G1** Layered architecture | GE     | Full stack: `OrganisationApiController` → `OrganisationService` → validators → `OrganisationEnrichme |
| **G3** Flyway               | GE     | `V20230301120030__create_table.sql` (+ 7 migrations)                                                 |
| **G8** QueryBuilder         | GE     | 7 dedicated query builders                                                                           |
| **G10** Audit fields        | GE     | DDL + `OrganisationEnrichmentService.java`                                                           |
| **G12** Config external     | GE     | `Configuration.java`                                                                                 |
| **G13** Kafka producer      | GE     | `kafka/OrganizationProducer.java`                                                                    |
| **G15** PII encryption      | GE     | `EncryptionService.java`, `EncryptionDecryptionUtil.java`, encryption migration DDL                  |
| **G16** Observability       | GE     | `TracerConfiguration` in `MainConfiguration`                                                         |


**Key gaps:** No structured ErrorConstants; workflow util not wired into service; mixed write path for user cleanup.

---

## Platform-Wide Patterns

**Core services:** See [E4H_Core_Services_Guardrails_Compliance_Assessment.md](./E4H_Core_Services_Guardrails_Compliance_Assessment.md) for DIGIT upstream comparison and improvement-scope labelling (pre-existing in DIGIT vs introduced during adaptation).

### Guardrails followed consistently across e4h-services


| Guardrail                               | Services at GE level                                               |
| --------------------------------------- | ------------------------------------------------------------------ |
| G12 Configuration externalization       | 11 of 12 (all except processor at P)                               |
| G16 Observability (TracerConfiguration) | 11 of 12 at GE; processor gaps                                     |
| G3 Flyway migrations                    | 10 of 12 domain services; absent in processor, im-analytics, inbox |


### Universal gaps (e4h-services)


| Guardrail             | Status                                                                          |
| --------------------- | ------------------------------------------------------------------------------- |
| G11 UUID primary keys | **NF** across all e4h-services                                                  |
| G14 RBAC/ABAC         | **NF** in most services; ad-hoc checks only in hrms, im-services, inbox, vendor |
| G15 PII encryption    | **GE** only in vendor-registry                                                  |
| G6 OpenAPI            | **NF or P** in most e4h-services                                                |


### Service tier classification (e4h-services)


| Tier                                   | Services                                                      | Characteristics                                        |
| -------------------------------------- | ------------------------------------------------------------- | ------------------------------------------------------ |
| **Tier A — Reference implementations** | project, field-planner, field-planner-activity, amc-scheduler | Full E4H layering, persister writes, Flyway, enrichers |
| **Tier B — Mostly aligned**            | im-services, vendor-registry, egov-hrms                       | Strong patterns; minor gaps                            |
| **Tier C — Adaptation-scope gaps**     | asset-registry, inbox                                         | Guardrail gaps introduced during E4H adaptation        |
| **Tier D — Needs refactor**            | rms-service, processor-services                               | Thin layering / direct JDBC / worker-only              |


---

## Recommended Remediation by Service Priority


| Priority | Service              | Top actions                                                                          |
| -------- | -------------------- | ------------------------------------------------------------------------------------ |
| P0       | **rms-service**      | Refactor to layered architecture; migrate direct JDBC to persister; add QueryBuilder |
| P1       | **asset-registry**   | Add Enricher and QueryBuilder; implement workflow endpoint; remove inline search SQL |
| P1       | **im-services**      | Create `ErrorConstants` with paired codes; migrate hardcoded statuses to MDMS        |
| P2       | **vendor-registry**  | Add ErrorConstants; wire WorkflowUtil into OrganisationService                       |
| P2       | **All e4h-services** | Add OpenAPI specs; implement formal RBAC map                                         |


**Core services improvements:** See [E4H_Core_Services_Guardrails_Compliance_Assessment.md](./E4H_Core_Services_Guardrails_Compliance_Assessment.md) — P0: boundary-service (persister write path changed during adaptation); P1: health-facility-registry (full adaptation scope).

---

# Part II — Frontend Modules

**Source sections:** §13 Frontend Guardrails  
**Paths:** `frontend/installation-ui/` (E4H app), `frontend/micro-ui/` (DIGIT app)

> **Improvement snapshot:** **Biggest gap:** E4H modules (fa/pm/qc/amc/org) use custom tables instead of Inbox v2 · **Reference:** workbench module

Frontend guardrail reference (F1–F12  )


| ID  | Guardrail                                                                    |
| --- | ---------------------------------------------------------------------------- |
| F1  | DIGIT component library (Atoms/Molecules) — no duplicate custom components   |
| F2  | Localization — `t("KEY")` for all user-facing strings                        |
| F3  | `Digit.Hooks.use`* for data fetching (not ad-hoc axios/react-query)          |
| F4  | react-hook-form / FormComposerV2 / DIGIT form components                     |
| F5  | Role-based navigation hiding (`useAccessControl`)                            |
| F6  | Inbox v2 (`InboxSearchComposer`) for list/inbox screens                      |
| F7  | No inline styles; class-based CSS; rem/% not px                              |
| F8  | Redux / React Query state conventions (not mixed within modules)             |
| F9  | Loading, empty, and error states for async views                             |
| F10 | Accessibility — semantic markup, alt text, keyboard navigation (WCAG 2.2 AA) |
| F11 | Offline-tolerant data capture / draft states                                 |
| F12 | Responsive layout (desktop + mobile)                                         |


---

## Frontend Scorecard


| Module                                | GE  | P   | NF  | Priority                         | Top fix                         |
| ------------------------------------- | --- | --- | --- | -------------------------------- | ------------------------------- |
| **workbench** (micro-ui)              | 5   | 6   | 1   | Maintain                         | Reference — copy to E4H modules |
| **core** (installation-ui)            | 2   | 7   | 3   | Inbox v2 + remove inline styles  |                                 |
| **im** (micro-ui)                     | 2   | 8   | 2   | Migrate to InboxSearchComposer   |                                 |
| **hrms, engagement, bills, receipts** | ~2  | ~8  | ~2  | Legacy DIGIT — inbox uplift only |                                 |
| **fa, pm, org** (installation-ui)     | ~1  | ~5  | ~7  | Digit.Hooks + Inbox v2           |                                 |
| **qc, amc** (installation-ui)         | 0   | ~4  | ~9  | FormComposer + Inbox v2          |                                 |
| **dss** (micro-ui)                    | 0   | 4   | 7   | Custom charts — WCAG pass        |                                 |


---

## Frontend Overall Scorecard (detail)


| Module                                | GE  | P   | NF  | Overall                                              |
| ------------------------------------- | --- | --- | --- | ---------------------------------------------------- |
| **workbench** (micro-ui)              | 5   | 6   | 1   | **High** — reference for Inbox v2 + Digit.Hooks      |
| **core** (installation-ui)            | 2   | 7   | 3   | **Medium** — shell OK; styling/inbox gaps            |
| **im** (micro-ui)                     | 2   | 8   | 2   | **Medium** — responsive inbox; legacy pre-v2 pattern |
| **hrms, engagement, bills, receipts** | 1–2 | 8–9 | 1–2 | **Medium** — mature DIGIT; legacy inbox              |
| **fa, pm, org** (installation-ui)     | 0–1 | 4–5 | 6–7 | **Low** — custom tables, ad-hoc hooks, inline styles |
| **qc, amc** (installation-ui)         | 0   | 3–4 | 8–9 | **Low** — no FormComposer, no Inbox v2               |
| **dss** (micro-ui)                    | 0   | 4   | 7   | **Low** — custom chart suite                         |


---

## Frontend Per-Module Assessment

### installation-ui — core

**Path:** `frontend/installation-ui/.../modules/core/`


| Guardrail        | Rating | How to improve                                                            |
| ---------------- | ------ | ------------------------------------------------------------------------- |
| F6 Inbox v2      | NF     | Replace list screens with `InboxSearchComposer` (workbench reference)     |
| F7 Inline styles | NF     | Move 37+ inline styles to SCSS; use rem not px                            |
| F1/F2/F4/F8–F12  | P      | Replace CustomTextInput with digit-ui; move App.js errors to locale files |


✓ Compliant: F3 Digit.Hooks, F5 role-based nav

---

### installation-ui — fa (Facility Admin)

**Path:** `frontend/installation-ui/.../modules/fa/`


| Guardrail           | Rating | How to improve                                                         |
| ------------------- | ------ | ---------------------------------------------------------------------- |
| F1 DIGIT components | NF     | Replace `CustomDropdown`, `CustomCheckBox` with digit-ui components    |
| F3 Digit.Hooks      | NF     | Refactor `useFacility.js` to `Digit.Hooks.useCustomAPIHook`            |
| F6 Inbox v2         | NF     | Replace `FacilityTable.js` with `InboxSearchComposer`                  |
| F7 Inline styles    | NF     | Extract 46 files of inline styles to SCSS                              |
| F5/F8/F10           | NF     | Add useAccessControl; separate Redux from react-query; add aria-labels |


---

### installation-ui — pm (Project Management)

**Path:** `frontend/installation-ui/.../modules/pm/`


| Guardrail             | Rating | How to improve                                                          |
| --------------------- | ------ | ----------------------------------------------------------------------- |
| F1/F3/F5/F6/F7/F8/F10 | NF     | Same pattern as fa — Digit.Hooks, Inbox v2, SCSS, semantic sort buttons |
| F2 Localization       | P      | Move `"Click to sort"` in ProjectTable.js to `t("PM_SORT_HINT")`        |


---

### installation-ui — qc (Quality Control)

**Path:** `frontend/installation-ui/.../modules/qc/`


| Guardrail           | Rating | How to improve                                                |
| ------------------- | ------ | ------------------------------------------------------------- |
| F4 FormComposerV2   | NF     | Rebuild QC review modal with FormComposerV2                   |
| F6 Inbox v2         | NF     | Replace custom facility QC list with InboxSearchComposer      |
| F3/F5/F7/F8/F10/F11 | NF     | Digit.Hooks; useAccessControl; SCSS; no Redux+react-query mix |


---

### installation-ui — amc

**Path:** `frontend/installation-ui/.../modules/amc/`


| Guardrail         | Rating | How to improve                                              |
| ----------------- | ------ | ----------------------------------------------------------- |
| F6 Inbox v2       | NF     | Point `/inbox` route to InboxSearchComposer + AMC inbox API |
| F4 FormComposerV2 | NF     | Rebuild visit review flows with FormComposerV2              |
| F7 Inline styles  | NF     | Extract 17 files to `amc.scss`                              |


---

### installation-ui — org (Organization)

**Path:** `frontend/installation-ui/.../modules/org/`


| Guardrail         | Rating | How to improve                                                            |
| ----------------- | ------ | ------------------------------------------------------------------------- |
| F1/F3/F6          | NF     | Replace CustomDropdown; use Digit.Hooks; InboxSearchComposer for org list |
| F4 FormComposerV2 | P      | Extend FormComposer to org user management screens                        |


---

### micro-ui — workbench

**Path:** `frontend/micro-ui/.../modules/workbench/` · **Reference module**


| Guardrail        | Rating | How to improve                                 |
| ---------------- | ------ | ---------------------------------------------- |
| F7 Inline styles | P      | Move remaining MDMS page inline styles to SCSS |


✓ Compliant: F1, F3, F6 Inbox v2, F8, F9 — copy this pattern to E4H modules

---

### micro-ui — im (Incident Management)

**Path:** `frontend/micro-ui/.../modules/im/`


| Guardrail   | Rating | How to improve                                           |
| ----------- | ------ | -------------------------------------------------------- |
| F6 Inbox v2 | P      | Migrate DesktopInbox/MobileInbox to InboxSearchComposer  |
| F7/F8       | NF     | Extract 18 inline-style files; separate Redux from hooks |


✓ Compliant: F2 localization, F12 responsive mobile/desktop split

---

### micro-ui — dss, hrms, engagement, bills, receipts, common


| Module               | Top fix                                     |
| -------------------- | ------------------------------------------- |
| **hrms, engagement** | Migrate legacy inbox to InboxSearchComposer |
| **bills, receipts**  | Complete InboxSearchComposer migration      |
| **dss**              | Add WCAG aria-labels to chart controls      |
| **common**           | Extract 13 inline-style files to SCSS       |


---

## Frontend Platform-Wide Findings


| Pattern            | How to improve (one action)                                        |
| ------------------ | ------------------------------------------------------------------ |
| **Inbox v2**       | Copy `workbench/MDMSSearchv2.js` InboxSearchComposer setup into fa |
| **Digit.Hooks**    | Refactor `fa/hooks/useFacility.js` first; replicate to pm/org      |
| **Inline styles**  | ESLint rule banning `style={{`; migrate to SCSS module by module   |
| **FormComposerV2** | Rebuild one review modal as template for others                    |


---

# Part III — Mobile App

**Source sections:** §13 (offline capture, localization, forms, accessibility)  
**Path:** `mobile/` (Flutter/Dart — BLoC, Isar, Dio, `digit_ui_components`, `digit_forms_engine`)

> **Improvement snapshot:** **Strongest:** offline/sync, MDMS-driven forms · **Weakest:** accessibility (M6 — zero Semantics)

Mobile guardrail reference (M1–M10  )


| ID  | Guardrail                              |
| --- | -------------------------------------- |
| M1  | Localization / i18n                    |
| M2  | Offline-tolerant data capture, drafts  |
| M3  | Role-based access / navigation hiding  |
| M4  | MDMS/schema-driven forms               |
| M5  | Responsive layout                      |
| M6  | Accessibility (Semantics, WCAG 2.2 AA) |
| M7  | Shared component patterns              |
| M8  | API / tenant / auth handling           |
| M9  | Error, loading, empty states           |
| M10 | MDMS/config-driven business rules      |


---

## Mobile Scorecard


| Area                    | GE  | P   | NF  | Top fix                                     |
| ----------------------- | --- | --- | --- | ------------------------------------------- |
| **Offline / Sync**      | 4   | 1   | 0   | Maintain — best-in-class                    |
| **Installation Report** | 3   | 5   | 2   | Semantics on form fields; i18n on home.dart |
| **AMC module**          | 3   | 5   | 2   | Remove hardcoded tenantId; i18n errors      |
| **Shared widgets**      | 3   | 2   | 1   | Continue digit kit adoption                 |
| **Inbox / Review**      | 2   | 6   | 2   | Show error states; MDMS workflow filters    |
| **Auth & onboarding**   | 1   | 4   | 3   | Semantics on login; wire RoleActionsModel   |
| **Accessibility (all)** | 0   | 0   | 10  | Add Semantics wrapper — apply page by page  |


---

## Mobile Per-Module Assessment

### Auth & Onboarding

**Paths:** `lib/pages/welcome.dart`, `login.dart`, `forgot_password.dart`, `enter_otp.dart`, `role_selection.dart`


| Guardrail        | Rating | How to improve                                                               |
| ---------------- | ------ | ---------------------------------------------------------------------------- |
| M6 Accessibility | NF     | Wrap login fields in `Semantics(label: context.translate(i18.login.userId))` |
| M2 Offline       | NF     | Allow cached credential check; queue token refresh when online               |
| M3 RBAC          | P      | Use fetched `RoleActionsModel` to hide menu items user cannot access         |
| M1 i18n          | P      | Replace `"User ID"`, `"Forgot Password"` hardcoded in login.dart             |


---

### App Init & MDMS Bootstrap

**Paths:** `lib/blocs/app_init/`, `lib/widgets/mdms/mdms_gate.dart`


| Guardrail | Rating | How to improve                                                        |
| --------- | ------ | --------------------------------------------------------------------- |
| M1 i18n   | P      | Move `'Token expired! Please login again.'` to i18_key_constants.dart |


✓ Compliant: M2 offline MDMS cache, M8 tenant/auth, M10 MDMS bootstrap

---

### Installation Report

**Paths:** `lib/pages/home.dart`, `dynamic_form.dart`, `draft.dart`, `lib/blocs/asset_submission/`


| Guardrail        | Rating | How to improve                                                 |
| ---------------- | ------ | -------------------------------------------------------------- |
| M6 Accessibility | NF     | Add Semantics to stepper steps and form fields                 |
| M1 i18n          | P      | Move `"Data Sync Pending!"` in home.dart to i18 keys           |
| M10 MDMS/config  | P      | Replace `WORKFLOW_STATUS_`* Dart enums with MDMS-loaded config |


✓ Compliant: M2 offline/drafts, M4 config forms, M7 shared components, M8 API

---

### AMC Module

**Paths:** `lib/pages/amc_home.dart`, `amc_dynamic_form.dart`, `amc_draft.dart`


| Guardrail       | Rating | How to improve                                                    |
| --------------- | ------ | ----------------------------------------------------------------- |
| M10 MDMS/config | P      | Load schema ID from MDMS; use envConfig tenantId in amc_home.dart |
| M1 i18n         | P      | Move `"Failed to load AMC form schema"` to i18 keys               |


✓ Compliant: M2 offline drafts, M3 RBAC routes, M4 MDMS forms

---

### Inbox & Review

**Paths:** `lib/pages/inbox.dart`, `inbox_asset_summary.dart`


| Guardrail        | Rating | How to improve                                                         |
| ---------------- | ------ | ---------------------------------------------------------------------- |
| M9 Error/loading | P      | Show error widget with retry instead of `SizedBox.shrink()` on failure |
| M10 MDMS/config  | P      | Load inbox filter statuses from MDMS not hardcoded enums               |


✓ Compliant: M1 i18n empty states, M3 supervisor vs field-staff tabs

---

### Media Upload

**Paths:** `lib/widgets/customized_digit_widget/image_uploader.dart`


| Guardrail     | Rating | How to improve                             |
| ------------- | ------ | ------------------------------------------ |
| M5 Responsive | P      | Use shared breakpoint constants from theme |


✓ Compliant: M2 offline/resumable uploads, M7 digit kit wrappers

---

### Shared Infrastructure

**Paths:** `lib/data/remote_client.dart`, `lib/utils/background_service.dart`, `lib/utils/utils.dart`


| Guardrail       | Rating | How to improve                                          |
| --------------- | ------ | ------------------------------------------------------- |
| M10 MDMS/config | P      | Fetch WORKFLOW_STATUS, USER_TYPES from MDMS at app init |


✓ Compliant: M2 Isar offline architecture, M8 auth/tenant, M9 error normalization

---

## Mobile Platform-Wide Findings


| Pattern                     | How to improve (one action)                                     |
| --------------------------- | --------------------------------------------------------------- |
| **Offline / field capture** | Maintain — Isar + background sync is reference quality          |
| **MDMS-driven forms**       | Maintain — digit_forms_engine + BOM/AMC schemas                 |
| **Accessibility**           | Create `AccessibleTextField` wrapper; apply to login.dart first |
| **Localization**            | Audit 7 pages with hardcoded strings; add i18 keys              |
| **Workflow rules**          | Replace Dart enums in utils.dart with MDMS fetch                |
| **Tenant handling**         | Grep for hardcoded `'in'`; replace with envConfig               |


---

# Part IV — Database Migrations

**Source sections:** §5.1 (Flyway), §12 (data lifecycle), §15 (schema choreography), §27 (migration plan)  
**Paths:** `backend/e4h-services/**/src/main/resources/db/migration/`  
**Core services migrations:** See [E4H_Core_Services_Guardrails_Compliance_Assessment.md](./E4H_Core_Services_Guardrails_Compliance_Assessment.md).

> **Improvement snapshot:** **Best aligned:** im-services (Java/ES migrations) · **Weakest:** UUID PKs, rollback docs, CASCADE usage

Migration guardrail reference (MG1–MG10  )


| ID   | Guardrail                                                         |
| ---- | ----------------------------------------------------------------- |
| MG1  | Flyway versioned migrations present and enabled                   |
| MG2  | Additive / backward-compatible changes only                       |
| MG3  | Idempotent migration scripts (`IF NOT EXISTS`, etc.)              |
| MG4  | Audit fields in DDL (`createdBy`, `createdTime`, `lastModified`*) |
| MG5  | Soft delete via status — no `ON DELETE CASCADE`                   |
| MG6  | UUID primary keys in DDL                                          |
| MG7  | ES / indexer migration scripts (where applicable)                 |
| MG8  | Data reconciliation / reindex approach documented                 |
| MG9  | Migration rollback strategy documented                            |
| MG10 | Java-based Flyway migrations for data transforms                  |


---

## Migration Scorecard


| Service                    | GE  | P   | NF  | Top fix                                     |
| -------------------------- | --- | --- | --- | ------------------------------------------- |
| **im-services**            | 3   | 4   | 3   | Add ES reconciliation runbook               |
| **asset-registry**         | 4   | 3   | 3   | UUID migration for asset_id                 |
| **rms-service**            | 3   | 2   | 5   | Rename audit columns to platform standard   |
| **vendor-registry**        | 3   | 4   | 3   | Add rollback docs                           |
| **project**                | 2   | 6   | 2   | Remove CASCADE on transaction comments      |
| **field-planner**          | 2   | 3   | 5   | Add IF NOT EXISTS to CREATE statements      |
| **amc-scheduler-service**  | 1   | 4   | 5   | Remove ON DELETE CASCADE on visit documents |
| **field-planner-activity** | 1   | 4   | 5   | Remove CASCADE on activity comments         |
| **egov-hrms**              | 1   | 4   | 5   | Outside adaptation scope                    |


---

## Migration Overall Scorecard (detail)


| Service                    | GE  | P   | NF  | Overall                                                |
| -------------------------- | --- | --- | --- | ------------------------------------------------------ |
| **im-services**            | 3   | 4   | 3   | **Medium-High** — only service with Java/ES migrations |
| **asset-registry**         | 4   | 3   | 3   | **Medium**                                             |
| **rms-service**            | 3   | 2   | 5   | **Medium** (schema); weak on audit fields              |
| **vendor-registry**        | 3   | 4   | 3   | **Medium**                                             |
| **project**                | 2   | 6   | 2   | **Medium**                                             |
| **field-planner**          | 2   | 3   | 5   | **Medium-Low**                                         |
| **amc-scheduler-service**  | 1   | 4   | 5   | **Low**                                                |
| **field-planner-activity** | 1   | 4   | 5   | **Low**                                                |
| **egov-hrms**              | 1   | 4   | 5   | **Pre-existing in DIGIT**                              |


---

## Migration Per-Service Assessment

### im-services (best aligned)

**Path:** `backend/e4h-services/im-services/src/main/resources/db/migration/`


| Guardrail          | Rating | How to improve                                             |
| ------------------ | ------ | ---------------------------------------------------------- |
| MG6 UUID PKs       | NF     | `ALTER COLUMN serviceRequestId TYPE UUID`                  |
| MG9 Rollback       | NF     | Add `db/migration/README.md` with rollback SQL per version |
| MG2 Additive only  | P      | Replace DROP COLUMN with phased deprecation                |
| MG3 Idempotent     | P      | Add `IF NOT EXISTS` to remaining CREATE TABLE statements   |
| MG8 Reconciliation | P      | Add `backend/docs/im-es-reconciliation.md` runbook         |


✓ Compliant: MG1 Flyway (31 SQL + 26 Java), MG4 audit fields, MG5 soft delete, MG7 ES migrations, MG10 Java transforms

---

### asset-registry


| Guardrail    | Rating | How to improve                                              |
| ------------ | ------ | ----------------------------------------------------------- |
| MG6 UUID PKs | NF     | UUID migration for `asset_id`                               |
| MG7–MG10     | NF/N/A | Add ES index migration if search indexed; document rollback |


✓ Compliant: MG1 Flyway, MG2 additive, MG4 audit fields, MG5 soft delete

---

### project


| Guardrail       | Rating | How to improve                                                    |
| --------------- | ------ | ----------------------------------------------------------------- |
| MG5 Soft delete | P      | Replace `ON DELETE CASCADE` on transaction comments with SET NULL |
| MG6 UUID PKs    | NF     | UUID migration for project entity tables                          |
| MG2/MG3         | P      | Use schema choreography for renames; add IF NOT EXISTS            |


✓ Compliant: MG1 Flyway (36 migrations), MG4 audit fields

---

### rms-service


| Guardrail          | Rating | How to improve                                        |
| ------------------ | ------ | ----------------------------------------------------- |
| MG4 Audit fields   | NF     | Rename `created_at`/`updated_at` to platform standard |
| MG6 UUID PKs       | NF     | UUID migration for RMS tables                         |
| MG8 Reconciliation | P      | Add reconciliation doc for pause-expiry cron outcomes |


✓ Compliant: MG1–MG3, MG5 soft delete

---

### Pre-existing in DIGIT (egov-hrms)


| Service       | Key migration gaps                                                                                     |
| ------------- | ------------------------------------------------------------------------------------------------------ |
| **egov-hrms** | Extensive `ON DELETE CASCADE` on child tables; `DROP COLUMN` alters; no platform audit on idgen tables |


---

### E4H domain services with CASCADE gaps


| Service                    | Evidence                                                                                   |
| -------------------------- | ------------------------------------------------------------------------------------------ |
| **amc-scheduler-service**  | `ON DELETE CASCADE` on visit documents in `V20251114180100__amc_create_ddl.sql`            |
| **field-planner-activity** | `ON DELETE CASCADE` in `V20251015163200__activity_facility_transaction_comment_create.sql` |


---

## Migration Platform-Wide Findings


| Guardrail area              | Overall         | Key finding                                                                                      |
| --------------------------- | --------------- | ------------------------------------------------------------------------------------------------ |
| **§5.1 Flyway**             | **P**           | 10 e4h-services have versioned migrations; idempotence and additive-only discipline inconsistent |
| **§12 Data lifecycle**      | **NF**          | No documented ES reconciliation SLO or reindex plan                                              |
| **§15 Schema choreography** | **NF**          | Direct renames/drops without phased read/write dual-support                                      |
| **§27 Migration plan**      | **NF**          | No migration plan, cut-over doc, or rollback strategy in repo                                    |
| **UUID PKs (MG6)**          | **NF**          | Not followed in any service under strict interpretation                                          |
| **Rollback (MG9)**          | **NF**          | No `U`__ undo migrations; no rollback docs anywhere                                              |
| **Best practice reference** | **im-services** | Only service with Java Flyway + ES index migration classes                                       |


---

## Cross-Platform Summary


| Area                        | Start here                     |
| --------------------------- | ------------------------------ |
| **Backend (e4h-services)**  | rms-service refactor           |
| **Backend (core-services)** | boundary-service persister fix |
| **Frontend**                | fa module Inbox v2             |
| **Mobile**                  | Semantics on login.dart        |
| **Migration**               | migration-plan.md + UUID PKs   |


---

## Methodology

1. Read `C2 Selco_E4H_Design_Guardrails_v1.txt` and mapped guardrails to assessable criteria per area (backend G1–G18, frontend F1–F12, mobile M1–M10, migration MG1–MG10).
2. **Backend (e4h-services):** Scanned `backend/e4h-services/` (12 services). **Core services** assessed separately in [E4H_Core_Services_Guardrails_Compliance_Assessment.md](./E4H_Core_Services_Guardrails_Compliance_Assessment.md) with DIGIT upstream comparison.
3. **Frontend:** Scanned `frontend/installation-ui/` (E4H modules) and `frontend/micro-ui/` (DIGIT modules).
4. **Mobile:** Scanned `mobile/lib/` (pages, blocs, repositories, widgets, utils).
5. **Migration:** Scanned `db/migration/` and `src/main/java/db/migration/` under e4h-services; core-services migrations covered in the core-services doc.
6. Excluded `target/` compiled output throughout.

**Limitation:** Process/governance guardrails (§4, §28–30, §33) describing team practices rather than code artefacts are not fully verifiable from the repository alone.

---

*Generated from per-service codebase analysis against Selco E4H Design Guardrails v1.0.*

---

## Appendix: Design Guardrails Reference

Source: `C2 Selco_E4H_Design_Guardrails_v1.txt`

### Part II — Architecture & Design

**§3 Architecture Principles**

- Config over code — tenant/deployment variability in MDMS, not hard-coded constants
- Least-privilege by design — every API mapped to role-action; default deny; no super-admin roles
- System of Record vs Transaction — derived/computed views stay out of the SoR
- API-first — versioned REST APIs with OpenAPI specs and example payloads
- Workflow everywhere — human approvals via Workflow v2; no custom status columns as substitutes
- Events, not cron — cross-service reactions via event bus; no DB polling/cron where events are feasible
- Data from day one — Elasticsearch indices and KPIs designed upfront, not retrofitted
- Observability from day one — metrics, logs, traces, and audit trails as first-class requirements

**§5 Service Design**

- DDD-lite — bounded contexts, aggregate roots, ownership table per service
- Schema — UUID PKs; `createdBy`/`createdTime`/`lastModified`*; soft delete via status; no `ON DELETE CASCADE`
- Flyway — idempotent, additive, backward-compatible migrations only
- Read models — no runtime cross-service joins; search views via Indexer and domain events
- MDMS — masters, flags, validation rules, templates in MDMS; validity dates; fail closed on unknown enums
- Workflow — states in MDMS + Workflow v2; SLAs, assignees, escalation, re-open paths
- API — minimal surface (create, update, search, workflow-transition); OpenAPI 3.0+; DIGIT common contracts; bulk APIs separate; idempotency on updates; business keys, not internal IDs
- Events — transactional vs domain events separated; topic naming `<domain>.<context>.<entity>.<event>.<version>`; DLQ per consumer; additive schemas with registry

**§6 Search & Data Access**

- Service search APIs query only owned data
- Cross-service enrichment via aggregator/BFF or denormalised ES read models
- No cross-service SQL or database-to-database joins

**§7 BFFs & Aggregators**

- Read-only by default; call owning services via public APIs only
- Version BFF endpoints independently; propagate pagination; timeouts, circuit breakers, graceful degradation
- Cache safe GETs with TTL/ETags; never cache RBAC beyond token lifetime

**§8 Extensibility**

- Configurable rules (MDMS) or plug-in evaluators — no hard-coded business rules
- External systems behind interfaces with config-driven endpoints
- PDFs from template packs, not embedded HTML

**§9 Security**

- OIDC/OAuth authentication; mTLS service-to-service; centralised secrets with rotation
- STRIDE threat modelling for internet-exposed endpoints
- PII — minimise collection; field-level encryption; redact from logs; FileStore via signed URLs; no PII in dashboards
- RBAC/ABAC — explicit API-to-action-to-role map; ABAC at domain level
- Input validation — size limits, file type checks, anti-zip-bomb, authorisation before DB access

**§10 Performance & Reliability**

- SLOs — RPS, p95 latency, throughput per hot path
- Caching — read-only masters via in-memory/Redis with TTL
- Backpressure — async queues for heavy flows; bulk exports as background jobs
- Resilience — circuit breakers, saga compensations, timeouts/retries with exponential backoff; error budgets

**§11 Observability & Audit**

- Metrics — API latency, workflow counts, queue/indexer lag, error rates
- Logs — structured JSON with traceId, user, tenant, action; no PII
- Tracing — W3C trace headers across gateway, services, Kafka consumers
- Audit — who/what/when with old-to-new diffs; sensitive fields redacted

**§12 Data Architecture**

- ES — routing keys, shard/replica strategy, ILM rollover, reconciliation SLO, PII masked/excluded
- Lifecycle — retention, archival, purge jobs, legal holds, RPO/RTO with restore drills

**§14 Multi-Tenancy**

- Documented isolation model (row-level vs schema-per-tenant); quotas; region-pinned data; per-tenant KMS where required

**§15 Progressive Delivery**

- Canary/blue-green deployments; feature-flag taxonomy with kill-switches
- Schema choreography — write-old/read-both → write-both/read-new → remove-old
- CI gates — SAST/DAST, SBOM, license checks, coverage thresholds

**§16 API Lifecycle**

- Stages — Alpha, Beta, GA, Deprecated, Retired with entry/exit criteria
- Backward-compatible by default; deprecation headers and migration guides
- ADRs required for data stores, event schemas, external dependencies

### Part III — Build Standards

**§17 Naming**

- Components — `<Entity><Role>` (e.g. `BoundaryService`, `BoundaryValidator`, `BoundaryQueryBuilder`)
- Methods — action verbs matching layer (`createBoundary`, `validateCreateBoundaryRequest`)
- Variables — descriptive, no abbreviations; collections plural; constants in dedicated files with paired code+message errors

**§18 Layering**

- Fixed flow — validate → enrich → persist → respond
- Controller routes only; Service orchestrates; Validator holds business rules; Enricher adds UUIDs/audit/defaults; Repository delegates to QueryBuilder
- No business logic in controllers; no direct DB access from services; no business logic in repositories

**§19 Error Handling**

- Platform structured errors only — paired code + message constants
- Collect all validation errors before surfacing; add runtime context in square brackets

**§20 Validation**

- Declarative — schema validation on contracts (required, type, format, size bounds)
- Programmatic — business rules in Validator private helpers (uniqueness, cross-field, MDMS references)

**§21 Data Contracts**

- Explicit serialised field names; auto-generated boilerplate (Lombok); builder pattern
- Schema-flexible fields as generic JSON (`additionalDetails`)
- Shared platform contracts for `RequestInfo`, `ResponseInfo`, `AuditDetails`

**§22 Data Access**

- All SQL in QueryBuilder; parameterised queries only; no inline string concatenation
- Writes via event bus → persister; reads via repositories directly

**§23 Event Producer**

- Thin Producer component; topic names from configuration, never hardcoded
- Payloads include version, tenantId, traceId, occurredAt; conform to schema registry

**§24 Configuration**

- Single config component per service; hierarchical dot-separated keys; no hardcoded hosts/topics/timeouts
- Secrets from secret manager, not config files

**§25 Dependencies**

- Use shared platform libraries — contracts, tracing, encryption client, MDMS client

**§26 Method Size & Idioms**

- Service methods 5–10 lines; validator public methods 3–5 lines; helpers 10–20 lines
- Higher-order iteration idioms; standard null/emptiness utilities

### Assessment Criteria (G1–G18)


| ID  | Guardrail                                                                                      |
| --- | ---------------------------------------------------------------------------------------------- |
| G1  | Layered architecture (Controller → Service → Validator → Enricher → Repository → QueryBuilder) |
| G2  | Kafka persister writes — no direct DB writes from service layer                                |
| G3  | Flyway versioned migrations                                                                    |
| G4  | MDMS / config-over-code                                                                        |
| G5  | Workflow v2 integration                                                                        |
| G6  | OpenAPI specification                                                                          |
| G7  | Elasticsearch / indexer read models                                                            |
| G8  | QueryBuilder pattern — no inline SQL                                                           |
| G9  | Structured errors — paired code + message constants                                            |
| G10 | Audit fields in DDL and enricher                                                               |
| G11 | UUID primary keys                                                                              |
| G12 | Configuration externalization                                                                  |
| G13 | Kafka producer pattern                                                                         |
| G14 | RBAC / ABAC                                                                                    |
| G15 | PII field-level encryption                                                                     |
| G16 | Observability (TracerConfiguration, structured logging)                                        |
| G17 | Naming conventions                                                                             |
| G18 | Postman / API documentation artefacts                                                          |


