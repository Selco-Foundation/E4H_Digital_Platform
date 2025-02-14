package org.egov.im.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.egov.im.config.IMConfiguration;
import org.egov.im.repository.ServiceRequestRepository;
import org.egov.im.web.models.*;
import org.egov.im.web.models.workflow.*;
import org.egov.tracer.model.CustomException;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.*;
import java.util.stream.Collectors;

import static org.egov.im.util.IMConstants.*;

@RequiredArgsConstructor
@Service
@Slf4j
public class WorkflowService {

    private final IMConfiguration imConfiguration;
    private final ServiceRequestRepository repository;
    private final ObjectMapper mapper;
    private final UserService userService;

    /*
     *
     * Should return the applicable BusinessService for the given request
     *
     * */
    public BusinessService getBusinessService(IncidentRequest incidentRequest) {
        String tenantId = incidentRequest.getIncident().getTenantId();
        StringBuilder url = getSearchURLWithParams(tenantId, IM_BUSINESSSERVICE);
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
    public String updateWorkflowStatus(IncidentRequest incidentRequest) {
        ProcessInstance processInstance = getProcessInstanceForIM(incidentRequest);
        ProcessInstanceRequest workflowRequest = new ProcessInstanceRequest(
                incidentRequest.getRequestInfo(), Collections.singletonList(processInstance));
        State state = callWorkFlow(workflowRequest);
        incidentRequest.getIncident().setApplicationStatus(state.getApplicationStatus());
        return state.getApplicationStatus();
    }


    public void validateAssignee(IncidentRequest incidentRequest) {
        /*
         * Call HRMS service and validate of the assignee belongs to same department
         * as the employee assigning it
         *
         * */

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

            RequestInfoWrapper requestInfoWrapper
                    = RequestInfoWrapper.builder()
                    .requestInfo(requestInfo)
                    .build();

            StringBuilder searchUrl = getprocessInstanceSearchURL(tenantId, StringUtils.join(serviceRequestIds, ','));
            Object result = repository.fetchResult(searchUrl, requestInfoWrapper);

            ProcessInstanceResponse processInstanceResponse = null;
            try {
                processInstanceResponse = mapper.convertValue(result, ProcessInstanceResponse.class);
            } catch (IllegalArgumentException e) {
                throw new CustomException("PARSING ERROR", "Failed to parse response of workflow processInstance search");
            }

            if (CollectionUtils.isEmpty(processInstanceResponse.getProcessInstances())
                    || processInstanceResponse.getProcessInstances().size() != serviceRequestIds.size())
                throw new CustomException("WORKFLOW_NOT_FOUND", "The workflow object is not found");

            Map<String, Workflow> businessIdToWorkflow
                    = getWorkflow(Objects.requireNonNull(processInstanceResponse.getProcessInstances()));

            tenantSpecificWrappers.forEach(pgrEntity -> pgrEntity.setWorkflow(businessIdToWorkflow.get(pgrEntity
                    .getIncident()
                    .getIncidentId())));

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
    private ProcessInstance getProcessInstanceForIM(IncidentRequest request) {

        Incident incident = request.getIncident();
        Workflow workflow = request.getWorkflow();
        if (request.getWorkflow().getAction().equalsIgnoreCase("RESOLVE")
                || request.getWorkflow().getAction().equalsIgnoreCase("REJECT")) {
            workflow.setAssignes(null);
            Map<String, String> reassigneeDetails = userService.getHRMSEmployee(request, "COMPLAINANT");
            List<String> assignee = Arrays.asList(reassigneeDetails.get("employeeUUID"));
            workflow.setAssignes(assignee);
        }
        ProcessInstance processInstance = new ProcessInstance();
        processInstance.setBusinessId(incident.getIncidentId());
        processInstance.setAction(request.getWorkflow().getAction());
        processInstance.setModuleName(IM_MODULENAME);
        processInstance.setTenantId(incident.getTenantId());
        processInstance.setBusinessService(getBusinessService(request).getBusinessService());
        processInstance.setDocuments(request.getWorkflow().getVerificationDocuments());
        processInstance.setComment(workflow.getComments());

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
                    .verificationDocuments((processInstance.getDocuments()))
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
    private State callWorkFlow(ProcessInstanceRequest workflowReq) {

        ProcessInstanceResponse response = null;
        StringBuilder url = new StringBuilder(imConfiguration.getWfHost().concat(imConfiguration.getWfTransitionPath()));
        Object optional = repository.fetchResult(url, workflowReq);
        response = mapper.convertValue(optional, ProcessInstanceResponse.class);
        return response.getProcessInstances().get(0).getState();
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
