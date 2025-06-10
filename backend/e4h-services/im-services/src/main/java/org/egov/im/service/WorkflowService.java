package org.egov.im.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.jsonpath.JsonPath;
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
public class WorkflowService {

    private IMConfiguration imConfiguration;

    private ServiceRequestRepository repository;

    private ObjectMapper mapper;

    private NotificationService notificationService;
    private MDMSUtils mdmsUtils;

    private static final Map<Priority, String> PRIORITY_BUSINESS_SERVICE_MAP = Map.of(
            Priority.HIGH, IM_BUSINESSSERVICE_HIGH,
            Priority.MEDIUM, IM_BUSINESSSERVICE_MEDIUM,
            Priority.LOW, IM_BUSINESSSERVICE_LOW
    );

    private List<State> states;

    @Autowired
    public WorkflowService(IMConfiguration imConfiguration,
                           ServiceRequestRepository repository,
                           ObjectMapper mapper, NotificationService notificationService, MDMSUtils mdmsUtils) {
        this.imConfiguration = imConfiguration;
        this.repository = repository;
        this.mapper = mapper;
        this.notificationService = notificationService;
        this.mdmsUtils = mdmsUtils;
    }

    /*
     *
     * Should return the applicable BusinessService for the given request
     *
     * */
    public BusinessService getBusinessService(IncidentRequest incidentRequest, Priority priority) {
        String tenantId = incidentRequest.getIncident().getTenantId();
        String businessService = PRIORITY_BUSINESS_SERVICE_MAP.getOrDefault(priority, IM_BUSINESSSERVICE);
        StringBuilder url = getSearchURLWithParams(tenantId, businessService);
        RequestInfoWrapper requestInfoWrapper
                = RequestInfoWrapper.builder().requestInfo(incidentRequest.getRequestInfo()).build();
        Object result = repository.fetchResult(url, requestInfoWrapper);
        BusinessServiceResponse response = null;
        try {
            response = mapper.convertValue(result, BusinessServiceResponse.class);
        } catch (IllegalArgumentException e) {
            throw new CustomException("PARSING ERROR", "Failed to parse response of workflow business service search");
        }

        if (CollectionUtils.isEmpty(response.getBusinessServices()))
            throw new CustomException("BUSINESSSERVICE_NOT_FOUND", "The businessService " + IM_BUSINESSSERVICE + " is not found");

        return response.getBusinessServices().get(0);
    }


    /*
     * Call the workflow service with the given action and update the status
     * return the updated status of the application
     *
     * */
    public ProcessInstance updateWorkflowStatus(IncidentRequest incidentRequest, Object mdmsData) {
        Priority priority = getPriorityFromMDMS(incidentRequest, mdmsData);
        ProcessInstance processInstance = getProcessInstanceForIM(incidentRequest, priority);
        ProcessInstanceRequest workflowRequest = new ProcessInstanceRequest(incidentRequest.getRequestInfo(), Collections.singletonList(processInstance));
        ProcessInstance updatedProcessInstance = callWorkFlow(workflowRequest);
        incidentRequest.getIncident().setApplicationStatus(updatedProcessInstance.getState().getApplicationStatus());
        updatedProcessInstance.getState().setTotalSlaRemaining(calculateTotalSla(incidentRequest));
        return updatedProcessInstance;
    }

    private Long calculateTotalSla(IncidentRequest request) {
        Long createdTime = request.getIncident().getAuditDetails().getCreatedTime();
        String applicationStatus = request.getIncident().getApplicationStatus();
        ZonedDateTime created = ZonedDateTime.ofInstant(Instant.ofEpochMilli(createdTime), ZoneId.of("Asia/Kolkata"));
        ZonedDateTime now = ZonedDateTime.now(ZoneId.of("Asia/Kolkata"));

        // Step 1: Fetch MDMS BusinessHours data
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
            throw new CustomException("MDMS_PARSE_ERROR", "Unable to parse BusinessHours from MDMS");
        }

        if (businessHourList == null || businessHourList.isEmpty()) {
            throw new CustomException("MDMS_MISSING", "BusinessHours config missing from MDMS");
        }

        // Step 3: Use BusinessHoursUtil
        BusinessHoursUtil util = new BusinessHoursUtil(businessHourList);
        long businessHoursElapsed = util.calculateBusinessDuration(created, now);

        return computeTotalSla(applicationStatus) - businessHoursElapsed;
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

        // FIX ME FOR BULK SEARCH
        Map<String, List<IncidentWrapper>> tenantIdToServiceWrapperMap = getTenantIdToServiceWrapperMap(incidentWrappers);

        List<IncidentWrapper> enrichedServiceWrappers = new ArrayList<>();

        for (String tenantId : tenantIdToServiceWrapperMap.keySet()) {

            List<String> serviceRequestIds = new ArrayList<>();

            List<IncidentWrapper> tenantSpecificWrappers = tenantIdToServiceWrapperMap.get(tenantId);

            tenantSpecificWrappers.forEach(pgrEntity -> {
                serviceRequestIds.add(pgrEntity.getIncident().getIncidentId());
            });

            RequestInfoWrapper requestInfoWrapper = RequestInfoWrapper.builder().requestInfo(requestInfo).build();

            StringBuilder searchUrl = getprocessInstanceSearchURL(tenantId, StringUtils.join(serviceRequestIds, ','));
            Object result = repository.fetchResult(searchUrl, requestInfoWrapper);


            ProcessInstanceResponse processInstanceResponse = null;
            try {
                processInstanceResponse = mapper.convertValue(result, ProcessInstanceResponse.class);
            } catch (IllegalArgumentException e) {
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

        Incident incident = request.getIncident();
        Workflow workflow = request.getWorkflow();
        if (request.getWorkflow().getAction().equalsIgnoreCase("RESOLVE")
                || request.getWorkflow().getAction().equalsIgnoreCase("REJECT")) {
            workflow.setAssignes(null);
            Map<String, String> reassigneeDetails = notificationService.getHRMSEmployee(request, "COMPLAINANT");
            List<String> assignee = Arrays.asList(reassigneeDetails.get("employeeUUID"));
            workflow.setAssignes(assignee);
        }
        if (request.getWorkflow().getAction().equalsIgnoreCase("OUT_OF_WARRANTY")) {
            workflow.setAssignes(null);
            Map<String, String> reassigneeDetails = notificationService.getHRMSEmployee(request, "COMPLAINT_FACILITATOR_1");
            List<String> assignee = Arrays.asList(reassigneeDetails.get("employeeUUID"));
            workflow.setAssignes(assignee);
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

    private long computeTotalSla(String currentState) {
        Map<String, Long> stateToSlaMap = new HashMap<>();

        // Populate the map: state name → SLA millis
        for (State state : this.states) {
            String key = state.getApplicationStatus(); // or use state.getState()
            if (key != null && state.getSla() != null) {
                stateToSlaMap.put(key, state.getSla());
            }
        }

        long totalSla = 0;

        if (PENDINGFORASSIGNMENT.equals(currentState) || PENDINGATVENDOR.equals(currentState)) {
            totalSla += stateToSlaMap.getOrDefault(PENDINGFORASSIGNMENT, 0L);
            totalSla += stateToSlaMap.getOrDefault(PENDINGATVENDOR, 0L);
        } else if (currentState.startsWith(PENDING_ASSIGNMENT_PREFIX)) {
            String suffix = currentState.replace(PENDING_ASSIGNMENT_PREFIX, "");
            String resolutionState = PENDING_RESOLUTION_PREFIX + suffix;
            totalSla += stateToSlaMap.getOrDefault(currentState, 0L);
            totalSla += stateToSlaMap.getOrDefault(resolutionState, 0L);
        } else if (currentState.startsWith(PENDING_RESOLUTION_PREFIX)) {
            String suffix = currentState.replace(PENDING_RESOLUTION_PREFIX, "");
            String assignmentState = PENDING_ASSIGNMENT_PREFIX + suffix;
            totalSla += stateToSlaMap.getOrDefault(currentState, 0L);
            totalSla += stateToSlaMap.getOrDefault(assignmentState, 0L);
        }
        return totalSla;
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

    /**
     * Method to integrate with workflow
     * <p>
     * take the ProcessInstanceRequest as paramerter to call wf-service
     * <p>
     * and return wf-response to sets the resultant status
     */
    private ProcessInstance callWorkFlow(ProcessInstanceRequest workflowReq) {

        ProcessInstanceResponse response = null;
        StringBuilder url = new StringBuilder(imConfiguration.getWfHost().concat(imConfiguration.getWfTransitionPath()));
        Object optional = repository.fetchResult(url, workflowReq);
        response = mapper.convertValue(optional, ProcessInstanceResponse.class);
        return response.getProcessInstances().get(0);
    }

    private Priority getPriorityFromMDMS(IncidentRequest request, Object mdmsData) {
        String serviceCode = request.getIncident().getIncidentSubType();
        String assetType = request.getIncident().getIncidentType();

        String jsonPath = MDMS_SERVICEDEF_SEARCH.replace("{SERVICEDEF}", serviceCode);

        List<Object> res;
        try {
            res = JsonPath.read(mdmsData, jsonPath);
        } catch (Exception e) {
            throw new CustomException("JSONPATH_ERROR", "Failed to parse mdms response");
        }

        if (CollectionUtils.isEmpty(res)) {
            throw new CustomException("INVALID_SERVICECODE", "The service code: " + serviceCode + " is not present in MDMS");
        }

        for (Object obj : res) {
            if (obj instanceof Map) {
                Map<String, Object> map = (Map<String, Object>) obj;

                String menuPath = String.valueOf(map.get("menuPath"));
                String mdmsServiceCode = String.valueOf(map.get("serviceCode"));

                if (assetType.equals(menuPath) && serviceCode.equals(mdmsServiceCode)) {
                    String priorityStr = String.valueOf(map.get("priority"));
                    return Priority.fromString(priorityStr);
                }
            }
        }

        throw new CustomException("PRIORITY_NOT_FOUND", "Priority not found for assetType: " + assetType + " and serviceCode: " + serviceCode);
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
