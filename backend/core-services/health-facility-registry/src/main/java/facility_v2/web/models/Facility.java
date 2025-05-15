package facility_v2.web.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.validation.annotation.Validated;

/**
 * Facility
 */
@Validated
@javax.annotation.Generated(value = "org.egov.codegen.SpringBootCodegen", date = "2025-05-14T17:15:00.238919256+05:30[Asia/Kolkata]")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Facility {
    @JsonProperty("tenant_id")

    private Object tenantId = null;

    @JsonProperty("facility_id")

    private Object facilityId = null;

    @JsonProperty("facility_category")

    private Object facilityCategory = null;

    @JsonProperty("facility_type")

    private Object facilityType = null;

    @JsonProperty("facility_subtype")

    private Object facilitySubtype = null;

    @JsonProperty("facility_name")

    private Object facilityName = null;

    @JsonProperty("facility_ownership")

    private Object facilityOwnership = null;

    @JsonProperty("facility_region")

    private Object facilityRegion = null;

    @JsonProperty("address")

    private Object address = null;

    @JsonProperty("facility_details")

    private Object facilityDetails = null;

    @JsonProperty("wfStatus")

    private Object wfStatus = null;

    @JsonProperty("additionalDetails")

    private Object additionalDetails = null;

    @JsonProperty("isActive")

    private Object isActive = null;


}
