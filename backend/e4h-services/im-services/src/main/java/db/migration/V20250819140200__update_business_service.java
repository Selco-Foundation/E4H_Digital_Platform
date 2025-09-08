package db.migration;

import org.flywaydb.core.api.migration.BaseJavaMigration;
import org.flywaydb.core.api.migration.Context;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Arrays;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.ArrayNode;

public class V20250819140200__update_business_service extends BaseJavaMigration {

  private static final String HOST_URL = System.getenv("EGOV_WORKFLOW_HOST");

  private static final String BASE_URL =  HOST_URL + "/egov-workflow-v2/egov-wf";

  private static final String BUSINESS_SERVICES_LITERAL = "BusinessServices";

  private static final Logger log = LoggerFactory.getLogger(V20250819140200__update_business_service.class);

  private static final List<String> TENANT_IDS = Arrays.asList("pg", "gj", "or","sk", "nl", "as", "mz", "mn", "ml", "mh");
  private static final List<String> BUSINESS_SERVICES = Arrays.asList("Incident_High", "Incident_Low", "Incident_Medium");

  private static final ObjectMapper mapper = new ObjectMapper();

  @Override
  public void migrate(Context context) throws Exception {

    // Fail fast if the workflow host isn’t configured

    if (HOST_URL == null || HOST_URL.isBlank()) {
      throw new IllegalStateException("EGOV_WORKFLOW_HOST environment variable is not set");
    }
    for (String tenantId : TENANT_IDS) {
      for (String service : BUSINESS_SERVICES) {
        try {
          processBusinessService(tenantId, service);
        } catch (Exception e) {
          log.error("Migration failed for tenant '{}', service '{}': {}", tenantId, service, e.getMessage());
          }
      }
    }
  }

  private void processBusinessService(String tenantId, String serviceName) throws Exception {
    JsonNode bsObject = fetchBusinessService(tenantId, serviceName);
    if (bsObject == null) return;

    bsObject = ensureCloseAfterRejectionState(bsObject, tenantId, serviceName);
    if (bsObject == null) return;

    String rejectionUuid = findRejectionUuid(bsObject, serviceName, tenantId);
    if (rejectionUuid == null) return;

    if (updateCloseAction(bsObject, rejectionUuid, serviceName, tenantId)) {
      updateBusinessService(bsObject);
      log.info("Final update done for {} in {}", serviceName, tenantId);
    }
  }

  private JsonNode ensureCloseAfterRejectionState(JsonNode bsObject, String tenantId, String serviceName) throws Exception {
    ArrayNode states = (ArrayNode) bsObject.get("states");
    if (hasCloseAfterRejectionState(states)) return bsObject;

    ObjectNode newState = createCloseAfterRejectionState();
    states.add(newState);

    JsonNode response = updateBusinessService(bsObject);
    log.info("Added CLOSEDAFTERREJECTION for {} in {}", serviceName, tenantId);

    return response != null ? response : fetchBusinessService(tenantId, serviceName);
  }

  private boolean hasCloseAfterRejectionState(ArrayNode states) {
    for (JsonNode state : states) {
      if ("CLOSEDAFTERREJECTION".equals(state.get("state").asText())) {
        return true;
      }
    }
    return false;
  }

  private ObjectNode createCloseAfterRejectionState() {
    ObjectNode newState = mapper.createObjectNode();
    newState.put("sla", (String) null);
    newState.put("state", "CLOSEDAFTERREJECTION");
    newState.put("applicationStatus", "CLOSEDAFTERREJECTION");
    newState.put("docUploadRequired", false);
    newState.put("isStartState", false);
    newState.put("isTerminateState", true);
    newState.put("isStateUpdatable", false);
    newState.set("actions", mapper.createArrayNode());
    return newState;
  }

  private String findRejectionUuid(JsonNode bsObject, String serviceName, String tenantId) {
    ArrayNode states = (ArrayNode) bsObject.get("states");
    for (JsonNode state : states) {
      if ("CLOSEDAFTERREJECTION".equals(state.get("state").asText()) && state.has("uuid")) {
        return state.get("uuid").asText();
      }
    }
    log.error("CLOSEDAFTERREJECTION UUID missing for {} ({})", serviceName, tenantId);
    return null;
  }

  private boolean updateCloseAction(JsonNode bsObject, String rejectionUuid, String serviceName, String tenantId) {
    ArrayNode states = (ArrayNode) bsObject.get("states");
    boolean modified = false;

    for (JsonNode state : states) {
      if ("REJECTED".equals(state.get("state").asText())) {
        modified |= updateCloseActionInState((ArrayNode) state.get("actions"), rejectionUuid, serviceName, tenantId);
      }
    }

    if (!modified) {
      log.warn("No CLOSE action found in REJECTED for {} ({})", serviceName, tenantId);
    }
    return modified;
  }

  private boolean updateCloseActionInState(ArrayNode actions, String rejectionUuid, String serviceName, String tenantId) {
    boolean modified = false;
    for (JsonNode action : actions) {
      if ("CLOSE".equals(action.get("action").asText())) {
        ((ObjectNode) action).put("nextState", rejectionUuid);
        modified = true;
        log.info("Updated CLOSE action nextState for {} ({})", serviceName, tenantId);
      }
    }
    return modified;
  }

  private JsonNode fetchBusinessService(String tenantId, String serviceName) throws Exception {
    String url = BASE_URL
            + "/businessservice/_search?tenantId=" + URLEncoder.encode(tenantId, StandardCharsets.UTF_8)
            + "&businessServices=" + URLEncoder.encode(serviceName, StandardCharsets.UTF_8);
    ObjectNode payload = getRequestInfo();

    JsonNode response = post(url, payload);

    if (response == null || !response.has(BUSINESS_SERVICES_LITERAL)) {
      log.warn("Search returned no BusinessServices node for {} in {}", serviceName, tenantId);
      return null;
    }

    ArrayNode services = (ArrayNode) response.get(BUSINESS_SERVICES_LITERAL);

    if (services == null || services.isEmpty()) {
      log.warn("No BusinessService found for {} in {}", serviceName, tenantId);
      return null;
    }

    return services.get(0);
  }

  private JsonNode updateBusinessService(JsonNode bsObject) throws Exception {
    String url = BASE_URL + "/businessservice/_update";
    ObjectNode payload = getRequestInfo();
    ArrayNode serviceArray = mapper.createArrayNode();
    serviceArray.add(bsObject);
    payload.set(BUSINESS_SERVICES_LITERAL, serviceArray);
    JsonNode response = post(url, payload);
    if (response == null || !response.has(BUSINESS_SERVICES_LITERAL)) {
      log.warn(" updateBusinessService: response missing BusinessServices");
      return null;
    }
    ArrayNode services = (ArrayNode) response.get(BUSINESS_SERVICES_LITERAL);
    if (services == null || services.isEmpty()) {
      log.warn("No BusinessService found for {} in updateBusinessService", services);
      return null;
    }
    return services.get(0);
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
    HttpURLConnection con = (HttpURLConnection) new URL(urlString).openConnection();
    con.setRequestMethod("POST");
    con.setRequestProperty("Content-Type", "application/json");
    con.setRequestProperty("Accept", "application/json");
    con.setConnectTimeout(10_000);
    con.setReadTimeout(30_000);
    con.setDoOutput(true);

    String json = mapper.writeValueAsString(jsonBody);
    try (OutputStream os = con.getOutputStream()) {
      os.write(json.getBytes(StandardCharsets.UTF_8));
    }

    int status = con.getResponseCode();
    java.io.InputStream stream = (status >= 200 && status < 300) ? con.getInputStream() : con.getErrorStream();
    if (stream == null) {
      throw new IllegalStateException("HTTP " + status + " with empty body for " + urlString);
    }
    try (java.util.Scanner scanner = new java.util.Scanner(stream, StandardCharsets.UTF_8)) {
      String response = scanner.useDelimiter("\\A").hasNext() ? scanner.next() : "";
      if (status < 200 || status >= 300) {
        throw new IllegalStateException("HTTP " + status + " from " + urlString + ": " + response);
      }
      return mapper.readTree(response);
      } finally {
        con.disconnect();
      }
  }
}