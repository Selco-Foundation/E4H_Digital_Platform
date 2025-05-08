package org.egov.asset.web.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.validation.annotation.Validated;

import java.time.LocalDate;

/**
 * AssetUpdate
 */
@Validated
@jakarta.annotation.Generated(value = "org.egov.codegen.SpringBootCodegen", date = "2025-05-05T14:19:51.67323111705:30[Asia/Kolkata]")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AssetUpdate {
    
    @JsonProperty("tenant_id")
    @NotNull
    private String tenantId = null;

    @JsonProperty("assetID")
    @NotNull
    private String assetID = null;

    @JsonProperty("warrantyStartDate")
    private LocalDate warrantyStartDate = null;

    @JsonProperty("warrantyDuration")
    private Integer warrantyDuration = null;

    @JsonProperty("warrantyEndDate")
    private LocalDate warrantyEndDate = null;


}
