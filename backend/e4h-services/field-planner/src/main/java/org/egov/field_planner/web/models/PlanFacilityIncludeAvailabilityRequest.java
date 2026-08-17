package org.egov.field_planner.web.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.egov.common.contract.request.RequestInfo;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PlanFacilityIncludeAvailabilityRequest {

    @JsonProperty("RequestInfo")
    @NotNull
    @Valid
    private RequestInfo requestInfo;

    @JsonProperty("planId")
    @NotNull
    private String planId;

    @JsonProperty("tenantId")
    @NotNull
    private String tenantId;

    @JsonProperty("facilityIds")
    private List<String> facilityIds;

    @JsonProperty("filters")
    private SubmissionQueueFilters filters;

    @JsonProperty("sort")
    private SubmissionQueueSort sort;

    @JsonProperty("limit")
    private Integer limit;

    @JsonProperty("offset")
    private Integer offset;
}
