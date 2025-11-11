package db.migration.main;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.NullNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.extern.slf4j.Slf4j;
import org.flywaydb.core.api.migration.BaseJavaMigration;
import org.flywaydb.core.api.migration.Context;
import org.springframework.http.*;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.FileWriter;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.net.URI;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;
import javax.net.ssl.*;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManagerBuilder;
import org.apache.hc.client5.http.io.HttpClientConnectionManager;
import org.apache.hc.client5.http.ssl.SSLConnectionSocketFactory;
import org.apache.hc.client5.http.ssl.TrustAllStrategy;
import org.apache.hc.core5.ssl.SSLContextBuilder;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;

@Slf4j
public class V20251105120000__update_ticket_indices_with_facility_data extends BaseJavaMigration {

    private static final String[] ES_INDICES = {
        "computed-sla-im-services",
        "audit-computed-sla-im-services"
    };

    @Override
    public boolean canExecuteInTransaction() {
        return false;
    }

    @Override
    public void migrate(Context context) throws Exception {
        log.info("🚀 Starting migration: Updating ES ticket indices with facility data");

        // Create RestTemplate with SSL verification disabled and increased timeouts
        RestTemplate restTemplate = createRestTemplateWithDisabledSSL();
        ObjectMapper objectMapper = new ObjectMapper();

        // Initialize migration log file
        String logFileName = "es_ticket_indices_facility_update_" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")) + ".log";
        String logFilePath = "./logs/" + logFileName;
        String absoluteLogPath = Paths.get(logFilePath).toAbsolutePath().normalize().toString();
        int totalUpdated = 0;
        int totalFailed = 0;

        try (PrintWriter migrationLogger = initializeMigrationLogger(logFilePath, absoluteLogPath)) {
            migrationLogger.println("========================================");
            migrationLogger.println("ELASTICSEARCH FACILITY UPDATE LOG");
            migrationLogger.println("Started at: " + LocalDateTime.now());
            migrationLogger.println("========================================\n");
            migrationLogger.flush();

            // Get Elasticsearch configuration
            String esHost = getEnvOrDefault("EGOV_ES_HOST", "https://localhost:9200");
            String esUsername = getEnvOrDefault("EGOV_ES_USERNAME", "");
            String esPassword = getEnvOrDefault("EGOV_ES_PASSWORD", "");
            String authToken = getEnvOrDefault("EGOV_AUTH_TOKEN", "");
            int sleepMs = Integer.parseInt(getEnvOrDefault("EGOV_ES_UPDATE_DELAY_MS", "100"));

            log.info("Elasticsearch Host: {}", esHost);

            // Read facility mappings from PostgreSQL
            Map<String, FacilityData> facilityMappingsByTenantId = loadFacilityMappings(context);

            log.info("Loaded {} facility mappings from database", facilityMappingsByTenantId.size());
            migrationLogger.println("Loaded " + facilityMappingsByTenantId.size() + " facility mappings from database\n");
            migrationLogger.flush();

            // Fetch boundary hierarchy details for facility boundary codes
            String boundaryHost = getEnvOrDefault("EGOV_BOUNDARY_HOST", "http://localhost:8082");
            String boundarySearchEndpoint = "/boundary-service/boundary-relationships/_search";
            String boundaryTenantId = getEnvOrDefault("EGOV_BOUNDARY_TENANT_ID", "in");

            Map<String, ObjectNode> boundaryDetailsByCode = fetchBoundaryHierarchyForFacilities(
                restTemplate,
                objectMapper,
                facilityMappingsByTenantId.values(),
                boundaryHost,
                boundarySearchEndpoint,
                boundaryTenantId,
                authToken
            );

            facilityMappingsByTenantId.values().forEach(data -> {
                if (data.getBoundaryCode() == null) {
                    return;
                }
                ObjectNode boundaryNode = boundaryDetailsByCode.get(data.getBoundaryCode());
                if (boundaryNode != null) {
                    data.setBoundary(boundaryNode);
                } else {
                    log.warn("No boundary hierarchy found for boundary code {}", data.getBoundaryCode());
                }
            });

            migrationLogger.println("Fetched boundary hierarchy for " + boundaryDetailsByCode.size() + " facilities\n");
            migrationLogger.flush();

            // Update each Elasticsearch index
            for (String indexName : ES_INDICES) {
                log.info("Processing index: {}", indexName);
                migrationLogger.println("\n========================================");
                migrationLogger.println("Processing Index: " + indexName);
                migrationLogger.println("========================================\n");
                migrationLogger.flush();

                int updated = 0;
                int failed = 0;

                for (Map.Entry<String, FacilityData> entry : facilityMappingsByTenantId.entrySet()) {
                    String tenantId = entry.getKey();
                    FacilityData facilityData = entry.getValue();

                    try {
                        updateDocumentsInES(
                            restTemplate, objectMapper, esHost, indexName,
                            tenantId, facilityData, esUsername, esPassword,
                            migrationLogger
                        );
                        updated++;
                        log.debug("✓ Updated documents for tenant: {} in index: {}", tenantId, indexName);

                        // Small delay to avoid overwhelming Elasticsearch
                        if (sleepMs > 0) {
                            Thread.sleep(sleepMs);
                        }
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        log.warn("Migration interrupted");
                        throw e;
                    } catch (Exception e) {
                        failed++;
                        log.error("Error updating tenant {} in index {}: {}", tenantId, indexName, e.getMessage(), e);
                    }
                }

                totalUpdated += updated;
                totalFailed += failed;

                log.info("Completed index {}: {} tenant updates succeeded, {} failed", indexName, updated, failed);
                migrationLogger.println("\n----------------------------------------");
                migrationLogger.println("Index " + indexName + " Summary:");
                migrationLogger.println("  ✓ Successful Updates: " + updated);
                migrationLogger.println("  ✗ Failed Updates: " + failed);
                migrationLogger.println("----------------------------------------\n");
                migrationLogger.flush();
            }

            // Final summary
            migrationLogger.println("\n========================================");
            migrationLogger.println("MIGRATION SUMMARY");
            migrationLogger.println("========================================");
            migrationLogger.println("Total Successful Updates: " + totalUpdated);
            migrationLogger.println("Total Failed Updates: " + totalFailed);
            migrationLogger.println("\nCompleted at: " + LocalDateTime.now());
            migrationLogger.println("========================================\n");
            migrationLogger.flush();
        }

        log.info("✅ Migration completed: ES indices updated with facility data");
        log.info("Total successful updates: {}", totalUpdated);
        log.info("Total failed updates: {}", totalFailed);
        log.info("📝 Migration log file: {}", absoluteLogPath);
    }

    private Map<String, FacilityData> loadFacilityMappings(Context context) throws Exception {
        Map<String, FacilityData> mappings = new HashMap<>();

        String query = "SELECT tenant_id, facility_id, boundary_code FROM facility_tenant_id_map";

        try (Connection conn = context.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {

            while (rs.next()) {
                String tenantId = rs.getString("tenant_id");
                String facilityId = rs.getString("facility_id");
                String boundaryCode = rs.getString("boundary_code");

                mappings.put(tenantId, new FacilityData(facilityId, boundaryCode));
                log.debug("Loaded mapping: {} -> facilityId: {}, boundaryCode: {}", tenantId, facilityId, boundaryCode);
            }

        } catch (Exception e) {
            log.error("Error loading facility mappings from database", e);
            throw e;
        }

        return mappings;
    }

    private void updateDocumentsInES(
            RestTemplate restTemplate, ObjectMapper objectMapper,
            String esHost, String indexName, String tenantId,
            FacilityData facilityData, String esUsername, String esPassword,
            PrintWriter migrationLogger
    ) throws Exception {
        // Build Elasticsearch Update By Query request
        String updateByQueryUrl = esHost + "/" + indexName + "/_update_by_query?conflicts=proceed";

        ObjectNode updateRequest = objectMapper.createObjectNode();

        // Script to update Data.incident.facilityId and Data.incident.boundaryCode
        ObjectNode script = objectMapper.createObjectNode();
        script.put(
                "source",
                "if (ctx._source.Data == null) { ctx._source.Data = [:]; } " +
                        "if (ctx._source.Data.incident == null) { ctx._source.Data.incident = [:]; } " +
                        "ctx._source.Data.incident.facilityId = params.facilityId; " +
                        "ctx._source.Data.facilityId = params.facilityId; " +
                        "ctx._source.Data.incident.boundaryCode = params.boundaryCode; " +
                        "ctx._source.Data.boundaryCode = params.boundaryCode; " +
                        "ctx._source.Data.incident.boundary = params.boundary;"
        );

        ObjectNode params = objectMapper.createObjectNode();
        params.put("facilityId", facilityData.getFacilityId());
        params.put("boundaryCode", facilityData.getBoundaryCode());
        if (facilityData.getBoundary() != null) {
            params.set("boundary", facilityData.getBoundary());
        } else {
            params.set("boundary", NullNode.instance);
        }
        script.set("params", params);

        updateRequest.set("script", script);

        // Query to match documents with this tenantId
        ObjectNode query = objectMapper.createObjectNode();
        ObjectNode term = objectMapper.createObjectNode();
        ObjectNode tenantIdMatch = objectMapper.createObjectNode();
        tenantIdMatch.put("Data.incident.tenantId", tenantId);
        term.set("term", tenantIdMatch);
        query.set("query", term);
        updateRequest.set("query", query.get("query"));

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        // Add Basic Authentication if provided
        if (esUsername != null && !esUsername.isEmpty() && esPassword != null && !esPassword.isEmpty()) {
            String auth = esUsername + ":" + esPassword;
            byte[] base64CredentialsBytes = Base64.getEncoder().encode(auth.getBytes());
            headers.set("Authorization", "Basic " + new String(base64CredentialsBytes));
        }

        try {
            String requestBody = objectMapper.writeValueAsString(updateRequest);
            HttpEntity<String> httpEntity = new HttpEntity<>(requestBody, headers);

            log.debug("Sending POST to: {}", updateByQueryUrl);
            log.debug("Request body: {}", requestBody);

            ResponseEntity<String> response = restTemplate.exchange(
                updateByQueryUrl,
                HttpMethod.POST,
                httpEntity,
                String.class
            );

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                JsonNode responseBody = objectMapper.readTree(response.getBody());
                int updated = responseBody.path("updated").asInt(0);
                int total = responseBody.path("total").asInt(0);
                int failures = responseBody.path("failures").size();

                log.info("Updated {} out of {} documents for tenant {} in index {}", updated, total, tenantId, indexName);

                if (updated > 0) {
                    migrationLogger.println(String.format(
                        "[SUCCESS] Tenant: %s | Index: %s | Updated: %d/%d documents | Facility: %s | Boundary: %s",
                        tenantId, indexName, updated, total,
                        facilityData.getFacilityId(), facilityData.getBoundaryCode()
                    ));
                    migrationLogger.flush();
                }

                // Log if there were failures in the response
                if (failures > 0) {
                    migrationLogger.println("\n[PARTIAL_FAILURE]");
                    migrationLogger.println("Tenant ID: " + tenantId);
                    migrationLogger.println("Index: " + indexName);
                    migrationLogger.println("Updated: " + updated + "/" + total);
                    migrationLogger.println("Failures: " + failures);
                    migrationLogger.println("Response: " + response.getBody());
                    migrationLogger.println();
                    migrationLogger.flush();
                }

            } else {
                String errorMsg = "ES update returned status: " + response.getStatusCode();
                log.error("ES update failed for tenant {} in index {}: {}", tenantId, indexName, response.getStatusCode());

                migrationLogger.println("\n[FAILED]");
                migrationLogger.println("Tenant ID: " + tenantId);
                migrationLogger.println("Index: " + indexName);
                migrationLogger.println("Reason: " + errorMsg);
                migrationLogger.println("Response: " + response.getBody());
                migrationLogger.println();
                migrationLogger.flush();

                throw new RuntimeException(errorMsg);
            }

        } catch (HttpClientErrorException e) {
            // Handle 4xx errors
            String errorMsg = "ES API returned " + e.getStatusCode() + " - " + e.getStatusText();
            String responseBody = e.getResponseBodyAsString();

            log.error("Client error updating ES for tenant {}: {} - {}", tenantId, errorMsg, responseBody);

            migrationLogger.println("\n[FAILED]");
            migrationLogger.println("Tenant ID: " + tenantId);
            migrationLogger.println("Index: " + indexName);
            migrationLogger.println("Reason: " + errorMsg);
            migrationLogger.println("Response: " + responseBody);
            migrationLogger.println();
            migrationLogger.flush();

            throw e;

        } catch (HttpServerErrorException e) {
            // Handle 5xx errors
            String errorMsg = "ES API returned " + e.getStatusCode() + " - " + e.getStatusText();
            String responseBody = e.getResponseBodyAsString();

            log.error("Server error updating ES for tenant {}: {} - {}", tenantId, errorMsg, responseBody);

            migrationLogger.println("\n[FAILED]");
            migrationLogger.println("Tenant ID: " + tenantId);
            migrationLogger.println("Index: " + indexName);
            migrationLogger.println("Reason: " + errorMsg);
            migrationLogger.println("Response: " + responseBody);
            migrationLogger.println();
            migrationLogger.flush();

            throw e;

        } catch (Exception e) {
            log.error("Error updating ES for tenant {} in index {}: {}", tenantId, indexName, e.getMessage(), e);

            migrationLogger.println("\n[FAILED]");
            migrationLogger.println("Tenant ID: " + tenantId);
            migrationLogger.println("Index: " + indexName);
            migrationLogger.println("Reason: Exception - " + e.getMessage());
            migrationLogger.println("Details: " + e.toString());
            migrationLogger.println();
            migrationLogger.flush();

            throw e;
        }
    }

    private PrintWriter initializeMigrationLogger(String logFilePath, String absolutePath) throws Exception {
        Files.createDirectories(Paths.get("./logs"));
        FileWriter fileWriter = new FileWriter(logFilePath, true);
        log.info("📝 Migration log file created: {}", absolutePath);
        return new PrintWriter(fileWriter, true);
    }

    private static String getEnvOrDefault(String key, String def) {
        String v = System.getenv(key);
        return (v == null || v.isEmpty()) ? def : v;
    }

    private Map<String, ObjectNode> fetchBoundaryHierarchyForFacilities(
            RestTemplate restTemplate,
            ObjectMapper objectMapper,
            Collection<FacilityData> facilityDataCollection,
            String boundaryHost,
            String boundarySearchEndpoint,
            String boundaryTenantId,
            String authToken
    ) {
        Map<String, ObjectNode> boundaryMap = new HashMap<>();

        if (facilityDataCollection == null || facilityDataCollection.isEmpty()) {
            return boundaryMap;
        }

        List<String> boundaryCodes = facilityDataCollection.stream()
                .map(FacilityData::getBoundaryCode)
                .filter(Objects::nonNull)
                .filter(code -> !code.isBlank())
                .distinct()
                .collect(Collectors.toList());

        if (boundaryCodes.isEmpty()) {
            log.warn("No boundary codes available for hierarchy fetch");
            return boundaryMap;
        }

        final int batchSize = 50;
        for (int i = 0; i < boundaryCodes.size(); i += batchSize) {
            List<String> batch = boundaryCodes.subList(i, Math.min(i + batchSize, boundaryCodes.size()));
            try {
                String uriWithCodes = UriComponentsBuilder.fromHttpUrl(boundaryHost + boundarySearchEndpoint)
                        .queryParam("boundaryType", "Facility")
                        .queryParam("includeParents", true)
                        .queryParam("tenantId", boundaryTenantId)
                        .queryParam("codes", String.join(",", batch))
                        .build(true)
                        .toUriString();

                URI uri = URI.create(uriWithCodes);

                Map<String, Object> requestBody = Map.of(
                        "RequestInfo", buildRequestInfo(authToken, "egov.boundary")
                );

                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.APPLICATION_JSON);

                HttpEntity<String> entity = new HttpEntity<>(objectMapper.writeValueAsString(requestBody), headers);

                ResponseEntity<String> response = restTemplate.exchange(
                        uri,
                        HttpMethod.POST,
                        entity,
                        String.class
                );

                if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                    JsonNode responseNode = objectMapper.readTree(response.getBody());
                    Map<String, ObjectNode> batchResult = parseBoundaryHierarchy(responseNode, objectMapper);
                    boundaryMap.putAll(batchResult);
                    log.info("Fetched boundary hierarchy for batch of {} facilities", batch.size());
                } else {
                    log.error("Boundary hierarchy fetch failed with status {} for batch {}", response.getStatusCode(), batch);
                }

            } catch (Exception e) {
                log.error("Error fetching boundary hierarchy for batch {}: {}", batch, e.getMessage(), e);
            }
        }

        return boundaryMap;
    }

    private Map<String, ObjectNode> parseBoundaryHierarchy(JsonNode response, ObjectMapper objectMapper) {
        Map<String, ObjectNode> result = new HashMap<>();
        if (response == null) {
            return result;
        }

        JsonNode tenantBoundaryArray = response.get("TenantBoundary");
        if (tenantBoundaryArray == null || !tenantBoundaryArray.isArray()) {
            return result;
        }

        for (JsonNode tenantBoundary : tenantBoundaryArray) {
            JsonNode boundaries = tenantBoundary.get("boundary");
            if (boundaries != null && boundaries.isArray()) {
                for (JsonNode boundaryNode : boundaries) {
                    traverseBoundaryHierarchy(boundaryNode, new ArrayDeque<>(), result, objectMapper);
                }
            }
        }

        return result;
    }

    private void traverseBoundaryHierarchy(
            JsonNode current,
            Deque<JsonNode> path,
            Map<String, ObjectNode> accumulator,
            ObjectMapper objectMapper
    ) {
        if (current == null || current.isMissingNode()) {
            return;
        }

        path.addLast(current);

        String boundaryType = current.path("boundaryType").asText("");
        if ("Facility".equalsIgnoreCase(boundaryType)) {
            String facilityCode = current.path("code").asText(null);
            if (facilityCode != null && !facilityCode.isBlank()) {
                ObjectNode boundaryObject = objectMapper.createObjectNode();
                boundaryObject.put("countryCode", findBoundaryCodeByType(path, "Country"));
                boundaryObject.put("stateCode", findBoundaryCodeByType(path, "State"));
                boundaryObject.put("districtCode", findBoundaryCodeByType(path, "District"));
                boundaryObject.put("blockCode", findBoundaryCodeByType(path, "Block"));
                boundaryObject.put("facilityCode", facilityCode);
                accumulator.put(facilityCode, boundaryObject);
            }
        }

        JsonNode children = current.get("children");
        if (children != null && children.isArray()) {
            for (JsonNode child : children) {
                traverseBoundaryHierarchy(child, path, accumulator, objectMapper);
            }
        }

        path.removeLast();
    }

    private String findBoundaryCodeByType(Deque<JsonNode> path, String boundaryType) {
        if (path == null || path.isEmpty()) {
            return null;
        }

        for (JsonNode node : path) {
            if (boundaryType.equalsIgnoreCase(node.path("boundaryType").asText(""))) {
                return node.path("code").asText(null);
            }
        }
        return null;
    }

    private Map<String, Object> buildRequestInfo(String authToken, String apiId) {
        Map<String, Object> userInfo = new HashMap<>();
        userInfo.put("id", 206);
        userInfo.put("uuid", "14d6dbdf-e4d2-45c3-9717-c82ba17a9f19");
        userInfo.put("userName", "SYSTEMUSER");
        userInfo.put("name", "System User");
        userInfo.put("mobileNumber", "1111112111");
        userInfo.put("emailId", "");
        userInfo.put("type", "EMPLOYEE");
        userInfo.put("active", true);
        userInfo.put("tenantId", "in");

        List<Map<String, Object>> roles = new ArrayList<>();
        roles.add(Map.of("name", "Employee", "code", "EMPLOYEE", "tenantId", "in"));
        roles.add(Map.of("name", "System user", "code", "SYSTEM", "tenantId", "in"));
        userInfo.put("roles", roles);

        Map<String, Object> requestInfo = new HashMap<>();
        requestInfo.put("apiId", apiId);
        requestInfo.put("ver", "1.0");
        long timestamp = System.currentTimeMillis();
        requestInfo.put("ts", timestamp);
        requestInfo.put("action", "create");
        requestInfo.put("msgId", timestamp + "|en_IN");
        requestInfo.put("authToken", authToken != null ? authToken : "");
        requestInfo.put("userInfo", userInfo);

        return requestInfo;
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

    // Helper class to hold facility data
    private static class FacilityData {
        private final String facilityId;
        private final String boundaryCode;
        private ObjectNode boundary;

        FacilityData(String facilityId, String boundaryCode) {
            this.facilityId = facilityId;
            this.boundaryCode = boundaryCode;
        }

        String getFacilityId() {
            return facilityId;
        }

        String getBoundaryCode() {
            return boundaryCode;
        }

        ObjectNode getBoundary() {
            return boundary;
        }

        void setBoundary(ObjectNode boundary) {
            this.boundary = boundary;
        }
    }
}

