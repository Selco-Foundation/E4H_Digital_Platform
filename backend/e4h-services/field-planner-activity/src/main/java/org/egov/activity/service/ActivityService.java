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
import org.egov.common.contract.request.Role;
import org.egov.common.producer.Producer;
import org.egov.activity.config.ActivityConfiguration;
import org.egov.activity.repository.ActivityFacilityRepository;
import org.egov.activity.service.enrichment.ActivityEnrichment;
import org.egov.activity.validator.ActivityValidator;
import org.egov.activity.web.models.*;
import org.egov.common.models.core.SearchResponse;
import org.egov.tracer.model.CustomException;
import org.egov.tracer.model.ServiceCallException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.sql.Array;
import java.util.*;
import java.util.stream.Collectors;

import static org.egov.activity.util.ActivityConstants.*;
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
        List<ActivityFacilityUser> activityFacilityUsers = new ArrayList<>();

        try {
            for (ActivityFacility activityFacility : activityFacilities) {
                log.info("processing {} valid entities", activityFacility);
                activityEnrichment.enrichActivityFacilityRequestOnCreate(activityFacility, request.getRequestInfo());
                List<ActivityFacilityUser> usersFacility = new ArrayList<>();
                // Get reviewer users. Can see facility activity on UI directly by getting field plan first
                if(activityFacility.getReviewerUser() != null && !activityFacility.getReviewerUser().isEmpty()){
                    for (String userId : activityFacility.getReviewerUser()){
                        ActivityFacilityUser facilityUser = ActivityFacilityUser.builder()
                                .activityFacilityId(activityFacility.getId())
                                .userId(userId)
                                .tenantId(activityFacility.getTenantId())
                                .isDeleted(false)
                                .additionalDetails(Map.of(ACTIVITY_FACILITY_USER_LINKAGE_TYPE_KEY, LINKAGE_TYPE_INSTALLATION_REVIEWER))
                                .build();
                        usersFacility.add(facilityUser);
                    }
                }

                // Get staff users. Can see facility once activity facility status = ASSIGNED_TO_FIELD_STAFF
                if(activityFacility.getFieldStaffUsers() != null && !activityFacility.getFieldStaffUsers().isEmpty()){
                    for (String userId : activityFacility.getFieldStaffUsers()){
                        ActivityFacilityUser facilityUser = ActivityFacilityUser.builder()
                                .activityFacilityId(activityFacility.getId())
                                .userId(userId)
                                .tenantId(activityFacility.getTenantId())
                                .isDeleted(false)
                                .additionalDetails(Map.of(ACTIVITY_FACILITY_USER_LINKAGE_TYPE_KEY, LINKAGE_TYPE_FIELD_STAFF))
                                .build();
                        usersFacility.add(facilityUser);
                    }
                }

                // Get supervisor users. Can see facility once activity facility status = ASSIGNED_TO_FIELD_STAFF
                if(activityFacility.getFieldSupervisorUsers() != null && !activityFacility.getFieldSupervisorUsers().isEmpty()){
                    for (String userId : activityFacility.getFieldSupervisorUsers()){
                        ActivityFacilityUser facilityUser = ActivityFacilityUser.builder()
                                .activityFacilityId(activityFacility.getId())
                                .userId(userId)
                                .tenantId(activityFacility.getTenantId())
                                .isDeleted(false)
                                .additionalDetails(Map.of(ACTIVITY_FACILITY_USER_LINKAGE_TYPE_KEY, LINKAGE_TYPE_FIELD_SUPERVISOR))
                                .build();
                        usersFacility.add(facilityUser);
                    }
                }
                // remove Duplicate activity facility users if the same user is REVIEWER, STAFF and SUPERVISOR
                Set<String> seenUsers = new HashSet<>();
                usersFacility = usersFacility.stream().filter(a -> seenUsers.add(a.getUserId()))
                        .toList();
                activityFacilityUsers.addAll(usersFacility);
            }


            // Create linked users, so that reviewer, staff and supervisor are linked to each activity facility. Reviewer can see list of activities on UI.
            if(activityFacilityUsers != null && !activityFacilityUsers.isEmpty()){
                ActivityFacilityUserBulkRequest activityFacilityUserBulkRequest = ActivityFacilityUserBulkRequest.builder()
                        .requestInfo(request.getRequestInfo())
                        .activityFacilityUsers(activityFacilityUsers)
                        .build();
                facilityUsersService.createActivityFacilityUsers(activityFacilityUserBulkRequest);
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
            log.info("successfully created Activity Assignment");
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

    public List<ActivityFacility> searchActivityFacility(ActivityFacilitySearchRequest request, Integer limit, Integer offset, String tenantId, Boolean includeDeleted, Long lastChangedSince) {
        activityValidator.validateSearchActivityRequest(request, limit, offset, tenantId);
        List<ActivityFacility> activityFacilities = activityFacilityRepository.getActivitiesFacility(request, limit, offset, tenantId, includeDeleted, lastChangedSince);
        Map<String, Boundary> listBlock = boundaryUtil.getBoundaryByCode();
        log.debug("🌍 Loaded {} boundaries for enrichment", listBlock.size());
        for (ActivityFacility activityFacility : activityFacilities) {
            log.info("processing get activity code", activityFacility);
            activityEnrichment.enrichActivityFacilityOnSearch(request, activityFacility);

            if(activityFacility.getFacility() == null)
                continue;

            Object additionalDetails = activityFacility.getFacility().getAdditionalDetails();
            String boundaryCode = activityFacility.getFacility().getBoundaryCode();
            log.trace("🔎 Processing projectId={} with boundaryCode={}", activityFacility.getFacility().getId(), boundaryCode);

            if (boundaryCode != null) {
                Boundary boundary = listBlock.get(boundaryCode);
                if (boundary != null) {
                    log.debug("✨ Enriching projectId={} with state={} and district={}", activityFacility.getId(), boundary.getState(), boundary.getDistrict());

                    Object enrichedAdditionalDetails = mergeListIntoAdditionalDetails(additionalDetails, "state", boundary.getState());
                    activityFacility.getFacility().setAdditionalDetails((Map<String, Object>) enrichedAdditionalDetails);

                    additionalDetails = activityFacility.getFacility().getAdditionalDetails();
                    enrichedAdditionalDetails = mergeListIntoAdditionalDetails(additionalDetails, "district", boundary.getDistrict());
                    activityFacility.getFacility().setAdditionalDetails((Map<String, Object>) enrichedAdditionalDetails);
                } else {
                    log.warn("⚠️ No boundary found for code={} in projectId={}", boundaryCode, activityFacility.getId());
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
        log.info("received request to delete bulk activity facility staff");
        activityValidator.validateActivityFacilityDeleteRequest(request);
        List<ActivityFacility> validEntities = request.getActivityFacilities();
        try {
            if (!validEntities.isEmpty()) {
                for (ActivityFacility activityFacility : validEntities) {
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
                        log.error("Activity Facility ID do not exist");
                        throw new CustomException("Activity Facility Delete", "Activity Facility ID do not exist");
                    }
                    activityFacility.setIsDeleted(true);
                    activityEnrichment.enrichActivityFacilityRequestOnUpdate(activityFacility, activityFacilities.get(0), request.getRequestInfo());
                    producer.push(activityConfiguration.getDeleteActivityFacilityTopic(), request);
                    log.info("successfully updated bulk project staff");
                }
            }
        } catch (Exception exception) {
            log.error("error occurred while updating project staff", ExceptionUtils.getStackTrace(exception));
        }

        return validEntities;
    }

    public List<ActivityAssignment> searchAssignedActivity(ActivityAssignmentSearchRequest request, Integer limit, Integer offset, String tenantId, Boolean includeDeleted, Long lastChangedSince) {
        activityValidator.validateSearchAssignActivityRequest(request, limit, offset, tenantId);
        List<ActivityAssignment> activityFacilities = activityAssignmentRepository.getActivitiesAssignment(request, limit, offset, tenantId, includeDeleted, lastChangedSince);
        for (ActivityAssignment activityAssignment : activityFacilities) {
            log.info("processing get activity code", activityAssignment);
            activityEnrichment.enrichActivityAssignmentOnSearch(request.getRequestInfo(), activityAssignment);
            List<FacilityStatusAgregation> statusAgregations = getStatusFacilityAssignmentsAgregation(activityAssignment.getFieldPlanId());
            if (statusAgregations != null) {
                Object enrichedAdditionalDetails = mergeListIntoAdditionalDetails(activityAssignment.getAdditionalDetails(), "statusAgregation", statusAgregations);
                activityAssignment.setAdditionalDetails((Map<String, Object>) enrichedAdditionalDetails);
            }
        }
        return activityFacilities;
    }

    public FacilityStatusWrapper updateFacilityWorkflow(FacilityWorkflowRequest request) throws Exception {
        // 1. Fetch the existing facility
        ActivityFacilitySearchCriteria searchCriteria = ActivityFacilitySearchCriteria.builder()
                .ids(List.of(request.getActivityFacilityId()))
                .tenantId(activityConfiguration.getTenantId())
                .build();

        ActivityFacilitySearchRequest searchRequest = ActivityFacilitySearchRequest.builder()
                .criteria(searchCriteria)
                .requestInfo(request.getRequestInfo())
                .build();

        List<ActivityFacility> activityFacilities = searchActivityFacility(searchRequest, activityConfiguration.getMaxLimit(), activityConfiguration.getDefaultOffset(),
                activityConfiguration.getTenantId(), false, null);

        if (activityFacilities == null || activityFacilities.isEmpty()) {
            throw new CustomException("FACILITY_NOT_FOUND", "Activity Facility not found with ID: " + request.getActivityFacilityId());
        }

        ActivityFacility existingActivityFacitlity = activityFacilities.get(0);

        // 2. Call workflow transition
        ProcessInstance updatedWorkflow;
        try {
            updatedWorkflow = workflowService.transitionWorkflow(
                    existingActivityFacitlity,
                    request.getWorkflow().getAction(),
                    request.getWorkflow().getDocuments(),
                    request.getRequestInfo(),
                    request.getWorkflow().getComments()
            );
        } catch (Exception e) {
            e.printStackTrace();
            log.error(e.getMessage());
            throw new CustomException("WORKFLOW_TRANSITION_FAILED",
                    "Failed to transition workflow for facility: " + request.getActivityFacilityId());
        }

        if(request.getTransactions() != null && !request.getTransactions().isEmpty()) {
            handleTransactionsAndComment(request, updatedWorkflow);
        }

        // 3. Inject workflow status into activity facility
        existingActivityFacitlity.setStatus(updatedWorkflow.getState().getState());

        // 4. Create a new Activity Instance instance with enriched additionalDetails
        ActivityFacility updatedActivityFacility = ActivityFacility.builder()
                .id(existingActivityFacitlity.getId())
                .tenantId(existingActivityFacitlity.getTenantId())
                .activityId(existingActivityFacitlity.getActivityId())
                .facilityId(existingActivityFacitlity.getFacilityId())
                .fieldPlanId(existingActivityFacitlity.getFieldPlanId())
                .status(existingActivityFacitlity.getStatus())
                .assignedUser(existingActivityFacitlity.getAssignedUser())
                .activatedAt(existingActivityFacitlity.getActivatedAt())
                .completedAt(System.currentTimeMillis())
                .scheduledAt(existingActivityFacitlity.getScheduledAt())
                .additionalDetails(existingActivityFacitlity.getAdditionalDetails())
                .build();

        // 5. Create project request wrapper
        ActivityFacilityBulkRequest enrichedRequest = ActivityFacilityBulkRequest.builder()
                .requestInfo(request.getRequestInfo())
                .activityFacilities(List.of(updatedActivityFacility))
                .build();

        // 6. Perform enriched update using standard handler
        handleUpdateActivityFacility(enrichedRequest, updatedActivityFacility, existingActivityFacitlity);

        // Step 7: After successful workflow transition, if action is APPROVED_BY_QC_SPOC
        if ("APPROVE".equalsIgnoreCase(request.getWorkflow().getAction())) {
            // once facility is fetched we need to fetch assets for that facility
            String activityFacilityId = existingActivityFacitlity.getId();
            if (activityFacilityId != null) {
                updateAssetsForFacility(existingActivityFacitlity, request.getRequestInfo(), activityFacilityId);
                // Step 8: Trigger installation completion side effects (Asset AMC creation and visit generation)
                triggerInstallationCompletionSideEffects(existingActivityFacitlity, request.getRequestInfo(), activityFacilityId);
                // Step 9: Mark facility as ONM ready
                markFacilityOnmReady(existingActivityFacitlity, request.getRequestInfo());
                // Step 10: Add facility to jurisdiction of COMPLAINT_RESOLVER in the same organisation
                addFacilityToComplaintResolverJurisdiction(existingActivityFacitlity, request.getRequestInfo());
            }
        }

        return new FacilityStatusWrapper(updatedActivityFacility, updatedWorkflow.getState().getState(), null, null);
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
        try {
            String facilityId = activityFacility.getFacilityId();
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

            // Ensure roles list exists
            if (requestInfo.getUserInfo().getRoles() == null) {
                requestInfo.getUserInfo().setRoles(new ArrayList<>());
            }

            // Optionally guard against duplicates if needed
            boolean hasSystemUser = requestInfo.getUserInfo().getRoles().stream()
                    .anyMatch(r -> "SYSTEM_USER".equals(r.getCode()));
            if (!hasSystemUser) {
                requestInfo.getUserInfo().getRoles().add(
                        Role.builder()
                                .name("System User")
                                .code("SYSTEM_USER")
                                .tenantId("in")
                                .build()
                );
            }

            Map<String, Object> updateRequest = new HashMap<>();
            updateRequest.put("RequestInfo", requestInfo);
            updateRequest.put("FacilityUpdate", facility);

            String url = activityConfiguration.getFacilityServiceHost()
                    + activityConfiguration.getFacilityServiceUpdateUrl();

            log.info("Marking facility {} as ONM ready via {}", facilityId, url);
            serviceRequest.fetchResult(new StringBuilder(url), updateRequest);
        } catch (Exception e) {
            log.error("Failed to mark facility ONM ready for activityFacility {}: {}",
                    activityFacility.getId(), e.getMessage(), e);
        }
    }

    private void updateAssetsForFacility(ActivityFacility activityFacility, RequestInfo requestInfo, String facilityId) throws CustomException {
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
            if (assets != null && !assets.isEmpty()) {
                for (Asset asset : assets) {
                    updateAssetOperationalStatus(asset, requestInfo);
                }
            }
        } catch (ServiceCallException e) {
            log.error("Service call failed while processing assets for project {}: {}", activityFacility.getId(), e.getMessage());
            throw new CustomException("ASSET_UPDATE_FAILED", "Failed to update asset operational status");
        } catch (Exception e) {
            log.error("Unexpected error while processing assets for project {}: {}", activityFacility.getId(), e.getMessage(), e);
            throw new CustomException("ASSET_PROCESSING_ERROR", "An error occurred while processing assets");
        }
    }

    private void updateAssetOperationalStatus(Asset asset, RequestInfo requestInfo) {
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

            serviceRequest.fetchResult(assetUpdateUri, createRequest);
        } catch (Exception e) {
            log.error("Failed to update asset {}: {}", asset.getAssetId(), e.getMessage());
        }
    }

    public Map<String, Object> updateBulkActivityFacilityWorkflow(FacilityBulkApproveRequest facilityBulkApproveRequest) throws Exception {

        List<String> activityFacilityIds = new ArrayList<>();
        int totalActivityFacilities = 0;
        int finalActivityFacilities = 0;

        if (facilityBulkApproveRequest.getIsAllSelected()) {
            // Case 1: Search all activityFacilitiesList using filters
            if(facilityBulkApproveRequest.getFilters() == null){
                throw new CustomException("INVALID_REQUEST", "Filters are required when isAllSelected is true");
            }

            ActivityFacilitySearchCriteria searchCriteria = facilityBulkApproveRequest.getFilters().getSearchCriteria();
            searchCriteria.setTenantId(activityConfiguration.getTenantId());

            ActivityFacilitySearchRequest searchRequest = ActivityFacilitySearchRequest.builder()
                    .criteria(searchCriteria)
                    .requestInfo(facilityBulkApproveRequest.getRequestInfo())
                    .build();

            List<ActivityFacility> activityFacilities = searchActivityFacility(searchRequest, activityConfiguration.getMaxLimit(), activityConfiguration.getDefaultOffset(),
                    activityConfiguration.getTenantId(), false, null);
            totalActivityFacilities = countAllFacilityActivities(searchRequest, activityConfiguration.getTenantId(), null, null);

            // only those activity facilities whose status is SUBMITTED_BY_SUPERVISOR
            List<ActivityFacility> activityFacilitiesList = activityFacilities.stream().filter(this::hasSubmittedBySupervisorStatus).toList();

            finalActivityFacilities = activityFacilitiesList.size();
            activityFacilityIds = activityFacilitiesList.stream().map(ActivityFacility::getId).collect(Collectors.toList());
        } else {
            // Case 2: Use provided activity facility IDs
            if (facilityBulkApproveRequest.getActivityFacilityIds() != null && !facilityBulkApproveRequest.getActivityFacilityIds().isEmpty()) {
                activityFacilityIds = facilityBulkApproveRequest.getActivityFacilityIds();
                totalActivityFacilities = activityFacilityIds.size();
            } else {
                throw new CustomException("INVALID_REQUEST", "activity facility IDs are required when isAllSelected is false");
            }
        }
        Map<String, Object> result = new HashMap<>();
        // Validate that we have projects to process
        if (activityFacilityIds.isEmpty()) {
            result.put("failedActivityFacilitiesIDs", new ArrayList<>());
            result.put("succeededActivityFacilitiesIDs", new ArrayList<>());
            result.put("totalActivityFacilties", 0);
            return result;
        }

        // Update workflow for all project IDs
        log.info("Starting bulk workflow update for {} activity facility", activityFacilityIds.size());
        List<String> failedActivityFacilityIDs = new ArrayList<>();
        List<String> succeededActivityFacilityIDs = new ArrayList<>();
        for (String activityFacilityId : activityFacilityIds) {
            try {
                FacilityWorkflowRequest workflowRequest = FacilityWorkflowRequest.builder()
                        .requestInfo(facilityBulkApproveRequest.getRequestInfo())
                        .activityFacilityId(activityFacilityId)
                        .workflow(facilityBulkApproveRequest.getWorkflow())
                        .build();

                FacilityStatusWrapper updatedProject = updateFacilityWorkflow(workflowRequest);
                log.info("Successfully updated workflow for activity facility: {}", activityFacilityId);
                succeededActivityFacilityIDs.add(activityFacilityId);
            } catch (Exception e) {
                log.error("Failed to update workflow for activity facility {}: {}", activityFacilityId, e.getMessage());
                failedActivityFacilityIDs.add(activityFacilityId);
            }
        }

        result.put("failedActivityFacilityIDs", failedActivityFacilityIDs);
        result.put("succeededActivityFacilityIDs", succeededActivityFacilityIDs);
        if(facilityBulkApproveRequest.getIsAllSelected() && finalActivityFacilities > 0) {
            result.put("totalActivityFacilities", finalActivityFacilities);
        } else {
            result.put("totalActivityFacilities", totalActivityFacilities);
        }
        return result;
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
        /*
         * Validate the update activity request
         */
        activityValidator.validateCreateActivityFacilityRequest(request);
        log.info("Update activity facility request validated");

        /*
         * Search for fieldplan based on fieldplan IDs provided in the request
         */
        List<ActivityFacility> activityFacilityListFromDB = searchActivityFacility(
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
        activityValidator.validateUpdateActivityAssignment(request);
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
     * After installation approval, find the COMPLAINT_RESOLVER user within the organisation
     * for the field staff / field supervisor who performed the installation and add this
     * facility to their jurisdiction (first COMPLAINT_RESOLVER only).
     */
    private void addFacilityToComplaintResolverJurisdiction(ActivityFacility activityFacility, RequestInfo requestInfo) {
        try {
            if (activityFacility == null || activityFacility.getTenantId() == null) {
                log.warn("Cannot update complaint resolver jurisdiction: activityFacility or tenantId is null");
                return;
            }

            // Field staff / supervisor lists are not loaded from DB on search; resolve from activity_facility_users + assignedUser
            String referenceUserId = resolveReferenceUserIdForComplaintResolver(activityFacility, requestInfo);
            if (referenceUserId == null) {
                log.info("No reference user (field staff/supervisor from activity_facility_users or assigned) found for activityFacility {}, skipping complaint resolver jurisdiction update", activityFacility.getId());
                return;
            }

            // Fetch organisation of the reference user from vendor-registry via org-user search
            String organisationId = fetchOrganisationIdForUser(referenceUserId, activityFacility, requestInfo);
            if (organisationId == null) {
                log.info("No organisation found for user {} and activityFacility {}, skipping complaint resolver jurisdiction update", referenceUserId, activityFacility.getId());
                return;
            }

            // From that organisation, find the first COMPLAINT_RESOLVER user
            Map<String, Object> complaintResolverOrgUser = fetchFirstComplaintResolverForOrganisation(organisationId, activityFacility, requestInfo);
            if (complaintResolverOrgUser == null) {
                log.info("No COMPLAINT_RESOLVER found in organisation {} for activityFacility {}, skipping jurisdiction update", organisationId, activityFacility.getId());
                return;
            }

            // Add facility to resolver's jurisdiction and call organisation user update
            updateComplaintResolverJurisdictionsWithFacility(complaintResolverOrgUser, activityFacility, requestInfo);

        } catch (Exception e) {
            log.error("Error while updating complaint resolver jurisdiction for activityFacility {}", activityFacility != null ? activityFacility.getId() : "null", e);
        }
    }

    /**
     * Prefer first field staff, then first field supervisor. Sources: request payload lists (if present),
     * persisted {@code activity_facility_users} rows with linkageType (FIELD_STAFF / FIELD_SUPERVISOR),
     * then {@link ActivityFacility#getAssignedUser()}.
     */
    private String resolveReferenceUserIdForComplaintResolver(ActivityFacility activityFacility, RequestInfo requestInfo) {
        if (activityFacility.getFieldStaffUsers() != null && !activityFacility.getFieldStaffUsers().isEmpty()) {
            return activityFacility.getFieldStaffUsers().get(0);
        }
        if (activityFacility.getFieldSupervisorUsers() != null && !activityFacility.getFieldSupervisorUsers().isEmpty()) {
            return activityFacility.getFieldSupervisorUsers().get(0);
        }
        List<ActivityFacilityUser> linked = fetchActivityFacilityUsers(activityFacility.getId(), activityFacility.getTenantId(), requestInfo);
        String fromLinked = pickReferenceUserIdFromLinkedActivityFacilityUsers(linked);
        if (fromLinked != null) {
            return fromLinked;
        }
        if (activityFacility.getAssignedUser() != null && !activityFacility.getAssignedUser().isBlank()) {
            return activityFacility.getAssignedUser();
        }
        return null;
    }

    private List<ActivityFacilityUser> fetchActivityFacilityUsers(String activityFacilityId, String tenantId, RequestInfo requestInfo) {
        try {
            ActivityFacilityUserSearchCriteria criteria = ActivityFacilityUserSearchCriteria.builder()
                    .activityFacilityId(List.of(activityFacilityId))
                    .tenantId(tenantId)
                    .build();
            ActivityFacilityUserSearchRequest searchRequest = ActivityFacilityUserSearchRequest.builder()
                    .criteria(criteria)
                    .requestInfo(requestInfo)
                    .build();
            SearchResponse<ActivityFacilityUser> response = facilityUsersService.search(
                    searchRequest,
                    activityConfiguration.getMaxLimit(),
                    0,
                    tenantId,
                    null,
                    false);
            if (response == null || response.getResponse() == null) {
                return Collections.emptyList();
            }
            return response.getResponse();
        } catch (Exception e) {
            log.error("Error while fetching activity facility users for activityFacility {}", activityFacilityId, e);
            return Collections.emptyList();
        }
    }

    private String pickReferenceUserIdFromLinkedActivityFacilityUsers(List<ActivityFacilityUser> linked) {
        if (linked == null || linked.isEmpty()) {
            return null;
        }
        for (ActivityFacilityUser u : linked) {
            if (Boolean.TRUE.equals(u.getIsDeleted())) {
                continue;
            }
            if (LINKAGE_TYPE_FIELD_STAFF.equalsIgnoreCase(String.valueOf(getLinkageTypeFromActivityFacilityUser(u)))) {
                return u.getUserId();
            }
        }
        for (ActivityFacilityUser u : linked) {
            if (Boolean.TRUE.equals(u.getIsDeleted())) {
                continue;
            }
            if (LINKAGE_TYPE_FIELD_SUPERVISOR.equalsIgnoreCase(String.valueOf(getLinkageTypeFromActivityFacilityUser(u)))) {
                return u.getUserId();
            }
        }
        return null;
    }

    private String getLinkageTypeFromActivityFacilityUser(ActivityFacilityUser u) {
        if (u.getAdditionalDetails() == null) {
            return null;
        }
        Object v = u.getAdditionalDetails().get(ACTIVITY_FACILITY_USER_LINKAGE_TYPE_KEY);
        return v != null ? v.toString() : null;
    }

    private String fetchOrganisationIdForUser(String userId, ActivityFacility activityFacility, RequestInfo requestInfo) {
        try {
            OrgUserSearchCriteria criteria = OrgUserSearchCriteria.builder()
                    .userId(List.of(userId))
                    .tenantId(activityFacility.getTenantId())
                    .build();

            OrgUserSearchRequest searchRequest = OrgUserSearchRequest.builder()
                    .requestInfo(requestInfo)
                    .criteria(criteria)
                    .build();

            List<?> orgUsers = searchOrgUsers(searchRequest, activityFacility.getTenantId(), 0, 10);
            if (orgUsers.isEmpty()) {
                return null;
            }

            Object first = orgUsers.get(0);
            if (!(first instanceof Map)) {
                return null;
            }

            Map<String, Object> firstOrgUser = (Map<String, Object>) first;
            Object orgIdObj = firstOrgUser.get("organizationId");
            return orgIdObj != null ? orgIdObj.toString() : null;
        } catch (Exception e) {
            log.error("Error while fetching organisation for user {} and activityFacility {}", userId, activityFacility.getId(), e);
            return null;
        }
    }

    private Map<String, Object> fetchFirstComplaintResolverForOrganisation(String organisationId, ActivityFacility activityFacility, RequestInfo requestInfo) {
        try {
            OrgUserSearchCriteria criteria = OrgUserSearchCriteria.builder()
                    .organizationId(List.of(organisationId))
                    .tenantId(activityFacility.getTenantId())
                    .build();

            OrgUserSearchRequest searchRequest = OrgUserSearchRequest.builder()
                    .requestInfo(requestInfo)
                    .criteria(criteria)
                    .build();

            int offset = 0;

            while (true) {
                List<?> orgUsers = searchOrgUsers(searchRequest, activityFacility.getTenantId(), offset, 100);
                if (orgUsers.isEmpty()) {
                    return null;
                }

                for (Object obj : orgUsers) {
                    if (obj instanceof Map && isComplaintResolverOrgUser((Map<String, Object>) obj)) {
                        return (Map<String, Object>) obj;
                    }
                }

                if (orgUsers.size() < 100) {
                    // No more pages
                    return null;
                }

                offset += 100;
            }
        } catch (Exception e) {
            log.error("Error while fetching COMPLAINT_RESOLVER for organisation {} and activityFacility {}", organisationId, activityFacility.getId(), e);
            return null;
        }
    }

    private boolean isComplaintResolverOrgUser(Map<String, Object> orgUser) {
        Object userObj = orgUser.get("user");
        if (!(userObj instanceof Map)) {
            return false;
        }

        Map<String, Object> user = (Map<String, Object>) userObj;
        Object rolesObj = user.get("roles");
        if (!(rolesObj instanceof List)) {
            return false;
        }

        List<?> roles = (List<?>) rolesObj;
        return roles.stream().anyMatch(roleObj -> {
            if (!(roleObj instanceof Map)) {
                return false;
            }
            Object codeObj = ((Map<String, Object>) roleObj).get("code");
            return codeObj != null && "COMPLAINT_RESOLVER".equalsIgnoreCase(codeObj.toString());
        });
    }

    /**
     * Common helper to call org-user search and return the OrgUsers list (or empty list).
     */
    private List<?> searchOrgUsers(OrgUserSearchRequest searchRequest, String tenantId, int offset, int limit) {
        StringBuilder url = new StringBuilder(activityConfiguration.getOrgUserHost())
                .append(activityConfiguration.getOrgUserSearchUrl())
                .append("?tenantId=").append(tenantId)
                .append("&offset=").append(offset)
                .append("&limit=").append(limit);

        Map<String, Object> response = serviceRequest.fetchResult(
                url,
                searchRequest,
                new TypeReference<Map<String, Object>>() {
                });

        if (response == null) {
            return Collections.emptyList();
        }

        Object orgUsersObj = response.get("OrgUsers");
        if (!(orgUsersObj instanceof List)) {
            return Collections.emptyList();
        }

        return (List<?>) orgUsersObj;
    }

    private void updateComplaintResolverJurisdictionsWithFacility(Map<String, Object> orgUser,
                                                                  ActivityFacility activityFacility,
                                                                  RequestInfo requestInfo) {
        if (orgUser == null) {
            return;
        }

        try {
            Object userObj = orgUser.get("user");
            if (!(userObj instanceof Map)) {
                log.warn("OrgUser user field is not a map, skipping jurisdiction update");
                return;
            }

            Map<String, Object> user = (Map<String, Object>) userObj;

            // Jurisdiction list is under key "jurisdiction" as per vendor-registry User model
            Object jurisdictionsObj = user.get("jurisdiction");
            List<Map<String, Object>> jurisdictions;
            if (jurisdictionsObj instanceof List) {
                jurisdictions = (List<Map<String, Object>>) jurisdictionsObj;
            } else {
                jurisdictions = new ArrayList<>();
            }

            // Use facility boundary code as jurisdiction boundary
            String boundaryCode = activityFacility.getFacility() != null
                    ? activityFacility.getFacility().getBoundaryCode()
                    : null;

            if (boundaryCode == null) {
                log.warn("Boundary code is null for activityFacility {}, skipping jurisdiction update", activityFacility.getId());
                return;
            }

            boolean alreadyPresent = jurisdictions.stream().anyMatch(j -> {
                Object boundary = j.get("boundary");
                return boundary != null && boundaryCode.equals(boundary.toString());
            });

            if (alreadyPresent) {
                log.info("Boundary {} already present in complaint resolver jurisdiction, skipping update", boundaryCode);
                return;
            }

            Map<String, Object> newJurisdiction = new HashMap<>();
            newJurisdiction.put("hierarchy", "ADMIN");
            newJurisdiction.put("boundary", boundaryCode);
            newJurisdiction.put("boundaryType", "Facility");
            newJurisdiction.put("tenantId", activityFacility.getTenantId());
            newJurisdiction.put("isActive", Boolean.TRUE);

            jurisdictions.add(newJurisdiction);
            user.put("jurisdiction", jurisdictions);

            // Build update request payload for organisation user update API
            Map<String, Object> updateBody = new HashMap<>();
            updateBody.put("RequestInfo", requestInfo);
            updateBody.put("OrgUser", orgUser);

            StringBuilder url = new StringBuilder(activityConfiguration.getOrgUserHost())
                    .append(activityConfiguration.getOrgUserUpdateUrl());

            serviceRequest.fetchResult(url, updateBody);
            log.info("Successfully updated complaint resolver jurisdiction for boundary {}", boundaryCode);
        } catch (Exception e) {
            log.error("Error while updating complaint resolver jurisdiction for facility {}", activityFacility.getFacilityId(), e);
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

            // Get project ID from field plan
            String projectId = null;
            if (activityFacility.getFieldPlanId() != null) {
                FieldPlan fieldPlan = activityValidator.getFieldPlanById(
                        requestInfo,
                        activityFacility.getFieldPlanId(),
                        activityFacility.getTenantId());
                if (fieldPlan != null) {
                    projectId = fieldPlan.getProjectId();
                }
            }

            if (projectId == null || projectId.isEmpty()) {
                log.warn("Project ID not found for activity facility: {}. Skipping AMC side effects.", activityFacilityId);
                return;
            }

            // Fetch installed assets for this facility
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

            if (installedAssets == null || installedAssets.isEmpty()) {
                log.info("No installed assets found for activity facility: {}. Skipping AMC side effects.", activityFacilityId);
                return;
            }

            // Get installation date
            Long installationDate = activityFacility.getAuditDetails().getLastModifiedTime();
            if (installationDate == null) {
                installationDate = System.currentTimeMillis();
            }

            // Call AMC scheduler service to process installation completion
            amcSchedulerService.processInstallationCompletion(
                    projectId,
                    activityFacility.getFacilityId(),
                    activityFacility.getTenantId(),
                    installedAssets,
                    installationDate,
                    requestInfo
            );

            log.info("Successfully triggered installation completion side effects for activity facility: {}", activityFacilityId);

        } catch (Exception e) {
            log.error("Error triggering installation completion side effects for activity facility {}: {}",
                    activityFacilityId, e.getMessage(), e);
        }
    }

}
