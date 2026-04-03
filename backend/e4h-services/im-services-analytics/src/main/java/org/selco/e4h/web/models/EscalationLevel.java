package org.selco.e4h.web.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

/**
 * Model representing an escalation level configuration
 * Based on LLD V2 schema: Incident.EscalationLevel
 * Supports both percentage and number-based breach calculation strategies
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
    
    @JsonProperty("breachThresholdInPercentage")
    private Integer breachThresholdInPercentage;
    
    @JsonProperty("breachCalculationStrategy")
    private String breachCalculationStrategy;
    
    @JsonProperty("active")
    private Boolean active;
}
