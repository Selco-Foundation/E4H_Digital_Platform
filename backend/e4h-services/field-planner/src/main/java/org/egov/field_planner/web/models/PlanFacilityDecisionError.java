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
public class PlanFacilityDecisionError {

    @JsonProperty("planFacilityId")
    private String planFacilityId;

    @JsonProperty("code")
    private String code;

    @JsonProperty("message")
    private String message;
}
