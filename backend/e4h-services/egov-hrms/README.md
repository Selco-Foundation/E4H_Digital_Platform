# eGov HRMS

HRMS manages employees enrolled onto the system, including their assignments, jurisdictions, service history, educational details, departmental tests, and (de)activation history. It is treated as a superset/companion of `egov-user` — every employee created through HRMS is also created as a user in `egov-user`, using the employee code as the login username.

## Service Dependencies
- egov-user (create/update/search users backing each employee)
- egov-localization
- egov-idgen (employee code generation)
- egov-mdms (master data for departments, designations, etc.)
- egov-filestore
- egov-boundary-service (jurisdiction boundary lookups)
- egov-otp (referenced in config; used for OTP-based flows)

## API Endpoints

BasePath: `/egov-hrms/employees`

**Employee** (`EmployeeController`)
- `POST /_create` — bulk-create employee(s) with assignments, jurisdictions, service history, educational details, departmental tests
- `POST /_update` — bulk-update employee(s); also used to deactivate/reactivate an employee (marks the linked user inactive too)
- `POST /_search` — search employees by id, uuid, name, code, status, type, department, designation, position (open search restricted to roles in `open.search.enabled.roles`)
- `POST /_count` — get counts of active/inactive employees for a tenant

## Events

Kafka producer topics, pushed via `HRMSProducer.push()` (topic names are resolved per-tenant via `MultiStateInstanceUtil`):

| Config key | Default topic | Purpose |
|---|---|---|
| `kafka.topics.save.service` | `save-hrms-employee` | New employee created |
| `kafka.topics.update.service` | `update-hrms-employee` | Employee updated |
| `kafka.topics.notification.sms` | `egov.core.notification.sms` | SMS notification to employee's phone number |
| `kafka.topics.hrms.updateData` | `egov-hrms-update` | Data-sync/update event |

No Kafka consumers.

## Configuration

Non-secret config lives in `src/main/resources/application.properties`, bound via `org/egov/hrms/config/PropertiesManager.java`:

| Property | Description |
|---|---|
| `egov.hrms.employee.app.link` | Link to the employee-facing app (env-specific) |
| `egov.hrms.default.pagination.limit` | Default pagination limit for employee search (default 200) |
| `egov.hrms.default.pwd.length` | Length of auto-generated password at employee creation (must match egov-user's password policy) |
| `open.search.enabled.roles` | Role codes allowed to perform open search in HRMS |
| `egov.idgen.ack.name` / `egov.idgen.ack.format` | Idgen key/format for employee code generation (e.g. `EMP-[city]-[SEQ_EG_HRMS_EMP_CODE]`) |
| `state.level.tenant.id` | State-level tenant identifier |
| `decryption.abac.enable` | Toggles ABAC-based decryption flag (`PropertiesManager.isDecryptionEnable`); currently no service-level PII encryption implemented despite this flag |
| `egov.mdms.host`, `egov.user.host`, `egov.localization.host`, `egov.boundary.host`, `egov.filestore.host`, `egov.otp.host` | Host URLs for dependent services |

## Database

Flyway migrations: `src/main/resources/db/migration/main` (12 versioned migrations, `V20190122152236` through `V20260407120000`) plus a `seed` location.

Key tables (all prefixed `eg_hrms_`):
- `eg_hrms_employee` — core employee record (code, phone, name, employee status/type, `active` flag)
- `eg_hrms_assignment` — designation/department assignment per employee
- `eg_hrms_jurisdiction` — hierarchy/boundary-type/boundary for an employee
- `eg_hrms_servicehistory`, `eg_hrms_educationaldetails`, `eg_hrms_departmentaltests`, `eg_hrms_empdocuments`, `eg_hrms_deactivationdetails` — child records FK'd to `eg_hrms_employee.uuid` with `ON DELETE CASCADE`

Conventions:
- Primary keys are the `uuid` column, but it is typed `CHARACTER VARYING(1024)`, not a native Postgres `UUID`.
- Standard audit columns present on every table: `createdby`, `createddate`, `lastmodifiedby`, `lastmodifieddate`.
- No soft-delete column; deactivation is modeled as a status change (`active=false` + a row in `eg_hrms_deactivationdetails`), not a row delete.

## Workflow

No workflow v2 integration. Employee lifecycle (activate/deactivate/reactivate) is handled directly via the `_update` API and status flags, with no `WorkflowService`/state-machine wiring.

## Local Setup

Basic build/run (Java 17, Maven, Spring Boot 3.2.2):

1. Ensure Postgres is reachable and update `spring.datasource.*` / `spring.flyway.*` in `src/main/resources/application.properties` if not using local defaults (`localhost:5432/egov_hrms`).
2. Ensure Kafka is reachable (`spring.kafka.bootstrap.servers`, default `localhost:9092`).
3. Point the dependent service hosts at reachable instances — these default to the public `https://dev.digit.org` instance, not localhost: `egov.mdms.host`, `egov.filestore.host`, `egov.localization.host`, `egov.otp.host`, `egov.user.host`, `egov.boundary.host`, `egov.idgen.host`. `egov.enc.host` defaults to `http://localhost:8088`.
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
   java -jar target/egov-hrms-2.9.0-SNAPSHOT.jar
   ```
6. Service listens on port `9999` under context path `/egov-hrms`.
