package org.selco.e4h.bulkindexer.repository;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.selco.e4h.bulkindexer.config.BulkIndexerProperties;
import org.selco.e4h.bulkindexer.util.LogSanitizer;
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
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

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

    /**
     * Result of one {@code _bulk} call.
     *
     * <p>{@code succeeded} counts documents ES accepted — which is NOT the same as documents that
     * newly appeared in the index. The {@code index} action overwrites, so a document whose
     * {@code _id} already exists comes back {@code result=updated} with no error and is counted as
     * a success. A batch that is entirely {@code updated} means something upstream sent this
     * {@code _id} set twice; that is invisible in a plain success count, so it is broken out here.
     *
     * <p>{@code resolvedIndexes} is the concrete index (or indexes) ES actually wrote to, read back
     * from the response. When the requested name is a write alias this is the only way to see where
     * the documents landed — an alias pointing somewhere other than what the dashboards read is
     * indistinguishable from a lost write if you only look at the request side.
     */
    public record BulkOutcome(int succeeded, int failed, int created, int updated, int noop,
                              Set<String> resolvedIndexes) {
        static BulkOutcome allFailed(int count) {
            return new BulkOutcome(0, count, 0, 0, 0, Set.of());
        }

        static BulkOutcome empty() {
            return new BulkOutcome(0, 0, 0, 0, 0, Set.of());
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
            return BulkOutcome.empty();
        }

        String body;
        try {
            body = buildNdjson(indexName, documents);
        } catch (Exception e) {
            log.error("Failed to serialise a _bulk body for index={} documents={}",
                    LogSanitizer.sanitize(indexName), documents.size(), e);
            return BulkOutcome.allFailed(documents.size());
        }

        String uri = baseUrl() + "/_bulk";
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(NDJSON);
        headers.set(HttpHeaders.AUTHORIZATION, basicAuth());

        // Sent as bytes, not a String: a String body would go through StringHttpMessageConverter,
        // which falls back to its ISO-8859-1 default because application/x-ndjson carries no
        // charset parameter. Any non-ASCII character in a document (a non-breaking space in a
        // facility name, say) would then reach ES as a bare Latin-1 byte and be rejected with
        // "Invalid UTF-8 start byte".
        byte[] payload = body.getBytes(StandardCharsets.UTF_8);

        try {
            ResponseEntity<Map> response = restTemplate.exchange(
                    uri, HttpMethod.POST, new HttpEntity<>(payload, headers), Map.class);
            return interpret(indexName, documents.size(), response.getBody());
        } catch (Exception e) {
            log.error("_bulk call failed for index={} documents={} uri={}",
                    LogSanitizer.sanitize(indexName), documents.size(), LogSanitizer.sanitize(uri), e);
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
            log.error("_bulk returned an empty body for index={} documents={}",
                    LogSanitizer.sanitize(indexName), sent);
            return BulkOutcome.allFailed(sent);
        }
        boolean flaggedErrors = Boolean.TRUE.equals(response.get("errors"));

        // Walk items even when errors==false. The flag only tells us nothing was *rejected*; it says
        // nothing about whether the write created a document or silently overwrote one, nor which
        // concrete index it landed in. Both are needed to tell "ES never got it" apart from "ES got
        // it and put it somewhere the dashboards are not reading".
        List<Map<String, Object>> items = (List<Map<String, Object>>) response.get("items");
        if (items == null) {
            if (!flaggedErrors) {
                log.warn("_bulk returned no items array for index={} documents={} — "
                                + "counting all as succeeded on the errors=false flag alone",
                        LogSanitizer.sanitize(indexName), sent);
                return new BulkOutcome(sent, 0, 0, 0, 0, Set.of());
            }
            log.error("_bulk reported errors but returned no items for index={}",
                    LogSanitizer.sanitize(indexName));
            return BulkOutcome.allFailed(sent);
        }

        int failed = 0;
        int created = 0;
        int updated = 0;
        int noop = 0;
        int unknown = 0;
        Set<String> resolvedIndexes = new LinkedHashSet<>();

        for (Map<String, Object> item : items) {
            Map<String, Object> result = (Map<String, Object>) item.get("index");
            if (result == null) {
                continue;
            }
            Object concreteIndex = result.get("_index");
            if (concreteIndex != null) {
                resolvedIndexes.add(String.valueOf(concreteIndex));
            }
            Object error = result.get("error");
            if (error != null) {
                failed++;
                log.error("ES rejected document _index={} _id={} status={} error={}",
                        LogSanitizer.sanitize(result.get("_index")), LogSanitizer.sanitize(result.get("_id")),
                        LogSanitizer.sanitize(result.get("status")), LogSanitizer.sanitize(error));
                continue;
            }
            // "created" = new _id. "updated" = an existing _id was overwritten. "noop" = the source
            // was byte-identical and ES skipped the write.
            String outcome = String.valueOf(result.get("result"));
            switch (outcome) {
                case "created" -> created++;
                case "updated" -> updated++;
                case "noop" -> noop++;
                default -> unknown++;
            }
        }

        if (items.size() != sent) {
            log.error("_bulk item count mismatch for index={} sent={} itemsReturned={} — "
                            + "some documents in this chunk have no result at all",
                    LogSanitizer.sanitize(indexName), sent, items.size());
        }
        if (unknown > 0) {
            log.warn("_bulk returned {} item(s) with an unrecognised result field for index={}",
                    unknown, LogSanitizer.sanitize(indexName));
        }
        // A requested name that resolves to something else is the alias case worth seeing explicitly.
        if (resolvedIndexes.size() > 1 || (!resolvedIndexes.isEmpty() && !resolvedIndexes.contains(indexName))) {
            log.info("_bulk requested index={} resolved to concrete index(es)={}",
                    LogSanitizer.sanitize(indexName), LogSanitizer.sanitize(resolvedIndexes.toString()));
        }

        return new BulkOutcome(sent - failed, failed, created, updated, noop, resolvedIndexes);
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
