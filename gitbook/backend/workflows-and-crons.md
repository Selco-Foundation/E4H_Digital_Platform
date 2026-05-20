# Workflows and crons

This page indexes workflow definitions, UI/backend sequence diagrams, and operational cron manifests.

## Workflow definitions

- Asset installation workflow: `docs/asset-registry/workflows/AssetInstallationWorkflow.json`.

## Sequence diagrams

- Facility create and update: `docs/facility-registry/sequence-diagrams`.
- Boundary, facility, and vendor ingestion: `docs/ingestion/sequence-diagrams`.
- UI flows such as asset submit, facility search, forgot password, and login: `docs/ui-sequence-diagrams`.

## Cron manifests

Cron and scheduled job manifests live under `backend/docs`.

Known manifests include:

- `automation-cronjob.yaml`
- `daily-escalation-cronjob.yaml`
- `rms-mapping-sync-cron.yaml`
- `rms-mapping-validate-cron.yaml`
- `rms-pause-expiry-cron.yaml`
- `rms-rule-engine-cron.yaml`
- `visit-scheduling-cronjob.yaml`
- `weekly-escalation-cronjob.yaml`

## Operational guidance

When a workflow, sequence diagram, or cron changes, update this page and the related service documentation so operators can find the runtime behavior from GitBook.
