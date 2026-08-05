package org.egov.field_planner.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.egov.common.contract.request.RequestInfo;
import org.egov.field_planner.repository.AssessmentFacilityRepository;
import org.egov.field_planner.repository.AssessmentPlanRepository;
import org.egov.field_planner.util.AssessmentConstants;
import org.egov.field_planner.web.models.*;
import org.egov.tracer.model.CustomException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
@Slf4j
@RequiredArgsConstructor
public class PlanFacilityIncludeService {

    private final AssessmentPlanRepository planRepository;
    private final AssessmentFacilityRepository facilityRepository;
    private final AssessmentProjectService projectService;
    private final AssessmentWorkflowService workflowService;
    private final AssessmentFacilityMetadataService facilityMetadataService;

    @Transactional
    public PlanFacilityBulkIncludeResponse bulkInclude(PlanFacilityBulkIncludeRequest request) {
        List<PlanFacilityIncludeItem> items = request.getFacilityIds().stream()
                .map(id -> PlanFacilityIncludeItem.builder().facilityId(id).build())
                .toList();
        return includeFacilities(
                request.getRequestInfo(),
                request.getPlanId(),
                request.getTenantId(),
                items
        );
    }

    @Transactional
    public PlanFacilityBulkIncludeResponse bulkCreate(PlanFacilityBulkCreateRequest request) {
        return includeFacilities(
                request.getRequestInfo(),
                request.getPlanId(),
                request.getTenantId(),
                request.getFacilities()
        );
    }

    private PlanFacilityBulkIncludeResponse includeFacilities(RequestInfo requestInfo, String planId,
                                                                String tenantId, List<PlanFacilityIncludeItem> items) {
        AssessmentPlan plan = planRepository.findById(planId)
                .orElseThrow(() -> new CustomException(AssessmentConstants.ASSESSMENT_PLAN_NOT_FOUND,
                        "Assessment plan not found: " + planId));

        Set<String> projectFacilityIds = projectService.getProjectFacilityIds(
                requestInfo, tenantId, plan.getProjectId());

        List<PlanFacility> created = new ArrayList<>();
        List<PlanFacilityIncludeError> errors = new ArrayList<>();
        List<PlanFacilityIncludeError> skipped = new ArrayList<>();
        String userId = requestInfo.getUserInfo() != null ? requestInfo.getUserInfo().getUuid() : "system";

        for (PlanFacilityIncludeItem item : items) {
            String facilityId = item.getFacilityId();
            if (facilityId == null || facilityId.isBlank()) {
                errors.add(PlanFacilityIncludeError.builder()
                        .facilityId(facilityId)
                        .code(AssessmentConstants.ASSESSMENT_FACILITY_NOT_ON_PROJECT)
                        .message("Facility Id is required")
                        .build());
                continue;
            }
            Optional<String> validationError = validateInclude(plan, facilityId, projectFacilityIds);
            if (validationError.isPresent()) {
                String[] parts = validationError.get().split("\\|", 2);
                errors.add(PlanFacilityIncludeError.builder()
                        .facilityId(facilityId)
                        .code(parts[0])
                        .message(parts.length > 1 ? parts[1] : parts[0])
                        .build());
                continue;
            }

            if (facilityRepository.findExistingOnPlan(planId, facilityId).isPresent()) {
                errors.add(PlanFacilityIncludeError.builder()
                        .facilityId(facilityId)
                        .code(AssessmentConstants.ASSESSMENT_FACILITY_ALREADY_ON_PLAN)
                        .message("Facility already included in this assessment plan")
                        .build());
                continue;
            }

            facilityMetadataService.enrichIncludeItem(item);

            PlanFacility createdFacility = facilityRepository.insertFacility(
                    planId, tenantId, facilityId, item, userId);
            try {
                workflowService.transitionWorkflow(
                        createdFacility.getPlanFacilityId(),
                        tenantId,
                        AssessmentConstants.WF_ACTION_CREATE,
                        requestInfo,
                        "Assessment facility included"
                );
            } catch (Exception e) {
                log.error("Workflow CREATE failed for facility {} on plan {}", facilityId, planId, e);
                facilityRepository.deleteById(createdFacility.getPlanFacilityId());
                errors.add(PlanFacilityIncludeError.builder()
                        .facilityId(facilityId)
                        .code(AssessmentConstants.WORKFLOW_TRANSITION_FAILED)
                        .message(e.getMessage() != null ? e.getMessage()
                                : "Workflow CREATE transition failed")
                        .build());
                continue;
            }
            created.add(createdFacility);
        }

        int facilityCount = planRepository.countFacilitiesOnPlan(planId);
        planRepository.updateHealthFacilityCount(planId, facilityCount);
        plan.setHealthFacilityCount(facilityCount);
        enrichCanProceed(plan);

        return PlanFacilityBulkIncludeResponse.builder()
                .created(created)
                .errors(errors)
                .skipped(skipped)
                .plan(plan)
                .build();
    }

    private Optional<String> validateInclude(AssessmentPlan plan, String facilityId, Set<String> projectFacilityIds) {
        if (!projectFacilityIds.contains(facilityId)) {
            return Optional.of(AssessmentConstants.ASSESSMENT_FACILITY_NOT_ON_PROJECT
                    + "|Facility is not linked to this project");
        }

        // R5 — ongoing assessment with PENDING overall_status
        if (facilityRepository.countPendingOverallForFacility(facilityId) > 0) {
            return Optional.of(AssessmentConstants.ASSESSMENT_FACILITY_ONGOING
                    + "|Facility has an ongoing assessment with pending result");
        }

        // R0 — source assessment plans must be CLOSED
        List<Map<String, Object>> openPlans = facilityRepository.findNonClosedSourcePlans(facilityId, plan.getId());
        if (!openPlans.isEmpty()) {
            return Optional.of(AssessmentConstants.ASSESSMENT_PLAN_NOT_COMPLETE
                    + "|Complete source assessment plan before reusing this facility");
        }

        // R1 — same project eligible unassigned
        if (facilityRepository.hasSameProjectEligibleActive(facilityId, plan.getProjectId(), plan.getId())) {
            return Optional.of(AssessmentConstants.ASSESSMENT_FACILITY_ELIGIBLE_ACTIVE
                    + "|Eligible facility in same project must be handed off or marked not eligible first");
        }

        return Optional.empty();
    }

    private void enrichCanProceed(AssessmentPlan plan) {
        if (plan.getHealthFacilityCount() == null || plan.getHealthFacilityCount() == 0) {
            plan.setCanProceedToFieldPlan(false);
            return;
        }
        AssessmentPlanMetrics metrics = planRepository.getMetrics(plan.getId());
        plan.setCanProceedToFieldPlan(metrics.getResultPending() == 0 && metrics.getRemoteAssessmentTotal() > 0);
    }
}
