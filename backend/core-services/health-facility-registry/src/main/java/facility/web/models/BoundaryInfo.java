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
public class BoundaryInfo {
    @JsonProperty("blockCode")
    private String blockCode;

    @JsonProperty("districtCode")
    private String districtCode;

    @JsonProperty("stateCode")
    private String stateCode;

    @JsonProperty("countryCode")
    private String countryCode;

    @JsonProperty("facilityCode")
    private String facilityCode;
}
