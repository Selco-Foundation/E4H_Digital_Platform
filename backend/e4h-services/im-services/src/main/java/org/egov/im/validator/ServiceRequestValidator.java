package org.egov.im.validator;

import com.jayway.jsonpath.JsonPath;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.egov.common.contract.request.RequestInfo;
import org.egov.im.config.IMConfiguration;
import org.egov.im.repository.IMRepository;
import org.egov.im.util.HRMSUtil;
import org.egov.im.web.models.*;
import org.egov.tracer.model.CustomException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.*;

import static org.egov.im.util.IMConstants.*;
import org.egov.im.service.SLAService;
import org.egov.im.service.WorkflowService;
import org.egov.im.util.JurisdictionAuthorizer;
import org.egov.im.web.models.workflow.Action;
import org.egov.im.web.models.workflow.BusinessService;
import org.egov.im.web.models.workflow.State;
import org.springframework.context.annotation.Lazy;

@Component
@Slf4j
public class ServiceRequestValidator {


    private IMConfiguration config;

    private IMRepository repository;

    private HRMSUtil hrmsUtil;

    private final JurisdictionAuthorizer jurisdictionAuthorizer;

    private final WorkflowService workflowService;

    private final SLAService slaService;

    @Autowired
    public ServiceRequestValidator(IMConfiguration config, IMRepository repository, HRMSUtil hrmsUtil,
                                   JurisdictionAuthorizer jurisdictionAuthorizer,
                                   @Lazy WorkflowService workflowService, @Lazy SLAService slaService) {
        this.config = config;
        this.repository = repository;
        this.hrmsUtil = hrmsUtil;
        this.jurisdictionAuthorizer = jurisdictionAuthorizer;
        this.workflowService = workflowService;
        this.slaService = slaService;
    }

    /**
     * Refuses an action the caller may not take where this ticket is.
     *
     * <p>Workflow decides which roles may take an action; HRMS decides where a person holds a role.
     * Neither sees the other, so without this a Tech POC in one state is offered - and allowed -
     * Tech POC actions on another state's ticket. Hiding the ticket from their inbox does not help:
     * a deep link, a search result or a notification all reach it.
     *
     * @param boundaryCode must be the STORED boundary on update. The request body is not a source
     *                     here - a check against the client's own value is defeated by sending a
     *                     boundary you do hold and acting on a ticket anywhere.
     */
    private void validateJurisdiction(IncidentRequest request, String currentState, String boundaryCode) {
        if (request.getWorkflow() == null || !StringUtils.isNotBlank(request.getWorkflow().getAction())) {
            return;
        }
        String action = request.getWorkflow().getAction();
        String tenantId = request.getIncident().getTenantId();

        List<String> allowedRoles;
        try {
            Priority priority = slaService.getPriorityFromIMPriorityTable(request.getIncident());
            BusinessService businessService = workflowService.getBusinessService(request, priority);
            allowedRoles = rolesForAction(businessService, currentState, action);
        } catch (Exception e) {
            // Fail closed: without the action's roles there is nothing to check against, and
            // allowing the transition would make the gate only as strong as this lookup.
            log.error("Could not resolve the roles for action {} on state {}; refusing", action, currentState, e);
            throw new CustomException("JURISDICTION_CHECK_UNAVAILABLE",
                    "Could not establish who may take this action. Please retry.");
        }

        if (allowedRoles.isEmpty()) {
            // Workflow owns which actions exist; an unknown one is its error to report, not ours.
            log.debug("No roles found for action {} on state {}; leaving it to workflow", action, currentState);
            return;
        }
        if (isMachineDriven(allowedRoles)) {
            log.debug("Action {} is machine-driven; no jurisdiction check", action);
            return;
        }
        if (!jurisdictionAuthorizer.mayActAt(request.getRequestInfo(), allowedRoles, boundaryCode, tenantId)) {
            throw new CustomException("ACTION_OUTSIDE_JURISDICTION",
                    "You do not hold a role that can take this action where this ticket is.");
        }
    }

    /** True when every role the action allows is one no real user holds. */
    private boolean isMachineDriven(List<String> allowedRoles) {
        List<String> machineRoles = config.getMachineRoles();
        return allowedRoles.stream()
                .allMatch(role -> machineRoles.stream().anyMatch(role::equalsIgnoreCase));
    }

    private List<String> rolesForAction(BusinessService businessService, String currentState, String action) {
        if (businessService == null || CollectionUtils.isEmpty(businessService.getStates())) {
            return Collections.emptyList();
        }
        for (State state : businessService.getStates()) {
            if (state == null || CollectionUtils.isEmpty(state.getActions())) {
                continue;
            }
            // A start action has no current state on the ticket, so match on the action alone there.
            boolean stateMatches = !StringUtils.isNotBlank(currentState)
                    || currentState.equalsIgnoreCase(state.getApplicationStatus())
                    || currentState.equalsIgnoreCase(state.getState());
            if (!stateMatches) {
                continue;
            }
            for (Action candidate : state.getActions()) {
                if (candidate != null && action.equalsIgnoreCase(candidate.getAction())
                        && !CollectionUtils.isEmpty(candidate.getRoles())) {
                    return candidate.getRoles();
                }
            }
        }
        return Collections.emptyList();
    }


    /**
     * Validates the create request
     * @param request Request for creating the complaint
     * @param mdmsData The master data for im
     */
    public void validateCreate(IncidentRequest request, Object mdmsData){
        log.info("serviceRequestValidator::Validating incident create request");
        Map<String,String> errorMap = new HashMap<>();
        validateUserData(request,errorMap);
        //validateSource(request.getService().getSource());
        validateMDMS(request, mdmsData);
        //validateDepartment(request, mdmsData);
        validateBoundary(request, errorMap);
        if(!errorMap.isEmpty())
            throw new CustomException(errorMap);
        // On create the payload's boundary is the thing being validated, so checking it is correct -
        // deliberately unlike update, where the stored value is the only trustworthy one.
        validateJurisdiction(request, null, request.getIncident().getBoundaryCode());
    }


    /**
     * Validates if the update request is valid
     * @param request The request to update complaint
     * @param mdmsData The master data for im
     */
    public void validateUpdate(IncidentRequest request, Object mdmsData){
        log.info("serviceRequestValidator::Validating incident update request");
        String id = request.getIncident().getId();
        String tenantId = request.getIncident().getTenantId();
        //validateSource(request.getService().getSource());
        //validateMDMS(request, mdmsData);
        //validateDepartment(request, mdmsData);
        validateReOpen(request);
        validateOnHold(request);
        RequestSearchCriteria criteria = RequestSearchCriteria.builder().ids(Collections.singleton(id)).tenantId(tenantId).build();
        criteria.setIsPlainSearch(false);
        List<IncidentWrapper> incidentWrappers = repository.getIncidentWrappers(criteria);

        if(CollectionUtils.isEmpty(incidentWrappers))
            throw new CustomException("INVALID_UPDATE","The record that you are trying to update does not exists");

        /*
         * Preserve existing warrantyStatus for updates when it is not explicitly
         * provided in the incoming request. This avoids resetting OUT_OF_WARRANTY
         * tickets back to WITHIN_WARRANTY on subsequent workflow actions.
         */
        Incident existingIncident = incidentWrappers.get(0).getIncident();
        if (request.getIncident().getWarrantyStatus() == null && existingIncident != null) {
            request.getIncident().setWarrantyStatus(existingIncident.getWarrantyStatus());
        }

        // The stored boundary and the stored state, never the request body's: a check against what
        // the caller sent is defeated by sending a boundary they do hold.
        if (existingIncident != null) {
            validateJurisdiction(request, existingIncident.getApplicationStatus(),
                    existingIncident.getBoundaryCode());
        }
    }

    /**
     * Validates the user related data in the complaint
     * @param request The request of creating/updating complaint
     * @param errorMap HashMap to capture any errors
     */
    private void validateUserData(IncidentRequest request,Map<String, String> errorMap){
        log.info("serviceRequestValidator::Validating user data");
        RequestInfo requestInfo = request.getRequestInfo();
        String accountId = request.getIncident().getAccountId();

        /*if(requestInfo.getUserInfo().getType().equalsIgnoreCase(USERTYPE_CITIZEN)
            && StringUtils.isEmpty(accountId)){
            errorMap.put("INVALID_REQUEST","AccountId cannot be null");
        }
        else if(requestInfo.getUserInfo().getType().equalsIgnoreCase(USERTYPE_CITIZEN)
                && !StringUtils.isEmpty(accountId)
                && !accountId.equalsIgnoreCase(requestInfo.getUserInfo().getUuid())){
            errorMap.put("INVALID_ACCOUNTID","The accountId is different from the user logged in");
        }*/

        if(requestInfo.getUserInfo().getType().equalsIgnoreCase(USERTYPE_EMPLOYEE)){
            User reporter = request.getIncident().getReporter();
            if(reporter == null)
                errorMap.put("INVALID_REQUEST","Reporter object cannot be null");
//            else if(reporter.getMobileNumber()==null || reporter.getName()==null)
//                errorMap.put("INVALID_REQUEST","Name and Mobile Number is mandatory in reporter object");
        }

    }


    /**
     * Validated the master data sent in the request
     * @param request The request of creating/updating complaint
     * @param mdmsData The master data for im
     */
    private void validateMDMS(IncidentRequest request, Object mdmsData){
        log.info("serviceRequestValidator::Validating mdms data");
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

    private void validateBoundary(IncidentRequest incidentRequest, Map<String, String> errorMap) {
        if (StringUtils.isEmpty(incidentRequest.getIncident().getBoundaryCode())) {
            errorMap.put("BOUNDARY_CODE_MISSING", "Boundary code not provided to enrich facility details");
        }
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
        log.info("serviceRequestValidator::Validating incident reopen request");
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
     * Validates the ON_HOLD action: a known reason, the government acknowledgement letter,
     * and a comment of at most 500 characters that is mandatory when the reason is Other.
     * @param request The request to update complaint
     */
    void validateOnHold(IncidentRequest request){
        if(!IM_WF_ON_HOLD.equalsIgnoreCase(request.getWorkflow().getAction()))
            return;

        Map<String,String> errorMap = new HashMap<>();
        Workflow workflow = request.getWorkflow();

        String reason = workflow.getOnHoldReason() == null ? null : workflow.getOnHoldReason().getReason();
        if(!ON_HOLD_REASON_FUND_UNAVAILABLE.equals(reason) && !ON_HOLD_REASON_OTHER.equals(reason))
            errorMap.put("INVALID_ON_HOLD_REASON","On hold reason must be one of "
                    + ON_HOLD_REASON_FUND_UNAVAILABLE + ", " + ON_HOLD_REASON_OTHER);

        String comments = workflow.getComments();
        if(ON_HOLD_REASON_OTHER.equals(reason) && StringUtils.isBlank(comments))
            errorMap.put("ON_HOLD_COMMENT_MANDATORY","Comment is mandatory when the on hold reason is Other");
        if(comments != null && comments.length() > ON_HOLD_COMMENT_MAX_LENGTH)
            errorMap.put("ON_HOLD_COMMENT_LENGTH_EXCEEDED","Comment cannot exceed " + ON_HOLD_COMMENT_MAX_LENGTH + " characters");

        boolean hasDocument = !CollectionUtils.isEmpty(workflow.getVerificationDocuments())
                && workflow.getVerificationDocuments().stream()
                        .anyMatch(document -> document != null && StringUtils.isNotBlank(document.getFileStoreId()));
        if(!hasDocument)
            errorMap.put("ON_HOLD_DOCUMENT_MANDATORY","Government acknowledgement letter is mandatory to put the ticket on hold");

        if(!errorMap.isEmpty())
            throw new CustomException(errorMap);
    }


    /**
     *
     * @param criteria
     */
    public void validateSearch(RequestInfo requestInfo, RequestSearchCriteria criteria){
        log.info("serviceRequestValidator::Validating incident search criteria");
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
        log.info("serviceRequestValidator::Validating incident search param");
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

    /**
     * Validates if the source is in the given list configures in application properties
     * @param source
     */
//    private void validateSource(String source){
//
//        List<String> allowedSourceStr = Arrays.asList(config.getAllowedSource().split(","));
//
//        if(!allowedSourceStr.contains(source))
//            throw new CustomException("INVALID_SOURCE","The source: "+source+" is not valid");
//
//    }


    public void validatePlainSearch(RequestSearchCriteria criteria) {
        log.info("serviceRequestValidator::Validating incident plain search criteria");
        if(CollectionUtils.isEmpty(criteria.getTenantIds())){
            throw new CustomException("TENANT_ID_LIST_EMPTY", "Tenant ids not provided for searching.");
        }
    }
}
