package org.egov.inbox.web.model.dss;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.Valid;
import lombok.*;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class MetricResponse {

        @Valid
        @JsonProperty("statusInfo")
        private StatusInfo statusInfo;

        @Valid
        @JsonProperty("responseData")
        private ResponseData responseData;

}
