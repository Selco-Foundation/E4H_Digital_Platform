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
        log.trace("Initializing ElasticSearchClient");
        this.INDEX_NAME = computedSlaImServicesIndex;
        log.debug("Index name set to: {}", INDEX_NAME);
        log.info("ElasticSearchClient initialized");
    }

    private static final String DOC_PATH = "_doc";

    public List<Map<String, Object>> fetchRequiredTickets(int from, int size ,boolean closedTickets) {
        log.trace("Fetching required tickets from index: {}, from: {}, size: {}, closedTickets: {}", INDEX_NAME, from, size, closedTickets);
        return fetchTickets(INDEX_NAME, from, size, closedTickets);
    }

    public List<Map<String, Object>> fetchOldRequiredTicketsFromImServices(int from, int size, boolean closedTickets) {
        log.trace("Fetching old required tickets from index: {}, from: {}, size: {}, closedTickets: {}", OLD_INDEX_NAME, from, size, closedTickets);
        return fetchTickets(OLD_INDEX_NAME, from, size,  closedTickets);
    }

    public Map<String, Object> getHFByBoundaryCode(String boundaryCode) {
        log.trace("Getting health facility by boundary code: {}", boundaryCode);
        return fetchTicketByBoundaryCode(phcIndex, boundaryCode);
    }

    public List<Map<String, Object>> getAllPHC(int from, int size) {
        log.trace("Getting all PHC documents, from: {}, size: {}", from, size);
        return fetchAllPHCs(phcIndex, from, size);
    }

    public int getPHCDocsSize() {
        log.trace("Getting PHC documents size");
        return getPHCsSize(phcIndex);
    }

    private List<Map<String, Object>> fetchTickets(String indexName, int from, int size, Boolean closedTickets) {
        log.trace("Fetching tickets from index: {}, from: {}, size: {}, closedTickets: {}", indexName, from, size, closedTickets);
        String uri = getBaseUrl() + "/" + indexName + "/" + SEARCH_PATH;
        log.debug("Elasticsearch URI: {}", uri);
        Map<String, Object> query = buildRequiredTicketQuery(from, size, closedTickets);
        HttpEntity<Object> entity = new HttpEntity<>(query, updateService.buildHeaders());

        try {
            log.debug("Executing Elasticsearch query");
            Map<String, Object> response = restTemplate.postForObject(uri, entity, Map.class);
            List<Map<String, Object>> tickets = parseESHits(response);
            log.info("Successfully fetched {} tickets from index: {}", tickets.size(), indexName);
            return tickets;
        } catch (Exception e) {
            log.error("Failed to fetch tickets from index '{}'", indexName, e);
            return Collections.emptyList();
        }
    }

    private List<Map<String, Object>> fetchAllPHCs(String indexName, int from, int size) {
        log.trace("Fetching all PHCs from index: {}, from: {}, size: {}", indexName, from, size);
        String uri = getBaseUrl() + "/" + indexName + "/" + SEARCH_PATH;
        Map<String, Object> query = buildHFQuery(from, size);
        HttpEntity<Object> entity = new HttpEntity<>(query, updateService.buildHeaders());
        try {
            log.debug("Executing Elasticsearch query for PHCs");
            Map<String, Object> response = restTemplate.postForObject(uri, entity, Map.class);
            List<Map<String, Object>> phcs = parseESHits(response);
            log.info("Successfully fetched {} PHC documents from index: {}", phcs.size(), indexName);
            return phcs;
        } catch (Exception e) {
            log.error("Failed to fetch PHCs from index '{}'", indexName, e);
            return Collections.emptyList();
        }
    }

    private int getPHCsSize(String indexName) {
        log.trace("Getting PHCs size from index: {}", indexName);
        String uri = getBaseUrl() + "/" + indexName + "/" + SEARCH_PATH;
        Map<String, Object> query = buildHFQuery(0, 1);
        HttpEntity<Object> entity = new HttpEntity<>(query, updateService.buildHeaders());
        try {
            log.debug("Executing Elasticsearch query to get total hits");
            Map<String, Object> response = restTemplate.postForObject(uri, entity, Map.class);
            int totalHits = parseESTotalHits(response);
            log.info("Total PHC documents in index {}: {}", indexName, totalHits);
            return totalHits;
        } catch (Exception e) {
            log.error("Failed to get PHCs size from index '{}'", indexName, e);
            return 0;
        }
    }

    private Map<String, Object> fetchTicketByBoundaryCode(String indexName, String boundaryCode) {
        log.trace("Fetching ticket by boundary code: {} from index: {}", boundaryCode, indexName);
        String uri = getBaseUrl() + "/{index}/" + DOC_PATH + "/{id}";
        log.debug("Elasticsearch URI: {}", uri);
        HttpEntity<String> entity = new HttpEntity<>(updateService.buildHeaders());
        try {
            ResponseEntity<Map> response = restTemplate.exchange(
                    uri,
                    HttpMethod.GET,
                    entity,
                    Map.class,
                    indexName,
                    boundaryCode
            );

            log.info("Fetched ticket audit for boundaryCode={} from index={}", boundaryCode, indexName);
            Map<String, Object> body = response.getBody() != null ? response.getBody() : Collections.emptyMap();
            log.debug("Retrieved ticket data, keys: {}", body.keySet());
            return body;

        } catch (Exception e) {
            log.error("Failed to fetch ticket audit from index '{}' with boundaryCode '{}'", indexName, boundaryCode, e);
            return Collections.emptyMap();
        }
    }

    private Map<String, Object> buildRequiredTicketQuery(int from, int size, Boolean closedTickets) {
        log.trace("Building required ticket query, from: {}, size: {}, closedTickets: {}", from, size, closedTickets);
        Map<String, Object> query = new HashMap<>();
        Map<String, Object> bool = new HashMap<>();

        List<Map<String, Object>> mustNot = new ArrayList<>();

        if(!closedTickets) {
            mustNot.add(Map.of("term", Map.of("Data.currentProcessInstance.state.isTerminateState", true)));
            mustNot.add(Map.of("terms", Map.of(
                    "Data.currentProcessInstance.state.applicationStatus.keyword",
                    List.of(REJECTED, CLOSED_AFTER_REJECTION, RESOLVED, CLOSED_AFTER_RESOLUTION)
            )));
            log.debug("Added filters to exclude closed tickets");
        }

        bool.put("must_not", mustNot);
        query.put("query", Map.of("bool", bool));
        query.put("_source", true);
        query.put("from", from);
        query.put("size", size);
        log.debug("Built query with pagination: from={}, size={}", from, size);

        return query;
    }

    private Map<String, Object> buildHFQuery(int from, int size) {
        log.trace("Building health facility query, from: {}, size: {}", from, size);
        Map<String, Object> query = new HashMap<>();
        Map<String, Object> bool = new HashMap<>();

        query.put("query", Map.of("bool", bool));
        query.put("_source", true);
        query.put("from", from);
        query.put("size", size);
        log.debug("Built HF query with pagination: from={}, size={}", from, size);

        return query;
    }

    private List<Map<String, Object>> parseESHits(Map<String, Object> response) {
        log.trace("Parsing Elasticsearch hits from response");
        List<Map<String, Object>> resultList = new ArrayList<>();
        if (response == null) {
            log.debug("Response is null, returning empty list");
            return resultList;
        }

        Map<String, Object> hits = (Map<String, Object>) response.get("hits");
        if (hits == null || !hits.containsKey("hits")) {
            log.debug("No hits found in response");
            return resultList;
        }

        List<Map<String, Object>> rawHits = (List<Map<String, Object>>) hits.get("hits");
        log.debug("Found {} raw hits in response", rawHits != null ? rawHits.size() : 0);
        for (Map<String, Object> hit : rawHits) {
            Map<String, Object> source = (Map<String, Object>) hit.get("_source");
            resultList.add(source);
        }

        log.debug("Parsed {} documents from Elasticsearch response", resultList.size());
        return resultList;
    }

    private int parseESTotalHits(Map<String, Object> response) {
        log.trace("Parsing total hits from Elasticsearch response");
        int totalIndex = 0;
        if (response == null) {
            log.debug("Response is null, returning 0");
            return totalIndex;
        }

        Map<String, Object> hits = (Map<String, Object>) response.get("hits");
        if (hits == null || !hits.containsKey("total")) {
            log.debug("No total hits found in response");
            return totalIndex;
        }

        Map<String, Object> totalHits = (Map<String, Object>) hits.get("total");
        if (totalHits == null || !totalHits.containsKey("value")) {
            log.debug("No total hits value found");
            return totalIndex;
        }
        totalIndex = (int)totalHits.get("value");
        log.debug("Parsed total hits: {}", totalIndex);

        return totalIndex;
    }

    /**
     * Generic search method for custom queries
     * Used by SLABreachDetectionService for escalation queries
     */
    public List<EscalationTicket> searchTickets(Map<String, Object> query) {
        log.trace("Searching tickets with custom query");
        String uri = getBaseUrl() + "/" + INDEX_NAME + "/" + SEARCH_PATH;
        log.debug("Elasticsearch search URI: {}", uri);
        HttpEntity<Object> entity = new HttpEntity<>(query, updateService.buildHeaders());

        try {
            log.info("Executing Elasticsearch query on index: {}", INDEX_NAME);
            log.debug("Query details: {}", query);
            Map<String, Object> response = restTemplate.postForObject(uri, entity, Map.class);
            List<EscalationTicket> tickets = parseEscalationTickets(response);
            log.info("Successfully executed search query, found {} tickets", tickets.size());
            return tickets;
        } catch (Exception e) {
            log.error("Failed to execute search query on index '{}'", INDEX_NAME, e);
            return Collections.emptyList();
        }
    }

    /**
     * Parse Elasticsearch response to EscalationTicket objects
     */
    private List<EscalationTicket> parseEscalationTickets(Map<String, Object> response) {
        log.trace("Parsing escalation tickets from Elasticsearch response");
        List<org.selco.e4h.web.models.EscalationTicket> tickets = new ArrayList<>();
        if (response == null) {
            log.debug("Response is null, returning empty list");
            return tickets;
        }

        Map<String, Object> hits = (Map<String, Object>) response.get("hits");
        if (hits == null || !hits.containsKey("hits")) {
            log.debug("No hits found in response");
            return tickets;
        }

        List<Map<String, Object>> rawHits = (List<Map<String, Object>>) hits.get("hits");
        log.debug("Found {} raw hits to parse", rawHits != null ? rawHits.size() : 0);
        int parsedCount = 0;
        for (Map<String, Object> hit : rawHits) {
            try {
                String documentId = (String) hit.get("_id");
                log.debug("Parsing ticket with document ID: {}", documentId);
                Map<String, Object> source = (Map<String, Object>) hit.get("_source");
                EscalationTicket ticket = convertToEscalationTicket(source, documentId);
                if (ticket != null) {
                    tickets.add(ticket);
                    parsedCount++;
                }
            } catch (Exception e) {
                log.warn("Error parsing ticket from Elasticsearch hit: {}", hit.get("_id"), e);
            }
        }

        log.info("Parsed {} escalation tickets from Elasticsearch response", parsedCount);
        return tickets;
    }

    /**
     * Convert Elasticsearch source document to EscalationTicket object
     */
    private EscalationTicket convertToEscalationTicket(Map<String, Object> source, String documentId) {
        try {
            log.debug("Converting Elasticsearch source: {}", source.keySet());

            Map<String, Object> data = extractDataFromSource(source);
            if (data == null) {
                return null;
            }

            Map<String, Object> incident = extractIncidentFromData(data);
            if (incident == null) {
                return null;
            }

            SLAData slaData = extractSLAData(data);
            TicketBasicInfo basicInfo = extractBasicTicketInfo(incident, data, documentId);
            SLAComplianceInfo slaCompliance = calculateSLACompliance(slaData);
            
            EscalationTicket ticket = buildEscalationTicket(basicInfo, slaData, slaCompliance, data, incident);
            
            log.debug("Created EscalationTicket: id={}, tenantId={}, incidentId={}, applicationStatus={}, district={}, block={}",
                ticket.getId(), ticket.getTenantId(), ticket.getIncidentId(),
                ticket.getApplicationStatus(), ticket.getDistrict(), ticket.getBlock());

            return ticket;
        } catch (Exception e) {
            log.error("Error converting Elasticsearch source to EscalationTicket", e);
            return null;
        }
    }

    private Map<String, Object> extractDataFromSource(Map<String, Object> source) {
        Map<String, Object> data = (Map<String, Object>) source.get("Data");
        if (data == null) {
            log.warn("No Data object found in Elasticsearch source: {}", source.keySet());
            return null;
        }
        log.debug("Data object keys: {}", data.keySet());
        return data;
    }

    private Map<String, Object> extractIncidentFromData(Map<String, Object> data) {
        Map<String, Object> incident = (Map<String, Object>) data.get("incident");
        if (incident == null) {
            log.warn("No incident found in Data: {}", data.keySet());
            return null;
        }
        log.debug("Incident object keys: {}", incident.keySet());
        return incident;
    }

    private SLAData extractSLAData(Map<String, Object> data) {
        Object slaRemaining = data.get("slaRemaining");
        Object totalSlaRemaining = data.get("totalSlaRemaining");
        Object stateSla = data.get("stateSla");
        Object definedTotalSla = data.get("definedTotalSla");
        
        log.debug("SLA fields - slaRemaining: {}, totalSlaRemaining: {}, stateSla: {}",
            slaRemaining, totalSlaRemaining, stateSla);
        
        return new SLAData(slaRemaining, totalSlaRemaining, stateSla, definedTotalSla);
    }

    private TicketBasicInfo extractBasicTicketInfo(Map<String, Object> incident, Map<String, Object> data, String documentId) {
        Map<String, Object> auditDetails = (Map<String, Object>) incident.get("auditDetails");
        Long createdTime = auditDetails != null ? getLongValue(auditDetails, "createdTime") : null;
        String mappedVendorName = extractVendorName(data);
        String priority = extractPriorityFromBusinessService(data);
        String comments = (String) incident.get("comments");
        boolean isSolarSystemWorking = "FUNCTIONAL".equals(data.get("systemFunctional"));
        
        return new TicketBasicInfo(documentId, incident, data, createdTime, mappedVendorName, priority, comments, isSolarSystemWorking);
    }

    private SLAComplianceInfo calculateSLACompliance(SLAData slaData) {
        Long slaBreachTime = null;
        if (slaData.slaRemaining instanceof Number && ((Number) slaData.slaRemaining).doubleValue() < 0) {
            slaBreachTime = System.currentTimeMillis();
        }
        
        boolean slaComplianceCurrentStatus = slaData.slaRemaining != null && getLongValue(slaData.slaRemaining) > 0;
        boolean slaComplianceOverallTicket = slaData.totalSlaRemaining != null && getLongValue(slaData.totalSlaRemaining) > 0;
        String definedSlaDurationCurrentStatus = slaData.stateSla != null ? slaData.stateSla.toString() : "Not Defined";
        String definedOverallSlaDuration = slaData.definedTotalSla != null ? slaData.definedTotalSla.toString() : "Not Defined";
        
        return new SLAComplianceInfo(slaBreachTime, slaComplianceCurrentStatus, slaComplianceOverallTicket, 
            definedSlaDurationCurrentStatus, definedOverallSlaDuration);
    }

    private EscalationTicket buildEscalationTicket(TicketBasicInfo basicInfo, SLAData slaData, 
                                                   SLAComplianceInfo slaCompliance, 
                                                   Map<String, Object> data, Map<String, Object> incident) {
        return EscalationTicket.builder()
                .id(basicInfo.documentId)
                .incidentId((String) incident.get("incidentId"))
                .tenantId((String) data.get("tenantId"))
                .applicationStatus((String) incident.get("applicationStatus"))
                .incidentType((String) incident.get("incidentType"))
                .incidentSubType((String) incident.get("incidentSubType"))
                .filedDate(basicInfo.createdTime)
                .slaBreachTime(slaCompliance.slaBreachTime)
                .escalationInfo(parseEscalations(data))
                .additionalDetails(data)
                .ticketNumber((String) incident.get("incidentId"))
                .district((String) data.get("district"))
                .block((String) data.get("block"))
                .healthFacilityName((String) data.get("tenantId_localized"))
                .healthFacilityType((String) incident.get("phcSubType"))
                .isSolarSystemWorking(basicInfo.isSolarSystemWorking)
                .issueType((String) incident.get("incidentType"))
                .issueSubType((String) incident.get("incidentSubType"))
                .priority(basicInfo.priority)
                .mappedVendor(basicInfo.mappedVendorName)
                .currentTicketStatus((String) incident.get("applicationStatus"))
                .slaComplianceCurrentStatus(slaCompliance.slaComplianceCurrentStatus)
                .definedSlaDurationCurrentStatus(slaCompliance.definedSlaDurationCurrentStatus)
                .slaComplianceOverallTicket(slaCompliance.slaComplianceOverallTicket)
                .definedOverallSlaDuration(slaCompliance.definedOverallSlaDuration)
                .comments(basicInfo.comments)
                .ticketFiledDate(basicInfo.createdTime)
                .build();
    }

    private static class SLAData {
        final Object slaRemaining;
        final Object totalSlaRemaining;
        final Object stateSla;
        final Object definedTotalSla;
        
        SLAData(Object slaRemaining, Object totalSlaRemaining, Object stateSla, Object definedTotalSla) {
            this.slaRemaining = slaRemaining;
            this.totalSlaRemaining = totalSlaRemaining;
            this.stateSla = stateSla;
            this.definedTotalSla = definedTotalSla;
        }
    }

    private static class TicketBasicInfo {
        final String documentId;
        final Long createdTime;
        final String mappedVendorName;
        final String priority;
        final String comments;
        final boolean isSolarSystemWorking;
        
        TicketBasicInfo(String documentId, Map<String, Object> incident, Map<String, Object> data,
                       Long createdTime, String mappedVendorName, String priority, String comments,
                       boolean isSolarSystemWorking) {
            this.documentId = documentId;
            this.createdTime = createdTime;
            this.mappedVendorName = mappedVendorName;
            this.priority = priority;
            this.comments = comments;
            this.isSolarSystemWorking = isSolarSystemWorking;
        }
    }

    private static class SLAComplianceInfo {
        final Long slaBreachTime;
        final boolean slaComplianceCurrentStatus;
        final boolean slaComplianceOverallTicket;
        final String definedSlaDurationCurrentStatus;
        final String definedOverallSlaDuration;
        
        SLAComplianceInfo(Long slaBreachTime, boolean slaComplianceCurrentStatus, boolean slaComplianceOverallTicket,
                         String definedSlaDurationCurrentStatus, String definedOverallSlaDuration) {
            this.slaBreachTime = slaBreachTime;
            this.slaComplianceCurrentStatus = slaComplianceCurrentStatus;
            this.slaComplianceOverallTicket = slaComplianceOverallTicket;
            this.definedSlaDurationCurrentStatus = definedSlaDurationCurrentStatus;
            this.definedOverallSlaDuration = definedOverallSlaDuration;
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
        log.trace("Extracting Long value from map, key: {}", key);
        Object value = map.get(key);
        if (value == null) {
            log.debug("Value is null for key: {}", key);
            return null;
        }
        if (value instanceof Long) return (Long) value;
        if (value instanceof Integer) return ((Integer) value).longValue();
        if (value instanceof String) {
            try {
                Long parsed = Long.parseLong((String) value);
                log.debug("Parsed Long value for key '{}': {}", key, parsed);
                return parsed;
            } catch (NumberFormatException e) {
                log.warn("Could not parse Long value for key '{}': {}", key, value);
                return null;
            }
        }
        log.debug("Value type not supported for key '{}': {}", key, value.getClass().getSimpleName());
        return null;
    }

    /**
     * Get long value from object with null safety
     */
    private Long getLongValue(Object value) {
        log.trace("Converting object to Long value");
        if (value instanceof Number) {
            Long result = ((Number) value).longValue();
            log.debug("Converted Number to Long: {}", result);
            return result;
        }
        log.debug("Object is not a Number, returning null");
        return null;
    }

    /**
     * Extract vendor name from data
     */
    private String extractVendorName(Map<String, Object> data) {
        log.trace("Extracting vendor name from data");
        // Try to get vendor name from mappedVendorName field
        String vendorName = (String) data.get("mappedVendorName");
        if (vendorName != null && !vendorName.isEmpty()) {
            log.debug("Found vendor name: {}", vendorName);
            return vendorName;
        }

        // Try to get vendor name from mappedVendorUserName field
        String vendorUserName = (String) data.get("mappedVendorUserName");
        if (vendorUserName != null && !vendorUserName.isEmpty()) {
            log.debug("Found vendor user name: {}", vendorUserName);
            return vendorUserName;
        }

        log.debug("No vendor name found, returning default");
        return "Not Assigned";
    }

    /**
     * Extract priority from business service name
     * Business service format: "Incident_High", "Incident_Low", "Incident_Medium"
     * Priority is the part after the underscore
     */
    private String extractPriorityFromBusinessService(Map<String, Object> data) {
        log.trace("Extracting priority from business service");
        try {
            Map<String, Object> currentProcessInstance = (Map<String, Object>) data.get("currentProcessInstance");
            if (currentProcessInstance == null) {
                log.debug("Current process instance is null, using default priority");
                return "Medium"; // Default fallback
            }

            Object businessServiceObj = currentProcessInstance.get("businessService");
            if (businessServiceObj instanceof String businessService && businessService.contains("_")) {
                String[] parts = businessService.split("_", 2);
                if (parts.length > 1) {
                    String priority = parts[1]; // Return part after underscore (High, Low, Medium)
                    log.debug("Extracted priority: {} from business service: {}", priority, businessService);
                    return priority;
                }
            }

            log.debug("Could not extract priority from business service, using default");
            return "Medium"; // Default fallback
        } catch (Exception e) {
            log.warn("Error extracting priority from business service: {}", e.getMessage());
            return "Medium"; // Default fallback
        }
    }

    private String getBaseUrl() {
        log.trace("Getting Elasticsearch base URL");
        String url = esHost + ":" + esPort;
        log.debug("Elasticsearch base URL: {}", url);
        return url;
    }
}
