package org.selco.e4h.web.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
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
@JsonIgnoreProperties(ignoreUnknown = true)
public class IncidentStatusAgregation {

    @JsonProperty("tenantId")
    private String tenantId;

    @JsonProperty("boundaryCode")
    private String boundaryCode;

    @JsonProperty("totalTickets")
    private int totalOccurences;

    @JsonProperty("openTickets")
    private int totalOpenOccurrences;

    @JsonProperty("closedTickets")
    private int totalCloseOccurrences;

    @JsonProperty("solarPanelStatus")
    private String systemFunctional;

    @JsonProperty("lastModifiedTime")
    private long lastModifiedTime;

    @JsonProperty("block")
    private String block;

    @JsonProperty("code")
    private String code;

    @JsonProperty("district")
    private String district;

    @JsonProperty("isLive")
    private boolean isLive;

    @JsonProperty("name")
    private String name;

    @JsonProperty("phcType")
    private String phcType;

    @JsonProperty("type")
    private String type;

    @JsonProperty("tenantIdLocalized")
    private String tenantIdLocalized;

    @JsonProperty("geoPoint")
    private List<Double> geoPoint;
}
