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
        log.trace("WorkflowUtil::getBusinessService called");
        log.info("Getting business service | tenantId={} businessServiceCode={}", tenantId, businessServiceCode);
        if (requestInfo == null || tenantId == null || businessServiceCode == null) {
            log.error("Invalid input parameters | tenantId={} businessServiceCode={}", tenantId, businessServiceCode);
            throw new CustomException("INVALID_INPUT", "RequestInfo, tenantId, and businessServiceCode cannot be null");
        }

        StringBuilder url = getSearchURLWithParams(tenantId, businessServiceCode);
        RequestInfoWrapper requestInfoWrapper = RequestInfoWrapper.builder().requestInfo(requestInfo).build();
        Object result;
        try {
            log.debug("Fetching business service from workflow service | url={}", url);
            result = repository.fetchResult(url, requestInfoWrapper, String.class);
        } catch (Exception e) {
            log.error("Error fetching business service | tenantId={} businessServiceCode={} error={}", 
                    tenantId, businessServiceCode, e.getMessage(), e);
            throw new CustomException("WF_SERVICE_CALL_FAILED", "Failed to fetch business service: "+ e.getMessage());
        }

        BusinessServiceResponse response = null;
        try {
            response = mapper.convertValue(result, BusinessServiceResponse.class);
            log.debug("Business service response parsed successfully | businessServiceCode={}", businessServiceCode);
        } catch (IllegalArgumentException e) {
            log.error("Error parsing business service response | businessServiceCode={} error={}", 
                    businessServiceCode, e.getMessage(), e);
            throw new CustomException(PARSING_ERROR, FAILED_TO_PARSE_BUSINESS_SERVICE_SEARCH);
        } catch (Exception e) {
            log.error("Error processing business service | businessServiceCode={} error={}", 
                    businessServiceCode, e.getMessage(), e);
            throw new CustomException("BUSINESS_SERVICE_PROCESSING_ERROR", "Error processing business service: "+e.getMessage());
        }

        if (CollectionUtils.isEmpty(response.getBusinessServices())) {
            log.error("Business service not found | tenantId={} businessServiceCode={}", tenantId, businessServiceCode);
            throw new CustomException(BUSINESS_SERVICE_NOT_FOUND, THE_BUSINESS_SERVICE + businessServiceCode + NOT_FOUND);
        }

        log.debug("Business service found | businessServiceCode={}", businessServiceCode);
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
        log.trace("WorkflowUtil::updateWorkflowStatus called");
        log.info("Updating workflow status | tenantId={} businessId={} action={} wfModuleName={}",
                tenantId, businessId, workflow.getAction(), wfModuleName);
        ProcessInstance processInstance = getProcessInstanceForWorkflow(requestInfo, tenantId, businessId,
                businessServiceCode, workflow, wfModuleName);
        ProcessInstanceRequest workflowRequest = new ProcessInstanceRequest(requestInfo, Collections.singletonList(processInstance));
        State state = callWorkFlow(workflowRequest);

        log.info("Workflow status updated successfully | businessId={} newStatus={}", businessId, state.getApplicationStatus());
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
        log.trace("WorkflowUtil::getSearchURLWithParams called");
        log.debug("Building search URL | tenantId={} businessService={}", tenantId, businessService);
        StringBuilder url = new StringBuilder(configs.getWfHost());
        url.append(configs.getWfBusinessServiceSearchPath());
        url.append(TENANTID);
        url.append(tenantId);
        url.append(BUSINESS_SERVICES);
        url.append(businessService);
        log.debug("Search URL built | url={}", url.toString());
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
        log.trace("WorkflowUtil::getProcessInstanceForWorkflow called");
        log.debug("Creating process instance | tenantId={} businessId={} action={}",
                tenantId, businessId, workflow.getAction());
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
            log.error("Error getting business service | tenantId={} businessServiceCode={} error={}", 
                    tenantId, businessServiceCode, e.getMessage(), e);
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
            log.debug("Assignees added to process instance | businessId={} assigneesCount={}", businessId, users.size());
        }

        log.info("Process instance created | businessId={} action={}", businessId, workflow.getAction());
        return processInstance;
    }

    /**
     * Gets the workflow corresponding to the processInstance
     *
     * @param processInstances
     * @return
     */
    public Map<String, Workflow> getWorkflow(List<ProcessInstance> processInstances) {
        log.trace("WorkflowUtil::getWorkflow called");
        log.debug("Mapping process instances to workflow | processInstancesCount={}", 
                processInstances.size());
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
            log.debug("Mapped workflow | businessId={} action={} assigneesCount={}",
                    processInstance.getBusinessId(), workflow.getAction(), userIds.size());
            businessIdToWorkflow.put(processInstance.getBusinessId(), workflow);
        });

        log.info("Workflow mapping completed | workflowsCount={}", businessIdToWorkflow.size());
        return businessIdToWorkflow;
    }

    /**
     * Method to take the ProcessInstanceRequest as parameter and set resultant status
     *
     * @param workflowReq
     * @return
     */
    private State callWorkFlow(ProcessInstanceRequest workflowReq) {
        log.trace("WorkflowUtil::callWorkFlow called");
        ProcessInstanceResponse response = null;
        StringBuilder url = new StringBuilder(configs.getWfHost().concat(configs.getWfTransitionPath()));
        Object result;
        try {
            log.debug("Invoking workflow transition API | url={}", url);
            result = repository.fetchResult(url, workflowReq, String.class);
        } catch (Exception e) {
            log.error("Error calling workflow transition API | url={} error={}", url, e.getMessage(), e);
            throw new CustomException();
        }

        try {
            response = mapper.convertValue(result, ProcessInstanceResponse.class);
            log.debug("Workflow response parsed successfully");
        } catch (Exception e) {
            log.error("Error parsing workflow response | error={}", e.getMessage(), e);
            throw new CustomException();
        }

        if (response == null || CollectionUtils.isEmpty(response.getProcessInstances())) {
            log.error("Workflow response is empty or null");
            throw new CustomException("WORKFLOW_RESPONSE_ERROR", "No process instances found in workflow response");
        }

        log.debug("Workflow transition completed | processInstancesCount={}", response.getProcessInstances().size());
        return response.getProcessInstances().get(0).getState();
    }
}