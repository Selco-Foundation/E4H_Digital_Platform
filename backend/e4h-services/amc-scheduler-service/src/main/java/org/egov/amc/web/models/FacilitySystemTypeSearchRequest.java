package org.egov.amc.web.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.egov.common.contract.request.RequestInfo;

/**
 * Body of {@code POST field-planner/v1/field-plans/facility/system_type/_search}. Mirrors
 * field-planner's {@code FieldPlanFacilitySearchRequest}.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FacilitySystemTypeSearchRequest {

    @JsonProperty("RequestInfo")
    private RequestInfo requestInfo;

    @JsonProperty("FieldPlanFacility")
    private FacilitySystemTypeSearchCriteria criteria;
}
