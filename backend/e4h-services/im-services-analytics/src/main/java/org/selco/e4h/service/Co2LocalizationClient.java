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
 * Resolves boundary display names via egov-localization ({@code rainmaker-in} module).
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
            Map<String, String> chunkResult = fetchLabelsChunk(requestInfo, tenantId, chunk, false);
            if (chunkResult.isEmpty()) {
                chunkResult = fetchLabelsChunk(requestInfo, tenantId, chunk, true);
            }
            merged.putAll(chunkResult);
        }
        return merged;
    }

    /**
     * @param codesInQuery when true, codes go in query string (Postman style); otherwise in POST body
     *                     (avoids ingress truncating long query strings in UAT).
     */
    private Map<String, String> fetchLabelsChunk(RequestInfo requestInfo,
                                                 String tenantId,
                                                 List<String> localizationCodes,
                                                 boolean codesInQuery) {
        UriComponentsBuilder urlBuilder = UriComponentsBuilder
                .fromHttpUrl(buildSearchUrl())
                .queryParam("tenantId", tenantId)
                .queryParam("module", properties.getLocalizationBoundaryModule())
                .queryParam("locale", properties.getLocalizationLocale());
        if (codesInQuery) {
            urlBuilder.queryParam("codes", String.join(",", localizationCodes));
        }
        String url = urlBuilder.build().toUriString();

        Map<String, Object> body = new HashMap<>();
        if (requestInfo != null) {
            body.put("RequestInfo", requestInfo);
        }
        if (!codesInQuery) {
            body.put("codes", localizationCodes);
        }

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        try {
            ResponseEntity<String> response = restTemplate.exchange(
                    url, HttpMethod.POST, new HttpEntity<>(body, headers), String.class);
            if (!response.getStatusCode().is2xxSuccessful() || response.getBody() == null) {
                return Map.of();
            }
            return parseMessages(response.getBody());
        } catch (Exception e) {
            log.debug("CO2 localization request failed tenantId={}: {}", tenantId, e.getMessage());
            return Map.of();
        }
    }

    /** Base URL only in EGOV_LOCALIZATION_HOST; path comes from properties. */
    String buildSearchUrl() {
        String host = properties.getLocalizationHost() == null
                ? ""
                : properties.getLocalizationHost().replaceAll("/+$", "");
        String context = properties.getLocalizationContextPath();
        String endpoint = properties.getLocalizationSearchEndpoint();
        if (host.contains("/localization/messages")) {
            return host + endpoint;
        }
        return host + context + endpoint;
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
            log.debug("Failed to parse localization response: {}", e.getMessage());
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
