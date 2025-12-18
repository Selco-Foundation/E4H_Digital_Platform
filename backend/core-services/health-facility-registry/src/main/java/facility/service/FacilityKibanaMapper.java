package facility.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import facility.repository.ServiceRequestRepository;
import facility.web.models.BoundaryInfo;
import facility.web.models.Facility;
import facility.web.models.FacilityKibanaIndex;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.egov.common.contract.request.RequestInfo;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.Base64;

import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Mapper service to transform Facility objects to Kibana index format
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class FacilityKibanaMapper {

    private final ServiceRequestRepository serviceRequestRepository;
    private final RestTemplate restTemplate;
    private final ObjectMapper mapper;

    @Value("${egov.boundary.host}")
    private String boundaryHost;

    @Value("${egov.boundary.relationship.search.path}")
    private String boundaryRelationshipSearchPath;

    @Value("${egov.indexer.es.host.name}")
    private String esHost;

    @Value("${egov.indexer.es.port.no}")
    private String esPort;

    private static final String INDEX_NAME = "health-facility-index-v0001";
    private static final String SEARCH_PATH = "_search";

    // Elasticsearch authentication
    @Value("${egov.indexer.es.username}")
    private String esUsername;

    @Value("${egov.indexer.es.password}")
    private String esPassword;

    /**
     * Transforms a Facility object to the format expected by Kibana indexer
     * 
     * @param facility The facility to transform
     * @param requestInfo RequestInfo for boundary service calls
     * @return FacilityKibanaIndex object ready for indexing
     */
    public FacilityKibanaIndex toKibanaIndex(Facility facility, RequestInfo requestInfo) {
        if (facility == null) {
            return null;
        }

        FacilityKibanaIndex.FacilityKibanaIndexBuilder builder = FacilityKibanaIndex.builder()
                .facilityId(facility.getFacilityId())
                .name(facility.getFacilityName())
                .phcType(facility.getFacilityType())
                .tenantId(facility.getTenantId())
                .tenantIdLocalized(facility.getTenantId())
                .code(facility.getBoundaryCode())
                .type(facility.getFacilityType())
                .isLive(facility.getIsActive())
                .synced(false)
                .totalTickets(0)
                .openTickets(0)
                .closedTickets(0)
                .lastModifiedTime(System.currentTimeMillis());

        // Set geoPoint from address if available
        if (facility.getAddress() != null && 
            facility.getAddress().getLatitude() != null && 
            facility.getAddress().getLongitude() != null) {
            String geoPoint = facility.getAddress().getLatitude() + "," + facility.getAddress().getLongitude();
            builder.geoPoint(geoPoint);
        }

        // Set solar panel status from additionalDetails if available
        if (facility.getAdditionalDetails() != null) {
            Object solarStatus = facility.getAdditionalDetails().get("solarPanelStatus");
            if (solarStatus != null) {
                builder.solarPanelStatus(solarStatus.toString());
            }
        }

        // Set vendor information from additionalDetails if available
        if (facility.getAdditionalDetails() != null) {
            Object vendorUserName = facility.getAdditionalDetails().get("mappedVendorUserName");
            Object vendorName = facility.getAdditionalDetails().get("mappedVendorName");
            if (vendorUserName != null) {
                builder.mappedVendorUserName(vendorUserName.toString());
            }
            if (vendorName != null) {
                builder.mappedVendorName(vendorName.toString());
            }
        }

        // Fetch boundary hierarchy and extract codes
        BoundaryCodes boundaryCodes = fetchBoundaryHierarchy(facility, requestInfo);
        
        // Set top-level fields from boundary hierarchy
        if (boundaryCodes != null) {
            builder.block(boundaryCodes.getBlockCode())
                   .district(boundaryCodes.getDistrictCode())
                   .state(boundaryCodes.getStateCode());
        }
        
        // Build boundary info from fetched hierarchy
        BoundaryInfo boundaryInfo = buildBoundaryInfo(facility, boundaryCodes);
        builder.boundary(boundaryInfo);

        return builder.build();
    }

    /**
     * Fetches boundary hierarchy from boundary service and extracts codes by boundary type
     */
    private BoundaryCodes fetchBoundaryHierarchy(Facility facility, RequestInfo requestInfo) {
        if (facility.getBoundaryCode() == null || facility.getTenantId() == null) {
            log.warn("Cannot fetch boundary hierarchy: boundaryCode or tenantId is null");
            return null;
        }

        try {
            // Boundary service expects a standard RequestInfo wrapper as body
            Map<String, Object> requestBody =
                    requestInfo != null ? Map.of("RequestInfo", requestInfo) : Map.of();

            // Build URI with query parameters
            String uri = UriComponentsBuilder.fromUriString(boundaryHost)
                    .path(boundaryRelationshipSearchPath)
                    .queryParam("tenantId", facility.getTenantId())
                    .queryParam("includeParents", true)
                    .queryParam("includeChildren", false)
                    .queryParam("codes", facility.getBoundaryCode())
                    .toUriString();

            // Call boundary service
            Object rawResponse = serviceRequestRepository.fetchResult(new StringBuilder(uri), requestBody);
            Map<String, Object> response = mapper.convertValue(rawResponse, new TypeReference<Map<String, Object>>() {});

            // Parse response and extract codes
            return parseBoundaryHierarchy(response);

        } catch (Exception e) {
            log.error("Error fetching boundary hierarchy for facility {}: {}", 
                     facility.getFacilityId(), e.getMessage(), e);
            return null;
        }
    }

    /**
     * Parses the boundary hierarchy response to extract codes by boundary type
     */
    @SuppressWarnings("unchecked")
    private BoundaryCodes parseBoundaryHierarchy(Map<String, Object> response) {
        BoundaryCodes codes = new BoundaryCodes();

        try {
            Object tenantBoundaryObj = response.get("TenantBoundary");
            if (!(tenantBoundaryObj instanceof List) || ((List<?>) tenantBoundaryObj).isEmpty()) {
                return codes;
            }

            List<Map<String, Object>> tenantBoundaryList = (List<Map<String, Object>>) tenantBoundaryObj;
            Map<String, Object> tenantBoundary = tenantBoundaryList.get(0);
            
            Object boundaryObj = tenantBoundary.get("boundary");
            if (!(boundaryObj instanceof List) || ((List<?>) boundaryObj).isEmpty()) {
                return codes;
            }

            List<Map<String, Object>> boundaryList = (List<Map<String, Object>>) boundaryObj;
            
            // Traverse the nested hierarchy to extract codes
            for (Map<String, Object> boundary : boundaryList) {
                extractCodesFromHierarchy(boundary, codes);
            }

        } catch (Exception e) {
            log.error("Error parsing boundary hierarchy response: {}", e.getMessage(), e);
        }

        return codes;
    }

    /**
     * Recursively traverses boundary hierarchy to extract codes by boundary type
     */
    @SuppressWarnings("unchecked")
    private void extractCodesFromHierarchy(Map<String, Object> boundaryNode, BoundaryCodes codes) {
        if (boundaryNode == null) {
            return;
        }

        String code = (String) boundaryNode.get("code");
        String boundaryType = (String) boundaryNode.get("boundaryType");

        // Extract code based on boundary type
        if (code != null && boundaryType != null) {
            switch (boundaryType) {
                case "Country":
                    codes.setCountryCode(code);
                    break;
                case "State":
                    codes.setStateCode(code);
                    break;
                case "District":
                    codes.setDistrictCode(code);
                    break;
                case "Block":
                    codes.setBlockCode(code);
                    break;
                case "Facility":
                    codes.setFacilityCode(code);
                    break;
            }
        }

        // Recursively process children
        Object childrenObj = boundaryNode.get("children");
        if (childrenObj instanceof List) {
            List<Map<String, Object>> children = (List<Map<String, Object>>) childrenObj;
            for (Map<String, Object> child : children) {
                extractCodesFromHierarchy(child, codes);
            }
        }
    }

    /**
     * Builds BoundaryInfo object from extracted codes
     */
    private BoundaryInfo buildBoundaryInfo(Facility facility, BoundaryCodes codes) {
        BoundaryInfo.BoundaryInfoBuilder builder =
            BoundaryInfo.builder()
                .facilityCode(codes != null && codes.getFacilityCode() != null 
                    ? codes.getFacilityCode() : facility.getBoundaryCode());

        if (codes != null) {
            builder.blockCode(codes.getBlockCode())
                   .districtCode(codes.getDistrictCode())
                   .stateCode(codes.getStateCode())
                   .countryCode(codes.getCountryCode());
        }

        return builder.build();
    }

    /**
     * Helper class to hold extracted boundary codes
     */
    private static class BoundaryCodes {
        private String countryCode;
        private String stateCode;
        private String districtCode;
        private String blockCode;
        private String facilityCode;

        public String getCountryCode() { return countryCode; }
        public void setCountryCode(String countryCode) { this.countryCode = countryCode; }
        public String getStateCode() { return stateCode; }
        public void setStateCode(String stateCode) { this.stateCode = stateCode; }
        public String getDistrictCode() { return districtCode; }
        public void setDistrictCode(String districtCode) { this.districtCode = districtCode; }
        public String getBlockCode() { return blockCode; }
        public void setBlockCode(String blockCode) { this.blockCode = blockCode; }
        public String getFacilityCode() { return facilityCode; }
        public void setFacilityCode(String facilityCode) { this.facilityCode = facilityCode; }
    }

    /**
     * Checks if a facility with the given facilityId exists in Kibana index
     * 
     * @param facilityId The facility ID to check
     * @param tenantId The tenant ID
     * @param requestInfo RequestInfo for API calls
     * @return true if facility exists in Kibana, false otherwise
     */
    public boolean existsInKibana(String facilityId, String tenantId, RequestInfo requestInfo) {
        if (facilityId == null || tenantId == null) {
            return false;
        }

        try {
            // Build Elasticsearch search query
            Map<String, Object> searchQuery = Map.of(
                "query", Map.of(
                    "bool", Map.of(
                        "must", List.of(
                            Map.of("term", Map.of("Data.facilityId.keyword", facilityId)),
                            Map.of("term", Map.of("Data.tenantId.keyword", tenantId))
                        )
                    )
                ),
                "size", 0
            );

            // Build URI: {host}:{port}/{INDEX_NAME}/{SEARCH_PATH}
            // Example: https://localhost:9200/health-facility-index-v0001/_search
            String uri = getBaseUrl() + "/" + INDEX_NAME + "/" + SEARCH_PATH;

            // Build headers with authentication
            HttpEntity<Object> entity = new HttpEntity<>(searchQuery, buildHeaders());

            log.info("Executing Elasticsearch query to check facility existence: {}", searchQuery);
            @SuppressWarnings("unchecked")
            Map<String, Object> response = restTemplate.postForObject(uri, entity, Map.class);

            // Parse response to check if document exists
            return parseSearchResponse(response);

        } catch (Exception e) {
            log.warn("Error checking if facility {} exists in Kibana: {}. Assuming not present.", 
                    facilityId, e.getMessage(), e);
            // If check fails, return false to allow push (fail open approach)
            return false;
        }
    }

    /**
     * Builds the base URL for Elasticsearch API
     * Direct Elasticsearch access: {host}:{port}
     */
    private String getBaseUrl() {
        String host = esHost.endsWith("/") ? esHost.substring(0, esHost.length() - 1) : esHost;
        String port = esPort.startsWith(":") ? esPort : ":" + esPort;
        return host + port;
    }

    /**
     * Builds HTTP headers with Elasticsearch authentication
     */
    private HttpHeaders buildHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));

        // Add Basic Authentication if credentials are provided
        if (esUsername != null && !esUsername.isEmpty() && esPassword != null && !esPassword.isEmpty()) {
            String auth = esUsername + ":" + esPassword;
            byte[] encodedAuth = Base64.getEncoder().encode(auth.getBytes(StandardCharsets.UTF_8));
            String authHeader = "Basic " + new String(encodedAuth);
            headers.set("Authorization", authHeader);
        }

        return headers;
    }

    /**
     * Parses the search response to determine if any documents were found
     */
    @SuppressWarnings("unchecked")
    private boolean parseSearchResponse(Map<String, Object> response) {
        try {
            // Check for hits in response
            Object hitsObj = response.get("hits");
            if (hitsObj instanceof Map) {
                Map<String, Object> hits = (Map<String, Object>) hitsObj;
                Object totalObj = hits.get("total");
                
                if (totalObj instanceof Map) {
                    // ES 7.x+ format: {"total": {"value": 1}}
                    Map<String, Object> totalMap = (Map<String, Object>) totalObj;
                    Object value = totalMap.get("value");
                    if (value instanceof Number) {
                        return ((Number) value).intValue() > 0;
                    }
                } else if (totalObj instanceof Number) {
                    // ES 6.x format: {"total": 1}
                    return ((Number) totalObj).intValue() > 0;
                }
            }
            
            return false;
        } catch (Exception e) {
            log.error("Error parsing search response: {}", e.getMessage(), e);
            return false;
        }
    }
}

