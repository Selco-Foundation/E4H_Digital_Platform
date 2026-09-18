# project — Entity-Relationship Diagram

Tables owned by this service, reconciled from its Flyway migrations (`src/main/resources/db/migration`) to their current shape. For how this service's data relates to the rest of the platform, see the root [ERD.md](../../../ERD.md).

```mermaid
erDiagram
    PROJECT {
        varchar id PK
        varchar tenantId
        varchar projectTypeId
        varchar projectType
        varchar projectSubType
        varchar projectNumber
        varchar name
        varchar department
        varchar referenceId
        varchar addressId
        date startDate
        date endDate
        varchar status
        varchar natureOfWork
        boolean isTaskEnabled
        varchar parent FK
    }

    PROJECT_STAFF {
        varchar id PK
        varchar tenantId
        varchar projectId FK
        varchar staffId "external: egov-hrms"
        date startDate
        date endDate
    }

    PROJECT_BENEFICIARY {
        varchar id PK
        varchar tenantId
        varchar projectId FK
        varchar beneficiaryId
        varchar beneficiaryClientReferenceId
        date dateOfRegistration
    }

    PROJECT_TASK {
        varchar id PK
        varchar tenantId
        varchar projectId FK
        varchar projectBeneficiaryId FK
        date plannedStartDate
        date plannedEndDate
        date actualStartDate
        date actualEndDate
        varchar addressId
        varchar status
    }

    TASK_RESOURCE {
        varchar id PK
        varchar tenantId
        varchar taskId FK
        varchar productVariantId
        int quantity
        boolean isDelivered
    }

    PROJECT_RESOURCE {
        varchar id PK
        varchar tenantId
        varchar projectId FK
        varchar productVariantId
        varchar type
        date startDate
        date endDate
    }

    PROJECT_FACILITY {
        varchar id PK
        varchar tenantId
        varchar projectId FK
        varchar facilityId "external: Facility Registry"
    }

    PROJECT ||--o{ PROJECT : "parent (self-referencing)"
    PROJECT ||--o{ PROJECT_STAFF : "projectId"
    PROJECT ||--o{ PROJECT_BENEFICIARY : "projectId"
    PROJECT ||--o{ PROJECT_TASK : "projectId"
    PROJECT ||--o{ PROJECT_RESOURCE : "projectId"
    PROJECT ||--o{ PROJECT_FACILITY : "projectId"
    PROJECT_BENEFICIARY ||--o{ PROJECT_TASK : "projectBeneficiaryId"
    PROJECT_TASK ||--o{ TASK_RESOURCE : "taskId"
```

## External references (not drawn as full entities)

- `PROJECT_STAFF.staffId` → `EMPLOYEE` (egov-hrms)
- `PROJECT_FACILITY.facilityId` → `FACILITY` (health-facility-registry)
- `PROJECT.id` is referenced externally as `project_id` by `field-planner.FIELD_PLAN` and `amc-scheduler-service.AMC_CONFIGURATION` / `SCHEDULED_VISIT`

## Not detailed here

`project_target` (`projectId` FK) and `project_transaction`/`project_comment` (`project_id` FK, workflow audit) exist in this service's schema but are omitted above as supporting/audit tables.
