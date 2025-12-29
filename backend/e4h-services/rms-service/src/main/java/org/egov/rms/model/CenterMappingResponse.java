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
public class CenterMappingResponse {

    @JsonProperty("list")
    private List<CenterMapping> list;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CenterMapping {
        @JsonProperty("centerId")
        private String centerId;

        @JsonProperty("HFRID")
        private String hfrid;

        @JsonProperty("NIN")
        private String nin;

        @JsonProperty("health_center_name")
        private String healthCenterName;
    }
}

