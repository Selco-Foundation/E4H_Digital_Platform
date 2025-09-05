package db.migration.main;

import org.flywaydb.core.api.migration.BaseJavaMigration;
import org.flywaydb.core.api.migration.Context;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class V20250905131000__search_business_service extends BaseJavaMigration {

    private static final Logger log = LoggerFactory.getLogger(V20250905131000__search_business_service.class);

    private static final String HOST_URL = System.getenv("EGOV_WORKFLOW_HOST");
    private static final String BASE_URL = HOST_URL + "/egov-workflow-v2/egov-wf";
    private static final String BUSINESS_SERVICES_LITERAL = "BusinessServices";

    private static final ObjectMapper mapper = new ObjectMapper();

    @Override
    public void migrate(Context context) throws Exception {
        if (HOST_URL == null || HOST_URL.isBlank()) {
            throw new IllegalStateException("EGOV_WORKFLOW_HOST environment variable is not set");
        }

        // Just test one tenant and one service for connectivity
        String tenantId = "pg";
        String service = "Incident_High";

        log.info("Testing search for {} in tenant {}", service, tenantId);

        JsonNode response = fetchBusinessService(tenantId, service);

        if (response == null || !response.has(BUSINESS_SERVICES_LITERAL)) {
            log.warn("No BusinessService found for {} in {}", service, tenantId);
        } else {
            log.info("BusinessService search response: {}", response.toPrettyString());
        }
    }

    private JsonNode fetchBusinessService(String tenantId, String serviceName) throws Exception {
        String url = BASE_URL
                + "/businessservice/_search?tenantId=" + URLEncoder.encode(tenantId, StandardCharsets.UTF_8)
                + "&businessServices=" + URLEncoder.encode(serviceName, StandardCharsets.UTF_8);

        ObjectNode payload = getRequestInfo();
        return post(url, payload);
    }

    private ObjectNode getRequestInfo() {
        ObjectNode requestInfo = mapper.createObjectNode();
        requestInfo.put("apiId", "Rainmaker");
        requestInfo.put("ver", ".01");
        requestInfo.put("ts", System.currentTimeMillis());
        requestInfo.put("action", "");
        requestInfo.put("did", 1);
        requestInfo.put("msgId", "20170310130900|en_IN");
        requestInfo.put("authToken", "dummy-token"); // Replace with valid token if required

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
