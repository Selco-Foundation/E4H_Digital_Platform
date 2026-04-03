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

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.concurrent.atomic.AtomicLong;

@Component
@Slf4j
public class VideoConsumer {

    @Autowired
    private ObjectMapper mapper;

    @Autowired
    private StorageService storageService;

    // Message tracking for health monitoring
    private final AtomicLong messageCount = new AtomicLong(0);
    private volatile LocalDateTime lastMessageTime = LocalDateTime.now();

    @KafkaListener(topics = { "${im.kafka.process.video.topic}"})
    public void listen(final HashMap<String, Object> record, @Header(KafkaHeaders.RECEIVED_TOPIC) String topic) {
        log.trace("Method invoked: listen, topic: {}", topic);
        
        try {
            log.info("Received video processing request from topic: {}", topic);
            log.debug("Processing Kafka message, record size: {}", record != null ? record.size() : 0);
            
            // Update tracking variables
            messageCount.incrementAndGet();
            lastMessageTime = LocalDateTime.now();
            
            log.trace("Converting record to StorageProcessingContext");
            StorageProcessingContext storageProcessingContext = mapper.convertValue(record, StorageProcessingContext.class);
            
            log.info("Starting file processing and storage");
            storageService.processAndStoreFiles(storageProcessingContext);
            
            log.info("Successfully processed video processing request from topic: {}. Total messages processed: {}", topic, messageCount.get());
        }
        catch (Exception e){
            log.error("Error occurred while processing the record from topic: {}", topic, e);
            // Don't rethrow - let Kafka handle retries based on configuration
        }
    }

    // Health monitoring methods
    public long getMessageCount() {
        log.trace("Method invoked: getMessageCount");
        return messageCount.get();
    }

    public LocalDateTime getLastMessageTime() {
        log.trace("Method invoked: getLastMessageTime");
        return lastMessageTime;
    }
}
