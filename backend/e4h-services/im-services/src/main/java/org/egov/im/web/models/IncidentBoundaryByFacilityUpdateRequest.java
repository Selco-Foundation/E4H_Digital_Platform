package org.egov.im.web.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.egov.common.contract.request.RequestInfo;
import org.springframework.validation.annotation.Validated;

/**
 * Bulk-update {@code eg_incident_v2.boundarycode} for all incidents tied to a facility ({@code facilityid}).
 */
@Validated
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class IncidentBoundaryByFacilityUpdateRequest {

    @NotNull
    @JsonProperty("RequestInfo")
    private RequestInfo requestInfo;

    @NotBlank
    @JsonProperty("tenant_id")
    private String tenantId;

    @NotBlank
    @JsonProperty("facility_id")
    private String facilityId;

    @NotBlank
    @JsonProperty("new_boundary_code")
    private String newBoundaryCode;
}
