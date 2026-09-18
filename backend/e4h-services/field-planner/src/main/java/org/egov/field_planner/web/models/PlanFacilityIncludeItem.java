package org.egov.field_planner.web.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PlanFacilityIncludeItem {

    @JsonProperty("facilityId")
    @NotNull
    private String facilityId;

    @JsonProperty("facilityCategory")
    private String facilityCategory;

    @JsonProperty("facilityType")
    private String facilityType;

    @JsonProperty("district")
    private String district;

    @JsonProperty("block")
    private String block;

    @JsonProperty("facilityName")
    private String facilityName;
}
