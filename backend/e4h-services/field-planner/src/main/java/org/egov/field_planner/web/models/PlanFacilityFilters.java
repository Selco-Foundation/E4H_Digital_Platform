package org.egov.field_planner.web.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PlanFacilityFilters {

    @JsonProperty("district")
    private String district;

    @JsonProperty("facilityCategory")
    private String facilityCategory;

    @JsonProperty("facilityType")
    private String facilityType;

    @JsonProperty("phoneStatus")
    private String phoneStatus;

    @JsonProperty("fieldStatus")
    private String fieldStatus;

    @JsonProperty("overallStatus")
    private String overallStatus;
}
