package org.egov.rms.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.egov.rms.config.RMSConfiguration;
import org.egov.rms.model.RMSApiRequest;
import org.egov.rms.model.RMSApiResponse;
import org.egov.rms.model.RMSFacilityData;
import org.egov.rms.service.CenterIdMappingService;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.client.RestClientException;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class DataCollectorService {

    private final RMSConfiguration config;
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    private final CenterIdMappingService mappingService;

    /**
     * Collects panel-level data for solar consumption analysis
     */
    public List<RMSFacilityData> collectPanelData() {
        log.info("Collecting panel data for solar vs grid consumption analysis");
        List<RMSFacilityData> allFacilities = new ArrayList<>();
        
        try {
            int page = 1;
            int pageSize = 100;
            boolean hasMore = true;

            while (hasMore) {
                RMSApiRequest request = RMSApiRequest.builder()
                        .graphType("solarVsGrid_Eb_Diff")
                        .timeRange(RMSApiRequest.TimeRange.builder()
                                .timePeriod(RMSApiRequest.TimePeriod.builder()
                                        .label("Last 7 days")
                                        .value("last_seven_days")
                                        .build())
                                .customRange(new HashMap<>())
                                .build())
                        .frequency("daily")
                        .aggregation("deltaSum")
                        .pagination(RMSApiRequest.Pagination.builder()
                                .page(page)
                                .size(pageSize)
                                .build())
                        .filters(new HashMap<String, Object>() {{
                            put("solarConsumptionPercent", new HashMap<String, Object>() {{
                                put("compareFunction", "lte");
                                put("compareValue", 60);
                            }});
                        }})
                        .build();

                RMSApiResponse response = callRMSApi(config.getCenterDetailsEndpoint(), request);
                
                if (response != null && response.getData() != null && 
                    response.getData().getFacilities() != null) {
                    allFacilities.addAll(response.getData().getFacilities());
                    
                    RMSApiResponse.Pagination pagination = response.getData().getPagination();
                    if (pagination != null && pagination.getTotalPages() != null) {
                        hasMore = page < pagination.getTotalPages();
                        page++;
                    } else {
                        hasMore = false;
                    }
                } else {
                    hasMore = false;
                }
            }

            log.info("Collected panel data for {} facilities", allFacilities.size());
            
            // Enrich with HFR IDs from mapping table
            mappingService.enrichFacilitiesWithHfrId(allFacilities);
            
            return allFacilities;
        } catch (Exception e) {
            log.error("Error collecting panel data", e);
            return new ArrayList<>();
        }
    }

    /**
     * Collects inverter data for devices with no signal
     */
    public List<RMSFacilityData> collectInverterNoSignalData() {
        log.info("Collecting inverter data for devices with no signal");
        List<RMSFacilityData> allFacilities = new ArrayList<>();
        
        try {
            int page = 1;
            int pageSize = 10000;
            boolean hasMore = true;

            while (hasMore) {
                RMSApiRequest request = RMSApiRequest.builder()
                        .status(List.of(new HashMap<String, String>() {{
                            put("label", "Inactive");
                        }}))
                        .pagination(RMSApiRequest.Pagination.builder()
                                .page(page)
                                .size(pageSize)
                                .build())
                        .build();

                RMSApiResponse response = callRMSApi(config.getCenterDatasEndpoint(), request);
                
                if (response != null && response.getData() != null && 
                    response.getData().getFacilities() != null) {
                    List<RMSFacilityData> facilities = response.getData().getFacilities();
                    
                    // Filter facilities with no signal for more than configured days
                    Instant cutoffTime = Instant.now().minus(config.getInverterNoSignalDays(), ChronoUnit.DAYS);
                    facilities.stream()
                            .filter(f -> f.getLastSyncTime() != null && 
                                    f.getLastSyncTime().isBefore(cutoffTime))
                            .forEach(allFacilities::add);
                    
                    RMSApiResponse.Pagination pagination = response.getData().getPagination();
                    if (pagination != null && pagination.getTotalPages() != null) {
                        hasMore = page < pagination.getTotalPages();
                        page++;
                    } else {
                        hasMore = false;
                    }
                } else {
                    hasMore = false;
                }
            }

            log.info("Collected inverter no-signal data for {} facilities", allFacilities.size());
            
            // Enrich with HFR IDs from mapping table
            mappingService.enrichFacilitiesWithHfrId(allFacilities);
            
            return allFacilities;
        } catch (Exception e) {
            log.error("Error collecting inverter no-signal data", e);
            return new ArrayList<>();
        }
    }

    /**
     * Collects inverter data for high voltage conditions
     */
    public List<RMSFacilityData> collectInverterHighVoltageData() {
        log.info("Collecting inverter data for high voltage conditions");
        List<RMSFacilityData> allFacilities = new ArrayList<>();
        
        try {
            int page = 1;
            int pageSize = 100;
            boolean hasMore = true;

            while (hasMore) {
                RMSApiRequest request = RMSApiRequest.builder()
                        .graphType("PCUvoltage")
                        .timeRange(RMSApiRequest.TimeRange.builder()
                                .timePeriod(RMSApiRequest.TimePeriod.builder()
                                        .label("Today")
                                        .value("today")
                                        .build())
                                .customRange(new HashMap<>())
                                .build())
                        .pagination(RMSApiRequest.Pagination.builder()
                                .page(page)
                                .size(pageSize)
                                .build())
                        .filters(new HashMap<String, Object>() {{
                            put("voltage", new HashMap<String, Object>() {{
                                put("gt", config.getInverterHighVoltageThreshold());
                            }});
                        }})
                        .build();

                RMSApiResponse response = callRMSApi(config.getCenterDetailsEndpoint(), request);
                
                if (response != null && response.getData() != null && 
                    response.getData().getFacilities() != null) {
                    allFacilities.addAll(response.getData().getFacilities());
                    
                    RMSApiResponse.Pagination pagination = response.getData().getPagination();
                    if (pagination != null && pagination.getTotalPages() != null) {
                        hasMore = page < pagination.getTotalPages();
                        page++;
                    } else {
                        hasMore = false;
                    }
                } else {
                    hasMore = false;
                }
            }

            log.info("Collected inverter high voltage data for {} facilities", allFacilities.size());
            return allFacilities;
        } catch (Exception e) {
            log.error("Error collecting inverter high voltage data", e);
            return new ArrayList<>();
        }
    }

    /**
     * Collects battery data for voltage = 0 conditions
     */
    public List<RMSFacilityData> collectBatteryVoltageZeroData() {
        log.info("Collecting battery data for voltage = 0 conditions");
        List<RMSFacilityData> allFacilities = new ArrayList<>();
        
        try {
            int page = 1;
            int pageSize = 100;
            boolean hasMore = true;

            while (hasMore) {
                RMSApiRequest request = RMSApiRequest.builder()
                        .graphType("batteryVoltage")
                        .timeRange(RMSApiRequest.TimeRange.builder()
                                .timePeriod(RMSApiRequest.TimePeriod.builder()
                                        .label("Today")
                                        .value("today")
                                        .build())
                                .customRange(new HashMap<>())
                                .build())
                        .pagination(RMSApiRequest.Pagination.builder()
                                .page(page)
                                .size(pageSize)
                                .build())
                        .filters(new HashMap<String, Object>() {{
                            put("batteryVoltage", new HashMap<String, Object>() {{
                                put("eq", 0);
                            }});
                        }})
                        .build();

                RMSApiResponse response = callRMSApi(config.getCenterDetailsEndpoint(), request);
                
                if (response != null && response.getData() != null && 
                    response.getData().getFacilities() != null) {
                    allFacilities.addAll(response.getData().getFacilities());
                    
                    RMSApiResponse.Pagination pagination = response.getData().getPagination();
                    if (pagination != null && pagination.getTotalPages() != null) {
                        hasMore = page < pagination.getTotalPages();
                        page++;
                    } else {
                        hasMore = false;
                    }
                } else {
                    hasMore = false;
                }
            }

            log.info("Collected battery voltage zero data for {} facilities", allFacilities.size());
            return allFacilities;
        } catch (Exception e) {
            log.error("Error collecting battery voltage zero data", e);
            return new ArrayList<>();
        }
    }

    /**
     * Collects grid voltage data for low/high voltage conditions
     */
    public List<RMSFacilityData> collectGridVoltageData() {
        log.info("Collecting grid voltage data");
        List<RMSFacilityData> allFacilities = new ArrayList<>();
        
        try {
            int page = 1;
            int pageSize = 100;
            boolean hasMore = true;

            while (hasMore) {
                RMSApiRequest request = RMSApiRequest.builder()
                        .graphType("gridVoltage")
                        .timeRange(RMSApiRequest.TimeRange.builder()
                                .timePeriod(RMSApiRequest.TimePeriod.builder()
                                        .label("Today")
                                        .value("today")
                                        .build())
                                .customRange(new HashMap<>())
                                .build())
                        .pagination(RMSApiRequest.Pagination.builder()
                                .page(page)
                                .size(pageSize)
                                .build())
                        .filters(new HashMap<String, Object>() {{
                            put("voltageLow", new HashMap<String, Object>() {{
                                put("lt", config.getGridVoltageLowThreshold());
                            }});
                            put("voltageHigh", new HashMap<String, Object>() {{
                                put("gt", config.getGridVoltageHighThreshold());
                            }});
                        }})
                        .build();

                RMSApiResponse response = callRMSApi(config.getCenterDetailsEndpoint(), request);
                
                if (response != null && response.getData() != null) {
                    if (response.getData().getLowVoltageFacilities() != null) {
                        allFacilities.addAll(response.getData().getLowVoltageFacilities());
                    }
                    if (response.getData().getHighVoltageFacilities() != null) {
                        allFacilities.addAll(response.getData().getHighVoltageFacilities());
                    }
                    
                    RMSApiResponse.Pagination pagination = response.getData().getPagination();
                    if (pagination != null && pagination.getTotalPages() != null) {
                        hasMore = page < pagination.getTotalPages();
                        page++;
                    } else {
                        hasMore = false;
                    }
                } else {
                    hasMore = false;
                }
            }

            log.info("Collected grid voltage data for {} facilities", allFacilities.size());
            return allFacilities;
        } catch (Exception e) {
            log.error("Error collecting grid voltage data", e);
            return new ArrayList<>();
        }
    }

    /**
     * Calls RMS API with retry logic
     */
    private RMSApiResponse callRMSApi(String endpoint, RMSApiRequest request) {
        String url = config.getRmsApiBaseUrl() + endpoint;
        
        HttpHeaders headers = new HttpHeaders();
        headers.set("Content-Type", "application/json");
        headers.set("Accept", "application/json");
        headers.set("Access-Token", config.getRmsApiAccessToken());
        
        HttpEntity<RMSApiRequest> entity = new HttpEntity<>(request, headers);
        
        int attempts = 0;
        long delay = config.getRetryBackoffDelay();
        
        while (attempts < config.getRetryMaxAttempts()) {
            try {
                log.debug("Calling RMS API: {} (attempt {})", url, attempts + 1);
                ResponseEntity<RMSApiResponse> response = restTemplate.exchange(
                        url, HttpMethod.POST, entity, RMSApiResponse.class);
                
                if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                    return response.getBody();
                }
            } catch (RestClientException e) {
                attempts++;
                if (attempts >= config.getRetryMaxAttempts()) {
                    log.error("Failed to call RMS API after {} attempts: {}", config.getRetryMaxAttempts(), e.getMessage());
                    throw e;
                }
                log.warn("RMS API call failed (attempt {}), retrying after {}ms: {}", attempts, delay, e.getMessage());
                try {
                    Thread.sleep(delay);
                    delay *= 2; // Exponential backoff
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    throw new RuntimeException("Interrupted during retry", ie);
                }
            }
        }
        
        return null;
    }
}

