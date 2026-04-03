package org.egov.activity.web.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.annotation.Generated;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.egov.common.contract.models.AuditDetails;
import org.springframework.validation.annotation.Validated;

import java.util.Map;

/**
 * Facility
 */
@Validated
@Generated(value = "org.egov.codegen.SpringBootCodegen", date = "2025-05-14T17:15:00.238919256+05:30[Asia/Kolkata]")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Facility {

    @JsonProperty("id")
    private String id = null;

    @JsonProperty("tenant_id")
    private String tenantId = null;

    @JsonProperty("facility_id")
    private String facilityId = null;

    @JsonProperty("facility_category")
    private String facilityCategory = null;

    @JsonProperty("facility_type")
    private String facilityType = null;

    @JsonProperty("facility_subtype")
    private String facilitySubtype = null;

    @JsonProperty("facility_name")
    private String facilityName = null;

    @JsonProperty("facility_ownership")
    private String facilityOwnership = null;

    @JsonProperty("facility_region")
    private String facilityRegion = null;

    @JsonProperty("address")
    private FacilityAddress address = null;

    @JsonProperty("wfStatus")
    private String wfStatus = null;

    @JsonProperty("additionalDetails")
    private Map<String, Object> additionalDetails = null;

    @JsonProperty("facility_details")
    private Map<String, Object> facilityDetails = null;

    @JsonProperty("isActive")
    private Boolean isActive = null;

    @JsonProperty("boundaryCode")
    private String boundaryCode = null;

    @JsonProperty("boundary")
    private Boundary boundary = null;

    @JsonProperty("isOnmReady")
    private Boolean isOnmReady = false;

    @JsonProperty("facility_poc_name")
    private String facilityPocName = null;

    @JsonProperty("facility_poc_phone")
    private String facilityPocPhone = null;

    @JsonProperty("facility_poc_email")
    private String facilityPocEmail = null;

    @JsonProperty("facility_status")
    private String facilityStatus = null;

    @JsonProperty("hfr_id")
    private String hfrId = null;

    @JsonProperty("nin_id")
    private String ninId = null;

    @JsonProperty("user_id")
    private String userId = null;

    @JsonProperty("auditDetails")
    private AuditDetails auditDetails = null;
}
