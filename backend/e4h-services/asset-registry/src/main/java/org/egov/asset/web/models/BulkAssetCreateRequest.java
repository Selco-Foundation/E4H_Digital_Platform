package org.egov.asset.web.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.egov.common.contract.request.RequestInfo;
import org.springframework.validation.annotation.Validated;

import jakarta.validation.Valid;
import javax.validation.constraints.NotNull;

/**
 * BulkAssetCreateRequest
 */
@Validated
@jakarta.annotation.Generated(value = "org.egov.codegen.SpringBootCodegen", date = "2025-05-05T14:19:51.673231117+05:30[Asia/Kolkata]")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class BulkAssetCreateRequest {
    @JsonProperty("RequestInfo")
    @NotNull

    @Valid
    private RequestInfo requestInfo = null;

    @JsonProperty("assetDetails")
    @NotNull

    private Object assetDetails = null;


}
