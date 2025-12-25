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
public class EncryptObject {

    @JsonProperty("mobileNumber")
    private String mobileNumber = null;

}
