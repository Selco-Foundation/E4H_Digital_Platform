package org.egov.field_planner.web.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.egov.common.contract.request.RequestInfo;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SubmissionPhoneCreateRequest {

    @JsonProperty("RequestInfo")
    @NotNull
    @Valid
    private RequestInfo requestInfo;

    @JsonProperty("planFacilityId")
    @NotNull
    private String planFacilityId;

    @JsonProperty("facilityCategory")
    @NotNull
    private String facilityCategory;

    @JsonProperty("assessmentPhase")
    @NotNull
    private String assessmentPhase;

    @JsonProperty("submissionData")
    @NotNull
    private Map<String, Object> submissionData;

    @JsonProperty("submittedByName")
    private String submittedByName;

    @JsonProperty("clientSubmissionTime")
    private Long clientSubmissionTime;

    @JsonProperty("tenantId")
    private String tenantId;
}
