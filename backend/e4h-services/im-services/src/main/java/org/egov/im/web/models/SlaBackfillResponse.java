package org.egov.im.web.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;
import lombok.*;
import org.egov.common.contract.response.ResponseInfo;
import org.springframework.validation.annotation.Validated;

/**
 * Outcome of an SLA backfill run
 */
@ApiModel(description = "Outcome of an SLA backfill run")
@Validated

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class SlaBackfillResponse {

        @JsonProperty("ResponseInfo")
        private ResponseInfo responseInfo;

        @JsonProperty("dryRun")
        private Boolean dryRun;

        /** Open tickets read from the index. */
        @JsonProperty("processed")
        private Integer processed;

        /** Documents Elasticsearch accepted the SLA fields for; on a dry run, the number that would have been sent. */
        @JsonProperty("updated")
        private Integer updated;

        /** Tickets whose SLA could not be computed (no workflow history, unknown business service) or whose update was rejected. */
        @JsonProperty("failed")
        private Integer failed;

        /** Documents passed over entirely, i.e. index anomalies carrying no document or incident id. */
        @JsonProperty("skipped")
        private Integer skipped;
}
