package org.egov.activity.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.egov.activity.repository.ActivityAssignmentRepository;
import org.egov.activity.util.ActivityServiceUtil;
import org.egov.activity.util.BoundaryUtil;
import org.egov.common.contract.models.AuditDetails;
import org.egov.common.contract.request.RequestInfo;
import org.egov.common.producer.Producer;
import org.egov.activity.config.ActivityConfiguration;
import org.egov.activity.repository.ActivityFacilityRepository;
import org.egov.activity.service.enrichment.ActivityEnrichment;
import org.egov.activity.validator.ActivityValidator;
import org.egov.activity.web.models.*;
import org.egov.tracer.model.CustomException;
import org.egov.tracer.model.ServiceCallException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.sql.Array;
import java.util.*;
import java.util.stream.Collectors;

import static org.egov.activity.util.ActivityConstants.SUBMITTED_BY_SUPERVISOR;
import static org.egov.common.utils.CommonUtils.populateErrorDetails;

@Service
@Slf4j
public class ActivityService {

    private final ActivityFacilityRepository activityFacilityRepository;

    private final ActivityAssignmentRepository activityAssignmentRepository;
    private final Producer producer;
    private final ActivityServiceUtil activityServiceUtil;
    private final ActivityEnrichment activityEnrichment;
    private final ActivityValidator activityValidator;
    private final ActivityConfiguration activityConfiguration;
    private final FacilityWorkflowService workflowService;
    private ServiceRequestRepository serviceRequest;
    private final JdbcTemplate jdbcTemplate;
    private final ActivityFacilityUsersService facilityUsersService;

    private BoundaryUtil boundaryUtil;

    private final AmcSchedulerService amcSchedulerService;

    @Qualifier("objectMapper")
    private final ObjectMapper mapper;

    @Autowired
    public ActivityService(
            ActivityFacilityRepository activityFacilityRepository, ActivityEnrichment activityEnrichment, ActivityConfiguration activityConfiguration, ActivityValidator activityValidator,
            Producer producer, FacilityWorkflowService workflowService, ActivityServiceUtil activityServiceUtil, ServiceRequestRepository serviceRequest, JdbcTemplate jdbcTemplate, ActivityFacilityUsersService facilityUsersService, @Qualifier("objectMapper") ObjectMapper mapper, ActivityAssignmentRepository activityAssignmentRepository, BoundaryUtil boundaryUtil, AmcSchedulerService amcSchedulerService) {
            this.producer = producer;
            this.activityConfiguration = activityConfiguration;
            this.activityFacilityRepository = activityFacilityRepository;
            this.activityEnrichment = activityEnrichment;
            this.workflowService = workflowService;
            this.jdbcTemplate = jdbcTemplate;
            this.activityServiceUtil = activityServiceUtil;
            this.serviceRequest = serviceRequest;
            this.facilityUsersService = facilityUsersService;
            this.mapper = mapper;
            this.activityValidator = activityValidator;
            this.activityAssignmentRepository = activityAssignmentRepository;
            this.boundaryUtil = boundaryUtil;
            this.amcSchedulerService = amcSchedulerService;
    }

    public List<Activity> createActivity(ActivityBulkRequest request) {
        log.trace("createActivity method invoked");
        List<Activity> activities = request.getActivities();
        int activityCount = activities != null ? activities.size() : 0;
        log.info("Received request to create bulk activities, count: {}", activityCount);
        try {
            log.debug("Processing {} activities for enrichment", activityCount);
            for (Activity activity : activities) {
                log.trace("Enriching activity with id: {}", activity.getId());
                activityEnrichment.enrichActivityRequestOnCreate(activity, request.getRequestInfo());
            }
            log.debug("Pushing activities to topic: {}", activityConfiguration.getCreateActivityTopic());
            producer.push(activityConfiguration.getCreateActivityTopic(), request);
            log.info("Successfully created {} activities", activityCount);
        } catch (Exception exception) {
            log.error("Error occurred while creating activities, count: {}", activityCount, exception);
        }

        return activities;
    }

    public List<ActivityFacility> createActivityFacility(ActivityFacilityBulkRequest request) {
        log.trace("createActivityFacility method invoked");
        log.info("Received request to create bulk activity facilities");
        activityValidator.validateCreateActivityFacilityRequest(request);
        List<ActivityFacility> activityFacilities = request.getActivityFacilities();
        int facilityCount = activityFacilities != null ? activityFacilities.size() : 0;
        log.debug("Processing {} activity facilities", facilityCount);

        try {
            List<ActivityFacilityUser> activityFacilityUsers = enrichFacilitiesAndCollectUsers(request, activityFacilities);
            createLinkedUsersIfPresent(request, activityFacilityUsers);
            pushCreateActivityFacilities(request, facilityCount);
        } catch (Exception exception) {
            log.error("Error occurred while creating activity facilities, count: {}", facilityCount, exception);
        }

        return activityFacilities;
    }

    private List<ActivityFacilityUser> enrichFacilitiesAndCollectUsers(ActivityFacilityBulkRequest request, List<ActivityFacility> activityFacilities) {
        List<ActivityFacilityUser> activityFacilityUsers = new ArrayList<>();
        for (ActivityFacility activityFacility : activityFacilities) {
            log.trace("Enriching activity facility with id: {}", activityFacility.getId());
            activityEnrichment.enrichActivityFacilityRequestOnCreate(activityFacility, request.getRequestInfo());
            List<ActivityFacilityUser> usersFacility = buildUsersForActivityFacility(activityFacility);
            activityFacilityUsers.addAll(removeDuplicateUsers(usersFacility));
        }
        return activityFacilityUsers;
    }

    private List<ActivityFacilityUser> buildUsersForActivityFacility(ActivityFacility activityFacility) {
        List<ActivityFacilityUser> usersFacility = new ArrayList<>();
        addUsersForRole(activityFacility, activityFacility.getReviewerUser(), usersFacility);
        addUsersForRole(activityFacility, activityFacility.getFieldStaffUsers(), usersFacility);
        addUsersForRole(activityFacility, activityFacility.getFieldSupervisorUsers(), usersFacility);
        return usersFacility;
    }

    private void addUsersForRole(ActivityFacility activityFacility, List<String> users, List<ActivityFacilityUser> usersFacility) {
        if (users == null || users.isEmpty()) {
            return;
        }
        for (String userId : users) {
            ActivityFacilityUser facilityUser = ActivityFacilityUser.builder()
                    .activityFacilityId(activityFacility.getId())
                    .userId(userId)
                    .tenantId(activityFacility.getTenantId())
                    .isDeleted(false)
                    .build();
            usersFacility.add(facilityUser);
        }
    }

    private List<ActivityFacilityUser> removeDuplicateUsers(List<ActivityFacilityUser> usersFacility) {
        Set<String> seenUsers = new HashSet<>();
        return usersFacility.stream()
                .filter(a -> seenUsers.add(a.getUserId()))
                .toList();
    }

    private void createLinkedUsersIfPresent(ActivityFacilityBulkRequest request, List<ActivityFacilityUser> activityFacilityUsers) throws Exception {
        if (activityFacilityUsers == null || activityFacilityUsers.isEmpty()) {
            return;
        }
        log.debug("Creating {} activity facility user mappings", activityFacilityUsers.size());
        ActivityFacilityUserBulkRequest activityFacilityUserBulkRequest = ActivityFacilityUserBulkRequest.builder()
                .requestInfo(request.getRequestInfo())
                .activityFacilityUsers(activityFacilityUsers)
                .build();
        facilityUsersService.createActivityFacilityUsers(activityFacilityUserBulkRequest);
    }

    private void pushCreateActivityFacilities(ActivityFacilityBulkRequest request, int facilityCount) {
        log.debug("Pushing activity facilities to topic: {}", activityConfiguration.getCreateActivityFacilityTopic());
        producer.push(activityConfiguration.getCreateActivityFacilityTopic(), request);
        log.info("Successfully created {} activity facilities", facilityCount);
    }

    public List<ActivityAssignment> createActivityAssignment(ActivityAssignmentBulkRequest request) {
        log.trace("createActivityAssignment method invoked");
        log.info("Received request to create bulk activity assignments");
        activityValidator.validateCreateActivityAssignmentRequest(request);
        List<ActivityAssignment> activityAssignments = request.getActivityAssignments();
        int assignmentCount = activityAssignments != null ? activityAssignments.size() : 0;
        log.debug("Processing {} activity assignments", assignmentCount);
        try {
            for (ActivityAssignment activityAssignment : activityAssignments) {
                log.trace("Enriching activity assignment with id: {}", activityAssignment.getId());
                activityEnrichment.enrichActivityAssignmentOnCreate(activityAssignment, request.getRequestInfo());
            }
            log.debug("Pushing activity assignments to topic: {}", activityConfiguration.getCreateActivityAssignmentTopic());
            producer.push(activityConfiguration.getCreateActivityAssignmentTopic(), request);
            log.info("Successfully created {} activity assignments", assignmentCount);
        } catch (Exception exception) {
            log.error("Error occurred while creating activity assignments, count: {}", assignmentCount, exception);
        }

        return activityAssignments;
    }

    public List<ActivityAssignment> unassignActivityAssignment(ActivityAssignmentBulkRequest request) {
        log.trace("unassignActivityAssignment method invoked");
        log.info("Received request to unassign bulk activity assignments");
        activityValidator.validateDeleteActivityAssignmentRequest(request);
        List<ActivityAssignment> activityAssignments = request.getActivityAssignments();
        int assignmentCount = activityAssignments != null ? activityAssignments.size() : 0;
        log.debug("Processing {} activity assignments for unassignment", assignmentCount);
        try {
            for (ActivityAssignment activityAssignment : activityAssignments) {
                log.trace("Unassigning activity assignment with id: {}", activityAssignment.getId());
                activityEnrichment.enrichFieldPlanRequestOnDelete(activityAssignment, request.getRequestInfo());
            }
            log.debug("Pushing unassignment request to topic: {}", activityConfiguration.getUnassignActivityAssignmentTopic());
            producer.push(activityConfiguration.getUnassignActivityAssignmentTopic(), request);
            log.info("Successfully unassigned {} activity assignments", assignmentCount);
        } catch (Exception exception) {
            log.error("Error occurred while unassigning activity assignments, count: {}", assignmentCount, exception);
        }

        return activityAssignments;
    }

    public List<ActivityFacility> searchActivityFacility(ActivityFacilitySearchRequest request, Integer limit, Integer offset, String tenantId, Boolean includeDeleted, Long lastChangedSince) {
        log.trace("searchActivityFacility method invoked with limit: {}, offset: {}, tenantId: {}", limit, offset, tenantId);
        activityValidator.validateSearchActivityRequest(request, limit, offset, tenantId);
        log.debug("Fetching activity facilities from repository");
        List<ActivityFacility> activityFacilities = activityFacilityRepository.getActivitiesFacility(request, limit, offset, tenantId, includeDeleted, lastChangedSince);
        log.debug("Retrieved {} activity facilities from repository", activityFacilities != null ? activityFacilities.size() : 0);
        Map<String, Boundary> listBlock = boundaryUtil.getBoundaryByCode();
        log.debug("Loaded {} boundaries for enrichment", listBlock.size());
        for (ActivityFacility activityFacility : activityFacilities) {
            log.trace("Enriching activity facility with id: {}", activityFacility.getId());
            activityEnrichment.enrichActivityFacilityOnSearch(request, activityFacility);

            Object additionalDetails = activityFacility.getFacility().getAdditionalDetails();
            String boundaryCode = activityFacility.getFacility().getBoundaryCode();
            log.trace("Processing facilityId={} with boundaryCode={}", activityFacility.getFacility().getId(), boundaryCode);

            if (boundaryCode != null) {
                Boundary boundary = listBlock.get(boundaryCode);
                if (boundary != null) {
                    log.debug("Enriching activityFacilityId={} with state={} and district={}", activityFacility.getId(), boundary.getState(), boundary.getDistrict());

                    Object enrichedAdditionalDetails = mergeListIntoAdditionalDetails(additionalDetails, "state", boundary.getState());
                    activityFacility.getFacility().setAdditionalDetails((Map<String, Object>) enrichedAdditionalDetails);

                    additionalDetails = activityFacility.getFacility().getAdditionalDetails();
                    enrichedAdditionalDetails = mergeListIntoAdditionalDetails(additionalDetails, "district", boundary.getDistrict());
                    activityFacility.getFacility().setAdditionalDetails((Map<String, Object>) enrichedAdditionalDetails);
                } else {
                    log.warn("No boundary found for code={} in activityFacilityId={}", boundaryCode, activityFacility.getId());
                }
            }
        }

            return activityFacilities;
    }

    public List<FacilityStatusAgregation> getStatusFacilityAssignmentsAgregation(String fieldPlanId) {
        return activityFacilityRepository.getStatusFacilitiesAgregation(fieldPlanId);
    }

    public List<Transaction> getTransactionsForActivityFacility(List<String> projectIds) {
        if (projectIds == null || projectIds.isEmpty()) return Collections.emptyList();

        String sql = "SELECT id, activity_facility_id, process_instance_id, created_by, last_modified_by, created_time, last_modified_time " +
                "FROM activity_facility_transaction WHERE activity_facility_id = ANY(?)";

        return jdbcTemplate.query(sql, ps -> {
            Array sqlArray = ps.getConnection().createArrayOf("text", projectIds.toArray(new String[0]));
            ps.setArray(1, sqlArray);
        }, (rs, rowNum) -> {
            Transaction transaction = new Transaction();
            transaction.setTransactionId(rs.getString("id"));
            transaction.setActivityFacilityId(rs.getString("activity_facility_id"));
            transaction.setProcessInstanceId(rs.getString("process_instance_id"));
            AuditDetails auditDetails = new AuditDetails();
            auditDetails.setCreatedBy(rs.getString("created_by"));
            auditDetails.setLastModifiedBy(rs.getString("last_modified_by"));
            auditDetails.setCreatedTime(rs.getLong("created_time"));
            auditDetails.setLastModifiedTime(rs.getLong("last_modified_time"));
            transaction.setAuditDetails(auditDetails);
            return transaction;
        });
    }

    public List<Comment> getCommentsForTransaction(List<String> transactionIds) {
        if (transactionIds == null || transactionIds.isEmpty()) return Collections.emptyList();

        String inSql = String.join(",", Collections.nCopies(transactionIds.size(), "?"));
        String sql = "SELECT id, transaction_id, comment_message, asset_type, created_by, last_modified_by, created_time, last_modified_time " +
                "FROM activity_facility_transaction_comment WHERE transaction_id IN (" + inSql + ")";

        return jdbcTemplate.query(sql, transactionIds.toArray(), (rs, rowNum) -> {
            Comment comment = new Comment();
            comment.setCmtId(UUID.fromString(rs.getString("id")));
            comment.setTransactionId(rs.getString("transaction_id"));
            comment.setCmtMsg(rs.getString("comment_message"));
            comment.setAssetType(rs.getString("asset_type"));
            AuditDetails auditDetails = new AuditDetails();
            auditDetails.setCreatedBy(rs.getString("created_by"));
            auditDetails.setLastModifiedBy(rs.getString("last_modified_by"));
            auditDetails.setCreatedTime(rs.getLong("created_time"));
            auditDetails.setLastModifiedTime(rs.getLong("last_modified_time"));
            comment.setAuditDetails(auditDetails);
            return comment;
        });
    }

    public List<ActivityFacility> delete(ActivityFacilityBulkRequest request) {
        log.trace("delete method invoked");
        log.info("Received request to delete bulk activity facilities");
        activityValidator.validateActivityFacilityDeleteRequest(request);
        List<ActivityFacility> validEntities = request.getActivityFacilities();
        int deleteCount = validEntities != null ? validEntities.size() : 0;
        log.debug("Processing {} activity facilities for deletion", deleteCount);
        try {
            if (!validEntities.isEmpty()) {
                for (ActivityFacility activityFacility : validEntities) {
                    log.trace("Deleting activity facility with id: {}", activityFacility.getId());
                    // 1. Fetch the existing facility
                    ActivityFacilitySearchCriteria searchCriteria = ActivityFacilitySearchCriteria.builder()
                            .ids(List.of(activityFacility.getId()))
                            .tenantId(activityConfiguration.getTenantId())
                            .build();

                    ActivityFacilitySearchRequest searchRequest = ActivityFacilitySearchRequest.builder()
                            .criteria(searchCriteria)
                            .requestInfo(request.getRequestInfo())
                            .build();

                    List<ActivityFacility> activityFacilities = searchActivityFacility(searchRequest, activityConfiguration.getMaxLimit(), activityConfiguration.getDefaultOffset(),
                            activityConfiguration.getTenantId(), false, null);
                    if(activityFacilities == null || activityFacilities.isEmpty()){
                        log.error("Activity facility not found for deletion, id: {}", activityFacility.getId());
                        throw new CustomException("Activity Facility Delete", "Activity Facility ID do not exist");
                    }
                    activityFacility.setIsDeleted(true);
                    activityEnrichment.enrichActivityFacilityRequestOnUpdate(activityFacility, activityFacilities.get(0), request.getRequestInfo());
                    log.debug("Pushing delete request to topic: {}", activityConfiguration.getDeleteActivityFacilityTopic());
                    producer.push(activityConfiguration.getDeleteActivityFacilityTopic(), request);
                }
                log.info("Successfully marked {} activity facilities as deleted", deleteCount);
            }
        } catch (Exception exception) {
            log.error("Error occurred while deleting activity facilities, count: {}", deleteCount, exception);
        }

        return validEntities;
    }

    public List<ActivityAssignment> searchAssignedActivity(ActivityAssignmentSearchRequest request, Integer limit, Integer offset, String tenantId, Boolean includeDeleted, Long lastChangedSince) {
        log.trace("searchAssignedActivity method invoked with limit: {}, offset: {}, tenantId: {}", limit, offset, tenantId);
        activityValidator.validateSearchAssignActivityRequest(request, limit, offset, tenantId);
        log.debug("Fetching activity assignments from repository");
        List<ActivityAssignment> activityFacilities = activityAssignmentRepository.getActivitiesAssignment(request, limit, offset, tenantId, includeDeleted, lastChangedSince);
        log.debug("Retrieved {} activity assignments from repository", activityFacilities != null ? activityFacilities.size() : 0);
        for (ActivityAssignment activityAssignment : activityFacilities) {
            log.trace("Enriching activity assignment with id: {}", activityAssignment.getId());
            activityEnrichment.enrichActivityAssignmentOnSearch(request.getRequestInfo(), activityAssignment);
            List<FacilityStatusAgregation> statusAgregations = getStatusFacilityAssignmentsAgregation(activityAssignment.getFieldPlanId());
            if (statusAgregations != null) {
                log.debug("Adding status aggregation for fieldPlanId: {}", activityAssignment.getFieldPlanId());
                Object enrichedAdditionalDetails = mergeListIntoAdditionalDetails(activityAssignment.getAdditionalDetails(), "statusAgregation", statusAgregations);
                activityAssignment.setAdditionalDetails((Map<String, Object>) enrichedAdditionalDetails);
            }
        }
        return activityFacilities;
    }

    public FacilityStatusWrapper updateFacilityWorkflow(FacilityWorkflowRequest request) throws Exception {
        log.trace("updateFacilityWorkflow method invoked for activityFacilityId: {}", request.getActivityFacilityId());
        log.info("Updating workflow for activity facility: {}, action: {}", request.getActivityFacilityId(), request.getWorkflow().getAction());
        ActivityFacility existingActivityFacility = fetchExistingActivityFacilityForWorkflow(request);
        ProcessInstance updatedWorkflow = transitionWorkflow(request, existingActivityFacility);
        handleWorkflowTransactionsIfPresent(request, updatedWorkflow);
        ActivityFacility updatedActivityFacility = buildUpdatedActivityFacility(existingActivityFacility, updatedWorkflow);
        ActivityFacilityBulkRequest enrichedRequest = buildActivityFacilityBulkRequest(request, updatedActivityFacility);
        log.debug("Updating activity facility after workflow transition");
        handleUpdateActivityFacility(enrichedRequest, updatedActivityFacility, existingActivityFacility);
        handlePostApprovalSideEffects(request, existingActivityFacility);

        log.info("Workflow update completed for activity facility: {}, new status: {}", request.getActivityFacilityId(), updatedWorkflow.getState().getState());
        return new FacilityStatusWrapper(updatedActivityFacility, updatedWorkflow.getState().getState(), null, null);
    }

    private ActivityFacility fetchExistingActivityFacilityForWorkflow(FacilityWorkflowRequest request) {
        ActivityFacilitySearchCriteria searchCriteria = ActivityFacilitySearchCriteria.builder()
                .ids(List.of(request.getActivityFacilityId()))
                .tenantId(activityConfiguration.getTenantId())
                .build();

        ActivityFacilitySearchRequest searchRequest = ActivityFacilitySearchRequest.builder()
                .criteria(searchCriteria)
                .requestInfo(request.getRequestInfo())
                .build();

        log.debug("Fetching existing activity facility for workflow update");
        List<ActivityFacility> activityFacilities = searchActivityFacility(
                searchRequest,
                activityConfiguration.getMaxLimit(),
                activityConfiguration.getDefaultOffset(),
                activityConfiguration.getTenantId(),
                false,
                null
        );

        if (activityFacilities == null || activityFacilities.isEmpty()) {
            log.error("Activity facility not found for workflow update, id: {}", request.getActivityFacilityId());
            throw new CustomException("FACILITY_NOT_FOUND", "Activity Facility not found with ID: " + request.getActivityFacilityId());
        }

        return activityFacilities.get(0);
    }

    private ProcessInstance transitionWorkflow(FacilityWorkflowRequest request, ActivityFacility existingActivityFacility) {
        try {
            log.debug("Transitioning workflow with action: {}", request.getWorkflow().getAction());
            ProcessInstance updatedWorkflow = workflowService.transitionWorkflow(
                    existingActivityFacility,
                    request.getWorkflow().getAction(),
                    request.getWorkflow().getDocuments(),
                    request.getRequestInfo(),
                    request.getWorkflow().getComments()
            );
            log.debug("Workflow transition successful, new state: {}", updatedWorkflow.getState() != null ? updatedWorkflow.getState().getState() : "null");
            return updatedWorkflow;
        } catch (Exception e) {
            log.error("Failed to transition workflow for activity facility: {}, action: {}", request.getActivityFacilityId(), request.getWorkflow().getAction(), e);
            throw new CustomException("WORKFLOW_TRANSITION_FAILED",
                    "Failed to transition workflow for facility: " + request.getActivityFacilityId());
        }
    }

    private void handleWorkflowTransactionsIfPresent(FacilityWorkflowRequest request, ProcessInstance updatedWorkflow) {
        if (request.getTransactions() != null && !request.getTransactions().isEmpty()) {
            log.debug("Processing {} transactions for workflow update", request.getTransactions().size());
            handleTransactionsAndComment(request, updatedWorkflow);
        }
    }

    private ActivityFacility buildUpdatedActivityFacility(ActivityFacility existingActivityFacility, ProcessInstance updatedWorkflow) {
        existingActivityFacility.setStatus(updatedWorkflow.getState().getState());
        log.debug("Updated activity facility status to: {}", updatedWorkflow.getState().getState());

        return ActivityFacility.builder()
                .id(existingActivityFacility.getId())
                .tenantId(existingActivityFacility.getTenantId())
                .activityId(existingActivityFacility.getActivityId())
                .facilityId(existingActivityFacility.getFacilityId())
                .fieldPlanId(existingActivityFacility.getFieldPlanId())
                .status(existingActivityFacility.getStatus())
                .assignedUser(existingActivityFacility.getAssignedUser())
                .activatedAt(existingActivityFacility.getActivatedAt())
                .completedAt(System.currentTimeMillis())
                .scheduledAt(existingActivityFacility.getScheduledAt())
                .build();
    }

    private ActivityFacilityBulkRequest buildActivityFacilityBulkRequest(FacilityWorkflowRequest request, ActivityFacility updatedActivityFacility) {
        return ActivityFacilityBulkRequest.builder()
                .requestInfo(request.getRequestInfo())
                .activityFacilities(List.of(updatedActivityFacility))
                .build();
    }

    private void handlePostApprovalSideEffects(FacilityWorkflowRequest request, ActivityFacility existingActivityFacility) throws Exception {
        if (!"APPROVE".equalsIgnoreCase(request.getWorkflow().getAction())) {
            return;
        }

        log.info("Processing approval side effects for activity facility: {}", existingActivityFacility.getId());
        String activityFacilityId = existingActivityFacility.getId();
        if (activityFacilityId == null) {
            return;
        }

        updateAssetsForFacility(existingActivityFacility, request.getRequestInfo(), activityFacilityId);
        triggerInstallationCompletionSideEffects(existingActivityFacility, request.getRequestInfo(), activityFacilityId);
        markFacilityOnmReady(existingActivityFacility, request.getRequestInfo());
    }

    private void handleTransactionsAndComment(FacilityWorkflowRequest request, ProcessInstance updatedWorkflow) {
        for(Transaction transaction: request.getTransactions()) {
            transaction.setProcessInstanceId(updatedWorkflow.getId());
            String userUUID = request.getRequestInfo().getUserInfo().getUuid();
            transaction.setActivityFacilityId(request.getActivityFacilityId());
            transaction.setAuditDetails(activityServiceUtil.getAuditDetails(userUUID, null, true));
            if(transaction.getTransactionId() == null || transaction.getTransactionId().isEmpty()) {
                transaction.setTransactionId(UUID.randomUUID().toString());
            }
            if(transaction.getComments() != null) handleCommentUpdate(transaction.getComments(), transaction.getTransactionId(), userUUID);
        }
        handleTransactionUpdate(request.getTransactions());
    }

    public void handleCommentUpdate(List<Comment> comments, String txId, String uuid) {
        comments.forEach(comment -> {
            comment.setAuditDetails(activityServiceUtil.getAuditDetails(uuid, null, true));
            if (comment.getCmtId() == null) {
                comment.setCmtId(UUID.randomUUID());
            }
            comment.setTransactionId(txId);
        });

        producer.push(activityConfiguration.getCommentPersistTopic(), new CommentRequest(comments));
    }

    private void handleTransactionUpdate(List<Transaction> transactions) {
        producer.push(activityConfiguration.getTransactionPersistTopic(), new TransactionRequest(transactions));
    }

    /**
     * Mark the underlying facility record as ONM ready (is_onm_ready = true) after installation approval.
     * Flow:
     *  - Fetch facility by facilityId using facility-service V2 search
     *  - Call facility-service update API to set is_onm_ready = true
     */
    private void markFacilityOnmReady(ActivityFacility activityFacility, RequestInfo requestInfo) {
        String facilityId = activityFacility.getFacilityId();
        try {
            if (facilityId == null || facilityId.isEmpty()) {
                log.warn("Cannot mark facility ONM ready: facilityId is null for activityFacility {}", activityFacility.getId());
                return;
            }

            Facility facility = activityValidator.getFacilityById(facilityId);
            if (facility == null) {
                log.warn("Facility not found in facility-service for facilityId {}. Skipping ONM ready update.", facilityId);
                return;
            }

            facility.setIsOnmReady(Boolean.TRUE);

            Map<String, Object> updateRequest = new HashMap<>();
            updateRequest.put("RequestInfo", requestInfo);
            updateRequest.put("Facility", facility);

            String url = activityConfiguration.getFacilityServiceHost()
                    + activityConfiguration.getFacilityServiceUpdateUrl();

            log.info("Marking facility as ONM ready, facilityId: {}, activityFacilityId: {}", facilityId, activityFacility.getId());
            log.debug("Calling facility service update endpoint: {}", url);
            serviceRequest.fetchResult(new StringBuilder(url), updateRequest);
            log.debug("Successfully marked facility as ONM ready, facilityId: {}", facilityId);
        } catch (Exception e) {
            log.error("Failed to mark facility ONM ready, facilityId: {}, activityFacilityId: {}", facilityId, activityFacility.getId(), e);
        }
    }

    private void updateAssetsForFacility(ActivityFacility activityFacility, RequestInfo requestInfo, String facilityId) throws CustomException {
        log.trace("updateAssetsForFacility method invoked for activityFacilityId: {}, facilityId: {}", activityFacility.getId(), facilityId);
        log.debug("Searching assets for facility: {}", facilityId);
        AssetSearchCriteria assetSearchCriteria = AssetSearchCriteria.builder()
                .activityFacilityID(facilityId)
                .tenantId(activityFacility.getTenantId())
                .build();

        AssetSearchRequest assetSearchRequest = AssetSearchRequest.builder()
                .requestInfo(requestInfo)
                .criteria(assetSearchCriteria)
                .build();

        StringBuilder assetSearchUri = new StringBuilder(activityConfiguration.getAssetHost())
                .append(activityConfiguration.getAssetSearchUrl());

        try {
            List<Asset> assets = serviceRequest.fetchResult(assetSearchUri, assetSearchRequest, new TypeReference<List<Asset>>() {});
            int assetCount = assets != null ? assets.size() : 0;
            log.debug("Found {} assets for facility: {}", assetCount, facilityId);
            if (assets != null && !assets.isEmpty()) {
                for (Asset asset : assets) {
                    log.trace("Updating operational status for asset: {}", asset.getAssetId());
                    updateAssetOperationalStatus(asset, requestInfo);
                }
                log.info("Successfully updated operational status for {} assets", assetCount);
            } else {
                log.debug("No assets found for facility: {}", facilityId);
            }
        } catch (ServiceCallException e) {
            log.error("Service call failed while processing assets, activityFacilityId: {}, facilityId: {}", activityFacility.getId(), facilityId, e);
            throw new CustomException("ASSET_UPDATE_FAILED", "Failed to update asset operational status");
        } catch (Exception e) {
            log.error("Unexpected error while processing assets, activityFacilityId: {}, facilityId: {}", activityFacility.getId(), facilityId, e);
            throw new CustomException("ASSET_PROCESSING_ERROR", "An error occurred while processing assets");
        }
    }

    private void updateAssetOperationalStatus(Asset asset, RequestInfo requestInfo) {
        log.trace("updateAssetOperationalStatus method invoked for assetId: {}", asset.getAssetId());
        try {
            asset.setIsOperational(true);

            String assetUpdateEndpoint = activityConfiguration.getAssetHost() +
                    activityConfiguration.getAssetUpdateUrl();

            StringBuilder assetUpdateUri = new StringBuilder(assetUpdateEndpoint);
            assetUpdateUri.append("?assetID=").append(asset.getAssetId());

            AssetCreate assetCreate = AssetCreate.builder()
                    .asset(asset)
                    .build();

            AssetCreateRequest createRequest = AssetCreateRequest.builder()
                    .requestInfo(requestInfo)
                    .assetDetail(assetCreate)
                    .build();

            log.debug("Updating asset operational status, assetId: {}", asset.getAssetId());
            serviceRequest.fetchResult(assetUpdateUri, createRequest);
            log.debug("Successfully updated asset operational status, assetId: {}", asset.getAssetId());
        } catch (Exception e) {
            log.error("Failed to update asset operational status, assetId: {}", asset.getAssetId(), e);
        }
    }

    public Map<String, Object> updateBulkActivityFacilityWorkflow(FacilityBulkApproveRequest facilityBulkApproveRequest) throws Exception {
        log.trace("updateBulkActivityFacilityWorkflow method invoked, isAllSelected: {}", facilityBulkApproveRequest.getIsAllSelected());
        log.info("Starting bulk workflow update, isAllSelected: {}", facilityBulkApproveRequest.getIsAllSelected());
        List<String> activityFacilityIds = new ArrayList<>();
        int totalActivityFacilities = 0;
        int finalActivityFacilities;

        BulkActivityFacilitySelection selection = resolveActivityFacilitiesForBulkUpdate(facilityBulkApproveRequest);
        activityFacilityIds = selection.activityFacilityIds();
        totalActivityFacilities = selection.totalActivityFacilities();
        finalActivityFacilities = selection.finalActivityFacilities();

        Map<String, Object> result = new HashMap<>();
        // Validate that we have projects to process
        if (activityFacilityIds.isEmpty()) {
            log.warn("No activity facilities to process for bulk workflow update");
            result.put("failedActivityFacilitiesIDs", new ArrayList<>());
            result.put("succeededActivityFacilitiesIDs", new ArrayList<>());
            result.put("totalActivityFacilties", 0);
            return result;
        }

        // Update workflow for all project IDs
        log.info("Starting bulk workflow update for {} activity facilities", activityFacilityIds.size());
        List<String> failedActivityFacilityIDs = new ArrayList<>();
        List<String> succeededActivityFacilityIDs = new ArrayList<>();
        processBulkWorkflowUpdates(facilityBulkApproveRequest, activityFacilityIds, failedActivityFacilityIDs, succeededActivityFacilityIDs);
        log.info("Bulk workflow update completed. Succeeded: {}, Failed: {}", succeededActivityFacilityIDs.size(), failedActivityFacilityIDs.size());

        result.put("failedActivityFacilityIDs", failedActivityFacilityIDs);
        result.put("succeededActivityFacilityIDs", succeededActivityFacilityIDs);
        if(facilityBulkApproveRequest.getIsAllSelected() && finalActivityFacilities > 0) {
            result.put("totalActivityFacilities", finalActivityFacilities);
        } else {
            result.put("totalActivityFacilities", totalActivityFacilities);
        }
        return result;
    }

    private record BulkActivityFacilitySelection(List<String> activityFacilityIds, int totalActivityFacilities, int finalActivityFacilities) {}

    private BulkActivityFacilitySelection resolveActivityFacilitiesForBulkUpdate(FacilityBulkApproveRequest facilityBulkApproveRequest) {
        if (facilityBulkApproveRequest.getIsAllSelected()) {
            return resolveAllSelectedActivityFacilities(facilityBulkApproveRequest);
        } else {
            return resolveExplicitActivityFacilities(facilityBulkApproveRequest);
        }
    }

    private BulkActivityFacilitySelection resolveAllSelectedActivityFacilities(FacilityBulkApproveRequest facilityBulkApproveRequest) {
        log.debug("Processing all selected activity facilities with filters");
        if (facilityBulkApproveRequest.getFilters() == null) {
            throw new CustomException("INVALID_REQUEST", "Filters are required when isAllSelected is true");
        }

        ActivityFacilitySearchCriteria searchCriteria = facilityBulkApproveRequest.getFilters().getSearchCriteria();
        searchCriteria.setTenantId(activityConfiguration.getTenantId());

        ActivityFacilitySearchRequest searchRequest = ActivityFacilitySearchRequest.builder()
                .criteria(searchCriteria)
                .requestInfo(facilityBulkApproveRequest.getRequestInfo())
                .build();

        List<ActivityFacility> activityFacilities = searchActivityFacility(
                searchRequest,
                activityConfiguration.getMaxLimit(),
                activityConfiguration.getDefaultOffset(),
                activityConfiguration.getTenantId(),
                false,
                null
        );
        int totalActivityFacilities = countAllFacilityActivities(searchRequest, activityConfiguration.getTenantId(), null, null);

        List<ActivityFacility> activityFacilitiesList = activityFacilities.stream()
                .filter(this::hasSubmittedBySupervisorStatus)
                .toList();
        log.debug("Filtered {} activity facilities with SUBMITTED_BY_SUPERVISOR status from {} total", activityFacilitiesList.size(), activityFacilities.size());

        int finalActivityFacilities = activityFacilitiesList.size();
        List<String> activityFacilityIds = activityFacilitiesList.stream()
                .map(ActivityFacility::getId)
                .collect(Collectors.toList());

        return new BulkActivityFacilitySelection(activityFacilityIds, totalActivityFacilities, finalActivityFacilities);
    }

    private BulkActivityFacilitySelection resolveExplicitActivityFacilities(FacilityBulkApproveRequest facilityBulkApproveRequest) {
        if (facilityBulkApproveRequest.getActivityFacilityIds() == null || facilityBulkApproveRequest.getActivityFacilityIds().isEmpty()) {
            log.error("Activity facility IDs are required when isAllSelected is false");
            throw new CustomException("INVALID_REQUEST", "activity facility IDs are required when isAllSelected is false");
        }
        List<String> activityFacilityIds = facilityBulkApproveRequest.getActivityFacilityIds();
        int totalActivityFacilities = activityFacilityIds.size();
        log.debug("Processing {} provided activity facility IDs", totalActivityFacilities);

        return new BulkActivityFacilitySelection(activityFacilityIds, totalActivityFacilities, 0);
    }

    private void processBulkWorkflowUpdates(FacilityBulkApproveRequest facilityBulkApproveRequest,
                                            List<String> activityFacilityIds,
                                            List<String> failedActivityFacilityIDs,
                                            List<String> succeededActivityFacilityIDs) {
        for (String activityFacilityId : activityFacilityIds) {
            try {
                log.trace("Processing workflow update for activity facility: {}", activityFacilityId);
                FacilityWorkflowRequest workflowRequest = FacilityWorkflowRequest.builder()
                        .requestInfo(facilityBulkApproveRequest.getRequestInfo())
                        .activityFacilityId(activityFacilityId)
                        .workflow(facilityBulkApproveRequest.getWorkflow())
                        .build();

                updateFacilityWorkflow(workflowRequest);
                log.debug("Successfully updated workflow for activity facility: {}", activityFacilityId);
                succeededActivityFacilityIDs.add(activityFacilityId);
            } catch (Exception e) {
                log.error("Failed to update workflow for activity facility: {}", activityFacilityId, e);
                failedActivityFacilityIDs.add(activityFacilityId);
            }
        }
    }

    private boolean hasSubmittedBySupervisorStatus(ActivityFacility activityFacility) {
        String activityFacilityStatus = activityFacility.getStatus();
        return activityFacilityStatus != null && SUBMITTED_BY_SUPERVISOR.equals(activityFacilityStatus);
    }

    public Integer countAllFacilityActivities(ActivityFacilitySearchRequest request, String tenantId, Long lastChangedSince, Boolean includeDeleted) {
        return activityFacilityRepository.getActivitiesFacilityCount(request, tenantId, lastChangedSince, includeDeleted);
    }

    public Integer countAllAssignedActivities(ActivityAssignmentSearchRequest request, String tenantId, Long lastChangedSince, Boolean includeDeleted) {
        return activityAssignmentRepository.getActivitiesCount(request, tenantId, lastChangedSince, includeDeleted);
    }

    public ActivityFacilityBulkRequest updateActivityFacility(ActivityFacilityBulkRequest request) {
        log.trace("updateActivityFacility method invoked");
        int facilityCount = request.getActivityFacilities() != null ? request.getActivityFacilities().size() : 0;
        log.info("Received request to update {} activity facilities", facilityCount);
        /*
         * Validate the update activity request
         */
        activityValidator.validateCreateActivityFacilityRequest(request);
        log.debug("Activity facility update request validated");

        /*
         * Search for fieldplan based on fieldplan IDs provided in the request
         */
        log.debug("Fetching existing activity facilities from database");
        List<ActivityFacility> activityFacilityListFromDB = searchActivityFacility(
                getSearchActivityFacilityRequest(request.getActivityFacilities(), request.getRequestInfo()),
                activityConfiguration.getMaxLimit(), activityConfiguration.getDefaultOffset(),
                request.getActivityFacilities().get(0).getTenantId(), false, null);
        log.debug("Retrieved {} activity facilities from database for update", activityFacilityListFromDB != null ? activityFacilityListFromDB.size() : 0);

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
        log.trace("updateActivityAssignment method invoked");
        int assignmentCount = request.getActivityAssignments() != null ? request.getActivityAssignments().size() : 0;
        log.info("Received request to update {} activity assignments", assignmentCount);
        /*
         * Validate the update activity request
         */
        activityValidator.validateUpdateActivityAssignment(request);
        log.debug("Activity assignment update request validated");

        /*
         * Search for fieldplan based on fieldplan IDs provided in the request
         */
        log.debug("Fetching existing activity assignments from database");
        List<ActivityAssignment> activityAssignmentListFromDB = searchAssignedActivity(
                getSearchActivityAssignmentRequest(request.getActivityAssignments(), request.getRequestInfo()),
                activityConfiguration.getMaxLimit(), activityConfiguration.getDefaultOffset(),
                request.getActivityAssignments().get(0).getTenantId(), false, null);
        log.debug("Retrieved {} activity assignments from database for update", activityAssignmentListFromDB != null ? activityAssignmentListFromDB.size() : 0);

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
         * Ensure that no other properties are being updated besides the assignedUser, status, conditionsMet, additionalDetails
         */
        ActivitySearchCriteria criteria = ActivitySearchCriteria.builder().ids(List.of(activityFacility.getActivityId())).build();
        Activity existingActivity = activityFacilityRepository.getActivityObject(criteria);
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

    private Object mergeListIntoAdditionalDetails(Object additionalDetails, String key, Object value) {
        if (additionalDetails instanceof Map) {
            ((Map<String, Object>) additionalDetails).put(key, value);
            return additionalDetails;
        } else {
            // default to HashMap if null or unknown type
            Map<String, Object> map = new HashMap<>();
            map.put(key, value);
            return map;
        }
    }

    /**
     * Trigger installation completion side effects:
     * 1. Create Asset AMCs for installed assets from the project's AMC configuration
     * 2. Generate all future visits for each configuration in DRAFT state
     */
    private void triggerInstallationCompletionSideEffects(ActivityFacility activityFacility, RequestInfo requestInfo, String activityFacilityId) {
        try {
            log.info("Triggering installation completion side effects for activity facility: {}", activityFacilityId);

            String projectId = resolveProjectId(activityFacility, requestInfo, activityFacilityId);
            if (projectId == null || projectId.isEmpty()) {
                return;
            }

            List<Asset> installedAssets = fetchInstalledAssets(activityFacility, requestInfo, activityFacilityId);
            if (installedAssets == null || installedAssets.isEmpty()) {
                return;
            }

            Long installationDate = resolveInstallationDate(activityFacility);

            log.debug("Calling AMC scheduler service for installation completion, projectId: {}, facilityId: {}",
                    projectId, activityFacility.getFacilityId());
            amcSchedulerService.processInstallationCompletion(
                    projectId,
                    activityFacility.getFacilityId(),
                    activityFacility.getTenantId(),
                    installedAssets,
                    installationDate,
                    requestInfo
            );

            log.info("Successfully triggered installation completion side effects for activity facility: {}, assets processed: {}",
                    activityFacilityId, installedAssets.size());

        } catch (Exception e) {
            log.error("Error triggering installation completion side effects for activity facility: {}", activityFacilityId, e);
        }
    }

    private String resolveProjectId(ActivityFacility activityFacility, RequestInfo requestInfo, String activityFacilityId) {
        String projectId = null;
        if (activityFacility.getFieldPlanId() != null) {
            log.debug("Fetching field plan for fieldPlanId: {}", activityFacility.getFieldPlanId());
            FieldPlan fieldPlan = activityValidator.getFieldPlanById(
                    requestInfo,
                    activityFacility.getFieldPlanId(),
                    activityFacility.getTenantId());
            if (fieldPlan != null) {
                projectId = fieldPlan.getProjectId();
                log.debug("Retrieved projectId: {} from field plan", projectId);
            }
        }

        if (projectId == null || projectId.isEmpty()) {
            log.warn("Project ID not found for activity facility: {}. Skipping AMC side effects.", activityFacilityId);
        }

        return projectId;
    }

    private List<Asset> fetchInstalledAssets(ActivityFacility activityFacility, RequestInfo requestInfo, String activityFacilityId) {
        try {
            log.debug("Fetching installed assets for activity facility: {}", activityFacilityId);
            AssetSearchCriteria assetSearchCriteria = AssetSearchCriteria.builder()
                    .activityFacilityID(activityFacilityId)
                    .tenantId(activityFacility.getTenantId())
                    .build();

            AssetSearchRequest assetSearchRequest = AssetSearchRequest.builder()
                    .requestInfo(requestInfo)
                    .criteria(assetSearchCriteria)
                    .build();

            StringBuilder assetSearchUri = new StringBuilder(activityConfiguration.getAssetHost())
                    .append(activityConfiguration.getAssetSearchUrl());

            List<Asset> installedAssets = serviceRequest.fetchResult(
                    assetSearchUri,
                    assetSearchRequest,
                    new TypeReference<>() {
                    });

            int assetCount = installedAssets != null ? installedAssets.size() : 0;
            if (installedAssets == null || installedAssets.isEmpty()) {
                log.info("No installed assets found for activity facility: {}. Skipping AMC side effects.", activityFacilityId);
                return Collections.emptyList();
            }
            log.debug("Found {} installed assets for activity facility: {}", assetCount, activityFacilityId);
            return installedAssets;

        } catch (Exception e) {
            log.error("Error while fetching installed assets for activity facility: {}", activityFacilityId, e);
            return Collections.emptyList();
        }
    }

    private Long resolveInstallationDate(ActivityFacility activityFacility) {
        Long installationDate = activityFacility.getAuditDetails() != null
                ? activityFacility.getAuditDetails().getLastModifiedTime()
                : null;

        if (installationDate == null) {
            installationDate = System.currentTimeMillis();
            log.debug("Using current timestamp as installation date");
        }

        return installationDate;
    }

}
