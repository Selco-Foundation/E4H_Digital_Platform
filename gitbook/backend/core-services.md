# Core services

Core services live under `backend/core-services`.

They provide reusable platform functions that domain services depend on: geography, file storage, ID generation, master data, notifications, workflow state management, facility registry, and routing.

## Services

| Service | Path | Notes |
| --- | --- | --- |
| [Boundary service](core-services/boundary-service.md) | `backend/core-services/boundary-service` | Boundary and geography API service. |
| [eGov Filestore](core-services/egov-filestore.md) | `backend/core-services/egov-filestore` | Upload/download and storage abstraction service. |
| [eGov IDGen](core-services/egov-idgen.md) | `backend/core-services/egov-idgen` | Format-driven ID generation service. |
| [eGov MDMS Service v2](core-services/egov-mdms-service-v2.md) | `backend/core-services/egov-mdms-service-v2` | Master-data management service. |
| [eGov Notification SMS](core-services/egov-notification-sms.md) | `backend/core-services/egov-notification-sms` | Kafka consumer for SMS notifications. |
| [eGov Workflow v2](core-services/egov-workflow-v2.md) | `backend/core-services/egov-workflow-v2` | Workflow state machine and inbox-support service. |
| [Health Facility Registry](core-services/health-facility-registry.md) | `backend/core-services/health-facility-registry` | Facility registry service. |
| [Zuul Gateway](core-services/zuul.md) | `backend/core-services/zuul` | Gateway/routing service. |

## Documentation references

- Facility API: `docs/facility-registry/facility-v2-api.yaml`.
- Facility master data: `docs/facility-registry/master-data-schema`.
- Facility SQL schema: `docs/facility-registry/schema/V1__create_facility_registry_schema.sql`.
- Facility sequence diagrams: `docs/facility-registry/sequence-diagrams`.

## Recent additions

- **Health Facility Registry** now encrypts POC (point-of-contact) mobile numbers at rest via `facility/util/PocPhoneCipher.java` (backed by `egov-enc-service`), applied on every `FacilityRepository` write and decrypted only on demand.
- **Health Facility Registry** publishes domain events through a new `FacilityAnalyticsService`, feeding the platform-wide user analytics pipeline described in [E4H services](e4h-services.md#recent-platform-wide-additions).
- **Boundary service** similarly gained a `BoundaryAnalyticsService` for the same analytics pipeline.
- **eGov Filestore**'s `StorageController` picked up additional download/content-type handling.

## Maintenance guidance

Keep service-specific setup details in the service directory. Use this page as the cross-service index.
