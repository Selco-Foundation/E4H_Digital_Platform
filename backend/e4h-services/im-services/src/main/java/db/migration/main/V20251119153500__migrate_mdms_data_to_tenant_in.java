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
import org.springframework.http.*;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;

import java.io.FileWriter;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Slf4j
public class V20251119153500__migrate_mdms_data_to_tenant_in extends BaseJavaMigration {

    private static final String SOURCE_TENANT_ID = "pg";
    private static final String TARGET_TENANT_ID = "in";

    // Module -> Masters mapping
    private static final Map<String, List<String>> MODULE_MASTERS_MAP;
    static {
        Map<String, List<String>> m = new LinkedHashMap<>();
        m.put("Incident", List.of("SystemFunctionality", "RejectReasons", "SendBackReasons", "ServiceDefs"));
        m.put("common-masters", List.of("Department", "Designation", "wfSlaConfig", "GenderType"));
        MODULE_MASTERS_MAP = Collections.unmodifiableMap(m);
    }

    @Override
    public boolean canExecuteInTransaction() {
        return false;
    }

    @Override
    public void migrate(Context context) throws Exception {
        log.info("Starting migration: Migrating MDMS data from {} to {}", SOURCE_TENANT_ID, TARGET_TENANT_ID);

        RestTemplate restTemplate = createRestTemplateWithTimeouts();
        ObjectMapper objectMapper = new ObjectMapper();

        // Initialize migration log file
        String logFileName = "mdms_migration_" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")) + ".log";
        String logFilePath = "./logs/" + logFileName;
        String absoluteLogPath = Paths.get(logFilePath).toAbsolutePath().normalize().toString();

        try (PrintWriter migrationLogger = initializeMigrationLogger(logFilePath, absoluteLogPath)) {
            migrationLogger.println("========================================");
            migrationLogger.println("MDMS DATA MIGRATION LOG");
            migrationLogger.println("Source Tenant: " + SOURCE_TENANT_ID);
            migrationLogger.println("Target Tenant: " + TARGET_TENANT_ID);
            migrationLogger.println("Started at: " + LocalDateTime.now());
            migrationLogger.println("========================================\n");
            migrationLogger.flush();

            String mdmsHost = getEnvOrDefault("EGOV_MDMS_HOST", "http://localhost:8094");
            String authToken = getEnvOrDefault("EGOV_AUTH_TOKEN", "");
            int sleepMs = Integer.parseInt(getEnvOrDefault("EGOV_MDMS_UPDATE_DELAY_MS", "0"));

            log.info("MDMS Host: {}", mdmsHost);

            int totalSchemasCreated = 0;
            int totalSchemasSkipped = 0;
            int totalDataCreated = 0;
            int totalDataSkipped = 0;

            // Process each module
            for (Map.Entry<String, List<String>> moduleEntry : MODULE_MASTERS_MAP.entrySet()) {
                String module = moduleEntry.getKey();
                List<String> masters = moduleEntry.getValue();

                migrationLogger.println("\n========================================");
                migrationLogger.println("Processing Module: " + module);
                migrationLogger.println("========================================\n");
                migrationLogger.flush();

                log.info("Processing module: {} with {} masters", module, masters.size());

                // Process each master in the module
                for (String master : masters) {
                    String schemaCode = module + "." + master;
                    log.info("Processing schema: {}", schemaCode);

                    migrationLogger.println("--- Processing Schema: " + schemaCode + " ---");
                    migrationLogger.flush();

                    try {
                        // Step 1: Search for schema
                        ObjectNode schemaDefinition = searchAndCreateSchema(
                            restTemplate, objectMapper, mdmsHost, schemaCode, authToken, migrationLogger
                        );

                        if (schemaDefinition != null) {
                            totalSchemasCreated++;
                            log.info("✓ Schema created: {}", schemaCode);
                        } else {
                            totalSchemasSkipped++;
                            log.warn("✗ Schema skipped: {}", schemaCode);
                        }

                        // Step 2: Search and create MDMS data
                        int created = searchAndCreateMdmsData(
                            restTemplate, objectMapper, mdmsHost, schemaCode, authToken, sleepMs, migrationLogger
                        );
                        totalDataCreated += created;

                    } catch (Exception e) {
                        totalSchemasSkipped++;
                        totalDataSkipped++;
                        log.error("Error processing schema {}: {}", schemaCode, e.getMessage(), e);
                        migrationLogger.println("[ERROR] Failed to process schema: " + schemaCode);
                        migrationLogger.println("Reason: " + e.getMessage());
                        migrationLogger.println();
                        migrationLogger.flush();
                    }
                }
            }

            // Final summary
            migrationLogger.println("\n========================================");
            migrationLogger.println("MIGRATION SUMMARY");
            migrationLogger.println("========================================");
            migrationLogger.println("Schemas Created: " + totalSchemasCreated);
            migrationLogger.println("Schemas Skipped: " + totalSchemasSkipped);
            migrationLogger.println("Data Records Created: " + totalDataCreated);
            migrationLogger.println("Data Records Skipped: " + totalDataSkipped);
            migrationLogger.println("\nCompleted at: " + LocalDateTime.now());
            migrationLogger.println("========================================\n");
            migrationLogger.flush();

            log.info("Migration completed");
            log.info("Schemas created: {}, skipped: {}", totalSchemasCreated, totalSchemasSkipped);
            log.info("Data records created: {}, skipped: {}", totalDataCreated, totalDataSkipped);
            log.info("Migration log file: {}", absoluteLogPath);
        }
    }

    /**
     * Search for schema definition and create it in target tenant
     */
    private ObjectNode searchAndCreateSchema(
        RestTemplate restTemplate,
        ObjectMapper objectMapper,
        String mdmsHost,
        String schemaCode,
        String authToken,
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
                    return createSchema(restTemplate, objectMapper, mdmsHost, schemaDef, authToken, migrationLogger);
                } else {
                    log.warn("No schema definitions found for: {}", schemaCode);
                    migrationLogger.println("[SKIPPED] No schema definition found for: " + schemaCode);
                    migrationLogger.flush();
                    return null;
                }
            } else {
                log.error("Schema search failed for {}: {}", schemaCode, response.getStatusCode());
                migrationLogger.println("[SKIPPED] Schema search failed: " + schemaCode + " - Status: " + response.getStatusCode());
                migrationLogger.flush();
                return null;
            }

        } catch (HttpClientErrorException | HttpServerErrorException e) {
            log.error("HTTP error searching schema {}: {} - {}", schemaCode, e.getStatusCode(), e.getResponseBodyAsString());
            migrationLogger.println("[SKIPPED] Schema search HTTP error: " + schemaCode);
            migrationLogger.println("Status: " + e.getStatusCode() + " - " + e.getResponseBodyAsString());
            migrationLogger.flush();
            return null;
        } catch (Exception e) {
            log.error("Error searching schema {}: {}", schemaCode, e.getMessage(), e);
            migrationLogger.println("[SKIPPED] Schema search exception: " + schemaCode);
            migrationLogger.println("Error: " + e.getMessage());
            migrationLogger.flush();
            return null;
        }
    }

    /**
     * Create schema definition in target tenant with retry mechanism
     */
    private ObjectNode createSchema(
        RestTemplate restTemplate,
        ObjectMapper objectMapper,
        String mdmsHost,
        ObjectNode schemaDefinition,
        String authToken,
        PrintWriter migrationLogger
    ) throws Exception {
        String schemaCode = schemaDefinition.path("code").asText();

        // Retry delays: 2s, 5s, 15s, 30s
        int[] retryDelays = {2000, 5000, 15000, 30000};
        int maxAttempts = 5;

        for (int attempt = 0; attempt < maxAttempts; attempt++) {
            try {
                ObjectNode result = attemptCreateSchema(
                    restTemplate, objectMapper, mdmsHost, schemaDefinition, authToken, schemaCode, migrationLogger
                );

                if (result != null) {
                    return result;
                }

                // If not successful and not last attempt, retry
                if (attempt < maxAttempts - 1 ) {
                    int delayMs = retryDelays[attempt];
                    log.warn("Retry attempt {} for schema {} after {}ms", attempt + 1, schemaCode, delayMs);
                    migrationLogger.println("[RETRY] Attempt " + (attempt + 1) + "/" + maxAttempts + " - Retrying after " + delayMs + "ms");
                    migrationLogger.flush();
                    Thread.sleep(delayMs);
                }

            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw e;
            } catch (Exception e) {
                // If not last attempt, retry
                if (attempt < maxAttempts - 1 ) {
                    int delayMs = retryDelays[attempt];
                    log.warn(
                            "Exception on attempt {} for schema {}: {}. Retrying after {}ms",
                            attempt + 1, schemaCode, e.getMessage(), delayMs
                    );
                    migrationLogger.println("[RETRY] Exception on attempt " + (attempt + 1) + ": " + e.getMessage() + " - Retrying after " + delayMs + "ms");
                    migrationLogger.flush();
                    try {
                        Thread.sleep(delayMs);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        throw ie;
                    }
                }
            }
        }

        log.error("All retry attempts failed for schema: {}", schemaCode);
        migrationLogger.println("[FAILED] All " + maxAttempts + " attempts exhausted");
        migrationLogger.flush();
        return null;
    }

    /**
     * Attempt to create schema definition (single attempt)
     */
    private ObjectNode attemptCreateSchema(
        RestTemplate restTemplate,
        ObjectMapper objectMapper,
        String mdmsHost,
        ObjectNode schemaDefinition,
        String authToken,
        String schemaCode,
        PrintWriter migrationLogger
    ) {
        String schemaCreateUrl = mdmsHost + "/egov-mdms-service/schema/v1/_create";

        ObjectNode requestBody = objectMapper.createObjectNode();
        requestBody.set("RequestInfo", buildRequestInfoNode(objectMapper, authToken, "egov-mdms-service"));
        requestBody.set("SchemaDefinition", schemaDefinition);

        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<String> httpEntity = new HttpEntity<>(objectMapper.writeValueAsString(requestBody), headers);

            log.debug("Creating schema: {}", schemaCode);
            ResponseEntity<String> response = restTemplate.exchange(
                schemaCreateUrl,
                HttpMethod.POST,
                httpEntity,
                String.class
            );

            if (response.getStatusCode().is2xxSuccessful()) {
                log.info("✓ Schema created successfully: {}", schemaCode);
                migrationLogger.println("[SUCCESS] Schema created: " + schemaCode);
                migrationLogger.flush();
                return schemaDefinition;
            } else {
                // Check if it's a duplicate error in the response body
                String responseBody = response.getBody();
                if (isDuplicateError(responseBody, objectMapper)) {
                    log.warn("Schema already exists: {}", schemaCode);
                    migrationLogger.println("[SKIPPED] Schema already exists: " + schemaCode);
                    migrationLogger.flush();
                    return schemaDefinition; // Return it anyway as it exists
                }
                log.error("Schema creation failed: {} - {}", schemaCode, response.getStatusCode());
                migrationLogger.println("[FAILED] Schema creation failed: " + schemaCode);
                migrationLogger.println("Status: " + response.getStatusCode() + " - " + responseBody);
                migrationLogger.flush();
                return null;
            }

        } catch (HttpClientErrorException | HttpServerErrorException e) {
            String responseBody = e.getResponseBodyAsString();
            // Check if it's a duplicate - don't retry for duplicates
            if (isDuplicateError(responseBody, objectMapper)) {
                log.warn("Schema already exists: {}", schemaCode);
                migrationLogger.println("[SKIPPED] Schema already exists: " + schemaCode);
                migrationLogger.flush();
                return schemaDefinition; // Return it anyway as it exists
            }
            // For other HTTP errors, return null to trigger retry
            log.error("HTTP error creating schema {}: {} - {}", schemaCode, e.getStatusCode(), responseBody);
            migrationLogger.println("[FAILED] Schema creation HTTP error: " + schemaCode);
            migrationLogger.println("Status: " + e.getStatusCode() + " - " + responseBody);
            migrationLogger.flush();
            return null;
        } catch (Exception e) {
            log.error("Error creating schema {}: {}", schemaCode, e.getMessage(), e);
            migrationLogger.println("[FAILED] Schema creation exception: " + schemaCode);
            migrationLogger.println("Error: " + e.getMessage());
            migrationLogger.flush();
            return null;
        }
    }

    /**
     * Search for MDMS data and create it in target tenant
     */
    private int searchAndCreateMdmsData(
        RestTemplate restTemplate,
        ObjectMapper objectMapper,
        String mdmsHost,
        String schemaCode,
        String authToken,
        int sleepMs,
        PrintWriter migrationLogger
    ) {
        String mdmsSearchUrl = mdmsHost + "/egov-mdms-service/v2/_search";

        // Build request - search without uniqueIdentifier to get all records
        ObjectNode requestBody = objectMapper.createObjectNode();
        requestBody.set("RequestInfo", buildRequestInfoNode(objectMapper, authToken, "asset-services"));

        ObjectNode mdmsCriteria = objectMapper.createObjectNode();
        mdmsCriteria.put("tenantId", SOURCE_TENANT_ID);
        mdmsCriteria.put("schemaCode", schemaCode);
        mdmsCriteria.put("limit", 1000);
        requestBody.set("MdmsCriteria", mdmsCriteria);

        int created = 0;
        int skipped = 0;

        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<String> httpEntity = new HttpEntity<>(objectMapper.writeValueAsString(requestBody), headers);

            log.debug("Searching MDMS data for schema: {}", schemaCode);
            ResponseEntity<String> response = restTemplate.exchange(
                mdmsSearchUrl,
                HttpMethod.POST,
                httpEntity,
                String.class
            );

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                JsonNode responseBody = objectMapper.readTree(response.getBody());
                ArrayNode mdmsArray = (ArrayNode) responseBody.path("mdms");

                if (mdmsArray != null && !mdmsArray.isEmpty()) {
                    log.info("Found {} records for schema: {}", mdmsArray.size(), schemaCode);
                    migrationLogger.println("Found " + mdmsArray.size() + " records to migrate");

                    // Process each MDMS record
                    for (int i = 0; i < mdmsArray.size(); i++) {
                        ObjectNode mdmsRecord = (ObjectNode) mdmsArray.get(i);

                        // Change tenantId to target tenant
                        mdmsRecord.put("tenantId", TARGET_TENANT_ID);

                        // Remove id to let system generate new one
                        mdmsRecord.remove("id");

                        // Update audit details
                        ObjectNode auditDetails = objectMapper.createObjectNode();
                        long currentTime = System.currentTimeMillis();
                        auditDetails.put("createdBy", "14d6dbdf-e4d2-45c3-9717-c82ba17a9f19");
                        auditDetails.put("lastModifiedBy", "14d6dbdf-e4d2-45c3-9717-c82ba17a9f19");
                        auditDetails.put("createdTime", currentTime);
                        auditDetails.put("lastModifiedTime", currentTime);
                        mdmsRecord.set("auditDetails", auditDetails);

                        // Create MDMS data in target tenant
                        if (createMdmsData(restTemplate, objectMapper, mdmsHost, mdmsRecord, authToken, sleepMs, migrationLogger)) {
                            created++;
                        } else {
                            skipped++;
                        }
                    }

                    migrationLogger.println("Created: " + created + ", Skipped: " + skipped);
                    migrationLogger.flush();
                } else {
                    log.warn("No MDMS data found for schema: {}", schemaCode);
                    migrationLogger.println("[SKIPPED] No data found for schema: " + schemaCode);
                    migrationLogger.flush();
                }
            } else {
                log.error("MDMS data search failed for {}: {}", schemaCode, response.getStatusCode());
                migrationLogger.println("[SKIPPED] MDMS data search failed: " + schemaCode + " - Status: " + response.getStatusCode());
                migrationLogger.flush();
            }

        } catch (HttpClientErrorException | HttpServerErrorException e) {
            log.error("HTTP error searching MDMS data for {}: {} - {}", schemaCode, e.getStatusCode(), e.getResponseBodyAsString());
            migrationLogger.println("[SKIPPED] MDMS data search HTTP error: " + schemaCode);
            migrationLogger.println("Status: " + e.getStatusCode() + " - " + e.getResponseBodyAsString());
            migrationLogger.flush();
        } catch (Exception e) {
            log.error("Error searching MDMS data for {}: {}", schemaCode, e.getMessage(), e);
            migrationLogger.println("[SKIPPED] MDMS data search exception: " + schemaCode);
            migrationLogger.println("Error: " + e.getMessage());
            migrationLogger.flush();
        }

        return created;
    }

    /**
     * Create MDMS data record in target tenant with retry mechanism
     */
    private boolean createMdmsData(
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

        // Retry delays: 2s, 5s, 15s, 30s (last retry)
        int[] retryDelays = {2000, 5000, 15000, 30000};
        int maxAttempts = 5;

        for (int attempt = 0; attempt < maxAttempts; attempt++) {
            try {
                boolean success = attemptCreateMdmsData(
                    restTemplate, objectMapper, mdmsHost, mdmsRecord, authToken,
                    schemaCode, uniqueIdentifier, migrationLogger
                );

                if (success) {
                    // Small delay to avoid overwhelming the service
                    if (sleepMs > 0) {
                        Thread.sleep(sleepMs);
                    }
                    return true;
                }

                // If not successful and not last attempt, retry
                if (attempt < maxAttempts - 1) {
                    int delayMs = retryDelays[attempt];
                    log.warn("Retry attempt {} for {} - {} after {}ms", attempt + 1, schemaCode, uniqueIdentifier, delayMs);
                    migrationLogger.println("  [RETRY] Attempt " + (attempt + 1) + "/" + maxAttempts + " - Retrying after " + delayMs + "ms");
                    migrationLogger.flush();
                    Thread.sleep(delayMs);
                }

            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw e;
            } catch (Exception e) {
                // If not last attempt, retry
                if (attempt < maxAttempts - 1) {
                    int delayMs = retryDelays[attempt];
                    log.warn(
                            "Exception on attempt {} for {} - {}: {}. Retrying after {}ms",
                            attempt + 1, schemaCode, uniqueIdentifier, e.getMessage(), delayMs
                    );
                    migrationLogger.println("  [RETRY] Exception on attempt " + (attempt + 1) + ": " + e.getMessage() + " - Retrying after " + delayMs + "ms");
                    migrationLogger.flush();
                    try {
                        Thread.sleep(delayMs);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        throw ie;
                    }
                }
            }
        }

        log.error("All retry attempts failed for {} - {}", schemaCode, uniqueIdentifier);
        migrationLogger.println("  [FAILED] All " + maxAttempts + " attempts exhausted");
        migrationLogger.flush();
        return false;
    }

    /**
     * Attempt to create MDMS data record (single attempt)
     */
    private boolean attemptCreateMdmsData(
        RestTemplate restTemplate,
        ObjectMapper objectMapper,
        String mdmsHost,
        ObjectNode mdmsRecord,
        String authToken,
        String schemaCode,
        String uniqueIdentifier,
        PrintWriter migrationLogger
    ) {
        String createUrl = mdmsHost + "/egov-mdms-service/v2/_create/" + schemaCode;

        ObjectNode requestBody = objectMapper.createObjectNode();
        requestBody.set("RequestInfo", buildRequestInfoNode(objectMapper, authToken, "egov-mdms-service"));
        requestBody.set("Mdms", mdmsRecord);

        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<String> httpEntity = new HttpEntity<>(objectMapper.writeValueAsString(requestBody), headers);

            log.debug("Creating MDMS data: {} - {}", schemaCode, uniqueIdentifier);
            ResponseEntity<String> response = restTemplate.exchange(
                createUrl,
                HttpMethod.POST,
                httpEntity,
                String.class
            );

            if (response.getStatusCode().is2xxSuccessful()) {
                log.debug("✓ MDMS data created: {} - {}", schemaCode, uniqueIdentifier);
                return true;
            } else {
                // Check if it's a duplicate error in the response body
                String responseBody = response.getBody();
                if (isDuplicateError(responseBody, objectMapper)) {
                    log.debug("MDMS data already exists: {} - {}", schemaCode, uniqueIdentifier);
                    return true; // Count as success since it exists
                }
                log.warn("MDMS data creation failed: {} - {} - {}", schemaCode, uniqueIdentifier, response.getStatusCode());
                migrationLogger.println("  [FAILED] " + uniqueIdentifier + " - Status: " + response.getStatusCode());
                migrationLogger.flush();
                return false;
            }

        } catch (HttpClientErrorException | HttpServerErrorException e) {
            String responseBody = e.getResponseBodyAsString();
            // Check if it's a duplicate - don't retry for duplicates
            if (isDuplicateError(responseBody, objectMapper)) {
                log.debug("MDMS data already exists: {} - {}", schemaCode, uniqueIdentifier);
                return true; // Count as success since it exists
            }
            // For other HTTP errors, return false to trigger retry
            log.warn("HTTP error creating MDMS data {} - {}: {} - {}", schemaCode, uniqueIdentifier, e.getStatusCode(), responseBody);
            migrationLogger.println("  [FAILED] " + uniqueIdentifier + " - HTTP " + e.getStatusCode() + ": " + responseBody);
            migrationLogger.flush();
            return false;
        } catch (Exception e) {
            log.warn("Error creating MDMS data {} - {}: {}", schemaCode, uniqueIdentifier, e.getMessage());
            migrationLogger.println("  [FAILED] " + uniqueIdentifier + " - Exception: " + e.getMessage());
            migrationLogger.flush();
            return false;
        }
    }

    /**
     * Check if the response indicates a duplicate record error
     */
    private boolean isDuplicateError(String responseBody, ObjectMapper objectMapper) {
        if (responseBody == null || responseBody.isEmpty()) {
            return false;
        }
        try {
            JsonNode responseNode = objectMapper.readTree(responseBody);
            JsonNode errorsNode = responseNode.path("Errors");
            if (errorsNode.isArray()) {
                for (JsonNode error : errorsNode) {
                    String errorCode = error.path("code").asText();
                    if ("DUPLICATE_RECORD".equals(errorCode) || "DUPLICATE_SCHEMA_CODE".equals(errorCode)) {
                        return true;
                    }
                }
            }
        } catch (Exception e) {
            // If parsing fails, fall back to string check
            log.debug("Failed to parse response body for duplicate check: {}", e.getMessage());
            return responseBody.contains("DUPLICATE_RECORD") || responseBody.contains("DUPLICATE_SCHEMA_CODE") || responseBody.contains("already exists");
        }
        return false;
    }

    /**
     * Build RequestInfo node for API calls
     */
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
        userInfo.put("tenantId", "in");

        ArrayNode roles = objectMapper.createArrayNode();
        ObjectNode role1 = objectMapper.createObjectNode();
        role1.put("name", "Employee");
        role1.put("code", "EMPLOYEE");
        role1.put("tenantId", "in");
        roles.add(role1);
        ObjectNode role2 = objectMapper.createObjectNode();
        role2.put("name", "System user");
        role2.put("code", "SYSTEM");
        role2.put("tenantId", "in");
        roles.add(role2);
        userInfo.set("roles", roles);

        requestInfo.set("userInfo", userInfo);
        return requestInfo;
    }

    /**
     * Initialize migration logger
     */
    private PrintWriter initializeMigrationLogger(String logFilePath, String absolutePath) throws Exception {
        Files.createDirectories(Paths.get("./logs"));
        FileWriter fileWriter = new FileWriter(logFilePath, true);
        log.info("Migration log file created: {}", absolutePath);
        return new PrintWriter(fileWriter, true);
    }

    /**
     * Create RestTemplate with configured timeouts
     */
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
