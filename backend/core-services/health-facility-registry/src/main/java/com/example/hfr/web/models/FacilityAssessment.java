package com.example.hfr.web.models;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonValue;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.annotation.Generated;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Size;

import java.util.Objects;
import java.util.UUID;

/**
 * FacilityAssessment
 */

@Generated(value = "org.openapitools.codegen.languages.SpringCodegen", date = "2025-04-24T16:00:58.522282987+05:30[Asia/Kolkata]", comments = "Generator version: 7.4.0")
public class FacilityAssessment {

    private String tenantId;

    private UUID assessmentId;

    private UUID facilityId;

    private Long rowVersion;
    private AssessmentTypeEnum assessmentType;
    private String dateOfAssessment;
    private String assessedBy;
    private FinalResultEnum finalResult;
    private Boolean isActive;

    public FacilityAssessment tenantId(String tenantId) {
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

    public void setTenantId(String tenantId) {
        this.tenantId = tenantId;
    }

    public FacilityAssessment assessmentId(UUID assessmentId) {
        this.assessmentId = assessmentId;
        return this;
    }

    /**
     * System generated unique identifier for the assessment created in the survey service
     *
     * @return assessmentId
     */
    @Valid
    @Size(min = 4, max = 36)
    @Schema(name = "assessment_id", accessMode = Schema.AccessMode.READ_ONLY, example = "44e128a5-ac7a-4c9a-be4c-224b6bf81b20", description = "System generated unique identifier for the assessment created in the survey service", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    @JsonProperty("assessment_id")
    public UUID getAssessmentId() {
        return assessmentId;
    }

    public void setAssessmentId(UUID assessmentId) {
        this.assessmentId = assessmentId;
    }

    public FacilityAssessment facilityId(UUID facilityId) {
        this.facilityId = facilityId;
        return this;
    }

    /**
     * Facility ID for which assessment is being created
     *
     * @return facilityId
     */
    @Valid
    @Size(min = 4, max = 36)
    @Schema(name = "facility_id", accessMode = Schema.AccessMode.READ_ONLY, example = "44e128a5-ac7a-4c9a-be4c-224b6bf81b20", description = "Facility ID for which assessment is being created", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    @JsonProperty("facility_id")
    public UUID getFacilityId() {
        return facilityId;
    }

    public void setFacilityId(UUID facilityId) {
        this.facilityId = facilityId;
    }

    public FacilityAssessment rowVersion(Long rowVersion) {
        this.rowVersion = rowVersion;
        return this;
    }

    /**
     * Field to indicate the version of the row that the client used as a base to perform an update operation. This is to validate and maintain the sequence of updates.
     *
     * @return rowVersion
     */

    @Schema(name = "rowVersion", description = "Field to indicate the version of the row that the client used as a base to perform an update operation. This is to validate and maintain the sequence of updates.", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    @JsonProperty("rowVersion")
    public Long getRowVersion() {
        return rowVersion;
    }

    public void setRowVersion(Long rowVersion) {
        this.rowVersion = rowVersion;
    }

    public FacilityAssessment assessmentType(AssessmentTypeEnum assessmentType) {
        this.assessmentType = assessmentType;
        return this;
    }

    /**
     * Get assessmentType
     *
     * @return assessmentType
     */

    @Schema(name = "assessment_type", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    @JsonProperty("assessment_type")
    public AssessmentTypeEnum getAssessmentType() {
        return assessmentType;
    }

    public void setAssessmentType(AssessmentTypeEnum assessmentType) {
        this.assessmentType = assessmentType;
    }

    public FacilityAssessment dateOfAssessment(String dateOfAssessment) {
        this.dateOfAssessment = dateOfAssessment;
        return this;
    }

    /**
     * Date in epoch time
     *
     * @return dateOfAssessment
     */

    @Schema(name = "date_of_assessment", description = "Date in epoch time", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    @JsonProperty("date_of_assessment")
    public String getDateOfAssessment() {
        return dateOfAssessment;
    }

    public void setDateOfAssessment(String dateOfAssessment) {
        this.dateOfAssessment = dateOfAssessment;
    }

    public FacilityAssessment assessedBy(String assessedBy) {
        this.assessedBy = assessedBy;
        return this;
    }

    /**
     * Name of the person who conducted the assessment
     *
     * @return assessedBy
     */

    @Schema(name = "assessed_by", description = "Name of the person who conducted the assessment", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    @JsonProperty("assessed_by")
    public String getAssessedBy() {
        return assessedBy;
    }

    public void setAssessedBy(String assessedBy) {
        this.assessedBy = assessedBy;
    }

    public FacilityAssessment finalResult(FinalResultEnum finalResult) {
        this.finalResult = finalResult;
        return this;
    }

    /**
     * Final result or assessment of the survey if applicable
     *
     * @return finalResult
     */

    @Schema(name = "final_result", example = "GO", description = "Final result or assessment of the survey if applicable", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    @JsonProperty("final_result")
    public FinalResultEnum getFinalResult() {
        return finalResult;
    }

    public void setFinalResult(FinalResultEnum finalResult) {
        this.finalResult = finalResult;
    }

    public FacilityAssessment isActive(Boolean isActive) {
        this.isActive = isActive;
        return this;
    }

    /**
     * Get isActive
     *
     * @return isActive
     */

    @Schema(name = "isActive", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    @JsonProperty("isActive")
    public Boolean getIsActive() {
        return isActive;
    }

    public void setIsActive(Boolean isActive) {
        this.isActive = isActive;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        FacilityAssessment facilityAssessment = (FacilityAssessment) o;
        return Objects.equals(this.tenantId, facilityAssessment.tenantId) &&
                Objects.equals(this.assessmentId, facilityAssessment.assessmentId) &&
                Objects.equals(this.facilityId, facilityAssessment.facilityId) &&
                Objects.equals(this.rowVersion, facilityAssessment.rowVersion) &&
                Objects.equals(this.assessmentType, facilityAssessment.assessmentType) &&
                Objects.equals(this.dateOfAssessment, facilityAssessment.dateOfAssessment) &&
                Objects.equals(this.assessedBy, facilityAssessment.assessedBy) &&
                Objects.equals(this.finalResult, facilityAssessment.finalResult) &&
                Objects.equals(this.isActive, facilityAssessment.isActive);
    }

    @Override
    public int hashCode() {
        return Objects.hash(tenantId, assessmentId, facilityId, rowVersion, assessmentType, dateOfAssessment, assessedBy, finalResult, isActive);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class FacilityAssessment {\n");
        sb.append("    tenantId: ").append(toIndentedString(tenantId)).append("\n");
        sb.append("    assessmentId: ").append(toIndentedString(assessmentId)).append("\n");
        sb.append("    facilityId: ").append(toIndentedString(facilityId)).append("\n");
        sb.append("    rowVersion: ").append(toIndentedString(rowVersion)).append("\n");
        sb.append("    assessmentType: ").append(toIndentedString(assessmentType)).append("\n");
        sb.append("    dateOfAssessment: ").append(toIndentedString(dateOfAssessment)).append("\n");
        sb.append("    assessedBy: ").append(toIndentedString(assessedBy)).append("\n");
        sb.append("    finalResult: ").append(toIndentedString(finalResult)).append("\n");
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
     * Gets or Sets assessmentType
     */
    public enum AssessmentTypeEnum {
        SOLUTION_DESIGN_TYPE("SOLUTION_DESIGN_TYPE"),

        SOLAR_SUITABILITY_ASSESSMENT("SOLAR_SUITABILITY_ASSESSMENT");

        private String value;

        AssessmentTypeEnum(String value) {
            this.value = value;
        }

        @JsonCreator
        public static AssessmentTypeEnum fromValue(String value) {
            for (AssessmentTypeEnum b : AssessmentTypeEnum.values()) {
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

    /**
     * Final result or assessment of the survey if applicable
     */
    public enum FinalResultEnum {
        GO("GO"),

        NO_GO("NO_GO");

        private String value;

        FinalResultEnum(String value) {
            this.value = value;
        }

        @JsonCreator
        public static FinalResultEnum fromValue(String value) {
            for (FinalResultEnum b : FinalResultEnum.values()) {
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

