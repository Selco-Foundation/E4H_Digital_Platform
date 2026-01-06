package org.egov.activity.web.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.egov.common.contract.models.AuditDetails;
import org.egov.common.models.core.AdditionalFields;
import org.springframework.validation.annotation.Validated;

import java.util.List;
import java.util.Map;

@Validated
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ActivityFacility {

    @JsonProperty("facilityId")
    private String facilityId = null;

    @JsonProperty("facility")
    private Facility facility = null;

    @JsonProperty("assignedUser")
    private String assignedUser = null;

    @JsonProperty("assignedEmployeeUser")
    private User assignedEmployeeUser = null;

    @JsonProperty("fieldStaffUsers")
    private List<String> fieldStaffUsers = null;

    @JsonProperty("fieldSupervisorUsers")
    private List<String> fieldSupervisorUsers = null;

    @JsonProperty("reviewerUser")
    private List<String> reviewerUser = null;

    @JsonProperty("fieldPlanId")
    private String fieldPlanId = null;

    @JsonProperty("id")
    protected String id;

    @JsonProperty("tenantId")
    protected String tenantId;

    @JsonProperty("activityId")
    protected String activityId;

    @JsonProperty("activityType")
    protected String activityType;

    @JsonProperty("scheduledAt")
    private Long scheduledAt = null;

    @JsonProperty("activatedAt")
    private Long activatedAt = null;

    @JsonProperty("completedAt")
    private Long completedAt = null;

    @JsonProperty("status")
    private String status = null;

    @JsonProperty("isDeleted")
    private Boolean isDeleted;

    @JsonProperty("additionalDetails")
    private Map<String, Object> additionalDetails = null;

    @JsonProperty("conditionsMet")
    private Object conditionsMet = null;

    @JsonProperty("hasErrors")
    protected Boolean hasErrors = Boolean.FALSE;

    @JsonProperty("additionalFields")
    protected @Valid AdditionalFields additionalFields;

    @JsonProperty("auditDetails")
    protected @Valid AuditDetails auditDetails;
}
