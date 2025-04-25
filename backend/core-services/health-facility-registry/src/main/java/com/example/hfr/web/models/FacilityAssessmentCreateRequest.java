package com.example.hfr.web.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.annotation.Generated;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * FacilityAssessmentCreateRequest
 */

@Generated(value = "org.openapitools.codegen.languages.SpringCodegen", date = "2025-04-24T16:00:58.522282987+05:30[Asia/Kolkata]", comments = "Generator version: 7.4.0")
public class FacilityAssessmentCreateRequest {

    private FacilityAssessmentCreateRequestRequestInfo requestInfo;

    @Valid
    private List<@Valid FacilityAssessment> assessments = new ArrayList<>();

    public FacilityAssessmentCreateRequest() {
        super();
    }

    /**
     * Constructor with only required parameters
     */
    public FacilityAssessmentCreateRequest(FacilityAssessmentCreateRequestRequestInfo requestInfo, List<@Valid FacilityAssessment> assessments) {
        this.requestInfo = requestInfo;
        this.assessments = assessments;
    }

    public FacilityAssessmentCreateRequest requestInfo(FacilityAssessmentCreateRequestRequestInfo requestInfo) {
        this.requestInfo = requestInfo;
        return this;
    }

    /**
     * Get requestInfo
     *
     * @return requestInfo
     */
    @NotNull
    @Valid
    @Schema(name = "RequestInfo", requiredMode = Schema.RequiredMode.REQUIRED)
    @JsonProperty("RequestInfo")
    public FacilityAssessmentCreateRequestRequestInfo getRequestInfo() {
        return requestInfo;
    }

    public void setRequestInfo(FacilityAssessmentCreateRequestRequestInfo requestInfo) {
        this.requestInfo = requestInfo;
    }

    public FacilityAssessmentCreateRequest assessments(List<@Valid FacilityAssessment> assessments) {
        this.assessments = assessments;
        return this;
    }

    public FacilityAssessmentCreateRequest addAssessmentsItem(FacilityAssessment assessmentsItem) {
        if (this.assessments == null) {
            this.assessments = new ArrayList<>();
        }
        this.assessments.add(assessmentsItem);
        return this;
    }

    /**
     * Get assessments
     *
     * @return assessments
     */
    @NotNull
    @Valid
    @Schema(name = "assessments", requiredMode = Schema.RequiredMode.REQUIRED)
    @JsonProperty("assessments")
    public List<@Valid FacilityAssessment> getAssessments() {
        return assessments;
    }

    public void setAssessments(List<@Valid FacilityAssessment> assessments) {
        this.assessments = assessments;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        FacilityAssessmentCreateRequest facilityAssessmentCreateRequest = (FacilityAssessmentCreateRequest) o;
        return Objects.equals(this.requestInfo, facilityAssessmentCreateRequest.requestInfo) &&
                Objects.equals(this.assessments, facilityAssessmentCreateRequest.assessments);
    }

    @Override
    public int hashCode() {
        return Objects.hash(requestInfo, assessments);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class FacilityAssessmentCreateRequest {\n");
        sb.append("    requestInfo: ").append(toIndentedString(requestInfo)).append("\n");
        sb.append("    assessments: ").append(toIndentedString(assessments)).append("\n");
        sb.append("}");
        return sb.toString();
    }

    /**
     * Convert the given object to string with each line indented by 4 spaces
     * (except the first line).
     */
    private String toIndentedString(Object o) {
        if (o == null) {
            return "null";
        }
        return o.toString().replace("\n", "\n    ");
    }
}

