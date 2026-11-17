package facility.web.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Result of a {@code POST /v2/facility/_update-amc-index} call.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FacilityAmcIndexUpdateResponse {

    @JsonProperty("facilityId")
    private String facilityId;

    /**
     * Indexed documents updated. {@code 0} means the facility is not in the index yet (e.g. not
     * ONM-ready), which is not an error.
     */
    @JsonProperty("updated")
    private int updated;
}
