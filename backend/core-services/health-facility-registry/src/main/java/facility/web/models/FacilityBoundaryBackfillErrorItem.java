package facility.web.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FacilityBoundaryBackfillErrorItem {

    @JsonProperty("facilityId")
    private String facilityId;

    @JsonProperty("tenantId")
    private String tenantId;

    @JsonProperty("boundaryCode")
    private String boundaryCode;

    @JsonProperty("message")
    private String message;
}
