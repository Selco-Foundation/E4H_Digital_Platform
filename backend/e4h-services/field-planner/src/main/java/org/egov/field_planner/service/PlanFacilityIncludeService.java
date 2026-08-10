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

    public PlanFacilityIncludeAvailabilityResponse checkIncludeAvailability(
            PlanFacilityIncludeAvailabilityRequest request) {
        AssessmentPlan plan = planRepository.findById(request.getPlanId())
                .orElseThrow(() -> new CustomException(AssessmentConstants.ASSESSMENT_PLAN_NOT_FOUND,
                        "Assessment plan not found: " + request.getPlanId()));

        Set<String> projectFacilityIds = projectService.getProjectFacilityIds(
                request.getRequestInfo(), request.getTenantId(), plan.getProjectId());

        List<String> facilityIds = request.getFacilityIds() != null
                ? request.getFacilityIds().stream().filter(Objects::nonNull).distinct().toList()
                : List.of();

        List<String> available = new ArrayList<>();
        List<PlanFacilityIncludeError> excluded = new ArrayList<>();

        for (String facilityId : facilityIds) {
            Optional<PlanFacilityIncludeError> exclusion = evaluateAvailability(
                    plan, facilityId, projectFacilityIds, true);
            if (exclusion.isPresent()) {
                excluded.add(exclusion.get());
            } else {
                available.add(facilityId);
            }
        }

        return PlanFacilityIncludeAvailabilityResponse.builder()
                .availableFacilityIds(available)
                .excluded(excluded)
                .build();
    }

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

            Optional<PlanFacilityIncludeError> exclusion = evaluateAvailability(
                    plan, facilityId, projectFacilityIds, false);
            if (exclusion.isPresent()) {
                PlanFacilityIncludeError issue = exclusion.get();
                if (AssessmentConstants.ASSESSMENT_FACILITY_NOT_ON_PROJECT.equals(issue.getCode())) {
                    errors.add(issue);
                } else {
                    skipped.add(issue);
                }
                continue;
            }

            facilityMetadataService.enrichIncludeItem(item, requestInfo, tenantId);

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

    Optional<PlanFacilityIncludeError> evaluateAvailability(AssessmentPlan plan, String facilityId,
                                                             Set<String> projectFacilityIds,
                                                             boolean forTemplateDownload) {
        if (!projectFacilityIds.contains(facilityId)) {
            return Optional.of(PlanFacilityIncludeError.builder()
                    .facilityId(facilityId)
                    .code(AssessmentConstants.ASSESSMENT_FACILITY_NOT_ON_PROJECT)
                    .message("Facility is not linked to this project")
                    .build());
        }

        if (facilityRepository.findExistingOnPlan(plan.getId(), facilityId).isPresent()) {
            if (forTemplateDownload) {
                return Optional.empty();
            }
            return Optional.of(PlanFacilityIncludeError.builder()
                    .facilityId(facilityId)
                    .code(AssessmentConstants.ASSESSMENT_FACILITY_ALREADY_ON_PLAN)
                    .message("Facility already included in this assessment plan")
                    .build());
        }

        if (facilityRepository.countPendingOverallOnOtherPlans(facilityId, plan.getId()) > 0) {
            return Optional.of(PlanFacilityIncludeError.builder()
                    .facilityId(facilityId)
                    .code(AssessmentConstants.ASSESSMENT_FACILITY_ONGOING)
                    .message("Facility has an ongoing assessment with pending result")
                    .build());
        }

        List<Map<String, Object>> openPlans = facilityRepository.findNonClosedSourcePlans(facilityId, plan.getId());
        if (!openPlans.isEmpty()) {
            return Optional.of(PlanFacilityIncludeError.builder()
                    .facilityId(facilityId)
                    .code(AssessmentConstants.ASSESSMENT_PLAN_NOT_COMPLETE)
                    .message("Complete source assessment plan before reusing this facility")
                    .build());
        }

        if (facilityRepository.hasSameProjectEligibleActive(facilityId, plan.getProjectId(), plan.getId())) {
            return Optional.of(PlanFacilityIncludeError.builder()
                    .facilityId(facilityId)
                    .code(AssessmentConstants.ASSESSMENT_FACILITY_ELIGIBLE_ACTIVE)
                    .message("Eligible facility in same project must be handed off or marked not eligible first")
                    .build());
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
