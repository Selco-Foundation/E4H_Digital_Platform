package org.selco.e4h.repository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.selco.e4h.config.CarbonEmissionProperties;
import org.selco.e4h.service.Co2EsDocumentFactory;
import org.selco.e4h.web.models.BulkIndexRequest;
import org.selco.e4h.web.models.Co2MonthlyDocument;
import org.selco.e4h.web.models.Co2MonthlyIndexPayload;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.function.Function;

/**
 * Publishes CO2 monthly documents for indexing.
 *
 * <p>Default path builds the final ES documents here and sends one batch per facility to the
 * bulk-indexer service, which writes them with a single {@code _bulk} call. A 20-year lifecycle is
 * ~240 facility-months, so this is ~240x fewer Kafka messages and ES round-trips than the legacy
 * path.
 *
 * <p>Setting {@code co2.bulk.index.enabled=false} restores the legacy per-month publishes to
 * egov-indexer's topics, which reshape the payload via egov-indexer's own mapping. Both paths
 * derive the document {@code _id} from {@link Co2MonthlyIndexPayload#documentId} and write to the
 * same indexes, so switching between them overwrites rather than duplicates.
 */
@Slf4j
@Repository
@RequiredArgsConstructor
public class Co2IndexerRepository {

    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final CarbonEmissionProperties properties;
    private final Co2EsDocumentFactory documentFactory;

    /** Publishes every actual (measured or back-estimated) month of one facility. */
    public void publishActuals(List<Co2MonthlyDocument> docs) {
        publishBatch(docs,
                Co2MonthlyIndexPayload::fromActual,
                properties.getActualIndex(),
                properties.getCo2MonthlyFacilityIndexerTopic());
    }

    /** Publishes every projected (future) month of one facility. */
    public void publishProjections(List<Co2MonthlyDocument> docs) {
        publishBatch(docs,
                Co2MonthlyIndexPayload::fromProjection,
                properties.getProjectionIndex(),
                properties.getCo2MonthlyProjectionIndexerTopic());
    }

    private void publishBatch(List<Co2MonthlyDocument> docs,
                              Function<Co2MonthlyDocument, Co2MonthlyIndexPayload> toPayload,
                              String index,
                              String legacyTopic) {
        if (docs == null || docs.isEmpty()) {
            return;
        }

        List<Co2MonthlyIndexPayload> payloads = docs.stream().map(toPayload).toList();

        if (!properties.isBulkIndexEnabled()) {
            for (Co2MonthlyIndexPayload payload : payloads) {
                send(legacyTopic, payload.getUuid(), payload);
            }
            return;
        }

        List<BulkIndexRequest.BulkIndexDocument> documents = payloads.stream()
                .map(payload -> BulkIndexRequest.BulkIndexDocument.builder()
                        .id(payload.getUuid())
                        .source(documentFactory.toEsDocument(payload))
                        .build())
                .toList();

        Co2MonthlyDocument first = docs.get(0);
        String batchId = first.getTenantId() + "_" + first.getFacilityId();
        BulkIndexRequest request = BulkIndexRequest.builder()
                .index(index)
                .batchId(batchId)
                .documents(documents)
                .build();

        // Key on the facility so every batch for one facility lands on the same partition and is
        // therefore applied in order by a single consumer.
        send(properties.getBulkIndexTopic(), first.getFacilityId(), request);
        log.debug("Published bulk batch index={} batchId={} documents={}",
                index, batchId, documents.size());
    }

    private void send(String topic, String key, Object payload) {
        try {
            kafkaTemplate.send(topic, key, payload);
            log.debug("Published to topic={} key={}", topic, key);
        } catch (Exception e) {
            log.error("Failed to publish CO2 index message topic={} key={}", topic, key, e);
        }
    }
}
