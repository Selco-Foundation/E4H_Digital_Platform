package org.egov.rms.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.egov.rms.repository.CenterIdMappingRepository;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.Optional;
/**
 * Resolves Elmeasure/Selco {@code center_id} (device instance id) from the mapping table.
 * Lookup order: HFR ID → NIN ID only (no facility-name fallback).
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CenterIdResolverService {

    private final CenterIdMappingRepository mappingRepository;

    /**
     * Resolve device/center id using registry identifiers only (no explicit centerId override).
     * Order is strict by presence, not by match success:
     * hfr_id present → HFR lookup only;
     * else nin_id present → NIN lookup only.
     */
    public Optional<String> resolveCenterId(String hfrId, String ninId, String facilityName) {
        if (isPresent(hfrId)) {
            Optional<String> byHfr = mappingRepository.findCenterIdByHfrId(hfrId.trim());
            if (byHfr.isEmpty()) {
                log.debug("No center_id mapping for hfrId={}", hfrId);
            }
            return byHfr;
        }

        if (isPresent(ninId)) {
            Optional<String> byNin = mappingRepository.findCenterIdByNinId(ninId.trim());
            if (byNin.isEmpty()) {
                log.debug("No center_id mapping for ninId={}", ninId);
            }
            return byNin;
        }

        return Optional.empty();
    }

    /** Explicit centerId on the request wins; otherwise {@link #resolveCenterId(String, String, String)}. */
    public Optional<String> resolveCenterId(String centerId, String hfrId, String ninId, String facilityName) {
        if (isPresent(centerId)) {
            return Optional.of(centerId.trim());
        }
        return resolveCenterId(hfrId, ninId, facilityName);
    }

    private static boolean isPresent(String value) {
        if (!StringUtils.hasText(value)) {
            return false;
        }
        String trimmed = value.trim();
        return !"null".equalsIgnoreCase(trimmed) && !"NULL".equals(trimmed);
    }
}
