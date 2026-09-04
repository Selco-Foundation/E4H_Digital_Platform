package org.selco.e4h.web.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.egov.common.contract.request.RequestInfo;
import org.selco.e4h.service.*;
import org.selco.e4h.util.StorageUtil;
import org.selco.e4h.util.CommonUtility;
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
import org.selco.e4h.util.EscalationTemplateType;
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
    private final ElasticSearchClient elasticSearchClient;
    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final ConsumerConfiguration consumerConfiguration;
    private final CommonUtility commonUtility;
    private final DailyStatePocSummaryBuilder dailyStatePocSummaryBuilder;
    private final DailyStatePocEmailService dailyStatePocEmailService;
    private final DailySeniorProgramManagerSummaryBuilder dailySeniorProgramManagerSummaryBuilder;
    private final DailySeniorProgramManagerEmailService dailySeniorProgramManagerEmailService;
    private final DailyProcurementSummaryBuilder dailyProcurementSummaryBuilder;
    private final DailyProcurementEmailService dailyProcurementEmailService;
    private final WeeklyEscalationAnalyticsService weeklyEscalationAnalyticsService;
    private final WeeklySeniorProgramManagerEmailService weeklySeniorProgramManagerEmailService;
    private final WeeklyProcurementEmailService weeklyProcurementEmailService;
    private final WeeklyLeadershipEmailService weeklyLeadershipEmailService;
    
    /**
     * Daily escalation endpoint
     */
    @PostMapping("/daily")
    public ResponseEntity<String> sendDailyEscalationEmail(@RequestBody EscalationEmailRequest request) {
        log.trace("Received request to send daily escalation email");
        try {
            log.info("Starting daily SLA escalation processing");
            
            // Use RequestInfo directly
            RequestInfo requestInfo = request.getRequestInfo();
            log.debug("RequestInfo extracted from request");
            
            // Fetch master data
            List<EscalationRecipient> escalationRecipients = masterDataService.fetchEscalationRecipients(requestInfo);
            List<String> activeTenantIds = masterDataService.fetchActiveTenantIds(requestInfo);
            log.debug("Fetched {} escalation recipients and {} active tenants", escalationRecipients.size(), activeTenantIds.size());
            
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
                
                log.debug("Processing escalation recipient: {}", escalationRecipient.getId());
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
     * Weekly escalation endpoint — routes by MDMS templateType to SPM / Procurement / Leadership weekly emails.
     */
    @PostMapping("/weekly")
    public ResponseEntity<String> sendWeeklyEscalationEmail(@RequestBody EscalationEmailRequest request) {
        log.trace("Received request to send weekly escalation email");
        try {
            log.info("Starting weekly escalation processing");
            
            RequestInfo requestInfo = request.getRequestInfo();
            
            List<EscalationRecipient> escalationRecipients = masterDataService.fetchEscalationRecipients(requestInfo);
            List<String> activeTenantIds = masterDataService.fetchActiveTenantIds(requestInfo);
            Map<String, String> activeTenantIdsName = masterDataService.getActiveTenantIdsName(requestInfo);
            
            if (escalationRecipients.isEmpty()) {
                log.warn("No escalation recipients found in MDMS");
                escalationStatusService.publishGeneralFailureStatus("weekly", "No escalation recipients found in MDMS");
                return ResponseEntity.ok("No escalation recipients found");
            }
            
            log.info("Found {} escalation recipients and {} active tenants", escalationRecipients.size(), activeTenantIds.size());

            for (EscalationRecipient recipient : escalationRecipients) {
                if (recipient.getActive() == null || !recipient.getActive()) {
                    continue;
                }
                if (isWeeklyTemplate(recipient.getTemplateType())) {
                    processWeeklyEscalationRecipient(requestInfo, recipient, activeTenantIds, activeTenantIdsName);
                } else {
                    log.warn("Skipping weekly recipient id={} — missing or unrecognized templateType: {}",
                            recipient.getId(), recipient.getTemplateType());
                }
            }

            log.info("Completed weekly escalation processing");
            return ResponseEntity.ok("Weekly escalation processing completed successfully");

        } catch (Exception e) {
            log.error("Error during weekly escalation processing", e);
            escalationStatusService.publishGeneralFailureStatus("weekly", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Weekly escalation processing failed: " + e.getMessage());
        }
    }
    
    /**
     * Get relevant tenant IDs for a specific email ID based on their roles
     */
    private Set<String> getRelevantTenantIdsForEmail(RequestInfo requestInfo, String emailId, 
                                                    List<EscalationRecipient> recipients, 
                                                    List<String> activeTenantIds) {
        log.trace("Getting relevant tenant IDs for email: {}, recipient count: {}", emailId, recipients != null ? recipients.size() : 0);
        Set<String> relevantTenantIds = new HashSet<>();
        Map<String, String> activeTenantIdsName = masterDataService.getActiveTenantIdsName(requestInfo);
        for (EscalationRecipient recipient : recipients) {
            if (recipient.getActive() == null || !recipient.getActive()) {
                continue;
            }
            
            List<String> roleCodes = Arrays.asList(recipient.getRecipientRole());
            
            if ("state".equals(recipient.getBoundaryLevel())) {
                // For state-level recipients, check each tenant individually and track tenant ID
                for (String tenantId : activeTenantIds) {
                    String state = activeTenantIdsName.get(tenantId);
                    List<User> tenantUsers = userService.searchUsersByRoleAndBoundaryCode(requestInfo, state, roleCodes);
                    for (User user : tenantUsers) {
                        if (emailId.equals(user.getEmailId())) {
                            relevantTenantIds.add(tenantId);
                            break; // Found user in this tenant, no need to check other users in same tenant
                        }
                    }
                }
            } else if ("country".equals(recipient.getBoundaryLevel())) {
                // For country-level recipients, get users with boundary "India" from 'in' tenant
                List<User> users = userService.searchUsersByRoleAndBoundaryCode(requestInfo, "India", roleCodes);
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
     * Process a single escalation recipient — routes by MDMS templateType only.
     */
    private void processEscalationRecipient(RequestInfo requestInfo, EscalationRecipient escalationRecipient, List<String> activeTenantIds, String escalationType) {
        try {
            log.info("Processing escalation recipient V2: {} role={} boundary={} templateType={} items={} ",
                    escalationRecipient.getId(), escalationRecipient.getRecipientRole(),
                    escalationRecipient.getBoundaryLevel(), escalationRecipient.getTemplateType(),
                    escalationRecipient.getEscalations() != null ? escalationRecipient.getEscalations().size() : 0);

            if ("daily".equals(escalationType) && EscalationTemplateType.DAILY_STATE_POC.equals(escalationRecipient.getTemplateType())) {
                processDailyStatePocEscalation(requestInfo, escalationRecipient, activeTenantIds);
                return;
            }

            if ("daily".equals(escalationType) && EscalationTemplateType.DAILY_SENIOR_PROGRAM_MANAGER.equals(escalationRecipient.getTemplateType())) {
                processDailySeniorProgramManagerEscalation(requestInfo, escalationRecipient, activeTenantIds);
                return;
            }

            if ("daily".equals(escalationType) && EscalationTemplateType.DAILY_PROCUREMENT.equals(escalationRecipient.getTemplateType())) {
                processDailyProcurementEscalation(requestInfo, escalationRecipient);
                return;
            }

            log.warn("Skipping {} recipient id={} — missing or unrecognized templateType: {}",
                    escalationType, escalationRecipient.getId(), escalationRecipient.getTemplateType());
            
        } catch (Exception e) {
            log.error("Error processing escalation recipient: {}", escalationRecipient.getId(), e);
        }
    }

    /**
     * Process daily State POC escalation using the new template (new vs previously open sections).
     */
    private void processDailyStatePocEscalation(RequestInfo requestInfo,
                                                EscalationRecipient escalationRecipient,
                                                List<String> activeTenantIds) {
        String escalationId = escalationRecipient.getId().toString();
        String recipientRoleName = escalationRecipient.getRecipientRole();
        List<EscalationRoleEscalationItem> items = escalationRecipient.getEscalations();

        if (items == null || items.isEmpty()) {
            log.warn("No escalation items configured for daily State POC recipient: {}", escalationId);
            return;
        }

        if (!"state".equals(escalationRecipient.getBoundaryLevel())) {
            log.warn("Daily State POC template is only supported at state boundary level, recipient: {}", escalationId);
            return;
        }

        items.sort((a, b) -> levelOrder(a.getEscalationLevel()) - levelOrder(b.getEscalationLevel()));
        EscalationRoleEscalationItem escalationItem = items.get(0);
        Map<String, String> activeTenantIdsName = masterDataService.getActiveTenantIdsName(requestInfo);

        for (String tenantId : activeTenantIds) {
            String state = activeTenantIdsName.get(tenantId);
            if (state == null || state.isBlank()) {
                continue;
            }

            try {
                List<String> roleCodes = List.of(recipientRoleName);
                List<User> users = userService.searchUsersByRoleAndBoundaryCode(requestInfo, state, roleCodes);

                if (users.isEmpty()) {
                    log.warn("No State POC users found for state: {} tenant: {}", state, tenantId);
                    escalationStatusService.publishSuccessStatus("daily", escalationId, tenantId, recipientRoleName);
                    continue;
                }

                List<EscalationTicket> newBreaches = slaBreachService.findSLABreachTickets(
                        state,
                        escalationItem.getWorkflowStates(),
                        escalationId,
                        escalationItem.getEscalationLevel(),
                        requestInfo);

                List<EscalationTicket> allTicketsForCsv = new ArrayList<>(newBreaches);
                List<EscalationTicket> previouslyOpen = slaBreachService.findPreviouslyEscalatedStillOpenTickets(
                        state,
                        escalationItem.getWorkflowStates(),
                        escalationId,
                        escalationItem.getEscalationLevel(),
                        requestInfo);
                allTicketsForCsv.addAll(previouslyOpen);

                String csvContent = csvGenerationService.generateEscalationCsv(allTicketsForCsv);
                String stateName = commonUtility.getStateDisplayName(state);
                String csvFileName = csvGenerationService.generateCsvFileName(
                        "daily", escalationItem.getEscalationLevel(), stateName);
                String csvFileStoreId = uploadCsvToFileStore(csvContent, csvFileName, "in", requestInfo);

                String downloadUrl = buildDownloadUrl(csvFileStoreId);

                if (!newBreaches.isEmpty()) {
                    elasticsearchEscalationService.updateEscalationsForTickets(
                            newBreaches, escalationId, escalationItem.getEscalationLevel());
                }

                for (User user : users) {
                    if (user.getEmailId() == null || user.getEmailId().isBlank()) {
                        log.warn("State POC user {} has no email, skipping", user.getName());
                        continue;
                    }

                    DailyStatePocSummary summary = dailyStatePocSummaryBuilder.buildSummary(
                            state, user.getName(), escalationItem, escalationId, requestInfo);
                    String emailSubject = dailyStatePocEmailService.generateEmailSubject(summary);
                    String emailBody = dailyStatePocEmailService.generateEmailHtml(summary, downloadUrl);

                    List<String> csvFileStoreIds = csvFileStoreId != null ? List.of(csvFileStoreId) : new ArrayList<>();
                    List<String> csvFileNames = csvFileStoreId != null ? List.of(csvFileName) : new ArrayList<>();
                    sendEmailViaKafka(user, emailSubject, emailBody, csvFileStoreIds, csvFileNames, tenantId);
                }

                escalationStatusService.publishSuccessStatus("daily", escalationId, tenantId, recipientRoleName);
                log.info("Completed daily State POC escalation for state: {} with {} new and {} previously open tickets",
                        state, newBreaches.size(), previouslyOpen.size());

            } catch (Exception e) {
                log.error("Error processing daily State POC escalation for tenant: {}", tenantId, e);
                escalationStatusService.publishFailureStatus("daily", escalationId, tenantId, recipientRoleName, e.getMessage());
            }
        }
    }

    private void processDailySeniorProgramManagerEscalation(RequestInfo requestInfo,
                                                          EscalationRecipient escalationRecipient,
                                                          List<String> activeTenantIds) {
        String escalationId = escalationRecipient.getId().toString();
        String recipientRoleName = escalationRecipient.getRecipientRole();
        List<EscalationRoleEscalationItem> items = escalationRecipient.getEscalations();

        if (items == null || items.isEmpty()) {
            log.warn("No escalation items for daily SPM recipient: {}", escalationId);
            return;
        }

        items.sort((a, b) -> levelOrder(a.getEscalationLevel()) - levelOrder(b.getEscalationLevel()));
        EscalationRoleEscalationItem escalationItem = items.get(0);
        Map<String, String> activeTenantIdsName = masterDataService.getActiveTenantIdsName(requestInfo);

        for (String tenantId : activeTenantIds) {
            String state = activeTenantIdsName.get(tenantId);
            if (state == null || state.isBlank()) {
                continue;
            }

            try {
                List<User> users = userService.searchUsersByRoleAndBoundaryCode(
                        requestInfo, state, List.of(recipientRoleName));
                if (users.isEmpty()) {
                    escalationStatusService.publishSuccessStatus("daily", escalationId, tenantId, recipientRoleName);
                    continue;
                }

                List<EscalationTicket> newBreaches = slaBreachService.findSLABreachTickets(
                        state, escalationItem.getWorkflowStates(), escalationId,
                        escalationItem.getEscalationLevel(), requestInfo);
                List<EscalationTicket> previouslyOpen = slaBreachService.findPreviouslyEscalatedStillOpenTickets(
                        state, escalationItem.getWorkflowStates(), escalationId,
                        escalationItem.getEscalationLevel(), requestInfo);

                List<EscalationTicket> allTickets = new ArrayList<>(newBreaches);
                allTickets.addAll(previouslyOpen);
                String csvContent = csvGenerationService.generateEscalationCsv(allTickets);
                String csvFileName = csvGenerationService.generateCsvFileName(
                        "daily", escalationItem.getEscalationLevel(), commonUtility.getStateDisplayName(state));
                String csvFileStoreId = uploadCsvToFileStore(csvContent, csvFileName, "in", requestInfo);
                String downloadUrl = buildDownloadUrl(csvFileStoreId);

                if (!newBreaches.isEmpty()) {
                    elasticsearchEscalationService.updateEscalationsForTickets(
                            newBreaches, escalationId, escalationItem.getEscalationLevel());
                }

                for (User user : users) {
                    if (user.getEmailId() == null || user.getEmailId().isBlank()) {
                        continue;
                    }
                    DailySeniorProgramManagerSummary summary = dailySeniorProgramManagerSummaryBuilder.buildSummary(
                            state, user.getName(), escalationItem, escalationId, requestInfo);
                    sendEmailViaKafka(user,
                            dailySeniorProgramManagerEmailService.generateEmailSubject(summary),
                            dailySeniorProgramManagerEmailService.generateEmailHtml(summary, downloadUrl),
                            csvFileStoreId != null ? List.of(csvFileStoreId) : new ArrayList<>(),
                            csvFileStoreId != null ? List.of(csvFileName) : new ArrayList<>(),
                            tenantId);
                }

                escalationStatusService.publishSuccessStatus("daily", escalationId, tenantId, recipientRoleName);
            } catch (Exception e) {
                log.error("Error processing daily SPM escalation for tenant: {}", tenantId, e);
                escalationStatusService.publishFailureStatus("daily", escalationId, tenantId, recipientRoleName, e.getMessage());
            }
        }
    }

    private void processDailyProcurementEscalation(RequestInfo requestInfo, EscalationRecipient escalationRecipient) {
        String escalationId = escalationRecipient.getId().toString();
        String recipientRoleName = escalationRecipient.getRecipientRole();
        List<EscalationRoleEscalationItem> items = escalationRecipient.getEscalations();
        int triggerDelayHours = escalationRecipient.getTriggerDelayHours() != null
                ? escalationRecipient.getTriggerDelayHours() : 48;

        if (items == null || items.isEmpty()) {
            log.warn("No escalation items for daily Procurement recipient: {}", escalationId);
            return;
        }

        items.sort((a, b) -> levelOrder(a.getEscalationLevel()) - levelOrder(b.getEscalationLevel()));
        EscalationRoleEscalationItem escalationItem = items.get(0);

        try {
            List<User> users = userService.searchUsersByRoleAndBoundaryCode(
                    requestInfo, "India", List.of(recipientRoleName));
            if (users.isEmpty()) {
                log.warn("No Procurement users found with boundary India");
                escalationStatusService.publishSuccessStatus("daily", escalationId, "in", recipientRoleName);
                return;
            }

            List<EscalationTicket> newBreaches = slaBreachService.findProcurementEligibleTickets(
                    escalationItem.getWorkflowStates(), escalationId,
                    escalationItem.getEscalationLevel(), triggerDelayHours, requestInfo);
            List<EscalationTicket> previouslyOpen = slaBreachService.findProcurementPreviouslyOpenTickets(
                    escalationItem.getWorkflowStates(), escalationId,
                    escalationItem.getEscalationLevel(), triggerDelayHours, requestInfo);

            List<EscalationTicket> allTickets = new ArrayList<>(newBreaches);
            allTickets.addAll(previouslyOpen);
            String csvContent = csvGenerationService.generateEscalationCsv(allTickets);
            String csvFileName = csvGenerationService.generateCsvFileName(
                    "daily", escalationItem.getEscalationLevel(), "AllStates");
            String csvFileStoreId = uploadCsvToFileStore(csvContent, csvFileName, "in", requestInfo);
            String downloadUrl = buildDownloadUrl(csvFileStoreId);

            if (!newBreaches.isEmpty()) {
                elasticsearchEscalationService.updateEscalationsForTickets(
                        newBreaches, escalationId, escalationItem.getEscalationLevel());
            }

            for (User user : users) {
                if (user.getEmailId() == null || user.getEmailId().isBlank()) {
                    continue;
                }
                DailyProcurementSummary summary = dailyProcurementSummaryBuilder.buildSummary(
                        user.getName(), escalationItem, escalationId, triggerDelayHours, requestInfo);
                sendEmailViaKafka(user,
                        dailyProcurementEmailService.generateEmailSubject(summary),
                        dailyProcurementEmailService.generateEmailHtml(summary, downloadUrl),
                        csvFileStoreId != null ? List.of(csvFileStoreId) : new ArrayList<>(),
                        csvFileStoreId != null ? List.of(csvFileName) : new ArrayList<>(),
                        "in");
            }

            escalationStatusService.publishSuccessStatus("daily", escalationId, "in", recipientRoleName);
        } catch (Exception e) {
            log.error("Error processing daily Procurement escalation", e);
            escalationStatusService.publishFailureStatus("daily", escalationId, "in", recipientRoleName, e.getMessage());
        }
    }

    private void processWeeklyEscalationRecipient(RequestInfo requestInfo,
                                                     EscalationRecipient recipient,
                                                     List<String> activeTenantIds,
                                                     Map<String, String> activeTenantIdsName) {
        String templateType = recipient.getTemplateType();
        String role = recipient.getRecipientRole();

        if (EscalationTemplateType.WEEKLY_LEADERSHIP.equals(templateType)
                || EscalationTemplateType.WEEKLY_PROCUREMENT.equals(templateType)) {
            Set<String> allStateCodes = activeTenantIds.stream()
                    .map(activeTenantIdsName::get)
                    .filter(s -> s != null && !s.isBlank())
                    .collect(Collectors.toSet());
            WeeklyEscalationAnalytics analytics = weeklyEscalationAnalyticsService.buildAnalytics(allStateCodes, requestInfo);
            String downloadUrl = uploadWeeklyCsv(allStateCodes, requestInfo);

            List<User> users = userService.searchUsersByRoleAndBoundaryCode(requestInfo, "India", List.of(role));
            for (User user : users) {
                if (user.getEmailId() == null || user.getEmailId().isBlank()) {
                    continue;
                }
                sendWeeklyEmailForTemplate(templateType, analytics, user, downloadUrl);
            }
            return;
        }

        if (EscalationTemplateType.WEEKLY_SENIOR_PROGRAM_MANAGER.equals(templateType)) {
            Map<String, List<EscalationRecipient>> recipientsByEmail = new HashMap<>();
            for (String tenantId : activeTenantIds) {
                String state = activeTenantIdsName.get(tenantId);
                if (state == null || state.isBlank()) {
                    continue;
                }
                List<User> users = userService.searchUsersByRoleAndBoundaryCode(requestInfo, state, List.of(role));
                for (User user : users) {
                    if (user.getEmailId() != null && !user.getEmailId().isBlank()) {
                        recipientsByEmail.computeIfAbsent(user.getEmailId(), k -> new ArrayList<>()).add(recipient);
                    }
                }
            }

            for (Map.Entry<String, List<EscalationRecipient>> entry : recipientsByEmail.entrySet()) {
                String emailId = entry.getKey();
                Set<String> relevantTenantIds = getRelevantTenantIdsForEmail(
                        requestInfo, emailId, entry.getValue(), activeTenantIds);
                Set<String> stateCodes = relevantTenantIds.stream()
                        .map(activeTenantIdsName::get)
                        .filter(s -> s != null && !s.isBlank())
                        .collect(Collectors.toSet());
                if (stateCodes.isEmpty()) {
                    continue;
                }

                WeeklyEscalationAnalytics analytics = weeklyEscalationAnalyticsService.buildAnalytics(stateCodes, requestInfo);
                String downloadUrl = uploadWeeklyCsv(stateCodes, requestInfo);
                User user = getUserByEmailId(requestInfo, emailId);
                if (user == null) {
                    user = new User();
                    user.setEmailId(emailId);
                    user.setName("Weekly SPM Recipient");
                }
                sendWeeklyEmailForTemplate(templateType, analytics, user, downloadUrl);
            }
        }
    }

    private void sendWeeklyEmailForTemplate(String templateType,
                                            WeeklyEscalationAnalytics analytics,
                                            User user,
                                            String downloadUrl) {
        String subject;
        String body;
        if (EscalationTemplateType.WEEKLY_SENIOR_PROGRAM_MANAGER.equals(templateType)) {
            subject = weeklySeniorProgramManagerEmailService.generateEmailSubject(analytics);
            body = weeklySeniorProgramManagerEmailService.generateEmailHtml(analytics, user.getName(), downloadUrl);
        } else if (EscalationTemplateType.WEEKLY_PROCUREMENT.equals(templateType)) {
            subject = weeklyProcurementEmailService.generateEmailSubject(analytics);
            body = weeklyProcurementEmailService.generateEmailHtml(analytics, user.getName(), downloadUrl);
        } else {
            subject = weeklyLeadershipEmailService.generateEmailSubject(analytics);
            body = weeklyLeadershipEmailService.generateEmailHtml(analytics, user.getName(), downloadUrl);
        }
        sendEmailViaKafka(user, subject, body, new ArrayList<>(), new ArrayList<>(), "in");
    }

    private String uploadWeeklyCsv(Set<String> stateCodes, RequestInfo requestInfo) {
        try {
            String csv = generateConsolidatedWeeklyCsv(stateCodes, requestInfo);
            String fileName = generateCsvFileName();
            String fileStoreId = uploadCsvToFileStore(csv, fileName, "in", requestInfo);
            return buildDownloadUrl(fileStoreId);
        } catch (Exception e) {
            log.error("Failed to upload weekly CSV", e);
            return "#";
        }
    }

    private String buildDownloadUrl(String fileStoreId) {
        if (fileStoreId == null) {
            return "#";
        }
        return commonUtility.generateDownloadUrl(
                fileStoreId, "in",
                consumerConfiguration.getFileStoreBaseUrl(),
                consumerConfiguration.getFileStoreDownloadEndpoint());
    }

    private boolean isWeeklyTemplate(String templateType) {
        return EscalationTemplateType.WEEKLY_SENIOR_PROGRAM_MANAGER.equals(templateType)
                || EscalationTemplateType.WEEKLY_PROCUREMENT.equals(templateType)
                || EscalationTemplateType.WEEKLY_LEADERSHIP.equals(templateType);
    }

    private int levelOrder(String level) {
        if ("LEVEL_ZERO".equals(level)) return 0;
        if ("LEVEL_ONE".equals(level)) return 1;
        if ("LEVEL_TWO".equals(level)) return 2;
        if ("LEVEL_THREE".equals(level)) return 3;
        return 99;
    }

    /**
     * Get user by email ID from user service
     */
    private User getUserByEmailId(RequestInfo requestInfo, String emailId) {
        try {
            // Search for users with this email ID across all active tenants
            List<String> activeTenantIds = masterDataService.fetchActiveTenantIds(requestInfo);
            Map<String, String> activeTenantIdsName = masterDataService.getActiveTenantIdsName(requestInfo);
            for (String tenantId : activeTenantIds) {
                // Search for users with any role in this tenant
                String state = activeTenantIdsName.get(tenantId);
                List<String> allRoles = Arrays.asList(
                        "STATE_POC", "SENIOR_PROGRAM_MANAGER", "PROCUREMENT", "LEADERSHIP", "VENDOR", "ADMIN");
                List<User> users = userService.searchUsersByRoleAndBoundaryCode(requestInfo, state, allRoles);
                
                for (User user : users) {
                    if (emailId.equals(user.getEmailId())) {
                        log.info("Found user: {} for email: {}", user.getName(), emailId);
                        return user;
                    }
                }
            }
            
            // Also check country-level users with boundary "India"
            List<String> allRoles = Arrays.asList(
                    "STATE_POC", "SENIOR_PROGRAM_MANAGER", "PROCUREMENT", "LEADERSHIP", "VENDOR", "ADMIN");
            List<User> countryUsers = userService.searchUsersByRoleAndBoundaryCode(requestInfo, "India", allRoles);
            
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
     * Uses boundary.stateCode to filter tickets since all tickets are now under tenantId "in".
     */
    private String generateConsolidatedWeeklyCsv(Set<String> stateCodes, RequestInfo requestInfo) {
        StringBuilder csv = new StringBuilder();
        appendCsvHeader(csv);

        try {
            List<Map<String, Object>> tickets = elasticSearchClient.fetchRequiredTickets(0, 10000, false);
            log.info("Consolidated CSV: fetched {} tickets from ES, filtering by {} state codes", tickets.size(), stateCodes.size());

            Map<String, Map<String, Object>> facilityAgg = new LinkedHashMap<>();
            int filteredCount = 0;
            for (Map<String, Object> ticket : tickets) {
                Map<String, Object> data = (Map<String, Object>) ticket.get("Data");
                if (data == null) continue;

                // Extract state code from boundary instead of tenantId
                String ticketStateCode = extractBoundaryStateCodeFromData(data);
                if (ticketStateCode == null || !isStateCodeInScope(ticketStateCode, stateCodes)) {
                    continue;
                }
                filteredCount++;

                String facilityName = resolveFacilityName(data);
                String ninOrHfr = getStringValue(data, "nin_hfr_id");
                // Use state code for state identification
                String district = getStringValue(data, "district");
                String block = getStringValue(data, "block");
                String hfType = resolveHfType(data);
                String vendor = resolveVendor(data);
                String status = extractApplicationStatus(data);
                boolean isClosed = isClosed(status);
                boolean isFunctional = "FUNCTIONAL".equalsIgnoreCase(getStringValue(data, "systemFunctional"));

                Map<String, Object> row = getOrCreateFacilityRow(facilityAgg, facilityName, ninOrHfr, ticketStateCode, district, block, hfType, vendor);
                incrementCounts(row, isClosed, isFunctional);
            }
            
            log.info("Consolidated CSV: filtered to {} tickets matching state codes", filteredCount);

            facilityAgg.values().forEach(r -> appendCsvRow(csv, r));

        } catch (Exception e) {
            log.error("Error generating consolidated weekly CSV", e);
        }

        return csv.toString();
    }

    private void appendCsvHeader(StringBuilder csv) {
        csv.append("\"Health Facility\",\"NIN OR HFR\",\"Solar Working\",\"State\",\"District\",\"Block\",\"Health Facility Type\",\"Mapped Vendor\",\"No of Ticket\",\"Open Ticket\",\"Closed Ticket\"\r\n");
    }

    /**
     * Check if state code is in scope (matches or starts with any of the target state codes)
     * Handles formats like "india_sikkim", "india_karnataka", etc.
     */
    private boolean isStateCodeInScope(String ticketStateCode, Set<String> targetStateCodes) {
        if (ticketStateCode == null || ticketStateCode.isEmpty()) return false;
        for (String targetStateCode : targetStateCodes) {
            if (ticketStateCode.equalsIgnoreCase(targetStateCode) || 
                ticketStateCode.startsWith(targetStateCode + ".") ||
                targetStateCode.startsWith(ticketStateCode + ".")) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * Extract state code from boundary in ticket data
     * Returns null if boundary or stateCode is not found
     */
    private String extractBoundaryStateCodeFromData(Map<String, Object> data) {
        Map<String, Object> incident = (Map<String, Object>) data.get("incident");
        if (incident == null) return null;
        
        Map<String, Object> boundary = (Map<String, Object>) incident.get("boundary");
        if (boundary == null) return null;
        
        return getStringValue(boundary, "stateCode");
    }

    private String resolveFacilityName(Map<String, Object> data) {
        String name = getStringValue(data, "tenantId_localized");
        return name.isEmpty() ? getStringValue(data, "tenantId") : name;
    }

    private String resolveHfType(Map<String, Object> data) {
       Map<String, Object> incident = (Map<String, Object>) data.get("incident");
        if (incident != null) {
            // Check localized version first (if available)
            String type = getStringValue(incident, "phcSubType_localized");
            if (!type.isEmpty()) return type;
            
            // Use phcSubType from incident (same as daily email - e.g., "Primary Health Center")
            type = getStringValue(incident, "phcSubType");
            if (!type.isEmpty()) return type;
        }
        
        return "";
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
                                                       String facility, String nin, String stateCode,
                                                       String district, String block, String type, String vendor) {
        // Use state code for facility key to uniquely identify facilities
        String key = facility + "|" + district + "|" + block + "|" + stateCode;
        return agg.computeIfAbsent(key, k -> {
            Map<String, Object> m = new HashMap<>();
            m.put("facility", facility);
            m.put("nin", nin);
            // Convert state code to display name (e.g., "india_sikkim" -> "Sikkim")
            m.put("state", commonUtility.getStateDisplayName(stateCode));
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
        log.trace("Health check endpoint called");
        log.debug("Escalation service health check successful");
        return ResponseEntity.ok("Escalation service is running");
    }
    
}