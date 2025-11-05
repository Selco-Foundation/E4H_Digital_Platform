package org.egov.rms.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RMSFacilityData {

    @JsonProperty("facilityId")
    private String facilityId;

    @JsonProperty("facilityName")
    private String facilityName;

    @JsonProperty("hfrId")
    private String hfrId;

    @JsonProperty("centerId")
    private String centerId;

    @JsonProperty("centerName")
    private String centerName;

    @JsonProperty("statusOfDevice")
    private String statusOfDevice;

    @JsonProperty("lastSyncTime")
    private Instant lastSyncTime;

    @JsonProperty("solarConsumption")
    private List<Double> solarConsumption;

    @JsonProperty("gridConsumption")
    private List<Double> gridConsumption;

    @JsonProperty("solarPercent")
    private List<Double> solarPercent;

    @JsonProperty("voltageReadings")
    private List<List<Object>> voltageReadings;

    @JsonProperty("batteryVoltage")
    private Double batteryVoltage;

    @JsonProperty("batteryReadings")
    private List<List<Object>> batteryReadings;

    @JsonProperty("gridVoltage")
    private Double gridVoltage;

    @JsonProperty("minVoltage")
    private Double minVoltage;

    @JsonProperty("maxVoltage")
    private Double maxVoltage;

    @JsonProperty("dateRange")
    private List<String> dateRange;
}

