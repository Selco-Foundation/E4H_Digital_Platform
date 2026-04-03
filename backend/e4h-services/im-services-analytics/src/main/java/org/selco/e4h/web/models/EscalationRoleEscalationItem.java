package org.selco.e4h.web.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class EscalationRoleEscalationItem {

    @JsonProperty("escalationLevel")
    private String escalationLevel; // LEVEL_ZERO | LEVEL_ONE | LEVEL_TWO

    @JsonProperty("workflowStates")
    private List<String> workflowStates;
}


