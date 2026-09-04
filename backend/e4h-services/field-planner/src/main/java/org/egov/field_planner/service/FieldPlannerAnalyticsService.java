package org.egov.field_planner.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.egov.common.contract.request.RequestInfo;
import org.egov.common.contract.request.Role;
import org.egov.common.contract.request.User;
import org.egov.common.producer.Producer;
import org.egov.field_planner.config.FieldPlannerConfiguration;
import org.egov.field_planner.util.MDMSUtils;
import org.egov.field_planner.web.models.FieldPlan;
import org.egov.field_planner.web.models.UserAnalyticsEvent;
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

import static org.egov.field_planner.util.FieldPlannerConstants.ANALYTICS_APPLICATION;
import static org.egov.field_planner.util.FieldPlannerConstants.ANALYTICS_ENTITY_TYPE_FIELD_PLAN;
import static org.egov.field_planner.util.FieldPlannerConstants.ANALYTICS_ENTITY_TYPE_ICC_REPORT;
import static org.egov.field_planner.util.FieldPlannerConstants.ANALYTICS_EVENT_FIELD_PLAN_CREATE;
import static org.egov.field_planner.util.FieldPlannerConstants.ANALYTICS_EVENT_ICC_REPORT_UPLOAD;
import static org.egov.field_planner.util.FieldPlannerConstants.ANALYTICS_MODULE_FIELD_PLANNER;
import static org.egov.field_planner.util.FieldPlannerConstants.BOUNDARY_LOCALIZATION_MODULE;
import static org.egov.field_planner.util.FieldPlannerConstants.GEOGRAPHY_DETAILS_STATE;
import static org.egov.field_planner.util.FieldPlannerConstants.LOCALIZATION_LOCALE;
import static org.egov.field_planner.util.FieldPlannerConstants.LOCALIZATION_TENANT_ID;
import static org.egov.field_planner.util.FieldPlannerConstants.MDMS_MASTER_USER_TYPE;
import static org.egov.field_planner.util.FieldPlannerConstants.SCHEDULED_STATUS;
import static org.egov.field_planner.util.FieldPlannerConstants.TENANTID;
import static org.egov.field_planner.util.FieldPlannerConstants.USER_ANALYTICS_MODULE;

/**
 * Builds and publishes {@link UserAnalyticsEvent}s to the shared {@code user-analytics-event}
 * topic, so they land in the same user-analytics-report index as the SEM events emitted by
 * im-services, the facility events emitted by health-facility-registry, the boundary events emitted
 * by boundary-service, the AMC events emitted by amc-scheduler-service, the project events emitted
 * by project and the installation-report events emitted by field-planner-activity. All events from
 * this producer carry {@code module = FIELD_PLANNER} and {@code application = MANAGEMENT_HUB} —
 * field plans are authored there, unlike the Field Assist report submissions that
 * {@code ActivityAnalyticsService} covers.
 *
 * <p>Two entry points, one per instrumented endpoint:
 * <ul>
 *   <li>{@link #publishScheduledEvent} — one FIELD_PLAN_CREATE for the
 *   {@code /field-plan/v1/_update} call that moves a plan into SCHEDULED, the point staffing and
 *   facilities are locked in and the activities are created downstream. This is the only place a
 *   field plan counts as created for the report: {@code /field-plan/v1/_create} merely drafts a
 *   plan, and a draft is edited repeatedly before it goes live exactly once, so the create endpoint
 *   itself is deliberately not instrumented.</li>
 *   <li>{@link #publishIccReportUploadEvent} — one ICC_REPORT_UPLOAD per
 *   {@code /field-plan/v1/icc-report/upload}.</li>
 * </ul>
 *
 * <p>There is no workflow action on any of these endpoints, so {@code event_type} is fixed per
 * entry point rather than looked up from an MDMS action master. primary_role / user_category /
 * system_role are resolved from the {@code USER_ANALYTICS.USER_TYPE} master by best-match against
 * the acting user's roles — active records are sorted by descending system_roles count (most
 * specific first) and the first record whose system_roles the user fully holds wins, the same
 * lookup {@code ProjectAnalyticsService} uses.
 *
 * <p>{@code state} is localized as {@code Boundary_<stateCode>} in {@code rainmaker-in}, the same
 * key the other producers use, so all producers write identical state strings into the shared
 * index. The State code is parsed straight out of the plan's {@code geographyDetails.state}
 * boundary code rather than fetched from the boundary service — see
 * {@link #extractStateBoundaryCode}. An ICC report is a state-agnostic template upload, so its
 * events carry no state.
 *
 * <p>Every entry point is best-effort — any failure is logged and swallowed so analytics can never
 * break a field plan update or an ICC report upload.
 */
@Service
@Slf4j
public class FieldPlannerAnalyticsService {

    private final FieldPlannerConfiguration configs;
    private final MDMSUtils mdmsUtils;
    private final Producer producer;
    private final RestTemplate restTemplate;
    private final ObjectMapper mapper;

    public FieldPlannerAnalyticsService(FieldPlannerConfiguration configs, MDMSUtils mdmsUtils, Producer producer,
                                        RestTemplate restTemplate, @Qualifier("objectMapper") ObjectMapper mapper) {
        this.configs = configs;
        this.mdmsUtils = mdmsUtils;
        this.producer = producer;
        this.restTemplate = restTemplate;
        this.mapper = mapper;
    }

    /**
     * Publishes one FIELD_PLAN_CREATE event for the update that moves a plan into SCHEDULED, and
     * nothing at all for any other update. This is the service's only field-plan event: a plan is
     * edited repeatedly while in DRAFT and those edits are not business events; going live happens
     * exactly once and is. The create endpoint publishes nothing, so a plan yields exactly one
     * FIELD_PLAN_CREATE over its lifetime, at the moment it is scheduled.
     * <p>
     * Called from the update flow rather than the controller, so a request that matches no plan in
     * the DB — or one that never reaches the persister push — publishes nothing either.
     *
     * @param fieldPlan       the plan as it came in on the request, i.e. with the new status
     * @param fieldPlanFromDB the plan as it was before the update, used as the geography fallback
     *                        when the request omits geographyDetails
     * @param priorStatus     the plan's status <em>before</em> the update, so an update that leaves
     *                        an already-SCHEDULED plan scheduled does not re-emit
     */
    public void publishScheduledEvent(RequestInfo requestInfo, FieldPlan fieldPlan, FieldPlan fieldPlanFromDB,
                                      String priorStatus) {
        String fieldPlanId = (fieldPlan != null) ? fieldPlan.getId() : null;
        try {
            if (fieldPlan == null || fieldPlanId == null) {
                log.info("Field planner analytics: no field plan id on update, skipping event");
                return;
            }
            if (!SCHEDULED_STATUS.equalsIgnoreCase(fieldPlan.getStatus())
                    || SCHEDULED_STATUS.equalsIgnoreCase(priorStatus)) {
                log.debug("Field planner analytics: fieldPlanId={} did not move into {} "
                                + "(priorStatus={}, status={}), no event",
                        fieldPlanId, SCHEDULED_STATUS, priorStatus, fieldPlan.getStatus());
                return;
            }

            String tenantId = (fieldPlan.getTenantId() != null) ? fieldPlan.getTenantId() : TENANTID;
            UserType userType = resolveUserType(requestInfo, tenantId,
                    extractUserRoleCodes(userOf(requestInfo)));
            // The update payload may carry only the changed fields, so fall back to the stored plan
            // for the geography rather than dropping the state.
            String stateBoundaryCode = geographyStateCode(fieldPlan);
            if (stateBoundaryCode == null) {
                stateBoundaryCode = geographyStateCode(fieldPlanFromDB);
            }

            publish(requestInfo, ANALYTICS_EVENT_FIELD_PLAN_CREATE, fieldPlanId,
                    ANALYTICS_ENTITY_TYPE_FIELD_PLAN, userType,
                    resolveState(stateBoundaryCode, requestInfo, fieldPlanId));
            log.info("Field planner analytics: published {} event for fieldPlanId={} (priorStatus={})",
                    ANALYTICS_EVENT_FIELD_PLAN_CREATE, fieldPlanId, priorStatus);
        } catch (Exception e) {
            log.error("Field planner analytics: failed to publish {} event for fieldPlanId={}",
                    ANALYTICS_EVENT_FIELD_PLAN_CREATE, fieldPlanId, e);
        }
    }

    /**
     * Publishes one ICC_REPORT_UPLOAD event per uploaded ICC report template. The upload carries no
     * boundary — an ICC report is keyed by system type and capacity, not by geography — so
     * {@code state} is left null and the MDMS lookup runs at the national tenant.
     *
     * @param iccReportId the id generated for the stored ICC report
     */
    public void publishIccReportUploadEvent(RequestInfo requestInfo, String iccReportId) {
        try {
            UserType userType = resolveUserType(requestInfo, TENANTID,
                    extractUserRoleCodes(userOf(requestInfo)));
            publish(requestInfo, ANALYTICS_EVENT_ICC_REPORT_UPLOAD, iccReportId,
                    ANALYTICS_ENTITY_TYPE_ICC_REPORT, userType, null);
            log.info("Field planner analytics: published {} event for iccReportId={}",
                    ANALYTICS_EVENT_ICC_REPORT_UPLOAD, iccReportId);
        } catch (Exception e) {
            log.error("Field planner analytics: failed to publish {} event for iccReportId={}",
                    ANALYTICS_EVENT_ICC_REPORT_UPLOAD, iccReportId, e);
        }
    }

    /** Assembles and pushes the event. All entry points funnel through here so the shape is one place. */
    private void publish(RequestInfo requestInfo, String eventType, String entityId, String entityType,
                         UserType userType, String state) {
        UserAnalyticsEvent event = UserAnalyticsEvent.builder()
                .eventId(UUID.randomUUID().toString())
                .eventType(eventType)
                .eventTime(Instant.now().toString())
                .application(ANALYTICS_APPLICATION)
                .user(userOf(requestInfo))
                .systemRole(userType.systemRole)
                .primaryRole(userType.primaryRole)
                .userCategory(userType.userCategory)
                .state(state)
                .module(ANALYTICS_MODULE_FIELD_PLANNER)
                .entityId(entityId)
                .entityType(entityType)
                .build();
        producer.push(configs.getUserAnalyticsTopic(), event);
    }

    /** The state boundary code out of a field plan's geographyDetails, or null when absent. */
    private String geographyStateCode(FieldPlan fieldPlan) {
        Map<String, Object> geographyDetails = (fieldPlan != null) ? fieldPlan.getGeographyDetails() : null;
        return (geographyDetails != null) ? asString(geographyDetails.get(GEOGRAPHY_DETAILS_STATE)) : null;
    }

    /** Resolves the localized state name for a boundary code, or null when it carries no state. */
    private String resolveState(String boundaryCode, RequestInfo requestInfo, String entityId) {
        String stateBoundaryCode = extractStateBoundaryCode(boundaryCode);
        if (stateBoundaryCode == null) {
            log.info("Field planner analytics: no state segment in boundaryCode={} for entityId={}, "
                    + "state will be null", boundaryCode, entityId);
            return null;
        }
        return localizeStateName(stateBoundaryCode, requestInfo);
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
                log.warn("Field planner analytics: localization not configured; state will be null");
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
                log.warn("Field planner analytics: localization search returned no body for {}", code);
                return null;
            }

            String message = extractMessage(response.getBody(), code);
            if (message == null) {
                log.warn("Field planner analytics: no localization for {} in module {} at tenant {}",
                        code, BOUNDARY_LOCALIZATION_MODULE, LOCALIZATION_TENANT_ID);
            }
            return message;
        } catch (Exception e) {
            log.warn("Field planner analytics: localization lookup failed for {}: {}", code, e.getMessage());
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
            log.warn("Field planner analytics: failed to parse localization response: {}", e.getMessage());
        }
        return null;
    }

    /**
     * Derives the State-level boundary code from a field plan's geography state code, no
     * boundary-service call needed. Boundary codes are hierarchy paths such as
     * {@code India_ArunachalPradesh_PapumPare_Doimukh}, so the State code is the
     * {@code India_<State>} prefix — the same derivation {@code AmcAnalyticsService},
     * {@code FacilityAnalyticsService}, {@code ProjectAnalyticsService} and
     * {@code ActivityAnalyticsService} use. Returns null when the code carries no usable state
     * segment.
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
                log.info("Field planner analytics: no {} match for roles {} in tenant {}",
                        MDMS_MASTER_USER_TYPE, userRoleCodes, tenantId);
            }
        } catch (Exception e) {
            log.warn("Field planner analytics: failed to resolve {} for tenant {}: {}",
                    MDMS_MASTER_USER_TYPE, tenantId, e.getMessage());
        }
        return result;
    }

    private User userOf(RequestInfo requestInfo) {
        return (requestInfo != null) ? requestInfo.getUserInfo() : null;
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
