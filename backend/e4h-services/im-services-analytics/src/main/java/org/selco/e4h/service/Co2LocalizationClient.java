package org.selco.e4h.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.egov.common.contract.request.RequestInfo;
import org.selco.e4h.config.CarbonEmissionProperties;
import org.selco.e4h.web.models.Co2Boundary;
import org.selco.e4h.web.models.Co2FacilityContext;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Resolves boundary display names via egov-localization ({@code rainmaker-in} module),
 * same pattern as im-services / ingestion-service boundary localization.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class Co2LocalizationClient {

    private static final int MAX_CODES_PER_REQUEST = 80;

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    private final CarbonEmissionProperties properties;

    public void enrichBoundaryLocalizedNames(RequestInfo requestInfo,
                                           String tenantId,
                                           List<Co2FacilityContext> facilities) {
        if (facilities == null || facilities.isEmpty()) {
            return;
        }
        Set<String> localizationCodes = collectBoundaryLocalizationCodes(facilities);
        if (localizationCodes.isEmpty()) {
            log.warn("CO2 localization skipped — no boundary codes on {} facilities", facilities.size());
            return;
        }
        Map<String, String> labels = fetchLabels(requestInfo, tenantId, localizationCodes);
        for (Co2FacilityContext facility : facilities) {
            applyLabels(facility, labels);
        }
        long resolved = labels.size();
        if (resolved == 0) {
            log.warn("CO2 localization returned 0 messages tenantId={} module={} codesRequested={} — "
                            + "check EGOV_LOCALIZATION_HOST and rainmaker-in Boundary_* entries",
                    tenantId, properties.getLocalizationBoundaryModule(), localizationCodes.size());
        } else {
            log.info("CO2 localization resolved {}/{} boundary codes for tenantId={}",
                    resolved, localizationCodes.size(), tenantId);
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

    /**
     * Same contract as im-services / manual Postman: codes comma-separated in query string.
     */
    private Map<String, String> fetchLabelsChunk(RequestInfo requestInfo,
                                                 String tenantId,
                                                 List<String> localizationCodes) {
        String url = UriComponentsBuilder
                .fromHttpUrl(properties.getLocalizationHost() + properties.getLocalizationContextPath()
                        + properties.getLocalizationSearchEndpoint())
                .queryParam("tenantId", tenantId)
                .queryParam("module", properties.getLocalizationBoundaryModule())
                .queryParam("locale", properties.getLocalizationLocale())
                .queryParam("codes", String.join(",", localizationCodes))
                .build()
                .toUriString();

        Map<String, Object> body = new HashMap<>();
        if (requestInfo != null) {
            body.put("RequestInfo", requestInfo);
        }

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        try {
            ResponseEntity<String> response = restTemplate.exchange(
                    url, HttpMethod.POST, new HttpEntity<>(body, headers), String.class);
            if (!response.getStatusCode().is2xxSuccessful() || response.getBody() == null) {
                log.warn("CO2 localization HTTP {} tenantId={}", response.getStatusCode(), tenantId);
                return Map.of();
            }
            return parseMessages(response.getBody());
        } catch (Exception e) {
            log.warn("CO2 boundary localization failed tenantId={} host={} codes={}: {}",
                    tenantId, properties.getLocalizationHost(), localizationCodes.size(), e.getMessage());
            return Map.of();
        }
    }

    private Map<String, String> parseMessages(String responseBody) {
        Map<String, String> result = new HashMap<>();
        try {
            JsonNode root = objectMapper.readTree(responseBody);
            JsonNode messages = root.path("messages");
            if (!messages.isArray()) {
                return result;
            }
            for (JsonNode message : messages) {
                String code = message.path("code").asText(null);
                String text = message.path("message").asText(null);
                if (code != null && !code.isBlank() && text != null && !text.isBlank()) {
                    result.put(code, text);
                }
            }
        } catch (Exception e) {
            log.warn("Failed to parse localization response: {}", e.getMessage());
        }
        return result;
    }

    private static String firstNonBlank(String primary, String fallback) {
        if (primary != null && !primary.isBlank()) {
            return primary;
        }
        return fallback;
    }
}
