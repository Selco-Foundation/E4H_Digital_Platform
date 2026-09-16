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
import java.util.List;

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
        for (BulkIndexRequest.BulkIndexDocument document : documents) {
            if (document == null || document.getSource() == null || document.getSource().isEmpty()) {
                malformed++;
                continue;
            }
            writable.add(new EsBulkRepository.IndexedDocument(document.getId(), document.getSource()));
        }
        if (malformed > 0) {
            log.error("Skipped {} document(s) with no source batchId={} index={}",
                    malformed, LogSanitizer.sanitize(request.getBatchId()), LogSanitizer.sanitize(index));
        }

        int succeeded = 0;
        int esFailures = 0;
        int chunkSize = Math.max(1, properties.getBulkChunkSize());
        for (int i = 0; i < writable.size(); i += chunkSize) {
            List<EsBulkRepository.IndexedDocument> chunk =
                    writable.subList(i, Math.min(i + chunkSize, writable.size()));
            EsBulkRepository.BulkOutcome outcome = esBulkRepository.bulkIndex(index, chunk);
            succeeded += outcome.succeeded();
            esFailures += outcome.failed();
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

        if (malformed == 0 && esFailures == 0) {
            log.info("Indexed batchId={} index={} documents={}",
                    LogSanitizer.sanitize(request.getBatchId()), LogSanitizer.sanitize(index), succeeded);
        } else {
            log.error("Indexed batchId={} index={} received={} indexed={} malformed={} esFailures={}",
                    LogSanitizer.sanitize(request.getBatchId()), LogSanitizer.sanitize(index),
                    documents.size(), succeeded, malformed, esFailures);
        }
    }
}
