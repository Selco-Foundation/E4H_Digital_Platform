package org.selco.e4h.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.minidev.json.JSONArray;
import org.apache.kafka.common.protocol.types.Field;
import org.egov.common.contract.request.RequestInfo;
import org.selco.e4h.util.ElasticSearchClient;
import org.selco.e4h.util.IMConstants;
import org.selco.e4h.util.MdmsUtil;
import org.selco.e4h.web.models.BusinessHours;
import org.selco.e4h.web.models.SLARequest;
import org.selco.e4h.web.models.workflow.ProcessInstance;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.time.*;
import java.time.format.TextStyle;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

import static org.selco.e4h.util.IMConstants.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class PrioritySLAService {

    private static final String STATE = "state";

    private final UpdateService updateService;
    private final MdmsUtil mdmsUtil;
    private final ElasticSearchClient esClient;
    private final JdbcTemplate jdbcTemplate;
    private final WorkflowService workflowService;

    private static final ZoneId INDIA_ZONE = ZoneId.of("Asia/Kolkata");

    public void computeAndUpdateSLA(SLARequest request, boolean transform , boolean closedTickets) {
        int pageSize = 5000;
        int from = 0;
        boolean hasMore = true;
        RequestInfo requestInfo = request.getRequestInfo();
        Map<String, Map<String, JSONArray>> bhMdmsData = mdmsUtil.fetchMdmsData(
                request.getRequestInfo(), request.getTenantId(),
                IMConstants.MODULE_NAME_COMMON_MASTERS,
                Collections.singletonList(IMConstants.BUSINESS_HOUR_MASTER));

        BusinessHours bh = parseBusinessHours(bhMdmsData);
        Map<TenantServiceStateKey, Duration> slaMap = loadSLADurationsFromBusinessService();

        while (hasMore) {
            List<Map<String, Object>> tickets = transform
                    ? esClient.fetchOldRequiredTicketsFromImServices(from, pageSize, closedTickets)
                    : esClient.fetchRequiredTickets(from, pageSize,closedTickets);

            if (tickets.isEmpty()) break;

            for (Map<String, Object> ticket : tickets) {
                try {
                    updateTicket(ticket, slaMap, bh, transform , requestInfo);
                } catch (Exception e) {
                    log.error("Error processing ticket: {}", ticket, e);
                }
            }

            hasMore = tickets.size() == pageSize;
            from += pageSize;
        }
    }


    private void updateTicket(Map<String, Object> ticket, Map<TenantServiceStateKey, Duration> slaMap,
                              BusinessHours bh, boolean transform, RequestInfo requestInfo) {
        Map<String, Object> data = (Map<String, Object>) ticket.get("Data");
        Map<String, Object> auditDetails = (Map<String, Object>) data.get("auditDetails");
        Map<String, Object> incident = (Map<String, Object>) data.get("incident");
        Map<String, Object> boundary = (Map<String, Object>) incident.get("boundary");
        Map<String, Object> currentProcessInstance = (Map<String, Object>) data.get("currentProcessInstance");

        String tenantId = (String) data.get("tenantId");
        String stateCode = (String) boundary.get("stateCode");
        String IncidentId = (String) incident.get("incidentId");

        //get process instances
        List<ProcessInstance> processInstances = workflowService.getAllProcessInstances(tenantId,IncidentId, requestInfo);

        if (tenantId.contains(".")) tenantId = tenantId.split("\\.")[0];
        String state = (String) ((Map<String, Object>) currentProcessInstance.get(STATE)).get("applicationStatus");

        long lastModifiedTime = Long.parseLong(auditDetails.get("lastModifiedTime").toString());
        long now = Instant.now().toEpochMilli();

        long businessElapsedFromModified = calculateBusinessMillis(lastModifiedTime, now, bh);



        String businessService;
        String priority;
        Object bsObj = currentProcessInstance.get("businessService");
        if(bsObj instanceof String bs && bs.contains("_")) {
            businessService = bs;
            priority = bs.split("_", 2)[1];
        } else {
            businessService = "Incident_Medium";
            priority = "Medium";
        }


        TenantServiceStateKey stateKey = new TenantServiceStateKey(tenantId, businessService, state);
        Duration stateSlaDuration = slaMap.getOrDefault(stateKey, Duration.ZERO);
        long stateSla = stateSlaDuration.toMillis();

        Duration totalSla = computeTotalSla(tenantId, priority, state, slaMap, processInstances);
        long definedTotalSla = totalSla.toMillis();

        long totalSlaRemaining = computeTotalSlaRemaining(tenantId,priority,processInstances,slaMap,bh);
        long slaRemaining = stateSla - businessElapsedFromModified;

        Object incidentIdObj = incident.get("incidentId");
        if (incidentIdObj == null) {
            log.warn("Incident ID is null for ticket: {}", ticket);
            return;
        }
        boolean isAClosedTicket = state.equals(CLOSED_AFTER_REJECTION) || state.equals(CLOSED_AFTER_RESOLUTION)
                || state.equals(REJECTED) || state.equals(RESOLVED);

        String incidentId = incidentIdObj.toString();
        if (transform) {
            Map<String, Object> fullDoc = buildDocumentToInsert(stateSla, slaRemaining, totalSlaRemaining, ticket);
            updateService.upsertTransformedTicket(incidentId, fullDoc);
        } else {
            updateService.updateSlaFields(
                    incidentId,
                    slaRemaining,
                    totalSlaRemaining,
                    stateSla,
                    businessService,
                    isAClosedTicket,
                    definedTotalSla
            );
        }
    }



    private Map<String, String> getIncidentKeyToPriorityMapping(Map<String, Map<String, JSONArray>> mdmsData) {
        Map<String, String> map = new HashMap<>();
        JSONArray serviceDefs = mdmsData.get("Incident").get("ServiceDefs");

        for (Object obj : serviceDefs) {
            if (obj instanceof Map<?, ?> entry) {
                String menuPath = (String) entry.get("menuPath");
                String serviceCode = (String) entry.get("serviceCode");
                String priority = (String) entry.get("priority");

                if (menuPath != null && serviceCode != null && priority != null) {
                    String key = buildIncidentKey(menuPath, serviceCode);
                    map.put(key, priority.toUpperCase());
                }
            }
        }

        return map;
    }


    private String buildIncidentKey(String type, String subType) {
        return type.trim().toUpperCase() + "::" + subType.trim().toUpperCase();
    }


    private Map<String, Object> buildDocumentToInsert(long stateSla, long slaRemaining, long totalSlaRemaining, Map<String, Object> oldTicket) {

        Map<String, Object> data = (Map<String, Object>) oldTicket.get("Data");

        if (data == null || data.isEmpty()) {
            log.warn("Missing 'Data' field, using oldTicket directly");
            data = oldTicket;
        }

        data.put("stateSla", stateSla);
        data.put("slaRemaining", slaRemaining);
        data.put("totalSlaRemaining", totalSlaRemaining);

        return data;
    }


    private BusinessHours parseBusinessHours(Map<String, Map<String, JSONArray>> mdmsData) {
        BusinessHours businessHours = new BusinessHours();

        try {
            JSONArray outerArray = mdmsData.get("common-masters").get("BusinessHours");
            if (outerArray == null || outerArray.isEmpty()) {
                log.warn("No business hours configuration found in MDMS");
                return businessHours;
            }

            if (!(outerArray.get(0) instanceof Map<?, ?> outerObject)) {
                log.warn("Invalid business hours structure in MDMS");
                return businessHours;
            }

            Object innerHoursObj = outerObject.get("BusinessHours");
            if (!(innerHoursObj instanceof List<?> innerHoursList)) {
                log.warn("BusinessHours is not a list in MDMS");
                return businessHours;
            }

            Map<String, BusinessHours.Schedule> scheduleMap = new HashMap<>();
            for (Object item : innerHoursList) {
                if (!(item instanceof Map<?, ?> entry)) continue;
                Object dayObj = entry.get("day");
                Object startObj = entry.get("start");
                Object endObj = entry.get("end");
                if (dayObj == null || startObj == null || endObj == null) continue;
                String day = dayObj.toString().toUpperCase();
                String start = startObj.toString();
                String end = endObj.toString();
                scheduleMap.put(day, new BusinessHours.Schedule(start, end));
            }
            businessHours.setSchedule(scheduleMap);
        } catch (Exception e) {
            log.error("Error parsing business hours from MDMS", e);
        }
        return businessHours;
    }

    private long calculateBusinessMillis(long startMillis, long endMillis, BusinessHours businessHours) {
        if (startMillis > endMillis) return 0;
        if (businessHours == null || businessHours.getSchedule() == null) return endMillis - startMillis;

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


    public long computeTotalSlaRemaining( String tenantId, String priority, List<ProcessInstance> processInstances, Map<TenantServiceStateKey, Duration> slaMap, BusinessHours businessHours) {
        if (processInstances == null || processInstances.isEmpty()) {
            return 0;
        }
        String businessService = INCIDENT_UNDERSCORE + capitalize(priority);
        Duration total = Duration.ZERO;


        long remainingTotalSla = 0;

        for (int i = 0; i < processInstances.size(); i++) {
            ProcessInstance current = processInstances.get(i);
            String status = current.getState().getApplicationStatus();

            if (PENDING_RESOLUTION.equals(status) || PENDING_FOR_ASSIGNMENT.equals(status)
                    || status.startsWith(PENDING_ASSIGNMENT_PREFIX) || status.startsWith(PENDING_RESOLUTION_PREFIX)) {

                long prevStateTime = current.getAuditDetails().getCreatedTime();
                long nextStateTime = (i + 1) < processInstances.size() ? processInstances.get(i + 1).getAuditDetails().getCreatedTime()
                        : Instant.now().toEpochMilli();

                long currentStateTimeSpent = calculateBusinessMillis(prevStateTime, nextStateTime, businessHours);
                long currentStateDefinedSla = getDurationFromMap(slaMap, tenantId, businessService, status).toMillis();
                if((i+1) >= processInstances.size() || currentStateDefinedSla-currentStateTimeSpent<0){
                    remainingTotalSla += currentStateDefinedSla-currentStateTimeSpent;
                }
            }
        }
        String currentState = processInstances.get(processInstances.size() - 1).getState().getState();
        if (currentState.equals(PENDING_FOR_ASSIGNMENT)) {
            remainingTotalSla += getDurationFromMap(slaMap, tenantId, businessService, PENDING_RESOLUTION).toMillis();
        } else if (currentState.startsWith(PENDING_ASSIGNMENT_PREFIX)) {
            String suffix = currentState.replace(PENDING_ASSIGNMENT_PREFIX, "");
            String resolutionState = PENDING_RESOLUTION_PREFIX + suffix;
            remainingTotalSla += getDurationFromMap(slaMap, tenantId, businessService, resolutionState).toMillis();
        }

        return remainingTotalSla;
    }

    private Duration computeTotalSla(String tenantId, String priority, String currentState, Map<TenantServiceStateKey, Duration> slaMap, List<ProcessInstance> processInstances) {
        String businessService = INCIDENT_UNDERSCORE + capitalize(priority);
        Duration total = Duration.ZERO;

        //calculating sla for all states till current state
        List<String> previousStates = processInstances
                .stream()
                .map(p -> p.getState().getApplicationStatus())
                .collect(Collectors.toList());

        for(String state : previousStates){
            if(PENDING_FOR_ASSIGNMENT.equals(state) || PENDING_RESOLUTION.equals(state)
                    || state.startsWith(PENDING_ASSIGNMENT_PREFIX) || (state.startsWith(PENDING_RESOLUTION_PREFIX))){
                total = total.plus(getDurationFromMap(slaMap, tenantId, businessService, state));
            }
        }
        if (currentState.equals(PENDING_FOR_ASSIGNMENT)) {
            total = total.plus(getDurationFromMap(slaMap, tenantId, businessService, PENDING_RESOLUTION));
        } else if (currentState.startsWith(PENDING_ASSIGNMENT_PREFIX)) {
            String suffix = currentState.replace(PENDING_ASSIGNMENT_PREFIX, "");
            String resolutionState = PENDING_RESOLUTION_PREFIX + suffix;
            total = total.plus(getDurationFromMap(slaMap, tenantId, businessService, resolutionState));
        }
        return total;
    }

    private Duration getDurationFromMap(Map<TenantServiceStateKey, Duration> map, String tenantId, String service, String state) {
        return map.getOrDefault(new TenantServiceStateKey(tenantId, service, state), Duration.ZERO);
    }

    private String capitalize(String value) {
        if (value == null || value.isEmpty()) return value;
        return value.substring(0, 1).toUpperCase() + value.substring(1).toLowerCase();
    }
}
