package org.egov.rms.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.egov.rms.util.InstantDeserializer;

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

    // For centerDatas/get API response
    @JsonProperty("center_id")
    private String centerId;

    @JsonProperty("center_name")
    private String centerName;

    @JsonProperty("device_name")
    private String deviceName;

    @JsonProperty("node_id")
    private String nodeId;

    @JsonProperty("statusOfDevice")
    private String statusOfDevice;

    @JsonProperty("last_sync_time")
    @JsonDeserialize(using = InstantDeserializer.class)
    private Instant lastSyncTime;

    // HFRID from centerDatas/get API (capital case)
    @JsonProperty("HFRID")
    private String hfrid;

    // Additional fields from centerDatas/get
    @JsonProperty("v")
    private Double voltage;

    @JsonProperty("latitude")
    private Double latitude;

    @JsonProperty("longitude")
    private Double longitude;

    @JsonProperty("centre_type")
    private String centreType;

    @JsonProperty("inverter_capacity")
    private String inverterCapacity;

    @JsonProperty("solar_capacity")
    private String solarCapacity;

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

