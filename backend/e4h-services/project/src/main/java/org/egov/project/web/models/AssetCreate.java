package org.egov.project.web.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.validation.annotation.Validated;

/**
 * AssetCreate
 */
@Validated
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AssetCreate {
    @JsonProperty("Asset")
    @NotNull
    @Valid
    private Asset asset = null;
}
