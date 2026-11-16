package facility.util;

import facility.config.Configuration;
import facility.web.models.FacilityScope;
import facility.web.models.Jurisdiction;
import facility.web.models.Role;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.egov.common.contract.request.RequestInfo;
import org.egov.common.contract.request.User;

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

/**
 * The decision table in RoleScopeResolver, one behaviour per test.
 *
 * Everything below the resolver - the HRMS call and the boundary walk - is mocked, so what these
 * pin is the policy: who is exempt, what a missing or forged header does, which grants apply to
 * which role, and that a failure refuses rather than quietly returning everything.
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class RoleScopeResolverTest {

    private static final String UUID = "caller-uuid";
    private static final String FA = "FACILITY_ADMIN";

    @Mock
    private HRMSUtils hrmsUtils;

    @Mock
    private BoundaryUtil boundaryUtil;

    private Configuration configs;

    @BeforeEach
    void setUp() {
        configs = new Configuration();
        configs.setJurisdictionScopeMode("enforce");
        configs.setJurisdictionSystemRoles(Arrays.asList("INTERNAL_MICROSERVICE_ROLE", "SYSTEM_USER", "SYSTEM"));
        configs.setInternalMicroserviceUserUuid("internal-uuid");
    }

    private RoleScopeResolver resolver() {
        return new RoleScopeResolver(configs, hrmsUtils, boundaryUtil);
    }

    private static RequestInfo caller(String uuid, String type, String... roleCodes) {
        RequestInfo requestInfo = new RequestInfo();
        User user = new User();
        user.setUuid(uuid);
        user.setType(type);
        user.setRoles(Arrays.stream(roleCodes)
                .map(c -> org.egov.common.contract.request.Role.builder().code(c).build())
                .collect(Collectors.toList()));
        requestInfo.setUserInfo(user);
        return requestInfo;
    }

    private static Jurisdiction grant(String boundary, String boundaryType, Boolean active, String... roleCodes) {
        List<Role> roles = roleCodes.length == 0 ? null
                : Arrays.stream(roleCodes).map(c -> Role.builder().code(c).build()).collect(Collectors.toList());
        return Jurisdiction.builder()
                .boundary(boundary).boundaryType(boundaryType).isActive(active).roles(roles)
                .build();
    }

    private void hrmsReturns(Jurisdiction... grants) {
        when(hrmsUtils.getActiveJurisdictionsForCaller(any()))
                .thenReturn(JurisdictionGrants.ok(Arrays.asList(grants)));
    }

    private void boundaryExpands(String code, String type, String... facilities) throws Exception {
        when(boundaryUtil.expandToFacilityCodes(eq(code), eq(type)))
                .thenReturn(new LinkedHashSet<>(Arrays.asList(facilities)));
    }

    @Test
    @DisplayName("with the flag off nothing is looked up at all")
    void flagOffShortCircuits() {
        configs.setJurisdictionScopeMode("off");

        FacilityScope scope = resolver().resolve(caller(UUID, null, FA), FA);

        assertEquals(FacilityScope.Decision.UNSCOPED_FLAG_OFF, scope.decision());
        verifyNoInteractions(hrmsUtils, boundaryUtil);
    }

    @Test
    @DisplayName("an unrecognised mode fails at startup rather than silently meaning off")
    void anUnknownModeRefusesToStart() {
        configs.setJurisdictionScopeMode("enforced");
        org.junit.jupiter.api.Assertions.assertThrows(IllegalStateException.class, this::resolver,
                "a typo must not quietly disable a security control");
    }

    @Test
    @DisplayName("a service is exempt on identity, before the header is even read")
    void aSystemRoleIsExemptWhateverTheHeaderSays() {
        FacilityScope scope = resolver().resolve(caller("svc", null, "INTERNAL_MICROSERVICE_ROLE"), "NONSENSE");

        assertEquals(FacilityScope.Decision.UNSCOPED_SYSTEM, scope.decision());
        verifyNoInteractions(hrmsUtils);
    }

    @Test
    @DisplayName("a SYSTEM user type is exempt")
    void aSystemUserTypeIsExempt() {
        assertEquals(FacilityScope.Decision.UNSCOPED_SYSTEM,
                resolver().resolve(caller("svc", "SYSTEM"), null).decision());
    }

    @Test
    @DisplayName("the configured internal microservice uuid is exempt")
    void theInternalMicroserviceUuidIsExempt() {
        assertEquals(FacilityScope.Decision.UNSCOPED_SYSTEM,
                resolver().resolve(caller("internal-uuid", null), null).decision());
    }

    @Test
    @DisplayName("a person who declares no acting role is refused")
    void aMissingHeaderIsRefused() {
        assertEquals(FacilityScope.Decision.DENY_MISSING_ACTING_ROLE,
                resolver().resolve(caller(UUID, null, FA), null).decision());
        assertEquals(FacilityScope.Decision.DENY_MISSING_ACTING_ROLE,
                resolver().resolve(caller(UUID, null, FA), "   ").decision());
    }

    @Test
    @DisplayName("declaring a role you do not hold is refused, so the header cannot acquire scope")
    void anUnheldActingRoleIsRefused() {
        FacilityScope scope = resolver().resolve(caller(UUID, null, FA), "PROJECT_MANAGER");

        assertEquals(FacilityScope.Decision.DENY_INVALID_ACTING_ROLE, scope.decision());
        verifyNoInteractions(hrmsUtils);
    }

    @Test
    @DisplayName("HRMS not answering refuses the request rather than answering it unscoped")
    void anHrmsFailureFailsClosed() {
        when(hrmsUtils.getActiveJurisdictionsForCaller(any()))
                .thenReturn(JurisdictionGrants.failed(JurisdictionGrants.Status.HRMS_UNAVAILABLE));

        FacilityScope scope = resolver().resolve(caller(UUID, null, FA), FA);

        assertEquals(FacilityScope.Decision.DENY_SCOPE_UNAVAILABLE, scope.decision());
        verifyNoInteractions(boundaryUtil);
    }

    @Test
    @DisplayName("a grant naming no roles applies to whatever role the holder is acting as")
    void aGrantWithoutRolesAppliesToEveryHeldRole() throws Exception {
        hrmsReturns(grant("S1", "State", true));
        boundaryExpands("S1", "State", "F1", "F2");

        FacilityScope scope = resolver().resolve(caller(UUID, null, FA), FA);

        assertEquals(FacilityScope.Decision.SCOPED, scope.decision());
        assertEquals(new LinkedHashSet<>(Arrays.asList("F1", "F2")), scope.ceiling(),
                "every pre-scoping grant looks like this; reading it the other way empties the platform");
    }

    @Test
    @DisplayName("a grant naming another role contributes nothing")
    void aGrantForAnotherRoleIsIgnored() {
        hrmsReturns(grant("S1", "State", true, "PROJECT_MANAGER"));

        FacilityScope scope = resolver().resolve(caller(UUID, null, FA, "PROJECT_MANAGER"), FA);

        assertEquals(FacilityScope.Decision.DENY_NO_JURISDICTION, scope.decision(),
                "acting as one role must not borrow another role's area");
    }

    @Test
    @DisplayName("the resolver trusts HRMSUtils to have filtered inactive grants already")
    void inactiveGrantsAreFilteredBeforeTheResolverSeesThem() throws Exception {
        // Where that filtering lives is tested in HRMSUtilsTest. Keeping it out of here is the
        // point: the resolver's job is role matching and expansion, not activity.
        hrmsReturns(grant("S2", "State", null));
        boundaryExpands("S2", "State", "F9");

        assertEquals(Collections.singleton("F9"), resolver().resolve(caller(UUID, null, FA), FA).ceiling());
    }

    @Test
    @DisplayName("grants at different levels union into one ceiling")
    void grantsAtDifferentLevelsUnion() throws Exception {
        hrmsReturns(grant("S1", "State", true), grant("B9", "Block", true));
        boundaryExpands("S1", "State", "F1", "F2");
        boundaryExpands("B9", "Block", "F7");

        FacilityScope scope = resolver().resolve(caller(UUID, null, FA), FA);

        assertEquals(new LinkedHashSet<>(Arrays.asList("F1", "F2", "F7")), scope.ceiling(),
                "holding a state here and a block elsewhere means both, not the narrower one");
    }

    @Test
    @DisplayName("every resolve re-reads the caller's grants and re-expands them")
    void nothingIsCachedBetweenRequests() throws Exception {
        hrmsReturns(grant("S1", "State", true));
        boundaryExpands("S1", "State", "F1");

        RoleScopeResolver resolver = resolver();
        resolver.resolve(caller(UUID, null, FA), FA);
        resolver.resolve(caller(UUID, null, FA), FA);

        // Deliberate, and the cost nobody has measured yet: with scope.mode=off these calls never
        // happen, so warn mode is the first chance to see what they cost on real traffic. Pinned so
        // that reintroducing a cache is a decision someone makes against this test, rather than a
        // change that passes unnoticed - and so that a per-pod cache is not added without first
        // weighing the shared Redis the platform already runs.
        verify(hrmsUtils, times(2)).getActiveJurisdictionsForCaller(any());
        verify(boundaryUtil, times(2)).expandToFacilityCodes(eq("S1"), eq("State"));
    }

    @Test
    @DisplayName("a failed lookup does not stick: the next request asks again")
    void failuresAreNotCached() {
        when(hrmsUtils.getActiveJurisdictionsForCaller(any()))
                .thenReturn(JurisdictionGrants.failed(JurisdictionGrants.Status.HRMS_UNAVAILABLE));

        RoleScopeResolver resolver = resolver();
        resolver.resolve(caller(UUID, null, FA), FA);
        resolver.resolve(caller(UUID, null, FA), FA);

        verify(hrmsUtils, times(2)).getActiveJurisdictionsForCaller(any());
    }

    @Test
    @DisplayName("warn mode computes the ceiling but does not apply it")
    void warnModeComputesWithoutApplying() throws Exception {
        configs.setJurisdictionScopeMode("warn");
        hrmsReturns(grant("S1", "State", true));
        boundaryExpands("S1", "State", "F1");

        FacilityScope scope = resolver().resolve(caller(UUID, null, FA), FA);

        assertEquals(FacilityScope.Decision.UNSCOPED_WARN, scope.decision());
        assertTrue(scope.ceiling().contains("F1"), "the would-be ceiling is what the audit log reports");
        assertTrue(!scope.isApplied(), "warn mode must not filter anything");
    }

    @Test
    @DisplayName("in warn mode a missing header is logged, not refused")
    void warnModeDoesNotRefuseAMissingHeader() {
        configs.setJurisdictionScopeMode("warn");

        FacilityScope scope = resolver().resolve(caller(UUID, null, FA), null);

        assertEquals(FacilityScope.Decision.UNSCOPED_WARN, scope.decision());
        assertTrue(!scope.isDeny(), "warn mode exists precisely so this traffic can be classified first");
    }

    @Test
    @DisplayName("several declared roles union their jurisdictions")
    void severalActingRolesUnionTheirGrants() throws Exception {
        // IM's case: one person is assessor in one place and facilitator in another, and a facility
        // must be visible to them whichever hat they are wearing. Picking the "broadest" single role
        // cannot express this - no one role covers both.
        hrmsReturns(grant("S1", "State", true, "COMPLAINT_ASSESSOR"),
                grant("S2", "State", true, "COMPLAINT_FACILITATOR_2"));
        boundaryExpands("S1", "State", "F1");
        boundaryExpands("S2", "State", "F2");

        FacilityScope scope = resolver().resolve(
                caller(UUID, null, "COMPLAINT_ASSESSOR", "COMPLAINT_FACILITATOR_2"),
                "COMPLAINT_ASSESSOR,COMPLAINT_FACILITATOR_2");

        assertEquals(FacilityScope.Decision.SCOPED, scope.decision());
        assertEquals(new LinkedHashSet<>(Arrays.asList("F1", "F2")), scope.ceiling());
    }

    @Test
    @DisplayName("a role outside the declared list still contributes nothing")
    void aRoleNotDeclaredIsStillExcluded() throws Exception {
        // The whole reason the caller names its roles: the Management Hub's pm grant must not widen
        // an IM screen just because the same person holds both.
        hrmsReturns(grant("S1", "State", true, "COMPLAINANT"),
                grant("S2", "State", true, "PROJECT_MANAGER"));
        boundaryExpands("S1", "State", "F1");

        FacilityScope scope = resolver().resolve(
                caller(UUID, null, "COMPLAINANT", "PROJECT_MANAGER"), "COMPLAINANT");

        assertEquals(Collections.singleton("F1"), scope.ceiling());
        verify(boundaryUtil, never()).expandToFacilityCodes(eq("S2"), anyString());
    }

    @Test
    @DisplayName("one unheld role in the list refuses the whole request")
    void oneUnheldRoleInTheListIsRefused() {
        // Not "drop it and carry on": a typo would then silently change the answer while still
        // looking like it worked, which is the failure mode this control exists to avoid.
        FacilityScope scope = resolver().resolve(caller(UUID, null, FA), FA + ",PROJECT_MANAGER");

        assertEquals(FacilityScope.Decision.DENY_INVALID_ACTING_ROLE, scope.decision());
        verifyNoInteractions(hrmsUtils);
    }

    @Test
    @DisplayName("blank entries and spacing in the header are tolerated")
    void theHeaderListIsParsedLeniently() throws Exception {
        hrmsReturns(grant("S1", "State", true, FA));
        boundaryExpands("S1", "State", "F1");

        FacilityScope scope = resolver().resolve(caller(UUID, null, FA), "  " + FA + " , ,");

        assertEquals(FacilityScope.Decision.SCOPED, scope.decision());
        assertEquals(FA, scope.actingRole(), "the audit log should read as the roles that applied");
    }

    @Test
    @DisplayName("a list of only separators counts as no header at all")
    void aListOfOnlySeparatorsIsAMissingHeader() {
        assertEquals(FacilityScope.Decision.DENY_MISSING_ACTING_ROLE,
                resolver().resolve(caller(UUID, null, FA), " , , ").decision());
    }
}
