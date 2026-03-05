package org.egov.rms.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Response model for centerDatas/get API
 * Response structure: { "data": [...], "pagination": { "noOfRecords": ... } }
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CenterDatasResponse {

    @JsonProperty("data")
    private List<RMSFacilityData> data;

    @JsonProperty("pagination")
    private Pagination pagination;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Pagination {
        @JsonProperty("noOfRecords")
        private Integer noOfRecords;
    }
}

