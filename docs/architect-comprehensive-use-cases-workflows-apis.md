# E4H Field Planner - Comprehensive Use Cases, Workflows, APIs & Configurations
## Architect Reference Document

### Version Control
| Version | Author | Date | Changes |
|---------|--------|------|---------|
| 1.0 | Tech Lead | 2025-01-21 | Initial comprehensive documentation |

---

## Table of Contents
1. [Executive Summary](#executive-summary)
2. [Use Case Matrix](#use-case-matrix)
3. [Detailed Use Cases & Workflows](#detailed-use-cases--workflows)
4. [API Usage by Use Case](#api-usage-by-use-case)
5. [Configuration Management](#configuration-management)
6. [Integration Points](#integration-points)
7. [Error Handling & Edge Cases](#error-handling--edge-cases)
8. [Performance Considerations](#performance-considerations)

---

## 1. Executive Summary

### 1.1 Document Purpose
This document provides a comprehensive breakdown of all use cases, workflows, APIs, and configurations for the E4H Field Planner module. It serves as a complete reference for architectural decisions, implementation planning, and system understanding.

### 1.2 Key Statistics
- **Total Use Cases**: 15 primary use cases
- **Total Workflows**: 8 core workflows
- **Total Domain APIs**: Split across 2 microservices (Field Planner + Activities) + 8 integration APIs
- **Total Configurations**: 6 configuration categories
- **Integration Points**: 6 external services

### 1.3 Architecture Overview
```
┌─────────────────┐    ┌─────────────────┐    ┌─────────────────┐
│   E4H Web UI    │    │   Mobile App    │    │  Admin Console  │
└─────────┬───────┘    └─────────┬───────┘    └─────────┬───────┘
          │                      │                      │
          └──────────────────────┼──────────────────────┘
                                 │
                    ┌─────────────┴─────────────┐
                    │      E4H API Gateway      │
                    └─────────────┬─────────────┘
                                  │
         ┌────────────────────────┴────────────────────────┐
         │                                                 │
┌────────▼────────┐                               ┌────────▼────────┐
│ Field Planner   │                               │   Activities    │
│    Service      │                               │     Service     │
└────────┬────────┘                               └────────┬────────┘
         │                                                 │
   (Field Plans, Facility                           (Activities, SPOC &
    mapping, Mobile Sync)                            Staff Assignments,
                                                     Activity Reports)
         │                                                 │
         ├───────────────┬───────────────┬───────────────┤
         │               │               │               │
   ┌─────▼─────┐   ┌─────▼─────┐   ┌─────▼─────┐   ┌─────▼─────┐
   │  Project  │   │    HFR    │   │  Workflow │   │   MDMS    │
   └───────────┘   └───────────┘   └───────────┘   └───────────┘
                               ┌─────▼─────┐
                               │ Filestore │
                               └───────────┘
```

---

## 2. Use Case Matrix

### 2.1 Primary Use Cases by Role

| Use Case | Primary Actor | Secondary Actors | Complexity | Priority |
|----------|---------------|------------------|------------|----------|
| **UC-01** | Project Manager | System | High | P0 |
| **UC-02** | Project Manager | Activity SPOC | Medium | P0 |
| **UC-03** | Activity SPOC | Field Staff | Medium | P0 |
| **UC-04** | Field Staff | Activity SPOC | Low | P0 |
| **UC-05** | Activity SPOC | Reviewer | Medium | P0 |
| **UC-06** | Project Manager | System | Medium | P1 |
| **UC-07** | Field Staff | System | Low | P1 |
| **UC-08** | Admin | System | High | P1 |
| **UC-09** | Activity SPOC | System | Medium | P2 |
| **UC-10** | Project Manager | Activity SPOC | Medium | P2 |

### 2.2 Use Case Categories

#### **P0 - Core Business Functions**
- UC-01: Create and Manage Field Plans
- UC-02: Assign Activities to SPOCs
- UC-03: Assign Facilities to Field Staff
- UC-04: Submit Activity Reports
- UC-05: Review and Approve Reports

#### **P1 - Operational Functions**
- UC-06: Monitor Project Progress
- UC-07: Mobile Data Synchronization
- UC-08: System Configuration Management

#### **P2 - Enhancement Functions**
- UC-09: Bulk Operations Management
- UC-10: Team Management

---

## 3. Detailed Use Cases & Workflows

### 3.1 UC-01: Create and Manage Field Plans

#### **3.1.1 Use Case Description**
**Primary Actor**: Project Manager
**Goal**: Create field plans for project execution with specified activities and facilities
**Preconditions**: 
- Project exists in system
- Project Manager has access to project
- Health facilities are available in project scope

#### **3.1.2 Workflow Steps**

**Step 1: Field Plan Metadata Entry**
```
User Action: Navigate to "Create Field Plan"
System Response: Display field plan creation form
Configuration: Load project list from Project Service
API Used: GET /project/v1/_search
```

**Step 2: Project Selection**
```
User Action: Select project from dropdown
System Response: Pre-populate geography and facility count
Configuration: Project boundaries from MDMS
API Used: GET /egov-mdms-service/v1/_search (boundaries)
```

**Step 3: Activity Selection**
```
User Action: Select activities (Installation, QC, etc.)
System Response: Show activity-specific requirements
Configuration: Activity definitions from MDMS
API Used: GET /egov-mdms-service/v1/_search (activities)
```

**Step 4: Facility Selection**
```
User Action: Download facility template and upload selection
System Response: Validate and process facility selection
Configuration: Facility validation rules
API Used: 
- GET /field-planner/v1/field-plans/facilities/_template
- POST /field-planner/v1/field-plans/facilities/_upload
```

**Step 5: Field Plan Creation**
```
User Action: Confirm field plan creation
System Response: Create field plan and initialize workflow
Configuration: Field plan naming rules, workflow initialization
API Used: POST /field-planner/v1/field-plans/_create
```

#### **3.1.3 Configuration Requirements**

**MDMS Master Data:**
```json
{
  "moduleName": "field-planner",
  "masterName": "activity-types",
  "data": [
    {
      "code": "INSTALLATION",
      "name": "Installation",
      "description": "DRE installation activity",
      "requiredRoles": ["INSTALLATION_SPOC", "INSTALLATION_REVIEWER"],
      "workflowCode": "field-plan-installation"
    },
    {
      "code": "FIELD_QC",
      "name": "Field Quality Check",
      "description": "Field quality control activity",
      "requiredRoles": ["FIELD_QC_SPOC", "FIELD_QC_REVIEWER"],
      "workflowCode": "field-plan-qc"
    }
  ]
}
```

**Field Plan Naming Rules:**
```json
{
  "moduleName": "field-planner",
  "masterName": "naming-rules",
  "data": {
    "fieldPlanFormat": "{GEOGRAPHY}-{ACTIVITIES}-{YEAR}-{SEQUENCE}",
    "geographyLevel": "STATE",
    "activitySeparator": "_",
    "maxActivities": 5
  }
}
```

#### **3.1.4 Error Handling**

**Validation Errors:**
- Project not found: Return 404 with project search suggestions
- Invalid activities: Return 400 with valid activity list
- Facility count mismatch: Return 400 with actual facility count
- Duplicate field plan name: Return 409 with suggested alternatives

**Business Rule Violations:**
- Date conflicts: Return 400 with conflicting field plans
- Insufficient permissions: Return 403 with required role information
- Workflow initialization failure: Return 500 with retry mechanism

### 3.2 UC-02: Assign Activities to SPOCs

#### **3.2.1 Use Case Description**
**Primary Actor**: Project Manager
**Goal**: Assign specific activities within field plans to Activity SPOCs
**Preconditions**: 
- Field plan exists and is in ACTIVE status
- Activities are defined in field plan
- SPOCs are available in system

#### **3.2.2 Workflow Steps**

**Step 1: Activity Assignment Interface**
```
User Action: Navigate to "Assign Activities"
System Response: Display activity assignment form
Configuration: Load SPOCs from HRMS
API Used: GET /egov-hrms/employees/_search
```

**Step 2: SPOC Selection**
```
User Action: Search and select SPOCs for each activity
System Response: Validate SPOC roles and availability
Configuration: Role validation rules
API Used: GET /egov-hrms/employees/_search (with role filter)
```

**Step 3: Assignment Confirmation**
```
User Action: Confirm activity assignments
System Response: Create assignments and send notifications
Configuration: Notification templates
API Used: POST /v1/activities/_assign-spoc
```

#### **3.2.3 Configuration Requirements**

**Role Assignment Rules:**
```json
{
  "moduleName": "field-planner",
  "masterName": "role-assignment-rules",
  "data": {
    "INSTALLATION": {
      "requiredRoles": ["INSTALLATION_SPOC", "INSTALLATION_REVIEWER"],
      "maxAssignments": 1,
      "notificationTemplate": "installation-assignment"
    },
    "FIELD_QC": {
      "requiredRoles": ["FIELD_QC_SPOC", "FIELD_QC_REVIEWER"],
      "maxAssignments": 1,
      "notificationTemplate": "qc-assignment"
    }
  }
}
```

**Notification Templates:**
```json
{
  "moduleName": "field-planner",
  "masterName": "notification-templates",
  "data": {
    "installation-assignment": {
      "subject": "Installation Activity Assignment",
      "body": "You have been assigned as Installation SPOC for field plan {fieldPlanName}",
      "channels": ["email", "sms"]
    }
  }
}
```

### 3.3 UC-03: Assign Facilities to Field Staff

#### **3.3.1 Use Case Description**
**Primary Actor**: Activity SPOC
**Goal**: Assign specific health facilities to field staff for activity execution
**Preconditions**: 
- Activity is assigned to SPOC
- Field staff are available
- Facilities are in ACTIVE status for activity

#### **3.3.2 Workflow Steps**

**Step 1: Facility Assignment Interface**
```
User Action: Navigate to "Assign Facilities"
System Response: Display facility assignment dashboard
Configuration: Load facilities from HFR
API Used: GET /facility/v1/_search
```

**Step 2: Staff Selection**
```
User Action: Select field staff for facilities
System Response: Validate staff availability and permissions
Configuration: Staff validation rules
API Used: GET /egov-hrms/employees/_search (field staff)
```

**Step 3: Bulk Assignment**
```
User Action: Upload facility-staff assignment file
System Response: Process and validate assignments
Configuration: Assignment validation rules
API Used: POST /v1/activities/_assign-staff
```

#### **3.3.3 Configuration Requirements**

**Assignment Validation Rules:**
```json
{
  "moduleName": "field-planner",
  "masterName": "assignment-rules",
  "data": {
    "maxFacilitiesPerStaff": 10,
    "maxStaffPerFacility": 1,
    "geographicConstraints": true,
    "workloadBalancing": true
  }
}
```

**Conditional Activation Rules:**
```json
{
  "moduleName": "field-planner",
  "masterName": "activation-rules",
  "data": {
    "INSTALLATION": {
      "conditions": [
        {
          "type": "ASSESSMENT_COMPLETE",
          "value": "GO",
          "required": true
        },
        {
          "type": "STAFF_ASSIGNED",
          "value": true,
          "required": true
        }
      ]
    }
  }
}
```

### 3.4 UC-04: Submit Activity Reports

#### **3.4.1 Use Case Description**
**Primary Actor**: Field Staff
**Goal**: Submit activity reports for completed work at health facilities
**Preconditions**: 
- Facility is assigned to field staff
- Activity is in ACTIVE status
- Field staff has mobile app access

#### **3.4.2 Workflow Steps**

**Step 1: Mobile App Access**
```
User Action: Open mobile app and sync
System Response: Download assignments and master data
Configuration: Mobile sync settings
API Used: POST /field-planner/v1/mobile/sync/assignments/_bulk
```

**Step 2: Report Creation**
```
User Action: Create activity report in mobile app
System Response: Validate report data and attachments
Configuration: Report validation rules
API Used: POST /v1/activities/reports/_create
```

**Step 3: Report Submission**
```
User Action: Submit report for review
System Response: Initialize workflow and send notifications
Configuration: Workflow initialization rules
API Used: POST /field-planner/v1/activity-reports/_workflow
```

#### **3.4.3 Configuration Requirements**

**Report Validation Rules:**
```json
{
  "moduleName": "field-planner",
  "masterName": "report-validation",
  "data": {
    "INSTALLATION": {
      "requiredFields": ["facilityId", "installationDate", "equipmentDetails"],
      "requiredAttachments": ["installationPhotos", "completionCertificate"],
      "maxAttachments": 10,
      "maxFileSize": "10MB"
    }
  }
}
```

**Mobile Sync Configuration:**
```json
{
  "moduleName": "field-planner",
  "masterName": "mobile-sync",
  "data": {
    "syncInterval": 300,
    "maxRetries": 3,
    "conflictResolution": "SERVER_WINS",
    "offlineStorage": true
  }
}
```

### 3.5 UC-05: Review and Approve Reports

#### **3.5.1 Use Case Description**
**Primary Actor**: Activity Reviewer (QC SPOC, Installation Reviewer)
**Goal**: Review submitted activity reports and approve or reject them
**Preconditions**: 
- Activity report is submitted and in REVIEW status
- Reviewer is assigned to activity
- Report meets minimum review criteria

#### **3.5.2 Workflow Steps**

**Step 1: Report Review Interface**
```
User Action: Navigate to "Review Reports"
System Response: Display reports pending review
Configuration: Review dashboard settings
API Used: POST /field-planner/v1/activity-reports/_search
```

**Step 2: Report Examination**
```
User Action: Review report details and attachments
System Response: Display report with all details
Configuration: Report display settings
API Used: POST /v1/activities/reports/_search (by id)
```

**Step 3: Approval Decision**
```
User Action: Approve or reject report with comments
System Response: Update workflow status and notify stakeholders
Configuration: Approval workflow rules
API Used: POST /v1/activities/_workflow
```

#### **3.5.3 Configuration Requirements**

**Review Workflow Rules:**
```json
{
  "moduleName": "field-planner",
  "masterName": "review-workflow",
  "data": {
    "INSTALLATION": {
      "autoApproval": false,
      "requireComments": true,
      "slaHours": 48,
      "escalationRole": "PROJECT_MANAGER"
    },
    "FIELD_QC": {
      "autoApproval": false,
      "requireComments": true,
      "slaHours": 24,
      "escalationRole": "FIELD_QC_SPOC"
    }
  }
}
```

**Approval Criteria:**
```json
{
  "moduleName": "field-planner",
  "masterName": "approval-criteria",
  "data": {
    "INSTALLATION": {
      "requiredAttachments": ["photos", "certificate"],
      "dataCompleteness": 0.95,
      "photoQuality": "HIGH"
    }
  }
}
```

---

## 4. API Usage by Use Case

### 4.1 Field Planner Service APIs (Domain)

#### **4.1.1 Field Plan Management APIs**

| API Endpoint | Method | Use Cases | Configuration |
|--------------|--------|-----------|---------------|
| `/v1/field-plans/_create` | POST | UC-01 | Field plan naming rules, validation rules |
| `/v1/field-plans/_update` | POST | UC-01, UC-06 | Update validation rules |
| `/v1/field-plans/_search` | POST | UC-01, UC-02, UC-06 | Search filters, pagination |
| `/v1/field-plans/_workflow` | POST | UC-01, UC-06 | Workflow state transitions |
| `/v1/field-plans/facilities/_template` | GET | UC-01 | Template download |
| `/v1/field-plans/facilities/_upload` | POST | UC-01 | Template upload |
| `/v1/field-plans/facilities/_assign` | POST | UC-01 | Facility assignment rules |
| `/v1/field-plans/facilities/_unassign` | POST | UC-01 | Facility unassignment |

### 4.2 Activities Service APIs (Domain)

| API Endpoint | Method | Use Cases | Configuration |
|--------------|--------|-----------|---------------|
| `/v1/activities/_create` | POST | UC-01, UC-02 | Activity creation |
| `/v1/activities/_update` | POST | UC-01, UC-06 | Activity updates |
| `/v1/activities/_search` | POST | UC-01, UC-02, UC-06 | Activity search |
| `/v1/activities/_workflow` | POST | UC-04, UC-05, UC-06 | Workflow transitions |
| `/v1/activities/_assign-spoc` | POST | UC-02 | Assign or reassign SPOC |
| `/v1/activities/_assign-staff` | POST | UC-03 | Assign staff with roles |

#### **Activities Service - Activity Report APIs**

| API Endpoint | Method | Use Cases | Configuration |
|--------------|--------|-----------|---------------|
| `/v1/activities/reports/_create` | POST | UC-04 | Report validation rules |
| `/v1/activities/reports/_update` | POST | UC-04 | Update validation rules |
| `/v1/activities/reports/_search` | POST | UC-05, UC-06 | Search filters |

#### **4.1.4 Mobile Sync APIs**

| API Endpoint | Method | Use Cases | Configuration |
|--------------|--------|-----------|---------------|
| `/v1/mobile/sync/assignments/_bulk` | POST | UC-04, UC-07 | Mobile sync settings |
| `/v1/mobile/reports/_bulk_upload` | POST | UC-04, UC-07 | Bulk upload settings |
| `/v1/mobile/masterdata/_sync` | POST | UC-07 | Master data sync settings |

#### **4.1.5 Team Management APIs**

| API Endpoint | Method | Use Cases | Configuration |
|--------------|--------|-----------|---------------|
| `/v1/teams/assignments/_search` | POST | UC-02, UC-03, UC-10 | Team search filters |

### 4.4 Integration APIs

#### **4.2.1 HRMS Integration**

| API Endpoint | Method | Use Cases | Configuration |
|--------------|--------|-----------|---------------|
| `/egov-hrms/employees/_search` | GET | UC-02, UC-03 | Employee search filters |
| `/egov-hrms/employees/{id}` | GET | UC-02, UC-03 | Employee details |

#### **4.2.2 Health Facility Registry Integration**

| API Endpoint | Method | Use Cases | Configuration |
|--------------|--------|-----------|---------------|
| `/facility/v1/_search` | GET | UC-01, UC-03 | Facility search filters |
| `/facility/v1/{id}` | GET | UC-01, UC-03 | Facility details |

#### **4.2.3 Project Service Integration**

| API Endpoint | Method | Use Cases | Configuration |
|--------------|--------|-----------|---------------|
| `/project/v1/_search` | GET | UC-01 | Project search filters |
| `/project/v1/{id}` | GET | UC-01 | Project details |

#### **4.2.4 Workflow Service Integration**

| API Endpoint | Method | Use Cases | Configuration |
|--------------|--------|-----------|---------------|
| `/egov-wf-v2/process/_transition` | POST | UC-01, UC-04, UC-05 | Workflow state transitions |
| `/egov-wf-v2/process/_search` | POST | UC-06 | Workflow search filters |

#### **4.2.5 MDMS Integration**

| API Endpoint | Method | Use Cases | Configuration |
|--------------|--------|-----------|---------------|
| `/egov-mdms-service/v1/_search` | POST | UC-01, UC-02, UC-03 | Master data search |

#### **4.2.6 Filestore Integration**

| API Endpoint | Method | Use Cases | Configuration |
|--------------|--------|-----------|---------------|
| `/filestore/v1/files` | POST | UC-04 | File upload settings |
| `/filestore/v1/files/{id}` | GET | UC-04, UC-05 | File download settings |

---

## 5. Configuration Management

### 5.1 Master Data Configuration

#### **5.1.1 Activity Types Configuration**
```json
{
  "moduleName": "field-planner",
  "masterName": "activity-types",
  "data": [
    {
      "code": "INSTALLATION",
      "name": "Installation",
      "description": "DRE installation activity",
      "requiredRoles": ["INSTALLATION_SPOC", "INSTALLATION_REVIEWER"],
      "workflowCode": "field-plan-installation",
      "slaHours": 72,
      "autoActivation": false,
      "requiresApproval": true
    },
    {
      "code": "FIELD_QC",
      "name": "Field Quality Check",
      "description": "Field quality control activity",
      "requiredRoles": ["FIELD_QC_SPOC", "FIELD_QC_REVIEWER"],
      "workflowCode": "field-plan-qc",
      "slaHours": 24,
      "autoActivation": false,
      "requiresApproval": true
    },
    {
      "code": "HANDOVER",
      "name": "Handover",
      "description": "Facility handover activity",
      "requiredRoles": ["HANDOVER_SPOC"],
      "workflowCode": "field-plan-handover",
      "slaHours": 48,
      "autoActivation": true,
      "requiresApproval": false
    }
  ]
}
```

#### **5.1.2 Status Configuration**
```json
{
  "moduleName": "field-planner",
  "masterName": "status-codes",
  "data": [
    {
      "code": "DRAFT",
      "name": "Draft",
      "description": "Initial draft status",
      "editable": true,
      "workflowAllowed": ["SUBMIT"]
    },
    {
      "code": "ACTIVE",
      "name": "Active",
      "description": "Active for execution",
      "editable": false,
      "workflowAllowed": ["COMPLETE", "PAUSE"]
    },
    {
      "code": "COMPLETED",
      "name": "Completed",
      "description": "Successfully completed",
      "editable": false,
      "workflowAllowed": []
    },
    {
      "code": "REJECTED",
      "name": "Rejected",
      "description": "Rejected during review",
      "editable": true,
      "workflowAllowed": ["RESUBMIT"]
    }
  ]
}
```

#### **5.1.3 Role Configuration**
```json
{
  "moduleName": "field-planner",
  "masterName": "user-roles",
  "data": [
    {
      "code": "FIELD_PLANNER_ADMIN",
      "name": "Field Planner Admin",
      "description": "System administrator",
      "hierarchyLevel": 1,
      "canCreateUsers": true,
      "scope": "SYSTEM_WIDE"
    },
    {
      "code": "PROJECT_MANAGER",
      "name": "Project Manager",
      "description": "Project and field plan management",
      "hierarchyLevel": 2,
      "canCreateUsers": true,
      "scope": "PROJECT_LEVEL"
    },
    {
      "code": "INSTALLATION_SPOC",
      "name": "Installation SPOC",
      "description": "Installation activity management",
      "hierarchyLevel": 3,
      "canCreateUsers": true,
      "scope": "ACTIVITY_LEVEL"
    }
  ]
}
```

### 5.2 Workflow Configuration

#### **5.2.1 Field Plan Workflow**
```json
{
  "moduleName": "field-planner",
  "masterName": "workflow-definitions",
  "data": {
    "field-plan-workflow": {
      "name": "Field Plan Workflow",
      "description": "Workflow for field plan lifecycle",
      "states": [
        {
          "name": "DRAFT",
          "description": "Initial draft state",
          "actions": ["SUBMIT", "DELETE"],
          "roles": ["PROJECT_MANAGER", "FIELD_PLANNER_ADMIN"]
        },
        {
          "name": "ACTIVE",
          "description": "Active for execution",
          "actions": ["PAUSE", "COMPLETE"],
          "roles": ["PROJECT_MANAGER", "FIELD_PLANNER_ADMIN"]
        },
        {
          "name": "PAUSED",
          "description": "Temporarily paused",
          "actions": ["RESUME", "COMPLETE"],
          "roles": ["PROJECT_MANAGER", "FIELD_PLANNER_ADMIN"]
        },
        {
          "name": "COMPLETED",
          "description": "Successfully completed",
          "actions": [],
          "roles": []
        }
      ],
      "transitions": [
        {
          "from": "DRAFT",
          "to": "ACTIVE",
          "action": "SUBMIT",
          "conditions": ["VALIDATION_PASSED", "ACTIVITIES_ASSIGNED"]
        },
        {
          "from": "ACTIVE",
          "to": "PAUSED",
          "action": "PAUSE",
          "conditions": []
        },
        {
          "from": "PAUSED",
          "to": "ACTIVE",
          "action": "RESUME",
          "conditions": []
        },
        {
          "from": "ACTIVE",
          "to": "COMPLETED",
          "action": "COMPLETE",
          "conditions": ["ALL_ACTIVITIES_COMPLETED"]
        }
      ]
    }
  }
}
```

#### **5.2.2 Activity Report Workflow**
```json
{
  "moduleName": "field-planner",
  "masterName": "workflow-definitions",
  "data": {
    "activity-report-workflow": {
      "name": "Activity Report Workflow",
      "description": "Workflow for activity report lifecycle",
      "states": [
        {
          "name": "DRAFT",
          "description": "Initial draft state",
          "actions": ["SUBMIT", "DELETE"],
          "roles": ["FIELD_STAFF"]
        },
        {
          "name": "SUBMITTED",
          "description": "Submitted for review",
          "actions": ["APPROVE", "REJECT", "REQUEST_CHANGES"],
          "roles": ["INSTALLATION_REVIEWER", "FIELD_QC_REVIEWER"]
        },
        {
          "name": "IN_REVIEW",
          "description": "Under review",
          "actions": ["APPROVE", "REJECT", "REQUEST_CHANGES"],
          "roles": ["INSTALLATION_REVIEWER", "FIELD_QC_REVIEWER"]
        },
        {
          "name": "APPROVED",
          "description": "Approved",
          "actions": [],
          "roles": []
        },
        {
          "name": "REJECTED",
          "description": "Rejected",
          "actions": ["RESUBMIT"],
          "roles": ["FIELD_STAFF"]
        }
      ],
      "transitions": [
        {
          "from": "DRAFT",
          "to": "SUBMITTED",
          "action": "SUBMIT",
          "conditions": ["VALIDATION_PASSED"]
        },
        {
          "from": "SUBMITTED",
          "to": "IN_REVIEW",
          "action": "ASSIGN_REVIEWER",
          "conditions": ["REVIEWER_ASSIGNED"]
        },
        {
          "from": "IN_REVIEW",
          "to": "APPROVED",
          "action": "APPROVE",
          "conditions": []
        },
        {
          "from": "IN_REVIEW",
          "to": "REJECTED",
          "action": "REJECT",
          "conditions": ["COMMENTS_PROVIDED"]
        },
        {
          "from": "REJECTED",
          "to": "DRAFT",
          "action": "RESUBMIT",
          "conditions": []
        }
      ]
    }
  }
}
```

### 5.3 System Configuration

#### **5.3.1 Application Properties**
```properties
# Field Planner Service Configuration
field-planner.service.name=field-planner-service
field-planner.service.version=1.0.0

# Database Configuration
field-planner.db.url=jdbc:postgresql://localhost:5432/e4h_field_planner
field-planner.db.username=${DB_USERNAME}
field-planner.db.password=${DB_PASSWORD}

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

# Notification Configuration
field-planner.notification.email.enabled=true
field-planner.notification.sms.enabled=true
field-planner.notification.push.enabled=true
```

#### **5.3.2 Validation Configuration**
```json
{
  "moduleName": "field-planner",
  "masterName": "validation-rules",
  "data": {
    "fieldPlan": {
      "name": {
        "minLength": 3,
        "maxLength": 100,
        "pattern": "^[A-Z0-9_-]+$"
      },
      "startDate": {
        "required": true,
        "futureDate": true
      },
      "endDate": {
        "required": true,
        "afterStartDate": true
      }
    },
    "activityReport": {
      "facilityId": {
        "required": true,
        "validFacility": true
      },
      "activityType": {
        "required": true,
        "validActivity": true
      },
      "attachments": {
        "maxCount": 10,
        "maxSize": "10MB",
        "allowedTypes": ["image/jpeg", "image/png", "application/pdf"]
      }
    }
  }
}
```

---

## 6. Integration Points

### 6.1 HRMS Integration

#### **6.1.1 User Management**
- **Purpose**: Validate users and roles
- **APIs Used**: `/egov-hrms/employees/_search`
- **Configuration**: Role mapping, permission validation
- **Error Handling**: User not found, insufficient permissions

#### **6.1.2 Team Management**
- **Purpose**: Manage team assignments and hierarchies
- **APIs Used**: `/egov-hrms/employees/_search` (with supervisor filter)
- **Configuration**: Supervisor-subordinate relationships
- **Error Handling**: Invalid team structure, circular references

### 6.2 Health Facility Registry Integration

#### **6.2.1 Facility Validation**
- **Purpose**: Validate facility existence and status
- **APIs Used**: `/facility/v1/_search`
- **Configuration**: Facility status codes, geographic boundaries
- **Error Handling**: Facility not found, inactive facility

#### **6.2.2 Facility Data**
- **Purpose**: Retrieve facility details for assignments
- **APIs Used**: `/facility/v1/{id}`
- **Configuration**: Facility attributes, contact information
- **Error Handling**: Facility data incomplete, access denied

### 6.3 Project Service Integration

#### **6.3.1 Project Validation**
- **Purpose**: Validate project existence and scope
- **APIs Used**: `/project/v1/_search`
- **Configuration**: Project boundaries, facility mappings
- **Error Handling**: Project not found, scope mismatch

#### **6.3.2 Project Data**
- **Purpose**: Retrieve project details for field plans
- **APIs Used**: `/project/v1/{id}`
- **Configuration**: Project metadata, timeline information
- **Error Handling**: Project data incomplete, access denied

### 6.4 Workflow Service Integration

#### **6.4.1 State Management**
- **Purpose**: Manage workflow state transitions
- **APIs Used**: `/egov-wf-v2/process/_transition`
- **Configuration**: Workflow definitions, state rules
- **Error Handling**: Invalid transition, workflow not found

#### **6.4.2 Workflow Tracking**
- **Purpose**: Track workflow progress and history
- **APIs Used**: `/egov-wf-v2/process/_search`
- **Configuration**: Workflow search filters, history retention
- **Error Handling**: Workflow not found, access denied

### 6.5 MDMS Integration

#### **6.5.1 Master Data**
- **Purpose**: Retrieve configurable master data
- **APIs Used**: `/egov-mdms-service/v1/_search`
- **Configuration**: Module names, master data types
- **Error Handling**: Master data not found, invalid module

#### **6.5.2 Configuration Data**
- **Purpose**: Retrieve system configuration
- **APIs Used**: `/egov-mdms-service/v1/_search`
- **Configuration**: Configuration keys, default values
- **Error Handling**: Configuration not found, invalid format

### 6.6 Filestore Integration

#### **6.6.1 File Upload**
- **Purpose**: Store attachments and documents
- **APIs Used**: `/filestore/v1/files`
- **Configuration**: File size limits, allowed types
- **Error Handling**: File too large, invalid type

#### **6.6.2 File Download**
- **Purpose**: Retrieve stored files
- **APIs Used**: `/filestore/v1/files/{id}`
- **Configuration**: Access permissions, download limits
- **Error Handling**: File not found, access denied

---

## 7. Error Handling & Edge Cases

### 7.1 Validation Errors

#### **7.1.1 Field Plan Validation**
```json
{
  "errorCode": "FIELD_PLAN_VALIDATION_ERROR",
  "errorMessage": "Field plan validation failed",
  "details": [
    {
      "field": "name",
      "error": "Field plan name must be unique within project",
      "suggestion": "Use name format: {GEOGRAPHY}-{ACTIVITIES}-{YEAR}-{SEQUENCE}"
    },
    {
      "field": "startDate",
      "error": "Start date must be in the future",
      "suggestion": "Select a future date"
    }
  ]
}
```

#### **7.1.2 Activity Assignment Validation**
```json
{
  "errorCode": "ACTIVITY_ASSIGNMENT_ERROR",
  "errorMessage": "Activity assignment validation failed",
  "details": [
    {
      "field": "spocId",
      "error": "SPOC does not have required role",
      "suggestion": "Assign user with INSTALLATION_SPOC role"
    },
    {
      "field": "activityType",
      "error": "Activity type not supported in field plan",
      "suggestion": "Add activity type to field plan first"
    }
  ]
}
```

### 7.2 Business Rule Violations

#### **7.2.1 Workflow Violations**
```json
{
  "errorCode": "WORKFLOW_VIOLATION",
  "errorMessage": "Invalid workflow transition",
  "details": {
    "currentState": "DRAFT",
    "requestedAction": "COMPLETE",
    "allowedActions": ["SUBMIT", "DELETE"],
    "suggestion": "Submit field plan first to activate it"
  }
}
```

#### **7.2.2 Permission Violations**
```json
{
  "errorCode": "PERMISSION_DENIED",
  "errorMessage": "Insufficient permissions",
  "details": {
    "requiredRole": "PROJECT_MANAGER",
    "userRole": "FIELD_STAFF",
    "action": "CREATE_FIELD_PLAN",
    "suggestion": "Contact your project manager for field plan creation"
  }
}
```

### 7.3 Integration Errors

#### **7.3.1 Service Unavailable**
```json
{
  "errorCode": "SERVICE_UNAVAILABLE",
  "errorMessage": "Integration service unavailable",
  "details": {
    "service": "HRMS",
    "endpoint": "/egov-hrms/employees/_search",
    "retryAfter": 300,
    "suggestion": "Please try again in 5 minutes"
  }
}
```

#### **7.3.2 Data Inconsistency**
```json
{
  "errorCode": "DATA_INCONSISTENCY",
  "errorMessage": "Data inconsistency detected",
  "details": {
    "field": "facilityId",
    "issue": "Facility not found in HFR",
    "suggestion": "Verify facility ID or contact system administrator"
  }
}
```

### 7.4 Mobile Sync Errors

#### **7.4.1 Sync Conflicts**
```json
{
  "errorCode": "SYNC_CONFLICT",
  "errorMessage": "Data sync conflict detected",
  "details": {
    "conflictType": "SERVER_WINS",
    "localChanges": 5,
    "serverChanges": 3,
    "resolution": "Server data preserved, local changes discarded"
  }
}
```

#### **7.4.2 Offline Sync**
```json
{
  "errorCode": "OFFLINE_SYNC",
  "errorMessage": "Offline sync in progress",
  "details": {
    "pendingChanges": 10,
    "estimatedTime": "2 minutes",
    "suggestion": "Please wait for sync to complete"
  }
}
```

---

## 8. Performance Considerations

### 8.1 API Performance

#### **8.1.1 Response Time Targets**
- **Field Plan Creation**: < 2 seconds
- **Activity Assignment**: < 1 second
- **Report Submission**: < 3 seconds (with file upload)
- **Search Operations**: < 500ms
- **Mobile Sync**: < 5 seconds

#### **8.1.2 Bulk Operations**
- **Bulk Field Plan Creation**: 1000 plans in < 30 seconds
- **Bulk Activity Assignment**: 1000 assignments in < 15 seconds
- **Bulk Report Upload**: 100 reports in < 60 seconds

### 8.2 Database Performance

#### **8.2.1 Indexing Strategy**
```sql
-- Field Plan Indexes
CREATE INDEX idx_field_plans_project_id ON field_plans(project_id);
CREATE INDEX idx_field_plans_status ON field_plans(status);
CREATE INDEX idx_field_plans_created_date ON field_plans(created_date);

-- Activity Assignment Indexes
CREATE INDEX idx_activity_assignments_field_plan_id ON activity_assignments(field_plan_id);
CREATE INDEX idx_activity_assignments_spoc_id ON activity_assignments(spoc_id);
CREATE INDEX idx_activity_assignments_activity_type ON activity_assignments(activity_type);

-- Facility Activity Indexes
CREATE INDEX idx_facility_activities_field_plan_id ON facility_activities(field_plan_id);
CREATE INDEX idx_facility_activities_facility_id ON facility_activities(facility_id);
CREATE INDEX idx_facility_activities_status ON facility_activities(status);

-- Activity Report Indexes
CREATE INDEX idx_activity_reports_facility_id ON activity_reports(facility_id);
CREATE INDEX idx_activity_reports_activity_type ON activity_reports(activity_type);
CREATE INDEX idx_activity_reports_status ON activity_reports(status);
CREATE INDEX idx_activity_reports_created_date ON activity_reports(created_date);
```

#### **8.2.2 Query Optimization**
- **Pagination**: All search APIs support pagination (default: 10 items)
- **Filtering**: Efficient filtering by status, date range, user
- **Sorting**: Optimized sorting by creation date, status
- **Caching**: Redis caching for frequently accessed data

### 8.3 Mobile Performance

#### **8.3.1 Sync Optimization**
- **Differential Sync**: Only sync changed data since last sync
- **Compression**: GZIP compression for mobile data transfer
- **Batch Processing**: Process multiple records in single request
- **Background Sync**: Non-blocking sync operations

#### **8.3.2 Offline Performance**
- **Local Storage**: SQLite database for offline data
- **Conflict Resolution**: Intelligent conflict resolution strategies
- **Data Compression**: Compress offline data to save storage
- **Sync Queuing**: Queue sync operations when offline

### 8.4 Scalability Considerations

#### **8.4.1 Horizontal Scaling**
- **Service Replication**: Multiple Field Planner service instances
- **Database Sharding**: Shard by tenant or geographic region
- **Load Balancing**: Distribute requests across instances
- **Auto-scaling**: Scale based on CPU/memory usage

#### **8.4.2 Caching Strategy**
- **Redis Clustering**: Distributed Redis for high availability
- **Cache Invalidation**: Topic-based cache invalidation
- **Cache Warming**: Pre-load frequently accessed data
- **Cache Partitioning**: Partition cache by tenant

---

## 9. Summary

### 9.1 Key Takeaways

1. **Comprehensive Coverage**: All 15 use cases are fully documented with workflows, APIs, and configurations
2. **Integration-First**: Heavy reliance on existing E4H platform services for consistency
3. **Configuration-Driven**: All business rules and validations are externalized to MDMS
4. **Mobile-Optimized**: Offline-first design with intelligent sync strategies
5. **Performance-Focused**: Clear performance targets and optimization strategies

### 9.2 Implementation Readiness

- **API Specifications**: Complete OpenAPI 3.0 specifications
- **Database Schema**: Full DDL with indexes and constraints
- **Workflow Definitions**: Complete workflow JSON configurations
- **Master Data**: All required MDMS configurations
- **Integration Points**: All external service integrations defined

### 9.3 Next Steps

1. **Architecture Review**: Validate design decisions with architect
2. **Implementation Planning**: Create detailed implementation roadmap
3. **Testing Strategy**: Define comprehensive testing approach
4. **Deployment Planning**: Plan production deployment strategy
5. **Monitoring Setup**: Define monitoring and alerting requirements

---

**Document Version**: 1.0  
**Last Updated**: 2025-01-21  
**Next Review**: 2025-02-21
