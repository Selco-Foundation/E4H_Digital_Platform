package org.egov.rms.producer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.egov.tracer.kafka.CustomKafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class Producer {

    private final CustomKafkaTemplate<String, Object> kafkaTemplate;
    private final ObjectMapper objectMapper;

    public void push(String topic, Object value) {
        log.info("Publishing RMS event to Kafka topic {}", topic);
        log.debug("Kafka publish payload type for topic {}: {}", topic, value != null ? value.getClass().getName() : "null");
        kafkaTemplate.send(topic, serialize(value));
    }

    private String serialize(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof String) {
            return (String) value;
        }
        try {
            return objectMapper.writeValueAsString(value);
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException("Failed to serialize Kafka payload", e);
        }
    }
}
