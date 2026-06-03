package org.egov.web.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.egov.common.contract.request.RequestInfo;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FacilityUpdateRequest {

    @JsonProperty("RequestInfo")
    private RequestInfo requestInfo;

    @JsonProperty("FacilityUpdate")
    private FacilityUpdatePayload facilityUpdate;
}
