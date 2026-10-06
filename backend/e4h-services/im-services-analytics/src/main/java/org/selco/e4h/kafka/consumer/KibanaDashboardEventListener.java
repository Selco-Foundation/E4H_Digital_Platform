package org.selco.e4h.kafka.consumer;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.selco.e4h.config.KibanaDashboardProperties;
import org.selco.e4h.util.UserAnalyticsConstants;
import org.selco.e4h.web.models.KibanaDashboardEvent;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.HashMap;
import java.util.UUID;

/**
 * Translates raw Kibana audit-log dashboard views into {@link KibanaDashboardEvent}s on the indexer
 * topic, so they land in the {@code kibana-dashboard-report} index the weekly report counts out of.
 * <p>
 * The inbound message is the Kibana audit record as shipped, e.g.
 * <pre>
 * {"@timestamp": 1787823485.18961, "event": "kibana_dashboard_view",
 *  "remote_addr": "172.31.70.254", "time": 1787823485}
 * </pre>
 * It is a pass-through translation — no user, MDMS or localization lookup happens, and there is
 * nothing to look one up by: unlike {@code kibana_login}, a dashboard-view record carries no
 * username. All the translation adds is a document id and an ISO-8601 {@code event_time}, because the
 * raw epoch seconds cannot be range-queried against the {@code Instant} bounds the report cuts weeks
 * on.
 * <p>
 * Errors are logged with the raw payload and the offset is committed, so a malformed record can never
 * stall the partition.
 * <p>
 * The sibling flow for {@code kibana_login} lives in im-services as {@code KibanaLoginConsumer} and
 * feeds the shared {@code user-analytics-event} topic instead, because those records do carry a
 * username and so have a user to attribute.
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class KibanaDashboardEventListener {

    private static final String FIELD_EVENT = "event";
    private static final String FIELD_REMOTE_ADDR = "remote_addr";
    private static final String FIELD_TIME = "time";
    private static final String FIELD_TIMESTAMP = "@timestamp";

    private final KibanaDashboardProperties properties;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    @KafkaListener(
            topics = "${kibana.dashboard.kafka.source.topic}",
            groupId = "${spring.kafka.consumer.group-id}-kibana-dashboard")
    public void listen(final HashMap<String, Object> record, @Header(KafkaHeaders.RECEIVED_TOPIC) String topic) {
        try {
            String sourceEvent = asString(record.get(FIELD_EVENT));
            // The audit topic may carry other Kibana events; only dashboard views are indexed here.
            if (!UserAnalyticsConstants.KIBANA_DASHBOARD_VIEW_SOURCE_EVENT.equalsIgnoreCase(sourceEvent)) {
                log.info("Kibana dashboard analytics: ignoring non-dashboard-view event {} on topic {}",
                        sourceEvent, topic);
                return;
            }

            KibanaDashboardEvent event = KibanaDashboardEvent.builder()
                    .eventId(UUID.randomUUID().toString())
                    .eventType(UserAnalyticsConstants.KIBANA_DASHBOARD_VIEW_EVENT_TYPE)
                    .eventTime(resolveEventTime(record))
                    .application(UserAnalyticsConstants.KIBANA_APPLICATION)
                    .remoteAddr(asString(record.get(FIELD_REMOTE_ADDR)))
                    .build();

            kafkaTemplate.send(properties.getIndexerTopic(), event.getEventId(), event);
            log.info("Kibana dashboard analytics: published {} event at {} from {}",
                    event.getEventType(), event.getEventTime(), event.getRemoteAddr());
        } catch (Exception ex) {
            log.error("Kibana dashboard analytics: error while listening to value: {} on topic: {}",
                    record, topic, ex);
        }
    }

    /**
     * The record's own time as an ISO-8601 instant, read off {@code time} and falling back to the
     * fractional {@code @timestamp}. Both are epoch seconds; the fraction is truncated because the
     * report only ever counts these into week-wide buckets.
     * <p>
     * A record carrying neither is timestamped on arrival rather than dropped — a view that happened
     * is worth counting even a few seconds off, and the two clocks are the same machine's anyway.
     */
    private String resolveEventTime(HashMap<String, Object> record) {
        Long epochSeconds = asEpochSeconds(record.get(FIELD_TIME));
        if (epochSeconds == null) {
            epochSeconds = asEpochSeconds(record.get(FIELD_TIMESTAMP));
        }
        if (epochSeconds == null) {
            log.warn("Kibana dashboard analytics: no usable {} or {} on record {}, stamping arrival time",
                    FIELD_TIME, FIELD_TIMESTAMP, record);
            return Instant.now().toString();
        }
        return Instant.ofEpochSecond(epochSeconds).toString();
    }

    /** Epoch seconds off whatever numeric shape the deserializer produced, or null if unusable. */
    private Long asEpochSeconds(Object value) {
        if (value instanceof Number) {
            return ((Number) value).longValue();
        }
        if (value == null) {
            return null;
        }
        try {
            return (long) Double.parseDouble(value.toString());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private String asString(Object value) {
        return (value != null) ? value.toString() : null;
    }
}
