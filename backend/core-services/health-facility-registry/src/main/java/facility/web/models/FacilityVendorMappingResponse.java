package facility.web.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/** The vendor organisations mapped to a facility, sorted by name. Empty when none is. */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FacilityVendorMappingResponse {

    @JsonProperty("facilityId")
    private String facilityId;

    @JsonProperty("organisations")
    private List<VendorOrganisation> organisations;

    /** One vendor organisation, as the Facility Admin sees it. */
    public record VendorOrganisation(
            @JsonProperty("id") String id,
            @JsonProperty("code") String code,
            @JsonProperty("name") String name) {
    }
}
