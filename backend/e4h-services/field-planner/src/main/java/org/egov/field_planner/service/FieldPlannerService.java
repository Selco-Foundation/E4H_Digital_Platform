package org.egov.field_planner.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.jsonpath.JsonPath;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.egov.common.contract.models.AuditDetails;
import org.egov.common.contract.request.RequestInfo;
import org.egov.common.models.core.AdditionalFields;
import org.egov.common.models.core.Field;
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

    private final FieldPlanTemplateService fieldPlanTemplateService;

    private final FieldPlannerAnalyticsService fieldPlannerAnalyticsService;

    @Autowired
    @Qualifier("objectMapper")
    ObjectMapper mapper;

    @Autowired
    public FieldPlannerService(
            FieldPlannerRepository fieldPlannerRepository, List<Validator<FieldPlanFacilityBulkRequest, FieldPlanFacility>> validators, FieldPlannerFacilityService facilityService,
            FieldPlannerValidator fieldPlannerValidator, FieldPlannerEnrichment fieldPlannerEnrichment, FieldPlannerConfiguration fieldPlannerConfiguration,
            Producer producer, MDMSUtils mdmsUtils, FieldPlannerServiceUtil fieldPlanServiceUtil, ServiceRequestRepository serviceRequestRepository,
            FieldPlanTemplateService fieldPlanTemplateService, FieldPlannerAnalyticsService fieldPlannerAnalyticsService) {
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
            this.fieldPlanTemplateService = fieldPlanTemplateService;
            this.fieldPlannerAnalyticsService = fieldPlannerAnalyticsService;
    }

    public FieldPlanRequest createFieldPlan(FieldPlanRequest fieldPlanRequest) {
        log.trace("Entering createFieldPlan method");
        log.info("Starting field plan creation request");

        fieldPlannerValidator.validateCreateFieldPlanRequest(fieldPlanRequest);
        log.debug("Field plan creation request validated successfully");

        for (FieldPlan fieldPlan : fieldPlanRequest.getFieldPlans()) {
            log.trace("Processing field plan for tenant: {}", fieldPlan.getTenantId());

            String baseName = getStateActivitiesYearFormat(fieldPlanRequest, fieldPlan.getTenantId(), fieldPlan);
//            String baseName = "KA-MT_HO-2024";
            if(baseName == null){
                log.error("Cannot generate field plan name for tenant: {}", fieldPlan.getTenantId());
                throw new CustomException("FORMAT ERROR", "Cannot generate the fieldplan name");
            }
            log.debug("Generated base name for field plan: {}", baseName);

            fieldPlan.setName(baseName);
            NameResult result = CheckDuplicateAndGenerateName(fieldPlan);
            if (result.isDuplicate()) {
                fieldPlan.setIsDuplicate(true);
                fieldPlan.setName(result.getGeneratedName());
                log.info("Duplicate field plan name found, using generated name: {}", result.getGeneratedName());
            } else {
                log.debug("No duplicate found, using base name: {}", result.getGeneratedName());
            }

            fieldPlannerEnrichment.enrichFieldPlanOnCreate(fieldPlan, fieldPlanRequest.getRequestInfo());
            log.info("Field plan enriched with ID: {} and audit details", fieldPlan.getId());

            producer.push(fieldPlannerConfiguration.getSaveFieldPlanTopic(), fieldPlanRequest);
            log.info("Field plan creation request pushed to Kafka topic: {}", fieldPlannerConfiguration.getSaveFieldPlanTopic());
        }

        // No analytics event here: a create only ever drafts a plan. The FIELD_PLAN_CREATE event is
        // emitted from the update that moves the plan into SCHEDULED — see
        // FieldPlannerAnalyticsService#publishScheduledEvent.

        log.info("Field plan creation request processed successfully");
        log.trace("Exiting createFieldPlan method");
        return fieldPlanRequest;
    }

    public FieldPlanRequest updateFieldPlan(FieldPlanRequest request) {
        log.trace("Entering updateFieldPlan method");
        log.info("Starting field plan update request");

        /*
         * Validate the update fieldPlan request
         */
        fieldPlannerValidator.validateUpdateFieldPlanRequest(request);
        log.debug("Field plan update request validated successfully");

        /*
         * Search for fieldplan based on fieldplan IDs provided in the request
         */
        List<FieldPlan> fieldPlansFromDB = searchFieldPlan(
                getSearchFieldPlanRequest(request.getFieldPlans(), request.getRequestInfo()),
                fieldPlannerConfiguration.getMaxLimit(), fieldPlannerConfiguration.getDefaultOffset(),
                request.getFieldPlans().get(0).getTenantId(), false, null, null, null);
        log.info("Fetched {} field plans from database for update", fieldPlansFromDB.size());

        /*
         * Validate the update fieldplan request against the fieldplans fetched from the database
         */
        fieldPlannerValidator.validateUpdateAgainstDB(request.getFieldPlans(), fieldPlansFromDB);
        log.debug("Field plan update request validated against database records");

        /*
         * Process each fieldPlan in the update request
         */
        for (FieldPlan fieldPlan : request.getFieldPlans()) {
            log.trace("Processing update for field plan ID: {}", fieldPlan.getId());
            processFieldPlanUpdate(request, fieldPlan, fieldPlansFromDB);
        }

        log.info("Field plan update request processed successfully");
        log.trace("Exiting updateFieldPlan method");
        return request;
    }

    public Integer countAllFieldPlans(FieldPlanSearchRequest request, String tenantId, Long lastChangedSince, Boolean includeDeleted) {
        log.trace("Entering countAllFieldPlans method");
        log.debug("Counting field plans for tenant: {}", tenantId);
        Integer count = fieldPlannerRepository.getFieldPlanCount(request, tenantId, lastChangedSince, includeDeleted);
        log.debug("Field plan count: {}", count);
        log.trace("Exiting countAllFieldPlans method");
        return count;
    }

    public NameResult CheckDuplicateAndGenerateName(FieldPlan fieldPlan) {
        log.trace("Entering CheckDuplicateAndGenerateName method for field plan");
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
        stateCode = boundaryCodeToCode(stateBoundary);
//        String state = fieldPlanServiceUtil.extractStateName(stateBoundary);
        List<Map<String, Object>> activities = fieldPlan.getActivities();
        log.debug("Extracted state: {}, activities count: {}", stateCode, activities.size());

        try {
            activitiesRes = JsonPath.read(mdmsData, jsonPathForActivities);
            stateInfoRes = JsonPath.read(mdmsData, jsonPathForStateInfo);
//            for (Object map : stateInfoRes) {
//                LinkedHashMap<String, Object> stateInfo = (LinkedHashMap<String, Object>) map;
//                String name = (String) stateInfo.get("name");
//                if (state.equalsIgnoreCase(name)) {
//                    stateCode = (String) stateInfo.get("code");
//                    break;
//                }
//            }

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
                log.info("Processing cascading field plan date update for field plan ID: {}", fieldPlanId);
                // Read before the update: analytics only tracks the update that schedules the plan,
                // so it needs the status the plan came from to ignore the DRAFT edits before it.
                String priorStatus = fielPlanFromDB.getStatus();
                handleUpdateFieldPlan(request, fieldPlan, fielPlanFromDB);
                // Only reached once the update has been pushed to the persister — a plan that failed
                // validation above threw and never gets here (best-effort, never throws).
                fieldPlannerAnalyticsService.publishScheduledEvent(request.getRequestInfo(), fieldPlan,
                        fielPlanFromDB, priorStatus);
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
                // Check if INSTALLATION_REVIEWER, one FIELD_SUPERVISOR and one FIELD_STAFF is assigned and if at least one facility is linked to the fieldplan
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
                // Check if at least one INSTALLATION_REVIEWER, one FIELD_SUPERVISOR and one FIELD_STAFF are already link to field plan
                if(!hasRequiredUsers(activityAssignmentList)){
                    throw new CustomException("FIELDPLAN", "INSTALLATION_REVIEWER and FIELD_STAFF and FIELD_SUPERVISOR need to be assigned for the fieldplan");
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
                    // Fetched once for the whole batch rather than once per facility/component -
                    // asset-registry.BrandSchema doesn't change within a single scheduling run.
                    Map<String, Map<String, String>> brandCodeByNameByAssetType =
                            fetchBrandCodeByNameByAssetType(request.getRequestInfo());
                    for (FieldPlanFacility fieldPlanFacility : fieldPlanFacilities){
                        Map<String, Object> additionalDetails = buildActivityFacilityAdditionalDetails(request, fieldPlanFacility, brandCodeByNameByAssetType);
                        for(Map<String, Object> activity : fieldPlan.getActivities()){
                            ActivityFacility activityFacility = ActivityFacility.builder()
                                    .tenantId("in")
                                    .fieldPlanId(fieldPlanFacility.getFieldPlanId())
                                    .facilityId(fieldPlanFacility.getFacilityId())
                                    .activityId((String)activity.get("code"))
                                    .scheduledAt(fieldPlan.getStartDate())
                                    .activatedAt(fieldPlan.getStartDate())
                                    .reviewerUser(roleToIds.get(INSTALLATION_REVIEWER_ROLE))
                                    .fieldStaffUsers(roleToIds.get(FIELD_STAFF_ROLE))
                                    .fieldSupervisorUsers(roleToIds.get(FIELD_SUPERVISOR_ROLE))
                                    .additionalDetails(additionalDetails)
                                    .build();

                            activityFacilities.add(activityFacility);
                        }
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

    /**
     * Builds the additionalDetails for an ActivityFacility being created on SCHEDULED field plan
     * update: the matching FieldPlanTemplate's templateData (by fieldPlanId + systemType, the
     * latter read from the FieldPlanFacility's own additionalFields) under key "bom", merged with
     * the FieldPlanFacility's additionalFields themselves (facilityType/systemType/
     * solarSolutionDesignType/totalSystemCapacity, as set at link time).
     */
    private Map<String, Object> buildActivityFacilityAdditionalDetails(
            FieldPlanRequest request, FieldPlanFacility fieldPlanFacility, Map<String, Map<String, String>> brandCodeByNameByAssetType) {
        log.trace("Entering buildActivityFacilityAdditionalDetails method for facility: {}", fieldPlanFacility.getFacilityId());
        Map<String, Object> additionalDetails = new HashMap<>(extractAdditionalFieldsAsMap(fieldPlanFacility.getAdditionalFields()));

        String systemType = (String) additionalDetails.get("systemType");
        String totalSystemCapacity = (String) additionalDetails.get("totalSystemCapacity");
        if (StringUtils.equalsIgnoreCase(totalSystemCapacity, "CUSTOM")) {
            totalSystemCapacity = (String) additionalDetails.get("customTotalSystemCapacity");
        }
        if (StringUtils.isNotBlank(systemType)) {
            FieldPlanTemplate template = findFieldPlanTemplate(request, fieldPlanFacility.getFieldPlanId(), systemType, totalSystemCapacity);
            if (template != null && template.getTemplateData() != null) {
                Map<String, Object> templateData = template.getTemplateData();
                additionalDetails.put("bom", templateData);
                additionalDetails.put("panel", buildBomComponent(templateData, "solar_module_capacity", "solar_module_make",
                        brandCodeByNameByAssetType.getOrDefault("PANEL", Collections.emptyMap())));
                additionalDetails.put("battery", buildBomComponent(templateData, "solar_battery_capacity", "solar_battery_make",
                        brandCodeByNameByAssetType.getOrDefault("BATTERY", Collections.emptyMap())));
                additionalDetails.put("inverter", buildInverterComponent(templateData,
                        brandCodeByNameByAssetType.getOrDefault("INVERTER", Collections.emptyMap())));
            } else {
                log.warn("No FieldPlanTemplate found for fieldPlanId: {}, systemType: {}", fieldPlanFacility.getFieldPlanId(), systemType);
            }
        } else {
            log.warn("FieldPlanFacility {} has no systemType in additionalFields - skipping BOM template lookup", fieldPlanFacility.getFacilityId());
        }

        log.trace("Exiting buildActivityFacilityAdditionalDetails method");
        return additionalDetails;
    }

    // The BOM template's "inverter" component is named differently per system type - only one
    // of these key pairs is ever present in a given templateData (each FieldPlanTemplate is
    // generated from exactly one system type's template), so the first pair found wins:
    //   - inverter_*: AC_OFF_GRID and AC_ON_GRID templates
    //   - solar_hybrid_pcu_*: AC_HYBRID template
    //   - solar_charge_controller_*: DC_OFF_GRID template (no dedicated inverter - CCU serves that role)
    private static final List<String[]> INVERTER_KEY_CANDIDATES = List.of(
            new String[]{"inverter_capacity", "inverter_make"},
            new String[]{"solar_on_grid_pcu_inverter_capacity", "solar_on_grid_pcu_inverter_make"},
            new String[]{"solar_hybrid_pcu_capacity", "solar_hybrid_pcu_make"},
            new String[]{"solar_charge_controller_capacity", "solar_charge_controller_make"}
    );

    private Map<String, Object> buildInverterComponent(Map<String, Object> templateData, Map<String, String> inverterBrandCodeByName) {
        for (String[] candidate : INVERTER_KEY_CANDIDATES) {
            if (templateData.containsKey(candidate[0]) || templateData.containsKey(candidate[1])) {
                return buildBomComponent(templateData, candidate[0], candidate[1], inverterBrandCodeByName);
            }
        }
        return buildBomComponent(templateData, INVERTER_KEY_CANDIDATES.get(0)[0], INVERTER_KEY_CANDIDATES.get(0)[1], inverterBrandCodeByName);
    }

    private static final String BRAND_SCHEMA_CODE = "asset-registry.BrandSchema";

    /**
     * Fetches every active brand registered in MDMS asset-registry.BrandSchema, grouped by
     * asset type (PANEL/BATTERY/INVERTER) and indexed by brand name -> brand code (e.g.
     * PANEL: {"Gautam Solar" -> "GAUTAM_SOLAR", "ReNew" -> "RENEW", ...}). Returns an empty map
     * (never throws) if MDMS can't be reached - brandCode then simply comes out null for every
     * component rather than blocking the whole field plan scheduling run.
     */
    private Map<String, Map<String, String>> fetchBrandCodeByNameByAssetType(RequestInfo requestInfo) {
        Map<String, Map<String, String>> result = new HashMap<>();
        try {
            Map<String, Object> mdmsCriteria = new HashMap<>();
            mdmsCriteria.put("tenantId", "in");
            mdmsCriteria.put("schemaCode", BRAND_SCHEMA_CODE);

            Map<String, Object> payload = new HashMap<>();
            payload.put("RequestInfo", requestInfo);
            payload.put("MdmsCriteria", mdmsCriteria);

            String url = fieldPlannerConfiguration.getMdmsHost() + fieldPlannerConfiguration.getMdmsV2SearchEndpoint();
            Object response = serviceRequestRepository.fetchResult(new StringBuilder(url), payload);
            JsonNode mdmsArray = mapper.valueToTree(response).path("mdms");
            for (JsonNode mdmsEntry : mdmsArray) {
                for (JsonNode brand : mdmsEntry.path("data").path("Brand")) {
                    if (brand.path("active").isBoolean() && !brand.path("active").asBoolean()) {
                        continue;
                    }
                    String assetType = brand.path("asset_type_code").asText(null);
                    String name = brand.path("name").asText(null);
                    String code = brand.path("code").asText(null);
                    if (assetType == null || name == null || code == null) {
                        continue;
                    }
                    result.computeIfAbsent(assetType, k -> new HashMap<>()).put(name, code);
                }
            }
        } catch (Exception e) {
            log.error("Error fetching {} for brand code lookup", BRAND_SCHEMA_CODE, e);
        }
        return result;
    }

    /**
     * Builds a {capacity, brandCode, brandName} component (panel/battery/inverter) from the
     * FieldPlanTemplate's flat templateData: brandCode is looked up from MDMS
     * asset-registry.BrandSchema by brand name (e.g. make "Gautam Solar" -> brandCode
     * "GAUTAM_SOLAR"), not derived from the name text itself - null if the name isn't a
     * registered brand for this asset type (shouldn't happen once /icc-reports' own brand name
     * validation is in place, but this stays defensive for already-stored data predating it).
     */
    private Map<String, Object> buildBomComponent(
            Map<String, Object> templateData, String capacityKey, String makeKey, Map<String, String> brandCodeByName) {
        Object capacity = templateData.get(capacityKey);
        Object make = templateData.get(makeKey);
        Map<String, Object> component = new HashMap<>();
        component.put("capacity", capacity);
        component.put("brandCode", make != null ? brandCodeByName.get(make.toString()) : null);
        component.put("brandName", make);
        return component;
    }

    private Map<String, Object> extractAdditionalFieldsAsMap(AdditionalFields additionalFields) {
        if (additionalFields == null || additionalFields.getFields() == null) {
            return new HashMap<>();
        }
        Map<String, Object> result = new HashMap<>();
        for (Field field : additionalFields.getFields()) {
            if (field.getKey() != null) {
                result.put(field.getKey(), field.getValue());
            }
        }
        return result;
    }

    private FieldPlanTemplate findFieldPlanTemplate(FieldPlanRequest request, String fieldPlanId, String systemType, String totalSystemCapacity) {
        try {
            FieldPlanTemplateSearchCriteria criteria = FieldPlanTemplateSearchCriteria.builder()
                    .fieldPlanId(List.of(fieldPlanId))
                    .systemType(List.of(systemType))
                    .totalCapacity(List.of(totalSystemCapacity))
                    .build();
            FieldPlanTemplateSearchRequest searchRequest = FieldPlanTemplateSearchRequest.builder()
                    .requestInfo(request.getRequestInfo())
                    .criteria(criteria)
                    .build();
            List<FieldPlanTemplate> templates = fieldPlanTemplateService.search(
                    searchRequest,
                    fieldPlannerConfiguration.getMaxLimit(),
                    fieldPlannerConfiguration.getDefaultOffset(),
                    "in",
                    null
            );
            return (templates != null && !templates.isEmpty()) ? templates.get(0) : null;
        } catch (Exception e) {
            log.error("Error searching field plan template for fieldPlanId: {}, systemType: {}", fieldPlanId, systemType, e);
            return null;
        }
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

    public boolean hasRequiredUsers(List<ActivityAssignment> activityAssignmentList) {
        boolean hasFieldStaff = false;
        boolean hasFieldSupervisor = false;
        boolean hasReviewer = false;

        for (ActivityAssignment assignment : activityAssignmentList) {
            Map<String, Object> roleMap = assignment.getRole();
            if (FIELD_STAFF_ROLE.equalsIgnoreCase((String) roleMap.get("code"))) {
                hasFieldStaff = true;
            }
            if (FIELD_SUPERVISOR_ROLE.equalsIgnoreCase((String) roleMap.get("code"))) {
                hasFieldSupervisor = true;
            }
            if (INSTALLATION_REVIEWER_ROLE.equalsIgnoreCase((String) roleMap.get("code"))) {
                hasReviewer = true;
            }
            if (hasFieldStaff && hasFieldSupervisor && hasReviewer) {
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

    // Output India_AndamanandNicobarIslands: → AN, India_Telangana → TE, India_Assam_Biswanath → AB
    public static String boundaryCodeToCode(String input) {
        if (input == null || input.isBlank()) {
            return "";
        }

        // Nettoyage
        String cleaned = input.trim();

        // Supprimer "India_" si présent
        if (cleaned.startsWith("India_")) {
            cleaned = cleaned.substring("India_".length());
        }

        // Supprimer ":" et tout ce qui suit
        int colonIndex = cleaned.indexOf(":");
        if (colonIndex >= 0) {
            cleaned = cleaned.substring(0, colonIndex);
        }

        // Enlever underscores
        cleaned = cleaned.replace("_", "");

        // Split CamelCase
        String[] words = cleaned.split("(?=[A-Z])");

        // Construire le code
        StringBuilder code = new StringBuilder();

        if (words.length >= 2) {
            code.append(Character.toUpperCase(words[0].charAt(0)));
            code.append(Character.toUpperCase(words[1].charAt(0)));
        } else if (words.length == 1 && words[0].length() >= 2) {
            code.append(Character.toUpperCase(words[0].charAt(0)));
            code.append(Character.toUpperCase(words[0].charAt(1)));
        } else if (words.length == 1) {
            code.append(Character.toUpperCase(words[0].charAt(0)));
        }

        return code.toString();
    }

}
