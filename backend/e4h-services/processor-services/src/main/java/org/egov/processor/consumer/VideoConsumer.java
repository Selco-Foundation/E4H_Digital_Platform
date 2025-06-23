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

        // Track consumer thread activity
        Thread currentThread = Thread.currentThread();
        long threadId = currentThread.getId();
        String threadName = currentThread.getName();
        
        try {
            // Update monitoring metrics
            long msgCount = messageCount.incrementAndGet();
            lastMessageTime = LocalDateTime.now();
            
            log.info("🎬 [THREAD-{}:{}] Received video processing request #{} at {} from topic: {}", 
                    threadId, threadName, msgCount, lastMessageTime, topic);
            log.debug("Message content: {}", record);
            
            StorageProcessingContext storageProcessingContext = mapper.convertValue(record, StorageProcessingContext.class);
            
            // Detailed timing and thread monitoring
            long startTime = System.currentTimeMillis();
            log.info("⏰ [THREAD-{}] Starting video processing at {}", threadId, java.time.Instant.ofEpochMilli(startTime));
            
            // Log every 5 minutes during processing to detect blocking
            Thread monitoringThread = new Thread(() -> {
                try {
                    int minuteCounter = 0;
                    while (!Thread.currentThread().isInterrupted()) {
                        Thread.sleep(300000); // 5 minutes
                        minuteCounter += 5;
                        long currentTime = System.currentTimeMillis();
                        long elapsedMinutes = (currentTime - startTime) / 60000;
                        log.warn("⏳ [THREAD-{}] Video processing still in progress after {}min (monitoring check #{})", 
                                threadId, elapsedMinutes, minuteCounter / 5);
                        
                        // Critical warning if approaching timeout
                        if (elapsedMinutes > 25) { // 5 minutes before 30min timeout
                            log.error("🚨 [THREAD-{}] CRITICAL: Video processing approaching 30min timeout! Current: {}min", 
                                    threadId, elapsedMinutes);
                        }
                    }
                } catch (InterruptedException e) {
                    log.info("📍 [THREAD-{}] Monitoring thread interrupted - processing completed", threadId);
                }
            });
            monitoringThread.setDaemon(true);
            monitoringThread.start();
            
            try {
                storageService.processAndStoreFiles(storageProcessingContext);
            } finally {
                monitoringThread.interrupt(); // Stop monitoring when processing completes
            }
            
            long processingTime = System.currentTimeMillis() - startTime;
            long processingMinutes = processingTime / 60000;
            
            log.info("✅ [THREAD-{}] Successfully processed video processing request #{} in {}ms ({}min {}sec)", 
                    threadId, msgCount, processingTime, processingMinutes, (processingTime % 60000) / 1000);
            
            // Progressive warnings based on processing time
            if (processingTime > 1800000) { // 30 minutes
                log.error("🚨 [THREAD-{}] EXCEEDED TIMEOUT: Video processing took {}min - THIS WILL CAUSE POLL TIMEOUT!", 
                        threadId, processingMinutes);
            } else if (processingTime > 1500000) { // 25 minutes  
                log.warn("⚠️ [THREAD-{}] DANGER ZONE: Video processing took {}min - close to 30min timeout limit", 
                        threadId, processingMinutes);
            } else if (processingTime > 600000) { // 10 minutes
                log.warn("⏰ [THREAD-{}] Long processing: Video took {}min - monitor for optimization opportunities", 
                        threadId, processingMinutes);
            }
            
        }
        catch (Exception e){
            long elapsedTime = System.currentTimeMillis() - java.time.ZoneOffset.UTC.getRules()
                .getOffset(lastMessageTime).getTotalSeconds() * 1000;
            log.error("❌ [THREAD-{}] Error occurred while processing video after {}ms from topic: {}", 
                    threadId, elapsedTime, topic, e);
            // Re-throw to ensure Kafka error handling mechanisms kick in
            throw new RuntimeException("Failed to process message", e);
        }

    }
    
    // Getter methods for monitoring
    public long getMessageCount() {
        return messageCount.get();
    }
    
    public LocalDateTime getLastMessageTime() {
        return lastMessageTime;
    }
}
