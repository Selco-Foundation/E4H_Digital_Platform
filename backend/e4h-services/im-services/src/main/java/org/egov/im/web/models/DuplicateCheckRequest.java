package org.egov.im.web.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.egov.common.contract.request.RequestInfo;
import org.springframework.validation.annotation.Validated;

/**
 * Open tickets of a facility for a component, checked right after the component is chosen
 * (SEM 4.0 Ticket Reporting) - before the reporter adds photos and a description.
 */
@Validated
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class DuplicateCheckRequest {

        @NotNull
        @Valid
        @JsonProperty("RequestInfo")
        private RequestInfo requestInfo = null;

        @NotBlank
        @JsonProperty("tenantId")
        private String tenantId = null;

        @NotBlank
        @JsonProperty("boundaryCode")
        private String boundaryCode = null;

        @NotBlank
        @JsonProperty("component")
        private String component = null;
}
