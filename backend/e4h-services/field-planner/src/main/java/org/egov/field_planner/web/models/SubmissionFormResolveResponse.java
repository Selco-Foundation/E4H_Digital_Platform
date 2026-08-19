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
public class SubmissionFormResolveResponse {

    @JsonProperty("ResponseInfo")
    private org.egov.common.contract.response.ResponseInfo responseInfo;

    @JsonProperty("formType")
    private String formType;

    @JsonProperty("schema")
    private AssessmentFormSchema schema;
}
