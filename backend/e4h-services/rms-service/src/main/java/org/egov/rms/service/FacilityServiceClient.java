package org.egov.rms.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.egov.rms.config.RMSConfiguration;
import org.egov.rms.model.FacilityDetails;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class FacilityServiceClient {

    private final RMSConfiguration config;
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    /**
     * Fetches facility details by hfrId
     */
    public FacilityDetails getFacilityByHfrId(String hfrId, String tenantId) {
        // Validate hfrId
        if (hfrId == null || hfrId.trim().isEmpty()) {
            log.warn("HFR id is provided as null");
            return null;
        }
        
        try {
            String url = config.getFacilityServiceBaseUrl() + config.getFacilityServiceSearchEndpoint();
            
            UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(url)
                    .queryParam("tenant_id", "in")
                    .queryParam("hfrId", hfrId)
                    .queryParam("limit", 1)
                    .queryParam("offset", 0);

            HttpHeaders headers = new HttpHeaders();
            headers.set("Accept", "application/json");
            HttpEntity<?> entity = new HttpEntity<>(headers);

            ResponseEntity<Map> response = restTemplate.exchange(
                    builder.toUriString(), HttpMethod.GET, entity, Map.class);

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                Map<String, Object> body = response.getBody();
                List<Map<String, Object>> facilities = (List<Map<String, Object>>) body.get("facilities");
                
                if (facilities != null && !facilities.isEmpty()) {
                    Map<String, Object> facility = facilities.get(0);
                    FacilityDetails details = mapToFacilityDetails(facility);
                    if (details != null) {
                        log.debug("Successfully fetched facility for hfrId: {} - facilityId: {}, boundaryCode: {}", 
                                hfrId, details.getFacilityId(), details.getBoundaryCode());
                    }
                    return details;
                } else {
                    log.warn("No facilities found in response for hfrId: {}", hfrId);
                }
            } else {
                log.warn("Facility service returned non-2xx status or null body for hfrId: {} - Status: {}", 
                        hfrId, response != null ? response.getStatusCode() : "null response");
            }

            return null;
        } catch (Exception e) {
            log.error("Error fetching facility by hfrId: {}", hfrId, e);
            return null;
        }
    }

    /**
     * Maps facility map to FacilityDetails object
     */
    private FacilityDetails mapToFacilityDetails(Map<String, Object> facility) {
        try {
            Map<String, Object> details = (Map<String, Object>) facility.get("facility_details");
            Map<String, Object> address = (Map<String, Object>) facility.get("address");
            
            return FacilityDetails.builder()
                    .facilityId((String) facility.get("facility_id"))
                    .facilityName((String) facility.get("facility_name"))
                    .hfrId(details != null ? (String) details.get("hfr_id") : null)
                    .ninId(details != null ? (String) details.get("nin_id") : null)
                    .district((String) address.get("district"))
                    .block((String) address.get("block"))
//                    .phcType((String) facility.get("phcType"))
//                    .phcSubType((String) facility.get("phcSubType"))
                    .tenantId((String) facility.get("tenant_id"))
                    .boundaryCode((String) facility.get("boundaryCode"))
                    .build();
        } catch (Exception e) {
            log.error("Error mapping facility details", e);
            return null;
        }
    }
}

