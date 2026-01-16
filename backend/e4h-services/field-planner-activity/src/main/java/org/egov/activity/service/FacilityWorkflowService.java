package org.egov.activity.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.egov.activity.config.ActivityConfiguration;
import org.egov.activity.web.models.*;
import org.egov.common.contract.models.RequestInfoWrapper;
import org.egov.common.contract.request.RequestInfo;
import org.egov.tracer.model.CustomException;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
public class FacilityWorkflowService {

    @Qualifier("objectMapper")
    private final ObjectMapper mapper;

    private final ActivityConfiguration activityConfiguration;

    private final ServiceRequestRepository repository;

    public FacilityWorkflowService(
            @Qualifier("objectMapper") ObjectMapper mapper,
            ActivityConfiguration activityConfiguration, ServiceRequestRepository repository
    ) {
        this.mapper = mapper;
        this.activityConfiguration = activityConfiguration;
        this.repository = repository;
    }

    public ProcessInstance transitionWorkflow(ActivityFacility activityFacility, String action, List<Document> documents, RequestInfo requestInfo, String workflowComment) {
        log.trace("transitionWorkflow method invoked for activityFacilityId: {}, action: {}", activityFacility.getId(), action);
        log.info("Transitioning workflow for activity facility: {}, action: {}", activityFacility.getId(), action);
        ProcessInstance instance = ProcessInstance.builder()
                .businessId(activityFacility.getId())
                .tenantId(activityFacility.getTenantId())
                .moduleName(activityConfiguration.getModuleName())
                .businessService(activityConfiguration.getBusinessService())
                .action(action)
                .documents(documents)
                .comment(workflowComment)
                .build();

        ProcessInstanceRequest wfRequest = ProcessInstanceRequest.builder()
                .requestInfo(requestInfo)
                .processInstances(List.of(instance))
                .build();

        String url = activityConfiguration.getWfHost() + activityConfiguration.getWfTransitionPath();
        log.debug("Calling workflow service transition endpoint: {}", url);
        Object response = repository.fetchResult(new StringBuilder(url), wfRequest);

        ProcessInstanceResponse wfResponse = mapper.convertValue(response, ProcessInstanceResponse.class);
        if (wfResponse == null || wfResponse.getProcessInstances() == null || wfResponse.getProcessInstances().isEmpty()) {
            log.error("Empty response from workflow transition, activityFacilityId: {}, action: {}", activityFacility.getId(), action);
            throw new CustomException("WORKFLOW_ERROR", "Empty response from workflow transition");
        }
        String newState = wfResponse.getProcessInstances().get(0).getState() != null ? wfResponse.getProcessInstances().get(0).getState().getState() : "null";
        log.debug("Workflow transition successful, new state: {}", newState);
        return wfResponse.getProcessInstances().get(0);
    }


     public List<ProcessInstance> getProcessInstanceById( String businessId, String tenantId, RequestInfo requestInfo) {
        log.trace("getProcessInstanceById method invoked for businessId: {}, tenantId: {}", businessId, tenantId);
        log.debug("Fetching process instances for businessId: {}", businessId);
        String url = activityConfiguration.getWfHost() + activityConfiguration.getWfSearchPath()
            + "?tenantId=" + tenantId
            + "&businessIds=" + businessId
            + "&history=" + true;

        // Wrap RequestInfo in RequestInfoWrapper
        RequestInfoWrapper requestInfoWrapper = new RequestInfoWrapper();
        requestInfoWrapper.setRequestInfo(requestInfo);

        // POST with requestInfoWrapper as body, query params in URL
        Object response = repository.fetchResult(new StringBuilder(url), requestInfoWrapper);

        ProcessInstanceResponse wfResponse = mapper.convertValue(response, ProcessInstanceResponse.class);
        List<ProcessInstance> processInstances = (wfResponse.getProcessInstances() == null || wfResponse.getProcessInstances().isEmpty())
            ? null
            : wfResponse.getProcessInstances();
        int instanceCount = processInstances != null ? processInstances.size() : 0;
        log.debug("Retrieved {} process instances for businessId: {}", instanceCount, businessId);
        return processInstances;
    }
}
