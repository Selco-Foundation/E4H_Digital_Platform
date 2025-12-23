package org.egov.rms.web.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.egov.rms.service.CenterIdMappingService;
import org.egov.rms.service.DataCollectorService;
import org.egov.rms.service.RMSOrchestratorService;
import org.egov.rms.service.TicketStatusUpdateService;
import org.egov.rms.model.RMSFacilityData;
import org.egov.rms.model.TicketStatusUpdateRequest;
import org.egov.rms.model.TicketStatusUpdateResponse;
import org.egov.rms.config.RMSConfiguration;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/rms-service/v1")
@RequiredArgsConstructor
public class RMSController {

    private final RMSOrchestratorService orchestratorService;
    private final CenterIdMappingService mappingService;
    private final DataCollectorService dataCollectorService;
    private final TicketStatusUpdateService ticketStatusUpdateService;
    private final RMSConfiguration config;

    /**
     * Manual trigger endpoint for RMS workflow
     */
    @PostMapping("/trigger")
    public ResponseEntity<String> triggerWorkflow() {
        try {
            log.info("Manual trigger received for RMS workflow");
            orchestratorService.executeWorkflow();
            return ResponseEntity.ok("RMS workflow executed successfully");
        } catch (Exception e) {
            log.error("Error executing RMS workflow", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error executing RMS workflow: " + e.getMessage());
        }
    }

    /**
     * Manual trigger endpoint for Center ID to HFR ID mapping sync
     * Uses the dedicated RMS mapping API
     */
    @PostMapping("/mapping/sync")
    public ResponseEntity<String> syncMappings() {
        try {
            log.info("Manual trigger received for mapping sync from RMS API");
            
            // Use the dedicated mapping API endpoint
            mappingService.syncMappingsFromApi();
            
            return ResponseEntity.ok("Mapping sync completed successfully");
        } catch (Exception e) {
            log.error("Error syncing mappings from API", e);
            // Fallback to facility data if API fails (only using working endpoint)
            try {
                log.info("Falling back to facility data sync from working endpoint");
                List<RMSFacilityData> facilities = new ArrayList<>();
                facilities.addAll(dataCollectorService.collectInverterNoSignalData());
                // Note: collectPanelData() is disabled as center_details/graph endpoint is not working
                mappingService.syncMappings(facilities);
                return ResponseEntity.ok("Mapping sync completed using fallback method");
            } catch (Exception fallbackError) {
                log.error("Error in fallback mapping sync", fallbackError);
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body("Error syncing mappings: " + fallbackError.getMessage());
            }
        }
    }

    /**
     * Manual trigger endpoint for mapping validation
     */
    @PostMapping("/mapping/validate")
    public ResponseEntity<String> validateMappings() {
        try {
            log.info("Manual trigger received for mapping validation");
            mappingService.validateMappings(config.getMappingValidationDays());
            return ResponseEntity.ok("Mapping validation completed successfully");
        } catch (Exception e) {
            log.error("Error validating mappings", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error validating mappings: " + e.getMessage());
        }
    }

    /**
     * Webhook endpoint to receive ticket status updates from Saura eMitra
     * When a ticket is closed/resolved in Saura eMitra, this endpoint updates
     * the corresponding alert(s) in the active_alerts table to prevent duplicate ticket creation
     */
    @PostMapping("/ticket/status/update")
    public ResponseEntity<TicketStatusUpdateResponse> updateTicketStatus(@RequestBody TicketStatusUpdateRequest request) {
        try {
            log.info("Received ticket status update webhook - Ticket ID: {}, Status: {}", 
                    request.getIncidentId(),request.getApplicationStatus());

            // Validate request
            if (!ticketStatusUpdateService.isValidRequest(request)) {
                TicketStatusUpdateResponse errorResponse = TicketStatusUpdateResponse.error(
                        "Invalid request: incidentId and applicationStatus are required");
                return ResponseEntity.badRequest().body(errorResponse);
            }

            // Process ticket status update
            int alertsUpdated = ticketStatusUpdateService.processTicketStatusUpdate(request);

            TicketStatusUpdateResponse response = TicketStatusUpdateResponse.success(
                    request.getIncidentId(),
                    request.getApplicationStatus(),
                    alertsUpdated,
                    request.isClosedStatus()
            );

            log.info("Successfully processed ticket status update - Ticket ID: {}, Alerts Updated: {}", 
                    request.getIncidentId(), alertsUpdated);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Error processing ticket status update", e);
            TicketStatusUpdateResponse errorResponse = TicketStatusUpdateResponse.error(
                    "Error processing ticket status update: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }
}

