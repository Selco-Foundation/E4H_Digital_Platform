package org.selco.e4h.web.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;
import org.egov.common.contract.request.RequestInfo;

/**
 * Model for escalation email request
 * Based on LLD API spec
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class EscalationEmailRequest {
    
    @JsonProperty("RequestInfo")
    private RequestInfo requestInfo;
}
