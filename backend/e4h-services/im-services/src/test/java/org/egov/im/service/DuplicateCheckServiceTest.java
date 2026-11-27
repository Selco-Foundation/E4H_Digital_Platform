package org.egov.im.service;

import org.egov.common.contract.request.RequestInfo;
import org.egov.common.contract.request.Role;
import org.egov.common.contract.request.User;
import org.egov.im.repository.IMRepository;
import org.egov.im.web.models.AuditDetails;
import org.egov.im.web.models.DuplicateTicket;
import org.egov.im.web.models.Incident;
import org.egov.im.web.models.IncidentRequest;
import org.egov.im.web.models.RequestSearchCriteria;
import org.egov.tracer.model.CustomException;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Covers the duplicate check by facility + component (SEM 4.0 Ticket Reporting).
 */
class DuplicateCheckServiceTest {

    private static final String FACILITY = "FAC_1";

    private final Map<String, Object> mdmsData = Map.of("MdmsRes", Map.of("Incident", Map.of(
            "Component", List.of(
                    Map.of("code", "INVERTER", "category", "FAULT", "active", true),
                    Map.of("code", "ADDITIONAL_SUPPORT", "category", "SUPPORT_REQUEST", "active", true)))));

    // Open tickets returned by the fake repository, and the criteria it was last queried with
    private List<Incident> openTickets = new ArrayList<>();
    private RequestSearchCriteria lastCriteria;

    private final DuplicateCheckService service = new DuplicateCheckService(new IMRepository(null, null, null, null) {
        @Override
        public List<Incident> getIncidents(RequestSearchCriteria criteria) {
            lastCriteria = criteria;
            return openTickets;
        }
    });

    {
        ReflectionTestUtils.setField(service, "openTicketStatuses", Set.of("PENDINGFORASSIGNMENT", "PENDINGRESOLUTION"));
    }

    @Test
    void facilityUserTicketWithOpenMatchIsRefused() {
        openTickets = List.of(ticket("IM-2", 200L), ticket("IM-1", 100L));
        CustomException e = assertThrows(CustomException.class,
                () -> service.checkOnCreate(request("INVERTER", "EMPLOYEE"), mdmsData));
        assertEquals("DUPLICATE_TICKET", e.getCode());
        assertTrue(e.getMessage().contains("IM-2") && e.getMessage().contains("IM-1"));
        assertEquals(FACILITY, lastCriteria.getBoundaryCode());
        assertEquals(Set.of("INVERTER"), lastCriteria.getComponent());
        assertEquals(Set.of("PENDINGFORASSIGNMENT", "PENDINGRESOLUTION"), lastCriteria.getApplicationStatus());
    }

    @Test
    void crmTicketWithOpenMatchIsRefused() {
        openTickets = List.of(ticket("IM-1", 100L));
        CustomException e = assertThrows(CustomException.class,
                () -> service.checkOnCreate(request("INVERTER", "COMPLAINT_ASSESSOR"), mdmsData));
        assertEquals("DUPLICATE_TICKET", e.getCode());
    }

    @Test
    void ticketWithoutMatchPasses() {
        IncidentRequest request = request("INVERTER", "EMPLOYEE");
        assertDoesNotThrow(() -> service.checkOnCreate(request, mdmsData));
        assertFalse(request.getIncident().isPotentialDuplicate());
    }

    @Test
    void supportRequestIsNeverChecked() {
        openTickets = List.of(ticket("IM-1", 100L));
        IncidentRequest request = request("ADDITIONAL_SUPPORT", "COMPLAINT_ASSESSOR");

        assertDoesNotThrow(() -> service.checkOnCreate(request, mdmsData));
        assertFalse(request.getIncident().isPotentialDuplicate());
        assertNull(lastCriteria, "support requests must not query for duplicates");
        assertFalse(service.isApplicable("ADDITIONAL_SUPPORT", mdmsData));
    }

    @Test
    void duplicateTicketsCarryNumberStatusAndDate() {
        List<DuplicateTicket> duplicates = service.toDuplicateTickets(List.of(ticket("IM-1", 100L)));
        assertEquals(1, duplicates.size());
        assertEquals("IM-1", duplicates.get(0).getIncidentId());
        assertEquals("PENDINGFORASSIGNMENT", duplicates.get(0).getApplicationStatus());
        assertEquals(100L, duplicates.get(0).getCreatedTime());
    }

    private IncidentRequest request(String component, String roleCode) {
        User userInfo = User.builder().uuid("user-1")
                .roles(List.of(Role.builder().code(roleCode).build())).build();
        return IncidentRequest.builder()
                .requestInfo(RequestInfo.builder().userInfo(userInfo).build())
                .incident(Incident.builder().tenantId("in").boundaryCode(FACILITY).component(component).build())
                .build();
    }

    private static Incident ticket(String incidentId, long createdTime) {
        return Incident.builder()
                .incidentId(incidentId)
                .component("INVERTER")
                .applicationStatus("PENDINGFORASSIGNMENT")
                .auditDetails(AuditDetails.builder().createdTime(createdTime).build())
                .build();
    }
}
