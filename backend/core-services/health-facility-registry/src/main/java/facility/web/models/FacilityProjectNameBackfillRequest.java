package facility.web.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.egov.common.contract.request.RequestInfo;
import org.springframework.validation.annotation.Validated;

/**
 * Request for the operator endpoint {@code POST /v2/facility/_backfill-project-name}, which
 * scans the health facility index and backfills {@code projectName} on each document from the
 * project service.
 */
@Validated
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FacilityProjectNameBackfillRequest {

    @JsonProperty("RequestInfo")
    @NotNull
    private RequestInfo requestInfo;

    /** When set, only facilities for this tenant are scanned/backfilled. */
    @JsonProperty("tenantId")
    private String tenantId;

    /** Elasticsearch scan page size. Defaults to 500 when null or non-positive. */
    @JsonProperty("batchSize")
    private Integer batchSize;
}
