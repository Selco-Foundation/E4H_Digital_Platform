package org.selco.e4h.web.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.egov.common.contract.request.RequestInfo;
import org.selco.e4h.service.*;
import org.selco.e4h.util.StorageUtil;
import org.selco.e4h.web.models.*;
import org.selco.e4h.web.models.ProcessingContext;
import org.selco.e4h.web.models.storage.StorageResponse;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;
import java.text.SimpleDateFormat;
import org.springframework.kafka.core.KafkaTemplate;
import org.selco.e4h.config.ConsumerConfiguration;

/**
 * Controller for SLA escalation processing
 */
@Slf4j
@RestController
@RequestMapping("/v1/escalation-emails")
@RequiredArgsConstructor
public class EscalationController {
    
    private final EscalationMasterDataService masterDataService;
    private final UserService userService;
    private final SLABreachDetectionService slaBreachService;
    private final CSVGenerationService csvGenerationService;
    private final StorageUtil storageUtil;
    private final ElasticsearchEscalationService elasticsearchEscalationService;
    private final EscalationStatusService escalationStatusService;
    private final DynamicEmailTemplateService dynamicEmailTemplateService;
    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final ConsumerConfiguration consumerConfiguration;
    
    /**
     * Daily escalation endpoint
     */
    @PostMapping("/daily")
    public ResponseEntity<String> sendDailyEscalationEmail(@RequestBody EscalationEmailRequest request) {
        try {
            log.info("Starting daily SLA escalation processing");
            
            // Use RequestInfo directly
            RequestInfo requestInfo = request.getRequestInfo();
            
            // Fetch master data
            List<EscalationRecipient> escalationRecipients = masterDataService.fetchEscalationRecipients(requestInfo);
//            List<String> activeTenantIds = masterDataService.fetchActiveTenantIds(requestInfo);
            List<String> activeTenantIds = new ArrayList<>();
            activeTenantIds.add("pg");
            if (escalationRecipients.isEmpty()) {
                log.warn("No escalation recipients found in MDMS");
                escalationStatusService.publishGeneralFailureStatus("daily", "No escalation recipients found in MDMS");
                return ResponseEntity.ok("No escalation recipients found");
            }

            log.info("Found {} escalation recipients and {} active tenants", escalationRecipients.size(), activeTenantIds.size());
            
            // Process each escalation recipient in priority order
            for (EscalationRecipient escalationRecipient : escalationRecipients) {
                if (escalationRecipient.getActive() == null || !escalationRecipient.getActive()) {
                    log.info("Skipping inactive escalation recipient: {}", escalationRecipient.getId());
                    continue;
                }
                
                processEscalationRecipient(requestInfo, escalationRecipient, activeTenantIds, "daily");
            }
            
            log.info("Completed daily SLA escalation processing");
            return ResponseEntity.ok("Daily SLA escalation processing completed successfully");
            
        } catch (Exception e) {
            log.error("Error during daily SLA escalation processing", e);
            escalationStatusService.publishGeneralFailureStatus("daily", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Daily SLA escalation processing failed: " + e.getMessage());
        }
    }
    
    /**
     * Weekly escalation endpoint
     */
    @PostMapping("/weekly")
    public void sendWeeklyEscalationEmail(@RequestBody EscalationEmailRequest request) {
        try {
            log.info("Starting weekly SLA escalation processing");
            
            // Use RequestInfo directly
            RequestInfo requestInfo = request.getRequestInfo();
            
            // Fetch master data
            List<EscalationRecipient> escalationRecipients = masterDataService.fetchEscalationRecipients(requestInfo);
            List<String> activeTenantIds = masterDataService.fetchActiveTenantIds(requestInfo);
            if (escalationRecipients.isEmpty()) {
                log.warn("No escalation recipients found in MDMS");
                escalationStatusService.publishGeneralFailureStatus("weekly", "No escalation recipients found in MDMS");
                ResponseEntity.ok("No escalation recipients found");
                return;
            }
            
            log.info("Found {} escalation recipients and {} active tenants", escalationRecipients.size(), activeTenantIds.size());
            
            // Process each escalation recipient in priority order
            for (EscalationRecipient escalationRecipient : escalationRecipients) {
                if (escalationRecipient.getActive() == null || !escalationRecipient.getActive()) {
                    log.info("Skipping inactive escalation recipient: {}", escalationRecipient.getId());
                    continue;
                }
                
                processEscalationRecipient(requestInfo, escalationRecipient, activeTenantIds, "weekly");
            }
            
            log.info("Completed weekly SLA escalation processing");
            ResponseEntity.ok("Weekly SLA escalation processing completed successfully");

        } catch (Exception e) {
            log.error("Error during weekly SLA escalation processing", e);
            escalationStatusService.publishGeneralFailureStatus("weekly", e.getMessage());
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Weekly SLA escalation processing failed: " + e.getMessage());
        }
    }
    
    /**
     * Process a single escalation recipient
     * Based on LLD sequence diagram Loop 1
     */
    private void processEscalationRecipient(RequestInfo requestInfo, EscalationRecipient escalationRecipient, List<String> activeTenantIds, String escalationType) {
        try {
            log.info("Processing escalation recipient V2: {} role={} boundary={} items={} ", escalationRecipient.getId(), escalationRecipient.getRecipientRole(), escalationRecipient.getBoundaryLevel(), escalationRecipient.getEscalations() != null ? escalationRecipient.getEscalations().size() : 0);

            RecipientRole recipientRole = RecipientRole.builder()
                    .role(escalationRecipient.getRecipientRole())
                    .boundaryLevel(escalationRecipient.getBoundaryLevel())
                    .workflowStates(null)
                    .build();

            processRecipientRole(requestInfo, escalationRecipient, recipientRole, activeTenantIds, escalationType);
            
        } catch (Exception e) {
            log.error("Error processing escalation recipient: {}", escalationRecipient.getId(), e);
        }
    }
    
    /**
     * Process a single recipient role
     */
    private void processRecipientRole(RequestInfo requestInfo, EscalationRecipient escalationRecipient,
                                    RecipientRole recipientRole, List<String> activeTenantIds, String escalationType) {
        try {
            log.info("Processing recipient role: {} with boundary level: {}", 
                recipientRole.getRole(), recipientRole.getBoundaryLevel());
            
            String escalationId = escalationRecipient.getId().toString();
            String recipientRoleName = recipientRole.getRole();
            
            if ("state".equals(recipientRole.getBoundaryLevel())) {
                // State level processing - Loop 3
                for (String tenantId : activeTenantIds) {
                    try {
                        processStateLevelEscalation(requestInfo, escalationRecipient, recipientRole, tenantId, escalationType);
                    } catch (Exception e) {
                        log.error("Error processing state level escalation for tenant: {}", tenantId, e);
                        escalationStatusService.publishFailureStatus(escalationType, escalationId, tenantId, recipientRoleName, e.getMessage());
                    }
                }
                
            } else if ("country".equals(recipientRole.getBoundaryLevel())) {
                // Country level processing
                try {
                    processCountryLevelEscalation(requestInfo, escalationRecipient, recipientRole, escalationType, "in");
                } catch (Exception e) {
                    log.error("Error processing country level escalation", e);
                    escalationStatusService.publishFailureStatus(escalationType, escalationId, "in", recipientRoleName, e.getMessage());
                }
            }
            
        } catch (Exception e) {
            log.error("Error processing recipient role: {}", recipientRole.getRole(), e);
        }
    }
    
    /**
     * Process state level escalation with separate queries per escalation item
     */
    private void processStateLevelEscalation(RequestInfo requestInfo, EscalationRecipient escalationRecipient,
                                           RecipientRole recipientRole, String tenantId, String escalationType) {
        String escalationId = escalationRecipient.getId().toString();
        String recipientRoleName = recipientRole.getRole();
        
        // Step 3a: Query users for role
        List<String> roleCodes = List.of(recipientRole.getRole());
        List<User> users = userService.searchUsersByRoleAndTenant(requestInfo, tenantId, roleCodes);
        
        if (users.isEmpty()) {
            log.warn("No users found for role: {} in tenant: {}", recipientRole.getRole(), tenantId);
            escalationStatusService.publishSuccessStatus(escalationType, escalationId, tenantId, recipientRoleName);
            return;
        }
        
        // Process each escalation item (L0 -> L1 -> L2) with separate queries
        List<EscalationRoleEscalationItem> items = escalationRecipient.getEscalations();
        if (items == null || items.isEmpty()) {
            escalationStatusService.publishSuccessStatus(escalationType, escalationId, tenantId, recipientRoleName);
            return;
        }

        items.sort((a, b) -> levelOrder(a.getEscalationLevel()) - levelOrder(b.getEscalationLevel()));

        // Collect tickets by escalation level for single email
        Map<String, List<EscalationTicket>> ticketsByLevel = new HashMap<>();
        List<String> csvFileStoreIds = new ArrayList<>();
        List<String> csvFileNames = new ArrayList<>();

        // Separate query per escalation item as per LLD requirement
        for (EscalationRoleEscalationItem item : items) {
            log.info("Processing escalation item: {} with workflow states: {}", 
                item.getEscalationLevel(), item.getWorkflowStates());
            
            // One query per escalation item in array (LLD requirement)
            // Pass RequestInfo for MDMS-driven threshold calculation
            List<EscalationTicket> tickets = slaBreachService.findSLABreachTickets(
                    tenantId,
                    item.getWorkflowStates(),
                    escalationId,
                    item.getEscalationLevel(),
                    requestInfo
            );

            if (tickets != null && !tickets.isEmpty()) {
                ticketsByLevel.put(item.getEscalationLevel(), tickets);
                
                // Generate CSV for this level
                String csvContent = csvGenerationService.generateEscalationCsv(tickets);
                String csvFileName = csvGenerationService.generateCsvFileName("daily", item.getEscalationLevel(), tenantId);
                String csvFileStoreId = uploadCsvToFileStore(csvContent, csvFileName, tenantId, requestInfo);
                
                if (csvFileStoreId != null) {
                    csvFileStoreIds.add(csvFileStoreId);
                    csvFileNames.add(csvFileName);
                }

                // Update Elasticsearch for this level
                elasticsearchEscalationService.updateEscalationsForTickets(tickets, escalationId, item.getEscalationLevel());
                
                log.info("Found {} tickets for escalation level: {}", tickets.size(), item.getEscalationLevel());
            }
        }

        // Always send email (even with zero counts) - use new role-based email generation
        sendRoleBasedEscalationEmail(requestInfo, users, ticketsByLevel, recipientRole.getRole(),
            recipientRole.getBoundaryLevel(), csvFileStoreIds, csvFileNames, escalationType, tenantId);

        escalationStatusService.publishSuccessStatus(escalationType, escalationId, tenantId, recipientRoleName);
        log.info("Completed state level escalation (V2) for tenant: {} and role: {} with {} levels", 
            tenantId, recipientRoleName, ticketsByLevel.size());
    }
    
    /**
     * Process country level escalation with separate queries per escalation item
     */
    private void processCountryLevelEscalation(RequestInfo requestInfo, EscalationRecipient escalationRecipient,
                                             RecipientRole recipientRole, String escalationType, String tenantId) {
        String escalationId = escalationRecipient.getId().toString();
        String recipientRoleName = recipientRole.getRole();
        
        // Step 3b: Query users for role in 'in' tenant
        List<String> roleCodes = List.of(recipientRole.getRole());
        List<User> users = userService.searchUsersByRoleInCountry(requestInfo, roleCodes);
        
        if (users.isEmpty()) {
            log.warn("No users found for role: {} in 'in' tenant", recipientRole.getRole());
            escalationStatusService.publishSuccessStatus(escalationType, escalationId, "in", recipientRoleName);
            return;
        }
        
        List<EscalationRoleEscalationItem> items = escalationRecipient.getEscalations();
        if (items == null || items.isEmpty()) {
            escalationStatusService.publishSuccessStatus(escalationType, escalationId, "in", recipientRoleName);
            return;
        }

        items.sort((a, b) -> levelOrder(a.getEscalationLevel()) - levelOrder(b.getEscalationLevel()));

        // Collect tickets by escalation level for single email
        Map<String, List<EscalationTicket>> ticketsByLevel = new HashMap<>();
        List<String> csvFileStoreIds = new ArrayList<>();
        List<String> csvFileNames = new ArrayList<>();

        // Separate query per escalation item as per LLD requirement
        for (EscalationRoleEscalationItem item : items) {
            log.info("Processing country escalation item: {} with workflow states: {}", 
                item.getEscalationLevel(), item.getWorkflowStates());
            
            // One query per escalation item in array (LLD requirement)
            // Pass RequestInfo for MDMS-driven threshold calculation
            List<EscalationTicket> tickets = slaBreachService.findSLABreachTicketsForCountry(
                    item.getWorkflowStates(),
                    escalationId,
                    item.getEscalationLevel(),
                    requestInfo
            );

            if (tickets != null && !tickets.isEmpty()) {
                ticketsByLevel.put(item.getEscalationLevel(), tickets);
                
                // Generate CSV for this level
                String csvContent = csvGenerationService.generateEscalationCsv(tickets);
                String csvFileName = csvGenerationService.generateCsvFileName("daily", item.getEscalationLevel(), "in");
                String csvFileStoreId = uploadCsvToFileStore(csvContent, csvFileName, tenantId, requestInfo);
                
                if (csvFileStoreId != null) {
                    csvFileStoreIds.add(csvFileStoreId);
                    csvFileNames.add(csvFileName);
                }

                // Update Elasticsearch for this level
                elasticsearchEscalationService.updateEscalationsForTickets(tickets, escalationId, item.getEscalationLevel());
                
                log.info("Found {} tickets for country escalation level: {}", tickets.size(), item.getEscalationLevel());
            }
        }

        // Always send email (even with zero counts) - use new role-based email generation
        sendRoleBasedEscalationEmail(requestInfo, users, ticketsByLevel, recipientRole.getRole(),
            recipientRole.getBoundaryLevel(), csvFileStoreIds, csvFileNames, escalationType, "in");

        escalationStatusService.publishSuccessStatus(escalationType, escalationId, "in", recipientRoleName);
        log.info("Completed country level escalation (V2) for role: {} with {} levels", 
            recipientRoleName, ticketsByLevel.size());
    }

    private int levelOrder(String level) {
        if ("LEVEL_ZERO".equals(level)) return 0;
        if ("LEVEL_ONE".equals(level)) return 1;
        if ("LEVEL_TWO".equals(level)) return 2;
        return 99;
    }
    
    /**
     * Extract escalation level from CSV filename
     * Example: "escalation_daily_LEVEL_ONE_in_20251010_045240.csv" -> "LEVEL_ONE"
     */
    private String extractEscalationLevelFromFileName(String fileName) {
        if (fileName == null || fileName.isEmpty()) {
            return null;
        }
        
        // Pattern: escalation_daily_LEVEL_ONE_in_20251010_045240.csv
        String[] parts = fileName.split("_");
        for (int i = 0; i < parts.length; i++) {
            if ("LEVEL".equals(parts[i]) && i + 1 < parts.length) {
                String levelPart = parts[i + 1];
                // Remove .csv extension if present
                if (levelPart.endsWith(".csv")) {
                    levelPart = levelPart.substring(0, levelPart.length() - 4);
                }
                return "LEVEL_" + levelPart;
            }
        }
        
        return null;
    }
    
    /**
     * Send role-based escalation email
     * Handles all 4 roles: STATE_POC, CENTRAL_POC, CENTRAL_ONM_PROJECT_MANAGER, CENTRAL_OPERATIONS_LEAD
     * Always sends email even with zero ticket counts
     */
    private void sendRoleBasedEscalationEmail(RequestInfo requestInfo, List<User> users, 
                                             Map<String, List<EscalationTicket>> ticketsByLevel,
                                             String recipientRole, String boundaryLevel,
                                             List<String> csvFileStoreIds, List<String> csvFileNames,
                                             String escalationType, String tenantId) {
        try {
            log.info("Sending role-based escalation email to {} users for role: {}, levels: {}", 
                users.size(), recipientRole, ticketsByLevel.keySet());
            
            // Calculate total tickets (may be zero)
            int totalTickets = ticketsByLevel.values().stream()
                .mapToInt(List::size).sum();
            
            log.info("Total tickets for role {}: {}", recipientRole, totalTickets);
            
            // Create map of file store IDs by escalation level for download functionality
            Map<String, String> fileStoreIdsByLevel = new HashMap<>();
            for (int i = 0; i < csvFileStoreIds.size() && i < csvFileNames.size(); i++) {
                String fileName = csvFileNames.get(i);
                String fileStoreId = csvFileStoreIds.get(i);
                
                // Extract escalation level from filename (e.g., "escalation_daily_LEVEL_ONE_in_20251010_045240.csv")
                String level = extractEscalationLevelFromFileName(fileName);
                if (level != null) {
                    fileStoreIdsByLevel.put(level, fileStoreId);
                }
            }
            
            // Generate role-based email HTML with download functionality (handles zero counts gracefully)
            String emailBody = dynamicEmailTemplateService.generateRoleBasedEscalationEmailHTML(
                ticketsByLevel, 
                users.get(0).getName(), 
                recipientRole,
                boundaryLevel, 
                tenantId,
                requestInfo,
                fileStoreIdsByLevel
            );
            
            // Generate role-based email subject (uses formatted date)
            SimpleDateFormat dateFormat = new SimpleDateFormat("dd MMM yyyy");
            dateFormat.setTimeZone(TimeZone.getTimeZone("Asia/Kolkata"));
            String formattedDate = dateFormat.format(new Date());
            
            String emailSubject = dynamicEmailTemplateService.generateRoleBasedEmailSubject(
                recipientRole, 
                tenantId, 
                formattedDate
            );
            
            // Send email to each user via Kafka
            for (User user : users) {
                if (user.getEmailId() != null && !user.getEmailId().trim().isEmpty()) {
                    try {
                        sendEmailViaKafka(user, emailSubject, emailBody, csvFileStoreIds, csvFileNames, tenantId);
                        log.info("Published role-based escalation email to Kafka for role: {}, user: {} ({})", 
                            recipientRole, user.getName(), user.getEmailId());
                        
                    } catch (Exception e) {
                        log.error("Error publishing role-based email to Kafka for user: {} ({})", 
                            user.getName(), user.getEmailId(), e);
                    }
                } else {
                    log.warn("User {} has no email address, skipping notification", user.getName());
                }
            }
            
            log.info("Completed publishing role-based escalation emails to Kafka for {} users (role: {}, total tickets: {})", 
                users.size(), recipientRole, totalTickets);
            
        } catch (Exception e) {
            log.error("Error sending role-based escalation emails for role: {}", recipientRole, e);
        }
    }
    
    
    /**
     * Send email via Kafka with CSV attachments
     * Unified method for all roles
     */
    private void sendEmailViaKafka(User user, String subject, String body,
                                  List<String> csvFileStoreIds, List<String> csvFileNames, String tenantId) {
        try {
            // Prepare email attachments from CSV files
            List<Map<String, String>> attachments = new ArrayList<>();
            
            for (int i = 0; i < csvFileStoreIds.size(); i++) {
                String fileStoreId = csvFileStoreIds.get(i);
                String fileName = i < csvFileNames.size() ? csvFileNames.get(i) : "escalation_" + i + ".csv";
                
                try {
                    // Download CSV from file store using existing method
                    byte[] csvBytes = downloadFileFromStorage(tenantId, fileStoreId);
                    
                    if (csvBytes != null && csvBytes.length > 0) {
                        // Convert to Base64
                        String base64Content = Base64.getEncoder().encodeToString(csvBytes);
                        
                        // Create attachment
                        Map<String, String> attachment = new HashMap<>();
                        attachment.put("fileName", fileName);
                        attachment.put("fileContent", base64Content);
                        attachment.put("mimeType", "text/csv");
                        
                        attachments.add(attachment);
                        
                        log.debug("Added CSV attachment: {} (fileStoreId: {})", fileName, fileStoreId);
                    }
                    
                } catch (Exception e) {
                    log.error("Error downloading/encoding CSV file: {} (fileStoreId: {})", fileName, fileStoreId, e);
                }
            }
            
            // Create Kafka email request
            Map<String, Object> emailRequest = new HashMap<>();
            emailRequest.put("email", user.getEmailId());
            emailRequest.put("subject", subject);
            emailRequest.put("body", body);
            emailRequest.put("isHTML", true);
            
            if (!attachments.isEmpty()) {
                emailRequest.put("attachments", attachments);
            }
            
            // Publish to Kafka
            String topic = consumerConfiguration.getNotificationEmailTopic();
            kafkaTemplate.send(topic, emailRequest);
            
            log.debug("Published email to Kafka topic: {} for user: {}", topic, user.getEmailId());
            
        } catch (Exception e) {
            log.error("Error sending email via Kafka for user: {}", user.getEmailId(), e);
            throw new RuntimeException("Failed to send email via Kafka", e);
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
    
    
    /**
     * Upload CSV file to FileStore using StorageUtil
     */
    private String uploadCsvToFileStore(String csvContent, String fileName, String tenantId, RequestInfo requestInfo) {
        try {
            log.info("Uploading CSV file: {} to FileStore for tenant: {}", fileName, tenantId);
            
            // Create MultipartFile from CSV content
            MultipartFile csvFile = createMultipartFileFromContent(csvContent, fileName, "text/csv");
            
            // Create ProcessingContext for StorageUtil
            ProcessingContext context = ProcessingContext.builder()
                    .tenantId(tenantId)
                    .module("Incident")
                    .tag("escalation-csv")
                    .requestInfo(convertRequestInfoToJson(requestInfo))
                    .build();
            
            // Upload to FileStore using existing StorageUtil
            StorageResponse response = storageUtil.uploadToFileStorage(Arrays.asList(csvFile), context);
            
            if (response != null && response.getFiles() != null && !response.getFiles().isEmpty()) {
                String fileStoreId = response.getFiles().get(0).getFileStoreId();
                log.info("Successfully uploaded CSV file: {} with fileStoreId: {}", fileName, fileStoreId);
                return fileStoreId;
            } else {
                log.error("Failed to upload CSV file: {}", fileName);
                return null;
            }
            
        } catch (Exception e) {
            log.error("Error uploading CSV file: {} for tenant: {}", fileName, tenantId, e);
            return null;
        }
    }
    
    /**
     * Create MultipartFile from string content
     */
    private MultipartFile createMultipartFileFromContent(String content, String fileName, String contentType) {
        return new MultipartFile() {
            @Override
            public String getName() {
                return "file";
            }
            
            @Override
            public String getOriginalFilename() {
                return fileName;
            }
            
            @Override
            public String getContentType() {
                return contentType;
            }
            
            @Override
            public boolean isEmpty() {
                return content == null || content.isEmpty();
            }
            
            @Override
            public long getSize() {
                return content != null ? content.getBytes().length : 0;
            }
            
            @Override
            public byte[] getBytes() throws IOException {
                return content != null ? content.getBytes() : new byte[0];
            }
            
            @Override
            public InputStream getInputStream() throws IOException {
                return new ByteArrayInputStream(getBytes());
            }
            
            @Override
            public void transferTo(java.io.File dest) throws IOException, IllegalStateException {
                try (java.io.FileOutputStream fos = new java.io.FileOutputStream(dest)) {
                    fos.write(getBytes());
                }
            }
            
            @Override
            public Resource getResource() {
                try {
                    return new ByteArrayResource(getBytes()) {
                        @Override
                        public String getFilename() {
                            return fileName;
                        }
                    };
                } catch (IOException e) {
                    log.error("Error creating resource for file: {}", fileName, e);
                    return new ByteArrayResource(new byte[0]) {
                        @Override
                        public String getFilename() {
                            return fileName;
                        }
                    };
                }
            }
        };
    }
    
    /**
     * Combine weekly ticket lists with overlap handling
      */
    private List<EscalationTicket> combineWeeklyTicketLists(List<EscalationTicket> previouslyEscalatedTickets, 
                                                           List<EscalationTicket> currentlyInBreachTickets) {
        // Create a map to avoid duplicates based on incident ID
        Map<String, EscalationTicket> combinedTickets = new HashMap<>();
        
        // Add previously escalated tickets first
        for (EscalationTicket ticket : previouslyEscalatedTickets) {
            combinedTickets.put(ticket.getIncidentId(), ticket);
        }
        
        // Add currently in breach tickets (will overwrite if same incident ID)
        for (EscalationTicket ticket : currentlyInBreachTickets) {
            combinedTickets.put(ticket.getIncidentId(), ticket);
        }
        
        List<EscalationTicket> result = new ArrayList<>(combinedTickets.values());
        log.info("Combined weekly tickets: {} previously escalated + {} currently in breach = {} unique tickets", 
                previouslyEscalatedTickets.size(), currentlyInBreachTickets.size(), result.size());
        
        return result;
    }
    
    /**
     * Get last week's start date (Monday)
     */
    private Date getLastWeekStart() {
        Calendar cal = Calendar.getInstance();
        cal.setTime(new Date());
        
        // Go back to last Monday
        int dayOfWeek = cal.get(Calendar.DAY_OF_WEEK);
        int daysToSubtract = (dayOfWeek == Calendar.SUNDAY) ? 7 : dayOfWeek - Calendar.MONDAY;
        cal.add(Calendar.DAY_OF_MONTH, -daysToSubtract - 7); // Go back one more week
        
        // Set to start of day
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        
        return cal.getTime();
    }
    
    /**
     * Get last week's end date (Sunday)
     */
    private Date getLastWeekEnd() {
        Calendar cal = Calendar.getInstance();
        cal.setTime(getLastWeekStart());
        
        // Add 6 days to get to Sunday
        cal.add(Calendar.DAY_OF_MONTH, 6);
        
        // Set to end of day
        cal.set(Calendar.HOUR_OF_DAY, 23);
        cal.set(Calendar.MINUTE, 59);
        cal.set(Calendar.SECOND, 59);
        cal.set(Calendar.MILLISECOND, 999);
        
        return cal.getTime();
    }
    
    /**
     * Health check endpoint
     */
    @GetMapping("/health")
    public ResponseEntity<String> healthCheck() {
        return ResponseEntity.ok("Escalation service is running");
    }
    
    /**
     * Convert RequestInfo object to JSON string for filestore service
     */
    private String convertRequestInfoToJson(RequestInfo requestInfo) {
        try {
            // Configure ObjectMapper to handle potential serialization issues
            ObjectMapper mapper = new ObjectMapper();
            mapper.configure(com.fasterxml.jackson.databind.SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
            mapper.configure(com.fasterxml.jackson.databind.DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
            
            return mapper.writeValueAsString(requestInfo);
        } catch (Exception e) {
            log.warn("Failed to serialize RequestInfo to JSON, using default: {}", e.getMessage());
            // Return a default RequestInfo JSON if serialization fails
            return createDefaultRequestInfoJson();
        }
    }
    
    /**
     * Create a default RequestInfo JSON string
     */
    private String createDefaultRequestInfoJson() {
        return "{\"apiId\":\"im-services-analytics\",\"ver\":\"1.0\",\"ts\":" + System.currentTimeMillis() + 
               ",\"action\":\"_create\",\"did\":\"1\",\"key\":\"\",\"msgId\":\"20170310130900|en_IN\"," +
               "\"requesterId\":\"\",\"authToken\":\"\",\"userInfo\":{\"id\":1,\"uuid\":\"system\"," +
               "\"type\":\"SYSTEM\",\"tenantId\":\"in\",\"roles\":[{\"name\":\"System\",\"code\":\"SYSTEM\"," +
               "\"tenantId\":\"in\"}]}}";
    }
}