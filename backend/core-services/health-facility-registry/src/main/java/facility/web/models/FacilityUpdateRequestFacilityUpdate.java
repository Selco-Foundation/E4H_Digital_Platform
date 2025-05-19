package facility.web.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.annotation.Generated;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Size;
import lombok.Setter;

import java.util.Map;
import java.util.Objects;

/**
 * FacilityUpdateRequestFacilityUpdate
 */

@Setter
@JsonTypeName("FacilityUpdateRequest_FacilityUpdate")
@Generated(value = "org.openapitools.codegen.languages.SpringCodegen", date = "2025-04-24T16:00:58.522282987+05:30[Asia/Kolkata]", comments = "Generator version: 7.4.0")
public class FacilityUpdateRequestFacilityUpdate {

    private String tenantId;

    private String facilityId;

    private String facilityType;

    private String facilitySubtype;

    private String facilityName;

    private FacilityAddress address;

    private Map<String, Object> additionalDetails = null;

    public FacilityUpdateRequestFacilityUpdate tenantId(String tenantId) {
        this.tenantId = tenantId;
        return this;
    }

    /**
     * Get tenantId
     *
     * @return tenantId
     */
    @Size(min = 2, max = 128)
    @Schema(name = "tenant_id", example = "state1.phc1", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    @JsonProperty("tenant_id")
    public String getTenantId() {
        return tenantId;
    }

    public FacilityUpdateRequestFacilityUpdate facilityId(String facilityId) {
        this.facilityId = facilityId;
        return this;
    }

    /**
     * The facility to be updated. This cannot be overritten.
     *
     * @return facilityId
     */

    @Schema(name = "facilityId", accessMode = Schema.AccessMode.READ_ONLY, description = "The facility to be updated. This cannot be overritten.", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    @JsonProperty("facilityId")
    public String getFacilityId() {
        return facilityId;
    }

    public FacilityUpdateRequestFacilityUpdate facilityType(String facilityType) {
        this.facilityType = facilityType;
        return this;
    }

    /**
     * The exact facility type under a category.
     *
     * @return facilityType
     */

    @Schema(name = "facility_type", description = "The exact facility type under a category.", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    @JsonProperty("facility_type")
    public String getFacilityType() {
        return facilityType;
    }

    public FacilityUpdateRequestFacilityUpdate facilitySubtype(String facilitySubtype) {
        this.facilitySubtype = facilitySubtype;
        return this;
    }

    /**
     * Optional facility sub-type
     *
     * @return facilitySubtype
     */

    @Schema(name = "facility_subtype", description = "Optional facility sub-type", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    @JsonProperty("facility_subtype")
    public String getFacilitySubtype() {
        return facilitySubtype;
    }

    public FacilityUpdateRequestFacilityUpdate facilityName(String facilityName) {
        this.facilityName = facilityName;
        return this;
    }

    /**
     * Name of the facility
     *
     * @return facilityName
     */
    @Size(min = 2, max = 256)
    @Schema(name = "facility_name", example = "Gejjalgetta PHC", description = "Name of the facility", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    @JsonProperty("facility_name")
    public String getFacilityName() {
        return facilityName;
    }

    public FacilityUpdateRequestFacilityUpdate address(FacilityAddress address) {
        this.address = address;
        return this;
    }

    /**
     * Get address
     *
     * @return address
     */
    @Valid
    @Schema(name = "address", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    @JsonProperty("address")
    public FacilityAddress getAddress() {
        return address;
    }

    public FacilityUpdateRequestFacilityUpdate additionalDetails(Map<String, Object> additionalDetails) {
        this.additionalDetails = additionalDetails;
        return this;
    }

    /**
     * Extra details to be added as key value attribute pairs as needed. To be used only post release by implementation teams for customization requirements. Not to be used to store UI values etc..
     *
     * @return additionalDetails
     */
    @Valid
    @Schema(name = "additionalDetails", description = "Extra details to be added as key value attribute pairs as needed. To be used only post release by implementation teams for customization requirements. Not to be used to store UI values etc..", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    @JsonProperty("additionalDetails")
    public Map<String, Object> getAdditionalDetails() {
        return additionalDetails;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        FacilityUpdateRequestFacilityUpdate facilityUpdateRequestFacilityUpdate = (FacilityUpdateRequestFacilityUpdate) o;
        return Objects.equals(this.tenantId, facilityUpdateRequestFacilityUpdate.tenantId) &&
                Objects.equals(this.facilityId, facilityUpdateRequestFacilityUpdate.facilityId) &&
                Objects.equals(this.facilityType, facilityUpdateRequestFacilityUpdate.facilityType) &&
                Objects.equals(this.facilitySubtype, facilityUpdateRequestFacilityUpdate.facilitySubtype) &&
                Objects.equals(this.facilityName, facilityUpdateRequestFacilityUpdate.facilityName) &&
                Objects.equals(this.address, facilityUpdateRequestFacilityUpdate.address) &&
                Objects.equals(this.additionalDetails, facilityUpdateRequestFacilityUpdate.additionalDetails);
    }

    @Override
    public int hashCode() {
        return Objects.hash(tenantId, facilityId, facilityType, facilitySubtype, facilityName, address, additionalDetails);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class FacilityUpdateRequestFacilityUpdate {\n");
        sb.append("    tenantId: ").append(toIndentedString(tenantId)).append("\n");
        sb.append("    facilityId: ").append(toIndentedString(facilityId)).append("\n");
        sb.append("    facilityType: ").append(toIndentedString(facilityType)).append("\n");
        sb.append("    facilitySubtype: ").append(toIndentedString(facilitySubtype)).append("\n");
        sb.append("    facilityName: ").append(toIndentedString(facilityName)).append("\n");
        sb.append("    address: ").append(toIndentedString(address)).append("\n");
        sb.append("    additionalDetails: ").append(toIndentedString(additionalDetails)).append("\n");
        sb.append("}");
        return sb.toString();
    }

    /**
     * Convert the given object to string with each line indented by 4 spaces
     * (except the first line).
     */
    private String toIndentedString(Object o) {
        if (o == null) {
            return "null";
        }
        return o.toString().replace("\n", "\n    ");
    }
}

