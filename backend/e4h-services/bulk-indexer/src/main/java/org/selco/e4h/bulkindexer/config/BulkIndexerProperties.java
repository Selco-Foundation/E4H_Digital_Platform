package org.selco.e4h.bulkindexer.config;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Getter
@Component
public class BulkIndexerProperties {

    @Value("${kafka.topics.bulk-index-documents}")
    private String bulkIndexTopic;

    @Value("${egov.bulkindexer.es.host.name}")
    private String esHostName;

    @Value("${egov.bulkindexer.es.port.no}")
    private int esPortNo;

    @Value("${egov.bulkindexer.es.username}")
    private String esUsername;

    @Value("${egov.bulkindexer.es.password}")
    private String esPassword;

    /** Documents per _bulk call. Keeps a single request well under ES's http.max_content_length. */
    @Value("${egov.bulkindexer.es.bulk.chunk.size}")
    private int bulkChunkSize;
}
