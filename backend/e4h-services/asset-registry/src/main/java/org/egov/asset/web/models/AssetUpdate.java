package org.egov.asset.web.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.validation.annotation.Validated;

/**
 * AssetUpdate
 */
@Validated
@jakarta.annotation.Generated(value = "org.egov.codegen.SpringBootCodegen", date = "2025-05-05T14:19:51.673231117+05:30[Asia/Kolkata]")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AssetUpdate {
    @JsonProperty("tenant_id")

    private Object tenantId = null;

    @JsonProperty("assetID")

    private Object assetID = null;

    @JsonProperty("warrantyStartDate")

    private Object warrantyStartDate = null;

    @JsonProperty("warrantyDuration")

    private Object warrantyDuration = null;

    @JsonProperty("warrantyEndDate")

    private Object warrantyEndDate = null;


}
