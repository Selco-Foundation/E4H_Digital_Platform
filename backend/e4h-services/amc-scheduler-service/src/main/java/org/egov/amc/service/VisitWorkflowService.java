package org.egov.amc.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.egov.amc.config.AMCServiceConfiguration;
import org.egov.amc.web.models.*;
import org.egov.common.contract.models.RequestInfoWrapper;
import org.egov.common.contract.request.RequestInfo;
import org.egov.tracer.model.CustomException;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
public class VisitWorkflowService {

    @Qualifier("objectMapper")
    private final ObjectMapper mapper;

    private final AMCServiceConfiguration configuration;

    private final ServiceRequestRepository repository;

    public VisitWorkflowService(
            @Qualifier("objectMapper") ObjectMapper mapper,
            AMCServiceConfiguration configuration, ServiceRequestRepository repository
    ) {
        this.mapper = mapper;
        this.configuration = configuration;
        this.repository = repository;
    }

    public ProcessInstance transitionWorkflow(ScheduledVisit activityFacility, String action, List<Document> documents, RequestInfo requestInfo, String workflowComment) {
        log.trace("Entering transitionWorkflow method for visitId: {}, action: {}", activityFacility.getId(), action);
        log.info("Transitioning workflow for visitId: {}, action: {}", activityFacility.getId(), action);
        ProcessInstance instance = ProcessInstance.builder()
                .businessId(activityFacility.getId())
                .tenantId(activityFacility.getTenantId())
                .moduleName(configuration.getModuleName())
                .businessService(configuration.getBusinessService())
                .action(action)
                .documents(documents)
                .comment(workflowComment)
                .build();

        ProcessInstanceRequest wfRequest = ProcessInstanceRequest.builder()
                .requestInfo(requestInfo)
                .processInstances(List.of(instance))
                .build();

        String url = configuration.getWfHost() + configuration.getWfTransitionPath();
        log.debug("Calling workflow service at URL: {} for visitId: {}", url, activityFacility.getId());
        Object response = repository.fetchResult(new StringBuilder(url), wfRequest);

        ProcessInstanceResponse wfResponse = mapper.convertValue(response, ProcessInstanceResponse.class);
        if (wfResponse == null || wfResponse.getProcessInstances() == null || wfResponse.getProcessInstances().isEmpty()) {
            log.error("Empty response from workflow transition for visitId: {}, action: {}", activityFacility.getId(), action);
            throw new CustomException("WORKFLOW_ERROR", "Empty response from workflow transition");
        }
        ProcessInstance result = wfResponse.getProcessInstances().get(0);
        log.info("Workflow transition successful for visitId: {}, new state: {}", activityFacility.getId(), 
                result.getState() != null ? result.getState().getState() : "N/A");
        return result;
    }


     public List<ProcessInstance> getProcessInstanceById( String businessId, String tenantId, RequestInfo requestInfo) {
        log.trace("Entering getProcessInstanceById method for businessId: {}, tenantId: {}", businessId, tenantId);
        String url = configuration.getWfHost() + configuration.getWfSearchPath()
            + "?tenantId=" + tenantId
            + "&businessIds=" + businessId
            + "&history=" + true;

        // Wrap RequestInfo in RequestInfoWrapper
        RequestInfoWrapper requestInfoWrapper = new RequestInfoWrapper();
        requestInfoWrapper.setRequestInfo(requestInfo);

        // POST with requestInfoWrapper as body, query params in URL
        log.debug("Calling workflow search service at URL: {} for businessId: {}", url, businessId);
        Object response = repository.fetchResult(new StringBuilder(url), requestInfoWrapper);

        ProcessInstanceResponse wfResponse = mapper.convertValue(response, ProcessInstanceResponse.class);
        List<ProcessInstance> result = (wfResponse.getProcessInstances() == null || wfResponse.getProcessInstances().isEmpty())
            ? null
            : wfResponse.getProcessInstances();
        log.debug("Retrieved {} process instance(s) for businessId: {}", 
                result != null ? result.size() : 0, businessId);
        return result;
    }
}
