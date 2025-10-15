package org.egov.activity.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.egov.activity.config.ActivityConfiguration;
import org.egov.activity.web.models.*;
import org.egov.common.contract.models.RequestInfoWrapper;
import org.egov.common.contract.request.RequestInfo;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
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
        Object response = repository.fetchResult(new StringBuilder(url), wfRequest);

        ProcessInstanceResponse wfResponse = mapper.convertValue(response, ProcessInstanceResponse.class);
        return wfResponse.getProcessInstances().get(0);
    }


     public List<ProcessInstance> getProcessInstanceById( String businessId, String tenantId, RequestInfo requestInfo) {
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
        return (wfResponse.getProcessInstances() == null || wfResponse.getProcessInstances().isEmpty())
            ? null
            : wfResponse.getProcessInstances();
    }
}
