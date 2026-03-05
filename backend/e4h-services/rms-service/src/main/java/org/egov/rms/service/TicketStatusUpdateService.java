package org.egov.rms.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.egov.rms.model.Alert;
import org.egov.rms.model.TicketStatusUpdateRequest;
import org.egov.rms.repository.AlertRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class TicketStatusUpdateService {

    private final AlertRepository alertRepository;

    /**
     * Processes ticket status update from Saura eMitra
     * When a ticket is closed/resolved, updates the corresponding alert(s) in active_alerts table
     *
     * @param request Ticket status update request containing ticket ID and new status
     */
    public int processTicketStatusUpdate(TicketStatusUpdateRequest request) {
        if (request == null || request.getIncidentId() == null || request.getIncidentId().isEmpty()) {
            log.warn("Invalid ticket status update request: missing incident ID");
            return 0;
        }

        String ticketId = request.getIncidentId();
        String status = request.getApplicationStatus();

        log.info("Processing ticket status update - Ticket ID: {}, Status: {}", ticketId, status);

        // Check if this is a closed status
        if (!request.isClosedStatus()) {
            log.debug("Ticket {} status {} is not a closed status, no action needed", ticketId, status);
            return 0;
        }

        // Find alerts associated with this ticket
        List<Alert> alerts = alertRepository.findAlertsByTicketId(ticketId);
        
        if (alerts.isEmpty()) {
            log.warn("No alerts found for ticket ID: {} - ticket may not have been created by RMS service", ticketId);
            return 0;
        }

        log.info("Found {} alert(s) associated with ticket ID: {}", alerts.size(), ticketId);

        // Resolve all alerts associated with this ticket
        int updated = alertRepository.resolveAlertsByTicketId(ticketId);

        if (updated > 0) {
            log.info("Successfully resolved {} alert(s) for closed ticket: {}", updated, ticketId);
            for (Alert alert : alerts) {
                log.info("Resolved alert - ID: {}, Facility: {}, Type: {}, SubType: {}", 
                        alert.getId(), alert.getFacilityId(), alert.getAlertType(), alert.getAlertSubType());
            }
        } else {
            log.info("Alert(s) for ticket {} were already resolved", ticketId);
        }

        return updated;
    }

    /**
     * Validates if a ticket status update request is valid
     */
    public boolean isValidRequest(TicketStatusUpdateRequest request) {
        return request != null && 
               request.getIncidentId() != null && 
               !request.getIncidentId().isEmpty() &&
               request.getApplicationStatus() != null &&
               !request.getApplicationStatus().isEmpty();
    }
}


