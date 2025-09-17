package org.selco.e4h.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.selco.e4h.config.ConsumerConfiguration;
import org.selco.e4h.kafka.consumer.KafkaProducerService;
import org.selco.e4h.web.models.EscalationStatus;
import org.springframework.stereotype.Service;

/**
 * Service to publish escalation status messages
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class EscalationStatusService {
    
    private final KafkaProducerService kafkaProducerService;
    private final ConsumerConfiguration consumerConfiguration;
    
    /**
     * Publish success status
     */
    public void publishSuccessStatus(String escalationType, String escalationId, String tenantId, String recipientRole) {
        try {
            EscalationStatus status = EscalationStatus.builder()
                .escalationType(escalationType)
                .escalationId(escalationId)
                .tenantId(tenantId)
                .recipientRole(recipientRole)
                .escalationTime(System.currentTimeMillis())
                .status("SUCCESS")
                .message(null)
                .build();
            
            kafkaProducerService.sendIncident(consumerConfiguration.getEscalationStatusTopic(), status);
            log.info("Published SUCCESS status for escalation: {} in tenant: {} for role: {}", 
                escalationId, tenantId, recipientRole);
            
        } catch (Exception e) {
            log.error("Error publishing SUCCESS status for escalation: {} in tenant: {} for role: {}", 
                escalationId, tenantId, recipientRole, e);
        }
    }
    
    /**
     * Publish failure status
     */
    public void publishFailureStatus(String escalationType, String escalationId, String tenantId, String recipientRole, String errorMessage) {
        try {
            EscalationStatus status = EscalationStatus.builder()
                .escalationType(escalationType)
                .escalationId(escalationId)
                .tenantId(tenantId)
                .recipientRole(recipientRole)
                .escalationTime(System.currentTimeMillis())
                .status("FAILED")
                .message(errorMessage)
                .build();
            
            kafkaProducerService.sendIncident(consumerConfiguration.getEscalationStatusTopic(), status);
            log.info("Published FAILED status for escalation: {} in tenant: {} for role: {} with message: {}", 
                escalationId, tenantId, recipientRole, errorMessage);
            
        } catch (Exception e) {
            log.error("Error publishing FAILED status for escalation: {} in tenant: {} for role: {}", 
                escalationId, tenantId, recipientRole, e);
        }
    }
    
    /**
     * Publish general failure status (for overall process failures)
     */
    public void publishGeneralFailureStatus(String escalationType, String errorMessage) {
        try {
            EscalationStatus status = EscalationStatus.builder()
                .escalationType(escalationType)
                .escalationId(null)
                .tenantId("in")
                .recipientRole(null)
                .escalationTime(System.currentTimeMillis())
                .status("FAILED")
                .message(errorMessage)
                .build();
            
            kafkaProducerService.sendIncident(consumerConfiguration.getEscalationStatusTopic(), status);
            log.info("Published general FAILED status for escalation type: {} with message: {}", 
                escalationType, errorMessage);
            
        } catch (Exception e) {
            log.error("Error publishing general FAILED status for escalation type: {}", escalationType, e);
        }
    }
}
