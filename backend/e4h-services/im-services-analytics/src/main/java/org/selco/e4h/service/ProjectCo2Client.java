package org.selco.e4h.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.egov.common.contract.request.RequestInfo;
import org.selco.e4h.config.CarbonEmissionProperties;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class ProjectCo2Client {

    private final RestTemplate restTemplate;
    private final CarbonEmissionProperties properties;
    private final ObjectMapper objectMapper;

    public Map<String, String> fetchProjectNamesByFacility(RequestInfo requestInfo,
                                                             String tenantId,
                                                             List<String> facilityIds) {
        if (facilityIds == null || facilityIds.isEmpty()) {
            return Map.of();
        }
        String url = properties.getProjectHost() + properties.getProjectFetchByFacilitiesPath();
        Map<String, Object> body = new HashMap<>();
        body.put("RequestInfo", requestInfo);
        body.put("tenantId", tenantId);
        body.put("facilityIds", facilityIds);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        Map<String, String> result = new HashMap<>();
        try {
            String response = restTemplate.postForObject(url, new HttpEntity<>(body, headers), String.class);
            JsonNode root = objectMapper.readTree(response);
            JsonNode rows = root.path("projectsByFacility");
            if (rows.isArray()) {
                for (JsonNode row : rows) {
                    String facilityId = row.path("facilityId").asText(null);
                    String projectName = row.path("projectName").asText(null);
                    if (facilityId != null && projectName != null) {
                        result.put(facilityId, projectName);
                    }
                }
            }
        } catch (Exception e) {
            log.error("fetchProjectsByFacilities failed for tenantId={}", tenantId, e);
        }
        return result;
    }
}
