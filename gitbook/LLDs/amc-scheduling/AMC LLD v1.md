# AMC Scheduling System Implementation Plan

## Executive Summary

This plan outlines the design and implementation of an AMC (Annual Maintenance Contract) scheduling system for managing maintenance of solar panel installations at healthcare facilities. The system will enable configuration of AMC parameters per vendor-facility combination and automate visit scheduling while handling legacy asset integration.

## Background & Context

### Current System Architecture

The platform already implements:

- **Project Service** (`backend/e4h-services/project`): Manages projects with funding codes, start/end dates, and facility assignments
- **Field Planner Service** (`backend/e4h-services/field-planner`): Creates field plans, assigns activities to vendors, tracks facility-level activities
- **Asset Registry** (`backend/e4h-services/asset-registry`): Manages asset installation at facilities with workflow support

### Problem Statement

As outlined in the PRD (`AMC_PRD.md`), the current AMC process is:

- Manual and fragmented (physical paperwork, emails, spreadsheets)
- Lacks centralized records for active/expired contracts
- No systematic renewal alerts or SLA tracking
- Vendor report compliance issues
- Person-dependent with limited transparency

## Solution Architecture

### Core Data Model

#### 1. AMC Configuration Entity

Central entity linking vendor to facility with contract terms:

```
amc_configuration:
  - id (PK)
  - tenant_id
  - vendor_id (FK to vendor_registry)
  - facility_id (FK to facility)
  - field_plan_id (FK to field_plans)
  - duration_months (e.g., 36 for 3 years)
  - visit_frequency_months (numeric: 1, 3, 6, 12)
  - configuration_start_date
  - configuration_end_date
  - status (ACTIVE, EXPIRED, CANCELLED)
  - sla_days (for visit completion)
  - grace_period_days (scheduling flexibility)
  - additional_details (JSONB for vendor contract specifics)
  - created_by, created_time
  - last_modified_by, last_modified_time
```

**Key Characteristics:**

- Unique per vendor-facility combination within a field plan
- Acts as master template for all assets under this vendor-facility
- Supports vendor switching by creating new configuration with different dates

#### 2. Asset AMC Record Entity

Links individual assets to their AMC configuration:

```
asset_amc:
  - id (PK)
  - tenant_id
  - asset_id (FK to asset)
  - amc_configuration_id (FK to amc_configuration)
  - amc_start_date (installation/commissioning date or manual for legacy)
  - amc_end_date (calculated: start_date + duration_months)
  - status (ACTIVE, EXPIRED, UNDER_MAINTENANCE, INACTIVE)
  - is_legacy_asset (boolean flag)
  - additional_details (JSONB)
  - created_by, created_time
  - last_modified_by, last_modified_time
```

**Key Characteristics:**

- Inherits duration and frequency from amc_configuration
- `amc_start_date` is the critical field that can be set manually for legacy assets
- Automatically expires when current date > amc_end_date

#### 3. Scheduled Visit Entity

Generated maintenance visits based on AMC configuration:

```
scheduled_visits:
  - id (PK)
  - tenant_id
  - amc_configuration_id (FK)
  - facility_id (FK to facility)
  - visit_number (sequence within AMC period)
  - scheduled_date (calculated from earliest asset start + frequency)
  - actual_visit_date
  - visit_report (JSONB) (json schema validation according to solution design of the facility)
  - created_by, created_time
  - last_modified_by, last_modified_time

scheduled_visit_assignments:
  - id (PK)
  - scheduled_visit_id (FK to scheduled_visits.id)
  - assigned_user (FK to hrms employee)
```

**Key Characteristics:**

- One visit covers all assets at facility under same vendor
- Visit status ( ToBeScheduled, Scheduled, PendingOTPApproval, PendingApproval ) will be tracked via amc_visit business service workflow.
- each visit can have multiple users assigned: amc_field_supervisor, amc_field_staff. amc_spoc and amc_reviewer will be managed as activity_spoc

#### 4. Visit Report

Captures AMC report submission and approval:

A visit_report will be defined as json schema driven by form engine. These will be input by a support engineer, same as for asset installation report

### Database Schema Extensions

Building on existing tables:

- `field_plans` (existing) - parent entity for AMC planning
- `facility_activities` (existing) - tracks AMC activity status
- `asset` (existing) - links to asset_amc

## Solution Workflow

### Phase 1: Project & Field Plan Setup (Existing)

**Already Implemented in Codebase**

1. Project Manager creates project via Project Service

 - Reference: `ProjectService.createProject()` in `backend/e4h-services/project`
 - Captures funding code, start/end dates, target facilities

2. Project Manager creates field plans via Field Planner Service

 - Reference: `FieldPlannerService.createFieldPlan()` in `backend/e4h-services/field-planner`
 - Defines geography scope, selects activities including AMC
 - Assigns vendor pool (1-2 vendors per field plan)

### Phase 2: AMC Configuration (New Implementation)

**When:** After field plan creation, before asset installation

**Who:** Central AMC Coordinator (Project Manager role)

**Process Flow:**

```
1. User selects field plan with AMC activity
2. For each facility in field plan:
   a. User selects assigned vendor (from field plan vendor pool)
   b. User configures:
      - Duration (e.g., 36 months, 60 months)
      - Visit frequency (Monthly, Quarterly, Bi-Annual, Annual)
   c. System creates/updates amc_configuration record
   d. System updates facility_activities status to "AMC_CONFIGURED"
```

**API Endpoints:**

- `POST /amc-configuration/v1/_create` - Create AMC configuration
- `POST /amc-configuration/v1/_bulk-create` - Create in bulk
- `POST /amc-configuration/v1/_update` - Update existing configuration
- `POST /amc-configuration/v1/_search` - Search configurations

**Validation Rules:**

- Only one active AMC configuration per vendor-facility-field_plan combination
- Vendor must be assigned to facility in field plan
- Field plan must have AMC as selected activity

### Phase 3: Asset Installation & AMC Activation (Enhanced Existing)

**When:** During asset installation process

**Who:** Vendor Field Staff, AMC Coordinator

**Process Flow:**

```
1. Asset installation happens (existing workflow in asset-registry)
   - Reference: AssetService.createAsset() in backend/e4h-services/asset-registry
   
2. After QC approval and asset becomes "OPERATIONAL":
   a. System retrieves AMC configuration for facility-vendor
   b. System creates asset_amc record:
      - Links asset to amc_configuration
      - Sets amc_start_date = asset commissioning date (or installation date)
      - Calculates amc_end_date = start_date + duration_months
      - Sets status = ACTIVE
      - Sets is_legacy_asset = false
   c. System triggers visit schedule generation (if first asset for this config)
   
3. For Legacy Assets (no installation records):
   a. AMC Coordinator manually creates asset_amc record:
      - Links asset to amc_configuration
      - Manually sets amc_start_date (user input)
      - System calculates amc_end_date
      - Sets is_legacy_asset = true
   b. System regenerates/adjusts visit schedule if needed
```

**API Endpoints:**

- `POST /asset-amc/v1/_create` - Create asset AMC record
- `POST /asset-amc/v1/_bulk-create` - Bulk creation for legacy assets
- `POST /asset-amc/v1/_update` - Update AMC dates
- `POST /asset-amc/v1/_search` - Search asset AMC records

**Validation Rules:**

- Asset must be in "OPERATIONAL" status
- Asset facility must match AMC configuration facility
- AMC start date cannot be in future beyond current date + grace period
- Cannot create duplicate asset_amc for same asset

### Phase 4: Scheduled Visits Generation (Automated)

**Triggered:** Daily CRON job

**Who:** System (background job/service)

**Scheduling Algorithm:**
TODO: revisit the scheduling algorithm -> for each amc_configuration, create a visit if next_visit_date <= configuration_end_date _and_ next_visit_date - today <= VISIT_NOTICE_IN_DAYS (master data) _and_ no active scheduled visit for current visit_number. Each visit will track amc_configuration and each amc_configuration will track list of asset_amc which gives a list of assetIds to report on. Query for amc_configuration that have these conditions met and TODO: either call /amc-scheduled-visits/v1/_bulk-generate and log failures, ~or have a bulk generate endpoint or push to a queue to process one by one~
next_visit_date is a stored value in amc_configuration, that is updated after creation of each visit

```
For each amc_configuration:
  1. Find earliest amc_start_date among all linked asset_amc records
  2. Calculate number_of_visits = duration_months / visit_frequency_months
  3. For i = 1 to number_of_visits:
     a. scheduled_date = earliest_start_date + (i × visit_frequency_months)
     b. If scheduled_date <= current_date + configuration_end_date:
        - Create scheduled_visit record
        - Set visit_number = i
        - Set status = UPCOMING (if future) or OVERDUE (if past)
  4. Bundle all assets under this amc_configuration in visit.additional_details
```

**API Endpoints:**

- `POST /amc-scheduled-visits/v1/_generate` - Generate visit for AMC config
- `POST /amc-scheduled-visits/v1/_bulk-generate` - generate visit in bulk
- `POST /amc-scheduled-visits/v1/_search` - Search scheduled visits by field_plan_id, assigned_user, and workflow_status


### Phase 5: Vendor Assignment & Visit Execution

**When:** Based on scheduled visit dates

**Who:** Vendor POC, AMC Field Staff, Central AMC Coordinator

**Process Flow:**

**5.1 Assignment (T-30 days before visit):**

```
1. System generates alert for Vendor POC:
   - Lists upcoming visits for next 30 days
   - Shows facility details, asset count
   
2. Vendor POC assigns field staff:
   - Selects employee from vendor org (via HRMS)
   - Updates scheduled_visit.assigned_field_staff_id
   - Optional: Can request reschedule with reason
   
3. If rescheduling requested:
   - System validates reason (from configured list)
   - Vendor POC proposes new date within window
   - AMC Coordinator approves/rejects
   - If approved: Updates scheduled_date, logs in additional_details
```

**5.2 Visit Execution (On scheduled date):**

```
1. Field staff arrives at facility:
   - Mobile app shows visit details, asset list, checklist
   
2. Performs maintenance:
   - Completes checklist for each asset type
   - Captures GPS-tagged photos/videos
   - Records findings and actions taken
   
3. Obtains facility verification:
   - Sends OTP to Health Facility POC (from PRD requirement)
   - POC verifies work completion
   - POC provides signature/seal (digital or scanned)
   
4. Submits AMC report:
   - Creates visit_report record (status = SUBMITTED)
   - Uploads signed document to filestore
   - Attaches images/videos with GPS metadata
   - System updates scheduled_visit.status = IN_PROGRESS → COMPLETED
   - Sets actual_visit_date = current_date
   - Checks SLA: If actual_visit_date > visit_window_end, set sla_breach_flag = true
```

**API Endpoints:**

- `POST /amc-scheduled-visits/v1/_assign` - Assign field staff
- `POST /amc-scheduled-visits/v1/workflow/update` - Request rescheduling

### Phase 6: Report Review & Approval

**When:** After visit report submission

**Who:** Central AMC Manager (AMC Reviewer role)

**Process Flow:**

```
1. System routes report to AMC Reviewer queue
2. Reviewer accesses submitted report:
   - Views checklist responses
   - Reviews photos/videos
   - Checks OTP verification status
   - Reviews signed document
   
3. Reviewer decision:
   a. APPROVE:
      - Sets visit_report.report_status = APPROVED
      - Sets scheduled_visit.status = COMPLETED
      - System updates asset_amc records in additional_details (last_serviced_date)
      
   b. REJECT:
      - Sets visit_report.report_status = REJECTED
      - Adds review_comments (required)
      - Scheduled_visit.status = IN_PROGRESS (reopened)
      - System notifies Vendor POC and Field Staff
      - Field staff can resubmit corrections
      
4. Auto-approval (from PRD):
   - If no action taken within X days (configurable, e.g., 7 days)
   - System automatically sets status = APPROVED
   - Logs auto-approval in additional_details
```

**API Endpoints:**

- `POST /amc-scheduled-visits/v1/workflow/update` - Approve/Reject

## Technical Implementation Details

### Integration Points

**1. With Existing Services:**

- **Field Planner Service:** AMC activity type, vendor assignments
- **Asset Registry:** Asset commissioning dates, operational status
- **Vendor Registry:** Vendor details, field staff employees
- **HRMS Service:** Employee assignments, roles
- **Filestore Service:** Document uploads (signed reports, photos, videos)
- **SMS Notification:** Alerts to vendors and facility POCs
- **Workflow Service:** Optional for report approval workflow

**2. New Microservice:**

Create `amc-scheduler-service` in `backend/e4h-services/amc-scheduler-service/`

**Structure (following existing patterns):**

```
amc-scheduler-service/
├── src/main/java/org/egov/amc/
│   ├── config/
│   │   └── AmcSchedulerConfiguration.java
│   ├── repository/
│   │   ├── AmcConfigurationRepository.java
│   │   ├── AssetAmcRepository.java
│   │   └── ScheduledVisitRepository.java
│   ├── service/
│   │   ├── AmcConfigurationService.java
│   │   ├── AssetAmcService.java
│   │   ├── VisitScheduleGenerationService.java
│   │   └── ScheduledVisitService.java
│   ├── validator/
│   │   ├── AmcConfigurationValidator.java
│   │   ├── AssetAmcValidator.java
│   │   └── VisitReportValidator.java
│   ├── web/
│   │   ├── controllers/
│   │   └── models/
├── src/main/resources/
│   ├── db/migration/main/
│   │   └── V1__amc_scheduler_ddl.sql
│   ├── amc-configuration-persister.yml
│   ├── asset-amc-persister.yml
│   ├── scheduled-visit-persister.yml
│   └── application.properties
└── pom.xml (following existing service patterns)
```

### Configuration Parameters

**MDMS Master Data:**

```
AMCConfiguration:
  - VisitFrequency: [MONTHLY, QUARTERLY, BI_ANNUALLY, ANNUALLY]
  - DurationOptions: [12, 24, 36, 48, 60] (months)
  - RescheduleReasons: [WEATHER, FACILITY_CLOSED, STAFF_UNAVAILABLE, etc.]
  - ChecklistTemplates: (by asset type)
  - VISIT_NOTICE_IN_DAYS
Asset Master
  - Asset
  - Asset Category
  - Solar Solution Design Type
```

### Data Migration Strategy

**For Legacy Assets:**

1. Provide bulk upload template (Excel/CSV):
 - Columns: Facility ID, Asset ID, Vendor ID, AMC Start Date, Duration, Frequency
2. Validation during upload:
 - Cross-check against existing assets in registry
 - Verify vendor assignments
 - Date format validation
3. Create asset_amc records with is_legacy_asset = true
4. Trigger schedule generation for each AMC configuration

### Performance Considerations

**1. Visit Generation:**

- Run as async job (Kafka consumer pattern)
- Process in batches of 100 configurations
- Cache frequently accessed configurations

**2. Dashboard Queries:**

- Create materialized views for summary data
- Refresh daily or on-demand
- Index on: tenant_id, status, scheduled_date, vendor_id, facility_id

**3. Search Optimization:**

- Composite indexes on frequently filtered columns
- Consider ElasticSearch integration for complex searches (following existing pattern)

## Appendix: Process Flow Diagrams

### AMC Configuration Flow

```
[Field Plan Created] 
  → [AMC Coordinator selects facility]
  → [Assigns vendor from pool]
  → [Configures duration/frequency]
  → [System creates amc_configuration]
  → [Status: AMC_CONFIGURED]
```

### Asset Installation + AMC Activation Flow

```
[Asset Installed] 
  → [QC Approved]
  → [Status: OPERATIONAL]
  → [System retrieves amc_configuration]
  → [Creates asset_amc with start_date=commission_date]
  → [If first asset: Trigger visit schedule generation]
  → [Status: AMC_ACTIVE]
```

### Visit Execution Flow

```
[T-30: System alerts Vendor POC]
  → [POC assigns field staff]
  → [Staff performs maintenance]
  → [Completes checklist + uploads evidence]
  → [Facility POC verifies via OTP]
  → [Report submitted]
  → [AMC Reviewer approves/rejects]
  → [If approved: Visit COMPLETED]
  → [If rejected: Staff resubmits]
```

### Schedule Generation Flow

```
[Asset AMC created]
  → [Get amc_configuration]
  → [Find earliest start_date]
  → [Calculate visit count = duration/frequency]
  → [For each visit: scheduled_date = start + (i * frequency)]
  → [Create scheduled_visit records]
  → [Bundle all assets for facility-vendor in visit details]
```

### To-dos TODO: revisit

- [ ] Design and document complete data model for AMC entities (amc_configuration, asset_amc, scheduled_visits, visit_reports) with all fields, relationships, and indexes
- [ ] Create Flyway migration scripts for all AMC tables following existing patterns in project/field-planner services
- [ ] Set up amc-scheduler-service microservice skeleton with proper package structure, pom.xml, and configuration following existing service patterns
- [ ] Implement AMC configuration APIs (_create, _update, _search) with validators and enrichment services
- [ ] Implement Asset AMC APIs including bulk creation for legacy assets with date validation
- [ ] Build visit schedule generation service with bundling logic and date calculation utilities
- [ ] Create scheduled visit APIs for assignment, rescheduling, and status updates
- [ ] Build visit report submission and approval workflow with OTP verification integration
- [ ] Implement analytics service for dashboards with optimized queries and summary calculations
- [ ] Integrate SMS/email notifications for alerts, upcoming visits, and report status changes
- [ ] Create bulk upload tool for legacy asset AMC data with validation and preview
- [ ] Develop UI components for AMC configuration, visit management, and dashboards in micro-ui
- [ ] Comprehensive testing including unit tests, integration tests, and end-to-end scenarios
