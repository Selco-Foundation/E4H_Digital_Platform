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
                
                Map<String, Object> requestBody = new HashMap<>();
                requestBody.put("graphType", "solarVsGrid_Eb_Diff");
                requestBody.put("time_range", timeRange);
                requestBody.put("frequency", "daily");
                requestBody.put("aggregation", "deltaSum");
                requestBody.put("filters", filters);

                Map<String, Object> pagination = new HashMap<>();
                pagination.put("page", page);
                pagination.put("size", pageSize);
                requestBody.put("pagination", pagination);

                PanelGraphResponse response = callPanelGraphApi(config.getCenterDetailsEndpoint(), requestBody);
                
                if (response != null && response.getData() != null && 
                    response.getData().getFacilities() != null) {
                    
                    List<PanelGraphResponse.PanelFacility> facilities = response.getData().getFacilities();
                    PanelGraphResponse.Pagination responsePagination = response.getData().getPagination();
                    
                    for (PanelGraphResponse.PanelFacility panelFacility : facilities) {
                        // Convert PanelFacility to RMSFacilityData
                        RMSFacilityData facility = convertPanelFacilityToRMSFacilityData(panelFacility);
                        if (facility != null) {
                            allFacilities.add(facility);
                        }
                    }

                    // Decide if there are more pages based on API pagination metadata when available
                    if (responsePagination != null && responsePagination.getTotalPages() != null) {
                        if (page >= responsePagination.getTotalPages()) {
                            break;
                        }
                        page++;
                    } else {
                        // Fallback to existing behaviour if pagination block is missing
                        if (facilities.size() < pageSize) {
                            break;
                        }
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
            
            if (response == null) {
                log.warn("No response received from centerDatas/get API");
                return allFacilities;
            }
            
            log.debug("API response received - centerData size: {}, data size: {}", 
                    response.getCenterData() != null ? response.getCenterData().size() : 0,
                    response.getData() != null ? response.getData().size() : 0);
            
            if (response.getData() != null) {
                List<RMSFacilityData> facilities = response.getData();
                
                log.info("API returned {} inactive facilities (API already filters for inactive status)", facilities.size());
                
                // API already filters for inactive devices, so we trust the API response
                // No additional filtering by lastSyncTime needed - API handles the filtering
                for (RMSFacilityData facility : facilities) {
                    // Map centerDatas/get API fields to standard fields for consistency
                    if (facility.getCenterId() != null && facility.getFacilityId() == null) {
                        facility.setFacilityId(facility.getCenterId());
                    }
                    if (facility.getCenterName() != null && facility.getFacilityName() == null) {
                        facility.setFacilityName(facility.getCenterName());
                    }
                    // Note: HFRID to hfrId mapping is already handled in callCenterDatasApi method
                    // when building RMSFacilityData from CenterData
                    
                    // Add all facilities returned by API (API already filtered for inactive devices)
                    allFacilities.add(facility);
                    log.debug("Added facility with no signal: centerId={}, facilityName={}, lastSyncTime={}, hfrId={}", 
                            facility.getCenterId(), facility.getFacilityName(), 
                            facility.getLastSyncTime(), facility.getHfrId());
                }
                
                log.info("Processing {} facilities from API (no additional filtering - API handles inactive status)", 
                        allFacilities.size());
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

                Map<String, Object> pagination = new HashMap<>();
                pagination.put("page", page);
                pagination.put("size", pageSize);
                requestBody.put("pagination", pagination);

                PanelGraphResponse response = callBatteryVoltageApi(config.getCenterDetailsEndpoint(), requestBody);
                
                if (response != null && response.getData() != null && 
                    response.getData().getFacilities() != null) {
                    
                    List<PanelGraphResponse.PanelFacility> facilities = response.getData().getFacilities();
                    PanelGraphResponse.Pagination responsePagination = response.getData().getPagination();
                    
                    for (PanelGraphResponse.PanelFacility panelFacility : facilities) {
                        // Convert PanelFacility to RMSFacilityData
                        RMSFacilityData facility = convertBatteryFacilityToRMSFacilityData(panelFacility);
                        if (facility != null) {
                            allFacilities.add(facility);
                        }
                    }

                    if (responsePagination != null && responsePagination.getTotalPages() != null) {
                        if (page >= responsePagination.getTotalPages()) {
                            break;
                        }
                        page++;
                    } else {
                        if (facilities.size() < pageSize) {
                            break;
                        }
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
     * Collects battery data for deep discharging/overcharging conditions
     * API identifies abnormal charging/discharging patterns over 2-3 days
     */
    public List<RMSFacilityData> collectBatteryDeepDischargeData() {
        log.info("Collecting battery data for deep discharging/overcharging conditions");
        List<RMSFacilityData> allFacilities = new ArrayList<>();
        
        try {
            int page = 1;
            int pageSize = 100;
            boolean hasMore = true;

            while (hasMore) {
                // Build request matching the working curl
                Map<String, Object> timePeriod = new HashMap<>();
                timePeriod.put("label", "Last 7 days");
                timePeriod.put("value", "last_three_days");
                
                Map<String, Object> timeRange = new HashMap<>();
                timeRange.put("time_period", timePeriod);
                timeRange.put("custom_range", new HashMap<>());
                
                Map<String, Object> requestBody = new HashMap<>();
                requestBody.put("graphType", "batteryChargeVsDischarge_Eb_Filtered");
                requestBody.put("time_range", timeRange);
                requestBody.put("frequency", "daily");
                requestBody.put("aggregation", "deltaSum");

                Map<String, Object> pagination = new HashMap<>();
                pagination.put("page", page);
                pagination.put("size", pageSize);
                requestBody.put("pagination", pagination);

                PanelGraphResponse response = callBatteryChargeDischargeApi(config.getCenterDetailsEndpoint(), requestBody);
                
                if (response != null && response.getData() != null && 
                    response.getData().getFacilities() != null) {
                    
                    List<PanelGraphResponse.PanelFacility> facilities = response.getData().getFacilities();
                    PanelGraphResponse.Pagination responsePagination = response.getData().getPagination();
                    
                    for (PanelGraphResponse.PanelFacility panelFacility : facilities) {
                        // Convert PanelFacility to RMSFacilityData
                        // Only include facilities with abnormal battery health (info field indicates abnormality)
                        if (panelFacility.getBatteryHealth() != null && 
                            panelFacility.getBatteryHealth().getInfo() != null &&
                            !panelFacility.getBatteryHealth().getInfo().isEmpty()) {
                            
                            RMSFacilityData facility = convertBatteryChargeDischargeToRMSFacilityData(panelFacility);
                            if (facility != null) {
                                allFacilities.add(facility);
                            }
                        }
                    }

                    if (responsePagination != null && responsePagination.getTotalPages() != null) {
                        if (page >= responsePagination.getTotalPages()) {
                            break;
                        }
                        page++;
                    } else {
                        if (facilities.size() < pageSize) {
                            break;
                        }
                        page++;
                    }
                } else {
                    hasMore = false;
                }
            }

            log.info("Collected battery deep discharge/overcharge data for {} facilities", allFacilities.size());
            
            // Enrich with HFR IDs from mapping table
            mappingService.enrichFacilitiesWithHfrId(allFacilities);
            
            return allFacilities;
        } catch (Exception e) {
            log.error("Error collecting battery deep discharge data", e);
            return new ArrayList<>();
        }
    }

    /**
     * Converts PanelFacility to RMSFacilityData for battery deep discharge/overcharge
     */
    private RMSFacilityData convertBatteryChargeDischargeToRMSFacilityData(PanelGraphResponse.PanelFacility panelFacility) {
        try {
            PanelGraphResponse.CenterData centerData = panelFacility.getCenterData();
            PanelGraphResponse.BatteryHealth batteryHealth = panelFacility.getBatteryHealth();
            
            if (centerData == null || batteryHealth == null) {
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
                    .batteryCharging(batteryHealth.getBatteryCharging())
                    .batteryDischarging(batteryHealth.getBatteryDischarging())
                    .batteryHealthInfo(batteryHealth.getInfo())
                    .build();
            
            return facility;
        } catch (Exception e) {
            log.error("Error converting battery charge/discharge PanelFacility to RMSFacilityData", e);
            return null;
        }
    }

    /**
     * Calls center_details/graph API for battery charge vs discharge data
     */
    private PanelGraphResponse callBatteryChargeDischargeApi(String endpoint, Map<String, Object> requestBody) throws Exception {
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
                log.debug("Calling RMS battery charge/discharge API: {} (attempt {})", url, attempts + 1);
                ResponseEntity<PanelGraphResponse> response = rt.exchange(
                        url, HttpMethod.POST, entity, PanelGraphResponse.class);
                
                if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                    return response.getBody();
                }
            } catch (RestClientException e) {
                attempts++;
                if (attempts >= config.getRetryMaxAttempts()) {
                    log.error("Failed to call RMS battery charge/discharge API after {} attempts: {}", config.getRetryMaxAttempts(), e.getMessage());
                    throw e;
                }
                log.warn("RMS battery charge/discharge API call failed (attempt {}), retrying after {}ms: {}", attempts, delay, e.getMessage());
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
     * Makes separate API calls for low voltage (< 200V) and high voltage (> 250V)
     */
    public List<RMSFacilityData> collectGridVoltageData() {
        log.info("Collecting grid voltage data");
        List<RMSFacilityData> allFacilities = new ArrayList<>();
        
        try {
            // Collect high voltage facilities (> 250V)
            List<RMSFacilityData> highVoltageFacilities = collectGridHighVoltageData();
            allFacilities.addAll(highVoltageFacilities);
            
            // Collect low voltage facilities (< 200V)
            List<RMSFacilityData> lowVoltageFacilities = collectGridLowVoltageData();
            allFacilities.addAll(lowVoltageFacilities);

            log.info("Collected grid voltage data for {} facilities ({} high, {} low)", 
                    allFacilities.size(), highVoltageFacilities.size(), lowVoltageFacilities.size());
            
            // Enrich with HFR IDs from mapping table
            mappingService.enrichFacilitiesWithHfrId(allFacilities);
            
            return allFacilities;
        } catch (Exception e) {
            log.error("Error collecting grid voltage data", e);
            return new ArrayList<>();
        }
    }

    /**
     * Collects grid voltage data for high voltage conditions (> 250V)
     */
    private List<RMSFacilityData> collectGridHighVoltageData() {
        log.info("Collecting grid high voltage data (> 250V)");
        List<RMSFacilityData> allFacilities = new ArrayList<>();
        
        try {
            int page = 1;
            int pageSize = 100;
            boolean hasMore = true;

            while (hasMore) {
                // Build request matching the working curl for high voltage
                Map<String, Object> timePeriod = new HashMap<>();
                timePeriod.put("value", "today");
                
                Map<String, Object> timeRange = new HashMap<>();
                timeRange.put("time_period", timePeriod);
                timeRange.put("custom_range", new HashMap<>());
                
                List<Map<String, Object>> filters = new ArrayList<>();
                Map<String, Object> filterHigh = new HashMap<>();
                filterHigh.put("compareFunction", "gt");
                filterHigh.put("compareValue", 250);
                filters.add(filterHigh);
                
                Map<String, Object> pagination = new HashMap<>();
                pagination.put("page", page);
                pagination.put("size", pageSize);
                
                Map<String, Object> requestBody = new HashMap<>();
                requestBody.put("graphType", "gridVoltage_Filtered");
                requestBody.put("time_range", timeRange);
                requestBody.put("frequency", "daily");
                requestBody.put("aggregation", "avg");
                requestBody.put("filters", filters);
                requestBody.put("pagination", pagination);

                PanelGraphResponse response = callGridVoltageApi(config.getCenterDetailsEndpoint(), requestBody);
                
                if (response != null && response.getData() != null && 
                    response.getData().getFacilities() != null) {
                    
                    List<PanelGraphResponse.PanelFacility> facilities = response.getData().getFacilities();
                    
                    for (PanelGraphResponse.PanelFacility panelFacility : facilities) {
                        // Convert PanelFacility to RMSFacilityData
                        RMSFacilityData facility = convertGridVoltageToRMSFacilityData(panelFacility, true);
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

            log.info("Collected grid high voltage data for {} facilities", allFacilities.size());
            return allFacilities;
        } catch (Exception e) {
            log.error("Error collecting grid high voltage data", e);
            return new ArrayList<>();
        }
    }

    /**
     * Collects grid voltage data for low voltage conditions (< 200V)
     */
    private List<RMSFacilityData> collectGridLowVoltageData() {
        log.info("Collecting grid low voltage data (< 200V)");
        List<RMSFacilityData> allFacilities = new ArrayList<>();
        
        try {
            int page = 1;
            int pageSize = 100;
            boolean hasMore = true;

            while (hasMore) {
                // Build request for low voltage
                Map<String, Object> timePeriod = new HashMap<>();
                timePeriod.put("value", "today");
                
                Map<String, Object> timeRange = new HashMap<>();
                timeRange.put("time_period", timePeriod);
                timeRange.put("custom_range", new HashMap<>());
                
                List<Map<String, Object>> filters = new ArrayList<>();
                Map<String, Object> filterLow = new HashMap<>();
                filterLow.put("compareFunction", "lt");
                filterLow.put("compareValue", 200);
                filters.add(filterLow);
                
                Map<String, Object> pagination = new HashMap<>();
                pagination.put("page", page);
                pagination.put("size", pageSize);
                
                Map<String, Object> requestBody = new HashMap<>();
                requestBody.put("graphType", "gridVoltage_Filtered");
                requestBody.put("time_range", timeRange);
                requestBody.put("frequency", "daily");
                requestBody.put("aggregation", "avg");
                requestBody.put("filters", filters);
                requestBody.put("pagination", pagination);

                PanelGraphResponse response = callGridVoltageApi(config.getCenterDetailsEndpoint(), requestBody);
                
                if (response != null && response.getData() != null && 
                    response.getData().getFacilities() != null) {
                    
                    List<PanelGraphResponse.PanelFacility> facilities = response.getData().getFacilities();
                    
                    for (PanelGraphResponse.PanelFacility panelFacility : facilities) {
                        // Convert PanelFacility to RMSFacilityData
                        RMSFacilityData facility = convertGridVoltageToRMSFacilityData(panelFacility, false);
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

            log.info("Collected grid low voltage data for {} facilities", allFacilities.size());
            return allFacilities;
        } catch (Exception e) {
            log.error("Error collecting grid low voltage data", e);
            return new ArrayList<>();
        }
    }

    /**
     * Converts PanelFacility to RMSFacilityData for grid voltage
     * @param isHighVoltage true for high voltage, false for low voltage
     */
    private RMSFacilityData convertGridVoltageToRMSFacilityData(PanelGraphResponse.PanelFacility panelFacility, boolean isHighVoltage) {
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
                !centerData.getHfrid().equalsIgnoreCase("Not available") &&
                !centerData.getHfrid().equalsIgnoreCase("Not Available")) {
                hfrId = centerData.getHfrid().trim();
            }
            
            // Extract grid voltage from voltageReadings
            Double gridVoltage = null;
            if (consumption.getVoltageReadings() != null && !consumption.getVoltageReadings().isEmpty()) {
                gridVoltage = consumption.getVoltageReadings().get(0);
            }
            
            RMSFacilityData facility = RMSFacilityData.builder()
                    .facilityId(centerData.getCenterId())
                    .centerId(centerData.getCenterId())
                    .facilityName(centerData.getCenterName())
                    .centerName(centerData.getCenterName())
                    .hfrId(hfrId)
                    .deviceName(centerData.getDeviceName())
                    .statusOfDevice(centerData.getStatusOfDevice())
                    .gridVoltage(gridVoltage)
                    .minVoltage(isHighVoltage ? null : gridVoltage) // Store as minVoltage for low voltage
                    .maxVoltage(isHighVoltage ? gridVoltage : null) // Store as maxVoltage for high voltage
                    .build();
            
            return facility;
        } catch (Exception e) {
            log.error("Error converting grid voltage PanelFacility to RMSFacilityData", e);
            return null;
        }
    }

    /**
     * Calls center_details/graph API for grid voltage data
     */
    private PanelGraphResponse callGridVoltageApi(String endpoint, Map<String, Object> requestBody) throws Exception {
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
                log.debug("Calling RMS grid voltage API: {} (attempt {})", url, attempts + 1);
                ResponseEntity<PanelGraphResponse> response = rt.exchange(
                        url, HttpMethod.POST, entity, PanelGraphResponse.class);
                
                if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                    return response.getBody();
                }
            } catch (RestClientException e) {
                attempts++;
                if (attempts >= config.getRetryMaxAttempts()) {
                    log.error("Failed to call RMS grid voltage API after {} attempts: {}", config.getRetryMaxAttempts(), e.getMessage());
                    throw e;
                }
                log.warn("RMS grid voltage API call failed (attempt {}), retrying after {}ms: {}", attempts, delay, e.getMessage());
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
                    
                    if (tmp.getCenterData() != null && !tmp.getCenterData().isEmpty()) {
                        log.debug("Parsing {} centerDatas from API response", tmp.getCenterData().size());
                        for (CenterData centerData : tmp.getCenterData()){
                            try {
                                // Parse lastSyncTime safely
                                Instant lastSyncTime = null;
                                if (centerData.getLastSyncTime() != null && !centerData.getLastSyncTime().isEmpty()) {
                                    try {
                                        lastSyncTime = Instant.parse(centerData.getLastSyncTime());
                                    } catch (Exception e) {
                                        log.warn("Failed to parse lastSyncTime for center {}: {}", 
                                                centerData.getCenterId(), centerData.getLastSyncTime());
                                    }
                                }
                                
                                // Map HFRID to hfrId (handle "Not Available" case)
                                String hfrId = null;
                                if (centerData.getHfrid() != null && !centerData.getHfrid().isEmpty() && 
                                    !centerData.getHfrid().equalsIgnoreCase("Not Available") &&
                                    !centerData.getHfrid().equalsIgnoreCase("Not available")) {
                                    hfrId = centerData.getHfrid().trim();
                                }
                                
                                RMSFacilityData data = RMSFacilityData.builder()
                                        .centerId(centerData.getCenterId())
                                        .centerName(centerData.getCenterName())
                                        .facilityId(centerData.getCenterId()) // Set facilityId same as centerId
                                        .facilityName(centerData.getCenterName()) // Set facilityName same as centerName
                                        .hfrId(hfrId)
                                        .hfrid(centerData.getHfrid()) // Keep original HFRID field too
                                        .lastSyncTime(lastSyncTime)
                                        .deviceName(centerData.getDeviceName())
                                        .statusOfDevice(centerData.getStatusOfDevice())
                                        .build();
                                listFacility.add(data);
                            } catch (Exception e) {
                                log.warn("Error parsing centerData for center {}: {}", 
                                        centerData.getCenterId(), e.getMessage());
                            }
                        }
                        log.info("Successfully parsed {} facilities from centerDatas API", listFacility.size());
                    } else {
                        log.warn("centerDatas array is null or empty in API response");
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

