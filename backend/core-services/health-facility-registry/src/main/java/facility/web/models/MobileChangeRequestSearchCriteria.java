package facility.web.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Filters for {@code _search}. A caller without the CRM role always gets only their own
 * requests, whatever {@link #userUuid} says.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class MobileChangeRequestSearchCriteria {

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
}
