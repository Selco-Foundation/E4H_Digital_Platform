package org.egov.asset.kafka;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.HashMap;

@Slf4j
@Component
public class Consumer {

    @Autowired
    private AssetService assetService;
    @Autowired
    private ObjectMapper objectMapper;

    /*
     * Uncomment the below line to start consuming record from kafka.topics.consumer
     * Value of the variable kafka.topics.consumer should be overwritten in application.properties
     */
    @KafkaListener(topics = "${kafka.topics.asset.create}")
    public void listen(final HashMap<String, Object> record) {
        try {
            Asset asset = objectMapper.convertValue(record, Asset.class);
            assetService.saveAssetFromConsumer(asset);
        } catch (Exception e) {
            log.error("Error processing asset create event", e);
        }
    }
}
