package org.egov.amc.web.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;
import org.egov.common.contract.models.AuditDetails;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ScheduledVisitAssignment {
    private String id;
    private String tenantId;
    private String scheduledVisitId;
    private String assignedUser;
    private User user;
    @JsonProperty("isActive")
    private boolean isActive;
    private Map<String, Object> additionalDetails;
    private AuditDetails auditDetails;
}
