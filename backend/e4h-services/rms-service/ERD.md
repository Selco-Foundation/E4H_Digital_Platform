# rms-service — Entity-Relationship Diagram

Tables owned by this service, reconciled from its Flyway migrations (`src/main/resources/db/migration`) to their current shape. For how this service's data relates to the rest of the platform, see the root [ERD.md](../../../ERD.md).

```mermaid
erDiagram
    ACTIVE_ALERTS {
        varchar id PK
        varchar facility_id
        varchar hfr_id "external: Facility Registry"
        varchar alert_type
        varchar alert_sub_type
        varchar status
        timestamp detected_at
        timestamp resolved_at
        timestamp last_suppressed_at
        varchar ticket_id "external: im-services"
        jsonb metadata
    }

    ALERT_HISTORY {
        varchar id PK
        varchar alert_id FK
        varchar facility_id
        varchar hfr_id "external: Facility Registry"
        varchar alert_type
        varchar alert_sub_type
        varchar status
        timestamp detected_at
        timestamp resolved_at
        varchar ticket_id "external: im-services"
    }

    CENTER_ID_TO_HFR_ID_MAPPING {
        varchar id PK
        varchar center_id
        varchar device_id
        varchar device_instance_id
        varchar hfr_id "external: Facility Registry"
        varchar nin_id
        varchar facility_name
        boolean is_active
        timestamp last_sync_time
        timestamp last_validated_at
    }

    RMS_TICKET_PAUSE_CONFIG {
        varchar id PK
        varchar facility_id "external: Facility Registry"
        varchar tenant_id
        varchar facility_name
        varchar boundary_code
        timestamp paused_until
        varchar reason
        varchar requested_by
        boolean is_active
    }

    TELEMETRY_DATA {
        varchar id PK
        varchar facility_id
        varchar hfr_id "external: Facility Registry"
        varchar center_id
        varchar graph_type
        varchar reading_type
        jsonb reading_data
        timestamp collected_at
    }

    ACTIVE_ALERTS ||--o{ ALERT_HISTORY : "alert_id"
```

`CENTER_ID_TO_HFR_ID_MAPPING`, `RMS_TICKET_PAUSE_CONFIG`, and `TELEMETRY_DATA` are not linked with FK lines above because they carry no declared foreign key to `ACTIVE_ALERTS` — they're all independently keyed by `facility_id`/`hfr_id`/`center_id`.

## External references (not drawn as full entities)

- `ACTIVE_ALERTS.hfr_id`, `ALERT_HISTORY.hfr_id`, `TELEMETRY_DATA.hfr_id`, `RMS_TICKET_PAUSE_CONFIG.facility_id` → `FACILITY` (health-facility-registry). `CENTER_ID_TO_HFR_ID_MAPPING` is the resolver table that translates RMS's native `center_id`/`device_id` into an `hfr_id`, since raw RMS telemetry APIs return center IDs, not Facility Registry IDs.
- `ACTIVE_ALERTS.ticket_id` / `ALERT_HISTORY.ticket_id` → `INCIDENT` (`eg_incident_v2` in im-services) — the ticket RMS auto-creates when an alert fires.
- There is no DB-level foreign key across any of these service boundaries (each service has its own database) — all links above are logical, via shared ID values.

## Not detailed here

A handful of CO2-dashboard reference tables (tenant/state-scoped, not tied to a facility) also live in this service's migrations but are omitted above as out of scope for a facility-centric ERD.
