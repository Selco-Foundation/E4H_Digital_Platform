package org.egov.amc.web.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;
import lombok.*;
import org.egov.common.contract.request.RequestInfo;
import org.springframework.validation.annotation.Validated;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.List;

/**
 * Contract class to receive request. Array of TradeLicense items are used in case of create, whereas single TradeLicense item is used for update
 */
@ApiModel(description = "Contract class to receive request. Array of TradeLicense items are used in case of create, whereas single TradeLicense item is used for update")
@Validated
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@ToString
public class ProcessInstanceRequest {
        @JsonProperty("RequestInfo")
        private RequestInfo requestInfo;

        @JsonProperty("ProcessInstances")
        @Valid
        @NotNull
        private List<ProcessInstance> processInstances;


        public ProcessInstanceRequest addProcessInstanceItem(ProcessInstance processInstanceItem) {
            if (this.processInstances == null) {
            this.processInstances = new ArrayList<>();
            }
        this.processInstances.add(processInstanceItem);
        return this;
        }

}

