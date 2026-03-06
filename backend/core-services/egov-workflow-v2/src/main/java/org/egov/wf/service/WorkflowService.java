package org.egov.wf.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.egov.common.contract.request.RequestInfo;
import org.egov.wf.config.WorkflowConfig;
import org.egov.wf.producer.Producer;
import org.egov.wf.repository.BusinessServiceRepository;
import org.egov.wf.repository.WorKflowRepository;
import org.egov.wf.util.ElasticSearchClient;
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
    private final ElasticSearchClient esClient;

    private ImServiceClient imServiceClient;

    @Autowired
    public WorkflowService(WorkflowConfig config, TransitionService transitionService,
                           EnrichmentService enrichmentService, WorkflowValidator workflowValidator,
                           StatusUpdateService statusUpdateService, WorKflowRepository workflowRepository,
                           WorkflowUtil util, BusinessServiceRepository businessServiceRepository,
                           Producer producer, ElasticSearchClient esClient, ImServiceClient imServiceClient) {
        this.config = config;
        this.transitionService = transitionService;
        this.enrichmentService = enrichmentService;
        this.workflowValidator = workflowValidator;
        this.statusUpdateService = statusUpdateService;
        this.workflowRepository = workflowRepository;
        this.util = util;
        this.businessServiceRepository = businessServiceRepository;
        this.producer = producer;
        this.esClient = esClient;
        this.imServiceClient = imServiceClient;
    }


    /**
     * Creates or updates the processInstanceFromRequest
     * @param request The incoming request for workflow transition
     * @return The list of processInstanceFromRequest objects after taking action
     */
    public List<ProcessInstance> transition(ProcessInstanceRequest request){
        log.trace("Entering transition method");
        RequestInfo requestInfo = request.getRequestInfo();
        int processInstanceCount = request.getProcessInstances() != null ? request.getProcessInstances().size() : 0;
        log.info("Processing workflow transition request for {} process instance(s)", processInstanceCount);

        List<ProcessStateAndAction> processStateAndActions = transitionService.getProcessStateAndActions(request.getProcessInstances(),true);
        log.debug("Retrieved processStateAndActions: {}", processStateAndActions.size());
        
        enrichmentService.enrichProcessRequest(requestInfo,processStateAndActions);
        log.debug("Enriched process request with user and SLA information");
        
        workflowValidator.validateRequest(requestInfo,processStateAndActions);
        log.debug("Validated transition request");
        
        statusUpdateService.updateStatus(requestInfo,processStateAndActions);
        log.info("Successfully completed workflow transition for {} process instance(s)", processInstanceCount);
        log.trace("Exiting transition method");
        return request.getProcessInstances();
    }


    /**
     * Fetches ProcessInstances from db based on processSearchCriteria
     * @param requestInfo The RequestInfo of the search request
     * @param criteria The object containing Search params
     * @return List of processInstances based on search criteria
     */
    public List<ProcessInstance> search(RequestInfo requestInfo,ProcessInstanceSearchCriteria criteria){
        log.trace("Entering search method");
        log.info("Searching process instances with criteria - businessService: {}, tenantId: {}", 
                criteria.getBusinessService(), criteria.getTenantId());
        
        List<ProcessInstance> processInstances;
        if(criteria.isNull()) {
            log.debug("Search criteria is null, fetching user-based process instances");
            processInstances = getUserBasedProcessInstances(requestInfo, criteria);
        } else {
            log.debug("Searching process instances from repository");
            processInstances = workflowRepository.getProcessInstances(criteria);
        }
        
        if(CollectionUtils.isEmpty(processInstances)) {
            log.info("No process instances found for search criteria");
            log.trace("Exiting search method - empty result");
            return processInstances;
        }

        log.debug("Found {} process instance(s), enriching with user data", processInstances.size());
        enrichmentService.enrichUsersFromSearch(requestInfo,processInstances);
        
        log.debug("Enriching next actions for search results");
        List<ProcessStateAndAction> processStateAndActions = enrichmentService.enrichNextActionForSearch(requestInfo,processInstances);
    //    workflowValidator.validateSearch(requestInfo,processStateAndActions);
        
        log.debug("Enriching and updating SLA for search results");
        enrichmentService.enrichAndUpdateSlaForSearch(processInstances);
        
        log.info("Search completed successfully, returning {} process instance(s)", processInstances.size());
        log.trace("Exiting search method");
        return processInstances;
    }

    public List<ProcessInstance> searchProcessInstanceMigration(RequestInfo requestInfo,ProcessInstanceSearchCriteria criteria){
        List<ProcessInstance> processInstances;
        processInstances = workflowRepository.getProcessInstanceForMigration(criteria);
        if(CollectionUtils.isEmpty(processInstances))
            return processInstances;

        try {
            enrichmentService.enrichUsersFromSearch(requestInfo,processInstances);
        }
        catch (Exception e){}
//        List<ProcessStateAndAction> processStateAndActions = enrichmentService.enrichNextActionForSearch(requestInfo,processInstances);
//            workflowValidator.validateSearch(requestInfo,processStateAndActions);
        enrichmentService.enrichAndUpdateSlaForSearch(processInstances);
        return processInstances;
    }


    public Integer count(RequestInfo requestInfo,ProcessInstanceSearchCriteria criteria){
        log.trace("Entering count method");
        Integer count;
        
     // Enrich slot sla limit in case of nearingSla count
        if(criteria.getIsNearingSlaCount()){
            log.info("Processing nearing SLA count request");
            
            if(ObjectUtils.isEmpty(criteria.getBusinessService())) {
                log.error("Business service is mandatory for nearing SLA count but was not provided");
                throw new CustomException("EG_WF_BUSINESSSRV_ERR", "Providing business service is mandatory for nearing escalation count");
            }

            log.debug("Fetching slot percentage and max business service SLA");
            Integer slotPercentage = mdmsService.fetchSlotPercentageForNearingSla(requestInfo);
            Long maxBusinessServiceSla = businessMasterService.getMaxBusinessServiceSla(criteria);
            Long slotPercentageSlaLimit = maxBusinessServiceSla - slotPercentage * (maxBusinessServiceSla/100);
            criteria.setSlotPercentageSlaLimit(slotPercentageSlaLimit);
            log.debug("Calculated slot percentage SLA limit: {}", slotPercentageSlaLimit);
        }
        
        if(criteria.isNull()){
            log.debug("Criteria is null, enriching from user and getting inbox count");
            enrichSearchCriteriaFromUser(requestInfo, criteria);
            count = workflowRepository.getInboxCount(criteria);
        }
        else {
            log.debug("Getting process instances count from repository");
            count = workflowRepository.getProcessInstancesCount(criteria);
        }

        log.info("Count query completed, result: {}", count);
        log.trace("Exiting count method");
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
        log.trace("Entering getUserBasedProcessInstances method");

        enrichSearchCriteriaFromUser(requestInfo, criteria);
        log.debug("Enriched search criteria from user");
        
        List<ProcessInstance> processInstances = workflowRepository.getProcessInstancesForUserInbox(criteria);
        log.debug("Retrieved {} process instance(s) from user inbox", 
                processInstances != null ? processInstances.size() : 0);

        processInstances = filterDuplicates(processInstances);
        int finalCount = processInstances != null ? processInstances.size() : 0;
        log.debug("After filtering duplicates: {} process instance(s)", finalCount);
        log.trace("Exiting getUserBasedProcessInstances method");

        return processInstances;

    }
    public Integer getUserBasedProcessInstancesCount(RequestInfo requestInfo,ProcessInstanceSearchCriteria criteria){
        log.trace("Entering getUserBasedProcessInstancesCount method");
        Integer count = workflowRepository.getProcessInstancesForUserInboxCount(criteria);
        log.debug("User-based process instances count: {}", count);
        log.trace("Exiting getUserBasedProcessInstancesCount method");
        return count;
    }

    /**
     * Removes duplicate businessId which got created due to simultaneous request
     * @param processInstances
     * @return
     */
    private List<ProcessInstance> filterDuplicates(List<ProcessInstance> processInstances){
        log.trace("Entering filterDuplicates method");

        if(CollectionUtils.isEmpty(processInstances)) {
            log.trace("Exiting filterDuplicates method - empty input");
            return processInstances;
        }

        int originalSize = processInstances.size();
        Map<String,ProcessInstance> businessIdToProcessInstanceMap = new LinkedHashMap<>();

        for(ProcessInstance processInstance : processInstances){
            businessIdToProcessInstanceMap.put(processInstance.getBusinessId(), processInstance);
        }

        List<ProcessInstance> filtered = new LinkedList<>(businessIdToProcessInstanceMap.values());
        int filteredSize = filtered.size();
        if(originalSize != filteredSize) {
            log.debug("Filtered duplicates: {} duplicate(s) removed", originalSize - filteredSize);
        }
        log.trace("Exiting filterDuplicates method");
        return filtered;
    }
    
    public List statusCount(RequestInfo requestInfo,ProcessInstanceSearchCriteria criteria){
        log.trace("Entering statusCount method");
        log.info("Fetching status count for businessService: {}", criteria.getBusinessService());
        
        List result;
        if(criteria.isNull() && !isNull(criteria.getBusinessService()) && !criteria.getBusinessService().equalsIgnoreCase(WorkflowConstants.FSM_MODULE)){
        	log.debug("Criteria is null and not FSM module, enriching from user and getting inbox status count");
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

        int resultSize = result != null ? result.size() : 0;
        log.info("Status count query completed, returning {} status count(s)", resultSize);
        log.trace("Exiting statusCount method");
        return result;
    }

    /**
     * Enriches processInstance search criteria based on requestInfo
     * @param requestInfo
     * @param criteria
     */
    private void enrichSearchCriteriaFromUser(RequestInfo requestInfo,ProcessInstanceSearchCriteria criteria){
        log.trace("Entering enrichSearchCriteriaFromUser method");

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
        log.debug("Enriched statuses in search criteria");
        
        criteria.setAssignee(requestInfo.getUserInfo().getUuid());
        log.debug("Set assignee in criteria to user UUID");
        log.trace("Exiting enrichSearchCriteriaFromUser method");

    }


    public List<ProcessInstance> escalatedApplicationsSearch(RequestInfo requestInfo, ProcessInstanceSearchCriteria criteria) {
        log.trace("Entering escalatedApplicationsSearch method");
        log.info("Searching for escalated applications - businessService: {}, tenantId: {}", 
                criteria.getBusinessService(), criteria.getTenantId());
        
        List<String> escalatedApplicationsBusinessIds;
        List<ProcessInstance> escalatedApplications = new ArrayList<>();
        criteria.setIsEscalatedCount(false);
//        Set<String> autoEscalationEmployeesUuids = enrichmentService.enrichUuidsOfAutoEscalationEmployees(requestInfo, criteria);
        //Set<String> statesToIgnore = enrichmentService.fetchStatesToIgnoreFromMdms(requestInfo, criteria.getTenantId());
        escalatedApplicationsBusinessIds = workflowRepository.fetchEscalatedApplicationsBusinessIdsFromDb(requestInfo,criteria);
        if(CollectionUtils.isEmpty(escalatedApplicationsBusinessIds)){
            log.info("No escalated applications found");
            log.trace("Exiting escalatedApplicationsSearch method - empty result");
            return escalatedApplications;
        }
        
        log.debug("Found {} escalated application business ID(s)", escalatedApplicationsBusinessIds.size());
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
        log.info("Escalated applications search completed, returning {} process instance(s)", 
                escalatedApplications != null ? escalatedApplications.size() : 0);
        log.trace("Exiting escalatedApplicationsSearch method");
        return escalatedApplications;
    }

    public Integer countEscalatedApplications(RequestInfo requestInfo,ProcessInstanceSearchCriteria criteria){
        log.trace("Entering countEscalatedApplications method");
        criteria.setIsEscalatedCount(true);
        Integer count = workflowRepository.getEscalatedApplicationsCount(requestInfo,criteria);
        log.info("Escalated applications count: {}", count);
        log.trace("Exiting countEscalatedApplications method");
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

        log.info("Starting updateBusinessServiceV2");

        /* 1) Récupération des process instances THEFT */
        log.debug("Fetching process instances for THEFT flow");
        List<ProcessInstance> processInstancesTheft = proceedUpdateProcessInstanceTheft(requestInfo);
        log.info("THEFT process instances fetched: {}", processInstancesTheft != null ? processInstancesTheft.size() : 0);

        /* 2) Récupération des process instances SPARE_PART_NEEDED */
        log.info("Fetching process instances for SPARE_PART_NEEDED flow");
        List<ProcessInstance> processInstancesSparePart = proceedUpdateProcessInstanceSparePartNeed(requestInfo);
        log.info("SPARE_PART_NEEDED process instances fetched: {}",
                processInstancesSparePart != null ? processInstancesSparePart.size() : 0);

        /* 3) Fusion des deux listes en évitant les doublons */
        Set<ProcessInstance> set = new LinkedHashSet<>();
        set.addAll(processInstancesTheft);
        set.addAll(processInstancesSparePart);

        List<ProcessInstance> mergedList = new ArrayList<>(set);
        log.info("Merged process instances count (after deduplication): {}", mergedList.size());

        /* Split into batches to avoid Kafka message size limits */
//        int batchSize = config.getProcessInstanceUpdateBatchSize();
//        int totalBatches = (mergedList.size() + batchSize - 1) / batchSize;
//        log.info("Pushing {} process instances to workflow update topic in {} batch(es) of size {}",
//                mergedList.size(), totalBatches, batchSize);
//
//        for (int i = 0; i < mergedList.size(); i += batchSize) {
//            int end = Math.min(i + batchSize, mergedList.size());
//            List<ProcessInstance> batch = mergedList.subList(i, end);
//
//            ProcessInstanceRequest processInstanceRequest =
//                    new ProcessInstanceRequest(requestInfo, batch);
//            producer.push(config.getUpdateProcessInstanceTopic(), processInstanceRequest);
//
//            log.debug("Pushed batch {}/{} containing {} process instance(s)",
//                    (i / batchSize) + 1, totalBatches, batch.size());
//        }

//        log.info("Successfully pushed all {} process instances to topic: {}",
//                mergedList.size(), config.getUpdateProcessInstanceTopic());

        // Update Kibana index (im-services) with only Data.currentProcessInstance for each migrated process instance
        log.info("Starting Kibana index update for migrated process instances");

        for (ProcessInstance pi : mergedList) {

            if (pi.getBusinessId() == null) {
                log.warn("Skipping process instance with null businessId, processInstanceId={}",
                        pi.getId());
                continue;
            }

//            Map<String, Object> ticket = esClient.getTicketByIncidentId(pi.getBusinessId());
            log.trace("Updating Kibana document for incidentId={}", pi.getBusinessId());

            try {
                esClient.updateProcessInstanceFields(pi);
                log.info("Kibana updated successfully for incidentId={}", pi.getBusinessId());
            } catch (Exception e) {
                log.error("Failed to update Kibana for incidentId={}",
                        pi.getBusinessId(), e);
            }
        }

        log.info("Completed updateBusinessServiceV2");
    }

    // Used to update process instance v3 for this ticket number #1979 PENDINGFORASSIGNMENT and Issue Type == THEFT -> PENDINGFORASSIGNMENT_THEFT
    public List<ProcessInstance> proceedUpdateProcessInstanceTheft(RequestInfo requestInfo) {
        return updateProcessInstances(
                requestInfo,
                "PENDINGFORASSIGNMENT",
                "PENDINGFORASSIGNMENT_THEFT",
                true,
                false
        );
    }

    // Used to update process instance v3 for this ticket number #1979 PENDING_ASSIGNMENT_SPARE_PART_NEEDED -> PENDING_RESOLUTION_SPARE_PART_NEEDED and assign owner to the
    // previously selected vendor for that ticket. ( i.e. bypass the re-assignment of ticket )
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

        log.info("Starting updateProcessInstances | sourceState={}, targetState={}, filterByTheftSubtype={}, copyAssignesFromPendingResolution={}",
                sourceState, targetState, filterByTheftSubtype, copyAssignesFromPendingResolution);

        ObjectMapper mapper = new ObjectMapper();

        List<BusinessServiceStateMigration> businessServicesAndStates =
                workflowRepository.getBusinessServicesAndStatesV2();

        log.info("Fetched {} business service state migrations", businessServicesAndStates.size());

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

        log.info("Source state '{}' resolved to {} UUID(s)", sourceState, sourceStateUuids.size());

        /* 2) chercher les process instances source */
        ProcessInstanceSearchCriteria criteria = new ProcessInstanceSearchCriteria();
        criteria.setTenantId("in");
        criteria.setHistory(true);
        criteria.setStatus(new ArrayList<>(sourceStateUuids));

        List<ProcessInstance> processInstances =
                searchProcessInstanceMigration(requestInfo, criteria);
        List<ProcessInstance> processInstancesKibana = new ArrayList<>();

        // Permet de stocker les ticket de theft mais dont on doit update le status de PENDINGFORASSIGNMENT a PENDINGFORASSIGNMENT_THEFT et le currentprocessInstance dans ES
        Set<String> theftIncidentAssignmentKibanaSet = new HashSet<>();
        Set<String> theftIncidentSParePartKibanaSet = new HashSet<>();

        log.info("IM request search returned {} process instances", processInstances.size());

        /* 3) filtrage THEFT si demandé */
        if (filterByTheftSubtype && !processInstances.isEmpty()) {

            log.info("Applying THEFT subtype filter");

            Set<String> theftIncidentIdSet = new HashSet<>();

            for (ProcessInstance instance : processInstances) {
                String businessId = instance.getBusinessId();
                if (businessId == null) {
                    log.debug("Skipping PI with null businessId | piId={}", instance.getId());
                    continue;
                }

                Map<String, Object> imCriteria = new HashMap<>();
                imCriteria.put("incidentId", businessId);
                imCriteria.put("incidentSubType", "Theft");
                imCriteria.put("tenantId", "in");

                Object response = searchImServiceTicket(imCriteria, requestInfo);

                if (response == null) {
                    log.debug("No IM response for businessId={}", businessId);
                    continue;
                }

                IncidentResponse incidentResponse =
                        mapper.convertValue(response, IncidentResponse.class);

                if (incidentResponse.getIncidentWrappers() == null) {
                    log.debug("No incident wrappers for businessId={}", businessId);
                    continue;
                }

                incidentResponse.getIncidentWrappers().forEach(wrapper -> {
                    if (wrapper.getIncident() != null &&
                            wrapper.getIncident().getIncidentId() != null) {
                        theftIncidentIdSet.add(wrapper.getIncident().getIncidentId());
                    }
                    if (wrapper.getIncident() != null && wrapper.getIncident().getIncidentId() != null
                            && wrapper.getIncident().getApplicationStatus()!=null && wrapper.getIncident().getApplicationStatus().equals("PENDINGFORASSIGNMENT")) {
                        theftIncidentAssignmentKibanaSet.add(wrapper.getIncident().getIncidentId());
                    }
                });
            }

            int beforeFilter = processInstances.size();

            // Filtrer que les process instances dont leur business service subtype est THEFT
            processInstances =
                    processInstances.stream()
                            .filter(pi -> theftIncidentIdSet.contains(pi.getBusinessId()))
                            .collect(Collectors.toList());

            // Filtrer que les process instances dont leur recent status est PENDINGFORASSIGNMENT, donc mettre a jour ES avec PENDINGFORASSIGNMENT_THEFT
            processInstancesKibana =
                    processInstances.stream()
                            .filter(pi -> theftIncidentAssignmentKibanaSet.contains(pi.getBusinessId()))
                            .collect(Collectors.toList());

            log.info("THEFT filter applied | before={}, after={}", beforeFilter, processInstances.size());
        }

        /* 4) mise à jour du state + récupération des assignes si nécessaire */
        for (ProcessInstance pi : processInstances) {

            log.debug("Processing PI | id={}, businessId={}", pi.getId(), pi.getBusinessId());

            BusinessServiceStateMigration migration =
                    stateMap.get(pi.getBusinessService() + "_" + targetState);

            if (migration == null) {
                log.warn("No migration found for businessService={}, targetState={}",
                        pi.getBusinessService(), targetState);
                continue;
            }

            /* Cas SPARE_PART_NEEDED : récupérer les assignes depuis PENDING_RESOLUTION */
            if (copyAssignesFromPendingResolution && pi.getBusinessId() != null) {
                // Trouver le assign owner pour le ticket. Pour ca trouver la liste des process instance pour le businessId, ensuite recuperer le process instance qui est a PENDINGRESOLUTION, ensuite recuperer
                // le uuid du assignee dans la table eg_wf_assignee_v2
                BusinessServiceStateMigration pendingResolutionMigration =
                        stateMap.get(pi.getBusinessService() + "_PENDINGRESOLUTION");

                // Rechercher le process instance PENDINGRESOLUTION qui correspond au businessId pour pouvoir recuperer le assignee et le mettre dans PENDING_RESOLUTION_SPARE_PART_NEEDED
                if (pendingResolutionMigration != null) {

                    log.debug("Searching PENDINGRESOLUTION PI for businessId={}", pi.getBusinessId());

                    // Rechercher le process instance ayant comme statusid PENDINGRESOLUTION
                    ProcessInstanceSearchCriteria searchByBusinessId = new ProcessInstanceSearchCriteria();
                    searchByBusinessId.setTenantId("in");
                    searchByBusinessId.setHistory(true);
                    searchByBusinessId.setBusinessIds(Collections.singletonList(pi.getBusinessId()));
                    searchByBusinessId.setStatus(
                            Collections.singletonList(pendingResolutionMigration.getStateUuid())
                    );

                    List<ProcessInstance> existingInstances =
                            searchProcessInstanceMigration(requestInfo, searchByBusinessId);

                    if (existingInstances != null && !existingInstances.isEmpty()) {

                        // Recuperer le assignee dans le pi trouve
                        Optional<ProcessInstance> mostRecent =
                                existingInstances.stream()
                                        .filter(process -> process.getAuditDetails() != null)
                                        .max(Comparator.comparingLong(
                                                process -> process.getAuditDetails().getLastModifiedTime()
                                        ));

                        if (mostRecent.isPresent()) {
                            ProcessInstance latest = mostRecent.get();

                            pi.setAssignes(latest.getAssignes());

                            log.info("Copied {} assignees from PI {} to PI {}",
                                    latest.getAssignes() != null ? latest.getAssignes().size() : 0,
                                    latest.getId(), pi.getId());

                            // Ajouter le processInstanceId de l'ancien SPARE_PART_NEEDED a eg_wf_assignee table. Comme ca apres Mise a jour status vers PENDING_RESOLUTION_SPARE_PART_NEEDED, celui ci aura un assignee
                            ProcessInstanceRequest processInstanceRequest =
                                    new ProcessInstanceRequest(requestInfo, Collections.singletonList(pi));

                            producer.push(config.getAddWfAssigneeTopic(), processInstanceRequest);
                        }
                    } else {
                        log.debug("No PENDINGRESOLUTION PI found for businessId={}", pi.getBusinessId());
                    }

                    // Pour les tickets PENDING_ASSIGNMENT_SPARE_PART_NEEDED, voir si PENDING_ASSIGNMENT_SPARE_PART_NEEDED est le current status pour le ticket. Si oui alors update ES avec PENDING_RESOLUTION_SPARE_PART_NEEDED status
                    Map<String, Object> imCriteria = new HashMap<>();
                    imCriteria.put("incidentId", pi.getBusinessId());
                    imCriteria.put("tenantId", "in");

                    Object response = searchImServiceTicket(imCriteria, requestInfo);

                    if (response == null) {
                        log.debug("No IM response for businessId={}", pi.getBusinessId());
                        continue;
                    }

                    IncidentResponse incidentResponse =
                            mapper.convertValue(response, IncidentResponse.class);

                    if (incidentResponse.getIncidentWrappers() == null) {
                        log.debug("No incident wrappers for businessId={}", pi.getBusinessId());
                        continue;
                    }

                    // Filtrer que les process instances dont leur recent status est PENDING_ASSIGNMENT_SPARE_PART_NEEDED, donc mettre a jour ES avec PENDING_RESOLUTION_SPARE_PART_NEEDED et currentprocessInstance
                    incidentResponse.getIncidentWrappers().forEach(wrapper -> {
                        if (wrapper.getIncident() != null && wrapper.getIncident().getIncidentId() != null
                                && wrapper.getIncident().getApplicationStatus()!=null && wrapper.getIncident().getApplicationStatus().equals("PENDING_ASSIGNMENT_SPARE_PART_NEEDED")) {
                            theftIncidentSParePartKibanaSet.add(wrapper.getIncident().getIncidentId());
                        }
                    });
                }
            }

            // Mis a jour avec le state target
            pi.setState(migration.getStateObject());
            pi.setStateSla(migration.getStateSla());

            ProcessInstanceRequest processInstanceRequest = new ProcessInstanceRequest(requestInfo, Collections.singletonList(pi));
            producer.push(config.getUpdateProcessInstanceTopic(), processInstanceRequest);

            log.info("PI updated | id={}, newState={}", pi.getId(), migration.getStateObject().getState());
        }

        if (copyAssignesFromPendingResolution ) {
            processInstancesKibana =
                    processInstances.stream()
                            .filter(pi -> theftIncidentSParePartKibanaSet.contains(pi.getBusinessId()))
                            .collect(Collectors.toList());
        }

        log.info("updateProcessInstances completed | totalUpdated={}", processInstances.size());

        return processInstancesKibana;
    }


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
