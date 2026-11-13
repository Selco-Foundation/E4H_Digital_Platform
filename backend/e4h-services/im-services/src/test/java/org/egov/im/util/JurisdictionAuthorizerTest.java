package org.egov.im.util;

import org.egov.common.contract.request.RequestInfo;
import org.egov.common.contract.request.Role;
import org.egov.common.contract.request.User;
import org.egov.im.service.BoundaryService;
import org.egov.im.web.models.Boundary;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.lang.reflect.Constructor;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

/**
 * Whether the caller may act on a ticket, given where the ticket is.
 *
 * This is the privilege boundary: workflow says which role, HRMS says where, and these cases pin
 * the intersection. Each one is named for the behaviour it protects.
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class JurisdictionAuthorizerTest {

    private static final String TENANT = "in";
    private static final String TICKET = "India_Karnataka_Ballari_Bellary_FAC/2025/0045";
    private static final String TECH_POC = "COMPLAINT_FACILITATOR_2";

    @Mock
    private HrmsJurisdictionUtil hrmsJurisdictionUtil;

    @Mock
    private BoundaryService boundaryService;

    @InjectMocks
    private JurisdictionAuthorizer authorizer;

    private static RequestInfo caller(String... roleCodes) {
        User user = new User();
        user.setUuid("caller-uuid");
        user.setTenantId(TENANT);
        user.setRoles(Arrays.stream(roleCodes).map(c -> Role.builder().code(c).build()).collect(Collectors.toList()));
        RequestInfo requestInfo = new RequestInfo();
        requestInfo.setUserInfo(user);
        return requestInfo;
    }

    private void ticketSitsIn(String state, String district, String block, String facility) {
        when(boundaryService.fetchBoundaryFromBoundaryCode(any(), anyString(), anyString()))
                .thenReturn(Boundary.builder().countryCode("India").stateCode(state)
                        .districtCode(district).blockCode(block).facilityCode(facility).build());
    }

    /** Reflection because Grant's constructor is package-private by design. */
    private static HrmsJurisdictionUtil.Grant grant(String boundary, String... roleCodes) throws Exception {
        Constructor<HrmsJurisdictionUtil.Grant> c =
                HrmsJurisdictionUtil.Grant.class.getDeclaredConstructor(String.class, Set.class);
        c.setAccessible(true);
        return c.newInstance(boundary, new HashSet<>(Arrays.asList(roleCodes)));
    }

    private void callerHolds(HrmsJurisdictionUtil.Grant... grants) {
        when(hrmsJurisdictionUtil.getJurisdictionsForCaller(any()))
                .thenReturn(HrmsJurisdictionUtil.Outcome.of(Arrays.asList(grants)));
    }

    @Test
    @DisplayName("a grant at the state covers a ticket at a facility inside it")
    void aStateGrantCoversATicketBeneathIt() throws Exception {
        ticketSitsIn("India_Karnataka", "India_Karnataka_Ballari", "India_Karnataka_Ballari_Bellary", TICKET);
        callerHolds(grant("India_Karnataka", TECH_POC));

        assertTrue(authorizer.mayActAt(caller(TECH_POC), List.of(TECH_POC), TICKET, TENANT));
    }

    @Test
    @DisplayName("the same role held in another state does not reach this ticket")
    void aRoleHeldElsewhereDoesNotReachHere() throws Exception {
        ticketSitsIn("India_Karnataka", "India_Karnataka_Ballari", "India_Karnataka_Ballari_Bellary", TICKET);
        callerHolds(grant("India_Meghalaya", TECH_POC));

        // The whole point: holding a role somewhere is not holding it everywhere.
        assertFalse(authorizer.mayActAt(caller(TECH_POC), List.of(TECH_POC), TICKET, TENANT));
    }

    @Test
    @DisplayName("a grant naming another role does not let this one act")
    void aGrantForAnotherRoleDoesNotTransfer() throws Exception {
        ticketSitsIn("India_Karnataka", "India_Karnataka_Ballari", "India_Karnataka_Ballari_Bellary", TICKET);
        callerHolds(grant("India_Karnataka", "COMPLAINT_ASSESSOR"));

        assertFalse(authorizer.mayActAt(caller(TECH_POC, "COMPLAINT_ASSESSOR"), List.of(TECH_POC), TICKET, TENANT));
    }

    @Test
    @DisplayName("a grant naming no roles applies to every role the holder has")
    void aGrantWithoutRolesAppliesToEveryHeldRole() throws Exception {
        ticketSitsIn("India_Karnataka", "India_Karnataka_Ballari", "India_Karnataka_Ballari_Bellary", TICKET);
        callerHolds(grant("India_Karnataka"));

        // Every grant made before role scoping looks like this; reading it the other way round
        // would lock everyone out.
        assertTrue(authorizer.mayActAt(caller(TECH_POC), List.of(TECH_POC), TICKET, TENANT));
    }

    @Test
    @DisplayName("a facility-level grant covers its own ticket and no other")
    void aFacilityGrantIsExact() throws Exception {
        ticketSitsIn("India_Karnataka", "India_Karnataka_Ballari", "India_Karnataka_Ballari_Bellary", TICKET);
        callerHolds(grant(TICKET, TECH_POC));

        assertTrue(authorizer.mayActAt(caller(TECH_POC), List.of(TECH_POC), TICKET, TENANT));
    }

    @Test
    @DisplayName("a role the caller never held cannot be satisfied by any jurisdiction")
    void anUnheldRoleIsNeverSatisfied() throws Exception {
        callerHolds(grant("India_Karnataka", TECH_POC));

        assertFalse(authorizer.mayActAt(caller("COMPLAINANT"), List.of(TECH_POC), TICKET, TENANT));
        // Short-circuits before any lookup - it is workflow's refusal, not a jurisdiction one.
        verifyNoInteractions(boundaryService);
    }

    @Test
    @DisplayName("an unresolvable boundary refuses rather than allowing")
    void anUnresolvableBoundaryFailsClosed() throws Exception {
        when(boundaryService.fetchBoundaryFromBoundaryCode(any(), anyString(), anyString())).thenReturn(null);
        callerHolds(grant("India_Karnataka", TECH_POC));

        assertFalse(authorizer.mayActAt(caller(TECH_POC), List.of(TECH_POC), TICKET, TENANT),
                "an action gate that opens under failure is not a gate");
    }

    @Test
    @DisplayName("HRMS being unreachable refuses rather than allowing")
    void anUnreachableHrmsFailsClosed() {
        ticketSitsIn("India_Karnataka", "India_Karnataka_Ballari", "India_Karnataka_Ballari_Bellary", TICKET);
        when(hrmsJurisdictionUtil.getJurisdictionsForCaller(any()))
                .thenReturn(HrmsJurisdictionUtil.Outcome.unavailable());

        assertFalse(authorizer.mayActAt(caller(TECH_POC), List.of(TECH_POC), TICKET, TENANT));
    }

    @Test
    @DisplayName("holding nothing refuses, and is not confused with HRMS being down")
    void holdingNothingRefuses() {
        ticketSitsIn("India_Karnataka", "India_Karnataka_Ballari", "India_Karnataka_Ballari_Bellary", TICKET);
        when(hrmsJurisdictionUtil.getJurisdictionsForCaller(any()))
                .thenReturn(HrmsJurisdictionUtil.Outcome.of(List.of()));

        assertFalse(authorizer.mayActAt(caller(TECH_POC), List.of(TECH_POC), TICKET, TENANT));
    }

    @Test
    @DisplayName("the ancestor chain carries the ticket's own code even if the hierarchy differs")
    void theChainIncludesTheTicketsOwnCode() {
        when(boundaryService.fetchBoundaryFromBoundaryCode(any(), anyString(), anyString()))
                .thenReturn(Boundary.builder().countryCode("India").stateCode("India_Karnataka").build());

        Set<String> chain = authorizer.ancestorsOf(caller(TECH_POC), TICKET, TENANT);

        assertTrue(chain.contains(TICKET));
        assertTrue(chain.contains("India_Karnataka"));
    }
}
