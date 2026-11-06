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

    /** The hierarchy levels a client may filter on, in the order they nest. */
    public static final List<String> FILTERABLE_LEVELS =
            Collections.unmodifiableList(Arrays.asList("state", "district", "block"));

    /**
     * Each hierarchy level the client filtered on, resolved to the facility codes beneath it.
     *
     * Only levels the client actually supplied appear in the result. Each becomes its own ANDed
     * SQL clause, so a facility has to sit beneath one of the states <em>and</em> one of the
     * districts <em>and</em> one of the blocks - the reading a cascading filter bar needs, and the
     * one that guarantees a filter can only ever narrow. Doing it in SQL rather than intersecting
     * in Java means an empty level simply matches nothing, with no special case to get wrong.
     *
     * {@code boundaryCodes} is deliberately absent: those are facility-level codes already and go
     * straight to the column.
     *
     * A requested code stays in its own set alongside its descendants, because a facility row whose
     * {@code boundary_code} was never migrated into the hierarchy has nothing to descend from -
     * which is what {@code _backfill-boundary-relationships} exists to repair.
     */
    public Map<String, Set<String>> resolveLevelFilters(FacilityBulkSearchCriteria criteria) {
        Map<String, List<String>> requested = new LinkedHashMap<>();
        putIfPresent(requested, "state", criteria.getState());
        putIfPresent(requested, "district", criteria.getDistrict());
        putIfPresent(requested, "block", criteria.getBlock());
        if (requested.isEmpty()) {
            return Collections.emptyMap();
        }

        List<String> everyCode = requested.values().stream()
                .flatMap(List::stream)
                .distinct()
                .collect(Collectors.toList());

        Map<String, Set<String>> beneath;
        try {
            // One call for every level at once: boundary-service filters `codes` with a plain IN
            // and no level constraint, so mixed-level codes come back as one tree.
            beneath = facilityCodesBeneath(everyCode);
        } catch (Exception e) {
            log.error("Failed to resolve the client's boundary filters", e);
            throw new CustomException("CONFIG_ERROR", "Error in fetching inbox query boundary " + e.getMessage());
        }

        Map<String, Set<String>> resolved = new LinkedHashMap<>();
        for (Map.Entry<String, List<String>> level : requested.entrySet()) {
            Set<String> selected = new LinkedHashSet<>();
            for (String code : level.getValue()) {
                selected.add(code);
                selected.addAll(beneath.getOrDefault(code, Collections.emptySet()));
            }
            resolved.put(level.getKey(), selected);
        }
        return resolved;
    }

    private static void putIfPresent(Map<String, List<String>> target, String level, List<String> codes) {
        if (codes == null || codes.isEmpty()) {
            return;
        }
        List<String> cleaned = codes.stream()
                .filter(Objects::nonNull)
                .map(String::trim)
                .filter(code -> !code.isEmpty())
                .distinct()
                .collect(Collectors.toList());
        if (!cleaned.isEmpty()) {
            target.put(level, cleaned);
        }
    }

    /**
     * Every facility-level code at or beneath each of the given boundary codes, keyed by the code asked for.
     *
     * A code that is itself a Facility maps to just itself. A code absent from the hierarchy maps to
     * an empty set rather than being missing, so callers never have to distinguish the two.
     */
    public Map<String, Set<String>> facilityCodesBeneath(Collection<String> boundaryCodes) throws JsonProcessingException {
        Map<String, Set<String>> result = new LinkedHashMap<>();
        List<String> wanted = boundaryCodes == null ? Collections.emptyList()
                : boundaryCodes.stream()
                        .filter(Objects::nonNull)
                        .map(String::trim)
                        .filter(code -> !code.isEmpty())
                        .distinct()
                        .collect(Collectors.toList());
        if (wanted.isEmpty()) {
            return result;
        }

        JsonNode root = objectMapper.readTree(getBoundaryData(wanted));
        Map<String, JsonNode> byCode = new HashMap<>();
        // Walk every tenant and every root boundary. The previous version read TenantBoundary[0] only,
        // which threw on an empty response.
        for (JsonNode tenantBoundary : root.path("TenantBoundary")) {
            for (JsonNode boundary : tenantBoundary.path("boundary")) {
                indexByCode(boundary, byCode);
            }
        }

        for (String code : wanted) {
            Set<String> facilities = new LinkedHashSet<>();
            JsonNode node = byCode.get(code);
            if (node != null) {
                collectFacilitiesRecursive(node, facilities);
            } else {
                log.warn("Boundary code {} is not in the {} hierarchy; it selects no descendants",
                        code, boundaryHierarchyType);
            }
            result.put(code, facilities);
        }
        return result;
    }

    /**
     * Facility-level codes at or beneath one boundary, for callers holding a single grant.
     *
     * A Facility-level boundary resolves to itself without calling boundary-service at all, which is
     * the common case for vendor and facility-POC users.
     */
    public Set<String> expandToFacilityCodes(String boundaryCode, String boundaryType) throws JsonProcessingException {
        if (boundaryCode == null || boundaryCode.trim().isEmpty()) {
            return Collections.emptySet();
        }
        String code = boundaryCode.trim();
        if (FACILITY_BOUNDARY_TYPE.equalsIgnoreCase(boundaryType)) {
            return Collections.singleton(code);
        }
        Set<String> facilities = facilityCodesBeneath(Collections.singletonList(code))
                .getOrDefault(code, Collections.emptySet());
        return new LinkedHashSet<>(facilities);
    }

    /** Indexes every node in the subtree by its code, so a later lookup needs no second walk. */
    private static void indexByCode(JsonNode node, Map<String, JsonNode> index) {
        JsonNode codeNode = node.get("code");
        if (codeNode != null) {
            index.putIfAbsent(codeNode.asText(), node);
        }
        JsonNode children = node.path("children");
        if (children.isArray()) {
            for (JsonNode child : children) {
                indexByCode(child, index);
            }
        }
    }

    /**
     * Collects the Facility-type codes at or beneath this node.
     *
     * Visits every sibling. The previous version stopped the whole search at the first match, so a
     * filter naming several boundaries resolved only the first one found in each subtree.
     */
    private static void collectFacilitiesRecursive(JsonNode node, Set<String> result) {
        if (FACILITY_BOUNDARY_TYPE.equalsIgnoreCase(node.path("boundaryType").asText())) {
            result.add(node.path("code").asText());
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
