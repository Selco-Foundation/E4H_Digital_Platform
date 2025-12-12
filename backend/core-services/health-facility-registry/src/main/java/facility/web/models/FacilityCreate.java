package facility.web.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.annotation.Generated;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.validation.annotation.Validated;

import java.util.Map;

/**
 * FacilityCreate
 */
@Validated
@Generated(value = "org.egov.codegen.SpringBootCodegen", date = "2025-05-14T17:15:00.238919256+05:30[Asia/Kolkata]")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class FacilityCreate {
    @JsonProperty("tenant_id")
    private String tenantId = null;

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

    @JsonProperty("facility_details")
    private HealthFacilityDetails facilityDetails = null;

    @JsonProperty("wfStatus")
    private String wfStatus = null;

    @JsonProperty("additionalDetails")
    private Map<String, Object> additionalDetails = null;

    @JsonProperty("isActive")
    private Boolean isActive = null;

    @JsonProperty("blockBoundaryCode")
    @NotBlank(message = "blockBoundaryCode is mandatory")
    private String blockBoundaryCode;

    @JsonProperty("isOnmReady")
    private Boolean isOnmReady = false;
}
