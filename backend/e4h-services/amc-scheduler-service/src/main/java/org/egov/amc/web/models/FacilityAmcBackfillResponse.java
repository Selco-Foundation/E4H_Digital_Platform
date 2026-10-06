package org.egov.amc.web.models;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.egov.common.contract.response.ResponseInfo;

/**
 * Outcome of a facility AMC index backfill run.
 *
 * <p>The counters are deliberately separate rather than a single "processed" total: a run where
 * every facility was scanned but nothing matched a document in the index looks identical to a
 * successful one unless {@code facilitiesNotInIndex} is reported on its own.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FacilityAmcBackfillResponse {

    private ResponseInfo responseInfo;

    /** Facilities read from the registry and attempted. */
    private Integer facilitiesScanned;

    /** Of those, the ones that had an active AMC configuration to index. */
    private Integer facilitiesWithAmc;

    /** Facilities whose index document was actually updated. */
    private Integer facilitiesIndexed;

    /**
     * Facilities the index had no document for, so the snapshot landed nowhere. Not an error - a
     * facility that is not ONM-ready is never indexed - but a large number here means the backfill
     * covered far less than {@code facilitiesScanned} suggests.
     */
    private Integer facilitiesNotInIndex;

    /** Facilities skipped because their snapshot or push threw. */
    private Integer facilitiesFailed;
}
