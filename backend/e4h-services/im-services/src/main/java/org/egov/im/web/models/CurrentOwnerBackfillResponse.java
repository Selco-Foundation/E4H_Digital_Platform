package org.egov.im.web.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;
import lombok.*;
import org.egov.common.contract.response.ResponseInfo;
import org.springframework.validation.annotation.Validated;

import java.util.Map;

/**
 * Outcome of a currentOwner backfill run
 */
@ApiModel(description = "Outcome of a currentOwner backfill run")
@Validated

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CurrentOwnerBackfillResponse {

        @JsonProperty("ResponseInfo")
        private ResponseInfo responseInfo;

        @JsonProperty("dryRun")
        private Boolean dryRun;

        /** Documents read from the index. */
        @JsonProperty("processed")
        private Integer processed;

        /**
         * Documents whose owner fields Elasticsearch accepted a new value for; on a dry run, the
         * number of documents that would have been sent.
         */
        @JsonProperty("updated")
        private Integer updated;

        /** Documents already carrying the derived owner, so nothing was sent for them. */
        @JsonProperty("unchanged")
        private Integer unchanged;

        /** Documents an owner could be derived for. */
        @JsonProperty("resolved")
        private Integer resolved;

        /**
         * Documents left with a null owner: terminal states, states whose roles are outside USER_TYPE,
         * and states missing from the business service definition.
         */
        @JsonProperty("unresolved")
        private Integer unresolved;

        /** Documents whose bulk update Elasticsearch rejected. */
        @JsonProperty("failed")
        private Integer failed;

        /** Documents passed over entirely, i.e. index anomalies carrying no document id. */
        @JsonProperty("skipped")
        private Integer skipped;

        /**
         * Every {@code businessService|state} the run saw, mapped to the owner it derived (or
         * {@code NONE}), with the document count. The audit trail of what the script decided.
         */
        @JsonProperty("ownerByState")
        private Map<String, String> ownerByState;
}
