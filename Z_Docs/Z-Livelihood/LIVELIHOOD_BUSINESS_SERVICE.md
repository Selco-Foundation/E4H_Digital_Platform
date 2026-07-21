# Livelihood — Business Service (`LivelihoodIncident`)

**Payload file:** `LIVELIHOOD_BUSINESS_SERVICE.json` (same shape as AMC / other business service defs — `BusinessServices` only)  
**Default tenant:** `livelihood` (set on deploy API / MDMS, not repeated in JSON)  
**Source:** `LIVELIHOOD_LLD_WORKFLOW_AND_SLA_CONCISE.md`  
**Service:** `egov-workflow-v2` / `im-services`  
**Not in scope:** AMC (`amc-visit`) — omitted from Livelihood Phase 1

---

## Deploy

1. Wrap `LIVELIHOOD_BUSINESS_SERVICE.json` in a `RequestInfo` + `BusinessServices` body (or load via MDMS / config pipeline used for other business services).
2. POST to workflow for tenant **`livelihood`**:

   `POST /egov-workflow-v2/egov-wf/businessservice/_create?tenantId=livelihood`

3. Persister topic `save-wf-businessservice` writes to `eg_wf_businessservice_v2`, `eg_wf_state_v2`, `eg_wf_action_v2`.
4. Configure **MDMS `Workflow.AutoEscalation`** separately (breach side-effects — see § MDMS below).
5. Add `LivelihoodIncident` to `automation-cronjob` `business_services` for tenant `livelihood`.

---

## State machine summary

| State | SLA (ms) | SLA (human) | Assignee |
| --- | ---: | --- | --- |
| *(start / null)* | — | — | — |
| `PENDING_FOR_RESOLUTION` | 604800000 | 7 days | Vendor |
| `OUT_OF_SCOPE_PENDING_POC` | 259200000 | 3 days | Program POC |
| `OUT_OF_SCOPE_PENDING_VENDOR` | 604800000 | 7 days | Vendor |
| `OUT_OF_WARRANTY_PENDING_VENDOR` | 1209600000 | 14 days | Vendor |
| `RESOLVED` | 259200000 | 72 hours | — |
| `CLOSED_AFTER_RESOLUTION` | — | terminal | — |
| `CLOSED_AFTER_DECLINE` | — | terminal | — |

`businessServiceSla` is `null` (same as AMC sample); **per-state `sla`** drives workflow timers and escalation.

---

## Actions by role

| Action | Roles | From → To |
| --- | --- | --- |
| `AUTO_ASSIGN` | `SYSTEM_USER` | start → `PENDING_FOR_RESOLUTION` |
| `CREATE` | `COMPLAINANT`, `LIVELIHOOD_POC`, `SYSTEM_USER` | start → `PENDING_FOR_RESOLUTION` (fallback if create does not use `AUTO_ASSIGN`) |
| `RESOLVE` | `LIVELIHOOD_VENDOR`, `COMPLAINT_RESOLVER` | vendor states → `RESOLVED` |
| `OUT_OF_SCOPE` | vendor roles | `PENDING_FOR_RESOLUTION` → `OUT_OF_SCOPE_PENDING_POC` |
| `OUT_OF_WARRANTY` | vendor roles | `PENDING_FOR_RESOLUTION` → `OUT_OF_WARRANTY_PENDING_VENDOR` |
| `REASSIGN` / `ASSIGN_VENDOR` | `LIVELIHOOD_POC` | `OUT_OF_SCOPE_PENDING_POC` → `OUT_OF_SCOPE_PENDING_VENDOR` |
| `DECLINE_POC` | `LIVELIHOOD_POC` | `OUT_OF_SCOPE_PENDING_POC` → `CLOSED_AFTER_DECLINE` |
| `DECLINE` | vendor roles | `OUT_OF_WARRANTY_PENDING_VENDOR` → `CLOSED_AFTER_DECLINE` |
| `REOPEN` | `COMPLAINANT` | `RESOLVED` → `PENDING_FOR_RESOLUTION` |
| `AUTO_CLOSE` | `SYSTEM_USER` | `RESOLVED` → `CLOSED_AFTER_RESOLUTION` |

**im-services create path (target):** after vendor is resolved from `assetId`, call workflow with `action: AUTO_ASSIGN` and assignee = vendor user UUID.

**72h reopen:** enforced in `im-services` (not in workflow JSON); `REOPEN` only valid within window.

---

## MDMS AutoEscalation (not in business service JSON)

Configure in MDMS module `Workflow.AutoEscalation` for `businessService: LivelihoodIncident`:

| status | threshold | action |
| --- | --- | --- |
| `PENDING_FOR_RESOLUTION` | 7d | `ESCALATE_TO_POC` |
| `OUT_OF_SCOPE_PENDING_POC` | 3d | `REMIND_POC` |
| `OUT_OF_WARRANTY_PENDING_VENDOR` | 7d | `REMIND_END_USER_AND_VENDOR` |
| `OUT_OF_WARRANTY_PENDING_VENDOR` | 12d | `REMIND_END_USER_AND_VENDOR` |
| `OUT_OF_WARRANTY_PENDING_VENDOR` | 14d | `REMIND_VENDOR` |
| `RESOLVED` | 72h | `AUTO_CLOSE` |

Cron: `POST /egov-wf/auto/LivelihoodIncident/_escalate` → Kafka `im-auto-escalation` → extend `im-services` `NotificationConsumer` for Livelihood (no blind E4H `CLOSE`).

---

## HRMS / org roles to provision

| Role code | Actor |
| --- | --- |
| `COMPLAINANT` | Facility manager |
| `LIVELIHOOD_POC` | Program POC |
| `LIVELIHOOD_VENDOR` | Vendor org user (preferred Livelihood name) |
| `COMPLAINT_RESOLVER` | Vendor (E4H alias — included for reuse) |
| `SYSTEM_USER` | Cron and service account (same convention as AMC) |

---

## Open points (from LLD)

- Prefer single `AUTO_ASSIGN` on create vs also exposing `CREATE` on start state.
- `ASSIGN_VENDOR` vs `REASSIGN` — same `nextState`; distinction may be assignee payload only in `im-services`.
- State-level tenant: use `livelihood` or state-level tenant (e.g. `ka`) per deployment strategy.
