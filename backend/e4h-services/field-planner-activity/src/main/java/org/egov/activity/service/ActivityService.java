package org.egov.activity.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.egov.activity.repository.ActivityAssignmentRepository;
import org.egov.activity.util.ActivityServiceUtil;
import org.egov.common.contract.request.RequestInfo;
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
import java.util.Objects;

@Service
@Slf4j
public class ActivityService {

    private final ActivityRepository activityRepository;

    private final ActivityAssignmentRepository activityAssignmentRepository;

    private final Producer producer;

    private final ActivityServiceUtil activityServiceUtil;
    private final ActivityEnrichment activityEnrichment;

    private final ActivityValidator activityValidator;

    private final ActivityConfiguration activityConfiguration;
    private final MDMSUtils mdmsUtils;

    @Qualifier("objectMapper")
    private final ObjectMapper mapper;

    @Autowired
    public ActivityService(
            ActivityRepository activityRepository, ActivityEnrichment activityEnrichment, ActivityConfiguration activityConfiguration, ActivityValidator activityValidator,
            Producer producer, MDMSUtils mdmsUtils, ActivityServiceUtil activityServiceUtil, @Qualifier("objectMapper") ObjectMapper mapper, ActivityAssignmentRepository activityAssignmentRepository) {
            this.producer = producer;
            this.activityConfiguration = activityConfiguration;
            this.activityRepository = activityRepository;
            this.activityEnrichment = activityEnrichment;
            this.mdmsUtils = mdmsUtils;
            this.activityServiceUtil = activityServiceUtil;
            this.mapper = mapper;
            this.activityValidator = activityValidator;
            this.activityAssignmentRepository = activityAssignmentRepository;
    }

    public List<Activity> createActivity(ActivityBulkRequest request) {
        log.info("received request to create bulk activity bulk");
        List<Activity> activities = request.getActivities();
        try {
            for (Activity activity : activities) {
                log.info("processing {} valid entities", activity);
                activityEnrichment.enrichActivityRequestOnCreate(activity, request.getRequestInfo());
            }
            producer.push(activityConfiguration.getCreateActivityTopic(), request);
            log.info("successfully created activity");
        } catch (Exception exception) {
            log.error("error occurred while creating activity: {}", ExceptionUtils.getStackTrace(exception));
        }

        return activities;
    }

    public List<ActivityFacility> createActivityFacility(ActivityFacilityBulkRequest request) {
        log.info("received request to create bulk fieldplan facility");

        activityValidator.validateCreateActivityFacilityRequest(request);
        List<ActivityFacility> activityFacilities = request.getActivityFacilities();
        try {
            for (ActivityFacility activityFacility : activityFacilities) {
                log.info("processing {} valid entities", activityFacility);
                activityEnrichment.enrichActivityFacilityRequestOnCreate(activityFacility, request.getRequestInfo());
            }
            producer.push(activityConfiguration.getCreateActivityFacilityTopic(), request);
            log.info("successfully created activity facility");
        } catch (Exception exception) {
            log.error("error occurred while creating Activity facility: {}", ExceptionUtils.getStackTrace(exception));
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
            log.error("error occurred while creating Activity Assignment: {}", ExceptionUtils.getStackTrace(exception));
        }

        return activityAssignments;
    }

    public List<ActivityAssignment> unassignActivityAssignment(ActivityAssignmentBulkRequest request) {
        log.info("received request to unassign bulk Activity facility");

        activityValidator.validateDeleteActivityAssignmentRequest(request);
        List<ActivityAssignment> activityAssignments = request.getActivityAssignments();
        try {
            for (ActivityAssignment activityAssignment : activityAssignments) {
                log.info("processing {} valid entities", activityAssignment);
                activityEnrichment.enrichFieldPlanRequestOnDelete(activityAssignment, request.getRequestInfo());
            }
            log.info("successfully unassign fieldplan activities");
            producer.push(activityConfiguration.getUnassignActivityAssignmentTopic(), request);
        } catch (Exception exception) {
            log.error("error occurred while creating project facility: {}", ExceptionUtils.getStackTrace(exception));
        }

        return activityAssignments;
    }

    public List<ActivityFacility> searchActivity(ActivityFacilitySearchRequest request, Integer limit, Integer offset, String tenantId, Boolean includeDeleted, Long lastChangedSince) {
        activityValidator.validateSearchActivityRequest(request, limit, offset, tenantId);
        List<ActivityFacility> activityFacilities = activityRepository.getActivitiesFacility(request, limit, offset, tenantId, includeDeleted, lastChangedSince);
        return activityFacilities;
    }

    public List<ActivityAssignment> searchAssignedActivity(ActivityAssignmentSearchRequest request, Integer limit, Integer offset, String tenantId, Boolean includeDeleted, Long lastChangedSince) {
        activityValidator.validateSearchAssignActivityRequest(request, limit, offset, tenantId);
        List<ActivityAssignment> activityFacilities = activityAssignmentRepository.getActivitiesAssignment(request, limit, offset, tenantId, includeDeleted, lastChangedSince);
        for (ActivityAssignment activityAssignment : activityFacilities) {
            log.info("processing get activity code", activityAssignment);
            activityEnrichment.enrichActivityOnSearch(activityAssignment);
        }
        return activityFacilities;
    }

    public Integer countAllFacilityActivities(ActivityFacilitySearchRequest request, String tenantId, Long lastChangedSince, Boolean includeDeleted) {
        return activityRepository.getActivitiesCount(request, tenantId, lastChangedSince, includeDeleted);
    }

    public Integer countAllAssignedActivities(ActivityAssignmentSearchRequest request, String tenantId, Long lastChangedSince, Boolean includeDeleted) {
        return activityAssignmentRepository.getActivitiesCount(request, tenantId, lastChangedSince, includeDeleted);
    }

    public ActivityFacilityBulkRequest updateActivityFacitlity(ActivityFacilityBulkRequest request) {
        /*
         * Validate the update activity request
         */
        activityValidator.validateCreateActivityFacilityRequest(request);
        log.info("Update activity facility request validated");

        /*
         * Search for fieldplan based on fieldplan IDs provided in the request
         */
        List<ActivityFacility> activityFacilityListFromDB = searchActivity(
                getSearchActivityFacilityRequest(request.getActivityFacilities(), request.getRequestInfo()),
                activityConfiguration.getMaxLimit(), activityConfiguration.getDefaultOffset(),
                request.getActivityFacilities().get(0).getTenantId(), false, null);
        log.info("Fetched activities for update request");

        /*
         * Validate the update fieldplan request against the fieldplans fetched from the database
         */
        activityValidator.validateUpdateAgainstDB(request.getActivityFacilities(), activityFacilityListFromDB);

        /*
         * Process each project in the update request
         */
        for (ActivityFacility activityFacility : request.getActivityFacilities()) {
            processActivityFacilityUpdate(request, activityFacility, activityFacilityListFromDB);
        }

        return request;
    }

    public ActivityAssignmentBulkRequest updateActivityAssignment(ActivityAssignmentBulkRequest request) {
        /*
         * Validate the update activity request
         */
        activityValidator.validateCreateActivityAssignmentRequest(request);
        log.info("Update activity assignment request validated");

        /*
         * Search for fieldplan based on fieldplan IDs provided in the request
         */
        List<ActivityAssignment> activityAssignmentListFromDB = searchAssignedActivity(
                getSearchActivityAssignmentRequest(request.getActivityAssignments(), request.getRequestInfo()),
                activityConfiguration.getMaxLimit(), activityConfiguration.getDefaultOffset(),
                request.getActivityAssignments().get(0).getTenantId(), false, null);
        log.info("Fetched activities for update request");

        /*
         * Validate the update fieldplan request against the fieldplans fetched from the database
         */
        activityValidator.validateUpdateActivityAssignmentAgainstDB(request.getActivityAssignments(), activityAssignmentListFromDB);

        /*
         * Process each project in the update request
         */
        for (ActivityAssignment activityAssignment : request.getActivityAssignments()) {
            processActivityAssignmentUpdate(request, activityAssignment, activityAssignmentListFromDB);
        }

        return request;
    }

    private ActivityFacilitySearchRequest getSearchActivityFacilityRequest(List<ActivityFacility> activityFacilities, RequestInfo requestInfo) {
        List<String> activityFacilityIds = activityFacilities.stream().map(ActivityFacility::getId).toList();
        ActivityFacilitySearchCriteria criteria = ActivityFacilitySearchCriteria.builder().ids(activityFacilityIds).tenantId(activityFacilities.get(0).getTenantId()).build();
        return ActivityFacilitySearchRequest.builder()
                .requestInfo(requestInfo)
                .criteria(criteria)
                .build();
    }

    private ActivityAssignmentSearchRequest getSearchActivityAssignmentRequest(List<ActivityAssignment> activityAssignments, RequestInfo requestInfo) {
        List<String> activityAssignmentIds = activityAssignments.stream().map(ActivityAssignment::getId).toList();
        ActivityAssignmentSearchCriteria criteria = ActivityAssignmentSearchCriteria.builder().ids(activityAssignmentIds).tenantId(activityAssignments.get(0).getTenantId()).build();
        return ActivityAssignmentSearchRequest.builder()
                .requestInfo(requestInfo)
                .criteria(criteria)
                .build();
    }

    private void processActivityFacilityUpdate(ActivityFacilityBulkRequest request, ActivityFacility activityFacility, List<ActivityFacility> activityFacilityListFromDB) {
        /*
         * Convert activity facility ID to string for comparison
         */
        String activityFacilityId = String.valueOf(activityFacility.getId());

        /*
         * Find the activity from the database that matches the current project ID
         */
        ActivityFacility activityFacilityFromDB = findActivityFacilityById(activityFacilityId, activityFacilityListFromDB);

        if (activityFacilityFromDB != null) {
            /*
             * Merge additional details of the project from the request and project from DB
             */
            activityServiceUtil.mergeAdditionalDetails(activityFacility, activityFacilityFromDB);

            handleUpdateActivityFacility(request, activityFacility, activityFacilityFromDB);

        }
    }

    private void processActivityAssignmentUpdate(ActivityAssignmentBulkRequest request, ActivityAssignment activityAssignment, List<ActivityAssignment> activityAssignmentListFromDB) {
        /*
         * Convert activity facility ID to string for comparison
         */
        String activityFacilityId = String.valueOf(activityAssignment.getId());

        /*
         * Find the activity from the database that matches the current project ID
         */
        ActivityAssignment activityAssignmentFromDB = findActivityAssignmentById(activityFacilityId, activityAssignmentListFromDB);

        if (activityAssignmentFromDB != null) {
            /*
             * Merge additional details of the project from the request and project from DB
             */
            activityServiceUtil.mergeActivityAssignmentAdditionalDetails(activityAssignment, activityAssignmentFromDB);

            handleUpdateActivityAssignment(request, activityAssignment, activityAssignmentFromDB);

        }
    }

    private void handleUpdateActivityFacility(ActivityFacilityBulkRequest request, ActivityFacility activityFacility, ActivityFacility activityFacilityFromDB) {

        /*
         * Ensure that no other properties are being updated besides the start and end dates
         */
        ActivitySearchCriteria criteria = ActivitySearchCriteria.builder().ids(List.of(activityFacility.getActivityId())).build();
        Activity existingActivity = activityRepository.getActivityList(criteria);
        activityFacility.setActivityId(existingActivity.getId());
        if (!isValidCascadingUpdateActivityFacility(activityFacilityFromDB, activityFacility)) {
            throw new CustomException(
                    "ACTIVITY_CASCADE_UPDATE_ERROR",
                    "Can only update Activity facility dates, geographyDetails and additional details if cascade FieldPlan date update true"
            );
        }

        /*
         * Update lastModifiedTime and lastModifiedBy for the activity
         */
        activityEnrichment.enrichActivityFacilityRequestOnUpdate(activityFacility, activityFacilityFromDB, request.getRequestInfo());

        /*
         * Check and enrich cascading project dates and push the update to the message broker
         */
        producer.push(activityConfiguration.getUpdateActivityFacilityTopic(), request);
    }

    private void handleUpdateActivityAssignment(ActivityAssignmentBulkRequest request, ActivityAssignment activityAssignment, ActivityAssignment activityAssignmentFromDB) {

        /*
         * Ensure that no other properties are being updated besides the start and end dates
         */
        if (!isValidCascadingUpdateActivityAssignment(activityAssignmentFromDB, activityAssignment)) {
            throw new CustomException(
                    "ACTIVITY_CASCADE_UPDATE_ERROR",
                    "Can only update Activity facility dates, geographyDetails and additional details if cascade FieldPlan date update true"
            );
        }

        /*
         * Update lastModifiedTime and lastModifiedBy for the activity
         */
        activityEnrichment.enrichActivityAssignmentRequestOnUpdate(activityAssignment, activityAssignmentFromDB, request.getRequestInfo());

        /*
         * Check and enrich cascading project dates and push the update to the message broker
         */
        producer.push(activityConfiguration.getUpdateActivityAssignmentTopic(), request);
    }

    private boolean isValidCascadingUpdateActivityFacility(ActivityFacility activityFacilityFromDB, ActivityFacility activityFacility) {
        // Check if only allowed fields are being updated
        return Objects.equals(activityFacilityFromDB.getId(), activityFacility.getId()) &&
                Objects.equals(activityFacilityFromDB.getTenantId(), activityFacility.getTenantId()) &&
                Objects.equals(activityFacilityFromDB.getActivityId(), activityFacility.getActivityId()) &&
                Objects.equals(activityFacilityFromDB.getFacilityId(), activityFacility.getFacilityId());
        // Note: We allow assignedUser, status, conditionsMet, additionalDetails to be different
    }

    private boolean isValidCascadingUpdateActivityAssignment(ActivityAssignment activityAssignmentFromDB, ActivityAssignment activityAssignment) {
        // Check if only allowed fields are being updated
        return Objects.equals(activityAssignmentFromDB.getId(), activityAssignment.getId()) &&
                Objects.equals(activityAssignmentFromDB.getTenantId(), activityAssignment.getTenantId()) &&
                Objects.equals(activityAssignmentFromDB.getActivityId(), activityAssignment.getActivityId()) &&
                Objects.equals(activityAssignmentFromDB.getFieldPlanId(), activityAssignment.getFieldPlanId());
        // Note: We allow assignedUser, status, conditionsMet, additionalDetails to be different
    }

    private ActivityFacility findActivityFacilityById(String activityFacilityId, List<ActivityFacility> activityFacilityListFromDB) {
        /*
         * Find and return the activity with the matching ID from the list of activity fetched from the database
         */
        return activityFacilityListFromDB.stream()
                .filter(p -> activityFacilityId.equals(String.valueOf(p.getId())))
                .findFirst()
                .orElse(null);
    }

    private ActivityAssignment findActivityAssignmentById(String activityAssignmentId, List<ActivityAssignment> activityAssignmentListFromDB) {
        /*
         * Find and return the activity with the matching ID from the list of activity fetched from the database
         */
        return activityAssignmentListFromDB.stream()
                .filter(p -> activityAssignmentId.equals(String.valueOf(p.getId())))
                .findFirst()
                .orElse(null);
    }


}
