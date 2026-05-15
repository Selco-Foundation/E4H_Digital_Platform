package db.migration.main;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
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

/**
 * Aligns Elasticsearch / Kibana incident documents with the PostgreSQL rename of
 * Grid|GRID / LowVoltage → ReverseVoltage (see V20260507120000__rename_grid_low_to_reverse_voltage.sql).
 * Matches both {@code Grid} and {@code GRID} incident types in Elasticsearch.
 * <p>
 * Targets the same indices as other IM ticket ES migrations. Requires {@code EGOV_ES_HOST}
 * (and optional {@code EGOV_ES_USERNAME} / {@code EGOV_ES_PASSWORD}) at deploy time, consistent with
 * {@link V20260226130000__update_es_warranty_status}.
 */
@Slf4j
public class V20260513103000__update_es_grid_lowvoltage_to_reverse_voltage extends BaseJavaMigration {

    private static final String[] ES_INDICES = {
            "computed-sla-im-services-write",
            "audit-computed-sla-im-services-write"
    };

    /** Historical tickets use either casing for grid incident type. */
    private static final String[] GRID_INCIDENT_TYPES = {"Grid", "GRID"};

    @Override
    public boolean canExecuteInTransaction() {
        return false;
    }

    @Override
    public void migrate(Context context) throws Exception {
        log.info("Starting migration: ES — GRID LowVoltage → ReverseVoltage (Kibana / ticket index)");

        RestTemplate restTemplate = createRestTemplateWithDisabledSSL();
        ObjectMapper objectMapper = new ObjectMapper();

        String logFileName = "es_grid_reverse_voltage_rename_" + LocalDateTime.now()
                .format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")) + ".log";
        Path logsDir = Paths.get("logs");
        Files.createDirectories(logsDir);
        Path logFilePath = logsDir.resolve(logFileName).toAbsolutePath().normalize();

        try (PrintWriter migrationLogger = initializeMigrationLogger(logFilePath)) {
            migrationLogger.println("========================================");
            migrationLogger.println("ELASTICSEARCH GRID SUBTYPE RENAME (LowVoltage → ReverseVoltage)");
            migrationLogger.println("Started at: " + LocalDateTime.now());
            migrationLogger.println("========================================\n");
            migrationLogger.flush();

            String esHost = getEnvOrDefault("EGOV_ES_HOST", "https://localhost:9200");
            String esUsername = getEnvOrDefault("EGOV_ES_USERNAME", "");
            String esPassword = getEnvOrDefault("EGOV_ES_PASSWORD", "");

            log.info("Elasticsearch host: {}", esHost);

            int ok = 0;
            int failed = 0;

            for (String indexName : ES_INDICES) {
                migrationLogger.println("Index: " + indexName);
                migrationLogger.flush();
                try {
                    updateIndex(restTemplate, objectMapper, esHost, indexName, esUsername, esPassword, migrationLogger);
                    ok++;
                } catch (Exception e) {
                    failed++;
                    log.error("Failed updating index {}: {}", indexName, e.getMessage(), e);
                    migrationLogger.println("[FAILED] " + indexName + " — " + e.getMessage());
                    migrationLogger.flush();
                    throw e;
                }
            }

            migrationLogger.println("\n========================================");
            migrationLogger.println("SUMMARY: indexes succeeded=" + ok + ", failed=" + failed);
            migrationLogger.println("Completed at: " + LocalDateTime.now());
            migrationLogger.println("========================================\n");
            migrationLogger.flush();
        }

        log.info("Migration completed. Log: {}", logFilePath);
    }

    private void updateIndex(RestTemplate restTemplate,
                             ObjectMapper objectMapper,
                             String esHost,
                             String indexName,
                             String esUsername,
                             String esPassword,
                             PrintWriter migrationLogger) throws Exception {

        String updateByQueryUrl = esHost + "/" + indexName + "/_update_by_query?conflicts=proceed&refresh=true";

        ObjectNode updateRequest = objectMapper.createObjectNode();
        ObjectNode script = objectMapper.createObjectNode();
        script.put("source",
                "if (ctx._source.Data == null) { ctx._source.Data = [:]; } "
                        + "if (ctx._source.Data.incident == null) { ctx._source.Data.incident = [:]; } "
                        + "ctx._source.Data.incident.incidentSubType = params.newSubType; "
                        + "ctx._source.Data.incident.incidentSubType_localized = params.newSubTypeLocalized; "
                        + "if (ctx._source.Data.incidentSubType != null) { "
                        + "  ctx._source.Data.incidentSubType = params.newSubType; "
                        + "} "
                        + "if (ctx._source.Data.indexView == null) { ctx._source.Data.indexView = [:]; } "
                        + "ctx._source.Data.indexView.incidentSubType_localized = params.newSubTypeLocalized;");
        ObjectNode params = objectMapper.createObjectNode();
        params.put("newSubType", "ReverseVoltage");
        params.put("newSubTypeLocalized", "Reverse Voltage");
        script.set("params", params);
        updateRequest.set("script", script);

        // incidentType = Grid OR GRID (keyword and raw field per casing)
        ArrayNode typeShould = objectMapper.createArrayNode();
        for (String gridType : GRID_INCIDENT_TYPES) {
            ObjectNode typeTermKw = objectMapper.createObjectNode();
            typeTermKw.set("Data.incident.incidentType.keyword",
                    objectMapper.createObjectNode().put("value", gridType));
            typeShould.add(objectMapper.createObjectNode().set("term", typeTermKw));
            ObjectNode typeTermRaw = objectMapper.createObjectNode();
            typeTermRaw.set("Data.incident.incidentType",
                    objectMapper.createObjectNode().put("value", gridType));
            typeShould.add(objectMapper.createObjectNode().set("term", typeTermRaw));
        }
        ObjectNode typeBool = objectMapper.createObjectNode();
        typeBool.set("should", typeShould);
        typeBool.put("minimum_should_match", 1);

        // Match rows still on LowVoltage OR already ReverseVoltage but localized label not updated yet
        ArrayNode subShould = objectMapper.createArrayNode();

        ObjectNode subLowKw = objectMapper.createObjectNode();
        subLowKw.set("Data.incident.incidentSubType.keyword",
                objectMapper.createObjectNode().put("value", "LowVoltage"));
        subShould.add(objectMapper.createObjectNode().set("term", subLowKw));
        ObjectNode subLowRaw = objectMapper.createObjectNode();
        subLowRaw.set("Data.incident.incidentSubType",
                objectMapper.createObjectNode().put("value", "LowVoltage"));
        subShould.add(objectMapper.createObjectNode().set("term", subLowRaw));

        ObjectNode locLowKw = objectMapper.createObjectNode();
        locLowKw.set("Data.incident.incidentSubType_localized.keyword",
                objectMapper.createObjectNode().put("value", "Low Voltage"));
        subShould.add(objectMapper.createObjectNode().set("term", locLowKw));
        ObjectNode locLowRaw = objectMapper.createObjectNode();
        locLowRaw.set("Data.incident.incidentSubType_localized",
                objectMapper.createObjectNode().put("value", "Low Voltage"));
        subShould.add(objectMapper.createObjectNode().set("term", locLowRaw));

        ArrayNode reverseLabelMust = objectMapper.createArrayNode();
        ObjectNode revSubKw = objectMapper.createObjectNode();
        revSubKw.set("Data.incident.incidentSubType.keyword",
                objectMapper.createObjectNode().put("value", "ReverseVoltage"));
        reverseLabelMust.add(objectMapper.createObjectNode().set("term", revSubKw));
        ObjectNode revLocKw = objectMapper.createObjectNode();
        revLocKw.set("Data.incident.incidentSubType_localized.keyword",
                objectMapper.createObjectNode().put("value", "Low Voltage"));
        reverseLabelMust.add(objectMapper.createObjectNode().set("term", revLocKw));
        ObjectNode reverseBool = objectMapper.createObjectNode();
        reverseBool.set("must", reverseLabelMust);
        subShould.add(objectMapper.createObjectNode().set("bool", reverseBool));

        ObjectNode subBool = objectMapper.createObjectNode();
        subBool.set("should", subShould);
        subBool.put("minimum_should_match", 1);

        ArrayNode mustArray = objectMapper.createArrayNode();
        mustArray.add(objectMapper.createObjectNode().set("bool", typeBool));
        mustArray.add(objectMapper.createObjectNode().set("bool", subBool));

        ObjectNode bool = objectMapper.createObjectNode();
        bool.set("must", mustArray);
        updateRequest.set("query", objectMapper.createObjectNode().set("bool", bool));

        HttpHeaders headers = buildAuthHeaders(esUsername, esPassword);
        HttpEntity<String> httpEntity = new HttpEntity<>(objectMapper.writeValueAsString(updateRequest), headers);

        JsonNode response = restTemplate.postForObject(updateByQueryUrl, httpEntity, JsonNode.class);
        int updated = response != null ? response.path("updated").asInt(0) : 0;
        int total = response != null ? response.path("total").asInt(0) : 0;
        int failures = response != null && response.path("failures").isArray() ? response.path("failures").size() : 0;

        log.info("Index {}: updated {} of {} documents (failures: {})", indexName, updated, total, failures);
        migrationLogger.println(String.format("  updated=%d total=%d failures=%d", updated, total, failures));
        migrationLogger.flush();

        if (updated == 0 && total == 0) {
            log.warn("Index {}: no documents matched (Grid|GRID) with LowVoltage or stale 'Low Voltage' label. "
                    + "Verify incidentType and incidentSubType field paths.", indexName);
        }
    }

    private HttpHeaders buildAuthHeaders(String esUsername, String esPassword) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        if (esUsername != null && !esUsername.isEmpty() && esPassword != null && !esPassword.isEmpty()) {
            String auth = esUsername + ":" + esPassword;
            String encoded = Base64.getEncoder().encodeToString(auth.getBytes(StandardCharsets.UTF_8));
            headers.set(HttpHeaders.AUTHORIZATION, "Basic " + encoded);
        }
        return headers;
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
        SSLContext sslContext = SSLContextBuilder.create()
                .loadTrustMaterial(null, new TrustAllStrategy())
                .build();

        SSLConnectionSocketFactory sslSocketFactory = new SSLConnectionSocketFactory(
                sslContext,
                (hostname, session) -> true
        );

        HttpClientConnectionManager connectionManager = PoolingHttpClientConnectionManagerBuilder.create()
                .setSSLSocketFactory(sslSocketFactory)
                .build();

        CloseableHttpClient httpClient = HttpClients.custom()
                .setConnectionManager(connectionManager)
                .build();

        HttpComponentsClientHttpRequestFactory requestFactory = new HttpComponentsClientHttpRequestFactory(httpClient);
        requestFactory.setConnectTimeout(60000);
        requestFactory.setConnectionRequestTimeout(60000);

        log.info("RestTemplate created with SSL verification disabled (ES migration)");
        return new RestTemplate(requestFactory);
    }
}
