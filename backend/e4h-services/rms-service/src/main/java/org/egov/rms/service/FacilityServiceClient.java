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
        return searchByIdentifier("hfrId", hfrId, tenantId);
    }

    /**
     * Fetches facility details by ninId. RMS reports a single identifier in its HFRID field, and
     * for some facilities that value is a NIN, so callers may need to retry an unmatched
     * identifier as a NIN before concluding the facility is unknown.
     */
    public FacilityDetails getFacilityByNinId(String ninId, String tenantId) {
        // Validate ninId
        if (ninId == null || ninId.trim().isEmpty()) {
            log.warn("NIN id is provided as null");
            return null;
        }
        return searchByIdentifier("ninId", ninId, tenantId);
    }

    private FacilityDetails searchByIdentifier(String paramName, String identifier, String tenantId) {
        try {
            String url = config.getFacilityServiceBaseUrl() + config.getFacilityServiceSearchEndpoint();

            UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(url)
                    .queryParam("tenant_id", "in")
                    .queryParam(paramName, identifier)
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

                    if (facility.containsKey("facility_status")) {
                        String status = String.valueOf(facility.get("facility_status"));

                        if ("UNINSTALLED".equalsIgnoreCase(status)) {
                            log.info("Facility {}={} is UNINSTALLED. Skipping ticket creation.", paramName, identifier);
                            return null;
                        }
                    }

                    // Skip facilities that are marked as RMS inactive (open RMS/Theft incidents already present)
                    Object rmsInactiveObj = facility.get("rms_inactive");
                    boolean rmsInactive = false;
                    if (rmsInactiveObj instanceof Boolean) {
                        rmsInactive = (Boolean) rmsInactiveObj;
                    } else if (rmsInactiveObj != null) {
                        rmsInactive = Boolean.parseBoolean(String.valueOf(rmsInactiveObj));
                    }
                    if (rmsInactive) {
                        log.info("Facility {}={} is marked as rms_inactive=true. Skipping ticket creation.", paramName, identifier);
                        return null;
                    }

                    FacilityDetails details = mapToFacilityDetails(facility);
                    if (details != null) {
                        log.debug("Successfully fetched facility for {}: {} - facilityId: {}, boundaryCode: {}",
                                paramName, identifier, details.getFacilityId(), details.getBoundaryCode());
                    }
                    return details;
                } else {
                    log.warn("No facilities found in response for {}: {}", paramName, identifier);
                }
            } else {
                log.warn("Facility service returned non-2xx status or null body for {}: {} - Status: {}",
                        paramName, identifier, response != null ? response.getStatusCode() : "null response");
            }

            return null;
        } catch (Exception e) {
            log.error("Error fetching facility by {}: {}", paramName, identifier, e);
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
                    .rmsInactive(facility.get("rms_inactive") instanceof Boolean
                            ? (Boolean) facility.get("rms_inactive")
                            : facility.get("rms_inactive") != null
                                ? Boolean.parseBoolean(String.valueOf(facility.get("rms_inactive")))
                                : null)
                    .build();
        } catch (Exception e) {
            log.error("Error mapping facility details", e);
            return null;
        }
    }
}

