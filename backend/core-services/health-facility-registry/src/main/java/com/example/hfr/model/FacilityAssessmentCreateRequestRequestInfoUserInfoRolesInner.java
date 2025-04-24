package com.example.hfr.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;
import io.swagger.v3.oas.annotations.media.Schema;

import javax.annotation.Generated;
import javax.validation.constraints.*;
import java.util.Objects;

/**
 * minimal representation of the Roles in the system to be carried along in UserInfo with RequestInfo meta data. Actual authorization service to extend this to have more role related attributes
 */

@Schema(name = "FacilityAssessmentCreateRequest_RequestInfo_userInfo_roles_inner", description = "minimal representation of the Roles in the system to be carried along in UserInfo with RequestInfo meta data. Actual authorization service to extend this to have more role related attributes ")
@JsonTypeName("FacilityAssessmentCreateRequest_RequestInfo_userInfo_roles_inner")
@Generated(value = "org.openapitools.codegen.languages.SpringCodegen", date = "2025-04-24T16:00:58.522282987+05:30[Asia/Kolkata]", comments = "Generator version: 7.4.0")
public class FacilityAssessmentCreateRequestRequestInfoUserInfoRolesInner {

    private String tenantId;

    private String id;

    private String name;

    private String description;

    public FacilityAssessmentCreateRequestRequestInfoUserInfoRolesInner() {
        super();
    }

    /**
     * Constructor with only required parameters
     */
    public FacilityAssessmentCreateRequestRequestInfoUserInfoRolesInner(String tenantId) {
        this.tenantId = tenantId;
    }

    public FacilityAssessmentCreateRequestRequestInfoUserInfoRolesInner tenantId(String tenantId) {
        this.tenantId = tenantId;
        return this;
    }

    /**
     * TenantId for which the role is given
     *
     * @return tenantId
     */
    @NotNull
    @Schema(name = "tenantId", description = "TenantId for which the role is given", requiredMode = Schema.RequiredMode.REQUIRED)
    @JsonProperty("tenantId")
    public String getTenantId() {
        return tenantId;
    }

    public void setTenantId(String tenantId) {
        this.tenantId = tenantId;
    }

    public FacilityAssessmentCreateRequestRequestInfoUserInfoRolesInner id(String id) {
        this.id = id;
        return this;
    }

    /**
     * id of the role
     *
     * @return id
     */

    @Schema(name = "id", description = "id of the role", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    @JsonProperty("id")
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public FacilityAssessmentCreateRequestRequestInfoUserInfoRolesInner name(String name) {
        this.name = name;
        return this;
    }

    /**
     * Unique name of the role
     *
     * @return name
     */
    @Size(max = 64)
    @Schema(name = "name", description = "Unique name of the role", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    @JsonProperty("name")
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public FacilityAssessmentCreateRequestRequestInfoUserInfoRolesInner description(String description) {
        this.description = description;
        return this;
    }

    /**
     * brief description of the role
     *
     * @return description
     */

    @Schema(name = "description", description = "brief description of the role", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    @JsonProperty("description")
    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        FacilityAssessmentCreateRequestRequestInfoUserInfoRolesInner facilityAssessmentCreateRequestRequestInfoUserInfoRolesInner = (FacilityAssessmentCreateRequestRequestInfoUserInfoRolesInner) o;
        return Objects.equals(this.tenantId, facilityAssessmentCreateRequestRequestInfoUserInfoRolesInner.tenantId) &&
                Objects.equals(this.id, facilityAssessmentCreateRequestRequestInfoUserInfoRolesInner.id) &&
                Objects.equals(this.name, facilityAssessmentCreateRequestRequestInfoUserInfoRolesInner.name) &&
                Objects.equals(this.description, facilityAssessmentCreateRequestRequestInfoUserInfoRolesInner.description);
    }

    @Override
    public int hashCode() {
        return Objects.hash(tenantId, id, name, description);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class FacilityAssessmentCreateRequestRequestInfoUserInfoRolesInner {\n");
        sb.append("    tenantId: ").append(toIndentedString(tenantId)).append("\n");
        sb.append("    id: ").append(toIndentedString(id)).append("\n");
        sb.append("    name: ").append(toIndentedString(name)).append("\n");
        sb.append("    description: ").append(toIndentedString(description)).append("\n");
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

