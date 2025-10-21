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
            long escalationTime = System.currentTimeMillis();
            String uniqueId = generateUniqueId(escalationId, tenantId, recipientRole, escalationTime);
            
            EscalationStatus status = EscalationStatus.builder()
                .id(uniqueId)
                .escalationType(escalationType)
                .escalationId(escalationId)
                .tenantId(tenantId)
                .recipientRole(recipientRole)
                .escalationTime(escalationTime)
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
            long escalationTime = System.currentTimeMillis();
            String uniqueId = generateUniqueId(escalationId, tenantId, recipientRole, escalationTime);
            
            EscalationStatus status = EscalationStatus.builder()
                .id(uniqueId)
                .escalationType(escalationType)
                .escalationId(escalationId)
                .tenantId(tenantId)
                .recipientRole(recipientRole)
                .escalationTime(escalationTime)
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
            long escalationTime = System.currentTimeMillis();
            String uniqueId = generateUniqueId("GENERAL", "in", "SYSTEM", escalationTime);
            
            EscalationStatus status = EscalationStatus.builder()
                .id(uniqueId)
                .escalationType(escalationType)
                .escalationId(null)
                .tenantId("in")
                .recipientRole(null)
                .escalationTime(escalationTime)
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
    
    /**
     * Generate unique ID for escalation status
     * Format: escalationId_tenantId_recipientRole_escalationTime
     */
    private String generateUniqueId(String escalationId, String tenantId, String recipientRole, long escalationTime) {
        return String.format("%s_%s_%s_%d", 
            escalationId != null ? escalationId : "null",
            tenantId != null ? tenantId : "null",
            recipientRole != null ? recipientRole : "null",
            escalationTime);
    }
}
