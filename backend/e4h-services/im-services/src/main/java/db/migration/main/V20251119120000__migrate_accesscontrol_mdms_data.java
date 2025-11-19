package db.migration.main;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.extern.slf4j.Slf4j;
import org.apache.hc.client5.http.config.RequestConfig;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.util.Timeout;
import org.flywaydb.core.api.migration.BaseJavaMigration;
import org.flywaydb.core.api.migration.Context;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;

import java.io.FileWriter;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

@Slf4j
public class V20251119120000__migrate_accesscontrol_mdms_data extends BaseJavaMigration {

    private static final String SOURCE_TENANT_ID = "pg";
    private static final String TARGET_TENANT_ID = "in";
    private static final String ACTIONS_SCHEMA = "ACCESSCONTROL-ACTIONS-TEST.actions-test";
    private static final String ROLEACTIONS_SCHEMA = "ACCESSCONTROL-ROLEACTIONS.roleactions";
    private static final int PAGE_LIMIT = 1000;

    @Override
    public boolean canExecuteInTransaction() {
        return false;
    }

    @Override
    public void migrate(Context context) throws Exception {
        log.info("🚀 Starting Access Control MDMS migration from {} to {}", SOURCE_TENANT_ID, TARGET_TENANT_ID);

        RestTemplate restTemplate = createRestTemplateWithTimeouts();
        ObjectMapper objectMapper = new ObjectMapper();

        String logFileName = "accesscontrol_mdms_migration_" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")) + ".log";
        String logFilePath = "./logs/" + logFileName;
        String absoluteLogPath = Paths.get(logFilePath).toAbsolutePath().normalize().toString();

        try (PrintWriter migrationLogger = initializeMigrationLogger(logFilePath, absoluteLogPath)) {
            migrationLogger.println("========================================");
            migrationLogger.println("ACCESS CONTROL MDMS MIGRATION LOG");
            migrationLogger.println("Source Tenant: " + SOURCE_TENANT_ID);
            migrationLogger.println("Target Tenant: " + TARGET_TENANT_ID);
            migrationLogger.println("Started at: " + LocalDateTime.now());
            migrationLogger.println("========================================\n");
            migrationLogger.flush();

            String mdmsHost = getEnvOrDefault("EGOV_MDMS_HOST", "http://localhost:8094");
            String authToken = getEnvOrDefault("EGOV_AUTH_TOKEN", "");
            int sleepMs = Integer.parseInt(getEnvOrDefault("EGOV_MDMS_UPDATE_DELAY_MS", "0"));

            log.info("MDMS Host: {}", mdmsHost);
            migrationLogger.println("MDMS Host: " + mdmsHost);
            migrationLogger.flush();

            Map<String, String> actionIdMap = new HashMap<>();

            // Create schemas first
            log.info("Creating schema {}", ACTIONS_SCHEMA);
            migrationLogger.println("\n--- Creating Schema: " + ACTIONS_SCHEMA + " ---");
            migrationLogger.flush();
            boolean actionsSchemaCreated = searchAndCreateSchema(restTemplate, objectMapper, mdmsHost, ACTIONS_SCHEMA, authToken, sleepMs, migrationLogger);

            log.info("Creating schema {}", ROLEACTIONS_SCHEMA);
            migrationLogger.println("\n--- Creating Schema: " + ROLEACTIONS_SCHEMA + " ---");
            migrationLogger.flush();
            boolean roleActionsSchemaCreated = searchAndCreateSchema(restTemplate, objectMapper, mdmsHost, ROLEACTIONS_SCHEMA, authToken, sleepMs, migrationLogger);

            // Migrate data
            log.info("Processing schema {}", ACTIONS_SCHEMA);
            migrationLogger.println("\n--- Processing Schema: " + ACTIONS_SCHEMA + " ---");
            migrationLogger.flush();

            int actionsMigrated = migrateActions(context, restTemplate, objectMapper, mdmsHost, authToken, sleepMs, actionIdMap, migrationLogger);

            log.info("Processing schema {}", ROLEACTIONS_SCHEMA);
            migrationLogger.println("\n--- Processing Schema: " + ROLEACTIONS_SCHEMA + " ---");
            migrationLogger.flush();

            int roleActionsMigrated = migrateRoleActions(context, restTemplate, objectMapper, mdmsHost, authToken, sleepMs, actionIdMap, migrationLogger);

            migrationLogger.println("\n========================================");
            migrationLogger.println("MIGRATION SUMMARY");
            migrationLogger.println("========================================");
            migrationLogger.println("ACTIONS Schema Created: " + (actionsSchemaCreated ? "Yes" : "No"));
            migrationLogger.println("ROLEACTIONS Schema Created: " + (roleActionsSchemaCreated ? "Yes" : "No"));
            migrationLogger.println("Total ACTIONS migrated: " + actionsMigrated);
            migrationLogger.println("Total ROLEACTIONS migrated: " + roleActionsMigrated);
            migrationLogger.println("\nCompleted at: " + LocalDateTime.now());
            migrationLogger.println("========================================\n");
            migrationLogger.flush();

            log.info("✅ Access control MDMS migration completed. Log: {}", absoluteLogPath);
        }
    }

    private int migrateActions(
        Context context,
        RestTemplate restTemplate,
        ObjectMapper objectMapper,
        String mdmsHost,
        String authToken,
        int sleepMs,
        Map<String, String> actionIdMap,
        PrintWriter migrationLogger
    ) throws Exception {
        ArrayNode records = fetchAllMdmsRecords(restTemplate, objectMapper, mdmsHost, ACTIONS_SCHEMA, authToken, migrationLogger);
        if (records == null || records.isEmpty()) {
            migrationLogger.println("[SKIPPED] No records found for schema: " + ACTIONS_SCHEMA);
            migrationLogger.flush();
            return 0;
        }

        int nextUniqueIdentifier = queryNextUniqueIdentifier(context, ACTIONS_SCHEMA);
        int created = 0;
        int skipped = 0;

        migrationLogger.println("Found " + records.size() + " action records to migrate. Starting uniqueIdentifier at " + nextUniqueIdentifier);
        migrationLogger.flush();

        for (int i = 0; i < records.size(); i++) {
            ObjectNode record = (ObjectNode) records.get(i);
            String originalUniqueIdentifier = record.path("uniqueIdentifier").asText();
            String newUniqueIdentifier = String.valueOf(nextUniqueIdentifier++);

            try {
                record.put("tenantId", TARGET_TENANT_ID);
                record.put("uniqueIdentifier", newUniqueIdentifier);
                record.remove("id");
                record.remove("auditDetails");

                if (record.has("data") && record.get("data").isObject()) {
                    ((ObjectNode) record.get("data")).put("id", Integer.parseInt(newUniqueIdentifier));
                }

                boolean success = createMdmsDataWithUniqueId(
                    restTemplate, objectMapper, mdmsHost, record, authToken, sleepMs, migrationLogger
                );

                if (success) {
                    created++;
                    actionIdMap.put(originalUniqueIdentifier, newUniqueIdentifier);
                } else {
                    skipped++;
                }
            } catch (Exception e) {
                skipped++;
                log.error("Failed to migrate action {}: {}", originalUniqueIdentifier, e.getMessage(), e);
                migrationLogger.println("  [FAILED] Action " + originalUniqueIdentifier + " - " + e.getMessage());
                migrationLogger.flush();
            }
        }

        migrationLogger.println("Actions migrated: " + created + ", skipped: " + skipped);
        migrationLogger.flush();
        return created;
    }

    private int migrateRoleActions(
        Context context,
        RestTemplate restTemplate,
        ObjectMapper objectMapper,
        String mdmsHost,
        String authToken,
        int sleepMs,
        Map<String, String> actionIdMap,
        PrintWriter migrationLogger
    ) throws Exception {
        ArrayNode records = fetchAllMdmsRecords(restTemplate, objectMapper, mdmsHost, ROLEACTIONS_SCHEMA, authToken, migrationLogger);
        if (records == null || records.isEmpty()) {
            migrationLogger.println("[SKIPPED] No records found for schema: " + ROLEACTIONS_SCHEMA);
            migrationLogger.flush();
            return 0;
        }

        int nextUniqueIdentifier = queryNextUniqueIdentifier(context, ROLEACTIONS_SCHEMA);
        int created = 0;
        int skipped = 0;

        migrationLogger.println("Found " + records.size() + " role-action records to migrate. Starting uniqueIdentifier at " + nextUniqueIdentifier);
        migrationLogger.flush();

        for (int i = 0; i < records.size(); i++) {
            ObjectNode record = (ObjectNode) records.get(i);
            String originalUniqueIdentifier = record.path("uniqueIdentifier").asText();
            String newUniqueIdentifier = String.valueOf(nextUniqueIdentifier++);

            try {
                ObjectNode dataNode = record.has("data") && record.get("data").isObject()
                    ? (ObjectNode) record.get("data")
                    : objectMapper.createObjectNode();

                String originalActionId = dataNode.path("actionid").asText();
                String mappedActionId = actionIdMap.get(originalActionId);

                if (mappedActionId == null || mappedActionId.isEmpty()) {
                    skipped++;
                    log.warn("No mapped action id for roleaction {} actionid {}", originalUniqueIdentifier, originalActionId);
                    migrationLogger.println("  [SKIPPED] RoleAction " + originalUniqueIdentifier + " - Missing action mapping for " + originalActionId);
                    migrationLogger.flush();
                    continue;
                }

                record.put("tenantId", TARGET_TENANT_ID);
                record.put("uniqueIdentifier", newUniqueIdentifier);
                record.remove("id");
                record.remove("auditDetails");

                dataNode.put("tenantId", TARGET_TENANT_ID);
                dataNode.put("actionid", Integer.parseInt(mappedActionId));
                dataNode.put("id", Integer.parseInt(newUniqueIdentifier));
                record.set("data", dataNode);

                boolean success = createMdmsDataWithUniqueId(
                    restTemplate, objectMapper, mdmsHost, record, authToken, sleepMs, migrationLogger
                );

                if (success) {
                    created++;
                } else {
                    skipped++;
                }
            } catch (Exception e) {
                skipped++;
                log.error("Failed to migrate role action {}: {}", originalUniqueIdentifier, e.getMessage(), e);
                migrationLogger.println("  [FAILED] RoleAction " + originalUniqueIdentifier + " - " + e.getMessage());
                migrationLogger.flush();
            }
        }

        migrationLogger.println("RoleActions migrated: " + created + ", skipped: " + skipped);
        migrationLogger.flush();
        return created;
    }

    private int queryNextUniqueIdentifier(Context context, String schemaCode) throws Exception {
        String sql = "SELECT COALESCE(MAX(uniqueidentifier)::int, 0) FROM eg_mdms_data WHERE schemacode = ? AND tenantid = ?";
        try (Connection connection = context.getConfiguration().getDataSource().getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, schemaCode);
            statement.setString(2, TARGET_TENANT_ID);

            try (ResultSet rs = statement.executeQuery()) {
                if (rs.next()) {
                    int maxValue = rs.getInt(1);
                    return maxValue + 1;
                }
            }
        }
        return 1;
    }

    /**
     * Search for schema definition and create it in target tenant
     */
    private boolean searchAndCreateSchema(
        RestTemplate restTemplate,
        ObjectMapper objectMapper,
        String mdmsHost,
        String schemaCode,
        String authToken,
        int sleepMs,
        PrintWriter migrationLogger
    ) {
        String schemaSearchUrl = mdmsHost + "/egov-mdms-service/schema/v1/_search";

        // Build request
        ObjectNode requestBody = objectMapper.createObjectNode();
        requestBody.set("RequestInfo", buildRequestInfoNode(objectMapper, authToken, "egov-mdms-service"));

        ObjectNode schemaDefCriteria = objectMapper.createObjectNode();
        schemaDefCriteria.put("tenantId", SOURCE_TENANT_ID);
        ArrayNode codes = objectMapper.createArrayNode();
        codes.add(schemaCode);
        schemaDefCriteria.set("codes", codes);
        schemaDefCriteria.put("offset", 0);
        schemaDefCriteria.put("limit", 10);
        requestBody.set("SchemaDefCriteria", schemaDefCriteria);

        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<String> httpEntity = new HttpEntity<>(objectMapper.writeValueAsString(requestBody), headers);

            log.debug("Searching schema: {}", schemaCode);
            ResponseEntity<String> response = restTemplate.exchange(
                schemaSearchUrl,
                HttpMethod.POST,
                httpEntity,
                String.class
            );

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                JsonNode responseBody = objectMapper.readTree(response.getBody());
                ArrayNode schemaDefinitions = (ArrayNode) responseBody.path("SchemaDefinitions");

                if (schemaDefinitions != null && !schemaDefinitions.isEmpty()) {
                    // Get first schema definition
                    ObjectNode schemaDef = (ObjectNode) schemaDefinitions.get(0);

                    // Change tenantId to target tenant
                    schemaDef.put("tenantId", TARGET_TENANT_ID);

                    // Remove id, audit details to let system generate new one
                    schemaDef.remove("id");
                    schemaDef.remove("auditDetails");

                    // Create schema in target tenant
                    return createSchema(restTemplate, objectMapper, mdmsHost, schemaDef, authToken, sleepMs, migrationLogger);
                } else {
                    log.warn("No schema definitions found for: {}", schemaCode);
                    migrationLogger.println("[SKIPPED] No schema definition found for: " + schemaCode);
                    migrationLogger.flush();
                    return false;
                }
            } else {
                log.error("Schema search failed for {}: {}", schemaCode, response.getStatusCode());
                migrationLogger.println("[SKIPPED] Schema search failed: " + schemaCode + " - Status: " + response.getStatusCode());
                migrationLogger.flush();
                return false;
            }

        } catch (HttpClientErrorException | HttpServerErrorException e) {
            String responseBody = e.getResponseBodyAsString();
            log.error("HTTP error searching schema {}: {} - {}", schemaCode, e.getStatusCode(), responseBody);
            migrationLogger.println("[SKIPPED] Schema search HTTP error: " + schemaCode);
            migrationLogger.println("Status: " + e.getStatusCode() + " - " + responseBody);
            migrationLogger.flush();
            return false;
        } catch (Exception e) {
            log.error("Error searching schema {}: {}", schemaCode, e.getMessage(), e);
            migrationLogger.println("[SKIPPED] Schema search exception: " + schemaCode);
            migrationLogger.println("Error: " + e.getMessage());
            migrationLogger.flush();
            return false;
        }
    }

    /**
     * Create schema definition in target tenant
     */
    private boolean createSchema(
        RestTemplate restTemplate,
        ObjectMapper objectMapper,
        String mdmsHost,
        ObjectNode schemaDefinition,
        String authToken,
        int sleepMs,
        PrintWriter migrationLogger
    ) throws InterruptedException {
        String schemaCreateUrl = mdmsHost + "/egov-mdms-service/schema/v1/_create";

        ObjectNode requestBody = objectMapper.createObjectNode();
        requestBody.set("RequestInfo", buildRequestInfoNode(objectMapper, authToken, "egov-mdms-service"));
        requestBody.set("SchemaDefinition", schemaDefinition);

        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<String> httpEntity = new HttpEntity<>(objectMapper.writeValueAsString(requestBody), headers);

            String schemaCode = schemaDefinition.path("code").asText();
            log.debug("Creating schema: {}", schemaCode);
            ResponseEntity<String> response = restTemplate.exchange(
                schemaCreateUrl,
                HttpMethod.POST,
                httpEntity,
                String.class
            );

            if (sleepMs > 0) {
                Thread.sleep(sleepMs);
            }

            if (response.getStatusCode().is2xxSuccessful()) {
                log.info("✓ Schema created successfully: {}", schemaCode);
                migrationLogger.println("[SUCCESS] Schema created: " + schemaCode);
                migrationLogger.flush();
                return true;
            } else {
                log.error("Schema creation failed: {} - {}", schemaCode, response.getStatusCode());
                migrationLogger.println("[FAILED] Schema creation failed: " + schemaCode);
                migrationLogger.println("Status: " + response.getStatusCode() + " - " + response.getBody());
                migrationLogger.flush();
                return false;
            }

        } catch (HttpClientErrorException | HttpServerErrorException e) {
            String responseBody = e.getResponseBodyAsString();
            String schemaCode = schemaDefinition.path("code").asText();
            // Check if it's a duplicate (409 or similar)
            if (e.getStatusCode().value() == 409 || (responseBody != null && responseBody.contains("already exists"))) {
                log.warn("Schema already exists: {}", schemaCode);
                migrationLogger.println("[SKIPPED] Schema already exists: " + schemaCode);
                migrationLogger.flush();
                return true; // Return true as it exists
            }
            log.error("HTTP error creating schema {}: {} - {}", schemaCode, e.getStatusCode(), responseBody);
            migrationLogger.println("[FAILED] Schema creation HTTP error: " + schemaCode);
            migrationLogger.println("Status: " + e.getStatusCode() + " - " + responseBody);
            migrationLogger.flush();
            return false;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw e;
        } catch (Exception e) {
            String schemaCode = schemaDefinition.path("code").asText();
            log.error("Exception creating schema {}: {}", schemaCode, e.getMessage(), e);
            migrationLogger.println("[FAILED] Schema creation exception: " + schemaCode);
            migrationLogger.println("Error: " + e.getMessage());
            migrationLogger.flush();
            return false;
        }
    }

    private ArrayNode fetchAllMdmsRecords(
        RestTemplate restTemplate,
        ObjectMapper objectMapper,
        String mdmsHost,
        String schemaCode,
        String authToken,
        PrintWriter migrationLogger
    ) throws Exception {
        String url = mdmsHost + "/egov-mdms-service/v2/_search";
        ArrayNode aggregated = objectMapper.createArrayNode();
        int offset = 0;

        while (true) {
            ObjectNode requestBody = objectMapper.createObjectNode();
            requestBody.set("RequestInfo", buildRequestInfoNode(objectMapper, authToken, "asset-services"));

            ObjectNode mdmsCriteria = objectMapper.createObjectNode();
            mdmsCriteria.put("tenantId", SOURCE_TENANT_ID);
            mdmsCriteria.put("schemaCode", schemaCode);
            mdmsCriteria.put("limit", PAGE_LIMIT);
            mdmsCriteria.put("offset", offset);
            requestBody.set("MdmsCriteria", mdmsCriteria);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<String> httpEntity = new HttpEntity<>(objectMapper.writeValueAsString(requestBody), headers);

            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.POST, httpEntity, String.class);
            if (!response.getStatusCode().is2xxSuccessful() || response.getBody() == null) {
                migrationLogger.println("[WARN] Failed to fetch data for schema: " + schemaCode + " Offset: " + offset);
                migrationLogger.flush();
                break;
            }

            JsonNode body = objectMapper.readTree(response.getBody());
            ArrayNode records = (ArrayNode) body.path("mdms");
            if (records == null || records.isEmpty()) {
                break;
            }

            records.forEach(aggregated::add);

            if (records.size() < PAGE_LIMIT) {
                break;
            }
            offset += PAGE_LIMIT;
        }

        return aggregated;
    }

    private boolean createMdmsDataWithUniqueId(
        RestTemplate restTemplate,
        ObjectMapper objectMapper,
        String mdmsHost,
        ObjectNode mdmsRecord,
        String authToken,
        int sleepMs,
        PrintWriter migrationLogger
    ) throws InterruptedException {
        String schemaCode = mdmsRecord.path("schemaCode").asText();
        String uniqueIdentifier = mdmsRecord.path("uniqueIdentifier").asText();
        String url = mdmsHost + "/egov-mdms-service/v2/_create/" + schemaCode;

        ObjectNode requestBody = objectMapper.createObjectNode();
        requestBody.set("RequestInfo", buildRequestInfoNode(objectMapper, authToken, "egov-mdms-service"));
        requestBody.set("Mdms", mdmsRecord);

        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<String> httpEntity = new HttpEntity<>(objectMapper.writeValueAsString(requestBody), headers);

            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.POST, httpEntity, String.class);
            if (sleepMs > 0) {
                Thread.sleep(sleepMs);
            }

            if (response.getStatusCode().is2xxSuccessful()) {
                log.debug("✓ Created MDMS record {} in schema {}", uniqueIdentifier, schemaCode);
                return true;
            }

            log.warn("MDMS create failed for schema {} uniqueIdentifier {} status {}", schemaCode, uniqueIdentifier, response.getStatusCode());
            migrationLogger.println("  [FAILED] Schema " + schemaCode + " UniqueIdentifier " + uniqueIdentifier + " - Status: " + response.getStatusCode());
            migrationLogger.flush();
            return false;

        } catch (HttpClientErrorException | HttpServerErrorException e) {
            String responseBody = e.getResponseBodyAsString();
            if (e.getStatusCode().value() == 409 || responseBody.contains("already exists")) {
                log.debug("MDMS record already exists schema {} uniqueIdentifier {}", schemaCode, uniqueIdentifier);
                return true;
            }
            log.error("HTTP error creating MDMS record schema {} uniqueIdentifier {} status {} body {}", schemaCode, uniqueIdentifier, e.getStatusCode(), responseBody);
            migrationLogger.println(
                    "  [FAILED] Schema " + schemaCode + " UniqueIdentifier " + uniqueIdentifier + " - HTTP "
                            + e.getStatusCode() + ": " + responseBody
            );
            migrationLogger.flush();
            return false;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw e;
        } catch (Exception e) {
            log.error("Exception creating MDMS record schema {} uniqueIdentifier {} - {}", schemaCode, uniqueIdentifier, e.getMessage(), e);
            migrationLogger.println("  [FAILED] Schema " + schemaCode + " UniqueIdentifier " + uniqueIdentifier + " - " + e.getMessage());
            migrationLogger.flush();
            return false;
        }
    }

    private ObjectNode buildRequestInfoNode(ObjectMapper objectMapper, String authToken, String apiId) {
        ObjectNode requestInfo = objectMapper.createObjectNode();
        requestInfo.put("apiId", apiId);
        requestInfo.put("ver", "1.0");
        long timestamp = System.currentTimeMillis();
        requestInfo.put("ts", timestamp);
        requestInfo.put("action", "create");
        requestInfo.put("msgId", timestamp + "|en_IN");
        requestInfo.put("authToken", authToken != null ? authToken : "");

        ObjectNode userInfo = objectMapper.createObjectNode();
        userInfo.put("id", 206);
        userInfo.put("uuid", "14d6dbdf-e4d2-45c3-9717-c82ba17a9f19");
        userInfo.put("userName", "SYSTEMUSER");
        userInfo.put("name", "System User");
        userInfo.put("mobileNumber", "1111112111");
        userInfo.put("emailId", "");
        userInfo.put("type", "EMPLOYEE");
        userInfo.put("active", true);
        userInfo.put("tenantId", TARGET_TENANT_ID);

        ArrayNode roles = objectMapper.createArrayNode();
        ObjectNode employeeRole = objectMapper.createObjectNode();
        employeeRole.put("name", "Employee");
        employeeRole.put("code", "EMPLOYEE");
        employeeRole.put("tenantId", TARGET_TENANT_ID);
        roles.add(employeeRole);

        ObjectNode systemRole = objectMapper.createObjectNode();
        systemRole.put("name", "System user");
        systemRole.put("code", "SYSTEM");
        systemRole.put("tenantId", TARGET_TENANT_ID);
        roles.add(systemRole);

        userInfo.set("roles", roles);
        requestInfo.set("userInfo", userInfo);
        return requestInfo;
    }

    private PrintWriter initializeMigrationLogger(String logFilePath, String absolutePath) throws Exception {
        Files.createDirectories(Paths.get("./logs"));
        FileWriter fileWriter = new FileWriter(logFilePath, true);
        log.info("📝 Migration log file created: {}", absolutePath);
        return new PrintWriter(fileWriter, true);
    }

    @SuppressWarnings("deprecation")
    private RestTemplate createRestTemplateWithTimeouts() {
        RequestConfig requestConfig = RequestConfig.custom()
            .setConnectTimeout(Timeout.ofSeconds(30))
            .setConnectionRequestTimeout(Timeout.ofSeconds(30))
            .setResponseTimeout(Timeout.ofSeconds(60))
            .build();

        CloseableHttpClient httpClient = HttpClients.custom()
            .setDefaultRequestConfig(requestConfig)
            .build();

        HttpComponentsClientHttpRequestFactory factory = new HttpComponentsClientHttpRequestFactory(httpClient);

        log.info("RestTemplate created with timeouts: 30s connect, 30s connection request, 60s read");
        return new RestTemplate(factory);
    }

    private static String getEnvOrDefault(String key, String def) {
        String v = System.getenv(key);
        return (v == null || v.isEmpty()) ? def : v;
    }
}

