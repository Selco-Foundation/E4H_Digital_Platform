package facility.web.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.egov.common.contract.request.RequestInfo;
import org.springframework.validation.annotation.Validated;

import java.util.ArrayList;
import java.util.List;

/**
 * Checks HFR ID, NIN ID and POC username of facilities that are about to be created, without
 * creating them. Runs the same uniqueness rules as facility create.
 */
@Validated
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FacilityIdentifierValidationRequest {

    @JsonProperty("RequestInfo")
    @NotNull
    private RequestInfo requestInfo;

    @JsonProperty("tenantId")
    @NotBlank
    private String tenantId;

    @JsonProperty("facilities")
    @Valid
    @Builder.Default
    private List<FacilityIdentifierCheck> facilities = new ArrayList<>();
}
