package org.selco.e4h.web.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.util.List;

/**
 * Model representing a recipient role configuration for escalation
 * Based on LLD schema: RecipientRole in EscalationRecipient
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class RecipientRole {
    
    @JsonProperty("role")
    private String role;
    
    @JsonProperty("boundaryLevel")
    private String boundaryLevel; // "state" or "country"
    
    @JsonProperty("workflowStates")
    private List<String> workflowStates;
}
