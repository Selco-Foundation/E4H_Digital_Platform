package facility.util;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import facility.config.Configuration;
import facility.web.models.FacilityScope;
import facility.web.models.Jurisdiction;
import facility.web.models.Role;
import lombok.extern.slf4j.Slf4j;
import org.egov.common.contract.request.RequestInfo;
import org.egov.common.contract.request.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * Decides what the caller may see, per the role they say they are acting as.
 *
 * <h3>Why the acting role, and not the union of the caller's roles</h3>
 * One person can be a Facility Administrator in one state and a Project Manager in another. The
 * endpoint is the same from both screens, so without the caller naming a role the server can only
 * offer the union - which lets the fa screens reach pm's state. The header names the hat.
 *
 * <h3>Why a forged header cannot escalate</h3>
 * The claimed role is checked against the roles on the caller's own {@code RequestInfo}, which the
 * gateway re-attaches from the authenticated token. So the header can only ever <em>select among</em>
 * scopes the caller already holds. It can never acquire one.
 *
 * <h3>Fail closed</h3>
 * If HRMS or boundary-service cannot be reached, the request is refused rather than answered
 * unscoped. That is the opposite of the inbox's choice, and it is deliberate: an inbox showing too
 * much is a nuisance, a facility registry showing every facility in the country with decrypted POC
 * phone numbers is the thing this exists to prevent. The cost is real and is stated in the plan -
 * HRMS becomes a hard dependency of every facility picker in the platform.
 */
@Slf4j
@Component
public class RoleScopeResolver {

    public static final String X_ACTING_ROLE = "x-acting-role";

    /** Audit trail, on its own logger so it can be routed and queried apart from service logs. */
    private static final org.slf4j.Logger AUDIT = org.slf4j.LoggerFactory.getLogger("JURISDICTION_AUDIT");

    public enum ScopeMode {
        OFF, WARN, ENFORCE;

        static ScopeMode parse(String raw) {
            String value = raw == null ? "" : raw.trim().toUpperCase(Locale.ROOT);
            for (ScopeMode mode : values()) {
                if (mode.name().equals(value)) {
                    return mode;
                }
            }
            // Fail fast at startup. A typo'd "enforced" silently meaning "off" is the worst
            // possible outcome for a security control.
            throw new IllegalStateException("facility.jurisdiction.scope.mode must be one of "
                    + "off, warn or enforce - got '" + raw + "'");
        }
    }

    private final Configuration configs;
    private final HRMSUtils hrmsUtils;
    private final BoundaryUtil boundaryUtil;
    private final ScopeMode mode;
    private final Set<String> systemRoles;

    /** Caller uuid to their grants. Successful lookups only - see {@link #grantsFor}. */
    private final Cache<String, JurisdictionGrants> grantsByCaller;

    /**
     * One boundary code to the facility codes beneath it.
     *
     * Keyed on a single code rather than on a caller's whole grant set, so every user scoped to the
     * same state shares one entry and a state is expanded once per TTL across the platform.
     */
    private final Cache<String, Set<String>> facilitiesByBoundary;

    @Autowired
    public RoleScopeResolver(Configuration configs, HRMSUtils hrmsUtils, BoundaryUtil boundaryUtil) {
        this.configs = configs;
        this.hrmsUtils = hrmsUtils;
        this.boundaryUtil = boundaryUtil;
        this.mode = ScopeMode.parse(configs.getJurisdictionScopeMode());
        this.systemRoles = new HashSet<>(configs.getJurisdictionSystemRoles() == null
                ? java.util.Collections.emptyList()
                : configs.getJurisdictionSystemRoles());
        this.grantsByCaller = Caffeine.newBuilder()
                .expireAfterWrite(configs.getJurisdictionHrmsCacheTtlSeconds(), TimeUnit.SECONDS)
                .maximumSize(10_000)
                .recordStats()
                .build();
        this.facilitiesByBoundary = Caffeine.newBuilder()
                .expireAfterWrite(configs.getJurisdictionBoundaryCacheTtlSeconds(), TimeUnit.SECONDS)
                .maximumSize(5_000)
                .recordStats()
                .build();
        log.info("Facility jurisdiction scoping is {}", mode);
    }

    public ScopeMode mode() {
        return mode;
    }

    /** Call once per request, in the controller, and hand the result to both the search and the count. */
    public FacilityScope resolve(RequestInfo requestInfo, String actingRoleHeader) {
        if (mode == ScopeMode.OFF) {
            return FacilityScope.unscoped(FacilityScope.Decision.UNSCOPED_FLAG_OFF);
        }

        // Identity first, before the header is read: a service is unscoped whatever it claims.
        if (isSystemCaller(requestInfo)) {
            return FacilityScope.unscoped(FacilityScope.Decision.UNSCOPED_SYSTEM);
        }

        String uuid = callerUuid(requestInfo);
        if (uuid == null) {
            return settle(FacilityScope.Decision.DENY_MISSING_ACTING_ROLE, null, null, "no caller identity");
        }

        String actingRole = actingRoleHeader == null ? "" : actingRoleHeader.trim();
        if (actingRole.isEmpty()) {
            return settle(FacilityScope.Decision.DENY_MISSING_ACTING_ROLE, null, null, "no " + X_ACTING_ROLE);
        }
        if (!heldRoleCodes(requestInfo).contains(actingRole)) {
            return settle(FacilityScope.Decision.DENY_INVALID_ACTING_ROLE, actingRole, null, "role not held");
        }

        JurisdictionGrants grants = grantsFor(uuid, requestInfo);
        if (!grants.isOk()) {
            return settle(FacilityScope.Decision.DENY_SCOPE_UNAVAILABLE, actingRole, null,
                    "hrms=" + grants.status());
        }

        Set<String> ceiling = new LinkedHashSet<>();
        StringBuilder summary = new StringBuilder();
        for (Jurisdiction grant : grants.grants()) {
            if (!appliesTo(grant, actingRole)) {
                continue;
            }
            Set<String> beneath;
            try {
                beneath = expand(grant);
            } catch (Exception e) {
                log.error("Could not expand jurisdiction {} for caller {}", grant.getBoundary(), uuid, e);
                return settle(FacilityScope.Decision.DENY_SCOPE_UNAVAILABLE, actingRole, null, "boundary expansion failed");
            }
            ceiling.addAll(beneath);
            summary.append(summary.length() == 0 ? "" : ",")
                    .append(grant.getBoundaryType()).append(':').append(grant.getBoundary())
                    .append('(').append(beneath.size()).append(')');
        }

        if (ceiling.isEmpty()) {
            return settle(FacilityScope.Decision.DENY_NO_JURISDICTION, actingRole, ceiling, summary.toString());
        }
        return settle(FacilityScope.Decision.SCOPED, actingRole, ceiling, summary.toString());
    }

    /**
     * In warn mode every outcome is computed and logged but none is applied, so real traffic can be
     * classified before the control is switched on.
     */
    private FacilityScope settle(FacilityScope.Decision decision, String actingRole, Set<String> ceiling, String summary) {
        Set<String> resolved = ceiling == null ? java.util.Collections.emptySet() : ceiling;
        if (mode == ScopeMode.WARN) {
            AUDIT.info("would_be_decision={} acting_role={} ceiling_size={} grants={}",
                    decision, actingRole, resolved.size(), summary);
            return FacilityScope.warn(actingRole, resolved, summary);
        }
        if (decision == FacilityScope.Decision.SCOPED) {
            return FacilityScope.scoped(actingRole, resolved, summary);
        }
        return FacilityScope.deny(decision, actingRole);
    }

    /**
     * A grant with no roles applies to every role its holder has.
     *
     * That is the shape of every grant written before role scoping existed, so reading it the other
     * way round would empty the platform on the day this is switched on.
     */
    private boolean appliesTo(Jurisdiction grant, String actingRole) {
        List<Role> roles = grant.getRoles();
        if (roles == null || roles.isEmpty()) {
            return true;
        }
        for (Role role : roles) {
            if (role != null && actingRole.equals(role.getCode())) {
                return true;
            }
        }
        return false;
    }

    private Set<String> expand(Jurisdiction grant) throws Exception {
        String code = grant.getBoundary().trim();
        Set<String> cached = facilitiesByBoundary.getIfPresent(code);
        if (cached != null) {
            return cached;
        }
        Set<String> expanded = boundaryUtil.expandToFacilityCodes(code, grant.getBoundaryType());
        facilitiesByBoundary.put(code, expanded);
        return expanded;
    }

    /**
     * Never negative-cache. With fail-closed, caching a failure would turn a two-second HRMS blip
     * into a two-minute platform outage.
     */
    private JurisdictionGrants grantsFor(String uuid, RequestInfo requestInfo) {
        JurisdictionGrants cached = grantsByCaller.getIfPresent(uuid);
        if (cached != null) {
            return cached;
        }
        JurisdictionGrants fresh = hrmsUtils.getActiveJurisdictionsForCaller(requestInfo);
        if (fresh.isOk()) {
            grantsByCaller.put(uuid, fresh);
        }
        return fresh;
    }

    /** Drops a caller's cached grants, for when this service changes them itself. */
    public void forgetCaller(String uuid) {
        if (uuid != null) {
            grantsByCaller.invalidate(uuid);
        }
    }

    /** Drops every cached boundary expansion, for when this service adds a facility to the hierarchy. */
    public void forgetBoundaries() {
        facilitiesByBoundary.invalidateAll();
    }

    private boolean isSystemCaller(RequestInfo requestInfo) {
        User user = requestInfo == null ? null : requestInfo.getUserInfo();
        if (user == null) {
            return false;
        }
        if (user.getType() != null && "SYSTEM".equalsIgnoreCase(user.getType())) {
            return true;
        }
        String internalUuid = configs.getInternalMicroserviceUserUuid();
        if (internalUuid != null && !internalUuid.trim().isEmpty()
                && internalUuid.trim().equals(user.getUuid())) {
            return true;
        }
        for (String code : heldRoleCodes(requestInfo)) {
            if (systemRoles.contains(code)) {
                return true;
            }
        }
        return false;
    }

    private static String callerUuid(RequestInfo requestInfo) {
        User user = requestInfo == null ? null : requestInfo.getUserInfo();
        String uuid = user == null ? null : user.getUuid();
        return uuid == null || uuid.trim().isEmpty() ? null : uuid.trim();
    }

    private static Set<String> heldRoleCodes(RequestInfo requestInfo) {
        Set<String> codes = new HashSet<>();
        User user = requestInfo == null ? null : requestInfo.getUserInfo();
        if (user == null || user.getRoles() == null) {
            return codes;
        }
        for (org.egov.common.contract.request.Role role : user.getRoles()) {
            if (role != null && role.getCode() != null) {
                codes.add(role.getCode().trim());
            }
        }
        return codes;
    }
}
