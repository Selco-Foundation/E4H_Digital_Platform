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
public class PlanFacilityDecisionBulkResponse {

    @JsonProperty("ResponseInfo")
    private org.egov.common.contract.response.ResponseInfo responseInfo;

    @JsonProperty("success")
    private List<PlanFacility> success;

    @JsonProperty("errors")
    private List<PlanFacilityDecisionError> errors;
}
