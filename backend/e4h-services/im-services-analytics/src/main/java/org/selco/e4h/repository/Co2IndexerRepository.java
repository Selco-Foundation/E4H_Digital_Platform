package org.selco.e4h.repository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.selco.e4h.config.CarbonEmissionProperties;
import org.selco.e4h.web.models.Co2MonthlyDocument;
import org.selco.e4h.web.models.Co2MonthlyIndexPayload;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Publishes CO2 documents to egov-indexer Kafka topics.
 * Projection months are upserted by document id (tenantId_facilityId_year_month) — no separate delete topic.
 */
@Slf4j
@Repository
@RequiredArgsConstructor
public class Co2IndexerRepository {

    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final CarbonEmissionProperties properties;

    public void publishActual(Co2MonthlyDocument doc) {
        Co2MonthlyIndexPayload payload = Co2MonthlyIndexPayload.fromActual(doc);
        send(properties.getCo2MonthlyFacilityIndexerTopic(), payload.getUuid(), payload);
    }

    public void publishProjection(Co2MonthlyDocument doc) {
        Co2MonthlyIndexPayload payload = Co2MonthlyIndexPayload.fromProjection(doc);
        send(properties.getCo2MonthlyProjectionIndexerTopic(), payload.getUuid(), payload);
    }

    public void publishProjections(List<Co2MonthlyDocument> docs) {
        if (docs == null || docs.isEmpty()) {
            return;
        }
        for (Co2MonthlyDocument doc : docs) {
            publishProjection(doc);
        }
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
