package org.egov.amc.web.models;

import lombok.*;
import org.egov.common.contract.models.AuditDetails;
import org.egov.common.models.project.Project;

import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AmcConfiguration {
    private String id;
    private String tenantId;
    private String vendorId;
    private Organisation vendor;
    private String facilityId;
    private Facility facility;
    private String projectId;
    private Project project;
    private List<AssetAmc> assetsAmc;
    private List<Map<String, Object>> assetTypes;
    private List<AmcConfigurationAssignment> assignments;
    private Integer durationMonths;
    private Integer visitFrequencyMonths;
    private Long configurationStartDate;
    private Long configurationEndDate;
    private String status; // ACTIVE, EXPIRED, CANCELLED
    private Map<String, Object> additionalDetails;
    private AuditDetails auditDetails;
    private Integer totalVisits;
    private Integer completedVisits;
}

