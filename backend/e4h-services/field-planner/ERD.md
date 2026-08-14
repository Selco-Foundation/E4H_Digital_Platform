# field-planner — Entity-Relationship Diagram

Tables owned by this service, reconciled from its Flyway migrations (`src/main/resources/db/migration`) to their current shape. For how this service's data relates to the rest of the platform, see the root [ERD.md](../../../ERD.md).

```mermaid
erDiagram
    FIELD_PLAN {
        varchar id PK
        varchar tenant_id
        varchar name
        varchar project_id "external: project"
        varchar health_facility_number
        date start_date
        date end_date
        jsonb geography_scope
        jsonb selected_activities
        varchar status
        varchar created_by "external: egov-hrms"
    }

    FIELD_PLAN_FACILITY {
        varchar id PK
        varchar tenant_id
        varchar field_plan_id FK
        varchar facility_id "external: Facility Registry"
        varchar status
    }

    ACTIVITY {
        varchar id PK
        varchar tenant_id
        varchar name
        varchar code
        jsonb required_roles
        int sequence_order
        boolean is_active
    }

    ACTIVITY_ASSIGNMENT {
        varchar id PK
        varchar tenant_id
        varchar field_plan_id FK
        varchar activity_id FK
        varchar assigned_to "external: egov-hrms"
        varchar assigned_by "external: egov-hrms"
        date start_date
        date end_date
        varchar status
        varchar role
    }

    FACILITY_ACTIVITY {
        varchar id PK
        varchar tenant_id
        varchar facility_id "external: Facility Registry"
        varchar activity_id FK
        varchar field_plan_id FK
        varchar assigned_user "external: egov-hrms"
        varchar status
        timestamp scheduled_at
        timestamp activated_at
        timestamp completed_at
    }

    FIELD_PLAN ||--o{ FIELD_PLAN_FACILITY : "field_plan_id"
    FIELD_PLAN ||--o{ ACTIVITY_ASSIGNMENT : "field_plan_id"
    FIELD_PLAN ||--o{ FACILITY_ACTIVITY : "field_plan_id"
    ACTIVITY ||--o{ ACTIVITY_ASSIGNMENT : "activity_id"
    ACTIVITY ||--o{ FACILITY_ACTIVITY : "activity_id"
```

## External references (not drawn as full entities)

- `FIELD_PLAN.project_id` → `PROJECT` (project)
- `FIELD_PLAN_FACILITY.facility_id` / `FACILITY_ACTIVITY.facility_id` → `FACILITY` (health-facility-registry)
- `FIELD_PLAN.created_by`, `ACTIVITY_ASSIGNMENT.assigned_to`/`assigned_by`, `FACILITY_ACTIVITY.assigned_user` → `EMPLOYEE` (egov-hrms)
- `FACILITY_ACTIVITY.id` is referenced externally (as `activity_facility_id`) by `field-planner-activity.BOM` and `field-planner-activity.ACTIVITY_FACILITY_USERS`, and by `asset-registry.ASSET.activity_facility_id` — despite the similar name, `field-planner-activity` is a **separate service** with its own database.
