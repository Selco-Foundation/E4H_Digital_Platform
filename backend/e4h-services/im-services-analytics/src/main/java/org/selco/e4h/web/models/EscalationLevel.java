package org.selco.e4h.web.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

/**
 * Model representing an escalation level configuration
 * Based on LLD schema: Incident.EscalationLevel
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class EscalationLevel {
    
    @JsonProperty("id")
    private Integer id;
    
    @JsonProperty("escalationLevel")
    private String escalationLevel;
    
    @JsonProperty("breachThresholdInHours")
    private Integer breachThresholdInHours;
    
    @JsonProperty("active")
    private Boolean active;
}
