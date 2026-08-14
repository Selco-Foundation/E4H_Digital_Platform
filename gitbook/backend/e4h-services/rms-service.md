# RMS Service

## Purpose

The RMS service collects telemetry from RMS devices, applies anomaly detection rules, deduplicates alerts, and creates tickets in Saura eMitra through IM Services.

## Source location

- Service path: [`backend/e4h-services/rms-service`](https://github.com/Selco-Foundation/E4H_Digital_Platform/tree/add-gitbook-docs/backend/e4h-services/rms-service)
- README: [`backend/e4h-services/rms-service/README.md`](https://github.com/Selco-Foundation/E4H_Digital_Platform/blob/add-gitbook-docs/backend/e4h-services/rms-service/README.md)
- OpenAPI spec: [`backend/e4h-services/rms-service/openapi.json`](https://github.com/Selco-Foundation/E4H_Digital_Platform/blob/add-gitbook-docs/backend/e4h-services/rms-service/openapi.json)
- Testing docs: [`README-TESTING.md`](https://github.com/Selco-Foundation/E4H_Digital_Platform/blob/add-gitbook-docs/backend/e4h-services/rms-service/README-TESTING.md), [`TESTING.md`](https://github.com/Selco-Foundation/E4H_Digital_Platform/blob/add-gitbook-docs/backend/e4h-services/rms-service/TESTING.md)
- LLD docs: [`RMS_District_MDMS_Gating_LLD.md`](https://github.com/Selco-Foundation/E4H_Digital_Platform/blob/add-gitbook-docs/docs/rms-service/RMS_District_MDMS_Gating_LLD.md), [`RMS_Ticket_Pause_LLD.md`](https://github.com/Selco-Foundation/E4H_Digital_Platform/blob/add-gitbook-docs/docs/rms-service/RMS_Ticket_Pause_LLD.md)
- ERD: [`backend/e4h-services/rms-service/ERD.md`](https://github.com/Selco-Foundation/E4H_Digital_Platform/blob/add-gitbook-docs/backend/e4h-services/rms-service/ERD.md)

## Architecture

The README describes this flow:

```text
Device Telemetry -> Data Collector -> Rule Engine -> Deduplication Manager -> Payload Generator -> Saura eMitra Connector -> IM Service
```

## Responsibilities

- Fetches telemetry from RMS APIs.
- Applies anomaly rules for panel, inverter, battery, and grid conditions.
- Prevents duplicate tickets through active-alert tracking and suppression windows.
- Generates IM Service ticket payloads.
- Calls IM Services with retry and backoff behavior.
- Tracks ticket IDs after successful creation.
- Syncs and validates RMS Center ID to HFR ID mapping.

## Rule areas from README

- Panel: low solar consumption over consecutive days.
- Inverter: no signal detection.
- Inverter: high voltage.
- Battery: voltage equals zero.
- Battery: deep discharge or overcharge pattern.
- Grid: low or high voltage.

## Data model areas

The README describes:

- `active_alerts`: active alert tracking for deduplication.
- `telemetry_data`: raw telemetry storage where enabled.
- `alert_history`: audit trail for alerts.
- `center_id_to_hfr_id_mapping`: RMS Center ID to HFR ID mapping.

## API surface

All endpoints are served under the `/rms-service` context path. Full request/response schemas are in the OpenAPI spec at `backend/e4h-services/rms-service/openapi.json`.

RMS workflow:

- `POST /rms-service/v1/trigger`: synchronously runs the full RMS workflow (collects facility telemetry, evaluates alert rules, creates IM-service tickets for tripped rules); also used as a CronJob target.
- `POST /rms-service/v1/mapping/sync`: refreshes the center-id-to-HFR-id mapping table from the RMS mapping API, falling back to deriving mappings from the inverter-no-signal facility data collection endpoint if that call fails.
- `POST /rms-service/v1/mapping/validate`: validates existing center-id-to-HFR-id mappings not revalidated within the configured window (default 7 days).

Ticket status:

- `POST /rms-service/v1/ticket/status/update`: webhook called by Saura eMitra on ticket status changes; marks matching `active_alerts` rows resolved on closed/resolved statuses (RESOLVED, CLOSEDAFTERRESOLUTION, REJECTED, CLOSEDAFTERREJECTION).

Ticket pause:

- `POST /rms-service/v1/ticket/pause`: pauses or resumes RMS auto-ticket creation for a facility; `action=PAUSE` requires a future `pausedUntil` timestamp, `action=RESUME` deactivates any active pause; publishes a pause/resume audit Kafka event on both paths.
- `POST /rms-service/v1/ticket/pause/_search`: returns whether a facility currently has an active auto-ticket-creation pause and how many days remain.
- `POST /rms-service/v1/ticket/paused_facility`: lists currently paused facilities under a boundary filter (most specific of boundaryCodes, block, district, state wins).
- `POST /rms-service/v1/ticket/pause/_expire`: CronJob target that deactivates pauses whose `pausedUntil` has elapsed and publishes a RESUME audit event for each.

CO2:

- `POST /rms-service/v1/co2/consumption/monthly/batch`: resolves each requested facility/month/year to a Selco center id and fetches solar/grid kWh consumption for that month from the Selco Elmeasure dashboard graph API; unmapped facilities come back with null consumption and `source=CENTER_NOT_MAPPED`.
- `GET /rms-service/v1/co2/reference`: returns grid intensity factors, archetype lookups, archetype properties, and state sunshine hours for a tenant, consumed by the im-services-analytics monthly CO2-emissions job.

## Configuration

Key properties from README include:

- `rms.api.base.url`
- `rms.api.access.token`
- `im.service.base.url`
- `rms.scheduler.rule.engine.cron`
- `rms.scheduler.solar.daily.cron`
- `rms.rule.*`
- `rms.deduplication.suppression.window.hours`

## Cron references

Related operational manifests are indexed in [Workflows and crons](../workflows-and-crons.md), including RMS mapping sync, validation, pause expiry, and rule engine crons.

## Operational notes

RMS changes can affect ticket volumes, deduplication, field workload, district gating, and pause/resume behavior. Review the testing docs and LLDs before changing rule thresholds, mapping behavior, or IM payload generation.
