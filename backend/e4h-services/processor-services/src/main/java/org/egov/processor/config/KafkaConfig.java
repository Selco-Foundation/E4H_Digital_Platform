package org.egov.processor.config;

import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.egov.tracer.kafka.deserializer.HashMapDeserializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.listener.ContainerProperties;

import java.util.HashMap;
import java.util.Map;

import static org.apache.kafka.clients.consumer.ConsumerConfig.*;

@Configuration
@Slf4j
public class KafkaConfig {

    @Value("${kafka.config.bootstrap_server_config}")
    private String bootstrapServers;

    @Value("${spring.kafka.consumer.group-id}")
    private String groupId;
    
    // Force system properties as a fallback
    static {
        System.setProperty("spring.kafka.consumer.max-poll-interval-ms", "1800000");
        System.setProperty("spring.kafka.consumer.max-poll-records", "1");
        System.setProperty("spring.kafka.consumer.heartbeat-interval", "5000");
        System.setProperty("spring.kafka.consumer.session-timeout", "60000");
        System.out.println("FORCED system properties for Kafka consumer configuration");
    }

    @Bean
    public ConsumerFactory<String, Object> consumerFactory() {
        log.info("Creating ConsumerFactory with FORCED configuration overrides");
        
        Map<String, Object> configProps = new HashMap<>();
        
        // Basic configuration
        configProps.put(BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        configProps.put(GROUP_ID_CONFIG, groupId);
        configProps.put(KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        configProps.put(VALUE_DESERIALIZER_CLASS_CONFIG, HashMapDeserializer.class);
        configProps.put("security.protocol", "SSL");
        
        // CRITICAL: Force the poll timeout settings - TRIPLE OVERRIDE
        configProps.put(MAX_POLL_INTERVAL_MS_CONFIG, 1800000); // 30 minutes
        configProps.put(MAX_POLL_RECORDS_CONFIG, 1);           // One record at a time
        configProps.put("max.poll.interval.ms", 1800000);      // Alternative property name
        configProps.put("max.poll.records", 1);                // Alternative property name
        
        // Additional resilience settings optimized for video processing
        configProps.put(HEARTBEAT_INTERVAL_MS_CONFIG, 5000);     // 5 seconds
        configProps.put(SESSION_TIMEOUT_MS_CONFIG, 60000);       // 60 seconds (1 minute)
        configProps.put(AUTO_OFFSET_RESET_CONFIG, "earliest");
        
        // Force processing of old messages for new consumer group
        log.info("Consumer group '{}' will start from earliest offset to process backlogged messages", groupId);
        configProps.put(ENABLE_AUTO_COMMIT_CONFIG, true);
        configProps.put(AUTO_COMMIT_INTERVAL_MS_CONFIG, 100);
        
        log.info("=== PROGRAMMATIC KAFKA CONFIG (FORCED) ===");
        log.info("Bootstrap Servers: {}", bootstrapServers);
        log.info("Group ID: {}", groupId);
        log.info("Heartbeat Interval: {}ms ({}s)", configProps.get(HEARTBEAT_INTERVAL_MS_CONFIG), (Integer)configProps.get(HEARTBEAT_INTERVAL_MS_CONFIG)/1000);
        log.info("Session Timeout: {}ms ({}s)", configProps.get(SESSION_TIMEOUT_MS_CONFIG), (Integer)configProps.get(SESSION_TIMEOUT_MS_CONFIG)/1000);
        log.info("Max Poll Interval: {}ms ({}min) [TRIPLE OVERRIDE]", configProps.get(MAX_POLL_INTERVAL_MS_CONFIG), (Integer)configProps.get(MAX_POLL_INTERVAL_MS_CONFIG)/60000);
        log.info("Max Poll Records: {} [TRIPLE OVERRIDE]", configProps.get(MAX_POLL_RECORDS_CONFIG));
        log.info("Alternative max.poll.interval.ms: {}", configProps.get("max.poll.interval.ms"));
        log.info("Alternative max.poll.records: {}", configProps.get("max.poll.records"));
        log.info("===========================================");
        
        return new DefaultKafkaConsumerFactory<>(configProps);
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, Object> kafkaListenerContainerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, Object> factory = new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(consumerFactory());
        
        // Additional container properties
        factory.getContainerProperties().setAckMode(ContainerProperties.AckMode.RECORD);
        factory.getContainerProperties().setPollTimeout(30000);
        
        log.info("Kafka Listener Container Factory configured with custom settings");
        
        return factory;
    }
} 