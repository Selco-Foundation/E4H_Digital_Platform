package org.egov.field_planner.web.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.egov.common.contract.request.RequestInfo;
import org.egov.common.models.project.ApiOperation;
import org.springframework.validation.annotation.Validated;

import java.util.ArrayList;
import java.util.List;

@Validated
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class FieldPlanRequest {
    @JsonProperty("RequestInfo")
    private @NotNull @Valid RequestInfo requestInfo = null;

    @JsonProperty("FieldPlans")
    private @NotNull @Valid @Size(min = 1)
    List<FieldPlan> fieldPlans = new ArrayList();

    @JsonProperty("isCascadingProjectDateUpdate")
    private @Valid boolean isCascadingProjectDateUpdate = false;
    
    @JsonProperty("apiOperation")
    private @Valid ApiOperation apiOperation = null;
}
