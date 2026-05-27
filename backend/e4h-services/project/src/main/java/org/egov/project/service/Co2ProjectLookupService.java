package org.egov.project.service;

import lombok.RequiredArgsConstructor;
import org.egov.project.repository.Co2ProjectLookupRepository;
import org.egov.project.web.models.FacilityProjectMapping;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class Co2ProjectLookupService {

    private final Co2ProjectLookupRepository co2ProjectLookupRepository;

    public List<FacilityProjectMapping> fetchProjectsByFacilities(String tenantId, List<String> facilityIds) {
        if (facilityIds == null || facilityIds.isEmpty()) {
            return List.of();
        }
        return co2ProjectLookupRepository.fetchProjectsByFacilities(tenantId, facilityIds);
    }
}
