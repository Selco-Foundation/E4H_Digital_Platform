package org.egov.field_planner.service;

import lombok.RequiredArgsConstructor;
import org.egov.field_planner.repository.AssessmentFacilityRepository;
import org.egov.field_planner.repository.AssessmentPlanRepository;
import org.egov.field_planner.repository.FieldPlanFacilityRepository;
import org.egov.field_planner.util.AssessmentAdditionalDetailsHelper;
import org.egov.field_planner.util.AssessmentConstants;
import org.egov.field_planner.web.models.*;
import org.egov.tracer.model.CustomException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AssessmentHandoffService {

    private final AssessmentFacilityRepository facilityRepository;
    private final AssessmentPlanRepository planRepository;
    private final FieldPlanFacilityRepository fieldPlanFacilityRepository;

    @Transactional
    public PlanFacilityHandoffResponse applyHandoff(PlanFacilityHandoffRequest request) {
        PlanFacility facility = facilityRepository.findById(request.getPlanFacilityId())
                .orElseThrow(() -> new CustomException(AssessmentConstants.ASSESSMENT_PLAN_FACILITY_NOT_FOUND,
                        "Plan facility not found: " + request.getPlanFacilityId()));

        if (!AssessmentConstants.COMPLETION_ELIGIBLE.equals(facility.getAssessmentCompletionStatus())) {
            throw new CustomException(AssessmentConstants.ASSESSMENT_FACILITY_NOT_ELIGIBLE,
                    "planFacilityId not ELIGIBLE or already handed off");
        }

        if (request.getFieldPlanFacilityId() == null || request.getFieldPlanFacilityId().isBlank()) {
            throw new CustomException(AssessmentConstants.ASSESSMENT_HANDOFF_FIELD_PLAN_FACILITY_REQUIRED,
                    "fieldPlanFacilityId is required for assessment handoff");
        }

        String assessmentPlanId = facility.getAssessmentPlanId();
        AssessmentPlan assessmentPlan = planRepository.findById(assessmentPlanId)
                .orElseThrow(() -> new CustomException(AssessmentConstants.ASSESSMENT_PLAN_NOT_FOUND,
                        "Assessment plan not found: " + assessmentPlanId));

        String installationProjectId = fieldPlanFacilityRepository
                .findProjectIdByFieldPlanId(request.getInstallationFieldPlanId())
                .orElseThrow(() -> new CustomException(AssessmentConstants.ASSESSMENT_PROJECT_NOT_FOUND,
                        "Installation plan not found: " + request.getInstallationFieldPlanId()));

        String userId = request.getRequestInfo().getUserInfo().getUuid();
        fieldPlanFacilityRepository.linkAssessmentSource(
                request.getFieldPlanFacilityId(),
                request.getPlanFacilityId(),
                assessmentPlanId,
                userId
        );

        boolean sameProject = assessmentPlan.getProjectId().equals(installationProjectId);
        if (sameProject) {
            if (AssessmentConstants.COMPLETION_MOVED_TO_FIELD_PLAN.equals(facility.getAssessmentCompletionStatus())) {
                throw new CustomException(AssessmentConstants.ASSESSMENT_FACILITY_ALREADY_ON_FIELD_PLAN,
                        "Facility already handed off to an installation plan");
            }
            applySameProjectHandoff(facility, request, userId);
        }

        PlanFacility updated = facilityRepository.findById(facility.getPlanFacilityId()).orElseThrow();
        return PlanFacilityHandoffResponse.builder().facility(updated).build();
    }

    private void applySameProjectHandoff(PlanFacility facility, PlanFacilityHandoffRequest request, String userId) {
        long now = System.currentTimeMillis();
        var additionalDetails = AssessmentAdditionalDetailsHelper.appendAuditEvent(
                facility.getAdditionalDetails(),
                AssessmentAuditEvent.builder()
                        .event(AssessmentConstants.AUDIT_HANDOFF)
                        .timestamp(now)
                        .actor(userId)
                        .build()
        );

        facilityRepository.applyHandoff(
                facility.getPlanFacilityId(),
                request.getInstallationFieldPlanId(),
                request.getFieldPlanFacilityId(),
                additionalDetails,
                userId
        );
    }
}
