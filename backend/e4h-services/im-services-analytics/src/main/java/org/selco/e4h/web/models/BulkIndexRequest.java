package org.selco.e4h.web.models;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

/**
 * Envelope for the bulk-indexer service: every month of one facility travels in a single Kafka
 * message instead of one message per month.
 *
 * <p>Documents are in their final Elasticsearch shape — the bulk-indexer writes them verbatim, so
 * this service owns the index name and the document layout.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class BulkIndexRequest {

    /** Target index or alias. */
    private String index;

    /** Logged by the consumer so a failed batch can be traced back to a facility. */
    private String batchId;

    private List<BulkIndexDocument> documents;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class BulkIndexDocument {

        /** Stable {@code _id} so a re-trigger overwrites rather than duplicates. */
        private String id;

        private Map<String, Object> source;
    }
}
