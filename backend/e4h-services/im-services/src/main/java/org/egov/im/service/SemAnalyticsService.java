package org.egov.im.service;

import com.jayway.jsonpath.JsonPath;
import lombok.extern.slf4j.Slf4j;
import org.egov.common.contract.request.RequestInfo;
import org.egov.common.contract.request.Role;
import org.egov.common.contract.request.User;
import org.egov.im.config.IMConfiguration;
import org.egov.im.producer.Producer;
import org.egov.im.util.MDMSUtils;
import org.egov.im.web.models.Boundary;
import org.egov.im.web.models.Incident;
import org.egov.im.web.models.IncidentRequest;
import org.egov.im.web.models.LocalizationResponse;
import org.egov.im.web.models.UserAnalyticsEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.time.Instant;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import static org.egov.im.util.IMConstants.BOUNDARY_LOCALIZATION_MODULE;
import static org.egov.im.util.IMConstants.NOTIFICATION_LOCALE;
import static org.egov.im.util.IMConstants.SEM_APPLICATION;
import static org.egov.im.util.IMConstants.SEM_ENTITY_TYPE;
import static org.egov.im.util.IMConstants.SEM_JSONPATH;
import static org.egov.im.util.IMConstants.USER_TYPE_JSONPATH;

/**
 * Builds and publishes a {@link UserAnalyticsEvent} to Kafka on every incident
 * create/update. The indexer consumes this topic to build the SEM user-analytics index.
 * <p>
 * The event type and system role are derived from the {@code USER_ANALYTICS.SEM} master by
 * matching the request's workflow action; the primary role and user category are derived from
 * the {@code USER_ANALYTICS.USER_TYPE} master by matching the acting user's roles.
 */
@Service
@Slf4j
public class SemAnalyticsService {

    private final IMConfiguration config;
    private final MDMSUtils mdmsUtils;
    private final Producer producer;
    private final LocalizationService localizationService;

    @Autowired
    public SemAnalyticsService(IMConfiguration config, MDMSUtils mdmsUtils, Producer producer,
                                LocalizationService localizationService) {
        this.config = config;
        this.mdmsUtils = mdmsUtils;
        this.producer = producer;
        this.localizationService = localizationService;
    }

    /**
     * Publishes a SEM analytics event for the given incident request. Any failure here is
     * swallowed (logged) so it can never break the incident create/update flow.
     */
    public void publishEvent(IncidentRequest request, Boundary boundary) {
        try {
            if (request == null || request.getIncident() == null) {
                return;
            }
            RequestInfo requestInfo = request.getRequestInfo();
            Incident incident = request.getIncident();
            String tenantId = incident.getTenantId();

            String action = (request.getWorkflow() != null) ? request.getWorkflow().getAction() : null;
            if (action == null || action.isBlank()) {
                log.info("SEM analytics: no workflow action on request for incidentId={}, skipping event",
                        incident.getIncidentId());
                return;
            }

            User user = (requestInfo != null) ? requestInfo.getUserInfo() : null;
            Set<String> userRoleCodes = extractUserRoleCodes(user);

            // Fetch both SEM and USER_TYPE masters in a single MDMS call.
            Object mdmsData = mdmsUtils.getUserAnalyticsMDMSData(requestInfo, tenantId);

            List<Map<String, Object>> semRecords = readList(mdmsData, SEM_JSONPATH);
            List<Map<String, Object>> userTypeRecords = readList(mdmsData, USER_TYPE_JSONPATH);

            // Derive event_type (action_analytics) + the action_roles for the matched action.
            String eventType = null;
            List<String> actionRoles = Collections.emptyList();
            for (Map<String, Object> record : semRecords) {
                if (!isActive(record)) {
                    continue;
                }
                List<Map<String, Object>> workflows = asMapList(record.get("action_workflow"));
                for (Map<String, Object> wf : workflows) {
                    if (action.equalsIgnoreCase(asString(wf.get("action_name")))) {
                        eventType = asString(record.get("action_analytics"));
                        actionRoles = asStringList(wf.get("action_roles"));
                        break;
                    }
                }
                if (eventType != null) {
                    break;
                }
            }

            if (eventType == null) {
                log.info("SEM analytics: no matching SEM record for action={} incidentId={}, skipping event",
                        action, incident.getIncidentId());
                return;
            }

            // system_role: the user's role that is listed in the matched action's action_roles.
            String systemRole = actionRoles.stream()
                    .filter(userRoleCodes::contains)
                    .findFirst()
                    .orElse(null);

            // primary_role + user_category, resolved from the action-derived system role (A):
            //   1. Shortlist active USER_TYPE records whose system_roles contain A.
            //   2. Sort that shortlist by descending count of system_roles (most specific first).
            //   3. The first record whose system_roles are ALL held by the user wins.
            String primaryRole = null;
            String userCategory = null;
            if (systemRole != null) {
                List<Map<String, Object>> candidates = userTypeRecords.stream()
                        .filter(this::isActive)
                        .filter(record -> asStringList(record.get("system_roles")).contains(systemRole))
                        .sorted(Comparator.comparingInt(
                                (Map<String, Object> record) -> asStringList(record.get("system_roles")).size()).reversed())
                        .toList();
                for (Map<String, Object> record : candidates) {
                    List<String> systemRoles = asStringList(record.get("system_roles"));
                    if (userRoleCodes.containsAll(systemRoles)) {
                        primaryRole = asString(record.get("program_role"));
                        userCategory = asString(record.get("user_category"));
                        break;
                    }
                }
            }

            String state = resolveState(requestInfo, tenantId, boundary, incident.getIncidentId());

            UserAnalyticsEvent event = UserAnalyticsEvent.builder()
                    .eventId(UUID.randomUUID().toString())
                    .eventType(eventType)
                    .eventTime(Instant.now().toString())
                    .application(SEM_APPLICATION)
                    .user(user)
                    .systemRole(systemRole)
                    .primaryRole(primaryRole)
                    .userCategory(userCategory)
                    .state(state)
                    .module(null)
                    .entityId(incident.getIncidentId())
                    .entityType(SEM_ENTITY_TYPE)
                    .build();

            producer.push(tenantId, config.getUserAnalyticsTopic(), event);
            log.info("SEM analytics: published event_type={} for incidentId={}", eventType, incident.getIncidentId());
        } catch (Exception e) {
            log.error("SEM analytics: failed to publish event for incidentId={}",
                    request != null && request.getIncident() != null ? request.getIncident().getIncidentId() : null, e);
        }
    }

    /**
     * Resolves the localized state name exactly the way the indexer flow resolves it in
     * {@link LocalizationService#enrichLocalizedFieldsForIndexing}: look up
     * {@code Boundary_<boundary.stateCode>} in the {@code rainmaker-in} module and return the
     * localized message. Best-effort — returns null on any failure so it never blocks the rest
     * of the analytics event.
     */
    private String resolveState(RequestInfo requestInfo, String tenantId, Boundary boundary, String incidentId) {
        try {
            if (boundary == null || boundary.getStateCode() == null) {
                log.info("SEM analytics: no boundary stateCode available for incidentId={}, state will be null",
                        incidentId);
                return null;
            }
            String stateTenant = tenantId.split("\\.")[0];
            String stateCode = "Boundary_" + boundary.getStateCode();
            LocalizationResponse boundaryResponse = localizationService.getLocalizationMessages(
                    requestInfo, stateTenant, BOUNDARY_LOCALIZATION_MODULE, NOTIFICATION_LOCALE, stateCode);
            return (boundaryResponse != null) ? boundaryResponse.getMessageByCode(stateCode) : null;
        } catch (Exception e) {
            log.warn("SEM analytics: failed to resolve localized state for incidentId={}", incidentId, e);
            return null;
        }
    }

    private Set<String> extractUserRoleCodes(User user) {
        if (user == null || CollectionUtils.isEmpty(user.getRoles())) {
            return Collections.emptySet();
        }
        return user.getRoles().stream()
                .map(Role::getCode)
                .filter(Objects::nonNull)
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    @SuppressWarnings("unchecked")
    private List<Map<String, Object>> readList(Object mdmsData, String jsonPath) {
        try {
            Object result = JsonPath.read(mdmsData, jsonPath);
            if (result instanceof List) {
                return (List<Map<String, Object>>) result;
            }
        } catch (Exception e) {
            log.warn("SEM analytics: unable to read MDMS path {}", jsonPath);
        }
        return Collections.emptyList();
    }

    @SuppressWarnings("unchecked")
    private List<Map<String, Object>> asMapList(Object value) {
        return (value instanceof List) ? (List<Map<String, Object>>) value : Collections.emptyList();
    }

    @SuppressWarnings("unchecked")
    private List<String> asStringList(Object value) {
        return (value instanceof List) ? (List<String>) value : Collections.emptyList();
    }

    private String asString(Object value) {
        return (value != null) ? value.toString() : null;
    }

    private boolean isActive(Map<String, Object> record) {
        Object active = record.get("active");
        return active == null || Boolean.parseBoolean(active.toString());
    }
}
