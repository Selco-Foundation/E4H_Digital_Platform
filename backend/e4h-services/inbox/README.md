# Inbox

Inbox is a cross-module aggregation service (BFF) that combines workflow-v2 process state with Elasticsearch-indexed
application data from municipal/E4H services (FSM, BPA, TL, NOC, WS, SW, PT, Billing amendments, etc.) to serve a
single paginated, role-filtered "inbox" view per user. It does not own any application data itself — it queries
workflow and per-module ES indices/searcher endpoints and stitches the results together, applying role-based
inbox/action filtering per module. Two API generations exist side by side: a v1 controller (`InboxService`,
module-specific filter services) and a v2 controller (`InboxServiceV2`, generic ES-driven query builder).

## Service Dependencies

- workflow-v2
- user-service
- egov-searcher
- The Municipal/E4H service for which inbox configuration is added (e.g. FSM, BPA, TL, NOC, WS, SW, PT, vehicle, billing-service)

## API Endpoints

Base context path: `/inbox`

### v1 (`/v1`, `InboxController`)
- `POST /v1/_search` — search inbox application data (workflow + module data) based on criteria
- `POST /v1/dss/_search` — aggregate metric data for DSS charts (`DSSInboxFilterService`)
- `POST /v1/elastic/_search` — placeholder ES search endpoint (currently returns null; not wired up)

### v2 (`/v2`, `InboxV2Controller`)
- `POST /v2/_search` — generic ES + workflow-driven inbox search (`InboxServiceV2#getInboxResponse`)
- `POST /v2/project/_search` — inbox search returning project-module-shaped results (`getInboxResponseProject`)
- `POST /v2/_getFields` — fetch a specific set of fields from the configured ES index (`getSpecificFieldsFromESIndex`)

## Events

Inbox is read-only with respect to Kafka: it has no producers or consumers, and no topics are configured in
`application.properties`. All data is fetched synchronously over REST/ES at request time; nothing is persisted or
published by this service.

## Configuration

Key properties (see `src/main/resources/application.properties`, bound via `InboxConfiguration.java`):

- `server.servlet.context-path=/inbox`, `server.port=9011`
- `workflow.host`, `workflow.process.search.path`, `workflow.businessservice.search.path`,
  `workflow.process.count.path`, `workflow.process.statuscount.path`, `workflow.process.nearing.sla.count.path`
- `service.search.mapping` — JSON map of business-service/module -> search path, data root, and ID params used to
  fetch application data per module (FSM, PT, TL, BPA, NOC, WS, SW, billing amendments, vehicle trips, etc.)
- `bs.businesscode.service.search` — analogous mapping for billing-service-driven searches (WS/SW)
- `egov.searcher.*` — per-module egov-searcher endpoints (search/count/desc) for PT, TL, BPA, NOC, FSM, WS, SW, BS
- `egov.user.host`, `egov.user.search.path`, `egov.user.create.path`
- `egov.mdms.host`, `egov.mdms.search.endpoint` (used by `MDMSUtil`)
- `egov.boundary.host`, `egov.boundary.search.endpoint`
- `egov.es.username`, `egov.es.password`, `services.esindexer.host` — Elasticsearch connection (credentials must be
  overridden per environment, not left at repo defaults)
- `water.es.index`, `sewerage.es.index`, `es.search.pagination.default.limit/offset`, `es.search.pagination.max.search.limit`,
  `es.search.default.sort.order` — ES search/pagination defaults
- `inbox.water.search.allowed`, `parent.level.tenant.id`, `state.level.tenant.id`, `state.level.tenantid.length`,
  `is.environment.central.instance`, `cache.expiry.minutes`
- `spring.flyway.enabled=false` — no owned schema/migrations

## Data Access

Inbox is a read-only BFF/aggregator per guardrail §6/§7 (Search and Data Access; BFFs and Aggregator APIs). It never
writes to any owning service's database and has no persistence layer of its own — `DataSourceAutoConfiguration`,
`DataSourceTransactionManagerAutoConfiguration`, and `HibernateJpaAutoConfiguration` are explicitly excluded in
`InboxApplication`. Reads happen through two paths: (1) `WorkflowService` querying workflow-v2's process/business-service
APIs, and (2) `ElasticSearchRepository`/`ElasticSearchQueryBuilder` (and, on v2, `InboxQueryBuilder`) querying
module-owned Elasticsearch read models, with per-module filter services (`FSMInboxFilterService`, `BPAInboxFilterService`,
`TLInboxFilterService`, `NOCInboxFilterService`, `WSInboxFilterService`, `SWInboxFilterService`, `PtInboxFilterService`,
`BillingAmendmentInboxFilterService`) merging results and applying role-based inbox/action visibility from the
requesting user's roles.

## Local Setup

A `LOCALSETUP.md` already exists in this directory with dependency/port-forwarding details; summary below.

Build:

```bash
cd backend/e4h-services/inbox
mvn clean install
```

Run (after building, or directly via Spring Boot):

```bash
mvn spring-boot:run
# or
java -jar target/inbox-1.3.1-SNAPSHOT.jar
```

Before running locally, port-forward `egov-workflow-v2`, `user-service`, `egov-searcher`, and the municipal/E4H
service configured in `service.search.mapping`, and update `application.properties` (`workflow.host`,
`egov.user.host`, `egov.searcher.host`, `service.search.mapping`, `egov.es.username`/`egov.es.password`, etc.) to
point at your local/dev instances. Service listens on port `9011` under context path `/inbox`.

### Swagger API Contract

Link to the swagger API contract [yaml](https://raw.githubusercontent.com/egovernments/municipal-services/master/docs/inbox.yml)

### Postman Collection

Link to the postman collection [here](https://www.getpostman.com/collections/5e9f36ddf4b34460287e)
