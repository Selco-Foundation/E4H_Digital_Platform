package org.egov.field_planner.web.models;

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
public class PlanFacilityDetail {

    @JsonProperty("planFacilityId")
    private String planFacilityId;

    @JsonProperty("assessmentPlanId")
    private String assessmentPlanId;

    @JsonProperty("facilityId")
    private String facilityId;

    @JsonProperty("facilityName")
    private String facilityName;

    @JsonProperty("facilityCategory")
    private String facilityCategory;

    @JsonProperty("facilityType")
    private String facilityType;

    @JsonProperty("district")
    private String district;

    @JsonProperty("block")
    private String block;

    @JsonProperty("phoneStatus")
    private String phoneStatus;

    @JsonProperty("fieldStatus")
    private String fieldStatus;

    @JsonProperty("overallStatus")
    private String overallStatus;

    @JsonProperty("phoneOutcome")
    private String phoneOutcome;

    @JsonProperty("fieldOutcome")
    private String fieldOutcome;

    @JsonProperty("overallManuallySet")
    private Boolean overallManuallySet;

    @JsonProperty("eligibleReason")
    private String eligibleReason;

    @JsonProperty("ineligibleReason")
    private String ineligibleReason;

    @JsonProperty("assessmentCompletionStatus")
    private String assessmentCompletionStatus;

    @JsonProperty("allowedActions")
    private AllowedActions allowedActions;

    @JsonProperty("submissions")
    private List<AssessmentSubmission> submissions;

    @JsonProperty("auditTrail")
    private List<AssessmentAuditEvent> auditTrail;
}
