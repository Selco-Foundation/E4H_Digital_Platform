package org.selco.e4h.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.selco.e4h.config.CarbonEmissionProperties;
import org.selco.e4h.web.models.Co2ReferenceBundle;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class Co2ReferenceClient {

    private final RestTemplate restTemplate;
    private final CarbonEmissionProperties properties;

    public Co2ReferenceBundle fetchReferenceData(String tenantId) {
        String url = properties.getRmsHost() + properties.getRmsCo2ReferencePath()
                + "?tenantId=" + tenantId;
        try {
            @SuppressWarnings("unchecked")
            Map<String, Object> body = restTemplate.getForObject(url, Map.class);
            if (body == null) {
                return Co2ReferenceBundle.builder().build();
            }
            return Co2ReferenceBundle.fromRmsResponse(body);
        } catch (Exception e) {
            log.error("Failed to load CO2 reference data from rms-service for tenantId={}", tenantId, e);
            return Co2ReferenceBundle.builder().build();
        }
    }
}
