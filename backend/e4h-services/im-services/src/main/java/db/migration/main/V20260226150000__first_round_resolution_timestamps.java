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
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

import javax.net.ssl.SSLContext;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

@Slf4j
public class V20260226150000__first_round_resolution_timestamps extends BaseJavaMigration {

    private static final String MAIN_INDEX = "computed-sla-im-services-write";
    private static final String AUDIT_INDEX = "audit-computed-sla-im-services-write";

    @Override
    public boolean canExecuteInTransaction() {
        return false;
    }

    @Override
    public void migrate(Context context) throws Exception {
        log.info("Starting migration: backfilling first-round resolved/declined timestamps from audit index");

        RestTemplate restTemplate = createRestTemplateWithDisabledSSL();
        ObjectMapper objectMapper = new ObjectMapper();

        String esHost = getEnvOrDefault("EGOV_ES_HOST", "https://localhost:9200");
        String esUsername = getEnvOrDefault("EGOV_ES_USERNAME", "");
        String esPassword = getEnvOrDefault("EGOV_ES_PASSWORD", "");

        log.info("Elasticsearch host: {}", esHost);

        // Step 1: Aggregate over audit index to compute first RESOLVED / REJECTED timestamps per incident
        Map<String, Long> incidentToResolvedTs = new HashMap<>();
        Map<String, Long> incidentToDeclinedTs = new HashMap<>();
        computeFirstRoundTimestampsFromAudit(restTemplate, objectMapper, esHost, AUDIT_INDEX, esUsername, esPassword,
                incidentToResolvedTs, incidentToDeclinedTs);

        log.info("Computed first-round timestamps for {} incidents (resolved) and {} incidents (declined)",
                incidentToResolvedTs.size(), incidentToDeclinedTs.size());

        // Step 2: Apply to main index
        updateIndexWithTimestamps(restTemplate, objectMapper, esHost, MAIN_INDEX, esUsername, esPassword,
                incidentToResolvedTs, incidentToDeclinedTs);

        // Step 3: Apply to audit index itself
        updateIndexWithTimestamps(restTemplate, objectMapper, esHost, AUDIT_INDEX, esUsername, esPassword,
                incidentToResolvedTs, incidentToDeclinedTs);

        log.info("Migration completed: first-round resolvedTimestamp / declinedTimestamp backfilled in ES");
    }

    private void computeFirstRoundTimestampsFromAudit(RestTemplate restTemplate,
                                                      ObjectMapper objectMapper,
                                                      String esHost,
                                                      String auditIndex,
                                                      String esUsername,
                                                      String esPassword,
                                                      Map<String, Long> resolvedMap,
                                                      Map<String, Long> declinedMap) throws Exception {
        String searchUrl = esHost + "/" + auditIndex + "/_search";
        HttpHeaders headers = buildAuthHeaders(esUsername, esPassword);

        // Use composite aggregation with pagination so we can handle more than 10k incidents.
        JsonNode afterKey = null;
        int page = 0;

        while (true) {
            ObjectNode searchBody = objectMapper.createObjectNode();
            searchBody.put("size", 0);

            // aggregation: by incidentId -> first_resolved.min_ts, first_declined.min_ts
            ObjectNode aggs = objectMapper.createObjectNode();

            // composite aggregation on incidentId
            ObjectNode composite = objectMapper.createObjectNode();
            composite.put("size", 1000);
            ArrayNode sources = objectMapper.createArrayNode();
            ObjectNode sourceObj = objectMapper.createObjectNode();
            ObjectNode terms = objectMapper.createObjectNode();
            terms.put("field", "Data.incident.incidentId.keyword");
            sourceObj.set("incidentId", objectMapper.createObjectNode().set("terms", terms));
            sources.add(sourceObj);
            composite.set("sources", sources);
            if (afterKey != null && !afterKey.isNull()) {
                composite.set("after", afterKey);
            }

            ObjectNode byIncident = objectMapper.createObjectNode();
            byIncident.set("composite", composite);

            // RESOLVED filter + min TS
            ObjectNode resolvedMatch = objectMapper.createObjectNode();
            resolvedMatch.put("Data.endingStatus", "RESOLVED");
            ObjectNode resolvedFilterNode = objectMapper.createObjectNode();
            resolvedFilterNode.set("match_phrase", resolvedMatch);
            ObjectNode resolvedMinAgg = objectMapper.createObjectNode();
            resolvedMinAgg.put("field", "Data.auditDetails.lastModifiedTime");
            ObjectNode resolvedMinTs = objectMapper.createObjectNode();
            resolvedMinTs.set("min", resolvedMinAgg);
            ObjectNode resolvedAggs = objectMapper.createObjectNode();
            resolvedAggs.set("min_ts", resolvedMinTs);
            ObjectNode resolvedFilter = objectMapper.createObjectNode();
            resolvedFilter.set("filter", resolvedFilterNode);
            resolvedFilter.set("aggs", resolvedAggs);

            // REJECTED filter + min TS (treated as decline)
            ObjectNode declinedMatch = objectMapper.createObjectNode();
            declinedMatch.put("Data.endingStatus", "REJECTED");
            ObjectNode declinedFilterNode = objectMapper.createObjectNode();
            declinedFilterNode.set("match_phrase", declinedMatch);
            ObjectNode declinedMinAgg = objectMapper.createObjectNode();
            declinedMinAgg.put("field", "Data.auditDetails.lastModifiedTime");
            ObjectNode declinedMinTs = objectMapper.createObjectNode();
            declinedMinTs.set("min", declinedMinAgg);
            ObjectNode declinedAggs = objectMapper.createObjectNode();
            declinedAggs.set("min_ts", declinedMinTs);
            ObjectNode declinedFilter = objectMapper.createObjectNode();
            declinedFilter.set("filter", declinedFilterNode);
            declinedFilter.set("aggs", declinedAggs);

            ObjectNode byIncidentAggs = objectMapper.createObjectNode();
            byIncidentAggs.set("first_resolved", resolvedFilter);
            byIncidentAggs.set("first_declined", declinedFilter);
            byIncident.set("aggs", byIncidentAggs);

            aggs.set("by_incident", byIncident);
            searchBody.set("aggs", aggs);

            HttpEntity<String> searchEntity = new HttpEntity<>(objectMapper.writeValueAsString(searchBody), headers);
            ResponseEntity<JsonNode> resp = restTemplate.postForEntity(searchUrl, searchEntity, JsonNode.class);
            JsonNode body = resp.getBody();
            if (body == null || !body.has("aggregations")) {
                log.warn("Audit index {}: aggregations missing on page {}; stopping backfill", auditIndex, page);
                return;
            }

            JsonNode buckets = body.path("aggregations").path("by_incident").path("buckets");
            if (!buckets.isArray() || buckets.size() == 0) {
                log.info("Audit index {}: no more buckets after page {}; finished pagination", auditIndex, page);
                break;
            }

            for (JsonNode bucket : buckets) {
                String incidentId = bucket.path("key").path("incidentId").asText(null);
                if (incidentId == null) continue;

                JsonNode resolvedVal = bucket.path("first_resolved").path("min_ts").path("value");
                if (resolvedVal != null && resolvedVal.isNumber()) {
                    resolvedMap.put(incidentId, resolvedVal.asLong());
                }

                JsonNode declinedVal = bucket.path("first_declined").path("min_ts").path("value");
                if (declinedVal != null && declinedVal.isNumber()) {
                    declinedMap.put(incidentId, declinedVal.asLong());
                }
            }

            afterKey = body.path("aggregations").path("by_incident").path("after_key");
            page++;

            if (afterKey == null || afterKey.isMissingNode() || afterKey.isNull()) {
                log.info("Audit index {}: reached end of composite aggregation after page {}", auditIndex, page);
                break;
            }
        }
    }

    private void updateIndexWithTimestamps(RestTemplate restTemplate,
                                           ObjectMapper objectMapper,
                                           String esHost,
                                           String indexName,
                                           String esUsername,
                                           String esPassword,
                                           Map<String, Long> resolvedMap,
                                           Map<String, Long> declinedMap) throws Exception {

        String updateByQueryUrl = esHost + "/" + indexName + "/_update_by_query?conflicts=proceed";
        HttpHeaders headers = buildAuthHeaders(esUsername, esPassword);

        for (Map.Entry<String, Long> entry : resolvedMap.entrySet()) {
            String incidentId = entry.getKey();
            Long resolvedTs = entry.getValue();
            Long declinedTs = declinedMap.get(incidentId);

            ObjectNode updateRequest = objectMapper.createObjectNode();
            ObjectNode script = objectMapper.createObjectNode();

            StringBuilder source = new StringBuilder();
            source.append("if (ctx._source.Data == null) { ctx._source.Data = [:]; } ");
            source.append("Long resolvedTs = params.resolvedTs; ");
            source.append("Long declinedTs = params.declinedTs; ");
            source.append("if (resolvedTs != null) { ctx._source.Data.resolvedTimestamp = resolvedTs; } ");
            source.append("if (declinedTs != null) { ctx._source.Data.declinedTimestamp = declinedTs; } ");

            script.put("source", source.toString());

            ObjectNode params = objectMapper.createObjectNode();
            params.put("resolvedTs", resolvedTs);
            if (declinedTs != null) {
                params.put("declinedTs", declinedTs);
            } else {
                params.putNull("declinedTs");
            }
            script.set("params", params);
            updateRequest.set("script", script);

            ObjectNode term = objectMapper.createObjectNode();
            term.put("Data.incident.incidentId.keyword", incidentId);
            updateRequest.set("query", objectMapper.createObjectNode().set("term", term));

            try {
                HttpEntity<String> requestEntity = new HttpEntity<>(objectMapper.writeValueAsString(updateRequest), headers);
                restTemplate.postForEntity(updateByQueryUrl, requestEntity, JsonNode.class);
            } catch (Exception e) {
                log.warn("Failed to update {} for incident {}: {}", indexName, incidentId, e.getMessage());
            }
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

    private String getEnvOrDefault(String key, String defaultValue) {
        String value = System.getenv(key);
        return (value == null || value.isEmpty()) ? defaultValue : value;
    }
}

