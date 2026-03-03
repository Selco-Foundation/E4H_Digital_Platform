package db.migration.main;

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
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Slf4j
public class V20260226233000__add_warranty_status_localizations extends BaseJavaMigration {

    private static final String TARGET_TENANT_ID = "in";
    private static final String TARGET_MODULE = "rainmaker-im";

    @Override
    public boolean canExecuteInTransaction() {
        return false;
    }

    @Override
    public void migrate(Context context) throws Exception {
        log.info("Starting migration: adding warranty status localizations for tenant {}", TARGET_TENANT_ID);

        RestTemplate restTemplate = createRestTemplateWithTimeouts();
        ObjectMapper objectMapper = new ObjectMapper();

        String logFileName = "warranty_status_localization_" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")) + ".log";
        Path logsDir = Paths.get("logs");
        Files.createDirectories(logsDir);
        Path logFilePath = logsDir.resolve(logFileName).toAbsolutePath().normalize();

        try (PrintWriter migrationLogger = initializeMigrationLogger(logFilePath)) {
            migrationLogger.println("========================================");
            migrationLogger.println("WARRANTY STATUS LOCALIZATION MIGRATION LOG");
            migrationLogger.println("Target Tenant: " + TARGET_TENANT_ID);
            migrationLogger.println("Target Module: " + TARGET_MODULE);
            migrationLogger.println("Started at: " + LocalDateTime.now());
            migrationLogger.println("========================================\n");
            migrationLogger.flush();

            String localizationHost = getEnvOrDefault("EGOV_LOCALIZATION_HOST", "http://localhost:8095");
            String authToken = getEnvOrDefault("EGOV_AUTH_TOKEN", "");

            int created = upsertWarrantyMessages(restTemplate, objectMapper, localizationHost, authToken, migrationLogger);

            migrationLogger.println("\n========================================");
            migrationLogger.println("MIGRATION SUMMARY");
            migrationLogger.println("========================================");
            migrationLogger.println("Messages Created/Updated: " + created);
            migrationLogger.println("Completed at: " + LocalDateTime.now());
            migrationLogger.println("========================================\n");
            migrationLogger.flush();

            log.info("Migration completed. Log file: {}", logFilePath);
        }
    }

    private int upsertWarrantyMessages(RestTemplate restTemplate,
                                       ObjectMapper objectMapper,
                                       String localizationHost,
                                       String authToken,
                                       PrintWriter migrationLogger) {
        String upsertUrl = localizationHost + "/localization/messages/v1/_upsert";

        ObjectNode requestBody = objectMapper.createObjectNode();
        requestBody.set("RequestInfo", buildRequestInfoNode(objectMapper, authToken));
        requestBody.put("tenantId", TARGET_TENANT_ID);

        ArrayNode messages = objectMapper.createArrayNode();

        ObjectNode within = objectMapper.createObjectNode();
        within.put("code", "CS_COMMON_WITHIN_WARRANTY");
        within.put("message", "Within warranty");
        within.put("module", TARGET_MODULE);
        within.put("locale", "en_IN");
        messages.add(within);

        ObjectNode out = objectMapper.createObjectNode();
        out.put("code", "CS_COMMON_OUT_OF_WARRANTY");
        out.put("message", "Out of warranty");
        out.put("module", TARGET_MODULE);
        out.put("locale", "en_IN");
        messages.add(out);

        requestBody.set("messages", messages);

        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<String> httpEntity = new HttpEntity<>(objectMapper.writeValueAsString(requestBody), headers);

            ResponseEntity<String> response = restTemplate.postForEntity(upsertUrl, httpEntity, String.class);

            if (response.getStatusCode().is2xxSuccessful()) {
                log.info("Successfully upserted warranty status localizations");
                migrationLogger.println("Successfully upserted warranty status localizations");
                migrationLogger.flush();
                return messages.size();
            } else {
                log.warn("Upsert failed for warranty status localizations - Status: {} - {}", response.getStatusCode(), response.getBody());
                migrationLogger.println("Upsert failed for warranty status localizations - Status: " + response.getStatusCode());
                migrationLogger.flush();
                return 0;
            }
        } catch (HttpClientErrorException | HttpServerErrorException e) {
            log.error("HTTP error upserting warranty status localizations: {} - {}", e.getStatusCode(), e.getResponseBodyAsString());
            migrationLogger.println("HTTP error upserting warranty status localizations: " + e.getStatusCode());
            migrationLogger.println(e.getResponseBodyAsString());
            migrationLogger.flush();
            return 0;
        } catch (Exception e) {
            log.error("Error upserting warranty status localizations: {}", e.getMessage(), e);
            migrationLogger.println("Error upserting warranty status localizations: " + e.getMessage());
            migrationLogger.flush();
            return 0;
        }
    }

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
        requestInfo.set("userInfo", userInfo);

        return requestInfo;
    }

    private PrintWriter initializeMigrationLogger(Path logPath) throws IOException {
        Files.createDirectories(logPath.getParent());
        FileWriter fileWriter = new FileWriter(logPath.toFile(), true);
        log.info("Migration log file created at {}", logPath);
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

    private String getEnvOrDefault(String key, String def) {
        String v = System.getenv(key);
        return (v == null || v.isEmpty()) ? def : v;
    }
}
