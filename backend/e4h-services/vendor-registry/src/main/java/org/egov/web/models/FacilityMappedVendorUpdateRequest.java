package org.egov.web.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.egov.common.contract.request.RequestInfo;

/**
 * Body for facility-service {@code POST /v2/facility/_update-mapped-vendor}.
 *
 * <p>Carries the mapping and nothing else - the facility is named by its boundary code, which this
 * service already holds on the vendor's jurisdiction. A null name and username unmap the facility.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FacilityMappedVendorUpdateRequest {

    @JsonProperty("RequestInfo")
    private RequestInfo requestInfo;

    @JsonProperty("tenantId")
    private String tenantId;

    @JsonProperty("facilityId")
    private String facilityId;

    @JsonProperty("boundaryCode")
    private String boundaryCode;

    @JsonProperty("mappedVendorName")
    private String mappedVendorName;

    @JsonProperty("mappedVendorUserName")
    private String mappedVendorUserName;
}
