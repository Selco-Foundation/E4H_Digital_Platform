package facility.web.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

/** Result of a {@code POST /v2/facility/_backfill-mapped-vendor} run. */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FacilityMappedVendorBackfillResponse {

    /** Facilities examined: the selected cohort that was still missing a mapped vendor. */
    @JsonProperty("scanned")
    private int scanned;

    /** Facilities whose row (and index document, when indexed) now carries a mapped vendor. */
    @JsonProperty("updated")
    private int updated;

    /** Facilities HRMS holds no mapping for, or that carry no boundary code. */
    @JsonProperty("skipped")
    private int skipped;

    @JsonProperty("failed")
    private int failed;

    /** True when the run resolved without writing anything. */
    @JsonProperty("dryRun")
    private boolean dryRun;

    @JsonProperty("errors")
    @Builder.Default
    private List<FacilityBoundaryBackfillErrorItem> errors = new ArrayList<>();
}
