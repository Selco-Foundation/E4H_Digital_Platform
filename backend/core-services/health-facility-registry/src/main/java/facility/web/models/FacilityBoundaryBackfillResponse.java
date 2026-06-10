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
public class FacilityBoundaryBackfillResponse {

    @JsonProperty("scanned")
    private int scanned;

    @JsonProperty("missing")
    private int missing;

    @JsonProperty("created")
    private int created;

    @JsonProperty("skippedInvalid")
    private int skippedInvalid;

    @JsonProperty("failed")
    private int failed;

    @JsonProperty("errors")
    @Builder.Default
    private List<FacilityBoundaryBackfillErrorItem> errors = new ArrayList<>();
}
