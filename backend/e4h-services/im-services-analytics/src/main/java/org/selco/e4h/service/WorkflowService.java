package org.selco.e4h.service;


import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.egov.common.contract.request.RequestInfo;
import org.egov.tracer.model.CustomException;
import org.selco.e4h.config.ConsumerConfiguration;
import org.selco.e4h.repository.ServiceRequestRepository;
import org.selco.e4h.util.IMConstants;
import org.selco.e4h.web.models.workflow.*;
import org.selco.e4h.web.models.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.CollectionUtils;
import java.util.*;

import static org.selco.e4h.util.IMConstants.*;

@org.springframework.stereotype.Service
@Slf4j
public class WorkflowService {

    private ConsumerConfiguration consumerConfiguration;

    private ServiceRequestRepository repository;

    private ObjectMapper mapper;




    @Getter
    private List<State> states;

    @Autowired
    public WorkflowService(ConsumerConfiguration consumerConfiguration,
                           ServiceRequestRepository repository,
                           ObjectMapper mapper) {
        this.consumerConfiguration = consumerConfiguration;
        this.repository = repository;
        this.mapper = mapper;

    }


    public List<ProcessInstance> getAllProcessInstances(String tenantId, String IncidentId, RequestInfo requestInfo){

        RequestInfoWrapper requestInfoWrapper = RequestInfoWrapper.builder().requestInfo(requestInfo).build();

        StringBuilder URL = getprocessInstanceSearchURL(tenantId, IncidentId);
        URL.append("&").append("history=true");

        Object result = repository.fetchResult(URL, requestInfoWrapper);
        ProcessInstanceResponse processInstanceResponse = null;
        try {
            processInstanceResponse = mapper.convertValue(result, ProcessInstanceResponse.class);
        } catch (IllegalArgumentException e) {
            throw new CustomException("PARSING ERROR", "Failed to parse response of workflow processInstance search");
        }
        if (processInstanceResponse == null || CollectionUtils.isEmpty(processInstanceResponse.getProcessInstances())) {
            return Collections.emptyList();
        }

        List<ProcessInstance> processInstances =  processInstanceResponse.getProcessInstances();

        // Get latest cycle of the ticket
        Collections.reverse(processInstances);
        int lastIndex = -1;
        for (int i = processInstances.size() - 1; i >= 0; i--) {
            if (PENDING_FOR_ASSIGNMENT.equals(processInstances.get(i).getState().getState())) {
                lastIndex = i;
                break;
            }
        }
        if (lastIndex != -1) {
            return processInstances.subList(lastIndex, processInstances.size());
        } else {
            return processInstances;
        }
    }


    public StringBuilder getprocessInstanceSearchURL(String tenantId, String IncidentId) {

        StringBuilder url = new StringBuilder(consumerConfiguration.getWfHost());
        url.append(consumerConfiguration.getWfProcessInstanceSearchPath());
        url.append("?tenantId=");
        url.append(tenantId);
        url.append("&businessIds=");
        url.append(IncidentId);
        return url;
    }
}

