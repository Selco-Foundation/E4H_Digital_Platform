package org.egov.rms.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TicketPausePayload {

    @JsonProperty("action")
    private TicketPauseManageRequest.Action action;

    @JsonProperty("tenantId")
    private String tenantId;

    @JsonProperty("facilityId")
    private String facilityId;

    @JsonProperty("facilityName")
    private String facilityName;

    @JsonProperty("boundaryCode")
    private String boundaryCode;

    @JsonProperty("pausedUntil")
    private Instant pausedUntil;

    @JsonProperty("reason")
    private String reason;
}
