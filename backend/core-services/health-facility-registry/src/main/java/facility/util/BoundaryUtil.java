package facility.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import facility.web.models.Boundary;
import facility.web.models.FacilityBulkSearchCriteria;
import org.egov.common.contract.request.RequestInfo;
import org.egov.tracer.model.CustomException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

import static facility.config.ServiceConstants.*;

@Component
public class BoundaryUtil {
    @Autowired
    private RestTemplate restTemplate;

    @Value("${egov.boundary.host}")
    private String boundaryHost;

    @Value("${egov.boundary.relationship.search.path}")
    private String boundaryUrl;

    @Value("${egov.boundary.type}")
    private String boundaryType;

    @Value("${egov.boundary.hierarchy.type}")
    private String boundaryHierarchyType;

    @Autowired
    private ObjectMapper objectMapper;

//    @Cacheable(value="boundaryConfiguration")
    public String getBoundaryData() {
        String jsonString = null;
        String params = "?boundaryType="+boundaryType+"&includeChildren=true&tenantId=in&hierarchyType="+boundaryHierarchyType;
        StringBuilder uri = new StringBuilder();
        uri.append(boundaryHost).append(boundaryUrl).append(params);
        RequestInfo requestInfo = new RequestInfo();
        Object response = null;
        try {
            response = restTemplate.postForObject(uri.toString(), requestInfo, Map.class);
            if (response == null) {
              throw new CustomException("CONFIG_ERROR", "Boundary service returned null response");
            }
            jsonString = objectMapper.writeValueAsString(response);
        }catch(Exception e) {
            throw new CustomException("CONFIG_ERROR","Error in fetching inbox query boundary ");
        }

        return jsonString;
    }

    public String getBoundaryData(String codes) {
        String jsonString = null;
        String params = "?codes="+codes+"&includeChildren=true&tenantId=in&hierarchyType="+boundaryHierarchyType;
        StringBuilder uri = new StringBuilder();
        uri.append(boundaryHost).append(boundaryUrl).append(params);
        RequestInfo requestInfo = new RequestInfo();
        Object response = null;
        try {
            response = restTemplate.postForObject(uri.toString(), requestInfo, Map.class);
            if (response == null) {
                throw new CustomException("CONFIG_ERROR", "Boundary service returned null response");
            }
            jsonString = objectMapper.writeValueAsString(response);
        }catch(Exception e) {
            e.printStackTrace();
            throw new CustomException("CONFIG_ERROR","Error in fetching inbox query boundary ");
        }

        return jsonString;
    }

    public Map<String, Boundary> getBoundaryByCode() {
        Map<String, Boundary> listBlock = null;
        try {
            String jsonString = getBoundaryData();
            listBlock = extractBlockToDistrictMapping(jsonString);
        }catch(Exception e) {
//            e.printStackTrace();
            throw new CustomException("CONFIG_ERROR","Error in fetching inbox query boundary ");
        }

        return listBlock;
    }

    public static Map<String, Boundary> extractBlockToDistrictMapping(String json) throws IOException {
        Map<String, Boundary> blockToDistrictMap = new HashMap<>();
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
                                        JsonNode facilities = block.get("children");
                                        JsonNode blockCodeNode = block.get("code");
                                        String blockCode = blockCodeNode.asText();
                                        // To take into account facilities whose boundary code is: India_Assam_Kamrup_Amingaon_FAC/2025/0045
                                        if (facilities != null && facilities.isArray() && !facilities.isEmpty()) {
                                            for (JsonNode facility : facilities) {
                                                JsonNode boundaryTypeNode = facility.get("boundaryType");
                                                JsonNode facilityCodeNode = facility.get("code");
                                                if (boundaryTypeNode != null && facilityCodeNode !=null && "Facility".equals(boundaryTypeNode.asText())) {
                                                    String facilityCode = facilityCodeNode.asText();
                                                    Boundary boundary = Boundary.builder().state(stateCode).district(districtCode).block(blockCode).build();
                                                    blockToDistrictMap.put(facilityCode, boundary);
                                                }
                                            }
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

//    private List<String> getFacilityCodesByStateDistrictBlock(
//            String json,
//            List<String> stateNames,
//            List<String> districtNames,
//            List<String> blockNames
//    ) throws JsonProcessingException {
//        if (json == null || json.trim().isEmpty()) {
//            throw new IllegalArgumentException("JSON input cannot be null or empty");
//        }
//        ObjectMapper objectMapper = new ObjectMapper();
//        JsonNode rootNode = objectMapper.readTree(json);
//        List<String> facilityCodes = new ArrayList<>();
//
//        JsonNode states = rootNode
//                .path("TenantBoundary")
//                .get(0)
//                .path("boundary");
//
//        for (JsonNode state : states) {
//
//            if (!"State".equalsIgnoreCase(state.path("boundaryType").asText())) {
//                continue;
//            }
//
//            if (!matches(state, "code", stateNames)) {
//                continue;
//            }
//
//            // District
//            for (JsonNode district : state.path("children")) {
//
//                if (!"District".equalsIgnoreCase(district.path("boundaryType").asText())) {
//                    continue;
//                }
//
//                if (!matches(district, "code", districtNames)) {
//                    continue;
//                }
//
//                // Block
//                for (JsonNode block : district.path("children")) {
//
//                    if (!"Block".equalsIgnoreCase(block.path("boundaryType").asText())) {
//                        continue;
//                    }
//
//                    if (!matches(block, "code", blockNames)) {
//                        continue;
//                    }
//
//                    // 🎯 Facilities sous ce block
//                    collectFacilities(block, facilityCodes);
//                }
//            }
//        }
//
//        return facilityCodes;
//    }
//
//    private static boolean matches(JsonNode node, String field, List<String> values) {
//        return values == null
//                || values.isEmpty()
//                || values.stream()
//                .anyMatch(v -> v.equalsIgnoreCase(node.path(field).asText()));
//    }
//
//    private static void collectFacilities(JsonNode block, List<String> result) {
//
//        for (JsonNode child : block.path("children")) {
//            if ("Facility".equalsIgnoreCase(child.path("boundaryType").asText())) {
//                result.add(child.path("code").asText());
//            }
//        }
//    }

    public List<String> getFacilityCodesFromBoundary(FacilityBulkSearchCriteria criteria) {
        List<String> listBlock = null;
        try {
            listBlock = resolveFacilityCodes(criteria);
        }catch(Exception e) {
            e.printStackTrace();
            throw new CustomException("CONFIG_ERROR","Error in fetching inbox query boundary ");
        }

        return listBlock;
    }

    public String detectLowestLevel(FacilityBulkSearchCriteria criteria) {

        if (criteria.getBoundaryCodes() != null && !criteria.getBoundaryCodes().isEmpty()) {
            return BOUNDARY_CODE;
        }
        if (criteria.getBlock() != null && !criteria.getBlock().isEmpty()) {
            return BLOCK_CODE;
        }
        if (criteria.getDistrict() != null && !criteria.getDistrict().isEmpty()) {
            return DISTRICT_CODE;
        }
        if (criteria.getState() != null && !criteria.getState().isEmpty()) {
            return STATE_CODE;
        }
        return null;
    }

    public List<String> resolveFacilityCodes(FacilityBulkSearchCriteria criteria) throws JsonProcessingException {
        String level = detectLowestLevel(criteria);
        ObjectMapper objectMapper = new ObjectMapper();
        switch (level) {

            case BOUNDARY_CODE:
                String boundaryCodes = criteria.getBoundaryCodes().stream()
                        .filter(Objects::nonNull)
                        .map(String::trim)
                        .collect(Collectors.joining(","));
                String jsonString = getBoundaryData(boundaryCodes);
                JsonNode rootNode = objectMapper.readTree(jsonString);
                return extractFacilityCodesByLevel(rootNode, criteria.getBoundaryCodes());

            case BLOCK_CODE:
                String blockBoundaryCodes = criteria.getBlock().stream()
                        .filter(Objects::nonNull)
                        .map(String::trim)
                        .collect(Collectors.joining(","));
                String blockJsonString = getBoundaryData(blockBoundaryCodes);
                JsonNode blockRootNode = objectMapper.readTree(blockJsonString);
                return extractFacilityCodesByLevel(blockRootNode, criteria.getBlock());

            case DISTRICT_CODE:
                String districtBoundaryCodes = criteria.getDistrict().stream()
                        .filter(Objects::nonNull)
                        .map(String::trim)
                        .collect(Collectors.joining(","));
                String districtJsonString = getBoundaryData(districtBoundaryCodes);
                JsonNode districtRootNode = objectMapper.readTree(districtJsonString);
                return extractFacilityCodesByLevel(districtRootNode, criteria.getDistrict());

            case STATE_CODE:
                String stateBoundaryCodes = criteria.getState().stream()
                        .filter(Objects::nonNull)
                        .map(String::trim)
                        .collect(Collectors.joining(","));
                String stateJsonString = getBoundaryData(stateBoundaryCodes);
                JsonNode stateRootNode = objectMapper.readTree(stateJsonString);
                return extractFacilityCodesByLevel(stateRootNode, criteria.getBlock());

            default:
                return Collections.emptyList();
        }
    }

    public List<String> extractFacilityCodesByLevel(JsonNode rootNode, List<String> boundaryCodes) {
        List<String> result = new ArrayList<>();

        if (boundaryCodes == null || boundaryCodes.isEmpty()) {
            return result;
        }

        Set<String> targetCodes = new HashSet<>(boundaryCodes);

        JsonNode boundaries = rootNode
                .path("TenantBoundary")
                .get(0)
                .path("boundary");

        for (JsonNode node : boundaries) {
            findAndCollect(node, targetCodes, result);
        }
        return result;
    }

    private boolean findAndCollect(
            JsonNode node,
            Set<String> boundaryCodes,
            List<String> result
    ) {
        String code = node.path("code").asText();

        // Si on a trouvé le bon niveau et le bon code
        if (boundaryCodes.contains(code)) {
            collectFacilitiesRecursive(node, result);
            return true; // Stop la recherche
        }

        // Continue à chercher dans les enfants
        JsonNode children = node.path("children");
        if (children.isArray()) {
            for (JsonNode child : children) {
                if (findAndCollect(child, boundaryCodes, result)) {
                    return true;
                }
            }
        }
        return false;
    }

    private void collectFacilitiesRecursive(JsonNode node, List<String> result) {

        if ("Facility".equalsIgnoreCase(node.path("boundaryType").asText())) {
            String fullCode = node.path("code").asText();
            result.add(fullCode);
            return;
        }

        JsonNode children = node.path("children");
        if (children.isArray()) {
            for (JsonNode child : children) {
                collectFacilitiesRecursive(child, result);
            }
        }
    }



}

