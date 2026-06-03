package org.egov.rms.service;

import lombok.RequiredArgsConstructor;
import org.egov.rms.model.co2.Co2ReferenceDataResponse;
import org.egov.rms.repository.Co2ReferenceRepository;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class Co2ReferenceService {

    private final Co2ReferenceRepository co2ReferenceRepository;

    public Co2ReferenceDataResponse getReferenceData(String tenantId) {
        return Co2ReferenceDataResponse.builder()
                .tenantId(tenantId)
                .gridIntensityFactors(co2ReferenceRepository.findGridIntensityFactors(tenantId))
                .archetypeLookups(co2ReferenceRepository.findArchetypeLookups(tenantId))
                .archetypeProperties(co2ReferenceRepository.findArchetypeProperties(tenantId))
                .stateSunshineHours(co2ReferenceRepository.findStateSunshineHours(tenantId))
                .build();
    }
}
