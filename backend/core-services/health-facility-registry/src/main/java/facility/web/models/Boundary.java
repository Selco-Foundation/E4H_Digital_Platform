package facility.web.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import digit.models.coremodels.AuditDetails;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.validation.annotation.Validated;

/**
 * BoundaryCreate
 */
@Validated
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Boundary {

    @JsonProperty("id")
    private String id = null;

    @JsonProperty("tenantId")
    private String tenantId = null;

    @JsonProperty("code")
    @NotNull
    private String code = null;

    @JsonProperty("state")
    private String state = null;

    @JsonProperty("district")
    private String district = null;

    @JsonProperty("block")
    private String block = null;

    @JsonProperty("geometry")
    @Valid
    private JsonNode geometry = null;

    @JsonProperty("auditDetails")
    private AuditDetails auditDetails = null;

    @JsonProperty("additionalDetails")
    private JsonNode additionalDetails = null;

}
