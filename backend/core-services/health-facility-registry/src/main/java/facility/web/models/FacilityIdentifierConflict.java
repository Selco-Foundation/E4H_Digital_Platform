package facility.web.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FacilityIdentifierConflict {

    /** Position of the facility in the request's facilities list. */
    @JsonProperty("index")
    private int index;

    @JsonProperty("errors")
    private List<String> errors;
}
