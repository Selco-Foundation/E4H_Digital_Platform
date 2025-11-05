package org.egov.rms.web.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.egov.rms.service.CenterIdMappingService;
import org.egov.rms.service.DataCollectorService;
import org.egov.rms.service.RMSOrchestratorService;
import org.egov.rms.model.RMSFacilityData;
import org.egov.rms.config.RMSConfiguration;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
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
            // Fallback to facility data if API fails
            try {
                log.info("Falling back to facility data sync");
                List<RMSFacilityData> facilities = new ArrayList<>();
                facilities.addAll(dataCollectorService.collectInverterNoSignalData());
                facilities.addAll(dataCollectorService.collectPanelData());
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
}

