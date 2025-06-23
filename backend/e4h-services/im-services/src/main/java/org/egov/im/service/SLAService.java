package org.egov.im.service;

import org.egov.im.web.models.IncidentRequest;
import org.egov.im.web.models.Priority;
import org.egov.im.web.models.workflow.State;
import org.egov.tracer.model.CustomException;
import com.jayway.jsonpath.JsonPath;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class SLAService {

    private static final String PENDINGFORASSIGNMENT = "PENDINGFORASSIGNMENT";
    private static final String PENDINGATVENDOR = "PENDINGATVENDOR";
    private static final String PENDING_ASSIGNMENT_PREFIX = "PENDING_ASSIGNMENT_";
    private static final String PENDING_RESOLUTION_PREFIX = "PENDING_RESOLUTION_";
    private static final String MDMS_SERVICEDEF_SEARCH = "$.MdmsRes['im-services'].ServiceDefs[?(@.serviceCode=='{SERVICEDEF}')]";

    private List<State> states;

    public long computeTotalSla(String currentState, List<State> states) {
        Map<String, Long> stateToSlaMap = new HashMap<>();
        for (State state : states) {
            String key = state.getApplicationStatus();
            if (key != null && state.getSla() != null) {
                stateToSlaMap.put(key, state.getSla());
            }
        }
        long totalSla = 0;
        if (PENDINGFORASSIGNMENT.equals(currentState) || PENDINGATVENDOR.equals(currentState)) {
            totalSla += stateToSlaMap.getOrDefault(PENDINGFORASSIGNMENT, 0L);
            totalSla += stateToSlaMap.getOrDefault(PENDINGATVENDOR, 0L);
        } else if (currentState.startsWith(PENDING_ASSIGNMENT_PREFIX)) {
            String suffix = currentState.replace(PENDING_ASSIGNMENT_PREFIX, "");
            String resolutionState = PENDING_RESOLUTION_PREFIX + suffix;
            totalSla += stateToSlaMap.getOrDefault(currentState, 0L);
            totalSla += stateToSlaMap.getOrDefault(resolutionState, 0L);
        } else if (currentState.startsWith(PENDING_RESOLUTION_PREFIX)) {
            String suffix = currentState.replace(PENDING_RESOLUTION_PREFIX, "");
            String assignmentState = PENDING_ASSIGNMENT_PREFIX + suffix;
            totalSla += stateToSlaMap.getOrDefault(currentState, 0L);
            totalSla += stateToSlaMap.getOrDefault(assignmentState, 0L);
        }
        return totalSla;
    }

    public Priority getPriorityFromMDMS(IncidentRequest request, Object mdmsData) {
        String serviceCode = request.getIncident().getIncidentSubType();
        String assetType = request.getIncident().getIncidentType();
        String jsonPath = MDMS_SERVICEDEF_SEARCH.replace("{SERVICEDEF}", serviceCode);
        List<Object> res;
        try {
            res = JsonPath.read(mdmsData, jsonPath);
        } catch (Exception e) {
            throw new CustomException("JSONPATH_ERROR", "Failed to parse mdms response");
        }
        if (CollectionUtils.isEmpty(res)) {
            throw new CustomException("INVALID_SERVICECODE", "The service code: " + serviceCode + " is not present in MDMS");
        }
        for (Object obj : res) {
            if (obj instanceof Map) {
                Map<String, Object> map = (Map<String, Object>) obj;
                String menuPath = String.valueOf(map.get("menuPath"));
                String mdmsServiceCode = String.valueOf(map.get("serviceCode"));
                if (assetType.equals(menuPath) && serviceCode.equals(mdmsServiceCode)) {
                    String priorityStr = String.valueOf(map.get("priority"));
                    return Priority.fromString(priorityStr);
                }
            }
        }
        throw new CustomException("PRIORITY_NOT_FOUND", "Priority not found for assetType: " + assetType + " and serviceCode: " + serviceCode);
    }
} 