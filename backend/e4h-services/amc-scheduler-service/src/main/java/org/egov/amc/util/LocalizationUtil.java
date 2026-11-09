package org.egov.amc.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.egov.amc.web.models.LocalizationResponse;
import org.egov.common.contract.request.RequestInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Resolves boundary (state/district/block) display names via egov-localization,
 * following the same "Boundary_" code prefix and rainmaker-in module convention
 * used elsewhere in this platform (see Co2LocalizationClient in im-services-analytics).
 */
@Slf4j
@Component
public class LocalizationUtil {

    private static final String BOUNDARY_CODE_PREFIX = "Boundary_";

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    @Value("${egov.localization.host}")
    private String localizationHost;

    @Value("${egov.localization.context.path}")
    private String localizationContextPath;

    @Value("${egov.localization.search.endpoint}")
    private String localizationSearchEndpoint;

    @Value("${amc.localization.boundary.module:rainmaker-in}")
    private String localizationBoundaryModule;

    @Value("${amc.localization.locale:en_IN}")
    private String localizationLocale;

    /**
     * Fetches localized display names for boundary codes (state/district/block) from egov-localization.
     * Returns a map keyed by the RAW boundary code (without the "Boundary_" prefix) to its localized label,
     * so callers can look it up using the same code stored on the Boundary model.
     */
    public Map<String, String> fetchBoundaryLabels(RequestInfo requestInfo, String tenantId, Set<String> boundaryCodes) {
        Map<String, String> labels = new HashMap<>();
        if (boundaryCodes == null || boundaryCodes.isEmpty()) {
            return labels;
        }

        List<String> localizationCodes = boundaryCodes.stream()
                .map(code -> code.startsWith(BOUNDARY_CODE_PREFIX) ? code : BOUNDARY_CODE_PREFIX + code)
                .distinct()
                .toList();

        String uri = localizationHost + localizationContextPath + localizationSearchEndpoint
                + "?tenantId=" + tenantId
                + "&module=" + localizationBoundaryModule
                + "&locale=" + localizationLocale;

        Map<String, Object> body = new HashMap<>();
        body.put("RequestInfo", requestInfo);
        body.put("codes", localizationCodes);

        try {
            Object response = restTemplate.postForObject(uri, body, Object.class);
            if (response == null) {
                return labels;
            }
            LocalizationResponse localizationResponse = objectMapper.convertValue(response, LocalizationResponse.class);
            if (localizationResponse.getMessages() == null) {
                return labels;
            }
            for (LocalizationResponse.Message message : localizationResponse.getMessages()) {
                if (message.getCode() != null && message.getMessage() != null && !message.getMessage().isBlank()) {
                    labels.put(message.getCode().replaceFirst("^" + BOUNDARY_CODE_PREFIX, ""), message.getMessage());
                }
            }
        } catch (Exception e) {
            log.error("Error fetching boundary localization labels for tenantId={}: {}", tenantId, e.getMessage());
        }
        return labels;
    }
}
