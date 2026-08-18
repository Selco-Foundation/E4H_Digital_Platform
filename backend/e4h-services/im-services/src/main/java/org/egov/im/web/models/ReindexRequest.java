package org.egov.im.web.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.egov.common.contract.request.RequestInfo;
import org.hibernate.validator.constraints.SafeHtml;
import org.springframework.validation.annotation.Validated;

/**
 * Request object to re-publish a single incident's current DB state to the indexer topic
 */
@ApiModel(description = "Request object to re-publish a single incident's current DB state to the indexer topic")
@Validated

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ReindexRequest {

        @NotNull
        @JsonProperty("RequestInfo")
        private RequestInfo requestInfo = null;

        @NotNull
        @SafeHtml
        @JsonProperty("tenantId")
        private String tenantId;

        @NotNull
        @SafeHtml
        @JsonProperty("incidentId")
        private String incidentId;
}
