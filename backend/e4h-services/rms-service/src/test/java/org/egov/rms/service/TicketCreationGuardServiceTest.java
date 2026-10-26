package org.egov.rms.service;

import org.egov.rms.config.RMSConfiguration;
import org.egov.rms.model.Alert;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("TicketCreationGuardService Unit Tests")
class TicketCreationGuardServiceTest {

    private static final Set<String> BATTERY_ROOT_CAUSES = Set.of("BatteryDisconnected", "DeepDischarge");

    @Mock
    private OpenIncidentChecker openIncidentChecker;

    @Mock
    private TicketPauseService ticketPauseService;

    @Mock
    private FacilityEligibilityService facilityEligibilityService;

    @Mock
    private FacilityServiceClient facilityServiceClient;

    @Mock
    private RMSConfiguration config;

    @InjectMocks
    private TicketCreationGuardService guard;

    @BeforeEach
    void setUp() {
        when(facilityEligibilityService.isEligibleByHfrOrFacilityId(any(), any())).thenReturn(true);
        when(ticketPauseService.isFacilityPaused(any(), any())).thenReturn(false);
    }

    private static Alert alert(Alert.AlertType type, Alert.AlertSubType subType) {
        return Alert.builder().id("a1").facilityId("CENTER-1").alertType(type).alertSubType(subType).build();
    }

    @Test
    @DisplayName("Open inverter shutdown ticket (from any source) blocks every alert")
    void openInverterShutdown_blocksAll() {
        Alert gridAlert = alert(Alert.AlertType.GRID, Alert.AlertSubType.VOLTAGE_VARIATION_HIGH);
        when(openIncidentChecker.hasOpenIncident(gridAlert, "INVERTER", Set.of("ShutdownInverter"))).thenReturn(true);

        assertTrue(guard.shouldBlockTicketCreation(gridAlert));
    }

    @Test
    @DisplayName("Open battery root-cause ticket blocks battery alerts")
    void openBatteryRootCause_blocksBattery() {
        Alert batteryAlert = alert(Alert.AlertType.BATTERY, Alert.AlertSubType.OVERCHARGING);
        when(openIncidentChecker.hasOpenIncident(batteryAlert, "INVERTER", Set.of("ShutdownInverter"))).thenReturn(false);
        when(openIncidentChecker.hasOpenIncident(batteryAlert, "BATTERY", BATTERY_ROOT_CAUSES)).thenReturn(true);

        assertTrue(guard.shouldBlockTicketCreation(batteryAlert));
    }

    @Test
    @DisplayName("Battery root-cause rule is not evaluated for alerts outside its scope")
    void batteryRule_skippedOutOfScope() {
        Alert gridAlert = alert(Alert.AlertType.GRID, Alert.AlertSubType.VOLTAGE_VARIATION_REVERSE);
        when(openIncidentChecker.hasOpenIncident(gridAlert, "INVERTER", Set.of("ShutdownInverter"))).thenReturn(false);

        assertFalse(guard.shouldBlockTicketCreation(gridAlert));
        verify(openIncidentChecker, never()).hasOpenIncident(any(), eq("BATTERY"), any());
    }

    @Test
    @DisplayName("No blocking incidents allows the ticket")
    void noBlockingIncidents_allows() {
        Alert panelAlert = alert(Alert.AlertType.PANEL, Alert.AlertSubType.LOW_GENERATION);
        when(openIncidentChecker.hasOpenIncident(any(), anyString(), any())).thenReturn(false);

        assertFalse(guard.shouldBlockTicketCreation(panelAlert));
    }
}
