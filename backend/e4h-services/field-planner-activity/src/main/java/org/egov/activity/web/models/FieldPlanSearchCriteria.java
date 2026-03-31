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
public class FieldPlanSearchCriteria {

    @JsonProperty("ids")
    private @Valid List<String> ids = null;

    @JsonProperty("projectIds")
    private @Valid List<String> projectId = null;

    @JsonProperty("statuses")
    private @Valid List<String> statuses = null;

    @JsonProperty("assignedToMe")
    private String assignedToMe = null;

    private String tenantId;

    private Long fromDate;

    private Long toDate;
    
    private boolean isCountQuery;
}
