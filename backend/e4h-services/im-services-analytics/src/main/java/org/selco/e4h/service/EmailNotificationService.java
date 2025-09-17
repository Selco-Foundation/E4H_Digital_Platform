package org.selco.e4h.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.selco.e4h.config.ConsumerConfiguration;
import org.selco.e4h.util.StorageUtil;
import org.selco.e4h.web.models.Attachment;
import org.selco.e4h.web.models.EmailNotification;
import org.selco.e4h.web.models.EscalationTicket;
import org.selco.e4h.web.models.User;
import org.springframework.core.io.Resource;
import org.springframework.http.*;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Service to send email notifications via Kafka to egov-notification-mail service
 * LLD Compliant: Kafka messaging to egov.core.notification.email topic
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class EmailNotificationService {
    
    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final EmailTemplateService emailTemplateService;
    private final StorageUtil storageUtil;
    private final ConsumerConfiguration consumerConfiguration;
    
    /**
     * Send escalation email notifications to users
     * LLD Compliant: Kafka messaging to egov.core.notification.email topic
     */
    public void sendEscalationEmails(List<User> users, List<EscalationTicket> tickets, 
                                   String escalationRecipientName, String boundaryLevel, 
                                   String csvFileStoreId, String csvFileName, String tenantId) {
        sendEscalationEmails(users, tickets, escalationRecipientName, boundaryLevel, csvFileStoreId, csvFileName, "daily", tenantId);
    }
    
    /**
     * Send escalation email notifications to users (with escalation type)
     * LLD Compliant: Kafka messaging to egov.core.notification.email topic
     */
    public void sendEscalationEmails(List<User> users, List<EscalationTicket> tickets, 
                                   String escalationRecipientName, String boundaryLevel, 
                                   String csvFileStoreId, String csvFileName, String escalationType, String tenantId) {
        try {
            log.info("Sending {} escalation emails to {} users for {} tickets", escalationType, users.size(), tickets.size());
            
            // Generate email content based on escalation type
            String emailBody;
            String emailSubject;
            
            if ("weekly".equals(escalationType)) {
                emailBody = emailTemplateService.generateWeeklySummaryEmailHTML(tickets, escalationRecipientName, boundaryLevel);
                emailSubject = emailTemplateService.generateWeeklySummaryEmailSubject(escalationRecipientName, tickets.size(), boundaryLevel);
            } else {
                emailBody = emailTemplateService.generateEscalationEmailHTML(tickets, escalationRecipientName, boundaryLevel);
                emailSubject = emailTemplateService.generateEmailSubject(escalationRecipientName, tickets.size(), boundaryLevel);
            }
            
            // Send email to each user via Kafka
            for (User user : users) {
                if (user.getEmailId() != null && !user.getEmailId().trim().isEmpty()) {
                    try {
                        sendEmailViaKafka(user, emailSubject, emailBody, csvFileStoreId, csvFileName, tenantId);
                        log.info("Published escalation email to Kafka for user: {} ({})", user.getName(), user.getEmailId());
                        
                    } catch (Exception e) {
                        log.error("Error publishing email to Kafka for user: {} ({})", user.getName(), user.getEmailId(), e);
                    }
                } else {
                    log.warn("User {} has no email address, skipping notification", user.getName());
                }
            }
            
            log.info("Completed publishing escalation emails to Kafka for {} users", users.size());
            
        } catch (Exception e) {
            log.error("Error publishing escalation emails to Kafka", e);
        }
    }
    
    /**
     * Send weekly summary email notifications to users
     * LLD Compliant: Kafka messaging to egov.core.notification.email topic
     */
    public void sendWeeklySummaryEmails(List<User> users, 
                                      List<EscalationTicket> previouslyEscalatedTickets,
                                      List<EscalationTicket> currentlyInBreachTickets,
                                      String escalationRecipientName, String boundaryLevel, 
                                      String csvFileStoreId, String csvFileName, String tenantId) {
        try {
            log.info("Sending weekly summary emails to {} users for {} previously escalated + {} currently in breach tickets", 
                    users.size(), previouslyEscalatedTickets.size(), currentlyInBreachTickets.size());
            
            // Generate email content for weekly summary
            String emailBody = emailTemplateService.generateWeeklySummaryEmailHTML(
                previouslyEscalatedTickets, currentlyInBreachTickets, escalationRecipientName, boundaryLevel);
            String emailSubject = emailTemplateService.generateWeeklySummaryEmailSubject(
                escalationRecipientName, previouslyEscalatedTickets.size() + currentlyInBreachTickets.size(), boundaryLevel);
            
            // Send email to each user via Kafka
            for (User user : users) {
                if (user.getEmailId() != null && !user.getEmailId().trim().isEmpty()) {
                    try {
                        sendEmailViaKafka(user, emailSubject, emailBody, csvFileStoreId, csvFileName, tenantId);
                        log.info("Published weekly summary email to Kafka for user: {} ({})", user.getName(), user.getEmailId());
                        
                    } catch (Exception e) {
                        log.error("Error publishing weekly summary email to Kafka for user: {} ({})", user.getName(), user.getEmailId(), e);
                    }
                } else {
                    log.warn("User {} has no email address, skipping weekly summary notification", user.getName());
                }
            }
            
            log.info("Completed publishing weekly summary emails to Kafka for {} users", users.size());
            
        } catch (Exception e) {
            log.error("Error publishing weekly summary emails to Kafka", e);
        }
    }
    
    /**
     * Send email via Kafka to egov-notification-mail service
     * LLD Compliant: Kafka messaging to egov.core.notification.email topic
     */
    private void sendEmailViaKafka(User user, String subject, String body, String csvFileStoreId, String csvFileName, String tenantId) {
        try {
            log.info("Publishing email to Kafka for user: {} ({})", user.getName(), user.getEmailId());
            log.info("Email details - To: {}, Subject: {}", user.getEmailId(), subject);
            
            // Prepare email request for Kafka
            Map<String, Object> emailRequest = new HashMap<>();
            emailRequest.put("to", user.getEmailId());
            emailRequest.put("subject", subject);
            emailRequest.put("body", body);
            emailRequest.put("isHTML", true);
            
            // Add CSV attachment if available
            if (csvFileStoreId != null && csvFileName != null) {
                try {
                    // Fetch CSV file from filestore and convert to base64
                    byte[] csvContent = downloadFileFromStorage(tenantId, csvFileStoreId);
                    if (csvContent != null) {
                        String base64Content = Base64.getEncoder().encodeToString(csvContent);
                        
                        Map<String, Object> attachment = new HashMap<>();
                        attachment.put("fileName", csvFileName);
                        attachment.put("contentType", "text/csv");
                        attachment.put("base64Content", base64Content);
                        
                        List<Map<String, Object>> attachments = new ArrayList<>();
                        attachments.add(attachment);
                        emailRequest.put("attachments", attachments);
                        
                        log.debug("Added CSV attachment: {} ({} bytes)", csvFileName, csvContent.length);
                    }
                } catch (Exception e) {
                    log.warn("Failed to fetch CSV file from filestore: {}, sending email without attachment", csvFileStoreId, e);
                }
            }
            
            log.debug("Email request payload for Kafka: {}", emailRequest);
            
            // Publish to Kafka topic
            String topic = consumerConfiguration.getNotificationEmailTopic();
            kafkaTemplate.send(topic, emailRequest);
            
            log.info("Successfully published email to Kafka topic: {} for user: {}", topic, user.getEmailId());
            
        } catch (Exception e) {
            log.error("Error publishing email to Kafka for user: {}", user.getEmailId(), e);
            throw e;
        }
    }
    
    /**
     * Download file from StorageUtil and convert to byte array
     */
    private byte[] downloadFileFromStorage(String tenantId, String fileStoreId) {
        try {
            // Use tenantId 'in' for escalation files
            Resource resource = storageUtil.getFile(tenantId, fileStoreId);
            
            if (resource != null && resource.exists()) {
                try (InputStream inputStream = resource.getInputStream();
                     ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
                    
                    byte[] buffer = new byte[4096];
                    int bytesRead;
                    while ((bytesRead = inputStream.read(buffer)) != -1) {
                        outputStream.write(buffer, 0, bytesRead);
                    }
                    
                    return outputStream.toByteArray();
                }
            }
        } catch (Exception e) {
            log.error("Error downloading file with ID: {}", fileStoreId, e);
        }
        
        return null;
    }
}
