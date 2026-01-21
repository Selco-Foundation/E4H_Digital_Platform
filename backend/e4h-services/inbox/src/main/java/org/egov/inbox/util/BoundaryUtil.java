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

import static org.egov.inbox.util.InboxConstants.*;

import lombok.extern.slf4j.Slf4j;

@Slf4j
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
        log.trace("Method invoked: getBoundaryByCode");
        log.info("Fetching boundary configuration by code");
        Map<String, Boundary> listBlock = null;
        String params = "?boundaryType=State&includeChildren=true&tenantId=in&hierarchyType=SELCO";
        StringBuilder uri = new StringBuilder();
        uri.append(boundaryHost).append(boundaryUrl).append(params);
        log.debug("Calling boundary service - URI: {}", uri.toString());
        RequestInfo requestInfo = new RequestInfo();
        Object response = null;
        try {
            response = restTemplate.postForObject(uri.toString(), requestInfo, Map.class);
            if (response == null) {
                log.error("Boundary service returned null response");
                throw new CustomException("CONFIG_ERROR", "Boundary service returned null response");
            }
            log.debug("Boundary service response received");
            String jsonString = objectMapper.writeValueAsString(response);
            log.debug("Extracting block to district mapping from boundary response");
            listBlock = extractBlockToDistrictMapping(jsonString);
            log.info("Boundary configuration retrieved successfully - boundaryCount: {}", 
                    listBlock != null ? listBlock.size() : 0);
        }catch(Exception e) {
            log.error("Error in fetching boundary configuration", e);
            throw new CustomException("CONFIG_ERROR","Error in fetching inbox query boundary ");
        }

        return listBlock;
    }

    public static Map<String, Boundary> extractBlockToDistrictMapping(String json) throws IOException {
        log.trace("Method invoked: extractBlockToDistrictMapping");
        Map<String, Boundary> blockToDistrictMap = new HashMap<>();
        if (json == null || json.trim().isEmpty()) {
            log.error("JSON input is null or empty");
            throw new IllegalArgumentException("JSON input cannot be null or empty");
        }
        log.debug("Parsing boundary JSON - jsonLength: {}", json.length());
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

        log.debug("Block to district mapping extracted - mappingCount: {}", blockToDistrictMap.size());
        return blockToDistrictMap;
    }
}

