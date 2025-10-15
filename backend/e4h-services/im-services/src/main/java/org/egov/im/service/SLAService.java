package org.egov.im.service;

import org.apache.kafka.common.protocol.types.Field;
import org.egov.im.repository.IMPriorityRepository;
import org.egov.im.web.models.IMPrioritySearchCriteria;
import org.egov.im.web.models.Incident;
import org.egov.im.web.models.IncidentRequest;
import org.egov.im.web.models.Priority;
import org.egov.im.web.models.workflow.ProcessInstance;
import org.egov.im.web.models.workflow.State;
import org.egov.tracer.model.CustomException;
import com.jayway.jsonpath.JsonPath;

import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.egov.im.util.IMConstants.*;

@Slf4j
@Service
public class SLAService {
    
    private  final IMPriorityRepository imPriorityRepository;
    
    @Autowired
    public SLAService(IMPriorityRepository imPriorityRepository){
        this.imPriorityRepository = imPriorityRepository;
    }
    
    public long computeTotalSla(String currentState, List<State> states, List<ProcessInstance> processInstances) {
        log.info("SLAService::computeTotalSla called | currentState={}", currentState);
        Map<String, Long> stateToSlaMap = new HashMap<>();
        for (State state : states) {
            String key = state.getApplicationStatus();
            if (key != null && state.getSla() != null) {
                stateToSlaMap.put(key, state.getSla());
            }
        }
        long totalSla = 0;
        //calculating sla for all states till current state
        List<String> previousStates = processInstances
                .stream()
                .map(p -> p.getState().getApplicationStatus())
                .collect(Collectors.toList());

        if(previousStates.isEmpty() || !previousStates.get(previousStates.size() - 1).equals(currentState)){
            previousStates.add(currentState);
        }
        for(String state : previousStates){
            if(PENDINGFORASSIGNMENT.equals(state) || PENDINGATVENDOR.equals(state)
                    || state.startsWith(PENDING_ASSIGNMENT_PREFIX) || (state.startsWith(PENDING_RESOLUTION_PREFIX))){
                totalSla += stateToSlaMap.getOrDefault(state, 0L);
            }
        }

        //add positive follow-up state
        if (PENDINGFORASSIGNMENT.equals(currentState)) {
            totalSla += stateToSlaMap.getOrDefault(PENDINGATVENDOR, 0L);
            log.debug("Computed SLA for combined state={} totalSla={}", currentState, totalSla);
        } else if (currentState.startsWith(PENDING_ASSIGNMENT_PREFIX)) {
            String suffix = currentState.replace(PENDING_ASSIGNMENT_PREFIX, "");
            String resolutionState = PENDING_RESOLUTION_PREFIX + suffix;
            totalSla += stateToSlaMap.getOrDefault(resolutionState, 0L);
            log.debug("Computed SLA for assignment workflow | currentState={} resolutionState={} totalSla={}",
                    currentState, resolutionState, totalSla);
        }
        return totalSla;
    }

    public Priority getPriorityFromMDMS(IncidentRequest request, Object mdmsData) {
        String serviceCode = request.getIncident().getIncidentSubType();
        String assetType = request.getIncident().getIncidentType();
        log.info("SLAService::getPriorityFromMDMS called | assetType={} serviceCode={}", assetType, serviceCode);
        String jsonPath = MDMS_SERVICEDEF_SEARCH.replace("{SERVICEDEF}", serviceCode);
        List<Object> res;
        try {
            res = JsonPath.read(mdmsData, jsonPath);
        } catch (Exception e) {
            throw new CustomException(
                "JSONPATH_ERROR",
                "Failed to parse MDMS response for service code: " + serviceCode + ". Error: " + e.getMessage()
            );
        }
        if (CollectionUtils.isEmpty(res)) {
            throw new CustomException(
                "INVALID_SERVICECODE",
                "The service code: " + serviceCode + " is not present in MDMS"
            );
        }
        for (Object obj : res) {
            try {
                if (obj instanceof Map) {
                    @SuppressWarnings("unchecked")
                    Map<String, Object> map = (Map<String, Object>) obj;
                    String menuPath = getStringValue(map, "menuPath");
                    String mdmsServiceCode = getStringValue(map, "serviceCode");
                    if (assetType.equals(menuPath) && serviceCode.equals(mdmsServiceCode)) {
                        String priorityStr = getStringValue(map, "priority");
                        return Priority.fromString(priorityStr);
                    }
                }
            } catch (Exception e) {
                throw new CustomException(
                    "MDMS_DATA_ERROR",
                    "Error processing MDMS data: " + e.getMessage()
                );
            }
        }
        // Log when default priority is used - could indicate missing MDMS configuration
        log.warn("No priority found in MDMS for assetType: {} and serviceCode: {}, using default priority: MEDIUM",
                 assetType, serviceCode);
        return Priority.MEDIUM;
    }

    private String getStringValue(Map<String, Object> map, String key) {
        Object value = map.get(key);
        return value != null ? String.valueOf(value) : null;
    }

    public Priority getPriorityFromIMPriorityTable(Incident incident) {
        String stateTenantId = incident.getTenantId().split("\\.")[0];
        IMPrioritySearchCriteria criteria = IMPrioritySearchCriteria.builder()
                .tenantId(stateTenantId)
                .incidentType(incident.getIncidentType())
                .incidentSubType(incident.getIncidentSubType())
                .systemFunctional(incident.getSystemFunctional())
                .build();
        return imPriorityRepository.getPriority(criteria);
    }
} 