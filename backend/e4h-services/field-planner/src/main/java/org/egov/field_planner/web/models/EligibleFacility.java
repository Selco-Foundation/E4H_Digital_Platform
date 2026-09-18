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
public class EligibleFacility {

    @JsonProperty("planFacilityId")
    private String planFacilityId;

    @JsonProperty("assessmentPlanId")
    private String assessmentPlanId;

    @JsonProperty("assessmentPlanName")
    private String assessmentPlanName;

    @JsonProperty("facilityId")
    private String facilityId;

    @JsonProperty("facilityName")
    private String facilityName;

    @JsonProperty("assessmentCompletionStatus")
    private String assessmentCompletionStatus;

    @JsonProperty("installationFieldPlanId")
    private String installationFieldPlanId;

    @JsonProperty("overallStatus")
    private String overallStatus;

    @JsonProperty("projectId")
    private String projectId;
}
