package org.egov.asset.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import digit.models.coremodels.*;
import lombok.extern.slf4j.Slf4j;
import org.egov.asset.config.Configuration;
import org.egov.asset.repository.ServiceRequestRepository;
import org.egov.asset.web.models.Workflow;
import org.egov.common.contract.request.RequestInfo;
import org.egov.common.contract.request.User;
import org.egov.tracer.model.CustomException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.*;
import java.util.stream.Collectors;

import static org.egov.asset.config.ServiceConstants.*;

@Service
@Slf4j
public class WorkflowUtil {

    @Autowired
    private ServiceRequestRepository repository;

    @Autowired
    private ObjectMapper mapper;

    @Autowired
    private Configuration configs;


    /**
     * Searches the BussinessService corresponding to the businessServiceCode
     * Returns applicable BussinessService for the given parameters
     *
     * @param requestInfo
     * @param tenantId
     * @param businessServiceCode
     * @return
     */
    public BusinessService getBusinessService(RequestInfo requestInfo, String tenantId, String businessServiceCode) {
        log.info("WorkflowUtil::getBusinessService called | tenantId={} businessServiceCode={}", tenantId, businessServiceCode);
        if (requestInfo == null || tenantId == null || businessServiceCode == null) {
            throw new CustomException("INVALID_INPUT", "RequestInfo, tenantId, and businessServiceCode cannot be null");
        }

        StringBuilder url = getSearchURLWithParams(tenantId, businessServiceCode);
        RequestInfoWrapper requestInfoWrapper = RequestInfoWrapper.builder().requestInfo(requestInfo).build();
        Object result;
        try {
            log.debug("getBusinessService | fetching business service from WF service url={}", url);
            result = repository.fetchResult(url, requestInfoWrapper, String.class);
        } catch (Exception e) {
            throw new CustomException("WF_SERVICE_CALL_FAILED", "Failed to fetch business service: "+ e.getMessage());
        }

        BusinessServiceResponse response = null;
        try {
            response = mapper.convertValue(result, BusinessServiceResponse.class);
            log.debug("getBusinessService | response successfully parsed for businessServiceCode={}", businessServiceCode);
        } catch (IllegalArgumentException e) {
            throw new CustomException(PARSING_ERROR, FAILED_TO_PARSE_BUSINESS_SERVICE_SEARCH);
        } catch (Exception e) {
            throw new CustomException("BUSINESS_SERVICE_PROCESSING_ERROR", "Error processing business service: "+e.getMessage());
        }

        if (CollectionUtils.isEmpty(response.getBusinessServices()))
            throw new CustomException(BUSINESS_SERVICE_NOT_FOUND, THE_BUSINESS_SERVICE + businessServiceCode + NOT_FOUND);

        return response.getBusinessServices().get(0);
    }

    /**
     * Calls the workflow service with the given action and updates the status
     * Returns the updated status of the application
     *
     * @param requestInfo
     * @param tenantId
     * @param businessId
     * @param businessServiceCode
     * @param workflow
     * @param wfModuleName
     * @return
     */
    public String updateWorkflowStatus(RequestInfo requestInfo, String tenantId,
                                       String businessId, String businessServiceCode, Workflow workflow, String wfModuleName) {
        log.info("WorkflowUtil::updateWorkflowStatus called | tenantId={} businessId={} action={} wfModuleName={}",
                tenantId, businessId, workflow != null ? workflow.getAction() : "null", wfModuleName);
        ProcessInstance processInstance = getProcessInstanceForWorkflow(requestInfo, tenantId, businessId,
                businessServiceCode, workflow, wfModuleName);
        ProcessInstanceRequest workflowRequest = new ProcessInstanceRequest(requestInfo, Collections.singletonList(processInstance));
        State state = callWorkFlow(workflowRequest);

        log.info("updateWorkflowStatus | updated workflow status={} for businessId={}", state.getApplicationStatus(), businessId);
        return state.getApplicationStatus();
    }

    /**
     * Creates url for search based on given tenantId and businessServices
     *
     * @param tenantId
     * @param businessService
     * @return
     */
    private StringBuilder getSearchURLWithParams(String tenantId, String businessService) {
        log.debug("WorkflowUtil::getSearchURLWithParams | tenantId={} businessService={}", tenantId, businessService);
        StringBuilder url = new StringBuilder(configs.getWfHost());
        url.append(configs.getWfBusinessServiceSearchPath());
        url.append(TENANTID);
        url.append(tenantId);
        url.append(BUSINESS_SERVICES);
        url.append(businessService);
        return url;
    }

    /**
     * Enriches ProcessInstance Object for Workflow
     *
     * @param requestInfo
     * @param tenantId
     * @param businessId
     * @param businessServiceCode
     * @param workflow
     * @param wfModuleName
     * @return
     */
    private ProcessInstance getProcessInstanceForWorkflow(RequestInfo requestInfo, String tenantId,
                                                          String businessId, String businessServiceCode, Workflow workflow, String wfModuleName) {

        log.debug("WorkflowUtil::getProcessInstanceForWorkflow | tenantId={} businessId={} action={}",
                tenantId, businessId, workflow != null ? workflow.getAction() : "null");
        ProcessInstance processInstance = new ProcessInstance();
        processInstance.setBusinessId(businessId);
        processInstance.setAction(workflow.getAction());
        processInstance.setModuleName(wfModuleName);
        processInstance.setTenantId(tenantId);
        BusinessService businessService;
        try {
            businessService = getBusinessService(requestInfo, tenantId, businessServiceCode);
            processInstance.setBusinessService(businessService.getBusinessService());
        } catch (CustomException e) {
            throw e;
        } catch (Exception e) {
            throw new CustomException();
        }
        processInstance.setDocuments(workflow.getVerificationDocuments());
        processInstance.setComment(workflow.getComments());

        if (!CollectionUtils.isEmpty(workflow.getAssignees())) {
            List<User> users = new ArrayList<>();

            workflow.getAssignees().forEach(uuid -> {
                User user = new User();
                user.setUuid(uuid);
                users.add(user);
            });

            processInstance.setAssignes(users);
            log.debug("getProcessInstanceForWorkflow | assignees added count={}", users.size());
        }

        log.info("getProcessInstanceForWorkflow | processInstance created for businessId={} action={}",
                businessId, workflow.getAction());
        return processInstance;
    }

    /**
     * Gets the workflow corresponding to the processInstance
     *
     * @param processInstances
     * @return
     */
    public Map<String, Workflow> getWorkflow(List<ProcessInstance> processInstances) {
        log.debug("WorkflowUtil::getWorkflow");
        Map<String, Workflow> businessIdToWorkflow = new HashMap<>();

        processInstances.forEach(processInstance -> {
            List<String> userIds = new ArrayList<>();

            if (!CollectionUtils.isEmpty(processInstance.getAssignes())) {
                userIds = processInstance.getAssignes().stream().map(User::getUuid).collect(Collectors.toList());
            }

            Workflow workflow = Workflow.builder()
                    .action(processInstance.getAction())
                    .assignees(userIds)
                    .comments(processInstance.getComment())
                    .verificationDocuments(processInstance.getDocuments())
                    .build();
            log.debug("getWorkflow | mapped businessId={} with action={} assigneesCount={}",
                    processInstance.getBusinessId(), workflow.getAction(), userIds.size());
            businessIdToWorkflow.put(processInstance.getBusinessId(), workflow);
        });

        log.info("getWorkflow | returning workflow map size={}", businessIdToWorkflow.size());
        return businessIdToWorkflow;
    }

    /**
     * Method to take the ProcessInstanceRequest as parameter and set resultant status
     *
     * @param workflowReq
     * @return
     */
    private State callWorkFlow(ProcessInstanceRequest workflowReq) {
        ProcessInstanceResponse response = null;
        StringBuilder url = new StringBuilder(configs.getWfHost().concat(configs.getWfTransitionPath()));
        Object result;
        try {
            log.debug("callWorkFlow | invoking WF transition API url={}", url);
            result = repository.fetchResult(url, workflowReq, String.class);
        } catch (Exception e) {
            throw new CustomException();
        }

        try {
            response = mapper.convertValue(result, ProcessInstanceResponse.class);
            log.debug("callWorkFlow | response parsed successfully");
        } catch (Exception e) {
            throw new CustomException();
        }

        if (response == null || CollectionUtils.isEmpty(response.getProcessInstances())) {
            throw new CustomException("WORKFLOW_RESPONSE_ERROR", "No process instances found in workflow response");
        }

        return response.getProcessInstances().get(0).getState();
    }
}