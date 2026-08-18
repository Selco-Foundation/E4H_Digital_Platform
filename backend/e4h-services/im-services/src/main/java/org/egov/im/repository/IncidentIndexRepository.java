package org.egov.im.repository;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.extern.slf4j.Slf4j;
import org.egov.im.config.IMConfiguration;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

/**
 * Read-side access to the incident Elasticsearch index.
 *
 * <p>Indexing an incident is a full-document replace, so any field the service does not send is
 * lost. The mapped vendor is only ever set when a COMPLAINT_RESOLVER is assigned, which means on
 * every other update the previously indexed value has to be read back and re-sent verbatim.
 */
@Repository
@Slf4j
public class IncidentIndexRepository {

    private static final String MAPPED_VENDOR_NAME = "mappedVendorName";
    private static final String MAPPED_VENDOR_USER_NAME = "mappedVendorUserName";

    private static final int CONNECT_TIMEOUT_MS = 3000;
    private static final int READ_TIMEOUT_MS = 5000;

    private final IMConfiguration config;
    private final ObjectMapper mapper;

    /**
     * Deliberately not the shared (tracer-provided) RestTemplate, which carries no timeouts. This
     * lookup sits on the ticket-update request path and must fail fast rather than hang on a
     * degraded Elasticsearch.
     */
    private final RestTemplate restTemplate;

    public IncidentIndexRepository(IMConfiguration config, ObjectMapper mapper) {
        this.config = config;
        this.mapper = mapper;

        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(CONNECT_TIMEOUT_MS);
        requestFactory.setReadTimeout(READ_TIMEOUT_MS);
        this.restTemplate = new RestTemplate(requestFactory);
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
