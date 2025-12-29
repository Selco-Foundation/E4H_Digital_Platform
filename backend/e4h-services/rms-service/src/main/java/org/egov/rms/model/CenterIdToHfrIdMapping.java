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
public class CenterIdToHfrIdMapping {

    @JsonProperty("id")
    private String id;

    @JsonProperty("centerId")
    private String centerId;

    @JsonProperty("deviceId")
    private String deviceId;

    @JsonProperty("deviceInstanceId")
    private String deviceInstanceId;

    @JsonProperty("hfrId")
    private String hfrId;

    @JsonProperty("ninId")
    private String ninId;

    @JsonProperty("facilityName")
    private String facilityName;

    @JsonProperty("isActive")
    private Boolean isActive;

    @JsonProperty("lastSyncTime")
    private Instant lastSyncTime;

    @JsonProperty("lastValidatedAt")
    private Instant lastValidatedAt;

    @JsonProperty("createdAt")
    private Instant createdAt;

    @JsonProperty("updatedAt")
    private Instant updatedAt;
}

