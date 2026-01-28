package org.egov.im.producer;

import lombok.extern.slf4j.Slf4j;
import org.egov.common.utils.MultiStateInstanceUtil;
import org.egov.im.config.IMConfiguration;
import org.egov.tracer.kafka.CustomKafkaTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class Producer {

    @Autowired
    private CustomKafkaTemplate<String, Object> kafkaTemplate;

    @Autowired
    private MultiStateInstanceUtil centralInstanceUtil;

    public void push(String tenantId, String topic, Object value) {
        log.trace("Producer::push method invoked");
        String updatedTopic = centralInstanceUtil.getStateSpecificTopicName(tenantId, topic);
        log.debug("Pushing message to Kafka topic: {} for tenantId: {}", updatedTopic, tenantId);
        kafkaTemplate.send(updatedTopic, value);
    }
}
