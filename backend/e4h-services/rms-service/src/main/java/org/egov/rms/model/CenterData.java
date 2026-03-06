package org.egov.rms.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CenterData {
    @JsonProperty("center_id")
    private String centerId;

    @JsonProperty("center_name")
    private String centerName;

    @JsonProperty("device_name")
    private String deviceName;

    @JsonProperty("node_id")
    private String nodeId;

    @JsonProperty("IntIdOfNode")
    private Long intIdOfNode;

    @JsonProperty("node_name")
    private String nodeName;

    @JsonProperty("latitude")
    private Double latitude;

    @JsonProperty("longitude")
    private Double longitude;

    @JsonProperty("marker_color")
    private String markerColor;

    @JsonProperty("statusOfDevice")
    private String statusOfDevice;

    @JsonProperty("last_sync_time")
    private String lastSyncTime;

    @JsonProperty("parent_nodes")
    private List<String> parentNodes;

    @JsonProperty("stateId")
    private Long stateId;

    @JsonProperty("districtId")
    private Long districtId;

    @JsonProperty("blockId")
    private Long blockId;

    @JsonProperty("centre_type")
    private String centreType;

    @JsonProperty("center_type_id")
    private String centerTypeId;

    @JsonProperty("rms_install_date")
    private String rmsInstallDate;

    @JsonProperty("rms_install_date_in_time")
    private String rmsInstallDateInTime;

    @JsonProperty("inverter_capacity")
    private String inverterCapacity;

    @JsonProperty("solar_capacity")
    private String solarCapacity;

    @JsonProperty("HFRID")
    private String hfrid;

    @JsonProperty("selcoSensorId")
    private String selcoSensorId;

    @JsonProperty("stateName")
    private String stateName;

    @JsonProperty("districtName")
    private String districtName;

    @JsonProperty("blockName")
    private String blockName;

    @JsonProperty("deviceModelUsed")
    private String deviceModelUsed;

    @JsonProperty("deviceModelName")
    private String deviceModelName;

    @JsonProperty("gatewayInstanceID")
    private String gatewayInstanceId;

    @JsonProperty("gatewayInstanceIPAddress")
    private String gatewayInstanceIpAddress;
}
