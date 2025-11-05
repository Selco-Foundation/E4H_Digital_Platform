package org.egov.rms.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.egov.rms.config.RMSConfiguration;
import org.egov.rms.model.Alert;
import org.egov.rms.repository.AlertRepository;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class DeduplicationManager {

    private final AlertRepository alertRepository;
    private final RMSConfiguration config;

    /**
     * Filters alerts to remove duplicates based on active alerts table and suppression window
     */
    public List<Alert> deduplicateAlerts(List<Alert> alerts) {
        log.info("Deduplicating {} alerts", alerts.size());
        List<Alert> uniqueAlerts = new ArrayList<>();

        for (Alert alert : alerts) {
            // Check if alert already exists and is active
            boolean exists = alertRepository.findActiveAlert(
                    alert.getFacilityId(),
                    alert.getAlertType(),
                    alert.getAlertSubType()
            ).isPresent();

            if (exists) {
                // Check if alert should be suppressed
                boolean shouldSuppress = alertRepository.shouldSuppress(
                        alert.getFacilityId(),
                        alert.getAlertType(),
                        alert.getAlertSubType(),
                        config.getSuppressionWindowHours()
                );

                if (shouldSuppress) {
                    log.debug("Suppressing duplicate alert for facility: {}, type: {}, subType: {}",
                            alert.getFacilityId(), alert.getAlertType(), alert.getAlertSubType());
                    alertRepository.updateSuppressionTime(
                            alert.getFacilityId(),
                            alert.getAlertType(),
                            alert.getAlertSubType()
                    );
                    continue;
                }
            }

            // Alert is new or suppression window has passed
            uniqueAlerts.add(alert);
            alertRepository.saveAlert(alert);
        }

        log.info("After deduplication: {} unique alerts", uniqueAlerts.size());
        return uniqueAlerts;
    }
}

