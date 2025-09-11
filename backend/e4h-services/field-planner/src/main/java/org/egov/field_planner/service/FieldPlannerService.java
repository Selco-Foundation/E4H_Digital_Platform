package org.egov.field_planner.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.jsonpath.JsonPath;
import lombok.extern.slf4j.Slf4j;
import org.egov.common.contract.models.AuditDetails;
import org.egov.common.contract.request.RequestInfo;
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

    @Autowired
    @Qualifier("objectMapper")
    ObjectMapper mapper;

    @Autowired
    public FieldPlannerService(
            FieldPlannerRepository fieldPlannerRepository, List<Validator<FieldPlanFacilityBulkRequest, FieldPlanFacility>> validators,
            FieldPlannerValidator fieldPlannerValidator, FieldPlannerEnrichment fieldPlannerEnrichment, FieldPlannerConfiguration fieldPlannerConfiguration,
            Producer producer, MDMSUtils mdmsUtils, FieldPlannerServiceUtil fieldPlanServiceUtil) {
            this.fieldPlannerValidator = fieldPlannerValidator;
            this.producer = producer;
            this.fieldPlannerConfiguration = fieldPlannerConfiguration;
            this.fieldPlannerRepository = fieldPlannerRepository;
            this.fieldPlannerEnrichment = fieldPlannerEnrichment;
            this.mdmsUtils = mdmsUtils;
            this.validators = validators;
            this.fieldPlanServiceUtil = fieldPlanServiceUtil;
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

    public Integer countAllFieldPlans(FieldPlanRequest request, String tenantId, Long lastChangedSince, Boolean includeDeleted, Long createdFrom, Long createdTo) {
        return fieldPlannerRepository.getFieldPlanCount(request, tenantId, lastChangedSince, includeDeleted, createdFrom, createdTo);
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
        String state = (String)geographyDetails.get("state");
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
            int startYear = endDate.getYear();

            baseName = String.format("%s-%s-%s", stateCode, concatenatedActivityCode, startYear);
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
     * Handles fieldPlan name regeneration during updates
     * Compares the new base name with existing name and updates if different
     */
    private void handleFieldPlanNameUpdate(FieldPlanRequest request, FieldPlan fieldPlan, FieldPlan fieldPlanFromDB) {
        try {
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
    private FieldPlanRequest getSearchFieldPlanRequest(List<FieldPlan> fieldPlans, RequestInfo requestInfo) {
        List<FieldPlan> fieldPlanList = new ArrayList<>();

        for (FieldPlan fieldPlan : fieldPlans) {
            String fieldPlanId = fieldPlan.getId();
            FieldPlan newFieldPlan = FieldPlan.builder()
                    .id(fieldPlanId)
                    .tenantId(fieldPlan.getTenantId())
                    .build();

            fieldPlanList.add(newFieldPlan);
        }
        return FieldPlanRequest.builder()
                .requestInfo(requestInfo)
                .fieldPlans(fieldPlanList)
                .build();
    }

    public List<FieldPlan> searchFieldPlan(FieldPlanRequest request, Integer limit, Integer offset, String tenantId, Boolean includeDeleted, Long lastChangedSince, Long createdFrom, Long createdTo) {
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
             * Merge additional details of the fieldPlan from the request and fieldPlan from DB
             */
            fieldPlanServiceUtil.mergeAdditionalDetails(fieldPlan, fielPlanFromDB);

            /*
             * Handle cases where cascading fieldPlan date update is true
             */
            if (isCascadingFieldPlanDateUpdate) {
                handleUpdateFieldPlan(request, fieldPlan, fielPlanFromDB);
            }
            /*
             * Handle cases for normal update flow
             */
            else {
//                handleNormalUpdate(request, fieldPlan, fieldPlanFromDB);
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

        /*
         * Handle fieldPlan name regeneration if needed (dates changed)
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

}
