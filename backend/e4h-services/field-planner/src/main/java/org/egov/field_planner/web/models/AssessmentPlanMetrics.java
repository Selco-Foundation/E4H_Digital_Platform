package org.egov.field_planner.web.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AssessmentPlanMetrics {

    @JsonProperty("remoteAssessmentDone")
    private long remoteAssessmentDone;

    @JsonProperty("remoteAssessmentTotal")
    private long remoteAssessmentTotal;

    @JsonProperty("onSiteAssessmentDone")
    private long onSiteAssessmentDone;

    @JsonProperty("onSiteAssessmentAssigned")
    private long onSiteAssessmentAssigned;

    @JsonProperty("eligible")
    private long eligible;

    @JsonProperty("notEligible")
    private long notEligible;

    @JsonProperty("resultPending")
    private long resultPending;
}
