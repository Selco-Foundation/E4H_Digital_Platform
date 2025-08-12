# E4H Field Planner - Architecture Diagrams

This folder contains comprehensive Mermaid diagram files for the E4H Field Planner system architecture and detailed workflows.

## Architecture Diagrams

1. **`system_architecture.mmd`** - Complete system architecture showing microservices, data layer, and external systems
2. **`database_schema.mmd`** - Entity-relationship diagram for the database schema (original standalone version)
3. **`database_schema_integrated.mmd`** - **NEW** - E4H Platform integrated database schema showing existing vs new tables

## Detailed Sequence Diagrams

### Core Workflows

3. **`01_project_creation_workflow.mmd`** - **UPDATED WITH E4H INTEGRATION** - Complete project creation process including:
   - E4H authentication and authorization via existing services
   - Integration with eGov HRMS for user validation
   - Project creation via existing Project Service
   - Field plan creation via new Field Planner Service
   - Health facility validation via existing Health Facility Registry
   - eGov Filestore for template generation and uploads
   - MDMS integration for master data validation

4. **`02_field_plan_creation_workflow.mmd`** - **UPDATED WITH E4H INTEGRATION** - Field plan creation and activity assignment including:
   - E4H authentication and project validation via existing services
   - Integration with eGov HRMS for SPOC validation and management
   - Health Facility Registry integration for facility operations
   - eGov Filestore for Excel template generation and processing
   - eGov MDMS integration for master data and dropdowns
   - Activity Management Service coordinating with existing E4H services

5. **`03_facility_assignment_workflow.mmd`** - **UPDATED WITH E4H INTEGRATION** - Health facility assignment by Activity SPOCs including:
   - eGov HRMS integration for team member management
   - Activity Management Service for facility assignment logic
   - Health Facility Registry for facility data validation
   - eGov Filestore for bulk assignment templates
   - E4H notification system for assignment notifications
   - Individual and bulk facility assignments with E4H patterns

### Activity Management Workflows

6. **`04_activity_report_workflow.mmd`** - **UPDATED WITH E4H INTEGRATION** - Complete activity report lifecycle including:
   - E4H authentication for field staff via mobile app
   - Integration with Health Facility Registry for facility data
   - eGov HRMS validation for field staff and reviewers
   - eGov Filestore for report attachments
   - eGov Workflow v2 for report review workflows
   - Activity Management Service for report processing
   - Field data collection with offline support maintained

7. **`05_user_management_workflow.mmd`** - **UPDATED WITH E4H INTEGRATION** - User creation and team management including:
   - Integration with eGov HRMS for ALL employee management operations
   - Single and bulk employee creation via existing eGov HRMS service
   - Team management using eGov HRMS supervisor-employee relationships
   - eGov Filestore for bulk creation templates  
   - E4H audit patterns and notification system integration
   - No duplicate user management - leverages existing E4H infrastructure

### System Workflows

8. **`06_conditional_activation_workflow.mmd`** - Conditional health facility activation including:
   - Automated condition evaluation (cron-based)
   - Different activity condition types (Assessment, Installation, Field QC, Handover)
   - Manual activation triggers
   - Condition configuration management
   - Project-specific overrides
   - Error handling and recovery

9. **`07_mobile_sync_workflow.mmd`** - **UPDATED WITH E4H INTEGRATION** - Mobile app synchronization including:
   - E4H platform authentication and JWT token management
   - Mobile Sync Service integration with existing E4H services
   - Health Facility Registry data synchronization
   - eGov HRMS field staff validation
   - eGov Filestore for template and attachment management
   - Online/offline data sync with E4H PostgreSQL database
   - Background synchronization and conflict resolution maintained

10. **`field_plan_workflow.mmd`** - Original simplified field plan workflow (legacy)

## Converting to PNG

You can convert these Mermaid diagrams to PNG images using several methods:

### Method 1: Online Mermaid Editor (Easiest)

1. Go to [mermaid.live](https://mermaid.live/)
2. Copy the content from any `.mmd` file
3. Paste it into the editor
4. Click "Export" → "PNG" to download the image

### Method 2: Mermaid CLI (Local)

Install the Mermaid CLI:
```bash
npm install -g @mermaid-js/mermaid-cli
```

Convert individual diagrams:
```bash
mmdc -i system_architecture.mmd -o system_architecture.png --width 2000 --height 1500
mmdc -i database_schema.mmd -o database_schema.png --width 2000 --height 1500
mmdc -i 01_project_creation_workflow.mmd -o project_creation_workflow.png --width 2500 --height 2000
mmdc -i 02_field_plan_creation_workflow.mmd -o field_plan_creation_workflow.png --width 2500 --height 2000
mmdc -i 03_facility_assignment_workflow.mmd -o facility_assignment_workflow.png --width 2500 --height 2000
mmdc -i 04_activity_report_workflow.mmd -o activity_report_workflow.png --width 2500 --height 2000
mmdc -i 05_user_management_workflow.mmd -o user_management_workflow.png --width 2500 --height 2000
mmdc -i 06_conditional_activation_workflow.mmd -o conditional_activation_workflow.png --width 2500 --height 2000
mmdc -i 07_mobile_sync_workflow.mmd -o mobile_sync_workflow.png --width 2500 --height 2000
```

### Method 3: Batch Conversion Scripts

Use the provided scripts to convert all diagrams at once:

**Windows:**
```bash
# PowerShell
./convert_to_png.ps1

# Command Prompt
convert_to_png.bat
```

**Linux/Mac:**
```bash
./convert_to_png.sh
```

### Method 4: VS Code Extension

1. Install the "Mermaid Markdown Syntax Highlighting" extension
2. Open any `.mmd` file
3. Use the preview feature to view the diagram
4. Right-click and "Save as PNG"

### Method 5: Other Online Tools

- [Mermaid Chart](https://www.mermaidchart.com/)
- [Kroki](https://kroki.io/)
- [Draw.io](https://draw.io) (has Mermaid support)

## Diagram Descriptions

### Architecture Diagrams
- **System Architecture**: Shows the complete microservices architecture with frontend, API gateway, services, and data layers
- **Database Schema**: Comprehensive ERD showing all entities, relationships, and key constraints

### Workflow Sequence Diagrams
Each sequence diagram shows the complete end-to-end flow including:
- **User interactions** and UI flows
- **API calls** with request/response details
- **Service interactions** and business logic
- **Database transactions** and data persistence
- **Error handling** and edge cases
- **Audit logging** and compliance
- **Notification workflows**
- **Caching strategies**
- **Background processing**

### Key Features Covered
- ✅ **Authentication & Authorization** - JWT tokens, RBAC, session management
- ✅ **File Upload & Validation** - Excel templates, validation, error reporting
- ✅ **Offline Support** - Mobile sync, conflict resolution, local storage
- ✅ **Workflow Management** - Conditional activation, state transitions
- ✅ **Team Management** - User creation, role assignment, team collaboration
- ✅ **Audit & Compliance** - Complete audit trails, change logging
- ✅ **Error Handling** - Graceful degradation, retry mechanisms
- ✅ **Performance** - Caching, async processing, bulk operations

## Customization

You can modify the diagrams by editing the `.mmd` files:

- **Colors**: Change the `style` lines at the end of each diagram
- **Layout**: Modify the `subgraph` structures  
- **Content**: Add/remove participants, messages, and flows
- **Notes**: Update annotations and explanations

## High-Resolution Output

For high-resolution PNG output using Mermaid CLI:
```bash
mmdc -i diagram_name.mmd -o diagram_name.png --width 3000 --height 2500 --backgroundColor white
```

## SVG Format

For scalable vector graphics:
```bash
mmdc -i diagram_name.mmd -o diagram_name.svg
```

## PDF Format

For PDF output:
```bash
mmdc -i diagram_name.mmd -o diagram_name.pdf
```

## Documentation Integration

These diagrams can be embedded directly into:
- **Confluence** pages
- **GitHub/GitLab** README files
- **Technical documentation**
- **Architecture decision records (ADRs)**
- **API documentation**

## E4H Platform Integration

### Updated Diagrams for E4H Integration

The following diagrams have been **revised to show proper integration** with the existing E4H Digital Platform infrastructure:

- **`database_schema_integrated.mmd`** - NEW diagram showing integration with existing E4H tables
- **`01_project_creation_workflow.mmd`** - UPDATED to use existing Project Service and eGov services
- **`04_activity_report_workflow.mmd`** - UPDATED to integrate with Health Facility Registry, eGov HRMS, eGov Filestore
- **`07_mobile_sync_workflow.mmd`** - UPDATED to use E4H authentication and existing services

### Key Integration Points

**Existing E4H Services Used:**
- **eGov HRMS** - User/employee management and validation
- **Health Facility Registry** - Facility master data and operations
- **Project Service** - Existing project management (extended)
- **eGov Workflow v2** - State management and approval workflows
- **eGov Filestore** - File upload, download, and template generation
- **eGov MDMS v2** - Master data management and validation
- **E4H Auth Service** - Authentication and authorization
- **E4H API Gateway** - Routing and middleware

**New Field Planner Services:**
- **Field Planner Service** - Field plan management and coordination
- **Activity Management Service** - Activity execution and report processing
- **Mobile Sync Service** - Mobile app data synchronization

**Shared Infrastructure:**
- **E4H PostgreSQL Database** - Shared database with new Field Planner tables
- **Redis Cache** - Shared caching layer
- **Kafka Message Queue** - Event-driven notifications

### Benefits of E4H Integration

✅ **Consistency** - Follows existing E4H patterns and conventions  
✅ **Data Integrity** - Uses existing facility and user data sources  
✅ **Reduced Duplication** - Leverages existing services instead of rebuilding  
✅ **Security** - Uses established E4H authentication and authorization  
✅ **Maintainability** - Integrates with existing deployment and monitoring  
✅ **Scalability** - Benefits from E4H platform optimization  

## Maintenance Notes

- Keep diagrams updated as the system evolves
- Version control diagram changes alongside code changes
- Consider diagram complexity vs. readability when adding details
- Use consistent naming conventions across all diagrams
- Include error flows and edge cases for comprehensive coverage
- **Ensure new changes maintain E4H platform integration patterns** 