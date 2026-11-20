# Workflow change: `ASSIGN_NEW_VENDOR` on the `OUT_OF_SCOPE` state

Business services: `Incident_High`, `Incident_Medium`, `Incident_Low` — **every tenant**.

## Why

An out-of-scope ticket assigned by the state SPOC must land in a different state depending on the
vendor it goes to:

| Assignment | Resulting state |
|---|---|
| **same** vendor as before | `PENDING_RESOLUTION_OUT_OF_SCOPE` (existing `ASSIGN` action, unchanged) |
| **new** vendor | `PENDINGRESOLUTION` (**new** `ASSIGN_NEW_VENDOR` action) |

`egov-workflow-v2` resolves the next state from a static `action.nextState` column and forbids two
actions of the same name on one state, so the two outcomes must be two distinct action codes.
im-services compares the vendor organisations and emits the right one — see
`WorkflowService.resolveOutOfScopeAssignAction`.

The new action carries **`roles: ["SYSTEM"]`** on purpose: `nextActions` is filtered by the caller's
roles, so the action never appears in the SPOC's "Take Action" menu and no client can send it. Only
im-services emits it, adding the `SYSTEM` role for the duration of the transition call.

## Action to add

On the `OUT_OF_SCOPE` state, **append** this action. Do not touch the existing `ASSIGN` action.

```json
{
  "tenantId": "<TENANT>",
  "currentState": "<uuid of the OUT_OF_SCOPE state>",
  "action": "ASSIGN_NEW_VENDOR",
  "nextState": "<uuid of the PENDINGRESOLUTION state, same business service and tenant>",
  "roles": ["SYSTEM"],
  "active": true
}
```

> The state uuids differ per tenant **and** per business service. Always read them from the document
> you just fetched — never copy a uuid from another environment.

## Procedure

For each tenant × each of the three business services:

```bash
export WF_HOST=...        # e.g. https://<env>/egov-workflow-v2
export TOKEN=...          # auth token of a user allowed to update business services
export TENANT=in
export SERVICE=Incident_High
```

1. Fetch the current definition:

```bash
curl -s -X POST "$WF_HOST/egov-wf/businessservice/_search?tenantId=$TENANT&businessServices=$SERVICE" \
  -H 'Content-Type: application/json' \
  -d "{\"RequestInfo\":{\"authToken\":\"$TOKEN\"}}" > bs.json
```

2. Read the two uuids you need:

```bash
jq -r '.BusinessServices[0].states[] | select(.state=="OUT_OF_SCOPE" or .state=="PENDINGRESOLUTION") | "\(.state) \(.uuid)"' bs.json
```

3. Check the action is not already there (the operation must stay idempotent):

```bash
jq -r '.BusinessServices[0].states[] | select(.state=="OUT_OF_SCOPE") | .actions[].action' bs.json
```

4. Append the action into that state's `actions` array, then send the **whole** document back:

```bash
curl -s -X POST "$WF_HOST/egov-wf/businessservice/_update" \
  -H 'Content-Type: application/json' \
  -d "{\"RequestInfo\":{\"authToken\":\"$TOKEN\"},\"BusinessServices\":[$(jq -c '.BusinessServices[0]' bs.json)]}"
```

## Verification

```sql
SELECT bs.businessservice, s.tenantid, s.state, a.action, a.roles, ns.state AS next_state
FROM eg_wf_action_v2 a
JOIN eg_wf_state_v2 s ON s.uuid = a.currentstate
JOIN eg_wf_businessservice_v2 bs ON bs.uuid = s.businessserviceid
LEFT JOIN eg_wf_state_v2 ns ON ns.uuid = a.nextstate
WHERE s.state = 'OUT_OF_SCOPE'
ORDER BY bs.businessservice, s.tenantid, a.action;
```

Expected, per (tenant, business service) — exactly two rows:

| action | roles | next_state |
|---|---|---|
| `ASSIGN` | `COMPLAINT_FACILITATOR_1` | `PENDING_RESOLUTION_OUT_OF_SCOPE` |
| `ASSIGN_NEW_VENDOR` | `SYSTEM` | `PENDINGRESOLUTION` |

## Order of operations

The im-services feature is behind a flag, **off by default**:

```properties
im.out.of.scope.new.vendor.transition.enabled=false
```

1. Deploy im-services with the flag **off** (it also needs `egov.vendor.host` pointing at
   vendor-registry — a new service dependency for this module).
2. Apply this config change and run the verification query **on every tenant**.
3. Turn the flag on.

If the flag is turned on while a tenant is still missing the action, the SPOC's assignment fails with
`INVALID ACTION` (HTTP 400). Rollback is to switch the flag back off; the extra action then sits
inert, since nothing else can emit it.
