package org.selco.e4h.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.selco.e4h.config.ConsumerConfiguration;
import org.selco.e4h.util.ElasticSearchClient;
import org.selco.e4h.util.UpdateUtils;
import org.selco.e4h.web.models.EscalationTicket;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.*;

/**
 * Service to update Elasticsearch with escalation information using bulk APIs
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ElasticsearchEscalationService {
    
    private final RestTemplate restTemplate;
    private final ElasticSearchClient elasticSearchClient;
    private final ConsumerConfiguration consumerConfiguration;
    
    private static final String BULK_ENDPOINT = "_bulk";
    private static final String INDEX_NAME = "computed-sla-im-services-write";

    @Autowired
    private UpdateUtils indexerUtils;
    
    /**
     * Update escalations for tickets using Elasticsearch bulk API
     */
    public void updateEscalationsForTickets(List<EscalationTicket> tickets, String escalationRecipientId, String escalationLevel) {
        try {
            log.info("Updating escalations for {} tickets with escalation ID: {} and level: {}", tickets.size(), escalationRecipientId, escalationLevel);
            
            if (tickets.isEmpty()) {
                log.info("No tickets to update");
                return;
            }
            
            // Build bulk request
            String bulkRequest = buildBulkUpdateRequest(tickets, escalationRecipientId, escalationLevel);
            
            // Send bulk request to Elasticsearch
            String url = getBaseUrl() + "/" + BULK_ENDPOINT;
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.add("Authorization", indexerUtils.getESEncodedCredentials());
            
            HttpEntity<String> entity = new HttpEntity<>(bulkRequest, headers);
            
            ResponseEntity<Map> response = restTemplate.exchange(
                url,
                HttpMethod.POST,
                entity,
                Map.class
            );
            
            if (response.getStatusCode().is2xxSuccessful()) {
                log.info("Successfully updated escalations for {} tickets", tickets.size());
                logBulkResponse(response.getBody());
            } else {
                log.error("Failed to update escalations. Status: {}", response.getStatusCode());
            }
            
        } catch (Exception e) {
            log.error("Error updating escalations for tickets", e);
        }
    }
    
    /**
     * Build bulk update request for Elasticsearch
     */
    private String buildBulkUpdateRequest(List<EscalationTicket> tickets, String escalationRecipientId, String escalationLevel) {
        StringBuilder bulkRequest = new StringBuilder();
        long currentTime = System.currentTimeMillis();
        
        for (EscalationTicket ticket : tickets) {
            // Build update action metadata (without _op_type)
            Map<String, Object> updateAction = new HashMap<>();
            updateAction.put("_index", INDEX_NAME);
            updateAction.put("_id", ticket.getId());
            
            // Add action line
            bulkRequest.append("{\"update\":").append(convertToJson(updateAction)).append("}\n");
            
            // Build script for updating escalations array
            Map<String, Object> script = new HashMap<>();
            script.put("source", 
                "if (ctx._source.Data == null) { ctx._source.Data = [:] } " +
                "if (ctx._source.Data.incident == null) { ctx._source.Data.incident = [:] } " +
                "if (ctx._source.Data.incident.escalations == null) { ctx._source.Data.incident.escalations = [] } " +
                "ctx._source.Data.incident.escalations.add(params.escalation)");
            
            Map<String, Object> params = new HashMap<>();
            Map<String, Object> escalation = new HashMap<>();
            escalation.put("escalationId", escalationRecipientId);
            escalation.put("escalationTime", currentTime);
            escalation.put("escalationLevel", escalationLevel);
            params.put("escalation", escalation);
            script.put("params", params);
            
            Map<String, Object> doc = new HashMap<>();
            doc.put("script", script);
            
            // Add document line
            bulkRequest.append(convertToJson(doc)).append("\n");
        }
        
        return bulkRequest.toString();
    }
    
    /**
     * Convert object to JSON string
     */
    private String convertToJson(Object obj) {
        try {
            return new com.fasterxml.jackson.databind.ObjectMapper().writeValueAsString(obj);
        } catch (Exception e) {
            log.error("Error converting object to JSON", e);
            return "{}";
        }
    }
    
    /**
     * Log bulk response for debugging
     */
    private void logBulkResponse(Map<String, Object> response) {
        try {
            if (response != null && response.containsKey("items")) {
                List<Map<String, Object>> items = (List<Map<String, Object>>) response.get("items");
                int successCount = 0;
                int errorCount = 0;
                
                for (Map<String, Object> item : items) {
                    if (item.containsKey("update")) {
                        Map<String, Object> update = (Map<String, Object>) item.get("update");
                        if (update.containsKey("error")) {
                            errorCount++;
                            log.warn("Bulk update error: {}", update.get("error"));
                        } else {
                            successCount++;
                        }
                    }
                }
                
                log.info("Bulk update completed - Success: {}, Errors: {}", successCount, errorCount);
            }
        } catch (Exception e) {
            log.warn("Error parsing bulk response", e);
        }
    }
    
    /**
     * Get Elasticsearch base URL
     */
    private String getBaseUrl() {
        return consumerConfiguration.getEsHostName() + ":" + consumerConfiguration.getEsPortNo();
    }
}
