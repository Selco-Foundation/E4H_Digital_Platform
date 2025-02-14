package org.egov.im.service;

import org.egov.im.config.IMConfiguration;
import org.egov.im.repository.IdGenRepository;
import org.egov.im.util.IMUtils;
import org.egov.im.web.models.*;
import org.egov.im.web.models.Idgen.IdResponse;
import org.egov.im.web.models.workflow.Workflow;
import org.egov.tracer.model.CustomException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import static org.egov.im.util.IMConstants.USERTYPE_CITIZEN;

@Service
public class EnrichmentService {


    private IMUtils utils;

    private IdGenRepository idGenRepository;

    private IMConfiguration config;

    private UserService userService;

    @Autowired
    public EnrichmentService(IMUtils utils, IdGenRepository idGenRepository, IMConfiguration config, UserService userService) {
        this.utils = utils;
        this.idGenRepository = idGenRepository;
        this.config = config;
        this.userService = userService;
    }


    /**
     * Enriches the create request with auditDetails. uuids and custom ids from idGen service
     * @param incidentRequest The create request
     */
    public void enrichCreateRequest(IncidentRequest incidentRequest){

        RequestInfo requestInfo = incidentRequest.getRequestInfo();
        Incident incident = incidentRequest.getIncident();
        Workflow workflow = incidentRequest.getWorkflow();
        String tenantId = incident.getTenantId();

        incident.setAccountId(incidentRequest.getIncident().getReporter().getUuid());        
        incident.setReporterTenant(incidentRequest.getIncident().getReporter().getTenantId());

        incident.setBlock(toCamelCase(incident.getBlock()));
        incident.setDistrict(toCamelCase(incident.getDistrict()));

        userService.callUserService(incidentRequest);

        if(incident.getReporterTenant().equalsIgnoreCase("pg"))
        	incident.setReporterType("CRM");
        else
        	incident.setReporterType("HCR");
        
        AuditDetails auditDetails = utils.getAuditDetails(requestInfo.getUserInfo().getUuid(), incident,true);

        incident.setAuditDetails(auditDetails);
        incident.setId(UUID.randomUUID().toString());

        if(workflow.getVerificationDocuments()!=null){
            workflow.getVerificationDocuments().forEach(document -> {
                document.setId(UUID.randomUUID().toString());
            });
        }

        List<String> customIds = getIdList(requestInfo,tenantId,config.getServiceRequestIdGenName(),config.getServiceRequestIdGenFormat(),1);

        incident.setIncidentId(customIds.get(0));


    }


    /**
     * Enriches the update request (updates the lastModifiedTime in auditDetails0
     * @param incidentRequest The update request
     */
    public void enrichUpdateRequest(IncidentRequest incidentRequest){

        RequestInfo requestInfo = incidentRequest.getRequestInfo();
        Incident incident = incidentRequest.getIncident();
        AuditDetails auditDetails = utils.getAuditDetails(requestInfo.getUserInfo().getUuid(), incident,false);
        incident.setBlock(toCamelCase(incident.getBlock()));
        incident.setDistrict(toCamelCase(incident.getDistrict()));
        incident.setAuditDetails(auditDetails);

        userService.callUserService(incidentRequest);
    }

    /**
     * Enriches the search criteria in case of default search and enriches the userIds from mobileNumber in case of seach based on mobileNumber.
     * Also sets the default limit and offset if none is provided
     * @param requestInfo
     * @param criteria
     */
    public void enrichSearchRequest(RequestInfo requestInfo, RequestSearchCriteria criteria){

        if(criteria.isEmpty() && requestInfo.getUserInfo().getType().equalsIgnoreCase(USERTYPE_CITIZEN)){
            String citizenMobileNumber = requestInfo.getUserInfo().getUserName();
            criteria.setMobileNumber(citizenMobileNumber);
        }

        criteria.setAccountId(requestInfo.getUserInfo().getUuid());

        String tenantId = (criteria.getTenantId()!=null) ? criteria.getTenantId() : requestInfo.getUserInfo().getTenantId();

        if(criteria.getMobileNumber()!=null){
            userService.enrichUserIds(tenantId, criteria);
        }

        if(criteria.getLimit()==null)
            criteria.setLimit(config.getDefaultLimit());

        if(criteria.getOffset()==null)
            criteria.setOffset(config.getDefaultOffset());

        if(criteria.getLimit()!=null && criteria.getLimit() > config.getMaxLimit())
            criteria.setLimit(config.getMaxLimit());

    }


    /**
     * Returns a list of numbers generated from idgen
     *
     * @param requestInfo RequestInfo from the request
     * @param tenantId    tenantId of the city
     * @param idKey       code of the field defined in application properties for which ids are generated for
     * @param idformat    format in which ids are to be generated
     * @param count       Number of ids to be generated
     * @return List of ids generated using idGen service
     */
    private List<String> getIdList(RequestInfo requestInfo, String tenantId, String idKey,
                                   String idformat, int count) {
        List<IdResponse> idResponses = idGenRepository.getId(requestInfo, tenantId, idKey, idformat, count).getIdResponses();

        if (CollectionUtils.isEmpty(idResponses))
            throw new CustomException("IDGEN ERROR", "No ids returned from idgen Service");

        return idResponses.stream()
                .map(IdResponse::getId).collect(Collectors.toList());
    }
    
    public static String toCamelCase(String str)
	{
	    if (str == null || str.isEmpty()) {
        return str;
	    }
		str = new String (str.trim());
		StringBuilder converted = new StringBuilder();

        boolean convertNext = true;
            
        for (char ch : str.toCharArray()) {
        	if (Character.isSpaceChar(ch)){
            convertNext = true;
        	} 
        	else if (convertNext) {
            ch = Character.toTitleCase(ch);
            convertNext = false;
        	} 
        	else {
            ch = Character.toLowerCase(ch);
        	}
        converted.append(ch);
        }
        return converted.toString();
	}


}
