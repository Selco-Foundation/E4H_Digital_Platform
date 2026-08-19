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
public class PlanFacilityDecisionItem {

    @JsonProperty("planFacilityId")
    private String planFacilityId;

    @JsonProperty("assignForField")
    private Boolean assignForField;

    @JsonProperty("overallStatus")
    private String overallStatus;

    @JsonProperty("remarks")
    private String remarks;
}
