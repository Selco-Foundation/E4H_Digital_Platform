package org.egov.im.repository;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.extern.slf4j.Slf4j;
import org.egov.im.config.IMConfiguration;
import org.egov.tracer.model.CustomException;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Repository;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Read-side access to the incident Elasticsearch index.
 *
 * <p>Indexing an incident is a full-document replace, so any field the service does not send is
 * lost. The mapped vendor is only ever set when a COMPLAINT_RESOLVER is assigned, which means on
 * every other update the previously indexed value has to be read back and re-sent verbatim.
 *
 * <p>Also hosts the paged read and bulk partial update the current-owner backfill script runs on.
 */
@Repository
@Slf4j
public class IncidentIndexRepository {

    private static final String MAPPED_VENDOR_NAME = "mappedVendorName";
    private static final String MAPPED_VENDOR_USER_NAME = "mappedVendorUserName";

    private static final int CONNECT_TIMEOUT_MS = 3000;
    private static final int READ_TIMEOUT_MS = 5000;

    private static final int BULK_CONNECT_TIMEOUT_MS = 5000;
    private static final int BULK_READ_TIMEOUT_MS = 60000;

    /**
     * Sort key for the backfill's search_after paging. One document per incident, so the incident id
     * is both unique and stable, which is what search_after needs to not skip or repeat documents.
     */
    private static final String INCIDENT_ID_SORT_FIELD = "Data.incident.incidentId.keyword";

    private final IMConfiguration config;
    private final ObjectMapper mapper;

    /**
     * Deliberately not the shared (tracer-provided) RestTemplate, which carries no timeouts. This
     * lookup sits on the ticket-update request path and must fail fast rather than hang on a
     * degraded Elasticsearch.
     */
    private final RestTemplate restTemplate;

    /**
     * Separate from {@link #restTemplate}: a full-index page read and a bulk write are legitimately
     * slower than the fail-fast budget the request path needs.
     */
    private final RestTemplate bulkRestTemplate;

    public IncidentIndexRepository(IMConfiguration config, ObjectMapper mapper) {
        this.config = config;
        this.mapper = mapper;

        this.restTemplate = buildRestTemplate(CONNECT_TIMEOUT_MS, READ_TIMEOUT_MS);
        this.bulkRestTemplate = buildRestTemplate(BULK_CONNECT_TIMEOUT_MS, BULK_READ_TIMEOUT_MS);
    }

    private RestTemplate buildRestTemplate(int connectTimeoutMs, int readTimeoutMs) {
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(connectTimeoutMs);
        requestFactory.setReadTimeout(readTimeoutMs);
        return new RestTemplate(requestFactory);
    }

    /**
     * Returns the {@code mappedVendorName} / {@code mappedVendorUserName} currently indexed for the
     * given incident.
     *
     * <p>Never throws: Elasticsearch being unreachable must not fail a ticket update. An empty map
     * is returned for an unknown incident, a missing document, or any transport failure, and the
     * caller substitutes the "Not Applicable" placeholder.
     */
    public Map<String, String> fetchIndexedVendor(String incidentId) {
        Map<String, String> vendor = new HashMap<>();
        if (!StringUtils.hasText(incidentId)) {
            return vendor;
        }

        try {
            String url = trimTrailingSlash(config.getEsHost()) + "/" + config.getEsIncidentIndex() + "/_search";

            ObjectNode term = mapper.createObjectNode();
            term.put("Data.incident.incidentId.keyword", incidentId);
            ObjectNode query = mapper.createObjectNode();
            query.set("term", term);

            ObjectNode body = mapper.createObjectNode();
            body.put("size", 1);
            body.set("query", query);
            // Deliberately unsorted: the indexer upserts one document per incident, and sorting on a
            // field this index does not map would make Elasticsearch reject the whole search.
            body.set("_source", mapper.createArrayNode()
                    .add("Data." + MAPPED_VENDOR_NAME)
                    .add("Data." + MAPPED_VENDOR_USER_NAME));

            HttpEntity<String> entity = new HttpEntity<>(mapper.writeValueAsString(body), buildHeaders());
            ResponseEntity<JsonNode> response = restTemplate.postForEntity(url, entity, JsonNode.class);

            JsonNode source = response.getBody() == null ? null
                    : response.getBody().path("hits").path("hits").path(0).path("_source").path("Data");
            if (source == null || source.isMissingNode()) {
                log.info("No indexed document found for incidentId={}, mapped vendor cannot be carried forward",
                        incidentId);
                return vendor;
            }

            putIfPresent(vendor, source, MAPPED_VENDOR_NAME);
            putIfPresent(vendor, source, MAPPED_VENDOR_USER_NAME);
        } catch (Exception e) {
            log.error("Failed to read indexed mapped vendor for incidentId={}, falling back to placeholder",
                    incidentId, e);
        }
        return vendor;
    }

    /**
     * The owner fields to write against one indexed document. A null owner is written as an explicit
     * null so a ticket that has since reached a terminal state stops being reported as owned.
     */
    public record CurrentOwnerPatch(String documentId, String currentOwner, String currentOwnerSystemRole) {
    }

    /**
     * Reads one page of the incident index for the current-owner backfill: enough of each document to
     * derive the owner ({@code currentProcessInstance}) and to see whether it already holds the right
     * one.
     *
     * <p>Paged with search_after rather than from/size, which Elasticsearch refuses past
     * {@code index.max_result_window} and which shifts under concurrent writes. Documents with no
     * indexed incident id are filtered out rather than sorted last: they have no sort value to resume
     * a page from, which would break the walk, and a document with no incident id is not a ticket.
     *
     * @param size        page size
     * @param searchAfter sort values of the previous page's last hit, null for the first page
     * @return the raw {@code hits.hits} array, empty once the index has been walked
     */
    public JsonNode fetchOwnerBackfillPage(int size, JsonNode searchAfter) {
        String url = trimTrailingSlash(config.getEsHost()) + "/" + config.getEsIncidentIndex() + "/_search";

        ObjectNode body = mapper.createObjectNode();
        body.put("size", size);
        body.set("query", mapper.createObjectNode().set("bool", mapper.createObjectNode()
                .set("filter", mapper.createArrayNode().add(mapper.createObjectNode()
                        .set("exists", mapper.createObjectNode().put("field", INCIDENT_ID_SORT_FIELD))))));
        body.set("_source", mapper.createArrayNode()
                .add("Data.incident.incidentId")
                .add("Data.currentProcessInstance.businessService")
                .add("Data.currentProcessInstance.state")
                .add("Data.currentOwner")
                .add("Data.currentOwnerSystemRole"));
        body.set("sort", mapper.createArrayNode().add(mapper.createObjectNode()
                .set(INCIDENT_ID_SORT_FIELD, mapper.createObjectNode().put("order", "asc"))));
        if (searchAfter != null && searchAfter.isArray() && !searchAfter.isEmpty()) {
            body.set("search_after", searchAfter);
        }

        try {
            HttpEntity<String> entity = new HttpEntity<>(mapper.writeValueAsString(body), buildHeaders());
            ResponseEntity<JsonNode> response = bulkRestTemplate.postForEntity(url, entity, JsonNode.class);
            JsonNode root = response.getBody();
            return root == null ? mapper.createArrayNode() : root.path("hits").path("hits");
        } catch (Exception e) {
            // Deliberately fatal: swallowing this would end the walk early and report a partial run
            // as a complete one.
            throw new CustomException("ES_SEARCH_FAILED",
                    "Failed to read a page of " + config.getEsIncidentIndex() + ": " + e.getMessage());
        }
    }

    /**
     * Writes the owner fields of the given documents in a single {@code _bulk} partial update. Only
     * those two fields are sent, so nothing else on the document is touched.
     *
     * @return the number of documents Elasticsearch accepted; failures are logged per document
     */
    public int bulkUpdateCurrentOwner(List<CurrentOwnerPatch> patches) {
        if (CollectionUtils.isEmpty(patches)) {
            return 0;
        }

        StringBuilder ndjson = new StringBuilder();
        try {
            for (CurrentOwnerPatch patch : patches) {
                ObjectNode action = mapper.createObjectNode();
                action.set("update", mapper.createObjectNode()
                        .put("_index", config.getEsIncidentIndex())
                        .put("_id", patch.documentId()));
                ndjson.append(mapper.writeValueAsString(action)).append('\n');

                ObjectNode data = mapper.createObjectNode();
                putOrNull(data, "currentOwner", patch.currentOwner());
                putOrNull(data, "currentOwnerSystemRole", patch.currentOwnerSystemRole());
                ObjectNode wrapper = mapper.createObjectNode();
                wrapper.set("doc", mapper.createObjectNode().set("Data", data));
                ndjson.append(mapper.writeValueAsString(wrapper)).append('\n');
            }
        } catch (Exception e) {
            throw new CustomException("ES_BULK_BUILD_FAILED", "Failed to build the bulk owner update: " + e.getMessage());
        }

        HttpHeaders headers = buildHeaders();
        headers.setContentType(new MediaType("application", "x-ndjson", StandardCharsets.UTF_8));

        try {
            HttpEntity<String> entity = new HttpEntity<>(ndjson.toString(), headers);
            ResponseEntity<JsonNode> response = bulkRestTemplate.postForEntity(
                    trimTrailingSlash(config.getEsHost()) + "/_bulk", entity, JsonNode.class);
            return countBulkSuccesses(response.getBody(), patches.size());
        } catch (Exception e) {
            log.error("Bulk owner update of {} documents failed entirely", patches.size(), e);
            return 0;
        }
    }

    private void putOrNull(ObjectNode target, String field, String value) {
        if (StringUtils.hasText(value)) {
            target.put(field, value);
        } else {
            target.putNull(field);
        }
    }

    private int countBulkSuccesses(JsonNode body, int sent) {
        if (body == null || !body.path("items").isArray()) {
            log.warn("Bulk owner update returned no items for {} documents", sent);
            return 0;
        }
        int succeeded = 0;
        for (JsonNode item : body.path("items")) {
            JsonNode update = item.path("update");
            int status = update.path("status").asInt(0);
            if (status >= 200 && status < 300) {
                succeeded++;
            } else {
                log.warn("Bulk owner update failed for document {}: {}", update.path("_id").asText(), update);
            }
        }
        return succeeded;
    }

    private void putIfPresent(Map<String, String> target, JsonNode source, String field) {
        JsonNode value = source.path(field);
        if (!value.isMissingNode() && !value.isNull() && StringUtils.hasText(value.asText())) {
            target.put(field, value.asText());
        }
    }

    private HttpHeaders buildHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        if (StringUtils.hasText(config.getEsUsername())) {
            String credentials = config.getEsUsername() + ":" + config.getEsPassword();
            headers.set(HttpHeaders.AUTHORIZATION,
                    "Basic " + Base64.getEncoder().encodeToString(credentials.getBytes(StandardCharsets.UTF_8)));
        }
        return headers;
    }

    private String trimTrailingSlash(String host) {
        return host != null && host.endsWith("/") ? host.substring(0, host.length() - 1) : host;
    }
}
