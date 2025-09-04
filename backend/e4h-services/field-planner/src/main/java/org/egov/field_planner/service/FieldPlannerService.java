package org.egov.field_planner.service;

import com.jayway.jsonpath.JsonPath;
import lombok.extern.slf4j.Slf4j;
import org.egov.common.contract.request.RequestInfo;
import org.egov.common.models.project.Project;
import org.egov.common.models.project.ProjectRequest;
import org.egov.common.producer.Producer;
import org.egov.field_planner.config.FieldPlannerConfiguration;
import org.egov.field_planner.repository.FieldPlannerRepository;
import org.egov.field_planner.service.enrichment.FieldPlannerEnrichment;
import org.egov.field_planner.util.FieldPlannerServiceUtil;
import org.egov.field_planner.util.MDMSUtils;
import org.egov.field_planner.validator.FieldPlannerValidator;
import org.egov.field_planner.web.models.Activity;
import org.egov.field_planner.web.models.FieldPlan;
import org.egov.field_planner.web.models.FieldPlanRequest;
import org.egov.field_planner.web.models.NameResult;
import org.egov.tracer.model.CustomException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.egov.field_planner.util.FieldPlannerConstants.*;

@Service
@Slf4j
public class FieldPlannerService {

    private final FieldPlannerValidator fieldPlannerValidator;
    private final FieldPlannerRepository fieldPlannerRepository;
    private final Producer producer;
    private final FieldPlannerEnrichment fieldPlannerEnrichment;

    private final FieldPlannerConfiguration fieldPlannerConfiguration;
    private final MDMSUtils mdmsUtils;

    @Autowired
    public FieldPlannerService(
            FieldPlannerRepository fieldPlannerRepository,
            FieldPlannerValidator fieldPlannerValidator, FieldPlannerEnrichment fieldPlannerEnrichment, FieldPlannerConfiguration fieldPlannerConfiguration,
            Producer producer, FieldPlannerServiceUtil projectServiceUtil, MDMSUtils mdmsUtils) {
            this.fieldPlannerValidator = fieldPlannerValidator;
            this.producer = producer;
            this.fieldPlannerConfiguration = fieldPlannerConfiguration;
            this.fieldPlannerRepository = fieldPlannerRepository;
            this.fieldPlannerEnrichment = fieldPlannerEnrichment;
            this.mdmsUtils = mdmsUtils;
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




}
