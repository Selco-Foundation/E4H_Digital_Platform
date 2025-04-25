package com.example.hfr.web.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.annotation.Generated;

import java.util.Objects;

/**
 * Representation of a address. Indiavidual APIs may choose to extend from this using allOf if more details needed to be added in their case.
 */

@Schema(name = "Facility_address", description = "Representation of a address. Indiavidual APIs may choose to extend from this using allOf if more details needed to be added in their case. ")
@JsonTypeName("Facility_address")
@Generated(value = "org.openapitools.codegen.languages.SpringCodegen", date = "2025-04-24T16:00:58.522282987+05:30[Asia/Kolkata]", comments = "Generator version: 7.4.0")
public class FacilityAddress {

    private String tenantId;

    private Double latitude;

    private Double longitude;

    private String addressId;

    private String addressNumber;

    private String addressLine1;

    private String addressLine2;

    private String landmark;

    private String city;

    private String pincode;

    private String detail;

    public FacilityAddress tenantId(String tenantId) {
        this.tenantId = tenantId;
        return this;
    }

    /**
     * Unique Identifier of the tenant to which user primarily belongs
     *
     * @return tenantId
     */

    @Schema(name = "tenantId", description = "Unique Identifier of the tenant to which user primarily belongs", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    @JsonProperty("tenantId")
    public String getTenantId() {
        return tenantId;
    }

    public void setTenantId(String tenantId) {
        this.tenantId = tenantId;
    }

    public FacilityAddress latitude(Double latitude) {
        this.latitude = latitude;
        return this;
    }

    /**
     * latitude of the address
     *
     * @return latitude
     */

    @Schema(name = "latitude", description = "latitude of the address", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    @JsonProperty("latitude")
    public Double getLatitude() {
        return latitude;
    }

    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    public FacilityAddress longitude(Double longitude) {
        this.longitude = longitude;
        return this;
    }

    /**
     * longitude of the address
     *
     * @return longitude
     */

    @Schema(name = "longitude", description = "longitude of the address", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    @JsonProperty("longitude")
    public Double getLongitude() {
        return longitude;
    }

    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }

    public FacilityAddress addressId(String addressId) {
        this.addressId = addressId;
        return this;
    }

    /**
     * System generated id for the address
     *
     * @return addressId
     */

    @Schema(name = "addressId", accessMode = Schema.AccessMode.READ_ONLY, description = "System generated id for the address", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    @JsonProperty("addressId")
    public String getAddressId() {
        return addressId;
    }

    public void setAddressId(String addressId) {
        this.addressId = addressId;
    }

    public FacilityAddress addressNumber(String addressNumber) {
        this.addressNumber = addressNumber;
        return this;
    }

    /**
     * House, Door, Building number in the address
     *
     * @return addressNumber
     */

    @Schema(name = "addressNumber", description = "House, Door, Building number in the address", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    @JsonProperty("addressNumber")
    public String getAddressNumber() {
        return addressNumber;
    }

    public void setAddressNumber(String addressNumber) {
        this.addressNumber = addressNumber;
    }

    public FacilityAddress addressLine1(String addressLine1) {
        this.addressLine1 = addressLine1;
        return this;
    }

    /**
     * Apartment, Block, Street of the address
     *
     * @return addressLine1
     */

    @Schema(name = "addressLine1", description = "Apartment, Block, Street of the address", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    @JsonProperty("addressLine1")
    public String getAddressLine1() {
        return addressLine1;
    }

    public void setAddressLine1(String addressLine1) {
        this.addressLine1 = addressLine1;
    }

    public FacilityAddress addressLine2(String addressLine2) {
        this.addressLine2 = addressLine2;
        return this;
    }

    /**
     * Locality, Area, Zone, Ward of the address
     *
     * @return addressLine2
     */

    @Schema(name = "addressLine2", description = "Locality, Area, Zone, Ward of the address", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    @JsonProperty("addressLine2")
    public String getAddressLine2() {
        return addressLine2;
    }

    public void setAddressLine2(String addressLine2) {
        this.addressLine2 = addressLine2;
    }

    public FacilityAddress landmark(String landmark) {
        this.landmark = landmark;
        return this;
    }

    /**
     * additional landmark to help locate the address
     *
     * @return landmark
     */

    @Schema(name = "landmark", description = "additional landmark to help locate the address", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    @JsonProperty("landmark")
    public String getLandmark() {
        return landmark;
    }

    public void setLandmark(String landmark) {
        this.landmark = landmark;
    }

    public FacilityAddress city(String city) {
        this.city = city;
        return this;
    }

    /**
     * City of the address. Can be represented by the tenantid itself
     *
     * @return city
     */

    @Schema(name = "city", description = "City of the address. Can be represented by the tenantid itself", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    @JsonProperty("city")
    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public FacilityAddress pincode(String pincode) {
        this.pincode = pincode;
        return this;
    }

    /**
     * PIN code of the address. Indian pincodes will usually be all numbers.
     *
     * @return pincode
     */

    @Schema(name = "pincode", description = "PIN code of the address. Indian pincodes will usually be all numbers.", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    @JsonProperty("pincode")
    public String getPincode() {
        return pincode;
    }

    public void setPincode(String pincode) {
        this.pincode = pincode;
    }

    public FacilityAddress detail(String detail) {
        this.detail = detail;
        return this;
    }

    /**
     * more address detail as may be needed
     *
     * @return detail
     */

    @Schema(name = "detail", description = "more address detail as may be needed", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    @JsonProperty("detail")
    public String getDetail() {
        return detail;
    }

    public void setDetail(String detail) {
        this.detail = detail;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        FacilityAddress facilityAddress = (FacilityAddress) o;
        return Objects.equals(this.tenantId, facilityAddress.tenantId) &&
                Objects.equals(this.latitude, facilityAddress.latitude) &&
                Objects.equals(this.longitude, facilityAddress.longitude) &&
                Objects.equals(this.addressId, facilityAddress.addressId) &&
                Objects.equals(this.addressNumber, facilityAddress.addressNumber) &&
                Objects.equals(this.addressLine1, facilityAddress.addressLine1) &&
                Objects.equals(this.addressLine2, facilityAddress.addressLine2) &&
                Objects.equals(this.landmark, facilityAddress.landmark) &&
                Objects.equals(this.city, facilityAddress.city) &&
                Objects.equals(this.pincode, facilityAddress.pincode) &&
                Objects.equals(this.detail, facilityAddress.detail);
    }

    @Override
    public int hashCode() {
        return Objects.hash(tenantId, latitude, longitude, addressId, addressNumber, addressLine1, addressLine2, landmark, city, pincode, detail);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class FacilityAddress {\n");
        sb.append("    tenantId: ").append(toIndentedString(tenantId)).append("\n");
        sb.append("    latitude: ").append(toIndentedString(latitude)).append("\n");
        sb.append("    longitude: ").append(toIndentedString(longitude)).append("\n");
        sb.append("    addressId: ").append(toIndentedString(addressId)).append("\n");
        sb.append("    addressNumber: ").append(toIndentedString(addressNumber)).append("\n");
        sb.append("    addressLine1: ").append(toIndentedString(addressLine1)).append("\n");
        sb.append("    addressLine2: ").append(toIndentedString(addressLine2)).append("\n");
        sb.append("    landmark: ").append(toIndentedString(landmark)).append("\n");
        sb.append("    city: ").append(toIndentedString(city)).append("\n");
        sb.append("    pincode: ").append(toIndentedString(pincode)).append("\n");
        sb.append("    detail: ").append(toIndentedString(detail)).append("\n");
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

