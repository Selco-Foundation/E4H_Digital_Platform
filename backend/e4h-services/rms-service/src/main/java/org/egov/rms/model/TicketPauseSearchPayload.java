package org.egov.rms.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TicketPauseSearchPayload {

    @JsonProperty("facilityId")
    private String facilityId;

    @JsonProperty("tenantId")
    private String tenantId;
}
