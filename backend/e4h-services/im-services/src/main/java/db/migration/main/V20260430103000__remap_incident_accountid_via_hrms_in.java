package db.migration.main;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.extern.slf4j.Slf4j;
import org.flywaydb.core.api.migration.BaseJavaMigration;
import org.flywaydb.core.api.migration.Context;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * For each distinct {@code accountid} on {@code eg_incident_v2}:
 * <ol>
 *   <li>Search HRMS {@code employees/_search} <strong>without</strong> {@code tenantId} query param
 *       (try {@code uuids}, then {@code codes} if empty — HRMS maps {@code uuids} to employee.uuid).</li>
 *   <li>If an employee is found, read {@code code}.</li>
 *   <li>Search HRMS again with {@code tenantId=in} and {@code codes}=that code.</li>
 *   <li>If a user uuid is present on the employee payload, update all incidents with the old accountid
 *       to the new uuid.</li>
 * </ol>
 * <p>
 * Set {@code INCIDENT_ACCOUNTID_HRMS_REMAP_SKIP=true} to no-op. Requires reachable HRMS; configure
 * {@code EGOV_HRMS_HOST}, {@code EGOV_HRMS_SEARCH_ENDPOINT}. Optional {@code EGOV_INTERNAL_MICROSERVICE_USER_UUID}
 * for {@code RequestInfo.userInfo}.
 */
@Slf4j
public class V20260430103000__remap_incident_accountid_via_hrms_in extends BaseJavaMigration {

    private static final String TARGET_TENANT = "in";

    @Override
    public boolean canExecuteInTransaction() {
        return false;
    }

    @Override
    public void migrate(Context context) throws Exception {
        if (Boolean.parseBoolean(getEnvOrDefault("INCIDENT_ACCOUNTID_HRMS_REMAP_SKIP", "false"))) {
            log.info("Skipping incident accountId HRMS remap (INCIDENT_ACCOUNTID_HRMS_REMAP_SKIP=true)");
            return;
        }

        String hrmsHost = trimTrailingSlash(getEnvOrDefault("EGOV_HRMS_HOST", "http://localhost:8988"));
        String hrmsSearchPath = getEnvOrDefault("EGOV_HRMS_SEARCH_ENDPOINT", "/egov-hrms/employees/_search");
        String internalUserUuid = getEnvOrDefault("EGOV_INTERNAL_MICROSERVICE_USER_UUID", "4fef6612-07a8-4751-97e9-0e0ac0687ebe");
        long sleepMs = Long.parseLong(getEnvOrDefault("INCIDENT_ACCOUNTID_HRMS_REMAP_SLEEP_MS", "0"));

        RestTemplate restTemplate = new RestTemplate();
        ObjectMapper mapper = new ObjectMapper();

        String logFileName = "incident_accountid_hrms_remap_" + LocalDateTime.now()
                .format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")) + ".log";
        Path logsDir = Paths.get("logs");
        Files.createDirectories(logsDir);
        Path logFilePath = logsDir.resolve(logFileName).toAbsolutePath().normalize();

        try (PrintWriter migrationLogger = new PrintWriter(new FileWriter(logFilePath.toFile(), true), true);
             Connection connection = context.getConfiguration().getDataSource().getConnection()) {

            migrationLogger.println("Incident accountId remap via HRMS (tenant " + TARGET_TENANT + ")");
            migrationLogger.println("HRMS: " + hrmsHost + hrmsSearchPath);
            migrationLogger.println("Log: " + logFilePath);
            migrationLogger.println("Started: " + LocalDateTime.now());

            Set<String> accountIds = loadDistinctAccountIds(connection);
            migrationLogger.println("Distinct accountIds: " + accountIds.size());
            log.info("Incident accountId HRMS remap: {} distinct accountIds", accountIds.size());

            int updatedTickets = 0;
            int skipped = 0;
            int failed = 0;

            for (String accountId : accountIds) {
                if (accountId == null || accountId.isBlank()) {
                    continue;
                }
                try {
                    if (sleepMs > 0) {
                        Thread.sleep(sleepMs);
                    }
                    log.info("Account Id found: {}", accountId);
                    JsonNode first = hrmsSearch(restTemplate, mapper, hrmsHost, hrmsSearchPath, internalUserUuid,
                            accountId, HrmsLookupMode.UUIDS_NO_TENANT);
                    JsonNode employee1 = firstEmployee(first);
                    log.info("************** Employee found *******************: {}", employee1);
                    if (employee1 == null) {
                        first = hrmsSearch(restTemplate, mapper, hrmsHost, hrmsSearchPath, internalUserUuid,
                                accountId, HrmsLookupMode.CODES_NO_TENANT);
                        employee1 = firstEmployee(first);
                        log.info("************** First Employee found with old AccountId *******************: {}", employee1);
                    }
                    if (employee1 == null) {
                        migrationLogger.println("[SKIP] no HRMS employee for accountId=" + accountId);
                        skipped++;
                        continue;
                    }
                    String code = textOrNull(employee1.path("code"));
                    log.info("************** First Employee CODE found with old AccountId *******************: {}", code);
                    if (code == null) {
                        migrationLogger.println("[SKIP] no employee code for accountId=" + accountId);
                        skipped++;
                        continue;
                    }

                    JsonNode second = hrmsSearch(restTemplate, mapper, hrmsHost, hrmsSearchPath, internalUserUuid,
                            code, HrmsLookupMode.CODES_TENANT_IN);
                    JsonNode employee2 = firstEmployee(second);
                    log.info("************** Second Employee found with tenantId in *******************: {}", employee2);
                    if (employee2 == null) {
                        migrationLogger.println("[SKIP] no HRMS employee for code=" + code + " tenant=" + TARGET_TENANT);
                        skipped++;
                        continue;
                    }
                    String newUserUuid = textOrNull(employee2.path("user").path("uuid"));
                    log.info("************** Second Employee CODE found with tenantId in *******************: {}", newUserUuid);
                    if (newUserUuid == null) {
                        migrationLogger.println("[SKIP] no user.uuid on HRMS employee for code=" + code + " tenant=" + TARGET_TENANT);
                        skipped++;
                        continue;
                    }
                    if (newUserUuid.equalsIgnoreCase(accountId)) {
                        migrationLogger.println("[SKIP] same uuid accountId=" + accountId);
                        skipped++;
                        continue;
                    }

                    int rows = updateAccountId(connection, accountId, newUserUuid);
                    updatedTickets += rows;
                    migrationLogger.println("[OK] accountId " + accountId + " -> " + newUserUuid + " (rows=" + rows + ", code=" + code + ")");
                } catch (Exception e) {
                    failed++;
                    String msg = "accountId=" + accountId + " : " + e.getMessage();
                    log.warn("Incident accountId remap failed: {}", msg, e);
                    migrationLogger.println("[FAIL] " + msg);
                }
            }

            migrationLogger.println("Finished: updatedRows=" + updatedTickets + ", skippedAccounts=" + skipped + ", failedAccounts=" + failed);
            migrationLogger.println("Ended: " + LocalDateTime.now());
            log.info("Incident accountId HRMS remap done: updatedRows={}, skipped={}, failed={}", updatedTickets, skipped, failed);
        }
    }

    private enum HrmsLookupMode {
        UUIDS_NO_TENANT,
        CODES_NO_TENANT,
        CODES_TENANT_IN
    }

    private Set<String> loadDistinctAccountIds(Connection connection) throws Exception {
        Set<String> out = new LinkedHashSet<>();
        String sql = "SELECT DISTINCT accountid FROM eg_incident_v2 WHERE accountid IS NOT NULL AND TRIM(accountid) <> '' limit 2";
        try (PreparedStatement ps = connection.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                String id = rs.getString(1);
                if (id != null && !id.isBlank()) {
                    out.add(id.trim());
                }
            }
        }
        return out;
    }

    private int updateAccountId(Connection connection, String oldAccountId, String newAccountId) throws Exception {
        log.info("************** Updating old accountId {} with new accountId {} *******************: {}", oldAccountId, newAccountId);
        String sql = "UPDATE eg_incident_v2 SET accountid = ? WHERE accountid = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, newAccountId);
            ps.setString(2, oldAccountId);
            return ps.executeUpdate();
        }
    }

    private JsonNode hrmsSearch(RestTemplate rt, ObjectMapper mapper, String hrmsHost, String hrmsPath,
                                String internalUserUuid, String value, HrmsLookupMode mode) throws Exception {
        UriComponentsBuilder b = UriComponentsBuilder.fromHttpUrl(hrmsHost + hrmsPath)
                .queryParam("isActive", true)
                .queryParam("limit", 10)
                .queryParam("offset", 0);
        switch (mode) {
            case UUIDS_NO_TENANT:
                b.queryParam("uuids", value);
                break;
            case CODES_NO_TENANT:
                b.queryParam("codes", value);
                break;
            case CODES_TENANT_IN:
                b.queryParam("tenantId", TARGET_TENANT);
                b.queryParam("codes", value);
                break;
            default:
                throw new IllegalStateException("Unknown mode: " + mode);
        }
        String url = b.toUriString();
        ObjectNode body = mapper.createObjectNode();
        body.set("RequestInfo", buildRequestInfo(mapper, internalUserUuid));
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
        HttpEntity<String> entity = new HttpEntity<>(mapper.writeValueAsString(body), headers);
        ResponseEntity<String> resp = rt.postForEntity(url, entity, String.class);
        if (!resp.getStatusCode().is2xxSuccessful() || resp.getBody() == null) {
            throw new IllegalStateException("HRMS HTTP " + resp.getStatusCode());
        }
        return mapper.readTree(resp.getBody());
    }

    private JsonNode firstEmployee(JsonNode root) {
        if (root == null) {
            return null;
        }
        JsonNode employees = root.get("Employees");
        if (employees == null || !employees.isArray() || employees.isEmpty()) {
            return null;
        }
        return employees.get(0);
    }

    private ObjectNode buildRequestInfo(ObjectMapper mapper, String internalUserUuid) {
        ObjectNode ri = mapper.createObjectNode();
        ri.put("apiId", "im-services-migration");
        ri.put("ver", "1.0");
        String token = getEnvOrDefault("EGOV_AUTH_TOKEN", "");
        if (!token.isEmpty()) {
            ri.put("authToken", token);
        }
        ObjectNode userInfo = mapper.createObjectNode();
        userInfo.put("uuid", internalUserUuid);
        userInfo.put("type", "SYSTEM");
        ri.set("userInfo", userInfo);
        return ri;
    }

    private static String textOrNull(JsonNode n) {
        if (n == null || n.isNull() || n.isMissingNode()) {
            return null;
        }
        String v = n.asText(null);
        return (v == null || v.isBlank()) ? null : v.trim();
    }

    private static String trimTrailingSlash(String url) {
        if (url == null || url.isEmpty()) {
            return url;
        }
        return url.endsWith("/") ? url.substring(0, url.length() - 1) : url;
    }

    private static String getEnvOrDefault(String key, String defaultValue) {
        String v = System.getenv(key);
        return (v == null || v.isEmpty()) ? defaultValue : v;
    }
}
