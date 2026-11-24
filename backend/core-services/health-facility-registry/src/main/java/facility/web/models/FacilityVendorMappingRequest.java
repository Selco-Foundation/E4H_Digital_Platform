package facility.web.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.egov.common.contract.request.RequestInfo;

import java.util.List;

/**
 * Read or set the vendor organisations mapped to one facility.
 *
 * <p>For {@code _search}, {@code organisationCodes} is ignored. For {@code _update} it is the
 * <em>full desired list</em>: organisations not in it are unmapped, an empty list unmaps all.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FacilityVendorMappingRequest {

    @JsonProperty("RequestInfo")
    private RequestInfo requestInfo;

    @NotBlank
    @JsonProperty("tenantId")
    private String tenantId;

    @NotBlank
    @JsonProperty("facilityId")
    private String facilityId;

    @JsonProperty("organisationCodes")
    private List<String> organisationCodes;
}
