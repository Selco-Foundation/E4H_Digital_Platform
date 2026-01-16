package org.egov.wf.service;

import org.egov.common.contract.request.RequestInfo;
import org.egov.wf.config.WorkflowConfig;
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
import lombok.extern.slf4j.Slf4j;

import static java.util.Objects.isNull;

import java.util.*;


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


    @Autowired
    public WorkflowService(WorkflowConfig config, TransitionService transitionService,
                           EnrichmentService enrichmentService, WorkflowValidator workflowValidator,
                           StatusUpdateService statusUpdateService, WorKflowRepository workflowRepository,
                           WorkflowUtil util,BusinessServiceRepository businessServiceRepository) {
        this.config = config;
        this.transitionService = transitionService;
        this.enrichmentService = enrichmentService;
        this.workflowValidator = workflowValidator;
        this.statusUpdateService = statusUpdateService;
        this.workflowRepository = workflowRepository;
        this.util = util;
        this.businessServiceRepository = businessServiceRepository;
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
}
