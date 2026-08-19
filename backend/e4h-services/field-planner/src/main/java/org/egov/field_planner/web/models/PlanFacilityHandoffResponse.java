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
public class PlanFacilityHandoffResponse {

    @JsonProperty("ResponseInfo")
    private org.egov.common.contract.response.ResponseInfo responseInfo;

    @JsonProperty("facility")
    private PlanFacility facility;
}
