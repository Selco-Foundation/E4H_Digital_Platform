package db.migration.main;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.extern.slf4j.Slf4j;
import org.flywaydb.core.api.migration.BaseJavaMigration;
import org.flywaydb.core.api.migration.Context;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.Duration;
import java.util.UUID;

/**
 * COMPLAINT_RESOLVER → vendor org + {@code eg_org_user}, same layout as im-services Java migrations
 * ({@code src/main/java/db/migration/main}, {@code flyway.locations=classpath:/db/migration/main}).
 * <p>
 * <b>Database:</b> Flyway opens JDBC using {@code spring.flyway.url} / {@code spring.flyway.user} /
 * {@code spring.flyway.password} from {@code application.properties} (same idea as im-services
 * {@code flyway.url} / {@code flyway.user} / {@code flyway.password}). Use
 * {@link Context#getConnection()} for all SQL in this migration — do not open a separate JDBC URL here.
 * <p>
 * Enable with {@code RUN_COMPLAINT_RESOLVER_VENDOR_MIGRATION=true}. HTTP runs outside a transaction
 * ({@link #canExecuteInTransaction()}).
 */
@Slf4j
public class V20260420140000__MigrateComplaintResolversToVendorOrgs extends BaseJavaMigration {



    private static final String HRMS_SEARCH_URL =

            System.getenv("EGOV_HRMS_HOST") + "egov-hrms/employees/_search?roles=COMPLAINT_RESOLVER&tenantId=in&isActive=true";

    private static final String VENDOR_ORG_CREATE_URL =
            System.getenv("EGOV_VENDOR_HOST") + "vendor/organisation/v1/_create";

    private static final String HRMS_AUTH_HEADER_VALUE = "Bearer your-auth-token";

    private static final String HRMS_REQUEST_INFO_AUTH_TOKEN = "Bearer your-auth-token";

    private static final String ORG_REQUEST_INFO_AUTH_TOKEN = "AUTH_TOKEN";

    private static final String SYSTEM_USER = "00000000-0000-0000-0000-000000000001";

    private static final String TENANT_IN = "in";

    private static final Duration HTTP_TIMEOUT = Duration.ofMinutes(2);

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public boolean canExecuteInTransaction() {
        return false;
    }

    @Override
    public void migrate(Context context) throws Exception {

        HttpClient httpClient = HttpClient.newBuilder().connectTimeout(HTTP_TIMEOUT).build();

        String hrmsBody = buildHrmsSearchBody();
        JsonNode hrmsRoot = postJson(httpClient, HRMS_SEARCH_URL, hrmsBody, HRMS_AUTH_HEADER_VALUE);
        JsonNode employees = hrmsRoot.get("Employees");
        if (employees == null || !employees.isArray() || employees.isEmpty()) {
            log.warn("HRMS returned no employees for COMPLAINT_RESOLVER; nothing to migrate.");
            return;
        }

        Connection connection = context.getConnection();
        int created = 0;
        for (JsonNode employee : employees) {
            JsonNode user = employee.get("user");
            if (user == null || user.isNull()) {
                log.warn("Skipping employee without user node: {}", employee);
                continue;
            }
            String userUuid = textOrNull(user, "uuid");
            if (userUuid == null || userUuid.isBlank()) {
                log.warn("Skipping employee without user.uuid: {}", employee);
                continue;
            }
            if (orgUserExists(connection, userUuid)) {
                log.info("eg_org_user already present for userId {}; skipping.", userUuid);
                continue;
            }

            String orgName = textOrNull(user, "name");
            if (orgName == null || orgName.isBlank()) {
                orgName = textOrNull(user, "userName");
            }
            if (orgName == null || orgName.isBlank()) {
                orgName = textOrNull(employee, "code");
            }
            if (orgName == null || orgName.isBlank()) {
                log.warn("Skipping employee with no name/userName/code for uuid {}", userUuid);
                continue;
            }

            String orgCreateBody = buildOrgCreateBody(orgName);
            JsonNode orgResponse = postJson(httpClient, VENDOR_ORG_CREATE_URL, orgCreateBody, null);
            String organizationId = extractFirstOrganisationId(orgResponse);
            if (organizationId == null || organizationId.isBlank()) {
                throw new IllegalStateException("Organisation create did not return an id for user " + userUuid + "; response=" + orgResponse);
            }

            insertOrgUser(connection, organizationId, userUuid);
            created++;
            log.info("Linked COMPLAINT_RESOLVER user {} to organisation {}", userUuid, organizationId);
        }

        log.info("COMPLAINT_RESOLVER vendor migration finished; created {} eg_org_user row(s).", created);
    }

    private static boolean orgUserExists(Connection connection, String userUuid) throws Exception {
        String sql = "SELECT 1 FROM eg_org_user WHERE userid = ? AND tenantid = ? LIMIT 1";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, userUuid);
            ps.setString(2, TENANT_IN);
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
        return id != null && id.isTextual() ? id.asText() : null;
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

    private String buildOrgCreateBody(String organisationName) throws Exception {
        ObjectNode root = objectMapper.createObjectNode();
        ArrayNode organisations = root.putArray("organisations");
        ObjectNode org = organisations.addObject();
        org.put("tenantId", TENANT_IN);
        org.put("name", organisationName);
        org.put("orgType", "VENDOR");
        org.put("orgSubType", "INSTALLATION_VENDOR");
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
