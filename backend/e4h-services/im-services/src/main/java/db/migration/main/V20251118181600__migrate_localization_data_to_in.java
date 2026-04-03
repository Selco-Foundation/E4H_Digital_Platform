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
import org.springframework.web.util.UriComponentsBuilder;

import java.io.FileWriter;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Slf4j
public class V20251118181600__migrate_localization_data_to_in extends BaseJavaMigration {

    private static final String TARGET_TENANT_ID = "in";
    private static final String TARGET_MODULE_FOR_STATE_MODULES = "rainmaker-in";

    // Helper class to hold tenant and locales
    private static class TenantLocale {
        String tenantId;
        List<String> locales;

        TenantLocale(String tenantId, List<String> locales) {
            this.tenantId = tenantId;
            this.locales = locales;
        }
    }

    // Common modules: keep module name, change tenantId to "in"
    private static final Map<String, List<TenantLocale>> COMMON_MODULES;
    static {
        Map<String, List<TenantLocale>> m = new LinkedHashMap<>();
        m.put("rainmaker-common", List.of(
            new TenantLocale("gj", List.of("gu_IN")),
            new TenantLocale("mz", List.of("lus_IN")),
            new TenantLocale("or", List.of("or_IN")),
            new TenantLocale("mh", List.of("mr_IN")),
            new TenantLocale("pg", List.of("ka_IN", "en_IN"))
        ));
        m.put("rainmaker-im", List.of(
            new TenantLocale("gj", List.of("gu_IN")),
            new TenantLocale("mz", List.of("lus_IN")),
            new TenantLocale("or", List.of("or_IN")),
            new TenantLocale("mh", List.of("mr_IN")),
            new TenantLocale("pg", List.of("ka_IN", "en_IN"))
        ));
        COMMON_MODULES = Collections.unmodifiableMap(m);
    }

    // State modules: change module to "rainmaker-in", change tenantId to "in"
    private static final Map<String, List<TenantLocale>> STATE_MODULES;
    static {
        Map<String, List<TenantLocale>> m = new LinkedHashMap<>();
        m.put("rainmaker-as", List.of(new TenantLocale("as", List.of("en_IN"))));
        m.put("rainmaker-ml", List.of(new TenantLocale("ml", List.of("en_IN"))));
        m.put("rainmaker-mn", List.of(new TenantLocale("mn", List.of("en_IN"))));
        m.put("rainmaker-nl", List.of(new TenantLocale("nl", List.of("en_IN"))));
        m.put("rainmaker-sk", List.of(new TenantLocale("sk", List.of("en_IN"))));
        m.put("rainmaker-gj", List.of(new TenantLocale("gj", List.of("en_IN", "gu_IN"))));
        m.put("rainmaker-mz", List.of(new TenantLocale("mz", List.of("en_IN", "lus_IN"))));
        m.put("rainmaker-or", List.of(new TenantLocale("or", List.of("en_IN", "or_IN"))));
        m.put("rainmaker-pg", List.of(new TenantLocale("pg", List.of("en_IN", "ka_IN"))));
        m.put("rainmaker-mh", List.of(new TenantLocale("mh", List.of("en_IN", "mr_IN"))));
        STATE_MODULES = Collections.unmodifiableMap(m);
    }

    @Override
    public boolean canExecuteInTransaction() {
        return false;
    }

    @Override
    public void migrate(Context context) throws Exception {
        log.info("Starting migration: Migrating localization data to tenant {}", TARGET_TENANT_ID);

        RestTemplate restTemplate = createRestTemplateWithTimeouts();
        ObjectMapper objectMapper = new ObjectMapper();

        // Initialize migration log file
        String logFileName = "localization_migration_" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")) + ".log";
        String logFilePath = "./logs/" + logFileName;
        String absoluteLogPath = Paths.get(logFilePath).toAbsolutePath().normalize().toString();

        try (PrintWriter migrationLogger = initializeMigrationLogger(logFilePath, absoluteLogPath)) {
            migrationLogger.println("========================================");
            migrationLogger.println("LOCALIZATION DATA MIGRATION LOG");
            migrationLogger.println("Target Tenant: " + TARGET_TENANT_ID);
            migrationLogger.println("Started at: " + LocalDateTime.now());
            migrationLogger.println("========================================\n");
            migrationLogger.flush();

            String localizationHost = getEnvOrDefault("EGOV_LOCALIZATION_HOST", "http://localhost:8095");
            String authToken = getEnvOrDefault("EGOV_AUTH_TOKEN", "");
            int sleepMs = Integer.parseInt(getEnvOrDefault("EGOV_LOCALIZATION_UPDATE_DELAY_MS", "0"));

            log.info("Localization Host: {}", localizationHost);

            int totalMessagesMigrated = 0;
            int totalMessagesSkipped = 0;

            // Process common modules
            migrationLogger.println("\n========================================");
            migrationLogger.println("PROCESSING COMMON MODULES");
            migrationLogger.println("(Keeping module name, changing tenantId to 'in')");
            migrationLogger.println("========================================\n");
            migrationLogger.flush();

            int commonMigrated = migrateCommonModules(
                restTemplate, objectMapper, localizationHost, authToken, sleepMs, migrationLogger
            );
            totalMessagesMigrated += commonMigrated;

            // Process state modules
            migrationLogger.println("\n========================================");
            migrationLogger.println("PROCESSING STATE MODULES");
            migrationLogger.println("(Changing module to 'rainmaker-in', changing tenantId to 'in')");
            migrationLogger.println("========================================\n");
            migrationLogger.flush();

            int stateMigrated = migrateStateModules(
                restTemplate, objectMapper, localizationHost, authToken, sleepMs, migrationLogger
            );
            totalMessagesMigrated += stateMigrated;

            // Final summary
            migrationLogger.println("\n========================================");
            migrationLogger.println("MIGRATION SUMMARY");
            migrationLogger.println("========================================");
            migrationLogger.println("Total Messages Migrated: " + totalMessagesMigrated);
            migrationLogger.println("Total Messages Skipped: " + totalMessagesSkipped);
            migrationLogger.println("\nCompleted at: " + LocalDateTime.now());
            migrationLogger.println("========================================\n");
            migrationLogger.flush();

            log.info("Migration completed");
            log.info("Total messages migrated: {}", totalMessagesMigrated);
            log.info("Migration log file: {}", absoluteLogPath);
        }
    }

    /**
     * Migrate common modules
     * Keep module name, change tenantId to "in"
     */
    private int migrateCommonModules(
        RestTemplate restTemplate,
        ObjectMapper objectMapper,
        String localizationHost,
        String authToken,
        int sleepMs,
        PrintWriter migrationLogger
    ) throws Exception {
        int totalMigrated = 0;

        for (Map.Entry<String, List<TenantLocale>> moduleEntry : COMMON_MODULES.entrySet()) {
            String module = moduleEntry.getKey();
            List<TenantLocale> tenantLocales = moduleEntry.getValue();

            migrationLogger.println("\n--- Processing Module: " + module + " ---");
            migrationLogger.flush();
            log.info("Processing module: {}", module);

            for (TenantLocale tenantLocale : tenantLocales) {
                String sourceTenantId = tenantLocale.tenantId;
                List<String> locales = tenantLocale.locales;

                migrationLogger.println("  Tenant: " + sourceTenantId);
                migrationLogger.flush();

                for (String locale : locales) {
                    try {
                        // Search for messages
                        ArrayNode messages = searchLocalizationMessages(
                            restTemplate, objectMapper, localizationHost, module, sourceTenantId, locale
                        );

                        if (messages != null && !messages.isEmpty()) {
                            log.info("Found {} messages for module: {}, tenant: {}, locale: {}", 
                                messages.size(), module, sourceTenantId, locale);

                            // Upsert messages with tenantId changed to "in", keeping module name
                            int migrated = upsertLocalizationMessages(
                                restTemplate, objectMapper, localizationHost, module, TARGET_TENANT_ID,
                                messages, authToken, migrationLogger
                            );
                            totalMigrated += migrated;

                            migrationLogger.println("    Locale: " + locale + " - Migrated: " + migrated + " messages");
                            migrationLogger.flush();
                        } else {
                            log.warn("No messages found for module: {}, tenant: {}, locale: {}", 
                                module, sourceTenantId, locale);
                            migrationLogger.println("    Locale: " + locale + " - No messages found");
                            migrationLogger.flush();
                        }

                        // Small delay to avoid overwhelming the service
                        if (sleepMs > 0) {
                            Thread.sleep(sleepMs);
                        }

                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        throw e;
                    } catch (Exception e) {
                        log.error("Error processing module: {}, tenant: {}, locale: {} - {}", 
                            module, sourceTenantId, locale, e.getMessage(), e);
                        migrationLogger.println("    Locale: " + locale + " - ERROR: " + e.getMessage());
                        migrationLogger.flush();
                    }
                }
            }
        }

        return totalMigrated;
    }

    /**
     * Migrate state modules
     * Change module to "rainmaker-in", change tenantId to "in"
     */
    private int migrateStateModules(
        RestTemplate restTemplate,
        ObjectMapper objectMapper,
        String localizationHost,
        String authToken,
        int sleepMs,
        PrintWriter migrationLogger
    ) throws Exception {
        int totalMigrated = 0;

        for (Map.Entry<String, List<TenantLocale>> moduleEntry : STATE_MODULES.entrySet()) {
            String sourceModule = moduleEntry.getKey();
            List<TenantLocale> tenantLocales = moduleEntry.getValue();

            migrationLogger.println("\n--- Processing Module: " + sourceModule + " ---");
            migrationLogger.flush();
            log.info("Processing module: {}", sourceModule);

            for (TenantLocale tenantLocale : tenantLocales) {
                String sourceTenantId = tenantLocale.tenantId;
                List<String> locales = tenantLocale.locales;

                migrationLogger.println("  Tenant: " + sourceTenantId);
                migrationLogger.flush();

                for (String locale : locales) {
                    try {
                        // Search for messages
                        ArrayNode messages = searchLocalizationMessages(
                            restTemplate, objectMapper, localizationHost, sourceModule, sourceTenantId, locale
                        );

                        if (messages != null && !messages.isEmpty()) {
                            log.info(
                                    "Found {} messages for module: {}, tenant: {}, locale: {}",
                                    messages.size(), sourceModule, sourceTenantId, locale
                            );

                            // Update module name in messages to "rainmaker-in"
                            ArrayNode updatedMessages = objectMapper.createArrayNode();
                            for (int i = 0; i < messages.size(); i++) {
                                ObjectNode message = (ObjectNode) messages.get(i);
                                ObjectNode updatedMessage = message.deepCopy();
                                updatedMessage.put("module", TARGET_MODULE_FOR_STATE_MODULES);
                                updatedMessages.add(updatedMessage);
                            }

                            // Upsert messages with tenantId changed to "in" and module changed to "rainmaker-in"
                            int migrated = upsertLocalizationMessages(
                                restTemplate, objectMapper, localizationHost, TARGET_MODULE_FOR_STATE_MODULES, TARGET_TENANT_ID,
                                updatedMessages, authToken, migrationLogger
                            );
                            totalMigrated += migrated;

                            migrationLogger.println("    Locale: " + locale + " - Migrated: " + migrated + " messages");
                            migrationLogger.flush();
                        } else {
                            log.warn(
                                    "No messages found for module: {}, tenant: {}, locale: {}",
                                    sourceModule, sourceTenantId, locale
                            );
                            migrationLogger.println("    Locale: " + locale + " - No messages found");
                            migrationLogger.flush();
                        }

                        // Small delay to avoid overwhelming the service
                        if (sleepMs > 0) {
                            Thread.sleep(sleepMs);
                        }

                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        throw e;
                    } catch (Exception e) {
                        log.error("Error processing module: {}, tenant: {}, locale: {} - {}", 
                            sourceModule, sourceTenantId, locale, e.getMessage(), e);
                        migrationLogger.println("    Locale: " + locale + " - ERROR: " + e.getMessage());
                        migrationLogger.flush();
                    }
                }
            }
        }

        return totalMigrated;
    }

    /**
     * Search for localization messages
     */
    private ArrayNode searchLocalizationMessages(
        RestTemplate restTemplate,
        ObjectMapper objectMapper,
        String localizationHost,
        String module,
        String tenantId,
        String locale
    ) throws Exception {
        String searchUrl = UriComponentsBuilder.fromHttpUrl(localizationHost + "/localization/messages/v1/_search")
            .queryParam("locale", locale)
            .queryParam("module", module)
            .queryParam("tenantId", tenantId)
            .build()
            .toUriString();

        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<String> httpEntity = new HttpEntity<>("{}", headers);

            log.debug("Searching localization messages: module={}, tenantId={}, locale={}", module, tenantId, locale);
            ResponseEntity<String> response = restTemplate.exchange(
                searchUrl,
                HttpMethod.POST,
                httpEntity,
                String.class
            );

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                JsonNode responseBody = objectMapper.readTree(response.getBody());
                ArrayNode messages = (ArrayNode) responseBody.path("messages");
                return messages;
            } else {
                log.warn("Localization search failed: module={}, tenantId={}, locale={} - Status: {}", 
                    module, tenantId, locale, response.getStatusCode());
                return null;
            }

        } catch (HttpClientErrorException | HttpServerErrorException e) {
            log.error("HTTP error searching localization: module={}, tenantId={}, locale={} - {} - {}", 
                module, tenantId, locale, e.getStatusCode(), e.getResponseBodyAsString());
            return null;
        } catch (Exception e) {
            log.error("Error searching localization: module={}, tenantId={}, locale={} - {}", 
                module, tenantId, locale, e.getMessage(), e);
            return null;
        }
    }

    /**
     * Upsert localization messages
     */
    private int upsertLocalizationMessages(
        RestTemplate restTemplate,
        ObjectMapper objectMapper,
        String localizationHost,
        String module,
        String tenantId,
        ArrayNode messages,
        String authToken,
        PrintWriter migrationLogger
    ) {
        String upsertUrl = localizationHost + "/localization/messages/v1/_upsert";

        // Build request body
        ObjectNode requestBody = objectMapper.createObjectNode();
        requestBody.set("RequestInfo", buildRequestInfoNode(objectMapper, authToken));
        requestBody.put("tenantId", tenantId);
        requestBody.set("messages", messages);

        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<String> httpEntity = new HttpEntity<>(objectMapper.writeValueAsString(requestBody), headers);

            log.debug("Upserting {} messages for module: {}, tenantId: {}", messages.size(), module, tenantId);
            ResponseEntity<String> response = restTemplate.exchange(
                upsertUrl,
                HttpMethod.POST,
                httpEntity,
                String.class
            );

            if (response.getStatusCode().is2xxSuccessful()) {
                log.debug("✓ Successfully upserted {} messages for module: {}, tenantId: {}", 
                    messages.size(), module, tenantId);
                return messages.size();
            } else {
                log.warn("Upsert failed for module: {}, tenantId: {} - Status: {} - {}", 
                    module, tenantId, response.getStatusCode(), response.getBody());
                migrationLogger.println("      [FAILED] Upsert - Status: " + response.getStatusCode());
                migrationLogger.flush();
                return 0;
            }

        } catch (HttpClientErrorException | HttpServerErrorException e) {
            log.error("HTTP error upserting messages for module: {}, tenantId: {} - {} - {}", 
                module, tenantId, e.getStatusCode(), e.getResponseBodyAsString());
            migrationLogger.println("      [FAILED] Upsert HTTP error: " + e.getStatusCode() + " - " + e.getResponseBodyAsString());
            migrationLogger.flush();
            return 0;
        } catch (Exception e) {
            log.error("Error upserting messages for module: {}, tenantId: {} - {}", 
                module, tenantId, e.getMessage(), e);
            migrationLogger.println("      [FAILED] Upsert exception: " + e.getMessage());
            migrationLogger.flush();
            return 0;
        }
    }

    /**
     * Build RequestInfo node for API calls
     */
    private ObjectNode buildRequestInfoNode(ObjectMapper objectMapper, String authToken) {
        ObjectNode requestInfo = objectMapper.createObjectNode();
        requestInfo.put("apiId", "Rainmaker");
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
