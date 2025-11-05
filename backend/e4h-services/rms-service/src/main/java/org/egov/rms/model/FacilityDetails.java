package org.egov.rms.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FacilityDetails {

    @JsonProperty("facilityId")
    private String facilityId;

    @JsonProperty("facilityName")
    private String facilityName;

    @JsonProperty("hfrId")
    private String hfrId;

    @JsonProperty("ninId")
    private String ninId;

    @JsonProperty("district")
    private String district;

    @JsonProperty("block")
    private String block;

    @JsonProperty("phcType")
    private String phcType;

    @JsonProperty("phcSubType")
    private String phcSubType;

    @JsonProperty("tenantId")
    private String tenantId;
}

