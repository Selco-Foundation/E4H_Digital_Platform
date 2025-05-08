package org.egov.asset.web.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.validation.annotation.Validated;

import jakarta.validation.Valid;

/**
 * AssetWorkflowUpdate
 */
@Validated
@jakarta.annotation.Generated(value = "org.egov.codegen.SpringBootCodegen", date = "2025-05-05T14:19:51.673231117+05:30[Asia/Kolkata]")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AssetWorkflowUpdate {
    @JsonProperty("tenant_id")

    private String tenantId = null;

    @JsonProperty("assetID")

    private String assetID = null;

    @JsonProperty("workflow")

    @Valid
    private Workflow workflow = null;


}
