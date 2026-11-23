package facility.web.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FacilityIdentifierValidationResponse {

    /** One entry per facility with at least one conflict; facilities without conflicts are left out. */
    @JsonProperty("conflicts")
    @Builder.Default
    private List<FacilityIdentifierConflict> conflicts = new ArrayList<>();
}
