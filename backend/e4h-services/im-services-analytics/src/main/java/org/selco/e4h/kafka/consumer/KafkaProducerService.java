package org.selco.e4h.kafka.consumer;

import lombok.extern.slf4j.Slf4j;
import org.selco.e4h.util.ElasticSearchClient;
import org.selco.e4h.web.models.IncidentStatusAgregation;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
@Slf4j
public class KafkaProducerService {

    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final ElasticSearchClient esClient;

    public KafkaProducerService(KafkaTemplate<String, Object> kafkaTemplate, ElasticSearchClient esClient) {
        this.kafkaTemplate = kafkaTemplate;
        this.esClient = esClient;
    }

    public void sendIncident(String topic, Object incident) {
        log.trace("Sending incident to Kafka topic: {}", topic);
        try {
            kafkaTemplate.send(topic, incident);
            log.debug("Incident sent to Kafka topic: {}, incident type: {}", topic, incident != null ? incident.getClass().getSimpleName() : "null");
            log.info("Successfully sent incident to Kafka topic: {}", topic);
        } catch (Exception e) {
            log.error("Failed to send incident to Kafka topic: {}", topic, e);
            throw e;
        }
    }
}

