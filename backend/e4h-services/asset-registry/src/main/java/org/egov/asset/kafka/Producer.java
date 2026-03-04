package org.egov.asset.kafka;

import lombok.extern.slf4j.Slf4j;
import org.egov.tracer.kafka.CustomKafkaTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

// NOTE: If tracer is disabled change CustomKafkaTemplate to KafkaTemplate in autowiring

@Service
@Slf4j
public class Producer {

    private final CustomKafkaTemplate<String, Object> kafkaTemplate;

    @Autowired
    public Producer(CustomKafkaTemplate<String, Object> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void push(String topic, Object value) {
        log.trace("Producer::push called");
        log.debug("Pushing message to Kafka topic | topic={}", topic);
        try {
            kafkaTemplate.send(topic, value);
            log.debug("Successfully pushed message to Kafka topic | topic={}", topic);
        } catch (Exception e) {
            log.error("Failed to push message to Kafka | topic={} error={}", topic, e.getMessage(), e);
            throw e;
        }
    }
}
