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
        log.info("IMService::create for tenantId={} ",request.getIncident().getTenantId());
        String tenantId = request.getIncident().getTenantId();
        Object mdmsData = mdmsUtils.mDMSCall(request);
        validator.validateCreate(request, mdmsData);
        Boundary boundary = boundaryService.fetchBoundaryFromBoundaryCode(
                request.getRequestInfo(), request.getIncident().getBoundaryCode(), request.getIncident().getTenantId()
        );
        if (boundary == null) {
            throw new CustomException("BOUNDARY_DATA_NOT_FOUND", "Boundary data not found for code " + request.getIncident().getBoundaryCode());
        }
        enrichmentService.enrichCreateRequest(request, boundary);
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
        if (incidentWrappers!=null && !incidentWrappers.isEmpty())
            request.getIncident().setPotentialDuplicate(true);
        else
            request.getIncident().setPotentialDuplicate(false);

        String startingStatus = request.getIncident().getApplicationStatus();
        IncidentRequestWrapper wrapper = IncidentRequestWrapper.builder()
                .incidentRequest(request)
                .indexView(new IndexView())
                .build();
        ProcessInstance updatedProcessInstance = workflowService.updateWorkflowStatus(wrapper, mdmsData);
        ProcessInstance trimmedUpdatedProcessInstance = imUtils.trimRolesFromProcessInstance(updatedProcessInstance);
        producer.push(tenantId,config.getCreateTopic(),wrapper.getIncidentRequest());
        wrapper.setProcessInstance(trimmedUpdatedProcessInstance);
        enrichmentService.enrichFieldsForIndexing(wrapper, boundary);
        producer.push(tenantId,config.getCreateTopicIndexer(),wrapper);
        enrichmentService.enrichFieldsForAuditIndexing(wrapper,startingStatus);
        producer.push(tenantId,config.getAuditCreateTopicIndexer(),wrapper);
        return request;
    }


    /**
     * Searches the complaints in the system based on the given criteria
     * @param requestInfo The requestInfo of the search call
     * @param criteria The search criteria containg the params on which to search
     * @return
     */
    public List<IncidentWrapper> search(RequestInfo requestInfo, RequestSearchCriteria criteria){
        log.info("IMService::search with criteria={}", criteria);
        validator.validateSearch(requestInfo, criteria);

        enrichmentService.enrichSearchRequest(requestInfo, criteria);

        if(criteria.isEmpty())
            return new ArrayList<>();

        if(criteria.getMobileNumber()!=null && CollectionUtils.isEmpty(criteria.getUserIds()))
            return new ArrayList<>();

        criteria.setIsPlainSearch(false);

        List<IncidentWrapper> incidentWrappers = repository.getIncidentWrappers(criteria);

        if(CollectionUtils.isEmpty(incidentWrappers))
            return new ArrayList<>();;

         //to add later
        //userService.enrichUsers(serviceWrappers);
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
        return sortedServiceWrappers;
    }


    /**
     * Updates the complaint (used to forward the complaint from one application status to another)
     * @param request The request containing the complaint to be updated
     * @return
     */
    public IncidentRequest update(IncidentRequest request){
        log.info("IMService::update for tenantId={} incidentId={} currentStatus={}",
                request.getIncident().getTenantId(), request.getIncident().getIncidentId(),
                request.getIncident().getApplicationStatus());
        String tenantId = request.getIncident().getTenantId();
        Object mdmsData = mdmsUtils.mDMSCall(request);
        validator.validateUpdate(request, mdmsData);
        enrichmentService.enrichUpdateRequest(request);
        String startingStatus = request.getIncident().getApplicationStatus();
        IncidentRequestWrapper wrapper = IncidentRequestWrapper.builder()
                .incidentRequest(request)
                .indexView(new IndexView())
                .build();
        ProcessInstance updatedProcessInstance = workflowService.updateWorkflowStatus(wrapper, mdmsData);
        ProcessInstance trimmedUpdatedProcessInstance = imUtils.trimRolesFromProcessInstance(updatedProcessInstance);
        producer.push(tenantId,config.getUpdateTopic(),wrapper.getIncidentRequest());
        wrapper.setProcessInstance(trimmedUpdatedProcessInstance);
        Boundary boundary = boundaryService.fetchBoundaryFromBoundaryCode(
                request.getRequestInfo(), request.getIncident().getBoundaryCode(), request.getIncident().getTenantId()
        );
        enrichmentService.enrichFieldsForIndexing(wrapper, boundary);
        imUtils.updateBusinessService(wrapper,mdmsData);
        producer.push(tenantId,config.getUpdateTopicIndexer(),wrapper);
        enrichmentService.enrichFieldsForAuditIndexing(wrapper,startingStatus);
        producer.push(tenantId,config.getAuditCreateTopicIndexer(),wrapper);
        return request;
    }

    /**
     * Returns the total number of comaplaints matching the given criteria
     * @param requestInfo The requestInfo of the search call
     * @param criteria The search criteria containg the params for which count is required
     * @return
     */
    public Integer count(RequestInfo requestInfo, RequestSearchCriteria criteria){
        log.info("IMService::count with criteria={}", criteria);
        criteria.setIsPlainSearch(false);
        Integer count = repository.getCount(criteria);
        return count;
    }


    public List<IncidentWrapper> plainSearch(RequestInfo requestInfo, RequestSearchCriteria criteria) {
        log.info("IMService::plainSearch with criteria={}", criteria);
        validator.validatePlainSearch(criteria);

        criteria.setIsPlainSearch(true);

        if(criteria.getLimit()==null)
            criteria.setLimit(config.getDefaultLimit());

        if(criteria.getOffset()==null)
            criteria.setOffset(config.getDefaultOffset());

        if(criteria.getLimit()!=null && criteria.getLimit() > config.getMaxLimit())
            criteria.setLimit(config.getMaxLimit());

        List<IncidentWrapper> incidentWrappers = repository.getIncidentWrappers(criteria);

        if(CollectionUtils.isEmpty(incidentWrappers)){
            return new ArrayList<>();
        }

        userService.enrichUsers(incidentWrappers);
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
        return sortedIncidentWrappers;
    }


	public int getComplaintTypes() {
		
		return Integer.valueOf(config.getComplaintTypes());
	}
}
