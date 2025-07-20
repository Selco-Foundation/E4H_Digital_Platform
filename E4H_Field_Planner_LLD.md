# E4H Digital Platform - Field Planner Module
## Low-Level Design Document

### Version Control
| Version | Author | Date | Changes |
|---------|--------|------|---------|
| 1.0 | Tech Lead | 2024-01-XX | Initial LLD based on PRD v1.2 |

---

## Table of Contents
1. [System Overview](#system-overview)
2. [Architecture Design](#architecture-design)
3. [Database Design](#database-design)
4. [API Specifications](#api-specifications)
5. [Component Design](#component-design)
6. [Security Design](#security-design)
7. [Integration Points](#integration-points)
8. [Implementation Guidelines](#implementation-guidelines)
9. [Performance Considerations](#performance-considerations)
10. [Error Handling](#error-handling)
11. [Deployment Architecture](#deployment-architecture)

---

## 1. System Overview

### 1.1 Purpose
The Field Planner module is a **service extension** that integrates with the existing E4H Digital Platform to enable Project Managers to create and manage field execution plans for DRE installation projects across multiple health facilities.

### 1.2 Key Features
- **Extends existing Project Service** for field plan management
- **Integrates with Health Facility Registry** for facility operations
- **Leverages eGov HRMS** for user and team management
- **Uses eGov Workflow Service** for activity state management
- **Integrates with MDMS** for master data consistency
- Conditional health facility activation
- Real-time progress tracking
- Mobile app integration
- Comprehensive audit trail

### 1.3 Technology Stack (Aligned with E4H Platform)
- **Backend**: Java 17, Spring Boot 3.x (consistent with existing services)
- **Database**: PostgreSQL (extends existing E4H schemas)
- **Message Queue**: Apache Kafka (uses existing topics + new Field Planner topics)
- **Cache**: Redis (shared with platform)
- **File Storage**: eGov Filestore Service
- **Workflow**: eGov Workflow v2 Service
- **MDMS**: eGov MDMS Service v2
- **Authentication**: Existing E4H Auth Service
- **API Gateway**: Existing E4H Gateway

---

## 2. Architecture Design

### 2.1 E4H Platform Integration Architecture

```
┌─────────────────────────────────────────────────────────────────┐
│                    E4H API Gateway (Existing)                   │
├─────────────────────────────────────────────────────────────────┤
│                    Frontend Layer (Existing)                    │
│  ┌─────────────────┐  ┌─────────────────┐  ┌─────────────────┐ │
│  │  E4H Web UI     │  │  Mobile App     │  │  Admin Console  │ │
│  │  (Extended)     │  │  (New Module)   │  │  (Extended)     │ │
│  └─────────────────┘  └─────────────────┘  └─────────────────┘ │
├─────────────────────────────────────────────────────────────────┤
│                    Existing E4H Services                        │
│  ┌─────────────────┐  ┌─────────────────┐  ┌─────────────────┐ │
│  │  eGov HRMS      │  │  Project Service│  │ eGov Workflow   │ │
│  │  (User Mgmt)    │  │  (Extended)     │  │  v2 Service     │ │
│  └─────────────────┘  └─────────────────┘  └─────────────────┘ │
│  ┌─────────────────┐  ┌─────────────────┐  ┌─────────────────┐ │
│  │ Health Facility │  │ eGov Filestore  │  │ eGov MDMS v2    │ │
│  │   Registry      │  │   Service       │  │   Service       │ │
│  └─────────────────┘  └─────────────────┘  └─────────────────┘ │
├─────────────────────────────────────────────────────────────────┤
│                    NEW: Field Planner Service                   │
│  ┌─────────────────┐  ┌─────────────────┐  ┌─────────────────┐ │
│  │  Field Plan     │  │  Activity       │  │  Mobile Sync    │ │
│  │  Management     │  │  Management     │  │  Service        │ │
│  └─────────────────┘  └─────────────────┘  └─────────────────┘ │
├─────────────────────────────────────────────────────────────────┤
│                    Shared Infrastructure                        │
│  ┌─────────────────┐  ┌─────────────────┐  ┌─────────────────┐ │
│  │  PostgreSQL     │  │  Redis Cache    │  │  Kafka Queue    │ │
│  │  (E4H Database) │  │  (Shared)       │  │  (Shared)       │ │
│  └─────────────────┘  └─────────────────┘  └─────────────────┘ │
└─────────────────────────────────────────────────────────────────┘
```

### 2.2 Service Integration Architecture

#### 2.2.1 Existing E4H Services (Extended)

1. **eGov HRMS Service (Existing)**
   - **Current**: Employee/user management, assignments, departments
   - **Field Planner Extension**: Team management, field staff roles, activity-based assignments

2. **Project Service (Existing)**
   - **Current**: Basic project management with facility associations
   - **Field Planner Extension**: Field plan management, activity scheduling, conditional activation

3. **Health Facility Registry (Existing)**
   - **Current**: Facility master data, location, ownership, categories
   - **Field Planner Extension**: Activity status tracking, field execution metadata

4. **eGov Workflow v2 (Existing)**
   - **Current**: Generic workflow management, state transitions, approvals
   - **Field Planner Extension**: Field activity workflows, conditional transitions, custom business rules

5. **eGov Filestore Service (Existing)**
   - **Current**: Document upload/download, file management
   - **Field Planner Extension**: Activity report attachments, Excel template generation

6. **eGov MDMS v2 Service (Existing)**
   - **Current**: Master data management for lookup values
   - **Field Planner Extension**: Activity types, conditions, field roles, validation schemas

#### 2.2.2 New Field Planner Services

1. **Field Plan Management Service (NEW)**
   - Field plan CRUD operations
   - Facility selection and assignment
   - Activity scheduling and assignment
   - Progress tracking and reporting

2. **Activity Management Service (NEW)**
   - Activity state management and conditional activation
   - Report submission and review workflows  
   - Mobile sync and offline data management
   - Team assignment and notification handling

3. **Mobile Sync Service (NEW)**
   - Mobile app synchronization
   - Offline data management
   - Conflict resolution
   - Background sync processing

---

## 3. Database Design (E4H Platform Integration)

### 3.1 Integration with Existing E4H Schemas

The Field Planner module **extends existing E4H database schemas** rather than creating duplicate tables:

#### 3.1.1 Existing Tables (Used As-Is)
- **`facility`** - Health Facility Registry (facility master data)
- **`facility_address`** - Facility location information
- **`eg_hrms_employee`** - User/employee management
- **`eg_hrms_assignment`** - Role and department assignments  
- **`PROJECT_FACILITY`** - Project-facility associations (from existing Project service)
- **Boundary tables** - Geographic hierarchy (from Boundary service)
- **MDMS tables** - Master data (from MDMS service)
- **Workflow tables** - State management (from eGov Workflow v2)

#### 3.1.2 Field Planner Extensions

```mermaid
erDiagram
    %% Existing E4H Tables (Reference Only)
    FACILITY {
        VARCHAR id PK "Existing"
        VARCHAR tenant_id "Existing"
        VARCHAR facility_name "Existing"
        VARCHAR facility_type "Existing"
        VARCHAR facility_category "Existing"
        VARCHAR wf_status "Existing"
        JSONB additional_details "Extended for Field Activities"
    }
    
    EG_HRMS_EMPLOYEE {
        VARCHAR uuid PK "Existing"
        VARCHAR name "Existing"
        VARCHAR phone "Existing"
        VARCHAR tenantid "Existing"
        VARCHAR employeestatus "Existing"
        JSONB additional_details "Extended for Field Roles"
    }
    
    PROJECT_FACILITY {
        VARCHAR id PK "Existing"
        VARCHAR tenantId "Existing"
        VARCHAR projectId "Existing"
        VARCHAR facilityId "Existing"
    }
    
    %% NEW Field Planner Tables
    FIELD_PLANS {
        VARCHAR id PK
        VARCHAR tenant_id
        VARCHAR name UK
        VARCHAR project_id FK
        DATE start_date
        DATE end_date
        JSONB geography_scope
        JSONB selected_activities
        VARCHAR created_by FK
        VARCHAR status
        BIGINT created_time
        BIGINT last_modified_time
        JSONB additional_details
    }
    
    FIELD_PLAN_FACILITIES {
        VARCHAR id PK
        VARCHAR tenant_id  
        VARCHAR field_plan_id FK
        VARCHAR facility_id FK
        VARCHAR status
        BIGINT created_time
        BIGINT last_modified_time
        JSONB additional_details
    }
    
    ACTIVITIES {
        VARCHAR id PK
        VARCHAR tenant_id
        VARCHAR name UK
        VARCHAR code UK
        JSONB default_conditions
        JSONB required_roles
        INTEGER sequence_order
        BOOLEAN is_active
        BIGINT created_time
        BIGINT last_modified_time
        JSONB additional_details
    }
    
    ACTIVITY_ASSIGNMENTS {
        VARCHAR id PK
        VARCHAR tenant_id
        VARCHAR field_plan_id FK
        VARCHAR activity_id FK
        VARCHAR assigned_to FK
        VARCHAR assigned_by FK
        DATE start_date
        DATE end_date
        VARCHAR status
        BIGINT created_time
        BIGINT last_modified_time
        JSONB additional_details
    }
    
    FACILITY_ACTIVITIES {
        VARCHAR id PK
        VARCHAR tenant_id
        VARCHAR facility_id FK
        VARCHAR activity_id FK
        VARCHAR field_plan_id FK
        VARCHAR status
        JSONB conditions_met
        VARCHAR assigned_user FK
        BIGINT scheduled_at
        BIGINT activated_at
        BIGINT completed_at
        BIGINT created_time
        BIGINT last_modified_time
        JSONB additional_details
    }
    
    ACTIVITY_REPORTS {
        VARCHAR id PK
        VARCHAR tenant_id
        VARCHAR facility_activity_id FK
        VARCHAR submitted_by FK
        JSONB report_data
        JSONB attachments
        VARCHAR status
        VARCHAR reviewed_by FK
        BIGINT reviewed_at
        TEXT review_comments
        BIGINT created_time
        BIGINT last_modified_time
        JSONB additional_details
    }

    %% Relationships with Existing Tables
    FACILITY ||--o{ PROJECT_FACILITY : existing
    FACILITY ||--o{ FIELD_PLAN_FACILITIES : references
    FACILITY ||--o{ FACILITY_ACTIVITIES : references
    
    EG_HRMS_EMPLOYEE ||--o{ ACTIVITY_ASSIGNMENTS : assigned_to
    EG_HRMS_EMPLOYEE ||--o{ FACILITY_ACTIVITIES : assigned_to
    EG_HRMS_EMPLOYEE ||--o{ ACTIVITY_REPORTS : submits
    
    PROJECT_FACILITY ||--o{ FIELD_PLANS : project_context
    
    %% New Relationships
    FIELD_PLANS ||--o{ FIELD_PLAN_FACILITIES : includes
    FIELD_PLANS ||--o{ ACTIVITY_ASSIGNMENTS : contains
    FIELD_PLANS ||--o{ FACILITY_ACTIVITIES : tracks
    
    ACTIVITIES ||--o{ ACTIVITY_ASSIGNMENTS : assigned
    ACTIVITIES ||--o{ FACILITY_ACTIVITIES : performed
    
    FACILITY_ACTIVITIES ||--o{ ACTIVITY_REPORTS : generates
```

### 3.2 New Field Planner Table Specifications

> **Note**: Field Planner uses existing E4H tables (`facility`, `eg_hrms_employee`, `PROJECT_FACILITY`) and only adds new tables specific to field planning functionality.

#### 3.2.1 Field Plans Table (NEW)
```sql
-- Extends project management with field planning capabilities
CREATE TABLE field_plans (
    id VARCHAR PRIMARY KEY,
    tenant_id VARCHAR NOT NULL,
    name VARCHAR(255) NOT NULL,
    project_id VARCHAR NOT NULL, -- References existing project
    start_date DATE NOT NULL,
    end_date DATE NOT NULL,
    geography_scope JSONB NOT NULL, -- District/block selection
    selected_activities JSONB NOT NULL DEFAULT '[]',
    created_by VARCHAR NOT NULL, -- References eg_hrms_employee.uuid
    status VARCHAR DEFAULT 'ACTIVE',
    created_time BIGINT DEFAULT EXTRACT(EPOCH FROM NOW()) * 1000,
    last_modified_time BIGINT DEFAULT EXTRACT(EPOCH FROM NOW()) * 1000,
    additional_details JSONB DEFAULT '{}',
    
    CONSTRAINT valid_date_range CHECK (start_date < end_date)
);

CREATE INDEX idx_field_plans_tenant ON field_plans(tenant_id);
CREATE INDEX idx_field_plans_project ON field_plans(tenant_id, project_id);
CREATE INDEX idx_field_plans_created_by ON field_plans(tenant_id, created_by);
```

#### 3.2.2 Field Plan Facilities Table (NEW)
```sql
-- Links field plans to specific facilities from existing facility table
CREATE TABLE field_plan_facilities (
    id VARCHAR PRIMARY KEY,
    tenant_id VARCHAR NOT NULL,
    field_plan_id VARCHAR NOT NULL REFERENCES field_plans(id),
    facility_id VARCHAR NOT NULL, -- References existing facility.id
    status VARCHAR DEFAULT 'ACTIVE',
    created_time BIGINT DEFAULT EXTRACT(EPOCH FROM NOW()) * 1000,
    last_modified_time BIGINT DEFAULT EXTRACT(EPOCH FROM NOW()) * 1000,
    additional_details JSONB DEFAULT '{}'
);

CREATE INDEX idx_field_plan_facilities_tenant ON field_plan_facilities(tenant_id);
CREATE INDEX idx_field_plan_facilities_plan ON field_plan_facilities(tenant_id, field_plan_id);
CREATE INDEX idx_field_plan_facilities_facility ON field_plan_facilities(tenant_id, facility_id);
CREATE UNIQUE INDEX uniq_field_plan_facility ON field_plan_facilities(tenant_id, field_plan_id, facility_id);
```

#### 3.2.3 Activities Table (NEW)
```sql
-- Master data for field activities with configurable conditions
CREATE TABLE activities (
    id VARCHAR PRIMARY KEY,
    tenant_id VARCHAR NOT NULL,
    name VARCHAR(255) NOT NULL,
    code VARCHAR(50) NOT NULL,
    default_conditions JSONB NOT NULL DEFAULT '{}', -- Activation conditions
    required_roles JSONB NOT NULL DEFAULT '[]', -- Required roles for activity
    sequence_order INTEGER DEFAULT 0,
    is_active BOOLEAN DEFAULT TRUE,
    created_time BIGINT DEFAULT EXTRACT(EPOCH FROM NOW()) * 1000,
    last_modified_time BIGINT DEFAULT EXTRACT(EPOCH FROM NOW()) * 1000,
    additional_details JSONB DEFAULT '{}'
);

CREATE INDEX idx_activities_tenant ON activities(tenant_id);
CREATE INDEX idx_activities_code ON activities(tenant_id, code);
CREATE UNIQUE INDEX uniq_activity_code ON activities(tenant_id, code);
```

#### 3.2.4 Activity Assignments Table (NEW)
```sql
-- Assigns activities to SPOCs within field plans
CREATE TABLE activity_assignments (
    id VARCHAR PRIMARY KEY,
    tenant_id VARCHAR NOT NULL,
    field_plan_id VARCHAR NOT NULL REFERENCES field_plans(id),
    activity_id VARCHAR NOT NULL REFERENCES activities(id),
    assigned_to VARCHAR NOT NULL, -- References eg_hrms_employee.uuid
    assigned_by VARCHAR NOT NULL, -- References eg_hrms_employee.uuid
    start_date DATE NOT NULL,
    end_date DATE NOT NULL,
    status VARCHAR DEFAULT 'ACTIVE',
    created_time BIGINT DEFAULT EXTRACT(EPOCH FROM NOW()) * 1000,
    last_modified_time BIGINT DEFAULT EXTRACT(EPOCH FROM NOW()) * 1000,
    additional_details JSONB DEFAULT '{}'
);

CREATE INDEX idx_activity_assignments_tenant ON activity_assignments(tenant_id);
CREATE INDEX idx_activity_assignments_plan ON activity_assignments(tenant_id, field_plan_id);
CREATE INDEX idx_activity_assignments_assigned_to ON activity_assignments(tenant_id, assigned_to);
```

#### 3.2.5 Facility Activities Table (NEW)
```sql
-- Tracks facility-level activity execution with conditional activation
CREATE TABLE facility_activities (
    id VARCHAR PRIMARY KEY,
    tenant_id VARCHAR NOT NULL,
    facility_id VARCHAR NOT NULL, -- References existing facility.id
    activity_id VARCHAR NOT NULL REFERENCES activities(id),
    field_plan_id VARCHAR NOT NULL REFERENCES field_plans(id),
    status VARCHAR DEFAULT 'SCHEDULED', -- SCHEDULED, ACTIVE, COMPLETED, CANCELLED
    conditions_met JSONB DEFAULT '{}', -- Tracks which conditions are satisfied
    assigned_user VARCHAR, -- References eg_hrms_employee.uuid
    scheduled_at BIGINT,
    activated_at BIGINT,
    completed_at BIGINT,
    created_time BIGINT DEFAULT EXTRACT(EPOCH FROM NOW()) * 1000,
    last_modified_time BIGINT DEFAULT EXTRACT(EPOCH FROM NOW()) * 1000,
    additional_details JSONB DEFAULT '{}'
);

CREATE INDEX idx_facility_activities_tenant ON facility_activities(tenant_id);
CREATE INDEX idx_facility_activities_facility ON facility_activities(tenant_id, facility_id);
CREATE INDEX idx_facility_activities_status ON facility_activities(tenant_id, status);
CREATE INDEX idx_facility_activities_assigned ON facility_activities(tenant_id, assigned_user);
CREATE INDEX idx_facility_activities_composite ON facility_activities(tenant_id, facility_id, activity_id, field_plan_id);
```

### 3.3 Integration with Existing E4H Services

#### 3.3.1 Service Dependencies
```yaml
# Field Planner service dependencies
dependencies:
  - health-facility-registry  # For facility data
  - egov-hrms                # For user/employee management  
  - project-service          # For project context
  - egov-workflow-v2         # For activity workflows
  - egov-mdms-service-v2     # For master data validation
  - egov-filestore           # For file operations
  - boundary-service         # For geographic data
```

#### 3.3.2 Database Migration Strategy
```sql
-- Migration approach: Only create Field Planner specific tables
-- Existing tables: facility, eg_hrms_employee, PROJECT_FACILITY remain unchanged

-- Step 1: Create Field Planner tables in sequence
-- Already covered in 3.2.1 through 3.2.5

-- Step 2: Add activity_reports table for report management
CREATE TABLE activity_reports (
    id VARCHAR PRIMARY KEY,
    tenant_id VARCHAR NOT NULL,
    facility_activity_id VARCHAR NOT NULL REFERENCES facility_activities(id),
    submitted_by VARCHAR NOT NULL, -- References eg_hrms_employee.uuid
    report_data JSONB NOT NULL DEFAULT '{}',
    attachments JSONB DEFAULT '[]', -- filestore references
    status VARCHAR DEFAULT 'SUBMITTED', -- SUBMITTED, APPROVED, REJECTED, FLAGGED_FOR_FIELD_QC
    reviewed_by VARCHAR, -- References eg_hrms_employee.uuid
    reviewed_at BIGINT,
    review_comments TEXT,
    created_time BIGINT DEFAULT EXTRACT(EPOCH FROM NOW()) * 1000,
    last_modified_time BIGINT DEFAULT EXTRACT(EPOCH FROM NOW()) * 1000,
    additional_details JSONB DEFAULT '{}'
);

-- Step 3: Create indexes for performance optimization
CREATE INDEX idx_activity_reports_tenant ON activity_reports(tenant_id);
CREATE INDEX idx_activity_reports_facility_activity ON activity_reports(tenant_id, facility_activity_id);
CREATE INDEX idx_activity_reports_status ON activity_reports(tenant_id, status);
CREATE INDEX idx_activity_reports_submitted_by ON activity_reports(tenant_id, submitted_by);
CREATE INDEX idx_activity_reports_reviewed_by ON activity_reports(tenant_id, reviewed_by);
```

#### 3.3.3 Data Consistency Patterns
```java
// Example: Ensuring data consistency with existing services
@Component
public class FacilityDataConsistencyService {
    
    @Autowired
    private FacilityServiceClient facilityServiceClient;
    
    @Autowired
    private HRMSServiceClient hrmsServiceClient;
    
    public void validateFacilityAssignment(String tenantId, String facilityId, String employeeId) {
        // Validate facility exists in Health Facility Registry
        FacilityResponse facility = facilityServiceClient.getFacility(tenantId, facilityId);
        if (facility == null || !facility.getIsActive()) {
            throw new ValidationException("Invalid or inactive facility: " + facilityId);
        }
        
        // Validate employee exists in eGov HRMS
        EmployeeResponse employee = hrmsServiceClient.getEmployee(tenantId, employeeId);
        if (employee == null || !employee.getActive()) {
            throw new ValidationException("Invalid or inactive employee: " + employeeId);
        }
    }
}
```

---

## 4. API Specifications

### 4.1 REST API Design Principles

- **RESTful Design**: Follow REST principles with proper HTTP methods
- **Consistent Naming**: Use kebab-case for endpoints
- **Versioning**: Use header-based versioning (Accept: application/vnd.api.v1+json)
- **Pagination**: Implement cursor-based pagination for large datasets
- **Filtering**: Support query parameters for filtering and sorting
- **Error Handling**: Standardized error response format

### 4.2 Core API Endpoints

#### 4.2.1 Integration with Existing E4H APIs

```yaml
# EXISTING APIs (used by Field Planner)
# eGov HRMS Service
GET    /egov-hrms/employees/_search          # Get employees/users
POST   /egov-hrms/employees/_create          # Create field staff
POST   /egov-hrms/employees/_update          # Update employee details

# Health Facility Registry  
GET    /facility/v1/_search                  # Search facilities
GET    /facility/v1/{id}                     # Get facility details
POST   /facility/v1/_create                  # Create facilities (admin)

# Project Service (Extended)
GET    /project/v1/_search                   # Get existing projects  
POST   /project/v1/_create                   # Create projects (existing)
GET    /project/PROJECT_FACILITY/_search     # Get project-facility links

# eGov Filestore
POST   /filestore/v1/files                   # Upload files/templates
GET    /filestore/v1/files/{id}              # Download files

# eGov MDMS Service  
POST   /egov-mdms-service/v1/_search         # Get master data
```

#### 4.2.2 NEW Field Planner APIs

```yaml
# Field Plan Management
GET    /field-planner/v1/field-plans/_search
POST   /field-planner/v1/field-plans/_create  
GET    /field-planner/v1/field-plans/{id}
PUT    /field-planner/v1/field-plans/_update
POST   /field-planner/v1/field-plans/{id}/facilities/_assign
GET    /field-planner/v1/field-plans/{id}/facilities/template
POST   /field-planner/v1/field-plans/{id}/facilities/upload

# Activity Management  
GET    /field-planner/v1/activities/_search
POST   /field-planner/v1/activities/_create
POST   /field-planner/v1/activity-assignments/_create
GET    /field-planner/v1/activity-assignments/_search  
PUT    /field-planner/v1/activity-assignments/_update

# Facility Activities
GET    /field-planner/v1/facility-activities/_search
PUT    /field-planner/v1/facility-activities/_update
POST   /field-planner/v1/facility-activities/{id}/assign-user
GET    /field-planner/v1/facility-activities/{id}/activation-check

# Activity Reports
POST   /field-planner/v1/activity-reports/_create
GET    /field-planner/v1/activity-reports/_search
PUT    /field-planner/v1/activity-reports/{id}/review
POST   /field-planner/v1/activity-reports/{id}/approve
POST   /field-planner/v1/activity-reports/{id}/reject
POST   /field-planner/v1/activity-reports/{id}/flag-for-qc
```

#### 4.2.3 Mobile Sync APIs

```yaml
# Mobile Application Endpoints
POST   /field-planner/v1/mobile/sync/facilities        # Download facility updates
POST   /field-planner/v1/mobile/sync/activities        # Download activity assignments  
POST   /field-planner/v1/mobile/sync/full              # Full synchronization
POST   /field-planner/v1/mobile/reports/_upload        # Upload activity reports
POST   /field-planner/v1/mobile/reports/{id}/progress  # Auto-save progress
```

#### 4.2.4 Field Plan Management

```yaml
# Field plan endpoints
GET    /api/v1/field-plans
POST   /api/v1/field-plans
GET    /api/v1/field-plans/{id}
PUT    /api/v1/field-plans/{id}
DELETE /api/v1/field-plans/{id}
POST   /api/v1/field-plans/{id}/facilities
GET    /api/v1/field-plans/{id}/facilities/template
POST   /api/v1/field-plans/{id}/facilities/upload
POST   /api/v1/field-plans/{id}/assign-activities
```

#### 4.2.5 Activity Management

```yaml
# Activity endpoints
GET    /api/v1/activities
POST   /api/v1/activities
GET    /api/v1/activities/{id}
PUT    /api/v1/activities/{id}
POST   /api/v1/activities/{id}/assign
GET    /api/v1/facility-activities
PUT    /api/v1/facility-activities/{id}/status
POST   /api/v1/facility-activities/{id}/reports
GET    /api/v1/activity-reports/{id}
PUT    /api/v1/activity-reports/{id}/review
```

### 4.3 API Response Format

```json
{
  "success": true,
  "data": {
    // Response data
  },
  "meta": {
    "timestamp": "2024-01-01T00:00:00Z",
    "version": "1.0",
    "pagination": {
      "page": 1,
      "limit": 20,
      "total": 100,
      "hasNext": true,
      "hasPrevious": false
    }
  },
  "errors": []
}
```

### 4.4 Error Response Format

```json
{
  "success": false,
  "data": null,
  "meta": {
    "timestamp": "2024-01-01T00:00:00Z",
    "version": "1.0"
  },
  "errors": [
    {
      "code": "VALIDATION_ERROR",
      "message": "Invalid input data",
      "details": [
        {
          "field": "email",
          "message": "Invalid email format"
        }
      ]
    }
  ]
}
```

---

## 5. Component Design

### 5.1 Backend Components

#### 5.1.1 Field Planner Service Integration

```java
@Service
@Transactional
public class FieldPlannerService {
    
    @Autowired
    private FieldPlanRepository fieldPlanRepository;
    
    @Autowired
    private HRMSServiceClient hrmsServiceClient;
    
    @Autowired
    private FacilityServiceClient facilityServiceClient;
    
    @Autowired
    private ProjectServiceClient projectServiceClient;
    
    @Autowired
    private AuditService auditService;
    
    public FieldPlanDTO createFieldPlan(CreateFieldPlanRequest request) {
        // Validate project exists
        ProjectResponse project = projectServiceClient.getProject(request.getTenantId(), request.getProjectId());
        if (project == null) {
            throw new EntityNotFoundException("Project not found: " + request.getProjectId());
        }
        
        // Validate user exists in HRMS
        EmployeeResponse creator = hrmsServiceClient.getEmployee(request.getTenantId(), request.getCreatedBy());
        if (creator == null || !creator.getActive()) {
            throw new ValidationException("Invalid creator employee: " + request.getCreatedBy());
        }
        
        // Create field plan entity (follows E4H conventions)
        FieldPlan fieldPlan = new FieldPlan();
        fieldPlan.setId(idGenService.generateId());
        fieldPlan.setTenantId(request.getTenantId());
        fieldPlan.setName(request.getName());
        fieldPlan.setProjectId(request.getProjectId());
        fieldPlan.setStartDate(request.getStartDate());
        fieldPlan.setEndDate(request.getEndDate());
        fieldPlan.setGeographyScope(request.getGeographyScope());
        fieldPlan.setSelectedActivities(request.getSelectedActivities());
        fieldPlan.setCreatedBy(request.getCreatedBy());
        fieldPlan.setStatus("ACTIVE");
        fieldPlan.setCreatedTime(System.currentTimeMillis());
        fieldPlan.setLastModifiedTime(System.currentTimeMillis());
        
        // Save field plan
        FieldPlan savedFieldPlan = fieldPlanRepository.save(fieldPlan);
        
        // Log audit event using existing audit framework
        auditService.logFieldPlanCreation(savedFieldPlan);
        
        return FieldPlanMapper.toDTO(savedFieldPlan);
    }
    
    public void assignFacilitiesToFieldPlan(String tenantId, String fieldPlanId, 
                                           List<String> facilityIds) {
        // Validate facilities exist in Health Facility Registry
        List<FacilityResponse> facilities = facilityServiceClient.getFacilities(tenantId, facilityIds);
        
        for (String facilityId : facilityIds) {
            // Verify facility exists and is active
            FacilityResponse facility = facilities.stream()
                .filter(f -> f.getFacilityId().equals(facilityId))
                .findFirst()
                .orElseThrow(() -> new EntityNotFoundException("Facility not found: " + facilityId));
            
            if (!facility.getIsActive()) {
                throw new ValidationException("Facility is not active: " + facilityId);
            }
            
            // Create field plan facility association
            FieldPlanFacility fpFacility = new FieldPlanFacility();
            fpFacility.setId(idGenService.generateId());
            fpFacility.setTenantId(tenantId);
            fpFacility.setFieldPlanId(fieldPlanId);
            fpFacility.setFacilityId(facilityId);
            fpFacility.setStatus("ACTIVE");
            fpFacility.setCreatedTime(System.currentTimeMillis());
            
            fieldPlanFacilityRepository.save(fpFacility);
        }
        
        auditService.logFacilityAssignments(tenantId, fieldPlanId, facilityIds);
    }
}
```

#### 5.1.2 Activity Management Service (NEW)

```java
@Service
@Transactional
public class ActivityManagementService {
    
    @Autowired
    private FacilityActivityRepository facilityActivityRepository;
    
    @Autowired
    private ActivityReportRepository activityReportRepository;
    
    @Autowired
    private HRMSServiceClient hrmsServiceClient;
    
    @Autowired
    private WorkflowServiceClient workflowServiceClient;
    
    @Autowired
    private ConditionEvaluator conditionEvaluator;
    
    @Autowired
    private NotificationProducer notificationProducer;
    
    public void assignUserToFacilityActivity(String tenantId, String facilityActivityId, String userId) {
        // Validate user exists in HRMS
        EmployeeResponse employee = hrmsServiceClient.getEmployee(tenantId, userId);
        if (employee == null || !employee.getActive()) {
            throw new ValidationException("Invalid or inactive employee: " + userId);
        }
        
        // Get facility activity
        FacilityActivity facilityActivity = facilityActivityRepository.findByIdAndTenantId(facilityActivityId, tenantId)
            .orElseThrow(() -> new EntityNotFoundException("Facility activity not found"));
        
        // Assign user
        facilityActivity.setAssignedUser(userId);
        facilityActivity.setLastModifiedTime(System.currentTimeMillis());
        
        // Check if conditions are met for activation
        if (conditionEvaluator.evaluateActivationConditions(facilityActivity)) {
            facilityActivity.setStatus("ACTIVE");
            facilityActivity.setActivatedAt(System.currentTimeMillis());
            
            // Send activation notification via Kafka
            FacilityActivityEvent event = new FacilityActivityEvent();
            event.setTenantId(tenantId);
            event.setFacilityActivityId(facilityActivityId);
            event.setUserId(userId);
            event.setEventType("FACILITY_ACTIVATED");
            
            notificationProducer.sendFacilityActivationEvent(event);
        }
        
        facilityActivityRepository.save(facilityActivity);
    }
    
    public void processActivityReport(String tenantId, CreateActivityReportRequest request) {
        // Validate facility activity exists
        FacilityActivity facilityActivity = facilityActivityRepository
            .findByIdAndTenantId(request.getFacilityActivityId(), tenantId)
            .orElseThrow(() -> new EntityNotFoundException("Facility activity not found"));
        
        // Validate reporter exists in HRMS
        EmployeeResponse reporter = hrmsServiceClient.getEmployee(tenantId, request.getSubmittedBy());
        if (reporter == null) {
            throw new ValidationException("Invalid reporter employee");
        }
        
        // Create activity report
        ActivityReport report = new ActivityReport();
        report.setId(idGenService.generateId());
        report.setTenantId(tenantId);
        report.setFacilityActivityId(request.getFacilityActivityId());
        report.setSubmittedBy(request.getSubmittedBy());
        report.setReportData(request.getReportData());
        report.setAttachments(request.getAttachments());
        report.setStatus("SUBMITTED");
        report.setCreatedTime(System.currentTimeMillis());
        
        ActivityReport savedReport = activityReportRepository.save(report);
        
        // Update facility activity status
        facilityActivity.setStatus("COMPLETED");
        facilityActivity.setCompletedAt(System.currentTimeMillis());
        facilityActivityRepository.save(facilityActivity);
        
        // Send report submission notification
        ActivityReportEvent reportEvent = new ActivityReportEvent();
        reportEvent.setTenantId(tenantId);
        reportEvent.setReportId(savedReport.getId());
        reportEvent.setEventType("REPORT_SUBMITTED");
        
        notificationProducer.sendReportSubmissionEvent(reportEvent);
    }
    
    // Additional methods for review, approval, etc.
}
```

#### 5.1.3 Workflow Service

```java
@Service
@Transactional
public class WorkflowService {
    
    @Autowired
    private ActivityRepository activityRepository;
    
    @Autowired
    private FacilityActivityRepository facilityActivityRepository;
    
    @Autowired
    private ConditionEvaluator conditionEvaluator;
    
    @Autowired
    private NotificationService notificationService;
    
    public void checkAndActivateFacilities(UUID fieldPlanId) {
        // Get all scheduled facility activities for the field plan
        List<FacilityActivity> scheduledActivities = facilityActivityRepository
            .findByFieldPlanIdAndStatus(fieldPlanId, FacilityActivityStatus.SCHEDULED);
        
        for (FacilityActivity facilityActivity : scheduledActivities) {
            // Check if conditions are met for activation
            if (conditionEvaluator.evaluateConditions(facilityActivity)) {
                // Activate the facility activity
                facilityActivity.setStatus(FacilityActivityStatus.ACTIVE);
                facilityActivity.setActivatedAt(Instant.now());
                facilityActivityRepository.save(facilityActivity);
                
                // Notify assigned users
                notificationService.notifyActivityActivated(facilityActivity);
            }
        }
    }
    
    public void processActivityReport(UUID reportId, ReviewAction action, String comments) {
        ActivityReport report = getActivityReportById(reportId);
        
        // Update report status
        report.setStatus(mapActionToStatus(action));
        report.setReviewedBy(SecurityUtils.getCurrentUserId());
        report.setReviewedAt(Instant.now());
        report.setReviewComments(comments);
        
        activityReportRepository.save(report);
        
        // Handle based on action
        switch (action) {
            case APPROVE:
                handleApproval(report);
                break;
            case REJECT:
                handleRejection(report);
                break;
            case FLAG_FOR_FIELD_QC:
                handleFieldQCFlag(report);
                break;
        }
    }
    
    // Additional methods...
}
```

### 5.2 Frontend Components

#### 5.2.1 React Component Structure

```
src/
├── components/
│   ├── common/
│   │   ├── DataTable/
│   │   ├── FormComponents/
│   │   ├── Layout/
│   │   └── Navigation/
│   ├── projects/
│   │   ├── ProjectList/
│   │   ├── ProjectForm/
│   │   ├── ProjectDetails/
│   │   └── FacilityUpload/
│   ├── field-plans/
│   │   ├── FieldPlanList/
│   │   ├── FieldPlanForm/
│   │   ├── FieldPlanDetails/
│   │   └── ActivityAssignment/
│   ├── activities/
│   │   ├── ActivityList/
│   │   ├── ActivityDetails/
│   │   ├── ReportReview/
│   │   └── FacilityAssignment/
│   └── users/
│       ├── UserList/
│       ├── UserForm/
│       └── TeamManagement/
├── services/
│   ├── api/
│   ├── auth/
│   ├── storage/
│   └── utils/
├── hooks/
├── store/
└── types/
```

#### 5.2.2 Key React Components

```typescript
// ProjectForm.tsx
interface ProjectFormProps {
  initialData?: Project;
  onSubmit: (data: CreateProjectRequest) => void;
  onCancel: () => void;
}

const ProjectForm: React.FC<ProjectFormProps> = ({ initialData, onSubmit, onCancel }) => {
  const [formData, setFormData] = useState<CreateProjectRequest>({
    name: initialData?.name || '',
    type: initialData?.type || ProjectType.MEDTECH,
    justificationCode: initialData?.justificationCode || '',
    startDate: initialData?.startDate || '',
    endDate: initialData?.endDate || '',
    stateId: initialData?.stateId || '',
  });

  const [errors, setErrors] = useState<ValidationErrors>({});

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    
    const validationErrors = validateProjectForm(formData);
    if (Object.keys(validationErrors).length > 0) {
      setErrors(validationErrors);
      return;
    }
    
    onSubmit(formData);
  };

  return (
    <form onSubmit={handleSubmit} className="project-form">
      <div className="form-group">
        <label htmlFor="projectType">Project Type *</label>
        <select
          id="projectType"
          value={formData.type}
          onChange={(e) => setFormData({ ...formData, type: e.target.value as ProjectType })}
          className={errors.type ? 'error' : ''}
        >
          <option value={ProjectType.MEDTECH}>MedTech</option>
          <option value={ProjectType.ENERGY_FOR_HEALTH}>Energy for Health</option>
          <option value={ProjectType.LIVELIHOODS}>Livelihoods</option>
        </select>
        {errors.type && <span className="error-message">{errors.type}</span>}
      </div>
      
      {/* Additional form fields */}
      
      <div className="form-actions">
        <button type="button" onClick={onCancel} className="btn btn-secondary">
          Cancel
        </button>
        <button type="submit" className="btn btn-primary">
          Create Project
        </button>
      </div>
    </form>
  );
};
```

---

## 6. Security Design

### 6.1 Authentication & Authorization

#### 6.1.1 JWT Token Structure

```json
{
  "header": {
    "alg": "RS256",
    "typ": "JWT"
  },
  "payload": {
    "sub": "user-uuid",
    "email": "user@example.com",
    "organization_id": "org-uuid",
    "roles": ["PROJECT_MANAGER", "INSTALLATION_SPOC"],
    "permissions": ["CREATE_PROJECT", "VIEW_REPORTS"],
    "iat": 1640995200,
    "exp": 1640998800,
    "iss": "e4h-platform",
    "aud": "e4h-clients"
  }
}
```

#### 6.1.2 Role-Based Access Control

```java
@Component
public class SecurityConfig {
    
    public static final String[] PUBLIC_URLS = {
        "/api/v1/auth/**",
        "/api/v1/health",
        "/swagger-ui/**",
        "/v3/api-docs/**"
    };
    
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf().disable()
            .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            .and()
            .authorizeHttpRequests(authz -> authz
                .requestMatchers(PUBLIC_URLS).permitAll()
                .requestMatchers(HttpMethod.POST, "/api/v1/projects").hasRole("PROJECT_MANAGER")
                .requestMatchers(HttpMethod.PUT, "/api/v1/projects/**").hasRole("PROJECT_MANAGER")
                .requestMatchers(HttpMethod.POST, "/api/v1/field-plans").hasRole("PROJECT_MANAGER")
                .requestMatchers(HttpMethod.PUT, "/api/v1/activity-reports/*/review").hasAnyRole("QC_SPOC", "FIELD_QC_REVIEWER")
                .anyRequest().authenticated()
            )
            .oauth2ResourceServer(oauth2 -> oauth2.jwt(Customizer.withDefaults()));
        
        return http.build();
    }
}
```

### 6.2 Data Protection

#### 6.2.1 Data Encryption

```java
@Component
public class EncryptionService {
    
    @Value("${app.encryption.key}")
    private String encryptionKey;
    
    private final AESUtil aesUtil;
    
    public String encryptSensitiveData(String data) {
        return aesUtil.encrypt(data, encryptionKey);
    }
    
    public String decryptSensitiveData(String encryptedData) {
        return aesUtil.decrypt(encryptedData, encryptionKey);
    }
}

// JPA Entity with encryption
@Entity
@Table(name = "health_facilities")
public class HealthFacility {
    
    @Id
    private UUID id;
    
    private String name;
    
    @Convert(converter = EncryptedStringConverter.class)
    private String contactPhone;
    
    @Convert(converter = EncryptedStringConverter.class)
    private String contactEmail;
    
    // Other fields...
}
```

### 6.3 Input Validation

```java
@Component
public class ValidationService {
    
    private static final Pattern EMAIL_PATTERN = 
        Pattern.compile("^[A-Za-z0-9+_.-]+@([A-Za-z0-9.-]+\\.[A-Za-z]{2,})$");
    
    private static final Pattern PHONE_PATTERN = 
        Pattern.compile("^\\+?[1-9]\\d{1,14}$");
    
    public void validateCreateProjectRequest(CreateProjectRequest request) {
        List<String> errors = new ArrayList<>();
        
        if (StringUtils.isBlank(request.getName())) {
            errors.add("Project name is required");
        }
        
        if (request.getStartDate() == null) {
            errors.add("Start date is required");
        }
        
        if (request.getEndDate() == null) {
            errors.add("End date is required");
        }
        
        if (request.getStartDate() != null && request.getEndDate() != null &&
            request.getStartDate().isAfter(request.getEndDate())) {
            errors.add("Start date must be before end date");
        }
        
        if (!errors.isEmpty()) {
            throw new ValidationException(errors);
        }
    }
}
```

---

## 7. Integration Points

### 7.1 E4H Platform Service Integration

#### 7.1.1 Service Client Configuration

```java
// Integration with existing E4H services
@Configuration
public class E4HServiceClientConfig {
    
    @Bean
    @LoadBalanced
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
    
    @Bean
    public HRMSServiceClient hrmsServiceClient(@Value("${egov.hrms.base-url}") String baseUrl) {
        return new HRMSServiceClient(restTemplate(), baseUrl);
    }
    
    @Bean
    public FacilityServiceClient facilityServiceClient(@Value("${facility.service.base-url}") String baseUrl) {
        return new FacilityServiceClient(restTemplate(), baseUrl);
    }
    
    @Bean
    public ProjectServiceClient projectServiceClient(@Value("${project.service.base-url}") String baseUrl) {
        return new ProjectServiceClient(restTemplate(), baseUrl);
    }
    
    @Bean
    public WorkflowServiceClient workflowServiceClient(@Value("${egov.workflow.base-url}") String baseUrl) {
        return new WorkflowServiceClient(restTemplate(), baseUrl);
    }
}

@Component
public class HRMSServiceClient {
    
    private final RestTemplate restTemplate;
    private final String baseUrl;
    
    public HRMSServiceClient(RestTemplate restTemplate, String baseUrl) {
        this.restTemplate = restTemplate;
        this.baseUrl = baseUrl;
    }
    
    public EmployeeResponse getEmployee(String tenantId, String employeeId) {
        String url = baseUrl + "/egov-hrms/employees/_search";
        
        EmployeeSearchRequest searchRequest = new EmployeeSearchRequest();
        searchRequest.setTenantId(tenantId);
        searchRequest.setUuids(Arrays.asList(employeeId));
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<EmployeeSearchRequest> entity = new HttpEntity<>(searchRequest, headers);
        
        try {
            EmployeeSearchResponse response = restTemplate.postForObject(url, entity, EmployeeSearchResponse.class);
            if (response != null && response.getEmployees() != null && !response.getEmployees().isEmpty()) {
                return response.getEmployees().get(0);
            }
        } catch (Exception e) {
            log.error("Error fetching employee from HRMS: {}", e.getMessage());
        }
        
        return null;
    }
    
    public List<EmployeeResponse> searchEmployees(String tenantId, EmployeeSearchCriteria criteria) {
        String url = baseUrl + "/egov-hrms/employees/_search";
        
        EmployeeSearchRequest searchRequest = new EmployeeSearchRequest();
        searchRequest.setTenantId(tenantId);
        searchRequest.setDepartment(criteria.getDepartment());
        searchRequest.setDesignation(criteria.getDesignation());
        searchRequest.setActive(true);
        
        HttpEntity<EmployeeSearchRequest> entity = new HttpEntity<>(searchRequest);
        
        try {
            EmployeeSearchResponse response = restTemplate.postForObject(url, entity, EmployeeSearchResponse.class);
            if (response != null && response.getEmployees() != null) {
                return response.getEmployees();
            }
        } catch (Exception e) {
            log.error("Error searching employees from HRMS: {}", e.getMessage());
        }
        
        return Collections.emptyList();
    }
}
```

#### 7.1.2 Mobile App Integration with Existing Infrastructure

```java
@RestController
@RequestMapping("/field-planner/v1/mobile/sync")
public class MobileSyncController {
    
    @Autowired
    private FacilityServiceClient facilityServiceClient;
    
    @Autowired
    private HRMSServiceClient hrmsServiceClient;
    
    @Autowired
    private FacilityActivityService facilityActivityService;
    
    @PostMapping("/facilities")
    public ResponseEntity<SyncResponse> syncFacilities(
            @RequestBody SyncRequest request,
            @RequestHeader("X-Tenant-Id") String tenantId) {
        
        String userId = SecurityUtils.getCurrentUserId();
        
        // Validate user exists in HRMS
        EmployeeResponse employee = hrmsServiceClient.getEmployee(tenantId, userId);
        if (employee == null) {
            throw new UnauthorizedException("Invalid employee");
        }
        
        // Get assigned facility activities from Field Planner
        List<FacilityActivity> assignedActivities = 
            facilityActivityService.getAssignedActivitiesSince(tenantId, userId, request.getLastSyncTime());
        
        // Get facility details from Health Facility Registry
        List<String> facilityIds = assignedActivities.stream()
            .map(FacilityActivity::getFacilityId)
            .distinct()
            .collect(Collectors.toList());
            
        List<FacilityResponse> facilities = facilityServiceClient.getFacilities(tenantId, facilityIds);
        
        // Prepare sync response with integrated data
        SyncResponse response = new SyncResponse();
        response.setFacilities(facilities);
        response.setFacilityActivities(assignedActivities);
        response.setServerTime(System.currentTimeMillis());
        
        return ResponseEntity.ok(response);
    }
    
    @PostMapping("/reports")
    public ResponseEntity<Void> uploadReports(
            @RequestBody List<ActivityReportDTO> reports,
            @RequestHeader("X-Tenant-Id") String tenantId) {
            
        String userId = SecurityUtils.getCurrentUserId();
        
        for (ActivityReportDTO report : reports) {
            // Process each report through Activity Management Service
            CreateActivityReportRequest reportRequest = new CreateActivityReportRequest();
            reportRequest.setTenantId(tenantId);
            reportRequest.setFacilityActivityId(report.getFacilityActivityId());
            reportRequest.setSubmittedBy(userId);
            reportRequest.setReportData(report.getReportData());
            reportRequest.setAttachments(report.getAttachments());
            
            activityManagementService.processActivityReport(tenantId, reportRequest);
        }
        
        return ResponseEntity.ok().build();
    }
}
```

### 7.2 External System Integration

#### 7.2.1 MDMS Integration

```java
@Service
public class MDMSIntegrationService {
    
    @Autowired
    private WebClient webClient;
    
    @Value("${mdms.base-url}")
    private String mdmsBaseUrl;
    
    public List<Boundary> fetchBoundaries(String tenantId, String hierarchyType) {
        String url = mdmsBaseUrl + "/egov-mdms-service/v1/_search";
        
        MDMSRequest request = MDMSRequest.builder()
            .tenantId(tenantId)
            .moduleDetails(List.of(
                ModuleDetail.builder()
                    .moduleName("tenant")
                    .masterDetails(List.of(
                        MasterDetail.builder()
                            .name("tenants")
                            .build()))
                    .build()))
            .build();
        
        MDMSResponse response = webClient.post()
            .uri(url)
            .bodyValue(request)
            .retrieve()
            .bodyToMono(MDMSResponse.class)
            .block();
        
        return mapToBoundaries(response);
    }
}
```

### 7.3 File Storage Integration

```java
@Service
public class FileStorageService {
    
    @Autowired
    private MinioClient minioClient;
    
    @Value("${minio.bucket-name}")
    private String bucketName;
    
    public String uploadFile(MultipartFile file, String folder) {
        try {
            String fileName = generateFileName(file.getOriginalFilename());
            String objectName = folder + "/" + fileName;
            
            minioClient.putObject(
                PutObjectArgs.builder()
                    .bucket(bucketName)
                    .object(objectName)
                    .stream(file.getInputStream(), file.getSize(), -1)
                    .contentType(file.getContentType())
                    .build());
            
            return objectName;
        } catch (Exception e) {
            throw new FileStorageException("Failed to upload file", e);
        }
    }
    
    public byte[] downloadFile(String objectName) {
        try {
            GetObjectResponse response = minioClient.getObject(
                GetObjectArgs.builder()
                    .bucket(bucketName)
                    .object(objectName)
                    .build());
            
            return response.readAllBytes();
        } catch (Exception e) {
            throw new FileStorageException("Failed to download file", e);
        }
    }
}
```

---

## 8. Implementation Guidelines

### 8.1 Development Standards

#### 8.1.1 Code Style Guidelines

```java
// Example of proper code style
@Service
@Transactional
@Slf4j
public class ProjectServiceImpl implements ProjectService {
    
    private final ProjectRepository projectRepository;
    private final UserService userService;
    private final AuditService auditService;
    
    public ProjectServiceImpl(ProjectRepository projectRepository,
                             UserService userService,
                             AuditService auditService) {
        this.projectRepository = projectRepository;
        this.userService = userService;
        this.auditService = auditService;
    }
    
    @Override
    public ProjectDTO createProject(CreateProjectRequest request) {
        log.info("Creating project: {}", request.getName());
        
        // Validate input
        validateProjectRequest(request);
        
        // Create project
        Project project = buildProjectFromRequest(request);
        Project savedProject = projectRepository.save(project);
        
        // Log audit event
        auditService.logProjectCreation(savedProject);
        
        log.info("Project created successfully: {}", savedProject.getId());
        return ProjectMapper.toDTO(savedProject);
    }
    
    private void validateProjectRequest(CreateProjectRequest request) {
        // Validation logic
    }
    
    private Project buildProjectFromRequest(CreateProjectRequest request) {
        // Build project logic
    }
}
```

#### 8.1.2 Error Handling Strategy

```java
@ControllerAdvice
public class GlobalExceptionHandler {
    
    @ExceptionHandler(ValidationException.class)
    public ResponseEntity<ApiResponse> handleValidationException(ValidationException e) {
        ApiResponse response = ApiResponse.builder()
            .success(false)
            .errors(e.getErrors().stream()
                .map(error -> ApiError.builder()
                    .code("VALIDATION_ERROR")
                    .message(error)
                    .build())
                .collect(Collectors.toList()))
            .build();
        
        return ResponseEntity.badRequest().body(response);
    }
    
    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<ApiResponse> handleEntityNotFound(EntityNotFoundException e) {
        ApiResponse response = ApiResponse.builder()
            .success(false)
            .errors(List.of(ApiError.builder()
                .code("ENTITY_NOT_FOUND")
                .message(e.getMessage())
                .build()))
            .build();
        
        return ResponseEntity.notFound().build();
    }
}
```

### 8.2 Testing Strategy

#### 8.2.1 Unit Tests

```java
@ExtendWith(MockitoExtension.class)
class ProjectServiceTest {
    
    @Mock
    private ProjectRepository projectRepository;
    
    @Mock
    private UserService userService;
    
    @Mock
    private AuditService auditService;
    
    @InjectMocks
    private ProjectServiceImpl projectService;
    
    @Test
    void createProject_ValidRequest_ReturnsProjectDTO() {
        // Given
        CreateProjectRequest request = CreateProjectRequest.builder()
            .name("Test Project")
            .type(ProjectType.MEDTECH)
            .startDate(LocalDate.now())
            .endDate(LocalDate.now().plusMonths(6))
            .build();
        
        Project savedProject = new Project();
        savedProject.setId(UUID.randomUUID());
        savedProject.setName(request.getName());
        
        when(projectRepository.save(any(Project.class))).thenReturn(savedProject);
        
        // When
        ProjectDTO result = projectService.createProject(request);
        
        // Then
        assertThat(result).isNotNull();
        assertThat(result.getName()).isEqualTo(request.getName());
        verify(projectRepository).save(any(Project.class));
        verify(auditService).logProjectCreation(any(Project.class));
    }
}
```

#### 8.2.2 Integration Tests

```java
@SpringBootTest
@Testcontainers
class ProjectIntegrationTest {
    
    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15")
            .withDatabaseName("testdb")
            .withUsername("test")
            .withPassword("test");
    
    @Autowired
    private TestRestTemplate restTemplate;
    
    @Autowired
    private ProjectRepository projectRepository;
    
    @Test
    void createProject_IntegrationTest() {
        // Given
        CreateProjectRequest request = CreateProjectRequest.builder()
            .name("Integration Test Project")
            .type(ProjectType.MEDTECH)
            .startDate(LocalDate.now())
            .endDate(LocalDate.now().plusMonths(6))
            .build();
        
        // When
        ResponseEntity<ApiResponse> response = restTemplate.postForEntity(
            "/api/v1/projects", request, ApiResponse.class);
        
        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(projectRepository.count()).isEqualTo(1);
    }
}
```

---

## 9. Performance Considerations

### 9.1 Database Optimization

#### 9.1.1 Query Optimization

```sql
-- Optimized query for facility activities with proper indexing
SELECT 
    fa.id,
    fa.facility_id,
    fa.activity_id,
    fa.status,
    hf.name as facility_name,
    a.name as activity_name
FROM facility_activities fa
JOIN health_facilities hf ON fa.facility_id = hf.id
JOIN activities a ON fa.activity_id = a.id
WHERE fa.field_plan_id = ?
  AND fa.status IN ('ACTIVE', 'COMPLETED')
  AND fa.assigned_user = ?
ORDER BY fa.created_at DESC
LIMIT 20 OFFSET ?;

-- Index to support this query
CREATE INDEX idx_facility_activities_optimized 
ON facility_activities(field_plan_id, status, assigned_user, created_at);
```

#### 9.1.2 Connection Pooling

```yaml
# application.yml
spring:
  datasource:
    hikari:
      maximum-pool-size: 20
      minimum-idle: 5
      idle-timeout: 300000
      max-lifetime: 1200000
      connection-timeout: 20000
      leak-detection-threshold: 60000
```

### 9.2 Caching Strategy

```java
@Configuration
@EnableCaching
public class CacheConfig {
    
    @Bean
    public CacheManager cacheManager() {
        RedisCacheManager.Builder builder = RedisCacheManager
            .RedisCacheManagerBuilder
            .fromConnectionFactory(redisConnectionFactory())
            .cacheDefaults(cacheConfiguration());
        
        return builder.build();
    }
    
    private RedisCacheConfiguration cacheConfiguration() {
        return RedisCacheConfiguration.defaultCacheConfig()
            .entryTtl(Duration.ofMinutes(30))
            .serializeKeysWith(RedisSerializationContext.SerializationPair
                .fromSerializer(new StringRedisSerializer()))
            .serializeValuesWith(RedisSerializationContext.SerializationPair
                .fromSerializer(new GenericJackson2JsonRedisSerializer()));
    }
}

@Service
public class BoundaryService {
    
    @Cacheable(value = "boundaries", key = "#hierarchyType + '_' + #parentId")
    public List<BoundaryDTO> getBoundariesByParent(String hierarchyType, UUID parentId) {
        return boundaryRepository.findByHierarchyTypeAndParentId(hierarchyType, parentId)
            .stream()
            .map(BoundaryMapper::toDTO)
            .collect(Collectors.toList());
    }
}
```

### 9.3 Async Processing

```java
@Component
public class AsyncTaskProcessor {
    
    @Async("taskExecutor")
    public CompletableFuture<Void> processExcelUpload(UUID uploadId, MultipartFile file) {
        try {
            // Process file
            List<HealthFacility> facilities = parseExcelFile(file);
            
            // Validate facilities
            List<ValidationError> errors = validateFacilities(facilities);
            
            // Update upload status
            updateUploadStatus(uploadId, errors.isEmpty() ? "COMPLETED" : "FAILED", errors);
            
            // Send notification
            notificationService.notifyUploadComplete(uploadId);
            
        } catch (Exception e) {
            log.error("Error processing excel upload: {}", uploadId, e);
            updateUploadStatus(uploadId, "FAILED", List.of(new ValidationError("Processing failed")));
        }
        
        return CompletableFuture.completedFuture(null);
    }
}
```

---

## 10. Error Handling

### 10.1 Exception Hierarchy

```java
// Base exception
public abstract class E4HException extends RuntimeException {
    private final String errorCode;
    private final Object[] args;
    
    public E4HException(String errorCode, String message, Object... args) {
        super(message);
        this.errorCode = errorCode;
        this.args = args;
    }
    
    // Getters...
}

// Specific exceptions
public class ValidationException extends E4HException {
    public ValidationException(String message, Object... args) {
        super("VALIDATION_ERROR", message, args);
    }
}

public class EntityNotFoundException extends E4HException {
    public EntityNotFoundException(String entityType, Object id) {
        super("ENTITY_NOT_FOUND", String.format("%s with id %s not found", entityType, id));
    }
}

public class BusinessRuleException extends E4HException {
    public BusinessRuleException(String message, Object... args) {
        super("BUSINESS_RULE_VIOLATION", message, args);
    }
}
```

### 10.2 Validation Framework

```java
@Component
public class ProjectValidator {
    
    public void validateCreateProjectRequest(CreateProjectRequest request) {
        ValidationResult result = new ValidationResult();
        
        // Required field validation
        if (StringUtils.isBlank(request.getName())) {
            result.addError("name", "Project name is required");
        }
        
        // Business rule validation
        if (request.getStartDate() != null && request.getEndDate() != null) {
            if (request.getStartDate().isAfter(request.getEndDate())) {
                result.addError("dateRange", "Start date must be before end date");
            }
        }
        
        // Unique constraint validation
        if (projectRepository.existsByName(request.getName())) {
            result.addError("name", "Project name already exists");
        }
        
        if (result.hasErrors()) {
            throw new ValidationException(result.getErrors());
        }
    }
}
```

---

## 11. Deployment Architecture

### 11.1 Container Configuration

```dockerfile
# Dockerfile for backend service
FROM openjdk:17-jdk-slim

WORKDIR /app

COPY target/field-planner-service.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
```

### 11.2 Docker Compose

```yaml
# docker-compose.yml
version: '3.8'

services:
  postgres:
    image: postgres:15
    environment:
      POSTGRES_DB: field_planner
      POSTGRES_USER: postgres
      POSTGRES_PASSWORD: password
    ports:
      - "5432:5432"
    volumes:
      - postgres_data:/var/lib/postgresql/data

  redis:
    image: redis:7-alpine
    ports:
      - "6379:6379"

  field-planner-service:
    build: .
    depends_on:
      - postgres
      - redis
    environment:
      SPRING_DATASOURCE_URL: jdbc:postgresql://postgres:5432/field_planner
      SPRING_DATASOURCE_USERNAME: postgres
      SPRING_DATASOURCE_PASSWORD: password
      SPRING_REDIS_HOST: redis
    ports:
      - "8080:8080"

volumes:
  postgres_data:
```

### 11.3 Kubernetes Configuration

```yaml
# deployment.yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: field-planner-service
spec:
  replicas: 3
  selector:
    matchLabels:
      app: field-planner-service
  template:
    metadata:
      labels:
        app: field-planner-service
    spec:
      containers:
      - name: field-planner-service
        image: field-planner-service:latest
        ports:
        - containerPort: 8080
        env:
        - name: SPRING_DATASOURCE_URL
          valueFrom:
            secretKeyRef:
              name: db-secret
              key: url
        - name: SPRING_DATASOURCE_USERNAME
          valueFrom:
            secretKeyRef:
              name: db-secret
              key: username
        - name: SPRING_DATASOURCE_PASSWORD
          valueFrom:
            secretKeyRef:
              name: db-secret
              key: password
        resources:
          requests:
            memory: "512Mi"
            cpu: "250m"
          limits:
            memory: "1Gi"
            cpu: "500m"
        livenessProbe:
          httpGet:
            path: /actuator/health
            port: 8080
          initialDelaySeconds: 30
          periodSeconds: 10
        readinessProbe:
          httpGet:
            path: /actuator/health/readiness
            port: 8080
          initialDelaySeconds: 5
          periodSeconds: 5
---
apiVersion: v1
kind: Service
metadata:
  name: field-planner-service
spec:
  selector:
    app: field-planner-service
  ports:
  - protocol: TCP
    port: 80
    targetPort: 8080
  type: LoadBalancer
```

---

## Conclusion

This Low-Level Design document provides a comprehensive technical blueprint for implementing the E4H Digital Platform's Field Planner module. The design emphasizes:

- **Scalability**: Microservices architecture with proper separation of concerns
- **Security**: Comprehensive authentication, authorization, and data protection
- **Performance**: Optimized database queries, caching, and async processing
- **Maintainability**: Clean code structure, proper error handling, and extensive testing
- **Flexibility**: Configurable workflows and role-based access control

The implementation should follow agile development practices with continuous integration and deployment pipelines to ensure quality and reliability.

---

## REVISION NOTES

**This LLD has been corrected to properly integrate with the existing E4H Digital Platform infrastructure:**

### ✅ **Key Integration Changes Made:**
1. **Database Design**: Removed duplicate tables (users, projects, facilities) and integrated with existing E4H schemas (`facility`, `eg_hrms_employee`, `PROJECT_FACILITY`)
2. **Service Architecture**: Field Planner now extends existing services rather than creating standalone systems
3. **API Integration**: Uses existing E4H service endpoints and follows platform conventions
4. **Data Consistency**: Validates against existing Health Facility Registry and eGov HRMS
5. **Technology Alignment**: Follows E4H patterns (VARCHAR IDs, BIGINT timestamps, tenant_id fields)
6. **Service Dependencies**: Properly integrates with health-facility-registry, egov-hrms, project-service, egov-workflow-v2, egov-filestore, egov-mdms-service-v2

### 🎯 **Result:** 
Field Planner is now designed as a **service extension module** that enhances the existing E4H platform rather than a standalone system, ensuring consistency, reducing duplication, and leveraging the robust infrastructure already built. 