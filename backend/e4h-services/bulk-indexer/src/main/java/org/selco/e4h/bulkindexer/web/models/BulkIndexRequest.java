package org.selco.e4h.bulkindexer.web.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Envelope consumed from the bulk-index topic.
 *
 * <p>Documents arrive in their final Elasticsearch shape — this service does no reshaping. The
 * producer owns the document shape and names the target index, so adding or renaming a field is a
 * change in the producing service only and needs no change here.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class BulkIndexRequest {

    /** Target index or alias, e.g. {@code co2-monthly-facility-index-write}. */
    private String index;

    /** Free-form producer-side identifier, logged so a failed batch can be traced back. */
    private String batchId;

    private List<BulkIndexDocument> documents = new ArrayList<>();

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class BulkIndexDocument {

        /**
         * The {@code _id} to write under. Supplying a stable id makes re-runs overwrite rather than
         * duplicate; leaving it null lets ES generate one, so repeated batches would append.
         */
        private String id;

        /** The document body, written verbatim. */
        private Map<String, Object> source;
    }
}
