package facility.web.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.egov.common.contract.request.RequestInfo;

/**
 * Body of {@code _create}. Only {@code tenantId} and {@code newMobileNumber} are read from
 * {@link #mobileChangeRequest}; the user, facility and current number come from the logged-in
 * user's HRMS record.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class MobileChangeRequestCreateRequest {

    @JsonProperty("RequestInfo")
    @NotNull
    @Valid
    private RequestInfo requestInfo;

    @JsonProperty("MobileChangeRequest")
    @NotNull
    @Valid
    private MobileChangeRequest mobileChangeRequest;
}
