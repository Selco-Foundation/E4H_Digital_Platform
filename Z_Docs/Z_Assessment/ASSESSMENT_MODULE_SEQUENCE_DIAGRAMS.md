# Assessment Module — Sequence Diagrams

Companion to `ASSESSMENT_MODULE_LLD 1.md`. Persistence: `field_plans`, `facility_activities` (ASSESSMENT), `eg_assessment_submission`.

**Facility reuse rules (§2.2.9):** see **§8** — three flows for Rules 1–9 (same project AP, cross-project, Excel edge case); §§4–7 are worked examples.

---

## 1. Create assessment plan (wizard)

```mermaid
sequenceDiagram
  participant PM
  participant Web as PM web UI
  participant Asm as field-planner-service (AssessmentController)
  participant Prj as project-service

  PM->>Web: Wizard step 1 — plan info
  Web->>Asm: POST /plan/_create
  Asm->>Asm: INSERT field_plans (plan_type=ASSESSMENT, status=ACTIVE)
  Asm-->>Web: planId

  PM->>Web: Step 2 — grid select facilities
  Web->>Prj: project-linked facilities
  Prj-->>Web: facility list (filtered by §2.2.9)
  Web->>Asm: POST /plan/facility/_bulk-include
  Asm->>Asm: PlanFacilityIncludeService — R0–R7
  Asm->>Asm: INSERT facility_activities (ENROLLED)
  Asm-->>Web: created[] / errors[]

  PM->>Web: Step 3 — assign assessors
  Web->>Asm: POST /plan/_update
  Asm->>Asm: activity_assignments (ENUMERATOR, FIELD_POC)
```



**Optional:** ingestion `assessmentPlanIncludeApply` → `internal/plan/facility/_bulk-create` (same §2.2.9 validation).

---

## 2. Remote + on-site assessment

```mermaid
sequenceDiagram
  participant Enum as Remote Assessor
  participant FPOC as Field POC
  participant Mob as Mobile
  participant Asm as field-planner-service

  Enum->>Mob: Submit remote form
  Mob->>Asm: POST /submission/phone/_create
  Asm->>Asm: OutcomeEngine → phone_status QUALIFIED/NOT_QUALIFIED

  Note over Asm: PM assigns on-site (decision/_update)

  FPOC->>Mob: Submit on-site form
  Mob->>Asm: POST /submission/field/_create
  Asm->>Asm: OutcomeEngine → field_status
  alt Both phases QUALIFIED and not manually set
    Asm->>Asm: Case 3/8 — auto overall_status = ELIGIBLE
  else Both phases NOT_QUALIFIED and not manually set
    Asm->>Asm: Case 9 — auto overall_status = NOT_ELIGIBLE
  else Mixed or PM already set result
    Asm->>Asm: Case 7 — keep PM result OR stay PENDING for PM Cases 4/5
  end
```



---

## 2b. Assessment result — eligibility cases (§2.2.1)

Full decision logic: `ASSESSMENT_MODULE_LLD 1.md` §2.2.1 (Cases 1–9) and §2.7.3 Diagrams A–C.

```mermaid
flowchart TD
  subgraph auto [System auto on on-site submit]
    bothQ[Both phases QUALIFIED] --> C38[Cases 3 & 8 — auto ELIGIBLE]
    bothNQ[Both phases NOT_QUALIFIED] --> C9[Case 9 — auto NOT_ELIGIBLE]
    mixed[Mixed outcomes] --> pend[Result stays PENDING]
  end

  subgraph pm [PM manual]
    C4[Case 4 — Mark Eligible]
    C5[Case 5 — Mark Not Eligible + reason]
    C8ov[Case 8 override — Not Eligible + ineligibleReason]
    C9ov[Case 9 override — Eligible + eligibleReason]
  end

  subgraph gates [Hard gates]
    C1[Case 1 — remote pending: no on-site assign; no Mark Eligible or Not Eligible]
    C6[Case 6 — result set: no on-site assign]
    C7[Case 7 — PM result locked if set while on-site Pending]
  end

  C38 --> C8ov
  C9 --> C9ov
  pend --> C4
  pend --> C5
```

---

## 3. PM decisions + field plan handoff (multi-plan)

```mermaid
sequenceDiagram
  participant PM
  participant Web as PM web UI
  participant Asm as field-planner-service
  participant Ing as ingestion-service
  participant FP as field-planner

  PM->>Web: Mark eligible / not eligible
  Web->>Asm: decision/_update or _bulk-update

  PM->>Asm: POST /plan/_mark-complete (when all finalized)
  Asm->>Asm: field_plans.status = CLOSED

  PM->>Ing: fieldplanFacilityIngestionTemplate(assessmentPlanIds[])
  Ing->>Asm: project/eligible-facilities/_search (parent plans CLOSED)
  Ing-->>PM: Excel with planFacilityId

  PM->>Ing: createFieldPlanFacility
  Ing->>Asm: verify + handoff MOVED_TO_FIELD_PLAN
  Ing->>FP: facility/_create
```



---

## 4. Same project — eligible blocked from second assessment plan (R1)

```mermaid
sequenceDiagram
  participant PM
  participant Web as PM web UI
  participant Asm as field-planner-service

  Note over PM,Asm: Plan A: PHC Khunti is ELIGIBLE, not on any FP
  PM->>Web: Create Plan B (same project); include PHC Khunti
  Web->>Asm: POST /plan/facility/_bulk-include
  Asm->>Asm: R1 — same-project eligible unassigned
  Asm-->>Web: 400 ASSESSMENT_FACILITY_ELIGIBLE_ACTIVE
```



PM must hand off to installation field plan, mark not eligible, or (after marking Plan A complete) move facility to **another project** per §2.2.9 R3.

---

## 5. Same project — not eligible re-assessment (R2)

```mermaid
sequenceDiagram
  participant PM
  participant Web as PM web UI
  participant Asm as field-planner-service

  Note over PM,Asm: Plan A: AWC Murhu NOT_ELIGIBLE
  PM->>Asm: POST /plan/_mark-complete (Plan A)
  PM->>Web: Create Plan B; include AWC Murhu
  Web->>Asm: POST /plan/facility/_bulk-include
  Asm->>Asm: R0 pass (Plan A CLOSED); INSERT new ENROLLED row
  Asm-->>Web: created[] — Plan A row unchanged
```



---

## 6. Cross project — eligible carries forward (R3, Rule 8)

```mermaid
sequenceDiagram
  participant PM
  participant Web as PM web UI
  participant Asm as field-planner-service
  participant Ing as ingestion-service
  participant FP as field-planner
  participant Prj as project-service

  Note over PM,Asm: Plan A / Project 1: PHC Khunti ELIGIBLE (may be on Field Plan X in Proj_1)
  PM->>Asm: POST /plan/_mark-complete (Plan A)
  PM->>Prj: Link PHC Khunti to Project 2 (§2.2.9 validate)

  alt Include in Project 2 field plan (FP_1B)
    PM->>Ing: createFieldPlanFacility on Project 2
    Ing->>Asm: verify eligible — reuse Proj_1 eligibility
    Ing->>FP: facility/_create on Field Plan B
    Note over Asm: Plan A row stays ELIGIBLE — never EXPIRED
  else PM optionally starts fresh assessment (AP_2B)
    PM->>Web: Create Plan C on Project 2; include PHC Khunti
    Web->>Asm: POST /plan/facility/_bulk-include
    Asm->>Asm: INSERT Plan C row → ENROLLED
    Note over Asm: Plan A row still ELIGIBLE — not expired
    Asm-->>Web: created[]
  end
```



---

## 7. Post-installation — new project cycle (R7)

```mermaid
sequenceDiagram
  participant PM
  participant FP as field-planner
  participant Asm as field-planner-service
  participant Prj as project-service

  Note over PM,FP: Facility was MOVED_TO_FIELD_PLAN on FP X
  PM->>FP: Complete installation + report reviewed
  PM->>FP: Mark field plan complete (status=CLOSED)
  PM->>Prj: Link facility to new project (R0–R5, R7 pass)
  PM->>Asm: Include in new assessment plan on new project
  Asm->>Asm: INSERT fresh ENROLLED row; prior rows historical
```



---

## 8. Facility reuse — decision flow (§2.2.9)

Applies to `**_bulk-include**`, **field-plan ingestion apply**, and **project facility link** (`createFacilityAndUpdateProject`).

> Assessment result of a facility in **Proj_1** is valid for **Proj_2**. Eligible status in **Proj_1 / AP_1** is never marked expired on cross-project reuse.

### 8.1 Same project — include in Assessment Plan 2 / Project 1 (Rules 1 & 2)

Facility is on **Assessment Plan 1 / Project 1**. Project manager tries to include it in **Assessment Plan 2 / Project 1**.

```mermaid
flowchart TD
  facility_on_source([Facility on Assessment Plan 1 / Project 1]) --> is_eligible{Is the facility eligible?}

  is_eligible -->|Yes| rule_1[Eligible facility from Assessment Plan 1 / Project 1 cannot be included in Assessment Plan 2 / Project 1]
  is_eligible -->|No| rule_2[Non-eligible facility from Assessment Plan 1 / Project 1 can be included in Assessment Plan 2 / Project 1]

  rule_1 --> alternatives[Instead: hand off to installation field plan, mark not eligible, or move to another project]
```



### 8.2 Different project — add facility to Project 2 (Rules 3–8)

Facility is on **Assessment Plan 1 / Project 1**. Project manager tries to add it to **Project 2**.

```mermaid
flowchart TD
  facility_on_source([Facility on Assessment Plan 1 / Project 1]) --> ongoing_assessment{Is the facility under an ongoing assessment plan?<br/>overall status is pending}

  ongoing_assessment -->|Yes| rule_5[Any facility from Project 1 cannot be added to Project 2 while under an ongoing assessment plan]
  ongoing_assessment -->|No| is_eligible{Is the facility eligible?}

  is_eligible -->|Yes — eligible| rule_3[Eligible facility from Assessment Plan 1 / Project 1 can be included in Project 2 and added to a Project 2 installation field plan using Proj_1 eligibility]
  is_eligible -->|No — not eligible| rule_4[Non-eligible facility from Assessment Plan 1 / Project 1 can be included in Project 2 for new Assessment]

  rule_3 --> rule_6[A facility can be added to Project 2 while on an ongoing field plan or installation in Project 1]
  rule_6 --> cross_project_eligible{What does project manager choose for Project 2?}
  cross_project_eligible -->|Include in Project 2 field plan| field_plan_path[Add to Project 2 installation field plan using Proj_1 eligibility — allowed even if installation is ongoing in Project 1<br/>Assessment Plan 1 row stays eligible — never expired]
  cross_project_eligible -->|Optionally start new assessment| optional_assessment[Project manager may include in Assessment Plan 2B on Project 2 for fresh assessment<br/>Assessment Plan 1 row stays eligible — never expired]

  field_plan_path --> done([Facility active in Project 2])
  optional_assessment --> done
  rule_4 --> done
```



### 8.3 Field-plan Excel — Project 1 sheet after cross-project move (Rule 9)

Facility was moved from **Project 1** to **Project 2**. Project manager uploads **Field Plan 1 / Project 1** Excel that still lists the facility.

```mermaid
flowchart TD
  facility_moved([Facility moved from Project 1 to Project 2]) --> in_excel{Facility appears in Field Plan 1 / Project 1 Excel sheet?}

  in_excel -->|Yes| rule_9[Discard facility from Project 1 Excel — opt for fresh assessment in Project 2 only. Second assessment within Project 1 is not allowed for eligible facility.]
  in_excel -->|No| normal_ingest[Normal field plan ingestion]

  rule_9 --> fresh_assessment([Fresh assessment only in Project 2])
```



### Rules 1–9 (from LLD)


| Rule  | Condition                                                                       | Outcome                                                                                                                                                            |
| ----- | ------------------------------------------------------------------------------- | ------------------------------------------------------------------------------------------------------------------------------------------------------------------ |
| **1** | Eligible facility from AP_1/Proj_1 → AP_2/Proj_1                                | **Reject**                                                                                                                                                         |
| **2** | Not-eligible facility from AP_1/Proj_1 → AP_2/Proj_1                            | **Allow**                                                                                                                                                          |
| **3** | Eligible facility from AP_1/Proj_1 → Proj_2                                     | **Allow** — may go to Proj_2 field plan using Proj_1 eligibility, including while on ongoing FP in Proj_1                                                          |
| **4** | Not-eligible facility from AP_1/Proj_1 → Proj_2                                 | **Allow**                                                                                                                                                          |
| **5** | Add to Proj_2 while `overall_status = PENDING` on an assessment plan            | **Reject**                                                                                                                                                         |
| **6** | Add to Proj_2 while on an ongoing field plan in Proj_1                          | **Allow**                                                                                                                                                          |
| **7** | New assessment or new field plan in **Proj_1** after a prior installation cycle | **Available** in Proj_1 only after installation completes and report is reviewed. Does **not** block inclusion in **Proj_2** field plan during Proj_1 installation |
| **8** | Eligible facility from Proj_1/AP_1 linked to Proj_2                             | **Never expired** in AP_1; PM may optionally start Proj_2 assessment                                                                                               |
| **9** | Facility in Proj_2, then listed in FP_1/Proj_1 Excel                            | **Discard** Proj_1 row; use Proj_2 only                                                                                                                            |


---

## Participants


| Participant                                  | Role                                                                           |
| -------------------------------------------- | ------------------------------------------------------------------------------ |
| PM                                           | Project manager (web)                                                          |
| PM web UI                                    | Assessment plan screens (reuses field plan creation wizard for §1)             |
| ingestion-service                            | Excel export / field-plan ingestion                                            |
| field-planner-service (AssessmentController) | `/assessment/v1` — plans, `facility_activities`, submissions, handoff          |
| MDMS                                         | `AssessmentFormSchema`, `AssessmentOutcomeRules`, `solar_solution_design_type` |
| Mobile                                       | Remote Assessor / Field POC (`ENUMERATOR`, `FIELD_POC`)                        |
| project-service                              | Project-linked facility list                                                   |
| facility-service                             | Facility master enrichment                                                     |
| field-planner                                | Installation field plan (`/v1/field-plans`)                                    |


