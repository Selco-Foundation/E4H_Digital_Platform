package org.egov.rms.model;

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
public class TicketPausedFacilityListRequest {

    @JsonProperty("RequestInfo")
    private RequestInfo requestInfo;

    @JsonProperty("Facility")
    private FacilityFilter facility;
}

