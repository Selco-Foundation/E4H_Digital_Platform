package org.egov.field_planner.web.models;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.egov.common.contract.response.ResponseInfo;

import java.util.List;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SystemTypeCapacityResponse {
    private ResponseInfo responseInfo;
    // Facilities whose additionalFields rectificationSite is "No" or absent
    private List<SystemTypeCapacity> systemTypeCapacities;
    // Facilities whose additionalFields rectificationSite is "Yes"
    private List<SystemTypeCapacity> rectificationSystemTypeCapacities;
}
