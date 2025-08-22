package org.egov.project.util;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.egov.common.contract.request.RequestInfo;
import org.egov.common.http.client.ServiceRequestClient;
import org.egov.common.models.core.Boundary;
import org.egov.project.web.models.BoundaryV2;
import org.egov.project.web.models.boundary.BoundaryResponse;
import org.egov.tracer.model.CustomException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.egov.project.util.ProjectConstants.HIERARCHY_TYPE;
import static org.egov.project.util.ProjectConstants.TENANTID;

/**
 * Utility class to validate boundary details.
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class BoundaryV2Util {

    // Injecting boundary host value from configuration
    @Value("${egov.boundary.host}")
    private String boundaryHost;

    // Injecting boundary search URL value from configuration
    @Value("${egov.boundary.search.url}")
    private String boundarySearchUrl;

    @Value("${egov.boundary.search.endpoint}")
    private String boundaryHierarchySearchUrl;

    private final ServiceRequestClient serviceRequestClient;

    @Autowired
    private ObjectMapper objectMapper;

    /**
     * Validates boundary details against the egov-location service response.
     *
     * @param boundaryTypeBoundariesMap A map of boundary types with lists of boundary codes
     * @param tenantId                  The tenant ID
     * @param requestInfo               Request information
     * @param hierarchyTypeCode         Hierarchy type code
     */
    public void validateBoundaryDetails(Map<String, List<String>> boundaryTypeBoundariesMap, String tenantId,
                                        RequestInfo requestInfo, String hierarchyTypeCode) {
        // Flatten the lists of boundary codes from the map values
        List<String> boundaries = boundaryTypeBoundariesMap.values().stream().flatMap(List::stream)
                .toList();
        if (CollectionUtils.isEmpty(boundaries)) return;
        try {
            // Fetch boundary details from the service
            log.debug("Fetching boundary details for tenantId: {}, boundaries: {}", tenantId, boundaries);
            BoundaryResponse boundarySearchResponse = serviceRequestClient.fetchResult(
                    new StringBuilder(boundaryHost
                            + boundarySearchUrl
                            + "?limit=" + boundaries.size()
                            + "&offset=0&tenantId=" + tenantId
                            + "&codes=" + String.join(",", boundaries)),
                    requestInfo,
                    BoundaryResponse.class
            );
            log.debug("Boundary details fetched successfully for tenantId: {}", tenantId);

            // Extract invalid boundary codes
            List<String> invalidBoundaryCodes = new ArrayList<>(boundaries);
            invalidBoundaryCodes.removeAll(boundarySearchResponse.getBoundary().stream()
                    .map(Boundary::getCode)
                    .toList()
            );

            // Throw exception if invalid boundary codes are found
            if (!invalidBoundaryCodes.isEmpty()) {
                log.error("The boundary data for the codes {} is not available.", invalidBoundaryCodes);
                throw new CustomException("INVALID_BOUNDARY_DATA", "The boundary data for the code "
                        + invalidBoundaryCodes + " is not available");
            }
        } catch (Exception e) {
            log.error("Exception while searching boundaries for tenantId: {}", tenantId, e);
            // Throw a custom exception if an error occurs during boundary search
            throw new CustomException("BOUNDARY_SERVICE_SEARCH_ERROR", "Error in while fetching boundaries from Boundary Service : " + e.getMessage());
        }
    }

//    @Cacheable(value="boundaryConfiguration")
    public Map<String, BoundaryV2> getBoundaryByCode() {
        Map<String, BoundaryV2> listBlock = null;
        String params = "?boundaryType=State&includeChildren=true&tenantId="+TENANTID+"&hierarchyType="+HIERARCHY_TYPE;
        StringBuilder uri = new StringBuilder();
        uri.append(boundaryHost).append(boundaryHierarchySearchUrl).append(params);
        RequestInfo requestInfo = new RequestInfo();
        try {
            log.debug("Fetching boundary details for tenantId: {}, boundaries: {}", TENANTID, HIERARCHY_TYPE);
            Object boundarySearchResponse = serviceRequestClient.fetchResult(uri, requestInfo, Map.class);
            if (boundarySearchResponse == null) {
                throw new CustomException("CONFIG_ERROR", "Boundary service returned null response");
            }
            log.debug("Boundary details fetched successfully for tenantId: {}", TENANTID);
            String jsonString = objectMapper.writeValueAsString(boundarySearchResponse);
            listBlock = extractBlockToDistrictMapping(jsonString);
        }catch(Exception e) {
            throw new CustomException("CONFIG_ERROR","Error in fetching inbox query boundary ");
        }

        return listBlock;
    }

    public static Map<String, BoundaryV2> extractBlockToDistrictMapping(String json) throws IOException {
        Map<String, BoundaryV2> blockToDistrictMap = new HashMap<>();
        if (json == null || json.trim().isEmpty()) {
            throw new IllegalArgumentException("JSON input cannot be null or empty");
        }
        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode root = objectMapper.readTree(json);

        JsonNode tenantBoundaryArray = root.get("TenantBoundary");
        if (tenantBoundaryArray != null && tenantBoundaryArray.isArray()) {
            for (JsonNode tenantBoundary : tenantBoundaryArray) {
                JsonNode boundaryArray = tenantBoundary.get("boundary");
                if (boundaryArray != null && boundaryArray.isArray()) {
                    for (JsonNode state : boundaryArray) {
                        JsonNode districts = state.get("children");
                        JsonNode stateCodeNode = state.get("code");
                        if (stateCodeNode == null) continue;
                        String stateCode = stateCodeNode.asText();
                        if (districts != null && districts.isArray()) {
                            for (JsonNode district : districts) {
                                JsonNode districtCodeNode = district.get("code");
                                if (districtCodeNode == null) continue;
                                String districtCode = districtCodeNode.asText();
                                JsonNode blocks = district.get("children");
                                if (blocks != null && blocks.isArray()) {
                                    for (JsonNode block : blocks) {
                                        JsonNode boundaryTypeNode = block.get("boundaryType");
                                        JsonNode blockCodeNode = block.get("code");
                                        if (boundaryTypeNode != null && blockCodeNode !=null && "Block".equals(boundaryTypeNode.asText())) {
                                            String blockCode = blockCodeNode.asText();
                                            BoundaryV2 boundary = BoundaryV2.builder().state(stateCode).district(districtCode).build();
                                            blockToDistrictMap.put(blockCode, boundary);
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        return blockToDistrictMap;
    }
}
