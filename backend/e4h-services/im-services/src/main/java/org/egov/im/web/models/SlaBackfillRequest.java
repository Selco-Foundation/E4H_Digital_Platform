package org.egov.im.web.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.egov.common.contract.request.RequestInfo;
import org.hibernate.validator.constraints.SafeHtml;
import org.springframework.validation.annotation.Validated;

/**
 * Request object for the script that writes the SLA anchor fields onto already indexed open tickets
 */
@ApiModel(description = "Request object for the script that writes the SLA anchor fields onto already indexed open tickets")
@Validated

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class SlaBackfillRequest {

        @NotNull
        @JsonProperty("RequestInfo")
        private RequestInfo requestInfo = null;

        /** Tenant the BusinessHours master and the workflow definitions are read for. */
        @NotNull
        @SafeHtml
        @JsonProperty("tenantId")
        private String tenantId;

        /** Documents per Elasticsearch page and per bulk update. Defaults to 200. */
        @Min(1)
        @JsonProperty("batchSize")
        private Integer batchSize;

        /** Stop after this many documents, for a rehearsal run; null walks every open ticket. */
        @Min(1)
        @JsonProperty("maxDocuments")
        private Integer maxDocuments;

        /** When true, computes the SLA fields of every ticket without writing anything. */
        @JsonProperty("dryRun")
        private Boolean dryRun;
}
