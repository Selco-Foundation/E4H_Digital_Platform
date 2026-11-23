package facility.web.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.egov.common.contract.request.RequestInfo;
import org.springframework.validation.annotation.Validated;

/**
 * Request for {@code POST /v2/facility/_update-mapped-vendor}: writes one facility's mapped vendor
 * and nothing else.
 *
 * <p>Exists so a caller that only knows the vendor mapping - vendor-registry, when a facility
 * jurisdiction is added to or removed from a vendor - does not have to round-trip the whole
 * facility through {@code /v2/facility/update}. That round-trip had to search the facility first,
 * echo every field back, and drove the ONM-ready branching along with POC user creation and
 * password resets, none of which a vendor mapping has any business triggering.
 *
 * <p>Identify the facility by {@code facilityId} or {@code boundaryCode} - exactly one is needed,
 * and {@code facilityId} wins when both are given. A blank vendor name and username together mean
 * "unmap this facility", which is a deliberate clear rather than a missing value.
 */
@Validated
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FacilityMappedVendorUpdateRequest {

    @JsonProperty("RequestInfo")
    @NotNull
    private RequestInfo requestInfo;

    @JsonProperty("tenantId")
    @NotNull
    private String tenantId;

    /** The facility to update. Takes precedence over {@code boundaryCode} when both are set. */
    @JsonProperty("facilityId")
    private String facilityId;

    /** The facility's boundary code, for callers that hold the jurisdiction rather than the id. */
    @JsonProperty("boundaryCode")
    private String boundaryCode;

    @JsonProperty("mappedVendorName")
    private String mappedVendorName;

    @JsonProperty("mappedVendorUserName")
    private String mappedVendorUserName;
}
