package org.selco.e4h.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.egov.common.contract.request.RequestInfo;
import org.selco.e4h.web.models.WeeklyReportData;
import org.selco.e4h.web.models.FunctionalMetrics;
import org.selco.e4h.web.models.AgeBucketData;
import org.selco.e4h.web.models.ArrowData;
import org.selco.e4h.web.models.TicketData;
import org.selco.e4h.util.CommonUtility;
import org.selco.e4h.util.ElasticSearchClient;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;


/**
 * Service for generating weekly DRE system reports
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class WeeklyReportService {
    
    private final ElasticSearchClient elasticSearchClient;
    private final CommonUtility commonUtility;
    
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("dd MMM yyyy");
    private static final SimpleDateFormat DATE_RANGE_FORMAT = new SimpleDateFormat("dd MMM");
    private static final SimpleDateFormat TODAY_FORMAT = new SimpleDateFormat("dd MMM yyyy");
    
    // Constants for field names and values
    private static final String FIELD_FILED_DATE = "filedDate";
    private static final String FIELD_CREATED_TIME = "createdTime";
    private static final String SYSTEM_STATUS_NON_FUNCTIONAL = "NON_FUNCTIONAL";

    static {
        // Set timezone to IST for date formatting
        TimeZone istTimeZone = TimeZone.getTimeZone("Asia/Kolkata");
        DATE_FORMAT.setTimeZone(istTimeZone);
        DATE_RANGE_FORMAT.setTimeZone(istTimeZone);
        TODAY_FORMAT.setTimeZone(istTimeZone);
    }
    
    /**
     * Utility method to filter tickets by tenant and process them
     */
    private List<Map<String, Object>> filterTicketsByTenant(List<Map<String, Object>> tickets, String stateCode) {
        log.trace("Filtering tickets by tenant, stateCode: {}, total tickets: {}", stateCode, tickets != null ? tickets.size() : 0);
        List<Map<String, Object>> filtered = tickets.stream()
            .filter(ticket -> {
                Map<String, Object> data = (Map<String, Object>) ticket.get("Data");
                if (data == null) return false;

//                String ticketTenantId = (String) data.get("tenantId");
                Map<String, Object> incident = (Map<String, Object>) data.get("incident");
                Map<String, Object> boundary = (Map<String, Object>) incident.get("boundary");
                if(boundary ==null)
                    return false;
                String ticketStateCode = (String) boundary.get("stateCode");
                if (ticketStateCode == null) return false;

                return ticketStateCode.equals(stateCode) || ticketStateCode.startsWith(stateCode + ".");
            })
            .collect(Collectors.toList());
        log.debug("Filtered to {} tickets for stateCode: {}", filtered.size(), stateCode);
        return filtered;
    }

    /**
     * Utility method to extract common ticket data fields
     */
    private TicketData extractTicketData(Map<String, Object> ticket) {
        log.trace("Extracting ticket data from ticket map");
        Map<String, Object> data = (Map<String, Object>) ticket.get("Data");
        if (data == null) {
            log.debug("Ticket data is null");
            return null;
        }

        // Normalize filedDate from any of the possible locations and formats
        // Check multiple locations: Data.filedDate, root filedDate, Data.incident.filedDate, Data.incident.auditDetails.createdTime
        Long filedDate = parseFiledDate(data.get(FIELD_FILED_DATE));
        if (filedDate == null) {
            filedDate = parseFiledDate(ticket.get(FIELD_FILED_DATE));
        }
        if (filedDate == null) {
            Map<String, Object> incident = (Map<String, Object>) data.get("incident");
            if (incident != null) {
                filedDate = parseFiledDate(incident.get(FIELD_FILED_DATE));
                if (filedDate == null) {
                    Map<String, Object> audit = (Map<String, Object>) incident.get("auditDetails");
                    if (audit != null) {
                        filedDate = parseFiledDate(audit.get(FIELD_CREATED_TIME));
                    }
                }
            }
        }
        log.debug("Extracted filedDate: {}", filedDate);

        TicketData ticketData = TicketData.builder()
            .tenantId((String) data.get("tenantId"))
            .filedDate(filedDate)
            .systemFunctional((String) data.get("systemFunctional"))
            .state((String) data.get("state"))
            .data(data)
            .build();
        log.debug("Extracted ticket data: tenantId={}, systemFunctional={}, state={}", 
            ticketData.getTenantId(), ticketData.getSystemFunctional(), ticketData.getState());
        return ticketData;
    }

    // Parse filedDate from number or formatted strings into epoch millis (IST)
    private Long parseFiledDate(Object filedDateObj) {
        log.trace("Parsing filed date from object, type: {}", filedDateObj != null ? filedDateObj.getClass().getSimpleName() : "null");
        if (filedDateObj == null) {
            log.debug("Filed date object is null");
            return null;
        }
        if (filedDateObj instanceof Number) {
            long v = ((Number) filedDateObj).longValue();
            log.debug("Parsed filed date from Number: {}", v);
            return v == 0L ? null : v;
        }
        if (filedDateObj instanceof String s) {
            String str = s.trim();
            try {
                long v = Long.parseLong(str);
                log.debug("Parsed filed date from String (long): {}", v);
                return v == 0L ? null : v;
            } catch (NumberFormatException ignored) { /* try date formats below */ }

            String[] patterns = new String[] {
                "yyyy-MM-dd HH:mm:ss z",            // 2025-07-02 17:09:24 IST
                "yyyy-MM-dd'T'HH:mm:ss.SSSX",       // ISO with millis
                "yyyy-MM-dd'T'HH:mm:ssX",           // ISO without millis
                "MMM dd, yyyy @ HH:mm:ss.SSS",      // Jul 16, 2025 @ 13:52:56.626
                "MMM dd, yyyy @ HH:mm:ss"           // Jul 16, 2025 @ 13:52:56
            };
            for (String p : patterns) {
                try {
                    java.text.SimpleDateFormat f = new java.text.SimpleDateFormat(p, java.util.Locale.ENGLISH);
                    f.setTimeZone(java.util.TimeZone.getTimeZone("Asia/Kolkata"));
                    java.util.Date d = f.parse(str);
                    if (d != null) {
                        log.debug("Parsed filed date using pattern: {}, result: {}", p, d.getTime());
                        return d.getTime();
                    }
                } catch (Exception ignored) {}
            }
            log.debug("Could not parse filed date from string: {}", str);
        }
        return null;
    }


    /**
     * Generate weekly report data for a specific tenant
     */
    public WeeklyReportData generateWeeklyReportData(String stateCode, RequestInfo requestInfo) {
        log.trace("Generating weekly report data for stateCode: {}", stateCode);
        try {
            log.info("Generating weekly report data for tenant: {}", stateCode);
            
            Date[] weekDates = getPreviousWeekDates();
            Date weekStart = weekDates[0];
            Date weekEnd = weekDates[1];
            Date today = new Date();
            log.debug("Week date range: {} to {}", weekStart, weekEnd);
            
            WeeklyReportDateInfo dateInfo = formatWeeklyReportDates(weekStart, weekEnd, today);
            log.info("Weekly report period: {} to {} ({})", dateInfo.weekStartStr, dateInfo.weekEndStr, dateInfo.dateRange);
            
            FunctionalMetrics startMetrics = getFunctionalMetrics(stateCode, weekStart);
            FunctionalMetrics endMetrics = getFunctionalMetrics(stateCode, weekEnd);
            log.debug("Start metrics - functional: {}, non-functional: {}", 
                startMetrics.getFunctionalCount(), startMetrics.getNonFunctionalCount());
            log.debug("End metrics - functional: {}, non-functional: {}", 
                endMetrics.getFunctionalCount(), endMetrics.getNonFunctionalCount());
            
            ArrowData funcArrow = calculateFunctionalArrows(startMetrics, endMetrics);
            ArrowData nonFuncArrow = calculateNonFunctionalArrows(startMetrics, endMetrics);
            
            AgeBucketData ageBucketData = getAgeBucketData(stateCode);
            Map<String, WeeklyReportData.StateAgeBucketData> stateData = getStateWiseAgeBucketData(stateCode);
            
            WeeklyReportData reportData = buildWeeklyReportData(stateCode, dateInfo, startMetrics, 
                endMetrics, funcArrow, nonFuncArrow, ageBucketData, stateData);
            
            log.info("Successfully generated weekly report data for tenant: {}", stateCode);
            return reportData;
            
        } catch (Exception e) {
            log.error("Error generating weekly report data for tenant: {}", stateCode, e);
            throw new RuntimeException("Failed to generate weekly report data", e);
        }
    }

    private WeeklyReportDateInfo formatWeeklyReportDates(Date weekStart, Date weekEnd, Date today) {
        String weekStartStr = DATE_FORMAT.format(weekStart);
        String weekEndStr = DATE_FORMAT.format(weekEnd);
        Calendar cal = Calendar.getInstance();
        cal.setTime(weekEnd);
        int year = cal.get(Calendar.YEAR);
        
        String dateRange = String.format("%s - %s %d", 
            DATE_RANGE_FORMAT.format(weekStart), 
            DATE_RANGE_FORMAT.format(weekEnd), 
            year);
        String todayFormatted = TODAY_FORMAT.format(today);
        
        return new WeeklyReportDateInfo(weekStartStr, weekEndStr, dateRange, todayFormatted);
    }

    private ArrowData calculateFunctionalArrows(FunctionalMetrics startMetrics, FunctionalMetrics endMetrics) {
        int totalStart = startMetrics.getFunctionalCount() + startMetrics.getNonFunctionalCount();
        int totalEnd = endMetrics.getFunctionalCount() + endMetrics.getNonFunctionalCount();
        double funcStartPct = totalStart > 0 ? (startMetrics.getFunctionalCount() * 100.0 / totalStart) : 0;
        double funcEndPct = totalEnd > 0 ? (endMetrics.getFunctionalCount() * 100.0 / totalEnd) : 0;
        return commonUtility.calculateArrow(funcStartPct, funcEndPct, true);
    }

    private ArrowData calculateNonFunctionalArrows(FunctionalMetrics startMetrics, FunctionalMetrics endMetrics) {
        int totalStart = startMetrics.getFunctionalCount() + startMetrics.getNonFunctionalCount();
        int totalEnd = endMetrics.getFunctionalCount() + endMetrics.getNonFunctionalCount();
        double nonFuncStartPct = totalStart > 0 ? (startMetrics.getNonFunctionalCount() * 100.0 / totalStart) : 0;
        double nonFuncEndPct = totalEnd > 0 ? (endMetrics.getNonFunctionalCount() * 100.0 / totalEnd) : 0;
        return commonUtility.calculateArrow(nonFuncStartPct, nonFuncEndPct, false);
    }

    private WeeklyReportData buildWeeklyReportData(String stateCode, WeeklyReportDateInfo dateInfo,
                                                   FunctionalMetrics startMetrics, FunctionalMetrics endMetrics,
                                                   ArrowData funcArrow, ArrowData nonFuncArrow,
                                                   AgeBucketData ageBucketData, Map<String, WeeklyReportData.StateAgeBucketData> stateData) {
        return WeeklyReportData.builder()
            .tenantId(stateCode)
            .dateRange(dateInfo.dateRange)
            .weekStartDate(dateInfo.weekStartStr)
            .weekEndDate(dateInfo.weekEndStr)
            .weekStartMetrics(startMetrics)
            .weekEndMetrics(endMetrics)
            .functionalArrow(funcArrow)
            .nonFunctionalArrow(nonFuncArrow)
            .totalAgeBuckets(ageBucketData)
            .stateData(stateData)
            .stateList(formatStateList(stateCode, stateData.keySet()))
            .todayFormatted(dateInfo.todayFormatted)
            .build();
    }

    private static class WeeklyReportDateInfo {
        final String weekStartStr;
        final String weekEndStr;
        final String dateRange;
        final String todayFormatted;
        
        WeeklyReportDateInfo(String weekStartStr, String weekEndStr, String dateRange, String todayFormatted) {
            this.weekStartStr = weekStartStr;
            this.weekEndStr = weekEndStr;
            this.dateRange = dateRange;
            this.todayFormatted = todayFormatted;
        }
    }
    
    /**
     * Get functional/non-functional metrics for a specific date using Elasticsearch
     */
    private FunctionalMetrics getFunctionalMetrics(String stateCode, Date date) {
        log.trace("Getting functional metrics for stateCode: {}, date: {}", stateCode, date);
        try {
            List<Map<String, Object>> tickets = elasticSearchClient.fetchRequiredTickets(0, 10000, false);
            log.info("Fetched {} total tickets from ES for tenant: {}", tickets.size(), stateCode);
            log.debug("Querying functional metrics for date: {}", date);

            List<Map<String, Object>> filteredTickets = filterTicketsByTenant(tickets, stateCode);
            log.info("Filtered to {} tickets for tenant: {}", filteredTickets.size(), stateCode);

            FunctionalMetricsCounts counts = countFunctionalMetrics(filteredTickets, date);
            
            log.info("Functional metrics for {} on {}: Functional={}, Non-Functional={}, Skipped(no filedDate)={}, Skipped(after date)={}",
                    stateCode, date, counts.functionalCount, counts.nonFunctionalCount, counts.skippedNoFiledDate, counts.skippedAfterDate);
            
            return FunctionalMetrics.builder()
                .functionalCount(counts.functionalCount)
                .nonFunctionalCount(counts.nonFunctionalCount)
                .build();
                
        } catch (Exception e) {
            log.error("Error getting functional metrics from Elasticsearch for tenant: {} on date: {}", stateCode, date, e);
            return FunctionalMetrics.builder()
                .functionalCount(0)
                .nonFunctionalCount(0)
                .build();
        }
    }

    private FunctionalMetricsCounts countFunctionalMetrics(List<Map<String, Object>> filteredTickets, Date date) {
        int functionalCount = 0;
        int nonFunctionalCount = 0;
        int skippedNoFiledDate = 0;
        int skippedAfterDate = 0;

        for (Map<String, Object> ticket : filteredTickets) {
            TicketData ticketData = extractTicketData(ticket);

            if (!isTicketValidForDate(ticketData, date)) {
                if (ticketData == null) {
                    continue;
                } else if (ticketData.getFiledDate() == null) {
                    skippedNoFiledDate++;
                } else if (ticketData.getFiledDate() > date.getTime()) {
                    skippedAfterDate++;
                }
                continue;
            }

            if (SYSTEM_STATUS_NON_FUNCTIONAL.equals(ticketData.getSystemFunctional())) {
                nonFunctionalCount++;
            } else {
                functionalCount++;
            }
        }
        
        return new FunctionalMetricsCounts(functionalCount, nonFunctionalCount, skippedNoFiledDate, skippedAfterDate);
    }

    private boolean isTicketValidForDate(TicketData ticketData, Date date) {
        return ticketData != null 
            && ticketData.getFiledDate() != null 
            && ticketData.getFiledDate() <= date.getTime();
    }

    private static class FunctionalMetricsCounts {
        final int functionalCount;
        final int nonFunctionalCount;
        final int skippedNoFiledDate;
        final int skippedAfterDate;
        
        FunctionalMetricsCounts(int functionalCount, int nonFunctionalCount, int skippedNoFiledDate, int skippedAfterDate) {
            this.functionalCount = functionalCount;
            this.nonFunctionalCount = nonFunctionalCount;
            this.skippedNoFiledDate = skippedNoFiledDate;
            this.skippedAfterDate = skippedAfterDate;
        }
    }
    
    
    /**
     * Get age bucket data for non-functional systems
     * Uses the correct age bucket logic
     * - 1 Week: 8 ≤ age in days ≤ 30
     * - 1 Month: 31 ≤ age in days ≤ 90  
     * - 3 Month: age in days > 90
     * - For facilities with multiple tickets, consider only the oldest ticket
     * - Counts unique facilities (HFs), not tickets
     * Uses boundary.stateCode to identify state since all tickets are now under tenantId 'in'
     */
    private AgeBucketData getAgeBucketData(String stateCode) {
        log.trace("Getting age bucket data for stateCode: {}", stateCode);
        try {
            List<Map<String, Object>> tickets = elasticSearchClient.fetchRequiredTickets(0, 10000, false);
            log.debug("Fetched {} tickets from Elasticsearch", tickets.size());
            
            List<Map<String, Object>> filteredTickets = filterTicketsByTenant(tickets, stateCode);
            log.debug("Filtered to {} tickets for stateCode: {}", filteredTickets.size(), stateCode);

            Map<String, TicketData> oldestTicketByFacility = buildOldestTicketMap(filteredTickets);
            AgeBucketCounts totalCounts = calculateTotalAgeBucketCounts(oldestTicketByFacility);
            
            log.info("Age bucket data for {}: Total facilities={}, <1Wk={}, <1Mo={}, <3Mo={}",
                stateCode, oldestTicketByFacility.size(), totalCounts.lt1Wk, totalCounts.lt1Mo, totalCounts.lt3Mo);
            
            return AgeBucketData.builder()
                .totalLt1Wk(totalCounts.lt1Wk)
                .totalLt1Mo(totalCounts.lt1Mo)
                .totalLt3Mo(totalCounts.lt3Mo)
                .build();
                
        } catch (Exception e) {
            log.error("Error getting age bucket data from Elasticsearch for tenant: {}", stateCode, e);
            return AgeBucketData.builder()
                .totalLt1Wk(0)
                .totalLt1Mo(0)
                .totalLt3Mo(0)
                .build();
        }
    }

    private Map<String, TicketData> buildOldestTicketMap(List<Map<String, Object>> filteredTickets) {
        Map<String, TicketData> oldestTicketByFacility = new HashMap<>();

        for (Map<String, Object> ticket : filteredTickets) {
            TicketData ticketData = extractTicketData(ticket);
            String facilityStateCode = extractBoundaryStateCode(ticketData);
            
            if (!isValidNonFunctionalTicket(ticketData) || facilityStateCode == null) {
                continue;
            }

            Map<String, Object> data = ticketData.getData();
            String facilityKey = generateFacilityKey(data, facilityStateCode);
            updateFacilityMapWithOldestTicket(oldestTicketByFacility, facilityKey, ticketData);
        }
        
        return oldestTicketByFacility;
    }

    private AgeBucketCounts calculateTotalAgeBucketCounts(Map<String, TicketData> oldestTicketByFacility) {
        int totalLt1Wk = 0;
        int totalLt1Mo = 0;
        int totalLt3Mo = 0;
        
        for (TicketData oldestTicket : oldestTicketByFacility.values()) {
            int ageInDays = computeBusinessDays(oldestTicket.getFiledDate(), System.currentTimeMillis());
            AgeBucketCounts counts = calculateAgeBucket(ageInDays);
            totalLt1Wk += counts.lt1Wk;
            totalLt1Mo += counts.lt1Mo;
            totalLt3Mo += counts.lt3Mo;
        }
        
        return new AgeBucketCounts(totalLt1Wk, totalLt1Mo, totalLt3Mo);
    }
    
    /**
     * Get state-wise age bucket data using Elasticsearch
     * Counts unique facilities (HFs) per state, using oldest ticket per facility
     * Uses boundary.stateCode to identify state since all tickets are now under tenantId 'in'
     */
    private Map<String, WeeklyReportData.StateAgeBucketData> getStateWiseAgeBucketData(String stateCode) {
        log.trace("Getting state-wise age bucket data for stateCode: {}", stateCode);
        try {
            List<Map<String, Object>> tickets = elasticSearchClient.fetchRequiredTickets(0, 10000, false);
            log.debug("Fetched {} tickets from Elasticsearch", tickets.size());
            
            List<Map<String, Object>> filteredTickets = filterTicketsByTenant(tickets, stateCode);
            log.debug("Filtered to {} tickets for stateCode: {}", filteredTickets.size(), stateCode);

            Map<String, Map<String, TicketData>> stateFacilityMap = buildStateFacilityMap(filteredTickets);
            Map<String, WeeklyReportData.StateAgeBucketData> stateData = buildStateAgeBucketData(stateFacilityMap);
            
            log.info("State-wise age bucket data for {}: {} states, total facilities={}",
                stateCode, stateData.size(),
                stateFacilityMap.values().stream().mapToInt(Map::size).sum());
            return stateData;
            
        } catch (Exception e) {
            log.error("Error getting state-wise age bucket data from Elasticsearch for tenant: {}", stateCode, e);
            return new HashMap<>();
        }
    }

    private Map<String, Map<String, TicketData>> buildStateFacilityMap(List<Map<String, Object>> filteredTickets) {
        Map<String, Map<String, TicketData>> stateFacilityMap = new HashMap<>();

        for (Map<String, Object> ticket : filteredTickets) {
            TicketData ticketData = extractTicketData(ticket);
            String ticketStateCode = extractBoundaryStateCode(ticketData);
            
            if (!isValidNonFunctionalTicket(ticketData) || ticketStateCode == null) {
                continue;
            }

            Map<String, Object> data = ticketData.getData();
            String facilityKey = generateFacilityKey(data, ticketStateCode);
            Map<String, TicketData> facilityMap = stateFacilityMap.computeIfAbsent(ticketStateCode, k -> new HashMap<>());
            updateFacilityMapWithOldestTicket(facilityMap, facilityKey, ticketData);
        }
        
        return stateFacilityMap;
    }

    private Map<String, WeeklyReportData.StateAgeBucketData> buildStateAgeBucketData(Map<String, Map<String, TicketData>> stateFacilityMap) {
        Map<String, WeeklyReportData.StateAgeBucketData> stateData = new LinkedHashMap<>();
        
        for (Map.Entry<String, Map<String, TicketData>> stateEntry : stateFacilityMap.entrySet()) {
            String ticketStateCode = stateEntry.getKey();
            Map<String, TicketData> facilityMap = stateEntry.getValue();

            WeeklyReportData.StateAgeBucketData stateBucket = stateData.computeIfAbsent(ticketStateCode,
                k -> WeeklyReportData.StateAgeBucketData.builder()
                    .stateName(commonUtility.getStateDisplayName(k))
                    .lt1Wk(0)
                    .lt1Mo(0)
                    .lt3Mo(0)
                    .build());

            updateStateBucketCounts(stateBucket, facilityMap);
        }
        
        return stateData;
    }

    private void updateStateBucketCounts(WeeklyReportData.StateAgeBucketData stateBucket, Map<String, TicketData> facilityMap) {
        for (TicketData oldestTicket : facilityMap.values()) {
            int ageInDays = computeBusinessDays(oldestTicket.getFiledDate(), System.currentTimeMillis());
            AgeBucketCounts counts = calculateAgeBucket(ageInDays);
            stateBucket.setLt1Wk(stateBucket.getLt1Wk() + counts.lt1Wk);
            stateBucket.setLt1Mo(stateBucket.getLt1Mo() + counts.lt1Mo);
            stateBucket.setLt3Mo(stateBucket.getLt3Mo() + counts.lt3Mo);
        }
    }
    
    /**
     * Helper method to safely get string value from map
     */
    private String getStringValue(Map<String, Object> map, String key) {
        log.trace("Getting string value from map, key: {}", key);
        Object value = map.get(key);
        String result = value != null ? value.toString() : "";
        log.debug("Retrieved value for key '{}': {}", key, result);
        return result;
    }
    
    /**
     * Extract boundary stateCode from ticket data
     * @param ticketData The ticket data
     * @return The stateCode from boundary, or null if not found
     */
    private String extractBoundaryStateCode(TicketData ticketData) {
        log.trace("Extracting boundary state code from ticket data");
        if (ticketData == null) {
            log.debug("Ticket data is null");
            return null;
        }
        
        Map<String, Object> data = ticketData.getData();
        if (data == null) {
            log.debug("Data map is null");
            return null;
        }
        
        Map<String, Object> incident = (Map<String, Object>) data.get("incident");
        if (incident == null) {
            log.debug("Incident map is null");
            return null;
        }
        
        Map<String, Object> boundary = (Map<String, Object>) incident.get("boundary");
        if (boundary == null) {
            log.debug("Boundary map is null");
            return null;
        }
        
        String stateCode = (String) boundary.get("stateCode");
        log.debug("Extracted state code: {}", stateCode);
        return stateCode;
    }
    
    /**
     * Generate unique facility key from ticket data
     * Format: facilityName|district|block|stateCode
     * @param data The ticket data map
     * @param stateCode The state code from boundary
     * @return Unique facility key
     */
    private String generateFacilityKey(Map<String, Object> data, String stateCode) {
        log.trace("Generating facility key for stateCode: {}", stateCode);
        String facilityName = getStringValue(data, "tenantId_localized");
        if (facilityName == null || facilityName.isEmpty()) {
            facilityName = getStringValue(data, "tenantId");
        }
        String district = getStringValue(data, "district");
        String block = getStringValue(data, "block");
        String key = facilityName + "|" + district + "|" + block + "|" + stateCode;
        log.debug("Generated facility key: {}", key);
        return key;
    }
    
    /**
     * Check if ticket is valid non-functional ticket with required fields
     * @param ticketData The ticket data
     * @return true if ticket is valid non-functional ticket
     */
    private boolean isValidNonFunctionalTicket(TicketData ticketData) {
        log.trace("Validating non-functional ticket");
        boolean isValid = ticketData != null
            && SYSTEM_STATUS_NON_FUNCTIONAL.equals(ticketData.getSystemFunctional())
            && ticketData.getFiledDate() != null;
        log.debug("Ticket validation result: {}, systemFunctional: {}, hasFiledDate: {}", 
            isValid, ticketData != null ? ticketData.getSystemFunctional() : "null",
            ticketData != null && ticketData.getFiledDate() != null);
        return isValid;
    }
    
    /**
     * Calculate age bucket for a given age in days
     * @param ageInDays Age in business days
     * @return AgeBucketCounts with counts for each bucket
     */
    private AgeBucketCounts calculateAgeBucket(int ageInDays) {
        log.trace("Calculating age bucket for age in days: {}", ageInDays);
        int lt1Wk = 0;
        int lt1Mo = 0;
        int lt3Mo = 0;
        
        // Age bucket logic:
        // 1 Week: 8 ≤ age in days ≤ 30
        // 1 Month: 31 ≤ age in days ≤ 90
        // 3 Month: age in days > 90
        if (ageInDays >= 8 && ageInDays <= 30) {
            lt1Wk = 1;
            log.debug("Age bucket: <1 Week");
        } else if (ageInDays >= 31 && ageInDays <= 90) {
            lt1Mo = 1;
            log.debug("Age bucket: <1 Month");
        } else if (ageInDays > 90) {
            lt3Mo = 1;
            log.debug("Age bucket: <3 Month");
        }
        
        return new AgeBucketCounts(lt1Wk, lt1Mo, lt3Mo);
    }
    
    /**
     * Update facility map with oldest ticket per facility
     * @param facilityMap The map to update (facilityKey -> TicketData)
     * @param facilityKey The unique facility key
     * @param ticketData The ticket data to potentially add
     */
    private void updateFacilityMapWithOldestTicket(Map<String, TicketData> facilityMap, 
                                                   String facilityKey, 
                                                   TicketData ticketData) {
        log.trace("Updating facility map with oldest ticket, facilityKey: {}", facilityKey);
        TicketData existing = facilityMap.get(facilityKey);
        if (existing == null || existing.getFiledDate() == null ||
            ticketData.getFiledDate() < existing.getFiledDate()) {
            facilityMap.put(facilityKey, ticketData);
            log.debug("Updated facility map with older ticket, filedDate: {}", ticketData.getFiledDate());
        } else {
            log.debug("Existing ticket is older, keeping existing entry");
        }
    }
    
    /**
     * Inner class to hold age bucket counts
     */
    private static class AgeBucketCounts {
        final int lt1Wk;
        final int lt1Mo;
        final int lt3Mo;
        
        AgeBucketCounts(int lt1Wk, int lt1Mo, int lt3Mo) {
            this.lt1Wk = lt1Wk;
            this.lt1Mo = lt1Mo;
            this.lt3Mo = lt3Mo;
        }
    }

    /**
     * Get previous week's Monday and Sunday dates
     */
    private Date[] getPreviousWeekDates() {
        log.trace("Calculating previous week dates (Monday to Sunday)");
        Calendar cal = Calendar.getInstance();
        cal.setTimeZone(TimeZone.getTimeZone("Asia/Kolkata"));
        
        // Go back to last Monday
        int dayOfWeek = cal.get(Calendar.DAY_OF_WEEK);
        int daysToSubtract = (dayOfWeek == Calendar.SUNDAY) ? 7 : dayOfWeek - Calendar.MONDAY;
        cal.add(Calendar.DAY_OF_MONTH, -daysToSubtract - 7); // Go back one more week
        log.debug("Calculated days to subtract: {}", daysToSubtract);
        
        // Set to start of day (Monday 00:00:00)
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        Date weekStart = cal.getTime();
        log.debug("Week start date: {}", weekStart);
        
        // Add 6 days to get to Sunday
        cal.add(Calendar.DAY_OF_MONTH, 6);
        
        // Set to end of day (Sunday 23:59:59)
        cal.set(Calendar.HOUR_OF_DAY, 23);
        cal.set(Calendar.MINUTE, 59);
        cal.set(Calendar.SECOND, 59);
        cal.set(Calendar.MILLISECOND, 999);
        Date weekEnd = cal.getTime();
        log.debug("Week end date: {}", weekEnd);
        
        return new Date[]{weekStart, weekEnd};
    }
    
    
    /**
     * Format state list for display
     */
    private String formatStateList(String tenantId, Set<String> states) {
        log.trace("Formatting state list for tenantId: {}, states count: {}", tenantId, states != null ? states.size() : 0);
        log.info("Formatting state list for tenantId: {}, states: {}", tenantId, states);
        
        if (states == null || states.isEmpty()) {
            // If no states with data, show the state name for the tenant
            String stateName = commonUtility.getStateDisplayName(tenantId);
            log.info("No states with data, returning state name: {}", stateName);
            return stateName;
        }
        
        String result = states.stream()
            .map(commonUtility::getStateDisplayName)
            .collect(Collectors.joining(", "));
        log.info("States with data, returning: {}", result);
        log.debug("Formatted state list: {}", result);
        return result;
    }
    
    /**
     * Compute business days between two instants using an 8-hour-per-day concept with Sunday off.
     * We treat each non-Sunday calendar day as one business day. This aligns buckets to
     * business-day counts rather than raw wall-clock days.
     */
    private int computeBusinessDays(long startMs, long endMs) {
        log.trace("Computing business days from {} to {}", startMs, endMs);
        if (endMs <= startMs) {
            log.debug("End time is before or equal to start time, returning 0");
            return 0;
        }
        TimeZone tz = TimeZone.getTimeZone("Asia/Kolkata");
        Calendar startCal = Calendar.getInstance(tz);
        Calendar endCal = Calendar.getInstance(tz);
        startCal.setTimeInMillis(startMs);
        endCal.setTimeInMillis(endMs);

        // Normalize to start of day for iteration
        startCal.set(Calendar.HOUR_OF_DAY, 0);
        startCal.set(Calendar.MINUTE, 0);
        startCal.set(Calendar.SECOND, 0);
        startCal.set(Calendar.MILLISECOND, 0);

        int days = 0;
        while (startCal.getTimeInMillis() < endCal.getTimeInMillis()) {
            int dow = startCal.get(Calendar.DAY_OF_WEEK);
            if (dow != Calendar.SUNDAY) {
                days++;
            }
            startCal.add(Calendar.DAY_OF_MONTH, 1);
        }
        log.debug("Computed {} business days (excluding Sundays)", days);
        return days;
    }

}
