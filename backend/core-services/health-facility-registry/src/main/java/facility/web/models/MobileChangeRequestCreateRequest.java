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
 * Body of {@code _create}. Only {@code newMobileNumber} is read from {@link #mobileChangeRequest};
 * the user, facility, tenant and current number come from the logged-in user's HRMS record.
 * {@code tenantId} must still be sent: the API gateway rejects a body without one.
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
