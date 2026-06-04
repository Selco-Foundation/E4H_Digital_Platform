package facility.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import facility.repository.ServiceRequestRepository;
import facility.web.models.BoundaryInfo;
import facility.web.models.Facility;
import facility.util.FacilityMappedVendorHelper;
import facility.web.models.FacilityKibanaIndex;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.egov.common.contract.request.RequestInfo;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.*;

import java.nio.charset.StandardCharsets;
import java.util.stream.Collectors;

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
        log.trace("Entering toKibanaIndex method");
        if (facility == null) {
            log.warn("Facility is null, cannot convert to Kibana index");
            return null;
        }
        log.info("Converting facility {} to Kibana index format", facility.getFacilityId());

        FacilityKibanaIndex.FacilityKibanaIndexBuilder builder = FacilityKibanaIndex.builder()
                .facilityId(facility.getFacilityId())
                .name(facility.getFacilityName())
                // Match existing index docs: null when unknown (not an empty object)
                .phcName(null)
                .phcType(facility.getFacilityType())
                .tenantId(facility.getTenantId())
                // Used downstream as the health facility display name (see im-services-analytics)
                .tenantIdLocalized(resolveTenantIdLocalized(facility))
                .code(resolveFacilityCodeForIndex(facility))
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

        // Keep index behavior deterministic: default to FUNCTIONAL unless explicitly provided.
        String solarPanelStatus = "FUNCTIONAL";
        if (facility.getAdditionalDetails() != null) {
            Object solarStatus = facility.getAdditionalDetails().get("solarPanelStatus");
            if (solarStatus != null && !solarStatus.toString().isBlank()) {
                solarPanelStatus = solarStatus.toString();
            }
        }
        builder.solarPanelStatus(solarPanelStatus);

        applyMappedVendorFields(facility, builder);

        // Fetch boundary hierarchy and extract codes
        BoundaryCodes boundaryCodes = fetchBoundaryHierarchy(facility, requestInfo);
        
        // Set top-level fields from boundary hierarchy
        String blockCode = null;
        String districtCode = null;
        String stateCode = null;
        String countryCode = null;
        if (boundaryCodes != null) {
            blockCode = boundaryCodes.getBlockCode();
            districtCode = boundaryCodes.getDistrictCode();
            stateCode = boundaryCodes.getStateCode();
            countryCode = boundaryCodes.getCountryCode();
            // Top-level state/district/block are human-readable labels (not full hierarchy codes)
            builder.block(boundaryHierarchyCodeToDisplayLabel(blockCode))
                   .district(boundaryHierarchyCodeToDisplayLabel(districtCode))
                   .state(boundaryHierarchyCodeToDisplayLabel(stateCode));
        }
        
        // Build boundary info from fetched hierarchy (use extracted values)
        BoundaryInfo boundaryInfo = buildBoundaryInfo(facility, boundaryCodes, blockCode, districtCode, stateCode, countryCode);
        builder.boundary(boundaryInfo);
        
        // Log boundary object status (avoid logging full objects in INFO)
        if (boundaryInfo != null) {
            log.debug("Boundary object built for facility {}: facilityCode={}, blockCode={}, districtCode={}, stateCode={}, countryCode={}",
                    sanitizeForLog(facility.getFacilityId()), sanitizeForLog(boundaryInfo.getFacilityCode()), sanitizeForLog(boundaryInfo.getBlockCode()),
                    sanitizeForLog(boundaryInfo.getDistrictCode()), sanitizeForLog(boundaryInfo.getStateCode()), sanitizeForLog(boundaryInfo.getCountryCode()));
        } else {
            log.warn("Boundary object is null for facility {}", sanitizeForLog(facility.getFacilityId()));
        }

        FacilityKibanaIndex result = builder.build();
        // Only log full boundary object in DEBUG level
        if (result.getBoundary() != null) {
            if (log.isDebugEnabled()) {
                log.debug("Boundary in FacilityKibanaIndex for facility {}: {}", sanitizeForLog(facility.getFacilityId()), result.getBoundary());
            }
        } else {
            log.warn("Boundary is null in FacilityKibanaIndex for facility {}", sanitizeForLog(facility.getFacilityId()));
        }
        log.info("Successfully converted facility {} to Kibana index format", facility.getFacilityId());
        log.trace("Exiting toKibanaIndex method");
        return result;
    }

    /**
     * Updates only mutable "display" fields for an existing Kibana index document.
     * Falls back to full mapping when no existing document can be found.
     */
    public FacilityKibanaIndex toKibanaIndexForFacilityUpdate(Facility facility, RequestInfo requestInfo) {
        if (facility == null) {
            log.info("Skipping Kibana index update mapping: facility is null");
            return null;
        }

        log.info("Preparing Kibana index update mapping for facilityId={} tenantId={}",
                facility.getFacilityId(), facility.getTenantId());
        FacilityKibanaIndex existingDoc = fetchExistingKibanaIndex(facility.getFacilityId(), facility.getTenantId());
        log.info("Existing Kibana document found for facilityId={} existingDoc={}",
                facility.getFacilityId(), existingDoc);
        if (existingDoc == null) {
            log.info("No existing Kibana document found for facilityId={} tenantId={}; falling back to full mapping",
                    facility.getFacilityId(), facility.getTenantId());
            return toKibanaIndex(facility, requestInfo);
        }

        if (facility.getFacilityName() != null && !facility.getFacilityName().isBlank()) {
            existingDoc.setName(facility.getFacilityName());
            existingDoc.setTenantIdLocalized(facility.getFacilityName());
            log.info("Updated Kibana field name for facilityId={}", facility.getFacilityId());
        }
        if (facility.getFacilityType() != null && !facility.getFacilityType().isBlank()) {
            existingDoc.setType(facility.getFacilityType());
            existingDoc.setPhcType(facility.getFacilityType());
            log.info("Updated Kibana fields type/phcType for facilityId={}", facility.getFacilityId());
        }
        if (facility.getIsActive() != null) {
            existingDoc.setIsLive(facility.getIsActive());
            log.info("Updated Kibana field isLive={} for facilityId={}",
                    facility.getIsActive(), facility.getFacilityId());
        }
        if (StringUtils.isNotBlank(facility.getMappedVendorName())) {
            existingDoc.setMappedVendorName(facility.getMappedVendorName());
            log.info("Updated Kibana field mappedVendorName for facilityId={}", facility.getFacilityId());
        }
        if (StringUtils.isNotBlank(facility.getMappedVendorUserName())) {
            existingDoc.setMappedVendorUserName(facility.getMappedVendorUserName());
            log.info("Updated Kibana field mappedVendorUserName for facilityId={}", facility.getFacilityId());
        }
        existingDoc.setLastModifiedTime(System.currentTimeMillis());
        log.info("Completed Kibana index update mapping for facilityId={} tenantId={}",
                facility.getFacilityId(), facility.getTenantId());

        return existingDoc;
    }

    /**
     * Prefer HFR / official facility code for the index {@code code} field; fall back to boundary code.
     */
    private static String resolveTenantIdLocalized(Facility facility) {
        String name = facility.getFacilityName();
        if (name != null && !name.isBlank()) {
            return name;
        }
        return facility.getTenantId();
    }

    private static String resolveFacilityCodeForIndex(Facility facility) {
        String hfr = facility.getHfrId();
        if (hfr != null && !hfr.isBlank()) {
            return hfr;
        }
        return facility.getBoundaryCode();
    }

    /**
     * The boundary relationship API returns hierarchical codes (e.g. {@code India_Nagaland_Zunheboto}).
     * Indexed documents expect the display fragment (e.g. Nagaland, Zunheboto) like legacy rows.
     */
    private static String boundaryHierarchyCodeToDisplayLabel(String boundaryCode) {
        if (boundaryCode == null || boundaryCode.isBlank()) {
            return null;
        }
        int i = boundaryCode.lastIndexOf('_');
        if (i < 0) {
            return boundaryCode;
        }
        return boundaryCode.substring(i + 1);
    }

    @SuppressWarnings("unchecked")
    private void applyMappedVendorFields(Facility facility, FacilityKibanaIndex.FacilityKibanaIndexBuilder builder) {
        FacilityMappedVendorHelper.hydrateFromAdditionalDetails(facility);
        String user = firstNonBlankString(facility.getMappedVendorUserName());
        String name = firstNonBlankString(facility.getMappedVendorName());
        Map<String, Object> ad = facility.getAdditionalDetails();
        if (ad != null && !ad.isEmpty()) {
            if (user == null) {
                user = firstNonBlankString(
                        ad.get(FacilityMappedVendorHelper.MAPPED_VENDOR_USER_NAME_KEY),
                        ad.get("mapped_vendor_user_name"),
                        ad.get("mappedVendorUsername"));
            }
            if (name == null) {
                name = firstNonBlankString(
                        ad.get(FacilityMappedVendorHelper.MAPPED_VENDOR_NAME_KEY),
                        ad.get("mapped_vendor_name"));
            }
            if (user == null || name == null) {
                Object nested = ad.get("vendor");
                if (nested instanceof Map) {
                    Map<String, Object> v = (Map<String, Object>) nested;
                    if (user == null) {
                        user = firstNonBlankString(v.get("userName"), v.get("mappedVendorUserName"), v.get("username"));
                    }
                    if (name == null) {
                        name = firstNonBlankString(v.get("name"), v.get("vendorName"));
                    }
                }
            }
        }
        if (user == null && name == null) {
            return;
        }
        if (user != null) {
            builder.mappedVendorUserName(user);
        }
        if (name != null) {
            builder.mappedVendorName(name);
        }
    }

    private static String firstNonBlankString(Object... values) {
        if (values == null) {
            return null;
        }
        for (Object o : values) {
            if (o == null) {
                continue;
            }
            String s = o.toString();
            if (!s.isBlank()) {
                return s;
            }
        }
        return null;
    }

    /**
     * Fetches boundary hierarchy from boundary service and extracts codes by boundary type
     */
    private BoundaryCodes fetchBoundaryHierarchy(Facility facility, RequestInfo requestInfo) {
        log.trace("Entering fetchBoundaryHierarchy method");
        if (facility.getBoundaryCode() == null || facility.getTenantId() == null) {
            log.warn("Cannot fetch boundary hierarchy: boundaryCode or tenantId is null for facility {}", facility.getFacilityId());
            return null;
        }
        log.debug("Fetching boundary hierarchy for facility {} with boundaryCode: {}", facility.getFacilityId(), facility.getBoundaryCode());

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
            BoundaryCodes codes = parseBoundaryHierarchy(response);
            log.debug("Successfully fetched boundary hierarchy for facility {}", facility.getFacilityId());
            log.trace("Exiting fetchBoundaryHierarchy method");
            return codes;

        } catch (Exception e) {
            log.error("Error fetching boundary hierarchy for facility {}: {}", 
                     sanitizeForLog(facility.getFacilityId()), e.getMessage(), e);
            return null;
        }
    }

    /**
     * Parses the boundary hierarchy response to extract codes by boundary type
     */
    @SuppressWarnings("unchecked")
    private BoundaryCodes parseBoundaryHierarchy(Map<String, Object> response) {
        log.trace("Entering parseBoundaryHierarchy method");
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

        log.trace("Exiting parseBoundaryHierarchy method");
        return codes;
    }

    /**
     * Recursively traverses boundary hierarchy to extract codes by boundary type
     */
    @SuppressWarnings("unchecked")
    private void extractCodesFromHierarchy(Map<String, Object> boundaryNode, BoundaryCodes codes) {
        log.trace("Entering extractCodesFromHierarchy method");
        if (boundaryNode == null) {
            log.trace("Boundary node is null, exiting extractCodesFromHierarchy method");
            return;
        }

        String code = (String) boundaryNode.get("code");
        String boundaryType = (String) boundaryNode.get("boundaryType");
        log.debug("Extracting codes from boundary node: code={}, type={}", code, boundaryType);

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
            log.debug("Processing {} child boundary nodes", children.size());
            for (Map<String, Object> child : children) {
                extractCodesFromHierarchy(child, codes);
            }
        }
        log.trace("Exiting extractCodesFromHierarchy method");
    }

    /**
     * Builds BoundaryInfo object from extracted codes
     * Uses provided block/district/state values to ensure boundary object is populated
     */
    private BoundaryInfo buildBoundaryInfo(Facility facility, BoundaryCodes codes,
                                          String blockCode, String districtCode, String stateCode, String countryCode) {
        log.trace("Entering buildBoundaryInfo method");
        BoundaryInfo.BoundaryInfoBuilder builder = BoundaryInfo.builder();
        
        // Set facilityCode (always available)
        String facilityCodeValue = codes != null && codes.getFacilityCode() != null 
            ? codes.getFacilityCode() : facility.getBoundaryCode();
        builder.facilityCode(facilityCodeValue);

        // Use codes from boundary hierarchy if available, otherwise use provided values
        // This ensures boundary object always has values when top-level fields are populated
        String finalBlockCode = codes != null && codes.getBlockCode() != null ? codes.getBlockCode() : blockCode;
        String finalDistrictCode = codes != null && codes.getDistrictCode() != null ? codes.getDistrictCode() : districtCode;
        String finalStateCode = codes != null && codes.getStateCode() != null ? codes.getStateCode() : stateCode;
        String finalCountryCode = codes != null && codes.getCountryCode() != null ? codes.getCountryCode() : countryCode;
        
        builder.blockCode(finalBlockCode)
               .districtCode(finalDistrictCode)
               .stateCode(finalStateCode)
               .countryCode(finalCountryCode);

        BoundaryInfo result = builder.build();
        log.debug("Built BoundaryInfo: {}", result);
        log.trace("Exiting buildBoundaryInfo method");
        return result;
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
        log.trace("Entering existsInKibana method");
        if (facilityId == null || tenantId == null) {
            log.debug("Facility ID or tenant ID is null, returning false");
            return false;
        }

        log.info("Checking if facility {} exists in Kibana for tenant {}", facilityId, tenantId);
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

            log.debug("Executing Elasticsearch query to check facility existence for facility: {}", facilityId);
            if (log.isTraceEnabled()) {
                log.trace("Elasticsearch query: {}", searchQuery);
            }
            @SuppressWarnings("unchecked")
            Map<String, Object> response = restTemplate.postForObject(uri, entity, Map.class);

            // Parse response to check if document exists
            boolean exists = parseSearchResponse(response);
            log.info("Facility {} {} in Kibana for tenant {}", facilityId, exists ? "exists" : "does not exist", tenantId);
            log.trace("Exiting existsInKibana method");
            return exists;

        } catch (Exception e) {
            log.warn("Error checking if facility {} exists in Kibana: {}. Assuming not present.", 
                    sanitizeForLog(facilityId), e.getMessage(), e);
            // If check fails, return false to allow push (fail open approach)
            return false;
        }
    }

    /**
     * Builds the base URL for Elasticsearch API
     * Direct Elasticsearch access: {host}:{port}
     */
    private String getBaseUrl() {
        log.trace("Entering getBaseUrl method");
        String host = esHost.endsWith("/") ? esHost.substring(0, esHost.length() - 1) : esHost;
        String port = esPort.startsWith(":") ? esPort : ":" + esPort;
        String result = host + port;
        log.trace("Exiting getBaseUrl method, base URL: {}", result);
        return result;
    }

    /**
     * Builds HTTP headers with Elasticsearch authentication
     */
    private HttpHeaders buildHeaders() {
        log.trace("Entering buildHeaders method");
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));

        // Add Basic Authentication if credentials are provided
        if (esUsername != null && !esUsername.isEmpty() && esPassword != null && !esPassword.isEmpty()) {
            log.debug("Adding Basic Authentication to headers");
            String auth = esUsername + ":" + esPassword;
            byte[] encodedAuth = Base64.getEncoder().encode(auth.getBytes(StandardCharsets.UTF_8));
            String authHeader = "Basic " + new String(encodedAuth);
            headers.set("Authorization", authHeader);
        }

        log.trace("Exiting buildHeaders method");
        return headers;
    }

    /**
     * Parses the search response to determine if any documents were found
     */
    @SuppressWarnings("unchecked")
    private boolean parseSearchResponse(Map<String, Object> response) {
        log.trace("Entering parseSearchResponse method");
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
                        boolean result = ((Number) value).intValue() > 0;
                        log.debug("Parsed search response: {} documents found", ((Number) value).intValue());
                        log.trace("Exiting parseSearchResponse method");
                        return result;
                    }
                } else if (totalObj instanceof Number) {
                    // ES 6.x format: {"total": 1}
                    boolean result = ((Number) totalObj).intValue() > 0;
                    log.debug("Parsed search response: {} documents found", ((Number) totalObj).intValue());
                    log.trace("Exiting parseSearchResponse method");
                    return result;
                }
            }
            
            log.debug("No documents found in search response");
            log.trace("Exiting parseSearchResponse method");
            return false;
        } catch (Exception e) {
            log.error("Error parsing search response: {}", e.getMessage(), e);
            log.trace("Exiting parseSearchResponse method");
            return false;
        }
    }

    /**
     * Sanitizes a string value for safe logging by removing control characters
     * that could be used for log injection attacks (newlines, carriage returns).
     *
     * @param value The string value to sanitize
     * @return null if input is null, otherwise the sanitized string with \r and \n replaced by spaces
     */
    private String sanitizeForLog(String value) {
        if (value == null) {
            return null;
        }
        return value.replace('\r', ' ').replace('\n', ' ');
    }

    @SuppressWarnings("unchecked")
    private FacilityKibanaIndex fetchExistingKibanaIndex(String facilityId, String tenantId) {
        if (facilityId == null || tenantId == null) {
            log.info("Skipping Kibana lookup: facilityId or tenantId is null (facilityId={}, tenantId={})",
                    facilityId, tenantId);
            return null;
        }

        log.info("Fetching existing Kibana document for facilityId={} tenantId={}", facilityId, tenantId);
        try {
            Map<String, Object> searchQuery = Map.of(
                    "query", Map.of(
                            "bool", Map.of(
                                    "must", List.of(
                                            Map.of("term", Map.of("Data.facilityId.keyword", facilityId)),
                                            Map.of("term", Map.of("Data.tenantId.keyword", tenantId))
                                    )
                            )
                    ),
                    "size", 1
            );

            String uri = getBaseUrl() + "/" + INDEX_NAME + "/" + SEARCH_PATH;
            HttpEntity<Object> entity = new HttpEntity<>(searchQuery, buildHeaders());
            log.info("Executing Kibana lookup query for facilityId={} tenantId={}", facilityId, tenantId);
            Map<String, Object> response = restTemplate.postForObject(uri, entity, Map.class);
            if (response == null) {
                log.info("No Kibana response received for facilityId={} tenantId={}", facilityId, tenantId);
                return null;
            }

            Object hitsObj = response.get("hits");
            if (!(hitsObj instanceof Map)) {
                log.info("Kibana response missing hits object for facilityId={} tenantId={}", facilityId, tenantId);
                return null;
            }

            Object hitListObj = ((Map<String, Object>) hitsObj).get("hits");
            if (!(hitListObj instanceof List) || ((List<?>) hitListObj).isEmpty()) {
                log.info("No existing Kibana document found for facilityId={} tenantId={}", facilityId, tenantId);
                return null;
            }

            Object firstHitObj = ((List<?>) hitListObj).get(0);
            if (!(firstHitObj instanceof Map)) {
                log.info("Kibana first hit has unexpected format for facilityId={} tenantId={}", facilityId, tenantId);
                return null;
            }

            Object sourceObj = ((Map<String, Object>) firstHitObj).get("_source");
            if (!(sourceObj instanceof Map)) {
                log.info("Kibana hit missing _source for facilityId={} tenantId={}", facilityId, tenantId);
                return null;
            }

            Object dataObj = ((Map<String, Object>) sourceObj).get("Data");
            if (!(dataObj instanceof Map)) {
                log.info("Kibana _source missing Data payload for facilityId={} tenantId={}", facilityId, tenantId);
                return null;
            }

            FacilityKibanaIndex existingDoc = mapper.convertValue(dataObj, FacilityKibanaIndex.class);
            String tenantIdLocalized = (String)((Map<String, Object>) dataObj).get("tenantId_localized");
//            List<Integer> geoPoint = (List<Integer>)((Map<String, Object>) dataObj).get("geo-point");
            Object geoPointObj = ((Map<String, Object>) dataObj).get("geo-point");
            Integer total_tickets = (Integer)((Map<String, Object>) dataObj).get("total_tickets");
            Integer open_tickets = (Integer)((Map<String, Object>) dataObj).get("open_tickets");
            Integer closed_tickets = (Integer)((Map<String, Object>) dataObj).get("closed_tickets");
            String solar_panel_status = (String)((Map<String, Object>) dataObj).get("solar_panel_status");
            existingDoc.setTenantIdLocalized(tenantIdLocalized);
//            String result = geoPoint!= null ? geoPoint.stream().map(String::valueOf).collect(Collectors.joining(", ")) : null;
//            existingDoc.setGeoPoint(result);
            String geoPointStr = null;
            if (geoPointObj instanceof List) {
                geoPointStr = ((List<?>) geoPointObj).stream()
                        .map(String::valueOf)
                        .collect(Collectors.joining(", "));
            } else if (geoPointObj != null) {
                geoPointStr = geoPointObj.toString();
            }
            existingDoc.setGeoPoint(geoPointStr);
            existingDoc.setTotalTickets(total_tickets);
            existingDoc.setOpenTickets(open_tickets);
            existingDoc.setClosedTickets(closed_tickets);
            existingDoc.setSolarPanelStatus(solar_panel_status);
            log.info("Successfully fetched existing Kibana document for facilityId={} tenantId={}", facilityId, tenantId);
            return existingDoc;
        } catch (Exception e) {
            log.warn("Unable to fetch existing Kibana document for facility {} and tenant {}: {}",
                    facilityId, tenantId, e.getMessage());
            return null;
        }
    }
}

