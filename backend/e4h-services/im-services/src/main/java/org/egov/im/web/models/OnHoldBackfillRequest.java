package org.egov.im.web.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.egov.common.contract.request.RequestInfo;
import org.hibernate.validator.constraints.SafeHtml;
import org.springframework.validation.annotation.Validated;

/**
 * Request object for the one-off script that sets systemFunctional=ON_HOLD on tickets already on hold
 */
@ApiModel(description = "Request object for the one-off script that sets systemFunctional=ON_HOLD on tickets already on hold")
@Validated

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class OnHoldBackfillRequest {

        @NotNull
        @JsonProperty("RequestInfo")
        private RequestInfo requestInfo = null;

        /** State tenant; tickets of this tenant and every city tenant under it are processed. */
        @NotNull
        @SafeHtml
        @JsonProperty("tenantId")
        private String tenantId;

        /**
         * When true, reports the on-hold tickets and their current systemFunctional without writing to
         * the database or re-publishing anything.
         */
        @JsonProperty("dryRun")
        private Boolean dryRun;
}
