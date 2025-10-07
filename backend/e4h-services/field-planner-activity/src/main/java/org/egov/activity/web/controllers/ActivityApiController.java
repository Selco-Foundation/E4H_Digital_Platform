package org.egov.activity.web.controllers;


import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.annotations.ApiParam;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.egov.common.contract.response.ResponseInfo;
import org.egov.common.models.core.URLParams;
import org.egov.common.producer.Producer;
import org.egov.common.utils.ResponseInfoFactory;
import org.egov.activity.config.ActivityConfiguration;
import org.egov.activity.service.*;
import org.egov.activity.web.models.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.*;


@Controller
@RequestMapping("/v1/activities")
@Validated
public class ActivityApiController {

    private final HttpServletRequest httpServletRequest;
    private final ActivityService activityService;

    @Autowired
    public ActivityApiController(ObjectMapper objectMapper, HttpServletRequest httpServletRequest,
                                 Producer producer,
                                 ActivityConfiguration fieldPlannerConfiguration,
                                 ActivityService activityService) {
        this.httpServletRequest = httpServletRequest;
        this.activityService = activityService;
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
    public ResponseEntity<ActivityFacilityResponse> updateActivityAssignment(@ApiParam(value = "Details for the updated Project.", required = true) @Valid @RequestBody ActivityFacilityBulkRequest request) {
        ActivityFacilityBulkRequest enrichedFieldPlanRequest = activityService.updateActivityFacitlity(request);

        ResponseInfo responseInfo = ResponseInfoFactory.createResponseInfo(request.getRequestInfo(), true);
        ActivityFacilityResponse activityFacilityResponse = ActivityFacilityResponse.builder().responseInfo(responseInfo).activityFacilities(enrichedFieldPlanRequest.getActivityFacilities()).build();
        return new ResponseEntity<ActivityFacilityResponse>(activityFacilityResponse, HttpStatus.OK);
    }

    @RequestMapping(value = "/_search", method = RequestMethod.POST)
    public ResponseEntity<ActivityFacilityResponse> searchActivityFacility(
            @ApiParam(value = "Details for the fieldPlan.", required = true) @Valid @RequestBody ActivityFacilitySearchRequest request,
            @Valid @ModelAttribute URLParams urlParams
    ) {
        List<ActivityFacility> fieldPlans = activityService.searchActivity(
                request,
                urlParams.getLimit(),
                urlParams.getOffset(),
                urlParams.getTenantId(),
                urlParams.getIncludeDeleted(),
                urlParams.getLastChangedSince()
        );
        ResponseInfo responseInfo = ResponseInfoFactory.createResponseInfo(request.getRequestInfo(), true);
        Integer count = activityService.countAllFacilityActivities(request, urlParams.getTenantId(), urlParams.getLastChangedSince(), urlParams.getIncludeDeleted());
        ActivityFacilityResponse activityFacilityResponse = ActivityFacilityResponse.builder().responseInfo(responseInfo).activityFacilities(fieldPlans).totalCount(count).build();
        return new ResponseEntity<ActivityFacilityResponse>(activityFacilityResponse, HttpStatus.OK);
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
}
