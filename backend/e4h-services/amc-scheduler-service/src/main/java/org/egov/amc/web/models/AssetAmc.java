package org.egov.amc.web.models;

import lombok.*;
import org.egov.common.contract.models.AuditDetails;

import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AssetAmc {
    private String id;
    private String tenantId;
    private String assetId;
    private Asset asset;
    private String amcConfigurationId;
    private AmcConfiguration amcConfiguration;
    private Long amcStartDate;
    private Long amcEndDate;
    private String status; // ACTIVE, EXPIRED, UNDER_MAINTENANCE, INACTIVE
    private Boolean isLegacyAsset;
    private Map<String, Object> additionalDetails;
    private AuditDetails auditDetails;
}
