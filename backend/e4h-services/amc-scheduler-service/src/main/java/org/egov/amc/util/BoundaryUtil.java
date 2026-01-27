package org.egov.amc.util;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.egov.amc.web.models.Boundary;
import org.egov.common.contract.request.RequestInfo;
import org.egov.tracer.model.CustomException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Component
@Slf4j
public class BoundaryUtil {
    @Autowired
    private RestTemplate restTemplate;

    @Value("${egov.boundary.host}")
    private String boundaryHost;

    @Value("${egov.boundary.search.endpoint}")
    private String boundaryUrl;

    @Value("${egov.boundary.type}")
    private String boundaryType;

    @Value("${egov.boundary.hierarchy.type}")
    private String boundaryHierarchyType;

    @Autowired
    private ObjectMapper objectMapper;

//    @Cacheable(value="boundaryConfiguration")
    public Map<String, Boundary> getBoundaryByCode() {
        log.trace("Entering getBoundaryByCode method");
        Map<String, Boundary> listBlock = null;
        String params = "?boundaryType="+boundaryType+"&includeChildren=true&tenantId=in&hierarchyType="+boundaryHierarchyType;
        StringBuilder uri = new StringBuilder();
        uri.append(boundaryHost).append(boundaryUrl).append(params);
        RequestInfo requestInfo = new RequestInfo();
        Object response = null;
        try {
            log.debug("Calling boundary service at URI: {}", uri);
            response = restTemplate.postForObject(uri.toString(), requestInfo, Map.class);
            if (response == null) {
                log.error("Boundary service returned null response for URI: {}", uri);
                throw new CustomException("CONFIG_ERROR", "Boundary service returned null response");
            }
            String jsonString = objectMapper.writeValueAsString(response);
            listBlock = extractBlockToDistrictMapping(jsonString);
            log.debug("Successfully fetched {} boundary mappings", listBlock != null ? listBlock.size() : 0);
        }catch(Exception e) {
            log.error("Error in fetching boundary data from service, URI: {}", uri, e);
            throw new CustomException("CONFIG_ERROR","Error in fetching inbox query boundary ");
        }

        return listBlock;
    }

    public static Map<String, Boundary> extractBlockToDistrictMapping(String json) throws IOException {
        if (json == null || json.trim().isEmpty()) {
            throw new IllegalArgumentException("JSON input cannot be null or empty");
        }

        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode root = objectMapper.readTree(json);

        Map<String, Boundary> blockToDistrictMap = new HashMap<>();
        JsonNode tenantBoundaryArray = root.get("TenantBoundary");
        if (tenantBoundaryArray == null || !tenantBoundaryArray.isArray()) {
            return blockToDistrictMap;
        }

        for (JsonNode tenantBoundary : tenantBoundaryArray) {
            JsonNode boundaryArray = tenantBoundary.get("boundary");
            if (boundaryArray == null || !boundaryArray.isArray()) {
                continue;
            }
            processStates(boundaryArray, blockToDistrictMap);
        }

        return blockToDistrictMap;
    }

    private static void processStates(JsonNode boundaryArray, Map<String, Boundary> blockToDistrictMap) {
        for (JsonNode state : boundaryArray) {
            JsonNode stateCodeNode = state.get("code");
            if (stateCodeNode == null) {
                continue;
            }
            String stateCode = stateCodeNode.asText();
            JsonNode districts = state.get("children");
            if (districts == null || !districts.isArray()) {
                continue;
            }
            processDistricts(districts, stateCode, blockToDistrictMap);
        }
    }

    private static void processDistricts(JsonNode districts,
                                         String stateCode,
                                         Map<String, Boundary> blockToDistrictMap) {
        for (JsonNode district : districts) {
            JsonNode districtCodeNode = district.get("code");
            if (districtCodeNode == null) {
                continue;
            }
            String districtCode = districtCodeNode.asText();
            JsonNode blocks = district.get("children");
            if (blocks == null || !blocks.isArray()) {
                continue;
            }
            processBlocks(blocks, stateCode, districtCode, blockToDistrictMap);
        }
    }

    private static void processBlocks(JsonNode blocks,
                                      String stateCode,
                                      String districtCode,
                                      Map<String, Boundary> blockToDistrictMap) {
        for (JsonNode block : blocks) {
            JsonNode blockCodeNode = block.get("code");
            if (blockCodeNode == null) {
                continue;
            }
            String blockCode = blockCodeNode.asText();
            JsonNode facilities = block.get("children");
            if (facilities == null || !facilities.isArray() || facilities.isEmpty()) {
                continue;
            }
            mapFacilities(facilities, stateCode, districtCode, blockCode, blockToDistrictMap);
        }
    }

    private static void mapFacilities(JsonNode facilities,
                                      String stateCode,
                                      String districtCode,
                                      String blockCode,
                                      Map<String, Boundary> blockToDistrictMap) {
        for (JsonNode facility : facilities) {
            JsonNode boundaryTypeNode = facility.get("boundaryType");
            JsonNode facilityCodeNode = facility.get("code");
            if (boundaryTypeNode == null || facilityCodeNode == null) {
                continue;
            }
            if (!"Facility".equals(boundaryTypeNode.asText())) {
                continue;
            }
            String facilityCode = facilityCodeNode.asText();
            Boundary boundary = Boundary.builder()
                    .state(stateCode)
                    .district(districtCode)
                    .block(blockCode)
                    .build();
            blockToDistrictMap.put(facilityCode, boundary);
        }
    }
}

