# E4H User Activity Analytics — Flow Diagrams

Visual flows for the user adoption analytics solution.  
For full design details, see `E4H_User_Activity_Analytics_LLD.md`.

---

## 1. End-to-end flow

How a user action becomes a Kibana chart or weekly report.

```mermaid
flowchart LR
    A[Apps] --> B[Capture events]
    B --> C[e4h-user-analytics-service]
    C --> D[Kafka]
    D --> E[egov-indexer]
    E --> F[Elasticsearch]
    F --> G[Kibana]
    F --> H[Weekly Excel report]
```

| Step | What happens |
|---|---|
| 1 | User logs in, opens a page, or performs an action |
| 2 | App records the event (SDK or server hook) |
| 3 | Analytics service enriches event with role and state |
| 4 | Event is published to Kafka |
| 5 | Indexer writes event to Elasticsearch |
| 6 | Kibana and the report job read from Elasticsearch |

---

## 2. UI event flow

Events captured in the browser or mobile app (login, page view, dashboard viewed, etc.).

```mermaid
flowchart LR
    A[User] --> B[Web / Mobile App]
    B --> C[Analytics SDK]
    C --> D[POST /events/_bulk]
    D --> E[e4h-user-analytics-service]
    E --> F[Enrich user, role, state]
    F --> G[Kafka]
    G --> H[egov-indexer]
    H --> I[Elasticsearch]
```

**Example:** User opens the inbox page in Saura-eMitra → SDK sends `page_view` → service adds `user_uuid` and `CRM` role → event is indexed.

---

## 3. Business event flow

Events tied to a successful backend action (facility added, report approved, etc.).  
These can be sent directly to Kafka from the domain service.

```mermaid
flowchart LR
    A[User] --> B[App]
    B --> C[Domain API]
    C --> D[im-services / project / health-facility-registry]
    D --> E[Kafka]
    E --> F[egov-indexer]
    F --> G[Elasticsearch]
```

**Example:** Admin creates a facility → `health-facility-registry` API succeeds → `facility_added` event published to Kafka → indexed in Elasticsearch.

---

## 4. Where events come from (by application)

```mermaid
flowchart TB
    SEM[Saura-eMitra]
    FA[Field Assist]
    DSS[Kibana / DSS]
    MH[Management Hub]

    SDK[Analytics SDK]
    API[Domain APIs]

    SEM --> SDK
    FA --> SDK
    DSS --> SDK
    MH --> SDK

    SEM --> API
    FA --> API
    MH --> API

    SDK --> INGEST[e4h-user-analytics-service]
    API --> KAFKA[Kafka]

    INGEST --> KAFKA
```

| Application | Via SDK (UI) | Via domain API (business actions) |
|---|---|---|
| Saura-eMitra | Login, logout, page view, ticket viewed | Ticket updated |
| Field Assist | Login, logout, page view, module access, report viewed | Report submitted |
| Kibana / DSS | Login, logout, dashboard viewed, table downloaded | — |
| Management Hub | Login, logout, page view | Facility added, project created, report approved, etc. |

---

## 5. Data storage flow

```mermaid
flowchart LR
    A[Raw events] --> B[e4h-user-activity-raw]
    B --> C[Weekly rollup job]
    C --> D[e4h-user-activity-weekly]
    C --> E[e4h-user-activity-champions]
    D --> F[Kibana]
    E --> F
    D --> G[Excel report]
    E --> G
```

| Index | Purpose |
|---|---|
| `e4h-user-activity-raw-*` | Every event as it happens |
| `e4h-user-activity-weekly` | WAU, sessions, access counts per week |
| `e4h-user-activity-champions` | Top 5 users per role per week |

---

## 6. Weekly report flow

```mermaid
flowchart LR
    A[Scheduler] --> B[Query Elasticsearch]
    B --> C(Build Excel)
    C --> D[Upload to filestore]
    D --> E[Email to stakeholder]
```

Runs once a week (e.g. Monday morning) for the previous week.

---

## 7. Champion user flow

```mermaid
flowchart LR
    A[Weekly events in ES] --> B[Group by role]
    B --> C[Score each user]
    C --> D[Pick top 5 per role]
    D --> E[Save to champions index]
    E --> F[Kibana + Excel report]
```

Scoring uses login count, session count, page access count, and application actions — **within the same role only**.

---

## 8. Single event journey (summary)

```mermaid
sequenceDiagram
    participant U as User
    participant App as App
    participant Svc as e4h-user-analytics-service
    participant K as Kafka
    participant Idx as egov-indexer
    participant ES as Elasticsearch

    U->>App: Action e.g. page view
    App->>Svc: Send event
    Svc->>Svc: Add role and state
    Svc->>K: Publish
    Svc-->>App: 202 Accepted
    K->>Idx: Consume
    Idx->>ES: Index document
```
