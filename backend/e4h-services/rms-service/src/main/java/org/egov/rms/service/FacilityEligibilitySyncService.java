package org.egov.rms.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.egov.common.contract.request.RequestInfo;
import org.egov.rms.config.RMSConfiguration;
import org.egov.rms.model.EligibilitySnapshot;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.time.Instant;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
public class FacilityEligibilitySyncService {

    private final MDMSDistrictConfigService mdmsDistrictConfigService;
    private final RMSConfiguration config;
    private final RestTemplate restTemplate;

    public EligibilitySnapshot syncEligibleFacilities(RequestInfo requestInfo, String tenantId) {
        String resolvedTenantId = StringUtils.hasText(tenantId) ? tenantId.trim() : config.getDefaultTenantId();
        Set<String> allowedDistrictBoundaries = mdmsDistrictConfigService.getAllowedDistrictBoundaries(requestInfo, resolvedTenantId);

        if (allowedDistrictBoundaries.isEmpty()) {
            log.info("MDMS district allowlist empty: gating disabled for tenantId={}", resolvedTenantId);
            return EligibilitySnapshot.builder()
                    .allowedHfrIds(Collections.emptySet())
                    .allowedFacilityIds(Collections.emptySet())
                    .generatedAt(Instant.now())
                    .districtCount(0)
                    .facilityCount(0)
                    .build();
        }

        Set<String> allowedHfrIds = new LinkedHashSet<>();
        Set<String> allowedFacilityIds = new LinkedHashSet<>();

        for (String districtBoundaryCode : allowedDistrictBoundaries) {
            syncDistrictFacilities(resolvedTenantId, districtBoundaryCode, allowedHfrIds, allowedFacilityIds);
        }

        int facilityCount = allowedHfrIds.size() + allowedFacilityIds.size();
        log.info("Eligibility snapshot prepared: tenantId={}, districtCount={}, hfrCount={}, facilityIdCount={}",
                resolvedTenantId, allowedDistrictBoundaries.size(), allowedHfrIds.size(), allowedFacilityIds.size());

        return EligibilitySnapshot.builder()
                .allowedHfrIds(allowedHfrIds)
                .allowedFacilityIds(allowedFacilityIds)
                .generatedAt(Instant.now())
                .districtCount(allowedDistrictBoundaries.size())
                .facilityCount(facilityCount)
                .build();
    }

    private void syncDistrictFacilities(
            String tenantId,
            String districtBoundaryCode,
            Set<String> allowedHfrIds,
            Set<String> allowedFacilityIds
    ) {
        int offset = 0;
        int limit = Math.max(1, config.getMdmsFacilitySyncLimit() != null ? config.getMdmsFacilitySyncLimit() : 200);
        int totalCount = Integer.MAX_VALUE;

        while (offset < totalCount) {
            Map<String, Object> response = searchFacilitiesByDistrict(tenantId, districtBoundaryCode, limit, offset);
            if (response == null) {
                break;
            }
            totalCount = parseTotalCount(response);
            int fetched = processFacilities(response, allowedHfrIds, allowedFacilityIds);
            if (fetched == 0) {
                break;
            }
            offset += fetched;
        }
    }

    @SuppressWarnings("unchecked")
    private int processFacilities(
            Map<String, Object> response,
            Set<String> allowedHfrIds,
            Set<String> allowedFacilityIds
    ) {
        Object facilitiesObject = response.get("facilities");
        if (!(facilitiesObject instanceof List)) {
            return 0;
        }

        List<Map<String, Object>> facilities = (List<Map<String, Object>>) facilitiesObject;
        for (Map<String, Object> facility : facilities) {
            String districtBoundaryCode = extractDistrictBoundaryCode(facility);
            if (!StringUtils.hasText(districtBoundaryCode)) {
                continue;
            }

            String facilityStatus = extractString(facility.get("facility_status"));
            if ("UNINSTALLED".equalsIgnoreCase(facilityStatus)) {
                continue;
            }

            boolean rmsInactive = extractBoolean(facility.get("rms_inactive"));
            if (rmsInactive) {
                continue;
            }

            String hfrId = extractHfrId(facility);
            String facilityId = extractString(facility.get("facility_id"));

            if (!StringUtils.hasText(hfrId) && !StringUtils.hasText(facilityId)) {
                continue;
            }

            if (StringUtils.hasText(hfrId)) {
                allowedHfrIds.add(hfrId.trim());
            }
            if (StringUtils.hasText(facilityId)) {
                allowedFacilityIds.add(facilityId.trim());
            }
        }
        return facilities.size();
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private Map<String, Object> searchFacilitiesByDistrict(String tenantId, String districtBoundaryCode, int limit, int offset) {
        try {
            String url = config.getFacilityServiceBaseUrl() + config.getFacilityServiceSearchEndpoint();
            String uri = UriComponentsBuilder.fromUriString(url)
                    .queryParam("tenant_id", tenantId)
                    .queryParam("boundaryCode", districtBoundaryCode)
                    .queryParam("limit", limit)
                    .queryParam("offset", offset)
                    .toUriString();
            HttpHeaders headers = new HttpHeaders();
            headers.set("Accept", "application/json");
            HttpEntity<?> entity = new HttpEntity<>(headers);
            ResponseEntity<Map> response = restTemplate.exchange(uri, HttpMethod.GET, entity, Map.class);
            return response.getBody();
        } catch (Exception e) {
            log.error("Failed facility search for districtBoundaryCode={}, offset={}, limit={}",
                    districtBoundaryCode, offset, limit, e);
            return null;
        }
    }

    private int parseTotalCount(Map<String, Object> response) {
        Object totalCount = response.get("totalCount");
        if (totalCount instanceof Number) {
            return ((Number) totalCount).intValue();
        }
        if (totalCount instanceof String && StringUtils.hasText((String) totalCount)) {
            try {
                return Integer.parseInt(((String) totalCount).trim());
            } catch (NumberFormatException ignored) {
                return Integer.MAX_VALUE;
            }
        }
        return Integer.MAX_VALUE;
    }

    @SuppressWarnings("unchecked")
    private String extractHfrId(Map<String, Object> facility) {
        String topLevelHfr = extractString(facility.get("hfr_id"));
        if (StringUtils.hasText(topLevelHfr)) {
            return topLevelHfr;
        }
        Object detailsObject = facility.get("facility_details");
        if (detailsObject instanceof Map) {
            return extractString(((Map<String, Object>) detailsObject).get("hfr_id"));
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    private String extractDistrictBoundaryCode(Map<String, Object> facility) {
        Object boundaryObject = facility.get("boundary");
        if (!(boundaryObject instanceof Map)) {
            return null;
        }
        return extractString(((Map<String, Object>) boundaryObject).get("district"));
    }

    private String extractString(Object value) {
        if (value == null) {
            return null;
        }
        String str = String.valueOf(value);
        return StringUtils.hasText(str) ? str.trim() : null;
    }

    private boolean extractBoolean(Object value) {
        if (value instanceof Boolean) {
            return (Boolean) value;
        }
        return value != null && Boolean.parseBoolean(String.valueOf(value));
    }
}
