package org.egov.amc.web.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;
import org.egov.common.contract.request.RequestInfo;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ScheduledVisitSearchRequest {
    @JsonProperty("RequestInfo")
    private RequestInfo RequestInfo;
    @JsonProperty("searchCriteria")
    private ScheduledVisitSearchCriteria searchCriteria;
}
