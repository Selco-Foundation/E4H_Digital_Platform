package org.egov.rms.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.egov.rms.config.RMSConfiguration;
import org.egov.rms.model.*;
import org.egov.rms.service.CenterIdMappingService;
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

import static org.egov.rms.service.RestTemplateSslUtils.restTemplateAcceptingAllCerts;

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
     * API filters facilities with solar consumption < 10% for last 7 days
     */
    public List<RMSFacilityData> collectPanelData() {
        log.info("Collecting panel data for solar vs grid consumption analysis");
        List<RMSFacilityData> allFacilities = new ArrayList<>();
        
        try {
            int page = 1;
            int pageSize = 100;
            boolean hasMore = true;

            while (hasMore) {
                // Build request matching the working curl
                Map<String, Object> timePeriod = new HashMap<>();
                timePeriod.put("value", "last_seven_days");
                
                Map<String, Object> timeRange = new HashMap<>();
                timeRange.put("time_period", timePeriod);
                timeRange.put("custom_range", new HashMap<>());
                
                // Filters should be an array as per the curl example
                List<Map<String, Object>> filters = new ArrayList<>();
                Map<String, Object> filter = new HashMap<>();
                filter.put("compareFunction", "lte");
                filter.put("compareValue", 10);
                filters.add(filter);
                
                Map<String, Object> pagination = new HashMap<>();
                pagination.put("page", page);
                pagination.put("size", pageSize);
                
                Map<String, Object> requestBody = new HashMap<>();
                requestBody.put("graphType", "solarVsGrid_Eb_Diff");
                requestBody.put("time_range", timeRange);
                requestBody.put("frequency", "daily");
                requestBody.put("aggregation", "deltaSum");
                requestBody.put("filters", filters);
                requestBody.put("pagination", pagination);

                PanelGraphResponse response = callPanelGraphApi(config.getCenterDetailsEndpoint(), requestBody);
                
                if (response != null && response.getData() != null && 
                    response.getData().getFacilities() != null) {
                    
                    List<PanelGraphResponse.PanelFacility> facilities = response.getData().getFacilities();
                    
                    for (PanelGraphResponse.PanelFacility panelFacility : facilities) {
                        // Convert PanelFacility to RMSFacilityData
                        RMSFacilityData facility = convertPanelFacilityToRMSFacilityData(panelFacility);
                        if (facility != null) {
                            allFacilities.add(facility);
                        }
                    }
                    
                    // Check if there are more pages (if pagination info is available)
                    if (facilities.size() < pageSize) {
                        hasMore = false;
                    } else {
                        page++;
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
     * Converts PanelFacility to RMSFacilityData
     */
    private RMSFacilityData convertPanelFacilityToRMSFacilityData(PanelGraphResponse.PanelFacility panelFacility) {
        try {
            PanelGraphResponse.CenterData centerData = panelFacility.getCenterData();
            PanelGraphResponse.Consumption consumption = panelFacility.getConsumption();
            
            if (centerData == null || consumption == null) {
                return null;
            }
            
            // Map HFRID to hfrId (handle "Not available" case)
            String hfrId = null;
            if (centerData.getHfrid() != null && 
                !centerData.getHfrid().isEmpty() && 
                !centerData.getHfrid().equalsIgnoreCase("Not available")) {
                hfrId = centerData.getHfrid().trim();
            }
            
            RMSFacilityData facility = RMSFacilityData.builder()
                    .facilityId(centerData.getCenterId())
                    .centerId(centerData.getCenterId())
                    .facilityName(centerData.getCenterName())
                    .centerName(centerData.getCenterName())
                    .hfrId(hfrId)
                    .deviceName(centerData.getDeviceName())
                    .statusOfDevice(centerData.getStatusOfDevice())
                    .solarPercent(consumption.getSolarPercents())
                    .solarConsumption(consumption.getSolarDatas())
                    .gridConsumption(consumption.getGridDatas())
                    .build();
            
            return facility;
        } catch (Exception e) {
            log.error("Error converting PanelFacility to RMSFacilityData", e);
            return null;
        }
    }

    /**
     * Calls center_details/graph API for panel data
     */
    private PanelGraphResponse callPanelGraphApi(String endpoint, Map<String, Object> requestBody) throws Exception {
        String url = config.getRmsApiBaseUrl() + endpoint;
        RestTemplate rt = restTemplateAcceptingAllCerts();
        HttpHeaders headers = new HttpHeaders();
        headers.set("Content-Type", "application/json");
        headers.set("Accept", "application/json");
        headers.set("Access-Token", config.getRmsApiAccessToken());
        
        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);
        
        int attempts = 0;
        long delay = config.getRetryBackoffDelay();
        
        while (attempts < config.getRetryMaxAttempts()) {
            try {
                log.debug("Calling RMS panel graph API: {} (attempt {})", url, attempts + 1);
                ResponseEntity<PanelGraphResponse> response = rt.exchange(
                        url, HttpMethod.POST, entity, PanelGraphResponse.class);
                
                if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                    return response.getBody();
                }
            } catch (RestClientException e) {
                attempts++;
                if (attempts >= config.getRetryMaxAttempts()) {
                    log.error("Failed to call RMS panel graph API after {} attempts: {}", config.getRetryMaxAttempts(), e.getMessage());
                    throw e;
                }
                log.warn("RMS panel graph API call failed (attempt {}), retrying after {}ms: {}", attempts, delay, e.getMessage());
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

    /**
     * Collects inverter data for devices with no signal
     * API response structure: { "data": [...], "pagination": { "noOfRecords": ... } }
     */
    public List<RMSFacilityData> collectInverterNoSignalData() {
        log.info("Collecting inverter data for devices with no signal");
        List<RMSFacilityData> allFacilities = new ArrayList<>();
        
        try {
            RMSApiRequest request = RMSApiRequest.builder()
                    .status(List.of(new HashMap<String, String>() {{
                        put("label", "Inactive");
                    }}))
                    .pagination(RMSApiRequest.Pagination.builder()
                            .page(1)
                            .size(10000)
                            .build())
                    .build();

            // Call centerDatas/get API - this has a different response structure
            RMSApiResponseV2 response = callCenterDatasApi(config.getCenterDatasEndpoint(), request);
            
            if (response != null && response.getData() != null) {
                List<RMSFacilityData> facilities = response.getData();
                
                // Calculate cutoff time: 2 days ago from now
                Instant cutoffTime = Instant.now().minus(config.getInverterNoSignalDays(), ChronoUnit.DAYS);
                
                log.debug("Filtering facilities with last_sync_time before: {}", cutoffTime);
                
                // Filter facilities with no signal for more than configured days
                for (RMSFacilityData facility : facilities) {
                    // Map centerDatas/get API fields to standard fields for consistency
                    if (facility.getCenterId() != null && facility.getFacilityId() == null) {
                        facility.setFacilityId(facility.getCenterId());
                    }
                    if (facility.getCenterName() != null && facility.getFacilityName() == null) {
                        facility.setFacilityName(facility.getCenterName());
                    }
                    // Map HFRID to hfrId for consistency
//                    if (facility.getHfrid() != null && !facility.getHfrid().isEmpty()) {
//                        facility.setHfrId(facility.getHfrid());
//                    }
                    
                    // Check if last_sync_time is before cutoff (2 days ago)
                    if (facility.getLastSyncTime() != null && 
                        facility.getLastSyncTime().isBefore(cutoffTime)) {
                        allFacilities.add(facility);
                        log.debug("Found facility with no signal: centerId={}, facilityName={}, lastSyncTime={}, hfrId={}", 
                                facility.getCenterId(), facility.getFacilityName(), 
                                facility.getLastSyncTime(), facility.getHfrId());
                    }
                }
                
                log.info("Found {} facilities with no signal for more than {} days (out of {} total)", 
                        allFacilities.size(), config.getInverterNoSignalDays(), facilities.size());
            } else {
                log.warn("No data received from centerDatas/get API");
            }

            // Enrich with HFR IDs from mapping table (for facilities that don't have HFRID in response)
            mappingService.enrichFacilitiesWithHfrId(allFacilities);
            
            log.info("Collected inverter no-signal data for {} facilities", allFacilities.size());
            return allFacilities;
        } catch (Exception e) {
            log.error("Error collecting inverter no-signal data", e);
            return new ArrayList<>();
        }
    }

    /**
     * Collects inverter data for high voltage conditions
     * API filters facilities with PCU voltage > 250V for today
     */
    public List<RMSFacilityData> collectInverterHighVoltageData() {
        log.info("Collecting inverter data for high voltage conditions");
        List<RMSFacilityData> allFacilities = new ArrayList<>();
        
        try {
            int page = 1;
            int pageSize = 100;
            boolean hasMore = true;

            while (hasMore) {
                // Build request matching the working curl
                Map<String, Object> timePeriod = new HashMap<>();
                timePeriod.put("label", "Today");
                timePeriod.put("value", "today");
                
                Map<String, Object> timeRange = new HashMap<>();
                timeRange.put("time_period", timePeriod);
                timeRange.put("custom_range", new HashMap<>());
                
                List<Map<String, Object>> filters = new ArrayList<>();
                Map<String, Object> filter = new HashMap<>();
                filter.put("compareFunction", "gt");
                filter.put("compareValue", config.getInverterHighVoltageThreshold());
                filters.add(filter);
                
                Map<String, Object> pagination = new HashMap<>();
                pagination.put("page", page);
                pagination.put("size", pageSize);
                
                Map<String, Object> requestBody = new HashMap<>();
                requestBody.put("graphType", "PCUvoltage_Filtered");
                requestBody.put("time_range", timeRange);
                requestBody.put("frequency", "daily");
                requestBody.put("aggregation", "avg");
                requestBody.put("filters", filters);
                requestBody.put("pagination", pagination);

                InverterVoltageResponse response = callInverterVoltageApi(config.getCenterDetailsEndpoint(), requestBody);
                
                if (response != null && response.getCenterDatas() != null) {
                    List<InverterVoltageResponse.CenterData> centerDatas = response.getCenterDatas();
                    
                    for (InverterVoltageResponse.CenterData centerData : centerDatas) {
                        // Convert CenterData to RMSFacilityData
                        RMSFacilityData facility = convertInverterVoltageToRMSFacilityData(centerData);
                        if (facility != null) {
                            allFacilities.add(facility);
                        }
                    }
                    
                    // Check if there are more pages
                    if (centerDatas.size() < pageSize) {
                        hasMore = false;
                    } else {
                        page++;
                    }
                } else {
                    hasMore = false;
                }
            }

            log.info("Collected inverter high voltage data for {} facilities", allFacilities.size());
            
            // Enrich with HFR IDs from mapping table
            mappingService.enrichFacilitiesWithHfrId(allFacilities);
            
            return allFacilities;
        } catch (Exception e) {
            log.error("Error collecting inverter high voltage data", e);
            return new ArrayList<>();
        }
    }

    /**
     * Converts InverterVoltageResponse.CenterData to RMSFacilityData
     */
    private RMSFacilityData convertInverterVoltageToRMSFacilityData(InverterVoltageResponse.CenterData centerData) {
        try {
            if (centerData == null) {
                return null;
            }
            
            // Map HFRID to hfrId (handle "Not available" case)
            String hfrId = null;
            if (centerData.getHfrid() != null && 
                !centerData.getHfrid().isEmpty() && 
                !centerData.getHfrid().equalsIgnoreCase("Not available") &&
                !centerData.getHfrid().equalsIgnoreCase("Not Available")) {
                hfrId = centerData.getHfrid().trim();
            }
            
            RMSFacilityData facility = RMSFacilityData.builder()
                    .facilityId(centerData.getCenterId())
                    .centerId(centerData.getCenterId())
                    .facilityName(centerData.getCenterName())
                    .centerName(centerData.getCenterName())
                    .hfrId(hfrId)
                    .deviceName(centerData.getDeviceName())
                    .statusOfDevice(centerData.getStatusOfDevice())
                    .voltage(centerData.getVoltage())
                    .build();
            
            return facility;
        } catch (Exception e) {
            log.error("Error converting InverterVoltageResponse.CenterData to RMSFacilityData", e);
            return null;
        }
    }

    /**
     * Calls center_details/graph API for inverter voltage data
     */
    private InverterVoltageResponse callInverterVoltageApi(String endpoint, Map<String, Object> requestBody) throws Exception {
        String url = config.getRmsApiBaseUrl() + endpoint;
        RestTemplate rt = restTemplateAcceptingAllCerts();
        HttpHeaders headers = new HttpHeaders();
        headers.set("Content-Type", "application/json");
        headers.set("Accept", "application/json");
        headers.set("Access-Token", config.getRmsApiAccessToken());
        
        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);
        
        int attempts = 0;
        long delay = config.getRetryBackoffDelay();
        
        while (attempts < config.getRetryMaxAttempts()) {
            try {
                log.debug("Calling RMS inverter voltage API: {} (attempt {})", url, attempts + 1);
                ResponseEntity<InverterVoltageResponse> response = rt.exchange(
                        url, HttpMethod.POST, entity, InverterVoltageResponse.class);
                
                if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                    return response.getBody();
                }
            } catch (RestClientException e) {
                attempts++;
                if (attempts >= config.getRetryMaxAttempts()) {
                    log.error("Failed to call RMS inverter voltage API after {} attempts: {}", config.getRetryMaxAttempts(), e.getMessage());
                    throw e;
                }
                log.warn("RMS inverter voltage API call failed (attempt {}), retrying after {}ms: {}", attempts, delay, e.getMessage());
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

    /**
     * Collects battery data for voltage = 0 conditions
     * API filters facilities with battery voltage = 0 for today
     */
    public List<RMSFacilityData> collectBatteryVoltageZeroData() {
        log.info("Collecting battery data for voltage = 0 conditions");
        List<RMSFacilityData> allFacilities = new ArrayList<>();
        
        try {
            int page = 1;
            int pageSize = 100;
            boolean hasMore = true;

            while (hasMore) {
                // Build request matching the working curl
                Map<String, Object> timePeriod = new HashMap<>();
                timePeriod.put("value", "today");
                
                Map<String, Object> timeRange = new HashMap<>();
                timeRange.put("time_period", timePeriod);
                timeRange.put("custom_range", new HashMap<>());
                
                List<Map<String, Object>> filters = new ArrayList<>();
                Map<String, Object> filter = new HashMap<>();
                filter.put("compareFunction", "eq");
                filter.put("compareValue", 0);
                filters.add(filter);
                
                Map<String, Object> requestBody = new HashMap<>();
                requestBody.put("graphType", "batteryVoltage_Filtered");
                requestBody.put("time_range", timeRange);
                requestBody.put("frequency", "daily");
                requestBody.put("aggregation", "avg");
                requestBody.put("filters", filters);

                PanelGraphResponse response = callBatteryVoltageApi(config.getCenterDetailsEndpoint(), requestBody);
                
                if (response != null && response.getData() != null && 
                    response.getData().getFacilities() != null) {
                    
                    List<PanelGraphResponse.PanelFacility> facilities = response.getData().getFacilities();
                    
                    for (PanelGraphResponse.PanelFacility panelFacility : facilities) {
                        // Convert PanelFacility to RMSFacilityData
                        RMSFacilityData facility = convertBatteryFacilityToRMSFacilityData(panelFacility);
                        if (facility != null) {
                            allFacilities.add(facility);
                        }
                    }
                    
                    // Check if there are more pages
                    if (facilities.size() < pageSize) {
                        hasMore = false;
                    } else {
                        page++;
                    }
                } else {
                    hasMore = false;
                }
            }

            log.info("Collected battery voltage zero data for {} facilities", allFacilities.size());
            
            // Enrich with HFR IDs from mapping table
            mappingService.enrichFacilitiesWithHfrId(allFacilities);
            
            return allFacilities;
        } catch (Exception e) {
            log.error("Error collecting battery voltage zero data", e);
            return new ArrayList<>();
        }
    }

    /**
     * Converts PanelFacility to RMSFacilityData for battery voltage = 0
     */
    private RMSFacilityData convertBatteryFacilityToRMSFacilityData(PanelGraphResponse.PanelFacility panelFacility) {
        try {
            PanelGraphResponse.CenterData centerData = panelFacility.getCenterData();
            PanelGraphResponse.Consumption consumption = panelFacility.getConsumption();
            
            if (centerData == null || consumption == null) {
                return null;
            }
            
            // Map HFRID to hfrId (handle "Not available" case)
            String hfrId = null;
            if (centerData.getHfrid() != null && 
                !centerData.getHfrid().isEmpty() && 
                !centerData.getHfrid().equalsIgnoreCase("Not available")) {
                hfrId = centerData.getHfrid().trim();
            }
            
            // Extract battery voltage from voltageReadings (should be 0)
            Double batteryVoltage = null;
            if (consumption.getVoltageReadings() != null && !consumption.getVoltageReadings().isEmpty()) {
                batteryVoltage = consumption.getVoltageReadings().get(0);
            }
            
            RMSFacilityData facility = RMSFacilityData.builder()
                    .facilityId(centerData.getCenterId())
                    .centerId(centerData.getCenterId())
                    .facilityName(centerData.getCenterName())
                    .centerName(centerData.getCenterName())
                    .hfrId(hfrId)
                    .deviceName(centerData.getDeviceName())
                    .statusOfDevice(centerData.getStatusOfDevice())
                    .batteryVoltage(batteryVoltage)
                    .build();
            
            return facility;
        } catch (Exception e) {
            log.error("Error converting battery PanelFacility to RMSFacilityData", e);
            return null;
        }
    }

    /**
     * Calls center_details/graph API for battery voltage data
     */
    private PanelGraphResponse callBatteryVoltageApi(String endpoint, Map<String, Object> requestBody) throws Exception {
        String url = config.getRmsApiBaseUrl() + endpoint;
        RestTemplate rt = restTemplateAcceptingAllCerts();
        HttpHeaders headers = new HttpHeaders();
        headers.set("Content-Type", "application/json");
        headers.set("Accept", "application/json");
        headers.set("Access-Token", config.getRmsApiAccessToken());
        
        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);
        
        int attempts = 0;
        long delay = config.getRetryBackoffDelay();
        
        while (attempts < config.getRetryMaxAttempts()) {
            try {
                log.debug("Calling RMS battery voltage API: {} (attempt {})", url, attempts + 1);
                ResponseEntity<PanelGraphResponse> response = rt.exchange(
                        url, HttpMethod.POST, entity, PanelGraphResponse.class);
                
                if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                    return response.getBody();
                }
            } catch (RestClientException e) {
                attempts++;
                if (attempts >= config.getRetryMaxAttempts()) {
                    log.error("Failed to call RMS battery voltage API after {} attempts: {}", config.getRetryMaxAttempts(), e.getMessage());
                    throw e;
                }
                log.warn("RMS battery voltage API call failed (attempt {}), retrying after {}ms: {}", attempts, delay, e.getMessage());
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
     * Calls centerDatas/get API with retry logic
     * This API has a different response structure: { "data": [...], "pagination": { "noOfRecords": ... } }
     */
    private RMSApiResponseV2 callCenterDatasApi(String endpoint, RMSApiRequest request) throws Exception {
        String url = config.getRmsApiBaseUrl() + endpoint;
        RestTemplate rt = restTemplateAcceptingAllCerts();
        HttpHeaders headers = new HttpHeaders();
        headers.set("Content-Type", "application/json");
        headers.set("Accept", "application/json");
        headers.set("Access-Token", config.getRmsApiAccessToken());
        
        HttpEntity<RMSApiRequest> entity = new HttpEntity<>(request, headers);
        
        int attempts = 0;
        long delay = config.getRetryBackoffDelay();
        
        while (attempts < config.getRetryMaxAttempts()) {
            try {
                log.debug("Calling RMS centerDatas API: {} (attempt {})", url, attempts + 1);
                ResponseEntity<RMSApiResponseV2> response = rt.exchange(
                        url, HttpMethod.POST, entity, RMSApiResponseV2.class);
                
                if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                    RMSApiResponseV2 tmp = response.getBody();
                    List<RMSFacilityData> listFacility = new ArrayList<>();
                    for (CenterData centerData : tmp.getCenterData()){
                        RMSFacilityData data = RMSFacilityData.builder()
                                .centerId(centerData.getCenterId())
                                .centerName(centerData.getCenterName())
                                .hfrId(centerData.getHfrid())
                                .lastSyncTime(Instant.parse(centerData.getLastSyncTime()))
                                .build();
                        listFacility.add(data);
                    }
                    tmp.setData(listFacility);
                    return tmp;
                }
            } catch (RestClientException e) {
                attempts++;
                if (attempts >= config.getRetryMaxAttempts()) {
                    log.error("Failed to call RMS centerDatas API after {} attempts: {}", config.getRetryMaxAttempts(), e.getMessage());
                    throw e;
                }
                log.warn("RMS centerDatas API call failed (attempt {}), retrying after {}ms: {}", attempts, delay, e.getMessage());
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

    /**
     * Calls RMS API with retry logic
     */
    private RMSApiResponse callRMSApi(String endpoint, RMSApiRequest request) throws Exception {
        String url = config.getRmsApiBaseUrl() + endpoint;
        RestTemplate rt = restTemplateAcceptingAllCerts();
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
                ResponseEntity<RMSApiResponse> response = rt.exchange(
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

