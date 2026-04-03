package org.egov.activity.web.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.validation.annotation.Validated;

import java.util.List;


/**
 * Encapsulates all parameters for building a project search query.
 */
@Validated
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ActivityFacilitySearchCriteria {

    @JsonProperty("ids")
    private @Valid List<String> ids = null;

    @JsonProperty("fieldPlanIds")
    private @Valid List<String> fieldPlanId = null;

    @JsonProperty("facilityIds")
    private @Valid List<String> facilityId = null;

    @JsonProperty("activityIds")
    private @Valid List<String> activityId = null;

    @JsonProperty("activityCodes")
    private @Valid List<String> activityCodes = null;

    @JsonProperty("statuses")
    private @Valid List<String> statuses = null;

    @JsonProperty("assignedToMe")
    private String assignedToMe = null;

    @JsonProperty("assignedUserId")
    private String assignedUserId = null;

    @JsonProperty("facilityName")
    private @Valid String facilityName = null;

    @JsonProperty("boundaryCodes")
    private @Valid List<String> boundaryCodes = null;

    private String tenantId;

    private boolean isActive;

    private boolean isCountQuery;
}
