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
public class FacilityFilter {

    @JsonProperty("tenantId")
    private List<String> tenantId;

    @JsonProperty("state")
    private List<String> state;

    @JsonProperty("district")
    private List<String> district;

    @JsonProperty("block")
    private List<String> block;

    @JsonProperty("boundaryCodes")
    private List<String> boundaryCodes;

    @JsonProperty("offset")
    private Integer offset;

    @JsonProperty("limit")
    private Integer limit;
}
