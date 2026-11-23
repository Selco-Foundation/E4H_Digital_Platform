package facility.web.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.egov.common.contract.request.RequestInfo;
import org.springframework.validation.annotation.Validated;

import java.util.List;

/**
 * Request for the operator endpoint {@code POST /v2/facility/_backfill-mapped-vendor}, which
 * settles {@code mappedVendorName} / {@code mappedVendorUserName} on facilities that were created
 * without one, reading the mapping back out of HRMS by boundary code.
 */
@Validated
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FacilityMappedVendorBackfillRequest {

    @JsonProperty("RequestInfo")
    @NotNull
    private RequestInfo requestInfo;

    /** When set, only facilities for this tenant are considered. */
    @JsonProperty("tenantId")
    private String tenantId;

    /** When set, only these facility IDs are considered (still subject to the other filters). */
    @JsonProperty("facilityIds")
    private List<String> facilityIds;

    /**
     * When {@code true} (default), only {@code is_onm_ready = true} rows are considered - the same
     * cohort that is normally indexed. Non ONM-ready facilities have no Kibana document to keep in
     * step, so their rows are left alone unless this is set to {@code false}.
     */
    @JsonProperty("onmReadyOnly")
    @Builder.Default
    private Boolean onmReadyOnly = Boolean.TRUE;

    /**
     * When {@code true}, resolve and report what would change without writing anything. Lets an
     * operator size the affected set before touching rows.
     */
    @JsonProperty("dryRun")
    @Builder.Default
    private Boolean dryRun = Boolean.FALSE;
}
