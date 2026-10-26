package org.egov.rms.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.egov.rms.config.RMSConfiguration;
import org.egov.rms.model.Alert;
import org.egov.rms.model.FacilityDetails;
import org.egov.rms.repository.AlertRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("OpenIncidentChecker Unit Tests")
class OpenIncidentCheckerTest {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    @Mock
    private RMSConfiguration config;

    @Mock
    private RestTemplate restTemplate;

    @Mock
    private FacilityServiceClient facilityServiceClient;

    @Mock
    private AlertRepository alertRepository;

    @InjectMocks
    private OpenIncidentChecker checker;

    private Alert alert;

    @BeforeEach
    void setUp() {
        lenient().when(config.getImServiceBaseUrl()).thenReturn("http://im:8080");
        lenient().when(config.getImServiceCountEndpoint()).thenReturn("/im-services/v2/request/_count");
        lenient().when(config.getDefaultTenantId()).thenReturn("in");

        alert = Alert.builder()
                .id("alert-1")
                .facilityId("CENTER-1")
                .hfrId("HFR-1")
                .alertType(Alert.AlertType.INVERTER)
                .alertSubType(Alert.AlertSubType.SHUTDOWN)
                .build();
    }

    private void facilityHasBoundary(String boundaryCode) {
        when(facilityServiceClient.getFacilityByHfrId("HFR-1", "in"))
                .thenReturn(FacilityDetails.builder().boundaryCode(boundaryCode).build());
    }

    private void imCountReturns(String json) throws Exception {
        when(restTemplate.postForObject(any(URI.class), any(), eq(com.fasterxml.jackson.databind.JsonNode.class)))
                .thenReturn(MAPPER.readTree(json));
    }

    @Test
    @DisplayName("Open ticket from any source at the facility's boundary blocks the alert")
    void openTicketInIm_returnsTrue() throws Exception {
        facilityHasBoundary("PHC_001");
        imCountReturns("{\"count\": 1}");

        assertTrue(checker.hasOpenTicket(alert));
        verifyNoInteractions(alertRepository);
    }

    @Test
    @DisplayName("No open ticket in IM allows the alert")
    void noOpenTicketInIm_returnsFalse() throws Exception {
        facilityHasBoundary("PHC_001");
        imCountReturns("{\"count\": 0}");

        assertFalse(checker.hasOpenTicket(alert));
        verifyNoInteractions(alertRepository);
    }

    @Test
    @DisplayName("Count request carries the facility boundary, mapped type/sub-type and open statuses")
    void countRequest_hasExpectedCriteria() throws Exception {
        facilityHasBoundary("PHC_001");
        imCountReturns("{\"count\": 0}");

        checker.hasOpenTicket(alert);

        ArgumentCaptor<URI> uri = ArgumentCaptor.forClass(URI.class);
        verify(restTemplate).postForObject(uri.capture(), any(), eq(com.fasterxml.jackson.databind.JsonNode.class));
        String query = uri.getValue().getQuery();
        assertTrue(uri.getValue().toString().startsWith("http://im:8080/im-services/v2/request/_count?"));
        assertTrue(query.contains("tenantId=in"));
        assertTrue(query.contains("boundaryCode=PHC_001"));
        assertTrue(query.contains("incidentType=INVERTER"));
        assertTrue(query.contains("incidentSubType=ShutdownInverter"));
        assertTrue(query.contains("applicationStatus=PENDINGFORASSIGNMENT"));
        assertTrue(query.contains("applicationStatus=PENDING_RESOLUTION_OUT_OF_SCOPE"));
    }

    @Test
    @DisplayName("Facility is looked up once per alert across several checks")
    void boundaryCode_resolvedOncePerAlert() throws Exception {
        facilityHasBoundary("PHC_001");
        imCountReturns("{\"count\": 0}");

        checker.hasOpenTicket(alert);
        checker.hasOpenIncident(alert, "BATTERY", Set.of("BatteryDisconnected", "DeepDischarge"));

        verify(facilityServiceClient, times(1)).getFacilityByHfrId(anyString(), anyString());
        assertEquals("PHC_001", alert.getBoundaryCode());
    }

    @Test
    @DisplayName("IM call failure falls back to the eg_incident_v2 check")
    void imFailure_fallsBackToDb() {
        facilityHasBoundary("PHC_001");
        when(restTemplate.postForObject(any(URI.class), any(), eq(com.fasterxml.jackson.databind.JsonNode.class)))
                .thenThrow(new ResourceAccessException("connection refused"));
        when(alertRepository.hasOpenTicket("CENTER-1", Alert.AlertType.INVERTER, Alert.AlertSubType.SHUTDOWN))
                .thenReturn(true);

        assertTrue(checker.hasOpenTicket(alert));
    }

    @Test
    @DisplayName("Response without a count falls back to the eg_incident_v2 check")
    void malformedResponse_fallsBackToDb() throws Exception {
        facilityHasBoundary("PHC_001");
        imCountReturns("{\"ResponseInfo\": {}}");
        when(alertRepository.hasOpenTicket("CENTER-1", Alert.AlertType.INVERTER, Alert.AlertSubType.SHUTDOWN))
                .thenReturn(false);

        assertFalse(checker.hasOpenTicket(alert));
        verify(alertRepository).hasOpenTicket("CENTER-1", Alert.AlertType.INVERTER, Alert.AlertSubType.SHUTDOWN);
    }

    @Test
    @DisplayName("Unresolvable facility falls back to the eg_incident_v2 check without calling IM")
    void noBoundaryCode_fallsBackToDb() {
        when(facilityServiceClient.getFacilityByHfrId("HFR-1", "in")).thenReturn(null);
        when(alertRepository.hasOpenTicket("CENTER-1", Alert.AlertType.INVERTER, Alert.AlertSubType.SHUTDOWN))
                .thenReturn(false);

        assertFalse(checker.hasOpenTicket(alert));
        verifyNoInteractions(restTemplate);
    }

    @Test
    @DisplayName("Multi sub-type fallback blocks when any sub-type has an open RMS incident")
    void hasOpenIncident_fallbackChecksEachSubType() {
        when(facilityServiceClient.getFacilityByHfrId("HFR-1", "in")).thenReturn(null);
        lenient().when(alertRepository.hasOpenIncidentForRmsFacility("CENTER-1", "BATTERY", "BatteryDisconnected"))
                .thenReturn(false);
        lenient().when(alertRepository.hasOpenIncidentForRmsFacility("CENTER-1", "BATTERY", "DeepDischarge"))
                .thenReturn(true);

        assertTrue(checker.hasOpenIncident(alert, "BATTERY", Set.of("BatteryDisconnected", "DeepDischarge")));
    }
}
