# Livelihood Platform — Persister Configuration

**Purpose:** `egov-persister` YAML and schema deltas for Livelihood.  
**Related:** `LIVELIHOOD_INDEXER.md` (search), `LIVELIHOOD_LLD_WORKFLOW_AND_SLA_CONCISE.md` (workflow), `LIVELIHOOD_PLATFORM_CHANGES_CONCISE.md` (services).  
**Tenant:** `livelihood` (single-tenant fork).

**Principle:** Reuse **existing E4H tables and persister mappings** as the base. Livelihood adds only **new columns** (Flyway `ALTER TABLE`) or **new tables** where required—not greenfield schemas.

---

## 1. How persistence works

```text
Domain service (API)
    → Kafka: save-* / update-* topics
        → egov-persister (reads persister YAML)
            → PostgreSQL (E4H tables + Livelihood columns)
```

**Pair with indexer (same event, different consumer):**

| Kafka topic (example) | Consumer | Target |
| --- | --- | --- |
| `save-im-request` | **egov-persister** | `eg_incident_v2` |
| `save-im-request-indexer` | **egov-indexer** | Elasticsearch |

`im-services` publishes **`IncidentRequest`** to persister topics (`incident` + `RequestInfo` + `Workflow`), not the full `IncidentRequestWrapper`.

---

## 2. Persister files — reuse vs extend

| Service | E4H file (in repo) | Livelihood action |
| --- | --- | --- |
| **im-services** | `im-services-persister.yml` | **Extend** — new incident columns |
| **egov-workflow-v2** | `egov-workflow-v2-persister.yml` | **Reuse** — `businessService` = `LivelihoodIncident` in payload |
| **health-facility-registry** | `facility-persister.yml` | **Reuse** — manager contact in facility rows / `additional_details` |
| **asset-registry** | `asset-persister.yml` | **Extend** — `vendor_id` (recommended column) |
| **project** | `project-persister.yml` | **Reuse** — `justificationCode` in `project.additionalDetails` unless dedicated column added |
| **egov-hrms** | `hrms-employee-persister.yml` | **Reuse** — facility managers, POC, vendor staff |
| **boundary-service** | `boundary-persister.yml` | **Reuse** |
| **egov-mdms-service-v2** | `mdms-persister.yml` | **Reuse** — Livelihood MDMS modules |
| **amc-scheduler-service** | `amc-persister.yml` | **Not deployed** (Phase 1) |
| **field-planner** | (field-plan Kafka topics) | **Phase 2** — add persister if field-plan tables are introduced |

Deploy YAML under config repo `egov-persister/` and register paths in helm `persist-yml-path` (same pattern as E4H).

---

## 3. Schema strategy (Flyway on E4H base)

### 3.1 Incidents — table `eg_incident_v2` (existing)

**Already in E4H** (do not recreate): core columns from `V20240501115019__create_table.sql` plus alters for `district`, `block`, `phctype`, `phcsubtype`, `incidentsubtype`, `comments`, `facilityid`, `boundarycode`, `warranty_status`, `filed_date`, etc.

**Livelihood additive migration (proposed):**

```sql
-- V{timestamp}__livelihood_incident_columns.sql
ALTER TABLE eg_incident_v2
  ADD COLUMN IF NOT EXISTS asset_id CHARACTER VARYING(64),
  ADD COLUMN IF NOT EXISTS created_on_behalf BOOLEAN DEFAULT FALSE,
  ADD COLUMN IF NOT EXISTS entry_channel CHARACTER VARYING(64);

CREATE INDEX IF NOT EXISTS idx_eg_incident_v2_asset_id
  ON eg_incident_v2 (tenantid, asset_id);

CREATE INDEX IF NOT EXISTS idx_eg_incident_v2_facility_asset
  ON eg_incident_v2 (facilityid, asset_id);

-- Optional FK after asset table has vendor_id populated:
-- ALTER TABLE eg_incident_v2
--   ADD CONSTRAINT fk_eg_incident_v2_asset
--   FOREIGN KEY (asset_id) REFERENCES asset(asset_id);
```

| Column | Purpose |
| --- | --- |
| `asset_id` | Asset-level ticket; vendor resolved from `asset` |
| `created_on_behalf` | POC raised ticket |
| `entry_channel` | `DIRECT`, `POC_MANUAL`, `IVR_WHATSAPP` |

**Gap to fix in E4H fork:** `warranty_status` exists in DB (`V20260226120000`) but is **not** in current `im-services-persister.yml` INSERT/UPDATE—add to persister when enabling OOW flows.

**Health-only columns** (`phctype`, `phcsubtype`, `systemfunctional`, RMS tables): keep columns for shared fork compatibility; Livelihood UI/workflow does not require new values.

### 3.2 Assets — table `asset` (existing)

**E4H base:** `asset`, `asset_documents` (`V20250520141800__asset-service_ddl.sql`).

**Livelihood additive migration (proposed):**

```sql
ALTER TABLE asset
  ADD COLUMN IF NOT EXISTS vendor_id CHARACTER VARYING(64),
  ADD COLUMN IF NOT EXISTS item_code CHARACTER VARYING(128),
  ADD COLUMN IF NOT EXISTS qr_code CHARACTER VARYING(256);

CREATE INDEX IF NOT EXISTS idx_asset_facility_vendor
  ON asset (tenant_id, facility_id, vendor_id);
```

Until `vendor_id` exists, store vendor mapping in `additional_details` JSONB (works but harder to query).

### 3.3 Facility — table `facility` (existing)

**Reuse E4H** `facility-persister.yml` topics `save-facility` / `update-facility`.

Manager name/mobile: persist on facility row or `additional_details`; HRMS `COMPLAINANT` is provisioned in service code (not a separate persister topic).

### 3.4 Project — tables `project`, `project_facility` (existing)

**Reuse** `save-project`, `save-project-facility-topic`, etc.

**Justification code:** prefer `project.additionalDetails.justificationCode` (no migration) **or** add:

```sql
ALTER TABLE project ADD COLUMN IF NOT EXISTS justification_code CHARACTER VARYING(128);
CREATE INDEX IF NOT EXISTS idx_project_justification_code ON project (tenantId, justification_code);
```

Multi-state geography: continue using `project_address` / `project` hierarchy (existing persister).

### 3.5 Workflow — tables `eg_wf_*` (existing)

**No Livelihood-specific tables.** Process rows use `businessService = 'LivelihoodIncident'` (MDMS-defined). Persister topic `save-wf-transitions` unchanged.

### 3.6 New tables (only if needed)

| Table | When |
| --- | --- |
| `livelihood_item_code_vendor` | Only if item-code↔vendor is not in MDMS and not on `asset` |
| Field-plan × vendor link table | Phase 2 install track, if not modeled in existing field-planner tables |

Default: **avoid new tables** until MDMS + `additional_details` is insufficient.

---

## 4. im-services persister delta (`livelihood-im-persister.yml`)

Extend E4H `im-services-persister.yml`. Below is the **Livelihood INSERT/UPDATE fragment** (merge into existing mappings).

**Topics (unchanged):** `save-im-request`, `update-im-request`

```yaml
serviceMaps:
  serviceName: im-services
  mappings:
    - version: 1.0
      description: Livelihood — persist incident (extends E4H)
      fromTopic: save-im-request
      isTransaction: true
      queryMaps:
        - query: >
            INSERT INTO eg_incident_v2 (
              id, tenantid, incidenttype, incidentsubtype, incidentid,
              additionaldetails, applicationstatus,
              district, block, phctype, phcsubtype,
              createdby, createdtime, lastmodifiedby, lastmodifiedtime,
              comments, filed_date, legacy_id, migration_id,
              facilityid, boundarycode,
              warranty_status, asset_id, created_on_behalf, entry_channel
            ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?);
          basePath: incident
          jsonMaps:
            - jsonPath: $.incident.id
            - jsonPath: $.incident.tenantId
            - jsonPath: $.incident.incidentType
            - jsonPath: $.incident.incidentSubType
            - jsonPath: $.incident.incidentId
            - jsonPath: $.incident.additionalDetail
              type: JSON
              dbType: JSONB
            - jsonPath: $.incident.applicationStatus
            - jsonPath: $.incident.district
            - jsonPath: $.incident.block
            - jsonPath: $.incident.phcType
            - jsonPath: $.incident.phcSubType
            - jsonPath: $.incident.auditDetails.createdBy
            - jsonPath: $.incident.auditDetails.createdTime
            - jsonPath: $.incident.auditDetails.lastModifiedBy
            - jsonPath: $.incident.auditDetails.lastModifiedTime
            - jsonPath: $.incident.comments
            - jsonPath: $.incident.filedDate
            - jsonPath: $.incident.legacyId
            - jsonPath: $.incident.migrationId
            - jsonPath: $.incident.facilityId
            - jsonPath: $.incident.boundaryCode
            - jsonPath: $.incident.warrantyStatus
            - jsonPath: $.incident.assetId
            - jsonPath: $.incident.createdOnBehalf
            - jsonPath: $.incident.entryChannel

    - version: 1.0
      description: Livelihood — update incident (extends E4H)
      fromTopic: update-im-request
      isTransaction: true
      queryMaps:
        - query: >
            UPDATE eg_incident_v2 SET
              incidenttype = ?, incidentid = ?, incidentsubtype = ?,
              additionaldetails = ?, applicationstatus = ?,
              district = ?, block = ?, phctype = ?, phcsubtype = ?,
              lastmodifiedby = ?, lastmodifiedtime = ?,
              comments = ?, filed_date = ?, legacy_id = ?, migration_id = ?,
              facilityid = ?, boundarycode = ?,
              warranty_status = ?, asset_id = ?, created_on_behalf = ?, entry_channel = ?
            WHERE id = ?;
          basePath: incident
          jsonMaps:
            - jsonPath: $.incident.incidentType
            - jsonPath: $.incident.incidentId
            - jsonPath: $.incident.incidentSubType
            - jsonPath: $.incident.additionalDetail
              type: JSON
              dbType: JSONB
            - jsonPath: $.incident.applicationStatus
            - jsonPath: $.incident.district
            - jsonPath: $.incident.block
            - jsonPath: $.incident.phcType
            - jsonPath: $.incident.phcSubType
            - jsonPath: $.incident.auditDetails.lastModifiedBy
            - jsonPath: $.incident.auditDetails.lastModifiedTime
            - jsonPath: $.incident.comments
            - jsonPath: $.incident.filedDate
            - jsonPath: $.incident.legacyId
            - jsonPath: $.incident.migrationId
            - jsonPath: $.incident.facilityId
            - jsonPath: $.incident.boundaryCode
            - jsonPath: $.incident.warrantyStatus
            - jsonPath: $.incident.assetId
            - jsonPath: $.incident.createdOnBehalf
            - jsonPath: $.incident.entryChannel
            - jsonPath: $.incident.id
```

**API ↔ persister alignment:** Add `assetId`, `createdOnBehalf`, `entryChannel`, `warrantyStatus` on `Incident.java` before persister can bind them.

---

## 5. asset-registry persister delta (fragment)

Add columns to existing `save-asset` / `update-asset` mappings:

```yaml
# INSERT — append to column list after facility_id:
#   vendor_id, item_code, qr_code
# jsonMaps:
#   - jsonPath: $.vendorId
#   - jsonPath: $.itemCode
#   - jsonPath: $.qrCode
```

Quotation documents for OOW stay in **workflow** `eg_wf_document_v2` + **filestore** (existing workflow persister)—no new incident document table required for MVP.

---

## 6. Workflow persister (reuse)

| Topic | Table | Livelihood note |
| --- | --- | --- |
| `save-wf-transitions` | `eg_wf_processinstance_v2`, assignees, documents | `businessService` = `LivelihoodIncident` |
| `update-wf-processinstance` | process instance updates | SLA fields for inbox |
| `save-wf-businessservice` | business service defs | MDMS publish job |

Cron topic `im-auto-escalation` is consumed by **im-services**, not persister.

---

## 7. Kafka topic map (Phase 1 essentials)

| Topic | Producer | Persister file |
| --- | --- | --- |
| `save-im-request` | im-services | livelihood-im-persister.yml |
| `update-im-request` | im-services | livelihood-im-persister.yml |
| `save-wf-transitions` | workflow | egov-workflow-v2-persister.yml |
| `save-asset` / `update-asset` | asset-registry | asset-persister.yml (extended) |
| `save-facility` / `update-facility` | facility-registry | facility-persister.yml |
| `save-project-facility-topic` | project | project-persister.yml |
| `save-hrms-employee` | egov-hrms | hrms-employee-persister.yml |

---

## 8. Deployment checklist

1. Run **Flyway** migrations on Livelihood DB (additive only).
2. Update **Java models** (`Incident`, `Asset`) to match new columns.
3. Merge **persister YAML** into `egov-persister/` config repo.
4. Add file paths to **`persist-yml-path`** in Livelihood helm; restart **egov-persister**.
5. Verify: create incident → row in `eg_incident_v2` with `asset_id` populated.
6. Update **indexer YAML** (`LIVELIHOOD_INDEXER.md`) in parallel so search matches DB fields.

---

## 9. Open items

1. `asset_id` FK to `asset(asset_id)` — enable after data migration/backfill.
2. Store `createdOnBehalf` / `entryChannel` in `additionaldetails` only vs dedicated columns (columns recommended for reporting).
3. Field-planner install topics (`save-field-plan`, etc.) — confirm persister coverage in Phase 2.
4. Align `warranty_status` enum string between Java (`WarrantyStatus`) and DB column values.
