package org.egov.field_planner.validator;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.jsonpath.JsonPath;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.egov.common.contract.request.RequestInfo;
import org.egov.common.http.client.ServiceRequestClient;
import org.egov.common.models.project.*;
import org.egov.field_planner.config.FieldPlannerConfiguration;
import org.egov.field_planner.repository.FieldPlannerRepository;
import org.egov.field_planner.service.FieldPlannerService;
import org.egov.field_planner.util.FieldPlannerServiceUtil;
import org.egov.field_planner.util.MDMSUtils;
import org.egov.field_planner.web.models.FieldPlan;
import org.egov.field_planner.web.models.FieldPlanRequest;
import org.egov.field_planner.web.models.FieldPlanSearchCriteria;
import org.egov.field_planner.web.models.FieldPlanSearchRequest;
import org.egov.tracer.model.CustomException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.*;

import static org.egov.field_planner.util.FieldPlannerConstants.*;

@Component
@Slf4j
public class FieldPlannerValidator {

    @Autowired
    FieldPlannerRepository fieldPlannerRepository;

    @Autowired
    private final ServiceRequestClient serviceRequestRepository;

    public static final String START_DATE_SHOULD_BE_LESS_THAN_END_DATE = "Start date should be less than end date";
    public static final String IS_NOT_PRESENT_IN_MDMS = " is not present in MDMS";
    public static final String TENANT_ID_IS_MANDATORY_IN_FIELDPLAN_REQUEST_BODY = "Tenant ID is mandatory in FieldPlan request body";
    public static final String ACTIVITIES_IS_MANDATORY_IN_FIELDPLAN_REQUEST_BODY = "Activities are mandatory in FieldPlan request body";
    public static final String DOES_NOT_EXISTS_FOR_THE_FIELDPLAN = " that you are trying to update does not exists for the FieldPlan ";
    @Autowired
    MDMSUtils mdmsUtils;

    @Autowired
    FieldPlannerConfiguration config;
    private final FieldPlannerServiceUtil fieldPlanServiceUtil;
    @Autowired
    @Qualifier("objectMapper")
    ObjectMapper mapper;

    public FieldPlannerValidator(ServiceRequestClient serviceRequestRepository, FieldPlannerServiceUtil fieldPlanServiceUtil){
        this.serviceRequestRepository = serviceRequestRepository;
        this.fieldPlanServiceUtil = fieldPlanServiceUtil;
    }

    public void validateCreateFieldPlanRequest(FieldPlanRequest request) {
        log.trace("Entering validateCreateFieldPlanRequest method");
        log.info("Validating field plan creation request");
        
        Map<String, String> errorMap = new HashMap<>();
        RequestInfo requestInfo = request.getRequestInfo();

        validateRequestInfo(requestInfo);
        log.debug("Request info validation completed");
        
        validateFieldPlanRequest(request);
        log.debug("Field plan request validation completed");

        validateRequestMDMSData(request, request.getFieldPlans().get(0).getTenantId(), errorMap);
        log.debug("MDMS data validation completed, error count: {}", errorMap.size());

        if (!errorMap.isEmpty()) {
            log.error("Field plan creation request validation failed with {} errors", errorMap.size());
            throw new CustomException(errorMap);
        }
        log.info("Field plan creation request validation successful");
        log.trace("Exiting validateCreateFieldPlanRequest method");
    }

    private void validateFieldPlanRequest(FieldPlanRequest request) {
        log.trace("Entering validateFieldPlanRequest method");
        Map<String, String> errorMap = new HashMap<>();

        if (request.getFieldPlans() == null || request.getFieldPlans().size() == 0) {
            log.error("Field Plans list is empty. Field Plans is mandatory");
            throw new CustomException("FIELDPLAN", "Field Plans are mandatory");
        }

        for (FieldPlan fieldPlan : request.getFieldPlans()) {
            if (fieldPlan == null) {
                log.error("FieldPlan is mandatory in FieldPlans");
                throw new CustomException("FieldPlan", "FieldPlan is mandatory");
            }

            if (fieldPlan.getProjectId() == null) {
                log.error("Project ID is mandatory in FieldPlans");
                throw new CustomException("FieldPlan", "Project ID is mandatory");
            }
            // Get existing fieldPlan with projectID from fieldPlan service
            Project existingProject = getProjectById(request, fieldPlan);
            if (existingProject == null) {
                log.error("Project ID do not exist");
                throw new CustomException("FieldPlan", "Project ID do not exist");
            }
            // Check if fieldPlan dates are within fieldPlan dates
            isFieldPlanWithinProject(existingProject, fieldPlan, errorMap);

            if (StringUtils.isBlank(fieldPlan.getTenantId())) {
                log.error(TENANT_ID_IS_MANDATORY_IN_FIELDPLAN_REQUEST_BODY);
                errorMap.put("TENANT_ID", "Tenant ID is mandatory");
            }
            if (fieldPlan.getActivities() == null || fieldPlan.getActivities().isEmpty()) {
                log.error(ACTIVITIES_IS_MANDATORY_IN_FIELDPLAN_REQUEST_BODY);
                errorMap.put("ACTIVITIES", "Activity is mandatory");
            }
            if ((fieldPlan.getStartDate() != null && fieldPlan.getEndDate() != null && fieldPlan.getEndDate() != 0) && (fieldPlan.getStartDate().compareTo(fieldPlan.getEndDate()) > 0)) {
                log.error(START_DATE_SHOULD_BE_LESS_THAN_END_DATE);
                errorMap.put("INVALID_DATE_ERROR", START_DATE_SHOULD_BE_LESS_THAN_END_DATE);
            }
            if (fieldPlan.getStartDate() != null && fieldPlan.getEndDate() != null && fieldPlan.getEndDate() != 0
                    && fieldPlan.getEndDate().compareTo(Instant.ofEpochMilli(fieldPlan.getStartDate()).plus(Duration.ofDays(1)).toEpochMilli()) < 0) {
                log.error("Start date and end date difference should at least be 1 day.");
                errorMap.put("INVALID_DATE", "Start date and end date difference should at least be 1 day.");
            }
        }

        if (!errorMap.isEmpty()) {
            log.error("Field plan request validation failed with {} errors", errorMap.size());
            throw new CustomException(errorMap);
        }
        log.trace("Exiting validateFieldPlanRequest method");
    }

    private void validateRequestInfo(RequestInfo requestInfo) {
        log.trace("Entering validateRequestInfo method");
        if (requestInfo == null) {
            log.error("Request info is mandatory");
            throw new CustomException("REQUEST_INFO", "Request info is mandatory");
        }
        if (requestInfo.getUserInfo() == null) {
            log.error("UserInfo is mandatory in RequestInfo");
            throw new CustomException("USERINFO", "UserInfo is mandatory");
        }
        if (requestInfo.getUserInfo() != null && StringUtils.isBlank(requestInfo.getUserInfo().getUuid())) {
            log.error("UUID is mandatory in UserInfo");
            throw new CustomException("USERINFO_UUID", "UUID is mandatory");
        }
        log.trace("Exiting validateRequestInfo method");
    }

    /* Validate Project Request MDMS data */
    private void validateRequestMDMSData(FieldPlanRequest request, String tenantId, Map<String, String> errorMap) {
        log.trace("Entering validateRequestMDMSData method");
        String rootTenantId = tenantId.split("\\.")[0];
        log.debug("Validating MDMS data for root tenant: {}", rootTenantId);

        //Get MDMS data using create fieldPlan request and tenantId
        Object mdmsData = mdmsUtils.mDMSCall(request, rootTenantId);

        validateMDMSData(request.getFieldPlans(), mdmsData, errorMap);
        log.debug("MDMS validation completed for tenant: {}", tenantId);
        log.trace("Exiting validateRequestMDMSData method");
    }

    /* Validates the request data against MDMS data */
    private void validateMDMSData(List<FieldPlan> fieldPlans, Object mdmsData, Map<String, String> errorMap) {
        log.trace("Entering validateMDMSData method");
        String mdmsRes = "$.MdmsRes.";
        final String jsonPathForActivities = mdmsRes + MDMS_COMMON_MASTERS_MODULE_NAME + "." + MASTER_ACTIVITIES + ".*.code";
        final String jsonPathForStateInfo = mdmsRes + MDMS_COMMON_MASTERS_MODULE_NAME + "." + MASTER_STATE_INFO + ".*.name";
        final String jsonPathForTenants = mdmsRes + MDMS_TENANT_MODULE_NAME + "." + MASTER_TENANTS + ".*";

        List<Object> activitiesRes = null;
        List<Object> stateInfoRes = null;
        List<Object> tenantRes = null;
        log.debug("Parsing MDMS response data");
        try {
            activitiesRes = JsonPath.read(mdmsData, jsonPathForActivities);
            stateInfoRes = JsonPath.read(mdmsData, jsonPathForStateInfo);
            tenantRes = JsonPath.read(mdmsData, jsonPathForTenants);
        } catch (Exception e) {
            log.error("Error parsing MDMS response", e);
            throw new CustomException("JSONPATH_ERROR", "Failed to parse mdms response");
        }

        for (FieldPlan fieldPlan : fieldPlans) {
            log.trace("Validating field plan against MDMS data");
            Map<String, Object> geographyDetails = fieldPlan.getGeographyDetails();
            List<Map<String, Object>> activities = fieldPlan.getActivities();
            String state = (String)geographyDetails.get("state");
            String mdmsNotPresent = IS_NOT_PRESENT_IN_MDMS;
            
            log.debug("Validating tenant ID: {} against MDMS", fieldPlan.getTenantId());
            if (!StringUtils.isBlank(fieldPlan.getTenantId()) && !tenantRes.contains(fieldPlan.getTenantId())) {
                log.error("Tenant ID: {} not found in MDMS", fieldPlan.getTenantId());
                errorMap.put("INVALID_TENANT", "The tenant: " + fieldPlan.getTenantId() + mdmsNotPresent);
            }
            
            log.debug("Validating state: {} against MDMS", state);
            if (!StringUtils.isBlank(state)) {
                String stateExtracted = fieldPlanServiceUtil.extractStateName(state);
                if (!stateInfoRes.contains(stateExtracted)){
                    log.error("State code: {} not found in MDMS", state);
                    errorMap.put("INVALID_STATE_CODE", "The state code: " + state + mdmsNotPresent);
                }
            }
        }
        log.trace("Exiting validateMDMSData method");
    }

    public Project getProjectById(FieldPlanRequest request, FieldPlan fieldPlan) {
        log.trace("Entering getProjectById method");
        String projectId = fieldPlan.getProjectId();
        log.debug("Fetching project with ID: {} for tenant: {}", projectId, fieldPlan.getTenantId());
        Project project = Project.builder().id(projectId).tenantId(fieldPlan.getTenantId()).build();
        ProjectRequest projectRequest = ProjectRequest.builder().requestInfo(request.getRequestInfo()).projects(List.of(project)).build();
        String url = config.getProjectServiceHost() + config.getProjectServiceSearchUrl()+ "?tenantId="+fieldPlan.getTenantId()+"&offset=0&limit=100";
        log.debug("Calling project service at URL: {}", url);
        Object response = serviceRequestRepository.fetchResult(new StringBuilder(url), projectRequest, Map.class);
        ProjectResponse projectResponse = mapper.convertValue(response, ProjectResponse.class);
        if(projectResponse != null && projectResponse.getProject() !=null && projectResponse.getProject().size() > 0){
            log.debug("Successfully retrieved project with ID: {}", projectId);
            log.trace("Exiting getProjectById method");
            return projectResponse.getProject().get(0);
        }
        log.warn("Project not found with ID: {}", projectId);
        log.trace("Exiting getProjectById method");
        return null;
    }

    public void isFieldPlanWithinProject(Project project, FieldPlan fieldPlan, Map<String, String> errorMap) {
        log.trace("Entering isFieldPlanWithinProject method");
        if (project == null || fieldPlan == null) {
            log.error("Project or FieldPlan is null");
            errorMap.put("FIELDPLAN", "Project or FieldPlan is null");
            log.trace("Exiting isFieldPlanWithinProject method");
            return;
        }

        Long projectStart = project.getStartDate();
        Long projectEnd   = project.getEndDate();
        Long fieldStart   = fieldPlan.getStartDate();
        Long fieldEnd     = fieldPlan.getEndDate();
        log.debug("Validating field plan dates within project dates, project: {}-{}, field plan: {}-{}", 
                projectStart, projectEnd, fieldStart, fieldEnd);

        if (projectStart == null || projectEnd == null) {
            log.error("Project dates are not mandatory");
            errorMap.put("FIELDPLAN_PROJECT", "Project dates are not mandatory");
        }
        if (fieldStart == null || fieldEnd == null) {
            log.error("FieldPlan dates are not mandatory");
            errorMap.put("FIELDPLAN", "FieldPlan dates are not mandatory");
        }

        if (fieldStart < projectStart) {
            log.error("The FieldPlan start date is earlier than the Project start date");
            errorMap.put("FIELDPLAN_STARTDATE", "The FieldPlan start date is earlier than the Project start date");
        }
        if (fieldEnd > projectEnd) {
            log.error("The FieldPlan end date is later than the Project end date");
            errorMap.put("FIELDPLAN_ENDDATE", "The FieldPlan end date is later than the Project end date");
        }
        log.trace("Exiting isFieldPlanWithinProject method");
    }

    /* Validates Update Project request body */
    public void validateUpdateFieldPlanRequest(FieldPlanRequest request) {
        log.trace("Entering validateUpdateFieldPlanRequest method");
        log.info("Validating field plan update request");
        
        Map<String, String> errorMap = new HashMap<>();
        RequestInfo requestInfo = request.getRequestInfo();

        validateRequestInfo(requestInfo);
        log.debug("Request info validation completed");
        
        validateFieldPlanRequest(request);
        log.debug("Field plan request validation completed");
        
        validateMultipleTenantIds(request);
        log.debug("Multiple tenant IDs validation completed");

        for (FieldPlan fieldPlan : request.getFieldPlans()) {
            if (StringUtils.isBlank(fieldPlan.getId())) {
                log.error("Field plan ID is mandatory for update request");
                throw new CustomException("UPDATE_FIELDPLAN", "FieldPlan Id is mandatory");
            }
        }
        log.debug("Field plan IDs validation completed");

        if (!errorMap.isEmpty()) {
            log.error("Field plan update request validation failed with {} errors", errorMap.size());
            throw new CustomException(errorMap);
        }
        log.info("Field plan update request validation successful");
        log.trace("Exiting validateUpdateFieldPlanRequest method");
    }


    /* Validates search FieldPlan request body and parameters*/
    public void validateSearchFieldPlanRequest(FieldPlanSearchRequest request, Integer limit, Integer offset, String tenantId, Long createdFrom, Long createdTo) {
        log.trace("Entering validateSearchFieldPlanRequest method");
        log.info("Validating field plan search request for tenant: {}", tenantId);
        
        Map<String, String> errorMap = new HashMap<>();
        RequestInfo requestInfo = request.getRequestInfo();

        validateRequestInfo(requestInfo);
        log.debug("Request info validation completed");
        
        validateSearchFieldPlanRequestParams(limit, offset, tenantId, createdFrom, createdTo);
        log.debug("Search request parameters validation completed");
        
        validateSearchProjectRequest(request.getFieldPlan(), tenantId, createdFrom);
        log.debug("Search request body validation completed");

        if (!errorMap.isEmpty()) {
            log.error("Field plan search request validation failed with {} errors", errorMap.size());
            throw new CustomException(errorMap);
        }
        log.info("Field plan search request validation successful");
        log.trace("Exiting validateSearchFieldPlanRequest method");
    }

    /* Validates if search Project request parameters are valid */
    private void validateSearchFieldPlanRequestParams(Integer limit, Integer offset, String tenantId, Long createdFrom, Long createdTo) {
        log.trace("Entering validateSearchFieldPlanRequestParams method");
        if (limit == null) {
            log.error("limit is mandatory parameter in Project search");
            throw new CustomException("SEARCH_PROJECT.LIMIT", "limit is mandatory for Project Search");
        }

        if (offset == null) {
            log.error("offset is mandatory parameter in Project search");
            throw new CustomException("SEARCH_PROJECT.OFFSET", "offset is mandatory for Project Search");
        }

        if (StringUtils.isBlank(tenantId)) {
            log.error("tenantId is mandatory parameter in Project search");
            throw new CustomException("SEARCH_PROJECT.TENANT_ID", "tenantId is mandatory for Project Search");
        }

        if ((createdFrom == null || createdFrom == 0) && (createdTo != null && createdTo != 0)) {
            log.error("Created From date is required if Created To date is given");
            throw new CustomException("INVALID_DATE_PARAM", "Created From date is required if Created To date is given");
        }

        if ((createdFrom != null && createdTo != null && createdTo != 0) && (createdFrom.compareTo(createdTo) > 0)) {
            log.error("Created From in Project search parameters should be less than Created To");
            throw new CustomException("INVALID_DATE", "Created From should be less than Created To");
        }
        log.trace("Exiting validateSearchFieldPlanRequestParams method");
    }

    /* Validates Search Project Request body */
    private void validateSearchProjectRequest(FieldPlanSearchCriteria fieldPlan, String tenantId, Long createdFrom) {
        log.trace("Entering validateSearchProjectRequest method");
//        checkFieldPlansIfEmpty(fieldPlans);
        doNullAndEmptyChecks(tenantId, createdFrom, fieldPlan);
//
        if ((fieldPlan.getFromDate() != null && fieldPlan.getToDate() != null && fieldPlan.getToDate() != 0) && (fieldPlan.getFromDate().compareTo(fieldPlan.getToDate()) > 0)) {
            log.error(START_DATE_SHOULD_BE_LESS_THAN_END_DATE);
            throw new CustomException("INVALID_DATE", START_DATE_SHOULD_BE_LESS_THAN_END_DATE);
        }

        if ((fieldPlan.getFromDate() == null || fieldPlan.getFromDate() == 0) && (fieldPlan.getToDate() != null && fieldPlan.getToDate() != 0)) {
            log.error("Start date is required if end date is passed");
            throw new CustomException("INVALID_DATE", "Start date is required if end date is passed");
        }
        log.trace("Exiting validateSearchProjectRequest method");
    }

    private static void checkFieldPlansIfEmpty(List<FieldPlan> fieldPlans) {
        if (fieldPlans == null || fieldPlans.size() == 0) {
            log.error("Fieldplan list is empty. FieldPlans is mandatory");
            throw new CustomException("Fieldplan", "FieldPlans are mandatory");
        }
    }

    private static void doNullAndEmptyChecks(String tenantId, Long createdFrom, FieldPlanSearchCriteria fieldPlan) {
        if (fieldPlan == null) {
            log.error("fieldPlan is mandatory in FieldPlans");
            throw new CustomException("FIELDPLAN", "FieldPlan is mandatory");
        }
        if (StringUtils.isBlank(fieldPlan.getTenantId())) {
            log.error(TENANT_ID_IS_MANDATORY_IN_FIELDPLAN_REQUEST_BODY);
            throw new CustomException("TENANT_ID", "Tenant ID is mandatory");
        }
        if ((fieldPlan.getIds()==null || fieldPlan.getIds().isEmpty()) && (fieldPlan.getProjectId()==null || fieldPlan.getProjectId().isEmpty())
                && (fieldPlan.getStatuses()==null || fieldPlan.getStatuses().isEmpty())
                && (fieldPlan.getFromDate() == null || fieldPlan.getFromDate() == 0)
                && (fieldPlan.getToDate() == null || fieldPlan.getToDate() == 0)
                && (createdFrom == null || createdFrom == 0)) {
            log.error("Any one fieldPlan search field is required for FieldPlan Search");
            throw new CustomException("FIELDPLAN_SEARCH_FIELDS", "Any one fieldplan search field is required");
        }

        if (!fieldPlan.getTenantId().equals(tenantId)) {
            log.error("Tenant Id must be same in URL param as well as project request body");
            throw new CustomException("MULTIPLE_TENANTS", "Tenant Id must be same in URL param and project request");
        }
    }

    /* Validates if all FieldPlans have same tenant Id */
    private void validateMultipleTenantIds(FieldPlanRequest request) {
        log.trace("Entering validateMultipleTenantIds method");
        List<FieldPlan> fieldPlans = request.getFieldPlans();
        String firstTenantId = fieldPlans.get(0).getTenantId();
        log.debug("Validating all field plans have same tenant ID: {}", firstTenantId);
        if (fieldPlans.stream().anyMatch(p -> !p.getTenantId().equals(firstTenantId))) {
            log.error("All fieldplans in FieldPlan request must have same tenant Id");
            throw new CustomException("MULTIPLE_TENANTS", "All fieldplans must have same tenant Id. Please create new request for different tentant id");
        }
        log.trace("Exiting validateMultipleTenantIds method");
    }

    /* Validates projects data in update request against projects data fetched from database */
    public void validateUpdateAgainstDB(List<FieldPlan> fieldPlansFromRequest, List<FieldPlan> fieldPlansFromDB) {
        log.trace("Entering validateUpdateAgainstDB method");
        log.info("Validating {} field plans from request against {} field plans from database", 
                fieldPlansFromRequest.size(), fieldPlansFromDB.size());
        if (CollectionUtils.isEmpty(fieldPlansFromDB)) {
            log.error("The fieldplan records that you are trying to update does not exists in the system");
            throw new CustomException("INVALID_FIELDPLAN_MODIFY", "The records that you are trying to update does not exists in the system");
        }
        Long currentTimestamp = Instant.now().toEpochMilli();
        log.debug("Current timestamp: {}", currentTimestamp);
        // Calculate the timestamp for midnight (12:00 AM) of the next date, plus 24 hours, in UTC
        Instant nextDateInstantUTC = Instant.ofEpochMilli(currentTimestamp)
                .plus(Duration.ofDays(1))  // Add 1 day to get the next date
                .atZone(ZoneOffset.UTC)
                .toLocalDate()  // Extract the date part
                .atStartOfDay(ZoneOffset.UTC)  // Set the time to midnight
                .toInstant()// Convert to Instant
                .plus(Duration.ofDays(1));  // Add 1 day

        Long nextDateTimestampUTC = nextDateInstantUTC.toEpochMilli();
        for (FieldPlan fieldPlan : fieldPlansFromRequest) {
            FieldPlan fieldPlanFromDB = fieldPlansFromDB.stream().filter(p -> p.getId().equals(fieldPlan.getId())).findFirst().orElse(null);

            if (fieldPlanFromDB == null) {
                log.error("The fieldplan id " + fieldPlan.getId() + " that you are trying to update does not exists for the fieldplan");
                throw new CustomException("INVALID_FIELDPLAN_MODIFY", "The fieldplan id " + fieldPlan.getId() + " that you are trying to update does not exists for the fieldplan");
            }

            validateStartDateAndEndDateAgainstDB(fieldPlan, fieldPlanFromDB, currentTimestamp, nextDateTimestampUTC);

//            validateUpdateAddressAgainstDB(project, projectFromDB);
        }
        log.info("Field plan update validation against database completed successfully");
        log.trace("Exiting validateUpdateAgainstDB method");
    }

    private void validateStartDateAndEndDateAgainstDB(FieldPlan fieldPlan, FieldPlan fieldPlanFromDB, Long currentTimestamp, Long nextDateTimestampUTC) {
        log.trace("Entering validateStartDateAndEndDateAgainstDB method for field plan ID: {}", fieldPlan.getId());
        log.debug("Validating field plan dates against database, current: start={}, end={}, DB: start={}, end={}", 
                fieldPlan.getStartDate(), fieldPlan.getEndDate(), 
                fieldPlanFromDB.getStartDate(), fieldPlanFromDB.getEndDate());
        String errorMessage = "";
        // Check if the fieldplan start date is not null and whether it's different from the one in the database
        errorMessage = getErrorMessage(fieldPlan, fieldPlanFromDB, currentTimestamp, nextDateTimestampUTC, errorMessage);
        // If there's an error message, log it and throw a CustomException
        if (!errorMessage.trim().isEmpty()) {
            log.error(errorMessage);
            throw new CustomException("INVALID_PROJECT_MODIFY", errorMessage);
        }

        errorMessage = "";
        // Check if the project end date is not null and whether it's different from the one in the database
        if (fieldPlan.getEndDate() != null) {
            // Check if the project end date is before the current timestamp or within 24 hours from the next date's midnight
            if (fieldPlan.getEndDate().compareTo(fieldPlanFromDB.getEndDate()) < 0) {
                if (fieldPlan.getEndDate().compareTo(currentTimestamp) < 0) {
                    errorMessage = "The fieldplan end date cannot be updated as it has already ended. The fieldplan end date cannot be decreased to a past date.";
                } else if (fieldPlan.getEndDate().compareTo(nextDateTimestampUTC) < 0) {
                    errorMessage = "The fieldplan end date cannot be updated as it should be at least 24 hours in advance from the current time and start after the next day onwards.";
                }
            }
        } else {
            errorMessage = "The fieldplan end date cannot be updated as it is null.";
        }
        // If there's an error message, log it and throw a CustomException
        if (!errorMessage.trim().isEmpty()) {
            log.error(errorMessage);
            throw new CustomException("INVALID_PROJECT_MODIFY", errorMessage);
        }
        log.trace("Exiting validateStartDateAndEndDateAgainstDB method");
    }

    private static String getErrorMessage(FieldPlan fieldPlan, FieldPlan fieldPlanFromDB, Long currentTimestamp, Long nextDateTimestampUTC, String errorMessage) {
        log.trace("Entering getErrorMessage method");
        if (fieldPlan.getStartDate() != null) {
            // Check if the project start date is different from the one in the database
            if (fieldPlan.getStartDate().compareTo(fieldPlanFromDB.getStartDate()) != 0) {
                // Check if the project start date is before the current timestamp or within 24 hours from the next date's midnight
                if (fieldPlanFromDB.getStartDate().compareTo(currentTimestamp) < 0) {
                    errorMessage = "The fieldplan start date cannot be updated as the fieldplan has already started.";
                } else if (fieldPlan.getStartDate().compareTo(nextDateTimestampUTC) < 0) {
                    errorMessage = "The fieldplan start date cannot be updated as it should be at least 24 hours in advance from the current time and start after the next day onwards.";
                }
            }
        } else {
            errorMessage = "The project start date cannot be updated as it is null.";
        }
        log.trace("Exiting getErrorMessage method");
        return errorMessage;
    }
}