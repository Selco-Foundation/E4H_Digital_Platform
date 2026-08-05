package org.egov.field_planner.service;

import lombok.RequiredArgsConstructor;
import org.egov.field_planner.repository.AssessmentFacilityRepository;
import org.egov.field_planner.repository.AssessmentSubmissionRepository;
import org.egov.field_planner.util.AssessmentAdditionalDetailsHelper;
import org.egov.field_planner.util.AssessmentConstants;
import org.egov.field_planner.web.models.*;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class PlanFacilitySearchService {

    private final AssessmentFacilityRepository facilityRepository;
    private final AssessmentSubmissionRepository submissionRepository;
    private final AllowedActionsService allowedActionsService;
    private final OutcomeEngineService outcomeEngineService;
    private final AssessmentMdmsService mdmsService;
    private final AssessmentFacilityMetadataService facilityMetadataService;

    public PlanFacilitySearchResponse search(PlanFacilitySearchRequest request, int limit, int offset) {
        boolean exportAll = Boolean.TRUE.equals(request.getExportAll());
        boolean includeSummary = Boolean.TRUE.equals(request.getIncludeResponseSummary());
        int effectiveLimit = exportAll ? Math.max(limit, 10000) : limit;
        int effectiveOffset = exportAll ? 0 : offset;

        List<PlanFacility> facilities = facilityRepository.searchByPlan(
                request.getPlanId(), request.getFilters(), effectiveLimit, effectiveOffset);
        facilities.forEach(f -> {
            facilityMetadataService.enrichDisplayFields(f);
            enrichFacility(f, includeSummary, request.getRequestInfo());
        });
        int total = facilityRepository.countByPlan(request.getPlanId(), request.getFilters());

        return PlanFacilitySearchResponse.builder()
                .facilities(facilities)
                .pagination(Pagination.builder().offset(effectiveOffset).limit(effectiveLimit).total(total).build())
                .build();
    }

    public PlanFacilityDetailResponse getDetail(PlanFacilityDetailRequest request) {
        PlanFacility facility = facilityRepository.findById(request.getPlanFacilityId())
                .orElseThrow(() -> new org.egov.tracer.model.CustomException(
                        AssessmentConstants.ASSESSMENT_PLAN_FACILITY_NOT_FOUND,
                        "Plan facility not found: " + request.getPlanFacilityId()));

        facilityMetadataService.enrichDisplayFields(facility);

        List<AssessmentSubmission> submissions = submissionRepository.findByPlanFacilityId(facility.getPlanFacilityId());
        AllowedActions actions = allowedActionsService.compute(facility);

        PlanFacilityDetail detail = PlanFacilityDetail.builder()
                .planFacilityId(facility.getPlanFacilityId())
                .assessmentPlanId(facility.getAssessmentPlanId())
                .facilityId(facility.getFacilityId())
                .facilityName(facility.getFacilityName())
                .facilityCategory(facility.getFacilityCategory())
                .facilityType(facility.getFacilityType())
                .district(facility.getDistrict())
                .block(facility.getBlock())
                .phoneStatus(facility.getPhoneStatus())
                .fieldStatus(facility.getFieldStatus())
                .overallStatus(facility.getOverallStatus())
                .phoneOutcome(facility.getPhoneOutcome())
                .fieldOutcome(facility.getFieldOutcome())
                .overallManuallySet(facility.getOverallManuallySet())
                .eligibleReason(facility.getEligibleReason())
                .ineligibleReason(facility.getIneligibleReason())
                .assessmentCompletionStatus(facility.getAssessmentCompletionStatus())
                .allowedActions(actions)
                .submissions(submissions)
                .auditTrail(facility.getAuditTrail())
                .build();

        return PlanFacilityDetailResponse.builder().facility(detail).build();
    }

    public PlanFacility enrichFacility(PlanFacility facility, boolean includeSummary,
                                        org.egov.common.contract.request.RequestInfo requestInfo) {
        facility.setAllowedActions(allowedActionsService.compute(facility));
        if (includeSummary) {
            appendResponseSummaries(facility, requestInfo);
        }
        return facility;
    }

    private void appendResponseSummaries(PlanFacility facility,
                                          org.egov.common.contract.request.RequestInfo requestInfo) {
        List<AssessmentSubmission> submissions = submissionRepository.findByPlanFacilityId(facility.getPlanFacilityId());
        for (AssessmentSubmission submission : submissions) {
            AssessmentFormSchema schema = mdmsService.getFormSchema(
                    requestInfo, "in", submission.getFormType());
            List<String> summary = outcomeEngineService.buildResponseSummary(schema, submission.getSubmissionData());
            if (AssessmentConstants.PHASE_PHONE.equals(submission.getAssessmentPhase())) {
                facility.setPhoneResponseSummary(summary);
            } else {
                facility.setFieldResponseSummary(summary);
            }
        }
    }

    public Map<String, Object> buildAssessmentMetadataUpdate(PlanFacility facility, Map<String, Object> updates) {
        Map<String, Object> additionalDetails = facility.getAdditionalDetails() != null
                ? new HashMap<>(facility.getAdditionalDetails()) : new HashMap<>();
        return AssessmentAdditionalDetailsHelper.mergeAssessmentUpdates(additionalDetails, updates);
    }
}
