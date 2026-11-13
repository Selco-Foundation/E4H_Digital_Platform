package org.egov.im.validator;

import com.jayway.jsonpath.JsonPath;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.egov.common.contract.request.RequestInfo;
import org.egov.im.config.IMConfiguration;
import org.egov.im.repository.IMRepository;
import org.egov.im.util.HRMSUtil;
import org.egov.im.util.HfServiceDefs;
import org.egov.im.web.models.*;
import org.egov.tracer.model.CustomException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.*;
import java.util.stream.Collectors;

import static org.egov.im.util.IMConstants.*;

@Component
@Slf4j
public class ServiceRequestValidator {


    private IMConfiguration config;

    private IMRepository repository;

    private HRMSUtil hrmsUtil;

    @Autowired
    public ServiceRequestValidator(IMConfiguration config, IMRepository repository, HRMSUtil hrmsUtil) {
        this.config = config;
        this.repository = repository;
        this.hrmsUtil = hrmsUtil;
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
        validateComponentAndObservations(request, mdmsData, errorMap);
        validateIncidentTypeProvided(request, errorMap);
        if(!errorMap.isEmpty())
            throw new CustomException(errorMap);
        validateMDMS(request, mdmsData);
        //validateDepartment(request, mdmsData);
        validateBoundary(request, errorMap);
        if(!errorMap.isEmpty())
            throw new CustomException(errorMap);
    }

    /**
     * incidentType and incidentSubType are mandatory. A facility user ticket sends a component and
     * observations instead - both are then set from the primary observation before validation.
     */
    private void validateIncidentTypeProvided(IncidentRequest request, Map<String, String> errorMap) {
        Incident incident = request.getIncident();
        if (StringUtils.isNotBlank(incident.getComponent())) {
            return;
        }
        if (StringUtils.isBlank(incident.getIncidentType()) || StringUtils.isBlank(incident.getIncidentSubType())) {
            errorMap.put("INVALID_REQUEST", "incidentType and incidentSubType are mandatory when no component is provided");
        }
    }

    /**
     * The component must be an active MDMS Incident.Component and every observation an Incident.ServiceDefs
     * entry of channel HF of that component - their priorities set the ticket priority, and the first one
     * of highest priority sets incidentType/incidentSubType. At least one observation is required; a
     * support request takes exactly one, as each of its observations is a ticket type of its own
     * (Shifting, Uninstall, Reinstall, ...). A component of category THEFT needs the FIR document.
     */
    void validateComponentAndObservations(IncidentRequest request, Object mdmsData, Map<String, String> errorMap) {
        String component = request.getIncident().getComponent();
        List<String> observations = request.getIncident().getObservations();
        if (StringUtils.isEmpty(component)) {
            if (!CollectionUtils.isEmpty(observations)) {
                errorMap.put("INVALID_OBSERVATIONS", "Observations require a component");
            }
            return;
        }
        Object category;
        try {
            List<Object> components = JsonPath.read(mdmsData, MDMS_COMPONENT_SEARCH.replace("{CODE}", component));
            if (CollectionUtils.isEmpty(components)) {
                errorMap.put("INVALID_COMPONENT", "The component: " + component + " is not present in MDMS");
                return;
            }
            category = components.get(0) instanceof Map ? ((Map<?, ?>) components.get(0)).get("category") : null;
        } catch (Exception e) {
            throw new CustomException("JSONPATH_ERROR", "Failed to parse mdms response");
        }
        if (COMPONENT_CATEGORY_THEFT.equals(category)) {
            validateFirDocument(request, component, errorMap);
        }
        if (CollectionUtils.isEmpty(observations)) {
            errorMap.put("OBSERVATION_REQUIRED", "At least one observation is required for component: " + component);
            return;
        }
        Set<String> offered = HfServiceDefs.forComponent(mdmsData, component).stream()
                .map(entry -> String.valueOf(entry.get(HfServiceDefs.SERVICE_CODE)))
                .collect(Collectors.toSet());
        List<String> invalid = observations.stream()
                .filter(code -> !offered.contains(code))
                .collect(Collectors.toList());
        if (!invalid.isEmpty()) {
            errorMap.put("INVALID_OBSERVATIONS",
                    "Observations " + invalid + " are not present in MDMS for component: " + component);
        } else if (COMPONENT_CATEGORY_SUPPORT_REQUEST.equals(category) && new HashSet<>(observations).size() > 1) {
            errorMap.put("INVALID_OBSERVATIONS", "Exactly one observation is allowed for component: " + component);
        }
    }

    /**
     * A theft report must carry the FIR or complaint letter, attached as a workflow document of type
     * FIR - photos may be attached too, so any other document does not count.
     */
    private void validateFirDocument(IncidentRequest request, String component, Map<String, String> errorMap) {
        List<Document> documents = request.getWorkflow() == null ? null : request.getWorkflow().getVerificationDocuments();
        boolean hasFir = !CollectionUtils.isEmpty(documents)
                && documents.stream().anyMatch(document -> document != null
                        && FIR_DOCUMENT_TYPE.equalsIgnoreCase(StringUtils.trim(document.getDocumentType()))
                        && StringUtils.isNotBlank(document.getFileStoreId()));
        if (!hasFir) {
            errorMap.put("FIR_DOCUMENT_MANDATORY",
                    "The FIR or complaint letter (document of type " + FIR_DOCUMENT_TYPE + ") is mandatory for component: " + component);
        }
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
        // incidentType/incidentSubType are no longer mandatory in the payload (a facility user sends a
        // component instead on create) - an update that leaves them out keeps the stored ones
        if (existingIncident != null) {
            if (StringUtils.isBlank(request.getIncident().getIncidentType())) {
                request.getIncident().setIncidentType(existingIncident.getIncidentType());
            }
            if (StringUtils.isBlank(request.getIncident().getIncidentSubType())) {
                request.getIncident().setIncidentSubType(existingIncident.getIncidentSubType());
            }
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
    void validateMDMS(IncidentRequest request, Object mdmsData){
        log.info("serviceRequestValidator::Validating mdms data");
        String incidentType = request.getIncident().getIncidentType();
        String serviceCode = request.getIncident().getIncidentSubType();
        String jsonPath = MDMS_SERVICEDEF_SEARCH.replace("{SERVICEDEF}",serviceCode);

        List<Object> res = null;

        try{
            res = JsonPath.read(mdmsData,jsonPath);
        }
        catch (Exception e){
            throw new CustomException("JSONPATH_ERROR","Failed to parse mdms response");
        }

        // serviceCode is not unique across ServiceDefs (Other, DisconnectedCable, ...): the subtype
        // must be one of the issue type it is sent with
        boolean found = !CollectionUtils.isEmpty(res) && res.stream().anyMatch(entry -> entry instanceof Map
                && StringUtils.equalsIgnoreCase(StringUtils.trim(incidentType),
                        StringUtils.trim(String.valueOf(((Map<?, ?>) entry).get(HfServiceDefs.MENU_PATH)))));
        if(!found)
            throw new CustomException("INVALID_SERVICECODE","The service code: "+serviceCode+" is not present in MDMS for the issue type: "+incidentType);


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
