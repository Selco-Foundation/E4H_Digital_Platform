package facility.web.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Set;

/**
 * Filters for {@code _search}. A caller without the CRM role always gets only their own
 * requests, whatever {@link #userUuid} says.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class MobileChangeRequestSearchCriteria {

    /** Filters by tenant; also required by the API gateway, which rejects a body without one. */
    @JsonProperty("tenantId")
    private String tenantId;

    @JsonProperty("ids")
    private List<String> ids;

    @JsonProperty("userUuid")
    private String userUuid;

    @JsonProperty("facilityId")
    private String facilityId;

    @JsonProperty("status")
    private List<MobileChangeRequestStatus> status;

    @JsonProperty("limit")
    private Integer limit;

    @JsonProperty("offset")
    private Integer offset;

    /**
     * Facility boundary codes the caller's CRM jurisdiction covers; null means no limit. Set by the
     * service, never read from the request - a client must not be able to name its own scope.
     */
    @JsonIgnore
    private Set<String> boundaryCodeCeiling;
}
