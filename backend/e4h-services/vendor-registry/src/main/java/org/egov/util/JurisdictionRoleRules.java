package org.egov.util;

import org.egov.tracer.model.CustomException;
import org.egov.web.models.Jurisdiction;
import org.egov.web.models.Role;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import static org.egov.util.OrganisationConstant.DEFAULT_COUNTRY_BOUNDARY;
import static org.egov.util.OrganisationConstant.DEFAULT_COUNTRY_BOUNDARY_TYPE;
import static org.egov.util.OrganisationConstant.JURISDICTION_HIERARCHY;

/**
 * How each role relates to jurisdiction, as set by its `Organisation.OrgRoles` MDMS record.
 *
 * This is the server-side twin of the org user form's `jurisdictionRoles.js`, so a caller using the
 * API directly gets exactly what the form enforces. Keep the two in step.
 *
 * - `jurisdictionScoped: false` - jurisdiction does not decide what the role sees. Such a role is
 *   not level-checked and needs no jurisdiction of its own.
 * - `allowedJurisdictionLevels` - the boundary types the role may be granted at.
 *
 * A missing field means no restriction, so a record without them (or an environment whose MDMS has
 * not been updated) behaves as before.
 *
 * `boundaryType` is read from the request, not verified against the boundary service.
 */
public final class JurisdictionRoleRules {

    public static final String LEVEL_NOT_ALLOWED = "ORG_JURISDICTION_LEVEL_NOT_ALLOWED_FOR_ROLE";
    public static final String JURISDICTION_REQUIRED_FOR_ROLE = "ORG_JURISDICTION_REQUIRED_FOR_ROLE";

    private JurisdictionRoleRules() {
    }

    /** One `OrgRoles` record, reduced to what jurisdiction rules need. Unknown MDMS fields are ignored. */
    @com.fasterxml.jackson.annotation.JsonIgnoreProperties(ignoreUnknown = true)
    public record RoleRule(String code, String name, Boolean jurisdictionScoped, List<String> allowedJurisdictionLevels) {

        boolean scoped() {
            return !Boolean.FALSE.equals(jurisdictionScoped);
        }
    }

    private static Map<String, RoleRule> byCode(List<RoleRule> rules) {
        return (rules == null ? List.<RoleRule>of() : rules).stream()
                .filter(rule -> rule != null && rule.code() != null)
                .collect(Collectors.toMap(RoleRule::code, Function.identity(), (first, second) -> first));
    }

    private static boolean isScoped(String code, Map<String, RoleRule> rules) {
        RoleRule rule = rules.get(code);
        return rule == null || rule.scoped();
    }

    private static Set<String> codesOf(List<Role> roles) {
        return (roles == null ? List.<Role>of() : roles).stream()
                .filter(Objects::nonNull)
                .map(Role::getCode)
                .filter(Objects::nonNull)
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    private static boolean active(Jurisdiction jurisdiction) {
        return jurisdiction != null && !Boolean.FALSE.equals(jurisdiction.getIsActive());
    }

    /** True when every role held is outside jurisdiction scoping. No roles is not "all unscoped". */
    public static boolean allRolesUnscoped(List<Role> userRoles, List<RoleRule> rules) {
        Map<String, RoleRule> byCode = byCode(rules);
        Set<String> codes = codesOf(userRoles);
        return !codes.isEmpty() && codes.stream().noneMatch(code -> isScoped(code, byCode));
    }

    /**
     * The roles `jurisdiction` applies to that may not be granted at its boundary type. A
     * jurisdiction with no roles applies to every role the user holds.
     */
    public static List<String> rolesNotAllowedAtLevel(Jurisdiction jurisdiction, List<Role> userRoles, List<RoleRule> rules) {
        Map<String, RoleRule> byCode = byCode(rules);
        boolean allRoles = jurisdiction.getRoles() == null || jurisdiction.getRoles().isEmpty();
        Set<String> applied = codesOf(allRoles ? userRoles : jurisdiction.getRoles());
        List<String> refused = new ArrayList<>();
        for (String code : applied) {
            RoleRule rule = byCode.get(code);
            if (rule == null || !rule.scoped() || rule.allowedJurisdictionLevels() == null) {
                continue;
            }
            if (!rule.allowedJurisdictionLevels().contains(jurisdiction.getBoundaryType())) {
                refused.add(code);
            }
        }
        return refused;
    }

    /**
     * The jurisdiction-scoped roles the user holds that no active jurisdiction covers. A
     * jurisdiction covers a role when it applies to all roles or names it.
     */
    public static List<String> scopedRolesWithoutJurisdiction(List<Jurisdiction> jurisdictions, List<Role> userRoles, List<RoleRule> rules) {
        Map<String, RoleRule> byCode = byCode(rules);
        List<Jurisdiction> activeGrants = (jurisdictions == null ? List.<Jurisdiction>of() : jurisdictions).stream()
                .filter(JurisdictionRoleRules::active)
                .toList();
        return codesOf(userRoles).stream()
                .filter(code -> isScoped(code, byCode))
                .filter(code -> activeGrants.stream().noneMatch(grant ->
                        grant.getRoles() == null || grant.getRoles().isEmpty() || codesOf(grant.getRoles()).contains(code)))
                .toList();
    }

    /**
     * `jurisdictions`, plus a Country-level grant naming the user's roles when the user has no active
     * jurisdiction and every role is outside jurisdiction scoping. Otherwise `jurisdictions` itself.
     *
     * The grant names the roles rather than being "all roles", so a scoped role added later is not
     * silently covered by it.
     */
    public static List<Jurisdiction> withDefaultCountryGrant(List<Jurisdiction> jurisdictions, List<Role> userRoles,
                                                             List<RoleRule> rules, String tenantId) {
        boolean anyActive = jurisdictions != null && jurisdictions.stream().anyMatch(JurisdictionRoleRules::active);
        if (anyActive || !allRolesUnscoped(userRoles, rules)) {
            return jurisdictions;
        }
        List<Jurisdiction> result = jurisdictions == null ? new ArrayList<>() : new ArrayList<>(jurisdictions);
        result.add(Jurisdiction.builder()
                .hierarchy(JURISDICTION_HIERARCHY)
                .boundary(DEFAULT_COUNTRY_BOUNDARY)
                .boundaryType(DEFAULT_COUNTRY_BOUNDARY_TYPE)
                .tenantId(tenantId)
                .isActive(true)
                .roles(new ArrayList<>(userRoles))
                .build());
        return result;
    }

    /** Refuses the first active jurisdiction granted at a level its role does not allow, then the first uncovered scoped role. */
    public static void enforce(List<Jurisdiction> jurisdictions, List<Role> userRoles, List<RoleRule> rules) {
        for (Jurisdiction jurisdiction : jurisdictions == null ? List.<Jurisdiction>of() : jurisdictions) {
            if (!active(jurisdiction)) {
                continue;
            }
            List<String> refused = rolesNotAllowedAtLevel(jurisdiction, userRoles, rules);
            if (!refused.isEmpty()) {
                throw new CustomException(LEVEL_NOT_ALLOWED, refused.get(0)
                        + " cannot be assigned a " + jurisdiction.getBoundaryType() + " level jurisdiction");
            }
        }
        List<String> uncovered = scopedRolesWithoutJurisdiction(jurisdictions, userRoles, rules);
        if (!uncovered.isEmpty()) {
            throw new CustomException(JURISDICTION_REQUIRED_FOR_ROLE,
                    uncovered.get(0) + " needs a jurisdiction that applies to it");
        }
    }
}
