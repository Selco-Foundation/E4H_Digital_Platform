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
public class FacilityIdentifierCheck {

    @JsonProperty("facilityName")
    private String facilityName;

    @JsonProperty("hfrId")
    private String hfrId;

    @JsonProperty("ninId")
    private String ninId;

    @JsonProperty("facilityPocUsername")
    private String facilityPocUsername;
}
