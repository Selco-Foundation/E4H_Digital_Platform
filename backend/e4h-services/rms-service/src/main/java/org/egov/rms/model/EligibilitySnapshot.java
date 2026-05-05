package org.egov.rms.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.Set;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EligibilitySnapshot {

    private Set<String> allowedHfrIds;
    private Set<String> allowedFacilityIds;
    private Instant generatedAt;
    private int districtCount;
    private int facilityCount;
}
