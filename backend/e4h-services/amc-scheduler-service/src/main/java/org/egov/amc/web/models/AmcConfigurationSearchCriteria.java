package org.egov.amc.web.models;

import lombok.*;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AmcConfigurationSearchCriteria {
    private String tenantId;
    private List<String> ids;
    private List<String> vendorIds;
    private List<String> facilityIds;
    private List<String> projectIds;
    private List<String> statuses;
    private String activeOnDate;
    private Long configurationStartDate;
    private Long configurationEndDate;
    private List<String> assignedUsers;
    private String createdBy;
    private Boolean includeExpired;
    private boolean isActive;
    private boolean isCountQuery;
}
