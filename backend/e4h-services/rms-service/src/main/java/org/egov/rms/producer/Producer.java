package org.egov.rms.producer;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.egov.tracer.kafka.CustomKafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class Producer {

    private final CustomKafkaTemplate<String, Object> kafkaTemplate;

    public void push(String topic, Object value) {
        log.info("Publishing RMS event to Kafka topic {}", topic);
        log.debug("Kafka publish payload type for topic {}: {}", topic, value != null ? value.getClass().getName() : "null");
        kafkaTemplate.send(topic, value);
    }
}
