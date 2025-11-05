package org.egov.rms.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.egov.rms.config.RMSConfiguration;
import org.egov.rms.model.CenterIdToHfrIdMapping;
import org.egov.rms.model.RMSFacilityData;

import java.util.Optional;
import org.egov.rms.repository.CenterIdMappingRepository;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class CenterIdMappingService {

    private final CenterIdMappingRepository mappingRepository;
    private final FacilityServiceClient facilityServiceClient;
    private final RMSConfiguration config;

    /**
     * Syncs mappings from provided facility data
     * This should be called periodically (every 7 days) to refresh mappings
     */
    public void syncMappings(List<RMSFacilityData> facilities) {
        log.info("Starting Center ID to HFR ID mapping sync for {} facilities", facilities.size());
            
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

