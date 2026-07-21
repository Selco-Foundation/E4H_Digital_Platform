# Livelihood Platform — API Specs

**Source:** `LIVELIHOOD_PLATFORM_CHANGES.md` §12, `LIVELIHOOD_LLD_WORKFLOW_AND_SLA.md`  
**Tenant:** single tenant `livelihood` (fork; no multi-tenant routing in APIs)  
**Auth:** `Authorization: Bearer <token>` on protected routes (DIGIT `RequestInfo` envelope)

---

## 1. Conventions

| Item | Value |
|------|--------|
| Asset create envelope | E4H shape: `assetDetail.Asset` (not flat `asset`) |
| Bulk asset create | `assetDetails[]` each with `Asset` (+ optional `workflow`) |
| Incident create | `facilityId` + `assetId`; auto-assign vendor from asset; **no** `/_create-on-behalf` route |
| POC on-behalf | Same `/_create` with `createdOnBehalf: true` |
| Incident workflow | Single `POST /v2/request/_update` (resolve, OOS, OOW+quotation, REOPEN, POC reassign) |
| Facility manager | Option 1: `facility.boundaryCode` + HRMS `COMPLAINANT`; **no** `manager/_link` |
| Item code master | MDMS preferred; `/item-code/_search` and `/item-code/_map` only if not MDMS-only |

---

## 2. Shared object models

### 2.1 `LivelihoodAsset` (inside `assetDetail.Asset` or bulk `assetDetails[].Asset`)

| Field | Type | Required | DB mapping (see §5) |
|-------|------|----------|---------------------|
| `tenantId` | string | yes | `asset.tenant_id` |
| `facilityID` | string | yes | `asset.facility_id` |
| `vendorId` | string | yes (Livelihood) | **add** `asset.vendor_id` |
| `itemCode` | string | yes (Livelihood) | **add** `asset.item_code` |
| `isSolar` | boolean | no | **add** `asset.is_solar` or JSONB |
| `assetTypeID` | string | yes | `asset.asset_type_id` |
| `brandID` | string | yes | `asset.brand_id` |
| `serialNumber` | string | yes | `asset.serial_number` |
| `modelNumber` | string | no | `asset.model_number` (free text) |
| `system` | string | yes | `asset.system` |
| `activityFacilityID` | string | yes (E4H) | `asset.activity_facility_id` |
| `warrantyStartDate` | date-time | no | `asset.warranty_start_date` |
| `warrantyDuration` | integer | no | `asset.warranty_duration` |
| `warrantyEndDate` | date-time | no | `asset.warranty_end_date` |
| `qrToken` | string | no | **add** `asset.qr_token` or JSONB |
| `assetDetails` | object | no | `asset.asset_details` |
| `additionalDetails` | object | no | `asset.additional_details` |

### 2.2 `LivelihoodIncident` (inside `Incident`)

| Field | Type | Required | DB mapping |
|-------|------|----------|------------|
| `tenantId` | string | yes | `eg_incident_v2.tenantid` |
| `facilityId` | string | yes | `eg_incident_v2.facilityid` ✓ exists |
| `assetId` | string | yes | **add** `eg_incident_v2.asset_id` |
| `incidentType` | string | yes | `eg_incident_v2.incidenttype` |
| `incidentSubType` | string | yes | `eg_incident_v2.incidentsubtype` |
| `comments` | string | no | `eg_incident_v2.comments` |
| `boundaryCode` | string | no | `eg_incident_v2.boundarycode` ✓ |
| `entryChannel` | enum | no | `additionaldetails` until column |
| `createdOnBehalf` | boolean | no | `additionaldetails` |
| `additionalDetail` | object | no | `eg_incident_v2.additionaldetails` |

### 2.3 `Workflow` (incident transitions)

| Field | Type | Notes |
|-------|------|--------|
| `action` | string | `AUTO_ASSIGN`, `RESOLVE`, `OUT_OF_SCOPE`, `OUT_OF_WARRANTY`, `REOPEN`, `ASSIGN_VENDOR`, `DECLINE_*` |
| `comments` | string | Mandatory for several actions |
| `assignes` | string[] | Vendor user UUIDs |
| `verificationDocuments` | array | OOW: `documentType: QUOTATION`, `fileStoreId` |

---

## 3. API reference — request / response samples

### 3.1 facility-service

#### `POST /facility-service/v2/facility/create` (Modify)

**Request:**
```json
{
  "RequestInfo": { "apiId": "livelihood-web", "authToken": "Bearer <token>" },
  "facility": {
    "tenantId": "livelihood",
    "facilityName": "Patil Dairy",
    "facilityType": "LIVELIHOOD_SITE",
    "facilityCategory": "DAIRY",
    "boundaryCode": "India_KA_BengaluruUrban_Block_fac/2025/41923",
    "facilityPocName": "Ramesh Patil",
    "facilityPocPhone": "9876543210",
    "facilityPocEmail": "ramesh@example.com",
    "isActive": true,
    "additionalDetails": { "programType": "LIVELIHOOD" }
  }
}
```

**Response (200):**
```json
{
  "ResponseInfo": { "status": "successful" },
  "facility": {
    "id": "fac/2025/41923",
    "tenantId": "livelihood",
    "facilityName": "Patil Dairy",
    "boundaryCode": "India_KA_BengaluruUrban_Block_fac/2025/41923",
    "facilityPocPhone": "9876543210",
    "wfStatus": "ACTIVE"
  }
}
```

*On Livelihood-ready: triggers HRMS `COMPLAINANT` user with jurisdiction = `boundaryCode`.*

---

#### `POST /facility-service/v2/facility/update` (Modify)

**Request:** same envelope as create; include `id`.

**Response (200):** updated `facility` object.

---

#### `GET /facility-service/v2/facility/search` (Modify)

**Query:** `?tenantId=livelihood&state=KA&projectId=proj-001&programType=LIVELIHOOD`

**Response (200):**
```json
{
  "facilities": [
    {
      "id": "fac/2025/41923",
      "facilityName": "Patil Dairy",
      "boundaryCode": "India_KA_..._fac/2025/41923",
      "facilityPocPhone": "9876543210"
    }
  ]
}
```

---

#### `POST /facility-service/v2/facility/_bulk-search` (Modify)

**Request:**
```json
{
  "RequestInfo": { "apiId": "livelihood-ingestion" },
  "criteria": {
    "tenantId": "livelihood",
    "facilityIds": ["fac/2025/41923", "fac/2025/41924"]
  }
}
```

**Response (200):** `{ "facilities": [ ... ] }`

---

#### `POST /facility-service/v2/facility/_bulk-search-with-boundary` (Modify)

**Request:**
```json
{
  "RequestInfo": { "apiId": "livelihood-web" },
  "criteria": {
    "tenantId": "livelihood",
    "boundaryCodes": ["India_KA_BengaluruUrban_Block"],
    "state": "KA"
  }
}
```

**Response (200):** POC state-scoped facility list with `boundaryCode` + manager contact fields.

---

#### `POST /facility-service/v2/facility/update-block` (Modify, conditional)

**Request:**
```json
{
  "RequestInfo": { "apiId": "livelihood-web" },
  "facilityId": "fac/2025/41923",
  "newBlockBoundaryCode": "India_KA_BengaluruUrban_NewBlock"
}
```

**Response (200):** updated facility; may call `im-services` `/_update-boundary-by-facility`.

---

#### `POST /facility-service/v2/facility/_resolve-by-manager-mobile` (New, optional)

**Request:**
```json
{
  "RequestInfo": { "apiId": "livelihood-web" },
  "tenantId": "livelihood",
  "mobileNumber": "9876543210"
}
```

**Response (200):**
```json
{
  "facilityId": "fac/2025/41923",
  "boundaryCode": "India_KA_BengaluruUrban_Block_fac/2025/41923",
  "facilityName": "Patil Dairy"
}
```

---

### 3.2 asset-registry

#### `POST /asset-registry/v1/asset/_create` (Modify)

**Request:**
```json
{
  "RequestInfo": { "apiId": "livelihood-web", "authToken": "Bearer <token>" },
  "assetDetail": {
    "Asset": {
      "tenantId": "livelihood",
      "facilityID": "fac/2025/41923",
      "vendorId": "org-vendor-001",
      "itemCode": "SOLAR_PUMP_SET_A",
      "isSolar": true,
      "assetTypeID": "PANEL",
      "brandID": "BRAND_X",
      "serialNumber": "SN-12345",
      "modelNumber": "MODEL-1",
      "system": "AC_OFF_GRID",
      "activityFacilityID": "act-fac-uuid",
      "warrantyStartDate": "2025-01-01T00:00:00.000Z",
      "warrantyDuration": 2,
      "warrantyEndDate": "2027-01-01T00:00:00.000Z"
    }
  }
}
```

**Response (201):**
```json
{
  "ResponseInfo": { "status": "successful" },
  "asset": {
    "assetId": "asset-uuid-001",
    "tenantId": "livelihood",
    "facilityID": "fac/2025/41923",
    "vendorId": "org-vendor-001",
    "itemCode": "SOLAR_PUMP_SET_A",
    "serialNumber": "SN-12345",
    "wfStatus": "ACTIVE",
    "isOperational": true
  }
}
```

---

#### `POST /asset-registry/v1/asset/_update` (Modify)

**Request:** same envelope; include `assetId` in `Asset`. `facilityID` immutable.

**Response (200):** updated `asset` (warranty, QR, install/wf status).

---

#### `POST /asset-registry/v1/asset/_search` (Modify)

**Request:** `?offset=0&limit=50`
```json
{
  "RequestInfo": { "apiId": "livelihood-web", "authToken": "Bearer <token>" },
  "criteria": {
    "tenantId": "livelihood",
    "facilityID": "fac/2025/41923",
    "isOperational": true
  }
}
```

**Response (200):**
```json
[
  {
    "assetId": "asset-uuid-001",
    "facilityID": "fac/2025/41923",
    "vendorId": "org-vendor-001",
    "itemCode": "SOLAR_PUMP_SET_A",
    "serialNumber": "SN-12345",
    "wfStatus": "ACTIVE"
  },
  {
    "assetId": "asset-uuid-002",
    "facilityID": "fac/2025/41923",
    "vendorId": "org-vendor-002",
    "itemCode": "ROTI_MACHINE_A",
    "serialNumber": "SN-67890",
    "wfStatus": "ACTIVE"
  }
]
```

---

#### `POST /asset-registry/v1/asset/bulk/_create` (Modify)

**Request:**
```json
{
  "RequestInfo": { "apiId": "livelihood-ingestion" },
  "assetDetails": [
    {
      "Asset": {
        "tenantId": "livelihood",
        "facilityID": "fac/2025/41923",
        "vendorId": "org-vendor-001",
        "itemCode": "SOLAR_PUMP_SET_A",
        "isSolar": true,
        "serialNumber": "SN-12345",
        "assetTypeID": "PANEL",
        "brandID": "BRAND_X",
        "system": "AC_OFF_GRID",
        "activityFacilityID": "act-fac-uuid"
      }
    },
    {
      "Asset": {
        "tenantId": "livelihood",
        "facilityID": "fac/2025/41923",
        "vendorId": "org-vendor-002",
        "itemCode": "ROTI_MACHINE_A",
        "isSolar": false,
        "serialNumber": "SN-67890",
        "assetTypeID": "INVERTER",
        "brandID": "BRAND_Y",
        "system": "DC",
        "activityFacilityID": "act-fac-uuid"
      }
    }
  ]
}
```

**Response (202):**
```json
{
  "ResponseInfo": { "status": "successful" },
  "assets": [
    { "assetId": "asset-uuid-001", "serialNumber": "SN-12345" }
  ],
  "errors": [
    {
      "index": 1,
      "serialNumber": "SN-67890",
      "code": "ASSET_DUPLICATE",
      "message": "Serial already exists at facility"
    }
  ]
}
```

---

#### `POST /asset-registry/v1/asset/workflow/{assetID}/_update` (Modify, conditional)

**Request:**
```json
{
  "RequestInfo": { "apiId": "livelihood-field" },
  "workflow": {
    "action": "SUBMIT_QC",
    "comments": "Solar QC complete"
  }
}
```

**Response (200):** updated asset `wfStatus`.

---

#### `POST /asset-registry/v1/asset/qr/_resolve` (New)

**Request:**
```json
{
  "tenantId": "livelihood",
  "qrPayload": "base64-or-signed-token",
  "facilityId": "fac/2025/41923"
}
```

**Response (200):**
```json
{
  "assetId": "asset-uuid-001",
  "facilityId": "fac/2025/41923",
  "itemCode": "SOLAR_PUMP_SET_A",
  "vendorId": "org-vendor-001"
}
```

---

#### `POST /asset-registry/v1/item-code/_search` (New, if not MDMS-only)

**Request:**
```json
{
  "RequestInfo": { "apiId": "livelihood-web" },
  "criteria": {
    "tenantId": "livelihood",
    "itemCode": "SOLAR_PUMP",
    "active": true
  }
}
```

**Response (200):**
```json
{
  "itemCodes": [
    {
      "code": "SOLAR_PUMP_SET_A",
      "name": "Solar pump set A",
      "vendorId": "org-vendor-001",
      "isSolar": true,
      "active": true
    }
  ]
}
```

---

### 3.3 vendor-registry

#### `POST /vendor/organisation/v1/_create` (Modify)

**Request:**
```json
{
  "RequestInfo": { "apiId": "livelihood-web" },
  "organisation": {
    "tenantId": "livelihood",
    "name": "Vendor One Pvt Ltd",
    "orgNumber": "ORG-001",
    "applicationStatus": "ACTIVE",
    "additionalDetails": { "livelihoodServiceCoverage": ["SOLAR_PUMP_SET_A", "ROTI_MACHINE_A"] }
  }
}
```

**Response (201):** created `organisation` with `id`.

---

#### `POST /vendor/organisation/v1/item-code/_map` (New, if not MDMS-only)

**Request:**
```json
{
  "RequestInfo": { "apiId": "livelihood-web" },
  "mappings": [
    { "itemCode": "SOLAR_PUMP_SET_A", "vendorId": "org-vendor-001", "active": true }
  ]
}
```

**Response (200):** `{ "mapped": 1 }`

---

#### `POST /vendor/organisation/v1/vendors-by-facility` (New)

**Request:**
```json
{
  "RequestInfo": { "apiId": "livelihood-web" },
  "tenantId": "livelihood",
  "facilityId": "fac/2025/41923"
}
```

**Response (200):**
```json
{
  "vendors": [
    { "vendorId": "org-vendor-001", "name": "Vendor One", "assetCount": 1 },
    { "vendorId": "org-vendor-002", "name": "Vendor Two", "assetCount": 1 }
  ]
}
```

*Computed from `asset` rows where `facility_id` = facilityId (requires `vendor_id` on asset).*

---

#### `POST /vendor/v1/_create` / `POST /vendor/v1/_update` (Modify)

Org **service** offerings aligned to item codes. Request/response follow existing vendor-registry `eg_org_function` shape with Livelihood extensions in `additionalDetails`.

---

### 3.4 project

#### `POST /project/v1/_create` (Modify)

**Request:**
```json
{
  "RequestInfo": { "apiId": "livelihood-web" },
  "project": {
    "tenantId": "livelihood",
    "name": "JC Karnataka Phase 1",
    "projectType": "LIVELIHOOD_INSTALL",
    "startDate": 1716537600000,
    "endDate": 1748073600000,
    "additionalDetails": {
      "justificationCode": "JC-KA-2025-001",
      "states": ["KA", "TN"]
    }
  }
}
```

**Response (201):** `{ "project": { "id": "proj-uuid-001", ... } }`

---

#### `POST /project/facility/v1/_create` (Modify)

**Request:**
```json
{
  "RequestInfo": { "apiId": "livelihood-web" },
  "projectFacility": {
    "tenantId": "livelihood",
    "projectId": "proj-uuid-001",
    "facilityId": "fac/2025/41923"
  }
}
```

**Response (201):** ProjectFacility link record.

---

#### `POST /project/v1/_create-from-justification` (New)

**Request:**
```json
{
  "RequestInfo": { "apiId": "livelihood-web" },
  "justificationCode": "JC-KA-2025-001",
  "project": {
    "tenantId": "livelihood",
    "name": "JC Karnataka Phase 1",
    "projectType": "LIVELIHOOD_INSTALL",
    "additionalDetails": { "states": ["KA"] }
  }
}
```

**Response (201):** project + auto `PROJECT_FACILITY` rows for all facilities under justification.

---

#### `POST /project/facility/v1/_map-by-justification` (New)

**Request:**
```json
{
  "RequestInfo": { "apiId": "livelihood-web" },
  "projectId": "proj-uuid-001",
  "justificationCode": "JC-KA-2025-001"
}
```

**Response (200):** `{ "linkedCount": 42 }`

---

#### `POST /project/v1/_search` (Modify)

**Request:**
```json
{
  "RequestInfo": { "apiId": "livelihood-web" },
  "criteria": {
    "tenantId": "livelihood",
    "justificationCode": "JC-KA-2025-001"
  }
}
```

**Response (200):** paginated `projects` list.

---

### 3.5 im-services

#### `POST /im-services/v2/request/_create` (Modify)

**Facility manager (self-serve):**
```json
{
  "RequestInfo": {
    "apiId": "livelihood-web",
    "userInfo": {
      "uuid": "fm-user-uuid",
      "roles": [{ "code": "COMPLAINANT" }]
    }
  },
  "Incident": {
    "tenantId": "livelihood",
    "facilityId": "fac/2025/41923",
    "assetId": "asset-uuid-001",
    "incidentType": "EQUIPMENT_NOT_WORKING",
    "incidentSubType": "PUMP_FAILURE",
    "comments": "Pump not starting",
    "entryChannel": "DIRECT_PLATFORM",
    "createdOnBehalf": false
  },
  "Workflow": { "action": "AUTO_ASSIGN" }
}
```

**POC on-behalf (same route):**
```json
{
  "RequestInfo": {
    "userInfo": { "uuid": "poc-uuid", "roles": [{ "code": "LIVELIHOOD_POC" }] }
  },
  "Incident": {
    "tenantId": "livelihood",
    "facilityId": "fac/2025/41923",
    "assetId": "asset-uuid-001",
    "incidentType": "EQUIPMENT_NOT_WORKING",
    "entryChannel": "POC_MANUAL",
    "createdOnBehalf": true
  },
  "Workflow": { "action": "AUTO_ASSIGN" }
}
```

**Response (202):**
```json
{
  "IncidentWrappers": [{
    "incident": {
      "id": "inc-uuid",
      "incidentId": "IM-2025-000123",
      "facilityId": "fac/2025/41923",
      "assetId": "asset-uuid-001",
      "applicationStatus": "PENDING_FOR_RESOLUTION"
    },
    "workflow": {
      "businessService": "LivelihoodIncident",
      "action": "AUTO_ASSIGN",
      "assignes": ["vendor-user-uuid"]
    }
  }]
}
```

**Server rules:** validate `asset.facilityID == facilityId`; assignee = asset's vendor; complainant = facility manager HRMS user when `createdOnBehalf`.

---

#### `POST /im-services/v2/request/_update` (Modify)

**Vendor resolve:**
```json
{
  "RequestInfo": { "userInfo": { "uuid": "vendor-uuid" } },
  "Incident": { "tenantId": "livelihood", "id": "inc-uuid" },
  "Workflow": {
    "action": "RESOLVE",
    "comments": "Replaced seal; tested OK",
    "verificationDocuments": [{
      "documentType": "RESOLUTION_PROOF",
      "fileStoreId": "filestore-uuid"
    }]
  }
}
```

**Vendor OOW (quotation via filestore first):**
```json
{
  "Workflow": {
    "action": "OUT_OF_WARRANTY",
    "comments": "Paid repair required",
    "verificationDocuments": [{
      "documentType": "QUOTATION",
      "fileStoreId": "filestore-quotation-pdf"
    }]
  }
}
```

**FM reopen (72h window):**
```json
{
  "Workflow": { "action": "REOPEN", "comments": "Issue not fixed" }
}
```

**POC reassign vendor:**
```json
{
  "Workflow": {
    "action": "ASSIGN_VENDOR",
    "assignes": ["other-vendor-user-uuid"],
    "comments": "Reassigned to vendor covering asset"
  }
}
```

**Response (202):** updated incident + workflow state.

---

#### `POST /im-services/v2/request/_search` (Modify)

**Request (POC state scope):**
```json
{
  "RequestInfo": { "apiId": "livelihood-web" },
  "incidentSearchCriteria": {
    "tenantId": "livelihood",
    "facilityState": "KA",
    "applicationStatus": ["PENDING_FOR_RESOLUTION", "RESOLVED"],
    "pagination": { "offset": 0, "limit": 50 }
  }
}
```

**Response (200):** `IncidentWrappers[]` (same filters as E4H search + role scoping).

---

#### `POST /im-services/v2/request/_count` / `_plainsearch` (Modify)

Same `incidentSearchCriteria` as `_search`. Count returns `{ "count": 123 }`.

---

#### `POST /im-services/v2/request/_update-boundary-by-facility` (Modify)

**Request:**
```json
{
  "RequestInfo": { "apiId": "livelihood-web" },
  "tenantId": "livelihood",
  "facilityId": "fac/2025/41923"
}
```

**Response (200):** `{ "updatedCount": 5 }` incidents synced to facility `boundaryCode`.

---

## 4. OpenAPI 3.0 bundle (machine-readable)

Export-friendly JSON. Schemas align with §2–§3 (corrected `assetDetail` / `assetDetails` envelopes).

```json
{
  "openapi": "3.0.3",
  "info": {
    "title": "Livelihood Platform API",
    "version": "1.1.0",
    "description": "New/modified APIs for Livelihood fork. Tenant: livelihood."
  },
  "servers": [{ "url": "https://{host}", "variables": { "host": { "default": "localhost" } } }],
  "tags": [
    { "name": "facility-service" },
    { "name": "asset-registry" },
    { "name": "vendor-registry" },
    { "name": "project" },
    { "name": "im-services" }
  ],
  "paths": {
    "/asset-registry/v1/asset/_create": {
      "post": {
        "tags": ["asset-registry"],
        "summary": "Create asset",
        "requestBody": {
          "content": {
            "application/json": {
              "schema": { "$ref": "#/components/schemas/AssetCreateRequest" },
              "example": {
                "RequestInfo": { "apiId": "livelihood-web" },
                "assetDetail": {
                  "Asset": {
                    "tenantId": "livelihood",
                    "facilityID": "fac/2025/41923",
                    "vendorId": "org-vendor-001",
                    "itemCode": "SOLAR_PUMP_SET_A",
                    "serialNumber": "SN-12345",
                    "assetTypeID": "PANEL",
                    "brandID": "BRAND_X",
                    "system": "AC_OFF_GRID",
                    "activityFacilityID": "act-fac-uuid"
                  }
                }
              }
            }
          }
        },
        "responses": { "201": { "description": "Created" } }
      }
    },
    "/asset-registry/v1/asset/bulk/_create": {
      "post": {
        "tags": ["asset-registry"],
        "summary": "Bulk create assets",
        "requestBody": {
          "content": {
            "application/json": {
              "schema": { "$ref": "#/components/schemas/BulkAssetCreateRequest" }
            }
          }
        },
        "responses": { "202": { "description": "Accepted (partial failure supported)" } }
      }
    },
    "/im-services/v2/request/_create": {
      "post": {
        "tags": ["im-services"],
        "summary": "Create incident",
        "requestBody": {
          "content": {
            "application/json": {
              "schema": { "$ref": "#/components/schemas/IncidentCreateRequest" }
            }
          }
        },
        "responses": { "202": { "description": "Accepted" } }
      }
    },
    "/im-services/v2/request/_update": {
      "post": {
        "tags": ["im-services"],
        "summary": "Workflow transition",
        "requestBody": {
          "content": {
            "application/json": {
              "schema": { "$ref": "#/components/schemas/IncidentUpdateRequest" }
            }
          }
        },
        "responses": { "202": { "description": "Accepted" } }
      }
    }
  },
  "components": {
    "securitySchemes": { "bearerAuth": { "type": "http", "scheme": "bearer" } },
    "schemas": {
      "LivelihoodAsset": {
        "type": "object",
        "required": ["tenantId", "facilityID", "vendorId", "itemCode", "serialNumber", "assetTypeID", "brandID", "system", "activityFacilityID"],
        "properties": {
          "tenantId": { "type": "string", "default": "livelihood" },
          "facilityID": { "type": "string" },
          "vendorId": { "type": "string" },
          "itemCode": { "type": "string" },
          "isSolar": { "type": "boolean" },
          "assetTypeID": { "type": "string" },
          "brandID": { "type": "string" },
          "serialNumber": { "type": "string" },
          "modelNumber": { "type": "string" },
          "system": { "type": "string" },
          "activityFacilityID": { "type": "string" },
          "warrantyStartDate": { "type": "string", "format": "date-time" },
          "warrantyDuration": { "type": "integer" },
          "warrantyEndDate": { "type": "string", "format": "date-time" },
          "qrToken": { "type": "string" },
          "assetDetails": { "type": "object" },
          "additionalDetails": { "type": "object" }
        }
      },
      "AssetCreateRequest": {
        "type": "object",
        "required": ["RequestInfo", "assetDetail"],
        "properties": {
          "RequestInfo": { "type": "object" },
          "assetDetail": {
            "type": "object",
            "required": ["Asset"],
            "properties": { "Asset": { "$ref": "#/components/schemas/LivelihoodAsset" } }
          }
        }
      },
      "BulkAssetCreateRequest": {
        "type": "object",
        "required": ["RequestInfo", "assetDetails"],
        "properties": {
          "RequestInfo": { "type": "object" },
          "assetDetails": {
            "type": "array",
            "items": {
              "type": "object",
              "required": ["Asset"],
              "properties": {
                "Asset": { "$ref": "#/components/schemas/LivelihoodAsset" },
                "workflow": { "type": "object" }
              }
            }
          }
        }
      },
      "LivelihoodIncident": {
        "type": "object",
        "required": ["tenantId", "facilityId", "assetId", "incidentType", "incidentSubType"],
        "properties": {
          "tenantId": { "type": "string", "default": "livelihood" },
          "facilityId": { "type": "string" },
          "assetId": { "type": "string" },
          "incidentType": { "type": "string" },
          "incidentSubType": { "type": "string" },
          "comments": { "type": "string" },
          "entryChannel": { "type": "string", "enum": ["DIRECT_PLATFORM", "POC_MANUAL", "IVR_WHATSAPP"] },
          "createdOnBehalf": { "type": "boolean" },
          "additionalDetail": { "type": "object" }
        }
      },
      "IncidentCreateRequest": {
        "type": "object",
        "required": ["RequestInfo", "Incident"],
        "properties": {
          "RequestInfo": { "type": "object" },
          "Incident": { "$ref": "#/components/schemas/LivelihoodIncident" },
          "Workflow": { "type": "object" }
        }
      },
      "IncidentUpdateRequest": {
        "type": "object",
        "required": ["RequestInfo", "Incident", "Workflow"],
        "properties": {
          "RequestInfo": { "type": "object" },
          "Incident": { "type": "object", "properties": { "id": { "type": "string" }, "tenantId": { "type": "string" } } },
          "Workflow": { "type": "object" }
        }
      }
    }
  }
}
```

*Full path list matches §3; extend this bundle with remaining paths using the same schema patterns.*

---

## 5. Database changes — existing vs required

Legend: **✓** column exists today | **+** add column/table | **JSONB** store in existing JSONB until DDL | **MDMS** master not in app DB | **WF** workflow service tables | **HRMS** external

### 5.1 `asset` (asset-registry)

**Exists today** (`V20250520141800` + later migrations):

| Column | Type |
|--------|------|
| `asset_id` | VARCHAR PK |
| `tenant_id` | VARCHAR |
| `facility_id` | VARCHAR |
| `asset_type_id` | VARCHAR |
| `brand_id` | VARCHAR |
| `serial_number` | VARCHAR |
| `model_number` | VARCHAR |
| `system` | VARCHAR |
| `asset_details` | JSONB |
| `warranty_*` | BIGINT / INT |
| `wf_status` | VARCHAR |
| `is_active` | BOOLEAN |
| `is_operational` | BOOLEAN |
| `activity_facility_id` | VARCHAR |
| `additional_details` | JSONB |

**Add for Livelihood APIs:**

| Column | Type | API field | Priority |
|--------|------|-----------|----------|
| `vendor_id` | VARCHAR(64) | `vendorId` | **P0** — incident auto-assign, vendors-by-facility |
| `item_code` | VARCHAR(64) | `itemCode` | **P0** — catalog + QC forms |
| `is_solar` | BOOLEAN | `isSolar` | **P1** — QC routing |
| `qr_token` | VARCHAR(256) | `qrPayload` / QR resolve | **P1** — optional if QR encodes `asset_id` only |

**Indexes to add:** `(tenant_id, facility_id, vendor_id)`, `(tenant_id, item_code)`, unique `(tenant_id, facility_id, serial_number)` if not already enforced in app layer.

**Interim (pre-DDL):** store `vendorId`, `itemCode`, `isSolar` in `additional_details` JSONB; service layer must read/write consistently.

---

### 5.2 `eg_incident_v2` (im-services)

**Exists today** (core + migrations):

| Column | Notes |
|--------|--------|
| `id`, `tenantid`, `incidenttype`, `incidentsubtype`, `incidentid` | ✓ |
| `applicationstatus`, `additionaldetails` | JSONB ✓ |
| `facilityid`, `boundarycode` | ✓ (added 2025) |
| `comments`, `district`, `block`, … | ✓ E4H health fields |
| `accountid` | reporter user UUID (not asset) |

**Add for Livelihood APIs:**

| Column | Type | API field | Priority |
|--------|------|-----------|----------|
| `asset_id` | VARCHAR(64) | `Incident.assetId` | **P0** — create/search/reporting |

**Persister update:** extend `im-services-persister.yml` INSERT/UPDATE to include `asset_id`.

**Store in JSONB until DDL:** `entryChannel`, `createdOnBehalf`, quotation metadata → `additionaldetails`.

**Workflow assignee:** remains in `egov_wf_process_*` (not `eg_incident_v2`).

---

### 5.3 `facility` (health-facility-registry)

**Exists today:**

| Column | API use |
|--------|---------|
| `id`, `tenant_id`, `facility_name`, `boundary_code` | ✓ |
| `facility_poc_name`, `facility_poc_phone`, `facility_poc_email` | ✓ manager contact |
| `facility_details`, `additional_details` | JSONB — Livelihood program attrs |

**Add (optional):**

| Column | Notes |
|--------|--------|
| `program_type` | VARCHAR — if not using `additional_details` |
| `livelihood_ready` | BOOLEAN — gate HRMS COMPLAINANT provision |

**No column for manager user:** HRMS employee with `jurisdictions.boundary = facility.boundary_code`, role `COMPLAINANT`.

---

### 5.4 `project` + `PROJECT_FACILITY`

**`project` exists:** `id`, `tenantId`, `projectTypeId`, `additionalDetails`, dates, …

| Livelihood need | Storage |
|-----------------|---------|
| `justificationCode` | **+** column `justification_code` VARCHAR **or** `additionalDetails.justificationCode` (JSONB interim) |
| Multi-state geography | `project_address` / `additionalDetails.states` |

**`PROJECT_FACILITY` exists:** `projectId`, `facilityId` — ✓ matches link APIs.

**No new table** for `_create-from-justification` / `_map-by-justification` (orchestration only).

---

### 5.5 Vendor / item code master

**`eg_org` (+ related) exists** for vendor organisations — ✓ `_create` APIs.

**Item code → vendor mapping:**

| Option | DB impact |
|--------|-----------|
| **A — MDMS only** (preferred) | No app table; drop `/item-code/_map` API |
| **B — App table** | **+** `livelihood_item_code_vendor` (`tenant_id`, `item_code`, `vendor_id`, `is_solar`, `active`, audit) |

**`vendors-by-facility`:** no new table — query `SELECT DISTINCT vendor_id FROM asset WHERE facility_id = ?` after `asset.vendor_id` exists.

---

### 5.6 Summary — minimum DDL to make APIs workable

| # | Migration | Service |
|---|-----------|---------|
| 1 | `ALTER TABLE asset ADD vendor_id, item_code` (+ indexes) | asset-registry |
| 2 | `ALTER TABLE eg_incident_v2 ADD asset_id` (+ index) | im-services |
| 3 | Update persister YAML + Java models for new columns | both |
| 4 | Optional: `asset.is_solar`, `asset.qr_token` | asset-registry |
| 5 | Optional: `project.justification_code` | project |
| 6 | Optional: `livelihood_item_code_vendor` **or** MDMS-only | vendor-registry / MDMS |

**No DDL needed:** `qr/_resolve`, `_resolve-by-manager-mobile`, `vendors-by-facility` (read/compute), workflow transitions (egov-workflow-v2), facility manager binding (HRMS).

---

## 6. API ↔ DB quick reference

| API object field | Table.column | Status |
|------------------|--------------|--------|
| `Asset.facilityID` | `asset.facility_id` | ✓ |
| `Asset.vendorId` | `asset.vendor_id` | **+ add** |
| `Asset.itemCode` | `asset.item_code` | **+ add** |
| `Asset.serialNumber` | `asset.serial_number` | ✓ |
| `Asset.modelNumber` | `asset.model_number` | ✓ |
| `Incident.facilityId` | `eg_incident_v2.facilityid` | ✓ |
| `Incident.assetId` | `eg_incident_v2.asset_id` | **+ add** |
| `Incident.createdOnBehalf` | `eg_incident_v2.additionaldetails` | JSONB interim |
| `facility.facilityPocPhone` | `facility.facility_poc_phone` | ✓ |
| `project.justificationCode` | `project.additionaldetails` or **+ column** | interim / add |
| Item code master | MDMS or **+ mapping table** | design choice |
| Vendor assignee | `egov_wf_process_instance` | WF (exists) |

---

*Align Flyway migrations with `LIVELIHOOD_LLD_DATA_MODEL.md` when that annex is authored.*
