package org.egov.rms.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.egov.rms.config.RMSConfiguration;
import org.egov.rms.model.Alert;
import org.egov.rms.model.IMServiceRequest;
import org.egov.rms.model.IMServiceResponse;
import org.egov.rms.repository.AlertRepository;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class SauraEmitraConnector {

    private final RMSConfiguration config;
    private final RestTemplate restTemplate;
    private final AlertRepository alertRepository;
    private final ObjectMapper objectMapper;

    /**
     * Creates a ticket in Saura eMitra (IM service) with retry logic
     */
    public boolean createTicket(Alert alert, IMServiceRequest request) {
        String url = config.getImServiceBaseUrl() + config.getImServiceCreateEndpoint();
        
        HttpHeaders headers = new HttpHeaders();
        headers.set("Content-Type", "application/json");
        headers.set("Accept", "application/json");
        
        HttpEntity<IMServiceRequest> entity = new HttpEntity<>(request, headers);
        
        int maxAttempts = config.getRetryMaxAttempts();
        long backoffDelay = config.getRetryBackoffDelay();
        int attempts = 0;
        
        while (attempts < maxAttempts) {
            try {
                log.info("Creating ticket in IM service for alert: {} (attempt {})", alert.getId(), attempts + 1);
                
                ResponseEntity<IMServiceResponse> response = restTemplate.exchange(
                        url, HttpMethod.POST, entity, IMServiceResponse.class);
                
                if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                    IMServiceResponse responseBody = response.getBody();
                    
                    // Extract ticket ID from response
                    String ticketId = extractTicketId(responseBody);
                    
                    if (ticketId != null) {
                        // Update alert with ticket ID
                        alertRepository.updateTicketId(alert.getId(), ticketId);
                        log.info("Successfully created ticket {} for alert: {}", ticketId, alert.getId());
                        return true;
                    } else {
                        log.warn("Ticket created but ticket ID not found in response for alert: {}", alert.getId());
                    }
                }
                
                // If we reach here, the request was successful but something went wrong
                attempts++;
                if (attempts < maxAttempts) {
                    log.warn("Ticket creation response incomplete, retrying after {}ms", backoffDelay);
                    Thread.sleep(backoffDelay);
                    backoffDelay *= 2; // Exponential backoff
                }
                
            } catch (RestClientException e) {
                e.printStackTrace();
                attempts++;
                if (attempts >= maxAttempts) {
                    log.error("Failed to create ticket after {} attempts for alert: {}", maxAttempts, alert.getId(), e);
                    return false;
                }
                
                log.warn("Error creating ticket (attempt {}), retrying after {}ms: {}", 
                        attempts, backoffDelay, e.getMessage());
                
                try {
                    Thread.sleep(backoffDelay);
                    backoffDelay *= 2; // Exponential backoff
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    log.error("Interrupted during retry", ie);
                    return false;
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                log.error("Interrupted during ticket creation", e);
                return false;
            } catch (Exception e) {
                log.error("Unexpected error creating ticket for alert: {}", alert.getId(), e);
                return false;
            }
        }
        
        return false;
    }

    /**
     * Extracts ticket ID from IM service response
     */
    private String extractTicketId(IMServiceResponse response) {
        try {
            if (response.getIncidentWrappers() != null && !response.getIncidentWrappers().isEmpty()) {
                IMServiceResponse.IncidentWrapper wrapper = response.getIncidentWrappers().get(0);
                if (wrapper.getIncident() != null) {
                    return wrapper.getIncident().getIncidentId();
                }
            }
        } catch (Exception e) {
            log.warn("Error extracting ticket ID from response", e);
        }
        return null;
    }
}

