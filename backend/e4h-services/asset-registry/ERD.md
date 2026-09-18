# asset-registry — Entity-Relationship Diagram

Tables owned by this service, reconciled from its Flyway migrations (`src/main/resources/db/migration`) to their current shape. For how this service's data relates to the rest of the platform, see the root [ERD.md](../../../ERD.md).

```mermaid
erDiagram
    ASSET {
        varchar asset_id PK
        varchar tenant_id
        varchar facility_id "external: Facility Registry"
        varchar asset_type_id
        varchar serial_number
        varchar model_number
        varchar brand_id
        jsonb asset_details
        date warranty_start_date
        date warranty_end_date
        varchar wf_status
        boolean is_active
        boolean is_operational
        varchar activity_facility_id "external: field-planner"
    }

    ASSET_DOCUMENTS {
        varchar id PK
        varchar tenant_id
        varchar asset_id FK
        varchar filestore_id
        varchar document_type
        decimal latitude
        decimal longitude
    }

    ASSET ||--o{ ASSET_DOCUMENTS : "asset_id"
```

## External references (not drawn as full entities)

- `ASSET.facility_id` → `FACILITY` (health-facility-registry)
- `ASSET.activity_facility_id` → `FACILITY_ACTIVITY` (field-planner) — links an asset to the field-plan activity that installed/serviced it
- Consumed by `amc-scheduler-service`'s `ASSET_AMC.asset_id`
