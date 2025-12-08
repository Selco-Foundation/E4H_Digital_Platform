package org.egov.rms.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Alert {

    @JsonProperty("id")
    private String id;

    @JsonProperty("facilityId")
    private String facilityId;

    @JsonProperty("hfrId")
    private String hfrId;

    @JsonProperty("alertType")
    private AlertType alertType;

    @JsonProperty("alertSubType")
    private AlertSubType alertSubType;

    @JsonProperty("status")
    private AlertStatus status;

    @JsonProperty("detectedAt")
    private Instant detectedAt;

    @JsonProperty("resolvedAt")
    private Instant resolvedAt;

    @JsonProperty("metadata")
    private String metadata; // JSON string for additional alert details

    @JsonProperty("additionalDetails")
    private Map<String, Object> additionalDetails = null;

    @JsonProperty("ticketId")
    private String ticketId; // IM service ticket ID if created

    @JsonProperty("lastSuppressedAt")
    private Instant lastSuppressedAt;

    public enum AlertType {
        PANEL,
        INVERTER,
        BATTERY,
        GRID
    }

    public enum AlertSubType {
        // Panel
        LOW_GENERATION,
        // Inverter
        SHUTDOWN,
        HIGH_VOLTAGE,
        // Battery
        BURNT_DISCONNECTED,
        DEEP_DISCHARGING,
        OVERCHARGING,
        // Grid
        VOLTAGE_VARIATION_LOW,
        VOLTAGE_VARIATION_HIGH
    }

    public enum AlertStatus {
        ACTIVE,
        SUPPRESSED,
        RESOLVED,
        TICKET_CREATED
    }
}

