# E4H Digital Platform

[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](LICENSE)
[![Docs](https://img.shields.io/badge/docs-GitBook-3884FF.svg)](gitbook/README.md)
[![Backend](https://img.shields.io/badge/backend-Java%2017%20%2F%20Spring%20Boot-6DB33F.svg)](backend)
[![Frontend](https://img.shields.io/badge/frontend-React-61DAFB.svg)](frontend)

Support and maintenance platform for Selco Foundation's **Energy for Health (E4H)** program, which brings reliable solar power to rural health facilities across India.

## What this is

Health facilities run on solar power systems (panels, inverters, batteries) that need to be tracked, maintained, and repaired. This platform is how that happens day to day:

- **Facility & asset tracking** — a registry of health facilities and the solar equipment installed at each one.
- **Remote monitoring & ticketing** — telemetry from solar devices is watched automatically; when something looks wrong (a panel underperforming, a battery reading zero, an inverter going silent), a ticket is raised for a technician without anyone needing to notice manually.
- **Field operations** — scheduling technician visits, tracking annual maintenance contracts (AMC), and recording completed work.
- **HR & vendor management** — the staff and vendor organizations who carry out the maintenance work.
- **Web applications** for the employees and implementation teams who run all of the above.

It builds on the open-source [DIGIT](https://core.digit.org/) eGovernance platform and adapts it for E4H's specific workflows.

Full documentation — architecture, setup guides, per-service API references, workflows, and low-level design docs — lives in this repo's [GitBook](gitbook/README.md).

## Architecture

```text
Solar devices (panel / inverter / battery / grid)
        |
        v
  RMS Service  ---- telemetry polling + anomaly rules ----> IM Service (ticketing)
        |                                                         |
        v                                                         v
 Facility Registry <---- linked by facility ----------->  Field Planner / AMC Scheduler
        |                                                         |
        v                                                         v
  Asset Registry                                    HRMS / Vendor Registry (who does the work)
        |
        v
  Web UI (frontend/micro-ui) <---- used by employees & implementation teams
```

Backend services are Java/Spring Boot microservices (one exception noted below), talking to each other over REST and Kafka, each with its own Postgres schema. They fall into two groups:

- `backend/core-services` — shared platform primitives inherited from DIGIT/eGov (boundary, ID generation, file storage, workflow engine, notifications, gateway).
- `backend/e4h-services` — domain services built for E4H itself (asset tracking, RMS, ticketing, field planning, HR, vendor management, ingestion).

See [Architecture](gitbook/overview/architecture.md) for the full breakdown.

## Modules

### Core services (`backend/core-services`)

| Service | Purpose |
|---|---|
| [boundary-service](backend/core-services/boundary-service/README.md) | Administrative boundary/geography hierarchy |
| [egov-filestore](backend/core-services/egov-filestore/README.md) | File upload/storage |
| [egov-idgen](backend/core-services/egov-idgen/README.md) | Sequential/formatted ID generation |
| [egov-mdms-service-v2](backend/core-services/egov-mdms-service-v2) | Master data (MDMS) service — no README yet |
| [egov-notification-sms](backend/core-services/egov-notification-sms/README.md) | SMS dispatch |
| [egov-workflow-v2](backend/core-services/egov-workflow-v2/README.md) | Generic workflow/state-machine engine |
| [health-facility-registry](backend/core-services/health-facility-registry/README.md) | Health facility registry (core-services version) |
| [zuul](backend/core-services/zuul) | API gateway — no README yet |

### E4H services (`backend/e4h-services`)

| Service | Purpose |
|---|---|
| [amc-scheduler-service](backend/e4h-services/amc-scheduler-service/README.md) | Annual maintenance contract (AMC) visit scheduling for solar assets |
| [asset-registry](backend/e4h-services/asset-registry/README.md) | Tracks installed solar systems/assets, brands, counts, warranties |
| [egov-hrms](backend/e4h-services/egov-hrms/README.md) | Employee/HR management |
| [field-planner](backend/e4h-services/field-planner/README.md) | Field plans linking facilities to installation/maintenance work |
| [field-planner-activity](backend/e4h-services/field-planner-activity/README.md) | Per-facility activity assignment and staff tracking for field plans |
| [im-services](backend/e4h-services/im-services/README.md) | Incident management ("Saura eMitra") — ticketing for facility issues |
| [im-services-analytics](backend/e4h-services/im-services-analytics/README.md) | Analytics/reporting over incident data |
| [inbox](backend/e4h-services/inbox/README.md) | Unified task inbox across workflow-backed services |
| [ingestion-service](backend/e4h-services/ingestion-service/README.md) | Bulk ingestion of facility/boundary/vendor data (Python, not Java) |
| [processor-services](backend/e4h-services/processor-services/README.md) | Video/image processing (ffmpeg) for uploaded media |
| [project](backend/e4h-services/project/README.md) | Project registry linking facilities, staff, beneficiaries, resources |
| [rms-service](backend/e4h-services/rms-service/README.md) | Remote Monitoring System — telemetry polling, anomaly detection, auto-ticketing |
| [translator-service](backend/e4h-services/translator-service) | Not currently in this repository — see note below |
| [vendor-registry](backend/e4h-services/vendor-registry/README.md) | Vendor/organisation registry |

### Frontend (`frontend`)

| App | Purpose |
|---|---|
| [micro-ui](frontend/micro-ui/README.md) | Main employee/implementation-team web application (DIGIT micro UI) |
| [installation-ui](frontend/installation-ui/README.md) | Installation-focused web UI |

> **Note:** `translator-service` has no source checked into this repository (only a local, gitignored build-output folder exists on some machines) — treat it as not yet part of this codebase rather than a working service.

## Getting started

1. Clone the repository and check out the branch you're working from.
2. Pick the area you need — you don't need to set up the whole platform to work on one piece:
   - Backend: Java 17 + Maven, Postgres, Kafka. See each service's README "Local Setup" section, or the [Backend setup guide](gitbook/getting-started/backend-setup.md).
   - Frontend: Node.js + Yarn. See the [Frontend setup guide](gitbook/getting-started/frontend-setup.md).
3. For full prerequisites and workflow, see [Local setup](gitbook/getting-started/local-setup.md) in the GitBook.

## Documentation

- [GitBook home](gitbook/README.md) — architecture, per-service pages, API reference, LLDs, operations.
- [Repository map](gitbook/overview/repository-map.md) — what lives where.
- [Security policy](SECURITY.md) — how to report a vulnerability.

## License

[MIT](LICENSE) © Selco Foundation
