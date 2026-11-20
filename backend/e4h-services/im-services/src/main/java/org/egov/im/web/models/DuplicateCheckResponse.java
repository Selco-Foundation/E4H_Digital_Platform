package org.egov.im.web.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;
import org.egov.common.contract.response.ResponseInfo;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class DuplicateCheckResponse {

        @JsonProperty("ResponseInfo")
        private ResponseInfo responseInfo;

        // false for a support request component: no duplicate check applies
        @JsonProperty("duplicateCheckApplicable")
        private boolean duplicateCheckApplicable;

        // Open tickets of the facility for the component, newest first
        @JsonProperty("duplicates")
        private List<DuplicateTicket> duplicates;
}
