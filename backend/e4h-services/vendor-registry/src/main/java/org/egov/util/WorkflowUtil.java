package org.egov.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.egov.common.contract.models.RequestInfoWrapper;
import org.egov.common.contract.models.Workflow;
import org.egov.common.contract.workflow.*;
import org.egov.config.Configuration;
import org.egov.repository.ServiceRequestRepository;
import lombok.extern.slf4j.Slf4j;
import org.egov.common.contract.request.RequestInfo;
import org.egov.common.contract.request.User;
import org.egov.tracer.model.CustomException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import com.fasterxml.jackson.databind.ObjectMapper;

@Service
@Slf4j
public class WorkflowUtil {

	private final ServiceRequestRepository repository;

	private final ObjectMapper mapper;

	private final Configuration configs;

	@Autowired
	public WorkflowUtil(ServiceRequestRepository repository, ObjectMapper mapper, Configuration configs) {
		this.repository = repository;
		this.mapper = mapper;
		this.configs = configs;
	}

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
		log.trace("WorkflowUtil::getBusinessService entry");
		log.debug("Fetching business service for code: {}, tenant: {}", businessServiceCode, tenantId);
		
		StringBuilder url = getSearchURLWithParams(tenantId, businessServiceCode);
		RequestInfoWrapper requestInfoWrapper = RequestInfoWrapper.builder().requestInfo(requestInfo).build();
		Object result = repository.fetchResult(url, requestInfoWrapper);
		BusinessServiceResponse response = null;
		try {
			response = mapper.convertValue(result, BusinessServiceResponse.class);
		} catch (IllegalArgumentException e) {
			log.error("Failed to parse workflow business service response", e);
			throw new CustomException("PARSING ERROR", "Failed to parse response of workflow business service search");
		}

		if (CollectionUtils.isEmpty(response.getBusinessServices())) {
			log.error("Business service not found: {} for tenant: {}", businessServiceCode, tenantId);
			throw new CustomException("BUSINESSSERVICE_NOT_FOUND",
					"The businessService " + businessServiceCode + " is not found");
		}

		log.debug("Successfully retrieved business service: {}", businessServiceCode);
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
	public String updateWorkflowStatus(RequestInfo requestInfo, String tenantId, String businessId,
									   String businessServiceCode, Workflow workflow, String wfModuleName) {
		log.trace("WorkflowUtil::updateWorkflowStatus entry");
		log.info("Updating workflow status for business ID: {}, service code: {}, tenant: {}", 
				businessId, businessServiceCode, tenantId);
		
		ProcessInstance processInstance = getProcessInstanceForWorkflow(requestInfo, tenantId, businessId,
				businessServiceCode, workflow, wfModuleName);
		ProcessInstanceRequest workflowRequest = new ProcessInstanceRequest(requestInfo,
				Collections.singletonList(processInstance));
		State state = callWorkFlow(workflowRequest);

		log.info("Workflow status updated to: {} for business ID: {}", state.getApplicationStatus(), businessId);
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
		log.trace("WorkflowUtil::getSearchURLWithParams entry");
		StringBuilder url = new StringBuilder(configs.getWfHost());
		url.append(configs.getWfBusinessServiceSearchPath());
		url.append("?tenantId=");
		url.append(tenantId);
		url.append("&businessServices=");
		url.append(businessService);
		log.debug("Constructed workflow search URL for tenant: {}, businessService: {}", tenantId, businessService);
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
	private ProcessInstance getProcessInstanceForWorkflow(RequestInfo requestInfo, String tenantId, String businessId,
			String businessServiceCode, Workflow workflow, String wfModuleName) {
		log.trace("WorkflowUtil::getProcessInstanceForWorkflow entry");
		log.debug("Creating process instance for business ID: {}, action: {}", businessId, workflow.getAction());
		
		ProcessInstance processInstance = new ProcessInstance();
		processInstance.setBusinessId(businessId);
		processInstance.setAction(workflow.getAction());
		processInstance.setModuleName(wfModuleName);
		processInstance.setTenantId(tenantId);
		processInstance.setBusinessService(
				getBusinessService(requestInfo, tenantId, businessServiceCode).getBusinessService());
		processInstance.setDocuments(workflow.getDocuments());
		processInstance.setComment(workflow.getComments());

		if (!CollectionUtils.isEmpty(workflow.getAssignes())) {
			List<User> users = new ArrayList<>();

			workflow.getAssignes().forEach(uuid -> {
				User user = new User();
				user.setUuid(uuid);
				users.add(user);
			});

			processInstance.setAssignes(users);
			log.debug("Assigned {} users to process instance", users.size());
		}

		log.debug("Process instance created successfully");
		return processInstance;
	}

	/**
	 * Gets the workflow corresponding to the processInstance
	 * 
	 * @param processInstances
	 * @return
	 */
	public Map<String, Workflow> getWorkflow(List<ProcessInstance> processInstances) {
		log.trace("WorkflowUtil::getWorkflow entry");
		log.debug("Converting {} process instances to workflow map", processInstances != null ? processInstances.size() : 0);

		Map<String, Workflow> businessIdToWorkflow = new HashMap<>();

		processInstances.forEach(processInstance -> {
			List<String> userIds = null;

			if (!CollectionUtils.isEmpty(processInstance.getAssignes())) {
				userIds = processInstance.getAssignes().stream().map(User::getUuid).collect(Collectors.toList());
			}

			Workflow workflow = Workflow.builder().action(processInstance.getAction()).assignes(userIds)
					.comments(processInstance.getComment()).documents(processInstance.getDocuments())
					.build();

			businessIdToWorkflow.put(processInstance.getBusinessId(), workflow);
		});

		log.debug("Created workflow map with {} entries", businessIdToWorkflow.size());
		return businessIdToWorkflow;
	}

	/**
	 * Method to take the ProcessInstanceRequest as parameter and set resultant
	 * status
	 * 
	 * @param workflowReq
	 * @return
	 */
	private State callWorkFlow(ProcessInstanceRequest workflowReq) {
		log.trace("WorkflowUtil::callWorkFlow entry");
		ProcessInstanceResponse response = null;
		StringBuilder url = new StringBuilder(configs.getWfHost().concat(configs.getWfTransitionPath()));
		log.debug("Calling workflow service for transition");
		Object optional = repository.fetchResult(url, workflowReq);
		response = mapper.convertValue(optional, ProcessInstanceResponse.class);
		State state = response.getProcessInstances().get(0).getState();
		log.debug("Workflow transition completed, new state: {}", state.getApplicationStatus());
		return state;
	}
}