package org.selco.e4h.kafka.consumer;

import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
public class KafkaProducerService {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public KafkaProducerService(KafkaTemplate<String, Object> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void sendIncident(String topic, Object incident) {
        kafkaTemplate.send(topic, incident);
        System.out.println("Sent incident: " + incident);
    }
}

