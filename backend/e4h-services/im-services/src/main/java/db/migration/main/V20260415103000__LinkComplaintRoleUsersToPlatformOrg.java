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
 * Creates a new PLATFORM organisation via vendor API (global name), finds HRMS employees with any of the complaint
 * workflow roles, and inserts {@code eg_org_user} rows linking each distinct user to that org's id from the response.
 * <p>
 * A user who appears under more than one role (e.g. COMPLAINANT and COMPLAINT_FACILITATOR_1) is still linked only once:
 * all role searches merge into a {@link java.util.LinkedHashSet} keyed by {@code user.uuid}.
 * <p>
 * Expects {@code eg_org_user} on the Flyway datasource (organisation / vendor-registry schema).
 */
@Slf4j
public class V20260415103000__LinkComplaintRoleUsersToPlatformOrg extends BaseJavaMigration {

    /** Single org name for the shared platform org (edit if your environment needs another label). */
    private static final String GLOBAL_PLATFORM_ORG_NAME = "Selco Foundation";


    private static final String HRMS_SEARCH_BASE =  System.getenv("EGOV_HRMS_HOST") + "egov-hrms/employees/_search";

    private static final String VENDOR_ORG_CREATE_URL =
            System.getenv("EGOV_VENDOR_HOST") + "vendor/organisation/v1/_create";

    private static final String HRMS_AUTH_HEADER_VALUE = "Bearer your-auth-token";

    private static final String HRMS_REQUEST_INFO_AUTH_TOKEN = "Bearer your-auth-token";

    private static final String ORG_REQUEST_INFO_AUTH_TOKEN = "AUTH_TOKEN";

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

        String platformOrgId = createPlatformOrganisationAndGetId(httpClient);
        log.info("Created PLATFORM organisation id={} name={}", platformOrgId, GLOBAL_PLATFORM_ORG_NAME);

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

    /** Calls organisation {@code _create} once and returns the new org's {@code id} (or {@code uuid}) from the response. */
    private String createPlatformOrganisationAndGetId(HttpClient httpClient) throws Exception {
        String body = buildPlatformOrgCreateBody();
        JsonNode orgResponse = postJson(httpClient, VENDOR_ORG_CREATE_URL, body, null);
        String id = extractFirstOrganisationId(orgResponse);
        if (id == null || id.isBlank()) {
            throw new IllegalStateException("Organisation create did not return an id; response=" + orgResponse);
        }
        return id;
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

    private static String extractFirstOrganisationId(JsonNode orgResponse) {
        JsonNode orgs = orgResponse.get("organisations");
        if (orgs == null || !orgs.isArray() || orgs.isEmpty()) {
            return null;
        }
        JsonNode first = orgs.get(0);
        JsonNode id = first.get("id");
        if (id != null && id.isTextual() && !id.asText().isBlank()) {
            return id.asText();
        }
        JsonNode uuid = first.get("uuid");
        if (uuid != null && uuid.isTextual() && !uuid.asText().isBlank()) {
            return uuid.asText();
        }
        return null;
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

    private String buildPlatformOrgCreateBody() throws Exception {
        ObjectNode root = objectMapper.createObjectNode();
        ArrayNode organisations = root.putArray("organisations");
        ObjectNode org = organisations.addObject();
        org.put("tenantId", TENANT_IN);
        org.put("name", GLOBAL_PLATFORM_ORG_NAME);
        org.put("orgType", "PLATFORM");
        org.put("orgStatus", "ACTIVE");
        org.put("isActive", true);
        org.putArray("orgAddress");

        ObjectNode ri = root.putObject("RequestInfo");
        ri.put("apiId", "Rainmaker");
        ri.put("authToken", ORG_REQUEST_INFO_AUTH_TOKEN);
        ri.put("msgId", "1776161055449|en_IN");
        ri.putObject("plainAccessRequest");
        ObjectNode userInfo = ri.putObject("userInfo");
        userInfo.put("id", 15633);
        userInfo.put("uuid", "495c29c3-ce82-40fd-b61a-0bc83ffe0e1d");
        userInfo.put("userName", "platform_admin_uat");
        userInfo.put("name", "Platform Admin UAT");
        userInfo.put("mobileNumber", "9909099909");
        userInfo.put("emailId", "platformadmin@selcofoundation.org");
        userInfo.put("locale", "en_IN");
        userInfo.put("type", "EMPLOYEE");
        userInfo.put("active", true);
        userInfo.put("tenantId", TENANT_IN);
        userInfo.put("permanentCity", "All");
        ArrayNode roles = userInfo.putArray("roles");
        ObjectNode r1 = roles.addObject();
        r1.put("name", "Employee");
        r1.put("code", "EMPLOYEE");
        r1.put("tenantId", TENANT_IN);
        ObjectNode r2 = roles.addObject();
        r2.put("name", "Organization Platform Administrator");
        r2.put("code", "ORG_PLATFORM_ADMIN");
        r2.put("tenantId", TENANT_IN);

        return objectMapper.writeValueAsString(root);
    }
}
