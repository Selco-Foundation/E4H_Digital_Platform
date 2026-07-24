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
    // Localized boundary display names, resolved from facility.additionalDetails.boundary
    // before indexing - not persisted in the DB, populated only for the search index payload.
    private String state;
    private String district;
    private String block;
    private Integer visitNumber;
    private Long scheduledDate;
    private Long actualVisitDate;
    private Long lastVisitDate;
    private String status; // DRAFT, SCHEDULED, APPROVED, etc.
    private VisitReport visitReport;
    private Workflow workflow;
    private List<ProcessInstance> processInstances;
    private List<Transaction> transactions;
    private List<ScheduledVisitAssignment> assignments;
    private Map<String, Object> additionalDetails;
    private AuditDetails auditDetails;
}
