package org.egov.im.service;

import org.egov.im.repository.IMPriorityRepository;
import org.egov.im.util.HfServiceDefsFixture;
import org.egov.im.web.models.IMPrioritySearchCriteria;
import org.egov.im.web.models.Incident;
import org.egov.im.web.models.Priority;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Covers the ticket priority taken from the HF Incident.ServiceDefs priorities of the observations
 * (SEM 4.0 Ticket Reporting).
 */
class SLAServiceObservationPriorityTest {

    // Priority returned by the im_services_priority table lookup, distinct from every observation priority
    private static final Priority TABLE_PRIORITY = Priority.LOW;

    private final SLAService slaService = new SLAService(new IMPriorityRepository(null, null, null) {
        @Override
        public Priority getPriority(IMPrioritySearchCriteria criteria) {
            return TABLE_PRIORITY;
        }
    });

    private final Map<String, Object> mdmsData = HfServiceDefsFixture.mdmsData();

    @Test
    void highestObservationPriorityWins() {
        assertEquals(Priority.HIGH, resolve("INVERTER", List.of("INVERTER_OTHER_ISSUE", "INVERTER_NOT_WORKING")));
    }

    @Test
    void singleObservationGivesItsPriority() {
        assertEquals(Priority.MEDIUM, resolve("INVERTER", List.of("INVERTER_OTHER_ISSUE")));
    }

    @Test
    void observationOfAnotherComponentIsIgnored() {
        assertEquals(TABLE_PRIORITY, resolve("INVERTER", List.of("BATTERY_OTHER_ISSUE")));
    }

    @Test
    void crmServiceDefIsNotAnObservation() {
        assertEquals(TABLE_PRIORITY, resolve("INVERTER", List.of("ShutdownInverter")));
    }

    @Test
    void noObservationFallsBackToPriorityTable() {
        assertEquals(TABLE_PRIORITY, resolve("INVERTER", List.of()));
    }

    @Test
    void noComponentFallsBackToPriorityTable() {
        assertEquals(TABLE_PRIORITY, resolve(null, List.of("INVERTER_NOT_WORKING")));
    }

    private Priority resolve(String component, List<String> observations) {
        Incident incident = Incident.builder()
                .tenantId("in")
                .incidentType("INVERTER")
                .incidentSubType("INVERTER_NOT_WORKING")
                .component(component)
                .observations(observations)
                .build();
        return slaService.resolvePriority(incident, mdmsData);
    }
}
