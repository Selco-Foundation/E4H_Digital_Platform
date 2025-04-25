package com.example.hfr.web.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.annotation.Generated;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * FacilityCreateResponse
 */

@Generated(value = "org.openapitools.codegen.languages.SpringCodegen", date = "2025-04-24T16:00:58.522282987+05:30[Asia/Kolkata]", comments = "Generator version: 7.4.0")
public class FacilityCreateResponse {

    private FacilityCreateResponseResponseInfo responseInfo;

    @Valid
    private List<@Valid Facility> facilities;

    public FacilityCreateResponse() {
        super();
    }

    /**
     * Constructor with only required parameters
     */
    public FacilityCreateResponse(FacilityCreateResponseResponseInfo responseInfo) {
        this.responseInfo = responseInfo;
    }

    public FacilityCreateResponse responseInfo(FacilityCreateResponseResponseInfo responseInfo) {
        this.responseInfo = responseInfo;
        return this;
    }

    /**
     * Get responseInfo
     *
     * @return responseInfo
     */
    @NotNull
    @Valid
    @Schema(name = "ResponseInfo", requiredMode = Schema.RequiredMode.REQUIRED)
    @JsonProperty("ResponseInfo")
    public FacilityCreateResponseResponseInfo getResponseInfo() {
        return responseInfo;
    }

    public void setResponseInfo(FacilityCreateResponseResponseInfo responseInfo) {
        this.responseInfo = responseInfo;
    }

    public FacilityCreateResponse facilities(List<@Valid Facility> facilities) {
        this.facilities = facilities;
        return this;
    }

    public FacilityCreateResponse addFacilitiesItem(Facility facilitiesItem) {
        if (this.facilities == null) {
            this.facilities = new ArrayList<>();
        }
        this.facilities.add(facilitiesItem);
        return this;
    }

    /**
     * Get facilities
     *
     * @return facilities
     */
    @Valid
    @Schema(name = "facilities", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    @JsonProperty("facilities")
    public List<@Valid Facility> getFacilities() {
        return facilities;
    }

    public void setFacilities(List<@Valid Facility> facilities) {
        this.facilities = facilities;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        FacilityCreateResponse facilityCreateResponse = (FacilityCreateResponse) o;
        return Objects.equals(this.responseInfo, facilityCreateResponse.responseInfo) &&
                Objects.equals(this.facilities, facilityCreateResponse.facilities);
    }

    @Override
    public int hashCode() {
        return Objects.hash(responseInfo, facilities);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class FacilityCreateResponse {\n");
        sb.append("    responseInfo: ").append(toIndentedString(responseInfo)).append("\n");
        sb.append("    facilities: ").append(toIndentedString(facilities)).append("\n");
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

