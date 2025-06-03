package org.selco.e4h.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.minidev.json.JSONArray;

import org.selco.e4h.util.ElasticSearchClient;
import org.selco.e4h.util.IMConstants;
import org.selco.e4h.util.MdmsUtil;
import org.selco.e4h.web.models.BusinessHours;
import org.selco.e4h.web.models.SLARequest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.time.*;
import java.time.format.TextStyle;
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
        int pageSize = 5000;
        int from = 0;
        boolean hasMore = true;

        Map<String, Map<String, JSONArray>> mdmsData = mdmsUtil.fetchMdmsData(
                request.getRequestInfo(), request.getTenantId(),
                IMConstants.MODULE_NAME_COMMON_MASTERS,
                Collections.singletonList(IMConstants.BUSINESS_HOUR_MASTER));

        BusinessHours bh = parseBusinessHours(mdmsData);
        Map<TenantServiceStateKey, Duration> slaMap = loadSLADurationsFromBusinessService();

        while (hasMore) {
            List<Map<String, Object>> tickets = esClient.fetchOpenTickets(from, pageSize);
            if (tickets.isEmpty()) break;

            for (Map<String, Object> ticket : tickets) {
                try {
                    updateTicket(ticket, slaMap, bh);
                } catch (Exception e) {
                    log.error("Error processing ticket: {}", ticket, e);
                }
            }

            hasMore = tickets.size() == pageSize;
            from += pageSize;
        }
    }

    private void updateTicket(Map<String, Object> ticket, Map<TenantServiceStateKey, Duration> slaMap, BusinessHours bh) {
        Map<String, Object> data = (Map<String, Object>) ticket.get("Data");
        Map<String, Object> auditDetails = (Map<String, Object>) data.get("auditDetails");
        Map<String, Object> incident = (Map<String, Object>) data.get("incident");
        Map<String, Object> currentProcessInstance = (Map<String, Object>) data.get("currentProcessInstance");

        String tenantId = (String) data.get("tenantId");
        if (tenantId.contains(".")) tenantId = tenantId.split("\\.")[0];
        String state = (String) ((Map<String, Object>) currentProcessInstance.get(STATE)).get("applicationStatus");
        String businessService = (String) currentProcessInstance.get("businessService");

        long createdTime = Long.parseLong(auditDetails.get("createdTime").toString());
        long lastModifiedTime = Long.parseLong(auditDetails.get("lastModifiedTime").toString());
        long now = Instant.now().toEpochMilli();

        long businessElapsedFromCreated = calculateBusinessMillis(createdTime, now, bh);
        long businessElapsedFromModified = calculateBusinessMillis(lastModifiedTime, now, bh);

        long stateSla = 0;
        Object stateObj = currentProcessInstance.get(STATE);
        if (stateObj instanceof Map<?, ?> stateMap) {
            Object slaObj = stateMap.get("sla");
            if (slaObj instanceof Number) {
                stateSla = ((Number) slaObj).longValue();
            }
        }

        String priority = extractPriority(businessService);
        Duration totalSla = computeHappyFlowTotalSla(tenantId, priority, state, slaMap);

        long totalSlaRemaining = totalSla.toMillis() - businessElapsedFromCreated;
        long slaRemaining = stateSla - businessElapsedFromModified;

        String incidentId = incident.get("incidentId").toString();
        updateService.updateSlaFields(
                incidentId,
                slaRemaining,
                totalSlaRemaining,
                stateSla
        );
    }

    private String extractPriority(String businessService) {
        if (businessService != null && businessService.contains("_")) {
            return businessService.split("_")[1].toUpperCase();
        }
        return "MEDIUM";
    }

    private Duration computeHappyFlowTotalSla(String tenantId, String priority, String currentState, Map<TenantServiceStateKey, Duration> slaMap) {
        String businessService = "Incident_" + priority.toUpperCase();
        Duration total = Duration.ZERO;

        if (currentState.equals("PENDINGFORASSIGNMENT") || currentState.equals("PENDINGRESOLUTION")) {
            total = total.plus(getDurationFromMap(slaMap, tenantId, businessService, "PENDINGFORASSIGNMENT"));
            total = total.plus(getDurationFromMap(slaMap, tenantId, businessService, "PENDINGRESOLUTION"));
        } else if (currentState.startsWith("PENDING_ASSIGNMENT_")) {
            total = total.plus(sumStatesWithPrefix(slaMap, tenantId, businessService, "PENDING_ASSIGNMENT_"));
            total = total.plus(sumStatesWithPrefix(slaMap, tenantId, businessService, "PENDING_RESOLUTION_"));
        }

        return total;
    }

    private Duration getDurationFromMap(Map<TenantServiceStateKey, Duration> map, String tenantId, String service, String state) {
        return map.getOrDefault(new TenantServiceStateKey(tenantId, service, state), Duration.ZERO);
    }

    private Duration sumStatesWithPrefix(Map<TenantServiceStateKey, Duration> map, String tenantId, String service, String prefix) {
        return map.entrySet().stream()
                .filter(entry -> entry.getKey().tenantId().equals(tenantId))
                .filter(entry -> entry.getKey().businessService().equals(service))
                .filter(entry -> entry.getKey().state().startsWith(prefix))
                .map(Map.Entry::getValue)
                .reduce(Duration.ZERO, Duration::plus);
    }

    private BusinessHours parseBusinessHours(Map<String, Map<String, JSONArray>> mdmsData) {
        BusinessHours businessHours = new BusinessHours();

        try {
            JSONArray outerArray = mdmsData.get("common-masters").get("BusinessHours");
            if (outerArray == null || outerArray.isEmpty()) {
                log.warn("No business hours configuration found in MDMS");
                return businessHours;
            }

            Map<String, Object> outerObject = (Map<String, Object>) outerArray.get(0);
            List<Map<String, String>> innerHours = (List<Map<String, String>>) outerObject.get("BusinessHours");

            Map<String, BusinessHours.Schedule> scheduleMap = new HashMap<>();
            for (Map<String, String> entry : innerHours) {
                String day = entry.get("day").toUpperCase();
                String start = entry.get("start");
                String end = entry.get("end");
                scheduleMap.put(day, new BusinessHours.Schedule(start, end));
            }

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

    public record TenantServiceStateKey(String tenantId, String businessService, String state) {}

    private Map<TenantServiceStateKey, Duration> loadSLADurationsFromBusinessService() {
        try {
            String sql = """
                SELECT s.tenantid, b.businessservice, s.applicationstatus, s.sla
                FROM eg_wf_state_v2 s
                JOIN eg_wf_businessservice_v2 b ON s.businessserviceid = b.uuid
                WHERE s.sla IS NOT NULL
            """;

            return jdbcTemplate.query(sql, rs -> {
                Map<TenantServiceStateKey, Duration> result = new HashMap<>();
                while (rs.next()) {
                    String tenantId = rs.getString("tenantid");
                    String businessService = rs.getString("businessservice");
                    String state = rs.getString("applicationstatus");
                    long slaMillis = rs.getLong("sla");

                    TenantServiceStateKey key = new TenantServiceStateKey(tenantId, businessService, state);
                    result.put(key, Duration.ofMillis(slaMillis));
                }
                return result;
            });
        } catch (Exception e) {
            log.error("Failed to load SLA durations from database", e);
            return Collections.emptyMap();
        }
    }
}
