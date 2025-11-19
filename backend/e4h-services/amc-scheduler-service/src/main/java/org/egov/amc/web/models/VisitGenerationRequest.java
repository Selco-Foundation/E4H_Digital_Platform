package org.egov.amc.web.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;
import org.egov.common.contract.request.RequestInfo;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class VisitGenerationRequest {

    @JsonProperty("RequestInfo")
    private RequestInfo requestInfo;

    @JsonProperty("generationStartDate")
    private String generationStartDate; // format: date (yyyy-MM-dd)

    @JsonProperty("generationEndDate")
    private String generationEndDate; // format: date (yyyy-MM-dd)

    @JsonProperty("regenerateExisting")
    private Boolean regenerateExisting = false;
}
