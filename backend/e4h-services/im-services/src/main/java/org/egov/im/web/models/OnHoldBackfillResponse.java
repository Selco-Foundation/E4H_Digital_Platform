package org.egov.im.web.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;
import lombok.*;
import org.egov.common.contract.response.ResponseInfo;
import org.springframework.validation.annotation.Validated;

import java.util.List;
import java.util.Map;

/**
 * Outcome of an ON_HOLD systemFunctional backfill run
 */
@ApiModel(description = "Outcome of an ON_HOLD systemFunctional backfill run")
@Validated

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class OnHoldBackfillResponse {

        @JsonProperty("ResponseInfo")
        private ResponseInfo responseInfo;

        @JsonProperty("dryRun")
        private Boolean dryRun;

        /** Tickets currently in OUT_OF_WARRANTY_ON_HOLD. */
        @JsonProperty("onHoldTickets")
        private Integer onHoldTickets;

        /**
         * systemFunctional the on-hold tickets held before the run, with counts ({@code NULL} for no
         * value). Everything except ON_HOLD is what gets saved as systemFunctionalBeforeOnHold.
         */
        @JsonProperty("previousSystemFunctional")
        private Map<String, Integer> previousSystemFunctional;

        /** Rows set to ON_HOLD in the database; on a dry run, the number that would be. */
        @JsonProperty("updated")
        private Integer updated;

        /** Distinct facilities (boundary codes) whose index document is recomputed. */
        @JsonProperty("facilities")
        private Integer facilities;

        /** Tickets re-published to the indexer, which also recomputes their facility's status. */
        @JsonProperty("reindexed")
        private Integer reindexed;

        /** Tickets whose re-publish failed; safe to re-run the backfill for them. */
        @JsonProperty("failedIncidentIds")
        private List<String> failedIncidentIds;
}
