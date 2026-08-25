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

    /**
     * {@code DD-MM-YYYY} in IST, not epoch millis - amc-scheduler-service formats every AMC
     * installation/valid-till/due/visit date before pushing it here so the dumps read as calendar dates.
     * See {@code FacilityAmcIndexSyncService#toIndexDate}.
     */
    @JsonProperty("amcInstallationDate")
    private String amcInstallationDate;

    @JsonProperty("amcApplicable")
    private String amcApplicable;

    @JsonProperty("amcApplicableYears")
    private Integer amcApplicableYears;

    @JsonProperty("amcFrequencyMonths")
    private Integer amcFrequencyMonths;

    @JsonProperty("amcValidTill")
    private String amcValidTill;

    /**
     * The AMC field staff assigned on the AMC configuration - distinct from {@code mappedVendorName},
     * which vendor-registry derives from the vendor's facility jurisdictions.
     */
    @JsonProperty("amcMappedVendorName")
    private String amcMappedVendorName;

    @JsonProperty("amcMappedVendorUserName")
    private String amcMappedVendorUserName;

    @JsonProperty("amcDueDate1")
    private String amcDueDate1;
    @JsonProperty("amcDueDate2")
    private String amcDueDate2;
    @JsonProperty("amcDueDate3")
    private String amcDueDate3;
    @JsonProperty("amcDueDate4")
    private String amcDueDate4;
    @JsonProperty("amcDueDate5")
    private String amcDueDate5;
    @JsonProperty("amcDueDate6")
    private String amcDueDate6;
    @JsonProperty("amcDueDate7")
    private String amcDueDate7;
    @JsonProperty("amcDueDate8")
    private String amcDueDate8;
    @JsonProperty("amcDueDate9")
    private String amcDueDate9;
    @JsonProperty("amcDueDate10")
    private String amcDueDate10;

    @JsonProperty("amcVisitDate1")
    private String amcVisitDate1;
    @JsonProperty("amcVisitDate2")
    private String amcVisitDate2;
    @JsonProperty("amcVisitDate3")
    private String amcVisitDate3;
    @JsonProperty("amcVisitDate4")
    private String amcVisitDate4;
    @JsonProperty("amcVisitDate5")
    private String amcVisitDate5;
    @JsonProperty("amcVisitDate6")
    private String amcVisitDate6;
    @JsonProperty("amcVisitDate7")
    private String amcVisitDate7;
    @JsonProperty("amcVisitDate8")
    private String amcVisitDate8;
    @JsonProperty("amcVisitDate9")
    private String amcVisitDate9;
    @JsonProperty("amcVisitDate10")
    private String amcVisitDate10;

    @JsonProperty("lastModifiedTime")
    private Long lastModifiedTime;
}

