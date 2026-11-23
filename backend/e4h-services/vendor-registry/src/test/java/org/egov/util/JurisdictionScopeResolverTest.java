package org.egov.util;

import org.egov.common.contract.request.RequestInfo;
import org.egov.common.contract.request.User;
import org.egov.config.Configuration;
import org.egov.tracer.model.CustomException;
import org.egov.web.models.Employee;
import org.egov.web.models.Jurisdiction;
import org.egov.web.models.Role;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

/**
 * The containment test behind org-user scoping.
 *
 * These cases are the ones where getting it wrong is invisible: a scope that reaches upward
 * instead of downward still returns plausible rows, and an unresolvable caller that is treated
 * as unscoped still shows a full table.
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class JurisdictionScopeResolverTest {

    private static final String CALLER = "caller-uuid";

    @Mock
    private HRMSUtils hrmsUtils;

    @Mock
    private Configuration config;

    private JurisdictionScopeResolver resolver;

    @BeforeEach
    void setUp() {
        when(config.getJurisdictionUnscopedRoles()).thenReturn(List.of("ORG_PLATFORM_ADMIN"));
        resolver = new JurisdictionScopeResolver(hrmsUtils, config);
    }

    private RequestInfo caller(String... roleCodes) {
        List<org.egov.common.contract.request.Role> roles = Arrays.stream(roleCodes)
                .map(code -> org.egov.common.contract.request.Role.builder().code(code).build())
                .toList();
        return RequestInfo.builder()
                .userInfo(User.builder().uuid(CALLER).roles(roles).build())
                .build();
    }

    private Jurisdiction jurisdiction(String boundary, String... roleCodes) {
        Jurisdiction jurisdiction = new Jurisdiction();
        jurisdiction.setBoundary(boundary);
        jurisdiction.setIsActive(true);
        if (roleCodes.length > 0) {
            jurisdiction.setRoles(Arrays.stream(roleCodes)
                    .map(code -> Role.builder().code(code).build())
                    .toList());
        }
        return jurisdiction;
    }

    private List<Jurisdiction> held(String... boundaries) {
        return Arrays.stream(boundaries).map(this::jurisdiction).toList();
    }

    private void callerHolds(Jurisdiction... jurisdictions) {
        Employee employee = new Employee();
        employee.setJurisdictions(List.of(jurisdictions));
        when(hrmsUtils.getUserById(any(), anyString())).thenReturn(employee);
    }

    private JurisdictionScopeResolver.Scope scopedTo(String... grants) {
        callerHolds(Arrays.stream(grants)
                .map(grant -> jurisdiction(grant, "VENDOR_ADMIN"))
                .toArray(Jurisdiction[]::new));
        return resolver.resolve(caller("VENDOR_ADMIN"));
    }

    @Test
    void platformAdminIsUnscoped() {
        JurisdictionScopeResolver.Scope scope = resolver.resolve(caller("ORG_PLATFORM_ADMIN"));

        assertTrue(scope.unscoped());
        assertTrue(scope.covers("anything-at-all"));
    }

    @Test
    void aGrantCoversItselfAndEverythingBeneathIt() {
        JurisdictionScopeResolver.Scope scope = scopedTo("India_Karnataka");

        assertFalse(scope.unscoped());
        assertTrue(scope.covers("India_Karnataka"));
        assertTrue(scope.covers("India_Karnataka_Koppal"));
        assertTrue(scope.covers("India_Karnataka_Koppal_Kushtagi"));
        // Facility codes carry slashes and still sit under their block.
        assertTrue(scope.covers("India_Karnataka_Koppal_Kushtagi_FAC/2025/45002"));
    }

    @Test
    void anAncestorOfAGrantIsNotCovered() {
        JurisdictionScopeResolver.Scope scope = scopedTo("India_Karnataka_Koppal");

        // The state containing the caller's district is above them, not inside them. Were this to
        // pass, a district administrator would reach the whole state.
        assertFalse(scope.covers("India_Karnataka"));
        assertFalse(scope.covers("India"));
    }

    @Test
    void aSiblingIsNotCovered() {
        JurisdictionScopeResolver.Scope scope = scopedTo("India_Karnataka");

        assertFalse(scope.covers("India_Telangana"));
        assertFalse(scope.covers("India_Telangana_Hyderabad"));
    }

    @Test
    void aCodeThatMerelySharesAPrefixIsNotCovered() {
        JurisdictionScopeResolver.Scope scope = scopedTo("India_Telangana_Hyderabad");

        // Requiring the level separator is what makes the containment test exact: this code
        // starts with the grant's text but is a different district, not a child of it.
        assertFalse(scope.covers("India_Telangana_Hyderabad2"));
        assertFalse(scope.covers("India_Telangana_HyderabadRural"));
        // ...while a real child, which has the separator, is covered.
        assertTrue(scope.covers("India_Telangana_Hyderabad_Block1"));
    }

    @Test
    void severalGrantsAreEachHonoured() {
        JurisdictionScopeResolver.Scope scope = scopedTo("India_Karnataka_Koppal", "India_Telangana");

        assertTrue(scope.covers("India_Karnataka_Koppal_Kushtagi"));
        assertTrue(scope.covers("India_Telangana_Hyderabad"));
        assertFalse(scope.covers("India_Karnataka_Ballari"));
    }

    @Test
    void onlyJurisdictionsCarryingTheRoleCount() {
        callerHolds(
                jurisdiction("India_Telangana", "VENDOR_ADMIN"),
                jurisdiction("India_Karnataka", "AMC_FIELD_STAFF"));

        JurisdictionScopeResolver.Scope scope = resolver.resolve(caller("VENDOR_ADMIN"));

        assertTrue(scope.covers("India_Telangana_Hyderabad"));
        assertFalse(scope.covers("India_Karnataka"));
    }

    @Test
    void aJurisdictionWithNoRolesAppliesToEveryRole() {
        callerHolds(jurisdiction("India_Telangana"));

        JurisdictionScopeResolver.Scope scope = resolver.resolve(caller("VENDOR_ADMIN"));

        // Predates role scoping, so it applies to whatever the holder does - the same rule HRMS
        // search uses. Reading it as "no roles, therefore no scope" would lock out every account
        // created before role authoring existed.
        assertTrue(scope.covers("India_Telangana_Hyderabad"));
    }

    @Test
    void anInactiveJurisdictionGrantsNothing() {
        Jurisdiction inactive = jurisdiction("India_Telangana", "VENDOR_ADMIN");
        inactive.setIsActive(false);
        callerHolds(inactive);

        JurisdictionScopeResolver.Scope scope = resolver.resolve(caller("VENDOR_ADMIN"));

        assertFalse(scope.unscoped());
        assertTrue(scope.codes().isEmpty());
        assertFalse(scope.covers("India_Telangana"));
    }

    @Test
    void noGrantsMeansNoAccessRatherThanFullAccess() {
        Employee employee = new Employee();
        employee.setJurisdictions(List.of());
        when(hrmsUtils.getUserById(any(), anyString())).thenReturn(employee);

        JurisdictionScopeResolver.Scope scope = resolver.resolve(caller("VENDOR_ADMIN"));

        assertFalse(scope.unscoped());
        assertTrue(scope.codes().isEmpty());
        assertFalse(scope.covers("India_Telangana"));
    }

    @Test
    void anHrmsFailureRefusesRatherThanPassing() {
        when(hrmsUtils.getUserById(any(), anyString())).thenThrow(new RuntimeException("hrms down"));

        assertThrows(CustomException.class, () -> resolver.resolve(caller("VENDOR_ADMIN")));
    }

    @Test
    void aCallerWithNoUuidIsRefused() {
        RequestInfo noUuid = RequestInfo.builder()
                .userInfo(User.builder().roles(List.of()).build())
                .build();

        assertThrows(CustomException.class, () -> resolver.resolve(noUuid));
    }

    @Test
    void aUserPartlyInsideTheScopeIsVisible_TrimmedToTheSliceInReach() {
        JurisdictionScopeResolver.Scope scope = scopedTo("India_Karnataka_Koppal");

        // The case the whole design turns on: this user works in the caller's district and in
        // another state entirely.
        List<Jurisdiction> target = held("India_Karnataka_Koppal_Kushtagi", "India_Telangana");

        List<Jurisdiction> visible = scope.within(target);
        assertEquals(1, visible.size());
        assertEquals("India_Karnataka_Koppal_Kushtagi", visible.get(0).getBoundary());

        // ...and the half they cannot see is exactly what a save has to put back.
        List<Jurisdiction> preserved = scope.outside(target);
        assertEquals(1, preserved.size());
        assertEquals("India_Telangana", preserved.get(0).getBoundary());
    }

    @Test
    void coversAllRejectsAListWithAnythingOutside() {
        JurisdictionScopeResolver.Scope scope = scopedTo("India_Karnataka");

        assertTrue(scope.coversAll(held("India_Karnataka_Koppal", "India_Karnataka_Ballari")));
        assertFalse(scope.coversAll(held("India_Karnataka_Koppal", "India_Telangana")));
    }

    @Test
    void anUnscopedCallerCoversEverything() {
        JurisdictionScopeResolver.Scope scope = JurisdictionScopeResolver.Scope.everything();
        List<Jurisdiction> target = held("India_Karnataka", "India_Telangana");

        assertEquals(2, scope.within(target).size());
        assertTrue(scope.outside(target).isEmpty());
        assertTrue(scope.coversAll(target));
    }

    @Test
    void containmentCostsNoNetworkCall() {
        JurisdictionScopeResolver.Scope scope = scopedTo("India_Karnataka");

        // Regression guard. Resolving containment through the boundary service meant listing
        // every descendant of a grant, and a state expands to thousands of facility codes - an
        // HRMS request line past the servlet's limit, rejected with a 400 before the app ran.
        // One HRMS lookup for the caller's own grants is the only call this should ever make.
        for (int i = 0; i < 500; i++) {
            scope.covers("India_Karnataka_Koppal_Kushtagi_FAC/2025/" + (45000 + i));
        }

        org.mockito.Mockito.verify(hrmsUtils, org.mockito.Mockito.times(1)).getUserById(any(), anyString());
        org.mockito.Mockito.verifyNoMoreInteractions(hrmsUtils);
    }
}
