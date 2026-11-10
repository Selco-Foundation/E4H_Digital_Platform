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

    /**
     * Declares the hierarchy under test, as each boundary's chain from itself upwards.
     *
     * The resolver asks for ancestors of the boundaries being *tested*, never for descendants of
     * the caller's grants, so the stub is keyed that way too.
     */
    private final java.util.Map<String, Set<String>> hierarchy = new java.util.LinkedHashMap<>();

    private void chain(String... fromLeafUpwards) {
        hierarchy.put(fromLeafUpwards[0], new java.util.LinkedHashSet<>(List.of(fromLeafUpwards)));
    }

    private void wireBoundaryService() {
        when(boundaryUtil.ancestorsOf(any(), anyString(), any(Collection.class))).thenAnswer(call -> {
            Collection<String> asked = call.getArgument(2);
            java.util.Map<String, Set<String>> answer = new java.util.LinkedHashMap<>();
            asked.forEach(code -> answer.put(code,
                    hierarchy.getOrDefault(code, new java.util.LinkedHashSet<>(List.of(code)))));
            return answer;
        });
    }

    private List<Jurisdiction> held(String... boundaries) {
        return java.util.Arrays.stream(boundaries).map(this::jurisdiction).toList();
    }

    @Test
    void platformAdminIsUnscoped() {
        JurisdictionScopeResolver.Scope scope = resolver.resolve(caller("ORG_PLATFORM_ADMIN"), TENANT);

        assertTrue(scope.unscoped());
        assertTrue(scope.covers("anything-at-all", java.util.Set.of()));
    }

    @Test
    void aGrantCoversItselfAndItsDescendants() {
        callerHolds(jurisdiction("India_Telangana", "VENDOR_ADMIN"));
        chain("India_Telangana", "India");
        chain("India_Telangana_Hyderabad", "India_Telangana", "India");
        chain("India_Telangana_Hyderabad_Block1", "India_Telangana_Hyderabad", "India_Telangana", "India");
        wireBoundaryService();

        JurisdictionScopeResolver.Scope scope = resolver.resolve(caller("VENDOR_ADMIN"), TENANT);
        List<Jurisdiction> visible = resolver.within(scope,
                held("India_Telangana", "India_Telangana_Hyderabad", "India_Telangana_Hyderabad_Block1"),
                caller("VENDOR_ADMIN"), TENANT);

        assertFalse(scope.unscoped());
        assertEquals(3, visible.size());
    }

    @Test
    void anAncestorOfAGrantIsNotCovered() {
        callerHolds(jurisdiction("India_Telangana_Hyderabad", "VENDOR_ADMIN"));
        chain("India_Telangana", "India");
        wireBoundaryService();

        JurisdictionScopeResolver.Scope scope = resolver.resolve(caller("VENDOR_ADMIN"), TENANT);

        // The state containing the caller's district is above them, not inside them. Were this to
        // pass, a district administrator would reach the whole state.
        assertTrue(resolver.within(scope, held("India_Telangana"), caller("VENDOR_ADMIN"), TENANT).isEmpty());
    }

    @Test
    void aSiblingIsNotCovered() {
        callerHolds(jurisdiction("India_Telangana", "VENDOR_ADMIN"));
        chain("India_Karnataka", "India");
        wireBoundaryService();

        JurisdictionScopeResolver.Scope scope = resolver.resolve(caller("VENDOR_ADMIN"), TENANT);

        assertTrue(resolver.within(scope, held("India_Karnataka"), caller("VENDOR_ADMIN"), TENANT).isEmpty());
    }

    @Test
    void aPrefixOfAGrantIsNotMistakenForADescendant() {
        callerHolds(jurisdiction("India_Telangana_Hyderabad", "VENDOR_ADMIN"));
        chain("India_Telangana_Hyderabad2", "India_Telangana", "India");
        wireBoundaryService();

        JurisdictionScopeResolver.Scope scope = resolver.resolve(caller("VENDOR_ADMIN"), TENANT);

        // Resolution goes through the boundary service, so a code that merely starts with the
        // same text is unrelated. A prefix match would wrongly include this.
        assertTrue(resolver.within(scope, held("India_Telangana_Hyderabad2"),
                caller("VENDOR_ADMIN"), TENANT).isEmpty());
    }

    @Test
    void onlyJurisdictionsCarryingTheRoleCount() {
        callerHolds(
                jurisdiction("India_Telangana", "VENDOR_ADMIN"),
                jurisdiction("India_Karnataka", "AMC_FIELD_STAFF"));
        chain("India_Telangana_Hyderabad", "India_Telangana", "India");
        chain("India_Karnataka", "India");
        wireBoundaryService();

        JurisdictionScopeResolver.Scope scope = resolver.resolve(caller("VENDOR_ADMIN"), TENANT);

        assertEquals(1, resolver.within(scope, held("India_Telangana_Hyderabad"),
                caller("VENDOR_ADMIN"), TENANT).size());
        assertTrue(resolver.within(scope, held("India_Karnataka"), caller("VENDOR_ADMIN"), TENANT).isEmpty());
    }

    @Test
    void aJurisdictionWithNoRolesAppliesToEveryRole() {
        callerHolds(jurisdiction("India_Telangana"));
        chain("India_Telangana_Hyderabad", "India_Telangana", "India");
        wireBoundaryService();

        JurisdictionScopeResolver.Scope scope = resolver.resolve(caller("VENDOR_ADMIN"), TENANT);

        // Predates role scoping, so it applies to whatever the holder does - the same rule HRMS
        // search uses. Reading it as "no roles, therefore no scope" would lock out every account
        // created before role authoring existed.
        assertEquals(1, resolver.within(scope, held("India_Telangana_Hyderabad"),
                caller("VENDOR_ADMIN"), TENANT).size());
    }

    @Test
    void anInactiveJurisdictionGrantsNothing() {
        Jurisdiction inactive = jurisdiction("India_Telangana", "VENDOR_ADMIN");
        inactive.setIsActive(false);
        callerHolds(inactive);

        JurisdictionScopeResolver.Scope scope = resolver.resolve(caller("VENDOR_ADMIN"), TENANT);

        assertFalse(scope.unscoped());
        assertTrue(scope.codes().isEmpty());
    }

    @Test
    void noGrantsMeansNoAccessRatherThanFullAccess() {
        Employee employee = new Employee();
        employee.setJurisdictions(List.of());
        when(hrmsUtils.getUserById(any(), anyString())).thenReturn(employee);

        JurisdictionScopeResolver.Scope scope = resolver.resolve(caller("VENDOR_ADMIN"), TENANT);

        assertFalse(scope.unscoped());
        assertTrue(scope.codes().isEmpty());
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
        chain("India_Telangana_Hyderabad", "India_Telangana", "India");
        chain("India_Karnataka", "India");
        wireBoundaryService();

        JurisdictionScopeResolver.Scope scope = resolver.resolve(caller("VENDOR_ADMIN"), TENANT);

        List<Jurisdiction> target = held("India_Telangana_Hyderabad", "India_Karnataka");
        List<Jurisdiction> visible = resolver.within(scope, target, caller("VENDOR_ADMIN"), TENANT);

        // The user works in both places; the caller administers one of them. This is the case the
        // whole design turns on - the user stays visible, trimmed to the slice in reach.
        assertEquals(1, visible.size());
        assertEquals("India_Telangana_Hyderabad", visible.get(0).getBoundary());

        List<Jurisdiction> preserved = resolver.outside(scope, target, caller("VENDOR_ADMIN"), TENANT);
        assertEquals(1, preserved.size());
        assertEquals("India_Karnataka", preserved.get(0).getBoundary());
    }

    @Test
    void coversAllRejectsAListWithAnythingOutside() {
        callerHolds(jurisdiction("India_Telangana", "VENDOR_ADMIN"));
        chain("India_Telangana_Hyderabad", "India_Telangana", "India");
        chain("India_Karnataka", "India");
        wireBoundaryService();

        JurisdictionScopeResolver.Scope scope = resolver.resolve(caller("VENDOR_ADMIN"), TENANT);

        assertTrue(resolver.coversAll(scope, held("India_Telangana_Hyderabad"),
                caller("VENDOR_ADMIN"), TENANT));
        assertFalse(resolver.coversAll(scope, held("India_Telangana_Hyderabad", "India_Karnataka"),
                caller("VENDOR_ADMIN"), TENANT));
    }

    @Test
    void anUnscopedCallerCoversEverything() {
        JurisdictionScopeResolver.Scope scope = JurisdictionScopeResolver.Scope.everything();
        List<Jurisdiction> target = held("India_Karnataka", "India_Telangana");

        assertEquals(2, resolver.within(scope, target, caller("ORG_PLATFORM_ADMIN"), TENANT).size());
        assertTrue(resolver.coversAll(scope, target, caller("ORG_PLATFORM_ADMIN"), TENANT));
        // An unscoped caller must not trigger a boundary lookup at all.
        org.mockito.Mockito.verifyNoInteractions(boundaryUtil);
    }

    @Test
    void aWholePageOfJurisdictionsCostsOneBoundaryCall() {
        callerHolds(jurisdiction("India_Telangana", "VENDOR_ADMIN"));
        chain("India_Telangana_Hyderabad", "India_Telangana", "India");
        chain("India_Telangana_Warangal", "India_Telangana", "India");
        chain("India_Karnataka", "India");
        wireBoundaryService();

        JurisdictionScopeResolver.Scope scope = resolver.resolve(caller("VENDOR_ADMIN"), TENANT);
        List<Jurisdiction> visible = resolver.within(scope,
                held("India_Telangana_Hyderabad", "India_Telangana_Warangal", "India_Karnataka"),
                caller("VENDOR_ADMIN"), TENANT);

        assertEquals(2, visible.size());
        // Batched: resolving per jurisdiction would be a network round trip per row of the table.
        org.mockito.Mockito.verify(boundaryUtil, org.mockito.Mockito.times(1))
                .ancestorsOf(any(), anyString(), any(Collection.class));
    }

    @Test
    void theCallerSGrantsAreNeverExpandedDownwards() {
        callerHolds(jurisdiction("India_Karnataka", "VENDOR_ADMIN"));
        chain("India_Karnataka_Koppal", "India_Karnataka", "India");
        wireBoundaryService();

        resolver.resolve(caller("VENDOR_ADMIN"), TENANT);
        resolver.within(scopeOf("India_Karnataka"), held("India_Karnataka_Koppal"),
                caller("VENDOR_ADMIN"), TENANT);

        // Regression guard. Expanding a state downwards lists every facility beneath it, which
        // produced an HRMS request line past the servlet's limit and a 400 before the app ran.
        org.mockito.ArgumentCaptor<Collection<String>> asked =
                org.mockito.ArgumentCaptor.forClass(Collection.class);
        org.mockito.Mockito.verify(boundaryUtil).ancestorsOf(any(), anyString(), asked.capture());
        assertEquals(List.of("India_Karnataka_Koppal"), List.copyOf(asked.getValue()));
    }

    private JurisdictionScopeResolver.Scope scopeOf(String... grants) {
        return new JurisdictionScopeResolver.Scope(false, new java.util.LinkedHashSet<>(List.of(grants)));
    }
}
