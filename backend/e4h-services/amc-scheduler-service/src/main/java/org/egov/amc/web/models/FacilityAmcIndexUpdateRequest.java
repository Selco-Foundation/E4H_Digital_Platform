package org.egov.amc.web.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.egov.common.contract.request.RequestInfo;

import java.util.Map;

/**
 * Body of {@code POST facility-service/v2/facility/_update-amc-index}, which writes a facility's AMC
 * snapshot onto the health facility index only - the facility table is not touched. Mirrors
 * health-facility-registry's {@code FacilityAmcIndexUpdateRequest}.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class FacilityAmcIndexUpdateRequest {

    @JsonProperty("RequestInfo")
    private RequestInfo requestInfo;

    @JsonProperty("facilityId")
    private String facilityId;

    @JsonProperty("tenantId")
    private String tenantId;

    /** AMC fields keyed by index field name; nulls are sent so cleared values are cleared. */
    @JsonProperty("amcFields")
    private Map<String, Object> amcFields;
}
