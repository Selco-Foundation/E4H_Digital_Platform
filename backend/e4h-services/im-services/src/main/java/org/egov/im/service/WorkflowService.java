package org.egov.im.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.jsonpath.JsonPath;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.egov.common.contract.request.RequestInfo;
import org.egov.common.contract.request.User;
import org.egov.im.config.IMConfiguration;
import org.egov.im.repository.ServiceRequestRepository;
import org.egov.im.util.BusinessHoursUtil;
import org.egov.im.util.MDMSUtils;
import org.egov.im.web.models.*;
import org.egov.im.web.models.workflow.*;
import org.egov.tracer.model.CustomException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.CollectionUtils;

import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.*;
import java.util.stream.Collectors;

import static org.egov.im.util.IMConstants.*;

@org.springframework.stereotype.Service
@Slf4j
public class WorkflowService {

    private IMConfiguration imConfiguration;

    private ServiceRequestRepository repository;

    private ObjectMapper mapper;

    private NotificationService notificationService;
    private MDMSUtils mdmsUtils;

    private SLAService slaService;

    private static final Map<Priority, String> PRIORITY_BUSINESS_SERVICE_MAP = Map.of(
            Priority.HIGH, IM_BUSINESSSERVICE_HIGH,
            Priority.MEDIUM, IM_BUSINESSSERVICE_MEDIUM,
            Priority.LOW, IM_BUSINESSSERVICE_LOW
    );

    @Getter
    private List<State> states;

    @Autowired
    public WorkflowService(IMConfiguration imConfiguration,
                           ServiceRequestRepository repository,
                           ObjectMapper mapper, NotificationService notificationService, MDMSUtils mdmsUtils, SLAService slaService) {
        this.imConfiguration = imConfiguration;
        this.repository = repository;
        this.mapper = mapper;
        this.notificationService = notificationService;
        this.mdmsUtils = mdmsUtils;
        this.slaService = slaService;
    }

    /*
     *
     * Should return the applicable BusinessService for the given request
     *
     * */
    public BusinessService getBusinessService(IncidentRequest incidentRequest, Priority priority) {
        log.trace("WorkflowService::getBusinessService method invoked");
        String tenantId = incidentRequest.getIncident().getTenantId();
        String businessService = PRIORITY_BUSINESS_SERVICE_MAP.getOrDefault(priority, IM_BUSINESSSERVICE);
        log.info("Fetching business service for tenant: {}, priority: {}, businessService: {}",
                tenantId, priority, businessService);
        log.trace("Building search URL and fetching business service");
        StringBuilder url = getSearchURLWithParams(tenantId, businessService);
        RequestInfoWrapper requestInfoWrapper
                = RequestInfoWrapper.builder().requestInfo(incidentRequest.getRequestInfo()).build();
        Object result = repository.fetchResult(url, requestInfoWrapper);
        BusinessServiceResponse response = null;
        try {
            response = mapper.convertValue(result, BusinessServiceResponse.class);
        } catch (IllegalArgumentException e) {
            log.error("Failed to parse business service response", e);
            throw new CustomException("PARSING ERROR", "Failed to parse response of workflow business service search");
        }

        if (CollectionUtils.isEmpty(response.getBusinessServices())) {
            log.error("Business service not found for tenant: {}, businessService: {}", tenantId, IM_BUSINESSSERVICE);
            throw new CustomException("BUSINESSSERVICE_NOT_FOUND", "The businessService " + IM_BUSINESSSERVICE + " is not found");
        }

        log.debug("Business service fetched successfully");
        return response.getBusinessServices().get(0);
    }


    /*
     * Call the workflow service with the given action and update the status
     * return the updated status of the application
     *
     * */
    public ProcessInstance updateWorkflowStatus(IncidentRequestWrapper wrapper, Object mdmsData) {
        log.trace("WorkflowService::updateWorkflowStatus method invoked");
        IncidentRequest incidentRequest = wrapper.getIncidentRequest();
        log.trace("Fetching priority from IM priority table");
        Priority priority = slaService.getPriorityFromIMPriorityTable(incidentRequest.getIncident());
        log.trace("Creating process instance for workflow");
        ProcessInstance processInstance = getProcessInstanceForIM(incidentRequest, priority);
        log.info("Updating workflow status for incident: {}, tenant: {}",
                incidentRequest.getIncident().getIncidentId(), incidentRequest.getIncident().getTenantId());
        ProcessInstanceRequest workflowRequest = new ProcessInstanceRequest(incidentRequest.getRequestInfo(), Collections.singletonList(processInstance));
        log.debug("Calling workflow transition for incident: {} with action: {}",
                incidentRequest.getIncident().getIncidentId(), incidentRequest.getWorkflow().getAction());
        ProcessInstance updatedProcessInstance = callWorkFlow(workflowRequest);
        String newStatus = updatedProcessInstance.getState().getApplicationStatus();
        incidentRequest.getIncident().setApplicationStatus(newStatus);
        log.info("Workflow status updated for incident: {}. New status: {}", incidentRequest.getIncident().getIncidentId(), newStatus);
        log.trace("Enriching total SLA");
        enrichTotalSla(wrapper, updatedProcessInstance);
        return updatedProcessInstance;
    }

    private void enrichTotalSla(IncidentRequestWrapper wrapper, ProcessInstance processInstance) {
        log.trace("WorkflowService::enrichTotalSla method invoked");
        IncidentRequest request = wrapper.getIncidentRequest();
        log.debug("Enriching SLA for incident: {}", request.getIncident().getIncidentId());
        String applicationStatus = request.getIncident().getApplicationStatus();
        RequestInfo requestInfo= request.getRequestInfo();
        String tenantId = request.getIncident().getTenantId();
        String IncidentId = request.getIncident().getIncidentId();

        // Step 1: Fetch MDMS BusinessHours data
        log.trace("Fetching BusinessHours from MDMS");
        Object mdmsData = mdmsUtils.fetchMDMSData(
                request.getRequestInfo(),
                request.getIncident().getTenantId(),
                "common-masters",
                List.of("BusinessHours"),
                null
        );

        // Step 2: Parse BusinessHours config
        List<Map<String, Object>> businessHourList;
        try {
            businessHourList = JsonPath.read(
                    mdmsData,
                    "$.MdmsRes['common-masters'].BusinessHours[0].BusinessHours"
            );
        } catch (Exception e) {
            log.error("Failed to parse BusinessHours from MDMS", e);
            throw new CustomException("MDMS_PARSE_ERROR", "Unable to parse BusinessHours from MDMS");
        }

        if (businessHourList == null || businessHourList.isEmpty()) {
            log.error("BusinessHours config missing from MDMS for tenant: {}", tenantId);
            throw new CustomException("MDMS_MISSING", "BusinessHours config missing from MDMS");
        }

        //get all process instances
        log.trace("Fetching all process instances for SLA calculation");
        List<ProcessInstance> processInstances = getAllProcessInstances(tenantId,IncidentId, requestInfo);
        Collections.reverse(processInstances);

        // Step 3: Use BusinessHoursUtil
        log.trace("Calculating business hours elapsed and total SLA");
        BusinessHoursUtil util = new BusinessHoursUtil(businessHourList);
        long businessHoursElapsed = util.calculateBusinessDurationForAllStates(processInstances);
        long definedTotalSla = slaService.computeTotalSla(applicationStatus, this.getStates(), processInstances);
        long totalSlaRemaining = definedTotalSla - businessHoursElapsed;
        log.debug("SLA calculation completed: definedTotalSla={}, businessHoursElapsed={}, totalSlaRemaining={}", 
                definedTotalSla, businessHoursElapsed, totalSlaRemaining);

        wrapper.getIndexView().setDefinedTotalSla(definedTotalSla);
        processInstance.getState().setTotalSlaRemaining(totalSlaRemaining);
    }

    /**
     * Creates url for search based on given tenantId and businessservices
     *
     * @param tenantId        The tenantId for which url is generated
     * @param businessService The businessService for which url is generated
     * @return The search url
     */
    private StringBuilder getSearchURLWithParams(String tenantId, String businessService) {

        StringBuilder url = new StringBuilder(imConfiguration.getWfHost());
        url.append(imConfiguration.getWfBusinessServiceSearchPath());
        url.append("?tenantId=");
        url.append(tenantId);
        url.append("&businessServices=");
        url.append(businessService);
        return url;
    }


    public List<IncidentWrapper> enrichWorkflow(RequestInfo requestInfo, List<IncidentWrapper> incidentWrappers) {
        log.trace("WorkflowService::enrichWorkflow method invoked");
        log.info("Enriching workflow for {} incident wrappers", incidentWrappers.size());

        // FIX ME FOR BULK SEARCH
        log.trace("Grouping incident wrappers by tenantId");
        Map<String, List<IncidentWrapper>> tenantIdToServiceWrapperMap = getTenantIdToServiceWrapperMap(incidentWrappers);

        List<IncidentWrapper> enrichedServiceWrappers = new ArrayList<>();

        for (String tenantId : tenantIdToServiceWrapperMap.keySet()) {

            List<String> serviceRequestIds = new ArrayList<>();

            List<IncidentWrapper> tenantSpecificWrappers = tenantIdToServiceWrapperMap.get(tenantId);

            tenantSpecificWrappers.forEach(pgrEntity -> {
                serviceRequestIds.add(pgrEntity.getIncident().getIncidentId());
            });

            RequestInfoWrapper requestInfoWrapper = RequestInfoWrapper.builder().requestInfo(requestInfo).build();

            log.trace("Fetching process instances for tenant: {} with {} incident IDs", tenantId, serviceRequestIds.size());
            StringBuilder searchUrl = getprocessInstanceSearchURL(tenantId, StringUtils.join(serviceRequestIds, ','));
            Object result = repository.fetchResult(searchUrl, requestInfoWrapper);


            ProcessInstanceResponse processInstanceResponse = null;
            try {
                processInstanceResponse = mapper.convertValue(result, ProcessInstanceResponse.class);
            } catch (IllegalArgumentException e) {
                log.error("Failed to parse process instance response", e);
                throw new CustomException("PARSING ERROR", "Failed to parse response of workflow processInstance search");
            }

            if (CollectionUtils.isEmpty(processInstanceResponse.getProcessInstances()) || processInstanceResponse.getProcessInstances().size() != serviceRequestIds.size())
                throw new CustomException("WORKFLOW_NOT_FOUND", "The workflow object is not found");

            Map<String, Workflow> businessIdToWorkflow = getWorkflow(processInstanceResponse.getProcessInstances());

            tenantSpecificWrappers.forEach(pgrEntity -> {
                pgrEntity.setWorkflow(businessIdToWorkflow.get(pgrEntity.getIncident().getIncidentId()));
            });

            enrichedServiceWrappers.addAll(tenantSpecificWrappers);
        }

        return enrichedServiceWrappers;

    }

    private Map<String, List<IncidentWrapper>> getTenantIdToServiceWrapperMap(List<IncidentWrapper> incidentWrappers) {
        Map<String, List<IncidentWrapper>> resultMap = new HashMap<>();
        for (IncidentWrapper incidentWrapper : incidentWrappers) {
            if (resultMap.containsKey(incidentWrapper.getIncident().getTenantId())) {
                resultMap.get(incidentWrapper.getIncident().getTenantId()).add(incidentWrapper);
            } else {
                List<IncidentWrapper> incidentWrapperList = new ArrayList<>();
                incidentWrapperList.add(incidentWrapper);
                resultMap.put(incidentWrapper.getIncident().getTenantId(), incidentWrapperList);
            }
        }
        return resultMap;
    }

    /**
     * Enriches ProcessInstance Object for workflow
     *
     * @param request
     */
    private ProcessInstance getProcessInstanceForIM(IncidentRequest request, Priority priority) {
        log.trace("WorkflowService::getProcessInstanceForIM method invoked");
        Incident incident = request.getIncident();
        Workflow workflow = request.getWorkflow();
        String action = request.getWorkflow().getAction();
        log.debug("Creating process instance for incident: {} with action: {}", incident.getIncidentId(), action);
        if (action.equalsIgnoreCase("RESOLVE") || action.equalsIgnoreCase("REJECT")) {
            reassignWorkflow(workflow, request, "COMPLAINANT");
        } else if (action.equalsIgnoreCase("OUT_OF_WARRANTY")) {
            reassignWorkflow(workflow, request, "COMPLAINT_FACILITATOR_1");
        }
        ProcessInstance processInstance = new ProcessInstance();
        processInstance.setBusinessId(incident.getIncidentId());
        processInstance.setAction(request.getWorkflow().getAction());
        processInstance.setModuleName(IM_MODULENAME);
        processInstance.setTenantId(incident.getTenantId());
        BusinessService businessService = getBusinessService(request, priority);
        this.states = businessService.getStates();
        processInstance.setBusinessService(businessService.getBusinessService());
        processInstance.setDocuments(request.getWorkflow().getVerificationDocuments());
        processInstance.setComment(workflow.getComments());

        if(request.getWorkflow().getAction().equalsIgnoreCase("RATE")) {
            processInstance.setRating(workflow.getRating());
        }

        if (!CollectionUtils.isEmpty(workflow.getAssignes())) {
            List<User> users = new ArrayList<>();

            workflow.getAssignes().forEach(uuid -> {
                User user = new User();
                user.setUuid(uuid);
                users.add(user);
            });

            processInstance.setAssignes(users);
        }

        return processInstance;
    }

    private void reassignWorkflow(Workflow workflow, IncidentRequest request, String role) {
        log.trace("WorkflowService::reassignWorkflow method invoked for role: {}", role);
        workflow.setAssignes(null);
        log.debug("Fetching employee details for role: {}", role);
        Map<String, String> reassigneeDetails = notificationService.getHRMSEmployee(request, role);
        List<String> assignee = Arrays.asList(reassigneeDetails.get("employeeUUID"));
        workflow.setAssignes(assignee);
        log.debug("Workflow reassigned to employee with UUID: {}", reassigneeDetails.get("employeeUUID"));
    }

    /**
     * @param processInstances
     */
    public Map<String, Workflow> getWorkflow(List<ProcessInstance> processInstances) {

        Map<String, Workflow> businessIdToWorkflow = new HashMap<>();

        processInstances.forEach(processInstance -> {
            List<String> userIds = null;

            if (!CollectionUtils.isEmpty(processInstance.getAssignes())) {
                userIds = processInstance.getAssignes().stream().map(User::getUuid).collect(Collectors.toList());
            }

            Workflow workflow = Workflow.builder()
                    .action(processInstance.getAction())
                    .assignes(userIds)
                    .comments(processInstance.getComment())
                    .rating(processInstance.getRating())
                    .verificationDocuments(processInstance.getDocuments())
                    .build();

            businessIdToWorkflow.put(processInstance.getBusinessId(), workflow);
        });

        return businessIdToWorkflow;
    }

    private List<ProcessInstance> getAllProcessInstances(String tenantId, String IncidentId, RequestInfo requestInfo){
        log.trace("WorkflowService::getAllProcessInstances method invoked");
        RequestInfoWrapper requestInfoWrapper = RequestInfoWrapper.builder().requestInfo(requestInfo).build();

        StringBuilder URL = getprocessInstanceSearchURL(tenantId, IncidentId);
        URL.append("&").append("history=true");

        log.trace("Fetching process instance history");
        Object result = repository.fetchResult(URL, requestInfoWrapper);
        ProcessInstanceResponse processInstanceResponse = null;
        try {
            processInstanceResponse = mapper.convertValue(result, ProcessInstanceResponse.class);
        } catch (IllegalArgumentException e) {
            log.error("Failed to parse process instance history response", e);
            throw new CustomException("PARSING ERROR", "Failed to parse response of workflow processInstance search");
        }
        if (processInstanceResponse == null || CollectionUtils.isEmpty(processInstanceResponse.getProcessInstances())) {
            log.debug("No process instances found in history for incident: {}", IncidentId);
            return Collections.emptyList();
        }

        log.debug("Found {} process instances in history", processInstanceResponse.getProcessInstances().size());
        return processInstanceResponse.getProcessInstances();
    }


    /**
     * Method to integrate with workflow
     * <p>
     * take the ProcessInstanceRequest as paramerter to call wf-service
     * <p>
     * and return wf-response to sets the resultant status
     */
    private ProcessInstance callWorkFlow(ProcessInstanceRequest workflowReq) {
        log.trace("WorkflowService::callWorkFlow method invoked");
        log.info("Calling workflow transition service");

        ProcessInstanceResponse response = null;
        StringBuilder url = new StringBuilder(imConfiguration.getWfHost().concat(imConfiguration.getWfTransitionPath()));
        log.trace("Calling workflow service at URL: {}", url);
        Object optional = repository.fetchResult(url, workflowReq);
        try {
            response = mapper.convertValue(optional, ProcessInstanceResponse.class);
        } catch (IllegalArgumentException e) {
            log.error("Failed to parse workflow transition response", e);
            throw new CustomException("PARSING_ERROR", "Failed to parse workflow transition response");
        }
        log.debug("Workflow transition completed successfully");
        return response.getProcessInstances().get(0);
    }

    public StringBuilder getprocessInstanceSearchURL(String tenantId, String IncidentId) {

        StringBuilder url = new StringBuilder(imConfiguration.getWfHost());
        url.append(imConfiguration.getWfProcessInstanceSearchPath());
        url.append("?tenantId=");
        url.append(tenantId);
        url.append("&businessIds=");
        url.append(IncidentId);
        return url;
    }
}
