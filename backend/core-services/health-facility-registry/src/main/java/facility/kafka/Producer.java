package facility.kafka;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.egov.tracer.kafka.CustomKafkaTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

// NOTE: If tracer is disabled change CustomKafkaTemplate to KafkaTemplate in autowiring

@Service
@Slf4j
@RequiredArgsConstructor
public class Producer {

    private final CustomKafkaTemplate<String, Object> kafkaTemplate;

    public void push(String topic, Object value) {
        log.trace("Entering push method");
        log.info("Pushing message to Kafka topic: {}", topic);
        log.debug("Message type: {}", value != null ? value.getClass().getSimpleName() : "null");
        try {
            kafkaTemplate.send(topic, value);
            log.debug("Successfully sent message to topic: {}", topic);
        } catch (Exception e) {
            log.error("Error pushing message to Kafka topic {}: {}", topic, e.getMessage(), e);
            throw e;
        }
        log.trace("Exiting push method");
    }
}
