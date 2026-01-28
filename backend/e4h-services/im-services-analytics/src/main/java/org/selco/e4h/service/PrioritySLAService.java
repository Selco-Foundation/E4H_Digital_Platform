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
        log.trace("Computing and updating SLA, tenantId: {}, transform: {}, closedTickets: {}", 
            request != null ? request.getTenantId() : "null", transform, closedTickets);
        log.info("Starting SLA computation for tenant: {}, transform: {}", 
            request != null ? request.getTenantId() : "null", transform);
        int pageSize = 5000;
        int from = 0;
        boolean hasMore = true;
        RequestInfo requestInfo = request.getRequestInfo();
        Map<String, Map<String, JSONArray>> bhMdmsData = mdmsUtil.fetchMdmsData(
                request.getRequestInfo(), request.getTenantId(),
                IMConstants.MODULE_NAME_COMMON_MASTERS,
                Collections.singletonList(IMConstants.BUSINESS_HOUR_MASTER));
        log.debug("Fetched business hours MDMS data");

        BusinessHours bh = parseBusinessHours(bhMdmsData);
        Map<TenantServiceStateKey, Duration> slaMap = loadSLADurationsFromBusinessService();
        log.debug("Loaded {} SLA duration mappings from business service", slaMap.size());

        int totalProcessed = 0;
        while (hasMore) {
            List<Map<String, Object>> tickets = transform
                    ? esClient.fetchOldRequiredTicketsFromImServices(from, pageSize, closedTickets)
                    : esClient.fetchRequiredTickets(from, pageSize,closedTickets);
            log.debug("Fetched {} tickets from Elasticsearch, offset: {}", tickets.size(), from);

            if (tickets.isEmpty()) break;

            for (Map<String, Object> ticket : tickets) {
                try {
                    updateTicket(ticket, slaMap, bh, transform , requestInfo);
                    totalProcessed++;
                } catch (Exception e) {
                    log.error("Error processing ticket", e);
                    log.debug("Failed ticket data keys: {}", ticket != null ? ticket.keySet() : "null");
                }
            }

            hasMore = tickets.size() == pageSize;
            from += pageSize;
        }
        log.info("Completed SLA computation, processed {} tickets", totalProcessed);
    }


    private void updateTicket(Map<String, Object> ticket, Map<TenantServiceStateKey, Duration> slaMap,
                              BusinessHours bh, boolean transform, RequestInfo requestInfo) {
        log.trace("Updating ticket SLA, transform: {}", transform);
        Map<String, Object> data = (Map<String, Object>) ticket.get("Data");
        Map<String, Object> auditDetails = (Map<String, Object>) data.get("auditDetails");
        Map<String, Object> incident = (Map<String, Object>) data.get("incident");
        Map<String, Object> boundary = (Map<String, Object>) incident.get("boundary");
        Map<String, Object> currentProcessInstance = (Map<String, Object>) data.get("currentProcessInstance");

//        String tenantId = (String) data.get("tenantId");
        String tenantId = (String) boundary.get("stateCode");
        String IncidentId = (String) incident.get("incidentId");
        log.debug("Processing ticket: incidentId={}, tenantId={}", IncidentId, tenantId);

        //get process instances
        List<ProcessInstance> processInstances = workflowService.getAllProcessInstances(tenantId,IncidentId, requestInfo);
        Collections.reverse(processInstances);
        log.debug("Retrieved {} process instances for incident: {}", processInstances.size(), IncidentId);

        if (tenantId.contains(".")) tenantId = tenantId.split("\\.")[0];
        String state = (String) ((Map<String, Object>) currentProcessInstance.get(STATE)).get("applicationStatus");
        log.debug("Current state: {}", state);

        long lastModifiedTime = Long.parseLong(auditDetails.get("lastModifiedTime").toString());
        long now = Instant.now().toEpochMilli();

        long businessElapsedFromCreated = calculateBusinessDurationForAllStates(processInstances, bh);
        long businessElapsedFromModified = calculateBusinessMillis(lastModifiedTime, now, bh);
        log.debug("Business elapsed time - from created: {}ms, from modified: {}ms", 
            businessElapsedFromCreated, businessElapsedFromModified);

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
        log.debug("Business service: {}, priority: {}", businessService, priority);


        TenantServiceStateKey stateKey = new TenantServiceStateKey(tenantId, businessService, state);
        Duration stateSlaDuration = slaMap.getOrDefault(stateKey, Duration.ZERO);
        long stateSla = stateSlaDuration.toMillis();

        Duration totalSla = computeTotalSla(tenantId, priority, state, slaMap, processInstances);
        long definedTotalSla = totalSla.toMillis();

        long totalSlaRemaining = definedTotalSla - businessElapsedFromCreated;
        long slaRemaining = stateSla - businessElapsedFromModified;
        log.debug("SLA calculations - stateSla: {}ms, totalSla: {}ms, slaRemaining: {}ms, totalSlaRemaining: {}ms",
            stateSla, definedTotalSla, slaRemaining, totalSlaRemaining);

        Object incidentIdObj = incident.get("incidentId");
        if (incidentIdObj == null) {
            log.warn("Incident ID is null, skipping ticket update");
            return;
        }
        boolean isAClosedTicket = state.equals(CLOSED_AFTER_REJECTION) || state.equals(CLOSED_AFTER_RESOLUTION)
                || state.equals(REJECTED) || state.equals(RESOLVED);

        String incidentId = incidentIdObj.toString();
        if (transform) {
            Map<String, Object> fullDoc = buildDocumentToInsert(stateSla, slaRemaining, totalSlaRemaining, ticket);
            updateService.upsertTransformedTicket(incidentId, fullDoc);
        }
        else {
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
        log.debug("Updated SLA for incident: {}", incidentId);
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
        log.trace("Building document to insert with SLA values");
        Map<String, Object> data = (Map<String, Object>) oldTicket.get("Data");

        if (data == null || data.isEmpty()) {
            log.warn("Missing 'Data' field, using oldTicket directly");
            data = oldTicket;
        }

        data.put("stateSla", stateSla);
        data.put("slaRemaining", slaRemaining);
        data.put("totalSlaRemaining", totalSlaRemaining);
        log.debug("Document built with SLA fields: stateSla={}, slaRemaining={}, totalSlaRemaining={}", 
            stateSla, slaRemaining, totalSlaRemaining);

        return data;
    }


    private BusinessHours parseBusinessHours(Map<String, Map<String, JSONArray>> mdmsData) {
        log.trace("Parsing business hours from MDMS data");
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
            log.debug("Parsed business hours with {} schedule entries", scheduleMap.size());
        } catch (Exception e) {
            log.error("Error parsing business hours from MDMS", e);
        }
        return businessHours;
    }

    private long calculateBusinessMillis(long startMillis, long endMillis, BusinessHours businessHours) {
        log.trace("Calculating business milliseconds from {} to {}", startMillis, endMillis);
        if (startMillis > endMillis) {
            log.debug("Start time is after end time, returning 0");
            return 0;
        }
        if (businessHours == null || businessHours.getSchedule() == null) {
            log.debug("No business hours configured, returning raw time difference: {}ms", endMillis - startMillis);
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
        log.debug("Calculated business milliseconds: {}ms", total);
        return total;
    }

    public long calculateBusinessDurationForAllStates(List<ProcessInstance> processInstances, BusinessHours businessHours) {
        log.trace("Calculating business duration for all states, processInstances count: {}", 
            processInstances != null ? processInstances.size() : 0);
        if (processInstances == null || processInstances.isEmpty()) {
            log.debug("No process instances provided, returning 0");
            return 0;
        }
        long totalBusinessDuration = 0;

        for (int i = 0; i < processInstances.size(); i++) {
            ProcessInstance current = processInstances.get(i);
            String state = current.getState().getApplicationStatus();

            if (PENDING_RESOLUTION.equals(state) || PENDING_FOR_ASSIGNMENT.equals(state)
                    || state.startsWith(PENDING_ASSIGNMENT_PREFIX) || state.startsWith(PENDING_RESOLUTION_PREFIX)) {

                long prevStateTime = current.getAuditDetails().getCreatedTime();
                long nextStateTime = (i + 1) < processInstances.size() ? processInstances.get(i + 1).getAuditDetails().getCreatedTime()
                        : Instant.now().toEpochMilli();

                long stateDuration = calculateBusinessMillis(prevStateTime, nextStateTime, businessHours);
                totalBusinessDuration += stateDuration;
                log.debug("State: {}, duration: {}ms", state, stateDuration);
            }
        }

        log.debug("Total business duration for all states: {}ms", totalBusinessDuration);
        return totalBusinessDuration;
    }

    public record TenantServiceStateKey(String tenantId, String businessService, String state) {}

    private Map<TenantServiceStateKey, Duration> loadSLADurationsFromBusinessService() {
        log.trace("Loading SLA durations from business service database");
        try {
            String sql = """
                SELECT s.tenantid, b.businessservice, s.applicationstatus, s.sla
                FROM eg_wf_state_v2 s
                JOIN eg_wf_businessservice_v2 b ON s.businessserviceid = b.uuid
                WHERE s.sla IS NOT NULL
            """;
            log.debug("Executing SQL query to load SLA durations");

            Map<TenantServiceStateKey, Duration> result = jdbcTemplate.query(sql, rs -> {
                Map<TenantServiceStateKey, Duration> slaMap = new HashMap<>();
                while (rs.next()) {
                    String tenantId = rs.getString("tenantid");
                    String businessService = rs.getString("businessservice");
                    String state = rs.getString("applicationstatus");
                    long slaMillis = rs.getLong("sla");

                    TenantServiceStateKey key = new TenantServiceStateKey(tenantId, businessService, state);
                    slaMap.put(key, Duration.ofMillis(slaMillis));
                }
                return slaMap;
            });
            log.info("Loaded {} SLA duration mappings from database", result.size());
            return result;
        } catch (Exception e) {
            log.error("Failed to load SLA durations from database", e);
            return Collections.emptyMap();
        }
    }

    private Duration computeTotalSla(String tenantId, String priority, String currentState, Map<TenantServiceStateKey, Duration> slaMap, List<ProcessInstance> processInstances) {
        log.trace("Computing total SLA for tenantId: {}, priority: {}, currentState: {}", tenantId, priority, currentState);
        String businessService = INCIDENT_UNDERSCORE + capitalize(priority);
        Duration total = Duration.ZERO;
        log.debug("Business service: {}", businessService);

        //calculating sla for all states till current state
        List<String> previousStates = processInstances
                .stream()
                .map(p -> p.getState().getApplicationStatus())
                .collect(Collectors.toList());
        log.debug("Previous states count: {}", previousStates.size());

        for(String state : previousStates){
            if(PENDING_FOR_ASSIGNMENT.equals(state) || PENDING_RESOLUTION.equals(state)
                    || state.startsWith(PENDING_ASSIGNMENT_PREFIX) || (state.startsWith(PENDING_RESOLUTION_PREFIX))){
                Duration stateDuration = getDurationFromMap(slaMap, tenantId, businessService, state);
                total = total.plus(stateDuration);
                log.debug("Added SLA duration for state {}: {}ms", state, stateDuration.toMillis());
            }
        }
        if (currentState.equals(PENDING_FOR_ASSIGNMENT)) {
            Duration resolutionDuration = getDurationFromMap(slaMap, tenantId, businessService, PENDING_RESOLUTION);
            total = total.plus(resolutionDuration);
            log.debug("Added resolution SLA duration: {}ms", resolutionDuration.toMillis());
        } else if (currentState.startsWith(PENDING_ASSIGNMENT_PREFIX)) {
            String suffix = currentState.replace(PENDING_ASSIGNMENT_PREFIX, "");
            String resolutionState = PENDING_RESOLUTION_PREFIX + suffix;
            Duration resolutionDuration = getDurationFromMap(slaMap, tenantId, businessService, resolutionState);
            total = total.plus(resolutionDuration);
            log.debug("Added resolution SLA duration for state {}: {}ms", resolutionState, resolutionDuration.toMillis());
        }
        log.debug("Total SLA computed: {}ms", total.toMillis());
        return total;
    }

    private Duration getDurationFromMap(Map<TenantServiceStateKey, Duration> map, String tenantId, String service, String state) {
        log.trace("Getting duration from map for tenantId: {}, service: {}, state: {}", tenantId, service, state);
        Duration duration = map.getOrDefault(new TenantServiceStateKey(tenantId, service, state), Duration.ZERO);
        log.debug("Retrieved duration: {}ms", duration.toMillis());
        return duration;
    }

    private String capitalize(String value) {
        log.trace("Capitalizing string: {}", value);
        if (value == null || value.isEmpty()) return value;
        String capitalized = value.substring(0, 1).toUpperCase() + value.substring(1).toLowerCase();
        log.debug("Capitalized: {} -> {}", value, capitalized);
        return capitalized;
    }
}
