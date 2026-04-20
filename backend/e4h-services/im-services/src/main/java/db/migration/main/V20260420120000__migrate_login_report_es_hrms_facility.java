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
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import javax.net.ssl.SSLContext;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@Slf4j
public class V20260420120000__migrate_login_report_es_hrms_facility extends BaseJavaMigration {

    private static final String ROLE_COMPLAINANT = "COMPLAINANT";

    @Override
    public boolean canExecuteInTransaction() {
        return false;
    }

    @Override
    public void migrate(Context context) throws Exception {
        if (Boolean.parseBoolean(getEnvOrDefault("LOGIN_REPORT_MIGRATION_SKIP", "false"))) {
            log.info("Skipping login-report ES migration (LOGIN_REPORT_MIGRATION_SKIP=true)");
            return;
        }

        RestTemplate restTemplate = createRestTemplateWithDisabledSSL();
        ObjectMapper objectMapper = new ObjectMapper();

        String logFileName = "login_report_es_migration_" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")) + ".log";
        Path logsDir = Paths.get("logs");
        Files.createDirectories(logsDir);
        Path logFilePath = logsDir.resolve(logFileName).toAbsolutePath().normalize();

        String esHost = trimTrailingSlash(getEnvOrDefault("EGOV_ES_HOST", "https://localhost:9200"));
        String esUser = getEnvOrDefault("EGOV_ES_USERNAME", "");
        String esPass = getEnvOrDefault("EGOV_ES_PASSWORD", "");
        String index = getEnvOrDefault("LOGIN_REPORT_ES_INDEX", "login-report");

        String hrmsHost = trimTrailingSlash(getEnvOrDefault("EGOV_HRMS_HOST", "http://localhost:8090"));
        String hrmsSearchPath = getEnvOrDefault("EGOV_HRMS_SEARCH_ENDPOINT", "/egov-hrms/employees/_search");
        String facilityHost = trimTrailingSlash(getEnvOrDefault("EGOV_FACILITY_HOST", "http://localhost:8082"));
        String facilitySearchPath = getEnvOrDefault("EGOV_FACILITY_SEARCH_PATH", "/facility-service/v2/facility/search");
        String internalUserUuid = getEnvOrDefault("EGOV_INTERNAL_MICROSERVICE_USER_UUID", "4fef6612-07a8-4751-97e9-0e0ac0687ebe");
        String defaultTenantId = getEnvOrDefault("LOGIN_REPORT_DEFAULT_TENANT_ID", "in");

        int scrollSize = Integer.parseInt(getEnvOrDefault("LOGIN_REPORT_MIGRATION_SCROLL_SIZE", "100"));
        int maxDocs = Integer.parseInt(getEnvOrDefault("LOGIN_REPORT_MIGRATION_MAX_DOCS", "0"));
        int esBulkSize = Integer.parseInt(getEnvOrDefault("LOGIN_REPORT_ES_BULK_SIZE", "200"));
        int progressLogEvery = Integer.parseInt(getEnvOrDefault("LOGIN_REPORT_PROGRESS_LOG_EVERY", "100"));
        boolean hrmsFilterRoleComplainant = Boolean.parseBoolean(getEnvOrDefault("LOGIN_REPORT_HRMS_FILTER_ROLE_COMPLAINANT", "true"));

        try (PrintWriter migrationLogger = initializeMigrationLogger(logFilePath)) {
            migrationLogger.println("Login-report ES migration (HRMS + facility backfill)");
            migrationLogger.println("Index: " + index);
            migrationLogger.println("Started: " + LocalDateTime.now());
            migrationLogger.println("Config -> pageSize=" + scrollSize + ", bulkSize=" + esBulkSize + ", progressEvery=" + progressLogEvery);
            migrationLogger.flush();

            HttpHeaders esHeaders = buildEsHeaders(esUser, esPass);

            int processed = 0;
            int updated = 0;
            int skipped = 0;
            int failed = 0;
            int cacheHits = 0;
            int cacheMisses = 0;

            String searchUrl = esHost + "/" + encodeIndexForUrl(index) + "/_search";
            JsonNode searchAfter = null;
            JsonNode hits = fetchPage(restTemplate, objectMapper, searchUrl, esHeaders, scrollSize, searchAfter);

            List<BulkUpdate> pendingBulk = new ArrayList<>();
            Map<String, EnrichedLocation> locationCacheByUserName = new HashMap<>();

            while (hits != null && hits.isArray() && !hits.isEmpty()) {
                for (JsonNode hit : hits) {
                    if (maxDocs > 0 && processed >= maxDocs) {
                        break;
                    }
                    String docId = hit.path("_id").asText(null);
                    String docIndex = hit.path("_index").asText(index);
                    JsonNode source = hit.path("_source");
                    processed++;

                    try {
                        JsonNode ulrNode = source.path("Data").path("userLoginReport");
                        String userName = textOrEmpty(ulrNode.path("userName"));
                        if (!userName.isEmpty() && locationCacheByUserName.containsKey(userName)) {
                            cacheHits++;
                        } else {
                            cacheMisses++;
                        }

                        ObjectNode mergedUserLoginReport = enrichDocument(restTemplate, objectMapper, source, locationCacheByUserName,
                                hrmsHost, hrmsSearchPath, facilityHost, facilitySearchPath, internalUserUuid, migrationLogger,
                                hrmsFilterRoleComplainant, defaultTenantId);
                        if (mergedUserLoginReport == null) {
                            skipped++;
                            continue;
                        }
                        pendingBulk.add(new BulkUpdate(docIndex, docId, mergedUserLoginReport));
                        if (pendingBulk.size() >= esBulkSize) {
                            int u = executeBulk(restTemplate, objectMapper, esHost, esHeaders, pendingBulk, migrationLogger);
                            updated += u;
                            pendingBulk.clear();
                            logProgress(migrationLogger, processed, updated, skipped, failed, cacheHits, cacheMisses, locationCacheByUserName.size());
                        }
                    } catch (Exception ex) {
                        failed++;
                        migrationLogger.println("[FAIL] id=" + docId + " " + ex.getMessage());
                        log.warn("Document failed: {}", docId, ex);
                    }

                    if (progressLogEvery > 0 && processed % progressLogEvery == 0) {
                        logProgress(migrationLogger, processed, updated, skipped, failed, cacheHits, cacheMisses, locationCacheByUserName.size());
                    }
                }

                if (maxDocs > 0 && processed >= maxDocs) {
                    break;
                }

                JsonNode lastHit = hits.get(hits.size() - 1);
                searchAfter = lastHit.path("sort");
                hits = fetchPage(restTemplate, objectMapper, searchUrl, esHeaders, scrollSize, searchAfter);
            }

            if (!pendingBulk.isEmpty()) {
                updated += executeBulk(restTemplate, objectMapper, esHost, esHeaders, pendingBulk, migrationLogger);
            }

            migrationLogger.println("Processed: " + processed + ", updated: " + updated + ", skipped: " + skipped + ", failed: " + failed);
            migrationLogger.println("Cache stats -> hit: " + cacheHits + ", miss: " + cacheMisses + ", unique users: " + locationCacheByUserName.size());
            migrationLogger.println("Finished: " + LocalDateTime.now());
            migrationLogger.flush();
            log.info("Login-report migration done. processed={}, updated={}, skipped={}, failed={}, cacheHit={}, cacheMiss={}, uniqueUsers={}",
                    processed, updated, skipped, failed, cacheHits, cacheMisses, locationCacheByUserName.size());
        }
    }

    private static String encodeIndexForUrl(String index) {
        try {
            return URLEncoder.encode(index, StandardCharsets.UTF_8).replace("+", "%20");
        } catch (Exception e) {
            return index;
        }
    }

    private JsonNode fetchPage(RestTemplate restTemplate, ObjectMapper mapper, String searchUrl, HttpHeaders headers,
                               int size, JsonNode searchAfter) throws IOException {
        ObjectNode searchBody = mapper.createObjectNode();
        searchBody.put("size", size);

        ObjectNode roleField = mapper.createObjectNode();
        roleField.put("Data.userLoginReport.userRole.keyword", ROLE_COMPLAINANT);
        ObjectNode term = mapper.createObjectNode();
        term.set("term", roleField);
        ObjectNode bool = mapper.createObjectNode();
        bool.set("filter", mapper.createArrayNode().add(term));
        ObjectNode query = mapper.createObjectNode();
        query.set("bool", bool);
        searchBody.set("query", query);

        searchBody.set("_source", mapper.createArrayNode().add("Data").add("tenantId"));
        searchBody.set("sort", mapper.createArrayNode().add(mapper.createObjectNode().put("_id", "asc")));
        if (searchAfter != null && searchAfter.isArray() && !searchAfter.isEmpty()) {
            searchBody.set("search_after", searchAfter);
        }

        HttpEntity<String> entity = new HttpEntity<>(mapper.writeValueAsString(searchBody), headers);
        ResponseEntity<JsonNode> response = restTemplate.postForEntity(searchUrl, entity, JsonNode.class);
        JsonNode root = response.getBody();
        if (root == null) {
            return mapper.createArrayNode();
        }
        return root.path("hits").path("hits");
    }

    private void logProgress(PrintWriter migrationLogger, int processed, int updated, int skipped, int failed,
                             int cacheHits, int cacheMisses, int uniqueUsers) {
        String message = String.format(
                "[PROGRESS] processed=%d, updated=%d, skipped=%d, failed=%d, cacheHit=%d, cacheMiss=%d, uniqueUsers=%d",
                processed, updated, skipped, failed, cacheHits, cacheMisses, uniqueUsers
        );
        migrationLogger.println(message);
        migrationLogger.flush();
        log.info(message);
    }

    private int executeBulk(RestTemplate restTemplate, ObjectMapper mapper, String esHost, HttpHeaders headers,
                            List<BulkUpdate> updates, PrintWriter log) throws IOException {
        StringBuilder ndjson = new StringBuilder();
        for (BulkUpdate u : updates) {
            ObjectNode action = mapper.createObjectNode();
            ObjectNode update = mapper.createObjectNode();
            update.put("_index", u.index);
            update.put("_id", u.id);
            action.set("update", update);
            ndjson.append(mapper.writeValueAsString(action)).append('\n');

            ObjectNode doc = mapper.createObjectNode();
            ObjectNode data = mapper.createObjectNode();
            data.set("userLoginReport", u.userLoginReport);
            doc.set("Data", data);
            ObjectNode wrap = mapper.createObjectNode();
            wrap.set("doc", doc);
            ndjson.append(mapper.writeValueAsString(wrap)).append('\n');
        }

        HttpHeaders bulkHeaders = new HttpHeaders();
        bulkHeaders.addAll(headers);
        bulkHeaders.setContentType(new MediaType("application", "x-ndjson", StandardCharsets.UTF_8));
        HttpEntity<String> entity = new HttpEntity<>(ndjson.toString(), bulkHeaders);
        ResponseEntity<JsonNode> resp = restTemplate.postForEntity(esHost + "/_bulk", entity, JsonNode.class);
        JsonNode body = resp.getBody();
        int ok = 0;
        if (body != null && body.path("items").isArray()) {
            for (JsonNode item : body.path("items")) {
                JsonNode upd = item.path("update");
                if (upd.path("status").asInt(0) >= 200 && upd.path("status").asInt(0) < 300) {
                    ok++;
                } else {
                    log.println("[BULK_FAIL] " + upd.toString());
                }
            }
        }
        return ok;
    }

    /**
     * Returns a deep copy of {@code userLoginReport} with location fields overwritten, or null to skip the document.
     */
    private ObjectNode enrichDocument(RestTemplate restTemplate, ObjectMapper mapper, JsonNode source, Map<String, EnrichedLocation> locationCacheByUserName,
                                    String hrmsHost, String hrmsSearchPath, String facilityHost, String facilitySearchPath,
                                    String internalUserUuid, PrintWriter log, boolean hrmsFilterRoleComplainant,
                                    String defaultTenantId) throws Exception {
        JsonNode data = source.path("Data");
        JsonNode ulrNode = data.path("userLoginReport");
        if (ulrNode.isMissingNode() || ulrNode.isNull()) {
            return null;
        }
        JsonNode ulrCopy = mapper.readTree(mapper.writeValueAsString(ulrNode));
        if (!(ulrCopy instanceof ObjectNode)) {
            return null;
        }
        ObjectNode ulr = (ObjectNode) ulrCopy;

        String userName = textOrEmpty(ulr.path("userName"));
        if (userName.isEmpty()) {
            return null;
        }
        // login-report index mapping does not include tenantId; use configured default tenant.
        String tenantId = defaultTenantId;
        if (tenantId == null || tenantId.isBlank()) {
            log.println("[SKIP] no tenantId configured for userName=" + userName);
            return null;
        }

        EnrichedLocation location = locationCacheByUserName.get(userName);
        if (location == null) {
            location = computeEnrichedLocationForUser(restTemplate, mapper, hrmsHost, hrmsSearchPath, facilityHost, facilitySearchPath,
                    internalUserUuid, tenantId, userName, hrmsFilterRoleComplainant, log);
            locationCacheByUserName.put(userName, location);
        }

        ulr.put("state", location.state);
        ulr.put("district", location.district);
        ulr.put("block", location.block);
        ulr.put("healthFacilityName", location.healthFacilityName);
        return ulr;
    }

    private EnrichedLocation computeEnrichedLocationForUser(RestTemplate restTemplate, ObjectMapper mapper,
                                                            String hrmsHost, String hrmsSearchPath,
                                                            String facilityHost, String facilitySearchPath,
                                                            String internalUserUuid, String tenantId, String userName,
                                                            boolean hrmsFilterRoleComplainant, PrintWriter log) throws Exception {
        String boundaryCode = fetchBoundaryFromHrmsByEmployeeCode(restTemplate, mapper, hrmsHost, hrmsSearchPath,
                internalUserUuid, tenantId, userName, hrmsFilterRoleComplainant);

        if (boundaryCode == null || boundaryCode.isBlank()) {
            log.println("[SKIP] no HRMS boundary for userName=" + userName);
            return EnrichedLocation.empty();
        }

        Map<String, String> facility = fetchFacilityDetails(restTemplate, facilityHost, facilitySearchPath, boundaryCode, tenantId);
        String[] fallbackDb = extractDistrictAndBlockFromBoundaryCode(boundaryCode);
        String district = firstNonBlank(facility.get("district"), fallbackDb[0]);
        String block = firstNonBlank(facility.get("block"), fallbackDb[1]);
        String state = firstNonBlank(facility.get("state"), extractStateFromBoundaryCode(boundaryCode));
        String health = firstNonBlank(facility.get("healthFacilityName"), "");
        return new EnrichedLocation(state, district, block, health);
    }

    /**
     * HRMS search using employee {@code codes} query param (employee code matches login {@code userName}).
     */
    private String fetchBoundaryFromHrmsByEmployeeCode(RestTemplate rt, ObjectMapper mapper, String hrmsHost, String hrmsPath,
                                                         String internalUuid, String tenantId, String employeeCode,
                                                         boolean filterRoleComplainant) throws Exception {
        UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(hrmsHost + hrmsPath)
                .queryParam("tenantId", tenantId)
                .queryParam("codes", employeeCode)
                .queryParam("isActive", true);
        if (filterRoleComplainant) {
            builder.queryParam("roles", ROLE_COMPLAINANT);
        }
        String url = builder.toUriString();
        return postHrmsAndExtractBoundary(rt, mapper, url, internalUuid);
    }

    private String postHrmsAndExtractBoundary(RestTemplate rt, ObjectMapper mapper, String url, String internalUuid) throws Exception {
        ObjectNode body = mapper.createObjectNode();
        body.set("RequestInfo", buildRequestInfo(mapper, internalUuid));
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> entity = new HttpEntity<>(mapper.writeValueAsString(body), headers);
        ResponseEntity<JsonNode> resp = rt.postForEntity(url, entity, JsonNode.class);
        JsonNode root = resp.getBody();
        if (root == null) {
            return null;
        }
        JsonNode employees = root.path("Employees");
        if (!employees.isArray() || employees.isEmpty()) {
            return null;
        }
        JsonNode boundary = employees.get(0).path("jurisdictions").path(0).path("boundary");
        return boundary.isMissingNode() || boundary.isNull() ? null : boundary.asText();
    }

    private Map<String, String> fetchFacilityDetails(RestTemplate rt, String facilityHost, String facilitySearchPath,
                                                     String boundaryCode, String tenantId) {
        Map<String, String> details = new HashMap<>();
        details.put("healthFacilityName", "");
        details.put("district", "");
        details.put("block", "");
        details.put("state", "");
        try {
            String url = UriComponentsBuilder.fromHttpUrl(facilityHost + facilitySearchPath)
                    .queryParam("tenantId", tenantId != null ? tenantId : "")
                    .queryParam("boundaryCode", boundaryCode)
                    .toUriString();
            HttpHeaders headers = new HttpHeaders();
            headers.setAccept(List.of(MediaType.APPLICATION_JSON));
            ResponseEntity<JsonNode> resp = rt.exchange(url, HttpMethod.GET, new HttpEntity<>(headers), JsonNode.class);
            JsonNode root = resp.getBody();
            if (root == null || !root.path("facilities").isArray() || root.path("facilities").isEmpty()) {
                return details;
            }
            JsonNode f = root.path("facilities").get(0);
            String name = textOrEmpty(f.path("facility_name"));
            if (name.isEmpty()) {
                name = textOrEmpty(f.path("name"));
            }
            if (name.isEmpty()) {
                name = textOrEmpty(f.path("facilityName"));
            }
            details.put("healthFacilityName", name);
            JsonNode b = f.path("boundary");
            if (!b.isMissingNode() && b.isObject()) {
                details.put("district", normalizeBoundaryName(b.path("district")));
                details.put("block", normalizeBoundaryName(b.path("block")));
                details.put("state", normalizeBoundaryName(b.path("state")));
            }
        } catch (Exception e) {
            log.warn("Facility search failed for boundaryCode={}: {}", boundaryCode, e.getMessage());
        }
        return details;
    }

    private ObjectNode buildRequestInfo(ObjectMapper mapper, String internalUserUuid) {
        ObjectNode ri = mapper.createObjectNode();
        ri.put("apiId", "Rainmaker");
        ri.put("ver", "1.0");
        ObjectNode userInfo = mapper.createObjectNode();
        userInfo.put("uuid", internalUserUuid);
        userInfo.put("type", "SYSTEM");
        ri.set("userInfo", userInfo);
        return ri;
    }

    private static String textOrEmpty(JsonNode n) {
        return n == null || n.isNull() || n.isMissingNode() ? "" : n.asText("");
    }

    private String[] extractDistrictAndBlockFromBoundaryCode(String boundaryCode) {
        String district = "";
        String block = "";
        if (boundaryCode == null || boundaryCode.isBlank()) {
            return new String[]{district, block};
        }
        String normalizedBoundaryCode = boundaryCode.replace('.', '_');
        List<String> segments = new ArrayList<>();
        for (String s : normalizedBoundaryCode.split("_")) {
            if (s != null && !s.isBlank()) {
                segments.add(s);
            }
        }
        if (segments.isEmpty()) {
            return new String[]{district, block};
        }
        int facilityIndex = -1;
        for (int i = 0; i < segments.size(); i++) {
            if (segments.get(i).toUpperCase(Locale.ROOT).contains("FAC/")) {
                facilityIndex = i;
                break;
            }
        }
        if (facilityIndex > 1) {
            district = segments.get(facilityIndex - 2);
            block = segments.get(facilityIndex - 1);
        } else if (segments.size() >= 4) {
            district = segments.get(2);
            block = segments.get(3);
        } else if (segments.size() >= 3) {
            district = segments.get(segments.size() - 2);
            block = segments.get(segments.size() - 1);
        } else if (segments.size() == 2) {
            district = segments.get(0);
            block = segments.get(1);
        }
        return new String[]{district, block};
    }

    private String extractStateFromBoundaryCode(String boundaryCode) {
        if (boundaryCode == null || boundaryCode.isBlank()) {
            return "";
        }
        String normalized = boundaryCode.replace('.', '_');
        String[] parts = normalized.split("_");
        List<String> segments = new ArrayList<>();
        for (String p : parts) {
            if (p != null && !p.isBlank()) {
                segments.add(p);
            }
        }
        if (segments.size() < 2) {
            return "";
        }
        return segments.get(1);
    }

    private String normalizeBoundaryName(JsonNode value) {
        if (value == null || value.isNull() || value.isMissingNode()) {
            return "";
        }
        String text = value.asText("").trim();
        if (text.isBlank()) {
            return "";
        }
        String normalized = text.replace('.', '_');
        String[] parts = normalized.split("_");
        if (parts.length == 0) {
            return text;
        }
        return parts[parts.length - 1].trim();
    }

    private String firstNonBlank(String preferred, String fallback) {
        if (preferred != null && !preferred.isBlank()) {
            return preferred;
        }
        return fallback == null ? "" : fallback;
    }

    private HttpHeaders buildEsHeaders(String esUsername, String esPassword) {
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

    private String trimTrailingSlash(String url) {
        if (url == null || url.length() <= 1) {
            return url;
        }
        return url.endsWith("/") ? url.substring(0, url.length() - 1) : url;
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

        return new RestTemplate(requestFactory);
    }

    private static final class BulkUpdate {
        final String index;
        final String id;
        final ObjectNode userLoginReport;

        BulkUpdate(String index, String id, ObjectNode userLoginReport) {
            this.index = index;
            this.id = id;
            this.userLoginReport = userLoginReport;
        }
    }

    private static final class EnrichedLocation {
        final String state;
        final String district;
        final String block;
        final String healthFacilityName;

        EnrichedLocation(String state, String district, String block, String healthFacilityName) {
            this.state = state;
            this.district = district;
            this.block = block;
            this.healthFacilityName = healthFacilityName;
        }

        static EnrichedLocation empty() {
            return new EnrichedLocation("", "", "", "");
        }
    }
}
