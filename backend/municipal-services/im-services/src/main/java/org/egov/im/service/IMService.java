package org.egov.im.service;


import org.egov.common.contract.request.RequestInfo;

import org.egov.im.config.IMConfiguration;
import org.egov.im.producer.Producer;
import org.egov.im.repository.IMRepository;
import org.egov.im.util.MDMSUtils;
import org.egov.im.validator.ServiceRequestValidator;
import org.egov.im.web.models.IncidentWrapper;
import org.egov.im.web.controllers.*;
import org.egov.im.web.models.RequestSearchCriteria;
import org.egov.im.web.models.IncidentRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.CollectionUtils;

import java.util.*;

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


    @Autowired
    public IMService(EnrichmentService enrichmentService, UserService userService, WorkflowService workflowService,
                      ServiceRequestValidator serviceRequestValidator, ServiceRequestValidator validator, Producer producer,
                      IMConfiguration config, IMRepository repository, MDMSUtils mdmsUtils) {
        this.enrichmentService = enrichmentService;
        this.userService = userService;
        this.workflowService = workflowService;
        this.serviceRequestValidator = serviceRequestValidator;
        this.validator = validator;
        this.producer = producer;
        this.config = config;
        this.repository = repository;
        this.mdmsUtils = mdmsUtils;
    }


    /**
     * Creates a complaint in the system
     * @param request The service request containg the complaint information
     * @return
     */
    public IncidentRequest create(IncidentRequest request){
        String tenantId = request.getIncident().getTenantId();
        Object mdmsData = mdmsUtils.mDMSCall(request);
        validator.validateCreate(request, mdmsData);
        enrichmentService.enrichCreateRequest(request);
        workflowService.updateWorkflowStatus(request);
        producer.push(tenantId,config.getCreateTopic(),request);
        producer.push(tenantId,config.getCreateTopicIndexer(),request);
        return request;
    }


    /**
     * Searches the complaints in the system based on the given criteria
     * @param requestInfo The requestInfo of the search call
     * @param criteria The search criteria containg the params on which to search
     * @return
     */
    public List<IncidentWrapper> search(RequestInfo requestInfo, RequestSearchCriteria criteria){
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
        String tenantId = request.getIncident().getTenantId();
        Object mdmsData = mdmsUtils.mDMSCall(request);
        validator.validateUpdate(request, mdmsData);
        enrichmentService.enrichUpdateRequest(request);
        workflowService.updateWorkflowStatus(request);
        producer.push(tenantId,config.getUpdateTopic(),request);
        producer.push(tenantId,config.getUpdateTopicIndexer(),request);

        return request;
    }

    /**
     * Returns the total number of comaplaints matching the given criteria
     * @param requestInfo The requestInfo of the search call
     * @param criteria The search criteria containg the params for which count is required
     * @return
     */
    public Integer count(RequestInfo requestInfo, RequestSearchCriteria criteria){
        criteria.setIsPlainSearch(false);
        Integer count = repository.getCount(criteria);
        return count;
    }


    public List<IncidentWrapper> plainSearch(RequestInfo requestInfo, RequestSearchCriteria criteria) {
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


	public Map<String, Integer> getDynamicData(String tenantId) {
		
		Map<String,Integer> dynamicData = repository.fetchDynamicData(tenantId);

		return dynamicData;
	}


	public int getComplaintTypes() {
		
		return Integer.valueOf(config.getComplaintTypes());
	}
}
