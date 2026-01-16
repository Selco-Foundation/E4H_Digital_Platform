package org.egov.web.notification.sms.config;


import lombok.extern.slf4j.Slf4j;
import org.egov.tracer.kafka.CustomKafkaTemplate;
import org.egov.tracer.model.CustomException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class Producer {

    @Autowired
    private CustomKafkaTemplate<String, Object> kafkaTemplate;

    public void push(String topic, Object value) {
        log.trace("push method invoked - sending message to Kafka topic: {}", topic);
        log.info("Pushing message to Kafka topic: {}", topic);
        log.debug("Message type: {}", value != null ? value.getClass().getSimpleName() : "null");
        kafkaTemplate.send(topic, value);
        log.debug("Message sent to Kafka topic: {}", topic);
    }
}
