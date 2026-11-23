package facility.web.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

/**
 * Result of the {@code POST /v2/facility/_backfill-project-name} operator run.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FacilityProjectNameBackfillResponse {

    /** Number of indexed facility documents scanned. */
    @JsonProperty("scanned")
    private int scanned;

    /** Documents whose projectName was written/updated. */
    @JsonProperty("updated")
    private int updated;

    /** Documents skipped (no project mapping found, or projectName already up to date). */
    @JsonProperty("skipped")
    private int skipped;

    /** Documents that failed to update. */
    @JsonProperty("failed")
    private int failed;

    @JsonProperty("errors")
    @Builder.Default
    private List<FacilityBoundaryBackfillErrorItem> errors = new ArrayList<>();
}
