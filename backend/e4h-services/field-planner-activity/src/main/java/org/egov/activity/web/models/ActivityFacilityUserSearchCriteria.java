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
public class ActivityFacilityUserSearchCriteria {

    @JsonProperty("id")
    private @Valid List<String> id = null;

    @JsonProperty("activityFacilityId")
    private @Valid List<String> activityFacilityId = null;

    @JsonProperty("userId")
    private @Valid List<String> userId = null;

    private String tenantId;

    private boolean isActive;

    private boolean isCountQuery;
}
