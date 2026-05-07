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
public class PausedFacilityItem {

    @JsonProperty("facilityId")
    private String facilityId;

    @JsonProperty("facilityName")
    private String facilityName;

    @JsonProperty("boundaryCode")
    private String boundaryCode;

    @JsonProperty("pausedUntil")
    private Instant pausedUntil;

    @JsonProperty("daysLeft")
    private Long daysLeft;

    @JsonProperty("reason")
    private String reason;

    @JsonProperty("pausedBy")
    private String pausedBy;

    @JsonProperty("updatedAt")
    private Instant updatedAt;
}

