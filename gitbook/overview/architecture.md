# Architecture

The repository is organized as a multi-application platform with backend services, web frontends, a Flutter mobile app, and shared data/API assets.

See the [README.md](../../README.md) for a non-technical overview, the full module list, and quick-start links; [User roles and permissions](user-roles-and-permissions.md) for who can do what; the [CHANGELOG.md](../../CHANGELOG.md) for repository-wide release history; and the [Platform Architecture doc](../../docs/PLATFORM_ARCHITECTURE.md) for the full system diagram — every service, Kafka topic, Elasticsearch index, and external dependency in one place.

## Platform-wide system diagram

This is the full system map: every backend service, how they talk to each other (REST, Kafka, shared Elasticsearch indices), and everything the platform depends on outside this repo. For the complete Kafka topic list, Elasticsearch index list, and external-dependency tables behind this diagram, see [Platform Architecture](../../docs/PLATFORM_ARCHITECTURE.md).

```mermaid
flowchart TB
    subgraph Actors["Users"]
        HCR["Facility Staff (HCR)"]
        Tech["Vendor / Technician"]
        CRM["CRM Operator"]
        POC["State Coordinator / State POC"]
    end

    subgraph Frontend["Frontend (frontend/)"]
        MicroUI["micro-ui"]
        InstallUI["installation-ui"]
    end

    subgraph Hub["Central registries"]
        Facility["health-facility-registry"]
        Boundary["boundary-service"]
    end

    subgraph Domain["E4H domain services (backend/e4h-services)"]
        Asset["asset-registry"]
        AMC["amc-scheduler-service"]
        VendorSvc["vendor-registry"]
        HRMS["egov-hrms"]
        Project["project"]
        FieldPlanner["field-planner"]
        FPActivity["field-planner-activity"]
        IM["im-services"]
        IMAnalytics["im-services-analytics"]
        Inbox["inbox"]
        Ingestion["ingestion-service"]
        Processor["processor-services"]
        RMS["rms-service"]
    end

    subgraph Utilities["Platform utilities (backend/core-services)"]
        MDMS["egov-mdms-service-v2"]
        IdGen["egov-idgen"]
        Workflow["egov-workflow-v2"]
        Filestore["egov-filestore"]
        SMS["egov-notification-sms"]
    end

    Kafka[["Kafka"]]
    ES[("Elasticsearch")]

    subgraph ExternalDIGIT["External DIGIT backbone (not in this repo)"]
        User["egov-user (auth/identity)"]
        Localization["egov-localization"]
        EncSvc["egov-enc-service (PII encryption)"]
        Searcher["egov-searcher"]
        URLShort["egov-url-shortening"]
        PdfSvc["pdf-service"]
    end

    subgraph ExternalThirdParty["Third-party / infrastructure"]
        RMSApi["RMS telemetry API\n(selco.theiox.com)"]
        SMSCountry["SMSCountry SMS gateway"]
        S3[("AWS S3")]
        GMaps["Google Maps API"]
        Kibana["Kibana"]
    end

    HCR --> MicroUI
    Tech --> MicroUI
    CRM --> MicroUI
    POC --> MicroUI
    HCR -.-> InstallUI
    Tech -.-> InstallUI

    MicroUI --> IM
    MicroUI --> Inbox
    MicroUI --> Project
    InstallUI --> FieldPlanner
    InstallUI --> FPActivity
    InstallUI --> AMC
    MicroUI --> GMaps
    InstallUI --> GMaps

    RMSApi --> RMS
    RMS -- "creates ticket" --> IM
    IM -- "ticket status webhook" --> RMS
    RMS -- "save-rms-ticket-pause-indexer" --> ES

    IM -- "save/update-im-request-indexer" --> Kafka
    Kafka -- consumed by --> IMAnalytics
    IMAnalytics -- "escalation reporting" --> ES
    IMAnalytics --> Kibana
    Workflow -- "computed-sla-im-services-write" --> ES
    Inbox --> ES

    FieldPlanner -- "facility_activities" --> FPActivity
    FPActivity --> Asset
    FPActivity --> AMC
    FPActivity --> VendorSvc
    AMC --> Asset
    AMC --> VendorSvc
    AMC --> Project
    Ingestion --> VendorSvc
    Ingestion --> Project
    Ingestion --> FieldPlanner
    Ingestion --> FPActivity
    Ingestion -.->|"direct Postgres access\n(bypasses REST)"| Facility

    Asset --> Facility
    AMC --> Facility
    Project --> Facility
    FieldPlanner --> Facility
    IM --> Facility
    RMS --> Facility
    IMAnalytics --> Facility
    Facility --> Boundary
    VendorSvc --> Boundary

    Domain -.->|"MDMS, IdGen, Workflow"| Utilities
    Utilities -.->|"user/auth, localization,\nencryption, search, URL-shortening, PDF"| ExternalDIGIT
    Domain -.->|"user/auth, localization,\nencryption"| ExternalDIGIT

    SMS --> SMSCountry
    Filestore --> S3
    Processor --> Filestore
```

## High-level structure

- **Users and field teams**
  - → [Frontend React applications](../../frontend)
  - → Flutter mobile application (no `mobile/` directory currently exists in this repository, despite the Mobile layer section below describing one)
  - → Backend: [core services](../../backend/core-services), [E4H services](../../backend/e4h-services)
  - → Registries, workflows, MDMS/master data, [ingestion](../../backend/e4h-services/ingestion-service), and operational jobs

## Backend layer

Backend services are Java/Maven services grouped into:

- [`backend/core-services`](../../backend/core-services): shared platform services such as boundary, filestore, ID generation, MDMS, SMS notification, workflow, facility registry, and gateway support.
- [`backend/e4h-services`](../../backend/e4h-services): E4H domain services such as asset registry, RMS, ingestion, project, field planning, HRMS, inbox, analytics, vendor registry, AMC scheduling, and processor services.

Many services include their own `README.md` (with a Local Setup section) and `CHANGELOG.md`.

## Frontend layer

The frontend is based on DIGIT UI patterns and React/Yarn projects under [`frontend`](../../frontend).

- [`frontend/micro-ui`](../../frontend/micro-ui) contains the main micro UI implementation.
- [`frontend/installation-ui`](../../frontend/installation-ui) contains installation-related web UI.
- [`frontend/build`](../../frontend/build) contains frontend build configuration.

## Mobile layer

The mobile app is a Flutter project under `mobile`. Its code is grouped around BLoCs, repositories, models, pages, routing, utilities, and widgets.

The app includes modules for auth, localization, asset submission, scheduled visits, installation images, summaries, MDMS-backed data, and local cache flows.

## Data layer

For a consolidated, cross-service view of the underlying data model, see the [Entity-Relationship Diagram](../../ERD.md) at the repository root.
