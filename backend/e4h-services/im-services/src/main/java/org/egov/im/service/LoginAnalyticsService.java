package org.egov.im.service;

import com.jayway.jsonpath.JsonPath;
import lombok.extern.slf4j.Slf4j;
import org.egov.common.contract.request.RequestInfo;
import org.egov.common.contract.request.Role;
import org.egov.im.config.IMConfiguration;
import org.egov.im.producer.Producer;
import org.egov.im.util.MDMSUtils;
import org.egov.im.web.models.LocalizationResponse;
import org.egov.im.web.models.User;
import org.egov.im.web.models.UserAnalyticsEvent;
import org.egov.im.web.models.UserRequest;
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
import static org.egov.im.util.IMConstants.USER_LOGIN_ENTITY_TYPE;
import static org.egov.im.util.IMConstants.USER_LOGIN_EVENT_TYPE;
import static org.egov.im.util.IMConstants.USER_TYPE_JSONPATH;

/**
 * Publishes a {@code USER_LOGIN} {@link UserAnalyticsEvent} to the shared
 * {@code user-analytics-event} topic on every {@code /user/login/_report} call, so logins land in
 * the same user-analytics-report index as the SEM ticket events, facility, boundary, AMC and
 * project events.
 * <p>
 * Unlike {@link SemAnalyticsService} this fires for every reported login regardless of role — the
 * {@code application} the caller declares on the request ({@code SAURA_EMITRA}, {@code
 * FIELD_ASSIST} or {@code MANAGEMENT_HUB}) is what distinguishes the source, and Management Hub
 * users hold none of the SEM roles the login report itself is gated on. That field is optional, so
 * a caller that has not been updated yet still produces an event, just with a null application.
 * <p>
 * A login carries no workflow action, so {@code event_type} is the fixed {@code USER_LOGIN} and
 * system_role / primary_role / user_category are resolved purely by best-match against the
 * {@code USER_ANALYTICS.USER_TYPE} master — the same derivation
 * {@code BoundaryAnalyticsService#resolveUserType} uses: active records whose {@code system_roles}
 * the user fully holds, most specific (largest {@code system_roles}) first.
 * <p>
 * {@code state} is localized as {@code Boundary_<stateCode>} in {@code rainmaker-in}, the same key
 * every other producer uses, so all of them write identical state strings into the shared index.
 * The state code is derived from the caller-resolved HRMS jurisdiction boundary code rather than
 * the raw {@code state} on the login report, which is a bare boundary-code segment.
 * <p>
 * Every entry point is best-effort — any failure is logged and swallowed so analytics can never
 * break a login.
 */
@Service
@Slf4j
public class LoginAnalyticsService {

    private final IMConfiguration config;
    private final MDMSUtils mdmsUtils;
    private final Producer producer;
    private final LocalizationService localizationService;

    @Autowired
    public LoginAnalyticsService(IMConfiguration config, MDMSUtils mdmsUtils, Producer producer,
                                 LocalizationService localizationService) {
        this.config = config;
        this.mdmsUtils = mdmsUtils;
        this.producer = producer;
        this.localizationService = localizationService;
    }

    /**
     * Publishes the login event for the given request.
     *
     * @param userRequest  the login report request; its {@code application} names the front-end
     * @param boundaryCode the user's HRMS jurisdiction boundary code, already resolved by the
     *                     caller so HRMS is hit at most once per login. May be null, in which case
     *                     {@code state} is left null.
     */
    public void publishLoginEvent(UserRequest userRequest, String boundaryCode) {
        String userName = null;
        try {
            if (userRequest == null || userRequest.getUser() == null) {
                return;
            }
            User user = userRequest.getUser();
            userName = user.getUserName();
            String tenantId = user.getTenantId();
            if (tenantId == null || tenantId.isBlank()) {
                log.info("Login analytics: no tenantId for user {}, skipping event", userName);
                return;
            }

            RequestInfo requestInfo = userRequest.getRequestInfo();
            Set<String> userRoleCodes = extractUserRoleCodes(user);
            UserType userType = resolveUserType(requestInfo, tenantId, userRoleCodes);

            UserAnalyticsEvent event = UserAnalyticsEvent.builder()
                    .eventId(UUID.randomUUID().toString())
                    .eventType(USER_LOGIN_EVENT_TYPE)
                    .eventTime(Instant.now().toString())
                    .application(userRequest.getApplication())
                    .user(toContractUser(user))
                    .systemRole(userType.systemRole)
                    .primaryRole(userType.primaryRole)
                    .userCategory(userType.userCategory)
                    .state(resolveState(requestInfo, tenantId, boundaryCode, userName))
                    .module(null)
                    .entityId(user.getUuid())
                    .entityType(USER_LOGIN_ENTITY_TYPE)
                    .build();

            producer.push(tenantId, config.getUserAnalyticsTopic(), event);
            log.info("Login analytics: published {} event for user {} from application {}",
                    USER_LOGIN_EVENT_TYPE, userName, userRequest.getApplication());
        } catch (Exception e) {
            log.error("Login analytics: failed to publish {} event for user {}",
                    USER_LOGIN_EVENT_TYPE, userName, e);
        }
    }

    /**
     * The login report's own {@code User} is a module-local model; the event schema shared with the
     * other producers carries the platform contract {@code User}, whose fields it mirrors one for
     * one apart from {@code active}. Field names must stay aligned because the indexer masks
     * {@code $.user.mobileNumber} and {@code $.user.emailId}.
     */
    private org.egov.common.contract.request.User toContractUser(User user) {
        return org.egov.common.contract.request.User.builder()
                .id(user.getId())
                .userName(user.getUserName())
                .name(user.getName())
                .type(user.getType())
                .mobileNumber(user.getMobileNumber())
                .emailId(user.getEmailId())
                .roles(user.getRoles())
                .tenantId(user.getTenantId())
                .uuid(user.getUuid())
                .build();
    }

    /**
     * Localizes {@code Boundary_<stateBoundaryCode>} in {@code rainmaker-in} at the state tenant,
     * matching {@link SemAnalyticsService#resolveState}. Best-effort — returns null on any failure
     * so it never blocks the rest of the event.
     */
    private String resolveState(RequestInfo requestInfo, String tenantId, String boundaryCode, String userName) {
        try {
            String stateBoundaryCode = extractStateBoundaryCode(boundaryCode);
            if (stateBoundaryCode == null) {
                log.info("Login analytics: no state segment in boundary code {} for user {}, "
                        + "state will be null", boundaryCode, userName);
                return null;
            }
            String stateTenant = tenantId.split("\\.")[0];
            String code = "Boundary_" + stateBoundaryCode;
            LocalizationResponse response = localizationService.getLocalizationMessages(
                    requestInfo, stateTenant, BOUNDARY_LOCALIZATION_MODULE, NOTIFICATION_LOCALE, code);
            return (response != null) ? response.getMessageByCode(code) : null;
        } catch (Exception e) {
            log.warn("Login analytics: failed to resolve localized state for user {}", userName, e);
            return null;
        }
    }

    /**
     * Derives the State-level boundary code from an HRMS jurisdiction boundary code, no extra
     * lookup needed. Boundary codes are hierarchy paths such as
     * {@code India_ArunachalPradesh_PapumPare_Doimukh}, so the State code is the
     * {@code India_<State>} prefix — the same derivation
     * {@code BoundaryAnalyticsService#extractStateBoundaryCode} uses. Returns null when the code
     * carries no usable state segment.
     */
    private String extractStateBoundaryCode(String boundaryCode) {
        if (boundaryCode == null || boundaryCode.isBlank()) {
            return null;
        }
        String[] parts = boundaryCode.replace('.', '_').split("_");
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
            log.info("Login analytics: no roles on user, USER_TYPE fields will be null");
            return result;
        }
        try {
            Object mdmsData = mdmsUtils.getUserAnalyticsMDMSData(requestInfo, tenantId);
            List<Map<String, Object>> userTypeRecords = readList(mdmsData, USER_TYPE_JSONPATH);

            List<Map<String, Object>> candidates = userTypeRecords.stream()
                    .filter(this::isActive)
                    .filter(record -> !asStringList(record.get("system_roles")).isEmpty())
                    .filter(record -> userRoleCodes.containsAll(asStringList(record.get("system_roles"))))
                    .sorted(Comparator.comparingInt(
                            (Map<String, Object> record) -> asStringList(record.get("system_roles")).size()).reversed())
                    .toList();

            if (candidates.isEmpty()) {
                log.info("Login analytics: no USER_TYPE match for roles {} in tenant {}", userRoleCodes, tenantId);
                return result;
            }
            Map<String, Object> match = candidates.get(0);
            List<String> systemRoles = asStringList(match.get("system_roles"));
            // system_role: the matched record's role the user actually holds, in the user's role order.
            result.systemRole = userRoleCodes.stream().filter(systemRoles::contains).findFirst().orElse(null);
            result.primaryRole = asString(match.get("program_role"));
            result.userCategory = asString(match.get("user_category"));
        } catch (Exception e) {
            log.warn("Login analytics: failed to resolve USER_TYPE for tenant {}: {}", tenantId, e.getMessage());
        }
        return result;
    }

    private Set<String> extractUserRoleCodes(User user) {
        if (CollectionUtils.isEmpty(user.getRoles())) {
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
            log.warn("Login analytics: unable to read MDMS path {}", jsonPath);
        }
        return Collections.emptyList();
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
