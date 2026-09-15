package org.selco.e4h.bulkindexer.repository;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.selco.e4h.bulkindexer.config.BulkIndexerProperties;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Repository;
import org.springframework.web.client.RestTemplate;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Writes mapped documents to Elasticsearch through the {@code _bulk} API. */
@Slf4j
@Repository
@RequiredArgsConstructor
public class EsBulkRepository {

    private static final MediaType NDJSON = MediaType.valueOf("application/x-ndjson");

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    private final BulkIndexerProperties properties;

    /** One document destined for {@code indexName}; a null {@code id} lets ES generate one. */
    public record IndexedDocument(String id, Map<String, Object> document) {
    }

    public record BulkOutcome(int succeeded, int failed) {
        static BulkOutcome allFailed(int count) {
            return new BulkOutcome(0, count);
        }
    }

    /**
     * Sends one {@code _bulk} request. Uses the {@code index} action so a document with an existing
     * {@code _id} is overwritten, making re-runs of the same period idempotent.
     *
     * <p>Never throws: per-document rejections are logged and counted, and a whole-request failure
     * counts every document in the chunk as failed.
     */
    public BulkOutcome bulkIndex(String indexName, List<IndexedDocument> documents) {
        if (documents.isEmpty()) {
            return new BulkOutcome(0, 0);
        }

        String body;
        try {
            body = buildNdjson(indexName, documents);
        } catch (Exception e) {
            log.error("Failed to serialise a _bulk body for index={} documents={}",
                    indexName, documents.size(), e);
            return BulkOutcome.allFailed(documents.size());
        }

        String uri = baseUrl() + "/_bulk";
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(NDJSON);
        headers.set(HttpHeaders.AUTHORIZATION, basicAuth());

        try {
            ResponseEntity<Map> response = restTemplate.exchange(
                    uri, HttpMethod.POST, new HttpEntity<>(body, headers), Map.class);
            return interpret(indexName, documents.size(), response.getBody());
        } catch (Exception e) {
            log.error("_bulk call failed for index={} documents={} uri={}",
                    indexName, documents.size(), uri, e);
            return BulkOutcome.allFailed(documents.size());
        }
    }

    private String buildNdjson(String indexName, List<IndexedDocument> documents) throws Exception {
        StringBuilder ndjson = new StringBuilder();
        for (IndexedDocument document : documents) {
            Map<String, Object> metadata = new LinkedHashMap<>();
            metadata.put("_index", indexName);
            if (document.id() != null) {
                metadata.put("_id", document.id());
            }
            ndjson.append(objectMapper.writeValueAsString(Map.of("index", metadata))).append('\n');
            ndjson.append(objectMapper.writeValueAsString(document.document())).append('\n');
        }
        return ndjson.toString();
    }

    @SuppressWarnings("unchecked")
    private BulkOutcome interpret(String indexName, int sent, Map<String, Object> response) {
        if (response == null) {
            log.error("_bulk returned an empty body for index={} documents={}", indexName, sent);
            return BulkOutcome.allFailed(sent);
        }
        if (!Boolean.TRUE.equals(response.get("errors"))) {
            return new BulkOutcome(sent, 0);
        }

        List<Map<String, Object>> items = (List<Map<String, Object>>) response.get("items");
        if (items == null) {
            log.error("_bulk reported errors but returned no items for index={}", indexName);
            return BulkOutcome.allFailed(sent);
        }

        int failed = 0;
        for (Map<String, Object> item : items) {
            Map<String, Object> result = (Map<String, Object>) item.get("index");
            if (result == null) {
                continue;
            }
            Object error = result.get("error");
            if (error == null) {
                continue;
            }
            failed++;
            log.error("ES rejected document _index={} _id={} status={} error={}",
                    result.get("_index"), result.get("_id"), result.get("status"), error);
        }
        return new BulkOutcome(sent - failed, failed);
    }

    private String baseUrl() {
        return properties.getEsHostName() + ":" + properties.getEsPortNo();
    }

    private String basicAuth() {
        String credentials = properties.getEsUsername() + ":" + properties.getEsPassword();
        return "Basic " + Base64.getEncoder()
                .encodeToString(credentials.getBytes(StandardCharsets.UTF_8));
    }
}
