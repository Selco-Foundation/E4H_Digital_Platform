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
public class AllowedActions {

    @JsonProperty("assignForField")
    private boolean assignForField;

    @JsonProperty("markEligible")
    private boolean markEligible;

    @JsonProperty("markNotEligible")
    private boolean markNotEligible;
}
