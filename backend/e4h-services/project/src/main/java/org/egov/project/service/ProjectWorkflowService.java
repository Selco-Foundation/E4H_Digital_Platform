package org.egov.project.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.egov.common.contract.models.RequestInfoWrapper;
import org.egov.common.contract.request.RequestInfo;
import org.egov.common.models.project.Project;
import org.egov.project.config.ProjectConfiguration;
import org.egov.project.web.models.Document;
import org.egov.project.web.models.ProcessInstance;
import org.egov.project.web.models.ProcessInstanceRequest;
import org.egov.project.web.models.ProcessInstanceResponse;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

@Service
@Slf4j
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
        log.trace("Entering transitionWorkflow for project: {}, action: {}", project.getId(), action);
        log.info("Transitioning workflow for project: {} with action: {}", project.getId(), action);
        ProcessInstance instance = ProcessInstance.builder()
                .businessId(project.getId())
                .tenantId(project.getTenantId())
                .moduleName(config.getModuleName())
                .businessService(config.getBusinessService())
                .action(action)
                .documents(documents)
                .comment(workflowComment)
                .build();
        log.debug("Created process instance for workflow transition");

        ProcessInstanceRequest wfRequest = ProcessInstanceRequest.builder()
                .requestInfo(requestInfo)
                .processInstances(List.of(instance))
                .build();

        String url = config.getWfHost() + config.getWfTransitionPath();
        log.debug("Calling workflow service at: {}", url);
        Object response = repository.fetchResult(new StringBuilder(url), wfRequest);

        ProcessInstanceResponse wfResponse = mapper.convertValue(response, ProcessInstanceResponse.class);
        log.info("Workflow transition completed successfully for project: {}", project.getId());
        log.trace("Exiting transitionWorkflow");
        return wfResponse.getProcessInstances().get(0);
    }


     public List<ProcessInstance> getProcessInstanceById( String businessId, String tenantId, RequestInfo requestInfo) {
        log.trace("Entering getProcessInstanceById for businessId: {}, tenantId: {}", businessId, tenantId);
        log.info("Fetching process instances for businessId: {}", businessId);
        String url = config.getWfHost() + config.getWfSearchPath()
            + "?tenantId=" + tenantId
            + "&businessIds=" + businessId
            + "&history=" + true;
        log.debug("Calling workflow search service at: {}", url);

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
