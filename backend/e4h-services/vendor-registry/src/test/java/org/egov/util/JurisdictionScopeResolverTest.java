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

import java.util.Collection;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.lenient;
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

    private static final String TENANT = "in";
    private static final String CALLER = "caller-uuid";

    @Mock
    private HRMSUtils hrmsUtils;

    @Mock
    private BoundaryUtil boundaryUtil;

    @Mock
    private Configuration config;

    private JurisdictionScopeResolver resolver;

    @BeforeEach
    void setUp() {
        when(config.getJurisdictionScopeCacheSeconds()).thenReturn(120L);
        when(config.getJurisdictionBoundaryCacheSeconds()).thenReturn(300L);
        when(config.getJurisdictionUnscopedRoles()).thenReturn(List.of("ORG_PLATFORM_ADMIN"));
        resolver = new JurisdictionScopeResolver(hrmsUtils, boundaryUtil, config);
    }

    private RequestInfo caller(String... roleCodes) {
        List<org.egov.common.contract.request.Role> roles = java.util.Arrays.stream(roleCodes)
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
            jurisdiction.setRoles(java.util.Arrays.stream(roleCodes)
                    .map(code -> Role.builder().code(code).build())
                    .toList());
        }
        return jurisdiction;
    }

    private void callerHolds(Jurisdiction... jurisdictions) {
        Employee employee = new Employee();
        employee.setJurisdictions(List.of(jurisdictions));
        when(hrmsUtils.getUserById(any(), anyString())).thenReturn(employee);
    }

    private void beneath(String boundary, String... descendants) {
        Set<String> all = new java.util.LinkedHashSet<>();
        all.add(boundary);
        all.addAll(List.of(descendants));
        when(boundaryUtil.codesBeneath(any(), anyString(), eqList(boundary))).thenReturn(all);
    }

    private static List<String> eqList(String single) {
        return org.mockito.ArgumentMatchers.argThat(argument ->
                argument != null && argument.size() == 1 && argument.contains(single));
    }

    @Test
    void platformAdminIsUnscoped() {
        JurisdictionScopeResolver.Scope scope = resolver.resolve(caller("ORG_PLATFORM_ADMIN"), TENANT);

        assertTrue(scope.unscoped());
        assertTrue(scope.covers("anything-at-all"));
    }

    @Test
    void aGrantCoversItselfAndItsDescendants() {
        callerHolds(jurisdiction("India_Telangana", "VENDOR_ADMIN"));
        beneath("India_Telangana", "India_Telangana_Hyderabad", "India_Telangana_Hyderabad_Block1");

        JurisdictionScopeResolver.Scope scope = resolver.resolve(caller("VENDOR_ADMIN"), TENANT);

        assertFalse(scope.unscoped());
        assertTrue(scope.covers("India_Telangana"));
        assertTrue(scope.covers("India_Telangana_Hyderabad"));
        assertTrue(scope.covers("India_Telangana_Hyderabad_Block1"));
    }

    @Test
    void anAncestorOfAGrantIsNotCovered() {
        callerHolds(jurisdiction("India_Telangana_Hyderabad", "VENDOR_ADMIN"));
        beneath("India_Telangana_Hyderabad", "India_Telangana_Hyderabad_Block1");

        JurisdictionScopeResolver.Scope scope = resolver.resolve(caller("VENDOR_ADMIN"), TENANT);

        // The state containing the caller's district is above them, not inside them. Were this to
        // pass, a district administrator would reach the whole state.
        assertFalse(scope.covers("India_Telangana"));
    }

    @Test
    void aSiblingIsNotCovered() {
        callerHolds(jurisdiction("India_Telangana", "VENDOR_ADMIN"));
        beneath("India_Telangana", "India_Telangana_Hyderabad");

        JurisdictionScopeResolver.Scope scope = resolver.resolve(caller("VENDOR_ADMIN"), TENANT);

        assertFalse(scope.covers("India_Karnataka"));
    }

    @Test
    void aPrefixOfAGrantIsNotMistakenForADescendant() {
        callerHolds(jurisdiction("India_Telangana_Hyderabad", "VENDOR_ADMIN"));
        beneath("India_Telangana_Hyderabad");

        JurisdictionScopeResolver.Scope scope = resolver.resolve(caller("VENDOR_ADMIN"), TENANT);

        // Resolution goes through the boundary service, so a code that merely starts with the
        // same text is unrelated. A prefix match would wrongly include this.
        assertFalse(scope.covers("India_Telangana_Hyderabad2"));
    }

    @Test
    void onlyJurisdictionsCarryingTheRoleCount() {
        callerHolds(
                jurisdiction("India_Telangana", "VENDOR_ADMIN"),
                jurisdiction("India_Karnataka", "AMC_FIELD_STAFF"));
        beneath("India_Telangana", "India_Telangana_Hyderabad");
        beneath("India_Karnataka", "India_Karnataka_Bengaluru");

        JurisdictionScopeResolver.Scope scope = resolver.resolve(caller("VENDOR_ADMIN"), TENANT);

        assertTrue(scope.covers("India_Telangana_Hyderabad"));
        assertFalse(scope.covers("India_Karnataka"));
    }

    @Test
    void aJurisdictionWithNoRolesAppliesToEveryRole() {
        callerHolds(jurisdiction("India_Telangana"));
        beneath("India_Telangana", "India_Telangana_Hyderabad");

        JurisdictionScopeResolver.Scope scope = resolver.resolve(caller("VENDOR_ADMIN"), TENANT);

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

        JurisdictionScopeResolver.Scope scope = resolver.resolve(caller("VENDOR_ADMIN"), TENANT);

        assertFalse(scope.unscoped());
        assertTrue(scope.codes().isEmpty());
        assertFalse(scope.covers("India_Telangana"));
    }

    @Test
    void noGrantsMeansNoAccessRatherThanFullAccess() {
        Employee employee = new Employee();
        employee.setJurisdictions(List.of());
        when(hrmsUtils.getUserById(any(), anyString())).thenReturn(employee);

        JurisdictionScopeResolver.Scope scope = resolver.resolve(caller("VENDOR_ADMIN"), TENANT);

        assertFalse(scope.unscoped());
        assertTrue(scope.codes().isEmpty());
        assertFalse(scope.covers("India_Telangana"));
    }

    @Test
    void anHrmsFailureRefusesRatherThanPassing() {
        when(hrmsUtils.getUserById(any(), anyString())).thenThrow(new RuntimeException("hrms down"));

        assertThrows(CustomException.class, () -> resolver.resolve(caller("VENDOR_ADMIN"), TENANT));
    }

    @Test
    void aCallerWithNoUuidIsRefused() {
        RequestInfo noUuid = RequestInfo.builder()
                .userInfo(User.builder().roles(List.of()).build())
                .build();

        assertThrows(CustomException.class, () -> resolver.resolve(noUuid, TENANT));
    }

    @Test
    void withinKeepsOnlyTheJurisdictionsInsideTheScope() {
        callerHolds(jurisdiction("India_Telangana", "VENDOR_ADMIN"));
        beneath("India_Telangana", "India_Telangana_Hyderabad");

        JurisdictionScopeResolver.Scope scope = resolver.resolve(caller("VENDOR_ADMIN"), TENANT);

        List<Jurisdiction> target = List.of(
                jurisdiction("India_Telangana_Hyderabad"),
                jurisdiction("India_Karnataka"));

        List<Jurisdiction> visible = scope.within(target);

        // The user works in both places; the caller administers one of them.
        assertEquals(1, visible.size());
        assertEquals("India_Telangana_Hyderabad", visible.get(0).getBoundary());
    }

    @Test
    void coversAllRejectsAListWithAnythingOutside() {
        callerHolds(jurisdiction("India_Telangana", "VENDOR_ADMIN"));
        beneath("India_Telangana", "India_Telangana_Hyderabad");

        JurisdictionScopeResolver.Scope scope = resolver.resolve(caller("VENDOR_ADMIN"), TENANT);

        assertTrue(resolver.coversAll(scope, List.of(jurisdiction("India_Telangana_Hyderabad"))));
        assertFalse(resolver.coversAll(scope,
                List.of(jurisdiction("India_Telangana_Hyderabad"), jurisdiction("India_Karnataka"))));
    }

    @Test
    void anUnscopedCallerCoversEverything() {
        JurisdictionScopeResolver.Scope scope = JurisdictionScopeResolver.Scope.everything();

        List<Jurisdiction> target = List.of(jurisdiction("India_Karnataka"), jurisdiction("India_Telangana"));

        assertEquals(2, scope.within(target).size());
        assertTrue(resolver.coversAll(scope, target));
    }

    @Test
    void theBoundaryServiceIsAskedOncePerGrantAndThenCached() {
        callerHolds(jurisdiction("India_Telangana", "VENDOR_ADMIN"));
        beneath("India_Telangana", "India_Telangana_Hyderabad");

        resolver.resolve(caller("VENDOR_ADMIN"), TENANT);
        resolver.resolve(caller("VENDOR_ADMIN"), TENANT);

        // Search and count resolve the scope separately on every request; without the cache that
        // is two HRMS calls and two boundary expansions per page view.
        org.mockito.Mockito.verify(hrmsUtils, org.mockito.Mockito.times(1)).getUserById(any(), anyString());
        org.mockito.Mockito.verify(boundaryUtil, org.mockito.Mockito.times(1))
                .codesBeneath(any(), anyString(), any(Collection.class));
    }
}
