package org.egov.processor.consumer;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.egov.processor.models.storage.StorageProcessingContext;
import org.egov.processor.service.StorageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

import java.util.HashMap;

@Component
@Slf4j
public class VideoConsumer {

    @Autowired
    private ObjectMapper mapper;

    @Autowired
    private StorageService storageService;

    @KafkaListener(topics = { "${im.kafka.process.video.topic}"})
    public void listen(final HashMap<String, Object> record, @Header(KafkaHeaders.RECEIVED_TOPIC) String topic) {

        try {
            log.info("Received video processing request " + record);
            StorageProcessingContext storageProcessingContext = mapper.convertValue(record, StorageProcessingContext.class);
            storageService.processAndStoreFiles(storageProcessingContext);
        }
        catch (Exception e){
            log.error("Error occured while processing the record from topic : " + topic, e);
        }

    }
}
