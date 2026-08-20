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
    // The AMC field staff mapped to this visit: the first active assignment whose HRMS user holds the
    // role configured in `amc.mapped.vendor.role.code`. Like state/district/block above, these are
    // resolved only for the search index payload and never persisted; both stay null when no assignee
    // holds that role, so the index can distinguish "unmapped" from a real name.
    private String mappedVendorName;
    private String mappedVendorUserName;
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
