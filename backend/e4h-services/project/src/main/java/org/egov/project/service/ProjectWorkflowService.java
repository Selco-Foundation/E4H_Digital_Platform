package org.egov.project.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.egov.common.contract.models.Document;
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

    public ProcessInstance transitionWorkflow(Project project, String action, List<Document> documents, RequestInfo requestInfo) {
        ProcessInstance instance = ProcessInstance.builder()
                .businessId(project.getId())
                .tenantId(project.getTenantId())
                .moduleName("Workflow")
                .businessService("FACILITY_INSTALLATION")
                .action(action)
                .documents(documents)
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
}
