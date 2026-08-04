package org.egov.activity.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.egov.activity.config.ActivityConfiguration;
import org.egov.activity.util.MDMSUtils;
import org.egov.activity.web.models.ActivityFacility;
import org.egov.activity.web.models.Facility;
import org.egov.activity.web.models.UserAnalyticsEvent;
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

import static org.egov.activity.util.ActivityConstants.ANALYTICS_APPLICATION_DEFAULT;
import static org.egov.activity.util.ActivityConstants.ANALYTICS_ENTITY_TYPE_ACTIVITY_FACILITY;
import static org.egov.activity.util.ActivityConstants.ANALYTICS_MODULE_FIELD_PLANNER;
import static org.egov.activity.util.ActivityConstants.BOUNDARY_LOCALIZATION_MODULE;
import static org.egov.activity.util.ActivityConstants.LOCALIZATION_LOCALE;
import static org.egov.activity.util.ActivityConstants.LOCALIZATION_TENANT_ID;
import static org.egov.activity.util.ActivityConstants.MDMS_MASTER_FIELD_PLANNER;
import static org.egov.activity.util.ActivityConstants.MDMS_MASTER_USER_TYPE;
import static org.egov.activity.util.ActivityConstants.TENANTID;

/**
 * Builds and publishes {@link UserAnalyticsEvent}s to the shared {@code user-analytics-event}
 * topic, so they land in the same user-analytics-report index as the SEM events emitted by
 * im-services, the facility events emitted by health-facility-registry, the boundary events emitted
 * by boundary-service, the AMC events emitted by amc-scheduler-service and the project events
 * emitted by project. All events from this producer carry {@code module = FIELD_PLANNER}.
 * <p>
 * One entry point: {@link #publishWorkflowEvent}, called once per successful
 * {@code /activity/v1/activities/workflow/update} transition. The bulk endpoint
 * {@code /activity/v1/activities/bulk/workflow/update} loops over that same method, so
 * bulk-approved reports are counted too rather than silently disappearing — that loop shares one
 * {@link AnalyticsContext} so a bulk approval of N reports still costs one MDMS call and one
 * localization call per state, not N of each.
 * <p>
 * The event type comes from the {@code USER_ANALYTICS.FIELD_PLANNER} master by matching the
 * request's workflow action, mirroring {@code SemAnalyticsService} and {@code AmcAnalyticsService}.
 * Because a single action means different things depending on the state it fires from, a record may
 * additionally narrow itself with {@code prior_status}: in the FACILITY_INSTALLATION business
 * service {@code SUBMIT_REPORT_A} out of {@code ASSIGNED_TO_FIELD_STAFF} is a first submission but
 * out of {@code REJECTED_BY_FIELD_SUPERVISOR} it is a re-submission, and {@code SUBMIT_REPORT_B} is
 * a first submission out of the three assignment/Part-A states but a re-submission out of
 * {@code REJECTED_BY_QC_SPOC}. Records naming the pre-transition status in {@code prior_status} win
 * over the unqualified fallback record for the same action.
 * <p>
 * Not every action on the business service is a tracked business event: {@code SCHEDULED} and
 * {@code ASSIGN_FIELD_STAFF} are driven automatically by {@code ActivityAssignmentConsumer} for
 * every facility of a new field plan, and are deliberately left out of the master so they emit
 * nothing.
 * <p>
 * {@code application} is read off the matched record rather than fixed as a constant, because this
 * one endpoint serves two personas: the field staff / supervisor submissions come from Field Assist
 * while the QC review decisions come from the Management Hub.
 * <p>
 * {@code state} is localized as {@code Boundary_<stateCode>} in {@code rainmaker-in}, the same key
 * the other producers use, so all producers write identical state strings into the shared index.
 * The State code is parsed straight out of the facility's boundary code rather than fetched from
 * the boundary service — see {@link #extractStateBoundaryCode}.
 * <p>
 * Every entry point is best-effort — any failure is logged and swallowed so analytics can never
 * break a workflow transition.
 */
@Service
@Slf4j
public class ActivityAnalyticsService {

    private final ActivityConfiguration configs;
    private final MDMSUtils mdmsUtils;
    private final Producer producer;
    private final RestTemplate restTemplate;
    private final ObjectMapper mapper;

    public ActivityAnalyticsService(ActivityConfiguration configs, MDMSUtils mdmsUtils, Producer producer,
                                    RestTemplate restTemplate, @Qualifier("objectMapper") ObjectMapper mapper) {
        this.configs = configs;
        this.mdmsUtils = mdmsUtils;
        this.producer = producer;
        this.restTemplate = restTemplate;
        this.mapper = mapper;
    }

    /**
     * Per-call memo for the MDMS masters and the resolved state names. Single-transition callers
     * create a throwaway context; the bulk workflow loop creates one and reuses it across every
     * activity facility in the batch.
     */
    public AnalyticsContext newContext() {
        return new AnalyticsContext();
    }

    /**
     * Publishes one event for a completed workflow transition on an activity facility.
     *
     * @param requestInfo      the acting user's request info
     * @param activityFacility the activity facility as it was read from the DB, i.e. with the
     *                         facility enriched, so the boundary code is available for the state
     * @param action           the workflow action that was applied
     * @param priorStatus      the activity facility's status <em>before</em> the transition, used to
     *                         tell a first submission apart from a re-submission
     * @param context          the shared memo, from {@link #newContext()}
     */
    public void publishWorkflowEvent(RequestInfo requestInfo, ActivityFacility activityFacility, String action,
                                     String priorStatus, AnalyticsContext context) {
        String activityFacilityId = (activityFacility != null) ? activityFacility.getId() : null;
        try {
            if (activityFacility == null || action == null || action.isBlank()) {
                log.info("Activity analytics: no activity facility or workflow action, skipping event for "
                        + "activityFacilityId={}", activityFacilityId);
                return;
            }
            AnalyticsContext ctx = (context != null) ? context : new AnalyticsContext();

            String tenantId = (activityFacility.getTenantId() != null)
                    ? activityFacility.getTenantId() : TENANTID;
            User user = (requestInfo != null) ? requestInfo.getUserInfo() : null;
            Set<String> userRoleCodes = extractUserRoleCodes(user);

            Map<String, List<Map<String, Object>>> masters = ctx.masters(requestInfo, tenantId, mdmsUtils);
            List<Map<String, Object>> fieldPlannerRecords =
                    masters.getOrDefault(MDMS_MASTER_FIELD_PLANNER, Collections.emptyList());
            List<Map<String, Object>> userTypeRecords =
                    masters.getOrDefault(MDMS_MASTER_USER_TYPE, Collections.emptyList());

            ActionMatch match = matchAction(fieldPlannerRecords, action, priorStatus);
            if (match == null) {
                // Logged once per (action, priorStatus) per request, not once per activity facility:
                // ActivityAssignmentConsumer drives SCHEDULED and ASSIGN_FIELD_STAFF through the bulk
                // path for every facility of a new field plan, and none of those system transitions
                // is a tracked business event, so a per-facility log would bury the real events.
                if (ctx.firstSkip(action, priorStatus)) {
                    log.info("Activity analytics: no matching {} record for action={} priorStatus={} "
                                    + "(e.g. activityFacilityId={}), skipping event(s)",
                            MDMS_MASTER_FIELD_PLANNER, action, priorStatus, activityFacilityId);
                }
                return;
            }

            // system_role: the user's role that is listed in the matched action's action_roles.
            String systemRole = match.actionRoles.stream()
                    .filter(userRoleCodes::contains)
                    .findFirst()
                    .orElse(null);
            UserType userType = resolveUserTypeBySystemRole(userTypeRecords, systemRole, userRoleCodes);

            Facility facility = activityFacility.getFacility();
            String boundaryCode = (facility != null) ? facility.getBoundaryCode() : null;

            UserAnalyticsEvent event = UserAnalyticsEvent.builder()
                    .eventId(UUID.randomUUID().toString())
                    .eventType(match.eventType)
                    .eventTime(Instant.now().toString())
                    .application(match.application)
                    .user(user)
                    .systemRole(systemRole)
                    .primaryRole(userType.primaryRole)
                    .userCategory(userType.userCategory)
                    .state(resolveState(boundaryCode, requestInfo, ctx, activityFacilityId))
                    .module(ANALYTICS_MODULE_FIELD_PLANNER)
                    .entityId(activityFacilityId)
                    .entityType(ANALYTICS_ENTITY_TYPE_ACTIVITY_FACILITY)
                    .build();
            producer.push(configs.getUserAnalyticsTopic(), event);

            log.info("Activity analytics: published event_type={} for activityFacilityId={} "
                    + "(action={}, priorStatus={})", match.eventType, activityFacilityId, action, priorStatus);
        } catch (Exception e) {
            log.error("Activity analytics: failed to publish event for activityFacilityId={} action={}",
                    activityFacilityId, action, e);
        }
    }

    /**
     * Finds the USER_ANALYTICS.FIELD_PLANNER record for a workflow action. Among the active records
     * naming the action, one that lists {@code priorStatus} in its {@code prior_status} wins;
     * otherwise the record with no {@code prior_status} is the fallback. That is how one action
     * (SUBMIT_REPORT_A) yields SUBMITTED out of DRAFT_IN_PROGRESS but RESUBMITTED out of
     * REJECTED_BY_FIELD_SUPERVISOR, without hardcoding either status here.
     */
    private ActionMatch matchAction(List<Map<String, Object>> fieldPlannerRecords, String action, String priorStatus) {
        ActionMatch fallback = null;
        for (Map<String, Object> record : fieldPlannerRecords) {
            if (!isActive(record)) {
                continue;
            }
            String eventType = asString(record.get("action_analytics"));
            if (eventType == null) {
                continue;
            }
            String application = asString(record.get("application"));
            if (application == null || application.isBlank()) {
                application = ANALYTICS_APPLICATION_DEFAULT;
            }
            for (Map<String, Object> workflow : asMapList(record.get("action_workflow"))) {
                if (!action.equalsIgnoreCase(asString(workflow.get("action_name")))) {
                    continue;
                }
                List<String> actionRoles = asStringList(workflow.get("action_roles"));
                List<String> priorStatuses = asStringList(record.get("prior_status"));
                if (priorStatuses.isEmpty()) {
                    if (fallback == null) {
                        fallback = new ActionMatch(eventType, application, actionRoles);
                    }
                } else if (priorStatus != null
                        && priorStatuses.stream().anyMatch(priorStatus::equalsIgnoreCase)) {
                    // A status-qualified record is more specific than the fallback — take it now.
                    return new ActionMatch(eventType, application, actionRoles);
                }
            }
        }
        return fallback;
    }

    /**
     * primary_role + user_category resolved from the action-derived system role, matching
     * {@code SemAnalyticsService} and {@code AmcAnalyticsService}:
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
                result.primaryRole = asString(record.get("program_role"));
                result.userCategory = asString(record.get("user_category"));
                break;
            }
        }
        return result;
    }

    /**
     * Resolves the localized state name for a facility boundary code, memoizing per state code in
     * the context so a bulk workflow update costs one localization call per distinct state.
     */
    private String resolveState(String boundaryCode, RequestInfo requestInfo, AnalyticsContext context,
                                String activityFacilityId) {
        String stateBoundaryCode = extractStateBoundaryCode(boundaryCode);
        if (stateBoundaryCode == null) {
            log.info("Activity analytics: no state segment in boundaryCode={} for activityFacilityId={}, "
                    + "state will be null", boundaryCode, activityFacilityId);
            return null;
        }
        // Not computeIfAbsent: a failed localization is null, and we want to cache that too rather
        // than retry the lookup for every activity facility in the state.
        if (!context.stateNameByCode.containsKey(stateBoundaryCode)) {
            context.stateNameByCode.put(stateBoundaryCode, localizeStateName(stateBoundaryCode, requestInfo));
        }
        return context.stateNameByCode.get(stateBoundaryCode);
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
                log.warn("Activity analytics: localization not configured; state will be null");
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
                log.warn("Activity analytics: localization search returned no body for {}", code);
                return null;
            }

            String message = extractMessage(response.getBody(), code);
            if (message == null) {
                log.warn("Activity analytics: no localization for {} in module {} at tenant {}",
                        code, BOUNDARY_LOCALIZATION_MODULE, LOCALIZATION_TENANT_ID);
            }
            return message;
        } catch (Exception e) {
            log.warn("Activity analytics: localization lookup failed for {}: {}", code, e.getMessage());
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
            log.warn("Activity analytics: failed to parse localization response: {}", e.getMessage());
        }
        return null;
    }

    /**
     * Derives the State-level boundary code from the facility's boundary code, no boundary-service
     * call needed. Boundary codes are hierarchy paths such as
     * {@code India_ArunachalPradesh_PapumPare_Doimukh}, so the State code is the
     * {@code India_<State>} prefix — the same derivation {@code AmcAnalyticsService},
     * {@code FacilityAnalyticsService} and {@code ProjectAnalyticsService} use. Returns null when
     * the code carries no usable state segment.
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
        if (!(value instanceof List)) {
            return Collections.emptyList();
        }
        return ((List<Object>) value).stream()
                .filter(Map.class::isInstance)
                .map(entry -> (Map<String, Object>) entry)
                .toList();
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

    /**
     * Memo shared across one API call. The bulk workflow endpoint transitions many activity
     * facilities in one request, and without this each one would repeat the MDMS fetch and the
     * localization lookup.
     */
    public static class AnalyticsContext {
        private final Map<String, Map<String, List<Map<String, Object>>>> mastersByTenant = new HashMap<>();
        private final Map<String, String> stateNameByCode = new HashMap<>();
        private final Set<String> loggedSkips = new LinkedHashSet<>();

        private Map<String, List<Map<String, Object>>> masters(RequestInfo requestInfo, String tenantId,
                                                               MDMSUtils mdmsUtils) {
            return mastersByTenant.computeIfAbsent(tenantId,
                    tenant -> mdmsUtils.fetchUserAnalyticsMasters(requestInfo, tenant));
        }

        /** True the first time an untracked (action, priorStatus) pair is seen in this request. */
        private boolean firstSkip(String action, String priorStatus) {
            return loggedSkips.add(action + "|" + priorStatus);
        }
    }

    /** The event type, source application and action roles of a matched FIELD_PLANNER record. */
    private static class ActionMatch {
        private final String eventType;
        private final String application;
        private final List<String> actionRoles;

        private ActionMatch(String eventType, String application, List<String> actionRoles) {
            this.eventType = eventType;
            this.application = application;
            this.actionRoles = actionRoles;
        }
    }

    /** The two role-derived fields resolved together from a single USER_TYPE record. */
    private static class UserType {
        private String primaryRole;
        private String userCategory;
    }
}
