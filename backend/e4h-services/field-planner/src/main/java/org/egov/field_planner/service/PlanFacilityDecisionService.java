package org.egov.field_planner.service;

import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.egov.common.contract.request.RequestInfo;
import org.egov.field_planner.repository.AssessmentFacilityRepository;
import org.egov.field_planner.util.AssessmentAdditionalDetailsHelper;
import org.egov.field_planner.util.AssessmentConstants;
import org.egov.field_planner.web.models.*;
import org.egov.tracer.model.CustomException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class PlanFacilityDecisionService {

    private final AssessmentFacilityRepository facilityRepository;
    private final AssessmentWorkflowService workflowService;
    private final AllowedActionsService allowedActionsService;
    private final PlanFacilitySearchService searchService;

    @Transactional
    public PlanFacilityDecisionResponse updateDecision(PlanFacilityDecisionRequest request) {
        PlanFacility facility = applyDecision(request.getRequestInfo(), toItem(request));
        return PlanFacilityDecisionResponse.builder().facility(facility).build();
    }

    @Transactional
    public PlanFacilityDecisionBulkResponse bulkUpdate(PlanFacilityDecisionBulkRequest request) {
        List<PlanFacility> success = new ArrayList<>();
        List<PlanFacilityDecisionError> errors = new ArrayList<>();
        for (PlanFacilityDecisionItem item : request.getDecisions()) {
            try {
                success.add(applyDecision(request.getRequestInfo(), item));
            } catch (CustomException e) {
                errors.add(PlanFacilityDecisionError.builder()
                        .planFacilityId(item.getPlanFacilityId())
                        .code(e.getCode())
                        .message(e.getMessage())
                        .build());
            }
        }
        return PlanFacilityDecisionBulkResponse.builder()
                .success(success)
                .errors(errors)
                .build();
    }

    private PlanFacility applyDecision(RequestInfo requestInfo, PlanFacilityDecisionItem item) {
        PlanFacility facility = facilityRepository.findById(item.getPlanFacilityId())
                .orElseThrow(() -> new CustomException(AssessmentConstants.ASSESSMENT_PLAN_FACILITY_NOT_FOUND,
                        "Plan facility not found: " + item.getPlanFacilityId()));

        if (Boolean.TRUE.equals(item.getAssignForField())) {
            return assignForField(requestInfo, facility);
        }
        if (StringUtils.isNotBlank(item.getOverallStatus())) {
            return markOverall(requestInfo, facility, item);
        }
        throw new CustomException("ASSESSMENT_INVALID_DECISION", "No decision action specified");
    }

    private PlanFacility assignForField(RequestInfo requestInfo, PlanFacility facility) {
        AllowedActions actions = allowedActionsService.compute(facility);
        if (!actions.isAssignForField()) {
            if (AssessmentConstants.FINAL_OVERALL_STATUSES.contains(facility.getOverallStatus())) {
                throw new CustomException(AssessmentConstants.ASSESSMENT_RESULT_ALREADY_SET,
                        "Assign for on-site blocked — result already final");
            }
            if (AssessmentConstants.REMOTE_PENDING_STATUSES.contains(facility.getPhoneStatus())
                    || AssessmentConstants.PHONE_PENDING.equals(facility.getPhoneStatus())) {
                throw new CustomException(AssessmentConstants.ASSESSMENT_ASSIGN_FIELD_INVALID,
                        "Assign for on-site requires remote Qualified or Not Qualified, on-site Not Initiated, and result Pending");
            }
            throw new CustomException(AssessmentConstants.ASSESSMENT_ASSIGN_FIELD_INVALID,
                    "Assign for on-site preconditions not met");
        }

        workflowService.transitionWorkflow(facility.getPlanFacilityId(), resolveWorkflowTenantId(requestInfo),
                AssessmentConstants.WF_ACTION_ASSIGN_FOR_FIELD, requestInfo, null);

        long now = System.currentTimeMillis();
        Map<String, Object> additionalDetails = AssessmentAdditionalDetailsHelper.appendAuditEvent(
                facility.getAdditionalDetails(),
                AssessmentAuditEvent.builder()
                        .event(AssessmentConstants.AUDIT_ASSIGNED_FOR_ONSITE)
                        .timestamp(now)
                        .actor(requestInfo.getUserInfo().getUuid())
                        .build()
        );

        facilityRepository.updateFacilityStatuses(
                facility.getPlanFacilityId(),
                null,
                AssessmentConstants.FIELD_PENDING,
                AssessmentConstants.OVERALL_PENDING,
                null,
                additionalDetails,
                requestInfo.getUserInfo().getUuid()
        );

        PlanFacility updated = facilityRepository.findById(facility.getPlanFacilityId()).orElseThrow();
        updated.setAllowedActions(allowedActionsService.compute(updated));
        return updated;
    }

    private PlanFacility markOverall(RequestInfo requestInfo, PlanFacility facility, PlanFacilityDecisionItem item) {
        if (AssessmentConstants.REMOTE_PENDING_STATUSES.contains(facility.getPhoneStatus())
                || AssessmentConstants.PHONE_PENDING.equals(facility.getPhoneStatus())) {
            throw new CustomException(AssessmentConstants.ASSESSMENT_REMOTE_PENDING,
                    "Remote assessment still pending");
        }

        String phoneOutcome = facility.getPhoneOutcome();
        String fieldOutcome = facility.getFieldOutcome();
        String targetStatus = item.getOverallStatus();

        if (AssessmentConstants.OVERALL_ELIGIBLE.equals(targetStatus)) {
            validateEligible(item, phoneOutcome, fieldOutcome, facility.getPlanFacilityId());
        } else if (AssessmentConstants.OVERALL_NOT_ELIGIBLE.equals(targetStatus)) {
            validateNotEligible(item, phoneOutcome, fieldOutcome, facility.getPlanFacilityId());
        } else {
            throw new CustomException("ASSESSMENT_INVALID_OVERALL_STATUS",
                    "overallStatus must be ELIGIBLE or NOT_ELIGIBLE");
        }

        String wfAction = AssessmentConstants.OVERALL_ELIGIBLE.equals(targetStatus)
                ? AssessmentConstants.WF_ACTION_MARK_ELIGIBLE
                : AssessmentConstants.WF_ACTION_MARK_NOT_ELIGIBLE;
        workflowService.transitionWorkflow(facility.getPlanFacilityId(), resolveWorkflowTenantId(requestInfo),
                wfAction, requestInfo, item.getRemarks());

        long now = System.currentTimeMillis();
        Map<String, Object> assessmentUpdates = new HashMap<>();
        assessmentUpdates.put("overallManuallySet", true);
        if (StringUtils.isNotBlank(item.getRemarks())) {
            assessmentUpdates.put("remarks", item.getRemarks());
        }
        Map<String, Object> additionalDetails = searchService.buildAssessmentMetadataUpdate(facility, assessmentUpdates);
        additionalDetails = AssessmentAdditionalDetailsHelper.appendAuditEvent(
                additionalDetails,
                AssessmentAuditEvent.builder()
                        .event(AssessmentConstants.OVERALL_ELIGIBLE.equals(targetStatus)
                                ? AssessmentConstants.AUDIT_OVERALL_SET_ELIGIBLE
                                : AssessmentConstants.AUDIT_OVERALL_SET_NOT_ELIGIBLE)
                        .timestamp(now)
                        .actor(requestInfo.getUserInfo().getUuid())
                        .build()
        );

        String completionStatus = AssessmentConstants.OVERALL_ELIGIBLE.equals(targetStatus)
                ? AssessmentConstants.COMPLETION_ELIGIBLE
                : AssessmentConstants.COMPLETION_NOT_ELIGIBLE;

        facilityRepository.updateFacilityStatuses(
                facility.getPlanFacilityId(),
                null,
                facility.getFieldStatus(),
                targetStatus,
                completionStatus,
                additionalDetails,
                requestInfo.getUserInfo().getUuid()
        );

        PlanFacility updated = facilityRepository.findById(facility.getPlanFacilityId()).orElseThrow();
        updated.setAllowedActions(allowedActionsService.compute(updated));
        return updated;
    }

    private void validateEligible(PlanFacilityDecisionItem item, String phoneOutcome, String fieldOutcome,
                                    String planFacilityId) {
        if (AssessmentConstants.OUTCOME_NOT_QUALIFIED.equals(phoneOutcome)
                && AssessmentConstants.OUTCOME_NOT_QUALIFIED.equals(fieldOutcome)
                && StringUtils.isBlank(item.getRemarks())) {
            throw new CustomException(AssessmentConstants.ASSESSMENT_ELIGIBLE_REASON_REQUIRED,
                    "remarks is required when both outcomes are NOT_QUALIFIED");
        }
    }

    private void validateNotEligible(PlanFacilityDecisionItem item, String phoneOutcome, String fieldOutcome,
                                      String planFacilityId) {
        if (AssessmentConstants.OUTCOME_QUALIFIED.equals(phoneOutcome)
                && AssessmentConstants.OUTCOME_QUALIFIED.equals(fieldOutcome)
                && StringUtils.isBlank(item.getRemarks())) {
            throw new CustomException(AssessmentConstants.ASSESSMENT_INELIGIBLE_REASON_REQUIRED,
                    "remarks is required when both outcomes are QUALIFIED");
        }
    }

    private PlanFacilityDecisionItem toItem(PlanFacilityDecisionRequest request) {
        return PlanFacilityDecisionItem.builder()
                .planFacilityId(request.getPlanFacilityId())
                .assignForField(request.getAssignForField())
                .overallStatus(request.getOverallStatus())
                .remarks(request.getRemarks())
                .build();
    }

    private String resolveWorkflowTenantId(RequestInfo requestInfo) {
        if (requestInfo != null && requestInfo.getUserInfo() != null
                && StringUtils.isNotBlank(requestInfo.getUserInfo().getTenantId())) {
            String tenantId = requestInfo.getUserInfo().getTenantId();
            int dotIndex = tenantId.indexOf('.');
            return dotIndex > 0 ? tenantId.substring(0, dotIndex) : tenantId;
        }
        return "in";
    }
}
