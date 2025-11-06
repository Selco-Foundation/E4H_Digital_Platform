package org.egov.rms.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.egov.common.contract.request.RequestInfo;
import org.egov.common.contract.request.User;
import org.egov.rms.config.RMSConfiguration;
import org.egov.rms.model.Alert;
import org.egov.rms.model.IMServiceRequest;
import org.egov.rms.model.RMSFacilityData;
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

    /**
     * Executes the complete RMS workflow: collect data, apply rules, deduplicate, generate tickets
     * Currently supports only 1 working endpoint:
     * - Inverter no signal - centerDatas/get (WORKING)
     * 
     * Note: Panel data endpoint (center_details/graph) is currently not working
     */
    public void executeWorkflow() {
        log.info("Starting RMS workflow execution");
        
        try {
            // Create system RequestInfo
            RequestInfo requestInfo = createSystemRequestInfo();
            
            // Collect inverter data (no signal) and apply rules - ONLY WORKING ENDPOINT
            processInverterNoSignalAlerts(requestInfo);
            
            // TODO: Re-enable once RMS team fixes the endpoints
            // processPanelAlerts(requestInfo);
            // processInverterHighVoltageAlerts(requestInfo);
            // processBatteryAlerts(requestInfo);
            // processGridAlerts(requestInfo);
            
            log.info("Completed RMS workflow execution");
        } catch (Exception e) {
            log.error("Error during RMS workflow execution", e);
        }
    }

    /**
     * Processes panel-level alerts
     * DISABLED: center_details/graph endpoint is not working
     */
    private void processPanelAlerts(RequestInfo requestInfo) {
        log.warn("Panel alerts processing is disabled - center_details/graph endpoint is not working");
        // Disabled until RMS team fixes the endpoint
        // try {
        //     List<RMSFacilityData> facilities = dataCollectorService.collectPanelData();
        //     List<Alert> alerts = ruleEngineService.applyPanelRules(facilities);
        //     List<Alert> uniqueAlerts = deduplicationManager.deduplicateAlerts(alerts);
        //     createTickets(uniqueAlerts, requestInfo);
        // } catch (Exception e) {
        //     log.error("Error processing panel alerts", e);
        // }
    }

    /**
     * Processes inverter no-signal alerts
     */
    private void processInverterNoSignalAlerts(RequestInfo requestInfo) {
        log.info("Processing inverter no-signal alerts");
        try {
            List<RMSFacilityData> facilities = dataCollectorService.collectInverterNoSignalData();
            List<Alert> alerts = ruleEngineService.applyInverterRules(facilities, true);
            List<Alert> uniqueAlerts = deduplicationManager.deduplicateAlerts(alerts);
            createTickets(uniqueAlerts, requestInfo);
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
            List<RMSFacilityData> facilities = dataCollectorService.collectInverterHighVoltageData();
            List<Alert> alerts = ruleEngineService.applyInverterRules(facilities, false);
            List<Alert> uniqueAlerts = deduplicationManager.deduplicateAlerts(alerts);
            createTickets(uniqueAlerts, requestInfo);
        } catch (Exception e) {
            log.error("Error processing inverter high voltage alerts", e);
        }
    }

    /**
     * Processes battery alerts
     */
    private void processBatteryAlerts(RequestInfo requestInfo) {
        log.info("Processing battery alerts");
        try {
            List<RMSFacilityData> facilities = dataCollectorService.collectBatteryVoltageZeroData();
            List<Alert> alerts = ruleEngineService.applyBatteryRules(facilities);
            List<Alert> uniqueAlerts = deduplicationManager.deduplicateAlerts(alerts);
            createTickets(uniqueAlerts, requestInfo);
        } catch (Exception e) {
            log.error("Error processing battery alerts", e);
        }
    }

    /**
     * Processes grid alerts
     */
    private void processGridAlerts(RequestInfo requestInfo) {
        log.info("Processing grid alerts");
        try {
            List<RMSFacilityData> facilities = dataCollectorService.collectGridVoltageData();
            List<Alert> alerts = ruleEngineService.applyGridRules(facilities);
            List<Alert> uniqueAlerts = deduplicationManager.deduplicateAlerts(alerts);
            createTickets(uniqueAlerts, requestInfo);
        } catch (Exception e) {
            log.error("Error processing grid alerts", e);
        }
    }

    /**
     * Creates tickets for alerts
     */
    private void createTickets(List<Alert> alerts, RequestInfo requestInfo) {
        log.info("Creating tickets for {} alerts", alerts.size());
        
        int successCount = 0;
        int failureCount = 0;
        
        for (Alert alert : alerts) {
            try {
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
        
        log.info("Ticket creation completed: {} succeeded, {} failed", successCount, failureCount);
    }

    /**
     * Creates system RequestInfo for automated operations
     */
    private RequestInfo createSystemRequestInfo() {
        User user = User.builder()
                .uuid(config.getSystemUserUuid())
                .userName("RMS_SYSTEM")
                .name("RMS System")
                .tenantId(config.getDefaultTenantId())
                .build();

        return RequestInfo.builder()
                .userInfo(user)
                .build();
    }
}

