package org.egov.im.validator;

import com.jayway.jsonpath.JsonPath;
import lombok.RequiredArgsConstructor;
import org.egov.im.config.IMConfiguration;
import org.egov.im.repository.IMRepository;
import org.egov.im.web.models.*;
import org.egov.tracer.model.CustomException;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.*;

import static org.egov.im.util.IMConstants.*;

@RequiredArgsConstructor
@Component
public class ServiceRequestValidator {
    private final IMConfiguration config;
    private final IMRepository repository;


    /**
     * Validates the create request
     * @param request Request for creating the complaint
     * @param mdmsData The master data for im
     */
    public void validateCreate(IncidentRequest request, Object mdmsData){
        Map<String,String> errorMap = new HashMap<>();
        validateUserData(request,errorMap);
        validateMDMS(request, mdmsData);
        if(!errorMap.isEmpty())
            throw new CustomException(errorMap);
    }


    /**
     * Validates if the update request is valid
     * @param request The request to update complaint
     * @param mdmsData The master data for im
     */
    public void validateUpdate(IncidentRequest request, Object mdmsData){

        String id = request.getIncident().getId();
        String tenantId = request.getIncident().getTenantId();
        validateReOpen(request);
        RequestSearchCriteria criteria = RequestSearchCriteria.builder().ids(Collections.singleton(id)).tenantId(tenantId).build();
        criteria.setIsPlainSearch(false);
        List<IncidentWrapper> incidentWrappers = repository.getIncidentWrappers(criteria);

        if(CollectionUtils.isEmpty(incidentWrappers))
            throw new CustomException("INVALID_UPDATE","The record that you are trying to update does not exists");

        // TO DO

    }

    /**
     * Validates the user related data in the complaint
     * @param request The request of creating/updating complaint
     * @param errorMap HashMap to capture any errors
     */
    private void validateUserData(IncidentRequest request,Map<String, String> errorMap){

        RequestInfo requestInfo = request.getRequestInfo();
        if(requestInfo.getUserInfo().getType().equalsIgnoreCase(USERTYPE_EMPLOYEE)){
            User reporter = request.getIncident().getReporter();
            if(reporter == null)
                errorMap.put("INVALID_REQUEST","Reporter object cannot be null");
        }

    }


    /**
     * Validated the master data sent in the request
     * @param request The request of creating/updating complaint
     * @param mdmsData The master data for im
     */
    private void validateMDMS(IncidentRequest request, Object mdmsData){

        String serviceCode = request.getIncident().getIncidentSubType();
        String jsonPath = MDMS_SERVICEDEF_SEARCH.replace("{SERVICEDEF}",serviceCode);

        List<Object> res = null;

        try{
            res = JsonPath.read(mdmsData,jsonPath);
        }
        catch (Exception e){
            throw new CustomException("JSONPATH_ERROR","Failed to parse mdms response");
        }

        if(CollectionUtils.isEmpty(res))
            throw new CustomException("INVALID_SERVICECODE","The service code: "+serviceCode+" is not present in MDMS");


    }


    /**
     *
     * @param request
     * @param mdmsData
     */
//    private void validateDepartment(IncidentRequest request, Object mdmsData){
//
//        String serviceCode = request.getIncident().getIncidentType();
//        List<String> assignes = request.getWorkflow().getAssignes();
//
//        if(CollectionUtils.isEmpty(assignes))
//            return;
//
//        List<String> departments = hrmsUtil.getDepartment(assignes, request.getRequestInfo());
//
//        String jsonPath = MDMS_DEPARTMENT_SEARCH.replace("{SERVICEDEF}",serviceCode);
//
//        List<String> res = null;
//        String departmentFromMDMS;
//
//        try{
//            res = JsonPath.read(mdmsData,jsonPath);
//        }
//        catch (Exception e){
//            throw new CustomException("JSONPATH_ERROR","Failed to parse mdms response for department");
//        }
//
//        if(CollectionUtils.isEmpty(res))
//            throw new CustomException("PARSING_ERROR","Failed to fetch department from mdms data for serviceCode: "+serviceCode);
//        else departmentFromMDMS = res.get(0);
//
//        Map<String, String> errorMap = new HashMap<>();
//
//        if(!departments.contains(departmentFromMDMS))
//            errorMap.put("INVALID_ASSIGNMENT","The application cannot be assigned to employee of department: "+departments.toString());
//
//
//        if(!errorMap.isEmpty())
//            throw new CustomException(errorMap);
//
//    }


    /**
     *
     * @param request
     */
    private void validateReOpen(IncidentRequest request){

        if(!request.getWorkflow().getAction().equalsIgnoreCase(IM_WF_REOPEN))
            return;


        Incident incident = request.getIncident();
        RequestInfo requestInfo = request.getRequestInfo();
        Long lastModifiedTime = incident.getAuditDetails().getLastModifiedTime();

        if(requestInfo.getUserInfo().getType().equalsIgnoreCase(USERTYPE_CITIZEN)){
            if(!requestInfo.getUserInfo().getUuid().equalsIgnoreCase(incident.getAccountId()))
                throw new CustomException("INVALID_ACTION","Not authorized to re-open the complain");
        }

        if(System.currentTimeMillis()-lastModifiedTime > config.getComplainMaxIdleTime())
            throw new CustomException("INVALID_ACTION","Complaint is closed");

    }


    /**
     *
     * @param criteria
     */
    public void validateSearch(RequestInfo requestInfo, RequestSearchCriteria criteria){

        /*
        * Checks if tenatId is provided with the search params
        * */
        if( (criteria.getMobileNumber()!=null 
                || criteria.getIncidentId()!=null || criteria.getIds()!=null
                || criteria.getIncidentType()!=null )
                && criteria.getTenantId()==null)
            throw new CustomException("INVALID_SEARCH","TenantId is mandatory search param");

        validateSearchParam(requestInfo, criteria);

    }


    /**
     * Validates if the user have access to search on given param
     * @param requestInfo
     * @param criteria
     */
    private void validateSearchParam(RequestInfo requestInfo, RequestSearchCriteria criteria){

        if(requestInfo.getUserInfo().getType().equalsIgnoreCase("EMPLOYEE" ) && criteria.isEmpty())
            throw new CustomException("INVALID_SEARCH","Search without params is not allowed");

//        if(requestInfo.getUserInfo().getType().equalsIgnoreCase("EMPLOYEE") && criteria.getTenantId().split("\\.").length == config.getStateLevelTenantIdLength()){
//            throw new CustomException("INVALID_SEARCH", "Employees cannot perform state level searches.");
//        }

        String allowedParamStr = null;

        if(requestInfo.getUserInfo().getType().equalsIgnoreCase("CITIZEN" ))
            allowedParamStr = config.getAllowedCitizenSearchParameters();
        else if(requestInfo.getUserInfo().getType().equalsIgnoreCase("EMPLOYEE" ) || requestInfo.getUserInfo().getType().equalsIgnoreCase("SYSTEM") )
            allowedParamStr = config.getAllowedEmployeeSearchParameters();
        else throw new CustomException("INVALID SEARCH","The userType: "+requestInfo.getUserInfo().getType()+
                    " does not have any search config");

        List<String> allowedParams = Arrays.asList(allowedParamStr.split(","));

        if(criteria.getIncidentType()!=null && !allowedParams.contains("incidentType"))
            throw new CustomException("INVALID SEARCH","Search on incidentType is not allowed");

        if(criteria.getIncidentId()!=null && !allowedParams.contains("incidentId"))
            throw new CustomException("INVALID SEARCH","Search on incidentid is not allowed");

        if(criteria.getApplicationStatus()!=null && !allowedParams.contains("applicationStatus"))
            throw new CustomException("INVALID SEARCH","Search on applicationStatus is not allowed");

        if(criteria.getPhcType()!=null && !allowedParams.contains("phcType"))
            throw new CustomException("INVALID SEARCH","Search on PHCType is not allowed");

        if(criteria.getIds()!=null && !allowedParams.contains("ids"))
            throw new CustomException("INVALID SEARCH","Search on ids is not allowed");

    }

    public void validatePlainSearch(RequestSearchCriteria criteria) {
        if(CollectionUtils.isEmpty(criteria.getTenantIds())){
            throw new CustomException("TENANT_ID_LIST_EMPTY", "Tenant ids not provided for searching.");
        }
    }
}
