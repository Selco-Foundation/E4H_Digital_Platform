package facility.web.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;
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
    private Map<String, String> phcName; // Optional - set to null if not needed

    @JsonProperty("phcType")
    private String phcType;

    @JsonProperty("facilityCategory")
    private String facilityCategory;
    
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

    @JsonProperty("projectName")
    private String projectName;

    @JsonProperty("boundary")
    private BoundaryInfo boundary;

    /** Also used for the AMC Data Dump's "System Type" column - same underlying facility attribute. */
    @JsonProperty("solutionDesignType")
    private String solutionDesignType;

    @JsonProperty("amcInstallationDate")
    private Long amcInstallationDate;

    @JsonProperty("amcApplicable")
    private String amcApplicable;

    @JsonProperty("amcApplicableYears")
    private Integer amcApplicableYears;

    @JsonProperty("amcFrequencyMonths")
    private Integer amcFrequencyMonths;

    @JsonProperty("amcValidTill")
    private Long amcValidTill;

    /**
     * The AMC field staff assigned on the AMC configuration - distinct from {@code mappedVendorName},
     * which vendor-registry derives from the vendor's facility jurisdictions.
     */
    @JsonProperty("amcMappedVendorName")
    private String amcMappedVendorName;

    @JsonProperty("amcMappedVendorUserName")
    private String amcMappedVendorUserName;

    @JsonProperty("amcDueDate1")
    private Long amcDueDate1;
    @JsonProperty("amcDueDate2")
    private Long amcDueDate2;
    @JsonProperty("amcDueDate3")
    private Long amcDueDate3;
    @JsonProperty("amcDueDate4")
    private Long amcDueDate4;
    @JsonProperty("amcDueDate5")
    private Long amcDueDate5;
    @JsonProperty("amcDueDate6")
    private Long amcDueDate6;
    @JsonProperty("amcDueDate7")
    private Long amcDueDate7;
    @JsonProperty("amcDueDate8")
    private Long amcDueDate8;
    @JsonProperty("amcDueDate9")
    private Long amcDueDate9;
    @JsonProperty("amcDueDate10")
    private Long amcDueDate10;

    @JsonProperty("amcVisitDate1")
    private Long amcVisitDate1;
    @JsonProperty("amcVisitDate2")
    private Long amcVisitDate2;
    @JsonProperty("amcVisitDate3")
    private Long amcVisitDate3;
    @JsonProperty("amcVisitDate4")
    private Long amcVisitDate4;
    @JsonProperty("amcVisitDate5")
    private Long amcVisitDate5;
    @JsonProperty("amcVisitDate6")
    private Long amcVisitDate6;
    @JsonProperty("amcVisitDate7")
    private Long amcVisitDate7;
    @JsonProperty("amcVisitDate8")
    private Long amcVisitDate8;
    @JsonProperty("amcVisitDate9")
    private Long amcVisitDate9;
    @JsonProperty("amcVisitDate10")
    private Long amcVisitDate10;

    @JsonProperty("lastModifiedTime")
    private Long lastModifiedTime;
}

