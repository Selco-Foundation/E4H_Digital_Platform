# vendor-registry — Entity-Relationship Diagram

Tables owned by this service, reconciled from its Flyway migrations (`src/main/resources/db/migration`) to their current shape. For how this service's data relates to the rest of the platform, see the root [ERD.md](../../../ERD.md).

```mermaid
erDiagram
    EG_ORG {
        varchar id PK
        varchar tenant_id
        varchar application_number
        varchar name
        varchar org_number
        varchar code
        varchar org_type "PLATFORM or VENDOR"
        varchar org_subtype "AMC_VENDOR or INSTALLATION_VENDOR"
        varchar org_status
        varchar application_status
        boolean is_active
        varchar org_poc_name
        varchar org_poc_phone
        varchar org_poc_email
    }

    EG_ORG_USER {
        varchar id PK
        varchar tenantid
        varchar organizationid FK
        varchar userid "external: shared user identity"
        boolean isdeleted
    }

    EG_ORG_ADDRESS {
        varchar id PK
        varchar org_id FK
    }

    EG_ORG_CONTACT_DETAIL {
        varchar id PK
        varchar org_id FK
        varchar individual_id "external: individual/citizen identity"
    }

    EG_ORG_JURISDICTION {
        varchar id PK
        varchar org_id FK
    }

    EG_ORG_FUNCTION {
        varchar id PK
        varchar org_id FK
    }

    EG_TAX_IDENTIFIER {
        varchar id PK
        varchar org_id FK
    }

    EG_ORG ||--o{ EG_ORG_USER : "organizationid"
    EG_ORG ||--o{ EG_ORG_ADDRESS : "org_id"
    EG_ORG ||--o{ EG_ORG_CONTACT_DETAIL : "org_id"
    EG_ORG ||--o{ EG_ORG_JURISDICTION : "org_id"
    EG_ORG ||--o{ EG_ORG_FUNCTION : "org_id"
    EG_ORG ||--o{ EG_TAX_IDENTIFIER : "org_id"
```

## External references (not drawn as full entities)

- `EG_ORG_USER.userid` → `EMPLOYEE` (egov-hrms) — same identity space as the platform user/HRMS employee `uuid`
- `EG_ORG_CONTACT_DETAIL.individual_id` → an individual/citizen identity, not an HRMS employee
- `EG_ORG.id` is referenced externally as `vendor_id`/`accountid` by `amc-scheduler-service.AMC_CONFIGURATION` and `im-services.INCIDENT`

## Not detailed here

`eg_org_address_geo_location` (child of `eg_org_address`) and `eg_org_document` (child of `eg_org_function`) exist but are omitted as attachment/detail tables.
