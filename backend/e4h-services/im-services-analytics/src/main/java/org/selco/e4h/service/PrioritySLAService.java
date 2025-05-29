package org.selco.e4h.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.minidev.json.JSONArray;
import java.time.*;
import java.time.format.TextStyle;

import org.selco.e4h.util.ElasticSearchClient;
import org.selco.e4h.util.IMConstants;
import org.selco.e4h.util.MdmsUtil;
import org.selco.e4h.web.models.BusinessHours;
import org.selco.e4h.web.models.SLARequest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class PrioritySLAService {

    private static final String STATE = "state";
    private final UpdateService updateService;
    private final MdmsUtil mdmsUtil;
    private final ElasticSearchClient esClient;
    private final JdbcTemplate jdbcTemplate;

    private static final ZoneId INDIA_ZONE = ZoneId.of("Asia/Kolkata");

    public void computeAndUpdateSLA(SLARequest request) {
        List<Map<String, Object>> tickets = esClient.fetchOpenTickets();

        Map<String, Map<String, JSONArray>> mdmsData = mdmsUtil.fetchMdmsData(
                request.getRequestInfo(), request.getTenantId(), IMConstants.MODULE_NAME_COMMON_MASTERS, Collections.singletonList(IMConstants.BUSINESS_HOUR_MASTER));

        BusinessHours bh = parseBusinessHours(mdmsData);

        Map<TenantStatePriorityKey, Duration> slaMap = loadSLADurationsFromBusinessService();

        for (Map<String, Object> ticket : tickets) {
            Map<String, Object> data = (Map<String, Object>) ticket.get("Data");
            Map<String, Object> auditDetails = (Map<String, Object>) data.get("auditDetails");
            Map<String, Object> incident = (Map<String, Object>) data.get("incident");
            Map<String, Object> currentProcessInstance = (Map<String, Object>) data.get("currentProcessInstance");

            String tenantId = (String) data.get("tenantId");
            String state = (String) ((Map<String, Object>) currentProcessInstance.get(STATE)).get("applicationStatus");
            String priority = (String) incident.get("priority");

            long createdTime = Long.parseLong(auditDetails.get("createdTime").toString());
            long lastModifiedTime = Long.parseLong(auditDetails.get("lastModifiedTime").toString());
            long now = Instant.now().toEpochMilli();

            Duration totalSla = slaMap.getOrDefault(new TenantStatePriorityKey(tenantId, state, priority), Duration.ZERO);
            long businessElapsedFromCreated = calculateBusinessMillis(createdTime, now, bh);
            long businessElapsedFromModified = calculateBusinessMillis(lastModifiedTime, now, bh);

            long totalSlaRemaining = totalSla.toMillis() - businessElapsedFromCreated;

            long stateSla = currentProcessInstance.get(STATE) instanceof Map
                    ? ((Number) ((Map<?, ?>) currentProcessInstance.get(STATE)).get("sla")).longValue()
                    : 0;

            long slaRemaining = stateSla - businessElapsedFromModified;

            // ✨ Update ES via UpdateService
            String incidentId = incident.get("incidentId").toString();
            updateService.updateSlaFields(
                    incidentId,
                    Math.max(slaRemaining, 0),
                    Math.max(totalSlaRemaining, 0),
                    stateSla
            );
        }
    }

    private BusinessHours parseBusinessHours(Map<String, Map<String, JSONArray>> mdmsData) {
        BusinessHours businessHours = new BusinessHours();

        try {
            JSONArray hoursArray = mdmsData.get("common-masters").get("BusinessHours");
            if (hoursArray == null || hoursArray.isEmpty()) {
                log.warn("No business hours configuration found in MDMS");
                return businessHours;
            }

            Map<String, BusinessHours.Schedule> scheduleMap = new HashMap<>();
            JsonNode node = new ObjectMapper().convertValue(hoursArray.get(0), JsonNode.class)
                    .path("schedule");

            node.fieldNames().forEachRemaining(day -> {
                JsonNode daySchedule = node.get(day);
                if (daySchedule != null && !daySchedule.isNull()) {
                    String start = daySchedule.get("start").asText();
                    String end = daySchedule.get("end").asText();
                    scheduleMap.put(day.toUpperCase(), new BusinessHours.Schedule(start, end));
                } else {
                    scheduleMap.put(day.toUpperCase(), null);
                }
            });

            businessHours.setSchedule(scheduleMap);
        } catch (Exception e) {
            log.error("Error parsing business hours from MDMS", e);
        }

        return businessHours;
    }

private long calculateBusinessMillis(long startMillis, long endMillis, BusinessHours businessHours) {
    if (startMillis > endMillis) {
        log.warn("Start time {} is after end time {}, returning 0", startMillis, endMillis);
        return 0;
    }
    if (businessHours == null || businessHours.getSchedule() == null) {
        log.warn("Business hours not configured, returning full duration");
        return endMillis - startMillis;
    }
    ZonedDateTime start = Instant.ofEpochMilli(startMillis).atZone(INDIA_ZONE);
    ZonedDateTime end = Instant.ofEpochMilli(endMillis).atZone(INDIA_ZONE);
    long total = 0;

        for (ZonedDateTime dt = start.truncatedTo(ChronoUnit.DAYS);
             !dt.isAfter(end.truncatedTo(ChronoUnit.DAYS));
             dt = dt.plusDays(1)) {

            String dayName = dt.getDayOfWeek().getDisplayName(TextStyle.FULL, Locale.ENGLISH).toUpperCase();
            BusinessHours.Schedule sched = businessHours.getSchedule().get(dayName);

            if (sched == null) continue;

            LocalTime schedStart = LocalTime.of(sched.getStartHour(), sched.getStartMinute());
            LocalTime schedEnd = LocalTime.of(sched.getEndHour(), sched.getEndMinute());

            ZonedDateTime dayStart = dt.with(schedStart);
            ZonedDateTime dayEnd = dt.with(schedEnd);

            ZonedDateTime intervalStart = start.isAfter(dayStart) ? start : dayStart;
            ZonedDateTime intervalEnd = end.isBefore(dayEnd) ? end : dayEnd;

            if (intervalStart.isBefore(intervalEnd)) {
                total += Duration.between(intervalStart, intervalEnd).toMillis();
            }
        }

        return total;
    }


    private record TenantStatePriorityKey(String tenantId, String state, String priority) {}

    private Map<TenantStatePriorityKey, Duration> loadSLADurationsFromBusinessService() {
        String sql = """
        SELECT s.tenantid, b.businessservice, s.applicationstatus, s.sla
        FROM eg_wf_state_v2 s
        JOIN eg_wf_businessservice_v2 b ON s.businessserviceid = b.uuid
        WHERE s.sla IS NOT NULL
    """;

        return jdbcTemplate.query(sql, rs -> {
            Map<TenantStatePriorityKey, Duration> result = new HashMap<>();
            while (rs.next()) {
                String tenantId = rs.getString("tenantid");
                String businessService = rs.getString("businessservice");
                String state = rs.getString("applicationstatus");
                long slaMillis = rs.getLong("sla");

                TenantStatePriorityKey key = new TenantStatePriorityKey(tenantId, businessService, state);
                result.put(key, Duration.ofMillis(slaMillis));
            }
            return result;
        });
    }

}
