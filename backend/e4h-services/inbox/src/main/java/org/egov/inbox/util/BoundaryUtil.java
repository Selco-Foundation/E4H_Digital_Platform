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
            String jsonString = objectMapper.writeValueAsString(response);
            listBlock = extractBlockToDistrictMapping(jsonString);
        }catch(Exception e) {
            throw new CustomException("CONFIG_ERROR","Error in fetching inbox query boundary ");
        }

        return listBlock;
    }

    public static Map<String, Boundary> extractBlockToDistrictMapping(String json) throws IOException {
        Map<String, Boundary> blockToDistrictMap = new HashMap<>();
        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode root = objectMapper.readTree(json);

        JsonNode tenantBoundaryArray = root.get("TenantBoundary");
        if (tenantBoundaryArray != null && tenantBoundaryArray.isArray()) {
            for (JsonNode tenantBoundary : tenantBoundaryArray) {
                JsonNode boundaryArray = tenantBoundary.get("boundary");
                if (boundaryArray != null && boundaryArray.isArray()) {
                    for (JsonNode state : boundaryArray) {
                        JsonNode districts = state.get("children");
                        String stateCode = state.get("code").asText();
                        if (districts != null && districts.isArray()) {
                            for (JsonNode district : districts) {
                                String districtCode = district.get("code").asText();
                                JsonNode blocks = district.get("children");
                                if (blocks != null && blocks.isArray()) {
                                    for (JsonNode block : blocks) {
                                        if ("Block".equals(block.get("boundaryType").asText())) {
                                            String blockCode = block.get("code").asText();
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

