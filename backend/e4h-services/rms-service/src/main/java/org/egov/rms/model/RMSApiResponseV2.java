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
public class RMSApiResponseV2 {

    @JsonProperty("status")
    private String status;

    @JsonProperty("pagination")
    private Pagination pagination;

    @JsonProperty("centerDatas")
    private List<CenterData> centerData;

    @JsonProperty("RMSFacility")
    private List<RMSFacilityData> data = null;

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

        @JsonProperty("noOfRecords")
        private Integer noOfRecords;
    }
}

