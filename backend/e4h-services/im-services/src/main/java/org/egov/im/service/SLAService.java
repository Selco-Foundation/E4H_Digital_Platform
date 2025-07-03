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

import static org.egov.im.util.IMConstants.*;

@Service
public class SLAService {

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
        return Priority.MEDIUM;
    }

    private String getStringValue(Map<String, Object> map, String key) {
        Object value = map.get(key);
        return value != null ? String.valueOf(value) : null;
    }
} 