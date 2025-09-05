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

    public void computeAndUpdateSLA(SLARequest request, boolean transform) {
        int pageSize = 5000;
        int from = 0;
        boolean hasMore = true;
        RequestInfo requestInfo = request.getRequestInfo();
        Map<String, Map<String, JSONArray>> serviceDefMdms = mdmsUtil.fetchMdmsData(
                request.getRequestInfo(), request.getTenantId(),
                IMConstants.INCIDENT,
                Collections.singletonList(IMConstants.SERVICE_DEF));

        Map<String, String> incidentTypeToPriority = getIncidentKeyToPriorityMapping(serviceDefMdms);

        Map<String, Map<String, JSONArray>> bhMdmsData = mdmsUtil.fetchMdmsData(
                request.getRequestInfo(), request.getTenantId(),
                IMConstants.MODULE_NAME_COMMON_MASTERS,
                Collections.singletonList(IMConstants.BUSINESS_HOUR_MASTER));

        BusinessHours bh = parseBusinessHours(bhMdmsData);
        Map<TenantServiceStateKey, Duration> slaMap = loadSLADurationsFromBusinessService();

        while (hasMore) {
            List<Map<String, Object>> tickets = transform
                    ? esClient.fetchOldOpenTicketsFromImServices(from, pageSize)
                    : esClient.fetchOpenTickets(from, pageSize);

            if (tickets.isEmpty()) break;

            for (Map<String, Object> ticket : tickets) {
                try {
                    updateTicket(ticket, slaMap, bh, transform, incidentTypeToPriority, requestInfo);
                } catch (Exception e) {
                    log.error("Error processing ticket: {}", ticket, e);
                }
            }

            hasMore = tickets.size() == pageSize;
            from += pageSize;
        }
    }


    private void updateTicket(Map<String, Object> ticket, Map<TenantServiceStateKey, Duration> slaMap,
                              BusinessHours bh, boolean transform, Map<String, String> incidentPriorityMap ,RequestInfo requestInfo) {
        Map<String, Object> data = (Map<String, Object>) ticket.get("Data");
        Map<String, Object> auditDetails = (Map<String, Object>) data.get("auditDetails");
        Map<String, Object> incident = (Map<String, Object>) data.get("incident");
        Map<String, Object> currentProcessInstance = (Map<String, Object>) data.get("currentProcessInstance");

        String tenantId = (String) data.get("tenantId");
        String IncidentId = (String) incident.get("incidentId");
        if (tenantId.contains(".")) tenantId = tenantId.split("\\.")[0];
        String state = (String) ((Map<String, Object>) currentProcessInstance.get(STATE)).get("applicationStatus");

        long createdTime = Long.parseLong(auditDetails.get("createdTime").toString());
        long lastModifiedTime = Long.parseLong(auditDetails.get("lastModifiedTime").toString());
        long now = Instant.now().toEpochMilli();

        long businessElapsedFromCreated = calculateBusinessMillis(createdTime, now, bh);
        long businessElapsedFromModified = calculateBusinessMillis(lastModifiedTime, now, bh);


        String incidentType = (String) incident.get("incidentType");
        String incidentSubType = (String) incident.get("incidentSubType");
        String key = buildIncidentKey(incidentType, incidentSubType);

        String existingBusinessService =null;
        if(incidentPriorityMap.containsKey(key)) {
             existingBusinessService = "Incident_" + incidentPriorityMap.get(key);
        }
        else{
             existingBusinessService = "Incident_Medium";
        }

        TenantServiceStateKey stateKey = new TenantServiceStateKey(tenantId, existingBusinessService, state);
        Duration stateSlaDuration = slaMap.getOrDefault(stateKey, Duration.ZERO);
        long stateSla = stateSlaDuration.toMillis();



        String priority;
        boolean needsOverride = existingBusinessService == null || !existingBusinessService.contains("_");
        String updatedBusinessService = null;

        if (needsOverride && incidentPriorityMap.containsKey(key)) {
            priority = incidentPriorityMap.get(key);
            updatedBusinessService = "Incident_" + capitalize(priority);
            currentProcessInstance.put(BUSINESS_SERVICE, updatedBusinessService);  // update ES doc in memory
        } else if (existingBusinessService != null && existingBusinessService.contains("_")) {
            priority = existingBusinessService.split("_")[1];
        } else {
            priority = "Medium";
            updatedBusinessService = "Incident_Medium";
            currentProcessInstance.put(BUSINESS_SERVICE, updatedBusinessService);
        }

        List<ProcessInstance> processInstances = workflowService.getAllProcessInstances(tenantId,IncidentId, requestInfo);
        List<String> previousStates = processInstances
                .stream()
                .map(p -> p.getState().getApplicationStatus())
                .collect(Collectors.toList());

        Duration totalSla = computeTotalSla(tenantId, priority, state, slaMap,previousStates);
        long totalSlaRemaining = totalSla.toMillis() - businessElapsedFromCreated;
        long slaRemaining = stateSla - businessElapsedFromModified;

        Object incidentIdObj = incident.get("incidentId");
        if (incidentIdObj == null) {
            log.warn("Incident ID is null for ticket: {}", ticket);
            return;
        }

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
                    updatedBusinessService
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

    private Duration computeTotalSla(String tenantId, String priority, String currentState, Map<TenantServiceStateKey, Duration> slaMap, List<String> previousStates) {
        String businessService = "Incident_" + priority;
        Duration total = Duration.ZERO;

        //calculating sla for all states till current state
        previousStates.add(currentState);
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
