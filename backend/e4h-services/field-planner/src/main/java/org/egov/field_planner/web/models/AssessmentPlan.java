package org.egov.field_planner.web.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.egov.common.contract.models.AuditDetails;

import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AssessmentPlan {

    @JsonProperty("id")
    private String id;

    @JsonProperty("tenantId")
    private String tenantId;

    @JsonProperty("projectId")
    private String projectId;

    @JsonProperty("name")
    private String name;

    @JsonProperty("state")
    private String state;

    @JsonProperty("startDate")
    private Long startDate;

    @JsonProperty("endDate")
    private Long endDate;

    @JsonProperty("status")
    private String status;

    @JsonProperty("planType")
    private String planType;

    @JsonProperty("healthFacilityCount")
    private Integer healthFacilityCount;

    @JsonProperty("canProceedToFieldPlan")
    private Boolean canProceedToFieldPlan;

    @JsonProperty("geographyDetails")
    private Map<String, Object> geographyDetails;

    @JsonProperty("assessors")
    private List<AssessorAssignment> assessors;

    @JsonProperty("metrics")
    private AssessmentPlanMetrics metrics;

    @JsonProperty("auditDetails")
    private AuditDetails auditDetails;
}
