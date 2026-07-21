# Assessment Module — API Specs

**Source:** `ASSESSMENT_MODULE_LLD 1.md` §3–§4, ERS V3.2  
**Auth:** `RequestInfo.authToken` (DIGIT envelope) on all routes  
**Tenant:** `tenantId` in body or derived from auth context

---

## 1. Conventions

| Item | Value |
|------|--------|
| Assessment service base | `field-planner-service` → `/assessment/v1` |
| Installation field plans | Existing `/v1/field-plans` (unchanged) |
| Ingestion base | `ingestion-service` |
| Request envelope | `RequestInfo` + domain payload (E4H / DIGIT standard) |
| Id mapping | `planId` / `assessmentPlanId` = assessment `field_plans.id` (stored on ASSESSMENT rows as `facility_activities.assessment_plan_id`); `planFacilityId` = ASSESSMENT `facility_activities.id`; `installationFieldPlanId` = installation `field_plans.id`; `assessmentPlanIds[]` for multi-plan field-plan pick; `sourcePlanFacilityId` = source ASSESSMENT row on `field_plan_facilities` (§5.5 LLD) |
| Data model | No separate `assessment_plans` table — assessment plans are `field_plans` where `plan_type = ASSESSMENT`. Optional DB view `assessment_plans`. |
| Facility trail | §5.5 LLD — `assessment_plan_id` + `installation_field_plan_id` on `facility_activities`; `source_plan_facility_id` + `assessmentSource` on `field_plan_facilities` |
| Pagination | `offset`, `limit` query params where noted; default `limit=50`, max `100` |
| Timestamps | Epoch milliseconds unless noted |
| Assessment result (§2.2.1) | **System auto:** Cases **3 & 8** → `ELIGIBLE` on `field/_create` when both phases `QUALIFIED`; Case **9** → `NOT_ELIGIBLE` when both `NOT_QUALIFIED` (skipped if `overallManuallySet = true`, Case 7). **PM manual:** Cases **4 & 5** via `decision/_update`. **Gates:** Case **1** blocks assign + both mark actions while remote pending; Case **6** blocks assign when result already set. |

### 1.1 OpenAPI summary — field-planner-service (`/assessment/v1`)

| Method | Path | Tag | Auth roles |
|--------|------|-----|------------|
| POST | `/plan/_create` | Plans | `PROJECT_MANAGER` |
| POST | `/plan/_search` | Plans | `PROJECT_MANAGER` |
| POST | `/plan/_detail` | Plans | `PROJECT_MANAGER` |
| POST | `/plan/_update` | Plans | `PROJECT_MANAGER` |
| POST | `/plan/_mark-complete` | Plans | `PROJECT_MANAGER` |
| POST | `/plan/facility/_search` | Plan facilities | `PROJECT_MANAGER` |
| POST | `/plan/facility/_detail` | Plan facilities | `PROJECT_MANAGER` |
| POST | `/plan/facility/_bulk-include` | Plan facilities | `PROJECT_MANAGER` |
| POST | `/plan/facility/decision/_update` | Decisions | `PROJECT_MANAGER` |
| POST | `/plan/facility/decision/_bulk-update` | Decisions | `PROJECT_MANAGER` |
| POST | `/internal/plan/facility/_bulk-create` | Internal | `SYSTEM_USER` (ingestion) |
| POST | `/internal/project/eligible-facilities/_search` | Internal | `SYSTEM_USER` (ingestion) |
| POST | `/internal/plan/passed-facilities/_search` | Internal | Deprecated — use `eligible-facilities/_search` |
| POST | `/submission/form/_resolve` | Submissions | `ENUMERATOR`, `FIELD_POC` |
| POST | `/submission/queue/_search` | Submissions | `ENUMERATOR`, `FIELD_POC` |
| POST | `/submission/phone/_create` | Submissions | `ENUMERATOR` |
| POST | `/submission/phone/_unable-to-contact` | Submissions | `ENUMERATOR` |
| POST | `/submission/field/_create` | Submissions | `FIELD_POC` |
| POST | `/submission/_search` | Submissions | `PROJECT_MANAGER` (optional) |

### 1.2 OpenAPI summary — ingestion-service (assessment-related)

| Method | Path | Tag | Auth roles |
|--------|------|-----|------------|
| POST | `/ingestion-service/template/assessmentPlanIncludeTemplate` | Assessment include | `PROJECT_MANAGER` |
| POST | `/ingestion-service/ingest/assessmentPlanIncludeValidateData` | Assessment include | `PROJECT_MANAGER` |
| POST | `/ingestion-service/ingest/assessmentPlanIncludeApply` | Assessment include | `PROJECT_MANAGER` |
| POST | `/ingestion-service/template/assessmentPlanFacilityExport` | Assessment export | `PROJECT_MANAGER` |
| POST | `/ingestion-service/template/fieldplanFacilityIngestionTemplate` | Field plan (extended) | `PROJECT_MANAGER` |
| POST | `/ingestion-service/ingest/fieldPlanfacilitiesValidateData` | Field plan (extended) | `PROJECT_MANAGER` |
| POST | `/ingestion-service/ingest/createFieldPlanFacility` | Field plan (extended) | `PROJECT_MANAGER` |

### 1.3 Enumerations

| Field | Allowed values |
|-------|----------------|
| `phoneStatus` / `phone_status` | `PENDING`, `PENDING_WRONG_NUMBER`, `PENDING_NO_ANSWER`, `QUALIFIED`, `NOT_QUALIFIED` |
| `fieldStatus` / `field_status` | `NULL` (Not Initiated), `PENDING`, `QUALIFIED`, `NOT_QUALIFIED` |
| `overallStatus` / `overall_status` | `PENDING`, `ELIGIBLE`, `NOT_ELIGIBLE` |
| `assessmentCompletionStatus` / `assessment_completion_status` | `ENROLLED`, `ELIGIBLE`, `NOT_ELIGIBLE`, `MOVED_TO_FIELD_PLAN`, `EXPIRED` |
| `facilityCategory` | `HEALTH`, `ANGANWADI` |
| `assessmentPhase` | `PHONE`, `FIELD` |
| `formType` (server-derived) | `HF_PHONE`, `HF_FIELD`, `AWC_PHONE`, `AWC_FIELD` |
| `outcome` | `QUALIFIED`, `NOT_QUALIFIED` |
| `unableToContactReason` | `WRONG_NUMBER`, `NO_ANSWER` |
| Assessor role codes | `ENUMERATOR` (Remote Assessor), `FIELD_POC` |

### 1.4 Standard error envelope

All assessment APIs return DIGIT-style errors:

```json
{
  "ResponseInfo": {
    "apiId": "assessment-web",
    "ver": "1.0",
    "ts": 1717000000000,
    "resMsgId": "uui-res-001",
    "msgId": "uui-req-001",
    "status": "failed"
  },
  "Errors": [
    {
      "code": "ASSESSMENT_REMOTE_PENDING",
      "message": "Selected facilities have pending remote assessments",
      "description": "Mark eligible/not eligible blocked while phone_status is PENDING, PENDING_WRONG_NUMBER, or PENDING_NO_ANSWER",
      "params": ["fa-uuid-001", "fa-uuid-002"]
    }
  ]
}
```

| HTTP | When |
|------|------|
| `400` | Validation / business rule violation |
| `401` | Missing or invalid auth token |
| `403` | Role not permitted for action |
| `404` | `planId` or `planFacilityId` not found |
| `409` | Duplicate submission (phone/field already submitted) |
| `500` | Unexpected server error |

### 1.5 Eligibility cases (§2.2.1 LLD)

| Case | Condition | API behaviour |
|------|-----------|---------------|
| **1** | Remote `PENDING` / `PENDING_WRONG_NUMBER` / `PENDING_NO_ANSWER` | `assignForField` rejected (`ASSESSMENT_ASSIGN_FIELD_INVALID`); `overallStatus` `ELIGIBLE` or `NOT_ELIGIBLE` rejected (`ASSESSMENT_REMOTE_PENDING`) |
| **2** | Remote `QUALIFIED` or `NOT_QUALIFIED` | `assignForField` allowed when §2.2.2 preconditions met |
| **3** | Both phases `QUALIFIED` | `field/_create` → auto `overallStatus = ELIGIBLE`, `assessmentCompletionStatus = ELIGIBLE` |
| **4** | PM marks Eligible | `decision/_update` → `overallStatus = ELIGIBLE`; sets `overallManuallySet = true` |
| **5** | PM marks Not Eligible | `decision/_update` → `overallStatus = NOT_ELIGIBLE`; `ineligibleReason` required; sets `overallManuallySet = true` |
| **6** | Result already `ELIGIBLE` or `NOT_ELIGIBLE` | `assignForField` rejected (`ASSESSMENT_RESULT_ALREADY_SET` or `ASSESSMENT_ASSIGN_FIELD_INVALID`) |
| **7** | PM set result while on-site `PENDING` | Later `field/_create` updates phase statuses only; does **not** overwrite PM result |
| **8** | Both phases `QUALIFIED` (after auto Eligible) | PM override to `NOT_ELIGIBLE` requires `ineligibleReason` (`ASSESSMENT_INELIGIBLE_REASON_REQUIRED`) |
| **9** | Both phases `NOT_QUALIFIED` | `field/_create` → auto `overallStatus = NOT_ELIGIBLE`; PM override to `ELIGIBLE` requires `eligibleReason` (`ASSESSMENT_ELIGIBLE_REASON_REQUIRED`) |

**`allowedActions` computation** (returned on `_search`, `_detail`, `decision/_update`):

| Flag | `true` when |
|------|-------------|
| `assignForField` | Remote `QUALIFIED` or `NOT_QUALIFIED`; `fieldStatus` null; `overallStatus = PENDING` (Cases 2; not 1 or 6) |
| `markEligible` | Remote not pending (not Case 1) |
| `markNotEligible` | Remote not pending (not Case 1) |

UI may additionally disable mark buttons when result is already final unless override modals apply (Cases 8 & 9 — §2.2.6).

---

## 2. Shared object models

### 2.1 `AssessmentPlan`

| Field | Type | Required | Notes |
|-------|------|----------|-------|
| `id` | string | response | `field_plans.id` — exposed as `planId` / `assessmentPlanId` (no separate `assessment_plans` table) |
| `tenantId` | string | yes | |
| `projectId` | string | yes | FK → project |
| `name` | string | yes | Unique per `(tenantId, projectId, name)` |
| `state` | string | yes | Stored in `geography_scope.state` |
| `startDate` | long | yes | Epoch ms |
| `endDate` | long | yes | Epoch ms |
| `status` | string | response | `ACTIVE`, `CLOSED` |
| `planType` | string | response | Always `ASSESSMENT` |
| `healthFacilityCount` | integer | response | Count of included facilities |
| `canProceedToFieldPlan` | boolean | response | `false` while any facility has `overall_status = PENDING` |
| `assessors` | `AssessorAssignment[]` | response | From `activity_assignments` |

### 2.2 `AssessorAssignment`

| Field | Type | Required | Notes |
|-------|------|----------|-------|
| `role` | string | yes | `ENUMERATOR` or `FIELD_POC` |
| `email` | string | yes | Resolved to `assigned_to` user id |
| `userId` | string | response | HRMS user uuid |
| `pocNumber` | string | no | Phone |

### 2.3 `PlanFacility`

| Field | Type | Required | Notes |
|-------|------|----------|-------|
| `planFacilityId` | string | response | `facility_activities.id` |
| `planId` | string | yes | Same as `assessmentPlanId` — `facility_activities.assessment_plan_id` → `field_plans.id` |
| `assessmentPlanId` | string | response | `facility_activities.assessment_plan_id` (assessment plan FK) |
| `facilityId` | string | yes | |
| `facilityName` | string | response | From snapshot or facility-service |
| `facilityCategory` | string | response | `HEALTH` / `ANGANWADI` |
| `facilityType` | string | response | e.g. `PHC`, `SC` |
| `district` | string | response | Snapshot |
| `block` | string | response | Snapshot |
| `phoneStatus` | string | response | Remote assessment status |
| `fieldStatus` | string | response | On-site status; `null` = Not Initiated |
| `overallStatus` | string | response | Assessment result |
| `assessmentCompletionStatus` | string | response | Enrollment lifecycle / handoff (§2.2.7) |
| `installationFieldPlanId` | string | response | Set when `MOVED_TO_FIELD_PLAN` — installation `field_plans.id` |
| `fieldPlanFacilityId` | string | response | FK → `field_plan_facilities.id` after handoff apply (optional) |
| `sourcePlanFacilityId` | string | response | On installation rows — source ASSESSMENT `planFacilityId` reused (cross-project trail) |
| `phoneOutcome` | string | response | `QUALIFIED` / `NOT_QUALIFIED` |
| `fieldOutcome` | string | response | `QUALIFIED` / `NOT_QUALIFIED` |
| `eligibleReason` | string | response / request | PM override when both outcomes `NOT_QUALIFIED` (Case 9 — §2.2.6) |
| `ineligibleReason` | string | response / request | Required for `NOT_ELIGIBLE` (Case 5); mandatory override when both outcomes `QUALIFIED` (Case 8 — §2.2.6) |
| `ineligibleRemarks` | string | response / request | Optional PM remarks |
| `overallManuallySet` | boolean | response | `true` after PM Cases 4 & 5; blocks Case 3 / Case 9 auto-overwrite on later `field/_create` (Case 7) |
| `lastActionTime` | long | response | For grid "last action" column |
| `allowedActions` | object | response | `{ assignForField, markEligible, markNotEligible }` — see §1.5 |

### 2.4 `AssessmentSubmission`

| Field | Type | Required | Notes |
|-------|------|----------|-------|
| `id` | string | response | |
| `planFacilityId` | string | yes | |
| `assessmentPlanId` | string | response | From `facility_activities.assessment_plan_id` |
| `assessmentPhase` | string | response | `PHONE` / `FIELD` |
| `formType` | string | response | Server-derived |
| `submissionData` | object | yes | Keys = MDMS `fieldCode` |
| `outcome` | string | response | `QUALIFIED` / `NOT_QUALIFIED` |
| `submittedBy` | string | response | User id |
| `submittedByName` | string | phone only | Assessor display name |
| `clientSubmissionTime` | long | no | Device epoch ms |
| `serverReceivedTime` | long | response | |

### 2.5 `AssessmentFormSchema` (from MDMS)

Returned by `submission/form/_resolve`:

```json
{
  "formType": "HF_PHONE",
  "fields": [
    {
      "fieldCode": "govtOwned",
      "label": "Is the health facility building owned by Government?",
      "type": "SELECT",
      "required": true,
      "options": ["YES", "NO"]
    }
  ]
}
```

### 2.6 `FacilityTrail` (read model — optional `plan/facility/_trail` or embedded in `_detail`)

Per-facility history across assessment plans and installation field plans (§5.5 LLD).

| Field | Type | Notes |
|-------|------|-------|
| `planFacilityId` | string | ASSESSMENT `facility_activities.id` |
| `assessmentPlanId` | string | `facility_activities.assessment_plan_id` |
| `projectId` | string | From `field_plans.project_id` |
| `assessmentPlanName` | string | |
| `assessmentPlanStatus` | string | `ACTIVE` / `CLOSED` |
| `assessmentCompletionStatus` | string | |
| `overallStatus` | string | |
| `installationFieldPlanId` | string | If handed off in same project |
| `installationFieldPlans` | array | From `field_plan_facilities` — `{ fieldPlanFacilityId, fieldPlanId, projectId, sourcePlanFacilityId, assessmentSource }` |

---

## 3. field-planner-service — Plans

### 3.1 `POST /assessment/v1/plan/_create`

Wizard step 1 — create assessment plan shell.

**Request:**
```json
{
  "RequestInfo": {
    "apiId": "assessment-web",
    "authToken": "Bearer <token>",
    "msgId": "plan-create-001"
  },
  "plan": {
    "tenantId": "in",
    "projectId": "proj-uuid-001",
    "name": "Jharkhand Assessment Plan Q1 2026",
    "state": "Jharkhand",
    "startDate": 1735689600000,
    "endDate": 1743465600000
  }
}
```

**Response (201):**
```json
{
  "ResponseInfo": { "status": "successful", "msgId": "plan-create-001" },
  "plan": {
    "id": "plan-uuid-001",
    "tenantId": "in",
    "projectId": "proj-uuid-001",
    "name": "Jharkhand Assessment Plan Q1 2026",
    "state": "Jharkhand",
    "startDate": 1735689600000,
    "endDate": 1743465600000,
    "status": "ACTIVE",
    "planType": "ASSESSMENT",
    "healthFacilityCount": 0,
    "canProceedToFieldPlan": false
  }
}
```

**Errors:**

| Code | HTTP | Sample message |
|------|------|----------------|
| `ASSESSMENT_PLAN_NAME_DUPLICATE` | 400 | Plan name already exists for this project |
| `ASSESSMENT_PROJECT_NOT_FOUND` | 404 | Project not found |
| `ASSESSMENT_INVALID_DATE_RANGE` | 400 | endDate must be after startDate |

```json
{
  "ResponseInfo": { "status": "failed" },
  "Errors": [
    {
      "code": "ASSESSMENT_PLAN_NAME_DUPLICATE",
      "message": "Plan name already exists for this project",
      "params": ["Jharkhand Assessment Plan Q1 2026"]
    }
  ]
}
```

---

### 3.2 `POST /assessment/v1/plan/_search`

List assessment plans for a project.

**Request:**
```json
{
  "RequestInfo": { "apiId": "assessment-web", "authToken": "Bearer <token>" },
  "criteria": {
    "tenantId": "in",
    "projectId": "proj-uuid-001"
  }
}
```

**Query:** `?offset=0&limit=20`

**Response (200):**
```json
{
  "ResponseInfo": { "status": "successful" },
  "plans": [
    {
      "id": "plan-uuid-001",
      "name": "Jharkhand Assessment Plan Q1 2026",
      "state": "Jharkhand",
      "startDate": 1735689600000,
      "endDate": 1743465600000,
      "status": "ACTIVE",
      "healthFacilityCount": 142,
      "canProceedToFieldPlan": false
    }
  ],
  "pagination": { "offset": 0, "limit": 20, "total": 3 }
}
```

---

### 3.3 `POST /assessment/v1/plan/_detail`

Plan screen metric cards + metadata.

**Request:**
```json
{
  "RequestInfo": { "apiId": "assessment-web", "authToken": "Bearer <token>" },
  "planId": "plan-uuid-001"
}
```

**Response (200):**
```json
{
  "ResponseInfo": { "status": "successful" },
  "plan": {
    "id": "plan-uuid-001",
    "name": "Jharkhand Assessment Plan Q1 2026",
    "state": "Jharkhand",
    "startDate": 1735689600000,
    "endDate": 1743465600000,
    "status": "ACTIVE",
    "canProceedToFieldPlan": true,
    "metrics": {
      "remoteAssessmentDone": 120,
      "remoteAssessmentTotal": 142,
      "onSiteAssessmentDone": 45,
      "onSiteAssessmentAssigned": 60,
      "eligible": 38,
      "notEligible": 82,
      "resultPending": 22
    },
    "assessors": [
      { "role": "ENUMERATOR", "email": "ramesh@example.com", "userId": "user-enum-001" },
      { "role": "FIELD_POC", "email": "suresh@example.com", "userId": "user-fpoc-001" }
    ]
  }
}
```

**Errors:**

| Code | HTTP | When |
|------|------|------|
| `ASSESSMENT_PLAN_NOT_FOUND` | 404 | Invalid `planId` |

---

### 3.4 `POST /assessment/v1/plan/_update`

Wizard step 3 — assign assessors; optionally update plan name/dates.

**Request:**
```json
{
  "RequestInfo": { "apiId": "assessment-web", "authToken": "Bearer <token>" },
  "plan": {
    "id": "plan-uuid-001",
    "tenantId": "in",
    "name": "Jharkhand Assessment Plan Q1 2026",
    "startDate": 1735689600000,
    "endDate": 1743465600000
  },
  "assessors": [
    { "role": "ENUMERATOR", "email": "ramesh@example.com", "pocNumber": "9876543210" },
    { "role": "FIELD_POC", "email": "suresh@example.com", "pocNumber": "9876543211" }
  ]
}
```

**Response (200):**
```json
{
  "ResponseInfo": { "status": "successful" },
  "plan": {
    "id": "plan-uuid-001",
    "assessors": [
      { "role": "ENUMERATOR", "email": "ramesh@example.com", "userId": "user-enum-001" },
      { "role": "FIELD_POC", "email": "suresh@example.com", "userId": "user-fpoc-001" }
    ]
  }
}
```

**Errors:**

| Code | HTTP | When |
|------|------|------|
| `ASSESSMENT_ASSESSOR_NOT_FOUND` | 400 | Email not found in HRMS |
| `ASSESSMENT_ASSESSOR_ROLE_REQUIRED` | 400 | Both ENUMERATOR and FIELD_POC required |

---

### 3.5 `POST /assessment/v1/plan/_mark-complete`

Mark assessment plan **complete** (`field_plans.status = CLOSED`). Required before facilities from this plan can be reused in another assessment plan, field plan, or project (§2.2.8, §2.2.9 R0).

**Precondition:** All facilities have `overall_status` ∈ {`ELIGIBLE`, `NOT_ELIGIBLE`} — same as `canProceedToFieldPlan = true`.

**Request:**
```json
{
  "RequestInfo": { "apiId": "assessment-web", "authToken": "Bearer <token>" },
  "planId": "plan-uuid-001",
  "tenantId": "in"
}
```

**Response (200):**
```json
{
  "ResponseInfo": { "status": "successful" },
  "plan": { "id": "plan-uuid-001", "status": "CLOSED", "canProceedToFieldPlan": true }
}
```

**Errors:**

| Code | HTTP | When |
|------|------|------|
| `ASSESSMENT_PLAN_HAS_PENDING_FACILITIES` | 400 | Any row still `overall_status = PENDING` |
| `ASSESSMENT_PLAN_NOT_FOUND` | 404 | Invalid `planId` |

---

## 4. field-planner-service — Plan facilities

### 4.1 `POST /assessment/v1/plan/facility/_search`

Paginated plan facility grid + export source.

**Request:**
```json
{
  "RequestInfo": { "apiId": "assessment-web", "authToken": "Bearer <token>" },
  "planId": "plan-uuid-001",
  "filters": {
    "district": "Khunti",
    "facilityCategory": "HEALTH",
    "facilityType": "PHC",
    "phoneStatus": "QUALIFIED",
    "fieldStatus": "PENDING",
    "overallStatus": "PENDING"
  },
  "exportAll": false,
  "includeResponseSummary": false
}
```

**Query:** `?offset=0&limit=50`

**Response (200):**
```json
{
  "ResponseInfo": { "status": "successful" },
  "facilities": [
    {
      "planFacilityId": "fa-uuid-001",
      "planId": "plan-uuid-001",
      "facilityId": "fac-uuid-001",
      "facilityName": "PHC Khunti",
      "facilityCategory": "HEALTH",
      "facilityType": "PHC",
      "district": "Khunti",
      "block": "Murhu",
      "phoneStatus": "QUALIFIED",
      "fieldStatus": "PENDING",
      "overallStatus": "PENDING",
      "phoneOutcome": "QUALIFIED",
      "fieldOutcome": null,
      "lastActionTime": 1716900000000,
      "allowedActions": {
        "assignForField": false,
        "markEligible": true,
        "markNotEligible": true
      }
    }
  ],
  "pagination": { "offset": 0, "limit": 50, "total": 142 }
}
```

When `includeResponseSummary=true` (ingestion export), each facility includes `phoneResponseSummary` and `fieldResponseSummary` string arrays.

---

### 4.2 `POST /assessment/v1/plan/facility/_detail`

Facility detail screen — submissions, audit trail, allowed actions.

**Request:**
```json
{
  "RequestInfo": { "apiId": "assessment-web", "authToken": "Bearer <token>" },
  "planFacilityId": "fa-uuid-001"
}
```

**Response (200):**
```json
{
  "ResponseInfo": { "status": "successful" },
  "facility": {
    "planFacilityId": "fa-uuid-001",
    "assessmentPlanId": "plan-uuid-001",
    "facilityId": "fac-uuid-001",
    "facilityName": "PHC Khunti",
    "facilityCategory": "HEALTH",
    "facilityType": "PHC",
    "district": "Khunti",
    "block": "Murhu",
    "phoneStatus": "QUALIFIED",
    "fieldStatus": "QUALIFIED",
    "overallStatus": "ELIGIBLE",
    "phoneOutcome": "QUALIFIED",
    "fieldOutcome": "QUALIFIED",
    "overallManuallySet": false,
    "ineligibleReason": null,
    "allowedActions": {
      "assignForField": false,
      "markEligible": false,
      "markNotEligible": false
    },
    "submissions": [
      {
        "id": "sub-phone-001",
        "assessmentPhase": "PHONE",
        "formType": "HF_PHONE",
        "outcome": "QUALIFIED",
        "submittedByName": "Ramesh Kumar",
        "submissionData": { "govtOwned": "YES", "existingSolar": "NO" },
        "serverReceivedTime": 1716800000000
      },
      {
        "id": "sub-field-001",
        "assessmentPhase": "FIELD",
        "formType": "HF_FIELD",
        "outcome": "QUALIFIED",
        "submissionData": { "roofCondition": "GOOD" },
        "serverReceivedTime": 1716900000000
      }
    ],
    "auditTrail": [
      { "event": "INCLUDED_IN_PLAN", "timestamp": 1716700000000, "actor": "pm-user-001", "assessmentPlanId": "plan-uuid-001" },
      { "event": "REMOTE_SUBMITTED", "timestamp": 1716800000000, "actor": "user-enum-001" },
      { "event": "ASSIGNED_FOR_ONSITE", "timestamp": 1716850000000, "actor": "pm-user-001" },
      { "event": "ONSITE_SUBMITTED", "timestamp": 1716900000000, "actor": "user-fpoc-001" },
      { "event": "OVERALL_AUTO_ELIGIBLE", "timestamp": 1716900000001, "actor": "SYSTEM" }
    ]
  }
}
```

Audit events for assessment result: `OVERALL_SET_ELIGIBLE` / `OVERALL_SET_NOT_ELIGIBLE` (PM Cases 4 & 5); `OVERALL_AUTO_ELIGIBLE` (Cases 3 & 8); `OVERALL_AUTO_NOT_ELIGIBLE` (Case 9).

**Example — remote pending (Case 1):** `allowedActions.assignForField`, `markEligible`, and `markNotEligible` are all `false` when `phoneStatus` ∈ {`PENDING`, `PENDING_WRONG_NUMBER`, `PENDING_NO_ANSWER`}.

---

### 4.3 `POST /assessment/v1/plan/facility/decision/_update`

Single-facility PM actions: assign on-site (Case 2), mark eligible (Case 4), mark not eligible (Case 5). Validates §2.2.1–§2.2.4 and §2.2.6.

**Validation summary:**

| Action | Blocked when | Required fields |
|--------|--------------|-----------------|
| `assignForField: true` | Case 1 (remote pending); Case 6 (result already `ELIGIBLE`/`NOT_ELIGIBLE`); §2.2.2 preconditions fail | — |
| `overallStatus: ELIGIBLE` | Case 1 (remote pending) | `eligibleReason` when both `phoneOutcome` and `fieldOutcome` are `NOT_QUALIFIED` (Case 9 override — §2.2.6) |
| `overallStatus: NOT_ELIGIBLE` | Case 1 (remote pending) | `ineligibleReason` always (Case 5); mandatory when both outcomes `QUALIFIED` (Case 8 override — §2.2.6) |

Sets `overallManuallySet = true` on successful PM mark eligible / not eligible (Cases 4 & 5; Case 7 when on-site still `PENDING`).

**Assign for on-site:**
```json
{
  "RequestInfo": { "apiId": "assessment-web", "authToken": "Bearer <token>" },
  "planFacilityId": "fa-uuid-001",
  "assignForField": true
}
```

**Mark eligible (Case 9 override — both outcomes `NOT_QUALIFIED`):**
```json
{
  "RequestInfo": { "apiId": "assessment-web", "authToken": "Bearer <token>" },
  "planFacilityId": "fa-uuid-008",
  "overallStatus": "ELIGIBLE",
  "eligibleReason": "District officer confirmed facility meets program criteria despite form responses"
}
```

**Mark eligible (Case 4 — standard):**
```json
{
  "RequestInfo": { "apiId": "assessment-web", "authToken": "Bearer <token>" },
  "planFacilityId": "fa-uuid-001",
  "overallStatus": "ELIGIBLE"
}
```

**Mark not eligible (Case 8 override — both outcomes `QUALIFIED`):**
```json
{
  "RequestInfo": { "apiId": "assessment-web", "authToken": "Bearer <token>" },
  "planFacilityId": "fa-uuid-009",
  "overallStatus": "NOT_ELIGIBLE",
  "ineligibleReason": "Site visit photos show roof unsuitable despite questionnaire pass",
  "remarks": "PM override"
}
```

**Mark not eligible (Case 5 — standard):**
```json
{
  "RequestInfo": { "apiId": "assessment-web", "authToken": "Bearer <token>" },
  "planFacilityId": "fa-uuid-010",
  "overallStatus": "NOT_ELIGIBLE",
  "ineligibleReason": "Existing solar installation"
}
```

**Response (200) — assign for on-site:**
```json
{
  "ResponseInfo": { "status": "successful" },
  "facility": {
    "planFacilityId": "fa-uuid-001",
    "fieldStatus": "PENDING",
    "overallStatus": "PENDING",
    "overallManuallySet": false,
    "allowedActions": {
      "assignForField": false,
      "markEligible": true,
      "markNotEligible": true
    }
  }
}
```

**Errors:**

| Code | HTTP | When |
|------|------|------|
| `ASSESSMENT_REMOTE_PENDING` | 400 | Case 1 — Mark eligible **or** Mark not eligible while `phone_status` ∈ {`PENDING`, `PENDING_WRONG_NUMBER`, `PENDING_NO_ANSWER`} |
| `ASSESSMENT_ASSIGN_FIELD_INVALID` | 400 | Case 1 or §2.2.2 — assign preconditions not met (remote not done, on-site already initiated, or result not `PENDING`) |
| `ASSESSMENT_RESULT_ALREADY_SET` | 400 | Case 6 — `assignForField` when `overall_status` already `ELIGIBLE`/`NOT_ELIGIBLE` |
| `ASSESSMENT_INELIGIBLE_REASON_REQUIRED` | 400 | Case 5 / Case 8 — `NOT_ELIGIBLE` without `ineligibleReason` (always required; mandatory override when both outcomes `QUALIFIED`) |
| `ASSESSMENT_ELIGIBLE_REASON_REQUIRED` | 400 | Case 9 — both outcomes `NOT_QUALIFIED` but `ELIGIBLE` without `eligibleReason` |
| `ASSESSMENT_PLAN_FACILITY_NOT_FOUND` | 404 | Invalid `planFacilityId` |

```json
{
  "ResponseInfo": { "status": "failed" },
  "Errors": [
    {
      "code": "ASSESSMENT_REMOTE_PENDING",
      "message": "Selected facilities have pending remote assessments. You can proceed once the remote assessments are completed.",
      "params": ["fa-uuid-003"]
    }
  ]
}
```

```json
{
  "ResponseInfo": { "status": "failed" },
  "Errors": [
    {
      "code": "ASSESSMENT_ASSIGN_FIELD_INVALID",
      "message": "Assign for on-site requires remote Qualified or Not Qualified, on-site Not Initiated, and result Pending",
      "params": ["fa-uuid-004"]
    }
  ]
}
```

---

### 4.4 `POST /assessment/v1/plan/facility/decision/_bulk-update`

Bulk PM actions — same validation as §4.3 (Cases 1–9), array of facilities. Partial success supported.

**Request:**
```json
{
  "RequestInfo": { "apiId": "assessment-web", "authToken": "Bearer <token>" },
  "planId": "plan-uuid-001",
  "decisions": [
    { "planFacilityId": "fa-uuid-001", "assignForField": true },
    { "planFacilityId": "fa-uuid-002", "overallStatus": "ELIGIBLE" },
    {
      "planFacilityId": "fa-uuid-003",
      "overallStatus": "NOT_ELIGIBLE",
      "ineligibleReason": "Existing solar installation"
    }
  ]
}
```

**Response (200) — partial success:**
```json
{
  "ResponseInfo": { "status": "successful" },
  "success": [
    { "planFacilityId": "fa-uuid-001", "fieldStatus": "PENDING" },
    { "planFacilityId": "fa-uuid-002", "overallStatus": "ELIGIBLE", "overallManuallySet": true }
  ],
  "errors": [
    {
      "planFacilityId": "fa-uuid-003",
      "code": "ASSESSMENT_REMOTE_PENDING",
      "message": "Remote assessment still pending"
    }
  ]
}
```

---

### 4.5 `POST /assessment/v1/plan/facility/_bulk-include`

Wizard step 2 — grid multi-select. Validates §2.2.9 (LLD). Same-project eligible unassigned → **rejected** (R1). Cross-project include does **not** expire source `ELIGIBLE` rows (Rule 8).

**Request:**
```json
{
  "RequestInfo": { "apiId": "assessment-web", "authToken": "Bearer <token>" },
  "planId": "plan-uuid-002",
  "tenantId": "in",
  "facilityIds": ["fac-uuid-001", "fac-uuid-002"]
}
```

**Response (201):**
```json
{
  "ResponseInfo": { "status": "successful" },
  "created": [
    {
      "planFacilityId": "fa-uuid-101",
      "facilityId": "fac-uuid-001",
      "assessmentPlanId": "plan-uuid-002",
      "assessmentCompletionStatus": "ENROLLED",
      "phoneStatus": "PENDING",
      "overallStatus": "PENDING"
    }
  ],
  "errors": []
}
```

On success, each `created[]` row has `assessment_plan_id` set to target `planId`. Source `ELIGIBLE` rows in other projects remain unchanged (never `EXPIRED`).

**Errors (per facility in `errors[]`):**

| Code | HTTP | When |
|------|------|------|
| `ASSESSMENT_FACILITY_ALREADY_ON_PLAN` | 409 | Facility already on this assessment plan |
| `ASSESSMENT_FACILITY_ELIGIBLE_ACTIVE` | 400 | R1 — same-project eligible unassigned |
| `ASSESSMENT_PLAN_NOT_COMPLETE` | 400 | Source assessment plan not `CLOSED` (R0) |
| `ASSESSMENT_FACILITY_ONGOING` | 400 | R5 — `overall_status = PENDING` on source plan |
| `ASSESSMENT_FACILITY_NOT_ON_PROJECT` | 400 | Facility not linked to project |

---

### 4.6 `POST /assessment/v1/internal/plan/facility/_bulk-create`

Called by ingestion on optional include apply. **Same §2.2.9 validation as `_bulk-include`.** Internal only.

**Request:**
```json
{
  "RequestInfo": { "apiId": "ingestion-service", "authToken": "Bearer <service-token>" },
  "planId": "plan-uuid-001",
  "tenantId": "in",
  "facilities": [
    {
      "facilityId": "fac-uuid-001",
      "facilityCategory": "HEALTH",
      "facilityType": "PHC",
      "district": "Khunti",
      "block": "Murhu",
      "facilityName": "PHC Khunti"
    },
    {
      "facilityId": "fac-uuid-002",
      "facilityCategory": "ANGANWADI",
      "facilityType": "AWC",
      "district": "Ranchi",
      "block": "Kanke",
      "facilityName": "AWC Kanke"
    }
  ]
}
```

**Response (201):**
```json
{
  "ResponseInfo": { "status": "successful" },
  "created": [
    { "planFacilityId": "fa-uuid-001", "facilityId": "fac-uuid-001", "assessmentPlanId": "plan-uuid-001", "phoneStatus": "PENDING", "overallStatus": "PENDING", "assessmentCompletionStatus": "ENROLLED" },
    { "planFacilityId": "fa-uuid-002", "facilityId": "fac-uuid-002", "assessmentPlanId": "plan-uuid-001", "phoneStatus": "PENDING", "overallStatus": "PENDING" }
  ],
  "skipped": [],
  "plan": { "id": "plan-uuid-001", "healthFacilityCount": 2 }
}
```

**Errors:**

| Code | HTTP | When |
|------|------|------|
| `ASSESSMENT_FACILITY_ALREADY_ON_PLAN` | 409 | Duplicate facility on same plan |
| `ASSESSMENT_FACILITY_NOT_ON_PROJECT` | 400 | Facility not linked to project |

---

### 4.7 `POST /assessment/v1/internal/project/eligible-facilities/_search`

Project-level eligible pool for **multi-plan** field-plan ingestion. **Internal only.**

**Request:**
```json
{
  "RequestInfo": { "apiId": "ingestion-service", "authToken": "Bearer <service-token>" },
  "projectId": "proj-uuid-001",
  "tenantId": "in",
  "assessmentPlanIds": ["plan-A-uuid", "plan-B-uuid"]
}
```

**Response (200):**
```json
{
  "ResponseInfo": { "status": "successful" },
  "facilities": [
    {
      "planFacilityId": "fa-uuid-001",
      "assessmentPlanId": "plan-A-uuid",
      "assessmentPlanName": "Assessment Plan A",
      "facilityId": "fac-uuid-001",
      "facilityName": "PHC Khunti",
      "assessmentCompletionStatus": "ELIGIBLE",
      "installationFieldPlanId": null,
      "overallStatus": "ELIGIBLE"
    }
  ],
  "total": 83
}
```

Criteria: `assessment_completion_status = ELIGIBLE`, `installation_field_plan_id IS NULL`, parent assessment plan(s) completed.

---

### 4.8 `POST /assessment/v1/internal/plan/passed-facilities/_search` (deprecated)

Use `eligible-facilities/_search` with `assessmentPlanIds: [planId]`.

---

## 5. field-planner-service — Submissions

### 5.1 `POST /assessment/v1/submission/queue/_search`

Assessor work queue — scoped by logged-in assessor's plan assignment.

**Request (Remote Assessor):**
```json
{
  "RequestInfo": { "apiId": "assessment-mobile", "authToken": "Bearer <token>" },
  "assessmentPhase": "PHONE",
  "tenantId": "in"
}
```

**Request (Field POC):**
```json
{
  "RequestInfo": { "apiId": "assessment-mobile", "authToken": "Bearer <token>" },
  "assessmentPhase": "FIELD",
  "tenantId": "in"
}
```

**Response (200):**
```json
{
  "ResponseInfo": { "status": "successful" },
  "queue": [
    {
      "planFacilityId": "fa-uuid-001",
      "planId": "plan-uuid-001",
      "facilityId": "fac-uuid-001",
      "facilityName": "PHC Khunti",
      "facilityCategory": "HEALTH",
      "phoneStatus": "PENDING",
      "fieldStatus": null
    }
  ],
  "total": 15
}
```

Queue filters:
- **PHONE:** `phone_status` ∈ {`PENDING`, `PENDING_NO_ANSWER`, `PENDING_WRONG_NUMBER`}
- **FIELD:** `field_status = PENDING`

---

### 5.2 `POST /assessment/v1/submission/form/_resolve`

Resolve `formType` and return MDMS schema.

**Request:**
```json
{
  "RequestInfo": { "apiId": "assessment-mobile", "authToken": "Bearer <token>" },
  "planFacilityId": "fa-uuid-001",
  "facilityCategory": "HEALTH",
  "assessmentPhase": "PHONE"
}
```

**Response (200):**
```json
{
  "ResponseInfo": { "status": "successful" },
  "formType": "HF_PHONE",
  "schema": {
    "formType": "HF_PHONE",
    "fields": [
      {
        "fieldCode": "govtOwned",
        "label": "Is the health facility building owned by Government?",
        "type": "SELECT",
        "required": true,
        "options": ["YES", "NO"]
      },
      {
        "fieldCode": "existingSolar",
        "label": "Is there an existing solar system?",
        "type": "SELECT",
        "required": true,
        "options": ["YES", "NO"]
      }
    ]
  }
}
```

**Errors:**

| Code | HTTP | When |
|------|------|------|
| `ASSESSMENT_CATEGORY_MISMATCH` | 400 | `facilityCategory` does not match plan facility snapshot |
| `ASSESSMENT_FORM_NOT_AVAILABLE` | 404 | MDMS schema missing for derived `formType` |

---

### 5.3 `POST /assessment/v1/submission/phone/_create`

Remote form submit — immutable. OutcomeEngine sets phase qualification only; `overall_status` stays `PENDING`.

**Request:**
```json
{
  "RequestInfo": { "apiId": "assessment-mobile", "authToken": "Bearer <token>" },
  "planFacilityId": "fa-uuid-001",
  "facilityCategory": "HEALTH",
  "assessmentPhase": "PHONE",
  "submissionData": {
    "govtOwned": "YES",
    "existingSolar": "NO",
    "renovationPlanned": "NO"
  },
  "submittedByName": "Ramesh Kumar",
  "clientSubmissionTime": 1717000000000
}
```

**Response (201):**
```json
{
  "ResponseInfo": { "status": "successful" },
  "submission": {
    "id": "sub-phone-001",
    "planFacilityId": "fa-uuid-001",
    "assessmentPhase": "PHONE",
    "formType": "HF_PHONE",
    "outcome": "QUALIFIED",
    "serverReceivedTime": 1717000000100
  },
  "facility": {
    "planFacilityId": "fa-uuid-001",
    "phoneStatus": "QUALIFIED",
    "phoneOutcome": "QUALIFIED",
    "overallStatus": "PENDING"
  }
}
```

**Response (201) — NOT_QUALIFIED via MDMS rule:**
```json
{
  "ResponseInfo": { "status": "successful" },
  "submission": {
    "id": "sub-phone-002",
    "outcome": "NOT_QUALIFIED"
  },
  "facility": {
    "planFacilityId": "fa-uuid-005",
    "phoneStatus": "NOT_QUALIFIED",
    "phoneOutcome": "NOT_QUALIFIED",
    "overallStatus": "PENDING"
  }
}
```

**Errors:**

| Code | HTTP | When |
|------|------|------|
| `ASSESSMENT_DUPLICATE_PHONE_SUBMISSION` | 409 | Phone already submitted for this plan facility |
| `ASSESSMENT_INVALID_FORM_DATA` | 400 | Schema validation failed |
| `ASSESSMENT_UNAUTHORIZED_ASSESSOR` | 403 | User not assigned ENUMERATOR on plan |

```json
{
  "ResponseInfo": { "status": "failed" },
  "Errors": [
    {
      "code": "ASSESSMENT_INVALID_FORM_DATA",
      "message": "Required field missing: govtOwned",
      "params": ["govtOwned"]
    }
  ]
}
```

---

### 5.4 `POST /assessment/v1/submission/phone/_unable-to-contact`

Unable to contact — no submission row created. Sets remote pending sub-status; **Case 1** applies (on-site stays Not Initiated; PM cannot mark Eligible or Not Eligible).

**Request:**
```json
{
  "RequestInfo": { "apiId": "assessment-mobile", "authToken": "Bearer <token>" },
  "planFacilityId": "fa-uuid-006",
  "reason": "NO_ANSWER"
}
```

| `reason` | Sets `phone_status` |
|----------|---------------------|
| `WRONG_NUMBER` | `PENDING_WRONG_NUMBER` |
| `NO_ANSWER` | `PENDING_NO_ANSWER` |

**Response (200):**
```json
{
  "ResponseInfo": { "status": "successful" },
  "facility": {
    "planFacilityId": "fa-uuid-006",
    "phoneStatus": "PENDING_NO_ANSWER",
    "fieldStatus": null,
    "overallStatus": "PENDING",
    "allowedActions": {
      "assignForField": false,
      "markEligible": false,
      "markNotEligible": false
    }
  }
}
```

---

### 5.5 `POST /assessment/v1/submission/field/_create`

On-site form submit — immutable. After OutcomeEngine sets `field_status` / `fieldOutcome`, evaluates assessment result per §2.2.1:

| Outcome | When | Response flags |
|---------|------|----------------|
| Case 3 / 8 | Both phases `QUALIFIED`; `overallManuallySet` ≠ true | `overallStatus = ELIGIBLE`; `autoEligible = true` |
| Case 9 | Both phases `NOT_QUALIFIED`; `overallManuallySet` ≠ true | `overallStatus = NOT_ELIGIBLE`; `autoNotEligible = true` |
| Case 7 | `overallManuallySet = true` | Phase statuses updated; PM result unchanged; both flags `false` |
| Mixed / pending | One phase `QUALIFIED`, other `NOT_QUALIFIED` | `overallStatus` stays `PENDING`; both flags `false` — PM uses Cases 4 or 5 |

**Request:**
```json
{
  "RequestInfo": { "apiId": "assessment-mobile", "authToken": "Bearer <token>" },
  "planFacilityId": "fa-uuid-001",
  "facilityCategory": "HEALTH",
  "assessmentPhase": "FIELD",
  "submissionData": {
    "roofCondition": "GOOD",
    "shadingIssues": "NO"
  },
  "clientSubmissionTime": 1717100000000
}
```

**Response (201) — Cases 3 & 8 auto-eligible (both phases `QUALIFIED`):**
```json
{
  "ResponseInfo": { "status": "successful" },
  "submission": {
    "id": "sub-field-001",
    "assessmentPhase": "FIELD",
    "formType": "HF_FIELD",
    "outcome": "QUALIFIED"
  },
  "facility": {
    "planFacilityId": "fa-uuid-001",
    "phoneStatus": "QUALIFIED",
    "fieldStatus": "QUALIFIED",
    "fieldOutcome": "QUALIFIED",
    "overallStatus": "ELIGIBLE",
    "assessmentCompletionStatus": "ELIGIBLE",
    "overallManuallySet": false
  },
  "autoEligible": true,
  "autoNotEligible": false
}
```

**Response (201) — Case 9 auto-not-eligible (both phases `NOT_QUALIFIED`):**
```json
{
  "ResponseInfo": { "status": "successful" },
  "submission": {
    "id": "sub-field-003",
    "assessmentPhase": "FIELD",
    "formType": "HF_FIELD",
    "outcome": "NOT_QUALIFIED"
  },
  "facility": {
    "planFacilityId": "fa-uuid-008",
    "phoneStatus": "NOT_QUALIFIED",
    "fieldStatus": "NOT_QUALIFIED",
    "fieldOutcome": "NOT_QUALIFIED",
    "overallStatus": "NOT_ELIGIBLE",
    "assessmentCompletionStatus": "NOT_ELIGIBLE",
    "overallManuallySet": false
  },
  "autoEligible": false,
  "autoNotEligible": true
}
```

**Response (201) — Mixed outcomes (result stays `PENDING` for PM Cases 4 or 5):**
```json
{
  "ResponseInfo": { "status": "successful" },
  "submission": {
    "id": "sub-field-002",
    "outcome": "NOT_QUALIFIED"
  },
  "facility": {
    "planFacilityId": "fa-uuid-007",
    "phoneStatus": "QUALIFIED",
    "fieldStatus": "NOT_QUALIFIED",
    "overallStatus": "PENDING"
  },
  "autoEligible": false,
  "autoNotEligible": false
}
```

**Response (201) — Case 7 (PM result preserved; `overallManuallySet = true`):**
```json
{
  "ResponseInfo": { "status": "successful" },
  "submission": {
    "id": "sub-field-004",
    "outcome": "QUALIFIED"
  },
  "facility": {
    "planFacilityId": "fa-uuid-011",
    "phoneStatus": "QUALIFIED",
    "fieldStatus": "QUALIFIED",
    "overallStatus": "ELIGIBLE",
    "overallManuallySet": true
  },
  "autoEligible": false,
  "autoNotEligible": false
}
```

**Errors:**

| Code | HTTP | When |
|------|------|------|
| `ASSESSMENT_FIELD_NOT_PENDING` | 400 | `field_status` ≠ `PENDING` |
| `ASSESSMENT_DUPLICATE_FIELD_SUBMISSION` | 409 | On-site already submitted |
| `ASSESSMENT_UNAUTHORIZED_ASSESSOR` | 403 | User not assigned FIELD_POC on plan |

---

### 5.6 `POST /assessment/v1/submission/_search` (optional)

PM read-only submission search. Prefer `plan/facility/_detail` for detail screen.

**Request:**
```json
{
  "RequestInfo": { "apiId": "assessment-web", "authToken": "Bearer <token>" },
  "criteria": {
    "planId": "plan-uuid-001",
    "facilityId": "fac-uuid-001",
    "assessmentPhase": "PHONE"
  }
}
```

**Response (200):**
```json
{
  "ResponseInfo": { "status": "successful" },
  "submissions": [
    {
      "id": "sub-phone-001",
      "planFacilityId": "fa-uuid-001",
      "assessmentPhase": "PHONE",
      "formType": "HF_PHONE",
      "outcome": "QUALIFIED",
      "submissionData": { "govtOwned": "YES" },
      "submittedByName": "Ramesh Kumar",
      "serverReceivedTime": 1717000000100
    }
  ]
}
```

---

## 6. ingestion-service — Assessment plan include (optional Excel)

> **Primary wizard path:** `POST /assessment/v1/plan/facility/_bulk-include` (grid multi-select — §4.5).  
> The endpoints below are **optional** bulk include via Excel (§4.2 LLD).

### 6.1 `POST /ingestion-service/template/assessmentPlanIncludeTemplate`

Download optional include Excel (alternative to wizard grid).

**Request:**
```json
{
  "RequestInfo": { "apiId": "assessment-web", "authToken": "Bearer <token>" },
  "projectId": "proj-uuid-001",
  "planId": "plan-uuid-001",
  "tenantId": "in"
}
```

**Response (200):** Binary `.xlsx` file (Content-Type: `application/vnd.openxmlformats-officedocument.spreadsheetml.sheet`)

Columns include: facility id, name, category, type, district, block, **Include in Assessment Plan (Yes/No)**.

---

### 6.2 `POST /ingestion-service/ingest/assessmentPlanIncludeValidateData`

**Request:** Multipart or job payload per existing ingestion pattern:
```json
{
  "RequestInfo": { "apiId": "assessment-web", "authToken": "Bearer <token>" },
  "jobId": "job-uuid-001",
  "planId": "plan-uuid-001",
  "projectId": "proj-uuid-001",
  "fileStoreId": "file-store-uuid-001"
}
```

**Response (200):**
```json
{
  "ResponseInfo": { "status": "successful" },
  "validation": {
    "totalRows": 150,
    "validRows": 148,
    "errorRows": 2,
    "errors": [
      {
        "row": 12,
        "facilityId": "fac-unknown",
        "code": "ASSESSMENT_FACILITY_NOT_ON_PROJECT",
        "message": "Facility is not linked to this project"
      },
      {
        "row": 45,
        "facilityId": "fac-uuid-099",
        "code": "ASSESSMENT_FACILITY_ALREADY_ON_PLAN",
        "message": "Facility already included in this assessment plan"
      }
    ]
  }
}
```

---

### 6.3 `POST /ingestion-service/ingest/assessmentPlanIncludeApply`

**Request:**
```json
{
  "RequestInfo": { "apiId": "assessment-web", "authToken": "Bearer <token>" },
  "jobId": "job-uuid-001",
  "planId": "plan-uuid-001",
  "projectId": "proj-uuid-001",
  "fileStoreId": "file-store-uuid-001"
}
```

**Response (200):**
```json
{
  "ResponseInfo": { "status": "successful" },
  "result": {
    "includedCount": 142,
    "skippedCount": 8,
    "planId": "plan-uuid-001"
  }
}
```

Internally calls `POST /assessment/v1/internal/plan/facility/_bulk-create` for **Yes** rows only.

---

## 7. ingestion-service — Plan facility export

### 7.1 `POST /ingestion-service/template/assessmentPlanFacilityExport`

Read-only Excel export of plan facility grid.

**Request:**
```json
{
  "RequestInfo": { "apiId": "assessment-web", "authToken": "Bearer <token>" },
  "planId": "plan-uuid-001",
  "filters": {
    "district": "Khunti",
    "phoneStatus": "QUALIFIED",
    "overallStatus": "PENDING"
  }
}
```

Internally calls `plan/facility/_search` with `exportAll=true`, `includeResponseSummary=true`.

**Response (200):** Binary `.xlsx` file.

---

## 8. ingestion-service — Field plan facility ingestion (extended)

### 8.1 `POST /ingestion-service/template/fieldplanFacilityIngestionTemplate`

When `assessmentPlanIds[]` is present, row source = **eligible unassigned** `planFacilityId`s from `eligible-facilities/_search`. Supports **multi-plan** and **partial handoff** (subset of eligibles).

**Request:**
```json
{
  "RequestInfo": { "apiId": "assessment-web", "authToken": "Bearer <token>" },
  "project_id": "proj-uuid-001",
  "fieldplan_id": "fp-uuid-001",
  "assessmentPlanIds": ["plan-A-uuid", "plan-B-uuid"],
  "tenantId": "in",
  "boundary_data": {}
}
```

**Response (200):** Binary `.xlsx` with **Solution Design Type** as editable dropdown (MDMS `solar_solution_design_type`).

---

### 8.2 `POST /ingestion-service/ingest/fieldPlanfacilitiesValidateData`

**Request:**
```json
{
  "RequestInfo": { "apiId": "assessment-web", "authToken": "Bearer <token>" },
  "jobId": "job-uuid-002",
  "fieldplan_id": "fp-uuid-001",
  "assessmentPlanIds": ["plan-A-uuid"],
  "fileStoreId": "file-store-uuid-002"
}
```

Excel rows must include **`planFacilityId`** for validation. On apply, assessment rows update to `MOVED_TO_FIELD_PLAN`.

**Response (200) — validation errors:**
```json
{
  "ResponseInfo": { "status": "successful" },
  "validation": {
    "totalRows": 40,
    "validRows": 37,
    "errorRows": 3,
    "errors": [
      {
        "row": 5,
        "planFacilityId": "fa-uuid-050",
        "code": "ASSESSMENT_FACILITY_NOT_ELIGIBLE",
        "message": "planFacilityId not ELIGIBLE or already handed off"
      },
      {
        "row": 18,
        "facilityId": "fac-uuid-061",
        "code": "ASSESSMENT_INVALID_SOLUTION_DESIGN_TYPE",
        "message": "Solution Design Type must be a valid MDMS value"
      }
    ]
  }
}
```

---

### 8.3 `POST /ingestion-service/ingest/createFieldPlanFacility`

**Request:**
```json
{
  "RequestInfo": { "apiId": "assessment-web", "authToken": "Bearer <token>" },
  "jobId": "job-uuid-002",
  "fieldplan_id": "fp-uuid-001",
  "assessmentPlanIds": ["plan-A-uuid"],
  "fileStoreId": "file-store-uuid-002"
}
```

Excel rows must include **`planFacilityId`** for validation. On apply:

- Creates `field_plan_facilities` with `source_plan_facility_id` = Excel `planFacilityId`
- Sets `additional_details.assessmentSource` = `{ assessmentPlanId, planFacilityId }` (required)
- For **same-project** handoff from eligible pool: updates assessment row → `MOVED_TO_FIELD_PLAN` + `installation_field_plan_id`
- For **cross-project Proj_2** include (§2.2.9 Rules 3, 6): may reuse Proj_1 eligibility without expiring source row; source AP_1 row stays `ELIGIBLE` even if Proj_1 installation is ongoing

**Response (200):**
```json
{
  "ResponseInfo": { "status": "successful" },
  "result": {
    "createdCount": 37,
    "skippedCount": 0,
    "fieldPlanId": "fp-uuid-001"
  }
}
```

Re-verifies each `planFacilityId` against §2.2.9; then field-planner `facility/_create`. Same-project eligible pool picks require `ELIGIBLE` and `installation_field_plan_id IS NULL`.

---

## 9. Error code reference

| Code | HTTP | Endpoint(s) | Description |
|------|------|-------------|-------------|
| `ASSESSMENT_PLAN_NOT_FOUND` | 404 | plan APIs | Invalid `planId` |
| `ASSESSMENT_PLAN_FACILITY_NOT_FOUND` | 404 | facility/decision/submission | Invalid `planFacilityId` |
| `ASSESSMENT_PLAN_NAME_DUPLICATE` | 400 | `plan/_create` | Duplicate plan name in project |
| `ASSESSMENT_PROJECT_NOT_FOUND` | 404 | `plan/_create` | Invalid `projectId` |
| `ASSESSMENT_REMOTE_PENDING` | 400 | `decision/_update`, `decision/_bulk-update` | Case 1 — Mark eligible or Mark not eligible blocked while remote pending |
| `ASSESSMENT_ASSIGN_FIELD_INVALID` | 400 | `decision/_update`, `decision/_bulk-update` | Case 1 / §2.2.2 — assign preconditions failed |
| `ASSESSMENT_RESULT_ALREADY_SET` | 400 | `decision/_update`, `decision/_bulk-update` | Case 6 — assign blocked when result already final |
| `ASSESSMENT_INELIGIBLE_REASON_REQUIRED` | 400 | `decision/_update`, `decision/_bulk-update` | Case 5 / Case 8 — `NOT_ELIGIBLE` without `ineligibleReason` |
| `ASSESSMENT_DUPLICATE_PHONE_SUBMISSION` | 409 | `phone/_create` | Phone already submitted |
| `ASSESSMENT_DUPLICATE_FIELD_SUBMISSION` | 409 | `field/_create` | On-site already submitted |
| `ASSESSMENT_FIELD_NOT_PENDING` | 400 | `field/_create` | On-site submit without PM assign |
| `ASSESSMENT_INVALID_FORM_DATA` | 400 | `phone/_create`, `field/_create` | Schema validation failure |
| `ASSESSMENT_CATEGORY_MISMATCH` | 400 | `form/_resolve`, submissions | Category does not match snapshot |
| `ASSESSMENT_UNAUTHORIZED_ASSESSOR` | 403 | submission APIs | Wrong assessor role for plan |
| `ASSESSMENT_FACILITY_NOT_ON_PROJECT` | 400 | include validate/apply | Facility not on project |
| `ASSESSMENT_ELIGIBLE_REASON_REQUIRED` | 400 | `decision/_update`, `decision/_bulk-update` | Case 9 — both outcomes `NOT_QUALIFIED`, mark Eligible without `eligibleReason` |
| `ASSESSMENT_FACILITY_ELIGIBLE_ACTIVE` | 400 | `_bulk-include` | R1 — same-project eligible unassigned |
| `ASSESSMENT_PLAN_NOT_COMPLETE` | 400 | `_bulk-include`, project link | Source assessment plan not `CLOSED` (R0) |
| `FIELD_PLAN_NOT_COMPLETE` | 400 | same-project reuse paths | Source installation FP not `CLOSED` (R0, same-project only) |
| `ASSESSMENT_FACILITY_ONGOING` | 400 | `_bulk-include`, project link | R5 — `overall_status = PENDING` |
| `ASSESSMENT_PLAN_HAS_PENDING_FACILITIES` | 400 | `plan/_mark-complete` | Cannot close plan |
| `ASSESSMENT_FACILITY_ALREADY_ON_PLAN` | 409 | `_bulk-include`, `_bulk-create` | Duplicate on same assessment plan |
| `ASSESSMENT_FACILITY_NOT_ELIGIBLE` | 400 | field plan validate/apply | `planFacilityId` not pickable |
| `ASSESSMENT_FACILITY_ALREADY_ON_FIELD_PLAN` | 400 | field plan apply | Already `MOVED_TO_FIELD_PLAN` |
| `ASSESSMENT_INVALID_SOLUTION_DESIGN_TYPE` | 400 | field plan validate | Value not in MDMS master |
| `ASSESSMENT_ASSESSOR_NOT_FOUND` | 400 | `plan/_update` | Email not in HRMS |
| `ASSESSMENT_FORM_NOT_AVAILABLE` | 404 | `form/_resolve` | MDMS schema missing |

---

## 10. Related documents

| Document | Purpose |
|----------|---------|
| `ASSESSMENT_MODULE_LLD 1.md` | Full LLD — workflows, data model, validation |
| `ASSESSMENT_MODULE_SEQUENCE_DIAGRAMS.md` | Sequence diagrams |
| `ASSESSMENT_BUSINESS_SERVICE.md` | Optional workflow-v2 state machine |
