package org.egov.activity.web.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.egov.common.contract.request.RequestInfo;

import javax.validation.constraints.NotNull;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AssetSearchRequest {
    @JsonProperty("RequestInfo")
    @NotNull

    @Valid
    private RequestInfo requestInfo = null;

    @JsonProperty("criteria")
    @NotNull
    private AssetSearchCriteria criteria = null;


}
