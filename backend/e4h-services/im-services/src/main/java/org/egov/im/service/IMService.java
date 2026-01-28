package org.egov.im.service;


import org.egov.common.contract.request.RequestInfo;

import org.egov.im.config.IMConfiguration;
import org.egov.im.producer.Producer;
import org.egov.im.repository.IMRepository;
import org.egov.im.util.IMUtils;
import org.egov.im.util.MDMSUtils;
import org.egov.im.validator.ServiceRequestValidator;
import org.egov.im.web.models.*;
import org.egov.im.web.models.workflow.ProcessInstance;
import org.egov.tracer.model.CustomException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.CollectionUtils;

import java.util.*;

import lombok.extern.slf4j.Slf4j;
@Slf4j
@org.springframework.stereotype.Service
public class IMService {

    private EnrichmentService enrichmentService;

    private UserService userService;

    private WorkflowService workflowService;

    private ServiceRequestValidator serviceRequestValidator;

    private ServiceRequestValidator validator;

    private Producer producer;

    private IMConfiguration config;

    private IMRepository repository;

    private MDMSUtils mdmsUtils;

    private IMUtils imUtils;

    private LocalizationService localizationService;

    private BoundaryService boundaryService;

    @Autowired
    public IMService(
            EnrichmentService enrichmentService, UserService userService, WorkflowService workflowService,
            ServiceRequestValidator serviceRequestValidator, ServiceRequestValidator validator, Producer producer,
            IMConfiguration config, IMRepository repository, MDMSUtils mdmsUtils, IMUtils imUtils,
            LocalizationService localizationService, BoundaryService boundaryService
    ) {
        this.enrichmentService = enrichmentService;
        this.userService = userService;
        this.workflowService = workflowService;
        this.serviceRequestValidator = serviceRequestValidator;
        this.validator = validator;
        this.producer = producer;
        this.config = config;
        this.repository = repository;
        this.mdmsUtils = mdmsUtils;
        this.imUtils = imUtils;
        this.localizationService = localizationService;
        this.boundaryService = boundaryService;
    }


    /**
     * Creates a complaint in the system
     * @param request The service request containg the complaint information
     * @return
     */
    public IncidentRequest create(IncidentRequest request){
        log.trace("IMService::create method invoked");
        log.info("Creating incident for tenantId={}", request.getIncident().getTenantId());
        String tenantId = request.getIncident().getTenantId();
        log.trace("Fetching MDMS data for create request");
        Object mdmsData = mdmsUtils.mDMSCall(request);
        log.trace("Validating create request");
        validator.validateCreate(request, mdmsData);
        log.trace("Fetching boundary from boundaryCode");
        Boundary boundary = boundaryService.fetchBoundaryFromBoundaryCode(
                request.getRequestInfo(), request.getIncident().getBoundaryCode(), request.getIncident().getTenantId()
        );
        if (boundary == null) {
            log.error("Boundary data not found for code: {}", request.getIncident().getBoundaryCode());
            throw new CustomException("BOUNDARY_DATA_NOT_FOUND", "Boundary data not found for code " + request.getIncident().getBoundaryCode());
        }
        log.trace("Enriching create request");
        enrichmentService.enrichCreateRequest(request, boundary);
        log.trace("Checking for potential duplicates");
        RequestSearchCriteria searchCriteria = RequestSearchCriteria.builder()
                .tenantId(request.getIncident().getTenantId())
                .boundaryCode(request.getIncident().getBoundaryCode())
                .applicationStatus(Set.of(
                        "PENDINGFORASSIGNMENT",
                        "PENDINGRESOLUTION",
                        "PENDING_ASSIGNMENT_SPARE_PART_NEEDED",
                        "PENDING_ASSIGNMENT_OUT_OF_WARRANTY",
                        "PENDING_RESOLUTION_SPARE_PART_NEEDED",
                        "PENDING_RESOLUTION_OUT_OF_WARRANTY"
                ))
                .incidentType(new HashSet<>(Collections.singletonList(request.getIncident().getIncidentType())))
                .incidentSubType(new HashSet<>(Collections.singletonList(request.getIncident().getIncidentSubType())))
                .build();
        List<IncidentWrapper> incidentWrappers = search(request.getRequestInfo(), searchCriteria);
        boolean isDuplicate = incidentWrappers != null && !incidentWrappers.isEmpty();
        request.getIncident().setPotentialDuplicate(isDuplicate);
        log.debug("Potential duplicate check completed, isDuplicate={}", isDuplicate);

        String startingStatus = request.getIncident().getApplicationStatus();
        log.info("Updating workflow status for incident creation");
        IncidentRequestWrapper wrapper = IncidentRequestWrapper.builder()
                .incidentRequest(request)
                .indexView(new IndexView())
                .build();
        ProcessInstance updatedProcessInstance = workflowService.updateWorkflowStatus(wrapper, mdmsData);
        ProcessInstance trimmedUpdatedProcessInstance = imUtils.trimRolesFromProcessInstance(updatedProcessInstance);
        log.trace("Publishing incident to create topic");
        producer.push(tenantId,config.getCreateTopic(),wrapper.getIncidentRequest());
        wrapper.setProcessInstance(trimmedUpdatedProcessInstance);
        log.trace("Enriching fields for indexing");
        enrichmentService.enrichFieldsForIndexing(wrapper, boundary);
        log.trace("Publishing incident to indexer topic");
        producer.push(tenantId,config.getCreateTopicIndexer(),wrapper);
        log.trace("Enriching fields for audit indexing");
        enrichmentService.enrichFieldsForAuditIndexing(wrapper,startingStatus);
        log.trace("Publishing incident to audit indexer topic");
        producer.push(tenantId,config.getAuditCreateTopicIndexer(),wrapper);
        log.info("Incident created successfully with incidentId={}", request.getIncident().getIncidentId());
        return request;
    }


    /**
     * Searches the complaints in the system based on the given criteria
     * @param requestInfo The requestInfo of the search call
     * @param criteria The search criteria containg the params on which to search
     * @return
     */
    public List<IncidentWrapper> search(RequestInfo requestInfo, RequestSearchCriteria criteria){
        log.trace("IMService::search method invoked");
        log.info("Searching incidents with criteria tenantId={}", criteria.getTenantId());
        log.trace("Validating search criteria");
        validator.validateSearch(requestInfo, criteria);

        log.trace("Enriching search request");
        enrichmentService.enrichSearchRequest(requestInfo, criteria);

        if(criteria.isEmpty()) {
            log.debug("Search criteria is empty, returning empty list");
            return new ArrayList<>();
        }

        if(criteria.getMobileNumber()!=null && CollectionUtils.isEmpty(criteria.getUserIds())) {
            log.debug("Mobile number provided but no userIds found, returning empty list");
            return new ArrayList<>();
        }

        criteria.setIsPlainSearch(false);
        log.trace("Fetching incidents from repository");
        List<IncidentWrapper> incidentWrappers = repository.getIncidentWrappers(criteria);
        log.debug("Found {} incidents from repository", incidentWrappers != null ? incidentWrappers.size() : 0);

        if(CollectionUtils.isEmpty(incidentWrappers)) {
            log.debug("No incidents found, returning empty list");
            return new ArrayList<>();
        }

         //to add later
        //userService.enrichUsers(serviceWrappers);
        log.trace("Enriching workflow for incidents");
        List<IncidentWrapper> enrichedServiceWrappers = workflowService.enrichWorkflow(requestInfo,incidentWrappers);
        log.debug("Sorting {} incidents by createdTime desc", enrichedServiceWrappers.size());
        Map<Long, List<IncidentWrapper>> sortedWrappers = new TreeMap<>(Collections.reverseOrder());
        for(IncidentWrapper svc : enrichedServiceWrappers){
            if(sortedWrappers.containsKey(svc.getIncident().getAuditDetails().getCreatedTime())){
                sortedWrappers.get(svc.getIncident().getAuditDetails().getCreatedTime()).add(svc);
            }else{
                List<IncidentWrapper> incidentWrapperList = new ArrayList<>();
                incidentWrapperList.add(svc);
                sortedWrappers.put(svc.getIncident().getAuditDetails().getCreatedTime(), incidentWrapperList);
            }
        }
        List<IncidentWrapper> sortedServiceWrappers = new ArrayList<>();
        for(Long createdTimeDesc : sortedWrappers.keySet()){
            sortedServiceWrappers.addAll(sortedWrappers.get(createdTimeDesc));
        }
        log.info("Search completed, returning {} incidents", sortedServiceWrappers.size());
        return sortedServiceWrappers;
    }


    /**
     * Updates the complaint (used to forward the complaint from one application status to another)
     * @param request The request containing the complaint to be updated
     * @return
     */
    public IncidentRequest update(IncidentRequest request){
        log.trace("IMService::update method invoked");
        log.info("Updating incident tenantId={} incidentId={} currentStatus={}",
                request.getIncident().getTenantId(), request.getIncident().getIncidentId(),
                request.getIncident().getApplicationStatus());
        String tenantId = request.getIncident().getTenantId();
        log.trace("Fetching MDMS data for update request");
        Object mdmsData = mdmsUtils.mDMSCall(request);
        log.trace("Validating update request");
        validator.validateUpdate(request, mdmsData);
        log.trace("Enriching update request");
        enrichmentService.enrichUpdateRequest(request);
        String startingStatus = request.getIncident().getApplicationStatus();
        log.info("Updating workflow status for incident update");
        IncidentRequestWrapper wrapper = IncidentRequestWrapper.builder()
                .incidentRequest(request)
                .indexView(new IndexView())
                .build();
        ProcessInstance updatedProcessInstance = workflowService.updateWorkflowStatus(wrapper, mdmsData);
        ProcessInstance trimmedUpdatedProcessInstance = imUtils.trimRolesFromProcessInstance(updatedProcessInstance);
        log.trace("Publishing incident to update topic");
        producer.push(tenantId,config.getUpdateTopic(),wrapper.getIncidentRequest());
        wrapper.setProcessInstance(trimmedUpdatedProcessInstance);
        log.trace("Fetching boundary for indexing");
        Boundary boundary = boundaryService.fetchBoundaryFromBoundaryCode(
                request.getRequestInfo(), request.getIncident().getBoundaryCode(), request.getIncident().getTenantId()
        );
        log.trace("Enriching fields for indexing");
        enrichmentService.enrichFieldsForIndexing(wrapper, boundary);
        log.trace("Updating business service");
        imUtils.updateBusinessService(wrapper,mdmsData);
        log.trace("Publishing incident to indexer topic");
        producer.push(tenantId,config.getUpdateTopicIndexer(),wrapper);
        log.trace("Enriching fields for audit indexing");
        enrichmentService.enrichFieldsForAuditIndexing(wrapper,startingStatus);
        log.trace("Publishing incident to audit indexer topic");
        producer.push(tenantId,config.getAuditCreateTopicIndexer(),wrapper);
        log.info("Incident updated successfully with incidentId={}", request.getIncident().getIncidentId());
        return request;
    }

    /**
     * Returns the total number of comaplaints matching the given criteria
     * @param requestInfo The requestInfo of the search call
     * @param criteria The search criteria containg the params for which count is required
     * @return
     */
    public Integer count(RequestInfo requestInfo, RequestSearchCriteria criteria){
        log.trace("IMService::count method invoked");
        log.info("Counting incidents with criteria tenantId={}", criteria.getTenantId());
        criteria.setIsPlainSearch(false);
        log.trace("Fetching count from repository");
        Integer count = repository.getCount(criteria);
        log.info("Count query completed, result={}", count);
        return count;
    }


    public List<IncidentWrapper> plainSearch(RequestInfo requestInfo, RequestSearchCriteria criteria) {
        log.trace("IMService::plainSearch method invoked");
        log.info("Plain searching incidents with criteria tenantId={}", criteria.getTenantId());
        log.trace("Validating plain search criteria");
        validator.validatePlainSearch(criteria);

        criteria.setIsPlainSearch(true);
        log.debug("Setting default limit and offset if not provided");
        if(criteria.getLimit()==null)
            criteria.setLimit(config.getDefaultLimit());

        if(criteria.getOffset()==null)
            criteria.setOffset(config.getDefaultOffset());

        if(criteria.getLimit()!=null && criteria.getLimit() > config.getMaxLimit())
            criteria.setLimit(config.getMaxLimit());

        log.trace("Fetching incidents from repository");
        List<IncidentWrapper> incidentWrappers = repository.getIncidentWrappers(criteria);
        log.debug("Found {} incidents from repository", incidentWrappers != null ? incidentWrappers.size() : 0);

        if(CollectionUtils.isEmpty(incidentWrappers)){
            log.debug("No incidents found, returning empty list");
            return new ArrayList<>();
        }

        log.trace("Enriching users for incidents");
        userService.enrichUsers(incidentWrappers);
        log.trace("Enriching workflow for incidents");
        List<IncidentWrapper> enrichedServiceWrappers = workflowService.enrichWorkflow(requestInfo, incidentWrappers);

        log.debug("Sorting {} incidents by createdTime desc", enrichedServiceWrappers.size());
        Map<Long, List<IncidentWrapper>> sortedWrappers = new TreeMap<>(Collections.reverseOrder());
        for(IncidentWrapper svc : enrichedServiceWrappers){
            if(sortedWrappers.containsKey(svc.getIncident().getAuditDetails().getCreatedTime())){
                sortedWrappers.get(svc.getIncident().getAuditDetails().getCreatedTime()).add(svc);
            }else{
                List<IncidentWrapper> serviceWrapperList = new ArrayList<>();
                serviceWrapperList.add(svc);
                sortedWrappers.put(svc.getIncident().getAuditDetails().getCreatedTime(), serviceWrapperList);
            }
        }
        List<IncidentWrapper> sortedIncidentWrappers = new ArrayList<>();
        for(Long createdTimeDesc : sortedWrappers.keySet()){
        	sortedIncidentWrappers.addAll(sortedWrappers.get(createdTimeDesc));
        }
        log.info("Plain search completed, returning {} incidents", sortedIncidentWrappers.size());
        return sortedIncidentWrappers;
    }


	public int getComplaintTypes() {
		
		return Integer.valueOf(config.getComplaintTypes());
	}
}
