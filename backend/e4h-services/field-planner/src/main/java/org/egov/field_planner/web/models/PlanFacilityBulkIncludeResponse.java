package org.egov.field_planner.web.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.egov.common.contract.response.ResponseInfo;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PlanFacilityBulkIncludeResponse {

    @JsonProperty("ResponseInfo")
    private ResponseInfo responseInfo;

    @JsonProperty("created")
    private List<PlanFacility> created;

    @JsonProperty("errors")
    private List<PlanFacilityIncludeError> errors;

    @JsonProperty("skipped")
    private List<PlanFacilityIncludeError> skipped;

    @JsonProperty("plan")
    private AssessmentPlan plan;
}
