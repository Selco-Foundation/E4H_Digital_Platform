package org.egov.util;

import org.egov.tracer.model.CustomException;
import org.egov.util.JurisdictionRoleRules.RoleRule;
import org.egov.web.models.Jurisdiction;
import org.egov.web.models.Role;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/** The server-side twin of the org user form's jurisdictionRoles.js; the cases mirror it. */
class JurisdictionRoleRulesTest {

    private static final List<RoleRule> RULES = List.of(
            new RoleRule("VENDOR_ADMIN", "Vendor Admin", false, null),
            new RoleRule("AMC_FIELD_STAFF", "Field Staff", false, null),
            new RoleRule("PROJECT_MANAGER", "Project Manager", null, List.of("Country", "State", "District", "Block")),
            new RoleRule("COMPLAINT_RESOLVER", "Resolver", null, List.of("Facility")),
            new RoleRule("VIEWER", "Viewer", null, null));

    private static List<Role> roles(String... codes) {
        return Arrays.stream(codes).map(code -> Role.builder().code(code).name(code).build()).toList();
    }

    private static Jurisdiction grant(String type, boolean active, String... roleCodes) {
        return Jurisdiction.builder().boundary("B").boundaryType(type).isActive(active).roles(roles(roleCodes)).build();
    }

    private static String refusalCode(List<Jurisdiction> grants, List<Role> userRoles) {
        return assertThrows(CustomException.class, () -> JurisdictionRoleRules.enforce(grants, userRoles, RULES)).getCode();
    }

    @Test
    void allRolesUnscopedOnlyWhenEveryRoleIs() {
        assertTrue(JurisdictionRoleRules.allRolesUnscoped(roles("VENDOR_ADMIN", "AMC_FIELD_STAFF"), RULES));
        assertFalse(JurisdictionRoleRules.allRolesUnscoped(roles("VENDOR_ADMIN", "PROJECT_MANAGER"), RULES));
        assertFalse(JurisdictionRoleRules.allRolesUnscoped(roles(), RULES));
        assertFalse(JurisdictionRoleRules.allRolesUnscoped(roles("NOT_IN_MDMS"), RULES), "unknown roles are scoped");
    }

    @Test
    void anUnscopedOnlyUserWithNoJurisdictionGetsACountryGrantNamingTheirRoles() {
        List<Role> userRoles = roles("VENDOR_ADMIN", "AMC_FIELD_STAFF");

        List<Jurisdiction> result = JurisdictionRoleRules.withDefaultCountryGrant(null, userRoles, RULES, "in");

        assertEquals(1, result.size());
        Jurisdiction country = result.get(0);
        assertEquals("India", country.getBoundary());
        assertEquals("Country", country.getBoundaryType());
        assertEquals("SELCO", country.getHierarchy());
        assertEquals("in", country.getTenantId());
        assertTrue(country.getIsActive());
        assertEquals(List.of("VENDOR_ADMIN", "AMC_FIELD_STAFF"), country.getRoles().stream().map(Role::getCode).toList());
        assertDoesNotThrow(() -> JurisdictionRoleRules.enforce(result, userRoles, RULES));
    }

    @Test
    void noDefaultGrantWhenAnActiveJurisdictionExistsOrAScopedRoleIsHeld() {
        List<Jurisdiction> existing = List.of(grant("State", true));
        assertSame(existing, JurisdictionRoleRules.withDefaultCountryGrant(existing, roles("VENDOR_ADMIN"), RULES, "in"));
        assertEquals(1, JurisdictionRoleRules.withDefaultCountryGrant(List.of(grant("State", false)), roles("VENDOR_ADMIN"), RULES, "in")
                .stream().filter(j -> Boolean.TRUE.equals(j.getIsActive())).count());
        assertNull(JurisdictionRoleRules.withDefaultCountryGrant(null, roles("VENDOR_ADMIN", "PROJECT_MANAGER"), RULES, "in"));
    }

    @Test
    void aScopedRoleAddedLaterIsNotCoveredByTheAutomaticGrant() {
        List<Jurisdiction> auto = JurisdictionRoleRules.withDefaultCountryGrant(null, roles("VENDOR_ADMIN"), RULES, "in");

        assertEquals(JurisdictionRoleRules.JURISDICTION_REQUIRED_FOR_ROLE,
                refusalCode(auto, roles("VENDOR_ADMIN", "PROJECT_MANAGER")));
    }

    @Test
    void allRolesNamingAndInactiveGrantsAreHandledWhenCoveringScopedRoles() {
        List<Role> userRoles = roles("VENDOR_ADMIN", "PROJECT_MANAGER");
        assertDoesNotThrow(() -> JurisdictionRoleRules.enforce(List.of(grant("State", true)), userRoles, RULES));
        assertDoesNotThrow(() -> JurisdictionRoleRules.enforce(List.of(grant("State", true, "PROJECT_MANAGER")), userRoles, RULES));
        assertEquals(JurisdictionRoleRules.JURISDICTION_REQUIRED_FOR_ROLE,
                refusalCode(List.of(grant("State", true, "VENDOR_ADMIN")), userRoles));
        assertEquals(JurisdictionRoleRules.JURISDICTION_REQUIRED_FOR_ROLE,
                refusalCode(List.of(grant("State", false)), userRoles));
    }

    @Test
    void aRoleMayOnlyBeGrantedAtItsAllowedLevels() {
        assertEquals(JurisdictionRoleRules.LEVEL_NOT_ALLOWED,
                refusalCode(List.of(grant("State", true, "COMPLAINT_RESOLVER")), roles("COMPLAINT_RESOLVER")));
        assertEquals(JurisdictionRoleRules.LEVEL_NOT_ALLOWED,
                refusalCode(List.of(grant("Facility", true, "PROJECT_MANAGER")), roles("PROJECT_MANAGER")));
        assertDoesNotThrow(() -> JurisdictionRoleRules.enforce(
                List.of(grant("Facility", true, "COMPLAINT_RESOLVER")), roles("COMPLAINT_RESOLVER"), RULES));
    }

    @Test
    void aGrantWithNoRolesIsCheckedAgainstEveryRoleTheUserHolds() {
        // "All roles" at State is refused because the resolver is facility-only, even though PM is fine there.
        assertEquals(JurisdictionRoleRules.LEVEL_NOT_ALLOWED,
                refusalCode(List.of(grant("State", true)), roles("PROJECT_MANAGER", "COMPLAINT_RESOLVER")));
    }

    @Test
    void unscopedRolesAndMissingFieldsAreNotRestricted() {
        assertDoesNotThrow(() -> JurisdictionRoleRules.enforce(
                List.of(grant("Facility", true)), roles("VENDOR_ADMIN", "VIEWER"), RULES));
        assertDoesNotThrow(() -> JurisdictionRoleRules.enforce(
                List.of(grant("Country", true)), roles("VIEWER"), List.of()), "no MDMS fields, no restriction");
    }

    @Test
    void inactiveGrantsAreNotLevelChecked() {
        assertDoesNotThrow(() -> JurisdictionRoleRules.enforce(
                List.of(grant("State", false, "COMPLAINT_RESOLVER"), grant("Facility", true, "COMPLAINT_RESOLVER")),
                roles("COMPLAINT_RESOLVER"), RULES));
    }
}
