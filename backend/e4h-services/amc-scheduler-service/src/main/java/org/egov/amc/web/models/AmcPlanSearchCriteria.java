package org.egov.amc.web.models;

import lombok.*;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AmcPlanSearchCriteria {
    private String tenantId;
    private List<String> ids;
    private List<String> projectIds;
    private List<String> statuses;
    private boolean isCountQuery;
}
