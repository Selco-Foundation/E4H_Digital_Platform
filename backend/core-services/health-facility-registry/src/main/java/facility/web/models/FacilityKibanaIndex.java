package facility.web.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Model representing the facility data format expected by Kibana indexer
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FacilityKibanaIndex {
    
    @JsonProperty("facilityId")
    private String facilityId;
    
    @JsonProperty("name")
    private String name;
    
    @JsonProperty("phcName")
    private String phcName;
    
    @JsonProperty("phcType")
    private String phcType;
    
    @JsonProperty("tenantId")
    private String tenantId;
    
    @JsonProperty("tenantIdLocalized")
    private String tenantIdLocalized;
    
    @JsonProperty("block")
    private String block;
    
    @JsonProperty("code")
    private String code;
    
    @JsonProperty("district")
    private String district;
    
    @JsonProperty("state")
    private String state;
    
    @JsonProperty("type")
    private String type;
    
    @JsonProperty("isLive")
    private Boolean isLive;
    
    @JsonProperty("synced")
    private Boolean synced;
    
    @JsonProperty("geoPoint")
    private String geoPoint; // Format: "latitude,longitude"
    
    @JsonProperty("totalTickets")
    private Integer totalTickets;
    
    @JsonProperty("openTickets")
    private Integer openTickets;
    
    @JsonProperty("closedTickets")
    private Integer closedTickets;
    
    @JsonProperty("solarPanelStatus")
    private String solarPanelStatus;
    
    @JsonProperty("mappedVendorUserName")
    private String mappedVendorUserName;
    
    @JsonProperty("mappedVendorName")
    private String mappedVendorName;
    
    @JsonProperty("boundary")
    private BoundaryInfo boundary;
}

