package org.egov.wf.service;


import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.egov.tracer.model.CustomException;
import org.egov.wf.repository.BusinessServiceRepository;
import org.egov.wf.repository.WorKflowRepository;
import org.egov.wf.util.WorkflowUtil;
import org.egov.wf.web.models.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
public class TransitionService {


    private WorKflowRepository repository;

    private BusinessServiceRepository businessServiceRepository;

    private WorkflowUtil workflowUtil;



    @Autowired
    public TransitionService(WorKflowRepository repository,
                             BusinessServiceRepository businessServiceRepository,
                             WorkflowUtil workflowUtil) {
        this.repository = repository;
        this.businessServiceRepository = businessServiceRepository;
        this.workflowUtil = workflowUtil;
    }




    /**
     * Creates list of ProcessStateAndAction from the list of the processInstances
     * @return List of ProcessStateAndAction containing the State object for status before the action and after the action and
     * the Action object for the given action
     */
    public List<ProcessStateAndAction> getProcessStateAndActions(List<ProcessInstance> processInstances,Boolean isTransitionCall){
        log.trace("Entering getProcessStateAndActions method");
        int processInstanceCount = processInstances != null ? processInstances.size() : 0;
        log.info("Creating process state and actions for {} process instance(s), isTransitionCall: {}", 
                processInstanceCount, isTransitionCall);

        BusinessService businessService = getBusinessService(processInstances);
        log.debug("Retrieved business service: {}", businessService != null ? businessService.getBusiness() : null);
        Map<String,ProcessInstance> idToProcessInstanceFromDbMap = prepareProcessStateAndAction(processInstances,businessService);
        List<String> allowedRoles = workflowUtil.rolesAllowedInService(businessService);
        log.debug("Retrieved {} process instance(s) from DB, {} allowed role(s)", 
                idToProcessInstanceFromDbMap.size(), allowedRoles != null ? allowedRoles.size() : 0);
        
        List<ProcessStateAndAction> processStateAndActions = new LinkedList<>();
        
        for(ProcessInstance processInstance: processInstances){
            ProcessStateAndAction processStateAndAction = buildProcessStateAndAction(
                    processInstance, businessService, idToProcessInstanceFromDbMap, allowedRoles, isTransitionCall);
            processStateAndActions.add(processStateAndAction);
        }

        log.info("Successfully created {} process state and action(s)", processStateAndActions.size());
        log.trace("Exiting getProcessStateAndActions method");
        return processStateAndActions;
    }

    /**
     * Builds a ProcessStateAndAction for a single ProcessInstance.
     */
    private ProcessStateAndAction buildProcessStateAndAction(ProcessInstance processInstance,
                                                            BusinessService businessService,
                                                            Map<String, ProcessInstance> idToProcessInstanceFromDbMap,
                                                            List<String> allowedRoles,
                                                            Boolean isTransitionCall) {
        ProcessStateAndAction processStateAndAction = new ProcessStateAndAction();
        processStateAndAction.setProcessInstanceFromRequest(processInstance);
        
        if(isTransitionCall){
            processStateAndAction.getProcessInstanceFromRequest().setModuleName(businessService.getBusiness());
        }
        
        processStateAndAction.setProcessInstanceFromDb(idToProcessInstanceFromDbMap.get(processInstance.getBusinessId()));
        
        State currentState = determineCurrentState(processStateAndAction, isTransitionCall);
        setCurrentState(processStateAndAction, currentState, businessService, processInstance);
        
        //Assign businessSla when creating processInstance
        if(processStateAndAction.getProcessInstanceFromDb()==null && isTransitionCall)
            processInstance.setBusinesssServiceSla(businessService.getBusinessServiceSla());

        findAndSetAction(processStateAndAction, processInstance, allowedRoles);
        
        if(isTransitionCall){
            validateAndSetResultantState(processStateAndAction, businessService, processInstance);
        }
        
        return processStateAndAction;
    }

    /**
     * Determines the current state from DB or request.
     */
    private State determineCurrentState(ProcessStateAndAction processStateAndAction, Boolean isTransitionCall) {
        if(processStateAndAction.getProcessInstanceFromDb()!=null && isTransitionCall)
            return processStateAndAction.getProcessInstanceFromDb().getState();
        else if(!isTransitionCall)
            return processStateAndAction.getProcessInstanceFromRequest().getState();
        return null;
    }

    /**
     * Sets the current state, finding start state if current state is null.
     */
    private void setCurrentState(ProcessStateAndAction processStateAndAction,
                                 State currentState,
                                 BusinessService businessService,
                                 ProcessInstance processInstance) {
        if(currentState == null){
            log.debug("Current state is null for businessId: {}, searching for start state", processInstance.getBusinessId());
            State startState = findStartState(businessService);
            if (startState == null) {
                log.error("No start state found in business service config for businessId: {}", processInstance.getBusinessId());
                throw new CustomException("START_STATE_NOT_FOUND", "No start state found in business service config");
            }
            processStateAndAction.setCurrentState(startState);
        } else {
            processStateAndAction.setCurrentState(currentState);
        }
    }

    /**
     * Finds the start state in the business service.
     */
    private State findStartState(BusinessService businessService) {
        for (State state : businessService.getStates()) {
            if(StringUtils.isEmpty(state.getState())) {
                return state;
            }
        }
        return null;
    }

    /**
     * Finds and sets the action matching the process instance action.
     */
    private void findAndSetAction(ProcessStateAndAction processStateAndAction,
                                 ProcessInstance processInstance,
                                 List<String> allowedRoles) {
        if(CollectionUtils.isEmpty(processStateAndAction.getCurrentState().getActions())){
            return;
        }
        
        for (Action action : processStateAndAction.getCurrentState().getActions()){
            if(action.getAction().equalsIgnoreCase(processInstance.getAction())){
                if(action.getRoles().contains("*"))
                    action.setRoles(allowedRoles);
                processStateAndAction.setAction(action);
                break;
            }
        }
    }

    /**
     * Validates action exists and sets the resultant state for transition calls.
     */
    private void validateAndSetResultantState(ProcessStateAndAction processStateAndAction,
                                             BusinessService businessService,
                                             ProcessInstance processInstance) {
        if(processStateAndAction.getAction()==null) {
            String action = processStateAndAction.getProcessInstanceFromRequest().getAction();
            String businessId = processStateAndAction.getProcessInstanceFromRequest().getBusinessId();
            log.error("Invalid action: {} not found in config for businessId: {}", action, businessId);
            throw new CustomException("INVALID ACTION","Action "+action
                    + " not found in config for the businessId: " + businessId);
        }

        for(State state : businessService.getStates()){
            if(state.getUuid().equalsIgnoreCase(processStateAndAction.getAction().getNextState())){
                processStateAndAction.setResultantState(state);
                log.debug("Set resultant state for businessId: {}, action: {}", 
                        processInstance.getBusinessId(), processInstance.getAction());
                break;
            }
        }
    }




    /**
     * Current status of the incoming request is fetched from the DB and set
     *
     * If the request object is being created for the first time
     *
     * then state will remain null
     *
     * @param processInstances The list of ProcessInstance to be created
     */
    private Map<String,ProcessInstance> prepareProcessStateAndAction(List<ProcessInstance> processInstances,BusinessService businessService) {
        log.trace("Entering prepareProcessStateAndAction method");

        /*
         * preparing the criteria to search the process instances from DB
         */
        ProcessInstanceSearchCriteria criteria = new ProcessInstanceSearchCriteria();
        List<String> businessIds = processInstances.stream().map(ProcessInstance::getBusinessId)
                .collect(Collectors.toList());
        criteria.setTenantId(processInstances.get(0).getTenantId());
        criteria.setBusinessIds(businessIds);
        /*
         * fetching the result from repository
         *
         * converting the list of process instances to map of businessId and state
         * object
         */
        List<ProcessInstance> processInstancesFromDB = repository.getProcessInstances(criteria);
        log.debug("Retrieved {} process instance(s) from DB for business IDs", processInstancesFromDB.size());

        Map<String, ProcessInstance> businessStateMap = new LinkedHashMap<>();
        for(ProcessInstance processInstance : processInstancesFromDB){
            businessStateMap.put(processInstance.getBusinessId(), processInstance);
        }

        log.trace("Exiting prepareProcessStateAndAction method");
        return businessStateMap;
    }



    private BusinessService getBusinessService(List<ProcessInstance> processInstances){
        log.trace("Entering getBusinessService method");
        ProcessInstanceSearchCriteria pInsSearchCriteria = new ProcessInstanceSearchCriteria();
        String tenantId = processInstances.get(0).getTenantId();
        String businessId = processInstances.get(0).getBusinessId();
        log.debug("Fetching business service for tenantId: {}, businessId: {}", tenantId, businessId);
        
        pInsSearchCriteria.setTenantId(tenantId);
        pInsSearchCriteria.setBusinessIds(Collections.singletonList(businessId));
        List<ProcessInstance> fetchedProcessInstances = repository.getProcessInstances(pInsSearchCriteria);
        
        BusinessServiceSearchCriteria criteria = new BusinessServiceSearchCriteria();
        String businessService = processInstances.get(0).getBusinessService();
        if (fetchedProcessInstances.size()>0) {
            businessService = fetchedProcessInstances.get(0).getBusinessService();
            processInstances.get(0).setBusinessService(businessService);
            log.debug("Updated business service from fetched process instance: {}", businessService);
        }
        criteria.setTenantId(tenantId);
        criteria.setBusinessServices(Collections.singletonList(businessService));
        List<BusinessService> businessServices = businessServiceRepository.getBusinessServices(criteria);
        if(CollectionUtils.isEmpty(businessServices)) {
            log.error("No business service found for businessService: {} and tenantId: {}", businessService, tenantId);
            throw new CustomException("BUSINESSSERVICE ERROR","No bussinessService object found for businessSerice: "+
                    businessService + " and tenantId: "+tenantId);
        }
        if(businessServices.size()!=1) {
            log.error("Multiple business services found for businessService: {} and tenantId: {}, count: {}", 
                    businessService, tenantId, businessServices.size());
            throw new CustomException("BUSINESSSERVICE ERROR","Multiple bussinessService object found for businessSerice: "+
                    businessService + " and tenantId: "+tenantId);
        }
        log.trace("Exiting getBusinessService method");
        return businessServices.get(0);
    }
}
