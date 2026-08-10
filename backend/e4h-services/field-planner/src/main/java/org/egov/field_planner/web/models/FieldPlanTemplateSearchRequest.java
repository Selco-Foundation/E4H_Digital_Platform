package org.egov.field_planner.web.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.egov.common.contract.request.RequestInfo;
import org.springframework.validation.annotation.Validated;

@Validated
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class FieldPlanTemplateSearchRequest {

    @JsonProperty("RequestInfo")
    private @NotNull @Valid RequestInfo requestInfo;

    @JsonProperty("FieldPlanTemplate")
    private @NotNull @Valid FieldPlanTemplateSearchCriteria criteria;

}