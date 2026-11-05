package org.egov.inbox.repository.builder.V2;

import org.egov.common.contract.request.RequestInfo;
import org.egov.common.contract.request.Role;
import org.egov.common.contract.request.User;
import org.egov.inbox.util.RoleJurisdictionScope;
import org.egov.inbox.web.model.InboxSearchCriteria;
import org.egov.inbox.web.model.InboxRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

/**
 * The jurisdiction filter is two clauses: what the caller may see, and what they asked to see.
 * These tests pin the intersection, because getting it wrong is either a leak or an empty inbox
 * and neither is visible from a successful build.
 */
@ExtendWith(MockitoExtension.class)
class InboxJurisdictionClauseTest {

    @Mock
    private RoleJurisdictionScope roleJurisdictionScope;

    @InjectMocks
    private InboxQueryBuilder queryBuilder;

    private Map<String, String> nameToPathMap;

    @BeforeEach
    void setUp() {
        nameToPathMap = new HashMap<>();
        nameToPathMap.put("country", "Data.incident.boundary.countryCode.keyword");
        nameToPathMap.put("state", "Data.incident.boundary.stateCode.keyword");
        nameToPathMap.put("district", "Data.incident.boundary.districtCode.keyword");
        nameToPathMap.put("block", "Data.incident.boundary.blockCode.keyword");
        nameToPathMap.put("facility", "Data.incident.boundary.facilityCode.keyword");
        nameToPathMap.put("status", STATUS_PATH);
        nameToPathMap.put("assignee", ASSIGNEE_PATH);
    }

    private InboxRequest request(Map<String, Set<String>> statusesByRole, boolean assignedToMe) {
        return request(statusesByRole, assignedToMe, new String[0]);
    }

    private InboxRequest request(Map<String, Set<String>> statusesByRole, boolean assignedToMe,
                                 String... heldRoles) {
        InboxRequest inboxRequest = new InboxRequest();
        RequestInfo requestInfo = new RequestInfo();
        User userInfo = new User();
        userInfo.setUserName("me");
        List<Role> roles = new ArrayList<>();
        for (String code : heldRoles) {
            roles.add(Role.builder().code(code).build());
        }
        userInfo.setRoles(roles);
        requestInfo.setUserInfo(userInfo);
        inboxRequest.setRequestInfo(requestInfo);
        InboxSearchCriteria inbox = new InboxSearchCriteria();
        inbox.setActionableStatusesByRole(statusesByRole);
        inbox.setAssignedToMe(assignedToMe);
        inboxRequest.setInbox(inbox);
        return inboxRequest;
    }

    private static final String STATUS_PATH = "Data.incident.applicationStatus.keyword";
    private static final String ASSIGNEE_PATH = "Data.currentProcessInstance.assignes.userName.keyword";

    /** A scope holding one role, spelled out because Map.of is Java 9 and this module targets 8. */
    private <V> Map<String, V> singleScope(String roleCode, V value) {
        Map<String, V> scope = new LinkedHashMap<>();
        scope.put(roleCode, value);
        return scope;
    }

    /** Every role acts on something, which is the ordinary case. */
    private Map<String, Set<String>> actsOnAnything(String... roleCodes) {
        Map<String, Set<String>> statuses = new LinkedHashMap<>();
        for (String roleCode : roleCodes) {
            statuses.put(roleCode, new HashSet<>(Arrays.asList("SOME_STATUS")));
        }
        return statuses;
    }

    private Map<String, Set<String>> at(String level, String... codes) {
        Map<String, Set<String>> byLevel = new LinkedHashMap<>();
        byLevel.put(level, new HashSet<>(Arrays.asList(codes)));
        return byLevel;
    }

    @SuppressWarnings("unchecked")
    private List<Map<String, Object>> build(Map<String, Object> narrowing) {
        return build(narrowing, actsOnAnything("COMPLAINT_FACILITATOR_1", "COMPLAINT_FACILITATOR_2"), false);
    }

    @SuppressWarnings("unchecked")
    private List<Map<String, Object>> build(Map<String, Object> narrowing,
                                            Map<String, Set<String>> statusesByRole,
                                            boolean assignedToMe) {
        return build(narrowing, statusesByRole, assignedToMe, new String[0]);
    }

    @SuppressWarnings("unchecked")
    private List<Map<String, Object>> build(Map<String, Object> narrowing,
                                            Map<String, Set<String>> statusesByRole,
                                            boolean assignedToMe, String... heldRoles) {
        return (List<Map<String, Object>>) ReflectionTestUtils.invokeMethod(
                queryBuilder, "buildJurisdictionClauses",
                request(statusesByRole, assignedToMe, heldRoles), narrowing, nameToPathMap);
    }

    /** Every `term` clause in a subtree, as path -> value. */
    @SuppressWarnings("unchecked")
    private Map<String, String> termIn(Object node, Map<String, String> into) {
        if (node instanceof Map) {
            ((Map<String, Object>) node).forEach((key, value) -> {
                if ("term".equals(key) && value instanceof Map) {
                    ((Map<String, Object>) value).forEach((path, v) -> into.put(path, String.valueOf(v)));
                } else {
                    termIn(value, into);
                }
            });
        } else if (node instanceof List) {
            ((List<Object>) node).forEach(item -> termIn(item, into));
        }
        return into;
    }

    /** Every `terms` clause in a subtree, as path -> codes. */
    @SuppressWarnings("unchecked")
    private Map<String, List<String>> termsIn(Object node, Map<String, List<String>> into) {
        if (node instanceof Map) {
            ((Map<String, Object>) node).forEach((key, value) -> {
                if ("terms".equals(key) && value instanceof Map) {
                    ((Map<String, Object>) value).forEach((path, codes) ->
                            into.put(path, new ArrayList<>((List<String>) codes)));
                } else {
                    termsIn(value, into);
                }
            });
        } else if (node instanceof List) {
            ((List<Object>) node).forEach(item -> termsIn(item, into));
        }
        return into;
    }

    @Test
    void narrowingToOneOfTwoHeldStatesKeepsBothClauses() {
        when(roleJurisdictionScope.resolve(org.mockito.ArgumentMatchers.any()))
                .thenReturn(singleScope("COMPLAINT_FACILITATOR_1", at("state", "S1", "S2")));

        Map<String, Object> narrowing = new HashMap<>();
        narrowing.put("state", Arrays.asList("S1"));

        List<Map<String, Object>> clauses = build(narrowing);

        // Two separate must clauses - scope and narrowing - so ES intersects them.
        assertEquals(2, clauses.size(), "expected a scope clause and a narrowing clause");

        Map<String, List<String>> scope = termsIn(clauses.get(0), new HashMap<>());
        assertEquals(new HashSet<>(Arrays.asList("S1", "S2")),
                new HashSet<>(scope.get("Data.incident.boundary.stateCode.keyword")));

        Map<String, List<String>> narrowed = termsIn(clauses.get(1), new HashMap<>());
        assertEquals(Arrays.asList("S1"), narrowed.get("Data.incident.boundary.stateCode.keyword"));
    }

    @Test
    void narrowingAcrossLevelsIsAnIntersectionNotAReplacement() {
        // Holds a state; narrows to a facility inside it. Both clauses stand, and a ticket at that
        // facility carries both codes, which is why level-matching works without expansion.
        when(roleJurisdictionScope.resolve(org.mockito.ArgumentMatchers.any()))
                .thenReturn(singleScope("COMPLAINT_FACILITATOR_2", at("state", "S1")));

        Map<String, Object> narrowing = new HashMap<>();
        narrowing.put("facility", Arrays.asList("F1"));

        List<Map<String, Object>> clauses = build(narrowing);

        assertEquals(Arrays.asList("S1"),
                termsIn(clauses.get(0), new HashMap<>()).get("Data.incident.boundary.stateCode.keyword"));
        assertEquals(Arrays.asList("F1"),
                termsIn(clauses.get(1), new HashMap<>()).get("Data.incident.boundary.facilityCode.keyword"));
    }

    @Test
    void eachRoleKeepsItsOwnBoundaries() {
        Map<String, Map<String, Set<String>>> scope = new LinkedHashMap<>();
        scope.put("COMPLAINT_FACILITATOR_1", at("state", "S1"));
        scope.put("COMPLAINT_FACILITATOR_2", at("state", "S2"));
        when(roleJurisdictionScope.resolve(org.mockito.ArgumentMatchers.any())).thenReturn(scope);

        List<Map<String, Object>> clauses = build(new HashMap<>());

        assertEquals(1, clauses.size());
        Map<String, Object> bool = (Map<String, Object>) clauses.get(0).get("bool");
        List<Object> perRole = (List<Object>) bool.get("should");
        assertEquals(2, perRole.size(), "one clause per role, not one merged set");
        assertEquals(1, bool.get("minimum_should_match"));
    }

    @Test
    void eachRolesStatusesArePairedWithItsOwnBoundaries() {
        // The leak this closes: SPOC in S1, Tech POC in S2. A flat union would offer S1 tickets in
        // Tech-POC states, because the user holds that role - just not there.
        Map<String, Map<String, Set<String>>> scope = new LinkedHashMap<>();
        scope.put("COMPLAINT_FACILITATOR_1", at("state", "S1"));
        scope.put("COMPLAINT_FACILITATOR_2", at("state", "S2"));
        when(roleJurisdictionScope.resolve(org.mockito.ArgumentMatchers.any())).thenReturn(scope);

        Map<String, Set<String>> statuses = new LinkedHashMap<>();
        statuses.put("COMPLAINT_FACILITATOR_1", new HashSet<>(Arrays.asList("PENDING_AT_SPOC")));
        statuses.put("COMPLAINT_FACILITATOR_2", new HashSet<>(Arrays.asList("PENDING_AT_TECH_POC")));

        List<Map<String, Object>> clauses = build(new HashMap<>(), statuses, true,
                "COMPLAINT_FACILITATOR_1", "COMPLAINT_FACILITATOR_2");

        List<Object> perRole = (List<Object>) ((Map<String, Object>) clauses.get(0).get("bool")).get("should");
        assertEquals(2, perRole.size());

        for (Object role : perRole) {
            List<Object> pair = (List<Object>) ((Map<String, Object>) ((Map<String, Object>) role).get("bool")).get("must");
            assertEquals(2, pair.size(), "each role pairs its boundaries with its own statuses");
            Map<String, List<String>> terms = termsIn(role, new HashMap<>());
            String state = terms.get("Data.incident.boundary.stateCode.keyword").get(0);
            String status = terms.get("Data.incident.applicationStatus.keyword").get(0);
            // S1 goes with the SPOC status, S2 with the Tech POC one - never crossed.
            assertEquals("S1".equals(state) ? "PENDING_AT_SPOC" : "PENDING_AT_TECH_POC", status);
        }
    }

    @Test
    void withoutAssignedToMeTheRolePairCarriesBoundariesOnly() {
        when(roleJurisdictionScope.resolve(org.mockito.ArgumentMatchers.any()))
                .thenReturn(singleScope("COMPLAINT_FACILITATOR_1", at("state", "S1")));

        List<Map<String, Object>> clauses = build(new HashMap<>(),
                actsOnAnything("COMPLAINT_FACILITATOR_1"), false);

        List<Object> perRole = (List<Object>) ((Map<String, Object>) clauses.get(0).get("bool")).get("should");
        List<Object> pair = (List<Object>) ((Map<String, Object>) ((Map<String, Object>) perRole.get(0)).get("bool")).get("must");
        assertEquals(1, pair.size(), "no status pairing when the caller asked for everything");
    }

    @Test
    void aRoleThatActsOnNothingHereGrantsNoVisibility() {
        // Facility Admin in STATE_2, Complaint Assessor in STATE_1. Only the assessor role appears
        // in the incident workflow, so only STATE_1 belongs in this inbox - holding ground in
        // STATE_2 under a role with no part in it must not list STATE_2 tickets.
        Map<String, Map<String, Set<String>>> scope = new LinkedHashMap<>();
        scope.put("FACILITY_ADMIN", at("state", "STATE_2"));
        scope.put("COMPLAINT_ASSESSOR", at("state", "STATE_1"));
        when(roleJurisdictionScope.resolve(org.mockito.ArgumentMatchers.any())).thenReturn(scope);

        List<Map<String, Object>> clauses = build(new HashMap<>(),
                actsOnAnything("COMPLAINT_ASSESSOR"), false);

        List<Object> perRole = (List<Object>) ((Map<String, Object>) clauses.get(0).get("bool")).get("should");
        assertEquals(1, perRole.size(), "only the role that acts on incidents contributes");
        Map<String, List<String>> terms = termsIn(clauses.get(0), new HashMap<>());
        assertEquals(Arrays.asList("STATE_1"), terms.get("Data.incident.boundary.stateCode.keyword"));
    }

    @Test
    void holdingGroundButNoRelevantRoleMatchesNothing() {
        when(roleJurisdictionScope.resolve(org.mockito.ArgumentMatchers.any()))
                .thenReturn(singleScope("FACILITY_ADMIN", at("state", "STATE_2")));

        List<Map<String, Object>> clauses = build(new HashMap<>(), actsOnAnything(), false);

        assertEquals(1, clauses.size());
        assertTrue(((Map<String, Object>) clauses.get(0).get("bool")).containsKey("must_not"),
                "a jurisdiction under an irrelevant role is not visibility");
    }

    // --- what "mine" means per role ---

    @Test
    void aCrmUserGetsTheAssessorPairingAndNoUnrestrictedBranch() {
        // CRM holds COMPLAINT_ASSESSOR (pooled) and COMPLAINANT (not). The complainant role must
        // not contribute a boundaries-only branch: that branch would be unrestricted and would
        // widen the query back to the user's whole jurisdiction, undoing the assessor pairing.
        Map<String, Map<String, Set<String>>> scope = new LinkedHashMap<>();
        scope.put("COMPLAINT_ASSESSOR", at("state", "S1"));
        scope.put("COMPLAINANT", at("state", "S1"));
        when(roleJurisdictionScope.resolve(org.mockito.ArgumentMatchers.any())).thenReturn(scope);

        Map<String, Set<String>> statuses = new LinkedHashMap<>();
        statuses.put("COMPLAINT_ASSESSOR", new HashSet<>(Arrays.asList("PENDINGFORASSIGNMENT")));
        statuses.put("COMPLAINANT", new HashSet<>(Arrays.asList("RESOLVED")));

        List<Map<String, Object>> clauses = build(new HashMap<>(), statuses, true,
                "COMPLAINT_ASSESSOR", "COMPLAINANT");

        List<Object> perRole = (List<Object>) ((Map<String, Object>) clauses.get(0).get("bool")).get("should");
        assertEquals(2, perRole.size());
        for (Object role : perRole) {
            List<Object> pair = (List<Object>) ((Map<String, Object>) ((Map<String, Object>) role).get("bool")).get("must");
            assertEquals(2, pair.size(), "every branch is boundaries AND a mine test, never boundaries alone");
        }
        // the assessor branch narrows by status, the complainant branch by assignee
        Map<String, List<String>> terms = termsIn(clauses.get(0), new HashMap<>());
        assertEquals(Arrays.asList("PENDINGFORASSIGNMENT"), terms.get(STATUS_PATH));
        assertEquals("me", termIn(clauses.get(0), new HashMap<>()).get(ASSIGNEE_PATH));
    }

    @Test
    void anHcrUserIsMatchedByAssignee() {
        when(roleJurisdictionScope.resolve(org.mockito.ArgumentMatchers.any()))
                .thenReturn(singleScope("COMPLAINANT", at("state", "S1")));

        List<Map<String, Object>> clauses = build(new HashMap<>(),
                actsOnAnything("COMPLAINANT"), true, "COMPLAINANT");

        assertEquals("me", termIn(clauses.get(0), new HashMap<>()).get(ASSIGNEE_PATH));
        assertNull(termsIn(clauses.get(0), new HashMap<>()).get(STATUS_PATH),
                "a complainant is not matched by the statuses they can act on");
    }

    @Test
    void aVendorIsMatchedByAssigneeWithNoStatusClamp() {
        when(roleJurisdictionScope.resolve(org.mockito.ArgumentMatchers.any()))
                .thenReturn(singleScope("COMPLAINT_RESOLVER", at("state", "S1")));

        List<Map<String, Object>> clauses = build(new HashMap<>(),
                actsOnAnything("COMPLAINT_RESOLVER"), true, "COMPLAINT_RESOLVER");

        assertEquals("me", termIn(clauses.get(0), new HashMap<>()).get(ASSIGNEE_PATH));
        assertNull(termsIn(clauses.get(0), new HashMap<>()).get(STATUS_PATH),
                "no status clamp, so the vendor's own closed tickets stay visible");
    }

    @Test
    void identitySurvivesAScopeThatFailedOpen() {
        // HRMS unreachable: boundaries fail open, but a vendor must not thereby see every ticket
        // in the tenant - the browser no longer sends an assignee of its own.
        when(roleJurisdictionScope.resolve(org.mockito.ArgumentMatchers.any())).thenReturn(null);

        List<Map<String, Object>> clauses = build(new HashMap<>(),
                actsOnAnything("COMPLAINT_RESOLVER"), true, "COMPLAINT_RESOLVER");

        assertEquals(1, clauses.size(), "no scope clause, but the identity clause stands");
        assertEquals("me", termIn(clauses.get(0), new HashMap<>()).get(ASSIGNEE_PATH));
    }

    @Test
    void theBrowsersEmptyPlaceholderIsNotTreatedAsABoundary() {
        // useNewInbox.js sends {country: ["-"]} when session storage is empty. Taken literally it
        // would AND an impossible country code against everything.
        when(roleJurisdictionScope.resolve(org.mockito.ArgumentMatchers.any()))
                .thenReturn(singleScope("COMPLAINT_FACILITATOR_1", at("state", "S1")));

        Map<String, Object> narrowing = new HashMap<>();
        narrowing.put("country", Arrays.asList("-"));

        List<Map<String, Object>> clauses = build(narrowing);

        assertEquals(1, clauses.size(), "the placeholder must not add a narrowing clause");
        assertTrue(termsIn(clauses.get(0), new HashMap<>())
                .containsKey("Data.incident.boundary.stateCode.keyword"));
    }

    @Test
    void anUnresolvableScopeFailsOpenButKeepsTheUsersFilter() {
        when(roleJurisdictionScope.resolve(org.mockito.ArgumentMatchers.any())).thenReturn(null);

        Map<String, Object> narrowing = new HashMap<>();
        narrowing.put("state", Arrays.asList("S1"));

        List<Map<String, Object>> clauses = build(narrowing);

        assertEquals(1, clauses.size(), "no scope clause, but the narrowing still applies");
        assertEquals(Arrays.asList("S1"),
                termsIn(clauses.get(0), new HashMap<>()).get("Data.incident.boundary.stateCode.keyword"));
    }

    @Test
    void holdingNothingMatchesNothing() {
        // Distinct from null: HRMS answered, and the answer was "no jurisdictions".
        when(roleJurisdictionScope.resolve(org.mockito.ArgumentMatchers.any()))
                .thenReturn(new LinkedHashMap<>());

        List<Map<String, Object>> clauses = build(new HashMap<>());

        assertEquals(1, clauses.size());
        Map<String, Object> bool = (Map<String, Object>) clauses.get(0).get("bool");
        assertTrue(bool.containsKey("must_not"), "expected a clause that matches nothing");
    }
}
