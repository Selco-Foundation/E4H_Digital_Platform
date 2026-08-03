package org.egov.field_planner.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.egov.common.contract.request.RequestInfo;
import org.egov.field_planner.config.FieldPlannerConfiguration;
import org.egov.field_planner.util.AssessmentConstants;
import org.egov.field_planner.web.models.ProcessInstance;
import org.egov.field_planner.web.models.ProcessInstanceRequest;
import org.egov.field_planner.web.models.ProcessInstanceResponse;
import org.egov.tracer.model.CustomException;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
public class AssessmentWorkflowService {

    private final ObjectMapper mapper;
    private final FieldPlannerConfiguration configuration;
    private final ServiceRequestRepository repository;

    public AssessmentWorkflowService(
            @Qualifier("objectMapper") ObjectMapper mapper,
            FieldPlannerConfiguration configuration,
            ServiceRequestRepository repository) {
        this.mapper = mapper;
        this.configuration = configuration;
        this.repository = repository;
    }

    public ProcessInstance transitionWorkflow(String businessId, String tenantId, String action,
                                                RequestInfo requestInfo, String comment) {
        log.info("Transitioning assessment workflow for businessId: {}, action: {}", businessId, action);
        ProcessInstance instance = ProcessInstance.builder()
                .businessId(businessId)
                .tenantId(tenantId)
                .moduleName(configuration.getModuleName())
                .businessService(configuration.getAssessmentBusinessService())
                .action(action)
                .comment(comment)
                .build();

        ProcessInstanceRequest wfRequest = ProcessInstanceRequest.builder()
                .requestInfo(requestInfo)
                .processInstances(List.of(instance))
                .build();

        String url = configuration.getWfHost() + configuration.getWfTransitionPath();
        Object response = repository.fetchResult(new StringBuilder(url), wfRequest);
        ProcessInstanceResponse wfResponse = mapper.convertValue(response, ProcessInstanceResponse.class);
        if (wfResponse == null || wfResponse.getProcessInstances() == null || wfResponse.getProcessInstances().isEmpty()) {
            throw new CustomException(AssessmentConstants.WORKFLOW_TRANSITION_FAILED,
                    "Empty response from workflow transition for action: " + action);
        }
        return wfResponse.getProcessInstances().get(0);
    }
}
