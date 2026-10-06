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
public class SubmissionCreateResponse {

    @JsonProperty("ResponseInfo")
    private org.egov.common.contract.response.ResponseInfo responseInfo;

    @JsonProperty("submission")
    private AssessmentSubmission submission;

    @JsonProperty("facility")
    private PlanFacility facility;

    @JsonProperty("autoEligible")
    private Boolean autoEligible;

    @JsonProperty("autoNotEligible")
    private Boolean autoNotEligible;
}
