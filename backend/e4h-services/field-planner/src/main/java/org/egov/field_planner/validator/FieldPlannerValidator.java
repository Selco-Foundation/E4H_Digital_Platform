package org.egov.field_planner.validator;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.jsonpath.JsonPath;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.egov.common.contract.request.RequestInfo;
import org.egov.common.http.client.ServiceRequestClient;
import org.egov.common.models.core.ProjectSearchURLParams;
import org.egov.common.models.project.*;
import org.egov.field_planner.config.FieldPlannerConfiguration;
import org.egov.field_planner.repository.FieldPlannerRepository;
import org.egov.field_planner.util.MDMSUtils;
import org.egov.field_planner.web.models.FieldPlan;
import org.egov.field_planner.web.models.FieldPlanRequest;
import org.egov.tracer.model.CustomException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.*;
import java.util.stream.Collectors;

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

    @Autowired
    @Qualifier("objectMapper")
    ObjectMapper mapper;

    public FieldPlannerValidator(ServiceRequestClient serviceRequestRepository){
        this.serviceRequestRepository = serviceRequestRepository;
    }

    public void validateCreateFieldPlanRequest(FieldPlanRequest request) {
        Map<String, String> errorMap = new HashMap<>();
        RequestInfo requestInfo = request.getRequestInfo();

        //Verify if RequestInfo and UserInfo is present
        validateRequestInfo(requestInfo);
        //Verify if FieldPlan request and mandatory fields are present
        validateFieldPlanRequest(request);

        validateRequestMDMSData(request, request.getFieldPlans().get(0).getTenantId(), errorMap);

        if (!errorMap.isEmpty())
            throw new CustomException(errorMap);
    }

    private void validateFieldPlanRequest(FieldPlanRequest request) {
        Map<String, String> errorMap = new HashMap<>();

        if (request.getFieldPlans() == null || request.getFieldPlans().size() == 0) {
            log.error("Field Plans list is empty. Field Plans is mandatory");
            throw new CustomException("FIELDPLAN", "Field Plans are mandatory");
        }

        for (FieldPlan fieldPlan : request.getFieldPlans()) {
            if (fieldPlan.getProjectId() == null) {
                log.error("Project ID is mandatory in FieldPlans");
                throw new CustomException("FieldPlan", "Project ID is mandatory");
            }
            // Get existing project with projectID from project service
//            Project existingProject = getProjectById(request, fieldPlan);
//            if (existingProject == null) {
//                log.error("Project ID do not exist");
//                throw new CustomException("FieldPlan", "Project ID do not exist");
//            }
            // Check if fieldPlan dates are within project dates
//            isFieldPlanWithinProject(existingProject, fieldPlan, errorMap);

            if (fieldPlan == null) {
                log.error("FieldPlan is mandatory in FieldPlans");
                throw new CustomException("FieldPlan", "FieldPlan is mandatory");
            }
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

        if (!errorMap.isEmpty())
            throw new CustomException(errorMap);
    }

    private void validateRequestInfo(RequestInfo requestInfo) {
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
    }

    /* Validate Project Request MDMS data */
    private void validateRequestMDMSData(FieldPlanRequest request, String tenantId, Map<String, String> errorMap) {
        String rootTenantId = tenantId.split("\\.")[0];

        //Get MDMS data using create project request and tenantId
        Object mdmsData = mdmsUtils.mDMSCall(request, rootTenantId);

        validateMDMSData(request.getFieldPlans(), mdmsData, errorMap);
        log.info("Request data validated with MDMS");
    }

    /* Validates the request data against MDMS data */
    private void validateMDMSData(List<FieldPlan> fieldPlans, Object mdmsData, Map<String, String> errorMap) {
        String mdmsRes = "$.MdmsRes.";
        final String jsonPathForActivities = mdmsRes + MDMS_COMMON_MASTERS_MODULE_NAME + "." + MASTER_ACTIVITIES + ".*.code";
        final String jsonPathForStateInfo = mdmsRes + MDMS_COMMON_MASTERS_MODULE_NAME + "." + MASTER_STATE_INFO + ".*.name";
        final String jsonPathForTenants = mdmsRes + MDMS_TENANT_MODULE_NAME + "." + MASTER_TENANTS + ".*";

        List<Object> activitiesRes = null;
        List<Object> stateInfoRes = null;
        List<Object> tenantRes = null;
        try {
            activitiesRes = JsonPath.read(mdmsData, jsonPathForActivities);
            stateInfoRes = JsonPath.read(mdmsData, jsonPathForStateInfo);
            tenantRes = JsonPath.read(mdmsData, jsonPathForTenants);
        } catch (Exception e) {
            log.error(e.getMessage());
            throw new CustomException("JSONPATH_ERROR", "Failed to parse mdms response");
        }

        for (FieldPlan fieldPlan : fieldPlans) {
            log.info("Validate Project type with MDMS");
            Map<String, Object> geographyDetails = fieldPlan.getGeographyDetails();
            List<Map<String, Object>> activities = fieldPlan.getActivities();
            String state = (String)geographyDetails.get("state");
            String mdmsNotPresent = IS_NOT_PRESENT_IN_MDMS;
//            if (!fieldPlan.getActivities().isEmpty() && !typeOfProjectRes.contains(fieldPlan.getActivities())) {
//                log.error("The project type: " + fieldPlan.getActivities() + mdmsNotPresent);
//                errorMap.put("INVALID_PROJECT_TYPE", "The project type: " + fieldPlan.getActivities() + mdmsNotPresent);
//            }
            log.info("Validate Tenant Id with MDMS");
            if (!StringUtils.isBlank(fieldPlan.getTenantId()) && !tenantRes.contains(fieldPlan.getTenantId())) {
                log.error("The tenant: " + fieldPlan.getTenantId() + mdmsNotPresent);
                errorMap.put("INVALID_TENANT", "The tenant: " + fieldPlan.getTenantId() + mdmsNotPresent);
            }
            log.info("Validate stateInfos with MDMS");
            if (!StringUtils.isBlank(state) && !stateInfoRes.contains(state)) {
                log.error("The state code: " + state + mdmsNotPresent);
                errorMap.put("INVALID_STATE_CODE", "The state code: " + state + mdmsNotPresent);
            }
        }
    }

    public Project getProjectById(FieldPlanRequest request, FieldPlan fieldPlan) {
        String projectId = fieldPlan.getProjectId();
        Project project = Project.builder().id(projectId).tenantId(fieldPlan.getTenantId()).build();
        ProjectRequest projectRequest = ProjectRequest.builder().requestInfo(request.getRequestInfo()).projects(List.of(project)).build();
        String url = config.getProjectServiceHost() + config.getProjectServiceSearchUrl()+ "?tenantId="+fieldPlan.getTenantId()+"&offset=0&limit=100";
        Object response = serviceRequestRepository.fetchResult(new StringBuilder(url), projectRequest, Map.class);
        ProjectResponse projectResponse = mapper.convertValue(response, ProjectResponse.class);
        if(projectResponse != null && projectResponse.getProject() !=null && projectResponse.getProject().size() > 0){
            return projectResponse.getProject().get(0);
        }
        return null;
    }

    public void isFieldPlanWithinProject(Project project, FieldPlan fieldPlan, Map<String, String> errorMap) {
        if (project == null || fieldPlan == null) {
            log.error("Project or FieldPlan is null");
            errorMap.put("FIELDPLAN", "Project or FieldPlan is null");
        }

        Long projectStart = project.getStartDate();
        Long projectEnd   = project.getEndDate();
        Long fieldStart   = fieldPlan.getStartDate();
        Long fieldEnd     = fieldPlan.getEndDate();

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
    }

    /* Validates Update Project request body */
    public void validateUpdateFieldPlanRequest(FieldPlanRequest request) {
        Map<String, String> errorMap = new HashMap<>();
        RequestInfo requestInfo = request.getRequestInfo();

        //Verify if RequestInfo and UserInfo is present
        validateRequestInfo(requestInfo);
        //Verify Project request and if mandatory fields are present
        validateFieldPlanRequest(request);
        //Verify if project request have multiple tenant Ids
        validateMultipleTenantIds(request);

        //Verify if FieldPlan id is present
        for (FieldPlan fieldPlan : request.getFieldPlans()) {
            if (StringUtils.isBlank(fieldPlan.getId())) {
                log.error("FieldPlan Id is mandatory");
                throw new CustomException("UPDATE_FIELDPLAN", "FieldPlan Id is mandatory");
            }
        }

//        String tenantId = request.getFieldPlans().get(0).getTenantId();
        //Verify MDMS Data
        // TODO: Uncomment and fix as per HCM once we get clarity
        // validateRequestMDMSData(request, tenantId, errorMap);

        //Get boundaries in list from all Projects in request body for validation
//        Map<String, List<String>> boundariesForValidation = getBoundaryForValidation(request.getProjects());
//        validateBoundary(boundariesForValidation, tenantId, requestInfo, errorMap);
//        log.info("Boundaries in request validated with Location Service");


        if (!errorMap.isEmpty())
            throw new CustomException(errorMap);
    }


    /* Validates search FieldPlan request body and parameters*/
    public void validateSearchProjectRequest(FieldPlanRequest request, Integer limit, Integer offset, String tenantId, Long createdFrom, Long createdTo) {
        Map<String, String> errorMap = new HashMap<>();
        RequestInfo requestInfo = request.getRequestInfo();

        //Verify if RequestInfo and UserInfo is present
        validateRequestInfo(requestInfo);
        //Verify if search fieldplan request parameters are valid
        validateSearchFieldPlanRequestParams(limit, offset, tenantId, createdFrom, createdTo);
        //Verify if search fieldplan request is valid
        validateSearchProjectRequest(request.getFieldPlans(), tenantId, createdFrom);
        //Verify if project request have multiple tenant Ids
        validateMultipleTenantIds(request);
        //Verify MDMS Data
        // TODO: Uncomment and fix as per HCM once we get clarity
        // validateRequestMDMSData(project, tenantId, errorMap);

        if (!errorMap.isEmpty())
            throw new CustomException(errorMap);
    }

    /* Validates if search Project request parameters are valid */
    private void validateSearchFieldPlanRequestParams(Integer limit, Integer offset, String tenantId, Long createdFrom, Long createdTo) {
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
    }

    /* Validates Search Project Request body */
    private void validateSearchProjectRequest(List<FieldPlan> fieldPlans, String tenantId, Long createdFrom) {
        checkFieldPlansIfEmpty(fieldPlans);

        for (FieldPlan fieldPlan : fieldPlans) {
            doNullAndEmptyChecks(tenantId, createdFrom, fieldPlan);

            if ((fieldPlan.getStartDate() != null && fieldPlan.getEndDate() != null && fieldPlan.getEndDate() != 0) && (fieldPlan.getStartDate().compareTo(fieldPlan.getEndDate()) > 0)) {
                log.error(START_DATE_SHOULD_BE_LESS_THAN_END_DATE);
                throw new CustomException("INVALID_DATE", START_DATE_SHOULD_BE_LESS_THAN_END_DATE);
            }

            if ((fieldPlan.getStartDate() == null || fieldPlan.getStartDate() == 0) && (fieldPlan.getEndDate() != null && fieldPlan.getEndDate() != 0)) {
                log.error("Start date is required if end date is passed");
                throw new CustomException("INVALID_DATE", "Start date is required if end date is passed");
            }

        }
    }

    private static void checkFieldPlansIfEmpty(List<FieldPlan> fieldPlans) {
        if (fieldPlans == null || fieldPlans.size() == 0) {
            log.error("Fieldplan list is empty. FieldPlans is mandatory");
            throw new CustomException("Fieldplan", "FieldPlans are mandatory");
        }
    }

    private static void doNullAndEmptyChecks(String tenantId, Long createdFrom, FieldPlan fieldPlan) {
        if (fieldPlan == null) {
            log.error("fieldPlan is mandatory in FieldPlans");
            throw new CustomException("FIELDPLAN", "FieldPlan is mandatory");
        }
        if (StringUtils.isBlank(fieldPlan.getTenantId())) {
            log.error(TENANT_ID_IS_MANDATORY_IN_FIELDPLAN_REQUEST_BODY);
            throw new CustomException("TENANT_ID", "Tenant ID is mandatory");
        }
        if (StringUtils.isBlank(fieldPlan.getId()) && StringUtils.isBlank(fieldPlan.getProjectId())
                && StringUtils.isBlank(fieldPlan.getName())
                && (fieldPlan.getStartDate() == null || fieldPlan.getStartDate() == 0)
                && (fieldPlan.getEndDate() == null || fieldPlan.getEndDate() == 0)
                && (createdFrom == null || createdFrom == 0)
                && (fieldPlan.getGeographyDetails() == null)) {
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
        List<FieldPlan> fieldPlans = request.getFieldPlans();
        String firstTenantId = fieldPlans.get(0).getTenantId();
        if (fieldPlans.stream().anyMatch(p -> !p.getTenantId().equals(firstTenantId))) {
            log.error("All fieldplans in FieldPlan request must have same tenant Id");
            throw new CustomException("MULTIPLE_TENANTS", "All fieldplans must have same tenant Id. Please create new request for different tentant id");
        }
    }

    /* Validates projects data in update request against projects data fetched from database */
    public void validateUpdateAgainstDB(List<FieldPlan> fieldPlansFromRequest, List<FieldPlan> fieldPlansFromDB) {
        if (CollectionUtils.isEmpty(fieldPlansFromDB)) {
            log.error("The fieldplan records that you are trying to update does not exists in the system");
            throw new CustomException("INVALID_FIELDPLAN_MODIFY", "The records that you are trying to update does not exists in the system");
        }
        Long currentTimestamp = Instant.now().toEpochMilli();
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
    }

    private void validateStartDateAndEndDateAgainstDB(FieldPlan fieldPlan, FieldPlan fieldPlanFromDB, Long currentTimestamp, Long nextDateTimestampUTC) {
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
    }

    private static String getErrorMessage(FieldPlan fieldPlan, FieldPlan fieldPlanFromDB, Long currentTimestamp, Long nextDateTimestampUTC, String errorMessage) {
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
        return errorMessage;
    }
}