package org.egov.im.consumer;

import java.time.Instant;
import java.util.HashMap;
import java.util.UUID;

import org.egov.common.contract.request.User;
import org.egov.im.config.IMConfiguration;
import org.egov.im.producer.Producer;
import org.egov.im.util.IMConstants;
import org.egov.im.web.models.UserAnalyticsEvent;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;

/**
 * Translates raw Kibana audit-log logins into {@link UserAnalyticsEvent}s on the shared
 * {@code user-analytics-event} topic, so Kibana logins land in the same user-analytics-report index
 * as the SEM ticket, app-login, facility, boundary, AMC and project events.
 * <p>
 * The inbound message is the Kibana audit record as shipped, e.g.
 * <pre>
 * {"@timestamp": 1786526454.15807, "remote_addr": "172.31.79.20", "time": 1786526454,
 *  "username": "elastic", "event": "kibana_login"}
 * </pre>
 * It is a pass-through translation — no user, MDMS or localization lookup happens. Kibana accounts
 * such as {@code elastic} are Elasticsearch-native and have no counterpart in the egov user table,
 * so {@code user} carries only the username and the role-derived fields
 * ({@code system_role}, {@code primary_role}, {@code user_category}), {@code state} and
 * {@code module} are all left null. {@code application=KIBANA} is the dimension that identifies
 * these records in the index.
 * <p>
 * {@code remote_addr} is deliberately dropped: the shared event schema has no field for it and
 * adding one would touch every other producer.
 * <p>
 * Errors are logged with the raw payload and the offset is committed, so a malformed record can
 * never stall the partition.
 */
@Component
@Slf4j
public class KibanaLoginConsumer {

    private static final String FIELD_USERNAME = "username";
    private static final String FIELD_EVENT = "event";

    private final IMConfiguration config;
    private final Producer producer;

    @Autowired
    public KibanaLoginConsumer(IMConfiguration config, Producer producer) {
        this.config = config;
        this.producer = producer;
    }

    @KafkaListener(topics = {"${im.kafka.kibana.login.topic}"})
    public void listen(final HashMap<String, Object> record, @Header(KafkaHeaders.RECEIVED_TOPIC) String topic) {
        try {
            String sourceEvent = asString(record.get(FIELD_EVENT));
            // The audit topic may carry other Kibana events; only logins map to KIBANA_LOGIN.
            if (!IMConstants.KIBANA_LOGIN_SOURCE_EVENT.equalsIgnoreCase(sourceEvent)) {
                log.info("Kibana login analytics: ignoring non-login event {} on topic {}", sourceEvent, topic);
                return;
            }

            String userName = asString(record.get(FIELD_USERNAME));
            if (userName == null || userName.isBlank()) {
                log.error("Kibana login analytics: no username on record {} from topic {}, skipping", record, topic);
                return;
            }

            String tenantId = config.getKibanaLoginTenantId();
            // Adding in MDC so that tracer can add it in header
            MDC.put(IMConstants.TENANTID_MDC_STRING, tenantId);

            UserAnalyticsEvent event = UserAnalyticsEvent.builder()
                    .eventId(UUID.randomUUID().toString())
                    .eventType(IMConstants.KIBANA_LOGIN_EVENT_TYPE)
                    .eventTime(Instant.now().toString())
                    .application(IMConstants.KIBANA_APPLICATION)
                    .user(User.builder().userName(userName).build())
                    .systemRole(null)
                    .primaryRole(null)
                    .userCategory(null)
                    .state(null)
                    .module(null)
                    .entityId(UUID.randomUUID().toString())
                    .entityType(IMConstants.KIBANA_LOGIN_ENTITY_TYPE)
                    .build();

            producer.push(tenantId, config.getUserAnalyticsTopic(), event);
            log.info("Kibana login analytics: published {} event for user {}",
                    IMConstants.KIBANA_LOGIN_EVENT_TYPE, userName);
        } catch (Exception ex) {
            log.error("Kibana login analytics: error while listening to value: {} on topic: {}", record, topic, ex);
        }
    }

    private String asString(Object value) {
        return (value != null) ? value.toString() : null;
    }
}
