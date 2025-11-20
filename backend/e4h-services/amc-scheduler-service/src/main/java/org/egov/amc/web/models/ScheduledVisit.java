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
    private String amcConfigurationId;
    private AmcConfiguration amcConfiguration;
    private String facilityId;
    private Facility facility;
    private Integer visitNumber;
    private Long scheduledDate;
    private Long actualVisitDate;
    private String status; // DRAFT, SCHEDULED, APPROVED, etc.
    private VisitReport visitReport;
    private Workflow workflow;
    private List<ProcessInstance> processInstances;
    private List<Transaction> transactions;
    private List<ScheduledVisitAssignment> assignments;
    private Map<String, Object> additionalDetails;
    private AuditDetails auditDetails;
}
