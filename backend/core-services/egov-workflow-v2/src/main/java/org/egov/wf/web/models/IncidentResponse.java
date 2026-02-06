package org.egov.wf.web.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;
import lombok.*;
import org.egov.common.contract.response.ResponseInfo;
import org.springframework.validation.annotation.Validated;

import java.util.List;

/**
 * Response to the incident request
 */
@ApiModel(description = "Response to the incident request")
@Validated
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class IncidentResponse {
        @JsonProperty("responseInfo")
        private ResponseInfo responseInfo = null;

        @JsonProperty("IncidentWrappers")
        private List<IncidentWrapper> IncidentWrappers = null;


}

