package org.selco.e4h.bulkindexer.kafka.consumer;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.selco.e4h.bulkindexer.service.BulkIndexService;
import org.selco.e4h.bulkindexer.util.LogSanitizer;
import org.selco.e4h.bulkindexer.web.models.BulkIndexRequest;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class BulkIndexListener {

    /** Batches are large; log a prefix rather than the whole payload when one cannot be parsed. */
    private static final int PAYLOAD_LOG_LIMIT = 500;

    private final BulkIndexService bulkIndexService;
    private final ObjectMapper objectMapper;

    @KafkaListener(
            topics = "${kafka.topics.bulk-index-documents}",
            groupId = "${spring.kafka.consumer.group-id}")
    public void onMessage(String payload) {
        BulkIndexRequest request;
        try {
            request = objectMapper.readValue(payload, BulkIndexRequest.class);
        } catch (Exception e) {
            log.error("Dropping unparseable bulk-index message (first {} chars): {}",
                    PAYLOAD_LOG_LIMIT, LogSanitizer.sanitize(payload, PAYLOAD_LOG_LIMIT), e);
            return;
        }
        try {
            bulkIndexService.process(request);
        } catch (Exception e) {
            // process() is already defensive; this only catches programming errors, and swallowing
            // keeps one bad batch from stalling the partition.
            log.error("Unexpected failure handling batchId={} index={}",
                    LogSanitizer.sanitize(request.getBatchId()), LogSanitizer.sanitize(request.getIndex()), e);
        }
    }

}
