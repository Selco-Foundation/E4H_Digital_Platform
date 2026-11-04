package org.egov.inbox.util;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.jsonpath.JsonPath;
import org.egov.common.contract.request.RequestInfo;
import org.egov.common.utils.MultiStateInstanceUtil;
import org.egov.inbox.web.model.Boundary;
import org.egov.inbox.web.model.V2.InboxQueryConfiguration;
import org.egov.mdms.model.MasterDetail;
import org.egov.mdms.model.MdmsCriteria;
import org.egov.mdms.model.MdmsCriteriaReq;
import org.egov.mdms.model.ModuleDetail;
import org.egov.tracer.model.CustomException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import static org.egov.inbox.util.InboxConstants.*;


@Component
public class BoundaryUtil {
    @Autowired
    private RestTemplate restTemplate;

    @Value("${egov.boundary.host}")
    private String boundaryHost;

    @Value("${egov.boundary.search.endpoint}")
    private String boundaryUrl;

    @Autowired
    private ObjectMapper objectMapper;

    @Cacheable(value="boundaryConfiguration")
    public Map<String, Boundary> getBoundaryByCode() {
        Map<String, Boundary> listBlock = null;
        String params = "?boundaryType=State&includeChildren=true&tenantId=in&hierarchyType=SELCO";
        StringBuilder uri = new StringBuilder();
        uri.append(boundaryHost).append(boundaryUrl).append(params);
        RequestInfo requestInfo = new RequestInfo();
        Object response = null;
        try {
            response = restTemplate.postForObject(uri.toString(), requestInfo, Map.class);
            if (response == null) {
              throw new CustomException("CONFIG_ERROR", "Boundary service returned null response");
            }
            String jsonString = objectMapper.writeValueAsString(response);
            listBlock = extractBlockToDistrictMapping(jsonString);
        }catch(Exception e) {
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
                                        JsonNode boundaryTypeNode = block.get("boundaryType");
                                        JsonNode blockCodeNode = block.get("code");
                                        if (boundaryTypeNode != null && blockCodeNode !=null && "Block".equals(boundaryTypeNode.asText())) {
                                            String blockCode = blockCodeNode.asText();
                                            Boundary boundary = Boundary.builder().state(stateCode).district(districtCode).build();
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

    /**
     * Every boundary code mapped to itself plus everything beneath it.
     *
     * A jurisdiction is granted at one level but governs the whole subtree: a role held at a State
     * covers that state's districts, blocks and facilities. Incidents are indexed with their own
     * level's code ({@code Data.incident.boundary.districtCode} and friends), so scoping by a state
     * code alone would match nothing - the scope has to be expanded to the codes that actually
     * appear on the documents.
     *
     * Expansion is by the hierarchy, never by string prefix. Codes look nested ("India_Gujarat"),
     * which makes prefix matching tempting and wrong: it would make "India_Kar" swallow
     * "India_Karnataka".
     *
     * Fetched from Country down, so a country-level grant - what the old `India` fallback produced,
     * and what the grandfathered users hold - expands to everything rather than matching nothing.
     */
    @Cacheable(value = "boundaryDescendants")
    public Map<String, Set<String>> getDescendantCodes() {
        log.info("Fetching the boundary tree for descendant expansion");
        String params = "?boundaryType=Country&includeChildren=true&tenantId=in&hierarchyType=SELCO";
        StringBuilder uri = new StringBuilder();
        uri.append(boundaryHost).append(boundaryUrl).append(params);
        try {
            Object response = restTemplate.postForObject(uri.toString(), new RequestInfo(), Map.class);
            if (response == null) {
                log.error("Boundary service returned null while building the descendant map");
                return Collections.emptyMap();
            }
            Map<String, Set<String>> descendants = new HashMap<>();
            JsonNode root = objectMapper.readTree(objectMapper.writeValueAsString(response));
            JsonNode tenantBoundaryArray = root.get("TenantBoundary");
            if (tenantBoundaryArray != null && tenantBoundaryArray.isArray()) {
                for (JsonNode tenantBoundary : tenantBoundaryArray) {
                    JsonNode boundaryArray = tenantBoundary.get("boundary");
                    if (boundaryArray != null && boundaryArray.isArray()) {
                        for (JsonNode node : boundaryArray) {
                            collectDescendants(node, descendants);
                        }
                    }
                }
            }
            log.info("Descendant map built - boundaryCount: {}", descendants.size());
            return descendants;
        } catch (Exception e) {
            // Caller fails open: an unscoped inbox beats an empty one, and this matches how the
            // service already treats a failed workflow lookup.
            log.error("Error building the boundary descendant map; scoping will fall back to open", e);
            return Collections.emptyMap();
        }
    }

    /** Depth-first, returning this node's own code plus every code beneath it. */
    private static Set<String> collectDescendants(JsonNode node, Map<String, Set<String>> into) {
        Set<String> codes = new HashSet<>();
        if (node == null) {
            return codes;
        }
        JsonNode codeNode = node.get("code");
        if (codeNode != null) {
            codes.add(codeNode.asText());
        }
        JsonNode children = node.get("children");
        if (children != null && children.isArray()) {
            for (JsonNode child : children) {
                codes.addAll(collectDescendants(child, into));
            }
        }
        if (codeNode != null) {
            into.put(codeNode.asText(), codes);
        }
        return codes;
    }
}

