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
public class AssessmentAuditEvent {

    @JsonProperty("event")
    private String event;

    @JsonProperty("timestamp")
    private Long timestamp;

    @JsonProperty("actor")
    private String actor;

    @JsonProperty("assessmentPlanId")
    private String assessmentPlanId;
}
