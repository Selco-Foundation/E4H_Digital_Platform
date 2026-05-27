package org.egov.web.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FacilityUpdatePayload {

    @JsonProperty("tenant_id")
    private String tenantId;

    @JsonProperty("facility_id")
    private String facilityId;

    @JsonProperty("facility_category")
    private String facilityCategory;

    @JsonProperty("facility_type")
    private String facilityType;

    @JsonProperty("facility_subtype")
    private String facilitySubtype;

    @JsonProperty("facility_name")
    private String facilityName;

    @JsonProperty("boundaryCode")
    private String boundaryCode;

    @JsonProperty("additionalDetails")
    private Map<String, Object> additionalDetails;

    @JsonProperty("mappedVendorName")
    private String mappedVendorName;

    @JsonProperty("mappedVendorUserName")
    private String mappedVendorUserName;

    @JsonProperty("address")
    private Map<String, Object> address;

    @JsonProperty("facility_details")
    private Map<String, Object> facilityDetails;

    @JsonProperty("facility_poc_name")
    private String facilityPocName;

    @JsonProperty("facility_poc_phone")
    private String facilityPocPhone;

    @JsonProperty("facility_poc_email")
    private String facilityPocEmail;

    @JsonProperty("facility_poc_username")
    private String facilityPocUsername;

    @JsonProperty("hfr_id")
    private String hfrId;

    @JsonProperty("nin_id")
    private String ninId;

    @JsonProperty("facility_status")
    private String facilityStatus;

    @JsonProperty("isActive")
    private Boolean isActive;

    @JsonProperty("isOnmReady")
    private Boolean isOnmReady;
}
