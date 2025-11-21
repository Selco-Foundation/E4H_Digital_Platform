package org.egov.amc.web.models;

import lombok.*;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AssetAmcSearchCriteria {
    private String tenantId;
    private List<String> ids;
    private List<String> assetIds;
    private List<String> amcConfigurationIds;
    private List<String> statuses;
    private Boolean includeLegacy;
    private Long startDateFrom;
    private Long startDateTo;
    private Long endDateFrom;
    private Long endDateTo;
    private boolean isActive;
    private boolean isCountQuery;
}
