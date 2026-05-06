package facility.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import facility.web.models.Boundary;
import facility.web.models.BoundaryRelationshipSearchCriteria;
import facility.web.models.FacilityBulkSearchCriteria;
import org.egov.common.contract.request.RequestInfo;
import org.egov.tracer.model.CustomException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

import static facility.config.ServiceConstants.*;

@Slf4j
@Component
public class BoundaryUtil {

    /**
     * Batch size for boundary-relationships API when resolving only codes present on facility rows.
     * Avoids loading the full hierarchy tree when enriching bulk search results.
     */
    private static final int BOUNDARY_CODE_BATCH_SIZE = 50;
    @Autowired
    private RestTemplate restTemplate;

    @Value("${egov.boundary.host}")
    private String boundaryHost;

    @Value("${egov.boundary.relationship.search.path}")
    private String boundaryUrl;

    @Value("${egov.boundary.relationship.search.v2.path}")
    private String boundaryV2Url;

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
//            e.printStackTrace();
            throw new CustomException("CONFIG_ERROR","Error in fetching inbox query boundary "+ e.getMessage());
        }

        return jsonString;
    }

    public String getBoundaryData(List<String> codes) {
        String jsonString = null;
//        String params = "?codes="+codes+"&includeChildren=true&tenantId=in&hierarchyType="+boundaryHierarchyType;
        StringBuilder uri = new StringBuilder();
        uri.append(boundaryHost).append(boundaryV2Url);
        RequestInfo requestInfo = new RequestInfo();
        BoundaryRelationshipSearchCriteria searchCriteria = BoundaryRelationshipSearchCriteria.builder()
                .tenantId("in")
                .includeChildren(true)
                .hierarchyType(boundaryHierarchyType)
                .codes(codes)
                .build();
        Map<String, Object> searchCriteriaRequest = new HashMap<>();
        searchCriteriaRequest.put("RequestInfo", requestInfo);
        searchCriteriaRequest.put("BoundaryRelationship", searchCriteria);
        Object response = null;
        try {
            response = restTemplate.postForObject(uri.toString(), searchCriteriaRequest, Map.class);
            if (response == null) {
                throw new CustomException("CONFIG_ERROR", "Boundary service returned null response");
            }
            jsonString = objectMapper.writeValueAsString(response);
        }catch(Exception e) {
//            e.printStackTrace();
            throw new CustomException("CONFIG_ERROR","Error in fetching inbox query boundary "+e.getMessage());
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
            throw new CustomException("CONFIG_ERROR","Error in fetching inbox query boundary "+ e.getMessage());
        }

        return listBlock;
    }

    /**
     * Resolves boundary hierarchy (state / district / block) only for the given facility boundary codes
     * by calling the boundary v2 API in batches, then parsing with the same mapping as the full-tree path.
     * Use this for bulk facility responses instead of {@link #getBoundaryByCode()} when you already know
     * which codes appear on {@code facility.boundary_code}.
     */
    public Map<String, Boundary> getBoundaryMapForFacilityCodes(Collection<String> boundaryCodes) {
        if (boundaryCodes == null || boundaryCodes.isEmpty()) {
            return Collections.emptyMap();
        }
        List<String> distinct = boundaryCodes.stream()
                .filter(Objects::nonNull)
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .distinct()
                .collect(Collectors.toList());
        if (distinct.isEmpty()) {
            return Collections.emptyMap();
        }
        Map<String, Boundary> merged = new HashMap<>();
        for (int i = 0; i < distinct.size(); i += BOUNDARY_CODE_BATCH_SIZE) {
            int end = Math.min(i + BOUNDARY_CODE_BATCH_SIZE, distinct.size());
            List<String> batch = distinct.subList(i, end);
            try {
                String jsonString = getBoundaryData(batch);
                merged.putAll(extractBlockToDistrictMapping(jsonString));
            } catch (IOException e) {
                log.error("Failed to parse boundary batch (size={}): {}", batch.size(), e.getMessage());
                throw new CustomException("CONFIG_ERROR", "Error parsing boundary response: " + e.getMessage());
            } catch (Exception e) {
                log.error("Failed to fetch boundary data for batch (size={}): {}", batch.size(), e.getMessage());
                throw new CustomException("CONFIG_ERROR", "Error in fetching boundary data: " + e.getMessage());
            }
        }
        return merged;
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
        if(isNotNullOrEmpty(criteria.getBoundaryCodes()) || isNotNullOrEmpty(criteria.getBlock()) || isNotNullOrEmpty(criteria.getDistrict()) ||
                isNotNullOrEmpty(criteria.getState())
        ){
            try {
                listBlock = resolveFacilityCodes(criteria);
            }catch(Exception e) {
                e.printStackTrace();
                throw new CustomException("CONFIG_ERROR","Error in fetching inbox query boundary "+ e.getMessage());
            }
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
                List<String> boundaryCodes = criteria.getBoundaryCodes().stream()
                        .filter(Objects::nonNull)
                        .map(String::trim)
                        .collect(Collectors.toList());
                String jsonString = getBoundaryData(boundaryCodes);
                JsonNode rootNode = objectMapper.readTree(jsonString);
                return extractFacilityCodesByLevel(rootNode, criteria.getBoundaryCodes());

            case BLOCK_CODE:
                List<String> blockBoundaryCodes = criteria.getBlock().stream()
                        .filter(Objects::nonNull)
                        .map(String::trim)
                        .collect(Collectors.toList());
                String blockJsonString = getBoundaryData(blockBoundaryCodes);
                JsonNode blockRootNode = objectMapper.readTree(blockJsonString);
                return extractFacilityCodesByLevel(blockRootNode, criteria.getBlock());

            case DISTRICT_CODE:
                List<String> districtBoundaryCodes = criteria.getDistrict().stream()
                        .filter(Objects::nonNull)
                        .map(String::trim)
                        .collect(Collectors.toList());
                String districtJsonString = getBoundaryData(districtBoundaryCodes);
                JsonNode districtRootNode = objectMapper.readTree(districtJsonString);
                return extractFacilityCodesByLevel(districtRootNode, criteria.getDistrict());

            case STATE_CODE:
                List<String> stateBoundaryCodes = criteria.getState().stream()
                        .filter(Objects::nonNull)
                        .map(String::trim)
                        .collect(Collectors.toList());
                String stateJsonString = getBoundaryData(stateBoundaryCodes);
                JsonNode stateRootNode = objectMapper.readTree(stateJsonString);
                return extractFacilityCodesByLevel(stateRootNode, criteria.getState());

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

    private boolean isNotNullOrEmpty(Collection<?> c) {
        return c != null && !c.isEmpty();
    }

}

