package org.egov.rms.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.egov.rms.config.RMSConfiguration;
import org.egov.rms.model.CenterIdToHfrIdMapping;
import org.egov.rms.model.CenterMappingResponse;
import org.egov.rms.model.RMSFacilityData;
import org.egov.rms.repository.CenterIdMappingRepository;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.egov.rms.service.RestTemplateSslUtils.restTemplateAcceptingAllCerts;

@Slf4j
@Service
@RequiredArgsConstructor
public class CenterIdMappingService {

    private final CenterIdMappingRepository mappingRepository;
    private final FacilityServiceClient facilityServiceClient;
    private final RMSConfiguration config;
    private final RestTemplate restTemplate;

    /**
     * Syncs mappings from RMS mapping API
     * This API is updated weekly by RMS team with latest Center ID to HFR ID and NIN ID mappings
     */
    public void syncMappingsFromApi() {
        log.info("Starting Center ID to HFR ID mapping sync from RMS API");
        
        try {
            CenterMappingResponse response = fetchMappingsFromApi();
            
            if (response == null || response.getList() == null || response.getList().isEmpty()) {
                log.warn("No mapping data received from RMS API");
                return;
            }
            
            int synced = 0;
            int updated = 0;
            int missingHfrId = 0;
            
            for (CenterMappingResponse.CenterMapping mappingData : response.getList()) {
                String centerId = mappingData.getCenterId();
                
                if (centerId == null || centerId.isEmpty()) {
                    continue;
                }
                
                // Extract HFR ID - handle "Not Available" and "Not available" as null
                String hfrid = mappingData.getHfrid();
                String hfrId = null;
                if (hfrid != null && !hfrid.isEmpty() && 
                    !hfrid.equalsIgnoreCase("Not Available") && 
                    !hfrid.equalsIgnoreCase("Not available")) {
                    hfrId = hfrid.trim();
                } else {
                    missingHfrId++;
                    log.debug("HFR ID missing or not available for center: {}, facility: {}", 
                            centerId, mappingData.getHealthCenterName());
                }
                
                // Extract NIN ID - handle empty strings as null
                String nin = mappingData.getNin();
                String ninId = (nin != null && !nin.trim().isEmpty()) ? nin.trim() : null;
                
                String facilityName = mappingData.getHealthCenterName();
                
                // Extract device instance ID from centerId (format: device_instance_XXXXX_...)
                String deviceInstanceId = centerId;
                String deviceId = centerId; // Same as centerId in this API
                
                // Create or update mapping
                CenterIdToHfrIdMapping mapping = CenterIdToHfrIdMapping.builder()
                        .id(UUID.randomUUID().toString())
                        .centerId(centerId)
                        .deviceId(deviceId)
                        .deviceInstanceId(deviceInstanceId)
                        .hfrId(hfrId)
                        .ninId(ninId)
                        .facilityName(facilityName)
                        .isActive(true)
                        .lastSyncTime(Instant.now())
                        .lastValidatedAt(Instant.now())
                        .build();
                
                // Check if mapping exists
                boolean exists = mappingRepository.findByCenterId(centerId).isPresent();
                
                mappingRepository.saveOrUpdateMapping(mapping);
                
                if (exists) {
                    updated++;
                } else {
                    synced++;
                }
            }
            
            log.info("Mapping sync completed: {} new, {} updated, {} missing HFR ID", 
                    synced, updated, missingHfrId);
            
        } catch (Exception e) {
            log.error("Error during mapping sync from API", e);
        }
    }

    /**
     * Syncs mappings from provided facility data (fallback method)
     * This should be called periodically (every 7 days) to refresh mappings
     */
    public void syncMappings(List<RMSFacilityData> facilities) {
        log.info("Starting Center ID to HFR ID mapping sync for {} facilities", facilities.size());
        
        try {
            int synced = 0;
            int updated = 0;
            int missingHfrId = 0;
            
            for (RMSFacilityData facility : facilities) {
                String centerId = facility.getCenterId() != null ? 
                        facility.getCenterId() : facility.getFacilityId();
                
                if (centerId == null || centerId.isEmpty()) {
                    continue;
                }
                
                // Try to get HFR ID from existing mapping or fetch from Facility Registry
                String hfrId = facility.getHfrId();
                
                if (hfrId == null || hfrId.isEmpty()) {
                    // Try to find from existing mapping
                    hfrId = mappingRepository.findHfrIdByCenterId(centerId)
                            .orElse(null);
                    
                    // If still not found, try to fetch from Facility Registry by facility name
                    if (hfrId == null && facility.getFacilityName() != null) {
                        // This would require additional Facility Registry search by name
                        // For now, we'll mark it for manual review
                        log.warn("HFR ID not found for center: {}, facility: {}", 
                                centerId, facility.getFacilityName());
                        missingHfrId++;
                    }
                }
                
                // Create or update mapping
                CenterIdToHfrIdMapping mapping = CenterIdToHfrIdMapping.builder()
                        .id(UUID.randomUUID().toString())
                        .centerId(centerId)
                        .deviceId(facility.getFacilityId())
                        .deviceInstanceId(facility.getFacilityId())
                        .hfrId(hfrId)
                        .ninId(null) // Not available from facility data
                        .facilityName(facility.getFacilityName())
                        .isActive(true)
                        .lastSyncTime(Instant.now())
                        .lastValidatedAt(Instant.now())
                        .build();
                
                // Check if mapping exists
                boolean exists = mappingRepository.findByCenterId(centerId).isPresent();
                
                mappingRepository.saveOrUpdateMapping(mapping);
                
                if (exists) {
                    updated++;
                } else {
                    synced++;
                }
            }
            
            log.info("Mapping sync completed: {} new, {} updated, {} missing HFR ID", 
                    synced, updated, missingHfrId);
            
        } catch (Exception e) {
            log.error("Error during mapping sync", e);
        }
    }

    /**
     * Fetches mappings from RMS mapping API
     */
    private CenterMappingResponse fetchMappingsFromApi() throws Exception {
        RestTemplate rt = restTemplateAcceptingAllCerts();
        String url = config.getRmsApiBaseUrl() + config.getCenterMappingsEndpoint();
        
        HttpHeaders headers = new HttpHeaders();
        headers.set("Content-Type", "application/json");
        headers.set("Accept", "application/json");
        headers.set("Access-Token", config.getRmsApiAccessToken());
        
        HttpEntity<String> entity = new HttpEntity<>("", headers);
        
        int maxAttempts = config.getRetryMaxAttempts();
        long backoffDelay = config.getRetryBackoffDelay();
        int attempts = 0;
        
        while (attempts < maxAttempts) {
            try {
                log.debug("Calling RMS mapping API: {} (attempt {})", url, attempts + 1);
                
                ResponseEntity<CenterMappingResponse> response = rt.exchange(
                        url, HttpMethod.POST, entity, CenterMappingResponse.class);
                
                if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                    return response.getBody();
                }
            } catch (RestClientException e) {
                attempts++;
                if (attempts >= maxAttempts) {
                    log.error("Failed to call RMS mapping API after {} attempts: {}", maxAttempts, e.getMessage());
                    throw e;
                }
                
                log.warn("RMS mapping API call failed (attempt {}), retrying after {}ms: {}", 
                        attempts, backoffDelay, e.getMessage());
                
                try {
                    Thread.sleep(backoffDelay);
                    backoffDelay *= 2; // Exponential backoff
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    throw new RuntimeException("Interrupted during retry", ie);
                }
            }
        }
        
        return null;
    }

    /**
     * Enriches facility data with HFR ID from mapping table
     */
    public void enrichFacilityDataWithHfrId(RMSFacilityData facility) {
        String centerId = facility.getCenterId() != null ? 
                facility.getCenterId() : facility.getFacilityId();
        
        if (centerId == null || centerId.isEmpty()) {
            return;
        }
        
        // If HFR ID is already present, update mapping
        if (facility.getHfrId() != null && !facility.getHfrId().isEmpty()) {
            CenterIdToHfrIdMapping mapping = CenterIdToHfrIdMapping.builder()
                    .id(UUID.randomUUID().toString())
                    .centerId(centerId)
                    .deviceId(facility.getFacilityId())
                    .deviceInstanceId(facility.getFacilityId())
                    .hfrId(facility.getHfrId())
                    .facilityName(facility.getFacilityName())
                    .isActive(true)
                    .lastSyncTime(Instant.now())
                    .build();
            
            mappingRepository.saveOrUpdateMapping(mapping);
            return;
        }
        
        // Try to get HFR ID from mapping table
        Optional<String> hfrIdOpt = mappingRepository.findHfrIdByCenterId(centerId);
        if (hfrIdOpt.isPresent()) {
            facility.setHfrId(hfrIdOpt.get());
        }
    }

    /**
     * Enriches a list of facilities with HFR IDs
     */
    public void enrichFacilitiesWithHfrId(List<RMSFacilityData> facilities) {
        for (RMSFacilityData facility : facilities) {
            enrichFacilityDataWithHfrId(facility);
        }
    }

    /**
     * Validates mappings that are older than specified days
     */
    public void validateMappings(int daysOld) {
        log.info("Validating mappings older than {} days", daysOld);
        
        List<CenterIdToHfrIdMapping> mappings = mappingRepository.findMappingsNeedingValidation(daysOld);
        
        int validated = 0;
        int markedInactive = 0;
        
        for (CenterIdToHfrIdMapping mapping : mappings) {
            try {
                // Validate by checking if facility still exists
                if (mapping.getHfrId() != null && !mapping.getHfrId().isEmpty()) {
                    var facility = facilityServiceClient.getFacilityByHfrId(
                            mapping.getHfrId(), config.getDefaultTenantId());
                    
                    if (facility != null) {
                        // Update validation timestamp
                        mappingRepository.updateValidationTimestamp(mapping.getCenterId());
                        validated++;
                    } else {
                        // Facility not found, mark as inactive
                        mappingRepository.markInactive(mapping.getCenterId());
                        markedInactive++;
                        log.warn("Marking mapping as inactive - facility not found: centerId={}, hfrId={}", 
                                mapping.getCenterId(), mapping.getHfrId());
                    }
                } else {
                    // No HFR ID, update validation timestamp but keep as is
                    mappingRepository.updateValidationTimestamp(mapping.getCenterId());
                    validated++;
                }
            } catch (Exception e) {
                log.error("Error validating mapping for centerId: {}", mapping.getCenterId(), e);
            }
        }
        
        log.info("Mapping validation completed: {} validated, {} marked inactive", 
                validated, markedInactive);
    }
}

