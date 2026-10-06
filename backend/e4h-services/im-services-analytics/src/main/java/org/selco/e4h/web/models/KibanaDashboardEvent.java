package org.selco.e4h.web.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * A Kibana dashboard view, published to the indexer topic so it lands in the
 * {@code kibana-dashboard-report} index.
 * <p>
 * Deliberately <em>not</em> a {@code UserAnalyticsEvent}: the raw Kibana record carries no username,
 * so these views have no user to attribute activity to and would only dilute the active-user and
 * event-type censuses of the shared {@code user-analytics-report} index. They get their own index,
 * which also gives {@code remote_addr} — a field the shared event schema has no room for — somewhere
 * to live.
 * <p>
 * Field names are snake_case to match the indexer convention, as in
 * {@code UserAnalyticsEvent}.
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class KibanaDashboardEvent {

    /** Unique id for this event (UUID); the indexer uses it as the document id. */
    @JsonProperty("event_id")
    private String eventId;

    /** Always {@code KIBANA_DASHBOARD_VIEW}. Carried so the index is self-describing. */
    @JsonProperty("event_type")
    private String eventType;

    /**
     * UTC timestamp (ISO-8601) the view happened at, derived from the raw record's epoch-second
     * {@code time}. ISO rather than epoch so the weekly report can range-query it with the same
     * {@code Instant.toString()} bounds it uses on {@code user-analytics-report}.
     */
    @JsonProperty("event_time")
    private String eventTime;

    /** Always {@code KIBANA}, matching the dimension the Kibana login events carry. */
    @JsonProperty("application")
    private String application;

    /** Client address the view came from, passed through as shipped. */
    @JsonProperty("remote_addr")
    private String remoteAddr;
}
