package org.egov.activity.web.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.egov.common.contract.models.AuditDetails;
import org.egov.common.models.core.AdditionalFields;
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

    private static Boolean $default$hasErrors() {
        return Boolean.FALSE;
    }
}
