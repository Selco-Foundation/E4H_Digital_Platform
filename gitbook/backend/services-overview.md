# Services overview

The backend is a collection of service applications rather than a single monolith. Services are grouped by responsibility:

- `backend/core-services`: shared platform capabilities used by multiple domains.
- `backend/e4h-services`: E4H domain services for asset, facility, project, RMS, field, ingestion, ticketing, and operations workflows.

Each service page in this GitBook gives the operational shape of the service: purpose, path, responsibilities, dependencies, API or async surface, data references, and where to look next.

## Core service catalog

| Service | Responsibility |
| --- | --- |
| [Boundary service](core-services/boundary-service.md) | Boundary/geography service generated from Swagger/Spring Boot scaffolding. |
| [eGov Filestore](core-services/egov-filestore.md) | File upload, file URL retrieval, thumbnails, and backing storage integration. |
| [eGov IDGen](core-services/egov-idgen.md) | Format-driven ID generation for platform services. |
| [eGov MDMS Service v2](core-services/egov-mdms-service-v2.md) | Master-data management used by services, UI, and workflows. |
| [eGov Notification SMS](core-services/egov-notification-sms.md) | Kafka-driven SMS notification consumer and provider adapter. |
| [eGov Workflow v2](core-services/egov-workflow-v2.md) | Workflow state machine engine for business services, inbox, actions, and SLAs. |
| [Health Facility Registry](core-services/health-facility-registry.md) | Facility registry API service and facility data contracts. |
| [Zuul Gateway](core-services/zuul.md) | Gateway/routing layer for backend traffic. |

## E4H service catalog

| Service | Responsibility |
| --- | --- |
| [AMC Scheduler Service](e4h-services/amc-scheduler-service.md) | Scheduled/project-style service for field and maintenance operations. |
| [Asset Registry](e4h-services/asset-registry.md) | Asset registry APIs, master data, SQL schema, and installation workflow. |
| [eGov HRMS](e4h-services/egov-hrms.md) | Employee records, assignments, jurisdictions, service history, and user linkage. |
| [Field Planner](e4h-services/field-planner.md) | Project, beneficiary, task, and staff planning APIs. |
| [Field Planner Activity](e4h-services/field-planner-activity.md) | Activity-level planning workflows for field execution. |
| [IM Services](e4h-services/im-services.md) | Complaint/ticket lifecycle, workflow integration, notifications, and persistence topics. |
| [IM Services Analytics](e4h-services/im-services-analytics.md) | Incident-management analytics service. |
| [Inbox](e4h-services/inbox.md) | Aggregated workflow/service search for inbox screens. |
| [Ingestion Service](e4h-services/ingestion-service.md) | Python ingestion project for asset-management ingestion processes. |
| [Processor Services](e4h-services/processor-services.md) | Background/async processing service area. |
| [Project Service](e4h-services/project.md) | Project, beneficiary, task, and staff management APIs. |
| [RMS Service](e4h-services/rms-service.md) | Telemetry collection, anomaly rules, deduplication, and IM ticket generation. |
| [Vendor Registry](e4h-services/vendor-registry.md) | Organisation/vendor registry for vendors and related organisations. |

## Documentation rule

Service READMEs remain the closest source of implementation detail. GitBook pages should summarize the service for navigation and link to source contracts, diagrams, setup docs, and operational files.
