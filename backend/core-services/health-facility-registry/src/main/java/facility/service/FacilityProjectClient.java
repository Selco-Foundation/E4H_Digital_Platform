package facility.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import facility.config.Configuration;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.egov.common.contract.request.RequestInfo;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Resolves the project name mapped to a facility via the project service
 * ({@code POST /project/v1/fetchProjectsByFacilities}). Used to enrich the
 * health facility index document with {@code projectName} so it is present on
 * every index push and never dropped on a full-document re-index.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class FacilityProjectClient {

    private final RestTemplate restTemplate;
    private final ObjectMapper mapper;
    private final Configuration configs;

    /**
     * Returns a {@code facilityId -> projectName} map for the given facilities. Never throws;
     * returns an empty map when the project host is not configured or the call fails.
     */
    public Map<String, String> fetchProjectNamesByFacility(RequestInfo requestInfo, String tenantId, List<String> facilityIds) {
        Map<String, String> result = new HashMap<>();
        if (facilityIds == null || facilityIds.isEmpty()) {
            return result;
        }
        String host = configs.getProjectHost();
        String path = configs.getProjectFetchByFacilitiesPath();
        if (host == null || host.isBlank() || path == null || path.isBlank()) {
            log.warn("Project host/path not configured; skipping projectName lookup for facilities={}", facilityIds);
            return result;
        }

        String url = host + path;
        Map<String, Object> body = new HashMap<>();
        body.put("RequestInfo", requestInfo);
        body.put("tenantId", tenantId);
        body.put("facilityIds", facilityIds);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        try {
            String response = restTemplate.postForObject(url, new HttpEntity<>(body, headers), String.class);
            JsonNode root = mapper.readTree(response);
            JsonNode rows = root.path("projectsByFacility");
            if (rows.isArray()) {
                for (JsonNode row : rows) {
                    String facilityId = row.path("facilityId").asText(null);
                    String projectName = row.path("projectName").asText(null);
                    if (facilityId == null || projectName == null || result.containsKey(facilityId)) {
                        continue;
                    }
                    result.put(facilityId, projectName);
                }
            }
        } catch (Exception e) {
            log.error("fetchProjectsByFacilities failed for tenantId={} facilities={}: {}",
                    tenantId, facilityIds, e.getMessage());
        }
        return result;
    }

    /**
     * Resolves a single facility's project name, or {@code null} when unavailable.
     */
    public String fetchProjectName(RequestInfo requestInfo, String tenantId, String facilityId) {
        if (facilityId == null || facilityId.isBlank()) {
            return null;
        }
        return fetchProjectNamesByFacility(requestInfo, tenantId, Collections.singletonList(facilityId)).get(facilityId);
    }
}
