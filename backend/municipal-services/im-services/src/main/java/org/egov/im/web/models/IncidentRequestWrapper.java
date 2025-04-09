package org.egov.im.web.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.egov.common.contract.request.RequestInfo;
import org.egov.im.web.models.workflow.ProcessInstance;
import org.springframework.validation.annotation.Validated;

/**
 * Request object to fetch the report data
 */
@ApiModel(description = "Request object to fetch the report data")
@Validated
@jakarta.annotation.Generated(value = "org.egov.codegen.SpringBootCodegen")

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class IncidentRequestWrapper {

        @Valid
        @JsonProperty("incidentRequest")
        private IncidentRequest incidentRequest = null;

        @Valid
        @JsonProperty("updatedProcessInstance")
        private ProcessInstance processInstance = null;


}
