package org.egov.activity.web.controllers;


import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.annotations.ApiParam;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.egov.activity.config.ActivityConfiguration;
import org.egov.activity.service.ActivityFacilityUsersService;
import org.egov.activity.service.ActivityService;
import org.egov.activity.service.FacilityWorkflowService;
import org.egov.activity.web.models.*;
import org.egov.common.contract.response.ResponseInfo;
import org.egov.common.models.core.URLParams;
import org.egov.common.producer.Producer;
import org.egov.common.utils.ResponseInfoFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;


@Controller
@RequestMapping("/v1/activities")
@Validated
public class ActivityApiController {

    private final HttpServletRequest httpServletRequest;
    private final ActivityService activityService;
    private final ActivityFacilityUsersService facilityUsersService;
    private final FacilityWorkflowService facilityWorkflowService;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Autowired
    public ActivityApiController(HttpServletRequest httpServletRequest,
                                 ActivityService activityService, ActivityFacilityUsersService facilityUsersService, FacilityWorkflowService facilityWorkflowService, KafkaTemplate<String, Object> kafkaTemplate) {
        this.httpServletRequest = httpServletRequest;
        this.activityService = activityService;
        this.facilityUsersService = facilityUsersService;
        this.facilityWorkflowService = facilityWorkflowService;
        this.kafkaTemplate = kafkaTemplate;
    }

    @RequestMapping(value = "/_create", method = RequestMethod.POST)
    public ResponseEntity<ActivityResponse> createActivity(@ApiParam(value = "Create activity data.", required = true) @Valid @RequestBody ActivityBulkRequest request) {

        List<Activity> activities = activityService.createActivity(request);
        ActivityResponse response = ActivityResponse.builder()
                .activities(activities)
                .responseInfo(ResponseInfoFactory
                        .createResponseInfo(request.getRequestInfo(), true))
                .build();
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(response);
    }

    @RequestMapping(value = "/_update", method = RequestMethod.POST)
    public ResponseEntity<ActivityFacilityResponse> updateActivityFacility(@ApiParam(value = "Details for the updated Project.", required = true) @Valid @RequestBody ActivityFacilityBulkRequest request) {
        ActivityFacilityBulkRequest enrichedFieldPlanRequest = activityService.updateActivityFacility(request);

        ResponseInfo responseInfo = ResponseInfoFactory.createResponseInfo(request.getRequestInfo(), true);
        ActivityFacilityResponse activityFacilityResponse = ActivityFacilityResponse.builder().responseInfo(responseInfo).activityFacilities(enrichedFieldPlanRequest.getActivityFacilities()).build();
        return new ResponseEntity<ActivityFacilityResponse>(activityFacilityResponse, HttpStatus.OK);
    }

    @RequestMapping(value = "/_delete", method = RequestMethod.POST)
    public ResponseEntity<ActivityFacilityResponse> deleteActivityFacility(@ApiParam(value = "Delete activity Facility.", required = true) @Valid @RequestBody ActivityFacilityBulkRequest request) {

        List<ActivityFacility> activityFacilities = activityService.delete(request);
        ActivityFacilityResponse response = ActivityFacilityResponse.builder()
                .activityFacilities(activityFacilities)
                .responseInfo(ResponseInfoFactory
                        .createResponseInfo(request.getRequestInfo(), true))
                .build();

        return ResponseEntity.status(HttpStatus.ACCEPTED).body(response);
    }

    @RequestMapping(value = "/_search", method = RequestMethod.POST)
    public ResponseEntity<FacilityStatusResponse> searchActivityFacility(
            @ApiParam(value = "Details for the Activity Facility.", required = true) @Valid @RequestBody ActivityFacilitySearchRequest request,
            @Valid @ModelAttribute URLParams urlParams
    ) {
        List<ActivityFacility> activityFacilityList = activityService.searchActivityFacility(
                request,
                urlParams.getLimit(),
                urlParams.getOffset(),
                urlParams.getTenantId(),
                urlParams.getIncludeDeleted(),
                urlParams.getLastChangedSince()
        );
        Integer count = activityService.countAllFacilityActivities(request, urlParams.getTenantId(), urlParams.getLastChangedSince(), urlParams.getIncludeDeleted());
        // Fetch all transactions by activityFacilityIds
        List<String> activityFacilityIds = activityFacilityList.stream().map(ActivityFacility::getId).toList();
        List<Transaction> allTransactions = activityService.getTransactionsForActivityFacility(activityFacilityIds);

        // Fetch all comments by transactionIds
        List<String> txnIds = allTransactions.stream().map(Transaction::getTransactionId).toList();
        List<Comment> allComments = activityService.getCommentsForTransaction(txnIds);

        // Group transactions by activityFacilityId
        Map<String, List<Transaction>> txnsByActivityFacilityId = allTransactions.stream()
                .collect(Collectors.groupingBy(Transaction::getActivityFacilityId));

        // Group comments by transactionId
        Map<String, List<Comment>> commentsByTxnId = allComments.stream()
                .collect(Collectors.groupingBy(Comment::getTransactionId));

        ObjectMapper mapper = new ObjectMapper();
        List<FacilityStatusWrapper> projectStatusWrappers = new ArrayList<>();
        for (ActivityFacility activityFacility : activityFacilityList) {
            String status = null;
            if (activityFacility != null && activityFacility.getStatus()!=null) {
                status = activityFacility.getStatus();
            }

            List<Transaction> txns = txnsByActivityFacilityId.getOrDefault(activityFacility.getId(), Collections.emptyList());
            for (Transaction txn : txns) {
                txn.setComments(commentsByTxnId.getOrDefault(txn.getTransactionId(), Collections.emptyList()));
            }

            List<ProcessInstance> processInstances = facilityWorkflowService.getProcessInstanceById(
                    activityFacility.getId(),
                    activityFacility.getTenantId(),
                    request.getRequestInfo()
            );
            FacilityStatusWrapper wrapper = FacilityStatusWrapper.builder()
                    .activityFacility(activityFacility)
                    .status(status)
                    .transactions(txns)
                    .processInstances(processInstances)
                    .build();
            projectStatusWrappers.add(wrapper);
        }

        ResponseInfo responseInfo = ResponseInfoFactory.createResponseInfo(request.getRequestInfo(), true);
        FacilityStatusResponse projectResponse = FacilityStatusResponse.builder()
                .responseInfo(responseInfo)
                .facility(projectStatusWrappers)
                .totalCount(count)
                .build();

        return ResponseEntity.ok(projectResponse);
    }

    @RequestMapping(value = "/_assign-activity", method = RequestMethod.POST)
    public ResponseEntity<ActivityAssignmentResponse> activityAssignmentV1CreatePost(@ApiParam(value = "Capture linkage of Project and facility.", required = true) @Valid @RequestBody ActivityAssignmentBulkRequest request) {

        List<ActivityAssignment> activityAssignments = activityService.createActivityAssignment(request);
        ActivityAssignmentResponse response = ActivityAssignmentResponse.builder()
                .activityAssignment(activityAssignments)
                .responseInfo(ResponseInfoFactory
                        .createResponseInfo(request.getRequestInfo(), true))
                .build();
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(response);
    }

    @RequestMapping(value = "/assignment/_update", method = RequestMethod.POST)
    public ResponseEntity<ActivityAssignmentResponse> updateFieldPlan(@ApiParam(value = "Details for the updated Project.", required = true) @Valid @RequestBody ActivityAssignmentBulkRequest request) {
        ActivityAssignmentBulkRequest enrichedFieldPlanRequest = activityService.updateActivityAssignment(request);

        ResponseInfo responseInfo = ResponseInfoFactory.createResponseInfo(request.getRequestInfo(), true);
        ActivityAssignmentResponse activityAssignmentResponse = ActivityAssignmentResponse.builder().responseInfo(responseInfo).activityAssignment(enrichedFieldPlanRequest.getActivityAssignments()).build();
        return new ResponseEntity<ActivityAssignmentResponse>(activityAssignmentResponse, HttpStatus.OK);
    }

    @RequestMapping(value = "/assignment/_search", method = RequestMethod.POST)
    public ResponseEntity<ActivityAssignmentResponse> searchActivityAssignment(
            @ApiParam(value = "Details for the fieldPlan.", required = true) @Valid @RequestBody ActivityAssignmentSearchRequest request,
            @Valid @ModelAttribute URLParams urlParams
    ) {
        List<ActivityAssignment> activityAssignments = activityService.searchAssignedActivity(
                request,
                urlParams.getLimit(),
                urlParams.getOffset(),
                urlParams.getTenantId(),
                urlParams.getIncludeDeleted(),
                urlParams.getLastChangedSince()
        );
        ResponseInfo responseInfo = ResponseInfoFactory.createResponseInfo(request.getRequestInfo(), true);
        Integer count = activityService.countAllAssignedActivities(request, urlParams.getTenantId(), urlParams.getLastChangedSince(), urlParams.getIncludeDeleted());
        ActivityAssignmentResponse activityAssignmentResponse = ActivityAssignmentResponse.builder().responseInfo(responseInfo).activityAssignment(activityAssignments).totalCount(count).build();
        return new ResponseEntity<ActivityAssignmentResponse>(activityAssignmentResponse, HttpStatus.OK);
    }

    @RequestMapping(value = "/_unassign-activity", method = RequestMethod.POST)
    public ResponseEntity<ActivityAssignmentResponse> activityAssignmentUnassign(@ApiParam(value = "Capture linkage of Field Plan and facility.", required = true) @Valid @RequestBody ActivityAssignmentBulkRequest request) {

        List<ActivityAssignment> activityAssignments = activityService.unassignActivityAssignment(request);
        ActivityAssignmentResponse response = ActivityAssignmentResponse.builder()
                .activityAssignment(activityAssignments)
                .responseInfo(ResponseInfoFactory
                        .createResponseInfo(request.getRequestInfo(), true))
                .build();
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(response);
    }

    @RequestMapping(value = "/_assign-staff", method = RequestMethod.POST)
    public ResponseEntity<ActivityFacilityResponse> activityFacilityV1CreatePost(@ApiParam(value = "Capture linkage of Project and facility.", required = true) @Valid @RequestBody ActivityFacilityBulkRequest request) {

        List<ActivityFacility> activityFacilities = activityService.createActivityFacility(request);
        ActivityFacilityResponse response = ActivityFacilityResponse.builder()
                .activityFacilities(activityFacilities)
                .responseInfo(ResponseInfoFactory
                        .createResponseInfo(request.getRequestInfo(), true))
                .build();
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(response);
    }

    @PostMapping("/workflow/update")
    public ResponseEntity<FacilityStatusResponse> updateProjectWorkflow(
            @Valid @RequestBody FacilityWorkflowRequest request) throws Exception {

        FacilityStatusWrapper updatedActivityFacility = activityService.updateFacilityWorkflow(request);

        ResponseInfo responseInfo = ResponseInfoFactory.createResponseInfo(request.getRequestInfo(), true);
        return ResponseEntity.ok(FacilityStatusResponse.builder()
                .responseInfo(responseInfo)
                .facility(List.of(updatedActivityFacility))
                .build());
    }

    @PostMapping("/bulk/workflow/update")
    public ResponseEntity<BulkFacilityUpdateResponse> updateBulkProjectWorkflow(
            @ApiParam(value = "Bulk project workflow activity Facility request", required = true)
            @Valid @RequestBody FacilityBulkApproveRequest projectBulkApproveRequest) throws Exception {

        Map<String, Object> result = activityService.updateBulkActivityFacilityWorkflow(projectBulkApproveRequest);
        List<String> failedActivityFacilityIDs = result.get("failedActivityFacilityIDs") instanceof List<?> list ?
                list.stream().map(String::valueOf).collect(Collectors.toList()) : Collections.emptyList();
        List<String> succeededActivityFacilityIDs = result.get("succeededActivityFacilityIDs") instanceof List<?> list ?
                list.stream().map(String::valueOf).collect(Collectors.toList()) : Collections.emptyList();
        int totalActivityFacilities = result.get("totalActivityFacilities") instanceof Integer count ? count : 0;

        ResponseInfo responseInfo = ResponseInfoFactory.createResponseInfo(projectBulkApproveRequest.getRequestInfo(), true);

        BulkFacilityUpdateResponse response = BulkFacilityUpdateResponse.builder()
                .responseInfo(responseInfo)
                .failedProjectIDs(failedActivityFacilityIDs)
                .succeededProjectIDs(succeededActivityFacilityIDs)
                .build();
        if (failedActivityFacilityIDs.isEmpty()) {
            // All succeeded
            return ResponseEntity.status(HttpStatus.OK).body(response);
        } else if (failedActivityFacilityIDs.size() == totalActivityFacilities) {
            // All failed
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        } else {
            // Partial success/fail
            return ResponseEntity.status(HttpStatus.MULTI_STATUS).body(response);
        }
    }

    @RequestMapping(value = "/staff/v1/_create", method = RequestMethod.POST)
    public ResponseEntity<ActivityFacilityUserResponse> facilityUsersV1CreatePost(@ApiParam(value = "Capture linkage of Activity Facility and staff user.", required = true) @Valid @RequestBody ActivityFacilityUserBulkRequest request) throws Exception {

        List<ActivityFacilityUser> staff = facilityUsersService.createActivityFacilityUsers(request);
        ActivityFacilityUserResponse response = ActivityFacilityUserResponse.builder()
                .activityFacilityUser(staff)
                .responseInfo(ResponseInfoFactory
                        .createResponseInfo(request.getRequestInfo(), true))
                .build();
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(response);
    }

    @RequestMapping(value = "/staff/v1/_update", method = RequestMethod.POST)
    public ResponseEntity<ActivityFacilityUserResponse> facilityUsersV1UpdatePost(@ApiParam(value = "Capture linkage of Project and staff user.", required = true) @Valid @RequestBody ActivityFacilityUserBulkRequest request) {

        List<ActivityFacilityUser> activityFacilityUsers = facilityUsersService.update(request);
        ActivityFacilityUserResponse response = ActivityFacilityUserResponse.builder()
                .activityFacilityUser(activityFacilityUsers)
                .responseInfo(ResponseInfoFactory
                        .createResponseInfo(request.getRequestInfo(), true))
                .build();

        return ResponseEntity.status(HttpStatus.ACCEPTED).body(response);
    }

    @RequestMapping(value = "/staff/v1/_delete", method = RequestMethod.POST)
    public ResponseEntity<ActivityFacilityUserResponse> facilityUsersV1DeletePost(@ApiParam(value = "Capture linkage of Project and staff user.", required = true) @Valid @RequestBody ActivityFacilityUserBulkRequest request) {

        List<ActivityFacilityUser> activityFacilityUsers = facilityUsersService.delete(request);
        ActivityFacilityUserResponse response = ActivityFacilityUserResponse.builder()
                .activityFacilityUser(activityFacilityUsers)
                .responseInfo(ResponseInfoFactory
                        .createResponseInfo(request.getRequestInfo(), true))
                .build();

        return ResponseEntity.status(HttpStatus.ACCEPTED).body(response);
    }

    @PostMapping("/test_update_activity")
    public String sendDummyTopicActivityFacility(@Valid @RequestBody ActivityFacilityBulkRequest incidentRequest) {
        Map<String, Object> producerRecord = new HashMap<>();
        producerRecord.put("topic", "save-activity-facility-topic");
        producerRecord.put("value", incidentRequest);
        kafkaTemplate.send("process-audit-records", producerRecord);
        return "Object sent!";
    }
}
