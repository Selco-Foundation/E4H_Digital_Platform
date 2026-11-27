package org.egov.amc.web.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
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
    private List<String> vendorIds;
    private List<String> states;      // boundary code strings, e.g. "India_Karnataka"
    private List<String> districts;   // boundary code strings
    private List<String> blocks;      // boundary code strings
    private Boolean delayed;
//    private Boolean rejected;
    @JsonProperty("sort_direction")
    private String sortDirection;
    private Boolean includeExpired;
    private boolean isActive;
    private boolean isCountQuery;
    /**
     * Held to the projects the caller is staff on, even for a project manager. Set by the service
     * for a search made on behalf of a signed-in user; never read from a request. Internal searches
     * (reindexing, index sync, visit generation) leave it off and see every visit, as before.
     */
    @JsonIgnore
    private boolean scopeProjectManagerToStaffProjects;
}
