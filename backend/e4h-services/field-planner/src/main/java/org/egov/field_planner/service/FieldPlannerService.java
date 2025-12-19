package org.egov.field_planner.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.jsonpath.JsonPath;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.egov.common.contract.models.AuditDetails;
import org.egov.common.contract.request.RequestInfo;
import org.egov.common.models.core.SearchResponse;
import org.egov.common.producer.Producer;
import org.egov.common.validator.Validator;
import org.egov.field_planner.config.FieldPlannerConfiguration;
import org.egov.field_planner.repository.FieldPlannerRepository;
import org.egov.field_planner.service.enrichment.FieldPlannerEnrichment;
import org.egov.field_planner.util.FieldPlannerServiceUtil;
import org.egov.field_planner.util.MDMSUtils;
import org.egov.field_planner.validator.FieldPlannerValidator;
import org.egov.field_planner.web.models.*;
import org.egov.tracer.model.CustomException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;
import java.util.stream.Collectors;

import static org.egov.field_planner.util.FieldPlannerConstants.*;

@Service
@Slf4j
public class FieldPlannerService {

    private final FieldPlannerValidator fieldPlannerValidator;
    private final FieldPlannerRepository fieldPlannerRepository;
    private final Producer producer;
    private final FieldPlannerEnrichment fieldPlannerEnrichment;

    private final FieldPlannerServiceUtil fieldPlanServiceUtil;

    private final List<Validator<FieldPlanFacilityBulkRequest, FieldPlanFacility>> validators;
    private final FieldPlannerConfiguration fieldPlannerConfiguration;
    private final MDMSUtils mdmsUtils;
    private final ServiceRequestRepository serviceRequestRepository;

    private final FieldPlannerFacilityService facilityService;

    @Autowired
    @Qualifier("objectMapper")
    ObjectMapper mapper;

    @Autowired
    public FieldPlannerService(
            FieldPlannerRepository fieldPlannerRepository, List<Validator<FieldPlanFacilityBulkRequest, FieldPlanFacility>> validators, FieldPlannerFacilityService facilityService,
            FieldPlannerValidator fieldPlannerValidator, FieldPlannerEnrichment fieldPlannerEnrichment, FieldPlannerConfiguration fieldPlannerConfiguration,
            Producer producer, MDMSUtils mdmsUtils, FieldPlannerServiceUtil fieldPlanServiceUtil, ServiceRequestRepository serviceRequestRepository) {
            this.fieldPlannerValidator = fieldPlannerValidator;
            this.producer = producer;
            this.fieldPlannerConfiguration = fieldPlannerConfiguration;
            this.fieldPlannerRepository = fieldPlannerRepository;
            this.fieldPlannerEnrichment = fieldPlannerEnrichment;
            this.mdmsUtils = mdmsUtils;
            this.validators = validators;
            this.fieldPlanServiceUtil = fieldPlanServiceUtil;
            this.serviceRequestRepository = serviceRequestRepository;
            this.facilityService = facilityService;
    }

    public FieldPlanRequest createFieldPlan(FieldPlanRequest fieldPlanRequest) {
        fieldPlannerValidator.validateCreateFieldPlanRequest(fieldPlanRequest);
        for (FieldPlan fieldPlan : fieldPlanRequest.getFieldPlans()) {
            String baseName = getStateActivitiesYearFormat(fieldPlanRequest, fieldPlan.getTenantId(), fieldPlan);
//            String baseName = "KA-MT_HO-2024";
            if(baseName == null){
                throw new CustomException("FORMAT ERROR", "Cannot generate the fieldplan name");
            };
            fieldPlan.setName(baseName);
            NameResult result = CheckDuplicateAndGenerateName(fieldPlan);
            if (result.isDuplicate()) {
                fieldPlan.setIsDuplicate(true);
                fieldPlan.setName(result.getGeneratedName());
                log.info("Duplicate found. Using generated name: " + result.getGeneratedName());
//                return fieldPlanRequest;
            } else {
                log.info("No duplicate. Name is: " + result.getGeneratedName());
            }
            fieldPlannerEnrichment.enrichFieldPlanOnCreate(fieldPlan, fieldPlanRequest.getRequestInfo());
            log.info("Enriched with FieldPlan Ids and AuditDetails {}", fieldPlan);
            producer.push(fieldPlannerConfiguration.getSaveFieldPlanTopic(), fieldPlanRequest);
            log.info("Pushed to kafka");
        }
        return fieldPlanRequest;
    }

    public FieldPlanRequest updateFieldPlan(FieldPlanRequest request) {
        /*
         * Validate the update fieldPlan request
         */
        fieldPlannerValidator.validateUpdateFieldPlanRequest(request);
        log.info("Update fieldplan request validated");

        /*
         * Search for fieldplan based on fieldplan IDs provided in the request
         */
        List<FieldPlan> fieldPlansFromDB = searchFieldPlan(
                getSearchFieldPlanRequest(request.getFieldPlans(), request.getRequestInfo()),
                fieldPlannerConfiguration.getMaxLimit(), fieldPlannerConfiguration.getDefaultOffset(),
                request.getFieldPlans().get(0).getTenantId(), false, null, null, null);
        log.info("Fetched fieldPlan for update request");

        /*
         * Validate the update fieldplan request against the fieldplans fetched from the database
         */
        fieldPlannerValidator.validateUpdateAgainstDB(request.getFieldPlans(), fieldPlansFromDB);

        /*
         * Process each fieldPlan in the update request
         */
        for (FieldPlan fieldPlan : request.getFieldPlans()) {
            processFieldPlanUpdate(request, fieldPlan, fieldPlansFromDB);
        }

        return request;
    }

    public Integer countAllFieldPlans(FieldPlanSearchRequest request, String tenantId, Long lastChangedSince, Boolean includeDeleted) {
        return fieldPlannerRepository.getFieldPlanCount(request, tenantId, lastChangedSince, includeDeleted);
    }

    public NameResult CheckDuplicateAndGenerateName(FieldPlan fieldPlan) {
        boolean isDuplicate = false;
        String baseName = fieldPlan.getName();
        String generatedName = baseName;
        List<FieldPlan> fieldPlans = fieldPlannerRepository.getHighestFielPlanName(fieldPlan);
        if (fieldPlans!=null && !fieldPlans.isEmpty()){
            FieldPlan fieldPlanDB = fieldPlans.get(0);
            isDuplicate = true;
            int nextSuffix = extractAndIncrementSuffix(fieldPlanDB.getName(), baseName);
            generatedName = baseName+ "-" + nextSuffix;
        }

        return new NameResult(isDuplicate, generatedName);
    }

    private int extractAndIncrementSuffix(String existingName, String baseName) {
        if (existingName == null || !existingName.startsWith(baseName)) {
            return 1;
        }

        try {
            // Extract the part after base name
            String suffixPart = existingName.substring(baseName.length());

            // Remove leading dash if present
            if (suffixPart.startsWith("-")) {
                suffixPart = suffixPart.substring(1);
            }

            // Parse the suffix number
            int currentSuffix = Integer.parseInt(suffixPart);
            return currentSuffix + 1;

        } catch (NumberFormatException e) {
            log.warn("Could not parse suffix from existing name: {}", existingName);
            return 1;
        }
    }

    private String getStateActivitiesYearFormat(FieldPlanRequest request, String tenantId, FieldPlan fieldPlan) {
        //Get MDMS data using create fieldPlan request and tenantId
        Object mdmsData = mdmsUtils.mDMSCall(request, tenantId);
        String mdmsRes = "$.MdmsRes.";
        final String jsonPathForActivities = mdmsRes + MDMS_COMMON_MASTERS_MODULE_NAME + "." + MASTER_ACTIVITIES;
        final String jsonPathForStateInfo = mdmsRes + MDMS_COMMON_MASTERS_MODULE_NAME + "." + MASTER_STATE_INFO;

        List<Object> activitiesRes = null;
        List<Object> stateInfoRes = null;
        String baseName = null;
        String stateCode = null;
        String concatenatedActivityCode = null;
        Map<String, Object> geographyDetails = fieldPlan.getGeographyDetails();
        String stateBoundary = (String)geographyDetails.get("state");
        String state = fieldPlanServiceUtil.extractStateName(stateBoundary);
        List<Map<String, Object>> activities = fieldPlan.getActivities();
        try {
            activitiesRes = JsonPath.read(mdmsData, jsonPathForActivities);
            stateInfoRes = JsonPath.read(mdmsData, jsonPathForStateInfo);
            for (Object map : stateInfoRes) {
                LinkedHashMap<String, Object> stateInfo = (LinkedHashMap<String, Object>) map;
                String name = (String) stateInfo.get("name");
                if (state.equalsIgnoreCase(name)) {
                    stateCode = (String) stateInfo.get("code");
                    break;
                }
            }

            concatenatedActivityCode = activities.stream()
                    .map(activity -> (String) activity.get("code"))
                    .collect(Collectors.joining("_"));

            LocalDateTime endDate = LocalDateTime.ofInstant(
                    Instant.ofEpochMilli(fieldPlan.getEndDate()),
                    ZoneId.systemDefault()
            );
            int endYear = endDate.getYear();

            baseName = String.format("%s-%s-%s", stateCode, concatenatedActivityCode, endYear);
//
//            for (Object map : activitiesRes) {
//                LinkedHashMap<String, Object> activity = (LinkedHashMap<String, Object>) map;
//                String name = (String) activity.get("name");
//                if (state.equalsIgnoreCase(name)) {
//                    stateCode = (String) activity.get("code");
//                    break; // on s’arrête dès qu’on trouve
//                }
//            }
        } catch (Exception e) {
            log.error(e.getMessage());
            throw new CustomException("JSONPATH_ERROR", "Failed to parse mdms response");
        }


        return baseName;
    }

    /**
     * Checks if any data that affects field plan name generation has changed
     * Name is affected by: endDate, activity, address.boundary (state)
     */
    private boolean hasNameAffectingDataChanged(FieldPlan fieldPlan, FieldPlan fieldPlanFromDB) {
        // Check if end date changed
        if (!Objects.equals(fieldPlan.getEndDate(), fieldPlanFromDB.getEndDate())) {
            log.info("End date changed for field plan: {} - name regeneration needed", fieldPlan.getId());
            return true;
        }

        // Check if activity details changed
        List<Map<String, Object>> currentActivities = fieldPlan.getActivities() != null ? fieldPlan.getActivities() : null;
        List<Map<String, Object>> existingActivities = fieldPlanFromDB.getActivities() != null ? fieldPlanFromDB.getActivities() : null;
        if (!Objects.equals(currentActivities, existingActivities)) {
            log.info("Activity list changed for field plan: {} - name regeneration needed", fieldPlan.getId());
            return true;
        }

        log.info("No name-affecting data changed for field plan: {}", fieldPlan.getId());
        return false;
    }

    /**
     * Handles fieldPlan name regeneration during updates
     * Compares the new base name with existing name and updates if different
     */
    private void handleFieldPlanNameUpdate(FieldPlanRequest request, FieldPlan fieldPlan, FieldPlan fieldPlanFromDB) {
        try {

            // Check if name-affecting data has changed
            if (!hasNameAffectingDataChanged(fieldPlan, fieldPlanFromDB)) {
                log.info("No name-affecting data changed for field plan: {}, keeping existing name: {}",
                        fieldPlan.getId(), fieldPlanFromDB.getName());
                return;
            }

            String newBaseName = getStateActivitiesYearFormat(request, fieldPlan.getTenantId(), fieldPlan);
//            String baseName = "KA-MT_HO-2024";
            if(newBaseName == null){
                throw new CustomException("FORMAT ERROR", "Cannot generate the fieldplan name");
            };

            String existingName = fieldPlanFromDB.getName();
            // Extract base name from existing name (remove any suffix like -1, -2, etc.)
            String existingBaseName = removeLastSuffix(existingName);
            if (newBaseName.equals(existingBaseName)) {
                log.info("FieldPlan name unchanged. Existing: {}, New base: {}", existingName, newBaseName);
                return;
            }

            log.info("FieldPlan name needs update. Existing: {}, New: {}", existingName, newBaseName);
            fieldPlan.setName(newBaseName);
            NameResult result = CheckDuplicateAndGenerateName(fieldPlan);
            if (result.isDuplicate()) {
                fieldPlan.setIsDuplicate(true);
                fieldPlan.setName(result.getGeneratedName());
                log.info("Duplicate found. Using generated name: " + result.getGeneratedName());
//                return fieldPlanRequest;
            } else {
                log.info("No duplicate. Name is: " + result.getGeneratedName());
            }

        } catch (Exception e) {
            log.error("Error handling fieldPlan name update for fieldPlan: {}", fieldPlan.getId(), e);
            // Don't throw exception - continue with update even if name generation fails
        }
    }

    public static String removeLastSuffix(String code) {
        if (code == null || code.isEmpty()) {
            return code;
        }

        int lastDash = code.lastIndexOf('-');
        if (lastDash == -1) {
            return code; // pas de tiret donc rien à enlever
        }

        String suffix = code.substring(lastDash + 1);

        // Vérifie si le suffixe est numérique OU alphanumérique
        if (suffix.matches("[A-Za-z0-9]+")) {
            return code.substring(0, lastDash); // enlève le suffixe
        }

        return code; // si le suffixe contient autre chose, on garde
    }

    /* Construct FieldPlan Request object for search which contains fieldplan id and tenantId */
    private FieldPlanSearchRequest getSearchFieldPlanRequest(List<FieldPlan> fieldPlans, RequestInfo requestInfo) {
        List<String> fieldPlanIds = fieldPlans.stream().map(FieldPlan::getId).toList();
        FieldPlanSearchCriteria criteria = FieldPlanSearchCriteria.builder().ids(fieldPlanIds).tenantId(fieldPlans.get(0).getTenantId()).build();
        return FieldPlanSearchRequest.builder()
                .requestInfo(requestInfo)
                .fieldPlan(criteria)
                .build();
    }

    public List<FieldPlan> searchFieldPlan(FieldPlanSearchRequest request, Integer limit, Integer offset, String tenantId, Boolean includeDeleted, Long lastChangedSince, Long createdFrom, Long createdTo) {
        fieldPlannerValidator.validateSearchFieldPlanRequest(request, limit, offset, tenantId, createdFrom, createdTo);
        List<FieldPlan> fieldPlanList = fieldPlannerRepository.getFieldPlans(request, limit, offset, tenantId, includeDeleted, lastChangedSince, createdFrom, createdTo);
        return fieldPlanList;
    }

    private void processFieldPlanUpdate(FieldPlanRequest request, FieldPlan fieldPlan, List<FieldPlan> fieldPlansFromDB) {
        /*
         * Convert fieldplan ID to string for comparison
         */
        String fieldPlanId = String.valueOf(fieldPlan.getId());

        /*
         * Find the fieldPlan from the database that matches the current fieldPlan ID
         */
        FieldPlan fielPlanFromDB = findFieldPlanById(fieldPlanId, fieldPlansFromDB);
        boolean isCascadingFieldPlanDateUpdate = request.isCascadingFieldPlanDateUpdate();

        if (fielPlanFromDB != null) {
            /*
             * Check if geography details (boundary codes) have changed and unlink facilities if needed
             */
//            handleFacilityUnlinkingOnGeographyChange(request, fieldPlan, fielPlanFromDB);

            /*
             * Merge additional details of the fieldPlan from the request and fieldPlan from DB
             */
            fieldPlanServiceUtil.mergeAdditionalDetails(fieldPlan, fielPlanFromDB);

            /*
             * Handle cases where cascading fieldPlan date update is true
             */
            if (isCascadingFieldPlanDateUpdate) {
                handleUpdateFieldPlan(request, fieldPlan, fielPlanFromDB);
            }
        }
    }

    private void handleUpdateFieldPlan(FieldPlanRequest request, FieldPlan fieldPlan, FieldPlan fieldPlanFromDB) {
        /*
         * Save original values of start date, end date, and additional details
         */
        Long originalStartDate = fieldPlanFromDB.getStartDate();
        Long originalEndDate = fieldPlanFromDB.getEndDate();
        Object originalGeographyDetails = fieldPlanFromDB.getGeographyDetails();
        Object originalActivity = fieldPlanFromDB.getActivities();
        AuditDetails originalAuditDetails = fieldPlanFromDB.getAuditDetails();


        /*
         * Update the fieldPlan with new start date, end date, and additional details
         */
        fieldPlanFromDB.setStartDate(fieldPlan.getStartDate());
        fieldPlanFromDB.setEndDate(fieldPlan.getEndDate());
        fieldPlanFromDB.setGeographyDetails(fieldPlan.getGeographyDetails());
        fieldPlanFromDB.setActivities(fieldPlan.getActivities());
        fieldPlanFromDB.setAuditDetails(fieldPlan.getAuditDetails());

        /*
         * Ensure that no other properties are being updated besides the start and end dates
         */
        if (!isValidCascadingUpdate(fieldPlanFromDB, fieldPlan)) {
            throw new CustomException(
                    "FIELDPLANE_CASCADE_UPDATE_ERROR",
                    "Can only update FieldPlan dates, geographyDetails and additional details if cascade FieldPlan date update true"
            );
        }

        /*
         * Restore original values of start date, end date, and additional details
         */
        fieldPlanFromDB.setStartDate(originalStartDate);
        fieldPlanFromDB.setEndDate(originalEndDate);
        fieldPlanFromDB.setGeographyDetails(mapper.convertValue(originalGeographyDetails, Map.class));
        fieldPlanFromDB.setActivities((List<Map<String, Object>>) originalActivity);
        fieldPlanFromDB.setAuditDetails(originalAuditDetails);

        /*
         * Update lastModifiedTime and lastModifiedBy for the fieldPlan
         */
        fieldPlannerEnrichment.enrichFieldPlanRequestOnUpdate(fieldPlan, fieldPlanFromDB, request.getRequestInfo());

        // If status equals to scheduled, so dont update the fieldplan name
        if(StringUtils.equals(fieldPlan.getStatus(), "SCHEDULED")){
            try {
                // Check if INSTALLATION_REVIEWER and INSTALLATION_SPOC is assigned and if at least one facility is linked to the fieldplan
                if (fieldPlan == null) {
                    log.error("Field Plan is mandatory");
                    throw new CustomException("FIELDPLAN", "Field Plan is mandatory");
                }
                if (fieldPlan.getId() == null) {
                    log.error("FieldPlan ID is mandatory");
                    throw new CustomException("FIELDPLAN", "FieldPlan ID");
                }

                List<ActivityAssignment> activityAssignmentList = getFieldPlanActivityAssignment(request, fieldPlan);
                if(activityAssignmentList==null || activityAssignmentList.isEmpty()){
                    log.error("Activity Assignment is empty for the fieldplan");
                    throw new CustomException("FIELDPLAN", "Activity Assignment is empty for the fieldplan");
                }
                if(!hasSpocAndReviewer(activityAssignmentList)){
                    throw new CustomException("FIELDPLAN", "INSTALLATION_REVIEWER and INSTALLATION_SPOC need to be assigned for the fieldplan");
                }

                sendActivityAssignmentEmail(request, activityAssignmentList);

                SearchResponse<FieldPlanFacility> fieldPlanFacilitySearchResponse = getFieldPlanFacilities(request, fieldPlan);
                if(fieldPlanFacilitySearchResponse== null || fieldPlanFacilitySearchResponse.getResponse().isEmpty() || fieldPlanFacilitySearchResponse.getTotalCount()==0){
                    log.error("No facility is linked to the fieldplan");
                    throw new CustomException("FIELDPLAN", "No facility is linked to the fieldplan");
                }
                // Call facility activity create with bulk facility activity
                List<FieldPlanFacility> fieldPlanFacilities = fieldPlanFacilitySearchResponse.getResponse();
                if (fieldPlanFacilities != null && !fieldPlanFacilities.isEmpty()){
                    List<ActivityFacility> activityFacilities = new ArrayList();

                    // On groupe par role.code et on récupère la liste des ids
                    Map<String, List<String>> roleToIds = activityAssignmentList.stream()
                            .filter(item -> item.getRole() != null)
                            .collect(Collectors.groupingBy(
                                    item -> (String) ((Map<String, Object>) item.getRole()).get("code"),
                                    Collectors.mapping(item -> (String) item.getAssignedTo(), Collectors.toList())
                            ));
                    for (FieldPlanFacility fieldPlanFacility : fieldPlanFacilities){
                        ActivityFacility activityFacility = ActivityFacility.builder()
                                .tenantId("in")
                                .fieldPlanId(fieldPlanFacility.getFieldPlanId())
                                .facilityId(fieldPlanFacility.getFacilityId())
                                .activityId((String)fieldPlan.getActivities().get(0).get("code"))
                                .scheduledAt(fieldPlan.getStartDate())
                                .activatedAt(fieldPlan.getStartDate())
                                .reviewerUser(roleToIds.get("INSTALLATION_REVIEWER"))
                                .fieldStaffUsers(roleToIds.get("INSTALLATION_SPOC"))
                                .fieldSupervisorUsers(roleToIds.get("INSTALLATION_SPOC"))
                                .build();

                        activityFacilities.add(activityFacility);
                    }

                    createFacilityActivity(request.getRequestInfo(), activityFacilities);

                }
            } catch (Exception e) {
                e.printStackTrace();
                throw new RuntimeException(e);
            }
        }

        /*
         * Handle fieldPlan name regeneration if needed (dates changed or activity)
         */
        handleFieldPlanNameUpdate(request, fieldPlan, fieldPlanFromDB);

        /*
         * Check and enrich cascading fieldPlan dates and push the update to the message broker
         */
        producer.push(fieldPlannerConfiguration.getUpdateFieldPlanTopic(), request);
    }

    private boolean isValidCascadingUpdate(FieldPlan fieldPlanFromDB, FieldPlan fieldPlan) {
        // Check if only allowed fields are being updated
        return Objects.equals(fieldPlanFromDB.getId(), fieldPlan.getId()) &&
                Objects.equals(fieldPlanFromDB.getTenantId(), fieldPlan.getTenantId()) &&
                isValidGeographyDetailsUpdate(fieldPlanFromDB.getGeographyDetails(), fieldPlan.getGeographyDetails());
        // Note: We allow startDate, endDate, name, geographyDetails, activities and auditDetails to be different
    }

    /**
     * Validates if only allowed fields in additionalDetails are being updated
     * Allowed: geographyDetails (districts, blocks)
     * Read-only: justificationCode field
     */
    private boolean isValidGeographyDetailsUpdate(Object originalGeographyDetails, Object newGeographyDetails) {
        if (originalGeographyDetails == null && newGeographyDetails == null) {
            return true;
        }
        if (originalGeographyDetails == null || newGeographyDetails == null) {
            return false;
        }

        try {
            // Convert to JsonNode for easier comparison
            JsonNode originalNode = mapper.valueToTree(originalGeographyDetails);
            JsonNode newNode = mapper.valueToTree(newGeographyDetails);

            // Check if state is unchanged (read-only)
            JsonNode originalState = originalNode.get("state");
            JsonNode newState = newNode.get("state");
            if (!Objects.equals(originalState, newState)) {
                log.warn("State cannot be changed during cascading update");
                return false;
            }

            return true;

        } catch (Exception e) {
            log.error("Error validating geographyDetails update", e);
            return false;
        }
    }

    private FieldPlan findFieldPlanById(String fieldPlanId, List<FieldPlan> fieldPlansFromDB) {
        /*
         * Find and return the fieldPlan with the matching ID from the list of fieldplan fetched from the database
         */
        return fieldPlansFromDB.stream()
                .filter(p -> fieldPlanId.equals(String.valueOf(p.getId())))
                .findFirst()
                .orElse(null);
    }

    public List<ActivityAssignment> getFieldPlanActivityAssignment(FieldPlanRequest request, FieldPlan fieldPlan) {
        String fieldPlanId = fieldPlan.getId();
        ActivityAssignmentSearchCriteria criteria = ActivityAssignmentSearchCriteria.builder().fieldPlanId(List.of(fieldPlanId)).tenantId(fieldPlan.getTenantId()).build();
        ActivityAssignmentSearchRequest assignmentSearchRequest = ActivityAssignmentSearchRequest.builder().criteria(criteria).requestInfo(request.getRequestInfo()).build();
        String url = fieldPlannerConfiguration.getFieldPlanActivityServiceHost() + fieldPlannerConfiguration.getFieldPlanActivitySearchUrl()+ "?tenantId="+fieldPlan.getTenantId()+"&offset=0&limit=100";
        Object response = serviceRequestRepository.fetchResult(new StringBuilder(url), assignmentSearchRequest);
        ActivityAssignmentResponse activityAssignmentList = mapper.convertValue(response, ActivityAssignmentResponse.class);
        if(activityAssignmentList != null && activityAssignmentList.getActivityAssignment() !=null){
            return activityAssignmentList.getActivityAssignment();
        }
        return null;
    }

    public List<ActivityAssignment> updateFieldPlanActivityAssignment(FieldPlanRequest request, List<ActivityAssignment> activityAssignmentList) {
        ActivityAssignmentBulkRequest activityAssignmentBulkRequest = ActivityAssignmentBulkRequest.builder()
                .requestInfo(request.getRequestInfo())
                .activityAssignments(activityAssignmentList)
                .build();
        String tenantId = activityAssignmentList.get(0).getTenantId();
        String url = fieldPlannerConfiguration.getFieldPlanActivityServiceHost() + fieldPlannerConfiguration.getFieldPlanActivityUpdateUrl()+ "?tenantId="+tenantId+"&offset=0&limit=100";
        Object response = serviceRequestRepository.fetchResult(new StringBuilder(url), activityAssignmentBulkRequest);
        ActivityAssignmentResponse assignmentResponse = mapper.convertValue(response, ActivityAssignmentResponse.class);
        if(assignmentResponse != null && assignmentResponse.getActivityAssignment() !=null){
            return assignmentResponse.getActivityAssignment();
        }
        return null;
    }

    public void createFacilityActivity(RequestInfo requestInfo, List<ActivityFacility> activityFacilities) {
        ActivityFacilityBulkRequest request = ActivityFacilityBulkRequest.builder().activityFacilities(activityFacilities).requestInfo(requestInfo).build();
        String url = fieldPlannerConfiguration.getFieldPlanActivityServiceHost() + fieldPlannerConfiguration.getFacilityActivityCreateUrl();
        Object response = serviceRequestRepository.fetchResult(new StringBuilder(url), request);
        ActivityFacilityResponse activityFacilityResponse = mapper.convertValue(response, ActivityFacilityResponse.class);
        log.info("All facility activities are added");
    }

    public boolean hasSpocAndReviewer(List<ActivityAssignment> activityAssignmentList) {
        boolean hasSpoc = false;
        boolean hasReviewer = false;

        for (ActivityAssignment assignment : activityAssignmentList) {
            Map<String, Object> roleMap = assignment.getRole();
            if ("INSTALLATION_SPOC".equalsIgnoreCase((String) roleMap.get("code"))) {
                hasSpoc = true;
            }
            if ("INSTALLATION_REVIEWER".equalsIgnoreCase((String) roleMap.get("code"))) {
                hasReviewer = true;
            }
            if (hasSpoc && hasReviewer) {
                return true;
            }
        }

        return false;
    }

    /**
     * Gets all facilities currently linked to a project
     */
    private List<FieldPlanFacility> getFacilitiesLinkedToFacility(String fieldPlanId, String tenantId, RequestInfo requestInfo) {
        try {
            List<String> fieldPlanIds = new ArrayList<>();
            fieldPlanIds.add(fieldPlanId);

            FieldPlanFacilitySearch projectFacilitySearch = FieldPlanFacilitySearch.builder()
                    .facility_id(fieldPlanIds)
                    .facility_id(null)
                    .build();

            FieldPlanFacilitySearchRequest projectFacilitySearchRequest = FieldPlanFacilitySearchRequest.builder()
                    .criteria(projectFacilitySearch)
                    .requestInfo(requestInfo)
                    .build();

            SearchResponse<FieldPlanFacility> searchResponse = facilityService.search(
                    projectFacilitySearchRequest,
                    1000, // Large limit to get all facilities
                    0,
                    tenantId,
                    null,
                    false
            );

            return (searchResponse != null && searchResponse.getResponse() != null)
                    ? searchResponse.getResponse()
                    : new ArrayList<>();

        } catch (Exception e) {
            log.error("Error getting facilities linked to project: {}", fieldPlanId, e);
            return new ArrayList<>();
        }
    }

    public SearchResponse<FieldPlanFacility> getFieldPlanFacilities(FieldPlanRequest request, FieldPlan fieldPlan) throws Exception {
        List<String> listFieldPlanId = new ArrayList<>();
        listFieldPlanId.add(fieldPlan.getId());
        FieldPlanFacilitySearch criteria = FieldPlanFacilitySearch.builder().field_plan_id(listFieldPlanId).build();
        FieldPlanFacilitySearchRequest searchRequest =  FieldPlanFacilitySearchRequest.builder().requestInfo(request.getRequestInfo()).criteria(criteria).build();
        SearchResponse<FieldPlanFacility> response = facilityService.search(searchRequest, fieldPlannerConfiguration.getMaxLimit(), fieldPlannerConfiguration.getDefaultOffset(),
                request.getFieldPlans().get(0).getTenantId(), null, false);

        return response;
    }

    /**
     * Handles facility unlinking when geography details (boundary codes) are changed
     * Only processes unlinking when geographyDetails is explicitly present in the request
     * Only allows unlinking for Draft projects (status = null)
     */
    private void handleFacilityUnlinkingOnGeographyChange(FieldPlanRequest request, FieldPlan fieldPlan, FieldPlan fieldPlanFromDB) {
        try {
            // Guard: Only process unlinking if geographyDetails is explicitly present in the request
            if (fieldPlan.getGeographyDetails() == null) {
                log.debug("No geographyDetails in request for field plan: {} - skipping facility unlinking", fieldPlan.getId());
                return;
            }

            // STATUS CHECK: Only allow facility unlinking for Draft field plan (status = null or missing)
            String fieldPlanStatus = fieldPlan.getStatus();
            if (!fieldPlanStatus.equals(DRAFT_STATUS)) {
                log.info("Field Plan {} has status '{}' - facility unlinking not allowed. Only Draft field  plans (status=DRAFT) can unlink facilities.",
                        fieldPlan.getId(), fieldPlanStatus);
                return;
            }
            // Extract boundary codes from old and new geography details
            Set<String> oldBoundaryCodes = extractBoundaryCodesFromGeographyDetails(fieldPlanFromDB.getGeographyDetails());
            Set<String> newBoundaryCodes = extractBoundaryCodesFromGeographyDetails(fieldPlan.getGeographyDetails());

            // Check if boundary codes have changed
            if (!oldBoundaryCodes.equals(newBoundaryCodes)) {
                log.info("Geography details changed for field  plan: {}. Old boundaries: {}, New boundaries: {}",
                        fieldPlan.getId(), oldBoundaryCodes, newBoundaryCodes);

                // Unlink facilities that are no longer associated with the new boundary codes
                unlinkFieldplanFacilities(fieldPlan.getId(), fieldPlan.getTenantId(), request.getRequestInfo(), newBoundaryCodes);
            } else {
                log.debug("Geography details unchanged for project: {} - no facility unlinking needed", fieldPlan.getId());
            }
        } catch (Exception e) {
            log.error("Error handling facility unlinking for project: {}", fieldPlan.getId(), e);
            // Don't throw exception - continue with update even if facility unlinking fails
        }
    }

    /**
     * Extracts boundary codes from geography details in additional details
     */
    private Set<String> extractBoundaryCodesFromGeographyDetails(Object geographyDetails) {
        Set<String> boundaryCodes = new HashSet<>();

        if (geographyDetails == null) {
            return boundaryCodes;
        }

        try {
            JsonNode geographyDetailsNode = mapper.valueToTree(geographyDetails);
            if (geographyDetailsNode != null) {
                // Extract boundary codes from blocks
                JsonNode blocks = geographyDetailsNode.get("blocks");
                if (blocks != null && blocks.isArray()) {
                    for (JsonNode block : blocks) {
                        JsonNode code = block.get("code");
                        if (code != null && !code.isNull()) {
                            boundaryCodes.add(code.asText());
                        }
                    }
                }
            }
        } catch (Exception e) {
            log.error("Error extracting boundary codes from geography details", e);
        }

        return boundaryCodes;
    }

    /**
     * Unlinks facilities that are no longer associated with the fieldplan's new boundary codes
     */
    private void unlinkFieldplanFacilities(String fieldPlanId, String tenantId, RequestInfo requestInfo, Set<String> newBoundaryCodes) {
        try {
            log.info("Starting selective facility unlinking for field plan: {} with new boundary codes: {}", fieldPlanId, newBoundaryCodes);

            // Step 1: Get all facilities currently linked to the field plan
            List<FieldPlanFacility> linkedFieldPlanFacilities = getFacilitiesLinkedToFacility(fieldPlanId, tenantId, requestInfo);

            if (linkedFieldPlanFacilities.isEmpty()) {
                log.info("No facilities currently linked to field plan: {}", fieldPlanId);
                return;
            }

            // Step 2: Get all facilities associated with the new boundary codes
            Set<String> facilitiesInNewBoundaries = getFacilitiesByBoundaryCodes(newBoundaryCodes, tenantId, requestInfo);

            // Defensive guard: if boundaries are non-empty but lookup yielded zero, skip unlink to avoid data loss
            if (!newBoundaryCodes.isEmpty() && facilitiesInNewBoundaries.isEmpty()) {
                log.warn("Facility lookup returned 0 results for non-empty boundaries {}. Skipping unlink to avoid accidental data loss for field plan: {}",
                        newBoundaryCodes, fieldPlanId);
                return;
            }

            // Step 3: Find facilities to unlink (linked to field plan but not in new boundary codes)
            List<FieldPlanFacility> facilitiesToUnlink = linkedFieldPlanFacilities.stream()
                    .filter(projectFacility -> !facilitiesInNewBoundaries.contains(projectFacility.getFacilityId()))
                    .collect(Collectors.toList());

            if (facilitiesToUnlink.isEmpty()) {
                log.info("No facilities need to be unlinked for field plan: {}", fieldPlanId);
                return;
            }

            log.info("Found {} facilities to unlink out of {} linked facilities for field plan: {}",
                    facilitiesToUnlink.size(), linkedFieldPlanFacilities.size(), fieldPlanId);

            // Step 4: Set isDeleted = true for the identified facilities using update API
            List<FieldPlanFacility> facilitiesToUpdate = facilitiesToUnlink.stream()
                    .map(fieldplanFacility -> {
                        // Create a copy with isDeleted = true
                        return FieldPlanFacility.builder()
                                .id(fieldplanFacility.getId())
                                .fieldPlanId(fieldplanFacility.getFieldPlanId())
                                .facilityId(fieldplanFacility.getFacilityId())
                                .tenantId(fieldplanFacility.getTenantId())
                                .isDeleted(true) // Set isDeleted = true
                                .rowVersion(fieldplanFacility.getRowVersion())
                                .auditDetails(fieldplanFacility.getAuditDetails())
                                .build();
                    })
                    .collect(Collectors.toList());

            // Use update API to set isDeleted = true
            FieldPlanFacilityBulkRequest updateRequest = FieldPlanFacilityBulkRequest.builder()
                    .requestInfo(requestInfo)
                    .fieldPlanFacilities(facilitiesToUpdate)
                    .build();

            facilityService.unassignBulk(updateRequest, true);

            log.info("Successfully unlinked {} facilities for project: {} by setting isDeleted=true", facilitiesToUpdate.size(), fieldPlanId);

        } catch (Exception e) {
            log.error("Error unlinking facilities for project: {}", fieldPlanId, e);
            throw new CustomException("FACILITY_UNLINKING_FAILED",
                    "Failed to unlink facilities for project: " + fieldPlanId + ". Error: " + e.getMessage());
        }
    }

    /**
     * Gets all facility IDs associated with the given boundary codes
     */
    private Set<String> getFacilitiesByBoundaryCodes(Set<String> boundaryCodes, String tenantId, RequestInfo requestInfo) {
        Set<String> facilityIds = new HashSet<>();

        if (boundaryCodes.isEmpty()) {
            return facilityIds;
        }

        try {
            // Search facilities by boundary codes
            for (String boundaryCode : boundaryCodes) {
                Set<String> facilitiesForBoundary = facilityService.searchFacilitiesByBoundaryCode(boundaryCode, tenantId, requestInfo);
                facilityIds.addAll(facilitiesForBoundary);
            }

            log.info("Found {} unique facilities across {} boundary codes", facilityIds.size(), boundaryCodes.size());

        } catch (Exception e) {
            log.error("Error getting facilities by boundary codes: {}", boundaryCodes, e);
        }

        return facilityIds;
    }

    private void sendActivityAssignmentEmail(FieldPlanRequest request, List<ActivityAssignment> activityAssignmentList){
        for (ActivityAssignment activityAssignment : activityAssignmentList) {
            log.info("processing {} valid entities", activityAssignment);
            if(activityAssignment.getAssignedTo() !=null && !activityAssignment.getAssignedTo().isEmpty() && !activityAssignment.getIsEmailSent()){
                Employee employee =  getUserById(request, activityAssignment.getAssignedTo());
                List<FieldPlan> fieldPlans = searchFieldPlan(
                        getSearchFieldPlanRequest(request.getFieldPlans(), request.getRequestInfo()),
                        fieldPlannerConfiguration.getMaxLimit(), fieldPlannerConfiguration.getDefaultOffset(),
                        request.getFieldPlans().get(0).getTenantId(), false, null, null, null);
                if(employee != null && fieldPlans != null && !fieldPlans.isEmpty()){
                    FieldPlan existingFieldPlan = fieldPlans.get(0);
                    String emailId = employee.getUser().getEmailId();
                    String username = employee.getUser().getUserName();
                    String subject = fieldPlannerConfiguration.getActivityEmailSubject();
                    String body = fieldPlanServiceUtil.replaceActivityAssignmentEmailBody((String)activityAssignment.getRole().get("name"), existingFieldPlan.getName(),
                            username,fieldPlannerConfiguration.getDefaultUserPassword(),fieldPlannerConfiguration.getActivityEmailBody());
                    fieldPlanServiceUtil.sendEmailViaKafka(emailId, subject, body, "in");
                    activityAssignment.setIsEmailSent(true);
                }
            }
        }

        updateFieldPlanActivityAssignment(request, activityAssignmentList);
    }

    public Employee getUserById(Object request, String userId) {

        String url = fieldPlannerConfiguration.getHrmsHost() + fieldPlannerConfiguration.getHrmsSearchUrl()+ "?tenantId=in&uuids="+userId;
        Object response = serviceRequestRepository.fetchResult(new StringBuilder(url), request);

        EmployeeResponse employeeResponse = mapper.convertValue(response, EmployeeResponse.class);
        if (employeeResponse == null || employeeResponse.getEmployees() == null || employeeResponse.getEmployees().isEmpty()) {
            throw new CustomException("EMPLOYEE_NOT_FOUND", "Employee not found with ID: " + userId);
        }
        return employeeResponse.getEmployees().get(0);
    }

}
