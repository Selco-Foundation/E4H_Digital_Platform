package org.selco.e4h.web.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class EscalationInfo {
    @JsonProperty("escalationId")
    private String escalationId;

    @JsonProperty("escalationTime")
    private Long escalationTime;

    @JsonProperty("escalationLevel")
    private String escalationLevel;

    @JsonProperty("recipientRole")
    private String recipientRole;
}
