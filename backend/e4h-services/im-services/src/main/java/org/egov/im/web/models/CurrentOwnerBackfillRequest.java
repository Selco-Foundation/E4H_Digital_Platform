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
 * Request object for the one-off script that derives currentOwner for already indexed incidents
 */
@ApiModel(description = "Request object for the one-off script that derives currentOwner for already indexed incidents")
@Validated

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CurrentOwnerBackfillRequest {

        @NotNull
        @JsonProperty("RequestInfo")
        private RequestInfo requestInfo = null;

        /**
         * Tenant the workflow and MDMS masters are read for. The index itself is walked in full: it
         * holds one document per incident across the state, and the owner derivation is the same for
         * all of them.
         */
        @NotNull
        @SafeHtml
        @JsonProperty("tenantId")
        private String tenantId;

        /** Documents per Elasticsearch page and per bulk update. Defaults to 500. */
        @Min(1)
        @JsonProperty("batchSize")
        private Integer batchSize;

        /**
         * Stop after this many documents. Intended for a rehearsal run on a slice of the index before
         * committing to the whole of it; null walks the index to the end.
         */
        @Min(1)
        @JsonProperty("maxDocuments")
        private Integer maxDocuments;

        /**
         * When true, resolves and reports the owner of every document without writing anything, so the
         * state -> owner mapping can be reviewed before the index is touched.
         */
        @JsonProperty("dryRun")
        private Boolean dryRun;
}
