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

@Validated
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FacilityKibanaReindexRequest {

    @JsonProperty("RequestInfo")
    @NotNull
    private RequestInfo requestInfo;

    /** When set, only facilities for this tenant are reindexed. */
    @JsonProperty("tenantId")
    private String tenantId;

    /** When set, only these facility IDs are reindexed (still subject to other filters). */
    @JsonProperty("facilityIds")
    private List<String> facilityIds;

    /**
     * When {@code true} (default), only {@code is_onm_ready = true} rows are reindexed —
     * the same cohort normally pushed to Kibana on create/update.
     */
    @JsonProperty("onmReadyOnly")
    @Builder.Default
    private Boolean onmReadyOnly = Boolean.TRUE;
}
