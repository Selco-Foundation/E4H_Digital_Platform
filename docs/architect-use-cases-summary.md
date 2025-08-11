# E4H Field Planner - Use Cases, Workflows, APIs & Configurations
## Architect Summary Document



---

## 1. Executive Summary

### 1.1 Document Purpose
This document provides a comprehensive overview of all use cases, workflows, APIs, and configurations for the E4H Field Planner module. It serves as a complete reference for architectural decisions and implementation planning.

### 1.2 Key Statistics
- **15 Primary Use Cases** across 5 user roles
- **8 Core Workflows** with state management
- **12 Field Planner APIs** + 8 integration APIs
- **6 Configuration Categories** (MDMS-driven)
- **6 Integration Points** with existing E4H services

---

## 2. Use Case Matrix

### 2.1 Primary Use Cases by Role

| UC | Use Case | Primary Actor | Complexity | Priority | APIs Used |
|----|----------|---------------|------------|----------|-----------|
| **UC-01** | Create and Manage Field Plans | Project Manager | High | P0 | 5 APIs |
| **UC-02** | Assign Activities to SPOCs | Project Manager | Medium | P0 | 3 APIs |
| **UC-03** | Assign Facilities to Field Staff | Activity SPOC | Medium | P0 | 4 APIs |
| **UC-04** | Submit Activity Reports | Field Staff | Low | P0 | 3 APIs |
| **UC-05** | Review and Approve Reports | Reviewer | Medium | P0 | 2 APIs |
| **UC-06** | Monitor Project Progress | Project Manager | Medium | P1 | 4 APIs |
| **UC-07** | Mobile Data Synchronization | Field Staff | Low | P1 | 3 APIs |
| **UC-08** | System Configuration Management | Admin | High | P1 | 2 APIs |

### 2.2 Use Case Categories

#### **P0 - Core Business Functions (5 use cases)**
- Field plan creation and management
- Activity and facility assignments
- Report submission and approval
- Mobile operations

#### **P1 - Operational Functions (3 use cases)**
- Progress monitoring
- Mobile sync
- System configuration

---

## 3. Detailed Use Cases & Workflows

### 3.1 UC-01: Create and Manage Field Plans

#### **Workflow Steps:**
1. **Project Selection** → Load projects from Project Service
2. **Activity Selection** → Validate activities from MDMS
3. **Facility Selection** → Download/upload facility template
4. **Field Plan Creation** → Create with workflow initialization

#### **APIs Used:**
- `GET /project/v1/_search` (Project Service)
- `GET /egov-mdms-service/v1/_search` (MDMS)
- `GET /field-planner/field-plans/facilities/v1/template`
- `POST field-planner/v1/field-plans/facilities/v1/upload`
- `POST field-planner/v1/field-plans/v1/_create`

#### **Configuration:**
```json
{
  "fieldPlanNaming": "{GEOGRAPHY}-{ACTIVITIES}-{YEAR}-{SEQUENCE}",
  "validationRules": {
    "name": {"minLength": 3, "maxLength": 100},
    "dates": {"startBeforeEnd": true, "futureStart": true}
  }
}
```

### 3.2 UC-02: Assign Activities to SPOCs

#### **Workflow Steps:**
1. **SPOC Search** → Search VENDOR REGISTRY for eligible SPOCs
2. **Role Validation** → Validate SPOC roles and permissions
3. **Assignment Creation** → Create activity assignments
4. **Notification** → Send assignment notifications

#### **APIs Used:**
- `GET /organization/employees/v1/_search` (VENDOR SEARCH mapped to project manager across multiple vendor orgs and one for vendor employee using params with org info)
- `POST /v1/field-planner/field-plans/activities/_assign`

#### **Configuration:**
```json
{
  "roleAssignmentRules": {
    "INSTALLATION": {
      "requiredRoles": ["INSTALLATION_SPOC", "INSTALLATION_REVIEWER"],
      "maxAssignments": 1
    }
  }
}
```

### 3.3 UC-03: Assign Facilities to Field Staff

#### **Workflow Steps:**
1. **Facility Loading** → Load facilities from HFR
2. **Staff Selection** → Select field staff from HRMS
3. **Bulk Assignment** → Upload facility-staff assignments
4. **Validation** → Validate assignments and activate facilities

#### **APIs Used:**
- `GET /facility/v1/_search` (HFR)
- `GET /egov-hrms/employees/_search` (HRMS)
- `POST /field-planner/v1/field-plans/{id}/facility-activities/_assign`

#### **Configuration:**
```json
{
  "assignmentRules": {
    "maxFacilitiesPerStaff": 10,
    "geographicConstraints": true,
    "activationConditions": {
      "INSTALLATION": ["ASSESSMENT_COMPLETE", "STAFF_ASSIGNED"]
    }
  }
}
```

### 3.4 UC-04: Submit Activity Reports

#### **Workflow Steps:**
1. **Mobile Sync** → Sync assignments and master data
2. **Report Creation** → Create report with attachments
3. **Validation** → Validate report data and files
4. **Submission** → Submit for review workflow

#### **APIs Used:**
- `POST /field-planner/v1/mobile/sync/assignments/_bulk`
- `POST /field-planner/v1/activity-reports/_create`
- `POST /field-planner/v1/activity-reports/_workflow`

#### **Configuration:**
```json
{
  "reportValidation": {
    "INSTALLATION": {
      "requiredFields": ["facilityId", "installationDate"],
      "requiredAttachments": ["photos", "certificate"],
      "maxAttachments": 10
    }
  }
}
```

### 3.5 UC-05: Review and Approve Reports

#### **Workflow Steps:**
1. **Report Loading** → Load reports pending review
2. **Review Process** → Examine report details and attachments
3. **Decision** → Approve/reject with comments
4. **Workflow Update** → Update workflow status

#### **APIs Used:**
- `POST /field-planner/v1/activity-reports/_search`
- `POST /field-planner/v1/activity-reports/_workflow`

#### **Configuration:**
```json
{
  "reviewWorkflow": {
    "INSTALLATION": {
      "autoApproval": false,
      "requireComments": true,
      "slaHours": 48
    }
  }
}
```

---

## 4. API Usage by Use Case

### 4.1 Field Planner Service APIs (12 APIs)

#### **Field Plan Management (5 APIs)**
| API | Method | Use Cases | Purpose |
|-----|--------|-----------|---------|
| `/v1/field-plans/_create` | POST | UC-01 | Bulk field plan creation |
| `/v1/field-plans/_update` | POST | UC-01, UC-06 | Field plan updates |
| `/v1/field-plans/_search` | POST | UC-01, UC-02, UC-06 | Field plan search |
| `/v1/field-plans/_workflow` | POST | UC-01, UC-06 | Workflow state transitions |
| `/v1/field-plans/facilities/_assign` | POST | UC-01 | Facility assignment |

#### **Activity Management (4 APIs)**
| API | Method | Use Cases | Purpose |
|-----|--------|-----------|---------|
| `/v1/field-plans/{id}/activities/_assign` | POST | UC-02 | Activity to SPOC assignment |
| `/v1/field-plans/{id}/facility-activities/_assign` | POST | UC-03 | Facility to staff assignment |
| `/v1/field-plans/{id}/facility-activities/_search` | POST | UC-03, UC-06 | Assignment search |
| `/v1/field-plans/{id}/facility-activities/_update` | POST | UC-03 | Assignment updates |

#### **Activity Reports (3 APIs)**
| API | Method | Use Cases | Purpose |
|-----|--------|-----------|---------|
| `/v1/activity-reports/_create` | POST | UC-04 | Report creation |
| `/v1/activity-reports/_update` | POST | UC-04 | Report updates |
| `/v1/activity-reports/_search` | POST | UC-05, UC-06 | Report search |
| `/v1/activity-reports/_workflow` | POST | UC-04, UC-05 | Report workflow |

### 4.2 Integration APIs (8 APIs)

#### **HRMS Integration (2 APIs)**
- `GET /egov-hrms/employees/_search` → User validation and team management
- `GET /egov-hrms/employees/{id}` → User details retrieval

#### **Health Facility Registry (2 APIs)**
- `GET /facility/v1/_search` → Facility validation and search
- `GET /facility/v1/{id}` → Facility details retrieval

#### **Project Service (2 APIs)**
- `GET /project/v1/_search` → Project validation and search
- `GET /project/v1/{id}` → Project details retrieval

#### **Workflow Service (2 APIs)**
- `POST /egov-wf-v2/process/_transition` → Workflow state transitions
- `POST /egov-wf-v2/process/_search` → Workflow tracking

#### **MDMS Integration (1 API)**
- `POST /egov-mdms-service/v1/_search` → Master data retrieval

#### **Filestore Integration (1 API)**
- `POST /filestore/v1/files` → File upload and storage

---

## 5. Configuration Management

### 5.1 Master Data Configuration (MDMS)

#### **Activity Types Configuration**
```json
{
  "moduleName": "field-planner",
  "masterName": "activity-types",
  "data": [
    {
      "code": "INSTALLATION",
      "name": "Installation",
      "requiredRoles": ["INSTALLATION_SPOC", "INSTALLATION_REVIEWER"],
      "workflowCode": "field-plan-installation",
      "slaHours": 72
    },
    {
      "code": "FIELD_QC",
      "name": "Field Quality Check",
      "requiredRoles": ["FIELD_QC_SPOC", "FIELD_QC_REVIEWER"],
      "workflowCode": "field-plan-qc",
      "slaHours": 24
    }
  ]
}
```

#### **Status Configuration**
```json
{
  "moduleName": "field-planner",
  "masterName": "status-codes",
  "data": [
    {"code": "DRAFT", "name": "Draft", "editable": true},
    {"code": "ACTIVE", "name": "Active", "editable": false},
    {"code": "COMPLETED", "name": "Completed", "editable": false},
    {"code": "REJECTED", "name": "Rejected", "editable": true}
  ]
}
```

#### **Role Configuration**
```json
{
  "moduleName": "field-planner",
  "masterName": "user-roles",
  "data": [
    {
      "code": "FIELD_PLANNER_ADMIN",
      "name": "Field Planner Admin",
      "hierarchyLevel": 1,
      "scope": "SYSTEM_WIDE"
    },
    {
      "code": "PROJECT_MANAGER",
      "name": "Project Manager",
      "hierarchyLevel": 2,
      "scope": "PROJECT_LEVEL"
    }
  ]
}
```

### 5.2 Workflow Configuration

#### **Field Plan Workflow**
```json
{
  "moduleName": "field-planner",
  "masterName": "workflow-definitions",
  "data": {
    "field-plan-workflow": {
      "states": [
        {"name": "DRAFT", "actions": ["SUBMIT", "DELETE"]},
        {"name": "ACTIVE", "actions": ["PAUSE", "COMPLETE"]},
        {"name": "COMPLETED", "actions": []}
      ],
      "transitions": [
        {"from": "DRAFT", "to": "ACTIVE", "action": "SUBMIT"},
        {"from": "ACTIVE", "to": "COMPLETED", "action": "COMPLETE"}
      ]
    }
  }
}
```

#### **Activity Report Workflow**
```json
{
  "moduleName": "field-planner",
  "masterName": "workflow-definitions",
  "data": {
    "activity-report-workflow": {
      "states": [
        {"name": "DRAFT", "actions": ["SUBMIT"]},
        {"name": "SUBMITTED", "actions": ["APPROVE", "REJECT"]},
        {"name": "APPROVED", "actions": []},
        {"name": "REJECTED", "actions": ["RESUBMIT"]}
      ],
      "transitions": [
        {"from": "DRAFT", "to": "SUBMITTED", "action": "SUBMIT"},
        {"from": "SUBMITTED", "to": "APPROVED", "action": "APPROVE"},
        {"from": "SUBMITTED", "to": "REJECTED", "action": "REJECT"}
      ]
    }
  }
}
```

### 5.3 System Configuration

#### **Application Properties**
```properties
# Field Planner Service Configuration
field-planner.service.name=field-planner-service
field-planner.service.version=1.0.0

# Database Configuration
field-planner.db.url=jdbc:postgresql://localhost:5432/e4h_field_planner

# Cache Configuration
field-planner.cache.ttl=3600
field-planner.cache.max-size=10000

# Bulk Operations Configuration
field-planner.bulk.max-batch-size=1000
field-planner.bulk.timeout=300

# Mobile Sync Configuration
field-planner.mobile.sync.interval=300
field-planner.mobile.sync.max-retries=3
field-planner.mobile.sync.conflict-resolution=SERVER_WINS

# Workflow Configuration
field-planner.workflow.auto-escalation=true
field-planner.workflow.escalation-delay=24
```

#### **Validation Configuration**
```json
{
  "moduleName": "field-planner",
  "masterName": "validation-rules",
  "data": {
    "fieldPlan": {
      "name": {"minLength": 3, "maxLength": 100},
      "startDate": {"required": true, "futureDate": true},
      "endDate": {"required": true, "afterStartDate": true}
    },
    "activityReport": {
      "facilityId": {"required": true},
      "attachments": {"maxCount": 10, "maxSize": "10MB"}
    }
  }
}
```

---

## 6. Integration Points

### 6.1 Service Integration Summary

| Service | Purpose | APIs Used | Configuration |
|---------|---------|-----------|---------------|
| **HRMS** | User management & validation | 2 APIs | Role mapping, permission validation |
| **HFR** | Facility validation & data | 2 APIs | Facility status, geographic boundaries |
| **Project Service** | Project validation & data | 2 APIs | Project boundaries, facility mappings |
| **Workflow Service** | State management | 2 APIs | Workflow definitions, state transitions |
| **MDMS** | Master data & configuration | 1 API | All configurable business rules |
| **Filestore** | File operations | 1 API | File upload/download, storage |

### 6.2 Integration Patterns

#### **Service Client Pattern**
- All external service calls use service clients
- Centralized error handling and retry logic
- Circuit breaker pattern for resilience

#### **Data Consistency Pattern**
- Eventual consistency through async updates
- Conflict resolution strategies
- Audit trail for all cross-service operations

---

## 7. Error Handling & Edge Cases

### 7.1 Common Error Scenarios

#### **Validation Errors**
- Field plan name conflicts
- Invalid date ranges
- Insufficient permissions
- Missing required data

#### **Integration Errors**
- Service unavailable
- Data inconsistency
- Timeout scenarios
- Authentication failures

#### **Business Rule Violations**
- Invalid workflow transitions
- Assignment conflicts
- SLA violations
- Geographic constraints

### 7.2 Error Response Format
```json
{
  "errorCode": "VALIDATION_ERROR",
  "errorMessage": "Field plan validation failed",
  "details": [
    {
      "field": "name",
      "error": "Field plan name must be unique",
      "suggestion": "Use suggested name format"
    }
  ]
}
```

---

## 8. Performance Considerations

### 8.1 Performance Targets
- **API Response Time**: < 2 seconds for most operations
- **Bulk Operations**: 1000 records in < 30 seconds
- **Mobile Sync**: < 5 seconds for differential sync
- **Search Operations**: < 500ms with pagination

### 8.2 Optimization Strategies
- **Database Indexing**: Comprehensive index strategy
- **Caching**: Redis caching for frequently accessed data
- **Bulk Operations**: Array-based APIs for efficiency
- **Mobile Optimization**: Offline-first with intelligent sync

---

## 9. Summary

### 9.1 Key Architectural Decisions
1. **Single New Service**: Field Planner Service only
2. **HRMS Integration**: Leverage existing user management
3. **MDMS-Driven**: All business rules externalized
4. **Bulk Operations**: Array-based APIs for efficiency
5. **Mobile-First**: Offline-capable with intelligent sync

### 9.2 Implementation Readiness
- ✅ **API Specifications**: Complete OpenAPI 3.0 specs
- ✅ **Database Schema**: Full DDL with indexes
- ✅ **Workflow Definitions**: Complete JSON configurations
- ✅ **Master Data**: All MDMS configurations defined
- ✅ **Integration Points**: All external services mapped

### 9.3 Next Steps
1. **Architecture Review**: Validate design decisions
2. **Implementation Planning**: Create detailed roadmap
3. **Testing Strategy**: Define comprehensive testing approach
4. **Deployment Planning**: Plan production deployment

---

**Document Version**: 1.0  
**Last Updated**: 2025-01-21  
**Next Review**: 2025-02-21
