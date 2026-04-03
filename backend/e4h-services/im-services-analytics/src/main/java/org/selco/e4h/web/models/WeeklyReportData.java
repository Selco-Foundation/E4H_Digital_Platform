package org.selco.e4h.web.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * Model for weekly report data containing DRE system metrics and age buckets
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WeeklyReportData {
    
    @JsonProperty("tenantId")
    private String tenantId;
    
    @JsonProperty("dateRange")
    private String dateRange;
    
    @JsonProperty("weekStartDate")
    private String weekStartDate;
    
    @JsonProperty("weekEndDate")
    private String weekEndDate;
    
    // Functional vs Non-Functional metrics at start and end of week
    @JsonProperty("weekStartMetrics")
    private FunctionalMetrics weekStartMetrics;
    
    @JsonProperty("weekEndMetrics")
    private FunctionalMetrics weekEndMetrics;
    
    // Arrow indicators for changes
    @JsonProperty("functionalArrow")
    private ArrowData functionalArrow;
    
    @JsonProperty("nonFunctionalArrow")
    private ArrowData nonFunctionalArrow;
    
    // Age bucket totals
    @JsonProperty("totalAgeBuckets")
    private AgeBucketData totalAgeBuckets;
    
    // State-wise age bucket data
    @JsonProperty("stateData")
    private Map<String, StateAgeBucketData> stateData;
    
    @JsonProperty("stateList")
    private String stateList;
    
    @JsonProperty("todayFormatted")
    private String todayFormatted;
    
    /**
     * Inner class for state-wise age bucket data
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class StateAgeBucketData {
        
        @JsonProperty("stateName")
        private String stateName;
        
        @JsonProperty("lt1Wk")
        private int lt1Wk;
        
        @JsonProperty("lt1Mo")
        private int lt1Mo;
        
        @JsonProperty("lt3Mo")
        private int lt3Mo;
    }
}
