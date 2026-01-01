package org.egov.web.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.egov.common.contract.models.AuditDetails;
import org.springframework.validation.annotation.Validated;

import java.util.Map;

@Validated
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ActivityAssignment {

    @JsonProperty("assignedTo")
    private String assignedTo = null;

    @JsonProperty("assignedBy")
    private String assignedBy = null;

    @JsonProperty("fieldPlanId")
    private String fieldPlanId = null;

    @JsonProperty("pocNumber")
    private String pocNumber = null;

    @JsonProperty("isDeleted")
    private Boolean isDeleted;

    @JsonProperty("id")
    protected String id;

    @JsonProperty("tenantId")
    protected String tenantId;

    @JsonProperty("role")
    protected Map<String, Object> role;

    @JsonProperty("activityId")
    protected String activityId;

    @JsonProperty("activityCode")
    protected String activityCode;

    @JsonProperty("startDate")
    private Long startDate = null;

    @JsonProperty("endDate")
    private Long endDate = null;

    @JsonProperty("status")
    private String status = null;

    @JsonProperty("additionalDetails")
    private Map<String, Object> additionalDetails = null;

    @JsonProperty("hasErrors")
    protected Boolean hasErrors = Boolean.FALSE;

    @JsonProperty("auditDetails")
    protected @Valid AuditDetails auditDetails;

    @JsonProperty("isEmailSent")
    private Boolean isEmailSent;

    private static Boolean $default$hasErrors() {
        return Boolean.FALSE;
    }
}
