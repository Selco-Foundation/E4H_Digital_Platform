package org.selco.e4h.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.egov.common.contract.request.RequestInfo;
import org.selco.e4h.util.ElasticSearchClient;
import org.selco.e4h.web.models.EscalationLevel;
import org.selco.e4h.web.models.EscalationTicket;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
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
     * Utility method to build escalation exclusion filters
     */
    private List<Map<String, Object>> buildEscalationExclusionFilters(String escalationRecipientId, String escalationLevel) {
        List<Map<String, Object>> mustNot = new ArrayList<>();
        if (escalationRecipientId != null) {
            // Use simple term queries instead of nested query to avoid mapping issues
            // This approach works with regular object fields (not nested)
            
            // Exclude tickets with same escalationId
            Map<String, Object> escalationIdFilter = new HashMap<>();
            Map<String, Object> escalationIdTerm = new HashMap<>();
            escalationIdTerm.put("Data.incident.escalations.escalationId.keyword", escalationRecipientId);
            escalationIdFilter.put("term", escalationIdTerm);
            mustNot.add(escalationIdFilter);
            
            // Exclude tickets with same escalationLevel
            Map<String, Object> escalationLevelFilter = new HashMap<>();
            Map<String, Object> escalationLevelTerm = new HashMap<>();
            escalationLevelTerm.put("Data.incident.escalations.escalationLevel.keyword", escalationLevel);
            escalationLevelFilter.put("term", escalationLevelTerm);
            mustNot.add(escalationLevelFilter);
            
            log.debug("Added escalation exclusion filters for recipientId: {} and level: {}", 
                escalationRecipientId, escalationLevel);
        }
        return mustNot;
    }

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

            // Wrap inside a "query" map for Elasticsearch
            Map<String, Object> finalQuery = new HashMap<>();
            finalQuery.put("query", query);

            log.info("Executing Elasticsearch query: {}", finalQuery);
            
            // Execute query using ElasticsearchClient
            List<EscalationTicket> breachTickets = elasticSearchClient.searchTickets(finalQuery);
            
            // The Elasticsearch query already filters for SLA breach and escalation exclusions
            // Only apply post-filtering for special cases like LEVEL_TWO aged tickets
            List<EscalationTicket> filteredTickets = new ArrayList<>();
            
            for (EscalationTicket ticket : breachTickets) {
                // Follow MDMS-driven ES filter strictly for all levels including LEVEL_TWO
                filteredTickets.add(ticket);
            }
            
            log.info("Found {} tickets in SLA breach for tenant: {} with escalation level: {} ({} final)", 
                breachTickets.size(), tenantId, escalationLevel, filteredTickets.size());
            
            return filteredTickets;
            
        } catch (Exception e) {
            log.error("Error finding SLA breach tickets for tenant: {} with escalation level: {}", 
                tenantId, escalationLevel, e);
            // Fallback to empty list if query fails
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
                
                return breachDurationHours >= maxBreachHours;
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
                
                return breachDurationHours >= maxBreachHours;
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

        // Filter by tenant - use prefix match to include state and all its sub-tenants
        Map<String, Object> tenantFilter = new HashMap<>();
        Map<String, Object> tenantPrefix = new HashMap<>();
        tenantPrefix.put("Data.tenantId.keyword", tenantId);
        tenantFilter.put("prefix", tenantPrefix);
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

        // Prevent overlap: exclude tickets that qualify for higher levels
        if ("LEVEL_ONE".equals(escalationLevel)) {
            EscalationLevel l2Config = getEscalationLevelConfig("LEVEL_TWO", requestInfo);
            if (l2Config != null && "number".equalsIgnoreCase(l2Config.getBreachCalculationStrategy())) {
                Integer l2Hours = l2Config.getBreachThresholdInHours();
                if (l2Hours != null) {
                    long l2Ms = (long) l2Hours * 60 * 60 * 1000L;
                    Map<String, Object> gtRange = new HashMap<>();
                    Map<String, Object> gtRangeBody = new HashMap<>();
                    gtRangeBody.put("gt", l2Ms); // include only > L2 threshold (i.e., not as overdue as L2)
                    gtRange.put("Data.slaRemaining", gtRangeBody);
                    Map<String, Object> range = new HashMap<>();
                    range.put("range", gtRange);
                    must.add(range);
                    log.debug("Added anti-overlap filter for LEVEL_ONE: slaRemaining > {}ms", l2Ms);
                }
            }
        }

        // Exclude tickets already escalated to this recipient AND level
        List<Map<String, Object>> mustNot = buildEscalationExclusionFilters(escalationRecipientId, escalationLevel);

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
            // Default strategy based on escalation level
            if ("LEVEL_ZERO".equals(escalationLevel)) {
                log.warn("Unknown strategy for LEVEL_ZERO: {}, using percentage-based (70% threshold)", strategy);
                return buildPercentageBasedSLAFilter(escalationLevel, config);
            } else {
                log.warn("Unknown strategy for {}: {}, using number-based (0% threshold)", escalationLevel, strategy);
                return buildNumberBasedSLAFilter(escalationLevel, config);
            }
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
        Map<String, Object> countrySlaFilter = buildSLAFilter(escalationLevel, escalationLevelConfig);
        if (countrySlaFilter != null) {
            must.add(countrySlaFilter);
            log.debug("Added SLA filter for {} (country level) using strategy: {} with threshold: {} hours / {}%", 
                escalationLevel, 
                escalationLevelConfig.getBreachCalculationStrategy(),
                escalationLevelConfig.getBreachThresholdInHours(),
                escalationLevelConfig.getBreachThresholdInPercentage());
        }

        // Prevent overlap for country level as well
        if ("LEVEL_ONE".equals(escalationLevel)) {
            EscalationLevel l2Config = getEscalationLevelConfig("LEVEL_TWO", requestInfo);
            if (l2Config != null && "number".equalsIgnoreCase(l2Config.getBreachCalculationStrategy())) {
                Integer l2Hours = l2Config.getBreachThresholdInHours();
                if (l2Hours != null) {
                    long l2Ms = (long) l2Hours * 60 * 60 * 1000L;
                    Map<String, Object> gtRange = new HashMap<>();
                    Map<String, Object> gtRangeBody = new HashMap<>();
                    gtRangeBody.put("gt", l2Ms);
                    gtRange.put("Data.slaRemaining", gtRangeBody);
                    Map<String, Object> range = new HashMap<>();
                    range.put("range", gtRange);
                    must.add(range);
                    log.debug("Added anti-overlap filter for LEVEL_ONE (country): slaRemaining > {}ms", l2Ms);
                }
            }
        }
        
        // Exclude tickets already escalated to this recipient AND level
        List<Map<String, Object>> mustNot = buildEscalationExclusionFilters(escalationRecipientId, escalationLevel);

        bool.put("must", must);
        bool.put("must_not", mustNot);
        query.put("bool", bool);
        
        log.debug("SLA breach query for country level with escalation level {}: {}", 
            escalationLevel, query);
        return query;
    }

}