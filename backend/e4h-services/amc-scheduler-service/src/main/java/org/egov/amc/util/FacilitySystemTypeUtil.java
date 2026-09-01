package org.egov.amc.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.egov.amc.config.AMCServiceConfiguration;
import org.egov.amc.service.ServiceRequestRepository;
import org.egov.amc.web.models.FacilitySystemType;
import org.egov.amc.web.models.FacilitySystemTypeSearchCriteria;
import org.egov.amc.web.models.FacilitySystemTypeSearchRequest;
import org.egov.amc.web.models.FacilitySystemTypeSearchResponse;
import org.egov.common.contract.request.RequestInfo;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Reads a facility's system type from field-planner, which owns it: the value is captured on the
 * installation plan a facility is linked to, not on the facility or its AMC.
 *
 * <p>Sits in amc-scheduler-service because the AMC index sync is what publishes it to the health
 * facility index - the AMC snapshot is the only index-only write path a facility document has, so the
 * system type rides along with it rather than getting a second push mechanism of its own.
 */
@Slf4j
@Component
public class FacilitySystemTypeUtil {

    /**
     * Facility ids per field-planner search. Matches the AMC backfill's own facility page size, so a
     * backfill batch resolves in a single call.
     */
    private static final int FACILITY_ID_BATCH_SIZE = 100;

    private final AMCServiceConfiguration amcServiceConfiguration;
    private final ServiceRequestRepository requestRepository;
    private final ObjectMapper mapper;

    public FacilitySystemTypeUtil(AMCServiceConfiguration amcServiceConfiguration,
                                  ServiceRequestRepository requestRepository,
                                  @Qualifier("objectMapper") ObjectMapper mapper) {
        this.amcServiceConfiguration = amcServiceConfiguration;
        this.requestRepository = requestRepository;
        this.mapper = mapper;
    }

    /**
     * Outcome of a system type lookup.
     *
     * <p>Deliberately not an {@code Optional<String>}: a facility with nothing recorded and a
     * field-planner that never answered both produce "no value", but the caller must treat them
     * differently - the first is publishable, the second must leave the index alone. Optional collapses
     * the two ({@code Optional.map} on a missing key yields empty either way), which is exactly the
     * confusion this type exists to prevent.
     *
     * @param resolved   whether field-planner answered at all
     * @param systemType the value, or null when resolved and nothing is recorded for the facility
     */
    public record Lookup(boolean resolved, String systemType) {

        /** field-planner could not be asked; the caller must not publish an absence. */
        public static Lookup failed() {
            return new Lookup(false, null);
        }

        /** field-planner answered: {@code systemType} may be null, meaning nothing is recorded. */
        public static Lookup of(String systemType) {
            return new Lookup(true, systemType);
        }
    }

    /** The system type of one facility. */
    public Lookup resolveSystemType(RequestInfo requestInfo, String facilityId, String tenantId) {
        if (facilityId == null || facilityId.isBlank()) {
            return Lookup.failed();
        }
        return getSystemTypeByFacilityId(requestInfo, List.of(facilityId), tenantId)
                .map(byFacilityId -> Lookup.of(byFacilityId.get(facilityId)))
                .orElseGet(Lookup::failed);
    }

    /**
     * The system type of each facility in {@code facilityIds}, in batched field-planner searches.
     *
     * <p>The return type deliberately separates two outcomes the caller must not conflate:
     *
     * <ul>
     *   <li><b>Empty Optional</b> - field-planner was unreachable, errored, or is not configured. The
     *       caller has learned nothing, and must leave whatever the index already holds alone. Writing
     *       "Not Applicable" here would let a transient field-planner outage erase a real system type
     *       from every facility the sync happens to touch during it.</li>
     *   <li><b>Present map</b> - field-planner answered, and the map is authoritative. A facility
     *       absent from it genuinely has no system type recorded, so the caller may safely publish
     *       that absence.</li>
     * </ul>
     *
     * <p>All-or-nothing across batches for the same reason: a partial map would be indistinguishable
     * from an authoritative one, so a facility whose batch failed would read as "nothing recorded".
     */
    public Optional<Map<String, String>> getSystemTypeByFacilityId(RequestInfo requestInfo,
                                                                   List<String> facilityIds,
                                                                   String tenantId) {
        String host = amcServiceConfiguration.getFieldPlannerHost();
        String path = amcServiceConfiguration.getFieldPlannerFacilitySystemTypeSearchUrl();
        if (host == null || host.isBlank() || path == null || path.isBlank()) {
            log.warn("field-planner host/path is not configured; indexing without a facility system type.");
            return Optional.empty();
        }
        List<String> distinctIds = facilityIds == null ? List.of() : facilityIds.stream()
                .filter(id -> id != null && !id.isBlank())
                .distinct()
                .toList();
        if (distinctIds.isEmpty()) {
            // Nothing to ask about is authoritative, not a failure: an empty map correctly says "none
            // of the zero facilities requested has a system type".
            return Optional.of(Map.of());
        }

        Map<String, String> systemTypeByFacilityId = new LinkedHashMap<>();
        for (int from = 0; from < distinctIds.size(); from += FACILITY_ID_BATCH_SIZE) {
            List<String> batch = distinctIds.subList(from,
                    Math.min(from + FACILITY_ID_BATCH_SIZE, distinctIds.size()));
            FacilitySystemTypeSearchRequest body = FacilitySystemTypeSearchRequest.builder()
                    .requestInfo(requestInfo)
                    .criteria(FacilitySystemTypeSearchCriteria.builder()
                            .facilityId(new ArrayList<>(batch))
                            .build())
                    .build();
            // tenantId is a query param on every field-planner search (URLParams), not part of the body.
            // limit/offset are sent only because URLParams declares them @NotNull and the controller
            // validates it - the endpoint itself ignores them and walks every matching row, so their
            // values do not bound the result. limit must also stay within URLParams' @Max(1000).
            String url = host + path + "?tenantId=" + tenantId
                    + "&limit=" + FACILITY_ID_BATCH_SIZE
                    + "&offset=0";
            try {
                Object response = requestRepository.fetchResult(new StringBuilder(url), body);
                FacilitySystemTypeSearchResponse parsed =
                        mapper.convertValue(response, FacilitySystemTypeSearchResponse.class);
                if (parsed == null || parsed.getFacilitySystemTypes() == null) {
                    continue;
                }
                for (FacilitySystemType facilitySystemType : parsed.getFacilitySystemTypes()) {
                    String facilityId = facilitySystemType.getFacilityId();
                    String systemType = facilitySystemType.getSystemType();
                    if (facilityId != null && systemType != null && !systemType.isBlank()) {
                        systemTypeByFacilityId.put(facilityId, systemType);
                    }
                }
            } catch (Exception e) {
                log.error("field-planner system type lookup failed for {} facility/facilities; leaving the "
                        + "indexed system type untouched for this batch.", batch.size(), e);
                return Optional.empty();
            }
        }
        return Optional.of(systemTypeByFacilityId);
    }
}
