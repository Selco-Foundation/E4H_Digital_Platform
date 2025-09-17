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
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
    private final EmailNotificationService emailNotificationService;
    private final ElasticsearchEscalationService elasticsearchEscalationService;
    private final EscalationStatusService escalationStatusService;
    
    /**
     * Daily escalation endpoint
     * OpenAPI Spec: /im-services-analytics/v1/escalation-emails/daily
     * Operation ID: sendDailyEscalationEmail
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
            activeTenantIds.add("as");
            if (escalationRecipients.isEmpty()) {
                log.warn("No escalation recipients found in MDMS");
                escalationStatusService.publishGeneralFailureStatus("daily", "No escalation recipients found in MDMS");
                return ResponseEntity.ok("No escalation recipients found");
            }

            log.info("Found {} escalation recipients and {} active tenants", escalationRecipients.size(), activeTenantIds.size());
            
            // Sort escalation recipients by escalation level priority (LEVEL_ONE first, then LEVEL_TWO)
            escalationRecipients.sort((r1, r2) -> {
                String level1 = r1.getEscalationLevel();
                String level2 = r2.getEscalationLevel();
                
                // LEVEL_ONE (0 hours) should be processed before LEVEL_TWO (-16 hours)
                if ("LEVEL_ONE".equals(level1) && "LEVEL_TWO".equals(level2)) {
                    return -1; // r1 comes first
                } else if ("LEVEL_TWO".equals(level1) && "LEVEL_ONE".equals(level2)) {
                    return 1; // r2 comes first
                } else {
                    return 0; // same level or unknown
                }
            });
            
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
     * OpenAPI Spec: /im-services-analytics/v1/escalation-emails/weekly
     * Operation ID: sendWeeklyEscalationEmail
     */
    @PostMapping("/weekly")
    public void sendWeeklyEscalationEmail(@RequestBody EscalationEmailRequest request) {
        try {
            log.info("Starting weekly SLA escalation processing");
            
            // Use RequestInfo directly
            RequestInfo requestInfo = request.getRequestInfo();
            
            // Fetch master data
            List<EscalationRecipient> escalationRecipients = masterDataService.fetchEscalationRecipients(requestInfo);
//            List<String> activeTenantIds = masterDataService.fetchActiveTenantIds(requestInfo);
            List<String> activeTenantIds = new ArrayList<>();
            activeTenantIds.add("pg");
            activeTenantIds.add("as");
            if (escalationRecipients.isEmpty()) {
                log.warn("No escalation recipients found in MDMS");
                escalationStatusService.publishGeneralFailureStatus("weekly", "No escalation recipients found in MDMS");
                ResponseEntity.ok("No escalation recipients found");
                return;
            }
            
            log.info("Found {} escalation recipients and {} active tenants", escalationRecipients.size(), activeTenantIds.size());
            
            // Sort escalation recipients by escalation level priority (LEVEL_ONE first, then LEVEL_TWO)
            escalationRecipients.sort((r1, r2) -> {
                String level1 = r1.getEscalationLevel();
                String level2 = r2.getEscalationLevel();
                
                // LEVEL_ONE (0 hours) should be processed before LEVEL_TWO (-16 hours)
                if ("LEVEL_ONE".equals(level1) && "LEVEL_TWO".equals(level2)) {
                    return -1; // r1 comes first
                } else if ("LEVEL_TWO".equals(level1) && "LEVEL_ONE".equals(level2)) {
                    return 1; // r2 comes first
                } else {
                    return 0; // same level or unknown
                }
            });
            
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
            log.info("Processing escalation recipient: {} ({})", escalationRecipient.getId(), escalationRecipient.getEscalationLevel());
            
            if (escalationRecipient.getRecipientRoles() == null || escalationRecipient.getRecipientRoles().isEmpty()) {
                log.warn("No recipient roles found for escalation recipient: {}", escalationRecipient.getId());
                return;
            }
            
            // Process each recipient role
            for (RecipientRole recipientRole : escalationRecipient.getRecipientRoles()) {
                processRecipientRole(requestInfo, escalationRecipient, recipientRole, activeTenantIds, escalationType);
            }
            
        } catch (Exception e) {
            log.error("Error processing escalation recipient: {}", escalationRecipient.getId(), e);
        }
    }
    
    /**
     * Process a single recipient role
     * Based on LLD sequence diagram Loop 2
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
     * Process state level escalation
     * Based on LLD sequence diagram steps 3a-10a
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
        
        List<EscalationTicket> tickets;
        List<EscalationTicket> previouslyEscalatedTickets = null;
        List<EscalationTicket> currentlyInBreachTickets = null;
        String csvContent;
        String csvFileName;
        
        if ("weekly".equals(escalationType)) {
            // Step 5a: Query both parts for Weekly Summary
            Date fromDate = getLastWeekStart();
            Date toDate = getLastWeekEnd();
            
            // Part 1: Previously escalated tickets now resolved
            previouslyEscalatedTickets = slaBreachService.findPreviouslyEscalatedTickets(
                tenantId, 
                recipientRole.getWorkflowStates(), 
                escalationId,
                fromDate,
                toDate
            );
            
            // Part 2: Currently in breach tickets (escalated more than one week ago)
            currentlyInBreachTickets = slaBreachService.findCurrentlyInBreachTickets(
                tenantId,
                recipientRole.getWorkflowStates(),
                escalationId
            );
            
            // Combine both lists (with overlap handling)
            tickets = combineWeeklyTicketLists(previouslyEscalatedTickets, currentlyInBreachTickets);
            
            if (tickets.isEmpty()) {
                log.info("No weekly summary tickets found for role: {} in tenant: {}", recipientRole.getRole(), tenantId);
                escalationStatusService.publishSuccessStatus(escalationType, escalationId, tenantId, recipientRoleName);
                return;
            }
            
            log.info("Weekly summary for tenant {}: {} previously escalated, {} currently in breach, {} total", 
                    tenantId, previouslyEscalatedTickets.size(), currentlyInBreachTickets.size(), tickets.size());
            
            // Step 7a: Create Weekly Summary CSV with both parts
            csvContent = csvGenerationService.generateWeeklySummaryCsv(previouslyEscalatedTickets, currentlyInBreachTickets);
            csvFileName = csvGenerationService.generateWeeklySummaryCsvFileName(tenantId);
            
        } else {
            // Step 5a: Query tickets in SLA breach (Daily Escalation)
            tickets = slaBreachService.findSLABreachTickets(
                tenantId, 
                recipientRole.getWorkflowStates(), 
                escalationId,
                escalationRecipient.getEscalationLevel()
            );
            
            if (tickets.isEmpty()) {
                log.info("No SLA breach tickets found for role: {} in tenant: {}", recipientRole.getRole(), tenantId);
                escalationStatusService.publishSuccessStatus(escalationType, escalationId, tenantId, recipientRoleName);
                return;
            }
            
            // Step 7a: Create Daily Escalation CSV
            csvContent = csvGenerationService.generateEscalationCsv(tickets);
            csvFileName = csvGenerationService.generateCsvFileName("daily", escalationRecipient.getEscalationLevel(), tenantId);
        }
        
        // Step 8a: Upload CSV file using StorageUtil
        String csvFileStoreId = uploadCsvToFileStore(csvContent, csvFileName, tenantId, requestInfo);
        
        if (csvFileStoreId == null) {
            throw new RuntimeException("Failed to upload CSV file");
        }
        
        // Step 9a: Update Elasticsearch with escalation information (only for daily escalations)
        if (!"weekly".equals(escalationType)) {
            elasticsearchEscalationService.updateEscalationsForTickets(tickets, escalationId, escalationRecipient.getEscalationLevel());
        }
        
        // Step 10a: Publish message with filestoreId
        if ("weekly".equals(escalationType)) {
            // Use the already queried tickets
            emailNotificationService.sendWeeklySummaryEmails(
                users, 
                previouslyEscalatedTickets, 
                currentlyInBreachTickets,
                escalationRecipient.getEscalationLevel(), 
                recipientRole.getBoundaryLevel(), 
                csvFileStoreId, 
                csvFileName,
                tenantId
            );
        } else {
            emailNotificationService.sendEscalationEmails(
                users, 
                tickets, 
                escalationRecipient.getEscalationLevel(), 
                recipientRole.getBoundaryLevel(), 
                csvFileStoreId, 
                csvFileName,
                escalationType,
                tenantId
            );
        }
        
        // Publish success status
        escalationStatusService.publishSuccessStatus(escalationType, escalationId, tenantId, recipientRoleName);
        
        log.info("Completed state level escalation for tenant: {} and role: {}", tenantId, recipientRoleName);
    }
    
    /**
     * Process country level escalation
     * Based on LLD sequence diagram steps 3b-10b
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
        
        List<EscalationTicket> tickets;
        List<EscalationTicket> previouslyEscalatedTickets = null;
        List<EscalationTicket> currentlyInBreachTickets = null;
        String csvContent;
        String csvFileName;
        
        if ("weekly".equals(escalationType)) {
            // Step 5b: Query both parts for Weekly Summary (Country Level)
            Date fromDate = getLastWeekStart();
            Date toDate = getLastWeekEnd();
            
            // Part 1: Previously escalated tickets now resolved
            previouslyEscalatedTickets = slaBreachService.findPreviouslyEscalatedTicketsForCountry(
                recipientRole.getWorkflowStates(), 
                escalationId,
                fromDate,
                toDate
            );
            
            // Part 2: Currently in breach tickets (escalated more than one week ago)
            currentlyInBreachTickets = slaBreachService.findCurrentlyInBreachTicketsForCountry(
                recipientRole.getWorkflowStates(),
                escalationId
            );
            
            // Combine both lists (with overlap handling)
            tickets = combineWeeklyTicketLists(previouslyEscalatedTickets, currentlyInBreachTickets);
            
            if (tickets.isEmpty()) {
                log.info("No weekly summary tickets found for role: {} at country level", recipientRole.getRole());
                escalationStatusService.publishSuccessStatus(escalationType, escalationId, "in", recipientRoleName);
                return;
            }
            
            log.info("Weekly summary for country level: {} previously escalated, {} currently in breach, {} total", 
                    previouslyEscalatedTickets.size(), currentlyInBreachTickets.size(), tickets.size());
            
            // Step 7b: Create Weekly Summary CSV with both parts
            csvContent = csvGenerationService.generateWeeklySummaryCsv(previouslyEscalatedTickets, currentlyInBreachTickets);
            csvFileName = csvGenerationService.generateWeeklySummaryCsvFileName("in");
            
        } else {
            // Step 5b: Query tickets in SLA breach (Daily Escalation)
            tickets = slaBreachService.findSLABreachTicketsForCountry(
                recipientRole.getWorkflowStates(), 
                escalationId,
                escalationRecipient.getEscalationLevel()
            );
            
            if (tickets.isEmpty()) {
                log.info("No SLA breach tickets found for role: {} at country level", recipientRole.getRole());
                escalationStatusService.publishSuccessStatus(escalationType, escalationId, "in", recipientRoleName);
                return;
            }
            
            // Step 7b: Create Daily Escalation CSV
            csvContent = csvGenerationService.generateEscalationCsv(tickets);
            csvFileName = csvGenerationService.generateCsvFileName("daily", escalationRecipient.getEscalationLevel(), "in");
        }
        
        // Step 8b: Upload CSV file using StorageUtil
        String csvFileStoreId = uploadCsvToFileStore(csvContent, csvFileName, tenantId, requestInfo);
        
        if (csvFileStoreId == null) {
            throw new RuntimeException("Failed to upload CSV file");
        }
        
        // Step 9b: Update Elasticsearch with escalation information (only for daily escalations)
        if (!"weekly".equals(escalationType)) {
            elasticsearchEscalationService.updateEscalationsForTickets(tickets, escalationId, escalationRecipient.getEscalationLevel());
        }
        
        // Step 10b: Publish message with filestoreId
        if ("weekly".equals(escalationType)) {
            // Use the already queried tickets
            emailNotificationService.sendWeeklySummaryEmails(
                users, 
                previouslyEscalatedTickets, 
                currentlyInBreachTickets,
                escalationRecipient.getEscalationLevel(), 
                recipientRole.getBoundaryLevel(), 
                csvFileStoreId, 
                csvFileName,
                tenantId
            );
        } else {
            emailNotificationService.sendEscalationEmails(
                users, 
                tickets, 
                escalationRecipient.getEscalationLevel(), 
                recipientRole.getBoundaryLevel(), 
                csvFileStoreId, 
                csvFileName,
                escalationType
            );
        }
        
        // Publish success status
        escalationStatusService.publishSuccessStatus(escalationType, escalationId, "in", recipientRoleName);
        
        log.info("Completed country level escalation for role: {}", recipientRoleName);
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
     * Anirudh mentioned: "There will be an overlap of the previous week escalations and currently in breach"
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