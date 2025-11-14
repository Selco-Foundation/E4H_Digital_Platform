package org.egov.im.web.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

/**
 * Boundary object containing hierarchy codes for country, state, district, block, and facility
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Boundary {

    @JsonProperty("countryCode")
    private String countryCode;

    @JsonProperty("stateCode")
    private String stateCode;

    @JsonProperty("districtCode")
    private String districtCode;

    @JsonProperty("blockCode")
    private String blockCode;

    @JsonProperty("facilityCode")
    private String facilityCode;
}

