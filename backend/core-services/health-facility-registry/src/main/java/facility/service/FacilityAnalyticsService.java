package facility.service;

import facility.config.Configuration;
import facility.kafka.Producer;
import facility.util.MdmsUtil;
import facility.web.models.Facility;
import facility.web.models.UserAnalyticsEvent;
import lombok.extern.slf4j.Slf4j;
import net.minidev.json.JSONArray;
import org.egov.common.contract.request.RequestInfo;
import org.egov.common.contract.request.Role;
import org.egov.common.contract.request.User;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import static facility.config.ServiceConstants.ANALYTICS_APPLICATION;
import static facility.config.ServiceConstants.ANALYTICS_ENTITY_TYPE_FACILITY;
import static facility.config.ServiceConstants.ANALYTICS_EVENT_FACILITY_CREATE;
import static facility.config.ServiceConstants.ANALYTICS_EVENT_FACILITY_UPDATE;
import static facility.config.ServiceConstants.MDMS_MASTER_USER_TYPE;
import static facility.config.ServiceConstants.USER_ANALYTICS_MODULE;

/**
 * Builds and publishes {@link UserAnalyticsEvent}s to the shared {@code user-analytics-event}
 * topic on facility create and update, so they land in the same user-analytics-report index as
 * the SEM events emitted by im-services.
 * <p>
 * event_type is fixed per operation (FACILITY_CREATE / FACILITY_UPDATE) — unlike SEM there is no
 * workflow action to map. primary_role / user_category / system_role are resolved from the
 * {@code USER_ANALYTICS.USER_TYPE} master by best-match against the acting user's roles: active
 * records are sorted by descending system_roles count (most specific first) and the first record
 * whose system_roles the user fully holds wins.
 * <p>
 * {@code state} is localized as {@code Boundary_<stateCode>} in {@code rainmaker-in}, the same
 * key SemAnalyticsService uses, so both producers write identical state strings into the shared
 * index. The State code is parsed straight out of the facility's boundary code rather than
 * fetched from the boundary service — see {@link #extractStateBoundaryCode}.
 * <p>
 * Every entry point is best-effort — any failure is logged and swallowed so analytics can never
 * break a facility create/update.
 */
@Service
@Slf4j
public class FacilityAnalyticsService {

    private final Configuration configs;
    private final MdmsUtil mdmsUtil;
    private final Producer producer;
    private final FacilityKibanaMapper facilityKibanaMapper;

    public FacilityAnalyticsService(Configuration configs, MdmsUtil mdmsUtil, Producer producer,
                                    FacilityKibanaMapper facilityKibanaMapper) {
        this.configs = configs;
        this.mdmsUtil = mdmsUtil;
        this.producer = producer;
        this.facilityKibanaMapper = facilityKibanaMapper;
    }

    /**
     * Publishes one FACILITY_CREATE event per created facility. MDMS is hit once per tenant and
     * the boundary/localization lookups are memoized per block, not repeated per facility.
     */
    public void publishCreateEvents(RequestInfo requestInfo, List<Facility> facilities) {
        if (facilities == null || facilities.isEmpty()) {
            return;
        }
        Map<String, List<Facility>> byTenant = facilities.stream()
                .filter(facility -> facility != null && facility.getTenantId() != null)
                .collect(Collectors.groupingBy(Facility::getTenantId));

        byTenant.forEach((tenantId, tenantFacilities) ->
                publishEvents(requestInfo, tenantId, ANALYTICS_EVENT_FACILITY_CREATE, tenantFacilities));
    }

    /** Publishes a single FACILITY_UPDATE event for the updated facility. */
    public void publishUpdateEvent(RequestInfo requestInfo, Facility facility) {
        if (facility == null || facility.getTenantId() == null || facility.getFacilityId() == null) {
            return;
        }
        publishEvents(requestInfo, facility.getTenantId(), ANALYTICS_EVENT_FACILITY_UPDATE, List.of(facility));
    }

    private void publishEvents(RequestInfo requestInfo, String tenantId, String eventType, List<Facility> facilities) {
        try {
            User user = (requestInfo != null) ? requestInfo.getUserInfo() : null;
            UserType userType = resolveUserType(requestInfo, tenantId, extractUserRoleCodes(user));
            String eventTime = Instant.now().toString();

            // One localization call per distinct state, not per facility.
            Map<String, String> stateNameByCode = new HashMap<>();

            for (Facility facility : facilities) {
                if (facility.getFacilityId() == null) {
                    continue;
                }
                String stateBoundaryCode = extractStateBoundaryCode(facility.getBoundaryCode());
                String state = null;
                if (stateBoundaryCode == null) {
                    log.info("Facility analytics: no state segment in boundaryCode={} for facilityId={}, "
                            + "state will be null", facility.getBoundaryCode(), facility.getFacilityId());
                } else {
                    // Not computeIfAbsent: a failed localization is null, and we want to cache that
                    // too rather than retry the lookup for every facility in the state.
                    if (!stateNameByCode.containsKey(stateBoundaryCode)) {
                        stateNameByCode.put(stateBoundaryCode,
                                facilityKibanaMapper.localizeBoundaryCode(stateBoundaryCode, requestInfo));
                    }
                    state = stateNameByCode.get(stateBoundaryCode);
                }

                UserAnalyticsEvent event = UserAnalyticsEvent.builder()
                        .eventId(UUID.randomUUID().toString())
                        .eventType(eventType)
                        .eventTime(eventTime)
                        .application(ANALYTICS_APPLICATION)
                        .user(user)
                        .systemRole(userType.systemRole)
                        .primaryRole(userType.primaryRole)
                        .userCategory(userType.userCategory)
                        .state(state)
                        .module(null)
                        .entityId(facility.getFacilityId())
                        .entityType(ANALYTICS_ENTITY_TYPE_FACILITY)
                        .build();
                producer.push(configs.getUserAnalyticsTopic(), event);
            }
            log.info("Facility analytics: published {} {} event(s) for tenant {}",
                    facilities.size(), eventType, tenantId);
        } catch (Exception e) {
            log.error("Facility analytics: failed to publish {} event(s) for tenant {}", eventType, tenantId, e);
        }
    }

    /**
     * Derives the State-level boundary code from a facility boundary code, no boundary-service
     * call needed. Facility boundary codes are hierarchy paths such as
     * {@code India_ArunachalPradesh_PapumPare_Doimukh_<facilityId>}, so the State code is the
     * {@code India_<State>} prefix — the same derivation
     * {@code ProjectNameGenerationService#extractStateBoundaryCodeFromBoundary} uses.
     * Returns null when the code carries no usable state segment.
     */
    private String extractStateBoundaryCode(String boundaryCode) {
        if (boundaryCode == null || boundaryCode.isBlank()) {
            return null;
        }
        String[] parts = boundaryCode.split("_");
        String countryPart = null;
        String statePart;
        if (parts.length >= 2 && "India".equalsIgnoreCase(parts[0])) {
            countryPart = parts[0];
            statePart = parts[1];
        } else {
            statePart = parts[0];
        }
        // Placeholder states seen in imported data — treat as absent rather than localizing them.
        if (statePart == null || statePart.isBlank()
                || statePart.equalsIgnoreCase("nan") || statePart.equalsIgnoreCase("XYZ")) {
            return null;
        }
        return (countryPart == null) ? statePart.trim() : countryPart.trim() + "_" + statePart.trim();
    }

    /**
     * Best-match lookup against USER_ANALYTICS.USER_TYPE: shortlist active records whose
     * system_roles the user fully holds, most specific (largest system_roles) first.
     */
    private UserType resolveUserType(RequestInfo requestInfo, String tenantId, Set<String> userRoleCodes) {
        UserType result = new UserType();
        if (userRoleCodes.isEmpty()) {
            return result;
        }
        try {
            Map<String, Map<String, JSONArray>> mdmsData =
                    mdmsUtil.fetchMdmsData(requestInfo, tenantId, USER_ANALYTICS_MODULE, List.of(MDMS_MASTER_USER_TYPE));
            List<Map<String, Object>> userTypeRecords = readRecords(mdmsData);

            List<Map<String, Object>> candidates = userTypeRecords.stream()
                    .filter(this::isActive)
                    .filter(record -> !asStringList(record.get("system_roles")).isEmpty())
                    .filter(record -> userRoleCodes.containsAll(asStringList(record.get("system_roles"))))
                    .sorted(Comparator.comparingInt(
                            (Map<String, Object> record) -> asStringList(record.get("system_roles")).size()).reversed())
                    .toList();

            if (!candidates.isEmpty()) {
                Map<String, Object> match = candidates.get(0);
                List<String> systemRoles = asStringList(match.get("system_roles"));
                // system_role: the matched record's role the user actually holds, in the user's role order.
                result.systemRole = userRoleCodes.stream().filter(systemRoles::contains).findFirst().orElse(null);
                result.primaryRole = asString(match.get("program_role"));
                result.userCategory = asString(match.get("user_category"));
            } else {
                log.info("Facility analytics: no USER_TYPE match for roles {} in tenant {}", userRoleCodes, tenantId);
            }
        } catch (Exception e) {
            log.warn("Facility analytics: failed to resolve USER_TYPE for tenant {}: {}", tenantId, e.getMessage());
        }
        return result;
    }

    private Set<String> extractUserRoleCodes(User user) {
        if (user == null || user.getRoles() == null || user.getRoles().isEmpty()) {
            return Collections.emptySet();
        }
        return user.getRoles().stream()
                .map(Role::getCode)
                .filter(Objects::nonNull)
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    @SuppressWarnings("unchecked")
    private List<Map<String, Object>> readRecords(Map<String, Map<String, JSONArray>> mdmsData) {
        if (mdmsData == null) {
            return Collections.emptyList();
        }
        Map<String, JSONArray> module = mdmsData.get(USER_ANALYTICS_MODULE);
        if (module == null) {
            return Collections.emptyList();
        }
        JSONArray master = module.get(MDMS_MASTER_USER_TYPE);
        if (master == null) {
            return Collections.emptyList();
        }
        List<Map<String, Object>> records = new ArrayList<>();
        for (Object entry : master) {
            if (entry instanceof Map) {
                records.add((Map<String, Object>) entry);
            }
        }
        return records;
    }

    @SuppressWarnings("unchecked")
    private List<String> asStringList(Object value) {
        if (!(value instanceof List)) {
            return Collections.emptyList();
        }
        return ((List<Object>) value).stream().filter(Objects::nonNull).map(Object::toString).toList();
    }

    private String asString(Object value) {
        return (value != null) ? value.toString() : null;
    }

    private boolean isActive(Map<String, Object> record) {
        Object active = record.get("active");
        return active == null || Boolean.parseBoolean(active.toString());
    }

    /** The three role-derived fields resolved together from a single USER_TYPE record. */
    private static class UserType {
        private String systemRole;
        private String primaryRole;
        private String userCategory;
    }
}
