package org.egov.field_planner.web.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AssessmentPlanSearchCriteria {

    @JsonProperty("tenantId")
    private String tenantId;

    @JsonProperty("projectId")
    private String projectId;

    @JsonProperty("ids")
    private List<String> ids;
}
