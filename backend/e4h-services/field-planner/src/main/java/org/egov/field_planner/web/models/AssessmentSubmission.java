package org.egov.field_planner.web.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AssessmentSubmission {

    @JsonProperty("id")
    private String id;

    @JsonProperty("tenantId")
    private String tenantId;

    @JsonProperty("planFacilityId")
    private String planFacilityId;

    @JsonProperty("planId")
    private String planId;

    @JsonProperty("facilityId")
    private String facilityId;

    @JsonProperty("assessmentPhase")
    private String assessmentPhase;

    @JsonProperty("formType")
    private String formType;

    @JsonProperty("outcome")
    private String outcome;

    @JsonProperty("submittedBy")
    private String submittedBy;

    @JsonProperty("submittedByName")
    private String submittedByName;

    @JsonProperty("submissionData")
    private Map<String, Object> submissionData;

    @JsonProperty("clientSubmissionTime")
    private Long clientSubmissionTime;

    @JsonProperty("serverReceivedTime")
    private Long serverReceivedTime;
}
