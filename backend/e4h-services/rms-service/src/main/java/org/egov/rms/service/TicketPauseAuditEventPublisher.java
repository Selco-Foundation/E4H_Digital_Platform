package org.egov.rms.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.egov.common.contract.request.RequestInfo;
import org.egov.rms.config.RMSConfiguration;
import org.egov.rms.model.TicketPauseManageRequest;
import org.egov.rms.producer.Producer;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

@Service
@Slf4j
@RequiredArgsConstructor
public class TicketPauseAuditEventPublisher {

    private final Producer producer;
    private final RMSConfiguration config;

    public void publishPauseEvent(
            RequestInfo requestInfo,
            TicketPauseManageRequest.Action action,
            String facilityId,
            String facilityName,
            String boundaryCode,
            Instant pausedUntil,
            String reason,
            String requestedBy,
            boolean isPaused,
            String tenantIdOverride
    ) {
        String topic = config.getTicketPauseAuditTopicIndexer();
        if (!StringUtils.hasText(topic)) {
            log.warn("Skipping RMS pause audit publish because topic is not configured");
            return;
        }

        String tenantId = StringUtils.hasText(tenantIdOverride)
                ? tenantIdOverride.trim()
                : extractTenantId(requestInfo);
        long eventTime = Instant.now().toEpochMilli();
        Map<String, Object> event = new LinkedHashMap<>();
        event.put("eventId", buildDocumentId(facilityId));
        event.put("eventType", "RMS_TICKET_PAUSE");
        event.put("action", action.name());
        event.put("isPaused", isPaused);
        event.put("facilityId", facilityId);
        event.put("facilityName", facilityName);
        event.put("boundaryCode", boundaryCode);
        event.put("pausedUntil", pausedUntil != null ? pausedUntil.toString() : null);
        event.put("reason", reason);
        event.put("requestedBy", requestedBy);
        event.put("tenantId", tenantId);
        event.put("eventTime", eventTime);
        event.put("requestInfo", requestInfo);

        log.info("Publishing pause audit event: action={}, facilityId={}, isPaused={}, topic={}",
                action, facilityId, isPaused, topic);
        producer.push(topic, event);
    }

    private String buildDocumentId(String facilityId) {
        String safeFacility = StringUtils.hasText(facilityId) ? facilityId.trim() : "unknown-facility";
        // Keep a stable per-facility document key for upsert behavior in indexer.
        return safeFacility;
    }

    private String extractTenantId(RequestInfo requestInfo) {
        if (requestInfo != null && requestInfo.getUserInfo() != null
                && StringUtils.hasText(requestInfo.getUserInfo().getTenantId())) {
            return requestInfo.getUserInfo().getTenantId().trim();
        }
        return config.getDefaultTenantId();
    }
}
