package db.migration;

import org.flywaydb.core.api.migration.BaseJavaMigration;
import org.flywaydb.core.api.migration.Context;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;
import java.util.List;
import java.util.Arrays;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.ArrayNode;

public class V20250731113430_update_workflow extends BaseJavaMigration {

    private static final String HOST_URL = System.getenv("EGOV_WORKFLOW_V2_PORT");
    private static final String BASE_URL =  HOST_URL + "/egov-workflow-v2/egov-wf";

    private static final List<String> TENANT_IDS = Arrays.asList("pg", "gj", "or","sk", "nl", "as", "mz", "mn", "ml", "mh");
    private static final List<String> BUSINESS_SERVICES = Arrays.asList("Incident_High", "Incident_Low", "Incident_Medium");

    private static final ObjectMapper mapper = new ObjectMapper();

    @Override
    public void migrate(Context context) throws Exception {
        for (String tenantId : TENANT_IDS) {
            for (String service : BUSINESS_SERVICES) {
                processBusinessService(tenantId, service);
            }
        }
    }

    private void processBusinessService(String tenantId, String serviceName) throws Exception {
        JsonNode bsObject = fetchBusinessService(tenantId, serviceName);
        if (bsObject == null) return;

        ArrayNode states = (ArrayNode) bsObject.get("states");
        boolean stateExists = false;

        for (JsonNode state : states) {
            if ("CLOSEDAFTERREJECTION".equals(state.get("state").asText())) {
                stateExists = true;
                break;
            }
        }

        if (!stateExists) {
            ObjectNode newState = mapper.createObjectNode();
            newState.put("sla", (String) null);
            newState.put("state", "CLOSEDAFTERREJECTION");
            newState.put("applicationStatus", "CLOSEDAFTERREJECTION");
            newState.put("docUploadRequired", false);
            newState.put("isStartState", false);
            newState.put("isTerminateState", true);
            newState.put("isStateUpdatable", false);
            newState.set("actions", mapper.createArrayNode());
            states.add(newState);

            updateBusinessService(bsObject);
            System.out.println("[INFO] Added CLOSEDAFTERREJECTION for " + serviceName + " in " + tenantId);
        }

        // Fetch updated service
        bsObject = fetchBusinessService(tenantId, serviceName);
        if (bsObject == null) return;
        states = (ArrayNode) bsObject.get("states");

        String rejectionUuid = null;
        for (JsonNode state : states) {
            if ("CLOSEDAFTERREJECTION".equals(state.get("state").asText()) && state.has("uuid")) {
                rejectionUuid = state.get("uuid").asText();
                break;
            }
        }

        if (rejectionUuid == null) {
            System.err.println("[ERROR] CLOSEDAFTERREJECTION UUID missing for " + serviceName + " (" + tenantId + ")");
            return;
        }

        boolean modified = false;
        for (JsonNode state : states) {
            if ("REJECTED".equals(state.get("state").asText()) && state.has("actions")) {
                ArrayNode actions = (ArrayNode) state.get("actions");
                for (JsonNode action : actions) {
                    if ("CLOSE".equals(action.get("action").asText())) {
                        ((ObjectNode) action).put("nextState", rejectionUuid);
                        modified = true;
                        System.out.println("[INFO] Updated CLOSE action nextState for " + serviceName + " (" + tenantId + ")");
                    }
                }
            }
        }

        if (modified) {
            updateBusinessService(bsObject);
            System.out.println("[SUCCESS] Final update done for " + serviceName + " in " + tenantId);
        } else {
            System.out.println("[WARN] No CLOSE action found in REJECTED for " + serviceName + " (" + tenantId + ")");
        }
    }

    private JsonNode fetchBusinessService(String tenantId, String serviceName) throws Exception {
        String url = BASE_URL + "/businessservice/_search?tenantId=" + tenantId + "&businessServices=" + serviceName;
        ObjectNode payload = getRequestInfo();

        JsonNode response = post(url, payload);
        ArrayNode services = (ArrayNode) response.get("BusinessServices");

        if (services == null || services.isEmpty()) {
            System.err.println("[WARN] No BusinessService found for " + serviceName + " in " + tenantId);
            return null;
        }

        return services.get(0);
    }

    private void updateBusinessService(JsonNode bsObject) throws Exception {
        String url = BASE_URL + "/businessservice/_update";
        ObjectNode payload = getRequestInfo();
        ArrayNode serviceArray = mapper.createArrayNode();
        serviceArray.add(bsObject);
        payload.set("BusinessServices", serviceArray);
        post(url, payload);
    }

    private ObjectNode getRequestInfo() {
        ObjectNode requestInfo = mapper.createObjectNode();
        requestInfo.put("apiId", "Rainmaker");
        requestInfo.put("action", "");
        requestInfo.put("did", 1);
        requestInfo.put("key", "");
        requestInfo.put("msgId", "20170310130900|en_IN");
        requestInfo.put("requesterId", "");
        requestInfo.put("ts", System.currentTimeMillis());
        requestInfo.put("ver", ".01");
        requestInfo.put("authToken", "f93e2db5-b153-49d3-b653-014c5368791e");

        ObjectNode userInfo = mapper.createObjectNode();
        userInfo.put("uuid", "c2b18504-c5d5-4edc-b6eb-a3a913c17add");
        userInfo.put("userName", "9686987977");
        userInfo.put("name", "One");
        userInfo.put("gender", "Male");
        userInfo.put("mobileNumber", "9686987977");
        userInfo.put("type", "EMPLOYEE");
        userInfo.put("tenantId", "pb");

        ArrayNode roles = mapper.createArrayNode();
        roles.add(mapper.createObjectNode().put("id", (String) null).put("name", "Citizen").put("code", "CITIZEN"));
        roles.add(mapper.createObjectNode().put("id", (String) null).put("name", "TL Approver").put("code", "TL_APPROVER"));
        userInfo.set("roles", roles);

        requestInfo.set("userInfo", userInfo);

        ObjectNode wrapper = mapper.createObjectNode();
        wrapper.set("RequestInfo", requestInfo);
        return wrapper;
    }

    private JsonNode post(String urlString, JsonNode jsonBody) throws Exception {
        URL url = new URL(urlString);
        HttpURLConnection con = (HttpURLConnection) url.openConnection();
        con.setRequestMethod("POST");
        con.setRequestProperty("Content-Type", "application/json");
        con.setDoOutput(true);

        String json = mapper.writeValueAsString(jsonBody);
        try (OutputStream os = con.getOutputStream()) {
            os.write(json.getBytes(StandardCharsets.UTF_8));
        }

        Scanner scanner = new Scanner(con.getInputStream(), StandardCharsets.UTF_8.name());
        String response = scanner.useDelimiter("\\A").next();
        return mapper.readTree(response);
    }
}
