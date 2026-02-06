package org.egov.wf.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.egov.common.contract.request.RequestInfo;
import org.egov.wf.config.WorkflowConfig;
import org.egov.wf.producer.Producer;
import org.egov.wf.repository.BusinessServiceRepository;
import org.egov.wf.repository.WorKflowRepository;
import org.egov.wf.util.WorkflowConstants;
import org.egov.wf.util.WorkflowUtil;
import org.egov.wf.validator.WorkflowValidator;
import org.egov.wf.web.models.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.egov.tracer.model.CustomException;
import org.springframework.util.ObjectUtils;

import static java.util.Objects.isNull;

import java.util.*;
import java.util.stream.Collectors;


@Service
@Slf4j
public class WorkflowService {

    private WorkflowConfig config;

    private TransitionService transitionService;

    private EnrichmentService enrichmentService;

    private WorkflowValidator workflowValidator;

    private StatusUpdateService statusUpdateService;

    private WorKflowRepository workflowRepository;

    private WorkflowUtil util;

    private BusinessServiceRepository businessServiceRepository;
    
    @Autowired
    private MDMSService mdmsService;

    @Autowired
    private BusinessMasterService businessMasterService;

    private Producer producer;

    private ImServiceClient imServiceClient;

    @Autowired
    public WorkflowService(WorkflowConfig config, TransitionService transitionService,
                           EnrichmentService enrichmentService, WorkflowValidator workflowValidator,
                           StatusUpdateService statusUpdateService, WorKflowRepository workflowRepository,
                           WorkflowUtil util,BusinessServiceRepository businessServiceRepository,
                           Producer producer, ImServiceClient imServiceClient) {
        this.config = config;
        this.transitionService = transitionService;
        this.enrichmentService = enrichmentService;
        this.workflowValidator = workflowValidator;
        this.statusUpdateService = statusUpdateService;
        this.workflowRepository = workflowRepository;
        this.util = util;
        this.businessServiceRepository = businessServiceRepository;
        this.producer = producer;
        this.imServiceClient = imServiceClient;
    }


    /**
     * Creates or updates the processInstanceFromRequest
     * @param request The incoming request for workflow transition
     * @return The list of processInstanceFromRequest objects after taking action
     */
    public List<ProcessInstance> transition(ProcessInstanceRequest request){
        RequestInfo requestInfo = request.getRequestInfo();

        List<ProcessStateAndAction> processStateAndActions = transitionService.getProcessStateAndActions(request.getProcessInstances(),true);
        enrichmentService.enrichProcessRequest(requestInfo,processStateAndActions);
        workflowValidator.validateRequest(requestInfo,processStateAndActions);
        statusUpdateService.updateStatus(requestInfo,processStateAndActions);
        return request.getProcessInstances();
    }


    /**
     * Fetches ProcessInstances from db based on processSearchCriteria
     * @param requestInfo The RequestInfo of the search request
     * @param criteria The object containing Search params
     * @return List of processInstances based on search criteria
     */
    public List<ProcessInstance> search(RequestInfo requestInfo,ProcessInstanceSearchCriteria criteria){
        List<ProcessInstance> processInstances;
        if(criteria.isNull())
            processInstances = getUserBasedProcessInstances(requestInfo, criteria);
        else processInstances = workflowRepository.getProcessInstances(criteria);
        if(CollectionUtils.isEmpty(processInstances))
            return processInstances;

        enrichmentService.enrichUsersFromSearch(requestInfo,processInstances);
        List<ProcessStateAndAction> processStateAndActions = enrichmentService.enrichNextActionForSearch(requestInfo,processInstances);
    //    workflowValidator.validateSearch(requestInfo,processStateAndActions);
        enrichmentService.enrichAndUpdateSlaForSearch(processInstances);
        return processInstances;
    }

    public List<ProcessInstance> searchProcessInstanceMigration(RequestInfo requestInfo,ProcessInstanceSearchCriteria criteria){
        List<ProcessInstance> processInstances;
        processInstances = workflowRepository.getProcessInstanceForMigration(criteria);
        if(CollectionUtils.isEmpty(processInstances))
            return processInstances;

//        enrichmentService.enrichUsersFromSearch(requestInfo,processInstances);
//        List<ProcessStateAndAction> processStateAndActions = enrichmentService.enrichNextActionForSearch(requestInfo,processInstances);
//            workflowValidator.validateSearch(requestInfo,processStateAndActions);
        enrichmentService.enrichAndUpdateSlaForSearch(processInstances);
        return processInstances;
    }


    public Integer count(RequestInfo requestInfo,ProcessInstanceSearchCriteria criteria){
        Integer count;
        
     // Enrich slot sla limit in case of nearingSla count
        if(criteria.getIsNearingSlaCount()){

            if(ObjectUtils.isEmpty(criteria.getBusinessService()))
                throw new CustomException("EG_WF_BUSINESSSRV_ERR", "Providing business service is mandatory for nearing escalation count");

            Integer slotPercentage = mdmsService.fetchSlotPercentageForNearingSla(requestInfo);
            Long maxBusinessServiceSla = businessMasterService.getMaxBusinessServiceSla(criteria);
            criteria.setSlotPercentageSlaLimit(maxBusinessServiceSla - slotPercentage * (maxBusinessServiceSla/100));
        }
        
        if(criteria.isNull()){
            enrichSearchCriteriaFromUser(requestInfo, criteria);
            count = workflowRepository.getInboxCount(criteria);
        }
        else count = workflowRepository.getProcessInstancesCount(criteria);

        return count;
    }





    /**
     * Searches the processInstances based on user and its roles
     * @param requestInfo The RequestInfo of the search request
     * @param criteria The object containing Search params
     * @return List of processInstances based on search criteria
     */
    private List<ProcessInstance> getUserBasedProcessInstances(RequestInfo requestInfo,
                                       ProcessInstanceSearchCriteria criteria){

        enrichSearchCriteriaFromUser(requestInfo, criteria);
        List<ProcessInstance> processInstances = workflowRepository.getProcessInstancesForUserInbox(criteria);

        processInstances = filterDuplicates(processInstances);

        return processInstances;

    }
    public Integer getUserBasedProcessInstancesCount(RequestInfo requestInfo,ProcessInstanceSearchCriteria criteria){
        Integer count;
        count = workflowRepository.getProcessInstancesForUserInboxCount(criteria);
        return count;
    }

    /**
     * Removes duplicate businessId which got created due to simultaneous request
     * @param processInstances
     * @return
     */
    private List<ProcessInstance> filterDuplicates(List<ProcessInstance> processInstances){

        if(CollectionUtils.isEmpty(processInstances))
            return processInstances;

        Map<String,ProcessInstance> businessIdToProcessInstanceMap = new LinkedHashMap<>();

        for(ProcessInstance processInstance : processInstances){
            businessIdToProcessInstanceMap.put(processInstance.getBusinessId(), processInstance);
        }

        return new LinkedList<>(businessIdToProcessInstanceMap.values());
    }
    
    public List statusCount(RequestInfo requestInfo,ProcessInstanceSearchCriteria criteria){
        List result;
        if(criteria.isNull() && !isNull(criteria.getBusinessService()) && !criteria.getBusinessService().equalsIgnoreCase(WorkflowConstants.FSM_MODULE)){
        	enrichSearchCriteriaFromUser(requestInfo, criteria);
            result = workflowRepository.getInboxStatusCount(criteria);
        }
        else {
//        	List<String> origCriteriaStatuses = criteria.getStatus();
        	// enrichSearchCriteriaFromUser(requestInfo, criteria);
//        	String tenantId = (criteria.getTenantId() == null ? (requestInfo.getUserInfo().getTenantId()) :(criteria.getTenantId()));
//        	List<String> finalCriteriaStatuses = new ArrayList<String>();
//        	if(origCriteriaStatuses != null && !origCriteriaStatuses.isEmpty()) {
//        		origCriteriaStatuses.forEach((status) ->{
//        			finalCriteriaStatuses.add(tenantId+":"+status);
//        		});
//        		criteria.setStatus(finalCriteriaStatuses);
//        	}
        	result = workflowRepository.getProcessInstancesStatusCount(criteria);
        }

        return result;
    }

    /**
     * Enriches processInstance search criteria based on requestInfo
     * @param requestInfo
     * @param criteria
     */
    private void enrichSearchCriteriaFromUser(RequestInfo requestInfo,ProcessInstanceSearchCriteria criteria){

        /*BusinessServiceSearchCriteria businessServiceSearchCriteria = new BusinessServiceSearchCriteria();

        *//*
         * If tenantId is sent in query param processInstances only for that tenantId is returned
         * else all tenantIds for which the user has roles are returned
         * *//*
        if(criteria.getTenantId()!=null)
            businessServiceSearchCriteria.setTenantIds(Collections.singletonList(criteria.getTenantId()));
        else
            businessServiceSearchCriteria.setTenantIds(util.getTenantIds(requestInfo.getUserInfo()));

        Map<String, Boolean> stateLevelMapping = stat

        List<BusinessService> businessServices = businessServiceRepository.getAllBusinessService();
        List<String> actionableStatuses = util.getActionableStatusesForRole(requestInfo,businessServices,criteria);
        criteria.setAssignee(requestInfo.getUserInfo().getUuid());
        criteria.setStatus(actionableStatuses);*/

        util.enrichStatusesInSearchCriteria(requestInfo, criteria);
        criteria.setAssignee(requestInfo.getUserInfo().getUuid());


    }


    public List<ProcessInstance> escalatedApplicationsSearch(RequestInfo requestInfo, ProcessInstanceSearchCriteria criteria) {
        List<String> escalatedApplicationsBusinessIds;
        List<ProcessInstance> escalatedApplications = new ArrayList<>();
        criteria.setIsEscalatedCount(false);
//        Set<String> autoEscalationEmployeesUuids = enrichmentService.enrichUuidsOfAutoEscalationEmployees(requestInfo, criteria);
        //Set<String> statesToIgnore = enrichmentService.fetchStatesToIgnoreFromMdms(requestInfo, criteria.getTenantId());
        escalatedApplicationsBusinessIds = workflowRepository.fetchEscalatedApplicationsBusinessIdsFromDb(requestInfo,criteria);
        if(CollectionUtils.isEmpty(escalatedApplicationsBusinessIds)){
            return escalatedApplications;
        }
        // SEARCH BASED ON FILTERED BUSINESS IDs DONE HERE
        ProcessInstanceSearchCriteria searchCriteria =  new ProcessInstanceSearchCriteria();
        searchCriteria.setBusinessIds(escalatedApplicationsBusinessIds);
        searchCriteria.setTenantId(criteria.getTenantId());
        searchCriteria.setBusinessService(criteria.getBusinessService());
//        searchCriteria.setHistory(true);
        escalatedApplications = search(requestInfo, searchCriteria);

        // Only last but one applications in history needs to show up where the employee failed to take action

//        HashMap<String, List<ProcessInstance>> businessIdsVsProcessInstancesMap = new HashMap<>();
//        HashMap<String, Integer> occurenceMap = new HashMap<>();
//        for(ProcessInstance processInstance : escalatedApplicationsWithHistory){
//            if(businessIdsVsProcessInstancesMap.containsKey(processInstance.getBusinessId())){
//                occurenceMap.put(processInstance.getBusinessId(), occurenceMap.get(processInstance.getBusinessId()) + 1);
//                businessIdsVsProcessInstancesMap.get(processInstance.getBusinessId()).add(processInstance);
//            }else{
//                occurenceMap.put(processInstance.getBusinessId(), 1);
//                List<ProcessInstance> processInstanceList = new ArrayList<>();
//                processInstanceList.add(processInstance);
//                businessIdsVsProcessInstancesMap.put(processInstance.getBusinessId(), processInstanceList);
//            }
//        }
//        criteria.setAssignee(requestInfo.getUserInfo().getUuid());
//        for(String businessId : occurenceMap.keySet()){
//            if(occurenceMap.get(businessId) >= 2){
//                Set<String> uuidsOfAssignees = new HashSet<>();
//                if(!CollectionUtils.isEmpty(businessIdsVsProcessInstancesMap.get(businessId).get(1).getAssignes())) {
//                    businessIdsVsProcessInstancesMap.get(businessId).get(1).getAssignes().forEach(user -> {
//                        uuidsOfAssignees.add(user.getUuid());
//                    });
//                }
//                if(autoEscalationEmployeesUuids.contains(businessIdsVsProcessInstancesMap.get(businessId).get(0).getAuditDetails().getCreatedBy()) && uuidsOfAssignees.contains(criteria.getAssignee())){
                   //if(!statesToIgnore.contains(businessIdsVsProcessInstancesMap.get(businessId).get(1).getState().getState()))
//                        escalatedApplications.add(businessIdsVsProcessInstancesMap.get(businessId).get(0));
//                }
//            }
//        }
        return escalatedApplications;
    }

    public Integer countEscalatedApplications(RequestInfo requestInfo,ProcessInstanceSearchCriteria criteria){
        Integer count;
        criteria.setIsEscalatedCount(true);
        count = workflowRepository.getEscalatedApplicationsCount(requestInfo,criteria);
        return count;
    }

    public List<ProcessInstance> proceedUpdateProcessInstance(RequestInfo requestInfo, List<ProcessInstance> processInstances){
        for(ProcessInstance processInstance: processInstances){
            Map<String,BusinessServiceStateMigration> bussinessServiceStateMap = new HashMap<>();
            Map<String,BusinessServiceStateMigration> bussinessServiceIncidentMap = new HashMap<>();
            List<BusinessServiceStateMigration> businessServicesAndStates = workflowRepository.getBusinessServicesAndStates();
            businessServicesAndStates.forEach(businessServiceStateMigration -> {
                if(businessServiceStateMigration.getBusinessService().trim().equals("Incident")){
                    bussinessServiceIncidentMap.put(businessServiceStateMigration.getStateUuid(), businessServiceStateMigration);
                }
                else
                    bussinessServiceStateMap.put(businessServiceStateMigration.getBusinessService()+"_"+businessServiceStateMigration.getTenantId()+"_"+businessServiceStateMigration.getApplicationStatus(), businessServiceStateMigration);
            });

            BusinessServiceStateMigration oldIncident = bussinessServiceIncidentMap.get(processInstance.getState().getApplicationStatus());
            if(oldIncident==null)
                continue;

            if(processInstance.getTenantId() == null || !processInstance.getTenantId().contains(".")) {
                continue;
            }
            String tenantId = processInstance.getTenantId().split("\\.")[0];
            BusinessServiceStateMigration newIncident = bussinessServiceStateMap.get(processInstance.getBusinessService()+"_"+tenantId+"_"+oldIncident.getApplicationStatus());
            if(newIncident == null) {
                continue;
            }
            State state = State.builder().uuid(newIncident.getStateUuid()).sla(newIncident.getStateSla())
                    .build();
            processInstance.setState(state);
            processInstance.setStateSla(newIncident.getStateSla());

            ProcessInstanceRequest processInstanceRequest = new ProcessInstanceRequest(requestInfo, Collections.singletonList(processInstance));
            producer.push(config.getUpdateProcessInstanceTopic(),processInstanceRequest);
        }
        return processInstances;
    }

    // Used to update process instance v3 for this ticket number #1979
    public void updateBusinessServiceV2(RequestInfo requestInfo){
//        List<ProcessInstance> processInstancesTheft = proceedUpdateProcessInstanceTheft(requestInfo);
        List<ProcessInstance> processInstancesSparePart = proceedUpdateProcessInstanceSparePartNeed(requestInfo);

        Set<ProcessInstance> set = new LinkedHashSet<>();
//        set.addAll(processInstancesTheft);
        set.addAll(processInstancesSparePart);

        List<ProcessInstance> mergedList = new ArrayList<>(set);

        ProcessInstanceRequest processInstanceRequest = new ProcessInstanceRequest(requestInfo, mergedList);
        producer.push(config.getUpdateProcessInstanceTopic(),processInstanceRequest);
    }

    public List<ProcessInstance> proceedUpdateProcessInstanceTheft(RequestInfo requestInfo) {
        return updateProcessInstances(
                requestInfo,
                "PENDINGFORASSIGNMENT",
                "PENDINGFORASSIGNMENT_THEFT",
                true,
                false
        );
    }

    public List<ProcessInstance> proceedUpdateProcessInstanceSparePartNeed(RequestInfo requestInfo) {
        return updateProcessInstances(
                requestInfo,
                "PENDING_ASSIGNMENT_SPARE_PART_NEEDED",
                "PENDING_RESOLUTION_SPARE_PART_NEEDED",
                false,
                true
        );
    }


    public List<ProcessInstance> updateProcessInstances(
            RequestInfo requestInfo,
            String sourceState,
            String targetState,
            boolean filterByTheftSubtype,
            boolean copyAssignesFromPendingResolution
    ) {

        ObjectMapper mapper = new ObjectMapper();

        List<BusinessServiceStateMigration> businessServicesAndStates =
                workflowRepository.getBusinessServicesAndStatesV2();

        Map<String, BusinessServiceStateMigration> stateMap =
                businessServicesAndStates.stream()
                        .collect(Collectors.toMap(
                                bs -> bs.getBusinessService() + "_" + bs.getApplicationStatus(),
                                bs -> bs
                        ));

        /* 1) récupérer les stateUuid source */
        Set<String> sourceStateUuids =
                businessServicesAndStates.stream()
                        .filter(bs -> sourceState.equalsIgnoreCase(bs.getState()))
                        .map(BusinessServiceStateMigration::getStateUuid)
                        .collect(Collectors.toSet());

        /* 2) chercher les process instances source */
        ProcessInstanceSearchCriteria criteria = new ProcessInstanceSearchCriteria();
        criteria.setTenantId("in");
        criteria.setHistory(true);
        criteria.setStatus(new ArrayList<>(sourceStateUuids));

        List<ProcessInstance> processInstances = searchProcessInstanceMigration(requestInfo, criteria);

        log.debug("IM request search returned response for proceedUpdateProcessInstance");

        /* 3) filtrage THEFT si demandé */
        if (filterByTheftSubtype && !processInstances.isEmpty()) {

            List<String> businessIds =
                    processInstances.stream()
                            .map(ProcessInstance::getBusinessId)
                            .filter(Objects::nonNull)
                            .collect(Collectors.toList());

            Map<String, Object> imCriteria = new HashMap<>();
            imCriteria.put("incidentId", businessIds);
            imCriteria.put("incidentSubType", "Theft");

            Object response = searchImServiceTicket(imCriteria, requestInfo);
            IncidentResponse incidentResponse =
                    mapper.convertValue(response, IncidentResponse.class);

            Set<String> theftIncidentIds =
                    incidentResponse.getIncidentWrappers().stream()
                            .map(w -> w.getIncident().getIncidentId())
                            .collect(Collectors.toSet());

            processInstances =
                    processInstances.stream()
                            .filter(pi -> theftIncidentIds.contains(pi.getBusinessId()))
                            .collect(Collectors.toList());
        }

        /* 4) mise à jour du state + récupération des assignes si nécessaire */
        for (ProcessInstance pi : processInstances) {

            BusinessServiceStateMigration migration =
                    stateMap.get(pi.getBusinessService() + "_" + targetState);

            if (migration == null) {
                continue;
            }

            /* Cas SPARE_PART_NEEDED : récupérer les assignes depuis PENDING_RESOLUTION */
            if (copyAssignesFromPendingResolution && pi.getBusinessId() != null) {
                // Trouver le assign owner pour le ticket. Pour ca trouver la liste des process instance pour le businessId, ensuite recuperer le process instance qui est a PENDINGRESOLUTION, ensuite recuperer
                // le uuid du assignee dans la table eg_wf_assignee_v2
                BusinessServiceStateMigration pendingResolutionMigration =
                        stateMap.get(pi.getBusinessService() + "_PENDINGRESOLUTION");

                if (pendingResolutionMigration != null) {

                    ProcessInstanceSearchCriteria searchByBusinessId = new ProcessInstanceSearchCriteria();
                    searchByBusinessId.setBusinessIds(Collections.singletonList(pi.getBusinessId()));
                    searchByBusinessId.setStatus(
                            Collections.singletonList(pendingResolutionMigration.getStateUuid())
                    );

                    List<ProcessInstance> existingInstances =
                            searchProcessInstanceMigration(requestInfo, searchByBusinessId);

                    if (existingInstances != null && !existingInstances.isEmpty()) {
                        pi.setAssignes(existingInstances.get(0).getAssignes());
                    }
                }
            }

            State state = State.builder()
                    .uuid(migration.getStateUuid())
                    .sla(migration.getStateSla())
                    .build();

            pi.setState(state);
            pi.setStateSla(migration.getStateSla());
        }

        return processInstances;
    }



    // PENDINGFORASSIGNMENT and Issue Type == THEFT -> PENDINGFORASSIGNMENT_THEFT
//    public List<ProcessInstance> proceedUpdateProcessInstanceTheft(RequestInfo requestInfo){
//        ObjectMapper mapper = new ObjectMapper();
//        List<BusinessServiceStateMigration> businessServicesAndStates = workflowRepository.getBusinessServicesAndStatesV2();
//        Map<String,BusinessServiceStateMigration> bussinessServiceStateMap = new HashMap<>();
//        businessServicesAndStates.forEach(businessServiceStateMigration -> {
//            bussinessServiceStateMap.put(businessServiceStateMigration.getBusinessService()+"_"+businessServiceStateMigration.getApplicationStatus(), businessServiceStateMigration);
//        });
//        Set<String> pendingForAssignmentStateUuids =
//                businessServicesAndStates.stream()
//                        .filter(bs -> "PENDINGFORASSIGNMENT".equalsIgnoreCase(bs.getState()))
//                        .map(BusinessServiceStateMigration::getStateUuid)
//                        .collect(Collectors.toSet());
//
//        // Recherche des processInstances dont le statusUUID est PENDINGFORASSIGNMENT
//        ProcessInstanceSearchCriteria instanceSearchCriteriaPendingForAssignmentStateUuids = new ProcessInstanceSearchCriteria();
//        instanceSearchCriteriaPendingForAssignmentStateUuids.setStatus(new ArrayList<>(pendingForAssignmentStateUuids));
//        List<ProcessInstance> processInstancesPendingForAssignment = search(requestInfo, instanceSearchCriteriaPendingForAssignmentStateUuids);
//        List<String> businessIdList =
//                processInstancesPendingForAssignment.stream()
//                        .map(ProcessInstance::getBusinessId)
//                        .collect(Collectors.toList());
//
//        // Recherche des businessId( A savoir incidentId) dont leur subtype est THEFT parmi la liste des processInstances trouvees precedemment
//        Map<String, Object> criteria = new HashMap<>();
//        criteria.put("incidentId", businessIdList);
//        criteria.put("incidentSubType", "Theft");
//        Object response = searchImServiceTicket(criteria, requestInfo);
//        IncidentResponse incidentResponse = mapper.convertValue(response, IncidentResponse.class);
//        List<String> theftIncidentIds =
//                incidentResponse.getIncidentWrappers().stream()
//                        .map(wrapper -> wrapper.getIncident().getIncidentId())
//                        .collect(Collectors.toList());
//
//        // Liste des process instance qui ont comme subtype THEFT
//        Set<String> theftIncidentIdSet = new HashSet<>(theftIncidentIds);
//        List<ProcessInstance> filteredTheftProcessInstances =
//                processInstancesPendingForAssignment.stream()
//                        .filter(pi -> pi.getBusinessId() != null)
//                        .filter(pi -> theftIncidentIdSet.contains(pi.getBusinessId()))
//                        .collect(Collectors.toList());
//
//        for(ProcessInstance processInstance: filteredTheftProcessInstances){
//            BusinessServiceStateMigration stateMigration = bussinessServiceStateMap.get(processInstance.getBusinessService()+"_PENDINGFORASSIGNMENT_THEFT");
//            State state = State.builder().uuid(stateMigration.getStateUuid()).sla(stateMigration.getStateSla()).build();
//
//            processInstance.setState(state);
//            processInstance.setStateSla(stateMigration.getStateSla());
//        }
//        return filteredTheftProcessInstances;
//    }
//
////    // Used to update process instance v3 for this ticket number #1979 PENDINGFORASSIGNMENT and Issue Type == THEFT -> PENDINGFORASSIGNMENT_THEFT
//    public List<ProcessInstance> proceedUpdateProcessInstanceSparePartNeed(RequestInfo requestInfo){
//        List<BusinessServiceStateMigration> businessServicesAndStates = workflowRepository.getBusinessServicesAndStatesV2();
//        Map<String,BusinessServiceStateMigration> bussinessServiceStateMap = new HashMap<>();
//        businessServicesAndStates.forEach(businessServiceStateMigration -> {
//            bussinessServiceStateMap.put(businessServiceStateMigration.getBusinessService()+"_"+businessServiceStateMigration.getApplicationStatus(), businessServiceStateMigration);
//        });
//        Set<String> pendingAssignmentSparePartNeededStateUuids =
//                businessServicesAndStates.stream()
//                        .filter(bs -> "PENDING_ASSIGNMENT_SPARE_PART_NEEDED"
//                                .equalsIgnoreCase(bs.getState()))
//                        .map(BusinessServiceStateMigration::getStateUuid)
//                        .collect(Collectors.toSet());
//
//        // Recherche des processInstances dont le statusUUID est PENDING_ASSIGNMENT_SPARE_PART_NEEDED
//        ProcessInstanceSearchCriteria instanceSearchCriteriaPendingForAssignmentStateUuids = new ProcessInstanceSearchCriteria();
//        instanceSearchCriteriaPendingForAssignmentStateUuids.setStatus(new ArrayList<>(pendingAssignmentSparePartNeededStateUuids));
//        List<ProcessInstance> processInstancesPendingAssignmentSparePartNeeded = search(requestInfo, instanceSearchCriteriaPendingForAssignmentStateUuids);
//
//        // Proceder a la mise a jour du status PENDING_ASSIGNMENT_SPARE_PART_NEEDED vers PENDING_RESOLUTION_SPARE_PART_NEEDED uuid
//        for(ProcessInstance processInstance: processInstancesPendingAssignmentSparePartNeeded){
//            BusinessServiceStateMigration stateMigration = bussinessServiceStateMap.get(processInstance.getBusinessService()+"_PENDING_RESOLUTION");
//            // Trouver le assign owner pour le ticket. Pour ca trouver la liste des process instance pour le businessId, ensuite recuperer le process instance qui est a PENDINGRESOLUTION, ensuite recuperer
//            // le uuid du assignee dans la table eg_wf_assignee_v2
//            ProcessInstanceSearchCriteria instanceSearchCriteriaBusinessId = new ProcessInstanceSearchCriteria();
//            instanceSearchCriteriaBusinessId.setBusinessIds(Collections.singletonList(processInstance.getBusinessId()));
//            instanceSearchCriteriaBusinessId.setStatus(Collections.singletonList(stateMigration.getStateUuid()));
//            List<ProcessInstance> processInstancesBusinessId = search(requestInfo, instanceSearchCriteriaPendingForAssignmentStateUuids);
//            if (processInstancesBusinessId!=null && !processInstancesBusinessId.isEmpty()){
//                ProcessInstance instancePendingResolution = processInstancesBusinessId.get(0);
//                processInstance.setAssignes(instancePendingResolution.getAssignes());
//            }
//
////            BusinessServiceStateMigration stateMigration = bussinessServiceStateMap.get(processInstance.getBusinessService()+"_PENDING_RESOLUTION_SPARE_PART_NEEDED");
//            State state = State.builder().uuid(stateMigration.getStateUuid()).sla(stateMigration.getStateSla()).build();
//
//            processInstance.setState(state);
//            processInstance.setStateSla(stateMigration.getStateSla());
//        }
//        return processInstancesPendingAssignmentSparePartNeeded;
//    }

    public Object searchImServiceTicket(Map<String, Object> searchCriteria, RequestInfo requestInfo){
        // Call IM service request search API (POST /request/_search)
        searchCriteria.put("limit", config.getMaxSearchLimit() != null ? config.getMaxSearchLimit() : 100);
        searchCriteria.put("offset", config.getDefaultOffset() != null ? config.getDefaultOffset() : 0);
        try {
            Map<String, Object> incidentResponse = imServiceClient.requestSearch(requestInfo, searchCriteria);
            // Use incidentResponse (contains responseInfo, IncidentWrappers) as needed
            if (incidentResponse != null) {
                log.debug("IM request search returned response for proceedUpdateProcessInstance");
                return incidentResponse;
            }
        } catch (Exception e) {
            log.warn("IM request search call failed in proceedUpdateProcessInstance: {}", e.getMessage());
        }
        return null;
    }
}
