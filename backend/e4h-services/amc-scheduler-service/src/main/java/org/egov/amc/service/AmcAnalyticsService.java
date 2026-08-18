package org.egov.amc.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.egov.amc.config.AMCServiceConfiguration;
import org.egov.amc.util.MDMSUtils;
import org.egov.amc.web.models.AmcConfiguration;
import org.egov.amc.web.models.AmcConfigurationRequest;
import org.egov.amc.web.models.Facility;
import org.egov.amc.web.models.FacilityBulkSearchApiRequest;
import org.egov.amc.web.models.FacilityBulkSearchCriteria;
import org.egov.amc.web.models.FacilitySearchResponse;
import org.egov.amc.web.models.ScheduledVisit;
import org.egov.amc.web.models.UserAnalyticsEvent;
import org.egov.common.contract.request.RequestInfo;
import org.egov.common.contract.request.Role;
import org.egov.common.contract.request.User;
import org.egov.common.producer.Producer;
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

import static org.egov.amc.util.AmcConstants.ANALYTICS_APPLICATION_FIELD_ASSIST;
import static org.egov.amc.util.AmcConstants.ANALYTICS_APPLICATION_MANAGEMENT_HUB;
import static org.egov.amc.util.AmcConstants.ANALYTICS_ENTITY_TYPE_AMC_CONFIGURATION;
import static org.egov.amc.util.AmcConstants.ANALYTICS_ENTITY_TYPE_AMC_VISIT;
import static org.egov.amc.util.AmcConstants.ANALYTICS_EVENT_AMC_SCHEDULED;
import static org.egov.amc.util.AmcConstants.ANALYTICS_EVENT_AMC_VISIT_OTP_VERIFIED;
import static org.egov.amc.util.AmcConstants.ANALYTICS_EVENT_AMC_VISIT_RESUBMITTED;
import static org.egov.amc.util.AmcConstants.ANALYTICS_EVENT_AMC_VISIT_SUBMITTED;
import static org.egov.amc.util.AmcConstants.ANALYTICS_MODULE_AMC;
import static org.egov.amc.util.AmcConstants.BOUNDARY_LOCALIZATION_MODULE;
import static org.egov.amc.util.AmcConstants.LOCALIZATION_LOCALE;
import static org.egov.amc.util.AmcConstants.LOCALIZATION_TENANT_ID;
import static org.egov.amc.util.AmcConstants.MDMS_MASTER_AMC;
import static org.egov.amc.util.AmcConstants.MDMS_MASTER_USER_TYPE;
import static org.egov.amc.util.AmcConstants.TENANTID;

/**
 * Builds and publishes {@link UserAnalyticsEvent}s to the shared {@code user-analytics-event}
 * topic, so they land in the same user-analytics-report index as the SEM events emitted by
 * im-services, the facility events emitted by health-facility-registry and the boundary events
 * emitted by boundary-service. All AMC events carry {@code application = MANAGEMENT_HUB} and
 * {@code module = AMC}.
 * <p>
 * Two entry points, one per tracked activity:
 * <ul>
 *   <li>{@link #publishConfigurationCreateEvents} — AMC_SCHEDULED, one event per AMC configuration
 *       in a {@code /configuration/_create} call (the call is bulk; the ingestion-service
 *       bulk-upload client is its main caller). The later {@code /visit/configuration/_generate}
 *       visit generation is cron/operator-triggered rather than a discrete user action, so it is
 *       deliberately not instrumented.</li>
 *   <li>{@link #publishVisitWorkflowEvent} — one event per {@code /visit/workflow/_update} call,
 *       with the event type driven by the workflow action.</li>
 * </ul>
 * The visit event type comes from the {@code USER_ANALYTICS.AMC} master by matching the request's
 * workflow action, mirroring {@code SemAnalyticsService}. Because a single action can mean
 * different things depending on where the visit came from, a record may additionally narrow itself
 * with {@code prior_status}: the report submission action is SUBMITTED out of SCHEDULED but
 * RESUBMITTED out of REJECTED. Records naming the visit's pre-transition status in
 * {@code prior_status} win over the unqualified fallback record for the same action.
 * <p>
 * {@code state} is localized as {@code Boundary_<stateCode>} in {@code rainmaker-in}, the same key
 * the other producers use, so all producers write identical state strings into the shared index.
 * The State code is parsed straight out of the facility's boundary code rather than fetched from
 * the boundary service — see {@link #extractStateBoundaryCode}.
 * <p>
 * Every entry point is best-effort — any failure is logged and swallowed so analytics can never
 * break an AMC flow.
 */
@Service
@Slf4j
public class AmcAnalyticsService {

    /** Max facility ids per facility-service bulk-search call, matching AmcConfigurationValidator. */
    private static final int FACILITY_BULK_CHUNK_SIZE = 500;

    private final AMCServiceConfiguration configs;
    private final MDMSUtils mdmsUtils;
    private final Producer producer;
    private final ServiceRequestRepository requestRepository;
    private final RestTemplate restTemplate;
    private final ObjectMapper mapper;

    public AmcAnalyticsService(AMCServiceConfiguration configs, MDMSUtils mdmsUtils, Producer producer,
                              ServiceRequestRepository requestRepository, RestTemplate restTemplate,
                              @Qualifier("objectMapper") ObjectMapper mapper) {
        this.configs = configs;
        this.mdmsUtils = mdmsUtils;
        this.producer = producer;
        this.requestRepository = requestRepository;
        this.restTemplate = restTemplate;
        this.mapper = mapper;
    }

    /**
     * Publishes one AMC_SCHEDULED event per AMC configuration in the create request. Creating a
     * configuration is the actual scheduling action, so this is the instrumentation point for
     * "AMC Scheduled".
     * <p>
     * Boundary codes are resolved with a single bulk facility search for all distinct facility ids
     * in the request (the validator's own prefetch is local to it), and localization is memoized
     * per distinct state, so a bulk create costs one facility search plus one localization call
     * per state regardless of how many configurations it carries.
     */
    public void publishConfigurationCreateEvents(AmcConfigurationRequest request) {
        try {
            if (request == null || request.getAmcConfigurations() == null) {
                return;
            }
            List<AmcConfiguration> configurations = request.getAmcConfigurations().stream()
                    .filter(Objects::nonNull)
                    .toList();
            if (configurations.isEmpty()) {
                return;
            }

            RequestInfo requestInfo = request.getRequestInfo();
            User user = (requestInfo != null) ? requestInfo.getUserInfo() : null;
            Set<String> userRoleCodes = extractUserRoleCodes(user);

            String tenantId = configurations.stream()
                    .map(AmcConfiguration::getTenantId)
                    .filter(Objects::nonNull)
                    .findFirst()
                    .orElse(TENANTID);

            // AMC_SCHEDULED is not a workflow action, so the role fields come from USER_TYPE alone,
            // the same best-match BoundaryAnalyticsService uses for its per-request create event.
            List<Map<String, Object>> userTypeRecords = mdmsUtils
                    .fetchUserAnalyticsMasters(requestInfo, tenantId)
                    .getOrDefault(MDMS_MASTER_USER_TYPE, Collections.emptyList());
            UserType userType = resolveUserTypeByRoles(userTypeRecords, userRoleCodes, tenantId);

            Map<String, Facility> facilitiesById = fetchFacilities(requestInfo, configurations);
            Map<String, String> stateNameByCode = new HashMap<>();

            for (AmcConfiguration configuration : configurations) {
                Facility facility = (configuration.getFacilityId() == null)
                        ? null : facilitiesById.get(configuration.getFacilityId());
                String boundaryCode = (facility != null) ? facility.getBoundaryCode() : null;

                UserAnalyticsEvent event = UserAnalyticsEvent.builder()
                        .eventId(UUID.randomUUID().toString())
                        .eventType(ANALYTICS_EVENT_AMC_SCHEDULED)
                        .eventTime(Instant.now().toString())
                        .application(ANALYTICS_APPLICATION_MANAGEMENT_HUB)
                        .user(user)
                        .systemRole(userType.systemRole)
                        .primaryRole(userType.primaryRole)
                        .userCategory(userType.userCategory)
                        .module(ANALYTICS_MODULE_AMC)
                        .state(resolveState(boundaryCode, requestInfo, stateNameByCode,
                                configuration.getFacilityId()))
                        .entityId(configuration.getId())
                        .entityType(ANALYTICS_ENTITY_TYPE_AMC_CONFIGURATION)
                        .build();
                producer.push(configs.getUserAnalyticsTopic(), event);
            }

            log.info("AMC analytics: published {} {} event(s) for tenant {}", configurations.size(),
                    ANALYTICS_EVENT_AMC_SCHEDULED, tenantId);
        } catch (Exception e) {
            log.error("AMC analytics: failed to publish {} event(s)", ANALYTICS_EVENT_AMC_SCHEDULED, e);
        }
    }

    /**
     * Publishes one event for a visit workflow transition. The event type is resolved from the
     * {@code USER_ANALYTICS.AMC} master using the workflow action plus {@code priorStatus} — the
     * visit's status *before* the transition, which is what separates a first report submission
     * (out of SCHEDULED) from a re-submission (out of REJECTED).
     *
     * @param visit        the visit as fetched before the transition; its enriched facility supplies
     *                     the boundary code, so no extra facility lookup is needed
     * @param action       the workflow action from the request
     * @param priorStatus  the visit status captured before the transition overwrote it
     */
    public void publishVisitWorkflowEvent(ScheduledVisit visit, String action, String priorStatus,
                                          RequestInfo requestInfo) {
        String visitId = (visit != null) ? visit.getId() : null;
        try {
            if (visit == null || action == null || action.isBlank()) {
                log.info("AMC analytics: no visit or workflow action, skipping event for visitId={}", visitId);
                return;
            }

            String tenantId = (visit.getTenantId() != null) ? visit.getTenantId() : TENANTID;
            User user = (requestInfo != null) ? requestInfo.getUserInfo() : null;
            Set<String> userRoleCodes = extractUserRoleCodes(user);

            Map<String, List<Map<String, Object>>> masters =
                    mdmsUtils.fetchUserAnalyticsMasters(requestInfo, tenantId);
            List<Map<String, Object>> amcRecords = masters.getOrDefault(MDMS_MASTER_AMC, Collections.emptyList());
            List<Map<String, Object>> userTypeRecords =
                    masters.getOrDefault(MDMS_MASTER_USER_TYPE, Collections.emptyList());

            ActionMatch match = matchAction(amcRecords, action, priorStatus);
            if (match == null) {
                log.info("AMC analytics: no matching {} record for action={} priorStatus={} visitId={}, "
                        + "skipping event", MDMS_MASTER_AMC, action, priorStatus, visitId);
                return;
            }

            // system_role: the user's role that is listed in the matched action's action_roles.
            String systemRole = match.actionRoles.stream()
                    .filter(userRoleCodes::contains)
                    .findFirst()
                    .orElse(null);
            UserType userType = resolveUserTypeBySystemRole(userTypeRecords, systemRole, userRoleCodes);

            Facility facility = visit.getFacility();
            String boundaryCode = (facility != null) ? facility.getBoundaryCode() : null;

            String application = resolveApplicationForEventType(match.eventType);

            UserAnalyticsEvent event = UserAnalyticsEvent.builder()
                    .eventId(UUID.randomUUID().toString())
                    .eventType(match.eventType)
                    .eventTime(Instant.now().toString())
                    .application(application)
                    .user(user)
                    .systemRole(systemRole)
                    .primaryRole(userType.primaryRole)
                    .userCategory(userType.userCategory)
                    .module(ANALYTICS_MODULE_AMC)
                    .state(resolveState(boundaryCode, requestInfo, new HashMap<>(), visit.getFacilityId()))
                    .entityId(visit.getId())
                    .entityType(ANALYTICS_ENTITY_TYPE_AMC_VISIT)
                    .build();
            producer.push(configs.getUserAnalyticsTopic(), event);

            log.info("AMC analytics: published event_type={} for visitId={} (action={}, priorStatus={})",
                    match.eventType, visitId, action, priorStatus);
        } catch (Exception e) {
            log.error("AMC analytics: failed to publish event for visitId={} action={}", visitId, action, e);
        }
    }

    /**
     * Finds the USER_ANALYTICS.AMC record for a workflow action. Among the active records naming
     * the action, one that lists {@code priorStatus} in its {@code prior_status} wins; otherwise
     * the record with no {@code prior_status} is the fallback. That is how one action
     * (report submission) yields SUBMITTED out of SCHEDULED but RESUBMITTED out of REJECTED,
     * without hardcoding either status here.
     */
    private ActionMatch matchAction(List<Map<String, Object>> amcRecords, String action, String priorStatus) {
        ActionMatch fallback = null;
        for (Map<String, Object> record : amcRecords) {
            if (!isActive(record)) {
                continue;
            }
            String eventType = asString(record.get("action_analytics"));
            if (eventType == null) {
                continue;
            }
            for (Map<String, Object> workflow : asMapList(record.get("action_workflow"))) {
                if (!action.equalsIgnoreCase(asString(workflow.get("action_name")))) {
                    continue;
                }
                List<String> actionRoles = asStringList(workflow.get("action_roles"));
                List<String> priorStatuses = asStringList(record.get("prior_status"));
                if (priorStatuses.isEmpty()) {
                    if (fallback == null) {
                        fallback = new ActionMatch(eventType, actionRoles);
                    }
                } else if (priorStatus != null
                        && priorStatuses.stream().anyMatch(priorStatus::equalsIgnoreCase)) {
                    // A status-qualified record is more specific than the fallback — take it now.
                    return new ActionMatch(eventType, actionRoles);
                }
            }
        }
        return fallback;
    }

    /**
     * primary_role + user_category resolved from the action-derived system role, matching
     * {@code SemAnalyticsService}:
     * <ol>
     *   <li>Shortlist active USER_TYPE records whose system_roles contain the system role.</li>
     *   <li>Sort that shortlist by descending count of system_roles (most specific first).</li>
     *   <li>The first record whose system_roles are ALL held by the user wins.</li>
     * </ol>
     */
    private UserType resolveUserTypeBySystemRole(List<Map<String, Object>> userTypeRecords, String systemRole,
                                                 Set<String> userRoleCodes) {
        UserType result = new UserType();
        if (systemRole == null) {
            return result;
        }
        List<Map<String, Object>> candidates = userTypeRecords.stream()
                .filter(this::isActive)
                .filter(record -> asStringList(record.get("system_roles")).contains(systemRole))
                .sorted(Comparator.comparingInt(
                        (Map<String, Object> record) -> asStringList(record.get("system_roles")).size()).reversed())
                .toList();
        for (Map<String, Object> record : candidates) {
            if (userRoleCodes.containsAll(asStringList(record.get("system_roles")))) {
                result.systemRole = systemRole;
                result.primaryRole = asString(record.get("program_role"));
                result.userCategory = asString(record.get("user_category"));
                break;
            }
        }
        return result;
    }

    /**
     * Best-match lookup against USER_ANALYTICS.USER_TYPE when there is no workflow action to derive
     * a system role from (the configuration create path): shortlist active records whose
     * system_roles the user fully holds, most specific (largest system_roles) first. Mirrors
     * {@code BoundaryAnalyticsService#resolveUserType}.
     */
    private UserType resolveUserTypeByRoles(List<Map<String, Object>> userTypeRecords, Set<String> userRoleCodes,
                                            String tenantId) {
        UserType result = new UserType();
        if (userRoleCodes.isEmpty()) {
            return result;
        }
        List<Map<String, Object>> candidates = userTypeRecords.stream()
                .filter(this::isActive)
                .filter(record -> !asStringList(record.get("system_roles")).isEmpty())
                .filter(record -> userRoleCodes.containsAll(asStringList(record.get("system_roles"))))
                .sorted(Comparator.comparingInt(
                        (Map<String, Object> record) -> asStringList(record.get("system_roles")).size()).reversed())
                .toList();

        if (candidates.isEmpty()) {
            log.info("AMC analytics: no {} match for roles {} in tenant {}", MDMS_MASTER_USER_TYPE, userRoleCodes,
                    tenantId);
            return result;
        }
        Map<String, Object> match = candidates.get(0);
        List<String> systemRoles = asStringList(match.get("system_roles"));
        // system_role: the matched record's role the user actually holds, in the user's role order.
        result.systemRole = userRoleCodes.stream().filter(systemRoles::contains).findFirst().orElse(null);
        result.primaryRole = asString(match.get("program_role"));
        result.userCategory = asString(match.get("user_category"));
        return result;
    }

    /**
     * One bulk facility search for every distinct facility id across the configurations, chunked
     * the same way {@code AmcConfigurationValidator} chunks its own prefetch. Returns an empty map
     * on failure — a missing facility only means a null state on the event.
     */
    private Map<String, Facility> fetchFacilities(RequestInfo requestInfo, List<AmcConfiguration> configurations) {
        Map<String, Facility> byFacilityId = new HashMap<>();
        try {
            LinkedHashSet<String> facilityIds = new LinkedHashSet<>();
            LinkedHashSet<String> tenantIds = new LinkedHashSet<>();
            for (AmcConfiguration configuration : configurations) {
                if (configuration.getFacilityId() != null && !configuration.getFacilityId().isBlank()) {
                    facilityIds.add(configuration.getFacilityId().trim());
                }
                if (configuration.getTenantId() != null && !configuration.getTenantId().isBlank()) {
                    tenantIds.add(configuration.getTenantId().trim());
                }
            }
            if (facilityIds.isEmpty()) {
                return byFacilityId;
            }
            List<String> tenantList = tenantIds.isEmpty() ? List.of(TENANTID) : new ArrayList<>(tenantIds);
            List<String> idList = new ArrayList<>(facilityIds);
            String url = configs.getFacilityServiceHost() + configs.getFacilityBulkSearchPath();

            for (int i = 0; i < idList.size(); i += FACILITY_BULK_CHUNK_SIZE) {
                int end = Math.min(i + FACILITY_BULK_CHUNK_SIZE, idList.size());
                FacilityBulkSearchApiRequest body = FacilityBulkSearchApiRequest.builder()
                        .requestInfo(requestInfo)
                        .facility(FacilityBulkSearchCriteria.forTenantAndFacilityIds(
                                tenantList, new ArrayList<>(idList.subList(i, end))))
                        .build();
                Object response = requestRepository.fetchResult(new StringBuilder(url), body);
                FacilitySearchResponse parsed = mapper.convertValue(response, FacilitySearchResponse.class);
                if (parsed == null || parsed.getFacilities() == null) {
                    continue;
                }
                for (Facility facility : parsed.getFacilities()) {
                    if (facility.getFacilityId() != null) {
                        byFacilityId.put(facility.getFacilityId(), facility);
                    }
                }
            }
        } catch (Exception e) {
            log.warn("AMC analytics: facility bulk search failed, state will be null: {}", e.getMessage());
        }
        return byFacilityId;
    }

    /**
     * Resolves the localized state name for a facility boundary code, memoizing per state code in
     * {@code stateNameByCode} so a bulk create costs one localization call per distinct state.
     */
    private String resolveState(String boundaryCode, RequestInfo requestInfo, Map<String, String> stateNameByCode,
                                String facilityId) {
        String stateBoundaryCode = extractStateBoundaryCode(boundaryCode);
        if (stateBoundaryCode == null) {
            log.info("AMC analytics: no state segment in boundaryCode={} for facilityId={}, state will be null",
                    boundaryCode, facilityId);
            return null;
        }
        // Not computeIfAbsent: a failed localization is null, and we want to cache that too rather
        // than retry the lookup for every configuration in the state.
        if (!stateNameByCode.containsKey(stateBoundaryCode)) {
            stateNameByCode.put(stateBoundaryCode, localizeStateName(stateBoundaryCode, requestInfo));
        }
        return stateNameByCode.get(stateBoundaryCode);
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
                log.warn("AMC analytics: localization not configured; state will be null");
                return null;
            }

            String url = UriComponentsBuilder.fromHttpUrl(searchUrl)
                    .queryParam("tenantId", LOCALIZATION_TENANT_ID)
                    .queryParam("module", BOUNDARY_LOCALIZATION_MODULE)
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
                log.warn("AMC analytics: localization search returned no body for {}", code);
                return null;
            }

            String message = extractMessage(response.getBody(), code);
            if (message == null) {
                log.warn("AMC analytics: no localization for {} in module {} at tenant {}", code,
                        BOUNDARY_LOCALIZATION_MODULE, LOCALIZATION_TENANT_ID);
            }
            return message;
        } catch (Exception e) {
            log.warn("AMC analytics: localization lookup failed for {}: {}", code, e.getMessage());
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
            log.warn("AMC analytics: failed to parse localization response: {}", e.getMessage());
        }
        return null;
    }

    /**
     * Derives the State-level boundary code from a facility boundary code, no boundary-service
     * lookup needed. Facility boundary codes are hierarchy paths such as
     * {@code India_ArunachalPradesh_PapumPare_Doimukh_<facilityId>}, so the State code is the
     * {@code India_<State>} prefix — the same derivation
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
    private List<Map<String, Object>> asMapList(Object value) {
        return (value instanceof List) ? (List<Map<String, Object>>) value : Collections.emptyList();
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

    private String resolveApplicationForEventType(String eventType) {
        if (eventType == null) {
            return ANALYTICS_APPLICATION_MANAGEMENT_HUB;
        }
        return switch (eventType) {
            case ANALYTICS_EVENT_AMC_VISIT_SUBMITTED,
                 ANALYTICS_EVENT_AMC_VISIT_RESUBMITTED,
                 ANALYTICS_EVENT_AMC_VISIT_OTP_VERIFIED -> ANALYTICS_APPLICATION_FIELD_ASSIST;
            default -> ANALYTICS_APPLICATION_MANAGEMENT_HUB;
        };
    }

    /** The USER_ANALYTICS.AMC record matched for a workflow action. */
    private static class ActionMatch {
        private final String eventType;
        private final List<String> actionRoles;

        private ActionMatch(String eventType, List<String> actionRoles) {
            this.eventType = eventType;
            this.actionRoles = actionRoles;
        }
    }

    /** The three role-derived fields resolved together from a single USER_TYPE record. */
    private static class UserType {
        private String systemRole;
        private String primaryRole;
        private String userCategory;
    }
}
