package org.selco.e4h.web.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Escalation {
    
    @JsonProperty("escalationId")
    private String escalationId;
    
    @JsonProperty("escalationTime")
    private Long escalationTime;
}
