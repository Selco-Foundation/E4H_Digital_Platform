package org.egov.field_planner.web.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PlanFacilitySearchCriteria {

    @JsonProperty("planId")
    @NotNull
    private String planId;

    @JsonProperty("filters")
    private PlanFacilityFilters filters;

    @JsonProperty("exportAll")
    private Boolean exportAll;

    @JsonProperty("includeResponseSummary")
    private Boolean includeResponseSummary;
}
