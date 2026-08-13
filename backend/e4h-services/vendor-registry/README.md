# Vendor Registry

A generic registry for organisations operating in the works/facility domain — vendors, contractors, and community-based organisations (CBOs). It stores organisation identity, contact/POC details, tax identifiers, addresses, functional-area classifications, and the staff users linked to each organisation. The service also links organisations to Individual/HRMS/User records and issues SMS notifications on create/update.

Note: the service still identifies internally as "Organisation" in most code (`org.egov`, `OrganisationApiController`, `eg_org*` tables); `vendor-registry` is its e4h module name (`server.contextPath=/vendor`, OpenAPI title "Vendor Registry Service").

## Service Dependencies

- DIGIT backbone services (User, Localization, MDMS, Workflow v2, Boundary/Location)
- Persister (Kafka topics consumed downstream for org and org-user persistence)
- Indexer
- IDGen (application number, org number, org code, function application number)
- Individual
- HRMS (employee sync for org users)
- egov-enc-service (PII encryption of POC mobile numbers)
- facility-service, field-planner-activity (org/facility and field-plan integrations)

## API Endpoints

All active endpoints are served by `OrganisationApiController` under `/organisation/v1`.

Organisation:
- `POST /organisation/v1/_create` — create one or more organisations (no workflow transition)
- `POST /organisation/v1/_search` — search organisations by criteria, returns list + total count
- `POST /organisation/v1/_update` — update organisations (no workflow transition)

Organisation Users:
- `POST /organisation/v1/user/_create` — link a user to an organisation
- `POST /organisation/v1/user/_search` — search organisation-user links
- `POST /organisation/v1/user/_update` — update an organisation-user link
- `POST /organisation/v1/user/_delete` — soft-delete an organisation-user link

There is a second controller, `OrgServicesApiController`, mapped under `/v1` (`_create`, `_search`, `_update`) backed by `OrgService`. Its create/update methods are no-op stubs that just echo the input request back without persisting or calling workflow, and search always returns an empty list. Treat these as inactive/placeholder — do not integrate against them.

## Events

Kafka producer (`OrganizationProducer`, bean name `OrganisationProducer`) publishes to:

| Topic (config key) | Default topic name | Published from |
|---|---|---|
| `org.kafka.create.topic` | `save-org` | Organisation create (`OrganisationService`) |
| `org.kafka.update.topic` | `update-org` | Organisation update (`OrganisationService`) |
| `org.user.kafka.create.topic` | `save-org-users` | Org-user create (`OrganisationUserService`) |
| `org.user.kafka.update.topic` | `update-org-users` | Org-user update (`OrganisationUserService`) |
| `org.user.kafka.delete.topic` | `delete-org-users` | Org-user delete (`OrganisationUserService`) |
| `org.contact.details.update.topic` | `organisation.contact.details.update` | Contact-detail diff on Individual sync (`IndividualService`) |
| `kafka.topics.notification.sms` (`egov.sms.notification.topic`) | `egov.core.notification.sms` | Create/update SMS notifications (`NotificationService`) |

A Kafka consumer, `OrganizationConsumer`, also exists in `org.egov.kafka` for inbound processing.

## Configuration

Defined in `src/main/java/org/egov/config/Configuration.java`, sourced from `application.properties` (non-secret keys only):

- Service: `server.contextPath` / `server.port` (`/vendor`, `8035`), `app.timezone`
- User service: `egov.user.host`, `egov.user.create.path`, `egov.user.search.path`, `egov.user.update.path`
- Individual: `works.individual.host`, `.create/.search/.update.endpoint`
- IDGen: `egov.idgen.host`, `egov.idgen.path`, plus per-sequence name/format keys (`egov.idgen.organisation.application.number.*`, `egov.idgen.organisation.number.*`, `egov.idgen.function.application.number.*`, `egov.idegn.organisation.code.*`)
- Workflow: `is.workflow.enabled`, `egov.workflow.host`, `egov.workflow.transition.path`, `egov.workflow.businessservice.search.path`, `egov.workflow.processinstance.search.path`
- MDMS: `egov.mdms.host`, `egov.mdms.search.endpoint`
- HRMS: `egov.hrms.host`, `.search/.create/.update.endpoint`
- Facility: `egov.facility.host`, `egov.facility.search.path`, `egov.facility.update.path`
- Field-plan activity: `egov.fieldplan.activity.host` + assignment/facility search URLs
- Location/boundary: `egov.location.host`, `egov.location.context.path`, `egov.location.endpoint`, `egov.location.hierarchy.type`
- Localization: `egov.localization.host`, `.context.path`, `.search.endpoint`, `.statelevel`
- Encryption: `egov.enc.host`, `egov.enc.encrypt.endpoint`, `egov.enc.decrypt.endpoint`, `state.level.tenant.id`, `global.tenant.id`
- Notification: `notification.sms.enabled`, `kafka.topics.notification.sms`
- Search paging: `org.default.offset`, `org.default.limit`, `org.search.max.limit`
- Kafka topic names: see Events table above
- Datasource/Flyway/Kafka bootstrap and OTEL exporter settings are standard Spring/OTEL properties in `application.properties`

`application.properties` also defines a default value for `user.default.password` used when auto-provisioning a DIGIT user for a new org POC — treat as a placeholder to override per environment, not a real credential.

## Database

Flyway migrations live under `src/main/resources/db/migration/main` (8 files, `spring.flyway.locations=classpath:/db/migration/main`).

Key tables (all `character varying(256)` string PKs, not UUID):
- `eg_org` — organisation core record: tenant, application number, name, org number, incorporation date, application status, `org_type`/`org_subtype` (PLATFORM/VENDOR, AMC_VENDOR/INSTALLATION_VENDOR), `org_poc_name`/`org_poc_phone`/`org_poc_email`/`org_poc_username`, `org_status`
- `eg_org_address` / `eg_org_address_geo_location` — addresses and geo-coordinates
- `eg_org_contact_detail` — contact name/mobile/email, linked `individual_id`
- `eg_tax_identifier` — tax identifiers, soft-deletable via `is_active`
- `eg_org_jurisdiction`, `eg_org_function` (has `wf_status`), `eg_org_document` (soft-deletable via `is_active`)
- `eg_org_user` — organisation-to-user (staff) links: `organizationid`, `userid`, `isdeleted` soft-delete flag

Audit fields (`created_by`, `last_modified_by`, `created_time`, `last_modified_time`) are set consistently via `OrganisationUtil`/`OrganisationEnrichmentService`. Most writes go through `OrganisationRepository` (query builders + Kafka producer), but `OrganisationUserRepository` executes direct `JdbcTemplate` SQL (select/update/delete) for `eg_org_user` rather than going through the Kafka persister pipeline.

## PII Encryption

The active implementation is `EncryptionDecryptionUtilV2` (`org.egov.util`), called through `OrganisationUtil.encryptMobileNumber()` / `decryptMobileNumber()`. It POSTs to the external `egov-enc-service` (`egov.enc.encrypt.endpoint` / `egov.enc.decrypt.endpoint`) to encrypt/decrypt the organisation POC mobile number (`org_poc_phone`, column comment: "Encrypted using encryption service"):

- On create/update (`OrganisationEnrichmentService`), the plaintext `orgPocPhone` is replaced with its encrypted form before being persisted.
- On read (`OrganisationRepository.getOrganisations`), the stored value is decrypted back to plaintext before being returned in the API response.

This is one of the platform's clearer end-to-end PII-encryption examples (DDL comment, dedicated encrypt/decrypt util, wired into both write and read paths). One correction to the prior guardrails assessment: the classes actually named `EncryptionService.java` and `EncryptionDecryptionUtil.java` (older, non-V2 versions, referencing a different `org.egov.encryption.EncryptionService` library type) are entirely commented out and unused — they are dead code, not the active implementation. `EncryptionDecryptionUtilV2` + `OrganisationUtil` is what actually runs.

## Workflow

`WorkflowUtil` (`org.egov.util`) implements business-service lookup and process-instance transition calls against egov-workflow-v2 (`egov.workflow.host`, transition/search paths), and `eg_org_function.wf_status` exists as a workflow-status column with a supporting index. However, `WorkflowUtil` is not currently invoked from `OrganisationService` — organisation create/update (`createOrganisationWithoutWorkFlow` / `updateOrganisationWithoutWorkFlow`) bypass workflow entirely, and the separate workflow-oriented controller path (`OrgServicesApiController` → `OrgService`) is a no-op stub (see API Endpoints). Workflow integration exists in the codebase but is not wired into the live organisation create/update flow.

## API Specs

https://raw.githubusercontent.com/egovernments/DIGIT-Specs/master/Domain%20Services/Works/Organisation-V1.0.0.yaml

Service also ships its own OpenAPI 3 spec at the repo root: `Organisation_V1.0_OAS3_final.yaml` / `openapi.json`.

## Postman Collection

https://raw.githubusercontent.com/egovernments/DIGIT-Works/master/backend/organisation/docs/Organisation%20Registry%20-%20Test%20Scripts.postman_collection.json

Additional collections under `docs/`: `Organisation Registry - Test Scripts.postman_collection.json`, `old - Organisation_Search.postman_collection.json`.

## Local Setup

Basic build/run (Java 17, Maven, Spring Boot 3.2.2):

1. Ensure Postgres is reachable and update `spring.datasource.*` / `spring.flyway.*` in `src/main/resources/application.properties` if not using local defaults (`localhost:5432/works`, user/password `egov`/`egov`; note `spring.flyway.url` points at a differently-named DB, `digit-works`, by default — align these before running).
2. Ensure Kafka is reachable (`kafka.config.bootstrap_server_config`, default `localhost:9092`).
3. Most dependent-service hosts default to the public `https://works-dev.digit.org` (or `unified-qa`/`unified-dev`) instances rather than localhost — point them at reachable instances if you want isolated local runs: `egov.localization.host`, `egov.mdms.host`, `egov.hrms.host`, `egov.user.host`, `works.individual.host`, `egov.idgen.host`, `egov.workflow.host`, `egov.url.shortner.host`, `egov.enc.host`, `works.cbo.url.host`. `egov.location.host` (boundary) and `egov.facility.host`/`egov.fieldplan.activity.host` default to `localhost`.
4. Build:
   ```bash
   mvn clean install
   ```
5. Run:
   ```bash
   mvn spring-boot:run
   ```
   or run the packaged jar:
   ```bash
   java -jar target/organisation-1.0.1.jar
   ```
6. Service listens on port `8035` under context path `/vendor`.
