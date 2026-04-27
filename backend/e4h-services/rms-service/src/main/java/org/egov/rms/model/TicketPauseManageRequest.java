package org.egov.rms.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.egov.common.contract.request.RequestInfo;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TicketPauseManageRequest {

    public enum Action {
        PAUSE,
        RESUME
    }

    @JsonProperty("RequestInfo")
    private RequestInfo requestInfo;

    @JsonProperty("action")
    private Action action;

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

