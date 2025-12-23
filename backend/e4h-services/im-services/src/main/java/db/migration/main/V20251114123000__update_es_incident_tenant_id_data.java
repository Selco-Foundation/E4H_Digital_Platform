package db.migration.main;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.extern.slf4j.Slf4j;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManagerBuilder;
import org.apache.hc.client5.http.io.HttpClientConnectionManager;
import org.apache.hc.client5.http.ssl.SSLConnectionSocketFactory;
import org.apache.hc.client5.http.ssl.TrustAllStrategy;
import org.apache.hc.core5.ssl.SSLContextBuilder;
import org.flywaydb.core.api.migration.BaseJavaMigration;
import org.flywaydb.core.api.migration.Context;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;

import javax.net.ssl.SSLContext;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Base64;

@Slf4j
public class V20251114123000__update_es_incident_tenant_id_data extends BaseJavaMigration {

    private static final String[] ES_INDICES = {
            "computed-sla-im-services-write",
            "audit-computed-sla-im-services-write"
    };

    private static final String TARGET_TENANT = "in";

    @Override
    public boolean canExecuteInTransaction() {
        return false;
    }

    public void migrate(Context context) throws Exception {
        log.info("Starting migration: updating Elasticsearch documents tenantId to '{}'", TARGET_TENANT);

        RestTemplate restTemplate = createRestTemplateWithDisabledSSL();
        ObjectMapper objectMapper = new ObjectMapper();

        String logFileName = "es_incident_update_" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")) + ".log";
        Path logsDir = Paths.get("logs");
        Files.createDirectories(logsDir);
        Path logFilePath = logsDir.resolve(logFileName).toAbsolutePath().normalize();

        try (PrintWriter migrationLogger = initializeMigrationLogger(logFilePath)) {
            migrationLogger.println("========================================");
            migrationLogger.println("ELASTICSEARCH TENANT UPDATE LOG");
            migrationLogger.println("Started at: " + LocalDateTime.now());
            migrationLogger.println("========================================\n");
            migrationLogger.flush();

            String esHost = getEnvOrDefault("EGOV_ES_HOST", "https://localhost:9200");
            String esUsername = getEnvOrDefault("EGOV_ES_USERNAME", "");
            String esPassword = getEnvOrDefault("EGOV_ES_PASSWORD", "");

            log.info("Elasticsearch host: {}", esHost);

            int totalUpdated = 0;
            int totalFailed = 0;

            for (String indexName : ES_INDICES) {
                log.info("Processing index: {}", indexName);
                migrationLogger.println("========================================");
                migrationLogger.println("Processing Index: " + indexName);
                migrationLogger.println("========================================\n");
                migrationLogger.flush();

                try {
                    boolean success = updateDocumentsInEs(restTemplate, objectMapper, esHost, indexName, esUsername, esPassword, migrationLogger);
                    if (success) {
                        totalUpdated++;
                    } else {
                        totalFailed++;
                    }
                } catch (Exception e) {
                    totalFailed++;
                    log.error("Error updating index {}: {}", indexName, e.getMessage(), e);
                }
            }

            migrationLogger.println("========================================");
            migrationLogger.println("MIGRATION SUMMARY");
            migrationLogger.println("========================================");
            migrationLogger.println("Indexes Updated Successfully: " + totalUpdated);
            migrationLogger.println("Indexes Failed: " + totalFailed);
            migrationLogger.println("Completed at: " + LocalDateTime.now());
            migrationLogger.println("========================================\n");
            migrationLogger.flush();
        }

        log.info("Migration completed. Log file: {}", logFilePath);
    }

    private boolean updateDocumentsInEs(RestTemplate restTemplate,
                                        ObjectMapper objectMapper,
                                        String esHost,
                                        String indexName,
                                        String esUsername,
                                        String esPassword,
                                        PrintWriter migrationLogger) throws Exception {

        String updateByQueryUrl = esHost + "/" + indexName + "/_update_by_query?conflicts=proceed";

        ObjectNode updateRequest = objectMapper.createObjectNode();
        ObjectNode script = objectMapper.createObjectNode();
        script.put("source",
                "if (ctx._source.Data == null) { ctx._source.Data = [:]; } " +
                        "if (ctx._source.Data.incident == null) { ctx._source.Data.incident = [:]; } " +
                        "ctx._source.Data.tenantId = params.targetTenant; " +
                        "ctx._source.Data.incident.tenantId = params.targetTenant");

        ObjectNode params = objectMapper.createObjectNode();
        params.put("targetTenant", TARGET_TENANT);
        script.set("params", params);
        updateRequest.set("script", script);

        ObjectNode matchAll = objectMapper.createObjectNode();
        ObjectNode query = objectMapper.createObjectNode();
        query.set("match_all", matchAll);
        updateRequest.set("query", query);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        if (esUsername != null && !esUsername.isEmpty() && esPassword != null && !esPassword.isEmpty()) {
            String auth = esUsername + ":" + esPassword;
            String encoded = Base64.getEncoder().encodeToString(auth.getBytes(StandardCharsets.UTF_8));
            headers.set(HttpHeaders.AUTHORIZATION, "Basic " + encoded);
        }

        try {
            HttpEntity<String> httpEntity = new HttpEntity<>(objectMapper.writeValueAsString(updateRequest), headers);
            JsonNode response = restTemplate.postForObject(updateByQueryUrl, httpEntity, JsonNode.class);

            int updated = response != null ? response.path("updated").asInt(0) : 0;
            int total = response != null ? response.path("total").asInt(0) : 0;
            int failures = response != null && response.path("failures").isArray() ? response.path("failures").size() : 0;

            log.info("Index {}: updated {} of {} documents (failures: {})", indexName, updated, total, failures);

            migrationLogger.println(String.format("Index: %s | Updated: %d/%d | Failures: %d",
                    indexName, updated, total, failures));
            migrationLogger.flush();
            return true;
        } catch (HttpClientErrorException e) {
            log.error("Client error updating ES index {}: {}", indexName, e.getResponseBodyAsString(), e);
            migrationLogger.println(String.format("[FAILED] Index: %s | Reason: %s - %s",
                    indexName, e.getStatusCode(), e.getStatusText()));
            migrationLogger.println("Response: " + e.getResponseBodyAsString());
            migrationLogger.println();
            migrationLogger.flush();
            throw e;
        } catch (HttpServerErrorException e) {
            log.error("Server error updating ES index {}: {}", indexName, e.getResponseBodyAsString(), e);
            migrationLogger.println(String.format("[FAILED] Index: %s | Reason: %s - %s",
                    indexName, e.getStatusCode(), e.getStatusText()));
            migrationLogger.println("Response: " + e.getResponseBodyAsString());
            migrationLogger.println();
            migrationLogger.flush();
            throw e;
        } catch (Exception e) {
            log.error("Error updating ES index {}: {}", indexName, e.getMessage(), e);
            migrationLogger.println(String.format("[FAILED] Index: %s | Reason: %s",
                    indexName, e.getMessage()));
            migrationLogger.println();
            migrationLogger.flush();
            throw e;
        }
    }

    private PrintWriter initializeMigrationLogger(Path logPath) throws IOException {
        Files.createDirectories(logPath.getParent());
        FileWriter fileWriter = new FileWriter(logPath.toFile(), true);
        log.info("Migration log file created at {}", logPath);
        return new PrintWriter(fileWriter, true);
    }

    private String getEnvOrDefault(String key, String defaultValue) {
        String value = System.getenv(key);
        return (value == null || value.isEmpty()) ? defaultValue : value;
    }

    private RestTemplate createRestTemplateWithDisabledSSL() throws Exception {
        // Create SSL context that trusts all certificates
        SSLContext sslContext = SSLContextBuilder.create()
                .loadTrustMaterial(null, new TrustAllStrategy())  // Trust all certificates
                .build();

        // Create SSL socket factory with no hostname verification
        SSLConnectionSocketFactory sslSocketFactory = new SSLConnectionSocketFactory(
                sslContext,
                (hostname, session) -> true  // Accept all hostnames
        );

        // Create connection manager with SSL config
        HttpClientConnectionManager connectionManager = PoolingHttpClientConnectionManagerBuilder.create()
                .setSSLSocketFactory(sslSocketFactory)
                .build();

        // Build Apache HttpClient with SSL config
        CloseableHttpClient httpClient = HttpClients.custom()
                .setConnectionManager(connectionManager)
                .build();

        // Create request factory with Apache HttpClient and timeouts
        HttpComponentsClientHttpRequestFactory requestFactory = new HttpComponentsClientHttpRequestFactory(httpClient);
        requestFactory.setConnectTimeout(30000);  // 30 seconds connection timeout
        requestFactory.setConnectionRequestTimeout(30000);  // 30 seconds request timeout
        // Note: HttpClient5 doesn't have setReadTimeout, it uses socket timeout in the client configuration

        log.info("RestTemplate created with Apache HttpClient5, SSL verification disabled and extended timeouts");
        return new RestTemplate(requestFactory);
    }
}

