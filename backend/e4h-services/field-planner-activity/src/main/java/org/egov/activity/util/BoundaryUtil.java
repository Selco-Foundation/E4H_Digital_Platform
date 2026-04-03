package org.egov.activity.util;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.egov.activity.web.models.Boundary;
import org.egov.common.contract.request.RequestInfo;
import org.egov.tracer.model.CustomException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
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

    @Autowired
    private ObjectMapper objectMapper;

//    @Cacheable(value="boundaryConfiguration")
    public Map<String, Boundary> getBoundaryByCode() {
        log.trace("getBoundaryByCode method invoked");
        log.debug("Fetching boundaries from boundary service");
        Map<String, Boundary> listBlock = null;
        String params = "?boundaryType=State&includeChildren=true&tenantId=in&hierarchyType=SELCO";
        StringBuilder uri = new StringBuilder();
        uri.append(boundaryHost).append(boundaryUrl).append(params);
        RequestInfo requestInfo = new RequestInfo();
        Object response = null;
        try {
            response = restTemplate.postForObject(uri.toString(), requestInfo, Map.class);
            if (response == null) {
              log.error("Boundary service returned null response");
              throw new CustomException("CONFIG_ERROR", "Boundary service returned null response");
            }
            String jsonString = objectMapper.writeValueAsString(response);
            listBlock = extractBlockToDistrictMapping(jsonString);
            int boundaryCount = listBlock != null ? listBlock.size() : 0;
            log.debug("Successfully loaded {} boundaries from boundary service", boundaryCount);
        }catch(Exception e) {
            log.error("Error fetching boundaries from boundary service", e);
            throw new CustomException("CONFIG_ERROR","Error in fetching inbox query boundary ");
        }

        return listBlock;
    }

    public static Map<String, Boundary> extractBlockToDistrictMapping(String json) throws IOException {
        log.trace("extractBlockToDistrictMapping method invoked");
        Map<String, Boundary> blockToDistrictMap = new HashMap<>();
        if (json == null || json.trim().isEmpty()) {
            log.error("JSON input is null or empty");
            throw new IllegalArgumentException("JSON input cannot be null or empty");
        }
        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode root = objectMapper.readTree(json);
        log.debug("Parsing boundary JSON structure");

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
}

