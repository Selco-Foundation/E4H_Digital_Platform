# E4H Field Planner - Architecture Diagrams

This folder contains comprehensive Mermaid diagram files for the E4H Field Planner system architecture and detailed workflows.

## Architecture Diagrams

1. **`system_architecture.mmd`** - Complete system architecture showing microservices, data layer, and external systems
2. **`database_schema.mmd`** - Entity-relationship diagram for the database schema

## Detailed Sequence Diagrams

### Core Workflows

3. **`01_project_creation_workflow.mmd`** - Complete project creation process including:
   - Project metadata validation
   - Health facility template generation and upload
   - Excel validation and error handling
   - Async facility processing
   - Audit logging and notifications

4. **`02_field_plan_creation_workflow.mmd`** - Field plan creation and activity assignment including:
   - Project selection and field plan setup
   - Facility selection and template processing
   - Activity assignment to SPOCs
   - Workflow initialization and notifications
   - SPOC onboarding flow

5. **`03_facility_assignment_workflow.mmd`** - Health facility assignment by Activity SPOCs including:
   - Team member management
   - Individual and bulk facility assignments
   - Conditional activation triggers
   - Mobile app notifications
   - Progress monitoring

### Activity Management Workflows

6. **`04_activity_report_workflow.mmd`** - Complete activity report lifecycle including:
   - Field data collection with offline support
   - Report submission and validation
   - QC review process (Approve/Reject/Flag for Field QC)
   - Notification workflows
   - Progress tracking and audit trails

7. **`05_user_management_workflow.mmd`** - User creation and team management including:
   - Single and bulk user creation
   - Email verification process
   - Role management and permissions
   - Team member lifecycle (add/update/deactivate/remove)
   - Audit logging for all user operations

### System Workflows

8. **`06_conditional_activation_workflow.mmd`** - Conditional health facility activation including:
   - Automated condition evaluation (cron-based)
   - Different activity condition types (Assessment, Installation, Field QC, Handover)
   - Manual activation triggers
   - Condition configuration management
   - Project-specific overrides
   - Error handling and recovery

9. **`07_mobile_sync_workflow.mmd`** - Mobile app synchronization including:
   - Online/offline data sync
   - Background synchronization
   - Conflict resolution strategies
   - Offline data collection
   - Progress auto-save and recovery
   - Network connectivity handling

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

## Maintenance Notes

- Keep diagrams updated as the system evolves
- Version control diagram changes alongside code changes
- Consider diagram complexity vs. readability when adding details
- Use consistent naming conventions across all diagrams
- Include error flows and edge cases for comprehensive coverage 