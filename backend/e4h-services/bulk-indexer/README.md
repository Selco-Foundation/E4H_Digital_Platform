# bulk-indexer

Consumes batches of documents from one Kafka topic and writes each batch to Elasticsearch with a
single `_bulk` call.

The service has **no knowledge of any document shape**. Producers send documents in their final ES
form and name the target index; this service only chunks and writes. Adding or renaming a field is
a change in the producing service alone — nothing here needs to change, and nothing here needs
redeploying.

## Why it exists

`im-services-analytics`' CO2 calculation walks every month of a facility's 20-year solar lifecycle
and used to publish **one Kafka message per facility-month** (~240 per facility, ~305 in a
mid-lifecycle example) to `save-co2-monthly-facility-indexer` /
`save-co2-monthly-projection-facility-indexer`. egov-indexer then turned each into its own
single-document ES write. At ~1000 facilities a single cron trigger produced ~240k messages and
~240k ES round-trips.

This service takes the same documents one facility at a time — **2 messages per facility** — and
writes them in one `_bulk` request each.

## Contract

Topic `bulk-index-documents`:

```json
{
  "index": "co2-monthly-facility-index-write",
  "batchId": "in_FAC123",
  "documents": [
    {
      "id": "in_FAC123_2024_1",
      "source": { "Data": { "state": "Karnataka" }, "month": 1, "year": 2024, "...": "..." }
    }
  ]
}
```

- `index` — target index or alias. Required; a batch without one is dropped.
- `id` — the `_id` to write under. Writes use the `index` action, so a stable id makes re-runs
  **idempotent** (overwrite, not duplicate). Omitting it lets ES generate one, which means repeated
  batches append.
- `source` — written verbatim.

Producers should key the Kafka message on the entity (e.g. facilityId) so all batches for one
entity stay on a single partition and apply in order.

For the CO2 case the document shape lives in
`im-services-analytics`' `Co2EsDocumentFactory`, which reproduces the field layout egov-indexer's
`customJsonMapping` used to apply.

## Failure handling

Nothing is retried and nothing is dead-lettered; failures are logged and counted, and the rest of
the batch still commits.

| Failure | Behaviour |
| --- | --- |
| Unparseable message | Dropped; first 500 chars logged |
| Missing `index` | Batch dropped |
| Document with no `source` | That document skipped, rest of the batch proceeds |
| ES rejects a document inside `_bulk` | `_id` + status + reason logged per document, rest commit |
| Whole `_bulk` call fails | Every document in that chunk counted failed, next chunk still attempted |

Counters: `bulkindexer.documents.indexed{index}`,
`bulkindexer.documents.failed{index,reason=malformed|elasticsearch}`,
`bulkindexer.messages.processed{index}`, `bulkindexer.messages.dropped{reason}`.

Since a dropped batch is not retried, **alert on `bulkindexer_messages_dropped_total` and
`bulkindexer_documents_failed_total`** — otherwise a malformed batch silently loses a whole
facility's history.

Only `GET /health` (k8s probe groups, no component detail) and `GET /prometheus` are exposed —
deliberately not `metrics`, not `info`, never `*`. Both sit under the `/bulk-indexer` context path.

## Operational notes

- **Message size.** ~240 documents ≈ 150KB per message. The producer sets
  `max.request.size=10MB` and this service sets `max.partition.fetch.bytes=10MB`, but the
  **broker's `max.message.bytes` must be raised to match** — the 1MB default leaves only ~7x
  headroom and a longer lifecycle or wider document would start silently failing sends.
- **TLS.** Certificate verification is left at the JVM default. For an ES8 cluster with a
  self-signed certificate, mount the CA and set `javax.net.ssl.trustStore` via `JAVA_TOOL_OPTIONS`
  rather than disabling verification.
- **Secrets.** `ES_PASSWORD` must come from the ES secret, not the properties file.

## Cutover

1. Deploy this service with `CO2_BULK_INDEX_ENABLED=false` still set on `im-services-analytics`.
2. Flip `CO2_BULK_INDEX_ENABLED=true` and re-trigger
   `POST /im-services-analytics/v1/carbon/trigger?facilityIds=…` for one facility. Both paths write
   the same `_id`s to the same indexes, so this overwrites the existing documents and the
   dashboards should not move.
3. Compare document counts and a sampled document against the pre-cutover values.
4. Once stable, delete the `save-co2-monthly-facility-indexer` and
   `save-co2-monthly-projection-facility-indexer` blocks from egov-indexer's `im-services.yml`.

Rollback at any point is `CO2_BULK_INDEX_ENABLED=false` plus a restart of the analytics service.
