package org.egov.rms.service;

import org.egov.rms.model.EligibilitySnapshot;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.Instant;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;

@Service
public class FacilityEligibilityService {

    private volatile EligibilitySnapshot snapshot = EligibilitySnapshot.builder()
            .allowedHfrIds(Collections.emptySet())
            .allowedFacilityIds(Collections.emptySet())
            .generatedAt(Instant.EPOCH)
            .districtCount(0)
            .facilityCount(0)
            .build();

    public void refreshSnapshot(EligibilitySnapshot newSnapshot) {
        if (newSnapshot == null) {
            return;
        }
        this.snapshot = EligibilitySnapshot.builder()
                .allowedHfrIds(copySet(newSnapshot.getAllowedHfrIds()))
                .allowedFacilityIds(copySet(newSnapshot.getAllowedFacilityIds()))
                .generatedAt(newSnapshot.getGeneratedAt())
                .districtCount(newSnapshot.getDistrictCount())
                .facilityCount(newSnapshot.getFacilityCount())
                .build();
    }

    public boolean isEligibleByHfrOrFacilityId(String hfrId, String facilityId) {
        EligibilitySnapshot current = this.snapshot;

        // MDMS district list empty => district gating disabled => allow all.
        if (current.getDistrictCount() == 0) {
            return true;
        }

        if (StringUtils.hasText(hfrId)) {
            return current.getAllowedHfrIds().contains(hfrId.trim());
        }
        if (StringUtils.hasText(facilityId)) {
            return current.getAllowedFacilityIds().contains(facilityId.trim());
        }
        return false;
    }

    private LinkedHashSet<String> copySet(Collection<String> source) {
        return source == null ? new LinkedHashSet<>() : new LinkedHashSet<>(source);
    }
}
