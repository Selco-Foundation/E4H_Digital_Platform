package org.selco.e4h.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.egov.common.contract.request.RequestInfo;
import org.selco.e4h.config.CarbonEmissionProperties;
import org.selco.e4h.web.models.Co2Boundary;
import org.selco.e4h.web.models.Co2FacilityContext;
import org.selco.e4h.web.models.Co2LocalizationMessage;
import org.selco.e4h.web.models.Co2LocalizationResponse;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Resolves boundary display names via egov-localization ({@code rainmaker-in} module),
 * same pattern as im-services {@code LocalizationService#enrichLocalizedDistrictAndBlockNames}.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class Co2LocalizationClient {

    private static final int MAX_CODES_PER_REQUEST = 80;

    private final RestTemplate restTemplate;
    private final CarbonEmissionProperties properties;

    public void enrichBoundaryLocalizedNames(RequestInfo requestInfo,
                                           String tenantId,
                                           List<Co2FacilityContext> facilities) {
        if (facilities == null || facilities.isEmpty()) {
            return;
        }
        Set<String> localizationCodes = collectBoundaryLocalizationCodes(facilities);
        if (localizationCodes.isEmpty()) {
            return;
        }
        Map<String, String> labels = fetchLabels(requestInfo, tenantId, localizationCodes);
        for (Co2FacilityContext facility : facilities) {
            applyLabels(facility, labels);
        }
    }

    private void applyLabels(Co2FacilityContext facility, Map<String, String> labels) {
        Co2Boundary boundary = facility.getBoundary();
        String stateCode = codeFromBoundary(boundary, "stateCode", facility.getState());
        String districtCode = codeFromBoundary(boundary, "districtCode", facility.getDistrict());
        String blockCode = codeFromBoundary(boundary, "blockCode", facility.getBlock());

        facility.setStateLocalized(resolveLabel(labels, stateCode));
        facility.setDistrictLocalized(resolveLabel(labels, districtCode));
        facility.setBlockLocalized(resolveLabel(labels, blockCode));
    }

    private static String codeFromBoundary(Co2Boundary boundary, String field, String fallback) {
        if (boundary == null) {
            return fallback;
        }
        return switch (field) {
            case "stateCode" -> firstNonBlank(boundary.getStateCode(), fallback);
            case "districtCode" -> firstNonBlank(boundary.getDistrictCode(), fallback);
            case "blockCode" -> firstNonBlank(boundary.getBlockCode(), fallback);
            default -> fallback;
        };
    }

    private static String resolveLabel(Map<String, String> labels, String rawCode) {
        if (rawCode == null || rawCode.isBlank()) {
            return null;
        }
        String key = toLocalizationCode(rawCode);
        String localized = labels.get(key);
        return localized != null && !localized.isBlank() ? localized : rawCode;
    }

    static Set<String> collectBoundaryLocalizationCodes(List<Co2FacilityContext> facilities) {
        Set<String> codes = new LinkedHashSet<>();
        for (Co2FacilityContext facility : facilities) {
            Co2Boundary boundary = facility.getBoundary();
            addLocalizationCode(codes, codeFromBoundary(boundary, "stateCode", facility.getState()));
            addLocalizationCode(codes, codeFromBoundary(boundary, "districtCode", facility.getDistrict()));
            addLocalizationCode(codes, codeFromBoundary(boundary, "blockCode", facility.getBlock()));
        }
        return codes;
    }

    private static void addLocalizationCode(Set<String> codes, String rawCode) {
        if (rawCode != null && !rawCode.isBlank()) {
            codes.add(toLocalizationCode(rawCode));
        }
    }

    static String toLocalizationCode(String rawCode) {
        if (rawCode.startsWith("Boundary_")) {
            return rawCode;
        }
        return "Boundary_" + rawCode;
    }

    private Map<String, String> fetchLabels(RequestInfo requestInfo,
                                            String tenantId,
                                            Set<String> localizationCodes) {
        Map<String, String> merged = new HashMap<>();
        List<String> codeList = new ArrayList<>(localizationCodes);
        for (int i = 0; i < codeList.size(); i += MAX_CODES_PER_REQUEST) {
            List<String> chunk = codeList.subList(i, Math.min(i + MAX_CODES_PER_REQUEST, codeList.size()));
            merged.putAll(fetchLabelsChunk(requestInfo, tenantId, chunk));
        }
        return merged;
    }

    private Map<String, String> fetchLabelsChunk(RequestInfo requestInfo,
                                                 String tenantId,
                                                 List<String> localizationCodes) {
        String codesParam = String.join(",", localizationCodes);
        String url = UriComponentsBuilder
                .fromHttpUrl(properties.getLocalizationHost() + properties.getLocalizationContextPath()
                        + properties.getLocalizationSearchEndpoint())
                .queryParam("tenantId", tenantId)
                .queryParam("module", properties.getLocalizationBoundaryModule())
                .queryParam("locale", properties.getLocalizationLocale())
                .queryParam("codes", codesParam)
                .build()
                .toUriString();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Object> requestEntity = new HttpEntity<>(
                Collections.singletonMap("RequestInfo", requestInfo), headers);

        try {
            ResponseEntity<Co2LocalizationResponse> response = restTemplate.exchange(
                    url, HttpMethod.POST, requestEntity, Co2LocalizationResponse.class);
            Co2LocalizationResponse body = response.getBody();
            if (body == null || body.getMessages() == null) {
                return Map.of();
            }
            Map<String, String> result = new HashMap<>();
            for (Co2LocalizationMessage message : body.getMessages()) {
                if (message.getCode() != null && message.getMessage() != null && !message.getMessage().isBlank()) {
                    result.put(message.getCode(), message.getMessage());
                }
            }
            return result;
        } catch (Exception e) {
            log.warn("CO2 boundary localization failed tenantId={} codes={}: {}",
                    tenantId, localizationCodes.size(), e.getMessage());
            return Map.of();
        }
    }

    private static String firstNonBlank(String primary, String fallback) {
        if (primary != null && !primary.isBlank()) {
            return primary;
        }
        return fallback;
    }
}
