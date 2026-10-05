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
import org.egov.im.util.IMConstants;
import org.egov.im.util.MDMSUtils;
import org.egov.im.web.models.*;
import org.egov.im.web.models.workflow.*;
import org.egov.tracer.model.CustomException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
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
        log.trace("Fetching all process instances for SLA and lifecycle calculation");
        List<ProcessInstance> processInstances = getAllProcessInstances(tenantId,IncidentId, requestInfo);

        // Compute first-round resolved / declined timestamps (before any reordering)
        enrichResolvedAndDeclinedTimestamps(wrapper, processInstances);

        // Step 3: Use BusinessHoursUtil (requires latest cycle ordering)
        Collections.reverse(processInstances);
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
     * Computes first-round resolved and declined timestamps from the workflow history
     * and stores them on IndexView so they are available in every index update.
     */
    private void enrichResolvedAndDeclinedTimestamps(IncidentRequestWrapper wrapper, List<ProcessInstance> processInstances) {
        if (CollectionUtils.isEmpty(processInstances)) {
            return;
        }

        // Ensure chronological order (oldest first) for first-occurrence detection
        List<ProcessInstance> ordered = new ArrayList<>(processInstances);
        ordered.sort(Comparator.comparing(pi -> {
            if (pi.getAuditDetails() != null && pi.getAuditDetails().getCreatedTime() != null) {
                return pi.getAuditDetails().getCreatedTime();
            }
            // fallback to 0 if timestamps are missing
            return 0L;
        }));

        Long firstResolvedTs = null;
        Long firstDeclinedTs = null;

        for (ProcessInstance pi : ordered) {
            State state = pi.getState();
            AuditDetails auditDetails = pi.getAuditDetails();

            if (state == null || auditDetails == null || auditDetails.getCreatedTime() == null) {
                continue;
            }

            String status = state.getApplicationStatus();
            Long ts = auditDetails.getCreatedTime();

            if (status == null) {
                continue;
            }

            if (firstResolvedTs == null && "RESOLVED".equalsIgnoreCase(status)) {
                firstResolvedTs = ts;
            }

            // Treat REJECTED as decline; extend if you introduce explicit DECLINE statuses
            if (firstDeclinedTs == null && "REJECTED".equalsIgnoreCase(status)) {
                firstDeclinedTs = ts;
            }

            if (firstResolvedTs != null && firstDeclinedTs != null) {
                break;
            }
        }

        IndexView indexView = wrapper.getIndexView();
        if (indexView == null) {
            indexView = new IndexView();
            wrapper.setIndexView(indexView);
        }

        indexView.setResolvedTimestamp(firstResolvedTs);
        indexView.setDeclinedTimestamp(firstDeclinedTs);
    }

    /**
     * Creates url for search based on given tenantId and businessservices
     *
     * @param tenantId        The tenantId for which url is generated
     * @param businessService The businessService for which url is generated
     * @return The search url
     */
    private StringBuilder getSearchURLWithParams(String tenantId, String businessService) {
        log.trace("WorkflowService::getSearchURLWithParams method invoked");
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
        log.trace("WorkflowService::getTenantIdToServiceWrapperMap method invoked");
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
        log.debug("Grouped {} incident wrappers into {} tenant groups", incidentWrappers.size(), resultMap.size());
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
        }
        // State SPOC and Tech POC are pooled roles: the states they own are deliberately left
        // unassigned by clearAssigneesForPooledState below, so nothing is reassigned to a named
        // facilitator here any more.
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

        clearAssigneesForPooledState(request, action, this.states);

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

    /**
     * Drops the assignees when the transition lands the ticket in a state owned by a pooled role, so
     * that it belongs to the role rather than to a person.
     * <p>
     * This is how a freshly raised ticket already works: nothing assigns it, so every CRM with
     * jurisdiction over its boundary sees it and any of them can act. The same now holds for the State
     * SPOC and Tech POC states - egov-workflow-v2 authorises a state-changing action purely on the
     * acting user's roles ({@code WorkflowValidator.validateAction}) and never requires them to be the
     * assignee, so leaving the assignee empty is what opens the ticket up to the whole role.
     * <p>
     * The decision is read off the workflow config rather than a hardcoded list of transitions: the
     * roles on the resultant state's actions are exactly the roles that state waits on, and if all of
     * the human ones are in {@link IMConstants#POOLED_ROLES} the ticket is pooled. Whatever the caller
     * sent as {@code assignes} is discarded for those states - a client pinning the ticket to one
     * facilitator is precisely what this removes.
     * <p>
     * Best-effort: any failure to resolve the resultant state leaves the assignees untouched, which is
     * the behaviour that predates this rule. An assignment must never fail because a lookup failed.
     */
    void clearAssigneesForPooledState(IncidentRequest request, String action, List<State> states) {
        Workflow workflow = request.getWorkflow();
        String incidentId = request.getIncident().getIncidentId();
        try {
            State resultantState = findResultantState(request.getIncident().getApplicationStatus(), action, states);
            if (resultantState == null) {
                log.debug("Pooling: resultant state of action {} on incident {} could not be resolved, "
                        + "leaving assignees untouched", action, incidentId);
                return;
            }
            if (!isPooledState(resultantState)) {
                return;
            }
            if (!CollectionUtils.isEmpty(workflow.getAssignes())) {
                log.info("Pooling: discarding assignees {} sent for incident {}, state {} is owned by a pooled role",
                        workflow.getAssignes(), incidentId, resultantState.getState());
            }
            workflow.setAssignes(null);
            log.info("Pooling: incident {} left unassigned in state {} so that every holder of the role can act",
                    incidentId, resultantState.getState());
        } catch (Exception e) {
            log.error("Pooling: failed to decide on assignees for incident {}, leaving them untouched",
                    incidentId, e);
        }
    }

    /**
     * The state the ticket is about to land in, resolved from the business service definition the same
     * way egov-workflow-v2's TransitionService resolves it: the action on the current state names its
     * next state by uuid.
     *
     * @return null when the current state, the action or the next state is not in the definition
     */
    private State findResultantState(String currentApplicationStatus, String action, List<State> states) {
        if (CollectionUtils.isEmpty(states) || StringUtils.isBlank(currentApplicationStatus)
                || StringUtils.isBlank(action)) {
            return null;
        }
        String status = currentApplicationStatus.trim();
        State currentState = states.stream()
                // The config may expose the status either as the state name or as applicationStatus.
                .filter(state -> status.equalsIgnoreCase(state.getState())
                        || status.equalsIgnoreCase(state.getApplicationStatus()))
                .findFirst()
                .orElse(null);
        if (currentState == null || CollectionUtils.isEmpty(currentState.getActions())) {
            return null;
        }
        String nextStateUuid = currentState.getActions().stream()
                .filter(a -> a != null && action.equalsIgnoreCase(a.getAction()))
                .map(Action::getNextState)
                .findFirst()
                .orElse(null);
        if (StringUtils.isBlank(nextStateUuid)) {
            return null;
        }
        return states.stream()
                .filter(state -> nextStateUuid.equals(state.getUuid()))
                .findFirst()
                .orElse(null);
    }

    /**
     * True when every human role that can move the ticket on from this state is a pooled one. A state
     * that also waits on a vendor or a health staff member is not pooled - those are real individuals
     * and the ticket has to reach the right one.
     * <p>
     * Terminal states are excluded: the ticket has finished its lifecycle, so it waits on nobody and
     * the actions it still carries (RATE on CLOSEDAFTERRESOLUTION) are after-the-fact ones.
     */
    private boolean isPooledState(State state) {
        if (Boolean.TRUE.equals(state.getIsTerminateState()) || CollectionUtils.isEmpty(state.getActions())) {
            return false;
        }
        List<String> humanRoles = state.getActions().stream()
                .filter(a -> a != null && !CollectionUtils.isEmpty(a.getRoles()))
                .flatMap(a -> a.getRoles().stream())
                .filter(Objects::nonNull)
                .distinct()
                .filter(role -> MACHINE_ROLES.stream().noneMatch(role::equalsIgnoreCase))
                .collect(Collectors.toList());
        return !humanRoles.isEmpty()
                && humanRoles.stream().allMatch(role -> POOLED_ROLES.stream().anyMatch(role::equalsIgnoreCase));
    }

    private void reassignWorkflow(Workflow workflow, IncidentRequest request, String role) {
        reassignWorkflow(workflow, request, role, request.getIncident().getBoundaryCode());
    }

    private void reassignWorkflow(Workflow workflow, IncidentRequest request, String role, String boundaryCode) {
        log.trace("WorkflowService::reassignWorkflow method invoked for role: {} and boundaryCode: {}", role, boundaryCode);
        workflow.setAssignes(null);
        log.debug("Fetching employee details for role: {} at boundary: {}", role, boundaryCode);
        Map<String, String> reassigneeDetails = notificationService.getHRMSEmployee(request, role, boundaryCode);
        List<String> assignee = Arrays.asList(reassigneeDetails.get("employeeUUID"));
        workflow.setAssignes(assignee);
        log.debug("Workflow reassigned to employee with UUID: {}", reassigneeDetails.get("employeeUUID"));
    }

    /**
     * @param processInstances
     */
    public Map<String, Workflow> getWorkflow(List<ProcessInstance> processInstances) {
        log.trace("WorkflowService::getWorkflow method invoked");
        log.debug("Converting {} process instances to workflow map", processInstances.size());
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

        log.debug("Successfully converted {} process instances to workflow map", businessIdToWorkflow.size());
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
        log.trace("WorkflowService::getprocessInstanceSearchURL method invoked");
        StringBuilder url = new StringBuilder(imConfiguration.getWfHost());
        url.append(imConfiguration.getWfProcessInstanceSearchPath());
        url.append("?tenantId=");
        url.append(tenantId);
        url.append("&businessIds=");
        url.append(IncidentId);
        return url;
    }
}
