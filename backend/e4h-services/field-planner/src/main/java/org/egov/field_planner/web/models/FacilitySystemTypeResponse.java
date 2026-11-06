package org.egov.field_planner.web.models;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.egov.common.contract.response.ResponseInfo;

import java.util.List;

/**
 * Response of {@code POST /v1/field-plans/facility/system_type/_search}: one entry per requested
 * facility that is linked to an installation plan.
 */
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class FacilitySystemTypeResponse {

    private ResponseInfo responseInfo;

    private List<FacilitySystemType> facilitySystemTypes;
}
