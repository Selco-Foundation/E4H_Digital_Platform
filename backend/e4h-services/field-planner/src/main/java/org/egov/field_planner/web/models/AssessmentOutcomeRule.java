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
public class AssessmentOutcomeRule {

    @JsonProperty("ruleId")
    private String ruleId;

    @JsonProperty("fieldCode")
    private String fieldCode;

    @JsonProperty("operator")
    private String operator;

    @JsonProperty("values")
    private List<String> values;

    @JsonProperty("outcome")
    private String outcome;

    @JsonProperty("priority")
    private Integer priority;
}
