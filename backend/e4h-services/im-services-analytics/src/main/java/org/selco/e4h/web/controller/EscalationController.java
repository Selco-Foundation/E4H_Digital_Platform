package org.selco.e4h.web.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.egov.common.contract.request.RequestInfo;
import org.selco.e4h.service.*;
import org.selco.e4h.util.StorageUtil;
import org.selco.e4h.util.CommonUtility;
import org.selco.e4h.web.models.FunctionalMetrics;
import org.selco.e4h.web.models.AgeBucketData;
import org.selco.e4h.web.models.ArrowData;
import org.selco.e4h.web.models.*;
import org.selco.e4h.web.models.ProcessingContext;
import org.selco.e4h.web.models.storage.StorageResponse;
import java.util.stream.Collectors;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.text.SimpleDateFormat;
import org.springframework.kafka.core.KafkaTemplate;
import org.selco.e4h.config.ConsumerConfiguration;
import org.selco.e4h.util.ElasticSearchClient;

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
    private final WeeklyReportService weeklyReportService;
    private final WeeklyReportEmailService weeklyReportEmailService;
    private final ElasticSearchClient elasticSearchClient;
    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final ConsumerConfiguration consumerConfiguration;
    private final CommonUtility commonUtility;
    
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
            List<String> activeTenantIds = masterDataService.fetchActiveTenantIds(requestInfo);
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
     * Uses Incident.EscalationRecipient MDMS to get users per state
     * Sends one email per email ID containing all states
     * Runs every Monday at 9:00 AM IST
     */
    @PostMapping("/weekly")
    public ResponseEntity<String> sendWeeklyEscalationEmail(@RequestBody EscalationEmailRequest request) {
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
                return ResponseEntity.ok("No escalation recipients found");
            }
            
            log.info("Found {} escalation recipients and {} active tenants", escalationRecipients.size(), activeTenantIds.size());
            
            // Process weekly reports using Incident.EscalationRecipient MDMS
            processWeeklyReportsWithEscalationRecipients(requestInfo, escalationRecipients, activeTenantIds);
            
            log.info("Completed weekly DRE system report processing");
            return ResponseEntity.ok("Weekly DRE system report processing completed successfully");

        } catch (Exception e) {
            log.error("Error during weekly DRE system report processing", e);
            escalationStatusService.publishGeneralFailureStatus("weekly", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Weekly DRE system report processing failed: " + e.getMessage());
        }
    }
    
    /**
     * Process weekly reports using Incident.EscalationRecipient MDMS
     * Sends one email per email ID containing all states
     */
    private void processWeeklyReportsWithEscalationRecipients(RequestInfo requestInfo, 
                                                           List<EscalationRecipient> escalationRecipients, 
                                                           List<String> activeTenantIds) {
        try {
            log.info("Processing weekly reports with {} escalation recipients for {} active tenants", 
                escalationRecipients.size(), activeTenantIds.size());
            
            // Group escalation recipients by email ID to send one email per recipient
            Map<String, List<EscalationRecipient>> recipientsByEmail = new HashMap<>();
            
            for (EscalationRecipient recipient : escalationRecipients) {
                if (recipient.getActive() == null || !recipient.getActive()) {
                    log.info("Skipping inactive escalation recipient: {}", recipient.getId());
                    continue;
                }
                
                // Get users for this recipient based on boundary level
                List<String> roleCodes = Arrays.asList(recipient.getRecipientRole());
                List<User> users = new ArrayList<>();
                
                if ("state".equals(recipient.getBoundaryLevel())) {
                    // For state-level recipients, get users from all active tenants
                    for (String tenantId : activeTenantIds) {
                        users.addAll(userService.searchUsersByRoleAndTenant(requestInfo, tenantId, roleCodes));
                    }
                } else if ("country".equals(recipient.getBoundaryLevel())) {
                    // For country-level recipients, get users from 'in' tenant
                    users = userService.searchUsersByRoleInCountry(requestInfo, roleCodes);
                }
                
                for (User user : users) {
                    if (user.getEmailId() != null && !user.getEmailId().trim().isEmpty()) {
                        recipientsByEmail.computeIfAbsent(user.getEmailId(), k -> new ArrayList<>()).add(recipient);
                    }
                }
            }
            
            log.info("Found {} unique email addresses for weekly reports", recipientsByEmail.size());
            
            // Process each unique email address
            for (Map.Entry<String, List<EscalationRecipient>> entry : recipientsByEmail.entrySet()) {
                String emailId = entry.getKey();
                List<EscalationRecipient> recipientsForEmail = entry.getValue();
                
                try {
                    processWeeklyReportForEmail(requestInfo, emailId, recipientsForEmail, activeTenantIds);
        } catch (Exception e) {
                    log.error("Error processing weekly report for email: {}", emailId, e);
                }
            }
            
        } catch (Exception e) {
            log.error("Error processing weekly reports with escalation recipients", e);
            throw e;
        }
    }
    
    /**
     * Get relevant tenant IDs for a specific email ID based on their roles
     */
    private Set<String> getRelevantTenantIdsForEmail(RequestInfo requestInfo, String emailId, 
                                                    List<EscalationRecipient> recipients, 
                                                    List<String> activeTenantIds) {
        Set<String> relevantTenantIds = new HashSet<>();
        
        for (EscalationRecipient recipient : recipients) {
            if (recipient.getActive() == null || !recipient.getActive()) {
                continue;
            }
            
            List<String> roleCodes = Arrays.asList(recipient.getRecipientRole());
            
            if ("state".equals(recipient.getBoundaryLevel())) {
                // For state-level recipients, check each tenant individually and track tenant ID
                for (String tenantId : activeTenantIds) {
                    List<User> tenantUsers = userService.searchUsersByRoleAndTenant(requestInfo, tenantId, roleCodes);
                    for (User user : tenantUsers) {
                        if (emailId.equals(user.getEmailId())) {
                            relevantTenantIds.add(tenantId);
                            break; // Found user in this tenant, no need to check other users in same tenant
                        }
                    }
                }
            } else if ("country".equals(recipient.getBoundaryLevel())) {
                // For country-level recipients, get users from 'in' tenant
                List<User> users = userService.searchUsersByRoleInCountry(requestInfo, roleCodes);
                for (User user : users) {
                    if (emailId.equals(user.getEmailId())) {
                        // For country-level roles, include all active tenants
                        relevantTenantIds.addAll(activeTenantIds);
                        break; // Found user, no need to check other users
                    }
                }
            }
        }
        
        log.info("Relevant tenant IDs for email {}: {}", emailId, relevantTenantIds);
        return relevantTenantIds;
    }
    
    /**
     * Process weekly report for a single email ID containing only relevant states
     */
    private void processWeeklyReportForEmail(RequestInfo requestInfo, String emailId, 
                                          List<EscalationRecipient> recipients, 
                                          List<String> activeTenantIds) {
        try {
            log.info("Processing weekly report for email: {} with {} recipients", emailId, recipients.size());
            
            // For weekly reports, only include tenant IDs where the user has roles
            Set<String> relevantTenantIds = getRelevantTenantIdsForEmail(requestInfo, emailId, recipients, activeTenantIds);
            
            // Generate consolidated weekly report data for all relevant tenants
            Map<String, WeeklyReportData> reportDataByTenant = new HashMap<>();
            Map<String, String> csvFileStoreIds = new HashMap<>();
            
            for (String tenantId : relevantTenantIds) {
                try {
                    // Generate weekly report data for this tenant
                    WeeklyReportData reportData = weeklyReportService.generateWeeklyReportData(tenantId, requestInfo);
                    reportDataByTenant.put(tenantId, reportData);
                } catch (Exception e) {
                    log.error("Error generating weekly report data for tenant: {}", tenantId, e);
                }
            }

            // Build ONE consolidated CSV across all mapped states and upload to tenant "in"
            try {
                String consolidatedCsv = generateConsolidatedWeeklyCsv(relevantTenantIds, requestInfo);
                String consolidatedFileName = generateCsvFileName();
                String consolidatedFsId = uploadCsvToFileStore(consolidatedCsv, consolidatedFileName, "in", requestInfo);
                if (consolidatedFsId != null) {
                    csvFileStoreIds.put("in", consolidatedFsId);
                    log.info("Uploaded consolidated weekly CSV with filestoreId {} under tenant 'in'", consolidatedFsId);
                }
            } catch (Exception e) {
                log.error("Failed to generate/upload consolidated weekly CSV", e);
            }

            // Send consolidated email with all states
            sendConsolidatedWeeklyReportEmail(requestInfo, emailId, reportDataByTenant, csvFileStoreIds);
            
        } catch (Exception e) {
            log.error("Error processing weekly report for email: {}", emailId, e);
            throw e;
        }
    }
    
    /**
     * Send consolidated weekly report email with all states
     */
    private void sendConsolidatedWeeklyReportEmail(RequestInfo requestInfo, String emailId, 
                                                 Map<String, WeeklyReportData> reportDataByTenant,
                                                 Map<String, String> csvFileStoreIds) {
        try {
            log.info("Sending consolidated weekly report email to: {} for {} tenants", emailId, reportDataByTenant.size());
            
            // Create a consolidated report data structure
            WeeklyReportData consolidatedData = createConsolidatedReportData(reportDataByTenant);
            
            // Get user info for email - try to get actual user name
            User user = getUserByEmailId(requestInfo, emailId);
            if (user == null) {
                user = new User();
                user.setEmailId(emailId);
                user.setName("Weekly Report Recipient"); // Fallback name
            }
            
            // Generate download URL for the first available CSV file
            String downloadUrl = "#";
            if (!csvFileStoreIds.isEmpty()) {
                String firstFileStoreId = csvFileStoreIds.values().iterator().next();
                downloadUrl = commonUtility.generateDownloadUrl(
                    firstFileStoreId, "in", 
                    consumerConfiguration.getFileStoreBaseUrl(),
                    consumerConfiguration.getFileStoreDownloadEndpoint()
                );
            }
            
            // Generate email HTML using the weekly report email service
            String emailBody = weeklyReportEmailService.generateWeeklyReportEmailHTML(
                consolidatedData, user.getName(), "consolidated", requestInfo, downloadUrl);
            
            // Generate email subject
            String emailSubject = weeklyReportEmailService.generateWeeklyReportEmailSubject(
                user.getName(), consolidatedData);
            
            // Send email via Kafka
            sendEmailViaKafka(user, emailSubject, emailBody, new ArrayList<>(), new ArrayList<>(), "in");
            
            log.info("Successfully sent consolidated weekly report email to: {}", emailId);

        } catch (Exception e) {
            log.error("Error sending consolidated weekly report email to: {}", emailId, e);
            throw e;
        }
    }
    
    /**
     * Create consolidated report data from multiple tenant reports
     */
    private WeeklyReportData createConsolidatedReportData(Map<String, WeeklyReportData> reportDataByTenant) {
        if (reportDataByTenant.isEmpty()) {
            return WeeklyReportData.builder().build();
        }
        
        // Use the first report as base and aggregate data
        WeeklyReportData firstReport = reportDataByTenant.values().iterator().next();
        
        int totalFuncStart = 0, totalNonFuncStart = 0;
        int totalFuncEnd = 0, totalNonFuncEnd = 0;
        int totalLt1Wk = 0, totalLt1Mo = 0, totalLt3Mo = 0;
        
        Map<String, WeeklyReportData.StateAgeBucketData> consolidatedStateData = new HashMap<>();
        
        for (WeeklyReportData reportData : reportDataByTenant.values()) {
            if (reportData.getWeekStartMetrics() != null) {
                totalFuncStart += reportData.getWeekStartMetrics().getFunctionalCount();
                totalNonFuncStart += reportData.getWeekStartMetrics().getNonFunctionalCount();
            }
            if (reportData.getWeekEndMetrics() != null) {
                totalFuncEnd += reportData.getWeekEndMetrics().getFunctionalCount();
                totalNonFuncEnd += reportData.getWeekEndMetrics().getNonFunctionalCount();
            }
            if (reportData.getTotalAgeBuckets() != null) {
                totalLt1Wk += reportData.getTotalAgeBuckets().getTotalLt1Wk();
                totalLt1Mo += reportData.getTotalAgeBuckets().getTotalLt1Mo();
                totalLt3Mo += reportData.getTotalAgeBuckets().getTotalLt3Mo();
            }
            
            // Merge state data
            if (reportData.getStateData() != null) {
                consolidatedStateData.putAll(reportData.getStateData());
            }
        }
        
        // Calculate percentages
        int totalStart = totalFuncStart + totalNonFuncStart;
        int totalEnd = totalFuncEnd + totalNonFuncEnd;
        
        double funcStartPct = totalStart > 0 ? (totalFuncStart * 100.0 / totalStart) : 0;
        double nonFuncStartPct = totalStart > 0 ? (totalNonFuncStart * 100.0 / totalStart) : 0;
        double funcEndPct = totalEnd > 0 ? (totalFuncEnd * 100.0 / totalEnd) : 0;
        double nonFuncEndPct = totalEnd > 0 ? (totalNonFuncEnd * 100.0 / totalEnd) : 0;
        
        // Calculate arrows
        ArrowData funcArrow = calculateArrow(funcStartPct, funcEndPct, true);
        ArrowData nonFuncArrow = calculateArrow(nonFuncStartPct, nonFuncEndPct, false);
        
        // Create consolidated state list - use tenant IDs if no state data
        String consolidatedStateList;
        log.info("Creating consolidated state list. consolidatedStateData size: {}, reportDataByTenant keys: {}", 
            consolidatedStateData.size(), reportDataByTenant.keySet());
        
        if (consolidatedStateData.isEmpty()) {
            // If no state data, use tenant IDs from the reports
            consolidatedStateList = reportDataByTenant.keySet().stream()
                .map(commonUtility::getStateDisplayName)
                .collect(Collectors.joining(", "));
            log.info("Using tenant IDs for state list: {}", consolidatedStateList);
        } else {
            // Use state data keys if available
            consolidatedStateList = consolidatedStateData.keySet().stream()
                .map(commonUtility::getStateDisplayName)
                .collect(Collectors.joining(", "));
            log.info("Using state data keys for state list: {}", consolidatedStateList);
        }
        
        // Create FunctionalMetrics objects
        FunctionalMetrics startMetrics = FunctionalMetrics.builder()
            .functionalCount(totalFuncStart)
            .nonFunctionalCount(totalNonFuncStart)
            .build();

        FunctionalMetrics endMetrics = FunctionalMetrics.builder()
            .functionalCount(totalFuncEnd)
            .nonFunctionalCount(totalNonFuncEnd)
            .build();

        // Create AgeBucketData object
        AgeBucketData totalAgeBuckets = AgeBucketData.builder()
            .totalLt1Wk(totalLt1Wk)
            .totalLt1Mo(totalLt1Mo)
            .totalLt3Mo(totalLt3Mo)
            .build();

        return WeeklyReportData.builder()
            .tenantId("consolidated")
            .dateRange(firstReport.getDateRange())
            .weekStartDate(firstReport.getWeekStartDate())
            .weekEndDate(firstReport.getWeekEndDate())
            .weekStartMetrics(startMetrics)
            .weekEndMetrics(endMetrics)
            .functionalArrow(funcArrow)
            .nonFunctionalArrow(nonFuncArrow)
            .totalAgeBuckets(totalAgeBuckets)
            .stateData(consolidatedStateData)
            .stateList(consolidatedStateList)
            .todayFormatted(firstReport.getTodayFormatted())
            .build();

    }
    
    /**
     * Calculate arrow direction and class for percentage changes
     */
    private ArrowData calculateArrow(double startPct, double endPct, boolean isFunctional) {
        double change = endPct - startPct;
        
        if (Math.abs(change) < 0.1) {
            return ArrowData.builder()
                .arrow("")
                .arrowClass("")
                .build();
        }
        
        if (isFunctional) {
            if (change > 0) {
                return ArrowData.builder()
                    .arrow("↑")
                    .arrowClass("up")
                    .build();
            } else {
                return ArrowData.builder()
                    .arrow("↓")
                    .arrowClass("down")
                    .build();
            }
        } else {
            if (change > 0) {
                return ArrowData.builder()
                    .arrow("↑")
                    .arrowClass("down")
                    .build();
            } else {
                return ArrowData.builder()
                    .arrow("↓")
                    .arrowClass("up")
                    .build();
            }
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

            // Always process escalation level, even with zero counts
            List<EscalationTicket> filteredTickets = new ArrayList<>();
            if (tickets != null && !tickets.isEmpty()) {
                // Filter tickets by MDMS workflow states to match email template logic
                filteredTickets = filterTicketsByWorkflowStates(tickets, item.getWorkflowStates());
            }
            
            // Always add to ticketsByLevel (even if empty) for consistent email generation
            ticketsByLevel.put(item.getEscalationLevel(), filteredTickets);
            
            // Always generate CSV (with headers only if no tickets)
            String csvContent = csvGenerationService.generateEscalationCsv(filteredTickets);
            String csvFileName = csvGenerationService.generateCsvFileName("daily", item.getEscalationLevel(), tenantId);
            String csvFileStoreId = uploadCsvToFileStore(csvContent, csvFileName, tenantId, requestInfo);
            
            if (csvFileStoreId != null) {
                csvFileStoreIds.add(csvFileStoreId);
                csvFileNames.add(csvFileName);
            }

            // Update Elasticsearch for this level (only if there are tickets)
            if (!filteredTickets.isEmpty()) {
                elasticsearchEscalationService.updateEscalationsForTickets(filteredTickets, escalationId, item.getEscalationLevel());
            }
            
            log.info("Processed escalation level: {} with {} tickets (filtered from {} total)", 
                item.getEscalationLevel(), filteredTickets.size(), tickets != null ? tickets.size() : 0);
        }

        // Always send email (even with zero counts) - use new role-based email generation
        // Pass MDMS workflow states to template for correct filtering
        Map<String, List<String>> workflowStatesByLevel = new HashMap<>();
        for (EscalationRoleEscalationItem item : items) {
            workflowStatesByLevel.put(item.getEscalationLevel(), item.getWorkflowStates());
        }
        
        sendRoleBasedEscalationEmail(requestInfo, users, ticketsByLevel, recipientRole.getRole(),
            recipientRole.getBoundaryLevel(), csvFileStoreIds, csvFileNames, escalationType, tenantId, workflowStatesByLevel);

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

            // Always process escalation level, even with zero counts
            List<EscalationTicket> filteredTickets = new ArrayList<>();
            if (tickets != null && !tickets.isEmpty()) {
                // Filter tickets by MDMS workflow states to match email template logic
                filteredTickets = filterTicketsByWorkflowStates(tickets, item.getWorkflowStates());
            }
            
            // Always add to ticketsByLevel (even if empty) for consistent email generation
            ticketsByLevel.put(item.getEscalationLevel(), filteredTickets);
            
            // Always generate CSV (with headers only if no tickets)
            String csvContent = csvGenerationService.generateEscalationCsv(filteredTickets);
            String csvFileName = csvGenerationService.generateCsvFileName("daily", item.getEscalationLevel(), "in");
            String csvFileStoreId = uploadCsvToFileStore(csvContent, csvFileName, tenantId, requestInfo);
            
            if (csvFileStoreId != null) {
                csvFileStoreIds.add(csvFileStoreId);
                csvFileNames.add(csvFileName);
            }

            // Update Elasticsearch for this level (only if there are tickets)
            if (!filteredTickets.isEmpty()) {
                elasticsearchEscalationService.updateEscalationsForTickets(filteredTickets, escalationId, item.getEscalationLevel());
            }
            
            log.info("Processed country escalation level: {} with {} tickets (filtered from {} total)", 
                item.getEscalationLevel(), filteredTickets.size(), tickets != null ? tickets.size() : 0);
        }

        // Special handling for CENTRAL_POC: Create combined CSV for L1 section (LEVEL_ZERO + LEVEL_ONE)
        if ("CENTRAL_POC".equals(recipientRole.getRole())) {
            // Combine LEVEL_ZERO and LEVEL_ONE tickets for L1 section
            List<EscalationTicket> l1Tickets = new ArrayList<>();
            if (ticketsByLevel.get("LEVEL_ZERO") != null) {
                l1Tickets.addAll(ticketsByLevel.get("LEVEL_ZERO"));
            }
            if (ticketsByLevel.get("LEVEL_ONE") != null) {
                l1Tickets.addAll(ticketsByLevel.get("LEVEL_ONE"));
            }
            
            // Generate combined CSV for L1 section
            if (!l1Tickets.isEmpty()) {
                String l1CsvContent = csvGenerationService.generateEscalationCsv(l1Tickets);
                String l1CsvFileName = csvGenerationService.generateCsvFileName("daily", "LEVEL_ONE", "in");
                String l1CsvFileStoreId = uploadCsvToFileStore(l1CsvContent, l1CsvFileName, "in", requestInfo);
                
                if (l1CsvFileStoreId != null) {
                    // Clear existing file store IDs and add only the combined L1 and L2 files
                    csvFileStoreIds.clear();
                    csvFileNames.clear();
                    
                    // Add L1 combined file
                    csvFileStoreIds.add(l1CsvFileStoreId);
                    csvFileNames.add(l1CsvFileName);
                    
                    // Add L2 file if it exists
                    if (ticketsByLevel.get("LEVEL_TWO") != null && !ticketsByLevel.get("LEVEL_TWO").isEmpty()) {
                        String l2CsvContent = csvGenerationService.generateEscalationCsv(ticketsByLevel.get("LEVEL_TWO"));
                        String l2CsvFileName = csvGenerationService.generateCsvFileName("daily", "LEVEL_TWO", "in");
                        String l2CsvFileStoreId = uploadCsvToFileStore(l2CsvContent, l2CsvFileName, "in", requestInfo);
                        
                        if (l2CsvFileStoreId != null) {
                            csvFileStoreIds.add(l2CsvFileStoreId);
                            csvFileNames.add(l2CsvFileName);
                        }
                    }
                }
            }
        }

        // Always send email (even with zero counts) - use new role-based email generation
        // Pass MDMS workflow states to template for correct filtering
        Map<String, List<String>> workflowStatesByLevel = new HashMap<>();
        for (EscalationRoleEscalationItem item : items) {
            workflowStatesByLevel.put(item.getEscalationLevel(), item.getWorkflowStates());
        }
        
        sendRoleBasedEscalationEmail(requestInfo, users, ticketsByLevel, recipientRole.getRole(),
            recipientRole.getBoundaryLevel(), csvFileStoreIds, csvFileNames, escalationType, "in", workflowStatesByLevel);

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
     * Filter tickets by MDMS workflow states to ensure consistency between email template and CSV
     */
    private List<EscalationTicket> filterTicketsByWorkflowStates(List<EscalationTicket> tickets, List<String> mdmsWorkflowStates) {
        if (mdmsWorkflowStates == null || mdmsWorkflowStates.isEmpty()) {
            return tickets;
        }
        
        return tickets.stream()
            .filter(ticket -> {
                String ticketStatus = ticket.getApplicationStatus();
                return ticketStatus != null && mdmsWorkflowStates.contains(ticketStatus);
            })
            .collect(Collectors.toList());
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
                                             String escalationType, String tenantId, 
                                             Map<String, List<String>> workflowStatesByLevel) {
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
                fileStoreIdsByLevel,
                workflowStatesByLevel
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
     * Get user by email ID from user service
     */
    private User getUserByEmailId(RequestInfo requestInfo, String emailId) {
        try {
            // Search for users with this email ID across all active tenants
            List<String> activeTenantIds = masterDataService.fetchActiveTenantIds(requestInfo);
            
            for (String tenantId : activeTenantIds) {
                // Search for users with any role in this tenant
                List<String> allRoles = Arrays.asList("CENTRAL_POC", "STATE_POC", "VENDOR", "ADMIN");
                List<User> users = userService.searchUsersByRoleAndTenant(requestInfo, tenantId, allRoles);
                
                for (User user : users) {
                    if (emailId.equals(user.getEmailId())) {
                        log.info("Found user: {} for email: {}", user.getName(), emailId);
                        return user;
                    }
                }
            }
            
            // Also check country-level tenant
            List<String> allRoles = Arrays.asList("CENTRAL_POC", "STATE_POC", "VENDOR", "ADMIN");
            List<User> countryUsers = userService.searchUsersByRoleInCountry(requestInfo, allRoles);
            
            for (User user : countryUsers) {
                if (emailId.equals(user.getEmailId())) {
                    log.info("Found country-level user: {} for email: {}", user.getName(), emailId);
                    return user;
                }
            }
            
            log.warn("No user found for email: {}", emailId);
            return null;
            
        } catch (Exception e) {
            log.error("Error fetching user by email ID: {}", emailId, e);
            return null;
        }
    }
    
    
    /**
     * Send email via Kafka without CSV attachments (download buttons are used instead)
     */
    private void sendEmailViaKafka(User user, String subject, String body, 
                                  List<String> csvFileStoreIds, List<String> csvFileNames, String tenantId) {
        try {
            // Create Email object following egov-notification-mail contract
            Map<String, Object> email = new HashMap<>();
            email.put("emailTo", new HashSet<>(Arrays.asList(user.getEmailId())));  // Set<String>
            email.put("subject", subject);
            email.put("body", body);
            email.put("isHTML", true);
            email.put("tenantId", tenantId);
            
            // Note: CSV files are not attached as email attachments anymore
            // Download functionality is provided via download buttons in the email template
            
            // Create EmailRequest wrapper with RequestInfo
            Map<String, Object> emailRequest = new HashMap<>();
            emailRequest.put("requestInfo", new HashMap<>());  // Empty RequestInfo is acceptable
            emailRequest.put("email", email);
            
            // Publish to Kafka
            String topic = consumerConfiguration.getNotificationEmailTopic();
            kafkaTemplate.send(topic, emailRequest);
            
            log.info("Published email to Kafka topic: {} for user: {} (no attachments - download buttons used instead)", 
                topic, user.getEmailId());
            
        } catch (Exception e) {
            log.error("Error sending email via Kafka for user: {}", user.getEmailId(), e);
            throw new RuntimeException("Failed to send email via Kafka", e);
        }
    }

    /**
     * Generate a single consolidated weekly CSV across all mapped state tenants.
     * The CSV includes both functional and non-functional facilities and is intended
     * to be uploaded under tenantId = "in".
     */
    private String generateConsolidatedWeeklyCsv(Set<String> stateTenantIds, RequestInfo requestInfo) {
        StringBuilder csv = new StringBuilder();
        appendCsvHeader(csv);

        try {
            List<Map<String, Object>> tickets = elasticSearchClient.fetchRequiredTickets(0, 10000, false);
            log.info("Consolidated CSV: fetched {} tickets from ES", tickets.size());

            Map<String, Map<String, Object>> facilityAgg = new LinkedHashMap<>();
            for (Map<String, Object> ticket : tickets) {
                Map<String, Object> data = (Map<String, Object>) ticket.get("Data");
                if (data == null) continue;

                String tenantId = getStringValue(data, "tenantId");
                if (!isInScope(tenantId, stateTenantIds)) continue;

                String facilityName = resolveFacilityName(data);
                String ninOrHfr = getStringValue(data, "ninOrHfr");
                String stateRoot = rootTenant(tenantId);
                String district = getStringValue(data, "district");
                String block = getStringValue(data, "block");
                String hfType = resolveHfType(data);
                String vendor = resolveVendor(data);
                String status = extractApplicationStatus(data);
                boolean isClosed = isClosed(status);
                boolean isFunctional = "FUNCTIONAL".equalsIgnoreCase(getStringValue(data, "systemFunctional"));

                Map<String, Object> row = getOrCreateFacilityRow(facilityAgg, facilityName, ninOrHfr, stateRoot, district, block, hfType, vendor);
                incrementCounts(row, isClosed, isFunctional);
            }

            facilityAgg.values().forEach(r -> appendCsvRow(csv, r));

        } catch (Exception e) {
            log.error("Error generating consolidated weekly CSV", e);
        }

        return csv.toString();
    }

    private void appendCsvHeader(StringBuilder csv) {
        csv.append("\"Health Facility\",\"NIN OR HFR\",\"Solar Working\",\"State\",\"District\",\"Block\",\"Health Facility Type\",\"Mapped Vendor\",\"No of Ticket\",\"Open Ticket\",\"Closed Ticket\"\r\n");
    }

    private boolean isInScope(String tenantId, Set<String> stateTenantIds) {
        if (tenantId == null || tenantId.isEmpty()) return false;
        for (String state : stateTenantIds) {
            if (tenantId.equalsIgnoreCase(state) || tenantId.startsWith(state + ".")) return true;
        }
        return false;
    }

    private String rootTenant(String tenantId) {
        return tenantId.contains(".") ? tenantId.substring(0, tenantId.indexOf('.')) : tenantId;
    }

    private String resolveFacilityName(Map<String, Object> data) {
        String name = getStringValue(data, "tenantId_localized");
        return name.isEmpty() ? getStringValue(data, "tenantId") : name;
    }

    private String resolveHfType(Map<String, Object> data) {
        String type = getStringValue(data, "phcType");
        if (!type.isEmpty()) return type;
        Map<String, Object> incident = (Map<String, Object>) data.get("incident");
        return incident != null ? getStringValue(incident, "phcSubType") : "";
    }

    private String resolveVendor(Map<String, Object> data) {
        String vendor = getStringValue(data, "mappedVendorName");
        return vendor.isEmpty() ? getStringValue(data, "mappedVendorUserName") : vendor;
    }

    private String extractApplicationStatus(Map<String, Object> data) {
        Map<String, Object> cpi = (Map<String, Object>) data.get("currentProcessInstance");
        if (cpi == null) return "N/A";
        Map<String, Object> st = (Map<String, Object>) cpi.get("state");
        return st != null ? getStringValue(st, "applicationStatus") : "N/A";
    }

    private boolean isClosed(String status) {
        return status.equalsIgnoreCase("RESOLVED") ||
               status.equalsIgnoreCase("CLOSED_AFTER_RESOLUTION") ||
               status.equalsIgnoreCase("CLOSED_AFTER_REJECTION") ||
               status.equalsIgnoreCase("REJECTED");
    }

    private Map<String, Object> getOrCreateFacilityRow(Map<String, Map<String, Object>> agg,
                                                       String facility, String nin, String stateRoot,
                                                       String district, String block, String type, String vendor) {
        String key = facility + "|" + district + "|" + block + "|" + stateRoot;
        return agg.computeIfAbsent(key, k -> {
            Map<String, Object> m = new HashMap<>();
            m.put("facility", facility);
            m.put("nin", nin);
            m.put("state", commonUtility.getStateDisplayName(stateRoot));
            m.put("district", district);
            m.put("block", block);
            m.put("type", type);
            m.put("vendor", vendor);
            m.put("total", 0L);
            m.put("open", 0L);
            m.put("closed", 0L);
            m.put("solarWorking", "No");
            return m;
        });
    }

    private void incrementCounts(Map<String, Object> row, boolean isClosed, boolean isFunctional) {
        row.put("total", (long) row.get("total") + 1);
        if (isClosed) {
            row.put("closed", (long) row.get("closed") + 1);
        } else {
            row.put("open", (long) row.get("open") + 1);
        }
        if (isFunctional) row.put("solarWorking", "Yes");
    }

    private void appendCsvRow(StringBuilder csv, Map<String, Object> row) {
        csv.append(escapeCsvField((String) row.get("facility"))).append(",")
           .append(escapeCsvField((String) row.get("nin"))).append(",")
           .append(escapeCsvField((String) row.get("solarWorking"))).append(",")
           .append(escapeCsvField((String) row.get("state"))).append(",")
           .append(escapeCsvField((String) row.get("district"))).append(",")
           .append(escapeCsvField((String) row.get("block"))).append(",")
           .append(escapeCsvField((String) row.get("type"))).append(",")
           .append(escapeCsvField((String) row.get("vendor"))).append(",")
           .append(row.get("total")).append(",")
           .append(row.get("open")).append(",")
           .append(row.get("closed")).append("\r\n");
    }

    
    /**
     * Helper method to safely get string value from map
     */
    private String getStringValue(Map<String, Object> map, String key) {
        Object value = map.get(key);
        return value != null ? value.toString() : "";
    }
    
    /**
     * Helper method to escape CSV fields
     */
    private String escapeCsvField(String field) {
        if (field == null) return "";
        if (field.contains(",") || field.contains("\"") || field.contains("\n")) {
            return "\"" + field.replace("\"", "\"\"") + "\"";
        }
        return field;
    }
    
    /**
     * Generate CSV filename for weekly report
     */
    private String generateCsvFileName() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd_HHmmss");
        dateFormat.setTimeZone(TimeZone.getTimeZone("Asia/Kolkata"));
        String timestamp = dateFormat.format(new Date());
        
        return String.format("weekly_report_%s.csv", timestamp);
    }
    
    /**
     * Upload CSV file to FileStore
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
                    .requestInfo(commonUtility.convertRequestInfoToJson(requestInfo))
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
     * Health check endpoint
     */
    @GetMapping("/health")
    public ResponseEntity<String> healthCheck() {
        return ResponseEntity.ok("Escalation service is running");
    }
    
}