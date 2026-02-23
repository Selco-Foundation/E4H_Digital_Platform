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
     * Also checks if tickets already exist to prevent duplicate ticket creation
     * For alerts from history, we trust the query filter and only check if alert itself has ticket_id
     */
    public List<Alert> deduplicateAlerts(List<Alert> alerts) {
        log.info("Deduplicating {} alerts", alerts.size());
        List<Alert> uniqueAlerts = new ArrayList<>();

        for (Alert alert : alerts) {
            // Check if alert already has an open ticket in eg_incident_v2 table
            // This checks both active_alerts and eg_incident_v2 to see if ticket is still open
            // If ticket is closed or doesn't exist, we allow creating a new ticket
            // Note: We don't skip alerts with ticket_id set because they might have closed tickets
            if (alertRepository.hasOpenTicket(
                    alert.getFacilityId(),
                    alert.getAlertType(),
                    alert.getAlertSubType())) {
                log.info("Skipping alert {} - open ticket already exists in eg_incident_v2 for facility: {}, type: {}, subType: {}",
                        alert.getId(), alert.getFacilityId(), alert.getAlertType(), alert.getAlertSubType());
                continue;
            }
            
            // If alert has a ticket_id but ticket is closed, log that we're processing it
            if (alert.getTicketId() != null && !alert.getTicketId().isEmpty()) {
                log.info("Alert {} has closed ticket {} - will create new ticket for facility: {}, type: {}, subType: {}",
                        alert.getId(), alert.getTicketId(), alert.getFacilityId(), alert.getAlertType(), alert.getAlertSubType());
            }

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
            // Save alert to active_alerts table if it doesn't exist
            try {
                alertRepository.saveAlert(alert);
                log.debug("Successfully saved alert {} to active_alerts", alert.getId());
            } catch (Exception e) {
                log.error("Failed to save alert {} to active_alerts: {}", alert.getId(), e.getMessage(), e);
                // Continue processing other alerts even if one fails
            }
        }

        log.info("After deduplication: {} unique alerts", uniqueAlerts.size());
        return uniqueAlerts;
    }
}

