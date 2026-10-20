package org.egov.amc.web.models;

import com.fasterxml.jackson.annotation.JsonProperty;
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
    // Groups this configuration under an AmcPlan for the project. Nullable: configurations created
    // before AmcPlan existed are not backfilled.
    private String amcPlanId;
    private List<AssetAmc> assetsAmc;
    private List<Map<String, Object>> assetTypes;
    private List<AmcConfigurationAssignment> assignments;
    private Integer durationMonths;
    private Integer visitFrequencyMonths;
    private Long configurationStartDate;
    private Long configurationEndDate;
    private String status; // ACTIVE, EXPIRED, CANCELLED
    // Soft-delete flag, distinct from `status`: status is the contract's lifecycle (an EXPIRED
    // configuration is still a real, visible record), isActive is whether the record exists at all.
    // Set on create, cleared by _delete; searches hide anything with isActive = false.
    @JsonProperty("isActive")
    private Boolean isActive;
    private Map<String, Object> geographyDetails = null;
    private Map<String, Object> additionalDetails;
    private AuditDetails auditDetails;
    private Integer totalVisits;
    private Integer completedVisits;
}

