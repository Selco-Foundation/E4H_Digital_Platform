package org.selco.e4h.service;


import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.egov.common.contract.request.RequestInfo;
import org.egov.tracer.model.CustomException;
import org.selco.e4h.config.ConsumerConfiguration;
import org.selco.e4h.repository.ServiceRequestRepository;
import org.selco.e4h.web.models.workflow.*;
import org.selco.e4h.web.models.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.CollectionUtils;
import java.util.*;

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
        log.trace("Getting all process instances for tenantId: {}, incidentId: {}", tenantId, IncidentId);
        log.info("Fetching process instances for incident: {}", IncidentId);

        RequestInfoWrapper requestInfoWrapper = RequestInfoWrapper.builder().requestInfo(requestInfo).build();

        StringBuilder URL = getprocessInstanceSearchURL(tenantId, IncidentId);
        URL.append("&").append("history=true");
        log.debug("Process instance search URL: {}", URL);

        Object result = repository.fetchResult(URL, requestInfoWrapper);
        ProcessInstanceResponse processInstanceResponse = null;
        try {
            processInstanceResponse = mapper.convertValue(result, ProcessInstanceResponse.class);
            log.debug("Parsed process instance response");
        } catch (IllegalArgumentException e) {
            log.error("Failed to parse workflow processInstance search response", e);
            throw new CustomException("PARSING ERROR", "Failed to parse response of workflow processInstance search");
        }
        if (processInstanceResponse == null || CollectionUtils.isEmpty(processInstanceResponse.getProcessInstances())) {
            log.warn("No process instances found for incident: {}", IncidentId);
            return Collections.emptyList();
        }

        log.info("Retrieved {} process instances for incident: {}", 
            processInstanceResponse.getProcessInstances().size(), IncidentId);
        return processInstanceResponse.getProcessInstances();
    }


    public StringBuilder getprocessInstanceSearchURL(String tenantId, String IncidentId) {
        log.trace("Building process instance search URL for tenantId: {}, incidentId: {}", tenantId, IncidentId);
        StringBuilder url = new StringBuilder(consumerConfiguration.getWfHost());
        url.append(consumerConfiguration.getWfProcessInstanceSearchPath());
        url.append("?tenantId=");
        url.append(tenantId);
        url.append("&businessIds=");
        url.append(IncidentId);
        log.debug("Process instance search URL built: {}", url);
        return url;
    }
}

