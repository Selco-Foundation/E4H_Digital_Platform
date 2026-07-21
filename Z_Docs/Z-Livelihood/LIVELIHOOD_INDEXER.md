# Livelihood Platform — Indexer Configuration

**Purpose:** `egov-indexer` YAML for Livelihood incident search/inbox.  
**Related:** `LIVELIHOOD_LLD_WORKFLOW_AND_SLA_CONCISE.md` (workflow), `LIVELIHOOD_PLATFORM_CHANGES_CONCISE.md` (services).  
**Tenant:** `livelihood` (single-tenant fork).

Note: Livelihood indexer yaml most takes inspiration from E4H indexer yaml

---

## 1. Indexing working

```text
im-services (_create / _update)
    → Kafka: save-im-request-indexer | update-im-request-indexer
        → egov-indexer (reads YAML below)
            → Elasticsearch: livelihood-incident-index-v1
                → inbox / egov-searcher (POC & vendor lists)
```

`im-services` publishes an `**IncidentRequestWrapper**` (not a bare incident):


| Kafka field              | Content                                                                        |
| ------------------------ | ------------------------------------------------------------------------------ |
| `incidentRequest`        | `Incident` + `Workflow` + `RequestInfo`                                        |
| `indexView`              | Enriched display fields (vendor name, boundary, localized labels, SLA helpers) |
| `updatedProcessInstance` | Current workflow state, assignee, SLA remaining                                |


Audit transitions use topic `**save-im-audit-request-indexer**` (same wrapper shape; `indexView` carries `startingStatus` / `endingStatus`).

---

## 2. Kafka topics & indexes


| Topic                           | Trigger                           | ES index                       | Document id      |
| ------------------------------- | --------------------------------- | ------------------------------ | ---------------- |
| `save-im-request-indexer`       | Incident create                   | `livelihood-incident-index-v1` | `incidentId`     |
| `update-im-request-indexer`     | Incident update / workflow action | `livelihood-incident-index-v1` | `incidentId`     |
| `save-im-audit-request-indexer` | Status transition audit           | `livelihood-incident-audit-v1` | `indexView.uuid` |


Topic names match `im-services` `application.properties` (reuse E4H pattern in the Livelihood fork).

---

## 3. Livelihood-specific indexed fields

Add these on `**Incident**` (DB + API + index enrichment) — required for Livelihood inbox filters:


| Field             | ES path (under `Data.incident`) | Used for                                     |
| ----------------- | ------------------------------- | -------------------------------------------- |
| `assetId`         | `assetId.keyword`               | Asset-level tickets; vendor derivation audit |
| `createdOnBehalf` | `createdOnBehalf`               | POC on-behalf tickets                        |
| `entryChannel`    | `entryChannel.keyword`          | `DIRECT` / `POC_MANUAL` / `IVR_WHATSAPP`     |
| `facilityId`      | `facilityId.keyword`            | Manager / POC facility scope                 |
| `boundaryCode`    | `boundaryCode.keyword`          | Manager jurisdiction (Option 1)              |


Workflow business service in index: `**LivelihoodIncident**` → `Data.currentProcessInstance.businessService.keyword`.

POC state filter uses boundary enrichment (same as E4H inbox):  
`Data.incident.boundary.stateCode.keyword`, `districtCode`, `blockCode`.

---

## 4. Indexer YAML — incidents (`livelihood-im-indexer.yml`)

Deploy to config repo: `egov-indexer/livelihood-im-indexer.yml`.  
Add path to helm `egov-indexer-yaml-repo-path`; restart `egov-indexer`.

```yaml
ServiceMaps:
  serviceName: Livelihood IM
  version: 1.0.0
  mappings:
    - topic: save-im-request-indexer
      configKey: INDEX
      indexes:
        - name: livelihood-incident-index-v1
          type: general
          id: $.incidentRequest.incident.incidentId
          jsonPath: $
          timeStampField: $.incidentRequest.incident.auditDetails.lastModifiedTime
          customJsonMapping:
            indexMapping:
              Data:
                incident: {}
                indexView: {}
                currentProcessInstance: {}
                history: []
                slaRemaining: null
                stateSla: null
                totalSlaRemaining: null
                filedDate: null
            fieldMapping:
              - inJsonPath: $.incidentRequest.incident
                outJsonPath: $.Data.incident
              - inJsonPath: $.indexView
                outJsonPath: $.Data.indexView
              - inJsonPath: $.updatedProcessInstance
                outJsonPath: $.Data.currentProcessInstance
              - inJsonPath: $.updatedProcessInstance.state.sla
                outJsonPath: $.Data.stateSla
              - inJsonPath: $.updatedProcessInstance.state.slaRemaining
                outJsonPath: $.Data.slaRemaining
              - inJsonPath: $.updatedProcessInstance.state.totalSlaRemaining
                outJsonPath: $.Data.totalSlaRemaining
              - inJsonPath: $.incidentRequest.incident.filedDate
                outJsonPath: $.Data.filedDate
            externalUriMapping:
              - path: http://egov-workflow-v2.egov:8080/egov-workflow-v2/egov-wf/process/_search
                queryParam: businessIds=$.incidentRequest.incident.incidentId,history=true,tenantId=$.incidentRequest.incident.tenantId
                apiRequest:
                  RequestInfo:
                    apiId: livelihood-im
                    ver: "1.0"
                    authToken: "SYSTEM_TOKEN"
                uriResponseMapping:
                  - inJsonPath: $.ProcessInstances
                    outJsonPath: $.Data.history

    - topic: update-im-request-indexer
      configKey: INDEX
      indexes:
        - name: livelihood-incident-index-v1
          type: general
          id: $.incidentRequest.incident.incidentId
          jsonPath: $
          timeStampField: $.incidentRequest.incident.auditDetails.lastModifiedTime
          customJsonMapping:
            indexMapping:
              Data:
                incident: {}
                indexView: {}
                currentProcessInstance: {}
                history: []
                slaRemaining: null
                stateSla: null
                totalSlaRemaining: null
                filedDate: null
            fieldMapping:
              - inJsonPath: $.incidentRequest.incident
                outJsonPath: $.Data.incident
              - inJsonPath: $.indexView
                outJsonPath: $.Data.indexView
              - inJsonPath: $.updatedProcessInstance
                outJsonPath: $.Data.currentProcessInstance
              - inJsonPath: $.updatedProcessInstance.state.sla
                outJsonPath: $.Data.stateSla
              - inJsonPath: $.updatedProcessInstance.state.slaRemaining
                outJsonPath: $.Data.slaRemaining
              - inJsonPath: $.updatedProcessInstance.state.totalSlaRemaining
                outJsonPath: $.Data.totalSlaRemaining
              - inJsonPath: $.incidentRequest.incident.filedDate
                outJsonPath: $.Data.filedDate
            externalUriMapping:
              - path: http://egov-workflow-v2.egov:8080/egov-workflow-v2/egov-wf/process/_search
                queryParam: businessIds=$.incidentRequest.incident.incidentId,history=true,tenantId=$.incidentRequest.incident.tenantId
                apiRequest:
                  RequestInfo:
                    apiId: livelihood-im
                    ver: "1.0"
                    authToken: "SYSTEM_TOKEN"
                uriResponseMapping:
                  - inJsonPath: $.ProcessInstances
                    outJsonPath: $.Data.history
```

**Notes:**

- Replace `egov-workflow-v2.egov:8080` and `SYSTEM_TOKEN` with Livelihood env values.
- `indexView.boundary` is set by `im-services` `EnrichmentService` before publish — POC state filters rely on it.
- `indexView.mappedVendorName` / `mappedVendorUserName` support vendor inbox display (existing E4H enrichment).

---

## 5. Indexer YAML — audit trail (`livelihood-im-audit-indexer.yml`)

Optional but recommended for transition history / analytics (E4H publishes on every create/update).

```yaml
ServiceMaps:
  serviceName: Livelihood IM Audit
  version: 1.0.0
  mappings:
    - topic: save-im-audit-request-indexer
      configKey: INDEX
      indexes:
        - name: livelihood-incident-audit-v1
          type: general
          id: $.indexView.uuid
          jsonPath: $
          timeStampField: $.incidentRequest.incident.auditDetails.lastModifiedTime
          customJsonMapping:
            indexMapping:
              Data:
                incident: {}
                indexView: {}
                currentProcessInstance: {}
            fieldMapping:
              - inJsonPath: $.incidentRequest.incident
                outJsonPath: $.Data.incident
              - inJsonPath: $.indexView
                outJsonPath: $.Data.indexView
              - inJsonPath: $.updatedProcessInstance
                outJsonPath: $.Data.currentProcessInstance
```

Audit docs capture `indexView.startingStatus`, `indexView.endingStatus`, and `indexView.documentUrls` (quotation links).

---

## 6. Inbox / search filters (what the index must support)


| Actor            | Typical ES filter | Field                                                                        |
| ---------------- | ----------------- | ---------------------------------------------------------------------------- |
| Vendor           | Assigned tickets  | `Data.currentProcessInstance.assignes.uuid.keyword`                          |
| Facility manager | Own facility      | `Data.incident.facilityId.keyword` or boundary match                         |
| Program POC      | State scope       | `Data.incident.boundary.stateCode.keyword`                                   |
| All              | Business service  | `Data.currentProcessInstance.businessService.keyword` = `LivelihoodIncident` |
| All              | Status            | `Data.incident.applicationStatus.keyword`                                    |
| SLA dashboards   | Nearing breach    | `Data.slaRemaining`, `Data.stateSla`                                         |


Configure matching **InboxConfiguration** in MDMS (`inbox-v2`) for module e.g. `livelihood-im` pointing at index `livelihood-incident-index-v1`.

---

## 7. Deployment checklist

1. Add `assetId`, `createdOnBehalf`, `entryChannel` to incident model + `EnrichmentService` (if not already present).
2. Ensure workflow business service name is `**LivelihoodIncident`** in published `updatedProcessInstance`.
3. Check in YAML files under `egov-indexer/` in config repo.
4. Add both file paths to `egov-indexer-yaml-repo-path` in Livelihood env helm values.
5. Restart `**egov-indexer**`; verify documents appear in `livelihood-incident-index-v1`.
6. If using DIGIT inbox UI: add MDMS inbox module config + restart `**inbox**`.

---

## 8. Open items

1. Confirm whether Livelihood reuses E4H index `health-facility-index` or uses separate indexes only (recommended: **separate** for clean tenant isolation).
2. Legacy reindex job URL if migrating existing incidents (`configKey: LEGACYINDEX`).
3. Whether facility master also needs indexer (`health-facility-index-v0001` pattern) — separate from incident indexer.

