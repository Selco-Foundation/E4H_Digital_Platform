package facility.web.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.egov.common.contract.request.RequestInfo;
import org.springframework.validation.annotation.Validated;

import java.util.Map;

/**
 * Request for {@code POST /v2/facility/_update-amc-index}, called by amc-scheduler-service to write
 * a facility's AMC snapshot (installation date, applicability, frequency, valid-till, mapped vendor,
 * and the due/visit date cycles) onto the health facility index.
 *
 * <p>Index-only by design: AMC data is owned by amc-scheduler-service and is deliberately not
 * persisted in the facility table, so this endpoint bypasses both the facility {@code _update} API
 * and the indexer Kafka topic and updates Elasticsearch directly.
 */
@Validated
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FacilityAmcIndexUpdateRequest {

    @JsonProperty("RequestInfo")
    @NotNull
    private RequestInfo requestInfo;

    @JsonProperty("facilityId")
    @NotNull
    private String facilityId;

    @JsonProperty("tenantId")
    private String tenantId;

    /**
     * The AMC fields to write, keyed by index field name. Null values are written as null so a
     * cleared AMC (or a shortened visit cadence) clears the previously indexed value.
     */
    @JsonProperty("amcFields")
    @NotNull
    private Map<String, Object> amcFields;
}
