package org.egov.project.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.egov.common.contract.models.Document;
import org.egov.common.contract.models.RequestInfoWrapper;
import org.egov.common.contract.request.RequestInfo;
import org.egov.common.contract.workflow.ProcessInstance;
import org.egov.common.contract.workflow.ProcessInstanceRequest;
import org.egov.common.contract.workflow.ProcessInstanceResponse;
import org.egov.common.models.project.Project;
import org.egov.project.config.ProjectConfiguration;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ProjectWorkflowService {

    @Qualifier("objectMapper")
    private final ObjectMapper mapper;

    private final ProjectConfiguration config;

    private final ServiceRequestRepository repository;

    public ProjectWorkflowService(
            @Qualifier("objectMapper") ObjectMapper mapper,
            ProjectConfiguration config,
            ServiceRequestRepository repository
    ) {
        this.mapper = mapper;
        this.config = config;
        this.repository = repository;
    }

    public ProcessInstance transitionWorkflow(Project project, String action, List<Document> documents, RequestInfo requestInfo, String workflowComment) {
        ProcessInstance instance = ProcessInstance.builder()
                .businessId(project.getId())
                .tenantId(project.getTenantId())
                .moduleName(config.getModuleName())
                .businessService(config.getBusinessService())
                .action(action)
                .documents(documents)
                .comment(workflowComment)
                .build();

        ProcessInstanceRequest wfRequest = ProcessInstanceRequest.builder()
                .requestInfo(requestInfo)
                .processInstances(List.of(instance))
                .build();

        String url = config.getWfHost() + config.getWfTransitionPath();
        Object response = repository.fetchResult(new StringBuilder(url), wfRequest);

        ProcessInstanceResponse wfResponse = mapper.convertValue(response, ProcessInstanceResponse.class);
        return wfResponse.getProcessInstances().get(0);
    }


     public ProcessInstance getProcessInstanceById( String businessId, String tenantId, RequestInfo requestInfo) {
        String url = config.getWfHost() + config.getWfSearchPath()
            + "?tenantId=" + tenantId
            + "&businessIds=" + businessId;

        // Wrap RequestInfo in RequestInfoWrapper
        RequestInfoWrapper requestInfoWrapper = new RequestInfoWrapper();
        requestInfoWrapper.setRequestInfo(requestInfo);

        // POST with requestInfoWrapper as body, query params in URL
        Object response = repository.fetchResult(new StringBuilder(url), requestInfoWrapper);

        ProcessInstanceResponse wfResponse = mapper.convertValue(response, ProcessInstanceResponse.class);
        return (wfResponse.getProcessInstances() == null || wfResponse.getProcessInstances().isEmpty())
            ? null
            : wfResponse.getProcessInstances().get(0);
    }
}
