package org.egov.im.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.egov.im.config.IMConfiguration;
import org.egov.im.util.IMConstants;
import org.egov.im.web.models.IncidentRequest;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

/**
 * Notifies RMS service when a ticket status is updated in IM service.
 * This is used to update RMS alert status when tickets are closed/resolved.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RmsStatusUpdateService {

    private final RestTemplate restTemplate;
    private final IMConfiguration config;

    /**
     * Sends ticket status update to RMS when status is in closed state.
     *
     * @param request IncidentRequest after workflow update (contains latest status)
     */
    public void notifyRmsOnStatusUpdate(IncidentRequest request) {
        String incidentId = request.getIncident().getIncidentId();
        String applicationStatus = request.getIncident().getApplicationStatus();

        if (incidentId == null || incidentId.isEmpty()) {
            log.warn("Skipping RMS ticket status notification - incidentId is null/empty");
            return;
        }

        if (applicationStatus == null || applicationStatus.isEmpty()) {
            log.warn("Skipping RMS ticket status notification - applicationStatus is null/empty");
            return;
        }

        String upperStatus = applicationStatus.toUpperCase();

        // Only notify RMS when ticket is moved to a closed/resolved state
        if (!isClosedStatus(upperStatus)) {
            log.debug("Ticket {} status {} is not a closed status - no RMS notification needed", incidentId, upperStatus);
            return;
        }

        String url = config.getRmsHost() + config.getRmsTicketStatusUpdatePath();

        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            Map<String, Object> body = new HashMap<>();
            body.put("incidentId", incidentId);
            body.put("applicationStatus", upperStatus);
            body.put("requestInfo", request.getRequestInfo());

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);

            log.info("Notifying RMS about ticket status update - Ticket ID: {}, Status: {}, URL: {}",
                    incidentId, upperStatus, url);

            ResponseEntity<String> response = restTemplate.exchange(
                    url,
                    HttpMethod.POST,
                    entity,
                    String.class
            );

            if (response.getStatusCode().is2xxSuccessful()) {
                log.info("Successfully notified RMS about ticket status update - Ticket ID: {}, Status: {}, RMS Response: {}",
                        incidentId, upperStatus, response.getBody());
            } else {
                log.warn("RMS ticket status update call returned non-2xx status for Ticket ID: {}, Status: {}, HTTP Status: {}, Body: {}",
                        incidentId, upperStatus, response.getStatusCode(), response.getBody());
            }
        } catch (Exception e) {
            log.error("Error notifying RMS about ticket status update for Ticket ID: {}, Status: {}",
                    incidentId, upperStatus, e);
        }
    }

    private boolean isClosedStatus(String status) {
        if (status == null) return false;
        String normalized = status.toUpperCase();
        return IMConstants.RESOLVED.equals(normalized)
                || IMConstants.CLOSED_AFTER_RESOLUTION.equals(normalized)
                || IMConstants.CLOSED_AFTER_REJECTION.equals(normalized)
                || IMConstants.REJECTED.equals(normalized);
    }
}


