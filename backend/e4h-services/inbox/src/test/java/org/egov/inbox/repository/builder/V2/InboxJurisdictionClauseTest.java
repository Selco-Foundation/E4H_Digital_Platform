package org.egov.inbox.repository.builder.V2;

import org.egov.common.contract.request.RequestInfo;
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
        nameToPathMap.put("status", "Data.incident.applicationStatus.keyword");
    }

    private InboxRequest request() {
        return request(null);
    }

    private InboxRequest request(Map<String, Set<String>> statusesByRole) {
        InboxRequest inboxRequest = new InboxRequest();
        inboxRequest.setRequestInfo(new RequestInfo());
        InboxSearchCriteria inbox = new InboxSearchCriteria();
        inbox.setActionableStatusesByRole(statusesByRole);
        inboxRequest.setInbox(inbox);
        return inboxRequest;
    }

    private Map<String, Set<String>> at(String level, String... codes) {
        Map<String, Set<String>> byLevel = new LinkedHashMap<>();
        byLevel.put(level, new HashSet<>(Arrays.asList(codes)));
        return byLevel;
    }

    @SuppressWarnings("unchecked")
    private List<Map<String, Object>> build(Map<String, Object> narrowing) {
        return build(narrowing, null);
    }

    @SuppressWarnings("unchecked")
    private List<Map<String, Object>> build(Map<String, Object> narrowing,
                                            Map<String, Set<String>> statusesByRole) {
        return (List<Map<String, Object>>) ReflectionTestUtils.invokeMethod(
                queryBuilder, "buildJurisdictionClauses", request(statusesByRole), narrowing, nameToPathMap);
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
                .thenReturn(new LinkedHashMap<>(Map.of("COMPLAINT_FACILITATOR_1", at("state", "S1", "S2"))));

        Map<String, Object> narrowing = new HashMap<>();
        narrowing.put("state", List.of("S1"));

        List<Map<String, Object>> clauses = build(narrowing);

        // Two separate must clauses - scope and narrowing - so ES intersects them.
        assertEquals(2, clauses.size(), "expected a scope clause and a narrowing clause");

        Map<String, List<String>> scope = termsIn(clauses.get(0), new HashMap<>());
        assertEquals(Set.of("S1", "S2"),
                new HashSet<>(scope.get("Data.incident.boundary.stateCode.keyword")));

        Map<String, List<String>> narrowed = termsIn(clauses.get(1), new HashMap<>());
        assertEquals(List.of("S1"), narrowed.get("Data.incident.boundary.stateCode.keyword"));
    }

    @Test
    void narrowingAcrossLevelsIsAnIntersectionNotAReplacement() {
        // Holds a state; narrows to a facility inside it. Both clauses stand, and a ticket at that
        // facility carries both codes, which is why level-matching works without expansion.
        when(roleJurisdictionScope.resolve(org.mockito.ArgumentMatchers.any()))
                .thenReturn(new LinkedHashMap<>(Map.of("COMPLAINT_FACILITATOR_2", at("state", "S1"))));

        Map<String, Object> narrowing = new HashMap<>();
        narrowing.put("facility", List.of("F1"));

        List<Map<String, Object>> clauses = build(narrowing);

        assertEquals(List.of("S1"),
                termsIn(clauses.get(0), new HashMap<>()).get("Data.incident.boundary.stateCode.keyword"));
        assertEquals(List.of("F1"),
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
        statuses.put("COMPLAINT_FACILITATOR_1", new HashSet<>(List.of("PENDING_AT_SPOC")));
        statuses.put("COMPLAINT_FACILITATOR_2", new HashSet<>(List.of("PENDING_AT_TECH_POC")));

        List<Map<String, Object>> clauses = build(new HashMap<>(), statuses);

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
                .thenReturn(new LinkedHashMap<>(Map.of("COMPLAINT_FACILITATOR_1", at("state", "S1"))));

        List<Map<String, Object>> clauses = build(new HashMap<>(), null);

        List<Object> perRole = (List<Object>) ((Map<String, Object>) clauses.get(0).get("bool")).get("should");
        List<Object> pair = (List<Object>) ((Map<String, Object>) ((Map<String, Object>) perRole.get(0)).get("bool")).get("must");
        assertEquals(1, pair.size(), "no status pairing when the caller asked for everything");
    }

    @Test
    void theBrowsersEmptyPlaceholderIsNotTreatedAsABoundary() {
        // useNewInbox.js sends {country: ["-"]} when session storage is empty. Taken literally it
        // would AND an impossible country code against everything.
        when(roleJurisdictionScope.resolve(org.mockito.ArgumentMatchers.any()))
                .thenReturn(new LinkedHashMap<>(Map.of("COMPLAINT_FACILITATOR_1", at("state", "S1"))));

        Map<String, Object> narrowing = new HashMap<>();
        narrowing.put("country", List.of("-"));

        List<Map<String, Object>> clauses = build(narrowing);

        assertEquals(1, clauses.size(), "the placeholder must not add a narrowing clause");
        assertTrue(termsIn(clauses.get(0), new HashMap<>())
                .containsKey("Data.incident.boundary.stateCode.keyword"));
    }

    @Test
    void anUnresolvableScopeFailsOpenButKeepsTheUsersFilter() {
        when(roleJurisdictionScope.resolve(org.mockito.ArgumentMatchers.any())).thenReturn(null);

        Map<String, Object> narrowing = new HashMap<>();
        narrowing.put("state", List.of("S1"));

        List<Map<String, Object>> clauses = build(narrowing);

        assertEquals(1, clauses.size(), "no scope clause, but the narrowing still applies");
        assertEquals(List.of("S1"),
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
