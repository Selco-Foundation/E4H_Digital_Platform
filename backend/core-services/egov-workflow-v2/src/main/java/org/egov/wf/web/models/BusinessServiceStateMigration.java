package org.egov.wf.web.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;
import lombok.*;
import org.egov.common.contract.request.User;
import org.springframework.validation.annotation.Validated;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.List;

/**
 * A Object holds the basic data for a Trade License
 */
@ApiModel(description = "A Object holds the basic data for a Trade License")
@Validated
@javax.annotation.Generated(value = "org.egov.codegen.SpringBootCodegen", date = "2018-12-04T11:26:25.532+05:30")

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@EqualsAndHashCode(of = {"id"})
@ToString
public class BusinessServiceStateMigration {

        @NotNull
        @Size(max=128)
        @JsonProperty("tenantId")
        private String tenantId = null;

        @NotNull
        @Size(max=128)
        @JsonProperty("businessService")
        private String businessService = null;

        @NotNull
        @Size(max=128)
        @JsonProperty("businessServiceUuid")
        private String businessServiceUuid = null;

        @NotNull
        @Size(max=128)
        @JsonProperty("businessServiceSla")
        private String businessServiceSla = null;

        @NotNull
        @Size(max=128)
        @JsonProperty("businessId")
        private String businessId = null;

        @NotNull
        @Size(max=128)
        @JsonProperty("stateUuid")
        private String stateUuid = null;

        @NotNull
        @Size(max=64)
        @JsonProperty("moduleName")
        private String moduleName = null;

        @JsonProperty("state")
        private String state = null;

        @JsonProperty("stateSla")
        private Long stateSla = null;

        @JsonProperty("businesssServiceSla")
        private Long businesssServiceSla = null;

        @JsonProperty("applicationStatus")
        @Size(max=128)
        private String applicationStatus = null;
}

