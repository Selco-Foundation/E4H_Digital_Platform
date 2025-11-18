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
    private List<Map<String, Object>> filterTicketsByTenant(List<Map<String, Object>> tickets, String tenantId) {
        return tickets.stream()
            .filter(ticket -> {
                Map<String, Object> data = (Map<String, Object>) ticket.get("Data");
                if (data == null) return false;
                
                String ticketTenantId = (String) data.get("tenantId");
                if (ticketTenantId == null) return false;

                return ticketTenantId.equals(tenantId) || ticketTenantId.startsWith(tenantId + ".");
            })
            .collect(Collectors.toList());
    }
    
    /**
     * Utility method to extract common ticket data fields
     */
    private TicketData extractTicketData(Map<String, Object> ticket) {
        Map<String, Object> data = (Map<String, Object>) ticket.get("Data");
        if (data == null) return null;
        
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
        
        return TicketData.builder()
            .tenantId((String) data.get("tenantId"))
            .filedDate(filedDate)
            .systemFunctional((String) data.get("systemFunctional"))
            .state((String) data.get("state"))
            .data(data)
            .build();
    }

    // Parse filedDate from number or formatted strings into epoch millis (IST)
    private Long parseFiledDate(Object filedDateObj) {
        if (filedDateObj == null) return null;
        if (filedDateObj instanceof Number) {
            long v = ((Number) filedDateObj).longValue();
            return v == 0L ? null : v;
        }
        if (filedDateObj instanceof String s) {
            String str = s.trim();
            try {
                long v = Long.parseLong(str);
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
                    if (d != null) return d.getTime();
                } catch (Exception ignored) {}
            }
        }
        return null;
    }
    
    
    /**
     * Generate weekly report data for a specific tenant
     */
    public WeeklyReportData generateWeeklyReportData(String tenantId, RequestInfo requestInfo) {
        try {
            log.info("Generating weekly report data for tenant: {}", tenantId);
            
            // Get date range for the previous week (Monday to Sunday)
            Date[] weekDates = getPreviousWeekDates();
            Date weekStart = weekDates[0];
            Date weekEnd = weekDates[1];
            Date today = new Date();
            
            // Format dates
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
            
            log.info("Weekly report period: {} to {} ({})", weekStartStr, weekEndStr, dateRange);
            
            // Get functional/non-functional counts at start and end of week
            FunctionalMetrics startMetrics = getFunctionalMetrics(tenantId, weekStart);
            FunctionalMetrics endMetrics = getFunctionalMetrics(tenantId, weekEnd);
            
            // Calculate percentages
            int totalStart = startMetrics.getFunctionalCount() + startMetrics.getNonFunctionalCount();
            int totalEnd = endMetrics.getFunctionalCount() + endMetrics.getNonFunctionalCount();
            
            double funcStartPct = totalStart > 0 ? (startMetrics.getFunctionalCount() * 100.0 / totalStart) : 0;
            double nonFuncStartPct = totalStart > 0 ? (startMetrics.getNonFunctionalCount() * 100.0 / totalStart) : 0;
            double funcEndPct = totalEnd > 0 ? (endMetrics.getFunctionalCount() * 100.0 / totalEnd) : 0;
            double nonFuncEndPct = totalEnd > 0 ? (endMetrics.getNonFunctionalCount() * 100.0 / totalEnd) : 0;
            
            // Calculate arrows for changes using shared utility
            ArrowData funcArrow = commonUtility.calculateArrow(funcStartPct, funcEndPct, true);
            ArrowData nonFuncArrow = commonUtility.calculateArrow(nonFuncStartPct, nonFuncEndPct, false);
            
            // Get age bucket data
            AgeBucketData ageBucketData = getAgeBucketData(tenantId);
            
            // Get state-wise data
            Map<String, WeeklyReportData.StateAgeBucketData> stateData = getStateWiseAgeBucketData(tenantId);
            
            // Build the report data
            WeeklyReportData reportData = WeeklyReportData.builder()
                .tenantId(tenantId)
                .dateRange(dateRange)
                .weekStartDate(weekStartStr)
                .weekEndDate(weekEndStr)
                .weekStartMetrics(startMetrics)
                .weekEndMetrics(endMetrics)
                .functionalArrow(funcArrow)
                .nonFunctionalArrow(nonFuncArrow)
                .totalAgeBuckets(ageBucketData)
                .stateData(stateData)
                .stateList(formatStateList(tenantId, stateData.keySet()))
                .todayFormatted(todayFormatted)
                .build();
            
            log.info("Successfully generated weekly report data for tenant: {}", tenantId);
            return reportData;
            
        } catch (Exception e) {
            log.error("Error generating weekly report data for tenant: {}", tenantId, e);
            throw new RuntimeException("Failed to generate weekly report data", e);
        }
    }
    
    /**
     * Get functional/non-functional metrics for a specific date using Elasticsearch
     */
    private FunctionalMetrics getFunctionalMetrics(String tenantId, Date date) {
        try {
            // Query Elasticsearch for tickets as of the specified date
            List<Map<String, Object>> tickets = elasticSearchClient.fetchRequiredTickets(0, 10000, false);
            log.info("Fetched {} total tickets from ES for tenant: {}", tickets.size(), tenantId);
            
            // Filter tickets by tenant
            List<Map<String, Object>> filteredTickets = filterTicketsByTenant(tickets, tenantId);
            log.info("Filtered to {} tickets for tenant: {}", filteredTickets.size(), tenantId);
            
            int functionalCount = 0;
            int nonFunctionalCount = 0;
            int skippedNoFiledDate = 0;
            int skippedAfterDate = 0;
            
            for (Map<String, Object> ticket : filteredTickets) {
                TicketData ticketData = extractTicketData(ticket);
                
                // Filter by date (tickets filed before or on the specified date)
                boolean shouldSkip = false;
                if (ticketData == null) {
                    shouldSkip = true;
                } else if (ticketData.getFiledDate() == null) {
                    skippedNoFiledDate++;
                    shouldSkip = true;
                } else if (ticketData.getFiledDate() > date.getTime()) {
                    skippedAfterDate++;
                    shouldSkip = true;
                }
                
                if (shouldSkip) {
                    continue;
                }
                
                // Count functional vs non-functional
                if (SYSTEM_STATUS_NON_FUNCTIONAL.equals(ticketData.getSystemFunctional())) {
                    nonFunctionalCount++;
                } else {
                    functionalCount++;
                }
            }
            
            log.info("Functional metrics for {} on {}: Functional={}, Non-Functional={}, Skipped(no filedDate)={}, Skipped(after date)={}", 
                tenantId, date, functionalCount, nonFunctionalCount, skippedNoFiledDate, skippedAfterDate);
            
            return FunctionalMetrics.builder()
                .functionalCount(functionalCount)
                .nonFunctionalCount(nonFunctionalCount)
                .build();
                
        } catch (Exception e) {
            log.error("Error getting functional metrics from Elasticsearch for tenant: {} on date: {}", tenantId, date, e);
            return FunctionalMetrics.builder()
                .functionalCount(0)
                .nonFunctionalCount(0)
                .build();
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
     */
    private AgeBucketData getAgeBucketData(String tenantId) {
        try {
            // Query Elasticsearch for all open tickets
            List<Map<String, Object>> tickets = elasticSearchClient.fetchRequiredTickets(0, 10000, false);
            
            // Filter tickets by tenant
            List<Map<String, Object>> filteredTickets = filterTicketsByTenant(tickets, tenantId);
            
            // Group tickets by facility (tenantId) and find oldest ticket per facility
            Map<String, TicketData> oldestTicketByFacility = new HashMap<>();
            
            for (Map<String, Object> ticket : filteredTickets) {
                TicketData ticketData = extractTicketData(ticket);
                
                // Only consider non-functional tickets with valid filedDate and tenantId
                String facilityTenantId = ticketData != null ? ticketData.getTenantId() : null;
                if (ticketData == null 
                    || !SYSTEM_STATUS_NON_FUNCTIONAL.equals(ticketData.getSystemFunctional())
                    || ticketData.getFiledDate() == null
                    || facilityTenantId == null) {
                    continue;
                }
                
                // For each facility, keep only the ticket with the oldest filedDate
                TicketData existing = oldestTicketByFacility.get(facilityTenantId);
                if (existing == null || existing.getFiledDate() == null || 
                    ticketData.getFiledDate() < existing.getFiledDate()) {
                    oldestTicketByFacility.put(facilityTenantId, ticketData);
                }
            }
            
            // Count facilities by age bucket
            int totalLt1Wk = 0;
            int totalLt1Mo = 0;
            int totalLt3Mo = 0;
            
            for (TicketData oldestTicket : oldestTicketByFacility.values()) {
                int ageInDays = computeBusinessDays(oldestTicket.getFiledDate(), System.currentTimeMillis());
                
                // Age bucket logic:
                // 1 Week: 8 ≤ age in days ≤ 30
                // 1 Month: 31 ≤ age in days ≤ 90
                // 3 Month: age in days > 90
                if (ageInDays >= 8 && ageInDays <= 30) {
                    totalLt1Wk++;
                } else if (ageInDays >= 31 && ageInDays <= 90) {
                    totalLt1Mo++;
                } else if (ageInDays > 90) {
                    totalLt3Mo++;
                }
            }
            
            log.info("Age bucket data for {}: Total facilities={}, <1Wk={}, <1Mo={}, <3Mo={}", 
                tenantId, oldestTicketByFacility.size(), totalLt1Wk, totalLt1Mo, totalLt3Mo);
            
            return AgeBucketData.builder()
                .totalLt1Wk(totalLt1Wk)
                .totalLt1Mo(totalLt1Mo)
                .totalLt3Mo(totalLt3Mo)
                .build();
                
        } catch (Exception e) {
            log.error("Error getting age bucket data from Elasticsearch for tenant: {}", tenantId, e);
            return AgeBucketData.builder()
                .totalLt1Wk(0)
                .totalLt1Mo(0)
                .totalLt3Mo(0)
                .build();
        }
    }
    
    /**
     * Get state-wise age bucket data using Elasticsearch
     * Counts unique facilities (HFs) per state, using oldest ticket per facility
     */
    private Map<String, WeeklyReportData.StateAgeBucketData> getStateWiseAgeBucketData(String tenantId) {
        try {
            // Query Elasticsearch for all open tickets
            List<Map<String, Object>> tickets = elasticSearchClient.fetchRequiredTickets(0, 10000, false);
            
            // Filter tickets by tenant
            List<Map<String, Object>> filteredTickets = filterTicketsByTenant(tickets, tenantId);
            
            // Group tickets by state and facility, find oldest ticket per facility
            // Structure: state -> facilityTenantId -> oldest TicketData
            Map<String, Map<String, TicketData>> stateFacilityMap = new HashMap<>();
            
            for (Map<String, Object> ticket : filteredTickets) {
                TicketData ticketData = extractTicketData(ticket);
                
                // Only consider non-functional tickets with valid filedDate and tenantId
                String facilityTenantId = ticketData != null ? ticketData.getTenantId() : null;
                if (ticketData == null 
                    || !SYSTEM_STATUS_NON_FUNCTIONAL.equals(ticketData.getSystemFunctional())
                    || ticketData.getFiledDate() == null
                    || facilityTenantId == null) {
                    continue;
                }
                
                // Extract root state tenant ID
                String rootTenantId = facilityTenantId.contains(".")
                    ? facilityTenantId.substring(0, facilityTenantId.indexOf('.'))
                    : facilityTenantId;
                
                // Get or create state map
                Map<String, TicketData> facilityMap = stateFacilityMap.computeIfAbsent(rootTenantId, k -> new HashMap<>());
                
                // For each facility, keep only the ticket with the oldest filedDate
                TicketData existing = facilityMap.get(facilityTenantId);
                if (existing == null || existing.getFiledDate() == null || 
                    ticketData.getFiledDate() < existing.getFiledDate()) {
                    facilityMap.put(facilityTenantId, ticketData);
                }
            }
            
            // Build state-wise age bucket data
            Map<String, WeeklyReportData.StateAgeBucketData> stateData = new LinkedHashMap<>();
            
            for (Map.Entry<String, Map<String, TicketData>> stateEntry : stateFacilityMap.entrySet()) {
                String rootTenantId = stateEntry.getKey();
                Map<String, TicketData> facilityMap = stateEntry.getValue();
                
                WeeklyReportData.StateAgeBucketData stateBucket = stateData.computeIfAbsent(rootTenantId, 
                    k -> WeeklyReportData.StateAgeBucketData.builder()
                        .stateName(commonUtility.getStateDisplayName(k))
                        .lt1Wk(0)
                        .lt1Mo(0)
                        .lt3Mo(0)
                        .build());
                
                // Count facilities by age bucket for this state
                for (TicketData oldestTicket : facilityMap.values()) {
                    int ageInDays = computeBusinessDays(oldestTicket.getFiledDate(), System.currentTimeMillis());
                    
                    // Age bucket logic
                    if (ageInDays >= 8 && ageInDays <= 30) {
                        stateBucket.setLt1Wk(stateBucket.getLt1Wk() + 1);
                    } else if (ageInDays >= 31 && ageInDays <= 90) {
                        stateBucket.setLt1Mo(stateBucket.getLt1Mo() + 1);
                    } else if (ageInDays > 90) {
                        stateBucket.setLt3Mo(stateBucket.getLt3Mo() + 1);
                    }
                }
            }
            
            log.info("State-wise age bucket data for {}: {} states, total facilities={}", 
                tenantId, stateData.size(), 
                stateFacilityMap.values().stream().mapToInt(Map::size).sum());
            return stateData;
            
        } catch (Exception e) {
            log.error("Error getting state-wise age bucket data from Elasticsearch for tenant: {}", tenantId, e);
            return new HashMap<>();
        }
    }

    /**
     * Get previous week's Monday and Sunday dates
     */
    private Date[] getPreviousWeekDates() {
        Calendar cal = Calendar.getInstance();
        cal.setTimeZone(TimeZone.getTimeZone("Asia/Kolkata"));
        
        // Go back to last Monday
        int dayOfWeek = cal.get(Calendar.DAY_OF_WEEK);
        int daysToSubtract = (dayOfWeek == Calendar.SUNDAY) ? 7 : dayOfWeek - Calendar.MONDAY;
        cal.add(Calendar.DAY_OF_MONTH, -daysToSubtract - 7); // Go back one more week
        
        // Set to start of day (Monday 00:00:00)
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        Date weekStart = cal.getTime();
        
        // Add 6 days to get to Sunday
        cal.add(Calendar.DAY_OF_MONTH, 6);
        
        // Set to end of day (Sunday 23:59:59)
        cal.set(Calendar.HOUR_OF_DAY, 23);
        cal.set(Calendar.MINUTE, 59);
        cal.set(Calendar.SECOND, 59);
        cal.set(Calendar.MILLISECOND, 999);
        Date weekEnd = cal.getTime();
        
        return new Date[]{weekStart, weekEnd};
    }
    
    
    /**
     * Format state list for display
     */
    private String formatStateList(String tenantId, Set<String> states) {
        log.info("formatStateList called with tenantId: {}, states: {}", tenantId, states);
        
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
        return result;
    }
    
    /**
     * Compute business days between two instants using an 8-hour-per-day concept with Sunday off.
     * We treat each non-Sunday calendar day as one business day. This aligns buckets to
     * business-day counts rather than raw wall-clock days.
     */
    private int computeBusinessDays(long startMs, long endMs) {
        if (endMs <= startMs) return 0;
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
        return days;
    }

}
