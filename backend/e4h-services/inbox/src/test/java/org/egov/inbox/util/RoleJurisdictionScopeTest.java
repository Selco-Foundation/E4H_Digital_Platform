package org.egov.inbox.util;

import org.egov.common.contract.request.RequestInfo;
import org.egov.common.contract.request.Role;
import org.egov.common.contract.request.User;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

/**
 * Grants become {@code role -> level -> codes} at the grant's own level, so a district or block
 * holder is scoped by the ticket's district or block code, never widened to the state.
 */
@ExtendWith(MockitoExtension.class)
class RoleJurisdictionScopeTest {

    @Mock
    private HRMSUtil hrmsUtil;

    @InjectMocks
    private RoleJurisdictionScope scope;

    private static HRMSUtil.JurisdictionGrant grant(String boundary, String boundaryType, String... roles) {
        return new HRMSUtil.JurisdictionGrant(boundary, boundaryType, new HashSet<>(Arrays.asList(roles)));
    }

    private static RequestInfo caller(String... roles) {
        List<Role> held = new ArrayList<>();
        for (String code : roles) {
            held.add(Role.builder().code(code).build());
        }
        User user = new User();
        user.setRoles(held);
        RequestInfo requestInfo = new RequestInfo();
        requestInfo.setUserInfo(user);
        return requestInfo;
    }

    private static Set<String> codes(String... codes) {
        return new HashSet<>(Arrays.asList(codes));
    }

    @Test
    void districtAndBlockGrantsAreScopedAtTheirOwnLevel() {
        when(hrmsUtil.getJurisdictionsForCaller(any())).thenReturn(Arrays.asList(
                grant("D1", "District"), grant("D2", "District"), grant("B9", "Block")));

        Map<String, Map<String, Set<String>>> resolved = scope.resolve(caller("COMPLAINT_ASSESSOR"));

        Map<String, Set<String>> crm = resolved.get("COMPLAINT_ASSESSOR");
        assertEquals(codes("D1", "D2"), crm.get("district"));
        assertEquals(codes("B9"), crm.get("block"));
        assertFalse(crm.containsKey("state"), "a district holder must not be widened to the state");
    }

    @Test
    void boundaryTypeIsMatchedWithoutCase() {
        when(hrmsUtil.getJurisdictionsForCaller(any())).thenReturn(Arrays.asList(
                grant("D1", "district"), grant("B9", " BLOCK "), grant("S1", "state")));

        Map<String, Set<String>> spoc = scope.resolve(caller("COMPLAINT_FACILITATOR_1")).get("COMPLAINT_FACILITATOR_1");

        assertEquals(codes("D1"), spoc.get("district"));
        assertEquals(codes("B9"), spoc.get("block"));
        assertEquals(codes("S1"), spoc.get("state"));
    }

    @Test
    void roleScopedGrantsAtDifferentLevelsStayWithTheirRoles() {
        // State CRM who is also SPOC for one district: the district grant must not narrow CRM work,
        // and the state grant must not widen SPOC work.
        when(hrmsUtil.getJurisdictionsForCaller(any())).thenReturn(Arrays.asList(
                grant("S1", "State", "COMPLAINT_ASSESSOR"),
                grant("D1", "District", "COMPLAINT_FACILITATOR_1")));

        Map<String, Map<String, Set<String>>> resolved =
                scope.resolve(caller("COMPLAINT_ASSESSOR", "COMPLAINT_FACILITATOR_1"));

        assertEquals(Collections.singletonMap("state", codes("S1")), resolved.get("COMPLAINT_ASSESSOR"));
        assertEquals(Collections.singletonMap("district", codes("D1")), resolved.get("COMPLAINT_FACILITATOR_1"));
    }

    @Test
    void unknownBoundaryTypeIsIgnored() {
        when(hrmsUtil.getJurisdictionsForCaller(any())).thenReturn(Arrays.asList(
                grant("X1", "Ward"), grant("D1", "District")));

        Map<String, Set<String>> crm = scope.resolve(caller("COMPLAINT_ASSESSOR")).get("COMPLAINT_ASSESSOR");

        assertEquals(Collections.singletonMap("district", codes("D1")), crm);
    }

    @Test
    void noGrantsLeavesTheInboxUnscoped() {
        when(hrmsUtil.getJurisdictionsForCaller(any())).thenReturn(Collections.emptyList());

        assertNull(scope.resolve(caller("COMPLAINT_ASSESSOR")));
    }
}
