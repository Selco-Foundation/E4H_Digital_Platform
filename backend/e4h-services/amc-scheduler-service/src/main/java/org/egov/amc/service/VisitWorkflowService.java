package org.egov.amc.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.egov.amc.config.AMCServiceConfiguration;
import org.egov.amc.web.models.*;
import org.egov.common.contract.models.RequestInfoWrapper;
import org.egov.common.contract.request.RequestInfo;
import org.egov.tracer.model.CustomException;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
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
        Object response = repository.fetchResult(new StringBuilder(url), wfRequest);

        ProcessInstanceResponse wfResponse = mapper.convertValue(response, ProcessInstanceResponse.class);
        if (wfResponse == null || wfResponse.getProcessInstances() == null || wfResponse.getProcessInstances().isEmpty()) {
            throw new CustomException("WORKFLOW_ERROR", "Empty response from workflow transition");
        }
        return wfResponse.getProcessInstances().get(0);
    }


     public List<ProcessInstance> getProcessInstanceById( String businessId, String tenantId, RequestInfo requestInfo) {
        String url = configuration.getWfHost() + configuration.getWfSearchPath()
            + "?tenantId=" + tenantId
            + "&businessIds=" + businessId
            + "&history=" + true;

        // Wrap RequestInfo in RequestInfoWrapper
        RequestInfoWrapper requestInfoWrapper = new RequestInfoWrapper();
        requestInfoWrapper.setRequestInfo(requestInfo);

        // POST with requestInfoWrapper as body, query params in URL
        Object response = repository.fetchResult(new StringBuilder(url), requestInfoWrapper);

        ProcessInstanceResponse wfResponse = mapper.convertValue(response, ProcessInstanceResponse.class);
        return (wfResponse.getProcessInstances() == null || wfResponse.getProcessInstances().isEmpty())
            ? null
            : wfResponse.getProcessInstances();
    }
}
