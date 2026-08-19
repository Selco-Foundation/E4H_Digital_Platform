package org.egov.amc.web.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.egov.common.contract.models.AuditDetails;

import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AmcConfigurationAssignment {
    private String id;
    private String tenantId;
    private String amcConfigurationId;
    private String assignedUser;
    @JsonProperty("isActive")
    private boolean isActive;
    private AuditDetails auditDetails;
    @JsonProperty("role")
    protected Map<String, Object> role;
    @JsonProperty("additionalDetails")
    private Map<String, Object> additionalDetails = null;
    @JsonProperty("pocNumber")
    private String pocNumber = null;
}
