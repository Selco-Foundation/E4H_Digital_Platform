package org.egov.im.service;

import lombok.extern.slf4j.Slf4j;
import org.egov.common.contract.request.RequestInfo;
import org.egov.im.config.IMConfiguration;
import org.egov.im.web.models.Boundary;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class BoundaryService {

    @Autowired
    private IMConfiguration config;

    @Autowired
    private RestTemplate restTemplate;

    /**
     * Enriches boundary object from boundary service using boundaryCode and tenantId
     * @param requestInfo RequestInfo of the logged-in user
     * @param boundaryCode The facility boundaryCode
     * @param tenantId The facility boundary's tenant id
     */
    public Boundary fetchBoundaryFromBoundaryCode(RequestInfo requestInfo, String boundaryCode, String tenantId) {
        log.trace("BoundaryService::fetchBoundaryFromBoundaryCode method invoked");
        if (boundaryCode == null || boundaryCode.isEmpty()) {
            log.debug("No boundaryCode provided in incident request, skipping boundary enrichment");
            return null;
        }

        log.debug("Fetching boundary for boundaryCode: {}, tenantId: {}", boundaryCode, tenantId);
        try {
            String url = buildBoundarySearchUrl(boundaryCode, tenantId);
            HttpEntity<Map<String, Object>> requestEntity = buildBoundarySearchRequest(requestInfo);

            ResponseEntity<Map<String, Object>> responseEntity = restTemplate.exchange(
                    url,
                    HttpMethod.POST,
                    requestEntity,
                    new ParameterizedTypeReference<Map<String, Object>>() {}
            );

            return extractBoundaryFromResponse(boundaryCode, responseEntity.getBody());
        } catch (Exception e) {
            log.error("Error enriching boundary details for boundaryCode: {}", boundaryCode, e);
        }

        return null;
    }

    /**
     * Builds boundary hierarchy from boundary service response
     * @param boundaries List of boundary objects from boundary service
     * @return Boundary object with hierarchy codes
     */
    private String buildBoundarySearchUrl(String boundaryCode, String tenantId) {
        return UriComponentsBuilder.fromHttpUrl(config.getBoundaryHost() + config.getBoundarySearchPath())
                .queryParam("tenantId", tenantId != null ? tenantId.split("\\.")[0] : "")
                .queryParam("codes", boundaryCode)
                .queryParam("includeParents", "true")
                .queryParam("boundaryType", "Facility")
                .toUriString();
    }

    private HttpEntity<Map<String, Object>> buildBoundarySearchRequest(RequestInfo requestInfo) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("RequestInfo", requestInfo);

        return new HttpEntity<>(requestBody, headers);
    }

    private Boundary extractBoundaryFromResponse(String boundaryCode, Map<String, Object> responseMap) {
        if (responseMap == null) {
            return null;
        }

        List<Map<String, Object>> boundaryRelationships =
                (List<Map<String, Object>>) responseMap.get("TenantBoundary");

        if (boundaryRelationships == null || boundaryRelationships.isEmpty()) {
            log.warn("No boundary relationships found for boundaryCode: {}", boundaryCode);
            return null;
        }

        Map<String, Object> tenantBoundary = boundaryRelationships.get(0);
        List<Map<String, Object>> boundaries = (List<Map<String, Object>>) tenantBoundary.get("boundary");

        if (boundaries == null || boundaries.isEmpty()) {
            log.warn("No boundaries found in response for boundaryCode: {}", boundaryCode);
            return null;
        }

        Boundary boundary = buildBoundaryHierarchy(boundaries);

        if (boundary != null) {
            log.debug("Enriched boundary for indexing for boundaryCode: {}", boundaryCode);
        }

        return boundary;
    }

    private Boundary buildBoundaryHierarchy(List<Map<String, Object>> boundaries) {
        log.trace("BoundaryService::buildBoundaryHierarchy method invoked");
        Boundary boundary = new Boundary();

        for (Map<String, Object> boundaryItem : boundaries) {
            String code = (String) boundaryItem.get("code");
            String boundaryType = (String) boundaryItem.get("boundaryType");

            if (code != null && boundaryType != null) {
                switch (boundaryType.toLowerCase()) {
                    case "country":
                        boundary.setCountryCode(code);
                        break;
                    case "state":
                        boundary.setStateCode(code);
                        break;
                    case "district":
                        boundary.setDistrictCode(code);
                        break;
                    case "block":
                        boundary.setBlockCode(code);
                        break;
                    case "facility":
                        boundary.setFacilityCode(code);
                        break;
                    default:
                        log.debug("Unknown boundaryType: {}", boundaryType);
                }
            }

            // Recursively process children if present
            List<Map<String, Object>> children = (List<Map<String, Object>>) boundaryItem.get("children");
            if (children != null && !children.isEmpty()) {
                Boundary childBoundary = buildBoundaryHierarchy(children);
                // Merge child boundary codes into parent
                if (childBoundary.getCountryCode() != null) boundary.setCountryCode(childBoundary.getCountryCode());
                if (childBoundary.getStateCode() != null) boundary.setStateCode(childBoundary.getStateCode());
                if (childBoundary.getDistrictCode() != null) boundary.setDistrictCode(childBoundary.getDistrictCode());
                if (childBoundary.getBlockCode() != null) boundary.setBlockCode(childBoundary.getBlockCode());
                if (childBoundary.getFacilityCode() != null) boundary.setFacilityCode(childBoundary.getFacilityCode());
            }
        }

        return boundary;
    }

}
