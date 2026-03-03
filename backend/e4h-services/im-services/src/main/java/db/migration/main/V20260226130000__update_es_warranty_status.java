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
import org.springframework.http.ResponseEntity;
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
import java.util.HashMap;
import java.util.Map;

@Slf4j
public class V20260226130000__update_es_warranty_status extends BaseJavaMigration {

    private static final String MAIN_INDEX = "computed-sla-im-services-write";
    private static final String AUDIT_INDEX = "audit-computed-sla-im-services-write";

    @Override
    public boolean canExecuteInTransaction() {
        return false;
    }

    @Override
    public void migrate(Context context) throws Exception {
        log.info("Starting migration: updating Elasticsearch documents with warrantyStatus");

        RestTemplate restTemplate = createRestTemplateWithDisabledSSL();
        ObjectMapper objectMapper = new ObjectMapper();

        String logFileName = "es_warranty_status_update_" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")) + ".log";
        Path logsDir = Paths.get("logs");
        Files.createDirectories(logsDir);
        Path logFilePath = logsDir.resolve(logFileName).toAbsolutePath().normalize();

        try (PrintWriter migrationLogger = initializeMigrationLogger(logFilePath)) {
            migrationLogger.println("========================================");
            migrationLogger.println("ELASTICSEARCH WARRANTY STATUS UPDATE LOG");
            migrationLogger.println("Audit index: from first OUT_OF_WARRANTY action onwards -> OUT_OF_WARRANTY; until then -> WITHIN_WARRANTY");
            migrationLogger.println("Started at: " + LocalDateTime.now());
            migrationLogger.println("========================================\n");
            migrationLogger.flush();

            String esHost = getEnvOrDefault("EGOV_ES_HOST", "https://localhost:9200");
            String esUsername = getEnvOrDefault("EGOV_ES_USERNAME", "");
            String esPassword = getEnvOrDefault("EGOV_ES_PASSWORD", "");

            log.info("Elasticsearch host: {}", esHost);

            int totalIndexesSucceeded = 0;
            int totalIndexesFailed = 0;

            // 1. Main index: one doc per incident; set warrantyStatus from applicationStatus
            migrationLogger.println("Processing index: " + MAIN_INDEX);
            migrationLogger.flush();
            try {
                boolean success = updateMainIndex(restTemplate, objectMapper, esHost, MAIN_INDEX, esUsername, esPassword, migrationLogger);
                if (success) totalIndexesSucceeded++; else totalIndexesFailed++;
            } catch (Exception e) {
                totalIndexesFailed++;
                log.error("Error updating main index {}: {}", MAIN_INDEX, e.getMessage(), e);
                migrationLogger.println("[FAILED] " + MAIN_INDEX + " - " + e.getMessage());
                migrationLogger.flush();
            }

            // 2. Audit index: per-incident cutoff; from first OUT_OF_WARRANTY transition onwards -> OUT_OF_WARRANTY
            migrationLogger.println("\nProcessing index: " + AUDIT_INDEX + " (per-incident cutoff)");
            migrationLogger.flush();
            try {
                boolean success = updateAuditIndex(restTemplate, objectMapper, esHost, AUDIT_INDEX, esUsername, esPassword, migrationLogger);
                if (success) totalIndexesSucceeded++; else totalIndexesFailed++;
            } catch (Exception e) {
                totalIndexesFailed++;
                log.error("Error updating audit index {}: {}", AUDIT_INDEX, e.getMessage(), e);
                migrationLogger.println("[FAILED] " + AUDIT_INDEX + " - " + e.getMessage());
                migrationLogger.flush();
            }

            migrationLogger.println("\n========================================");
            migrationLogger.println("MIGRATION SUMMARY");
            migrationLogger.println("========================================");
            migrationLogger.println("Indexes Updated Successfully: " + totalIndexesSucceeded);
            migrationLogger.println("Indexes Failed: " + totalIndexesFailed);
            migrationLogger.println("Completed at: " + LocalDateTime.now());
            migrationLogger.println("========================================\n");
            migrationLogger.flush();

            log.info("Migration completed. Log file: {}", logFilePath);
        }
    }

    /**
     * Main index: single document per incident. Set warrantyStatus from Data.incident.applicationStatus.
     */
    private boolean updateMainIndex(RestTemplate restTemplate,
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
                        "String within = params.withinWarranty; " +
                        "String out = params.outOfWarranty; " +
                        "String value = within; " +
                        "def appStatus = ctx._source.Data.incident.applicationStatus; " +
                        "if (appStatus != null && appStatus.contains('OUT_OF_WARRANTY')) { value = out; } " +
                        "ctx._source.Data.incident.warrantyStatus = value; " +
                        "ctx._source.Data.warrantyStatus = value;");

        ObjectNode params = objectMapper.createObjectNode();
        params.put("withinWarranty", "WITHIN_WARRANTY");
        params.put("outOfWarranty", "OUT_OF_WARRANTY");
        script.set("params", params);
        updateRequest.set("script", script);

        ObjectNode matchAll = objectMapper.createObjectNode();
        ObjectNode query = objectMapper.createObjectNode();
        query.set("match_all", matchAll);
        updateRequest.set("query", query);

        HttpHeaders headers = buildAuthHeaders(esUsername, esPassword);
        HttpEntity<String> httpEntity = new HttpEntity<>(objectMapper.writeValueAsString(updateRequest), headers);
        ResponseEntity<JsonNode> response = restTemplate.postForEntity(updateByQueryUrl, httpEntity, JsonNode.class);

        JsonNode body = response.getBody();
        int updated = body != null ? body.path("updated").asInt(0) : 0;
        int total = body != null ? body.path("total").asInt(0) : 0;
        int failures = body != null && body.path("failures").isArray() ? body.path("failures").size() : 0;

        log.info("Index {}: updated {} of {} documents (failures: {})", indexName, updated, total, failures);
        migrationLogger.println(String.format("Index: %s | Updated: %d/%d | Failures: %d", indexName, updated, total, failures));
        migrationLogger.flush();
        return true;
    }

    /**
     * Audit index: multiple docs per incident (one per state transition).
     * Rule: from the first OUT_OF_WARRANTY transition onwards -> warrantyStatus = OUT_OF_WARRANTY; until then -> WITHIN_WARRANTY.
     * We aggregate per incident the min(lastModifiedTime) where endingStatus contains OUT_OF_WARRANTY, then update each doc by that cutoff.
     */
    private boolean updateAuditIndex(RestTemplate restTemplate,
                                     ObjectMapper objectMapper,
                                     String esHost,
                                     String indexName,
                                     String esUsername,
                                     String esPassword,
                                     PrintWriter migrationLogger) throws Exception {

        HttpHeaders headers = buildAuthHeaders(esUsername, esPassword);

        // Build aggregation: terms by incidentId, then filter where endingStatus contains OUT_OF_WARRANTY, then min(lastModifiedTime)
        ObjectNode searchBody = objectMapper.createObjectNode();
        searchBody.put("size", 0);

        ObjectNode aggs = objectMapper.createObjectNode();
        ObjectNode termsNode = objectMapper.createObjectNode();
        termsNode.put("field", "Data.incident.incidentId.keyword");
        termsNode.put("size", 10000);
        ObjectNode byIncident = objectMapper.createObjectNode();
        byIncident.set("terms", termsNode);
        ObjectNode matchPhrase = objectMapper.createObjectNode();
        matchPhrase.put("Data.endingStatus", "OUT_OF_WARRANTY");
        ObjectNode filterNode = objectMapper.createObjectNode();
        filterNode.set("match_phrase", matchPhrase);
        ObjectNode minAgg = objectMapper.createObjectNode();
        minAgg.put("field", "Data.auditDetails.lastModifiedTime");
        ObjectNode minTs = objectMapper.createObjectNode();
        minTs.set("min", minAgg);
        ObjectNode oowAggs = objectMapper.createObjectNode();
        oowAggs.set("min_ts", minTs);
        ObjectNode oowFilter = objectMapper.createObjectNode();
        oowFilter.set("filter", filterNode);
        oowFilter.set("aggs", oowAggs);
        byIncident.set("aggs", objectMapper.createObjectNode().set("out_of_warranty_times", oowFilter));
        aggs.set("by_incident", byIncident);
        searchBody.set("aggs", aggs);

        String searchUrl = esHost + "/" + indexName + "/_search";
        HttpEntity<String> searchEntity = new HttpEntity<>(objectMapper.writeValueAsString(searchBody), headers);
        ResponseEntity<JsonNode> searchResponse = restTemplate.postForEntity(searchUrl, searchEntity, JsonNode.class);
        JsonNode searchResp = searchResponse.getBody();
        if (searchResp == null || !searchResp.has("aggregations")) {
            log.warn("Audit index {}: no aggregations in response; skipping per-incident update", indexName);
            migrationLogger.println("Audit index: no aggregations; applying simple rule (doc.endingStatus contains OUT_OF_WARRANTY -> OUT, else WITHIN)");
            migrationLogger.flush();
            return updateAuditIndexSimple(restTemplate, objectMapper, esHost, indexName, headers, migrationLogger);
        }

        JsonNode buckets = searchResp.path("aggregations").path("by_incident").path("buckets");
        if (!buckets.isArray() || buckets.size() == 0) {
            log.info("Audit index {}: no incident buckets; setting all to WITHIN_WARRANTY", indexName);
            return updateAuditIndexAllWithin(restTemplate, objectMapper, esHost, indexName, headers, migrationLogger);
        }

        Map<String, Long> incidentToCutoff = new HashMap<>();
        for (JsonNode bucket : buckets) {
            String incidentId = bucket.path("key").asText(null);
            if (incidentId == null) continue;
            JsonNode oow = bucket.path("out_of_warranty_times").path("min_ts").path("value");
            if (oow.isNumber() || oow.isIntegralNumber()) {
                incidentToCutoff.put(incidentId, oow.asLong());
            }
        }

        log.info("Audit index: found {} incidents with OUT_OF_WARRANTY cutoff (of {} total buckets)", incidentToCutoff.size(), buckets.size());
        migrationLogger.println("Incidents with OUT_OF_WARRANTY cutoff: " + incidentToCutoff.size());

        String updateByQueryUrl = esHost + "/" + indexName + "/_update_by_query?conflicts=proceed";

        // Update each incident: script uses cutoff so that lastModifiedTime >= cutoff -> OUT, else WITHIN
        for (Map.Entry<String, Long> entry : incidentToCutoff.entrySet()) {
            String incidentId = entry.getKey();
            long cutoff = entry.getValue();

            ObjectNode updateRequest = objectMapper.createObjectNode();
            ObjectNode script = objectMapper.createObjectNode();
            script.put("source",
                    "if (ctx._source.Data == null) { ctx._source.Data = [:]; } " +
                            "if (ctx._source.Data.incident == null) { ctx._source.Data.incident = [:]; } " +
                            "String within = params.withinWarranty; " +
                            "String out = params.outOfWarranty; " +
                            "long cutoff = params.cutoff; " +
                            "def ts = ctx._source.Data.auditDetails != null && ctx._source.Data.auditDetails.lastModifiedTime != null " +
                            "  ? ctx._source.Data.auditDetails.lastModifiedTime : 0L; " +
                            "String value = (ts >= cutoff) ? out : within; " +
                            "ctx._source.Data.incident.warrantyStatus = value; " +
                            "ctx._source.Data.warrantyStatus = value;");
            ObjectNode params = objectMapper.createObjectNode();
            params.put("withinWarranty", "WITHIN_WARRANTY");
            params.put("outOfWarranty", "OUT_OF_WARRANTY");
            params.put("cutoff", cutoff);
            script.set("params", params);
            updateRequest.set("script", script);

            ObjectNode term = objectMapper.createObjectNode();
            term.put("Data.incident.incidentId.keyword", incidentId);
            updateRequest.set("query", objectMapper.createObjectNode().set("term", term));

            try {
                HttpEntity<String> httpEntity = new HttpEntity<>(objectMapper.writeValueAsString(updateRequest), headers);
                restTemplate.postForEntity(updateByQueryUrl, httpEntity, JsonNode.class);
            } catch (Exception e) {
                log.warn("Audit index: failed for incident {}: {}", incidentId, e.getMessage());
            }
        }

        // Incidents that never had OUT_OF_WARRANTY: set all their audit docs to WITHIN_WARRANTY
        for (JsonNode bucket : buckets) {
            String incidentId = bucket.path("key").asText(null);
            if (incidentId == null || incidentToCutoff.containsKey(incidentId)) continue;

            ObjectNode updateRequest = objectMapper.createObjectNode();
            ObjectNode script = objectMapper.createObjectNode();
            script.put("source",
                    "if (ctx._source.Data == null) { ctx._source.Data = [:]; } " +
                            "if (ctx._source.Data.incident == null) { ctx._source.Data.incident = [:]; } " +
                            "ctx._source.Data.incident.warrantyStatus = params.withinWarranty; " +
                            "ctx._source.Data.warrantyStatus = params.withinWarranty;");
            ObjectNode params = objectMapper.createObjectNode();
            params.put("withinWarranty", "WITHIN_WARRANTY");
            script.set("params", params);
            updateRequest.set("script", script);

            ObjectNode term = objectMapper.createObjectNode();
            term.put("Data.incident.incidentId.keyword", incidentId);
            updateRequest.set("query", objectMapper.createObjectNode().set("term", term));

            try {
                HttpEntity<String> httpEntity = new HttpEntity<>(objectMapper.writeValueAsString(updateRequest), headers);
                restTemplate.postForEntity(updateByQueryUrl, httpEntity, JsonNode.class);
            } catch (Exception e) {
                log.warn("Audit index: failed setting WITHIN for incident {}: {}", incidentId, e.getMessage());
            }
        }

        migrationLogger.println("Audit index: updated documents for " + incidentToCutoff.size() + " incidents with cutoff; remaining incidents set to WITHIN_WARRANTY");
        migrationLogger.flush();
        return true;
    }

    /** Fallback: if aggregation not available, set by current doc's endingStatus only (less accurate for audit). */
    private boolean updateAuditIndexSimple(RestTemplate restTemplate,
                                           ObjectMapper objectMapper,
                                           String esHost,
                                           String indexName,
                                           HttpHeaders headers,
                                           PrintWriter migrationLogger) throws Exception {
        String updateByQueryUrl = esHost + "/" + indexName + "/_update_by_query?conflicts=proceed";
        ObjectNode updateRequest = objectMapper.createObjectNode();
        ObjectNode script = objectMapper.createObjectNode();
        script.put("source",
                "if (ctx._source.Data == null) { ctx._source.Data = [:]; } " +
                        "if (ctx._source.Data.incident == null) { ctx._source.Data.incident = [:]; } " +
                        "String within = params.withinWarranty; " +
                        "String out = params.outOfWarranty; " +
                        "def endStatus = ctx._source.Data.endingStatus; " +
                        "String value = (endStatus != null && endStatus.contains('OUT_OF_WARRANTY')) ? out : within; " +
                        "ctx._source.Data.incident.warrantyStatus = value; " +
                        "ctx._source.Data.warrantyStatus = value;");
        ObjectNode params = objectMapper.createObjectNode();
        params.put("withinWarranty", "WITHIN_WARRANTY");
        params.put("outOfWarranty", "OUT_OF_WARRANTY");
        script.set("params", params);
        updateRequest.set("script", script);
        updateRequest.set("query", objectMapper.createObjectNode().set("match_all", objectMapper.createObjectNode()));

        HttpEntity<String> httpEntity = new HttpEntity<>(objectMapper.writeValueAsString(updateRequest), headers);
        ResponseEntity<JsonNode> response = restTemplate.postForEntity(updateByQueryUrl, httpEntity, JsonNode.class);
        JsonNode body = response.getBody();
        int updated = body != null ? body.path("updated").asInt(0) : 0;
        migrationLogger.println("Updated (simple): " + updated);
        migrationLogger.flush();
        return true;
    }

    /** Set all audit docs to WITHIN_WARRANTY (when there are no buckets). */
    private boolean updateAuditIndexAllWithin(RestTemplate restTemplate,
                                              ObjectMapper objectMapper,
                                              String esHost,
                                              String indexName,
                                              HttpHeaders headers,
                                              PrintWriter migrationLogger) throws Exception {
        String updateByQueryUrl = esHost + "/" + indexName + "/_update_by_query?conflicts=proceed";
        ObjectNode updateRequest = objectMapper.createObjectNode();
        ObjectNode script = objectMapper.createObjectNode();
        script.put("source",
                "if (ctx._source.Data == null) { ctx._source.Data = [:]; } " +
                        "if (ctx._source.Data.incident == null) { ctx._source.Data.incident = [:]; } " +
                        "ctx._source.Data.incident.warrantyStatus = params.withinWarranty; " +
                        "ctx._source.Data.warrantyStatus = params.withinWarranty;");
        ObjectNode params = objectMapper.createObjectNode();
        params.put("withinWarranty", "WITHIN_WARRANTY");
        script.set("params", params);
        updateRequest.set("script", script);
        updateRequest.set("query", objectMapper.createObjectNode().set("match_all", objectMapper.createObjectNode()));

        HttpEntity<String> httpEntity = new HttpEntity<>(objectMapper.writeValueAsString(updateRequest), headers);
        restTemplate.postForEntity(updateByQueryUrl, httpEntity, JsonNode.class);
        migrationLogger.println("All audit docs set to WITHIN_WARRANTY (no buckets).");
        migrationLogger.flush();
        return true;
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
        requestFactory.setConnectTimeout(30000);
        requestFactory.setConnectionRequestTimeout(30000);

        log.info("RestTemplate created with Apache HttpClient5 and SSL verification disabled");
        return new RestTemplate(requestFactory);
    }
}
