package org.egov.field_planner.web.controllers;

import io.swagger.annotations.ApiParam;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.egov.common.contract.response.ResponseInfo;
import org.egov.common.utils.ResponseInfoFactory;
import org.egov.field_planner.service.*;
import org.egov.field_planner.web.models.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/assessment/v1")
@Validated
@Slf4j
public class AssessmentApiController {

    private final AssessmentPlanService assessmentPlanService;
    private final PlanFacilityIncludeService planFacilityIncludeService;
    private final PlanFacilitySearchService planFacilitySearchService;
    private final PlanFacilityDecisionService planFacilityDecisionService;
    private final AssessmentSubmissionService assessmentSubmissionService;
    private final EligibleFacilitiesService eligibleFacilitiesService;
    private final AssessmentHandoffService assessmentHandoffService;

    public AssessmentApiController(AssessmentPlanService assessmentPlanService,
                                   PlanFacilityIncludeService planFacilityIncludeService,
                                   PlanFacilitySearchService planFacilitySearchService,
                                   PlanFacilityDecisionService planFacilityDecisionService,
                                   AssessmentSubmissionService assessmentSubmissionService,
                                   EligibleFacilitiesService eligibleFacilitiesService,
                                   AssessmentHandoffService assessmentHandoffService) {
        this.assessmentPlanService = assessmentPlanService;
        this.planFacilityIncludeService = planFacilityIncludeService;
        this.planFacilitySearchService = planFacilitySearchService;
        this.planFacilityDecisionService = planFacilityDecisionService;
        this.assessmentSubmissionService = assessmentSubmissionService;
        this.eligibleFacilitiesService = eligibleFacilitiesService;
        this.assessmentHandoffService = assessmentHandoffService;
    }

    @PostMapping("/plan/_create")
    public ResponseEntity<AssessmentPlanResponse> createPlan(
            @ApiParam(required = true) @Valid @RequestBody AssessmentPlanCreateRequest request) {
        log.info("Creating assessment plan for project {}", request.getPlan().getProjectId());
        AssessmentPlan plan = assessmentPlanService.createPlan(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(AssessmentPlanResponse.builder()
                .responseInfo(ResponseInfoFactory.createResponseInfo(request.getRequestInfo(), true))
                .plan(plan)
                .build());
    }

    @PostMapping("/plan/_search")
    public ResponseEntity<AssessmentPlanResponse> searchPlans(
            @ApiParam(required = true) @Valid @RequestBody AssessmentPlanSearchRequest request,
            @RequestParam(defaultValue = "0") int offset,
            @RequestParam(defaultValue = "20") int limit) {
        AssessmentPlanResponse response = assessmentPlanService.searchPlans(request, limit, offset);
        response.setResponseInfo(ResponseInfoFactory.createResponseInfo(request.getRequestInfo(), true));
        return ResponseEntity.ok(response);
    }

    @PostMapping("/plan/_detail")
    public ResponseEntity<AssessmentPlanResponse> planDetail(
            @ApiParam(required = true) @Valid @RequestBody AssessmentPlanDetailRequest request) {
        AssessmentPlan plan = assessmentPlanService.getPlanDetail(request);
        return ResponseEntity.ok(AssessmentPlanResponse.builder()
                .responseInfo(ResponseInfoFactory.createResponseInfo(request.getRequestInfo(), true))
                .plan(plan)
                .build());
    }

    @PostMapping("/plan/_update")
    public ResponseEntity<AssessmentPlanResponse> updatePlan(
            @ApiParam(required = true) @Valid @RequestBody AssessmentPlanUpdateRequest request) {
        AssessmentPlan plan = assessmentPlanService.updatePlan(request);
        return ResponseEntity.ok(AssessmentPlanResponse.builder()
                .responseInfo(ResponseInfoFactory.createResponseInfo(request.getRequestInfo(), true))
                .plan(plan)
                .build());
    }

    @PostMapping("/plan/_mark-complete")
    public ResponseEntity<AssessmentPlanResponse> markComplete(
            @ApiParam(required = true) @Valid @RequestBody AssessmentPlanMarkCompleteRequest request) {
        AssessmentPlan plan = assessmentPlanService.markComplete(request);
        return ResponseEntity.ok(AssessmentPlanResponse.builder()
                .responseInfo(ResponseInfoFactory.createResponseInfo(request.getRequestInfo(), true))
                .plan(plan)
                .build());
    }

    @PostMapping("/plan/facility/_bulk-include")
    public ResponseEntity<PlanFacilityBulkIncludeResponse> bulkInclude(
            @ApiParam(required = true) @Valid @RequestBody PlanFacilityBulkIncludeRequest request) {
        PlanFacilityBulkIncludeResponse response = planFacilityIncludeService.bulkInclude(request);
        response.setResponseInfo(ResponseInfoFactory.createResponseInfo(request.getRequestInfo(), true));
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/plan/facility/_search")
    public ResponseEntity<PlanFacilitySearchResponse> searchPlanFacilities(
            @ApiParam(required = true) @Valid @RequestBody PlanFacilitySearchRequest request,
            @RequestParam(defaultValue = "0") int offset,
            @RequestParam(defaultValue = "50") int limit) {
        PlanFacilitySearchResponse response = planFacilitySearchService.search(request, limit, offset);
        response.setResponseInfo(ResponseInfoFactory.createResponseInfo(request.getRequestInfo(), true));
        return ResponseEntity.ok(response);
    }

    @PostMapping("/plan/facility/_detail")
    public ResponseEntity<PlanFacilityDetailResponse> planFacilityDetail(
            @ApiParam(required = true) @Valid @RequestBody PlanFacilityDetailRequest request) {
        PlanFacilityDetailResponse response = planFacilitySearchService.getDetail(request);
        response.setResponseInfo(ResponseInfoFactory.createResponseInfo(request.getRequestInfo(), true));
        return ResponseEntity.ok(response);
    }

    @PostMapping("/plan/facility/decision/_update")
    public ResponseEntity<PlanFacilityDecisionResponse> updateDecision(
            @ApiParam(required = true) @Valid @RequestBody PlanFacilityDecisionRequest request) {
        PlanFacilityDecisionResponse response = planFacilityDecisionService.updateDecision(request);
        response.setResponseInfo(ResponseInfoFactory.createResponseInfo(request.getRequestInfo(), true));
        return ResponseEntity.ok(response);
    }

    @PostMapping("/plan/facility/decision/_bulk-update")
    public ResponseEntity<PlanFacilityDecisionBulkResponse> bulkUpdateDecision(
            @ApiParam(required = true) @Valid @RequestBody PlanFacilityDecisionBulkRequest request) {
        PlanFacilityDecisionBulkResponse response = planFacilityDecisionService.bulkUpdate(request);
        response.setResponseInfo(ResponseInfoFactory.createResponseInfo(request.getRequestInfo(), true));
        return ResponseEntity.ok(response);
    }

    @PostMapping("/submission/queue/_search")
    public ResponseEntity<SubmissionQueueSearchResponse> searchSubmissionQueue(
            @ApiParam(required = true) @Valid @RequestBody SubmissionQueueSearchRequest request) {
        SubmissionQueueSearchResponse response = assessmentSubmissionService.searchQueue(request);
        response.setResponseInfo(ResponseInfoFactory.createResponseInfo(request.getRequestInfo(), true));
        return ResponseEntity.ok(response);
    }

    @PostMapping("/submission/form/_resolve")
    public ResponseEntity<SubmissionFormResolveResponse> resolveForm(
            @ApiParam(required = true) @Valid @RequestBody SubmissionFormResolveRequest request) {
        SubmissionFormResolveResponse response = assessmentSubmissionService.resolveForm(request);
        response.setResponseInfo(ResponseInfoFactory.createResponseInfo(request.getRequestInfo(), true));
        return ResponseEntity.ok(response);
    }

    @PostMapping("/submission/phone/_create")
    public ResponseEntity<SubmissionCreateResponse> createPhoneSubmission(
            @ApiParam(required = true) @Valid @RequestBody SubmissionPhoneCreateRequest request) {
        SubmissionCreateResponse response = assessmentSubmissionService.createPhoneSubmission(request);
        response.setResponseInfo(ResponseInfoFactory.createResponseInfo(request.getRequestInfo(), true));
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/submission/phone/_unable-to-contact")
    public ResponseEntity<PlanFacilityDecisionResponse> unableToContact(
            @ApiParam(required = true) @Valid @RequestBody SubmissionUnableToContactRequest request) {
        PlanFacility facility = assessmentSubmissionService.unableToContact(request);
        return ResponseEntity.ok(PlanFacilityDecisionResponse.builder()
                .responseInfo(ResponseInfoFactory.createResponseInfo(request.getRequestInfo(), true))
                .facility(facility)
                .build());
    }

    @PostMapping("/submission/field/_create")
    public ResponseEntity<SubmissionCreateResponse> createFieldSubmission(
            @ApiParam(required = true) @Valid @RequestBody SubmissionFieldCreateRequest request) {
        SubmissionCreateResponse response = assessmentSubmissionService.createFieldSubmission(request);
        response.setResponseInfo(ResponseInfoFactory.createResponseInfo(request.getRequestInfo(), true));
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/internal/plan/facility/_bulk-create")
    public ResponseEntity<PlanFacilityBulkIncludeResponse> bulkCreate(
            @ApiParam(required = true) @Valid @RequestBody PlanFacilityBulkCreateRequest request) {
        log.info("Internal bulk-create for assessment plan {}, count={}",
                request.getPlanId(), request.getFacilities().size());
        PlanFacilityBulkIncludeResponse response = planFacilityIncludeService.bulkCreate(request);
        response.setResponseInfo(ResponseInfoFactory.createResponseInfo(request.getRequestInfo(), true));
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/internal/project/eligible-facilities/_search")
    public ResponseEntity<EligibleFacilitiesSearchResponse> searchEligibleFacilities(
            @ApiParam(required = true) @Valid @RequestBody EligibleFacilitiesSearchRequest request) {
        EligibleFacilitiesSearchResponse response = eligibleFacilitiesService.search(request);
        response.setResponseInfo(ResponseInfoFactory.createResponseInfo(request.getRequestInfo(), true));
        return ResponseEntity.ok(response);
    }

    @PostMapping("/internal/plan/passed-facilities/_search")
    public ResponseEntity<EligibleFacilitiesSearchResponse> searchPassedFacilities(
            @ApiParam(required = true) @Valid @RequestBody PassedFacilitiesSearchRequest request) {
        EligibleFacilitiesSearchResponse response = eligibleFacilitiesService.searchPassedFacilities(request);
        response.setResponseInfo(ResponseInfoFactory.createResponseInfo(request.getRequestInfo(), true));
        return ResponseEntity.ok(response);
    }

    @PostMapping("/internal/plan/facility/_handoff")
    public ResponseEntity<PlanFacilityHandoffResponse> applyHandoff(
            @ApiParam(required = true) @Valid @RequestBody PlanFacilityHandoffRequest request) {
        PlanFacilityHandoffResponse response = assessmentHandoffService.applyHandoff(request);
        response.setResponseInfo(ResponseInfoFactory.createResponseInfo(request.getRequestInfo(), true));
        return ResponseEntity.ok(response);
    }
}
