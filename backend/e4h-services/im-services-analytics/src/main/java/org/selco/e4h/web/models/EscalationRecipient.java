package org.selco.e4h.web.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.util.List;

/**
 * Model representing an escalation recipient configuration
 * Based on LLD schema: Incident.EscalationRecipient
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class EscalationRecipient {
    
    @JsonProperty("id")
    private Integer id;

    @JsonProperty("active")
    private Boolean active;

    @JsonProperty("recipientRole")
    private String recipientRole;

    @JsonProperty("boundaryLevel")
    private String boundaryLevel; // "state" or "country"

    @JsonProperty("escalations")
    private List<EscalationRoleEscalationItem> escalations;
}
