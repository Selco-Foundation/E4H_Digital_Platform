package org.egov.asset.web.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.validation.annotation.Validated;

import javax.validation.constraints.NotNull;

/**
 * Criteria to search for assets. tenant_id is mandatory. Send one of the rest
 */
@Schema(description = "Criteria to search for assets. tenant_id is mandatory. Send one of the rest")
@Validated
@jakarta.annotation.Generated(value = "org.egov.codegen.SpringBootCodegen", date = "2025-05-05T14:19:51.673231117+05:30[Asia/Kolkata]")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AssetSearchCriteria {
    @JsonProperty("tenant_id")
    @NotNull

    private Object tenantId = null;

    @JsonProperty("assetID")

    private Object assetID = null;

    @JsonProperty("facilityID")

    private Object facilityID = null;

    @JsonProperty("serialNumber")

    private Object serialNumber = null;

    @JsonProperty("modelNumber")

    private Object modelNumber = null;

    @JsonProperty("brandID")

    private Object brandID = null;

    @JsonProperty("wfStatus")

    private Object wfStatus = null;


}
