package org.egov.rms.model.co2;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Co2ReferenceDataResponse {
    private String tenantId;
    private List<GridIntensityFactor> gridIntensityFactors;
    private List<ArchetypeLookup> archetypeLookups;
    private List<ArchetypeProperties> archetypeProperties;
    private List<StateSunshineHours> stateSunshineHours;
}
