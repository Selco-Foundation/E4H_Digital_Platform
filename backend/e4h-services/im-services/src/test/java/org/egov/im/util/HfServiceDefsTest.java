package org.egov.im.util;

import org.egov.im.web.models.Incident;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class HfServiceDefsTest {

    private final Map<String, Object> mdmsData = HfServiceDefsFixture.mdmsData();

    @Test
    void componentListsOnlyItsHfEntries() {
        List<Map<String, Object>> entries = HfServiceDefs.forComponent(mdmsData, "INVERTER");
        assertEquals(List.of("INVERTER_NOT_WORKING", "INVERTER_DISPLAY_ISSUE", "INVERTER_OTHER_ISSUE"),
                entries.stream().map(e -> e.get("serviceCode")).toList());
    }

    @Test
    void primaryObservationIsTheHighestPriorityOne() {
        Optional<Map<String, Object>> primary = HfServiceDefs.primaryObservation(mdmsData,
                incident("INVERTER", "INVERTER_OTHER_ISSUE", "INVERTER_DISPLAY_ISSUE"));
        assertEquals("INVERTER_DISPLAY_ISSUE", primary.orElseThrow().get("serviceCode"));
        assertEquals("INVERTER", primary.orElseThrow().get("menuPath"));
    }

    @Test
    void primaryObservationTieGoesToTheFirstChosen() {
        Optional<Map<String, Object>> primary = HfServiceDefs.primaryObservation(mdmsData,
                incident("INVERTER", "INVERTER_DISPLAY_ISSUE", "INVERTER_NOT_WORKING"));
        assertEquals("INVERTER_DISPLAY_ISSUE", primary.orElseThrow().get("serviceCode"));
    }

    @Test
    void codesOfAnotherComponentOrOfCrmAreIgnored() {
        assertTrue(HfServiceDefs.primaryObservation(mdmsData,
                incident("INVERTER", "BATTERY_OTHER_ISSUE", "ShutdownInverter")).isEmpty());
    }

    private static Incident incident(String component, String... observations) {
        return Incident.builder().component(component).observations(List.of(observations)).build();
    }
}
