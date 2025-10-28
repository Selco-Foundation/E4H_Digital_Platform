package org.egov.activity.web.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.egov.common.contract.response.ResponseInfo;
import org.springframework.validation.annotation.Validated;

import java.util.ArrayList;
import java.util.List;

@Validated
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class FieldPlanFacilityBulkResponse {
    @JsonProperty("ResponseInfo")
    private @NotNull @Valid ResponseInfo responseInfo = null;
    @JsonProperty("FieldPlanFacilities")
    private List<FieldPlanFacility> fieldPlanFacilities = new ArrayList();
    @JsonProperty("TotalCount")
    private @Valid Long totalCount;
}
