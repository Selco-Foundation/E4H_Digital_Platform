package facility.util;

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

/**
 * Decides what the caller may see, per the role they say they are acting as.
 *
 * <h3>Why the caller names the roles, and not the server</h3>
 * One person can be a Facility Administrator in one state and a Project Manager in another. The
 * endpoint is the same from both screens, so a server that simply unioned every role the caller
 * holds would let the fa screens reach pm's state. The header names the hats actually in play, and
 * may name more than one: the Management Hub sends the single role its module means, while IM
 * sends every IM role the caller holds, because there a facility is visible to all of those roles
 * alike. The server unions the jurisdictions of the roles it is given, and only those.
 *
 * <h3>Not handled: a caller who is different things at different levels</h3>
 * Someone holding assessor across a state and facilitator in one district of it gets the union, so
 * the district grant adds nothing and the state wins. Narrowing per level is deliberately out of
 * scope until a real user needs it - see NEXT_STEPS. The union is what IM asks for today: within a
 * jurisdiction it must not matter which IM hat the caller is wearing.
 *
 * <h3>Why a forged header cannot escalate</h3>
 * The claimed role is checked against the roles on the caller's own {@code RequestInfo}, which the
 * gateway re-attaches from the authenticated token. So the header can only ever <em>select among</em>
 * scopes the caller already holds. It can never acquire one.
 *
 * <h3>No caching, for now</h3>
 * Every scoped request asks HRMS for the caller's grants and boundary-service to expand each one.
 * An earlier version cached both, which is how a third-party cache library arrived in this service
 * for two maps. The honest position is that nobody has measured what these calls cost: with
 * {@code scope.mode=off} they do not happen at all, and warn mode makes exactly the same calls
 * while applying nothing - so the cost can be observed on real traffic before enforcement depends
 * on it. If it turns out to matter, Redis is already in the platform and is the natural place for
 * a scope cache shared across pods, rather than a per-pod one that multiplies by replica count.
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

    @Autowired
    public RoleScopeResolver(Configuration configs, HRMSUtils hrmsUtils, BoundaryUtil boundaryUtil) {
        this.configs = configs;
        this.hrmsUtils = hrmsUtils;
        this.boundaryUtil = boundaryUtil;
        this.mode = ScopeMode.parse(configs.getJurisdictionScopeMode());
        this.systemRoles = new HashSet<>(configs.getJurisdictionSystemRoles() == null
                ? java.util.Collections.emptyList()
                : configs.getJurisdictionSystemRoles());
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
        return decide(requestInfo, actingRoleHeader, mode);
    }

    /**
     * The same decision, always enforced, for a check that does not belong to the facility-search
     * rollout and so must not be switched off or reduced to logging by
     * {@code facility.jurisdiction.scope.mode} - the CRM's mobile-change queue, for one.
     */
    public FacilityScope resolveEnforced(RequestInfo requestInfo, String actingRole) {
        return decide(requestInfo, actingRole, ScopeMode.ENFORCE);
    }

    private FacilityScope decide(RequestInfo requestInfo, String actingRoleHeader, ScopeMode mode) {
        // Identity first, before the header is read: a service is unscoped whatever it claims.
        if (isSystemCaller(requestInfo)) {
            return FacilityScope.unscoped(FacilityScope.Decision.UNSCOPED_SYSTEM);
        }

        String uuid = callerUuid(requestInfo);
        if (uuid == null) {
            return settle(mode, FacilityScope.Decision.DENY_MISSING_ACTING_ROLE, null, null, "no caller identity");
        }

        Set<String> actingRoles = parseActingRoles(actingRoleHeader);
        if (actingRoles.isEmpty()) {
            return settle(mode, FacilityScope.Decision.DENY_MISSING_ACTING_ROLE, null, null, "no " + X_ACTING_ROLE);
        }
        String actingRole = String.join(",", actingRoles);
        // Every named role must be held, not just one of them. Dropping the unheld ones instead
        // would turn a typo into a quietly narrower result that still looks like it worked.
        Set<String> held = heldRoleCodes(requestInfo);
        if (!held.containsAll(actingRoles)) {
            return settle(mode, FacilityScope.Decision.DENY_INVALID_ACTING_ROLE, actingRole, null, "role not held");
        }

        JurisdictionGrants grants = hrmsUtils.getActiveJurisdictionsForCaller(requestInfo);
        if (!grants.isOk()) {
            return settle(mode, FacilityScope.Decision.DENY_SCOPE_UNAVAILABLE, actingRole, null,
                    "hrms=" + grants.status());
        }

        Set<String> ceiling = new LinkedHashSet<>();
        StringBuilder summary = new StringBuilder();
        for (Jurisdiction grant : grants.grants()) {
            if (!appliesTo(grant, actingRoles)) {
                continue;
            }
            Set<String> beneath;
            try {
                beneath = expand(grant);
            } catch (Exception e) {
                log.error("Could not expand jurisdiction {} for caller {}", grant.getBoundary(), uuid, e);
                return settle(mode, FacilityScope.Decision.DENY_SCOPE_UNAVAILABLE, actingRole, null, "boundary expansion failed");
            }
            ceiling.addAll(beneath);
            summary.append(summary.length() == 0 ? "" : ",")
                    .append(grant.getBoundaryType()).append(':').append(grant.getBoundary())
                    .append('(').append(beneath.size()).append(')');
        }

        if (ceiling.isEmpty()) {
            return settle(mode, FacilityScope.Decision.DENY_NO_JURISDICTION, actingRole, ceiling, summary.toString());
        }
        return settle(mode, FacilityScope.Decision.SCOPED, actingRole, ceiling, summary.toString());
    }

    /**
     * In warn mode every outcome is computed and logged but none is applied, so real traffic can be
     * classified before the control is switched on.
     */
    private FacilityScope settle(ScopeMode mode, FacilityScope.Decision decision, String actingRole, Set<String> ceiling, String summary) {
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
    private boolean appliesTo(Jurisdiction grant, Set<String> actingRoles) {
        List<Role> roles = grant.getRoles();
        if (roles == null || roles.isEmpty()) {
            return true;
        }
        for (Role role : roles) {
            if (role != null && role.getCode() != null && actingRoles.contains(role.getCode().trim())) {
                return true;
            }
        }
        return false;
    }

    /**
     * The roles named in the header, in the order given.
     *
     * One value is the Management Hub's case and the common one; a comma-separated list is IM
     * declaring every role it means at once. Ordered so the audit log reads the same way twice for
     * the same caller, which matters when the only evidence of what warn mode saw is a log line.
     */
    private static Set<String> parseActingRoles(String header) {
        Set<String> roles = new LinkedHashSet<>();
        if (header == null) {
            return roles;
        }
        for (String part : header.split(",")) {
            String code = part.trim();
            if (!code.isEmpty()) {
                roles.add(code);
            }
        }
        return roles;
    }

    private Set<String> expand(Jurisdiction grant) throws Exception {
        return boundaryUtil.expandToFacilityCodes(grant.getBoundary().trim(), grant.getBoundaryType());
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
