package org.egov.rms.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Response model for center_details/graph API (Inverter High Voltage)
 * Response structure: { "graphData": {...}, "centerDatas": [...] }
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InverterVoltageResponse {

    @JsonProperty("graphData")
    private GraphData graphData;

    @JsonProperty("centerDatas")
    private List<CenterData> centerDatas;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class GraphData {
        @JsonProperty("pagination")
        private Object pagination; // Can be empty object
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

        @JsonProperty("v")
        private Double voltage;

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
}

