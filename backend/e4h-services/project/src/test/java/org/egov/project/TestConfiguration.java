package org.egov.project;

import org.egov.project.service.Co2ProjectLookupService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.KafkaTemplate;

import static org.mockito.Mockito.mock;

@Configuration
public class TestConfiguration {
    @Bean
    @SuppressWarnings("unchecked")
    public KafkaTemplate<String, Object> kafkaTemplate() {
        return mock(KafkaTemplate.class);
    }

    @Bean
    public Co2ProjectLookupService co2ProjectLookupService() {
        return mock(Co2ProjectLookupService.class);
    }
}