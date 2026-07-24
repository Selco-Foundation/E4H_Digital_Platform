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
public class FacilityKibanaReindexResponse {

    @JsonProperty("scanned")
    private int scanned;

    @JsonProperty("reindexed")
    private int reindexed;

    @JsonProperty("skipped")
    private int skipped;

    @JsonProperty("failed")
    private int failed;

    @JsonProperty("errors")
    @Builder.Default
    private List<FacilityBoundaryBackfillErrorItem> errors = new ArrayList<>();
}
