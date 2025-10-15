package org.selco.e4h.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.egov.common.contract.request.RequestInfo;
import org.selco.e4h.util.ElasticSearchClient;
import org.selco.e4h.web.models.EscalationInfo;
import org.selco.e4h.web.models.EscalationLevel;
import org.selco.e4h.web.models.EscalationTicket;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Service to detect SLA breaches and fetch relevant tickets
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SLABreachDetectionService {
    
    private final ElasticSearchClient elasticSearchClient;
    private final EscalationMasterDataService escalationMasterDataService;
    
    // Cache for escalation level configurations from MDMS
    private Map<String, EscalationLevel> escalationLevelCache = new HashMap<>();
    private long lastEscalationLevelCacheRefresh = 0;
    private static final long ESCALATION_LEVEL_CACHE_INTERVAL = 3600000; // 1 hour

    /**
     * Find tickets in SLA breach for a specific tenant, workflow states, and escalation level
     * that don't already have the specified escalation recipient ID
     * Updated to support MDMS-driven breach threshold calculation (percentage or number strategy)
     */
    public List<EscalationTicket> findSLABreachTickets(String tenantId, List<String> workflowStates, 
                                                       String escalationRecipientId, String escalationLevel,
                                                       RequestInfo requestInfo) {
        try {
            log.info("Finding SLA breach tickets for tenant: {}, workflow states: {}, escalation level: {}, excluding escalation: {}", 
                tenantId, workflowStates, escalationLevel, escalationRecipientId);
            
            // Build Elasticsearch query for SLA breach tickets with escalation level threshold from MDMS
            Map<String, Object> query = buildSLABreachQueryWithLevel(tenantId, workflowStates, 
                escalationRecipientId, escalationLevel, requestInfo);
            
            // Execute query using ElasticsearchClient
            List<EscalationTicket> breachTickets = elasticSearchClient.searchTickets(query);
            
            // The Elasticsearch query already filters for SLA breach and escalation exclusions
            // Only apply post-filtering for special cases like LEVEL_TWO aged tickets
            List<EscalationTicket> filteredTickets = new ArrayList<>();
            int additionalFilterCount = 0;
            long currentTime = System.currentTimeMillis();
            
            for (EscalationTicket ticket : breachTickets) {
                // For LEVEL_TWO, apply additional age filtering (16+ hours breached)
                if ("LEVEL_TWO".equals(escalationLevel)) {
                    if (isTicketAgedBeyondBreach(ticket, currentTime, 16.0)) {
                        filteredTickets.add(ticket);
                        log.debug("Ticket {} included in LEVEL_TWO escalation - breached for more than 16 hours", 
                            ticket.getIncidentId());
                    } else {
                        additionalFilterCount++;
                        log.debug("Skipping ticket {} - not aged enough for LEVEL_TWO escalation", 
                            ticket.getIncidentId());
                    }
                } else {
                    // For LEVEL_ZERO and LEVEL_ONE, use tickets as returned by Elasticsearch
                    filteredTickets.add(ticket);
                }
            }
            
            log.info("Found {} tickets in SLA breach for tenant: {} with escalation level: {} ({} additional filters, {} final)", 
                breachTickets.size(), tenantId, escalationLevel, additionalFilterCount, filteredTickets.size());
            
            return filteredTickets;
            
        } catch (Exception e) {
            log.error("Error finding SLA breach tickets for tenant: {} with escalation level: {}", 
                tenantId, escalationLevel, e);
            // Fallback to empty list if query fails
            return new ArrayList<>();
        }
    }
    
    
    /**
     * Find tickets in SLA breach for country level (all tenants)
     */
    public List<EscalationTicket> findSLABreachTicketsForCountry(List<String> workflowStates, String escalationRecipientId) {
        try {
            log.info("Finding SLA breach tickets for country level, workflow states: {}, excluding escalation: {}", 
                workflowStates, escalationRecipientId);
            
            // Fetch tickets from Elasticsearch
            List<Map<String, Object>> tickets = elasticSearchClient.fetchRequiredTickets(0, 10000, false);
            
            List<EscalationTicket> breachTickets = new ArrayList<>();
            long currentTime = System.currentTimeMillis();
            
            for (Map<String, Object> ticket : tickets) {
                try {
                    EscalationTicket escalationTicket = convertToEscalationTicket(ticket);
                    
                    // Check if ticket is in the specified workflow states
                    if (!workflowStates.contains(escalationTicket.getApplicationStatus())) {
                        continue;
                    }
                    
                    // Check if ticket is already escalated to this recipient
                    if (isAlreadyEscalated(escalationTicket, escalationRecipientId)) {
                        continue;
                    }
                    
                    // Check if ticket is in SLA breach
                    if (isInSLABreach(escalationTicket, currentTime)) {
                        breachTickets.add(escalationTicket);
                    }
                    
                } catch (Exception e) {
                    log.warn("Error processing ticket: {}", ticket.get("id"), e);
                }
            }
            
            log.info("Found {} tickets in SLA breach for country level", breachTickets.size());
            return breachTickets;
            
        } catch (Exception e) {
            log.error("Error finding SLA breach tickets for country level", e);
            return new ArrayList<>();
        }
    }
    
    /**
     * Find tickets in SLA breach for country level (all tenants) with escalation level
     * Updated to support MDMS-driven breach threshold calculation
     */
    public List<EscalationTicket> findSLABreachTicketsForCountry(List<String> workflowStates, 
                                                                 String escalationRecipientId, 
                                                                 String escalationLevel,
                                                                 RequestInfo requestInfo) {
        try {
            log.info("Finding SLA breach tickets for country level, workflow states: {}, escalation level: {}, excluding escalation: {}", 
                workflowStates, escalationLevel, escalationRecipientId);
            
            // Build Elasticsearch query for SLA breach tickets with escalation level threshold from MDMS
            Map<String, Object> query = buildSLABreachQueryWithLevelForCountry(workflowStates, 
                escalationRecipientId, escalationLevel, requestInfo);
            
            // Execute query using ElasticsearchClient
            List<EscalationTicket> breachTickets = elasticSearchClient.searchTickets(query);
            
            log.info("Found {} tickets in SLA breach for country level with escalation level: {}", 
                breachTickets.size(), escalationLevel);
            return breachTickets;
            
        } catch (Exception e) {
            log.error("Error finding SLA breach tickets for country level with escalation level: {}", escalationLevel, e);
            return new ArrayList<>();
        }
    }
    
    
    /**
     * Convert Elasticsearch ticket data to EscalationTicket model
     */
    private EscalationTicket convertToEscalationTicket(Map<String, Object> ticketData) {
        try {
            log.debug("Converting ticket data: {}", ticketData.keySet());
            
            // Handle the actual Elasticsearch structure
            Map<String, Object> source = (Map<String, Object>) ticketData.get("_source");
            if (source == null) {
                log.warn("No _source found in ticket data: {}", ticketData.keySet());
                return createEmptyEscalationTicket();
            }
            
            log.debug("Source data keys: {}", source.keySet());
            
            Map<String, Object> data = (Map<String, Object>) source.get("Data");
            if (data == null) {
                log.warn("No Data found in _source: {}", source.keySet());
                return createEmptyEscalationTicket();
            }
            
            log.debug("Data keys: {}", data.keySet());
            
            // Extract incident data (nested within Data)
            Map<String, Object> incident = (Map<String, Object>) data.get("incident");
            if (incident == null) {
                log.warn("No incident found in Data: {}", data.keySet());
                return createEmptyEscalationTicket();
            }
            
            log.debug("Incident data keys: {}", incident.keySet());
            
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
            
            // Extract priority from incident
            String priority = (String) incident.get("priority");
            
            // Extract comments from incident
            String comments = (String) incident.get("comments");
            
            // Determine SLA compliance status
            boolean slaComplianceCurrentStatus = slaRemaining != null && getLongValue(slaRemaining) > 0;
            boolean slaComplianceOverallTicket = totalSlaRemaining != null && getLongValue(totalSlaRemaining) > 0;
            
            // Format SLA durations
            String definedSlaDurationCurrentStatus = formatSlaDuration(stateSla);
            String definedOverallSlaDuration = formatSlaDuration(totalSlaRemaining);
            
            // Determine if solar system is working based on system functional status
            boolean isSolarSystemWorking = "FUNCTIONAL".equals(data.get("systemFunctional"));
            
            EscalationTicket ticket = EscalationTicket.builder()
                .id((String) ticketData.get("_id"))
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
                .healthFacilityName((String) data.get("phcType"))  // phcType is in Data
                .healthFacilityType((String) data.get("phcSubType")) // phcSubType might be in Data
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
                
            // Log all the extracted values for debugging
            log.debug("Extracted values - id: {}, tenantId: {}, incidentId: {}, applicationStatus: {}, district: {}, block: {}, phcType: {}", 
                ticket.getId(), ticket.getTenantId(), ticket.getIncidentId(), 
                ticket.getApplicationStatus(), ticket.getDistrict(), ticket.getBlock(), ticket.getHealthFacilityName());
                
            log.debug("Created EscalationTicket: id={}, tenantId={}, incidentId={}, applicationStatus={}, district={}, block={}", 
                ticket.getId(), ticket.getTenantId(), ticket.getIncidentId(), 
                ticket.getApplicationStatus(), ticket.getDistrict(), ticket.getBlock());
                
            return ticket;
                
        } catch (Exception e) {
            log.error("Error converting ticket data to EscalationTicket: {}", e.getMessage(), e);
            return createEmptyEscalationTicket();
        }
    }
    
    /**
     * Helper method to safely extract Long values from Map
     */
    private Long getLongValue(Map<String, Object> map, String key) {
        Object value = map.get(key);
        if (value instanceof Number) {
            return ((Number) value).longValue();
        }
        return null;
    }
    
    /**
     * Create an empty EscalationTicket for error cases
     */
    private EscalationTicket createEmptyEscalationTicket() {
        return EscalationTicket.builder()
            .id("unknown")
            .incidentId("unknown")
            .tenantId("unknown")
            .applicationStatus("unknown")
            .incidentType("unknown")
            .incidentSubType("unknown")
            .filedDate(0L)
            .slaBreachTime(null)
            .escalationInfo(new ArrayList<>())
            .additionalDetails(new HashMap<>())
            .ticketNumber("unknown")
            .district("unknown")
            .block("unknown")
            .healthFacilityName("unknown")
            .healthFacilityType("unknown")
            .issueType("unknown")
            .issueSubType("unknown")
            .currentTicketStatus("unknown")
            .ticketFiledDate(0L)
            .build();
    }
    
    /**
     * Parse escalations from ticket data
     */
    @SuppressWarnings("unchecked")
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
     * Enhanced to deduplicate and handle Elasticsearch array data properly
     */
    private List<EscalationInfo> parseEscalationList(List<Map<String, Object>> escalationsData) {
        // Use a map to deduplicate escalations by (escalationId, escalationLevel, escalationTime) combination
        Map<String, EscalationInfo> uniqueEscalations = new HashMap<>();
        
        for (Map<String, Object> escalationData : escalationsData) {
            try {
                String escalationId = (String) escalationData.get("escalationId");
                String escalationLevel = (String) escalationData.get("escalationLevel");
                Long escalationTime = getLongValue(escalationData, "escalationTime");
                String recipientRole = (String) escalationData.get("recipientRole");
                
                // Skip invalid escalation data
                if (escalationId == null || escalationLevel == null || escalationTime == null) {
                    log.warn("Skipping invalid escalation data: {}", escalationData);
                    continue;
                }
                
                // Create unique key for deduplication
                String uniqueKey = escalationId + "_" + escalationLevel + "_" + escalationTime;
                
                // Only keep if we haven't seen this exact escalation before
                if (!uniqueEscalations.containsKey(uniqueKey)) {
            EscalationInfo escalation = EscalationInfo.builder()
                        .escalationId(escalationId)
                        .escalationTime(escalationTime)
                        .escalationLevel(escalationLevel)
                        .recipientRole(recipientRole)
                .build();
                    uniqueEscalations.put(uniqueKey, escalation);
                }
                
            } catch (Exception e) {
                log.warn("Error parsing escalation data: {}", escalationData, e);
            }
        }
        
        List<EscalationInfo> result = new ArrayList<>(uniqueEscalations.values());
        
        // Sort by escalation time to maintain chronological order
        result.sort((a, b) -> {
            if (a.getEscalationTime() == null && b.getEscalationTime() == null) return 0;
            if (a.getEscalationTime() == null) return 1;
            if (b.getEscalationTime() == null) return -1;
            return Long.compare(a.getEscalationTime(), b.getEscalationTime());
        });
        
        log.debug("Parsed {} unique escalations from {} raw entries", result.size(), escalationsData.size());
        return result;
    }
    
    /**
     * Check if ticket is already escalated to the specified recipient
     */
    private boolean isAlreadyEscalated(EscalationTicket ticket, String escalationRecipientId) {
        if (ticket.getEscalationInfo() == null || ticket.getEscalationInfo().isEmpty()) {
            return false;
        }
        
        return ticket.getEscalationInfo().stream()
            .anyMatch(escalation -> escalationRecipientId.equals(escalation.getEscalationId()));
    }
    

    /**
     * Find tickets that were previously escalated (last week) but are now resolved/closed
     * For Weekly Summary - Feature 2 from Saura-eMitra Enhancements
     */
    public List<EscalationTicket> findPreviouslyEscalatedTickets(String tenantId, 
                                                               List<String> workflowStates, 
                                                               String escalationId,
                                                               Date fromDate, 
                                                               Date toDate) {
        try {
            log.info("Finding previously escalated tickets for tenant: {}, escalationId: {}, from: {} to: {}", 
                    tenantId, escalationId, fromDate, toDate);
            
            // Build Elasticsearch query for tickets that:
            // 1. Were escalated in the specified date range (have escalationId and escalationTime in range)
            // 2. Are now in Resolved, Closed, or On-Track status
            // 3. Match the workflow states
            Map<String, Object> query = buildWeeklySummaryQuery(tenantId, workflowStates, escalationId, fromDate, toDate);
            
            // Execute query
            List<EscalationTicket> tickets = elasticSearchClient.searchTickets(query);
            
            log.info("Found {} previously escalated tickets for tenant: {}", tickets.size(), tenantId);
            return tickets;
            
        } catch (Exception e) {
            log.error("Error finding previously escalated tickets for tenant: {}", tenantId, e);
            return new ArrayList<>();
        }
    }
    
    /**
     * Find previously escalated tickets for country level (all tenants)
     */
    public List<EscalationTicket> findPreviouslyEscalatedTicketsForCountry(List<String> workflowStates, 
                                                                          String escalationId,
                                                                          Date fromDate, 
                                                                          Date toDate) {
        try {
            log.info("Finding previously escalated tickets for country level, escalationId: {}, from: {} to: {}", 
                    escalationId, fromDate, toDate);
            
            // Build Elasticsearch query for tickets across all tenants
            Map<String, Object> query = buildWeeklySummaryCountryQuery(workflowStates, escalationId, fromDate, toDate);
            
            // Execute query
            List<EscalationTicket> tickets = elasticSearchClient.searchTickets(query);
            
            log.info("Found {} previously escalated tickets for country level", tickets.size());
            return tickets;
            
        } catch (Exception e) {
            log.error("Error finding previously escalated tickets for country level", e);
            return new ArrayList<>();
        }
    }
    
    /**
     * Find tickets that are currently in breach (escalated more than one week ago)
     * Part 2 of Weekly Summary - tickets that slipped through the cracks
     */
    public List<EscalationTicket> findCurrentlyInBreachTickets(String tenantId, 
                                                             List<String> workflowStates, 
                                                             String escalationId) {
        try {
            log.info("Finding currently in breach tickets for tenant: {}, escalationId: {}", tenantId, escalationId);
            
            // Build Elasticsearch query for tickets that:
            // 1. Are currently in SLA breach
            // 2. Were escalated more than one week ago (have escalationId and escalationTime > 1 week ago)
            // 3. Match the workflow states
            Map<String, Object> query = buildCurrentlyInBreachQuery(tenantId, workflowStates, escalationId);
            
            // Execute query
            List<EscalationTicket> tickets = elasticSearchClient.searchTickets(query);
            
            log.info("Found {} currently in breach tickets for tenant: {}", tickets.size(), tenantId);
            return tickets;
            
        } catch (Exception e) {
            log.error("Error finding currently in breach tickets for tenant: {}", tenantId, e);
            return new ArrayList<>();
        }
    }
    
    /**
     * Find currently in breach tickets for country level (all tenants)
     */
    public List<EscalationTicket> findCurrentlyInBreachTicketsForCountry(List<String> workflowStates, 
                                                                        String escalationId) {
        try {
            log.info("Finding currently in breach tickets for country level, escalationId: {}", escalationId);
            
            // Build Elasticsearch query for tickets across all tenants
            Map<String, Object> query = buildCurrentlyInBreachCountryQuery(workflowStates, escalationId);
            
            // Execute query
            List<EscalationTicket> tickets = elasticSearchClient.searchTickets(query);
            
            log.info("Found {} currently in breach tickets for country level", tickets.size());
            return tickets;
            
        } catch (Exception e) {
            log.error("Error finding currently in breach tickets for country level", e);
            return new ArrayList<>();
        }
    }
    
    /**
     * Build Elasticsearch query for weekly summary (state level)
     */
    private Map<String, Object> buildWeeklySummaryQuery(String tenantId, List<String> workflowStates, 
                                                       String escalationId, Date fromDate, Date toDate) {
        Map<String, Object> query = new HashMap<>();
        Map<String, Object> bool = new HashMap<>();
        List<Map<String, Object>> must = new ArrayList<>();
        List<Map<String, Object>> mustNot = new ArrayList<>();
        
        // Filter by tenant using wildcard to match districts (e.g., pg* matches pg.dummy, pg.bangalore, etc.)
        Map<String, Object> tenantFilter = new HashMap<>();
        Map<String, Object> tenantWildcard = new HashMap<>();
        tenantWildcard.put("tenantId.keyword", tenantId + "*");
        tenantFilter.put("wildcard", tenantWildcard);
        must.add(tenantFilter);
        
        // Filter by workflow states (current status should be resolved/closed/on-track)
        List<String> resolvedStates = Arrays.asList("RESOLVED", "CLOSED", "ON_TRACK", "COMPLETED");
        Map<String, Object> statusFilter = new HashMap<>();
        Map<String, Object> statusTerms = new HashMap<>();
        statusTerms.put("applicationStatus.keyword", resolvedStates);
        statusFilter.put("terms", statusTerms);
        must.add(statusFilter);
        
        // Filter by escalationId in escalations array
        Map<String, Object> escalationFilter = new HashMap<>();
        Map<String, Object> escalationNested = new HashMap<>();
        Map<String, Object> escalationQuery = new HashMap<>();
        Map<String, Object> escalationBool = new HashMap<>();
        List<Map<String, Object>> escalationMust = new ArrayList<>();
        
        Map<String, Object> escalationIdTerm = new HashMap<>();
        escalationIdTerm.put("escalations.escalationId.keyword", escalationId);
        Map<String, Object> termFilter = new HashMap<>();
        termFilter.put("term", escalationIdTerm);
        escalationMust.add(termFilter);
        
        // Filter by escalation time range
        Map<String, Object> escalationTimeRange = new HashMap<>();
        Map<String, Object> escalationTimeRangeQuery = new HashMap<>();
        escalationTimeRangeQuery.put("gte", fromDate.getTime());
        escalationTimeRangeQuery.put("lte", toDate.getTime());
        escalationTimeRange.put("escalations.escalationTime", escalationTimeRangeQuery);
        Map<String, Object> rangeFilter = new HashMap<>();
        rangeFilter.put("range", escalationTimeRange);
        escalationMust.add(rangeFilter);
        
        escalationBool.put("must", escalationMust);
        escalationQuery.put("bool", escalationBool);
        escalationNested.put("query", escalationQuery);
        escalationNested.put("path", "escalations");
        escalationFilter.put("nested", escalationNested);
        must.add(escalationFilter);
        
        bool.put("must", must);
        bool.put("must_not", mustNot);
        query.put("bool", bool);
        
        log.debug("Weekly summary query for tenant {}: {}", tenantId, query);
        return query;
    }
    
    /**
     * Build Elasticsearch query for weekly summary (country level)
     */
    private Map<String, Object> buildWeeklySummaryCountryQuery(List<String> workflowStates, 
                                                              String escalationId, Date fromDate, Date toDate) {
        Map<String, Object> query = new HashMap<>();
        Map<String, Object> bool = new HashMap<>();
        List<Map<String, Object>> should = new ArrayList<>();
        
        // Query 1: Tickets that were escalated last week (have escalationId and escalationTime from last week)
        Map<String, Object> previouslyEscalatedQuery = buildPreviouslyEscalatedQuery(escalationId, fromDate, toDate);
        should.add(previouslyEscalatedQuery);
        
        // Query 2: Tickets that are currently in breach of SLA and have applicationStatus in workflowStates
        Map<String, Object> currentlyInBreachQuery = buildCurrentlyInBreachQueryForCountry(workflowStates, escalationId);
        should.add(currentlyInBreachQuery);
        
        // Use minimum_should_match to ensure at least one of the queries matches
        bool.put("should", should);
        bool.put("minimum_should_match", 1);
        query.put("bool", bool);
        
        log.debug("Weekly summary country query (union of 2 queries): {}", query);
        return query;
    }
    
    /**
     * Build query for tickets that were escalated last week (Query 1)
     */
    private Map<String, Object> buildPreviouslyEscalatedQuery(String escalationId, Date fromDate, Date toDate) {
        Map<String, Object> query = new HashMap<>();
        Map<String, Object> bool = new HashMap<>();
        List<Map<String, Object>> must = new ArrayList<>();
        
        // Filter by escalationId in escalations array
        Map<String, Object> escalationFilter = new HashMap<>();
        Map<String, Object> escalationNested = new HashMap<>();
        Map<String, Object> escalationQuery = new HashMap<>();
        Map<String, Object> escalationBool = new HashMap<>();
        List<Map<String, Object>> escalationMust = new ArrayList<>();
        
        Map<String, Object> escalationIdTerm = new HashMap<>();
        escalationIdTerm.put("Data.incident.escalations.escalationId.keyword", escalationId);
        Map<String, Object> termFilter = new HashMap<>();
        termFilter.put("term", escalationIdTerm);
        escalationMust.add(termFilter);
        
        // Filter by escalation time range (last week)
        Map<String, Object> escalationTimeRange = new HashMap<>();
        Map<String, Object> escalationTimeRangeQuery = new HashMap<>();
        escalationTimeRangeQuery.put("gte", fromDate.getTime());
        escalationTimeRangeQuery.put("lte", toDate.getTime());
        escalationTimeRange.put("Data.incident.escalations.escalationTime", escalationTimeRangeQuery);
        Map<String, Object> rangeFilter = new HashMap<>();
        rangeFilter.put("range", escalationTimeRange);
        escalationMust.add(rangeFilter);
        
        escalationBool.put("must", escalationMust);
        escalationQuery.put("bool", escalationBool);
        escalationNested.put("query", escalationQuery);
        escalationNested.put("path", "Data.incident.escalations");
        escalationFilter.put("nested", escalationNested);
        must.add(escalationFilter);
        
        bool.put("must", must);
        query.put("bool", bool);
        
        return query;
    }
    
    /**
     * Build query for tickets currently in breach of SLA (Query 2)
     */
    private Map<String, Object> buildCurrentlyInBreachQueryForCountry(List<String> workflowStates, String escalationId) {
        Map<String, Object> query = new HashMap<>();
        Map<String, Object> bool = new HashMap<>();
        List<Map<String, Object>> must = new ArrayList<>();
        List<Map<String, Object>> mustNot = new ArrayList<>();
        
        // Filter by workflow states (current application status should be in workflowStates)
        Map<String, Object> statusFilter = new HashMap<>();
        Map<String, Object> statusTerms = new HashMap<>();
        statusTerms.put("Data.incident.applicationStatus.keyword", workflowStates);
        statusFilter.put("terms", statusTerms);
        must.add(statusFilter);
        
        // Filter by current SLA breach (slaRemaining <= 0)
        Map<String, Object> slaBreachFilter = new HashMap<>();
        Map<String, Object> slaBreachRange = new HashMap<>();
        slaBreachRange.put("lte", 0);
        slaBreachFilter.put("Data.slaRemaining", slaBreachRange);
        Map<String, Object> rangeFilter = new HashMap<>();
        rangeFilter.put("range", slaBreachFilter);
        must.add(rangeFilter);
        
        // Exclude tickets already escalated to this recipient
        if (escalationId != null) {
            Map<String, Object> escalationFilter = new HashMap<>();
            Map<String, Object> escalationNested = new HashMap<>();
            Map<String, Object> escalationQuery = new HashMap<>();
            Map<String, Object> escalationBool = new HashMap<>();
            List<Map<String, Object>> escalationMust = new ArrayList<>();
            
            Map<String, Object> escalationIdTerm = new HashMap<>();
            escalationIdTerm.put("Data.incident.escalations.escalationId.keyword", escalationId);
            Map<String, Object> termFilter = new HashMap<>();
            termFilter.put("term", escalationIdTerm);
            escalationMust.add(termFilter);
            
            escalationBool.put("must", escalationMust);
            escalationQuery.put("bool", escalationBool);
            escalationNested.put("query", escalationQuery);
            escalationNested.put("path", "Data.incident.escalations");
            escalationFilter.put("nested", escalationNested);
            mustNot.add(escalationFilter);
        }
        
        bool.put("must", must);
        bool.put("must_not", mustNot);
        query.put("bool", bool);
        
        return query;
    }
    
    /**
     * Build Elasticsearch query for currently in breach tickets (state level)
     */
    private Map<String, Object> buildCurrentlyInBreachQuery(String tenantId, List<String> workflowStates, String escalationId) {
        Map<String, Object> query = new HashMap<>();
        Map<String, Object> bool = new HashMap<>();
        List<Map<String, Object>> must = new ArrayList<>();
        List<Map<String, Object>> mustNot = new ArrayList<>();
        
        // Filter by tenant using wildcard to match districts (e.g., pg* matches pg.dummy, pg.bangalore, etc.)
        Map<String, Object> tenantFilter = new HashMap<>();
        Map<String, Object> tenantWildcard = new HashMap<>();
        tenantWildcard.put("tenantId.keyword", tenantId + "*");
        tenantFilter.put("wildcard", tenantWildcard);
        must.add(tenantFilter);
        
        // Filter by workflow states (current status should be breach states)
        Map<String, Object> statusFilter = new HashMap<>();
        Map<String, Object> statusTerms = new HashMap<>();
        statusTerms.put("applicationStatus.keyword", workflowStates);
        statusFilter.put("terms", statusTerms);
        must.add(statusFilter);
        
        // Filter by escalationId in escalations array
        Map<String, Object> escalationFilter = new HashMap<>();
        Map<String, Object> escalationNested = new HashMap<>();
        Map<String, Object> escalationQuery = new HashMap<>();
        Map<String, Object> escalationBool = new HashMap<>();
        List<Map<String, Object>> escalationMust = new ArrayList<>();
        
        Map<String, Object> escalationIdTerm = new HashMap<>();
        escalationIdTerm.put("escalations.escalationId.keyword", escalationId);
        Map<String, Object> termFilter = new HashMap<>();
        termFilter.put("term", escalationIdTerm);
        escalationMust.add(termFilter);
        
        // Filter by escalation time > 1 week ago
        Date oneWeekAgo = new Date(System.currentTimeMillis() - (7 * 24 * 60 * 60 * 1000L));
        Map<String, Object> escalationTimeRange = new HashMap<>();
        Map<String, Object> escalationTimeRangeQuery = new HashMap<>();
        escalationTimeRangeQuery.put("lt", oneWeekAgo.getTime());
        escalationTimeRange.put("escalations.escalationTime", escalationTimeRangeQuery);
        Map<String, Object> rangeFilter = new HashMap<>();
        rangeFilter.put("range", escalationTimeRange);
        escalationMust.add(rangeFilter);
        
        escalationBool.put("must", escalationMust);
        escalationQuery.put("bool", escalationBool);
        escalationNested.put("query", escalationQuery);
        escalationNested.put("path", "escalations");
        escalationFilter.put("nested", escalationNested);
        must.add(escalationFilter);
        
        // Filter by current SLA breach (slaRemaining < 0 in additionalDetails)
        Map<String, Object> slaBreachFilter = new HashMap<>();
        Map<String, Object> slaBreachRange = new HashMap<>();
        slaBreachRange.put("lte", 0);
        slaBreachFilter.put("slaRemaining", slaBreachRange);
        Map<String, Object> rangeFilter2 = new HashMap<>();
        rangeFilter2.put("range", slaBreachFilter);
        must.add(rangeFilter2);
        
        bool.put("must", must);
        bool.put("must_not", mustNot);
        query.put("bool", bool);
        
        log.debug("Currently in breach query for tenant {}: {}", tenantId, query);
        return query;
    }
    
    /**
     * Build Elasticsearch query for currently in breach tickets (country level)
     */
    private Map<String, Object> buildCurrentlyInBreachCountryQuery(List<String> workflowStates, String escalationId) {
        Map<String, Object> query = new HashMap<>();
        Map<String, Object> bool = new HashMap<>();
        List<Map<String, Object>> must = new ArrayList<>();
        List<Map<String, Object>> mustNot = new ArrayList<>();
        
        // Filter by workflow states (current status should be breach states)
        Map<String, Object> statusFilter = new HashMap<>();
        Map<String, Object> statusTerms = new HashMap<>();
        statusTerms.put("applicationStatus.keyword", workflowStates);
        statusFilter.put("terms", statusTerms);
        must.add(statusFilter);
        
        // Filter by escalationId in escalations array
        Map<String, Object> escalationFilter = new HashMap<>();
        Map<String, Object> escalationNested = new HashMap<>();
        Map<String, Object> escalationQuery = new HashMap<>();
        Map<String, Object> escalationBool = new HashMap<>();
        List<Map<String, Object>> escalationMust = new ArrayList<>();
        
        Map<String, Object> escalationIdTerm = new HashMap<>();
        escalationIdTerm.put("escalations.escalationId.keyword", escalationId);
        Map<String, Object> termFilter = new HashMap<>();
        termFilter.put("term", escalationIdTerm);
        escalationMust.add(termFilter);
        
        // Filter by escalation time > 1 week ago
        Date oneWeekAgo = new Date(System.currentTimeMillis() - (7 * 24 * 60 * 60 * 1000L));
        Map<String, Object> escalationTimeRange = new HashMap<>();
        Map<String, Object> escalationTimeRangeQuery = new HashMap<>();
        escalationTimeRangeQuery.put("lt", oneWeekAgo.getTime());
        escalationTimeRange.put("escalations.escalationTime", escalationTimeRangeQuery);
        Map<String, Object> rangeFilter = new HashMap<>();
        rangeFilter.put("range", escalationTimeRange);
        escalationMust.add(rangeFilter);
        
        escalationBool.put("must", escalationMust);
        escalationQuery.put("bool", escalationBool);
        escalationNested.put("query", escalationQuery);
        escalationNested.put("path", "escalations");
        escalationFilter.put("nested", escalationNested);
        must.add(escalationFilter);
        
        // Filter by current SLA breach (slaRemaining < 0 in additionalDetails)
        Map<String, Object> slaBreachFilter = new HashMap<>();
        Map<String, Object> slaBreachRange = new HashMap<>();
        slaBreachRange.put("lte", 0);
        slaBreachFilter.put("slaRemaining", slaBreachRange);
        Map<String, Object> rangeFilter2 = new HashMap<>();
        rangeFilter2.put("range", slaBreachFilter);
        must.add(rangeFilter2);
        
        bool.put("must", must);
        bool.put("must_not", mustNot);
        query.put("bool", bool);
        
        log.debug("Currently in breach country query: {}", query);
        return query;
    }
    
    /**
     * Check if ticket is in SLA breach based on escalation level threshold
     * Enhanced to support breach age tracking for aged ticket identification
     */
    private boolean isInSLABreach(EscalationTicket ticket, long currentTime) {
        // If SLA breach time is set and current time is past the breach time
        if (ticket.getSlaBreachTime() != null && currentTime >= ticket.getSlaBreachTime()) {
            return true;
        }
        
        // Check SLA breach based on slaRemaining value and escalation level threshold
        if (ticket.getAdditionalDetails() != null) {
            Object slaRemaining = ticket.getAdditionalDetails().get("slaRemaining");
            if (slaRemaining instanceof Number) {
                double slaRemainingValue = ((Number) slaRemaining).doubleValue();
                
                // Convert slaRemaining from milliseconds to hours for comparison
                double slaRemainingHours = slaRemainingValue / (1000.0 * 60.0 * 60.0);
                
                // Check against escalation level thresholds
                // LEVEL_ONE: breach threshold = 0 hours (escalation when SLA completed)
                // LEVEL_TWO: breach threshold = -16 hours (escalation when 16 hours overdue)
                
                // For now, we'll check if slaRemaining <= 0 (LEVEL_ONE threshold)
                // The escalation level will be determined by the escalation recipient configuration
                if (slaRemainingHours <= 0) {
                    log.debug("Ticket {} is in SLA breach: slaRemaining = {} hours ({} ms)", 
                        ticket.getIncidentId(), slaRemainingHours, slaRemainingValue);
                    return true;
                }
            }
            
            // Also check totalSlaRemaining for overall SLA breach
            Object totalSlaRemaining = ticket.getAdditionalDetails().get("totalSlaRemaining");
            if (totalSlaRemaining instanceof Number) {
                double totalSlaRemainingValue = ((Number) totalSlaRemaining).doubleValue();
                double totalSlaRemainingHours = totalSlaRemainingValue / (1000.0 * 60.0 * 60.0);
                
                if (totalSlaRemainingHours <= 0) {
                    log.debug("Ticket {} is in total SLA breach: totalSlaRemaining = {} hours ({} ms)", 
                        ticket.getIncidentId(), totalSlaRemainingHours, totalSlaRemainingValue);
                    return true;
                }
            }
        }
        
        return false;
    }
    
    /**
     * Check if ticket has been breached for more than specified hours (for aged ticket identification)
     * Used for L2 escalation requirements (tickets breached for more than 2 business days)
     */
    public boolean isTicketAgedBeyondBreach(EscalationTicket ticket, long currentTime, double maxBreachHours) {
        if (ticket.getAdditionalDetails() == null) {
            return false;
        }
        
        // Check if ticket is currently in breach
        if (!isInSLABreach(ticket, currentTime)) {
            return false;
        }
        
        // Calculate how long the ticket has been breached
        Object slaRemaining = ticket.getAdditionalDetails().get("slaRemaining");
        if (slaRemaining instanceof Number) {
            double slaRemainingValue = ((Number) slaRemaining).doubleValue();
            double slaRemainingHours = slaRemainingValue / (1000.0 * 60.0 * 60.0);
            
            // If slaRemaining is negative, calculate breach duration
            if (slaRemainingHours < 0) {
                double breachDurationHours = Math.abs(slaRemainingHours);
                
                log.debug("Ticket {} breach duration: {} hours, threshold: {} hours", 
                    ticket.getIncidentId(), breachDurationHours, maxBreachHours);
                
                return breachDurationHours > maxBreachHours;
            }
        }
        
        // Also check totalSlaRemaining for overall breach age
        Object totalSlaRemaining = ticket.getAdditionalDetails().get("totalSlaRemaining");
        if (totalSlaRemaining instanceof Number) {
            double totalSlaRemainingValue = ((Number) totalSlaRemaining).doubleValue();
            double totalSlaRemainingHours = totalSlaRemainingValue / (1000.0 * 60.0 * 60.0);
            
            if (totalSlaRemainingHours < 0) {
                double breachDurationHours = Math.abs(totalSlaRemainingHours);
                
                log.debug("Ticket {} total breach duration: {} hours, threshold: {} hours", 
                    ticket.getIncidentId(), breachDurationHours, maxBreachHours);
                
                return breachDurationHours > maxBreachHours;
            }
        }
        
        return false;
    }

    /**
     * Build Elasticsearch query for SLA breach tickets with escalation level threshold from MDMS
     * Supports both "percentage" and "number" breach calculation strategies per LLD V2
     */
    private Map<String, Object> buildSLABreachQueryWithLevel(String tenantId, List<String> workflowStates, 
                                                             String escalationRecipientId, String escalationLevel,
                                                             RequestInfo requestInfo) {
        Map<String, Object> query = new HashMap<>();
        Map<String, Object> bool = new HashMap<>();
        List<Map<String, Object>> must = new ArrayList<>();

        // Filter by tenant
        Map<String, Object> tenantFilter = new HashMap<>();
        Map<String, Object> tenantWildcard = new HashMap<>();
        tenantWildcard.put("Data.incident.tenantId.keyword", tenantId + "*");
        tenantFilter.put("wildcard", tenantWildcard);
        must.add(tenantFilter);

        // Filter by workflow states
        Map<String, Object> statusFilter = new HashMap<>();
        Map<String, Object> statusTerms = new HashMap<>();
        statusTerms.put("Data.incident.applicationStatus.keyword", workflowStates);
        statusFilter.put("terms", statusTerms);
        must.add(statusFilter);

        // Filter by SLA breach based on escalation level configuration from MDMS
        EscalationLevel escalationLevelConfig = getEscalationLevelConfig(escalationLevel, requestInfo);
        
        if (escalationLevelConfig == null) {
            log.error("EscalationLevel config not found for {} - MDMS configuration is required", escalationLevel);
            throw new RuntimeException("EscalationLevel configuration not found for " + escalationLevel + ". Please ensure MDMS is properly configured.");
        }
        
        // Build SLA filter based on calculation strategy from MDMS
            Map<String, Object> slaFilter = buildSLAFilter(escalationLevel, escalationLevelConfig);
            if (slaFilter != null) {
                must.add(slaFilter);
            log.debug("Added SLA filter for {} using strategy: {} with threshold: {} hours / {}%", 
                escalationLevel, 
                escalationLevelConfig.getBreachCalculationStrategy(),
                escalationLevelConfig.getBreachThresholdInHours(),
                escalationLevelConfig.getBreachThresholdInPercentage());
        }

        // Exclude tickets already escalated to this recipient AND level
        List<Map<String, Object>> mustNot = new ArrayList<>();
        if (escalationRecipientId != null) {
            Map<String, Object> escalationFilter = new HashMap<>();
            Map<String, Object> escalationNested = new HashMap<>();
            Map<String, Object> escalationQuery = new HashMap<>();
            Map<String, Object> escalationBool = new HashMap<>();
            List<Map<String, Object>> escalationMust = new ArrayList<>();
            
            // Check for same escalationId AND escalationLevel
            Map<String, Object> escalationIdTerm = new HashMap<>();
            escalationIdTerm.put("Data.incident.escalations.escalationId.keyword", escalationRecipientId);
            Map<String, Object> termFilter = new HashMap<>();
            termFilter.put("term", escalationIdTerm);
            escalationMust.add(termFilter);
            
            Map<String, Object> escalationLevelTerm = new HashMap<>();
            escalationLevelTerm.put("Data.incident.escalations.escalationLevel.keyword", escalationLevel);
            Map<String, Object> levelFilter = new HashMap<>();
            levelFilter.put("term", escalationLevelTerm);
            escalationMust.add(levelFilter);
            
            escalationBool.put("must", escalationMust);
            escalationQuery.put("bool", escalationBool);
            escalationNested.put("query", escalationQuery);
            escalationNested.put("path", "Data.incident.escalations");
            escalationFilter.put("nested", escalationNested);
            mustNot.add(escalationFilter);
        }

        bool.put("must", must);
        bool.put("must_not", mustNot);
        query.put("bool", bool);

        log.debug("SLA breach query for tenant {} with escalation level {}: {}", 
            tenantId, escalationLevel, query);
        return query;
    }

    /**
     * Refresh escalation level cache from MDMS if needed
     */
    private synchronized void refreshEscalationLevelCacheIfNeeded(RequestInfo requestInfo) {
        long currentTime = System.currentTimeMillis();
        
        if (escalationLevelCache.isEmpty() || 
            (currentTime - lastEscalationLevelCacheRefresh) > ESCALATION_LEVEL_CACHE_INTERVAL) {
            try {
                log.info("Refreshing escalation level cache from MDMS");
                List<EscalationLevel> levels = escalationMasterDataService.fetchEscalationLevels(requestInfo);
                
                escalationLevelCache.clear();
                for (EscalationLevel level : levels) {
                    if (level.getActive() != null && level.getActive()) {
                        escalationLevelCache.put(level.getEscalationLevel(), level);
                        log.debug("Cached escalation level: {} with strategy: {}, threshold: {} hours / {}%",
                            level.getEscalationLevel(), 
                            level.getBreachCalculationStrategy(),
                            level.getBreachThresholdInHours(),
                            level.getBreachThresholdInPercentage());
                    }
                }
                
                lastEscalationLevelCacheRefresh = currentTime;
                log.info("Successfully refreshed escalation level cache with {} entries", escalationLevelCache.size());
                
            } catch (Exception e) {
                log.error("Error refreshing escalation level cache from MDMS", e);
            }
        }
    }
    
    /**
     * Get escalation level configuration from cache
     */
    private EscalationLevel getEscalationLevelConfig(String escalationLevel, RequestInfo requestInfo) {
        refreshEscalationLevelCacheIfNeeded(requestInfo);
        return escalationLevelCache.get(escalationLevel);
    }
    
    /**
     * Build SLA filter based on escalation level configuration
     * Supports both "percentage" and "number" strategies from LLD V2
     */
    private Map<String, Object> buildSLAFilter(String escalationLevel, EscalationLevel config) {
        String strategy = config.getBreachCalculationStrategy();
        
        if ("percentage".equalsIgnoreCase(strategy)) {
            return buildPercentageBasedSLAFilter(escalationLevel, config);
        } else if ("number".equalsIgnoreCase(strategy)) {
            return buildNumberBasedSLAFilter(escalationLevel, config);
        } else {
            log.warn("Unknown breach calculation strategy: {} for level: {}", strategy, escalationLevel);
            return buildNumberBasedSLAFilter(escalationLevel, config);
        }
    }
    
    /**
     * Build percentage-based SLA filter (for LEVEL_ZERO with 70% threshold)
     * Triggers when SLA has elapsed 70% (30% remaining)
     */
    private Map<String, Object> buildPercentageBasedSLAFilter(String escalationLevel, EscalationLevel config) {
        Integer percentage = config.getBreachThresholdInPercentage();
        
        if (percentage == null || percentage <= 0) {
            log.warn("Invalid percentage threshold for {}: {}, using 70% default", escalationLevel, percentage);
            percentage = 70;
        }
        
        // For percentage-based: we need to check slaRemaining/totalSla ratio
        // If 70% threshold: trigger when (slaRemaining/totalSla) <= 0.30 (30% remaining)
        // This requires a script query in Elasticsearch
        
        Map<String, Object> scriptFilter = new HashMap<>();
        Map<String, Object> script = new HashMap<>();
        
        double remainingPercentageThreshold = (100.0 - percentage) / 100.0; // 30% = 0.30
        
        // Script to calculate: (slaRemaining / totalSlaRemaining) <= 0.30
        String scriptSource = String.format(
            "doc['Data.slaRemaining'].size() > 0 && doc['Data.totalSlaRemaining'].size() > 0 && " +
            "doc['Data.slaRemaining'].value > 0 && " +
            "((double)doc['Data.slaRemaining'].value / (double)doc['Data.totalSlaRemaining'].value) <= %.2f",
            remainingPercentageThreshold
        );
        
        script.put("source", scriptSource);
        script.put("lang", "painless");
        scriptFilter.put("script", script);
        
        Map<String, Object> filter = new HashMap<>();
        filter.put("script", scriptFilter);
        
        log.debug("Built percentage-based SLA filter for {}: {}% elapsed ({}% remaining)", 
            escalationLevel, percentage, (100 - percentage));
        
        return filter;
    }
    
    /**
     * Build number-based SLA filter (for LEVEL_ONE and LEVEL_TWO with hour thresholds)
     */
    private Map<String, Object> buildNumberBasedSLAFilter(String escalationLevel, EscalationLevel config) {
        Integer thresholdHours = config.getBreachThresholdInHours();
        
        if (thresholdHours == null) {
            log.warn("Null threshold hours for {}, using default", escalationLevel);
            thresholdHours = 0;
        }
        
        long thresholdMs = (long) (thresholdHours * 60 * 60 * 1000); // Convert to milliseconds
        
        Map<String, Object> slaRemainingRange = new HashMap<>();
        Map<String, Object> slaRemainingRangeQuery = new HashMap<>();
        slaRemainingRangeQuery.put("lte", thresholdMs);
        slaRemainingRange.put("Data.slaRemaining", slaRemainingRangeQuery);
        
        Map<String, Object> filter = new HashMap<>();
        filter.put("range", slaRemainingRange);
        
        log.debug("Built number-based SLA filter for {}: {} hours ({}ms)", 
            escalationLevel, thresholdHours, thresholdMs);
        
        return filter;
    }
    
    
    
    /**
     * Build Elasticsearch query for SLA breach tickets with escalation level threshold (country level)
     * Updated to support MDMS-driven thresholds
     */
    private Map<String, Object> buildSLABreachQueryWithLevelForCountry(List<String> workflowStates, 
                                                                       String escalationRecipientId, 
                                                                       String escalationLevel,
                                                                       RequestInfo requestInfo) {
        Map<String, Object> query = new HashMap<>();
        Map<String, Object> bool = new HashMap<>();
        List<Map<String, Object>> must = new ArrayList<>();
        
        // Filter by workflow states
        Map<String, Object> statusFilter = new HashMap<>();
        Map<String, Object> statusTerms = new HashMap<>();
        statusTerms.put("Data.incident.applicationStatus.keyword", workflowStates);
        statusFilter.put("terms", statusTerms);
        must.add(statusFilter);
        
        // Filter by SLA breach based on escalation level configuration from MDMS
        EscalationLevel escalationLevelConfig = getEscalationLevelConfig(escalationLevel, requestInfo);
        
        if (escalationLevelConfig == null) {
            log.error("EscalationLevel config not found for {} (country level) - MDMS configuration is required", escalationLevel);
            throw new RuntimeException("EscalationLevel configuration not found for " + escalationLevel + ". Please ensure MDMS is properly configured.");
        }
        
        // Build SLA filter based on calculation strategy from MDMS
            Map<String, Object> slaFilter = buildSLAFilter(escalationLevel, escalationLevelConfig);
            if (slaFilter != null) {
                must.add(slaFilter);
            log.debug("Added SLA filter for {} (country level) using strategy: {} with threshold: {} hours / {}%", 
                escalationLevel, 
                escalationLevelConfig.getBreachCalculationStrategy(),
                escalationLevelConfig.getBreachThresholdInHours(),
                escalationLevelConfig.getBreachThresholdInPercentage());
        }
        
        // Exclude tickets already escalated to this recipient AND level
        List<Map<String, Object>> mustNot = new ArrayList<>();
        if (escalationRecipientId != null) {
            Map<String, Object> escalationFilter = new HashMap<>();
            Map<String, Object> escalationNested = new HashMap<>();
            Map<String, Object> escalationQuery = new HashMap<>();
            Map<String, Object> escalationBool = new HashMap<>();
            List<Map<String, Object>> escalationMust = new ArrayList<>();
            
            // Check for same escalationId AND escalationLevel
            Map<String, Object> escalationIdTerm = new HashMap<>();
            escalationIdTerm.put("Data.incident.escalations.escalationId.keyword", escalationRecipientId);
            Map<String, Object> termFilter = new HashMap<>();
            termFilter.put("term", escalationIdTerm);
            escalationMust.add(termFilter);
            
            Map<String, Object> escalationLevelTerm = new HashMap<>();
            escalationLevelTerm.put("Data.incident.escalations.escalationLevel.keyword", escalationLevel);
            Map<String, Object> levelFilter = new HashMap<>();
            levelFilter.put("term", escalationLevelTerm);
            escalationMust.add(levelFilter);
            
            escalationBool.put("must", escalationMust);
            escalationQuery.put("bool", escalationBool);
            escalationNested.put("query", escalationQuery);
            escalationNested.put("path", "Data.incident.escalations");
            escalationFilter.put("nested", escalationNested);
            mustNot.add(escalationFilter);
        }

        bool.put("must", must);
        bool.put("must_not", mustNot);
        query.put("bool", bool);
        
        log.debug("SLA breach query for country level with escalation level {}: {}", 
            escalationLevel, query);
        return query;
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
     * Format SLA duration in milliseconds to human readable format
     */
    private String formatSlaDuration(Object slaDuration) {
        if (slaDuration == null) {
            return "Not Defined";
        }
        
        Long durationMs = getLongValue(slaDuration);
        if (durationMs == null || durationMs <= 0) {
            return "Not Defined";
        }
        
        // Convert to hours and minutes
        long hours = durationMs / (1000 * 60 * 60);
        long minutes = (durationMs % (1000 * 60 * 60)) / (1000 * 60);
        
        if (hours > 0) {
            return hours + "h " + minutes + "m";
        } else {
            return minutes + "m";
        }
    }
}