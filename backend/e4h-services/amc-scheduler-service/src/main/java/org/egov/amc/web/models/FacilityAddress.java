package org.egov.amc.web.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FacilityAddress {

    @JsonProperty("addressLine1")
    private String addressLine1;

    @JsonProperty("latitude")
    private Double latitude;

    @JsonProperty("longitude")
    private Double longitude;

    @JsonProperty("state")
    private String state;

    @JsonProperty("district")
    private String district;

    @JsonProperty("block")
    private String block;
}
