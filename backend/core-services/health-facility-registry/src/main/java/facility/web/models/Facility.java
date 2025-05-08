package facility.web.models;


import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonValue;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.annotation.Generated;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Size;
import lombok.Setter;

import java.util.Map;
import java.util.Objects;

/**
 * Facility
 */

@Setter
@Generated(value = "org.openapitools.codegen.languages.SpringCodegen", date = "2025-04-24T16:00:58.522282987+05:30[Asia/Kolkata]", comments = "Generator version: 7.4.0")
public class Facility {

    private String tenantId;

    private String facilityId;

    private String facilityCategory;

    private String facilityType;

    private String facilitySubtype;

    private String facilityName;

    private String facilityOwnership;
    private FacilityRegionEnum facilityRegion;
    private FacilityAddress address;
    private Map<String, Object> facilityDetails = null;
    private String wfStatus;
    private Map<String, Object> additionalDetails = null;
    private Boolean isActive;

    public Facility tenantId(String tenantId) {
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

    /**
     * System generated unique identifier for the facility
     *
     * @return facilityId
     */
    @Valid
    @Size(min = 4, max = 36)
    @Schema(name = "facility_id", accessMode = Schema.AccessMode.READ_ONLY, example = "44e128a5-ac7a-4c9a-be4c-224b6bf81b20", description = "System generated unique identifier for the facility", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    @JsonProperty("facility_id")
    public String getFacilityId() {
        return facilityId;
    }

    public Facility facilityCategory(String facilityCategory) {
        this.facilityCategory = facilityCategory;
        return this;
    }

    /**
     * Master code indicating the facility category. For PHCs, the category is HEALTH
     *
     * @return facilityCategory
     */

    @Schema(name = "facility_category", description = "Master code indicating the facility category. For PHCs, the category is HEALTH", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    @JsonProperty("facility_category")
    public String getFacilityCategory() {
        return facilityCategory;
    }

    public Facility facilityType(String facilityType) {
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

    public Facility facilitySubtype(String facilitySubtype) {
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

    public Facility facilityName(String facilityName) {
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

    public Facility facilityOwnership(String facilityOwnership) {
        this.facilityOwnership = facilityOwnership;
        return this;
    }

    /**
     * The facility ownership code as defined in master data. The values to be defined in master data are listed below.
     *
     * @return facilityOwnership
     */

    @Schema(name = "facility_ownership", description = "The facility ownership code as defined in master data. The values to be defined in master data are listed below.", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    @JsonProperty("facility_ownership")
    public String getFacilityOwnership() {
        return facilityOwnership;
    }

    public Facility facilityRegion(FacilityRegionEnum facilityRegion) {
        this.facilityRegion = facilityRegion;
        return this;
    }

    /**
     * Region code where the facility is located. Eg. rural or urban
     *
     * @return facilityRegion
     */

    @Schema(name = "facility_region", example = "RURAL", description = "Region code where the facility is located. Eg. rural or urban", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    @JsonProperty("facility_region")
    public FacilityRegionEnum getFacilityRegion() {
        return facilityRegion;
    }

    public void setFacilityRegion(FacilityRegionEnum facilityRegion) {
        this.facilityRegion = facilityRegion;
    }

    public Facility address(FacilityAddress address) {
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

    public void setAddress(FacilityAddress address) {
        this.address = address;
    }

    public Facility facilityDetails(Map<String, Object> facilityDetails) {
        this.facilityDetails = facilityDetails;
        return this;
    }

    /**
     * A store of key value pairs per facility type. The exact set of key value pairs will be specific to the facility type. The schema can be defined as a master schema. Code should validate the payload against the appropriate schema as per the facility type.
     *
     * @return facilityDetails
     */
    @Valid
    @Schema(name = "facility_details", description = "A store of key value pairs per facility type. The exact set of key value pairs will be specific to the facility type. The schema can be defined as a master schema. Code should validate the payload against the appropriate schema as per the facility type.", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    @JsonProperty("facility_details")
    public Map<String, Object> getFacilityDetails() {
        return facilityDetails;
    }

    public Facility wfStatus(String wfStatus) {
        this.wfStatus = wfStatus;
        return this;
    }

    /**
     * The workflow status of the facility creation. Facilities may need to be approved for creates and edits before they become active in the system.
     *
     * @return wfStatus
     */

    @Schema(name = "wfStatus", description = "The workflow status of the facility creation. Facilities may need to be approved for creates and edits before they become active in the system.", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    @JsonProperty("wfStatus")
    public String getWfStatus() {
        return wfStatus;
    }

    public Facility additionalDetails(Map<String, Object> additionalDetails) {
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

    public Facility isActive(Boolean isActive) {
        this.isActive = isActive;
        return this;
    }

    /**
     * Whether a facility is active or not.
     *
     * @return isActive
     */

    @Schema(name = "isActive", description = "Whether a facility is active or not.", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    @JsonProperty("isActive")
    public Boolean getIsActive() {
        return isActive;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Facility facility = (Facility) o;
        return Objects.equals(this.tenantId, facility.tenantId) &&
                Objects.equals(this.facilityId, facility.facilityId) &&
                Objects.equals(this.facilityCategory, facility.facilityCategory) &&
                Objects.equals(this.facilityType, facility.facilityType) &&
                Objects.equals(this.facilitySubtype, facility.facilitySubtype) &&
                Objects.equals(this.facilityName, facility.facilityName) &&
                Objects.equals(this.facilityOwnership, facility.facilityOwnership) &&
                Objects.equals(this.facilityRegion, facility.facilityRegion) &&
                Objects.equals(this.address, facility.address) &&
                Objects.equals(this.facilityDetails, facility.facilityDetails) &&
                Objects.equals(this.wfStatus, facility.wfStatus) &&
                Objects.equals(this.additionalDetails, facility.additionalDetails) &&
                Objects.equals(this.isActive, facility.isActive);
    }

    @Override
    public int hashCode() {
        return Objects.hash(tenantId, facilityId, facilityCategory, facilityType, facilitySubtype, facilityName, facilityOwnership, facilityRegion, address, facilityDetails, wfStatus, additionalDetails, isActive);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class Facility {\n");
        sb.append("    tenantId: ").append(toIndentedString(tenantId)).append("\n");
        sb.append("    facilityId: ").append(toIndentedString(facilityId)).append("\n");
        sb.append("    facilityCategory: ").append(toIndentedString(facilityCategory)).append("\n");
        sb.append("    facilityType: ").append(toIndentedString(facilityType)).append("\n");
        sb.append("    facilitySubtype: ").append(toIndentedString(facilitySubtype)).append("\n");
        sb.append("    facilityName: ").append(toIndentedString(facilityName)).append("\n");
        sb.append("    facilityOwnership: ").append(toIndentedString(facilityOwnership)).append("\n");
        sb.append("    facilityRegion: ").append(toIndentedString(facilityRegion)).append("\n");
        sb.append("    address: ").append(toIndentedString(address)).append("\n");
        sb.append("    facilityDetails: ").append(toIndentedString(facilityDetails)).append("\n");
        sb.append("    wfStatus: ").append(toIndentedString(wfStatus)).append("\n");
        sb.append("    additionalDetails: ").append(toIndentedString(additionalDetails)).append("\n");
        sb.append("    isActive: ").append(toIndentedString(isActive)).append("\n");
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

    /**
     * Region code where the facility is located. Eg. rural or urban
     */
    public enum FacilityRegionEnum {
        RURAL("RURAL"),

        URBAN("URBAN");

        private String value;

        FacilityRegionEnum(String value) {
            this.value = value;
        }

        @JsonCreator
        public static FacilityRegionEnum fromValue(String value) {
            for (FacilityRegionEnum b : FacilityRegionEnum.values()) {
                if (b.value.equals(value)) {
                    return b;
                }
            }
            throw new IllegalArgumentException("Unexpected value '" + value + "'");
        }

        @JsonValue
        public String getValue() {
            return value;
        }

        @Override
        public String toString() {
            return String.valueOf(value);
        }
    }
}

