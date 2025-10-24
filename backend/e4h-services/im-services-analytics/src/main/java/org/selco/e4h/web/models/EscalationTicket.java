package org.selco.e4h.web.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;
import java.util.List;
import java.util.Map;

/**
 * Model for escalation ticket data
 * LLD Compliant: Contains all fields specified in LLD email template
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class EscalationTicket {
    
    @JsonProperty("id")
    private String id;
    
    @JsonProperty("ticketNumber")
    private String ticketNumber;
    
    @JsonProperty("district")
    private String district;
    
    @JsonProperty("block")
    private String block;
    
    @JsonProperty("healthFacilityName")
    private String healthFacilityName;
    
    @JsonProperty("healthFacilityType")
    private String healthFacilityType;
    
    @JsonProperty("isSolarSystemWorking")
    private Boolean isSolarSystemWorking;
    
    @JsonProperty("issueType")
    private String issueType;
    
    @JsonProperty("issueSubType")
    private String issueSubType;
    
    @JsonProperty("priority")
    private String priority;
    
    @JsonProperty("mappedVendor")
    private String mappedVendor;
    
    @JsonProperty("currentTicketStatus")
    private String currentTicketStatus;
    
    @JsonProperty("slaComplianceCurrentStatus")
    private Boolean slaComplianceCurrentStatus;
    
    @JsonProperty("definedSlaDurationCurrentStatus")
    private String definedSlaDurationCurrentStatus;
    
    @JsonProperty("slaComplianceOverallTicket")
    private Boolean slaComplianceOverallTicket;
    
    @JsonProperty("definedOverallSlaDuration")
    private String definedOverallSlaDuration;
    
    @JsonProperty("comments")
    private String comments;
    
    @JsonProperty("ticketFiledDate")
    private Long ticketFiledDate;
    
    @JsonProperty("tenantId")
    private String tenantId;
    
    @JsonProperty("applicationStatus")
    private String applicationStatus;
    
    @JsonProperty("slaBreachDetails")
    private String slaBreachDetails;
    
    @JsonProperty("escalationTime")
    private Long escalationTime;
    
    @JsonProperty("escalationId")
    private String escalationId;
    
    // Additional fields used in the code but missing from model
    @JsonProperty("incidentId")
    private String incidentId;
    
    @JsonProperty("incidentType")
    private String incidentType;
    
    @JsonProperty("incidentSubType")
    private String incidentSubType;
    
    @JsonProperty("filedDate")
    private Long filedDate;
    
    @JsonProperty("slaBreachTime")
    private Long slaBreachTime;
    
    @JsonProperty("escalationInfo")
    private List<EscalationInfo> escalationInfo;
    
    @JsonProperty("additionalDetails")
    private Map<String, Object> additionalDetails;
}