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
public class ActivityAssignmentSearchCriteria {

    @JsonProperty("ids")
    private @Valid List<String> ids = null;

    @JsonProperty("activityIds")
    private @Valid List<String> activityId = null;

    @JsonProperty("fieldPlanIds")
    private @Valid List<String> fieldPlanId = null;

    @JsonProperty("statuses")
    private @Valid List<String> statuses = null;

    @JsonProperty("roles")
    private @Valid List<String> roles = null;

    @JsonProperty("assignedTo")
    private String assignedTo = null;

    @JsonProperty("assignedBy")
    private String assignedBy = null;

    @JsonProperty("fieldPlanCode")
    private @Valid String fieldPlanCode = null;

    private String tenantId;

    private boolean isActive;

    private boolean isCountQuery;
}
