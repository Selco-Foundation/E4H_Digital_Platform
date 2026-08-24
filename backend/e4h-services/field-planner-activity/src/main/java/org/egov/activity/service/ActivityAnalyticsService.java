package org.egov.activity.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.egov.activity.config.ActivityConfiguration;
import org.egov.activity.util.MDMSUtils;
import org.egov.activity.validator.ActivityValidator;
import org.egov.activity.web.models.ActivityAssignment;
import org.egov.activity.web.models.ActivityFacility;
import org.egov.activity.web.models.Facility;
import org.egov.activity.web.models.FieldPlan;
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
import static org.egov.activity.util.ActivityConstants.ANALYTICS_APPLICATION_MANAGEMENT_HUB;
import static org.egov.activity.util.ActivityConstants.ANALYTICS_ENTITY_TYPE_ACTIVITY_ASSIGNMENT;
import static org.egov.activity.util.ActivityConstants.ANALYTICS_ENTITY_TYPE_ACTIVITY_FACILITY;
import static org.egov.activity.util.ActivityConstants.ANALYTICS_EVENT_ACTIVITY_ASSIGNED;
import static org.egov.activity.util.ActivityConstants.ANALYTICS_MODULE_FIELD_PLANNER;
import static org.egov.activity.util.ActivityConstants.BOUNDARY_LOCALIZATION_MODULE;
import static org.egov.activity.util.ActivityConstants.GEOGRAPHY_DETAILS_STATE;
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
 * Two entry points:
 * <ul>
 *   <li>{@link #publishWorkflowEvent}, called once per successful
 *   {@code /activity/v1/activities/workflow/update} transition. The bulk endpoint
 *   {@code /activity/v1/activities/bulk/workflow/update} loops over that same method, so
 *   bulk-approved reports are counted too rather than silently disappearing — that loop shares one
 *   {@link AnalyticsContext} so a bulk approval of N reports still costs one MDMS call and one
 *   localization call per state, not N of each.</li>
 *   <li>{@link #publishAssignmentEvents}, called once per staffing row created by
 *   {@code /activity/v1/activities/_assign-activity}.</li>
 * </ul>
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
 * every facility of a new installation plan, and are deliberately left out of the master so they emit
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
    /**
     * Only for {@link ActivityValidator#getFieldPlanById}: an assignment carries a fieldPlanId but
     * not the plan itself, and the plan is where the state boundary code lives.
     */
    private final ActivityValidator activityValidator;

    public ActivityAnalyticsService(ActivityConfiguration configs, MDMSUtils mdmsUtils, Producer producer,
                                    RestTemplate restTemplate, @Qualifier("objectMapper") ObjectMapper mapper,
                                    ActivityValidator activityValidator) {
        this.configs = configs;
        this.mdmsUtils = mdmsUtils;
        this.producer = producer;
        this.restTemplate = restTemplate;
        this.mapper = mapper;
        this.activityValidator = activityValidator;
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
                // path for every facility of a new installation plan, and none of those system transitions
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
     * Publishes one ACTIVITY_ASSIGNED event per staffing row created by
     * {@code /activity/v1/activities/_assign-activity} — that endpoint is how a Management Hub user
     * puts a field staff member, field supervisor or QC reviewer on an installation plan, and one call
     * carries the whole roster, so staffing a plan with three people yields three events, each with
     * its own assignment id as {@code entity_id}.
     * <p>
     * There is no workflow action here to look up in the FIELD_PLANNER master, so {@code event_type}
     * is fixed and the role fields come from the plain USER_TYPE best-match against the acting
     * user's roles, the way {@code ProjectAnalyticsService} resolves them. Note these describe the
     * <em>assigner</em>, not the person being assigned — the roster row itself is the entity.
     * <p>
     * Best-effort per assignment: a failure on one row is logged and the rest still publish.
     *
     * @param assignments the assignments as enriched on create, i.e. with their ids set
     */
    public void publishAssignmentEvents(RequestInfo requestInfo, List<ActivityAssignment> assignments) {
        if (assignments == null || assignments.isEmpty()) {
            return;
        }
        // One memo for the whole roster: the MDMS masters, the installation plan and its localized state
        // are each resolved once even though a staffing call creates several assignments.
        AnalyticsContext ctx = new AnalyticsContext();
        User user = (requestInfo != null) ? requestInfo.getUserInfo() : null;
        Set<String> userRoleCodes = extractUserRoleCodes(user);
        String eventTime = Instant.now().toString();
        int published = 0;

        for (ActivityAssignment assignment : assignments) {
            if (assignment == null || assignment.getId() == null) {
                continue;
            }
            try {
                String tenantId = (assignment.getTenantId() != null) ? assignment.getTenantId() : TENANTID;
                List<Map<String, Object>> userTypeRecords = ctx.masters(requestInfo, tenantId, mdmsUtils)
                        .getOrDefault(MDMS_MASTER_USER_TYPE, Collections.emptyList());
                UserType userType = resolveUserTypeByUserRoles(userTypeRecords, userRoleCodes);

                UserAnalyticsEvent event = UserAnalyticsEvent.builder()
                        .eventId(UUID.randomUUID().toString())
                        .eventType(ANALYTICS_EVENT_ACTIVITY_ASSIGNED)
                        .eventTime(eventTime)
                        .application(ANALYTICS_APPLICATION_MANAGEMENT_HUB)
                        .user(user)
                        .systemRole(userType.systemRole)
                        .primaryRole(userType.primaryRole)
                        .userCategory(userType.userCategory)
                        .state(resolveAssignmentState(assignment, requestInfo, tenantId, ctx))
                        .module(ANALYTICS_MODULE_FIELD_PLANNER)
                        .entityId(assignment.getId())
                        .entityType(ANALYTICS_ENTITY_TYPE_ACTIVITY_ASSIGNMENT)
                        .build();
                producer.push(configs.getUserAnalyticsTopic(), event);
                published++;
            } catch (Exception e) {
                log.error("Activity analytics: failed to publish {} event for activityAssignmentId={}",
                        ANALYTICS_EVENT_ACTIVITY_ASSIGNED, assignment.getId(), e);
            }
        }
        log.info("Activity analytics: published {} {} event(s)", published, ANALYTICS_EVENT_ACTIVITY_ASSIGNED);
    }

    /**
     * Localized state for an assignment, taken from its installation plan's
     * {@code geographyDetails.state} — a boundary code such as {@code India_Karnataka}. The
     * assign-activity payload carries only a fieldPlanId, so the plan is fetched from field-planner
     * unless the caller already inlined it; both the fetch and the localization are memoized, and a
     * whole roster names one plan, so this costs one call per request.
     */
    private String resolveAssignmentState(ActivityAssignment assignment, RequestInfo requestInfo, String tenantId,
                                          AnalyticsContext context) {
        String stateBoundaryCode = geographyStateCode(assignment.getFieldPlan());
        String fieldPlanId = assignment.getFieldPlanId();
        if (stateBoundaryCode == null && fieldPlanId != null) {
            // Not computeIfAbsent: a plan we could not fetch maps to null, and that needs caching
            // too rather than re-fetching it for every assignment in the roster.
            if (!context.planStateCodeById.containsKey(fieldPlanId)) {
                context.planStateCodeById.put(fieldPlanId, fetchPlanStateCode(requestInfo, fieldPlanId, tenantId));
            }
            stateBoundaryCode = context.planStateCodeById.get(fieldPlanId);
        }
        return resolveState(stateBoundaryCode, requestInfo, context, assignment.getId());
    }

    /**
     * The plan's state boundary code from field-planner, or null if it cannot be fetched. Swallows
     * its own failures rather than letting them reach the caller's catch, so an unreachable
     * field-planner costs the event its state dimension instead of costing us the event.
     */
    private String fetchPlanStateCode(RequestInfo requestInfo, String fieldPlanId, String tenantId) {
        try {
            return geographyStateCode(activityValidator.getFieldPlanById(requestInfo, fieldPlanId, tenantId));
        } catch (Exception e) {
            log.warn("Activity analytics: could not fetch fieldPlanId={} for state, state will be null: {}",
                    fieldPlanId, e.getMessage());
            return null;
        }
    }

    /** The state boundary code out of an installation plan's geographyDetails, or null when absent. */
    private String geographyStateCode(FieldPlan fieldPlan) {
        Map<String, Object> geographyDetails = (fieldPlan != null) ? fieldPlan.getGeographyDetails() : null;
        return (geographyDetails != null) ? asString(geographyDetails.get(GEOGRAPHY_DETAILS_STATE)) : null;
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
     * primary_role / user_category / system_role for a flow with no workflow action to key off:
     * shortlist active USER_TYPE records whose system_roles the user fully holds, most specific
     * (largest system_roles) first, and take the first. Same lookup {@code ProjectAnalyticsService}
     * uses; {@link #resolveUserTypeBySystemRole} is the action-driven variant.
     */
    private UserType resolveUserTypeByUserRoles(List<Map<String, Object>> userTypeRecords, Set<String> userRoleCodes) {
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
            log.info("Activity analytics: no {} match for roles {}", MDMS_MASTER_USER_TYPE, userRoleCodes);
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
     * Resolves the localized state name for a boundary code, memoizing per state code in the
     * context so a bulk workflow update costs one localization call per distinct state.
     */
    private String resolveState(String boundaryCode, RequestInfo requestInfo, AnalyticsContext context,
                                String entityId) {
        String stateBoundaryCode = extractStateBoundaryCode(boundaryCode);
        if (stateBoundaryCode == null) {
            log.info("Activity analytics: no state segment in boundaryCode={} for entityId={}, "
                    + "state will be null", boundaryCode, entityId);
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
        /** State boundary code per installation plan id, so one staffing roster fetches its plan once. */
        private final Map<String, String> planStateCodeById = new HashMap<>();
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

    /**
     * The role-derived fields resolved together from a single USER_TYPE record. systemRole is only
     * filled by {@link #resolveUserTypeByUserRoles}; the workflow path derives it from the matched
     * action's action_roles instead.
     */
    private static class UserType {
        private String systemRole;
        private String primaryRole;
        private String userCategory;
    }
}
