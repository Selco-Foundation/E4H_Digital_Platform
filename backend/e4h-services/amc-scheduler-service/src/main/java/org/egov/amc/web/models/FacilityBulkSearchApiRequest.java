package org.egov.amc.web.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.egov.common.contract.request.RequestInfo;

/**
 * Request body for {@code POST /facility-service/v2/facility/_bulk-search}.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FacilityBulkSearchApiRequest {

    @JsonProperty("RequestInfo")
    private RequestInfo requestInfo;

    @JsonProperty("Facility")
    private FacilityBulkSearchCriteria facility;
}
