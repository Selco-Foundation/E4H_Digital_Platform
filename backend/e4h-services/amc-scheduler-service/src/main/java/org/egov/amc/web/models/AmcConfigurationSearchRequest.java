package org.egov.amc.web.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.egov.common.contract.request.RequestInfo;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AmcConfigurationSearchRequest {
    @JsonProperty("RequestInfo")
    private RequestInfo RequestInfo;
    @JsonProperty("searchCriteria")
    private @NotNull @Valid AmcConfigurationSearchCriteria searchCriteria;
}
