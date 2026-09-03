package org.selco.e4h.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Topics, index name and field path for the Kibana dashboard-view pipeline: the raw audit topic it
 * reads, the indexer topic it writes, and the index the weekly report counts out of.
 * <p>
 * The field path is configuration for the same reason the user-analytics ones are — the index mapping
 * decides how a field is queryable, so a mapping change should be a config redeploy, not a code one.
 */
@Component
@Getter
@Setter
public class KibanaDashboardProperties {

    /**
     * Topic egov-indexer reads to build {@link #index}.
     * <p>
     * The raw source topic is not mirrored here — {@code KibanaDashboardEventListener} reads
     * {@code kibana.dashboard.kafka.source.topic} straight off its {@code @KafkaListener}, which needs
     * a property placeholder rather than a bean call.
     */
    @Value("${kibana.dashboard.kafka.indexer.topic}")
    private String indexerTopic;

    @Value("${kibana.dashboard.es.index}")
    private String index;

    /** Field the weekly report's range filter is applied to. */
    @Value("${kibana.dashboard.field.event.time}")
    private String eventTimeField;
}
