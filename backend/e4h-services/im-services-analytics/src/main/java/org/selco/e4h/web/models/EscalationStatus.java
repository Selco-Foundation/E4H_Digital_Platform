package org.selco.e4h.web.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

/**
 * Model for escalation status tracking
 * Based on LLD schema: EscalationStatus
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class EscalationStatus {
    
    @JsonProperty("escalationType")
    private String escalationType; // "daily" / "weekly"
    
    @JsonProperty("escalationId")
    private String escalationId;
    
    @JsonProperty("tenantId")
    private String tenantId;
    
    @JsonProperty("recipientRole")
    private String recipientRole;
    
    @JsonProperty("escalationTime")
    private Long escalationTime; // time in millis
    
    @JsonProperty("status")
    private String status; // "SUCCESS" / "FAILED"
    
    @JsonProperty("message")
    private String message; // error message if failed
}