package org.selco.e4h.util;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.selco.e4h.service.UpdateService;
import org.selco.e4h.web.models.EscalationInfo;
import org.selco.e4h.web.models.EscalationTicket;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.*;

import static org.selco.e4h.util.IMConstants.*;
@Slf4j
@Component
@RequiredArgsConstructor
public class ElasticSearchClient {

    @Value("${es.index.computed.sla.im.services}")
    private String computedSlaImServicesIndex;

    private final RestTemplate restTemplate;
    private final UpdateService updateService;

    @Value("${egov.indexer.es.host.name}")
    private String esHost;

    @Value("${egov.indexer.es.port.no}")
    private int esPort;

    @Value("${php.kafka.topic.indexer}")
    private String phcIndex;

    private static final String SEARCH_PATH = "_search";
    private static final String OLD_INDEX_NAME = "im-services";
    private String INDEX_NAME;
    private static final String INDEX_NAME_PHC = "phc-master-list-new-2";

    @PostConstruct
    public void init() {
        this.INDEX_NAME = computedSlaImServicesIndex;
    }

    private static final String DOC_PATH = "_doc";

    public List<Map<String, Object>> fetchRequiredTickets(int from, int size ,boolean closedTickets) {
        return fetchTickets(INDEX_NAME, from, size, closedTickets);
    }

    public List<Map<String, Object>> fetchOldRequiredTicketsFromImServices(int from, int size, boolean closedTickets) {
        return fetchTickets(OLD_INDEX_NAME, from, size,  closedTickets);
    }

    public Map<String, Object> getHFByTenantId(String tenantId) {
        return fetchTicketById(phcIndex, tenantId);
    }

    public List<Map<String, Object>> getAllPHC(int from, int size) {
        return fetchAllPHCs(phcIndex, from, size);
    }

    private List<Map<String, Object>> fetchTickets(String indexName, int from, int size, Boolean closedTickets) {
        String uri = getBaseUrl() + "/" + indexName + "/" + SEARCH_PATH;
        Map<String, Object> query = buildRequiredTicketQuery(from, size, closedTickets);
        HttpEntity<Object> entity = new HttpEntity<>(query, updateService.buildHeaders());

        try {
            Map<String, Object> response = restTemplate.postForObject(uri, entity, Map.class);
            return parseESHits(response);
        } catch (Exception e) {
            log.error("Failed to fetch open tickets from index '{}'", indexName, e);
            return Collections.emptyList();
        }
    }

    private List<Map<String, Object>> fetchAllPHCs(String indexName, int from, int size) {
        String uri = getBaseUrl() + "/" + indexName + "/" + SEARCH_PATH;
        Map<String, Object> query = buildHFQuery(from, size);
        HttpEntity<Object> entity = new HttpEntity<>(query, updateService.buildHeaders());
        try {
            Map<String, Object> response = restTemplate.postForObject(uri, entity, Map.class);
            return parseESHits(response);
        } catch (Exception e) {
            log.error("Failed to fetch open tickets from index '{}'", indexName, e);
            return Collections.emptyList();
        }
    }

    private Map<String, Object> fetchTicketById(String indexName, String tenantId) {
        String uri = getBaseUrl() + "/" + indexName + "/" + DOC_PATH + "/" + tenantId;

        HttpEntity<String> entity = new HttpEntity<>(updateService.buildHeaders());

        try {
            ResponseEntity<Map> response = restTemplate.exchange(
                    uri,
                    HttpMethod.GET,
                    entity,
                    Map.class
            );

            log.info("Fetched ticket audit for tenantId={} from index={}", tenantId, indexName);
            return response.getBody() != null ? response.getBody() : Collections.emptyMap();

        } catch (Exception e) {
            log.error("Failed to fetch ticket audit from index '{}' with tenantId '{}'", indexName, tenantId, e);
            return Collections.emptyMap();
        }
    }

    private Map<String, Object> buildRequiredTicketQuery(int from, int size, Boolean closedTickets) {
        Map<String, Object> query = new HashMap<>();
        Map<String, Object> bool = new HashMap<>();

        List<Map<String, Object>> mustNot = new ArrayList<>();

        if(!closedTickets) {
            mustNot.add(Map.of("term", Map.of("Data.currentProcessInstance.state.isTerminateState", true)));
            mustNot.add(Map.of("terms", Map.of(
                    "Data.currentProcessInstance.state.applicationStatus.keyword",
                    List.of(REJECTED, CLOSED_AFTER_REJECTION, RESOLVED, CLOSED_AFTER_RESOLUTION)
            )));
        }

        bool.put("must_not", mustNot);
        query.put("query", Map.of("bool", bool));
        query.put("_source", true);
        query.put("from", from);
        query.put("size", size);

        return query;
    }

    private Map<String, Object> buildHFQuery(int from, int size) {
        Map<String, Object> query = new HashMap<>();
        Map<String, Object> bool = new HashMap<>();

        query.put("query", Map.of("bool", bool));
        query.put("_source", true);
        query.put("from", from);
        query.put("size", size);

        return query;
    }

    private List<Map<String, Object>> parseESHits(Map<String, Object> response) {
        List<Map<String, Object>> resultList = new ArrayList<>();
        if (response == null) return resultList;

        Map<String, Object> hits = (Map<String, Object>) response.get("hits");
        if (hits == null || !hits.containsKey("hits")) return resultList;

        List<Map<String, Object>> rawHits = (List<Map<String, Object>>) hits.get("hits");
        for (Map<String, Object> hit : rawHits) {
            Map<String, Object> source = (Map<String, Object>) hit.get("_source");
            resultList.add(source);
        }

        return resultList;
    }

    /**
     * Generic search method for custom queries
     * Used by SLABreachDetectionService for escalation queries
     */
    public List<EscalationTicket> searchTickets(Map<String, Object> query) {
        String uri = getBaseUrl() + "/" + INDEX_NAME + "/" + SEARCH_PATH;
        HttpEntity<Object> entity = new HttpEntity<>(query, updateService.buildHeaders());

        try {
            log.debug("Executing Elasticsearch query: {}", query);
            ResponseEntity<Map> response = restTemplate.exchange(uri, HttpMethod.GET, entity, Map.class);
            return parseEscalationTickets(response.getBody());
        } catch (Exception e) {
            log.error("Failed to execute search query on index '{}'", INDEX_NAME, e);
            return Collections.emptyList();
        }
    }

    /**
     * Parse Elasticsearch response to EscalationTicket objects
     */
    private List<EscalationTicket> parseEscalationTickets(Map<String, Object> response) {
        List<org.selco.e4h.web.models.EscalationTicket> tickets = new ArrayList<>();
        if (response == null) return tickets;

        Map<String, Object> hits = (Map<String, Object>) response.get("hits");
        if (hits == null || !hits.containsKey("hits")) return tickets;

        List<Map<String, Object>> rawHits = (List<Map<String, Object>>) hits.get("hits");
        for (Map<String, Object> hit : rawHits) {
            try {
                String documentId = (String) hit.get("_id");
                Map<String, Object> source = (Map<String, Object>) hit.get("_source");
                EscalationTicket ticket = convertToEscalationTicket(source, documentId);
                if (ticket != null) {
                    tickets.add(ticket);
                }
            } catch (Exception e) {
                log.warn("Error parsing ticket from Elasticsearch hit: {}", hit.get("_id"), e);
            }
        }

        log.info("Parsed {} escalation tickets from Elasticsearch response", tickets.size());
        return tickets;
    }

    /**
     * Convert Elasticsearch source document to EscalationTicket object
     */
    private EscalationTicket convertToEscalationTicket(Map<String, Object> source, String documentId) {
        try {
            log.debug("Converting Elasticsearch source: {}", source.keySet());

            // Extract Data object which contains the main ticket information
            Map<String, Object> data = (Map<String, Object>) source.get("Data");
            if (data == null) {
                log.warn("No Data object found in Elasticsearch source: {}", source.keySet());
                return null;
            }

            log.debug("Data object keys: {}", data.keySet());

            // Extract incident data (nested within Data)
            Map<String, Object> incident = (Map<String, Object>) data.get("incident");
            if (incident == null) {
                log.warn("No incident found in Data: {}", data.keySet());
                return null;
            }

            log.debug("Incident object keys: {}", incident.keySet());

            // Extract SLA information from the correct location (directly in Data)
            Object slaRemaining = data.get("slaRemaining");
            Object totalSlaRemaining = data.get("totalSlaRemaining");
            Object stateSla = data.get("stateSla");

            log.debug("SLA fields - slaRemaining: {}, totalSlaRemaining: {}, stateSla: {}",
                slaRemaining, totalSlaRemaining, stateSla);

            // Calculate SLA breach time if slaRemaining is negative
            Long slaBreachTime = null;
            if (slaRemaining instanceof Number && ((Number) slaRemaining).doubleValue() < 0) {
                slaBreachTime = System.currentTimeMillis();
            }

            // Extract additional fields for complete ticket information
            Map<String, Object> auditDetails = (Map<String, Object>) incident.get("auditDetails");
            Long createdTime = auditDetails != null ? getLongValue(auditDetails, "createdTime") : null;

            // Extract vendor information
            String mappedVendorName = extractVendorName(data);

            // Extract priority from business service (since priority field doesn't exist in index)
            String priority = extractPriorityFromBusinessService(data);

            // Extract comments from incident
            String comments = (String) incident.get("comments");

            // Determine SLA compliance status
            boolean slaComplianceCurrentStatus = slaRemaining != null && getLongValue(slaRemaining) > 0;
            boolean slaComplianceOverallTicket = totalSlaRemaining != null && getLongValue(totalSlaRemaining) > 0;

            String definedSlaDurationCurrentStatus = stateSla != null ? stateSla.toString() : "Not Defined";
            Object definedTotalSla = data.get("definedTotalSla");
            String definedOverallSlaDuration = definedTotalSla != null ? definedTotalSla.toString() : "Not Defined";

            // Determine if solar system is working based on system functional status
            boolean isSolarSystemWorking = "FUNCTIONAL".equals(data.get("systemFunctional"));

            EscalationTicket ticket = EscalationTicket.builder()
                    .id(documentId)  // Use document ID from hit metadata
                    .incidentId((String) incident.get("incidentId"))
                    .tenantId((String) data.get("tenantId"))  // tenantId is in Data, not incident
                    .applicationStatus((String) incident.get("applicationStatus"))
                    .incidentType((String) incident.get("incidentType"))
                    .incidentSubType((String) incident.get("incidentSubType"))
                    .filedDate(createdTime)
                    .slaBreachTime(slaBreachTime)
                    .escalationInfo(parseEscalations(data))
                    .additionalDetails(data)
                    // Complete field mapping according to enhancement requirements
                    .ticketNumber((String) incident.get("incidentId"))
                    .district((String) data.get("district"))  // district is in Data, not incident
                    .block((String) data.get("block"))        // block is in Data, not incident
                    .healthFacilityName((String) data.get("tenantId_localized"))  // tenantId_localized is the health facility name
                    .healthFacilityType((String) incident.get("phcSubType")) // phcSubType is in incident for health facility type
                    .isSolarSystemWorking(isSolarSystemWorking)
                    .issueType((String) incident.get("incidentType"))
                    .issueSubType((String) incident.get("incidentSubType"))
                    .priority(priority)
                    .mappedVendor(mappedVendorName)
                    .currentTicketStatus((String) incident.get("applicationStatus"))
                    .slaComplianceCurrentStatus(slaComplianceCurrentStatus)
                    .definedSlaDurationCurrentStatus(definedSlaDurationCurrentStatus)
                    .slaComplianceOverallTicket(slaComplianceOverallTicket)
                    .definedOverallSlaDuration(definedOverallSlaDuration)
                    .comments(comments)
                    .ticketFiledDate(createdTime)
                    .build();

            log.debug("Created EscalationTicket: id={}, tenantId={}, incidentId={}, applicationStatus={}, district={}, block={}",
                ticket.getId(), ticket.getTenantId(), ticket.getIncidentId(),
                ticket.getApplicationStatus(), ticket.getDistrict(), ticket.getBlock());

            return ticket;
        } catch (Exception e) {
            log.error("Error converting Elasticsearch source to EscalationTicket", e);
            return null;
        }
    }

    /**
     * Parse escalations from ticket data
     */
    private List<EscalationInfo> parseEscalations(Map<String, Object> data) {
        // First try to find escalations in the incident object
        Map<String, Object> incident = (Map<String, Object>) data.get("incident");
        if (incident != null) {
            List<Map<String, Object>> escalationsData = (List<Map<String, Object>>) incident.get("escalations");
            if (escalationsData != null && !escalationsData.isEmpty()) {
                return parseEscalationList(escalationsData);
            }
        }

        // If not found in incident, try directly in Data object
        List<Map<String, Object>> escalationsData = (List<Map<String, Object>>) data.get("escalations");
        if (escalationsData != null && !escalationsData.isEmpty()) {
            return parseEscalationList(escalationsData);
        }

        // No escalations found
        return new ArrayList<>();
    }

    /**
     * Parse a list of escalation data into EscalationInfo objects
     */
    private List<EscalationInfo> parseEscalationList(List<Map<String, Object>> escalationsData) {
        List<EscalationInfo> escalations = new ArrayList<>();
        for (Map<String, Object> escalationData : escalationsData) {
            EscalationInfo escalation = EscalationInfo.builder()
                .escalationId((String) escalationData.get("escalationId"))
                .escalationTime(getLongValue(escalationData, "escalationTime"))
                .escalationLevel((String) escalationData.get("escalationLevel"))
                .recipientRole((String) escalationData.get("recipientRole"))
                .build();
            escalations.add(escalation);
        }
        return escalations;
    }

    /**
     * Safely extract Long value from Map
     */
    private Long getLongValue(Map<String, Object> map, String key) {
        Object value = map.get(key);
        if (value == null) return null;
        if (value instanceof Long) return (Long) value;
        if (value instanceof Integer) return ((Integer) value).longValue();
        if (value instanceof String) {
            try {
                return Long.parseLong((String) value);
            } catch (NumberFormatException e) {
                log.warn("Could not parse Long value for key '{}': {}", key, value);
                return null;
            }
        }
        return null;
    }

    /**
     * Get long value from object with null safety
     */
    private Long getLongValue(Object value) {
        if (value instanceof Number) {
            return ((Number) value).longValue();
        }
        return null;
    }

    /**
     * Extract vendor name from data
     */
    private String extractVendorName(Map<String, Object> data) {
        // Try to get vendor name from mappedVendorName field
        String vendorName = (String) data.get("mappedVendorName");
        if (vendorName != null && !vendorName.isEmpty()) {
            return vendorName;
        }

        // Try to get vendor name from mappedVendorUserName field
        String vendorUserName = (String) data.get("mappedVendorUserName");
        if (vendorUserName != null && !vendorUserName.isEmpty()) {
            return vendorUserName;
        }

        return "Not Assigned";
    }

    /**
     * Extract priority from business service name
     * Business service format: "Incident_High", "Incident_Low", "Incident_Medium"
     * Priority is the part after the underscore
     */
    private String extractPriorityFromBusinessService(Map<String, Object> data) {
        try {
            Map<String, Object> currentProcessInstance = (Map<String, Object>) data.get("currentProcessInstance");
            if (currentProcessInstance == null) {
                return "Medium"; // Default fallback
            }
            
            Object businessServiceObj = currentProcessInstance.get("businessService");
            if (businessServiceObj instanceof String businessService && businessService.contains("_")) {
                String[] parts = businessService.split("_", 2);
                if (parts.length > 1) {
                    return parts[1]; // Return part after underscore (High, Low, Medium)
                }
            }
            
            return "Medium"; // Default fallback
        } catch (Exception e) {
            log.warn("Error extracting priority from business service: {}", e.getMessage());
            return "Medium"; // Default fallback
        }
    }

    private String getBaseUrl() {
        return esHost + ":" + esPort;
    }
}
