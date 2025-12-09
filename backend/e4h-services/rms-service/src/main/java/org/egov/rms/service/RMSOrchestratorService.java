package org.egov.rms.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.egov.common.contract.request.RequestInfo;
import org.egov.common.contract.request.Role;
import org.egov.common.contract.request.User;
import org.egov.rms.config.RMSConfiguration;
import org.egov.rms.model.Alert;
import org.egov.rms.model.IMServiceRequest;
import org.egov.rms.model.RMSFacilityData;
import org.egov.rms.repository.AlertRepository;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class RMSOrchestratorService {

    private final DataCollectorService dataCollectorService;
    private final RuleEngineService ruleEngineService;
    private final DeduplicationManager deduplicationManager;
    private final PayloadGenerator payloadGenerator;
    private final SauraEmitraConnector sauraEmitraConnector;
    private final RMSConfiguration config;
    private final AlertRepository alertRepository;

    /**
     * Executes the complete RMS workflow: collect data, apply rules, deduplicate, generate tickets
     * Always processes all alert types: collects fresh data, creates alerts, and generates tickets
     * Supports all alert types:
     * - Panel low generation - center_details/graph
     * - Inverter no signal - centerDatas/get
     * - Inverter high voltage - center_details/graph
     * - Battery voltage = 0 - center_details/graph
     * - Battery deep discharge/overcharge - center_details/graph
     * - Grid voltage variation - center_details/graph
     */
    public void executeWorkflow() {
        log.info("Starting RMS workflow execution");
        
        try {
            // Create system RequestInfo
            RequestInfo requestInfo = createSystemRequestInfo();
            
            // ALWAYS process all alert types - collect fresh data, create alerts, and generate tickets
            log.info("Processing all alert types with fresh data collection");
            
            // Process all alert types (each handles its own data collection, alert creation, and ticket generation)
            processInverterNoSignalAlerts(requestInfo);
            processInverterHighVoltageAlerts(requestInfo);
            processPanelAlerts(requestInfo);
            processBatteryAlerts(requestInfo);
            processBatteryDeepDischargeAlerts(requestInfo);
            processGridAlerts(requestInfo);
            
            log.info("Completed RMS workflow execution - all alert types processed");
        } catch (Exception e) {
            log.error("Error during RMS workflow execution", e);
        }
    }

    /**
     * Processes panel-level alerts
     */
    private void processPanelAlerts(RequestInfo requestInfo) {
        log.info("Processing panel-level alerts");
        try {
            // Sync data from RMS servers - ENABLED: data collection enabled for trigger endpoint
            List<RMSFacilityData> facilities = dataCollectorService.collectPanelData();
            log.info("Collected {} facilities from RMS API for panel analysis", facilities.size());
            
            // Apply rule engine to create alerts
            List<Alert> alerts = ruleEngineService.applyPanelRules(facilities);
            log.info("Generated {} panel alerts from rule engine", alerts.size());
            
            // Apply deduplication to prevent duplicate tickets
            List<Alert> uniqueAlerts = deduplicationManager.deduplicateAlerts(alerts);
            createTickets(uniqueAlerts, requestInfo, "Panel Low Generation");
        } catch (Exception e) {
            log.error("Error processing panel alerts", e);
        }
    }

    /**
     * Processes inverter no-signal alerts
     * NOTE: For this trigger, we enable data collection from API since API already filters for inactive devices
     * No additional rule engine filtering needed - API handles the filtering
     */
    private void processInverterNoSignalAlerts(RequestInfo requestInfo) {
        log.info("Processing inverter no-signal alerts");
        try {
            // Enable data collection from RMS servers for this trigger
            // API already filters for inactive devices, so we collect and create alerts directly
            List<RMSFacilityData> facilities = dataCollectorService.collectInverterNoSignalData();
            log.info("Collected {} facilities with no signal from RMS API", facilities.size());
            
            // Apply rule engine to create alerts (rule engine just creates alerts for all facilities since API already filtered)
            List<Alert> alerts = ruleEngineService.applyInverterRules(facilities, true);
            log.info("Generated {} inverter no-signal alerts", alerts.size());
            
            // Apply deduplication to prevent duplicate tickets
            List<Alert> uniqueAlerts = deduplicationManager.deduplicateAlerts(alerts);
            createTickets(uniqueAlerts, requestInfo, "Inverter No Signal");
        } catch (Exception e) {
            log.error("Error processing inverter no-signal alerts", e);
        }
    }

    /**
     * Processes inverter high voltage alerts
     */
    private void processInverterHighVoltageAlerts(RequestInfo requestInfo) {
        log.info("Processing inverter high voltage alerts");
        try {
            // Sync data from RMS servers - ENABLED: data collection enabled for trigger endpoint
            List<RMSFacilityData> facilities = dataCollectorService.collectInverterHighVoltageData();
            log.info("Collected {} facilities from RMS API for inverter high voltage analysis", facilities.size());
            
            // Apply rule engine to create alerts
            List<Alert> alerts = ruleEngineService.applyInverterRules(facilities, false);
            log.info("Generated {} inverter high voltage alerts from rule engine", alerts.size());
            
            // Apply deduplication to prevent duplicate tickets
            List<Alert> uniqueAlerts = deduplicationManager.deduplicateAlerts(alerts);
            createTickets(uniqueAlerts, requestInfo, "Inverter High Voltage");
        } catch (Exception e) {
            log.error("Error processing inverter high voltage alerts", e);
        }
    }

    /**
     * Processes battery alerts (voltage = 0)
     */
    private void processBatteryAlerts(RequestInfo requestInfo) {
        log.info("Processing battery alerts (voltage = 0)");
        try {
            // Sync data from RMS servers - ENABLED: data collection enabled for trigger endpoint
            List<RMSFacilityData> facilities = dataCollectorService.collectBatteryVoltageZeroData();
            log.info("Collected {} facilities from RMS API for battery voltage zero analysis", facilities.size());
            
            // Apply rule engine to create alerts
            List<Alert> alerts = ruleEngineService.applyBatteryRules(facilities);
            log.info("Generated {} battery alerts from rule engine", alerts.size());
            
            // Apply deduplication to prevent duplicate tickets
            List<Alert> uniqueAlerts = deduplicationManager.deduplicateAlerts(alerts);
            createTickets(uniqueAlerts, requestInfo, "Battery Voltage Zero");
        } catch (Exception e) {
            log.error("Error processing battery alerts", e);
        }
    }

    /**
     * Processes battery deep discharge/overcharge alerts
     */
    private void processBatteryDeepDischargeAlerts(RequestInfo requestInfo) {
        log.info("Processing battery deep discharge/overcharge alerts");
        try {
            // Sync data from RMS servers - ENABLED: data collection enabled for trigger endpoint
            List<RMSFacilityData> facilities = dataCollectorService.collectBatteryDeepDischargeData();
            log.info("Collected {} facilities from RMS API for battery deep discharge analysis", facilities.size());
            
            // Apply rule engine to create alerts
            List<Alert> alerts = ruleEngineService.applyBatteryRules(facilities);
            log.info("Generated {} battery deep discharge/overcharge alerts from rule engine", alerts.size());
            
            // Apply deduplication to prevent duplicate tickets
            List<Alert> uniqueAlerts = deduplicationManager.deduplicateAlerts(alerts);
            createTickets(uniqueAlerts, requestInfo, "Battery Deep Discharge");
        } catch (Exception e) {
            log.error("Error processing battery deep discharge alerts", e);
        }
    }

    /**
     * Processes grid alerts
     */
    private void processGridAlerts(RequestInfo requestInfo) {
        log.info("Processing grid alerts");
        try {
            // Sync data from RMS servers - ENABLED: data collection enabled for trigger endpoint
            List<RMSFacilityData> facilities = dataCollectorService.collectGridVoltageData();
            log.info("Collected {} facilities from RMS API for grid voltage analysis", facilities.size());
            
            // Apply rule engine to create alerts
            List<Alert> alerts = ruleEngineService.applyGridRules(facilities);
            log.info("Generated {} grid alerts from rule engine", alerts.size());
            
            // Apply deduplication to prevent duplicate tickets
            List<Alert> uniqueAlerts = deduplicationManager.deduplicateAlerts(alerts);
            createTickets(uniqueAlerts, requestInfo, "Grid Voltage Variation");
        } catch (Exception e) {
            log.error("Error processing grid alerts", e);
        }
    }

    /**
     * Creates tickets for alerts
     * Limits the number of tickets created per trigger if testing limit is configured
     * Each trigger independently creates up to the configured limit
     * Skips alerts that already have tickets to prevent duplicates
     * NOTE: Testing limit is NOT applied for "All Alerts from History" trigger to process all alerts
     */
    private void createTickets(List<Alert> alerts, RequestInfo requestInfo, String triggerName) {
        log.info("Creating tickets for {} alerts (trigger: {})", alerts.size(), triggerName);
        
        // Apply testing limit if configured (for testing purposes)
        // Skip limit for "All Alerts from History" trigger to process all alerts
        List<Alert> alertsToProcess = alerts;
        if (!"All Alerts from History".equals(triggerName) && 
            config.getMaxTicketsPerTrigger() != null && config.getMaxTicketsPerTrigger() > 0) {
            int limit = config.getMaxTicketsPerTrigger();
            if (alerts.size() > limit) {
                alertsToProcess = alerts.subList(0, limit);
                log.info("Testing mode: Limiting ticket creation for '{}' trigger to {} tickets (out of {} total alerts)",
                        triggerName, limit, alerts.size());
            }
        } else if ("All Alerts from History".equals(triggerName)) {
            log.info("Processing all {} alerts from active_alerts (testing limit disabled for this trigger)", alerts.size());
        }
        
        int successCount = 0;
        int failureCount = 0;
        int skippedCount = 0;
        
        for (Alert alert : alertsToProcess) {
            try {
                int count = 0;
                if (count > 10) {
                    break;
                }

                // Check if there's an open ticket in eg_incident_v2
                // If ticket is closed or doesn't exist, we allow creating a new ticket
                // Note: We don't skip alerts with ticket_id set because they might have closed tickets
                if (alertRepository.hasOpenTicket(alert.getFacilityId(), alert.getAlertType(), alert.getAlertSubType())) {
                    log.info("Skipping alert {} - open ticket already exists in eg_incident_v2 for facility: {}, type: {}, subType: {}", 
                            alert.getId(), alert.getFacilityId(), alert.getAlertType(), alert.getAlertSubType());
                    skippedCount++;
                    continue;
                }
                
                // If alert has a ticket_id but ticket is closed, log that we're creating a new ticket
                if (alert.getTicketId() != null && !alert.getTicketId().isEmpty()) {
                    log.info("Alert {} has closed ticket {} - creating new ticket for facility: {}, type: {}, subType: {}", 
                            alert.getId(), alert.getTicketId(), alert.getFacilityId(), alert.getAlertType(), alert.getAlertSubType());
                }
                
                log.info("Creating ticket for alert: {} (facility: {}, type: {}, subType: {})", 
                        alert.getId(), alert.getFacilityId(), alert.getAlertType(), alert.getAlertSubType());
                IMServiceRequest ticketRequest = payloadGenerator.generateTicketPayload(alert, requestInfo);
                
                if (ticketRequest != null) {
                    boolean success = sauraEmitraConnector.createTicket(alert, ticketRequest);
                    if (success) {
                        successCount++;
                    } else {
                        failureCount++;
                    }
                } else {
                    log.warn("Failed to generate ticket payload for alert: {}", alert.getId());
                    failureCount++;
                }
                count++;
            } catch (Exception e) {
                log.error("Error creating ticket for alert: {}", alert.getId(), e);
                failureCount++;
            }
        }
        
        log.info("Ticket creation completed for '{}' trigger: {} succeeded, {} failed, {} skipped (processed {} out of {} alerts)", 
                triggerName, successCount, failureCount, skippedCount, alertsToProcess.size(), alerts.size());
    }

    /**
     * Creates system RequestInfo for automated operations
     */
    private RequestInfo createSystemRequestInfo() {
        List<Role> roles = new ArrayList<>();
        Role role1 = Role.builder()
                .name("Complainant")
                .code("COMPLAINANT")
                .tenantId("in")
                .build();
        roles.add(role1);
        Role role2 = Role.builder()
                .name("Employee")
                .code("EMPLOYEE")
                .tenantId("in")
                .build();
        roles.add(role2);
        Role role3 = Role.builder()
                .name("Complaint Assessor")
                .code("COMPLAINT_ASSESSOR")
                .tenantId("in")
                .build();
        roles.add(role3);
        Role role4 = Role.builder()
                .name("Complaint facilitator 2")
                .code("COMPLAINT_FACILITATOR_2")
                .tenantId("in")
                .build();
        roles.add(role4);
        Role role5 = Role.builder()
                .name("Super User")
                .code("SUPERUSER")
                .tenantId("in")
                .build();
        roles.add(role5);
        Role role6 = Role.builder()
                .name("RMS")
                .code("RMS")
                .tenantId("in")
                .build();
        roles.add(role6);
        
        User user = User.builder()
                .id(14301L)
                .uuid("fb022833-743d-43cb-adfa-312fbd13f438")
                .userName("rms_user")
                .name("nikhil")
                .mobileNumber("9901224634")
                .emailId("crm@gmail.com")
                .type("EMPLOYEE")
                .tenantId("in")
                .roles(roles)
                .build();

        return RequestInfo.builder()
                .apiId("Rainmaker")
                .authToken("fa70bd35-8036-4b35-a1b4-85e85c254b61")
                .userInfo(user)
                .msgId("1764924656583|en_IN")
                .build();
    }
}

