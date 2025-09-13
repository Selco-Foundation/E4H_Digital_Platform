package org.egov.activity.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.egov.common.contract.models.AuditDetails;
import org.egov.common.producer.Producer;
import org.egov.activity.config.ActivityConfiguration;
import org.egov.activity.repository.ActivityRepository;
import org.egov.activity.service.enrichment.ActivityEnrichment;
import org.egov.activity.util.MDMSUtils;
import org.egov.activity.validator.ActivityValidator;
import org.egov.activity.web.models.*;
import org.egov.tracer.model.CustomException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
@Slf4j
public class ActivityService {

    private final ActivityRepository activityRepository;

    private final Producer producer;

    private final ServiceRequestRepository serviceRequestClient;
    private final ActivityEnrichment activityEnrichment;

    private final ActivityValidator activityValidator;

    private final ActivityConfiguration activityConfiguration;
    private final MDMSUtils mdmsUtils;

    @Qualifier("objectMapper")
    private final ObjectMapper mapper;

    @Autowired
    public ActivityService(
            ActivityRepository activityRepository, ActivityEnrichment activityEnrichment, ActivityConfiguration activityConfiguration, ActivityValidator activityValidator,
            Producer producer, MDMSUtils mdmsUtils, ServiceRequestRepository serviceRequestClient, @Qualifier("objectMapper") ObjectMapper mapper) {
            this.producer = producer;
            this.activityConfiguration = activityConfiguration;
            this.activityRepository = activityRepository;
            this.activityEnrichment = activityEnrichment;
            this.mdmsUtils = mdmsUtils;
            this.serviceRequestClient = serviceRequestClient;
            this.mapper = mapper;
            this.activityValidator = activityValidator;
    }

    public List<ActivityFacility> createActivityFacility(ActivityFacilityBulkRequest request) {
        log.info("received request to create bulk fieldplan facility");

//        activityValidator.validateCreateActivityFacilityRequest(request);
        List<ActivityFacility> activityFacilities = request.getActivityFacilities();
        try {
            for (ActivityFacility activityFacility : activityFacilities) {
                log.info("processing {} valid entities", activityFacility);
                activityEnrichment.enrichActivityFacilityRequestOnCreate(activityFacility, request.getRequestInfo());
            }
            producer.push(activityConfiguration.getCreateActivityFacilityTopic(), request);
            log.info("successfully created activity facility");
        } catch (Exception exception) {
            log.error("error occurred while creating project facility: {}", ExceptionUtils.getStackTrace(exception));
        }

        return activityFacilities;
    }

    public List<ActivityAssignment> createActivityAssignment(ActivityAssignmentBulkRequest request) {
        log.info("received request to create bulk fieldplan facility");

        activityValidator.validateCreateActivityAssignmentRequest(request);
        List<ActivityAssignment> activityAssignments = request.getActivityAssignments();
        try {
            for (ActivityAssignment activityAssignment : activityAssignments) {
                log.info("processing {} valid entities", activityAssignment);
                activityEnrichment.enrichActivityAssignmentOnCreate(activityAssignment, request.getRequestInfo());
            }
            log.info("successfully created project facility");
            producer.push(activityConfiguration.getCreateActivityAssignmentTopic(), request);
        } catch (Exception exception) {
            log.error("error occurred while creating project facility: {}", ExceptionUtils.getStackTrace(exception));
        }

        return activityAssignments;
    }

    public List<ActivityFacility> searchActivity(ActivityFacilitySearchRequest request, Integer limit, Integer offset, String tenantId, Boolean includeDeleted, Long lastChangedSince) {
        activityValidator.validateSearchActivityRequest(request, limit, offset, tenantId);
        List<ActivityFacility> activityFacilities = activityRepository.getActivities(request, limit, offset, tenantId, includeDeleted, lastChangedSince);
        return activityFacilities;
    }

    public Integer countAllFieldPlans(ActivityFacilitySearchRequest request, String tenantId, Long lastChangedSince, Boolean includeDeleted) {
        return activityRepository.getActivitiesCount(request, tenantId, lastChangedSince, includeDeleted);
    }

//    public ActivityRequest updateProject(ActivityRequest request) {
//        /*
//         * Validate the update project request
//         */
//        activityValidator.validateUpdateFieldPlanRequest(request);
//        log.info("Update fieldplan request validated");
//
//        /*
//         * Search for fieldplan based on fieldplan IDs provided in the request
//         */
//        List<FieldPlan> fieldPlansFromDB = searchFieldPlan(
//                getSearchFieldPlanRequest(request.getFieldPlans(), request.getRequestInfo()),
//                fieldPlannerConfiguration.getMaxLimit(), fieldPlannerConfiguration.getDefaultOffset(),
//                request.getFieldPlans().get(0).getTenantId(), false, null, null, null);
//        log.info("Fetched fieldPlan for update request");
//
//        /*
//         * Validate the update fieldplan request against the fieldplans fetched from the database
//         */
//        fieldPlannerValidator.validateUpdateAgainstDB(request.getFieldPlans(), fieldPlansFromDB);
//
//        /*
//         * Process each project in the update request
//         */
//        for (FieldPlan fieldPlan : request.getFieldPlans()) {
//            processFieldPlanUpdate(request, fieldPlan, fieldPlansFromDB);
//        }
//
//        return request;
//    }

//    private void processFieldPlanUpdate(ActivityRequest request, FieldPlan fieldPlan, List<FieldPlan> fieldPlansFromDB) {
//        /*
//         * Convert fieldplan ID to string for comparison
//         */
//        String fieldPlanId = String.valueOf(fieldPlan.getId());
//
//        /*
//         * Find the project from the database that matches the current project ID
//         */
//        FieldPlan fielPlanFromDB = findFieldPlanById(fieldPlanId, fieldPlansFromDB);
//
//        if (fielPlanFromDB != null) {
//            /*
//             * Merge additional details of the project from the request and project from DB
//             */
//            fieldPlanServiceUtil.mergeAdditionalDetails(fieldPlan, fielPlanFromDB);
//
//            handleUpdateFieldPlan(request, fieldPlan, fielPlanFromDB);
//
//        }
//    }

//    private void handleUpdateFieldPlan(ActivityRequest request, FieldPlan fieldPlan, FieldPlan fieldPlanFromDB) {
//        /*
//         * Save original values of start date, end date, and additional details
//         */
//        Long originalStartDate = fieldPlanFromDB.getStartDate();
//        Long originalEndDate = fieldPlanFromDB.getEndDate();
//        Object originalGeographyDetails = fieldPlanFromDB.getGeographyDetails();
//        Object originalActivity = fieldPlanFromDB.getActivities();
//        AuditDetails originalAuditDetails = fieldPlanFromDB.getAuditDetails();
//
//
//        /*
//         * Update the project with new start date, end date, and additional details
//         */
//        fieldPlanFromDB.setStartDate(fieldPlan.getStartDate());
//        fieldPlanFromDB.setEndDate(fieldPlan.getEndDate());
//        fieldPlanFromDB.setGeographyDetails(fieldPlan.getGeographyDetails());
//        fieldPlanFromDB.setActivities(fieldPlan.getActivities());
//        fieldPlanFromDB.setAuditDetails(fieldPlan.getAuditDetails());
//
//        /*
//         * Ensure that no other properties are being updated besides the start and end dates
//         */
//        if (!isValidCascadingUpdate(fieldPlanFromDB, fieldPlan)) {
//            throw new CustomException(
//                    "FIELDPLANE_CASCADE_UPDATE_ERROR",
//                    "Can only update FieldPlan dates, geographyDetails and additional details if cascade FieldPlan date update true"
//            );
//        }
//
//        /*
//         * Restore original values of start date, end date, and additional details
//         */
//        fieldPlanFromDB.setStartDate(originalStartDate);
//        fieldPlanFromDB.setEndDate(originalEndDate);
//        fieldPlanFromDB.setGeographyDetails(mapper.convertValue(originalGeographyDetails, Map.class));
//        fieldPlanFromDB.setActivities((List<Map<String, Object>>) originalActivity);
//        fieldPlanFromDB.setAuditDetails(originalAuditDetails);
//
//        /*
//         * Update lastModifiedTime and lastModifiedBy for the project
//         */
//        fieldPlannerEnrichment.enrichFieldPlanRequestOnUpdate(fieldPlan, fieldPlanFromDB, request.getRequestInfo());
//
//        /*
//         * Handle project name regeneration if needed (dates changed)
//         */
////        handleFieldPlanNameUpdate(request, fieldPlan, fieldPlanFromDB);
//
//        /*
//         * Check and enrich cascading project dates and push the update to the message broker
//         */
//        producer.push(fieldPlannerConfiguration.getUpdateFieldPlanTopic(), request);
//    }


}
