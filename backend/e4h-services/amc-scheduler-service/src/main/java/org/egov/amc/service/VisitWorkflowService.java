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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Service
@Slf4j
public class VisitWorkflowService {

    /**
     * Page size asked of the workflow search. Matches egov-workflow-v2's own {@code egov.wf.max.limit}
     * default (100) - anything larger is silently clamped to it by WorkflowQueryBuilder.
     */
    private static final int HISTORY_PAGE_SIZE = 100;

    /** Backstop so a misbehaving search that never returns an empty page cannot loop forever. */
    private static final int MAX_HISTORY_PAGES = 50;

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

    /**
     * Fetch the complete transition history (history=true) of several visits in one shot.
     *
     * <p>egov-workflow-v2 always applies a LIMIT to this search - {@code egov.wf.default.limit} (10) when
     * none is sent, clamped to {@code egov.wf.max.limit} (100) otherwise - and with history=true it returns
     * one entry per transition rather than per visit, so a batch of visits overflows a single page easily.
     * This pages until the service returns nothing, advancing the offset by the number of entries actually
     * received instead of by the requested page size, so an environment configured with a smaller max limit
     * still yields the full history rather than a silently truncated one.
     */
    public List<ProcessInstance> getProcessInstanceHistory(List<String> businessIds, String tenantId, RequestInfo requestInfo) {
        if (businessIds == null || businessIds.isEmpty()) {
            return Collections.emptyList();
        }

        RequestInfoWrapper requestInfoWrapper = new RequestInfoWrapper();
        requestInfoWrapper.setRequestInfo(requestInfo);

        List<ProcessInstance> history = new ArrayList<>();
        int offset = 0;
        for (int page = 0; page < MAX_HISTORY_PAGES; page++) {
            String url = configuration.getWfHost() + configuration.getWfSearchPath()
                    + "?tenantId=" + tenantId
                    + "&businessIds=" + String.join(",", businessIds)
                    + "&history=" + true
                    + "&limit=" + HISTORY_PAGE_SIZE
                    + "&offset=" + offset;

            Object response = repository.fetchResult(new StringBuilder(url), requestInfoWrapper);
            ProcessInstanceResponse wfResponse = mapper.convertValue(response, ProcessInstanceResponse.class);
            List<ProcessInstance> pageInstances = (wfResponse == null) ? null : wfResponse.getProcessInstances();
            if (pageInstances == null || pageInstances.isEmpty()) {
                return history;
            }
            history.addAll(pageInstances);
            offset += pageInstances.size();
        }

        log.warn("Stopped reading workflow history after {} pages ({} transitions) for {} businessIds in tenantId={}. "
                        + "History may be incomplete.", MAX_HISTORY_PAGES, history.size(), businessIds.size(), tenantId);
        return history;
    }
}
