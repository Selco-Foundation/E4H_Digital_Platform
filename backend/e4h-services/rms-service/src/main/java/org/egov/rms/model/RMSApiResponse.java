package org.egov.rms.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RMSApiResponse {

    @JsonProperty("status")
    private String status;

    @JsonProperty("message")
    private String message;

    @JsonProperty("data")
    private RMSResponseData data;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RMSResponseData {
        @JsonProperty("facilities")
        private List<RMSFacilityData> facilities;

        @JsonProperty("lowVoltageFacilities")
        private List<RMSFacilityData> lowVoltageFacilities;

        @JsonProperty("highVoltageFacilities")
        private List<RMSFacilityData> highVoltageFacilities;

        @JsonProperty("pagination")
        private Pagination pagination;
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

        @JsonProperty("totalPages")
        private Integer totalPages;

        @JsonProperty("totalRecords")
        private Integer totalRecords;
    }
}

