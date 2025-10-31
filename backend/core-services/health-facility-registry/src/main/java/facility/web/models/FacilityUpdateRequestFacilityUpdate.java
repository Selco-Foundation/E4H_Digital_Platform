package facility.web.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.annotation.Generated;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.util.Map;

/**
 * FacilityUpdateRequestFacilityUpdate
 */

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonTypeName("FacilityUpdateRequest_FacilityUpdate")
@Generated(value = "org.egov.codegen.SpringBootCodegen", date = "2025-05-14T17:15:00.238919256+05:30[Asia/Kolkata]")
public class FacilityUpdateRequestFacilityUpdate {

    /** Tenant identifier */
    @Size(min = 2, max = 128)
    @Schema(name = "tenant_id", example = "state1.phc1", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    @JsonProperty("tenant_id")
    private String tenantId;

    /** Facility identifier (read‑only) */
    @Schema(name = "facilityId", accessMode = Schema.AccessMode.READ_ONLY, description = "The facility to be updated. This cannot be overwritten.", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    @JsonProperty("facilityId")
    private String facilityId;

    /** Exact facility type under a category */
    @Schema(name = "facility_type", description = "The exact facility type under a category.", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    @JsonProperty("facility_type")
    private String facilityType;

    /** Optional facility sub‑type */
    @Schema(name = "facility_subtype", description = "Optional facility sub-type", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    @JsonProperty("facility_subtype")
    private String facilitySubtype;

    /** Name of the facility */
    @Size(min = 2, max = 256)
    @Schema(name = "facility_name", example = "Gejjalgetta PHC", description = "Name of the facility", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    @JsonProperty("facility_name")
    private String facilityName;

    /** Facility address */
    @Valid
    @Schema(name = "address", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    @JsonProperty("address")
    private FacilityAddress address;

    /** Additional custom details */
    @Valid
    @Schema(name = "additionalDetails", description = "Extra details to be added as key value attribute pairs as needed. To be used only post release by implementation teams for customization requirements. Not to be used to store UI values etc..", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    @JsonProperty("additionalDetails")
    private Map<String, Object> additionalDetails;

    @Schema(name = "boundaryCode", accessMode = Schema.AccessMode.READ_ONLY, requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    @JsonProperty("boundaryCode")
    private String boundaryCode;

    @JsonProperty("facility_details")
    private HealthFacilityDetails facilityDetails = null;

    @Schema(name = "isOnmReady", accessMode = Schema.AccessMode.READ_ONLY, requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    @JsonProperty("isOnmReady")
    private Boolean isOnmReady;
}
