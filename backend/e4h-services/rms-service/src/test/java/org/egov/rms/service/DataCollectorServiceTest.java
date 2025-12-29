package org.egov.rms.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.egov.rms.config.RMSConfiguration;
import org.egov.rms.model.RMSApiResponse;
import org.egov.rms.model.RMSFacilityData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestTemplate;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("DataCollectorService Unit Tests")
class DataCollectorServiceTest {

    @Mock
    private RMSConfiguration config;

    @Mock
    private RestTemplate restTemplate;

    @Mock
    private ObjectMapper objectMapper;

    @InjectMocks
    private DataCollectorService dataCollectorService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(dataCollectorService, "config", config);
        ReflectionTestUtils.setField(dataCollectorService, "restTemplate", restTemplate);
        ReflectionTestUtils.setField(dataCollectorService, "objectMapper", objectMapper);
    }

    @Test
    @DisplayName("Should collect panel data successfully")
    void testCollectPanelData_Success() {
        // Arrange
        when(config.getCenterDetailsEndpoint()).thenReturn("/selco/center_details/graph");
        when(config.getRmsApiBaseUrl()).thenReturn("https://selco.theiox.com");
        when(config.getRmsApiAccessToken()).thenReturn("test-token");
        when(config.getRetryMaxAttempts()).thenReturn(3);
        when(config.getRetryBackoffDelay()).thenReturn(1000L);

        RMSApiResponse response = createMockPanelResponse();
        ResponseEntity<RMSApiResponse> responseEntity = new ResponseEntity<>(response, HttpStatus.OK);

        when(restTemplate.exchange(anyString(), any(), any(), eq(RMSApiResponse.class)))
                .thenReturn(responseEntity);

        // Act
        List<RMSFacilityData> result = dataCollectorService.collectPanelData();

        // Assert
        assertNotNull(result);
        assertFalse(result.isEmpty());
        verify(restTemplate, atLeastOnce()).exchange(anyString(), any(), any(), eq(RMSApiResponse.class));
    }

    @Test
    @DisplayName("Should collect inverter no signal data successfully")
    void testCollectInverterNoSignalData_Success() {
        // Arrange
        when(config.getCenterDatasEndpoint()).thenReturn("/selco/cachedData/centerDatas/get");
        when(config.getRmsApiBaseUrl()).thenReturn("https://selco.theiox.com");
        when(config.getRmsApiAccessToken()).thenReturn("test-token");
        when(config.getInverterNoSignalDays()).thenReturn(2);
        when(config.getRetryMaxAttempts()).thenReturn(3);
        when(config.getRetryBackoffDelay()).thenReturn(1000L);

        RMSApiResponse response = createMockInverterResponse();
        ResponseEntity<RMSApiResponse> responseEntity = new ResponseEntity<>(response, HttpStatus.OK);

        when(restTemplate.exchange(anyString(), any(), any(), eq(RMSApiResponse.class)))
                .thenReturn(responseEntity);

        // Act
        List<RMSFacilityData> result = dataCollectorService.collectInverterNoSignalData();

        // Assert
        assertNotNull(result);
        verify(restTemplate, atLeastOnce()).exchange(anyString(), any(), any(), eq(RMSApiResponse.class));
    }

    @Test
    @DisplayName("Should handle API errors gracefully")
    void testCollectPanelData_ApiError() {
        // Arrange
        when(config.getCenterDetailsEndpoint()).thenReturn("/selco/center_details/graph");
        when(config.getRmsApiBaseUrl()).thenReturn("https://selco.theiox.com");
        when(config.getRmsApiAccessToken()).thenReturn("test-token");
        when(config.getRetryMaxAttempts()).thenReturn(1);
        when(config.getRetryBackoffDelay()).thenReturn(100L);

        when(restTemplate.exchange(anyString(), any(), any(), eq(RMSApiResponse.class)))
                .thenThrow(new RuntimeException("API Error"));

        // Act
        List<RMSFacilityData> result = dataCollectorService.collectPanelData();

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    private RMSApiResponse createMockPanelResponse() {
        RMSFacilityData facility = RMSFacilityData.builder()
                .facilityId("device_instance_13688")
                .facilityName("PHC Test")
                .hfrId("KA-YAD-PH-3946")
                .solarPercent(List.of(4.0, 6.0, 7.0, 5.0, 6.0, 8.0, 6.0))
                .build();

        RMSApiResponse.RMSResponseData data = RMSApiResponse.RMSResponseData.builder()
                .facilities(List.of(facility))
                .pagination(RMSApiResponse.Pagination.builder()
                        .page(1)
                        .size(100)
                        .totalPages(1)
                        .totalRecords(1)
                        .build())
                .build();

        return RMSApiResponse.builder()
                .status("success")
                .message("Retrieved successfully")
                .data(data)
                .build();
    }

    private RMSApiResponse createMockInverterResponse() {
        RMSFacilityData facility = RMSFacilityData.builder()
                .facilityId("device_instance_12432")
                .facilityName("PHC Test")
                .hfrId("KA-YAD-PH-3946")
                .statusOfDevice("Inactive")
                .lastSyncTime(Instant.now().minus(3, ChronoUnit.DAYS))
                .build();

        RMSApiResponse.RMSResponseData data = RMSApiResponse.RMSResponseData.builder()
                .facilities(List.of(facility))
                .pagination(RMSApiResponse.Pagination.builder()
                        .page(1)
                        .size(10000)
                        .totalPages(1)
                        .totalRecords(1)
                        .build())
                .build();

        return RMSApiResponse.builder()
                .status("success")
                .message("Retrieved successfully")
                .data(data)
                .build();
    }
}

