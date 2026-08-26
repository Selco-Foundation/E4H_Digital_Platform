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

    @JsonProperty("facilityId")
    private String facilityId;

    @JsonProperty("phcName")
    private String phcName;

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

    @JsonProperty("state")
    private String state;

    @JsonProperty("district")
    private String district;

    @JsonProperty("isLive")
    private boolean isLive;

    @JsonProperty("synced")
    private boolean synced;

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

    @JsonProperty("boundary")
    private Boundary boundary;

    @JsonProperty("mappedVendorUserName")
    private String mappedVendorUserName;

    @JsonProperty("mappedVendorName")
    private String mappedVendorName;

    @JsonProperty("projectName")
    private String projectName;

    // AMC fields. Owned by amc-scheduler-service and living only on the health facility index - this
    // service has no source to rebuild them from. They are carried here purely so the full-document
    // re-index this object drives writes them back unchanged instead of dropping them, exactly as
    // mappedVendorName and projectName above are. See FacilityAmcFieldsHelper.
    @JsonProperty("amcApplicable")
    private String amcApplicable;

    @JsonProperty("amcApplicableYears")
    private Integer amcApplicableYears;

    @JsonProperty("amcFrequencyMonths")
    private Integer amcFrequencyMonths;

    @JsonProperty("amcInstallationDate")
    private String amcInstallationDate;

    @JsonProperty("amcValidTill")
    private String amcValidTill;

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
}
