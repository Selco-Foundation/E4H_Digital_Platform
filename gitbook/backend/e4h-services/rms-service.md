# RMS Service

## Purpose

The RMS service collects telemetry from RMS devices, applies anomaly detection rules, deduplicates alerts, and creates tickets in Saura eMitra through IM Services.

## Source location

- Service path: `backend/e4h-services/rms-service`
- README: `backend/e4h-services/rms-service/README.md`
- Testing docs: `backend/e4h-services/rms-service/README-TESTING.md`, `backend/e4h-services/rms-service/TESTING.md`
- LLD docs: `backend/e4h-services/rms-service/RMS_District_MDMS_Gating_LLD.md`, `backend/e4h-services/rms-service/RMS_Ticket_Pause_LLD.md`

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

Documented endpoints include:

- `POST /rms-service/v1/trigger`: manually triggers RMS workflow execution.
- `POST /rms-service/v1/mapping/sync`: syncs mapping data.

Check the service README and code for the full endpoint list.

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
