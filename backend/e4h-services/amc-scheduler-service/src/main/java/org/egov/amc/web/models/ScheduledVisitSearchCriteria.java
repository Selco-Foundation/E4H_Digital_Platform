package org.egov.amc.web.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ScheduledVisitSearchCriteria {
    private String tenantId;
    private List<String> ids;
    private List<String> amcConfigurationIds;
    private List<String> facilityIds;
    private List<String> projectsIds;
    private List<String> statuses;
    private Long scheduledDateFrom;
    private Long scheduledDateTo;
    private Long actualDateFrom;
    private Long actualDateTo;
    private List<Integer> visitNumbers;
    private List<String> assignedUsers;
    private String facilityName;
    @JsonProperty("sort_direction")
    private String sortDirection;
    private Boolean includeExpired;
    private boolean isActive;
    private boolean isCountQuery;
}
