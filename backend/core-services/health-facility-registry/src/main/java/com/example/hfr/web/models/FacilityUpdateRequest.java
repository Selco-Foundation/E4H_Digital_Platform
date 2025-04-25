package com.example.hfr.web.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.annotation.Generated;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

import java.util.Objects;

/**
 * FacilityUpdateRequest
 */

@Generated(value = "org.openapitools.codegen.languages.SpringCodegen", date = "2025-04-24T16:00:58.522282987+05:30[Asia/Kolkata]", comments = "Generator version: 7.4.0")
public class FacilityUpdateRequest {

    private FacilityAssessmentCreateRequestRequestInfo requestInfo;

    private FacilityUpdateRequestFacilityUpdate facilityUpdate;

    public FacilityUpdateRequest() {
        super();
    }

    /**
     * Constructor with only required parameters
     */
    public FacilityUpdateRequest(FacilityAssessmentCreateRequestRequestInfo requestInfo) {
        this.requestInfo = requestInfo;
    }

    public FacilityUpdateRequest requestInfo(FacilityAssessmentCreateRequestRequestInfo requestInfo) {
        this.requestInfo = requestInfo;
        return this;
    }

    /**
     * Get requestInfo
     *
     * @return requestInfo
     */
    @NotNull
    @Valid
    @Schema(name = "RequestInfo", requiredMode = Schema.RequiredMode.REQUIRED)
    @JsonProperty("RequestInfo")
    public FacilityAssessmentCreateRequestRequestInfo getRequestInfo() {
        return requestInfo;
    }

    public void setRequestInfo(FacilityAssessmentCreateRequestRequestInfo requestInfo) {
        this.requestInfo = requestInfo;
    }

    public FacilityUpdateRequest facilityUpdate(FacilityUpdateRequestFacilityUpdate facilityUpdate) {
        this.facilityUpdate = facilityUpdate;
        return this;
    }

    /**
     * Get facilityUpdate
     *
     * @return facilityUpdate
     */
    @Valid
    @Schema(name = "FacilityUpdate", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    @JsonProperty("FacilityUpdate")
    public FacilityUpdateRequestFacilityUpdate getFacilityUpdate() {
        return facilityUpdate;
    }

    public void setFacilityUpdate(FacilityUpdateRequestFacilityUpdate facilityUpdate) {
        this.facilityUpdate = facilityUpdate;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        FacilityUpdateRequest facilityUpdateRequest = (FacilityUpdateRequest) o;
        return Objects.equals(this.requestInfo, facilityUpdateRequest.requestInfo) &&
                Objects.equals(this.facilityUpdate, facilityUpdateRequest.facilityUpdate);
    }

    @Override
    public int hashCode() {
        return Objects.hash(requestInfo, facilityUpdate);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class FacilityUpdateRequest {\n");
        sb.append("    requestInfo: ").append(toIndentedString(requestInfo)).append("\n");
        sb.append("    facilityUpdate: ").append(toIndentedString(facilityUpdate)).append("\n");
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

