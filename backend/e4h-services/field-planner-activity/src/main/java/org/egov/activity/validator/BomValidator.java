package org.egov.activity.validator;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.jsonpath.JsonPath;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.egov.activity.config.ActivityConfiguration;
import org.egov.activity.repository.ActivityFacilityRepository;
import org.egov.activity.service.ActivityService;
import org.egov.activity.service.ServiceRequestRepository;
import org.egov.activity.util.MDMSUtils;
import org.egov.activity.web.models.*;
import org.egov.common.contract.request.RequestInfo;
import org.egov.common.http.client.ServiceRequestClient;
import org.egov.tracer.model.CustomException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.egov.activity.util.ActivityConstants.*;

@Component
@Slf4j
public class BomValidator {

    @Autowired
    ActivityFacilityRepository activityFacilityRepository;

    @Autowired
    private final ServiceRequestClient serviceRequestRepository;

    private ServiceRequestRepository serviceRequest;
    private ActivityService activityService;

    private final ActivityConfiguration activityConfiguration;

    public static final String START_DATE_SHOULD_BE_LESS_THAN_END_DATE = "Start date should be less than end date";
    public static final String IS_NOT_PRESENT_IN_MDMS = " is not present in MDMS";
    public static final String TENANT_ID_IS_MANDATORY_IN_ACTIVITY_REQUEST_BODY = "Tenant ID is mandatory in Activity request body";
    public static final String DATA_IS_MANDATORY_IN_ACTIVITY_REQUEST_BODY = "Data are mandatory in BOM request body";
    public static final String DOES_NOT_EXISTS_FOR_THE_FIELDPLAN = " that you are trying to update does not exists for the FieldPlan ";
    @Autowired
    MDMSUtils mdmsUtils;

    @Autowired
    ActivityConfiguration config;

    @Autowired
    @Qualifier("objectMapper")
    ObjectMapper mapper;

    public BomValidator(ServiceRequestClient serviceRequestRepository, ActivityConfiguration activityConfiguration, ServiceRequestRepository serviceRequest, ActivityService activityService){
        this.serviceRequestRepository = serviceRequestRepository;
        this.activityConfiguration = activityConfiguration;
        this.serviceRequest = serviceRequest;
        this.activityService = activityService;
    }

    public void validateCreateBomRequest(BomBulkRequest request) {
        Map<String, String> errorMap = new HashMap<>();
        RequestInfo requestInfo = request.getRequestInfo();

        //Verify if RequestInfo and UserInfo is present
        validateRequestInfo(requestInfo);
        //Verify if ActivityAssignment request and mandatory fields are present
        validateBomRequest(request);

//        validateRequestMDMSData(request, request.getRequestInfo().getUserInfo().getTenantId(),errorMap);

        if (!errorMap.isEmpty())
            throw new CustomException(errorMap);
    }

    private void validateBomRequest(BomBulkRequest request) {
        Map<String, String> errorMap = new HashMap<>();

        if (request.getBillOfMaterials() == null || request.getBillOfMaterials().size() == 0) {
            log.error("Activity list is empty. Activity is mandatory");
            throw new CustomException("ACTIVITY", "Activity are mandatory");
        }

        for (BillOfMaterial billOfMaterial : request.getBillOfMaterials()) {
            if (billOfMaterial == null) {
                log.error("Activity Assignment is mandatory in Activities");
                throw new CustomException("Activity", "Activity is mandatory");
            }

            if (billOfMaterial.getName() == null) {
                log.error("Name is mandatory in FieldPlans");
                throw new CustomException("Activity_FACILITY", "Facility ID is mandatory");
            }

            if (billOfMaterial.getAssignUser() == null) {
                log.error("Assign User is mandatory in BOM");
                throw new CustomException("BOM_ASSIGN_USER", "Assign User is mandatory");
            }

//            if (billOfMaterial.getFacilityId() == null) {
//                log.error("Facility ID is mandatory in FieldPlans");
//                throw new CustomException("Activity_FACILITY", "Facility ID is mandatory");
//            }
//
//            // Get existing facility with facilityId from facility service
//            Facility existingfacility = getFacilityById(billOfMaterial.getFacilityId());
//            if (existingfacility == null) {
//                log.error("Facility ID do not exist");
//                throw new CustomException("Activity_ERROR", "Facility ID do not exist");
//            }

            if (billOfMaterial.getActivityFacilityId() == null) {
                log.error("Activity Facility ID is mandatory in FieldPlans");
                throw new CustomException("Activity_FACILITY", "Activity Facility ID is mandatory");
            }

            // 1. Fetch the existing facility
            List<ActivityFacility> activityFacilities = getActivityFacilityById(request.getRequestInfo(), billOfMaterial);
            if (activityFacilities == null || activityFacilities.isEmpty()) {
                throw new CustomException("ACTIVITY_FACILITY_NOT_FOUND", "Activity Facility not found with ID: " + billOfMaterial.getActivityFacilityId());
            }

            if (StringUtils.isBlank(billOfMaterial.getTenantId())) {
                log.error(TENANT_ID_IS_MANDATORY_IN_ACTIVITY_REQUEST_BODY);
                errorMap.put("TENANT_ID", "Tenant ID is mandatory");
            }
            if (billOfMaterial.getData() == null) {
                log.error(DATA_IS_MANDATORY_IN_ACTIVITY_REQUEST_BODY);
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

    /* Validate BOM Request MDMS data */
    private void validateRequestMDMSData(BomBulkRequest request, String tenantId, Map<String, String> errorMap) {
        String rootTenantId = tenantId.split("\\.")[0];

        //Get MDMS data using create project request and tenantId
        Object mdmsData = mdmsUtils.mDMSCall(request.getRequestInfo(), rootTenantId);

        validateMDMSData(request.getBillOfMaterials(), mdmsData, errorMap);
        log.info("Request data validated with MDMS");
    }

    /* Validates the request data against MDMS data */
    private void validateMDMSData(List<BillOfMaterial> billOfMaterials, Object mdmsData, Map<String, String> errorMap) {
        String mdmsRes = "$.MdmsRes.";
        final String jsonPathForBom = mdmsRes + MDMS_COMMON_MASTERS_MODULE_NAME + "." + BOM_FORM + ".*.name";

        List<Object> bomsNameRes = null;
        try {
            bomsNameRes = JsonPath.read(mdmsData, jsonPathForBom);
        } catch (Exception e) {
            log.error(e.getMessage());
            throw new CustomException("JSONPATH_ERROR", "Failed to parse mdms response");
        }

        for (BillOfMaterial billOfMaterial : billOfMaterials) {
            log.info("Validate Project type with MDMS");
            String mdmsNotPresent = IS_NOT_PRESENT_IN_MDMS;
            log.info("Validate BOM name with MDMS");
            if (!StringUtils.isBlank(billOfMaterial.getName()) && !bomsNameRes.contains(billOfMaterial.getName())) {
                log.error("The bom name: " + billOfMaterial.getName() + mdmsNotPresent);
                errorMap.put("INVALID_BOM_NAME", "The name: " + billOfMaterial.getName() + mdmsNotPresent);
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


    /* Validates search FieldPlan request body and parameters*/
    public void validateSearchBOMRequest(BomSearchRequest request, Integer limit, Integer offset, String tenantId) {
        Map<String, String> errorMap = new HashMap<>();
        RequestInfo requestInfo = request.getRequestInfo();

        //Verify if RequestInfo and UserInfo is present
        validateRequestInfo(requestInfo);
        //Verify if search fieldplan request parameters are valid
        validateSearchBOMRequestParams(limit, offset, tenantId);
        //Verify if search fieldplan request is valid
        validateSearchRequest(request.getCriteria(), tenantId);
        //Verify MDMS Data
        // TODO: Uncomment and fix as per HCM once we get clarity
        // validateRequestMDMSData(project, tenantId, errorMap);

        if (!errorMap.isEmpty())
            throw new CustomException(errorMap);
    }

    /* Validates if search Project request parameters are valid */
    private void validateSearchBOMRequestParams(Integer limit, Integer offset, String tenantId) {
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
    private void validateSearchRequest(BomSearchCriteria criteria, String tenantId) {
        checkFieldPlansIfEmpty(criteria);

        doNullAndEmptyChecks(tenantId, criteria);
    }

    private static void checkFieldPlansIfEmpty(BomSearchCriteria criteria) {
        if (criteria == null) {
            log.error("Activity is empty. Activity is mandatory");
            throw new CustomException("Activity", "Activity are mandatory");
        }
    }

    private static void doNullAndEmptyChecks(String tenantId, BomSearchCriteria criteria) {
        if (criteria == null) {
            log.error("fieldPlan is mandatory in FieldPlans");
            throw new CustomException("FIELDPLAN", "FieldPlan is mandatory");
        }
        if (StringUtils.isBlank(criteria.getTenantId())) {
            log.error(TENANT_ID_IS_MANDATORY_IN_ACTIVITY_REQUEST_BODY);
            throw new CustomException("TENANT_ID", "Tenant ID is mandatory");
        }
        if ((criteria.getIds()==null || criteria.getIds().isEmpty()) && (criteria.getFacilityId()==null || criteria.getFacilityId().isEmpty())
                && (criteria.getName()==null || criteria.getName().isEmpty()) && (criteria.getActivityFacilityId()==null || criteria.getActivityFacilityId().isEmpty())
                && StringUtils.isBlank(criteria.getAssignUser()))
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
    public void validateUpdateAgainstDB(List<BillOfMaterial> bomFromRequest, List<BillOfMaterial> bomListFromDB) {
        if (CollectionUtils.isEmpty(bomListFromDB)) {
            log.error("The BOM records that you are trying to update does not exists in the system");
            throw new CustomException("INVALID_BOM_UPDATE", "The records that you are trying to update does not exists in the system");
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
        for (BillOfMaterial billOfMaterial : bomFromRequest) {
            BillOfMaterial bomFromDB = bomListFromDB.stream().filter(p -> p.getId().equals(billOfMaterial.getId())).findFirst().orElse(null);

            if (bomFromDB == null) {
                log.error("The BOM id " + billOfMaterial.getId() + " that you are trying to update does not exists for the BOM");
                throw new CustomException("INVALID_BOM_UPDATE", "The BOM id " + billOfMaterial.getId() + " that you are trying to update does not exists for the BOM");
            }
        }
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

    public List<ActivityFacility> getActivityFacilityById(RequestInfo requestInfo, BillOfMaterial billOfMaterial){
        ActivityFacilitySearchCriteria searchCriteria = ActivityFacilitySearchCriteria.builder()
                .ids(List.of(billOfMaterial.getActivityFacilityId()))
                .tenantId(activityConfiguration.getTenantId())
                .build();

        ActivityFacilitySearchRequest searchRequest = ActivityFacilitySearchRequest.builder()
                .criteria(searchCriteria)
                .requestInfo(requestInfo)
                .build();

        List<ActivityFacility> activityFacilities = activityService.searchActivityFacility(searchRequest, activityConfiguration.getMaxLimit(), activityConfiguration.getDefaultOffset(),
                activityConfiguration.getTenantId(), false, null);

        return activityFacilities;

    }
}