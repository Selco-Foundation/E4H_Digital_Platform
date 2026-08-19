package digit.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import digit.config.ApplicationProperties;
import digit.kafka.Producer;
import digit.util.MDMSUtils;
import digit.web.models.Boundary;
import digit.web.models.BoundaryRequest;
import digit.web.models.UserAnalyticsEvent;
import lombok.extern.slf4j.Slf4j;
import org.egov.common.contract.request.RequestInfo;
import org.egov.common.contract.request.Role;
import org.egov.common.contract.request.User;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.time.Instant;
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

import static digit.constants.BoundaryConstants.ANALYTICS_APPLICATION;
import static digit.constants.BoundaryConstants.ANALYTICS_ENTITY_TYPE_BOUNDARY;
import static digit.constants.BoundaryConstants.ANALYTICS_EVENT_BOUNDARY_CREATE;
import static digit.constants.BoundaryConstants.MDMS_MASTER_USER_TYPE;
import static digit.constants.BoundaryConstants.USER_ANALYTICS_MODULE;

/**
 * Builds and publishes {@link UserAnalyticsEvent}s to the shared {@code user-analytics-event}
 * topic on boundary create, so they land in the same user-analytics-report index as the SEM events
 * emitted by im-services and the facility events emitted by health-facility-registry.
 * <p>
 * One BOUNDARY_CREATE event is published per {@code /boundary/_create} call, not per boundary in
 * the (bulk) payload — the event records the operator action, so {@code entity_id} stays null.
 * primary_role / user_category / system_role are resolved from the
 * {@code USER_ANALYTICS.USER_TYPE} master by best-match against the acting user's roles: active
 * records are sorted by descending system_roles count (most specific first) and the first record
 * whose system_roles the user fully holds wins.
 * <p>
 * Facility boundaries are left out of the count: they are created by health-facility-registry behind
 * a facility create, which publishes its own event, so a call carrying only those publishes nothing
 * — see {@link #isFacilityBoundaryCode}.
 * <p>
 * {@code state} is localized as {@code Boundary_<stateCode>} in {@code rainmaker-in}, the same key
 * SemAnalyticsService and FacilityAnalyticsService use, so all producers write identical state
 * strings into the shared index. The State code is parsed straight out of the boundary code rather
 * than fetched back from the hierarchy — see {@link #extractStateBoundaryCode}.
 * <p>
 * Every entry point is best-effort — any failure is logged and swallowed so analytics can never
 * break a boundary create.
 */
@Service
@Slf4j
public class BoundaryAnalyticsService {

    /** Boundary localizations live in this module at the national tenant. */
    private static final String LOCALIZATION_MODULE = "rainmaker-in";
    private static final String LOCALIZATION_LOCALE = "en_IN";
    private static final String LOCALIZATION_TENANT_ID = "in";

    /** Leading token of the facility id that health-facility-registry appends to a facility boundary code. */
    private static final String FACILITY_BOUNDARY_CODE_PREFIX = "FAC/";

    private final ApplicationProperties configs;
    private final MDMSUtils mdmsUtils;
    private final Producer producer;
    private final RestTemplate restTemplate;
    private final ObjectMapper mapper;

    public BoundaryAnalyticsService(ApplicationProperties configs, MDMSUtils mdmsUtils, Producer producer,
                                    RestTemplate restTemplate, ObjectMapper mapper) {
        this.configs = configs;
        this.mdmsUtils = mdmsUtils;
        this.producer = producer;
        this.restTemplate = restTemplate;
        this.mapper = mapper;
    }

    /**
     * Publishes a single BOUNDARY_CREATE event for one {@code /boundary/_create} request. The state
     * is taken from the first boundary in the payload that carries a usable state segment; a bulk
     * payload spanning several states is logged, since one event can only name one.
     */
    public void publishCreateEvent(BoundaryRequest boundaryRequest) {
        String tenantId = null;
        try {
            if (boundaryRequest == null || boundaryRequest.getBoundary() == null
                    || boundaryRequest.getBoundary().isEmpty()) {
                return;
            }
            List<Boundary> boundaries = boundaryRequest.getBoundary().stream()
                    .filter(Objects::nonNull)
                    .toList();
            if (boundaries.isEmpty()) {
                return;
            }

            // Facility boundaries are created by health-facility-registry as a side effect of a
            // facility create, not by an operator managing the boundary hierarchy — and that facility
            // create already publishes its own event onto this topic. Counting them here would double
            // count the same action, so they are dropped before the event is built.
            List<Boundary> reportableBoundaries = boundaries.stream()
                    .filter(boundary -> !isFacilityBoundaryCode(boundary.getCode()))
                    .toList();
            if (reportableBoundaries.isEmpty()) {
                log.info("Boundary analytics: all {} boundaries in create request are Facility boundaries, "
                        + "skipping event", boundaries.size());
                return;
            }
            boundaries = reportableBoundaries;

            tenantId = boundaries.stream()
                    .map(Boundary::getTenantId)
                    .filter(Objects::nonNull)
                    .findFirst()
                    .orElse(null);
            if (tenantId == null) {
                log.info("Boundary analytics: no tenantId in create request, skipping event");
                return;
            }

            RequestInfo requestInfo = boundaryRequest.getRequestInfo();
            User user = (requestInfo != null) ? requestInfo.getUserInfo() : null;
            UserType userType = resolveUserType(requestInfo, tenantId, extractUserRoleCodes(user));

            String state = resolveState(boundaries, requestInfo);

            UserAnalyticsEvent event = UserAnalyticsEvent.builder()
                    .eventId(UUID.randomUUID().toString())
                    .eventType(ANALYTICS_EVENT_BOUNDARY_CREATE)
                    .eventTime(Instant.now().toString())
                    .application(ANALYTICS_APPLICATION)
                    .user(user)
                    .systemRole(userType.systemRole)
                    .primaryRole(userType.primaryRole)
                    .userCategory(userType.userCategory)
                    .state(state)
                    .module(null)
                    .entityId(null)
                    .entityType(ANALYTICS_ENTITY_TYPE_BOUNDARY)
                    .build();
            producer.push(configs.getUserAnalyticsTopic(), event);

            log.info("Boundary analytics: published {} event for tenant {} ({} boundaries in request)",
                    ANALYTICS_EVENT_BOUNDARY_CREATE, tenantId, boundaries.size());
        } catch (Exception e) {
            log.error("Boundary analytics: failed to publish {} event for tenant {}",
                    ANALYTICS_EVENT_BOUNDARY_CREATE, tenantId, e);
        }
    }

    /**
     * Picks the state for the event: the first boundary in the payload with a usable state segment
     * wins. When the payload spans more than one state the extras are logged and dropped — a single
     * per-request event has one state field.
     */
    private String resolveState(List<Boundary> boundaries, RequestInfo requestInfo) {
        Set<String> stateBoundaryCodes = boundaries.stream()
                .map(boundary -> extractStateBoundaryCode(boundary.getCode()))
                .filter(Objects::nonNull)
                .collect(Collectors.toCollection(LinkedHashSet::new));

        if (stateBoundaryCodes.isEmpty()) {
            log.info("Boundary analytics: no state segment in any boundary code of the request, "
                    + "state will be null");
            return null;
        }
        if (stateBoundaryCodes.size() > 1) {
            log.info("Boundary analytics: create request spans multiple states {}, using the first for the event",
                    stateBoundaryCodes);
        }
        return localizeStateName(stateBoundaryCodes.iterator().next(), requestInfo);
    }

    /**
     * Looks up {@code Boundary_<stateBoundaryCode>} in {@code rainmaker-in} / {@code en_IN} at the
     * national tenant and returns the localized state name, or null when it cannot be resolved.
     * Best-effort — never throws.
     */
    private String localizeStateName(String stateBoundaryCode, RequestInfo requestInfo) {
        String code = "Boundary_" + stateBoundaryCode;
        try {
            String searchUrl = buildLocalizationSearchUrl();
            if (searchUrl == null) {
                log.warn("Boundary analytics: localization not configured; state will be null");
                return null;
            }

            String url = UriComponentsBuilder.fromHttpUrl(searchUrl)
                    .queryParam("tenantId", LOCALIZATION_TENANT_ID)
                    .queryParam("module", LOCALIZATION_MODULE)
                    .queryParam("locale", LOCALIZATION_LOCALE)
                    .queryParam("codes", code)
                    .build()
                    .toUriString();

            Map<String, Object> body = new HashMap<>();
            if (requestInfo != null) {
                body.put("RequestInfo", requestInfo);
            }
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            ResponseEntity<String> response = restTemplate.exchange(
                    url, HttpMethod.POST, new HttpEntity<>(body.isEmpty() ? null : body, headers), String.class);
            if (!response.getStatusCode().is2xxSuccessful() || response.getBody() == null) {
                log.warn("Boundary analytics: localization search returned no body for {}", code);
                return null;
            }

            String message = extractMessage(response.getBody(), code);
            if (message == null) {
                log.warn("Boundary analytics: no localization for {} in module {} at tenant {}",
                        code, LOCALIZATION_MODULE, LOCALIZATION_TENANT_ID);
            }
            return message;
        } catch (Exception e) {
            log.warn("Boundary analytics: localization lookup failed for {}: {}", code, e.getMessage());
            return null;
        }
    }

    /**
     * Builds the localization search URL without doubling the context path. Deployed environments
     * (including prod) set {@code egov.localization.search.endpoint} to a full path that already
     * contains {@code egov.localization.context.path}; concatenating both would produce
     * {@code .../localization/messages/v1/localization/messages/v1/_search}, which the
     * localization service rejects with a 400 NoResourceFoundException.
     */
    private String buildLocalizationSearchUrl() {
        String host = configs.getLocalizationHost();
        String endpoint = configs.getLocalizationSearchEndpoint();
        if (host == null || host.isBlank() || endpoint == null || endpoint.isBlank()) {
            return null;
        }
        String base = trimTrailingSlash(host.trim());
        String contextPath = configs.getLocalizationContextPath();
        if (contextPath == null || contextPath.isBlank()) {
            return base + endpoint;
        }
        String trimmedContext = trimTrailingSlash(contextPath.trim());
        // Endpoint already carries the context path (the deployed case) — don't add it twice.
        return endpoint.startsWith(trimmedContext) ? base + endpoint : base + trimmedContext + endpoint;
    }

    private static String trimTrailingSlash(String value) {
        return value.endsWith("/") ? value.substring(0, value.length() - 1) : value;
    }

    @SuppressWarnings("unchecked")
    private String extractMessage(String responseBody, String code) {
        try {
            Map<String, Object> root = mapper.readValue(responseBody, new TypeReference<Map<String, Object>>() {});
            Object messages = root.get("messages");
            if (!(messages instanceof List)) {
                return null;
            }
            for (Object entry : (List<?>) messages) {
                if (entry instanceof Map) {
                    Map<String, Object> message = (Map<String, Object>) entry;
                    if (code.equals(message.get("code"))) {
                        String text = asString(message.get("message"));
                        return (text != null && !text.isBlank()) ? text : null;
                    }
                }
            }
        } catch (Exception e) {
            log.warn("Boundary analytics: failed to parse localization response: {}", e.getMessage());
        }
        return null;
    }

    /**
     * Derives the State-level boundary code from a boundary code, no extra lookup needed. Boundary
     * codes are hierarchy paths such as {@code India_ArunachalPradesh_PapumPare_Doimukh}, so the
     * State code is the {@code India_<State>} prefix — the same derivation
     * {@code FacilityAnalyticsService#extractStateBoundaryCode} uses. Returns null when the code
     * carries no usable state segment.
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
     * Tells a Facility boundary from an administrative one by its code. The create request carries no
     * boundaryType — that only arrives on the follow-up {@code /boundary-relationships/_create} — so
     * the type is read off the code instead: health-facility-registry appends the generated facility
     * id to the parent block code, giving {@code India_Assam_Kamrup_Amingaon_FAC/2025/0045}. Matching
     * the last segment against the {@code FAC/} prefix is the same check
     * {@code IMUtils#extractFacilityCode} uses.
     */
    private boolean isFacilityBoundaryCode(String boundaryCode) {
        if (boundaryCode == null || boundaryCode.isBlank()) {
            return false;
        }
        String[] parts = boundaryCode.split("_");
        return parts[parts.length - 1].startsWith(FACILITY_BOUNDARY_CODE_PREFIX);
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
            List<Map<String, Object>> userTypeRecords =
                    mdmsUtils.fetchMasterData(requestInfo, tenantId, USER_ANALYTICS_MODULE, MDMS_MASTER_USER_TYPE);

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
                log.info("Boundary analytics: no USER_TYPE match for roles {} in tenant {}", userRoleCodes, tenantId);
            }
        } catch (Exception e) {
            log.warn("Boundary analytics: failed to resolve USER_TYPE for tenant {}: {}", tenantId, e.getMessage());
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
