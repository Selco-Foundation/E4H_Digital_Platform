package org.egov.amc.web.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;
import org.egov.common.contract.models.AuditDetails;

import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AmcPlan {
    private String id;
    private String tenantId;
    // Auto-generated on create as StateCode-AMC-StartYear-NoOfFacility (see AmcPlanService); not
    // settable by the caller, recomputed on update whenever healthFacilityNumber or startDate change.
    private String name;
    private String projectId;
    private Integer healthFacilityNumber;
    private Long startDate;
    private Long endDate;
    private Map<String, Object> geographyScope;
    private List<Map<String, Object>> selectedActivities;
    private String status; // ACTIVE, COMPLETED, CANCELLED
    // Soft-delete flag - matches FieldPlan.isDeleted (the entity AmcPlan is modeled after), not
    // AmcConfiguration's isActive naming.
    @JsonProperty("isDeleted")
    private Boolean isDeleted;
    private Map<String, Object> additionalDetails;
    private AuditDetails auditDetails;
}
