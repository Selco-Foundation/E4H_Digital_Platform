package org.egov.field_planner.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.egov.common.contract.request.RequestInfo;
import org.egov.field_planner.config.FieldPlannerConfiguration;
import org.egov.field_planner.repository.AssessmentPlanRepository;
import org.egov.field_planner.util.AssessmentConstants;
import org.egov.field_planner.web.models.*;
import org.egov.field_planner.config.FieldPlannerConfiguration;
import org.egov.field_planner.service.FieldPlannerService;
import org.egov.field_planner.service.ServiceRequestRepository;
import org.egov.field_planner.web.models.ActivityAssignment;
import org.egov.field_planner.web.models.ActivityAssignmentBulkRequest;
import org.egov.field_planner.web.models.ActivityAssignmentResponse;
import org.egov.field_planner.web.models.ActivityAssignmentSearchCriteria;
import org.egov.field_planner.web.models.ActivityAssignmentSearchRequest;
import org.egov.field_planner.web.models.Employee;
import org.egov.tracer.model.CustomException;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
@Slf4j
@RequiredArgsConstructor
public class AssessmentPlanService {

    private final AssessmentPlanRepository planRepository;
    private final AssessmentProjectService projectService;
    private final FieldPlannerConfiguration configuration;
    private final ServiceRequestRepository serviceRequestRepository;
    private final FieldPlannerService fieldPlannerService;

    @Qualifier("objectMapper")
    private final ObjectMapper mapper;

    @Transactional
    public AssessmentPlan createPlan(AssessmentPlanCreateRequest request) {
        AssessmentPlan plan = request.getPlan();
        RequestInfo requestInfo = request.getRequestInfo();
        validateDateRange(plan);
        projectService.validateProjectExists(requestInfo, plan.getTenantId(), plan.getProjectId());

        if (planRepository.existsByName(plan.getTenantId(), plan.getProjectId(), plan.getName())) {
            throw new CustomException(AssessmentConstants.ASSESSMENT_PLAN_NAME_DUPLICATE,
                    "Plan name already exists for this project");
        }

        plan.setId(UUID.randomUUID().toString());
        plan.setStatus(AssessmentConstants.PLAN_STATUS_ACTIVE);
        plan.setPlanType(AssessmentConstants.PLAN_TYPE_ASSESSMENT);
        plan.setHealthFacilityCount(0);
        plan.setCanProceedToFieldPlan(false);

        planRepository.insertPlan(plan, requestInfo.getUserInfo().getUuid());
        return plan;
    }

    public AssessmentPlanResponse searchPlans(AssessmentPlanSearchRequest request, int limit, int offset) {
        AssessmentPlanSearchCriteria criteria = request.getCriteria() != null
                ? request.getCriteria()
                : AssessmentPlanSearchCriteria.builder().build();

        List<AssessmentPlan> plans = planRepository.search(criteria, limit, offset);
        plans.forEach(this::enrichPlanSummary);
        int total = planRepository.count(criteria);

        return AssessmentPlanResponse.builder()
                .plans(plans)
                .pagination(Pagination.builder().offset(offset).limit(limit).total(total).build())
                .build();
    }

    public AssessmentPlan getPlanDetail(AssessmentPlanDetailRequest request) {
        AssessmentPlan plan = planRepository.findById(request.getPlanId())
                .orElseThrow(() -> new CustomException(AssessmentConstants.ASSESSMENT_PLAN_NOT_FOUND,
                        "Assessment plan not found: " + request.getPlanId()));

        AssessmentPlanMetrics metrics = planRepository.getMetrics(plan.getId());
        plan.setMetrics(metrics);
        plan.setAssessors(getAssessors(request.getRequestInfo(), plan));
        enrichCanProceed(plan, metrics);
        return plan;
    }

    @Transactional
    public AssessmentPlan updatePlan(AssessmentPlanUpdateRequest request) {
        AssessmentPlan incoming = request.getPlan();
        AssessmentPlan existing = planRepository.findById(incoming.getId())
                .orElseThrow(() -> new CustomException(AssessmentConstants.ASSESSMENT_PLAN_NOT_FOUND,
                        "Assessment plan not found: " + incoming.getId()));

        validateDateRange(incoming);
        if (StringUtils.isNotBlank(incoming.getName())
                && !incoming.getName().equalsIgnoreCase(existing.getName())
                && planRepository.existsByName(existing.getTenantId(), existing.getProjectId(), incoming.getName())) {
            throw new CustomException(AssessmentConstants.ASSESSMENT_PLAN_NAME_DUPLICATE,
                    "Plan name already exists for this project");
        }

        existing.setName(incoming.getName());
        existing.setStartDate(incoming.getStartDate());
        existing.setEndDate(incoming.getEndDate());
        if (StringUtils.isNotBlank(incoming.getState())) {
            existing.setState(incoming.getState());
        }

        planRepository.updatePlan(existing, request.getRequestInfo().getUserInfo().getUuid());

        if (request.getAssessors() != null && !request.getAssessors().isEmpty()) {
            assignAssessors(request.getRequestInfo(), existing, request.getAssessors());
            existing.setAssessors(request.getAssessors());
        }

        return existing;
    }

    @Transactional
    public AssessmentPlan markComplete(AssessmentPlanMarkCompleteRequest request) {
        AssessmentPlan plan = planRepository.findById(request.getPlanId())
                .orElseThrow(() -> new CustomException(AssessmentConstants.ASSESSMENT_PLAN_NOT_FOUND,
                        "Assessment plan not found: " + request.getPlanId()));

        AssessmentPlanMetrics metrics = planRepository.getMetrics(plan.getId());
        if (metrics.getResultPending() > 0) {
            throw new CustomException(AssessmentConstants.ASSESSMENT_PLAN_HAS_PENDING_FACILITIES,
                    "All facilities must have ELIGIBLE or NOT_ELIGIBLE overall status before marking complete");
        }

        planRepository.updatePlanStatus(plan.getId(), AssessmentConstants.PLAN_STATUS_CLOSED,
                request.getRequestInfo().getUserInfo().getUuid());
        plan.setStatus(AssessmentConstants.PLAN_STATUS_CLOSED);
        plan.setCanProceedToFieldPlan(true);
        return plan;
    }

    private void validateDateRange(AssessmentPlan plan) {
        if (plan.getStartDate() == null || plan.getEndDate() == null || plan.getEndDate() <= plan.getStartDate()) {
            throw new CustomException(AssessmentConstants.ASSESSMENT_INVALID_DATE_RANGE,
                    "endDate must be after startDate");
        }
    }

    private void enrichPlanSummary(AssessmentPlan plan) {
        int count = planRepository.countFacilitiesOnPlan(plan.getId());
        plan.setHealthFacilityCount(count);
        AssessmentPlanMetrics metrics = planRepository.getMetrics(plan.getId());
        enrichCanProceed(plan, metrics);
    }

    private void enrichCanProceed(AssessmentPlan plan, AssessmentPlanMetrics metrics) {
        plan.setCanProceedToFieldPlan(
                metrics.getRemoteAssessmentTotal() > 0 && metrics.getResultPending() == 0);
    }

    private List<AssessorAssignment> getAssessors(RequestInfo requestInfo, AssessmentPlan plan) {
        try {
            ActivityAssignmentSearchCriteria criteria = ActivityAssignmentSearchCriteria.builder()
                    .fieldPlanId(List.of(plan.getId()))
                    .tenantId(plan.getTenantId())
                    .build();
            ActivityAssignmentSearchRequest searchRequest = ActivityAssignmentSearchRequest.builder()
                    .criteria(criteria)
                    .requestInfo(requestInfo)
                    .build();
            String url = configuration.getFieldPlanActivityServiceHost()
                    + configuration.getFieldPlanActivitySearchUrl()
                    + "?tenantId=" + plan.getTenantId() + "&offset=0&limit=100";
            Object response = serviceRequestRepository.fetchResult(new StringBuilder(url), searchRequest);
            ActivityAssignmentResponse assignmentResponse = mapper.convertValue(response, ActivityAssignmentResponse.class);
            if (assignmentResponse == null || assignmentResponse.getActivityAssignment() == null) {
                return Collections.emptyList();
            }
            return assignmentResponse.getActivityAssignment().stream()
                    .map(this::toAssessorAssignment)
                    .toList();
        } catch (Exception e) {
            log.warn("Failed to fetch assessors for plan {}", plan.getId(), e);
            return Collections.emptyList();
        }
    }

    private AssessorAssignment toAssessorAssignment(ActivityAssignment assignment) {
        String roleCode = assignment.getRole() != null ? (String) assignment.getRole().get("code") : null;
        return AssessorAssignment.builder()
                .role(roleCode)
                .userId(assignment.getAssignedTo())
                .pocNumber(assignment.getPocNumber())
                .build();
    }

    private void assignAssessors(RequestInfo requestInfo, AssessmentPlan plan, List<AssessorAssignment> assessors) {
        validateAssessorRoles(assessors);
        List<ActivityAssignment> assignments = new ArrayList<>();
        for (AssessorAssignment assessor : assessors) {
            Employee employee = findEmployeeByEmail(requestInfo, assessor.getEmail());
            if (employee == null) {
                throw new CustomException(AssessmentConstants.ASSESSMENT_ASSESSOR_NOT_FOUND,
                        "Assessor not found for email: " + assessor.getEmail());
            }
            Map<String, Object> role = Map.of(
                    "code", assessor.getRole(),
                    "name", assessor.getRole()
            );
            assignments.add(ActivityAssignment.builder()
                    .tenantId(plan.getTenantId())
                    .fieldPlanId(plan.getId())
                    .activityId(AssessmentConstants.ASSESSMENT_ACTIVITY_ID)
                    .activityCode(AssessmentConstants.ACTIVITY_CODE_ASSESSMENT)
                    .assignedTo(employee.getUser().getUuid())
                    .assignedBy(requestInfo.getUserInfo().getUuid())
                    .role(role)
                    .pocNumber(assessor.getPocNumber())
                    .startDate(plan.getStartDate())
                    .endDate(plan.getEndDate())
                    .status("ACTIVE")
                    .build());
            assessor.setUserId(employee.getUser().getUuid());
        }

        ActivityAssignmentBulkRequest bulkRequest = ActivityAssignmentBulkRequest.builder()
                .requestInfo(requestInfo)
                .activityAssignments(assignments)
                .build();
        String url = configuration.getFieldPlanActivityServiceHost()
                + configuration.getFieldPlanActivityAssignUrl()
                + "?tenantId=" + plan.getTenantId();
        serviceRequestRepository.fetchResult(new StringBuilder(url), bulkRequest);
    }

    private void validateAssessorRoles(List<AssessorAssignment> assessors) {
        Set<String> roles = new HashSet<>();
        for (AssessorAssignment assessor : assessors) {
            if (StringUtils.isBlank(assessor.getRole()) || StringUtils.isBlank(assessor.getEmail())) {
                throw new CustomException(AssessmentConstants.ASSESSMENT_ASSESSOR_ROLE_REQUIRED,
                        "Both ENUMERATOR and FIELD_POC assessors are required");
            }
            roles.add(assessor.getRole());
        }
        if (!roles.contains(AssessmentConstants.ROLE_ENUMERATOR)
                || !roles.contains(AssessmentConstants.ROLE_FIELD_POC)) {
            throw new CustomException(AssessmentConstants.ASSESSMENT_ASSESSOR_ROLE_REQUIRED,
                    "Both ENUMERATOR and FIELD_POC assessors are required");
        }
    }

    private Employee findEmployeeByEmail(RequestInfo requestInfo, String email) {
        try {
            String url = configuration.getHrmsHost() + configuration.getHrmsSearchUrl()
                    + "?tenantId=in&employees=0&limit=1&offset=0";
            Map<String, Object> payload = new HashMap<>();
            payload.put("RequestInfo", requestInfo);
            payload.put("Employee", Map.of("codes", List.of(email)));
            Object response = serviceRequestRepository.fetchResult(new StringBuilder(url), payload);
            var employeeResponse = mapper.convertValue(response, org.egov.field_planner.web.models.EmployeeResponse.class);
            if (employeeResponse != null && employeeResponse.getEmployees() != null
                    && !employeeResponse.getEmployees().isEmpty()) {
                return employeeResponse.getEmployees().get(0);
            }
        } catch (Exception e) {
            log.debug("HRMS lookup by email failed for {}, trying userName", email, e);
        }
        try {
            return fieldPlannerService.getUserById(Map.of("RequestInfo", requestInfo), email);
        } catch (Exception ex) {
            log.warn("Assessor lookup failed for email {}", email, ex);
            return null;
        }
    }
}
