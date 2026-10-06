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
    private List<SystemTypeCapacity> systemTypeCapacities;
}
