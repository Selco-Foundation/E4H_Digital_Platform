package org.egov.rms.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RMSApiRequest {

    @JsonProperty("centerId")
    private String centerId;

    @JsonProperty("graphType")
    private String graphType;

    @JsonProperty("graphTypes")
    private List<String> graphTypes;

    @JsonProperty("time_range")
    private TimeRange timeRange;

    @JsonProperty("frequency")
    private String frequency;

    @JsonProperty("aggregation")
    private String aggregation;

    @JsonProperty("pagination")
    private Pagination pagination;

    @JsonProperty("filters")
    private Map<String, Object> filters;

    @JsonProperty("status")
    private List<Map<String, String>> status;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TimeRange {
        @JsonProperty("time_period")
        private TimePeriod timePeriod;

        @JsonProperty("custom_range")
        private Map<String, Object> customRange;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TimePeriod {
        @JsonProperty("label")
        private String label;

        @JsonProperty("value")
        private String value;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Pagination {
        @JsonProperty("page")
        private Integer page;

        @JsonProperty("size")
        private Integer size;
    }
}

