package facility.web.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FacilityBlockUpdate {

    @NotBlank
    @Schema(name = "tenant_id", requiredMode = Schema.RequiredMode.REQUIRED)
    @JsonProperty("tenant_id")
    private String tenantId;

    @NotBlank
    @Schema(name = "facility_id", requiredMode = Schema.RequiredMode.REQUIRED)
    @JsonProperty("facility_id")
    private String facilityId;

    @NotBlank
    @Schema(name = "new_block_boundary_code", requiredMode = Schema.RequiredMode.REQUIRED)
    @JsonProperty("new_block_boundary_code")
    private String newBlockBoundaryCode;
}
