package org.egov.rms.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Response model for center_details/graph API (Panel data)
 * Response structure: { "data": { "graphType": "...", "facilities": [...] } }
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PanelGraphResponse {

    @JsonProperty("data")
    private PanelData data;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PanelData {
        @JsonProperty("graphType")
        private String graphType;

        @JsonProperty("facilities")
        private List<PanelFacility> facilities;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PanelFacility {
        @JsonProperty("deviceInstanceId")
        private String deviceInstanceId;

        @JsonProperty("centerData")
        private CenterData centerData;

        @JsonProperty("consumption")
        private Consumption consumption;

        @JsonProperty("batteryHealth")
        private BatteryHealth batteryHealth;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CenterData {
        @JsonProperty("center_id")
        private String centerId;

        @JsonProperty("center_name")
        private String centerName;

        @JsonProperty("device_name")
        private String deviceName;

        @JsonProperty("HFRID")
        private String hfrid;

        @JsonProperty("statusOfDevice")
        private String statusOfDevice;

        @JsonProperty("last_sync_time")
        private String lastSyncTime;

        @JsonProperty("districtName")
        private String districtName;

        @JsonProperty("blockName")
        private String blockName;

        @JsonProperty("centre_type")
        private String centreType;

        @JsonProperty("inverter_capacity")
        private String inverterCapacity;

        @JsonProperty("solar_capacity")
        private String solarCapacity;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Consumption {
        @JsonProperty("categories")
        private List<String> categories;

        @JsonProperty("solarPercents")
        private List<Double> solarPercents;

        @JsonProperty("solarDatas")
        private List<Double> solarDatas;

        @JsonProperty("gridDatas")
        private List<Double> gridDatas;

        @JsonProperty("totalConsumptionDatas")
        private List<Double> totalConsumptionDatas;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class BatteryHealth {
        @JsonProperty("batteryCharging")
        private Double batteryCharging;

        @JsonProperty("batteryDischarging")
        private Double batteryDischarging;
    }
}

