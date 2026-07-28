# E4H services

E4H services live under `backend/e4h-services`.

They implement the platform's E4H domain behavior: assets, projects, field planning, incident management, RMS telemetry, ingestion, vendors, HRMS, inbox aggregation, and background processing.

## Services

| Service | Path | Notes |
| --- | --- | --- |
| [AMC Scheduler Service](e4h-services/amc-scheduler-service.md) | `backend/e4h-services/amc-scheduler-service` | Scheduling automation for AMC and field work. |
| [Asset Registry](e4h-services/asset-registry.md) | `backend/e4h-services/asset-registry` | Asset domain service and installation workflow. |
| [eGov HRMS](e4h-services/egov-hrms.md) | `backend/e4h-services/egov-hrms` | Employee, assignment, jurisdiction, and user-linkage service. |
| [Field Planner](e4h-services/field-planner.md) | `backend/e4h-services/field-planner` | Field planning and project/task/staff flows. |
| [Field Planner Activity](e4h-services/field-planner-activity.md) | `backend/e4h-services/field-planner-activity` | Activity-level field planning workflows. |
| [IM Services](e4h-services/im-services.md) | `backend/e4h-services/im-services` | Incident management complaint/ticket service. |
| [IM Services Analytics](e4h-services/im-services-analytics.md) | `backend/e4h-services/im-services-analytics` | Analytics for incident management services. |
| [Inbox](e4h-services/inbox.md) | `backend/e4h-services/inbox` | Aggregated inbox and workflow/service search. |
| [Ingestion Service](e4h-services/ingestion-service.md) | `backend/e4h-services/ingestion-service` | Python ingestion processes for asset management. |
| [Processor Services](e4h-services/processor-services.md) | `backend/e4h-services/processor-services` | Background or async processing services. |
| [Project Service](e4h-services/project.md) | `backend/e4h-services/project` | Project, beneficiary, task, and staff APIs. |
| [RMS Service](e4h-services/rms-service.md) | `backend/e4h-services/rms-service` | RMS telemetry, rule engine, deduplication, and IM ticket generation. |
| [Vendor Registry](e4h-services/vendor-registry.md) | `backend/e4h-services/vendor-registry` | Organisation/vendor registry service. |

## Documentation references

- Asset registry API: `docs/asset-registry/asset-registry-1.0.0.yaml`.
- Asset registry master data and SQL: `docs/asset-registry`.
- Project API: `docs/project-service/project-v1.api.yaml`.
- Ingestion schemas and sequence diagrams: `docs/ingestion`.
- RMS detailed notes: `backend/e4h-services/rms-service`.

## Maintenance guidance

When a service behavior changes, update the service README and then update this GitBook index if the change affects platform-level understanding.
