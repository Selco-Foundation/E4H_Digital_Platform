package org.egov.project.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.egov.common.contract.request.RequestInfo;
import org.egov.common.contract.request.Role;
import org.egov.common.contract.request.User;
import org.egov.common.models.project.Project;
import org.egov.common.models.project.ProjectRequest;
import org.egov.common.producer.Producer;
import org.egov.project.config.ProjectConfiguration;
import org.egov.project.util.MDMSUtils;
import org.egov.project.web.models.UserAnalyticsEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
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

import static org.egov.project.util.ProjectConstants.ANALYTICS_APPLICATION;
import static org.egov.project.util.ProjectConstants.ANALYTICS_ENTITY_TYPE_PROJECT;
import static org.egov.project.util.ProjectConstants.ANALYTICS_EVENT_PROJECT_CREATE;
import static org.egov.project.util.ProjectConstants.MDMS_MASTER_USER_TYPE;
import static org.egov.project.util.ProjectConstants.USER_ANALYTICS_MODULE;

/**
 * Builds and publishes {@link UserAnalyticsEvent}s to the shared {@code user-analytics-event}
 * topic on project create, so they land in the same user-analytics-report index as the SEM events
 * emitted by im-services, the facility events emitted by health-facility-registry, the boundary
 * events emitted by boundary-service and the AMC events emitted by amc-scheduler-service.
 * <p>
 * One PROJECT_CREATE event is published per project in the (bulk) {@code /project/v1/_create}
 * payload, so {@code entity_id} carries the created project's id. event_type is fixed — the
 * project's workflow transitions are a separate flow with no action to map here.
 * primary_role / user_category / system_role are resolved from the
 * {@code USER_ANALYTICS.USER_TYPE} master by best-match against the acting user's roles: active
 * records are sorted by descending system_roles count (most specific first) and the first record
 * whose system_roles the user fully holds wins.
 * <p>
 * {@code state} is localized as {@code Boundary_<stateCode>} in {@code rainmaker-in}, the same key
 * SemAnalyticsService, FacilityAnalyticsService and BoundaryAnalyticsService use, so all producers
 * write identical state strings into the shared index. The State code is parsed straight out of
 * the project address' boundary code rather than fetched from the boundary service — see
 * {@link #extractStateBoundaryCode}.
 * <p>
 * Every entry point is best-effort — any failure is logged and swallowed so analytics can never
 * break a project create.
 */
@Service
@Slf4j
public class ProjectAnalyticsService {

    /** Boundary localizations live in this module at the national tenant. */
    private static final String LOCALIZATION_MODULE = "rainmaker-in";
    private static final String LOCALIZATION_LOCALE = "en_IN";
    private static final String LOCALIZATION_TENANT_ID = "in";

    private final ProjectConfiguration configs;
    private final MDMSUtils mdmsUtils;
    private final Producer producer;
    private final RestTemplate restTemplate;
    private final ObjectMapper mapper;

    public ProjectAnalyticsService(ProjectConfiguration configs, MDMSUtils mdmsUtils, Producer producer,
                                   RestTemplate restTemplate, @Qualifier("objectMapper") ObjectMapper mapper) {
        this.configs = configs;
        this.mdmsUtils = mdmsUtils;
        this.producer = producer;
        this.restTemplate = restTemplate;
        this.mapper = mapper;
    }

    /**
     * Publishes one PROJECT_CREATE event per created project. MDMS is hit once per tenant and the
     * localization lookup is memoized per state, not repeated per project.
     */
    public void publishCreateEvents(ProjectRequest projectRequest) {
        if (projectRequest == null || projectRequest.getProjects() == null
                || projectRequest.getProjects().isEmpty()) {
            return;
        }
        Map<String, List<Project>> byTenant = projectRequest.getProjects().stream()
                .filter(project -> project != null && project.getTenantId() != null)
                .collect(Collectors.groupingBy(Project::getTenantId));

        byTenant.forEach((tenantId, tenantProjects) ->
                publishEvents(projectRequest.getRequestInfo(), tenantId, tenantProjects));
    }

    private void publishEvents(RequestInfo requestInfo, String tenantId, List<Project> projects) {
        try {
            User user = (requestInfo != null) ? requestInfo.getUserInfo() : null;
            UserType userType = resolveUserType(requestInfo, tenantId, extractUserRoleCodes(user));
            String eventTime = Instant.now().toString();

            // One localization call per distinct state, not per project.
            Map<String, String> stateNameByCode = new HashMap<>();
            int published = 0;

            for (Project project : projects) {
                if (project.getId() == null) {
                    continue;
                }
                String boundaryCode = (project.getAddress() != null) ? project.getAddress().getBoundary() : null;
                String stateBoundaryCode = extractStateBoundaryCode(boundaryCode);
                String state = null;
                if (stateBoundaryCode == null) {
                    log.info("Project analytics: no state segment in boundary={} for projectId={}, "
                            + "state will be null", boundaryCode, project.getId());
                } else {
                    // Not computeIfAbsent: a failed localization is null, and we want to cache that
                    // too rather than retry the lookup for every project in the state.
                    if (!stateNameByCode.containsKey(stateBoundaryCode)) {
                        stateNameByCode.put(stateBoundaryCode,
                                localizeStateName(stateBoundaryCode, requestInfo));
                    }
                    state = stateNameByCode.get(stateBoundaryCode);
                }

                UserAnalyticsEvent event = UserAnalyticsEvent.builder()
                        .eventId(UUID.randomUUID().toString())
                        .eventType(ANALYTICS_EVENT_PROJECT_CREATE)
                        .eventTime(eventTime)
                        .application(ANALYTICS_APPLICATION)
                        .user(user)
                        .systemRole(userType.systemRole)
                        .primaryRole(userType.primaryRole)
                        .userCategory(userType.userCategory)
                        .state(state)
                        .module(null)
                        .entityId(project.getId())
                        .entityType(ANALYTICS_ENTITY_TYPE_PROJECT)
                        .build();
                producer.push(configs.getUserAnalyticsTopic(), event);
                published++;
            }
            log.info("Project analytics: published {} {} event(s) for tenant {}",
                    published, ANALYTICS_EVENT_PROJECT_CREATE, tenantId);
        } catch (Exception e) {
            log.error("Project analytics: failed to publish {} event(s) for tenant {}",
                    ANALYTICS_EVENT_PROJECT_CREATE, tenantId, e);
        }
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
                log.warn("Project analytics: localization not configured; state will be null");
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
                log.warn("Project analytics: localization search returned no body for {}", code);
                return null;
            }

            String message = extractMessage(response.getBody(), code);
            if (message == null) {
                log.warn("Project analytics: no localization for {} in module {} at tenant {}",
                        code, LOCALIZATION_MODULE, LOCALIZATION_TENANT_ID);
            }
            return message;
        } catch (Exception e) {
            log.warn("Project analytics: localization lookup failed for {}: {}", code, e.getMessage());
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
            log.warn("Project analytics: failed to parse localization response: {}", e.getMessage());
        }
        return null;
    }

    /**
     * Derives the State-level boundary code from the project address' boundary code, no
     * boundary-service call needed. Boundary codes are hierarchy paths such as
     * {@code India_ArunachalPradesh_PapumPare_Doimukh}, so the State code is the
     * {@code India_<State>} prefix — the same derivation
     * {@link ProjectNameGenerationService#resolveStateCode} and
     * {@code FacilityAnalyticsService#extractStateBoundaryCode} use. Returns null when the code
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
                log.info("Project analytics: no USER_TYPE match for roles {} in tenant {}", userRoleCodes, tenantId);
            }
        } catch (Exception e) {
            log.warn("Project analytics: failed to resolve USER_TYPE for tenant {}: {}", tenantId, e.getMessage());
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
