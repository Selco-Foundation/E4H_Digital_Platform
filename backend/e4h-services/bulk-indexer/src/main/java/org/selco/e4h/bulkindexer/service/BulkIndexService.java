package org.selco.e4h.bulkindexer.service;

import io.micrometer.core.instrument.MeterRegistry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.selco.e4h.bulkindexer.config.BulkIndexerProperties;
import org.selco.e4h.bulkindexer.repository.EsBulkRepository;
import org.selco.e4h.bulkindexer.util.LogSanitizer;
import org.selco.e4h.bulkindexer.web.models.BulkIndexRequest;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/** Chunks a batch and writes it to ES. Failures are logged and counted, never rethrown. */
@Slf4j
@Service
@RequiredArgsConstructor
public class BulkIndexService {

    private final EsBulkRepository esBulkRepository;
    private final BulkIndexerProperties properties;
    private final MeterRegistry meterRegistry;

    public void process(BulkIndexRequest request) {
        String index = request.getIndex();
        if (index == null || index.isBlank()) {
            log.error("Dropping message batchId={} — no target index",
                    LogSanitizer.sanitize(request.getBatchId()));
            meterRegistry.counter("bulkindexer.messages.dropped", "reason", "missing-index").increment();
            return;
        }

        List<BulkIndexRequest.BulkIndexDocument> documents = request.getDocuments();
        if (documents == null || documents.isEmpty()) {
            log.warn("Message batchId={} index={} carried no documents",
                    LogSanitizer.sanitize(request.getBatchId()), LogSanitizer.sanitize(index));
            return;
        }

        List<EsBulkRepository.IndexedDocument> writable = new ArrayList<>(documents.size());
        int malformed = 0;
        int missingId = 0;
        // Documents sharing an _id inside one message overwrite each other, so the batch can report
        // full success while contributing fewer documents than it carried. Catch it here rather than
        // inferring it from a document count that came up short days later.
        Set<String> idsInBatch = new LinkedHashSet<>();
        int duplicateIdsInBatch = 0;
        for (BulkIndexRequest.BulkIndexDocument document : documents) {
            if (document == null || document.getSource() == null || document.getSource().isEmpty()) {
                malformed++;
                continue;
            }
            String id = document.getId();
            if (id == null || id.isBlank()) {
                missingId++;
            } else if (!idsInBatch.add(id)) {
                duplicateIdsInBatch++;
                log.warn("Duplicate _id within one batch batchId={} index={} _id={} — "
                                + "the later document overwrites the earlier one",
                        LogSanitizer.sanitize(request.getBatchId()), LogSanitizer.sanitize(index),
                        LogSanitizer.sanitize(id));
            }
            writable.add(new EsBulkRepository.IndexedDocument(id, document.getSource()));
        }
        if (malformed > 0) {
            log.error("Skipped {} document(s) with no source batchId={} index={}",
                    malformed, LogSanitizer.sanitize(request.getBatchId()), LogSanitizer.sanitize(index));
        }
        if (missingId > 0) {
            // Without an _id ES generates one, so a re-run appends instead of overwriting.
            log.warn("{} document(s) carry no _id batchId={} index={} — ES will generate ids and "
                            + "re-runs of this batch will duplicate rather than overwrite",
                    missingId, LogSanitizer.sanitize(request.getBatchId()), LogSanitizer.sanitize(index));
        }

        int succeeded = 0;
        int esFailures = 0;
        int created = 0;
        int updated = 0;
        int noop = 0;
        Set<String> resolvedIndexes = new LinkedHashSet<>();
        int chunkSize = Math.max(1, properties.getBulkChunkSize());
        for (int i = 0; i < writable.size(); i += chunkSize) {
            List<EsBulkRepository.IndexedDocument> chunk =
                    writable.subList(i, Math.min(i + chunkSize, writable.size()));
            EsBulkRepository.BulkOutcome outcome = esBulkRepository.bulkIndex(index, chunk);
            succeeded += outcome.succeeded();
            esFailures += outcome.failed();
            created += outcome.created();
            updated += outcome.updated();
            noop += outcome.noop();
            resolvedIndexes.addAll(outcome.resolvedIndexes());
        }

        meterRegistry.counter("bulkindexer.documents.indexed", "index", index).increment(succeeded);
        if (malformed > 0) {
            meterRegistry.counter("bulkindexer.documents.failed",
                    "index", index, "reason", "malformed").increment(malformed);
        }
        if (esFailures > 0) {
            meterRegistry.counter("bulkindexer.documents.failed",
                    "index", index, "reason", "elasticsearch").increment(esFailures);
        }
        meterRegistry.counter("bulkindexer.messages.processed", "index", index).increment();
        // created vs updated is the difference between "this batch added documents to the index" and
        // "this batch overwrote documents that were already there". Only the first grows the count.
        meterRegistry.counter("bulkindexer.documents.created", "index", index).increment(created);
        meterRegistry.counter("bulkindexer.documents.updated", "index", index).increment(updated);
        if (noop > 0) {
            meterRegistry.counter("bulkindexer.documents.noop", "index", index).increment(noop);
        }
        if (duplicateIdsInBatch > 0) {
            meterRegistry.counter("bulkindexer.documents.duplicate_id", "index", index)
                    .increment(duplicateIdsInBatch);
        }

        String resolved = resolvedIndexes.isEmpty() ? "unknown" : resolvedIndexes.toString();
        if (malformed == 0 && esFailures == 0) {
            log.info("Indexed batchId={} index={} resolvedIndex={} received={} accepted={} "
                            + "created={} updated={} noop={} duplicateIdsInBatch={}",
                    LogSanitizer.sanitize(request.getBatchId()), LogSanitizer.sanitize(index),
                    LogSanitizer.sanitize(resolved), documents.size(), succeeded,
                    created, updated, noop, duplicateIdsInBatch);
        } else {
            log.error("Indexed batchId={} index={} resolvedIndex={} received={} indexed={} "
                            + "malformed={} esFailures={} created={} updated={} noop={} duplicateIdsInBatch={}",
                    LogSanitizer.sanitize(request.getBatchId()), LogSanitizer.sanitize(index),
                    LogSanitizer.sanitize(resolved), documents.size(), succeeded, malformed, esFailures,
                    created, updated, noop, duplicateIdsInBatch);
        }

        // Every document overwrote an existing one and none was new. Expected on a deliberate re-run
        // of a period already indexed — writes are idempotent by design. Unexpected on a first run,
        // where it means this _id set was already sent once, i.e. the producer visited the same
        // entity twice; the index then cannot reach the document count the message count implies.
        // Correlate with the producer's repeatVisits log before treating it as a fault.
        if (created == 0 && updated > 0) {
            log.warn("Batch batchId={} index={} overwrote {} existing document(s) and created none — "
                            + "benign on a re-run of an already-indexed period, a duplicate publish "
                            + "otherwise",
                    LogSanitizer.sanitize(request.getBatchId()), LogSanitizer.sanitize(index), updated);
        }
    }
}
