package org.egov.activity.validator;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.jsonpath.JsonPath;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.egov.activity.repository.ActivityFacilityRepository;
import org.egov.activity.service.ServiceRequestRepository;
import org.egov.activity.web.models.*;
import org.egov.common.contract.request.RequestInfo;
import org.egov.common.http.client.ServiceRequestClient;
import org.egov.activity.config.ActivityConfiguration;
import org.egov.activity.util.MDMSUtils;
import org.egov.tracer.model.CustomException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.*;

import static org.egov.activity.util.ActivityConstants.*;

@Component
@Slf4j
public class ActivityValidator {

    @Autowired
    ActivityFacilityRepository activityFacilityRepository;

    @Autowired
    private final ServiceRequestClient serviceRequestRepository;

    private ServiceRequestRepository serviceRequest;

    private final ActivityConfiguration activityConfiguration;

    public static final String START_DATE_SHOULD_BE_LESS_THAN_END_DATE = "Start date should be less than end date";
    public static final String IS_NOT_PRESENT_IN_MDMS = " is not present in MDMS";
    public static final String TENANT_ID_IS_MANDATORY_IN_ACTIVITY_REQUEST_BODY = "Tenant ID is mandatory in Activity request body";
    public static final String ACTIVITIES_IS_MANDATORY_IN_ACTIVITY_REQUEST_BODY = "Activity are mandatory in Activity request body";
    public static final String DOES_NOT_EXISTS_FOR_THE_FIELDPLAN = " that you are trying to update does not exists for the FieldPlan ";
    @Autowired
    MDMSUtils mdmsUtils;

    @Autowired
    ActivityConfiguration config;

    @Autowired
    @Qualifier("objectMapper")
    ObjectMapper mapper;

    public ActivityValidator(ServiceRequestClient serviceRequestRepository, ActivityConfiguration activityConfiguration, ServiceRequestRepository serviceRequest){
        this.serviceRequestRepository = serviceRequestRepository;
        this.activityConfiguration = activityConfiguration;
        this.serviceRequest = serviceRequest;
    }

    public void validateCreateActivityAssignmentRequest(ActivityAssignmentBulkRequest request) {
        Map<String, String> errorMap = new HashMap<>();
        RequestInfo requestInfo = request.getRequestInfo();

        //Verify if RequestInfo and UserInfo is present
        validateRequestInfo(requestInfo);
        //Verify if ActivityAssignment request and mandatory fields are present
        validateActivityAssignmentRequest(request);

        if (!errorMap.isEmpty())
            throw new CustomException(errorMap);
    }

    public void validateUpdateActivityAssignment(ActivityAssignmentBulkRequest request) {
        Map<String, String> errorMap = new HashMap<>();
        RequestInfo requestInfo = request.getRequestInfo();

        //Verify if RequestInfo and UserInfo is present
        validateRequestInfo(requestInfo);
        //Verify if ActivityAssignment request and mandatory fields are present
        validateUpdateActivityAssignmentRequest(request);

        if (!errorMap.isEmpty())
            throw new CustomException(errorMap);
    }

    public void validateDeleteActivityAssignmentRequest(ActivityAssignmentBulkRequest request) {
        Map<String, String> errorMap = new HashMap<>();
        RequestInfo requestInfo = request.getRequestInfo();

        //Verify if RequestInfo and UserInfo is present
        validateRequestInfo(requestInfo);
        //Verify if ActivityAssignment request and mandatory fields are present
        validateUnassignActivityAssignmentRequest(request);

        if (!errorMap.isEmpty())
            throw new CustomException(errorMap);
    }

    public void validateCreateActivityFacilityRequest(ActivityFacilityBulkRequest request) {
        Map<String, String> errorMap = new HashMap<>();
        RequestInfo requestInfo = request.getRequestInfo();

        //Verify if RequestInfo and UserInfo is present
        validateRequestInfo(requestInfo);
        //Verify if ActivityAssignment request and mandatory fields are present
        validateActivityFacilityRequest(request);

        if (!errorMap.isEmpty())
            throw new CustomException(errorMap);
    }

    private void validateActivityAssignmentRequest(ActivityAssignmentBulkRequest request) {
        Map<String, String> errorMap = new HashMap<>();

        if (request.getActivityAssignments() == null || request.getActivityAssignments().size() == 0) {
            log.error("Field Plans list is empty. Field Plans is mandatory");
            throw new CustomException("FIELDPLAN", "Field Plans are mandatory");
        }

        for (ActivityAssignment activityAssignment : request.getActivityAssignments()) {
            if (activityAssignment.getFieldPlanId() == null) {
                log.error("FieldPlan ID is mandatory in FieldPlans");
                throw new CustomException("FieldPlan", "Project ID is mandatory");
            }
            if (StringUtils.isBlank(activityAssignment.getTenantId())) {
                log.error(TENANT_ID_IS_MANDATORY_IN_ACTIVITY_REQUEST_BODY);
                errorMap.put("TENANT_ID", "Tenant ID is mandatory");
            }
            // Get existing project with projectID from project service
            FieldPlan existingFieldPlan = getFieldPlanById(request.getRequestInfo(), activityAssignment.getFieldPlanId(), activityAssignment.getTenantId());
            if (existingFieldPlan == null) {
                log.error("FieldPlan ID do not exist");
                throw new CustomException("FieldPlan", "FieldPlan ID do not exist");
            }
//             Check if fieldPlan dates are within project dates
            isActivityAsignmentWithinFieldPlan(existingFieldPlan, activityAssignment, errorMap);

            if (activityAssignment == null) {
                log.error("Activity Assignment is mandatory in Activities");
                throw new CustomException("Activity", "Activity is mandatory");
            }
            if (activityAssignment.getActivityId() == null) {
                log.error(ACTIVITIES_IS_MANDATORY_IN_ACTIVITY_REQUEST_BODY);
                errorMap.put("ACTIVITIES", "Activity is mandatory");
            }
            // Get existing project with projectID from project service
            ActivitySearchCriteria criteria = ActivitySearchCriteria.builder().code(List.of(activityAssignment.getActivityId())).build();
            Activity existingActivity = activityFacilityRepository.getActivityObject(criteria);
            if (existingActivity == null) {
                log.error("Activity code do not exist");
                throw new CustomException("Activity", "Activity code do not exist");
            }

            if ((activityAssignment.getStartDate() != null && activityAssignment.getEndDate() != null && activityAssignment.getEndDate() != 0) && (activityAssignment.getStartDate().compareTo(activityAssignment.getEndDate()) > 0)) {
                log.error(START_DATE_SHOULD_BE_LESS_THAN_END_DATE);
                errorMap.put("INVALID_DATE_ERROR", START_DATE_SHOULD_BE_LESS_THAN_END_DATE);
            }
            if (activityAssignment.getStartDate() != null && activityAssignment.getEndDate() != null && activityAssignment.getEndDate() != 0
                    && activityAssignment.getEndDate().compareTo(Instant.ofEpochMilli(activityAssignment.getStartDate()).plus(Duration.ofDays(1)).toEpochMilli()) < 0) {
                log.error("Start date and end date difference should at least be 1 day.");
                errorMap.put("INVALID_DATE", "Start date and end date difference should at least be 1 day.");
            }
        }

        if (!errorMap.isEmpty())
            throw new CustomException(errorMap);
    }

    private void validateUpdateActivityAssignmentRequest(ActivityAssignmentBulkRequest request) {
        Map<String, String> errorMap = new HashMap<>();

        if (request.getActivityAssignments() == null || request.getActivityAssignments().size() == 0) {
            log.error("Field Plans list is empty. Field Plans is mandatory");
            throw new CustomException("FIELDPLAN", "Field Plans are mandatory");
        }

        for (ActivityAssignment activityAssignment : request.getActivityAssignments()) {
            if (activityAssignment.getFieldPlanId() == null) {
                log.error("FieldPlan ID is mandatory in FieldPlans");
                throw new CustomException("FieldPlan", "Project ID is mandatory");
            }
            if (StringUtils.isBlank(activityAssignment.getTenantId())) {
                log.error(TENANT_ID_IS_MANDATORY_IN_ACTIVITY_REQUEST_BODY);
                errorMap.put("TENANT_ID", "Tenant ID is mandatory");
            }
            // Get existing project with projectID from project service
            FieldPlan existingFieldPlan = getFieldPlanById(request.getRequestInfo(), activityAssignment.getFieldPlanId(), activityAssignment.getTenantId());
            if (existingFieldPlan == null) {
                log.error("FieldPlan ID do not exist");
                throw new CustomException("FieldPlan", "Project ID do not exist");
            }
//             Check if fieldPlan dates are within project dates
            isActivityAsignmentWithinFieldPlan(existingFieldPlan, activityAssignment, errorMap);

            if (activityAssignment == null) {
                log.error("Activity Assignment is mandatory in Activities");
                throw new CustomException("Activity", "Activity is mandatory");
            }
            if (activityAssignment.getActivityId() == null) {
                log.error(ACTIVITIES_IS_MANDATORY_IN_ACTIVITY_REQUEST_BODY);
                errorMap.put("ACTIVITIES", "Activity is mandatory");
            }
            // Get existing project with projectID from project service
            ActivitySearchCriteria criteria = ActivitySearchCriteria.builder().ids(List.of(activityAssignment.getActivityId())).build();
            Activity existingActivity = activityFacilityRepository.getActivityObject(criteria);
            if (existingActivity == null) {
                log.error("Activity code do not exist");
                throw new CustomException("Activity", "Activity code do not exist");
            }

            if ((activityAssignment.getStartDate() != null && activityAssignment.getEndDate() != null && activityAssignment.getEndDate() != 0) && (activityAssignment.getStartDate().compareTo(activityAssignment.getEndDate()) > 0)) {
                log.error(START_DATE_SHOULD_BE_LESS_THAN_END_DATE);
                errorMap.put("INVALID_DATE_ERROR", START_DATE_SHOULD_BE_LESS_THAN_END_DATE);
            }
            if (activityAssignment.getStartDate() != null && activityAssignment.getEndDate() != null && activityAssignment.getEndDate() != 0
                    && activityAssignment.getEndDate().compareTo(Instant.ofEpochMilli(activityAssignment.getStartDate()).plus(Duration.ofDays(1)).toEpochMilli()) < 0) {
                log.error("Start date and end date difference should at least be 1 day.");
                errorMap.put("INVALID_DATE", "Start date and end date difference should at least be 1 day.");
            }
        }

        if (!errorMap.isEmpty())
            throw new CustomException(errorMap);
    }

    private void validateUnassignActivityAssignmentRequest(ActivityAssignmentBulkRequest request) {
        Map<String, String> errorMap = new HashMap<>();

        if (request.getActivityAssignments() == null || request.getActivityAssignments().size() == 0) {
            log.error("Field Plans list is empty. Field Plans is mandatory");
            throw new CustomException("FIELDPLAN", "Field Plans are mandatory");
        }

        for (ActivityAssignment activityAssignment : request.getActivityAssignments()) {
            if (activityAssignment.getFieldPlanId() == null) {
                log.error("FieldPlan ID is mandatory in FieldPlans");
                throw new CustomException("FieldPlan", "Project ID is mandatory");
            }
            // Get existing project with projectID from project service
            FieldPlan existingFieldPlan = getFieldPlanById(request.getRequestInfo(), activityAssignment.getFieldPlanId(), activityAssignment.getTenantId());
            if (existingFieldPlan == null) {
                log.error("FieldPlan ID do not exist");
                throw new CustomException("FieldPlan", "Project ID do not exist");
            }

            if (activityAssignment == null) {
                log.error("Activity Assignment is mandatory in Activities");
                throw new CustomException("Activity", "Activity is mandatory");
            }
            if (StringUtils.isBlank(activityAssignment.getTenantId())) {
                log.error(TENANT_ID_IS_MANDATORY_IN_ACTIVITY_REQUEST_BODY);
                errorMap.put("TENANT_ID", "Tenant ID is mandatory");
            }
        }

        if (!errorMap.isEmpty())
            throw new CustomException(errorMap);
    }

    private void validateActivityFacilityRequest(ActivityFacilityBulkRequest request) {
        Map<String, String> errorMap = new HashMap<>();

        if (request.getActivityFacilities() == null || request.getActivityFacilities().size() == 0) {
            log.error("Activity list is empty. Activity is mandatory");
            throw new CustomException("ACTIVITY", "Activity are mandatory");
        }

        for (ActivityFacility activityFacility : request.getActivityFacilities()) {
            if (activityFacility == null) {
                log.error("Activity Assignment is mandatory in Activities");
                throw new CustomException("Activity", "Activity is mandatory");
            }

            if (activityFacility.getFieldPlanId() == null) {
                log.error("FieldPlan ID is mandatory in FieldPlans");
                throw new CustomException("FieldPlan", "Project ID is mandatory");
            }
            // Get existing fieldplan with fieldPlanId from project service
            FieldPlan existingFieldPlan = getFieldPlanById(request.getRequestInfo(), activityFacility.getFieldPlanId(), activityFacility.getTenantId());
            if (existingFieldPlan == null) {
                log.error("FieldPlan ID do not exist");
                throw new CustomException("Activity_FieldPlan", "FieldPlan ID do not exist");
            }

            if (activityFacility.getFacilityId() == null) {
                log.error("Facility ID is mandatory in FieldPlans");
                throw new CustomException("Activity_FACILITY", "Facility ID is mandatory");
            }

            // Get existing facility with facilityId from facility service
            Facility existingfacility = getFacilityById(activityFacility.getFacilityId());
            if (existingfacility == null) {
                log.error("Facility ID do not exist");
                throw new CustomException("Activity_ERROR", "Facility ID do not exist");
            }

            if (StringUtils.isBlank(activityFacility.getTenantId())) {
                log.error(TENANT_ID_IS_MANDATORY_IN_ACTIVITY_REQUEST_BODY);
                errorMap.put("TENANT_ID", "Tenant ID is mandatory");
            }
            if (activityFacility.getActivityId() == null) {
                log.error(ACTIVITIES_IS_MANDATORY_IN_ACTIVITY_REQUEST_BODY);
                errorMap.put("ACTIVITIES", "Activity is mandatory");
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
//    private void validateRequestMDMSData(ActivityRequest request, String tenantId, Map<String, String> errorMap) {
//        String rootTenantId = tenantId.split("\\.")[0];
//
//        //Get MDMS data using create project request and tenantId
//        Object mdmsData = mdmsUtils.mDMSCall(request, rootTenantId);
//
//        validateMDMSData(request.getFieldPlans(), mdmsData, errorMap);
//        log.info("Request data validated with MDMS");
//    }

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

    public FieldPlan getFieldPlanById(RequestInfo request, String fieldPlanId, String tenantId) {
        FieldPlanSearchCriteria fieldPlan = FieldPlanSearchCriteria.builder().ids(List.of(fieldPlanId)).tenantId(tenantId).build();
        FieldPlanSearchRequest fieldPlanRequest = FieldPlanSearchRequest.builder().requestInfo(request).fieldPlan(fieldPlan).build();
        String url = config.getFieldPlanServiceHost() + config.getFieldPlanServiceSearchUrl()+ "?tenantId="+tenantId+"&offset=0&limit=100";
        Object response = serviceRequestRepository.fetchResult(new StringBuilder(url), fieldPlanRequest, Map.class);
        FieldPlanResponse fieldPlanResponse = mapper.convertValue(response, FieldPlanResponse.class);
        if(fieldPlanResponse != null && fieldPlanResponse.getFieldPlans() !=null && fieldPlanResponse.getFieldPlans().size() > 0){
            return fieldPlanResponse.getFieldPlans().get(0);
        }
        return null;
    }

    public void isActivityAsignmentWithinFieldPlan(FieldPlan fieldPlan, ActivityAssignment activityAssignment, Map<String, String> errorMap) {
        if (fieldPlan == null || activityAssignment == null) {
            log.error("FieldPlan or Activity is null");
            errorMap.put("FIELDPLAN", "Activity or FieldPlan is null");
        }

        Long fieldStart = fieldPlan.getStartDate();
        Long fieldEnd   = fieldPlan.getEndDate();
        Long activityStart   = activityAssignment.getStartDate();
        Long activityEnd     = activityAssignment.getEndDate();

        if (fieldStart == null || fieldEnd == null) {
            log.error("FieldPlan dates are not mandatory");
            errorMap.put("FIELDPLAN_PROJECT", "FieldPlan dates are not mandatory");
        }
        if (activityStart == null || activityEnd == null) {
            log.error("Activity dates are not mandatory");
            errorMap.put("ACTIVITY", "Activity dates are not mandatory");
        }

        if (activityStart < fieldStart) {
            log.error("The Activity start date is earlier than the FieldPlan start date");
            errorMap.put("FIELDPLAN_STARTDATE", "The Activity start date is earlier than the FieldPlan start date");
        }
        if (activityEnd > fieldEnd) {
            log.error("The FieldPlan end date is later than the Project end date");
            errorMap.put("FIELDPLAN_ENDDATE", "The Activity end date is later than the FieldPlan end date");
        }
    }


    /* Validates search FieldPlan request body and parameters*/
    public void validateSearchActivityRequest(ActivityFacilitySearchRequest request, Integer limit, Integer offset, String tenantId) {
        Map<String, String> errorMap = new HashMap<>();
        RequestInfo requestInfo = request.getRequestInfo();

        //Verify if RequestInfo and UserInfo is present
        validateRequestInfo(requestInfo);
        //Verify if search fieldplan request parameters are valid
        validateSearchFieldPlanRequestParams(limit, offset, tenantId);
        //Verify if search fieldplan request is valid
        validateSearchRequest(request.getCriteria(), tenantId);
        //Verify MDMS Data
        // TODO: Uncomment and fix as per HCM once we get clarity
        // validateRequestMDMSData(project, tenantId, errorMap);

        if (!errorMap.isEmpty())
            throw new CustomException(errorMap);
    }

    /* Validates search FieldPlan request body and parameters*/
    public void validateSearchAssignActivityRequest(ActivityAssignmentSearchRequest request, Integer limit, Integer offset, String tenantId) {
        Map<String, String> errorMap = new HashMap<>();
        RequestInfo requestInfo = request.getRequestInfo();

        //Verify if RequestInfo and UserInfo is present
        validateRequestInfo(requestInfo);
        //Verify if search fieldplan request parameters are valid
        validateSearchFieldPlanRequestParams(limit, offset, tenantId);
        //Verify if search fieldplan request is valid
        validateActivityAssignmentSearchRequest(request.getCriteria(), tenantId);
        //Verify MDMS Data
        // TODO: Uncomment and fix as per HCM once we get clarity
        // validateRequestMDMSData(project, tenantId, errorMap);

        if (!errorMap.isEmpty())
            throw new CustomException(errorMap);
    }

    /* Validates if search Project request parameters are valid */
    private void validateSearchFieldPlanRequestParams(Integer limit, Integer offset, String tenantId) {
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
    }

    /* Validates Search Project Request body */
    private void validateSearchRequest(ActivityFacilitySearchCriteria criteria, String tenantId) {
        checkFieldPlansIfEmpty(criteria);

        doNullAndEmptyChecks(tenantId, criteria);
    }

    private void validateActivityAssignmentSearchRequest(ActivityAssignmentSearchCriteria criteria, String tenantId) {
        if (criteria == null) {
            log.error("Activity is empty. Activity is mandatory");
            throw new CustomException("Activity", "Activity are mandatory");
        }

        doNullAndEmptyChecksActivityAssignment(tenantId, criteria);
    }

    private static void checkFieldPlansIfEmpty(ActivityFacilitySearchCriteria activityFacilities) {
        if (activityFacilities == null) {
            log.error("Activity is empty. Activity is mandatory");
            throw new CustomException("Activity", "Activity are mandatory");
        }
    }

    private static void doNullAndEmptyChecks(String tenantId, ActivityFacilitySearchCriteria activityFacility) {
        if (activityFacility == null) {
            log.error("fieldPlan is mandatory in FieldPlans");
            throw new CustomException("FIELDPLAN", "FieldPlan is mandatory");
        }
        if (StringUtils.isBlank(activityFacility.getTenantId())) {
            log.error(TENANT_ID_IS_MANDATORY_IN_ACTIVITY_REQUEST_BODY);
            throw new CustomException("TENANT_ID", "Tenant ID is mandatory");
        }
        if ((activityFacility.getIds()==null || activityFacility.getIds().isEmpty()) && (activityFacility.getFieldPlanId()==null || activityFacility.getFieldPlanId().isEmpty())
                && (activityFacility.getStatuses()==null || activityFacility.getStatuses().isEmpty()) && (activityFacility.getActivityId()==null || activityFacility.getActivityId().isEmpty())
                && StringUtils.isBlank(activityFacility.getAssignedToMe())
                && StringUtils.isBlank(activityFacility.getAssignedUserId())
                && (activityFacility.getBoundaryCodes()==null || activityFacility.getBoundaryCodes().isEmpty())
                && StringUtils.isBlank(activityFacility.getFacilityName()))
        {
            log.error("Any one Activity search field is required for FieldPlan Search");
            throw new CustomException("ACTIVITY_SEARCH_FIELDS", "Any one activity search field is required");
        }

        if (!activityFacility.getTenantId().equals(tenantId)) {
            log.error("Tenant Id must be same in URL param as well as project request body");
            throw new CustomException("MULTIPLE_TENANTS", "Tenant Id must be same in URL param and project request");
        }
    }

    private static void doNullAndEmptyChecksActivityAssignment(String tenantId, ActivityAssignmentSearchCriteria criteria) {
        if (criteria == null) {
            log.error("fieldPlan is mandatory in FieldPlans");
            throw new CustomException("FIELDPLAN", "FieldPlan is mandatory");
        }
        if (StringUtils.isBlank(criteria.getTenantId())) {
            log.error(TENANT_ID_IS_MANDATORY_IN_ACTIVITY_REQUEST_BODY);
            throw new CustomException("TENANT_ID", "Tenant ID is mandatory");
        }
        if ((criteria.getIds()==null || criteria.getIds().isEmpty()) && (criteria.getFieldPlanId()==null || criteria.getFieldPlanId().isEmpty())
                && (criteria.getStatuses()==null || criteria.getStatuses().isEmpty()) && (criteria.getActivityId()==null || criteria.getActivityId().isEmpty())
                && StringUtils.isBlank(criteria.getAssignedTo()) && StringUtils.isBlank(criteria.getTenantId())
                && StringUtils.isBlank(criteria.getFieldPlanCode())
                && StringUtils.isBlank(criteria.getAssignedBy()))
        {
            log.error("Any one Activity search field is required for FieldPlan Search");
            throw new CustomException("ACTIVITY_SEARCH_FIELDS", "Any one activity search field is required");
        }

        if (!criteria.getTenantId().equals(tenantId)) {
            log.error("Tenant Id must be same in URL param as well as project request body");
            throw new CustomException("MULTIPLE_TENANTS", "Tenant Id must be same in URL param and project request");
        }
    }

    /* Validates if all FieldPlans have same tenant Id */
    private void validateMultipleTenantIds(ActivityRequest request) {
        List<ActivityFacility> activityFacilities = request.getActivityFacilities();
        String firstTenantId = activityFacilities.get(0).getTenantId();
        if (activityFacilities.stream().anyMatch(p -> !p.getTenantId().equals(firstTenantId))) {
            log.error("All fieldplans in FieldPlan request must have same tenant Id");
            throw new CustomException("MULTIPLE_TENANTS", "All Activities must have same tenant Id. Please create new request for different tentant id");
        }
    }

    /* Validates projects data in update request against projects data fetched from database */
    public void validateUpdateAgainstDB(List<ActivityFacility> activitiesFacilityFromRequest, List<ActivityFacility> activitiesFacilityFromDB) {
        if (CollectionUtils.isEmpty(activitiesFacilityFromDB)) {
            log.error("The activities Facility records that you are trying to update does not exists in the system");
            throw new CustomException("INVALID_ACTIVITY_UPDATE", "The records that you are trying to update does not exists in the system");
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
        for (ActivityFacility activityFacility : activitiesFacilityFromRequest) {
            ActivityFacility activityFacilityFromDB = activitiesFacilityFromDB.stream().filter(p -> p.getId().equals(activityFacility.getId())).findFirst().orElse(null);

            if (activityFacilityFromDB == null) {
                log.error("The activity facilty id " + activityFacility.getId() + " that you are trying to update does not exists for the activity");
                throw new CustomException("INVALID_ACTIVITY_UPDATE", "The activity id " + activityFacility.getId() + " that you are trying to update does not exists for the Activity");
            }
        }
    }

    /* Validates projects data in update request against projects data fetched from database */
    public void validateUpdateActivityAssignmentAgainstDB(List<ActivityAssignment> activitiesAssignmentFromRequest, List<ActivityAssignment> activitiesAssignmentFromDB) {
        if (CollectionUtils.isEmpty(activitiesAssignmentFromDB)) {
            log.error("The activities Assignment records that you are trying to update does not exists in the system");
            throw new CustomException("INVALID_ACTIVITY_UPDATE", "The records that you are trying to update does not exists in the system");
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
        for (ActivityAssignment activityAssignment : activitiesAssignmentFromRequest) {
            ActivityAssignment activityAsignmentFromDB = activitiesAssignmentFromDB.stream().filter(p -> p.getId().equals(activityAssignment.getId())).findFirst().orElse(null);

            if (activityAsignmentFromDB == null) {
                log.error("The activity assignment id " + activityAssignment.getId() + " that you are trying to update does not exists for the activity");
                throw new CustomException("INVALID_ACTIVITY_ASSIGNMENT_UPDATE", "The activity id " + activityAssignment.getId() + " that you are trying to update does not exists for the Activity");
            }

            validateStartDateAndEndDateAgainstDB(activityAssignment, activityAsignmentFromDB, currentTimestamp, nextDateTimestampUTC);
        }
    }

    private void validateStartDateAndEndDateAgainstDB(ActivityAssignment activityAssignment, ActivityAssignment activityAssignmentFromDB, Long currentTimestamp, Long nextDateTimestampUTC) {
        String errorMessage = "";
        // Check if the fieldplan start date is not null and whether it's different from the one in the database
        errorMessage = getErrorMessage(activityAssignment, activityAssignmentFromDB, currentTimestamp, nextDateTimestampUTC, errorMessage);
        // If there's an error message, log it and throw a CustomException
        if (!errorMessage.trim().isEmpty()) {
            log.error(errorMessage);
            throw new CustomException("INVALID_PROJECT_MODIFY", errorMessage);
        }

        errorMessage = "";
        // Check if the project end date is not null and whether it's different from the one in the database
        if (activityAssignment.getEndDate() != null) {
            // Check if the project end date is before the current timestamp or within 24 hours from the next date's midnight
            if (activityAssignment.getEndDate().compareTo(activityAssignmentFromDB.getEndDate()) < 0) {
                if (activityAssignment.getEndDate().compareTo(currentTimestamp) < 0) {
                    errorMessage = "The fieldplan end date cannot be updated as it has already ended. The fieldplan end date cannot be decreased to a past date.";
                } else if (activityAssignment.getEndDate().compareTo(nextDateTimestampUTC) < 0) {
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

    private static String getErrorMessage(ActivityAssignment activityAssignment, ActivityAssignment activityAssignmentFromDB, Long currentTimestamp, Long nextDateTimestampUTC, String errorMessage) {
        if (activityAssignment.getStartDate() != null) {
            // Check if the project start date is different from the one in the database
            if (activityAssignment.getStartDate().compareTo(activityAssignmentFromDB.getStartDate()) != 0) {
                // Check if the project start date is before the current timestamp or within 24 hours from the next date's midnight
                if (activityAssignmentFromDB.getStartDate().compareTo(currentTimestamp) < 0) {
                    errorMessage = "The fieldplan start date cannot be updated as the fieldplan has already started.";
                } else if (activityAssignment.getStartDate().compareTo(nextDateTimestampUTC) < 0) {
                    errorMessage = "The fieldplan start date cannot be updated as it should be at least 24 hours in advance from the current time and start after the next day onwards.";
                }
            }
        } else {
            errorMessage = "The project start date cannot be updated as it is null.";
        }
        return errorMessage;
    }

    public Facility getFacilityById(String facilityId) {

        String url = activityConfiguration.getFacilityServiceHost() + activityConfiguration.getFacilityServiceSearchUrlV2()+ "?facilityId="+facilityId;
        Object response = serviceRequest.fetchResult(new StringBuilder(url));

        FacilitySearchResponse facilityList = mapper.convertValue(response, FacilitySearchResponse.class);
        if(facilityList != null && facilityList.getFacilities() !=null && facilityList.getFacilities().size() > 0){
            return facilityList.getFacilities().get(0);
        }
        return null;
    }

    public OrgUserEnriched getUserOrgById(ActivityFacilitySearchRequest request, ActivityFacility activityFacility) {
        String userId = activityFacility.getAssignedUser();
        List<String> userIds = new ArrayList<>();
        userIds.add(userId);
        OrgUserSearchCriteria criteria = OrgUserSearchCriteria.builder().userId(userIds).build();
        OrgUserSearchRequest searchRequest = OrgUserSearchRequest.builder().requestInfo(request.getRequestInfo()).criteria(criteria).build();
        String url = activityConfiguration.getOrgUserHost() + activityConfiguration.getOrgUserSearchUrl()+ "?tenantId="+activityFacility.getTenantId()+"&offset=0&limit=100";
        Object response = serviceRequestRepository.fetchResult(new StringBuilder(url), searchRequest, Map.class);

        OrgUserResponseSearch orgUserResponse = mapper.convertValue(response, OrgUserResponseSearch.class);
        if (orgUserResponse != null && orgUserResponse.getOrgUsers() != null && !orgUserResponse.getOrgUsers().isEmpty()) {
//            throw new CustomException("USER_NOT_FOUND", "User not found with ID: " + userId);
            return orgUserResponse.getOrgUsers().get(0);
        }

        return null;
    }

    public FieldPlanFacilityBulkResponse getFieldPlanFacilityById(RequestInfo request, String fieldPlanId, String tenantId) {
        FieldPlanFacilitySearch fieldPlanFacility = FieldPlanFacilitySearch.builder().field_plan_id(List.of(fieldPlanId)).build();
        FieldPlanFacilitySearchRequest fieldPlanRequest = FieldPlanFacilitySearchRequest.builder().requestInfo(request).criteria(fieldPlanFacility).build();
        String url = config.getFieldPlanServiceHost() + config.getFieldPlanFacilityServiceSearchUrl()+ "?tenantId="+tenantId+"&offset=0&limit=100";
        Object response = serviceRequestRepository.fetchResult(new StringBuilder(url), fieldPlanRequest, Map.class);
        FieldPlanFacilityBulkResponse fieldPlanResponse = mapper.convertValue(response, FieldPlanFacilityBulkResponse.class);
        if(fieldPlanResponse != null && fieldPlanResponse.getFieldPlanFacilities() !=null && fieldPlanResponse.getFieldPlanFacilities().size() > 0){
            return fieldPlanResponse;
        }
        return null;
    }

    public Employee getUserById(Object request, String userId) {

        String url = config.getHrmsHost() + config.getHrmsSearchUrl()+ "?tenantId=in&uuids="+userId;
        Object response = serviceRequest.fetchResult(new StringBuilder(url), request);

        EmployeeResponse employeeResponse = mapper.convertValue(response, EmployeeResponse.class);
        if (employeeResponse == null || employeeResponse.getEmployees() == null || employeeResponse.getEmployees().isEmpty()) {
            throw new CustomException("EMPLOYEE_NOT_FOUND", "Employee not found with ID: " + userId);
        }
        return employeeResponse.getEmployees().get(0);
    }
}