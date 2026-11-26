package org.selco.e4h.util;

import org.junit.jupiter.api.Test;
import org.selco.e4h.web.models.Employee;
import org.selco.e4h.web.models.EscalationTicket;
import org.selco.e4h.web.models.Jurisdiction;
import org.selco.e4h.web.models.Role;
import org.selco.e4h.web.models.User;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class EscalationScopeTest {

    private static final String SPOC = "COMPLAINT_FACILITATOR_1";
    private static final String KA = "India_Karnataka";
    private static final String BAGALKOTE = "India_Karnataka_Bagalkote";
    private static final String BADAMI = "India_Karnataka_Bagalkote_Badami";

    private static Jurisdiction jurisdiction(String boundary, String type, Boolean active, String... roles) {
        List<Role> roleList = new ArrayList<>();
        for (String role : roles) {
            roleList.add(Role.builder().code(role).build());
        }
        return Jurisdiction.builder().boundary(boundary).boundaryType(type).isActive(active).roles(roleList).build();
    }

    private static Employee employee(Jurisdiction... jurisdictions) {
        return Employee.builder().user(User.builder().name("Ravi").build()).jurisdictions(List.of(jurisdictions)).build();
    }

    private static EscalationTicket ticket(String state, String district, String block) {
        Map<String, Object> boundary = new HashMap<>();
        boundary.put("stateCode", state);
        boundary.put("districtCode", district);
        boundary.put("blockCode", block);
        Map<String, Object> incident = new HashMap<>();
        incident.put("boundary", boundary);
        Map<String, Object> data = new HashMap<>();
        data.put("incident", incident);
        return EscalationTicket.builder().id(block).additionalDetails(data).build();
    }

    @Test
    void scopesComeFromActiveJurisdictionsThatApplyToTheRole() {
        List<EscalationScope> scopes = EscalationScope.forRole(employee(
                jurisdiction(BAGALKOTE, "District", true),                 // no roles: every role
                jurisdiction(BADAMI, "block", true, SPOC),                 // scoped to SPOC, lower-case type
                jurisdiction("India_Kerala", "State", true, "COMPLAINT_ASSESSOR"), // another role only
                jurisdiction("India_Goa", "State", false),                 // inactive
                jurisdiction("FAC_1", "Facility", true)), SPOC);           // facility: not an area

        assertEquals(List.of("DISTRICT:" + BAGALKOTE, "BLOCK:" + BADAMI),
                scopes.stream().map(Object::toString).toList());
    }

    @Test
    void districtScopeLiesInItsStateOnly() {
        EscalationScope district = EscalationScope.forRole(employee(jurisdiction(BAGALKOTE, "District", true)), SPOC).get(0);

        assertTrue(district.liesIn(KA));
        assertFalse(district.liesIn("India_Kerala"));
        // A state whose code is a prefix of another's must not capture it.
        assertFalse(EscalationScope.forRole(employee(jurisdiction("India_AndhraPradesh_X", "District", true)), SPOC)
                .get(0).liesIn("India_Andhra"));
    }

    @Test
    void countryScopeLiesInEveryState() {
        EscalationScope country = EscalationScope.forRole(employee(jurisdiction("India", "Country", true)), SPOC).get(0);

        assertTrue(country.liesIn(KA));
        assertTrue(country.covers(ticket(KA, BAGALKOTE, BADAMI)));
    }

    @Test
    void ticketsAreMatchedOnTheirOwnLevel() {
        EscalationTicket inBadami = ticket(KA, BAGALKOTE, BADAMI);
        EscalationTicket elsewhereInKarnataka = ticket(KA, "India_Karnataka_Mysuru", "India_Karnataka_Mysuru_Hunsur");

        EscalationScope state = EscalationScope.forRole(employee(jurisdiction(KA, "State", true)), SPOC).get(0);
        EscalationScope district = EscalationScope.forRole(employee(jurisdiction(BAGALKOTE, "District", true)), SPOC).get(0);
        EscalationScope block = EscalationScope.forRole(employee(jurisdiction(BADAMI, "Block", true)), SPOC).get(0);

        assertTrue(state.covers(inBadami));
        assertTrue(state.covers(elsewhereInKarnataka));
        assertTrue(district.covers(inBadami));
        assertFalse(district.covers(elsewhereInKarnataka));
        assertTrue(block.covers(inBadami));
        assertFalse(block.covers(elsewhereInKarnataka));
    }

    @Test
    void ticketWithoutBoundaryReachesOnlyStateLevel() {
        Map<String, Object> data = new HashMap<>();
        data.put("state", "Karnataka");
        EscalationTicket legacy = EscalationTicket.builder().id("old").additionalDetails(data).build();

        assertTrue(EscalationScope.forRole(employee(jurisdiction(KA, "State", true)), SPOC).get(0).covers(legacy));
        assertFalse(EscalationScope.forRole(employee(jurisdiction(BAGALKOTE, "District", true)), SPOC).get(0).covers(legacy));
    }
}
