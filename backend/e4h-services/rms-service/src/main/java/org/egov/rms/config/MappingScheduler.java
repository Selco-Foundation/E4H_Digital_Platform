package org.egov.rms.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.egov.rms.service.CenterIdMappingService;
import org.egov.rms.service.DataCollectorService;
import org.egov.rms.model.RMSFacilityData;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class MappingScheduler {

    private final CenterIdMappingService mappingService;
    private final DataCollectorService dataCollectorService;
    private final RMSConfiguration config;

    /**
     * Scheduled job to sync Center ID to HFR ID mappings (runs weekly on Sunday at 2 AM)
     * Uses the dedicated RMS mapping API which is updated weekly
     */
    @Scheduled(cron = "${rms.mapping.sync.cron:0 0 2 * * 0}", zone = "Asia/Kolkata")
    @ConditionalOnProperty(value = "rms.mapping.sync.enabled", havingValue = "true", matchIfMissing = true)
    public void syncMappings() {
        log.info("Starting scheduled Center ID to HFR ID mapping sync from RMS API");
        try {
            // Use the dedicated mapping API endpoint
            mappingService.syncMappingsFromApi();
        } catch (Exception e) {
            log.error("Error in scheduled mapping sync from API", e);
            // Fallback to facility data if API fails
            log.info("Falling back to facility data sync");
            try {
                List<RMSFacilityData> facilities = new ArrayList<>();
                facilities.addAll(dataCollectorService.collectInverterNoSignalData());
                facilities.addAll(dataCollectorService.collectPanelData());
                mappingService.syncMappings(facilities);
            } catch (Exception fallbackError) {
                log.error("Error in fallback mapping sync", fallbackError);
            }
        }
        log.info("Completed scheduled mapping sync");
    }

    /**
     * Scheduled job to validate mappings (runs weekly on Sunday at 3 AM)
     */
    @Scheduled(cron = "${rms.mapping.validation.cron:0 0 3 * * 0}", zone = "Asia/Kolkata")
    @ConditionalOnProperty(value = "rms.mapping.sync.enabled", havingValue = "true", matchIfMissing = true)
    public void validateMappings() {
        log.info("Starting scheduled mapping validation");
        try {
            mappingService.validateMappings(config.getMappingValidationDays());
        } catch (Exception e) {
            log.error("Error in scheduled mapping validation", e);
        }
        log.info("Completed scheduled mapping validation");
    }
}

