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
     * Currently supports 4 working endpoints:
     * - Panel low generation - center_details/graph (WORKING)
     * - Inverter no signal - centerDatas/get (WORKING)
     * - Inverter high voltage - center_details/graph (WORKING)
     * - Battery voltage = 0 - center_details/graph (WORKING)
     */
    public void executeWorkflow() {
        log.info("Starting RMS workflow execution");
        
        try {
            // Create system RequestInfo
            RequestInfo requestInfo = createSystemRequestInfo();
            
            // Get ALL alerts from alert_history that don't have tickets
            List<Alert> allAlerts = alertRepository.getAllAlertsFromHistoryWithoutTickets();
            log.info("Found {} total alerts from history without tickets", allAlerts.size());
            
            if (allAlerts.isEmpty()) {
                log.info("No alerts found in history without tickets. Processing by type for backward compatibility...");
                // Fallback to processing by type for backward compatibility
                processInverterNoSignalAlerts(requestInfo);
                processInverterHighVoltageAlerts(requestInfo);
                processPanelAlerts(requestInfo);
                processBatteryAlerts(requestInfo);
                processBatteryDeepDischargeAlerts(requestInfo);
                processGridAlerts(requestInfo);
            } else {
                // Process all alerts together
                log.info("Processing {} alerts from history", allAlerts.size());
                // Apply deduplication to prevent duplicate tickets
                List<Alert> uniqueAlerts = deduplicationManager.deduplicateAlerts(allAlerts);
                log.info("After deduplication: {} unique alerts to process", uniqueAlerts.size());
                createTickets(uniqueAlerts, requestInfo, "All Alerts from History");
            }
            
            log.info("Completed RMS workflow execution");
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
            // Sync data from RMS servers - COMMENTED OUT: sync disabled for trigger endpoint
            // List<RMSFacilityData> facilities = dataCollectorService.collectPanelData();
            // Instead, get alerts from alert_history that don't have tickets
            List<Alert> alerts = alertRepository.getAlertsFromHistoryWithoutTickets(
                    Alert.AlertType.PANEL, Alert.AlertSubType.LOW_GENERATION);
            log.info("Found {} panel alerts from history without tickets", alerts.size());
            // Apply deduplication to prevent duplicate tickets
            List<Alert> uniqueAlerts = deduplicationManager.deduplicateAlerts(alerts);
            createTickets(uniqueAlerts, requestInfo, "Panel Low Generation");
        } catch (Exception e) {
            log.error("Error processing panel alerts", e);
        }
    }

    /**
     * Processes inverter no-signal alerts
     */
    private void processInverterNoSignalAlerts(RequestInfo requestInfo) {
        log.info("Processing inverter no-signal alerts");
        try {
            // Sync data from RMS servers - COMMENTED OUT: sync disabled for trigger endpoint
            // List<RMSFacilityData> facilities = dataCollectorService.collectInverterNoSignalData();
            // Instead, get alerts from alert_history that don't have tickets
            List<Alert> alerts = alertRepository.getAlertsFromHistoryWithoutTickets(
                    Alert.AlertType.INVERTER, Alert.AlertSubType.SHUTDOWN);
            log.info("Found {} inverter no-signal alerts from history without tickets", alerts.size());
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
            // Sync data from RMS servers - COMMENTED OUT: sync disabled for trigger endpoint
            // List<RMSFacilityData> facilities = dataCollectorService.collectInverterHighVoltageData();
            // Instead, get alerts from alert_history that don't have tickets
            List<Alert> alerts = alertRepository.getAlertsFromHistoryWithoutTickets(
                    Alert.AlertType.INVERTER, Alert.AlertSubType.HIGH_VOLTAGE);
            log.info("Found {} inverter high voltage alerts from history without tickets", alerts.size());
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
            // Sync data from RMS servers - COMMENTED OUT: sync disabled for trigger endpoint
            // List<RMSFacilityData> facilities = dataCollectorService.collectBatteryVoltageZeroData();
            // Instead, get alerts from alert_history that don't have tickets
            List<Alert> alerts = alertRepository.getAlertsFromHistoryWithoutTickets(
                    Alert.AlertType.BATTERY, Alert.AlertSubType.BURNT_DISCONNECTED);
            log.info("Found {} battery alerts from history without tickets", alerts.size());
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
            // Sync data from RMS servers - COMMENTED OUT: sync disabled for trigger endpoint
            // List<RMSFacilityData> facilities = dataCollectorService.collectBatteryDeepDischargeData();
            // Instead, get alerts from alert_history that don't have tickets (both deep discharge and overcharge)
            List<Alert> deepDischargeAlerts = alertRepository.getAlertsFromHistoryWithoutTickets(
                    Alert.AlertType.BATTERY, Alert.AlertSubType.DEEP_DISCHARGING);
            List<Alert> overchargeAlerts = alertRepository.getAlertsFromHistoryWithoutTickets(
                    Alert.AlertType.BATTERY, Alert.AlertSubType.OVERCHARGING);
            List<Alert> alerts = new ArrayList<>();
            alerts.addAll(deepDischargeAlerts);
            alerts.addAll(overchargeAlerts);
            log.info("Found {} battery deep discharge/overcharge alerts from history without tickets", alerts.size());
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
            // Sync data from RMS servers - COMMENTED OUT: sync disabled for trigger endpoint
            // List<RMSFacilityData> facilities = dataCollectorService.collectGridVoltageData();
            // Instead, get alerts from alert_history that don't have tickets (both low and high voltage)
            List<Alert> lowVoltageAlerts = alertRepository.getAlertsFromHistoryWithoutTickets(
                    Alert.AlertType.GRID, Alert.AlertSubType.VOLTAGE_VARIATION_LOW);
            List<Alert> highVoltageAlerts = alertRepository.getAlertsFromHistoryWithoutTickets(
                    Alert.AlertType.GRID, Alert.AlertSubType.VOLTAGE_VARIATION_HIGH);
            List<Alert> alerts = new ArrayList<>();
            alerts.addAll(lowVoltageAlerts);
            alerts.addAll(highVoltageAlerts);
            log.info("Found {} grid alerts from history without tickets", alerts.size());
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
     */
    private void createTickets(List<Alert> alerts, RequestInfo requestInfo, String triggerName) {
        log.info("Creating tickets for {} alerts (trigger: {})", alerts.size(), triggerName);
        
        // Apply testing limit if configured (for testing purposes)
        // Each trigger independently creates up to the configured limit
        List<Alert> alertsToProcess = alerts;
        if (config.getMaxTicketsPerTrigger() != null && config.getMaxTicketsPerTrigger() > 0) {
            int limit = config.getMaxTicketsPerTrigger();
            if (alerts.size() > limit) {
                alertsToProcess = alerts.subList(0, limit);
                log.info("Testing mode: Limiting ticket creation for '{}' trigger to {} tickets (out of {} total alerts)", 
                        triggerName, limit, alerts.size());
            }
        }
        
        int successCount = 0;
        int failureCount = 0;
        int skippedCount = 0;
        
        for (Alert alert : alertsToProcess) {
            try {
                // Double-check if alert already has a ticket - skip to prevent duplicates
                // (This should already be filtered by deduplication, but adding as safety check)
                if (alert.getTicketId() != null && !alert.getTicketId().isEmpty()) {
                    log.info("Skipping alert {} - already has ticket_id: {}", alert.getId(), alert.getTicketId());
                    skippedCount++;
                    continue;
                }
                
                if (alertRepository.hasExistingTicket(alert.getFacilityId(), alert.getAlertType(), alert.getAlertSubType())) {
                    log.info("Skipping alert {} - ticket already exists in active_alerts for facility: {}, type: {}, subType: {}", 
                            alert.getId(), alert.getFacilityId(), alert.getAlertType(), alert.getAlertSubType());
                    skippedCount++;
                    continue;
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
                .tenantId("pg")
                .build();
        roles.add(role1);
        Role role2 = Role.builder()
                .name("Employee")
                .code("EMPLOYEE")
                .tenantId("pg")
                .build();
        roles.add(role2);
        Role role3 = Role.builder()
                .name("Complaint Assessor")
                .code("COMPLAINT_ASSESSOR")
                .tenantId("pg")
                .build();
        roles.add(role3);
        Role role4 = Role.builder()
                .name("Complaint facilitator 2")
                .code("COMPLAINT_FACILITATOR_2")
                .tenantId("pg")
                .build();
        roles.add(role4);
        Role role5 = Role.builder()
                .name("Super User")
                .code("SUPERUSER")
                .tenantId("pg")
                .build();
        roles.add(role5);
        User user = User.builder()
                .id(95L)
                .uuid(config.getSystemUserUuid())
                .userName("7346864311")
                .name("nikhil")
                .type("EMPLOYEE")
                .tenantId("pg")
                .emailId("crm@gmail.com")
                .roles(roles)
                .build();

        return RequestInfo.builder()
                .authToken("52e344c6-4649-48c5-a188-98b3dc0c7e93")
                .userInfo(user)
                .build();
    }
}

