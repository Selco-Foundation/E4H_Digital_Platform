package org.egov.activity.util;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.egov.activity.config.ActivityConfiguration;
import org.egov.common.contract.request.RequestInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.egov.activity.util.ActivityConstants.BOUNDARY_LOCALIZATION_MODULE;
import static org.egov.activity.util.ActivityConstants.LOCALIZATION_LOCALE;
import static org.egov.activity.util.ActivityConstants.LOCALIZATION_TENANT_ID;

/**
 * Resolves boundary codes (e.g. {@code India_Assam_Darrang}) to their human-readable localized
 * names via the localization service, where they are stored under the {@code Boundary_<code>} key
 * in the {@code rainmaker-in} module at the national tenant.
 * <p>
 * Generalizes the single-code lookup already used by {@code ActivityAnalyticsService.localizeStateName}
 * into a batch call so several boundary levels (state, block, ...) can be resolved in one request.
 * Best-effort by design: a boundary that cannot be localized yields no
 * entry in the returned map, and any transport/parsing failure yields an empty map rather than an
 * exception, so callers can fall back to the raw code.
 */
@Component
@Slf4j
public class BoundaryLocalizationUtil {

    private static final String BOUNDARY_CODE_PREFIX = "Boundary_";

    private final RestTemplate restTemplate;
    private final ActivityConfiguration configs;
    private final ObjectMapper mapper;

    @Autowired
    public BoundaryLocalizationUtil(RestTemplate restTemplate,
                                    ActivityConfiguration configs,
                                    @Qualifier("objectMapper") ObjectMapper mapper) {
        this.restTemplate = restTemplate;
        this.configs = configs;
        this.mapper = mapper;
    }

    /**
     * Resolves several boundary codes in a single localization call.
     *
     * @return map of boundary code -> localized name; codes without a localization are absent.
     */
    public Map<String, String> localizeBoundaryCodes(Collection<String> boundaryCodes, RequestInfo requestInfo) {
        Set<String> codes = new LinkedHashSet<>();
        if (boundaryCodes != null) {
            for (String boundaryCode : boundaryCodes) {
                if (boundaryCode != null && !boundaryCode.isBlank()) {
                    codes.add(boundaryCode.trim());
                }
            }
        }
        if (codes.isEmpty()) {
            return Collections.emptyMap();
        }

        String searchUrl = buildLocalizationSearchUrl();
        if (searchUrl == null) {
            log.warn("Localization is not configured; boundary codes will not be localized");
            return Collections.emptyMap();
        }

        String localizationCodes = String.join(",",
                codes.stream().map(code -> BOUNDARY_CODE_PREFIX + code).toList());

        try {
            String url = UriComponentsBuilder.fromHttpUrl(searchUrl)
                    .queryParam("tenantId", LOCALIZATION_TENANT_ID)
                    .queryParam("module", BOUNDARY_LOCALIZATION_MODULE)
                    .queryParam("locale", LOCALIZATION_LOCALE)
                    .queryParam("codes", localizationCodes)
                    .build()
                    .toUriString();

            Map<String, Object> body = new HashMap<>();
            if (requestInfo != null) {
                body.put("RequestInfo", requestInfo);
            }
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            ResponseEntity<String> response = restTemplate.exchange(
                    url, HttpMethod.POST, new HttpEntity<>(body.isEmpty() ? null : body, headers), String.class);
            if (!response.getStatusCode().is2xxSuccessful() || response.getBody() == null) {
                log.warn("Localization search returned no body for boundary codes: {}", localizationCodes);
                return Collections.emptyMap();
            }

            Map<String, String> namesByBoundaryCode = extractMessages(response.getBody());
            log.debug("Localized {}/{} boundary code(s)", namesByBoundaryCode.size(), codes.size());
            return namesByBoundaryCode;
        } catch (Exception e) {
            log.warn("Localization lookup failed for boundary codes {}: {}", localizationCodes, e.getMessage());
            return Collections.emptyMap();
        }
    }

    /**
     * Localized name for a boundary code, falling back to the raw code when it cannot be resolved,
     * so the caller always has something displayable.
     */
    public String localizedNameOrCode(Map<String, String> namesByBoundaryCode, String boundaryCode) {
        if (boundaryCode == null || boundaryCode.isBlank()) {
            return null;
        }
        String name = namesByBoundaryCode != null ? namesByBoundaryCode.get(boundaryCode.trim()) : null;
        return (name != null && !name.isBlank()) ? name : boundaryCode;
    }

    /* Maps the localization response back onto the bare boundary codes (drops the Boundary_ prefix). */
    @SuppressWarnings("unchecked")
    private Map<String, String> extractMessages(String responseBody) {
        Map<String, String> namesByBoundaryCode = new HashMap<>();
        try {
            Map<String, Object> root = mapper.readValue(responseBody, new TypeReference<Map<String, Object>>() {});
            Object messages = root.get("messages");
            if (!(messages instanceof List)) {
                return namesByBoundaryCode;
            }
            for (Object entry : (List<?>) messages) {
                if (!(entry instanceof Map)) {
                    continue;
                }
                Map<String, Object> message = (Map<String, Object>) entry;
                Object codeValue = message.get("code");
                Object textValue = message.get("message");
                if (codeValue == null || textValue == null) {
                    continue;
                }
                String code = codeValue.toString();
                String text = textValue.toString();
                if (!code.startsWith(BOUNDARY_CODE_PREFIX) || text.isBlank()) {
                    continue;
                }
                namesByBoundaryCode.put(code.substring(BOUNDARY_CODE_PREFIX.length()), text);
            }
        } catch (Exception e) {
            log.warn("Failed to parse localization response: {}", e.getMessage());
        }
        return namesByBoundaryCode;
    }

    /**
     * Builds the localization search URL without doubling the context path. Deployed environments
     * set {@code egov.localization.search.endpoint} to a full path that already contains
     * {@code egov.localization.context.path}; concatenating both would produce
     * {@code .../localization/messages/v1/localization/messages/v1/_search}, which the localization
     * service rejects with a 400.
     */
    private String buildLocalizationSearchUrl() {
        String host = configs.getLocalizationHost();
        String endpoint = configs.getLocalizationSearchEndpoint();
        if (host == null || host.isBlank() || endpoint == null || endpoint.isBlank()) {
            return null;
        }
        String base = trimTrailingSlash(host.trim());
        String contextPath = configs.getLocalizationContextPath();
        if (contextPath == null || contextPath.isBlank()) {
            return base + endpoint;
        }
        String trimmedContext = trimTrailingSlash(contextPath.trim());
        return endpoint.startsWith(trimmedContext) ? base + endpoint : base + trimmedContext + endpoint;
    }

    private static String trimTrailingSlash(String value) {
        return value.endsWith("/") ? value.substring(0, value.length() - 1) : value;
    }
}
