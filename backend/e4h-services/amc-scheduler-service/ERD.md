# amc-scheduler-service — Entity-Relationship Diagram

Tables owned by this service, reconciled from its Flyway migrations (`src/main/resources/db/migration`) to their current shape. For how this service's data relates to the rest of the platform, see the root [ERD.md](../../../ERD.md).

```mermaid
erDiagram
    AMC_CONFIGURATION {
        varchar id PK
        varchar tenant_id
        varchar vendor_id "external: vendor-registry"
        varchar facility_id "external: Facility Registry"
        varchar project_id "external: project"
        jsonb asset_types
        int duration_months
        int visit_frequency_months
        date configuration_start_date
        date configuration_end_date
        varchar status
    }

    ASSET_AMC {
        varchar id PK
        varchar tenant_id
        varchar asset_id "external: asset-registry"
        varchar amc_configuration_id FK
        date amc_start_date
        date amc_end_date
        varchar status
        boolean is_legacy_asset
    }

    SCHEDULED_VISIT {
        varchar id PK
        varchar tenant_id
        varchar amc_configuration_id FK
        varchar facility_id "external: Facility Registry"
        varchar facility_name
        varchar project_id "external: project"
        int visit_number
        date scheduled_date
        date actual_visit_date
        date last_scheduled_visit_date
        varchar status
        jsonb visit_report
    }

    AMC_CONFIGURATION ||--o{ ASSET_AMC : "amc_configuration_id"
    AMC_CONFIGURATION ||--o{ SCHEDULED_VISIT : "amc_configuration_id"
```

## External references (not drawn as full entities)

- `AMC_CONFIGURATION.vendor_id` → `VENDOR_ORG` (vendor-registry)
- `AMC_CONFIGURATION.facility_id` / `SCHEDULED_VISIT.facility_id` → `FACILITY` (health-facility-registry)
- `AMC_CONFIGURATION.project_id` / `SCHEDULED_VISIT.project_id` → `PROJECT` (project)
- `ASSET_AMC.asset_id` → `ASSET` (asset-registry)

## Not detailed here

`amc_configuration_assignments`, `scheduled_visit_assignments` (user-assignment join tables) and `visit_transaction` (workflow/process-instance audit log) exist in this service's schema but are omitted from the diagram above as plumbing rather than core domain entities.
