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
public class ScheduledVisit {
    private String id;
    private String tenantId;
    private String projectId;
    private String amcConfigurationId;
    private AmcConfiguration amcConfiguration;
    private String facilityId;
    private String facilityName;
    private Facility facility;
    private Integer visitNumber;
    private Long scheduledDate;
    private Long actualVisitDate;
    private Long lastVisitDate;
    private String status; // DRAFT, SCHEDULED, APPROVED, etc.
    // Soft-delete flag, distinct from `status`: status is where the visit is in its workflow,
    // isActive is whether the visit still belongs to the plan at all. Cleared when a series is
    // regenerated on a new cadence; searches hide anything with isActive = false.
    @JsonProperty("isActive")
    private Boolean isActive;
    private VisitReport visitReport;
    private Workflow workflow;
    private List<ProcessInstance> processInstances;
    private List<Transaction> transactions;
    private List<ScheduledVisitAssignment> assignments;
    private Map<String, Object> additionalDetails;
    private AuditDetails auditDetails;
}
