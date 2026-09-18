package org.egov.field_planner.web.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.egov.common.contract.request.RequestInfo;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PlanFacilityDecisionRequest {

    @JsonProperty("RequestInfo")
    @NotNull
    @Valid
    private RequestInfo requestInfo;

    @JsonProperty("planFacilityId")
    @NotNull
    private String planFacilityId;

    @JsonProperty("assignForField")
    private Boolean assignForField;

    @JsonProperty("overallStatus")
    private String overallStatus;

    @JsonProperty("remarks")
    private String remarks;
}
