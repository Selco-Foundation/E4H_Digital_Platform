package org.selco.e4h.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.egov.common.contract.request.RequestInfo;
import org.selco.e4h.web.models.WeeklyReportData;
import org.selco.e4h.web.models.FunctionalMetrics;
import org.selco.e4h.web.models.AgeBucketData;
import org.selco.e4h.web.models.ArrowData;
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
    
    static {
        // Set timezone to IST for date formatting
        TimeZone istTimeZone = TimeZone.getTimeZone("Asia/Kolkata");
        DATE_FORMAT.setTimeZone(istTimeZone);
        DATE_RANGE_FORMAT.setTimeZone(istTimeZone);
        TODAY_FORMAT.setTimeZone(istTimeZone);
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
            
            // Calculate arrows for changes
            ArrowData funcArrow = calculateArrow(funcStartPct, funcEndPct, true);
            ArrowData nonFuncArrow = calculateArrow(nonFuncStartPct, nonFuncEndPct, false);
            
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
            
            int functionalCount = 0;
            int nonFunctionalCount = 0;
            
            for (Map<String, Object> ticket : tickets) {
                Map<String, Object> data = (Map<String, Object>) ticket.get("Data");
                if (data != null) {
                    // Filter by tenant
                    String ticketTenantId = (String) data.get("tenantId");
                    if (!tenantId.equals(ticketTenantId)) {
                        continue;
                    }
                    
                    // Filter by date (tickets filed before or on the specified date)
                    Long filedDate = (Long) data.get("filedDate");
                    if (filedDate != null && filedDate <= date.getTime()) {
                        String systemFunctional = (String) data.get("systemFunctional");
                        if ("NON_FUNCTIONAL".equals(systemFunctional)) {
                            nonFunctionalCount++;
                        } else {
                            functionalCount++;
                        }
                    }
                }
            }
            
            log.debug("Functional metrics for {} on {}: Functional={}, Non-Functional={}", 
                tenantId, date, functionalCount, nonFunctionalCount);
            
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
     * Uses the correct age bucket logic as per Slack clarification:
     * - 1 Week: 8 ≤ age in days ≤ 30
     * - 1 Month: 31 ≤ age in days ≤ 90  
     * - 3 Month: age in days > 90
     * - For facilities with multiple tickets, consider only the oldest ticket
     */
    private AgeBucketData getAgeBucketData(String tenantId) {
        try {
            // Query Elasticsearch for all open tickets
            List<Map<String, Object>> tickets = elasticSearchClient.fetchRequiredTickets(0, 10000, false);
            
            int totalLt1Wk = 0;
            int totalLt1Mo = 0;
            int totalLt3Mo = 0;
            
            for (Map<String, Object> ticket : tickets) {
                Map<String, Object> data = (Map<String, Object>) ticket.get("Data");
                if (data != null) {
                    // Filter by tenant
                    String ticketTenantId = (String) data.get("tenantId");
                    if (!tenantId.equals(ticketTenantId)) {
                        continue;
                    }
                    
                    // Only count non-functional tickets
                    String systemFunctional = (String) data.get("systemFunctional");
                    if ("NON_FUNCTIONAL".equals(systemFunctional)) {
                        // Calculate age in days
                        Long filedDate = (Long) data.get("filedDate");
                        if (filedDate != null) {
                            long ageInMillis = System.currentTimeMillis() - filedDate;
                            int ageInDays = (int) (ageInMillis / (1000 * 60 * 60 * 24));
                            
                            // Age bucket logic as per Slack clarification:
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
                    }
                }
            }
            
            log.debug("Age bucket data for {}: <1Wk={}, <1Mo={}, <3Mo={}", 
                tenantId, totalLt1Wk, totalLt1Mo, totalLt3Mo);
            
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
     */
    private Map<String, WeeklyReportData.StateAgeBucketData> getStateWiseAgeBucketData(String tenantId) {
        try {
            // Query Elasticsearch for all open tickets
            List<Map<String, Object>> tickets = elasticSearchClient.fetchRequiredTickets(0, 10000, false);
            
            Map<String, WeeklyReportData.StateAgeBucketData> stateData = new LinkedHashMap<>();
            
            for (Map<String, Object> ticket : tickets) {
                Map<String, Object> data = (Map<String, Object>) ticket.get("Data");
                if (data != null) {
                    // Filter by tenant
                    String ticketTenantId = (String) data.get("tenantId");
                    if (!tenantId.equals(ticketTenantId)) {
                        continue;
                    }
                    
                    // Only count non-functional tickets
                    String systemFunctional = (String) data.get("systemFunctional");
                    if ("NON_FUNCTIONAL".equals(systemFunctional)) {
                        // Calculate age in days
                        Long filedDate = (Long) data.get("filedDate");
                        if (filedDate != null) {
                            long ageInMillis = System.currentTimeMillis() - filedDate;
                            int ageInDays = (int) (ageInMillis / (1000 * 60 * 60 * 24));
                            
                            WeeklyReportData.StateAgeBucketData stateBucket = stateData.computeIfAbsent(ticketTenantId, 
                                k -> WeeklyReportData.StateAgeBucketData.builder()
                                    .stateName(commonUtility.getStateDisplayName(k))
                                    .lt1Wk(0)
                                    .lt1Mo(0)
                                    .lt3Mo(0)
                                    .build());
                            
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
                }
            }
            
            log.debug("State-wise age bucket data for {}: {} states", tenantId, stateData.size());
            return stateData;
            
        } catch (Exception e) {
            log.error("Error getting state-wise age bucket data from Elasticsearch for tenant: {}", tenantId, e);
            return new HashMap<>();
        }
    }
    
    /**
     * Calculate arrow direction and class for percentage changes
     */
    private ArrowData calculateArrow(double startPct, double endPct, boolean isFunctional) {
        double change = endPct - startPct;
        
        if (Math.abs(change) < 0.1) {
            // No significant change
            return ArrowData.builder()
                .arrow("")
                .arrowClass("")
                .build();
        }
        
        if (isFunctional) {
            // For functional systems: increase is good (green up), decrease is bad (red down)
            if (change > 0) {
                return ArrowData.builder()
                    .arrow("↑")
                    .arrowClass("up")
                    .build();
            } else {
                return ArrowData.builder()
                    .arrow("↓")
                    .arrowClass("down")
                    .build();
            }
        } else {
            // For non-functional systems: increase is bad (red up), decrease is good (green down)
            if (change > 0) {
                return ArrowData.builder()
                    .arrow("↑")
                    .arrowClass("down") // Red color
                    .build();
            } else {
                return ArrowData.builder()
                    .arrow("↓")
                    .arrowClass("up") // Green color
                    .build();
            }
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
    
}
