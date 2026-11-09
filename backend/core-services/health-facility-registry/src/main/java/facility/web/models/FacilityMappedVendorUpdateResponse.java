package facility.web.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Result of a {@code POST /v2/facility/_update-mapped-vendor} call. */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FacilityMappedVendorUpdateResponse {

    @JsonProperty("facilityId")
    private String facilityId;

    @JsonProperty("boundaryCode")
    private String boundaryCode;

    @JsonProperty("mappedVendorName")
    private String mappedVendorName;

    @JsonProperty("mappedVendorUserName")
    private String mappedVendorUserName;

    /** True when the row and the index document were written. */
    @JsonProperty("updated")
    private boolean updated;

    /**
     * True when the facility already carried exactly this vendor and nothing was written. The
     * index document is replaced wholesale on every push, so re-pushing an unchanged mapping is
     * not free: it races the other writers of that document for no gain.
     */
    @JsonProperty("unchanged")
    private boolean unchanged;

    /** True when the facility was found but holds no Kibana document to update. */
    @JsonProperty("indexSkipped")
    private boolean indexSkipped;
}
