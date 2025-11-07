package db.migration.main;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.egov.common.contract.request.RequestInfo;
import org.egov.common.contract.request.Role;
import org.egov.common.contract.request.User;
import org.egov.im.web.models.boundary.Boundary;
import org.egov.im.web.models.boundary.BoundaryCreateRequest;
import org.egov.im.web.models.boundary.BoundaryRelation;
import org.egov.im.web.models.boundary.BoundaryRelationshipRequest;
import org.flywaydb.core.api.migration.BaseJavaMigration;
import org.flywaydb.core.api.migration.Context;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
public class V20251030175200__districts_blocks_migration extends BaseJavaMigration {

    private static final Map<String, String> TENANT_TO_STATE;
    static {
        Map<String, String> m = new LinkedHashMap<>();
        m.put("as", "Assam");
        m.put("ml", "Meghalaya");
        m.put("mn", "Manipur");
        m.put("nl", "Nagaland");
        m.put("sk", "Sikkim");
        m.put("gj", "Gujarat");
        m.put("mz", "Mizoram");
        m.put("or", "Odisha");
        m.put("pg", "Karnataka");
        m.put("mh", "Maharashtra");
        TENANT_TO_STATE = Collections.unmodifiableMap(m);
    }
    private static final int BATCH_SIZE = 250;

    @Override
    public void migrate(Context context) throws Exception {
        // This migration will:
        // 1) Fetch tenants and geo masters (state/district/block) from MDMS Incident module
        // 2) Create boundaries for state, districts, and blocks
        // 3) Create parent-child relationships (state -> district -> block)

        RestTemplate restTemplate = new RestTemplate();
        ObjectMapper objectMapper = new ObjectMapper();

        String mdmsHost = getEnvOrDefault("EGOV_MDMS_HOST", "http://localhost:8094");
        String mdmsSearchEndpoint = getEnvOrDefault("EGOV_MDMS_SEARCH_ENDPOINT", "/egov-mdms-service/v1/_search");

        String boundaryHost = getEnvOrDefault("EGOV_BOUNDARY_HOST", "http://localhost:8082");
        String boundaryCreatePath = getEnvOrDefault("EGOV_BOUNDARY_CREATE_PATH", "/boundary-service/boundary/_create");
        String boundaryRelationshipCreatePath = getEnvOrDefault("EGOV_BOUNDARY_REL_CREATE_PATH", "/boundary-service/boundary-relationships/_create");

        // Build RequestInfo similar to typical DIGIT service calls
        RequestInfo requestInfo = buildRequestInfo();

        //Create Country Boundary and Boundary Relationship
        createBoundariesInBatches(restTemplate, objectMapper, boundaryHost + boundaryCreatePath, requestInfo, "in", Collections.singletonList("India"));
        createRelationship(
                restTemplate, objectMapper, boundaryHost + boundaryRelationshipCreatePath, requestInfo,
                "in", "India", "SELCO", "Country", null
        );

        // Iterate provided state tenantIds and mapped state names
        for (Map.Entry<String, String> entry : TENANT_TO_STATE.entrySet()) {
            String tenantId = entry.getKey();
            String stateName = entry.getValue();
            String stateCode = formatBoundaryCode(stateName);

            // Fetch District and Block from MDMS for this tenant
            Map<String, Object> mdmsReq = buildMdmsRequest(
                    requestInfo, tenantId, "Incident",
                    Arrays.asList("District", "Block")
            );
            JsonNode mdmsRes = post(objectMapper, restTemplate, mdmsHost + mdmsSearchEndpoint, mdmsReq);

            List<JsonNode> districts = getMasterArray(mdmsRes, "Incident", "District");
            List<JsonNode> blocks = getMasterArray(mdmsRes, "Incident", "Block");

            // Build district maps keyed by MDMS district.code
            Map<String, String> mdmsDistrictCodeToBoundaryCode = new HashMap<>();
            Map<String, String> mdmsDistrictCodeToName = new HashMap<>();
            for (JsonNode districtNode : districts) {
                String mdmsDistrictCode = pickField(districtNode, List.of("code"));
                String districtName = pickField(districtNode, List.of("name"));
                if (mdmsDistrictCode == null || mdmsDistrictCode.isEmpty() || districtName == null || districtName.isEmpty()) continue;
                String districtBoundaryCode = formatBoundaryCode(stateName + "_" + districtName);
                mdmsDistrictCodeToBoundaryCode.put(mdmsDistrictCode, districtBoundaryCode);
                mdmsDistrictCodeToName.put(mdmsDistrictCode, districtName);
            }

            // Prepare blocks grouped by parent district boundary code
            Map<String, List<String>> parentDistrictBoundaryCodeToBlockCodes = new HashMap<>();
            for (JsonNode blockNode : blocks) {
                String parentDistrictMdmsCode = pickField(blockNode, List.of("districtCode"));
                String blockName = pickField(blockNode, List.of("name"));
                if (parentDistrictMdmsCode == null || parentDistrictMdmsCode.isEmpty() || blockName == null || blockName.isEmpty()) continue;
                String districtNameForBlock = mdmsDistrictCodeToName.get(parentDistrictMdmsCode);
                String parentDistrictBoundaryCode = mdmsDistrictCodeToBoundaryCode.get(parentDistrictMdmsCode);
                if (districtNameForBlock == null || parentDistrictBoundaryCode == null) continue;
                String blockBoundaryCode = formatBoundaryCode(stateName + "_" + districtNameForBlock + "_" + blockName);
                parentDistrictBoundaryCodeToBlockCodes.computeIfAbsent(parentDistrictBoundaryCode, k -> new ArrayList<>()).add(blockBoundaryCode);
            }

            // Collect all boundary codes for bulk creation
            LinkedHashSet<String> codesToCreate = new LinkedHashSet<>();
            codesToCreate.add(stateCode);
            codesToCreate.addAll(mdmsDistrictCodeToBoundaryCode.values());
            for (List<String> list : parentDistrictBoundaryCodeToBlockCodes.values()) codesToCreate.addAll(list);

            // Bulk create boundaries in batches of BATCH_SIZE
            createBoundariesInBatches(restTemplate, objectMapper, boundaryHost + boundaryCreatePath, requestInfo, "in", new ArrayList<>(codesToCreate));

            // Create relationships after boundaries exist
            createRelationship(
                    restTemplate, objectMapper, boundaryHost + boundaryRelationshipCreatePath, requestInfo,
                    "in", stateCode, "SELCO", "State", "India"
            );

            for (String districtBoundaryCode : mdmsDistrictCodeToBoundaryCode.values()) {
                createRelationship(
                        restTemplate, objectMapper, boundaryHost + boundaryRelationshipCreatePath, requestInfo,
                        "in", districtBoundaryCode, "SELCO", "District", stateCode
                );
            }

            for (Map.Entry<String, List<String>> blockRelEntry : parentDistrictBoundaryCodeToBlockCodes.entrySet()) {
                String parentDistrictBoundaryCode = blockRelEntry.getKey();
                for (String blockCode : blockRelEntry.getValue()) {
                    createRelationship(
                            restTemplate, objectMapper, boundaryHost + boundaryRelationshipCreatePath, requestInfo,
                            "in", blockCode, "SELCO", "Block", parentDistrictBoundaryCode
                    );
                }
            }
        }
    }

    private static RequestInfo buildRequestInfo() {
        RequestInfo info = new RequestInfo();
        info.setApiId("Rainmaker");
        info.setVer(".01");
        info.setTs(System.currentTimeMillis());
        info.setAction("");
        info.setDid("1");
        info.setKey("");
        info.setMsgId("20170310130900|en_IN");
        String token = getEnvOrDefault("EGOV_AUTH_TOKEN", "f93e2db5-b153-49d3-b653-014c5368791e");
        if (!token.isEmpty()) info.setAuthToken(token);

        User user = new User();
        user.setUuid("c2b18504-c5d5-4edc-b6eb-a3a913c17add");
        user.setUserName("9686987977");
        user.setName("One");
        user.setMobileNumber("9686987977");
        user.setType("EMPLOYEE");
        user.setTenantId("pb");

        Role admin = new Role();
        admin.setCode("ADMIN");
        admin.setName("Admin");

        user.setRoles(List.of(admin));
        info.setUserInfo(user);
        return info;
    }

    private static void createBoundariesInBatches(
            RestTemplate restTemplate, ObjectMapper objectMapper, String url,
            RequestInfo requestInfo, String tenantId, List<String> codes
    ) {
        int total = codes.size();
        for (int i = 0; i < total; i += BATCH_SIZE) {
            int end = Math.min(i + BATCH_SIZE, total);
            List<String> batch = codes.subList(i, end);
            try {
                List<Boundary> boundaries = new ArrayList<>(batch.size());
                for (String code : batch) {
                    boundaries.add(Boundary.builder().tenantId(tenantId).code(code).build());
                }
                BoundaryCreateRequest req = BoundaryCreateRequest.builder()
                        .requestInfo(requestInfo)
                        .boundary(boundaries)
                        .build();

                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.APPLICATION_JSON);
                HttpEntity<String> httpEntity = new HttpEntity<>(objectMapper.writeValueAsString(req), headers);
                ResponseEntity<String> response = restTemplate.postForEntity(url, httpEntity, String.class);
                log.info("Bulk boundary create attempted tenant={} size={} status={}", tenantId, batch.size(), response.getStatusCode());
            } catch (Exception e) {
                log.error("Bulk boundary create failed tenant={} batchStart={} batchEnd={}", tenantId, i, end, e);
            }
        }
    }

    private static void createRelationship(
            RestTemplate restTemplate, ObjectMapper objectMapper, String url,
            RequestInfo requestInfo, String tenantId, String code,
            String hierarchyType, String boundaryType, String parentCode
    ) {
        try {
            BoundaryRelation relation = BoundaryRelation.builder()
                    .code(code)
                    .tenantId(tenantId)
                    .hierarchyType(hierarchyType)
                    .boundaryType(boundaryType)
                    .parent(parentCode)
                    .build();

            BoundaryRelationshipRequest req = BoundaryRelationshipRequest.builder()
                    .requestInfo(requestInfo)
                    .boundaryRelationship(relation)
                    .build();

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<String> httpEntity = new HttpEntity<>(objectMapper.writeValueAsString(req), headers);
            ResponseEntity<String> response = restTemplate.postForEntity(url, httpEntity, String.class);
            log.info("Boundary relationship created/attempted for code={} -> parent={} tenant={} status={}", code, parentCode, tenantId, response.getStatusCode());
        } catch (Exception e) {
            log.error("Failed to create boundary relationship code={} tenant={}", code, tenantId, e);
        }
    }

    private static Map<String, Object> buildMdmsRequest(
            RequestInfo requestInfo, String tenantId, String moduleName, List<String> masters
    ) {
        Map<String, Object> mdmsCriteriaReq = new HashMap<>();

        Map<String, Object> mdmsCriteria = new HashMap<>();
        mdmsCriteria.put("tenantId", (tenantId == null ? "" : tenantId));

        List<Map<String, Object>> masterDetails = masters.stream().distinct().map(name -> {
            Map<String, Object> md = new HashMap<>();
            md.put("name", name);
            return md;
        }).collect(Collectors.toList());

        Map<String, Object> moduleDetail = new HashMap<>();
        moduleDetail.put("moduleName", moduleName);
        moduleDetail.put("masterDetails", masterDetails);

        mdmsCriteria.put("moduleDetails", Collections.singletonList(moduleDetail));

        mdmsCriteriaReq.put("MdmsCriteria", mdmsCriteria);
        mdmsCriteriaReq.put("RequestInfo", requestInfo);

        return mdmsCriteriaReq;
    }

    private static JsonNode post(ObjectMapper objectMapper, RestTemplate restTemplate, String url, Object body) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<String> httpEntity = new HttpEntity<>(objectMapper.writeValueAsString(body), headers);
            ResponseEntity<String> response = restTemplate.postForEntity(url, httpEntity, String.class);
            if (!response.getStatusCode().is2xxSuccessful() || response.getBody() == null) {
                return objectMapper.createObjectNode();
            }
            return objectMapper.readTree(response.getBody());
        } catch (Exception e) {
            log.error("MDMS call failed: {}", url, e);
            return objectMapper.createObjectNode();
        }
    }

    private static List<JsonNode> getMasterArray(JsonNode mdmsRes, String module, String master) {
        if (mdmsRes == null || !mdmsRes.has("MdmsRes")) return Collections.emptyList();
        JsonNode moduleNode = mdmsRes.get("MdmsRes").get(module);
        if (moduleNode == null || moduleNode.isMissingNode()) return Collections.emptyList();
        JsonNode arr = moduleNode.get(master);
        if (arr == null || !arr.isArray()) return Collections.emptyList();
        List<JsonNode> list = new ArrayList<>();
        for (JsonNode n : arr) list.add(n);
        return list;
    }

    private static String pickField(JsonNode node, List<String> keys) {
        for (String k : keys) {
            if (node.has(k) && !node.get(k).isNull()) {
                String val = node.get(k).asText();
                if (val != null && !val.isEmpty()) return val;
            }
        }
        return null;
    }

    private static String formatBoundaryCode(String namePath) {
        // Build code like India_{state}_{district}_{block}
        String code = Arrays.stream(namePath.split("_"))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .collect(Collectors.joining("_"));
        if (!code.startsWith("India_")) code = "India_" + code;
        return code;
    }

    private static String getEnvOrDefault(String key, String def) {
        String v = System.getenv(key);
        return (v == null || v.isEmpty()) ? def : v;
    }
}
