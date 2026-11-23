package db.migration.main;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.extern.slf4j.Slf4j;
import org.flywaydb.core.api.migration.BaseJavaMigration;
import org.flywaydb.core.api.migration.Context;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.Duration;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * Finds HRMS employees with any of the complaint workflow roles and inserts {@code eg_org_user} rows linking each
 * distinct user to an existing platform organisation id ({@link #EXISTING_PLATFORM_ORG_ID} — edit per environment if needed).
 * <p>
 * A user who appears under more than one role (e.g. COMPLAINANT and COMPLAINT_FACILITATOR_1) is still linked only once:
 * all role searches merge into a {@link java.util.LinkedHashSet} keyed by {@code user.uuid}.
 * <p>
 * Expects {@code eg_org_user} on the Flyway datasource (organisation / vendor-registry schema).
 */
@Slf4j
public class V20260415103000__LinkComplaintRoleUsersToPlatformOrg extends BaseJavaMigration {

    /** Existing vendor organisation id (uuid); replace with the correct org for this deployment. */
    private static final String EXISTING_PLATFORM_ORG_ID = "c9661407-5594-4020-b1f4-95af1be22d8b";

    private static final String HRMS_SEARCH_BASE =  System.getenv("EGOV_HRMS_HOST") + "egov-hrms/employees/_search";

    private static final String HRMS_AUTH_HEADER_VALUE = "Bearer your-auth-token";

    private static final String HRMS_REQUEST_INFO_AUTH_TOKEN = "Bearer your-auth-token";

    private static final String SYSTEM_USER = "00000000-0000-0000-0000-000000000001";

    private static final String TENANT_IN = "in";

    private static final int HRMS_PAGE_SIZE = 200;

    private static final Duration HTTP_TIMEOUT = Duration.ofMinutes(2);

    private static final List<String> HRMS_ROLES = List.of(
            "COMPLAINT_FACILITATOR_2",
            "COMPLAINT_ASSESSOR",
            "COMPLAINT_FACILITATOR_1",
            "STATE_POC",
            "CENTRAL_POC",
            "CENTRAL_ONM_PROJECT_MANAGER",
            "SENIOR_PROGRAM_MANAGER"
    );

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public boolean canExecuteInTransaction() {
        return false;
    }

    @Override
    public void migrate(Context context) throws Exception {
        HttpClient httpClient = HttpClient.newBuilder().connectTimeout(HTTP_TIMEOUT).build();
        Connection connection = context.getConnection();

        String platformOrgId = EXISTING_PLATFORM_ORG_ID.trim();
        if (platformOrgId.isBlank()) {
            throw new IllegalStateException("EXISTING_PLATFORM_ORG_ID must be set to the target organisation uuid.");
        }
        log.info("Linking complaint-role users to existing organisation id={}", platformOrgId);

        // One eg_org_user per HRMS user.uuid; same person matched by several roles is deduped here.
        Set<String> userUuids = new LinkedHashSet<>();
        for (String role : HRMS_ROLES) {
            fetchEmployeeUuidsForRole(httpClient, role, userUuids);
        }
        log.info("Distinct HRMS user uuids across roles: {}", userUuids.size());

        int inserted = 0;
        for (String userUuid : userUuids) {
            if (orgUserLinkExists(connection, userUuid, platformOrgId)) {
                continue;
            }
            insertOrgUser(connection, platformOrgId, userUuid);
            inserted++;
        }
        log.info("PLATFORM org user links migration finished; inserted {} eg_org_user row(s).", inserted);
    }

    private void fetchEmployeeUuidsForRole(HttpClient httpClient, String role, Set<String> outUuids) throws Exception {
        int offset = 0;
        while (true) {
            String url = buildHrmsSearchUrl(role, offset);
            String hrmsBody = buildHrmsSearchBody();
            JsonNode hrmsRoot = postJson(httpClient, url, hrmsBody, HRMS_AUTH_HEADER_VALUE);
            JsonNode employees = hrmsRoot.get("Employees");
            if (employees == null || !employees.isArray() || employees.isEmpty()) {
                break;
            }
            int batch = 0;
            for (JsonNode employee : employees) {
                JsonNode user = employee.get("user");
                if (user == null || user.isNull()) {
                    continue;
                }
                String uuid = textOrNull(user, "uuid");
                if (uuid != null && !uuid.isBlank()) {
                    // Set.add: false when uuid already seen from another role → one row per user.
                    if (outUuids.add(uuid)) {
                        batch++;
                    }
                }
            }
            log.info("HRMS role {} offset {}: {} employees in page, {} new distinct uuids", role, offset, employees.size(), batch);
            if (employees.size() < HRMS_PAGE_SIZE) {
                break;
            }
            offset += HRMS_PAGE_SIZE;
        }
    }

    private static String buildHrmsSearchUrl(String role, int offset) throws Exception {
        String encRole = URLEncoder.encode(role, StandardCharsets.UTF_8);
        return HRMS_SEARCH_BASE + "?tenantId=" + TENANT_IN + "&isActive=true&roles=" + encRole
                + "&limit=" + HRMS_PAGE_SIZE + "&offset=" + offset;
    }

    private static boolean orgUserLinkExists(Connection connection, String userUuid, String organizationId) throws Exception {
        String sql = "SELECT 1 FROM eg_org_user WHERE userid = ? AND organizationid = ? AND tenantid = ? AND isdeleted = false LIMIT 1";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, userUuid);
            ps.setString(2, organizationId);
            ps.setString(3, TENANT_IN);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    private void insertOrgUser(Connection connection, String organizationId, String userUuid) throws Exception {
        long now = System.currentTimeMillis();
        String sql = "INSERT INTO eg_org_user (id, tenantid, organizationid, userid, additionaldetails, "
                + "createdby, lastmodifiedby, createdtime, lastmodifiedtime, isdeleted) "
                + "VALUES (?, ?, ?, ?, NULL, ?, ?, ?, ?, false)";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, UUID.randomUUID().toString());
            ps.setString(2, TENANT_IN);
            ps.setString(3, organizationId);
            ps.setString(4, userUuid);
            ps.setString(5, SYSTEM_USER);
            ps.setString(6, SYSTEM_USER);
            ps.setLong(7, now);
            ps.setLong(8, now);
            ps.executeUpdate();
        }
    }

    private JsonNode postJson(HttpClient httpClient, String url, String jsonBody, String authorizationHeader) throws Exception {
        HttpRequest.Builder b = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .timeout(HTTP_TIMEOUT)
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(jsonBody, StandardCharsets.UTF_8));
        if (authorizationHeader != null && !authorizationHeader.isBlank()) {
            b.header("Authorization", authorizationHeader);
        }
        HttpResponse<String> response = httpClient.send(b.build(), HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
        int code = response.statusCode();
        if (code < 200 || code >= 300) {
            throw new IllegalStateException("HTTP " + code + " from " + url + " body=" + response.body());
        }
        return objectMapper.readTree(response.body());
    }

    private static String textOrNull(JsonNode node, String field) {
        JsonNode v = node.get(field);
        if (v == null || v.isNull()) {
            return null;
        }
        return v.asText(null);
    }

    private String buildHrmsSearchBody() throws Exception {
        ObjectNode root = objectMapper.createObjectNode();
        ObjectNode ri = root.putObject("RequestInfo");
        ri.put("apiId", "project-api");
        ri.put("ver", "1.0");
        ri.put("ts", 1715510400000L);
        ri.put("action", "create");
        ri.put("did", "device-id");
        ri.put("key", "api-key");
        ri.put("msgId", "msg-001");
        ri.put("authToken", HRMS_REQUEST_INFO_AUTH_TOKEN);
        ri.put("correlationId", "corr-id-001");
        ri.putNull("plainAccessRequest");
        ObjectNode userInfo = ri.putObject("userInfo");
        userInfo.put("uuid", "user-uuid");
        userInfo.put("type", "EMPLOYEE");
        userInfo.put("userName", "secret");
        userInfo.put("tenantId", "pg");
        ArrayNode roles = userInfo.putArray("roles");
        ObjectNode role = roles.addObject();
        role.put("name", "EMPLOYEE");
        return objectMapper.writeValueAsString(root);
    }
}
