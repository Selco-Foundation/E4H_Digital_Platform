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

## Recent platform-wide additions

- **User analytics event pipeline.** Boundary service, Health Facility Registry, AMC Scheduler Service, Field Planner, Field Planner Activity, Project Service, and IM Services now each carry a `UserAnalyticsEvent` model and a per-domain analytics service (`BoundaryAnalyticsService`, `FacilityAnalyticsService`, `AmcAnalyticsService`, `FieldPlannerAnalyticsService`, `ActivityAnalyticsService`, `ProjectAnalyticsService`) that publish best-effort events for domain actions (facility create, visit complete, field plan submit, ICC report upload, logins, etc.) to Kafka. [IM Services Analytics](e4h-services/im-services-analytics.md) is the central consumer: `UserAnalyticsReportService` builds a weekly (Mon–Sun IST) active-user/login report by application, state, and role; `UserAnalyticsExcelService` renders it to an `.xlsx`; `UserAnalyticsMailService` emails it to every HRMS holder of the `USER_ANALYTICS_REPORT` role. `UserAnalyticsController` exposes `POST /v1/user-analytics/_report?weekStartDate=`, triggered weekly by the cron script `backend/docs/cron/run_user_analytics_report.py`. A separate `KibanaDashboardEventListener`/`KibanaDashboardRepository` tracks Kibana-dashboard-view events (no username) that feed the same report.
- **FieldPlanTemplate and ICC report feature** (Field Planner). `FieldPlanTemplate` (model/repository/service/controller/validator/enrichment, migration `V20260611120000__field_plan_template_create_ddl.sql`) lets a template of a field plan be saved and reused. `ICCReportService` uploads an Installation/Commissioning Certificate report to filestore, publishes it to the `saveIccTemplate` Kafka topic, and stores it via `IccTemplateRepository` (migration `V20260702180100__icc_template_create_ddl.sql`), searchable by system type + capacity. The Ingestion Service gained matching `app/config/icc_templates/*.json` BOM configs and `app/utils/icc_report_converter.py` to convert an uploaded ICC report into BOM data.
- **Facility system type / solar capacity master data.** New `FacilitySystemType`, `SystemTypeCapacity`, and related search request/response models in AMC Scheduler Service and Field Planner classify installations by system type and capacity; see [Master data](../data-and-integrations/master-data.md).
- **POC phone number encryption.** Health Facility Registry's `PocPhoneCipher` (backed by `egov-enc-service`) now encrypts POC mobile numbers on every write in `FacilityRepository`; AMC Scheduler Service mirrors this with `FacilityPocPhoneUtil` for its copy of POC data.
- **Current-owner tracking (IM Services).** `CurrentOwnerService` derives the program role currently owning an incident (via the `USER_ANALYTICS.USER_TYPE` MDMS master) and writes `currentOwner`/`currentOwnerSystemRole` onto the incident index on every create/update. `CurrentOwnerBackfillService` exposes `POST /v2/request/_backfill-currentowner` (with a `dryRun` option) to re-derive owner for already-indexed incidents.
- **Mapped vendor.** AMC Scheduler Service's `MappedVendorUtil` resolves which assignee is the AMC field vendor (via HRMS role vs. `amc.mapped.vendor.role.code`) for scheduled-visit and facility indexes. IM Services separately adds a mapped-vendor column to incidents via migration `V20260828120000__incident_mapped_vendor.sql`.
- **RMS fixes.** `TicketCreationGuardService` now resolves the registry facility ID (via HFR ID or NIN lookup) before checking pause state, fixing pauses that silently failed to apply when RMS's `facilityId` differed from the UI pause record's registry ID. Battery deep-discharge ticket generation is now gated by `rms.rule.battery.deep.discharge.tickets.enabled`.

## Maintenance guidance

When a service behavior changes, update the service README and then update this GitBook index if the change affects platform-level understanding.
