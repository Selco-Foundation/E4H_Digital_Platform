package org.egov.im.web.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

/**
 * An open ticket matching a facility and component, as shown to the reporter.
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class DuplicateTicket {

        @JsonProperty("incidentId")
        private String incidentId;

        @JsonProperty("component")
        private String component;

        @JsonProperty("applicationStatus")
        private String applicationStatus;

        @JsonProperty("createdTime")
        private Long createdTime;

        @JsonProperty("reporterType")
        private String reporterType;
}
