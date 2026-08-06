package org.egov.field_planner.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.egov.common.contract.request.RequestInfo;
import org.egov.field_planner.config.FieldPlannerConfiguration;
import org.egov.field_planner.repository.AssessmentPlanRepository;
import org.egov.field_planner.util.AssessmentConstants;
import org.egov.field_planner.util.AssessmentGeographyHelper;
import org.egov.field_planner.web.models.*;
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
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class AssessmentPlanService {

    private final AssessmentPlanRepository planRepository;
    private final AssessmentProjectService projectService;
    private final FieldPlannerConfiguration configuration;
    private final ServiceRequestRepository serviceRequestRepository;

    @Qualifier("objectMapper")
    private final ObjectMapper mapper;

    @Transactional
    public AssessmentPlan createPlan(AssessmentPlanCreateRequest request) {
        AssessmentPlan plan = request.getPlan();
        RequestInfo requestInfo = request.getRequestInfo();
        validateDateRange(plan);
        validateGeography(plan);
        AssessmentGeographyHelper.syncPlanGeography(plan);
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
        if (request.getRequestInfo() != null && !plans.isEmpty()) {
            enrichAssessors(request.getRequestInfo(), plans);
        }
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
        if (incoming.getGeographyDetails() != null && !incoming.getGeographyDetails().isEmpty()) {
            validateGeography(incoming);
            existing.setGeographyDetails(incoming.getGeographyDetails());
        } else if (StringUtils.isNotBlank(incoming.getState())) {
            existing.setState(incoming.getState());
        }
        AssessmentGeographyHelper.syncPlanGeography(existing);
        if (StringUtils.isNotBlank(incoming.getName())
                && !incoming.getName().equalsIgnoreCase(existing.getName())
                && planRepository.existsByName(existing.getTenantId(), existing.getProjectId(), incoming.getName())) {
            throw new CustomException(AssessmentConstants.ASSESSMENT_PLAN_NAME_DUPLICATE,
                    "Plan name already exists for this project");
        }

        existing.setName(incoming.getName());
        existing.setStartDate(incoming.getStartDate());
        existing.setEndDate(incoming.getEndDate());

        planRepository.updatePlan(existing, request.getRequestInfo().getUserInfo().getUuid());

        if (request.getAssessors() != null && !request.getAssessors().isEmpty()) {
            assignAssessors(request.getRequestInfo(), existing, request.getAssessors());
        }
        existing.setAssessors(getAssessors(request.getRequestInfo(), existing));

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

    private void validateGeography(AssessmentPlan plan) {
        AssessmentGeographyHelper.validateGeography(plan);
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

    private void enrichAssessors(RequestInfo requestInfo, List<AssessmentPlan> plans) {
        Map<String, List<AssessorAssignment>> assessorsByPlanId = fetchAssessorsByPlanIds(requestInfo, plans);
        for (AssessmentPlan plan : plans) {
            plan.setAssessors(assessorsByPlanId.getOrDefault(plan.getId(), Collections.emptyList()));
        }
    }

    private List<AssessorAssignment> getAssessors(RequestInfo requestInfo, AssessmentPlan plan) {
        return fetchAssessorsByPlanIds(requestInfo, List.of(plan))
                .getOrDefault(plan.getId(), Collections.emptyList());
    }

    private Map<String, List<AssessorAssignment>> fetchAssessorsByPlanIds(RequestInfo requestInfo,
                                                                          List<AssessmentPlan> plans) {
        List<String> planIds = plans.stream()
                .map(AssessmentPlan::getId)
                .filter(StringUtils::isNotBlank)
                .distinct()
                .toList();
        if (planIds.isEmpty()) {
            return Collections.emptyMap();
        }

        String tenantId = plans.stream()
                .map(AssessmentPlan::getTenantId)
                .filter(StringUtils::isNotBlank)
                .findFirst()
                .orElse(null);

        try {
            ActivityAssignmentSearchCriteria criteria = ActivityAssignmentSearchCriteria.builder()
                    .fieldPlanId(planIds)
                    .tenantId(tenantId)
                    .build();
            ActivityAssignmentSearchRequest searchRequest = ActivityAssignmentSearchRequest.builder()
                    .criteria(criteria)
                    .requestInfo(requestInfo)
                    .build();
            String url = configuration.getFieldPlanActivityServiceHost()
                    + configuration.getFieldPlanActivitySearchUrl()
                    + "?tenantId=" + tenantId + "&offset=0&limit=" + Math.max(100, planIds.size() * 4);
            Object response = serviceRequestRepository.fetchResult(new StringBuilder(url), searchRequest);
            ActivityAssignmentResponse assignmentResponse = mapper.convertValue(response, ActivityAssignmentResponse.class);
            if (assignmentResponse == null || assignmentResponse.getActivityAssignment() == null) {
                return Collections.emptyMap();
            }
            return assignmentResponse.getActivityAssignment().stream()
                    .filter(assignment -> StringUtils.isNotBlank(assignment.getFieldPlanId()))
                    .collect(Collectors.groupingBy(
                            ActivityAssignment::getFieldPlanId,
                            Collectors.mapping(assignment -> toAssessorAssignment(requestInfo, assignment), Collectors.toList())));
        } catch (Exception e) {
            log.warn("Failed to fetch assessors for plans {}", planIds, e);
            return Collections.emptyMap();
        }
    }

    private AssessorAssignment toAssessorAssignment(RequestInfo requestInfo, ActivityAssignment assignment) {
        String roleCode = assignment.getRole() != null ? (String) assignment.getRole().get("code") : null;
        AssessorAssignment assessor = AssessorAssignment.builder()
                .role(roleCode)
                .userId(assignment.getAssignedTo())
                .pocNumber(assignment.getPocNumber())
                .build();
        if (StringUtils.isNotBlank(assignment.getAssignedTo())) {
            Employee employee = getEmployeeByUserId(requestInfo, assignment.getAssignedTo());
            if (employee != null && employee.getUser() != null) {
                assessor.setEmail(employee.getUser().getEmailId());
            }
        }
        return assessor;
    }

    private void assignAssessors(RequestInfo requestInfo, AssessmentPlan plan, List<AssessorAssignment> assessors) {
        validateAssessorRoles(assessors);
        List<ActivityAssignment> assignments = new ArrayList<>();
        for (AssessorAssignment assessor : assessors) {
            Map<String, Object> role = Map.of(
                    "code", assessor.getRole(),
                    "name", assessor.getRole()
            );
            Employee employee = getEmployeeByUserId(requestInfo, assessor.getUserId());
            String pocNumber = assessor.getPocNumber();
            if (employee != null && employee.getUser() != null) {
                assessor.setEmail(employee.getUser().getEmailId());
                if (StringUtils.isBlank(pocNumber)) {
                    pocNumber = employee.getUser().getMobileNumber();
                }
            }
            assignments.add(ActivityAssignment.builder()
                    .tenantId(plan.getTenantId())
                    .fieldPlanId(plan.getId())
                    .activityId(AssessmentConstants.ACTIVITY_CODE_ASSESSMENT)
                    .activityCode(AssessmentConstants.ACTIVITY_CODE_ASSESSMENT)
                    .assignedTo(assessor.getUserId())
                    .assignedBy(requestInfo.getUserInfo().getUuid())
                    .role(role)
                    .pocNumber(pocNumber)
                    .startDate(plan.getStartDate())
                    .endDate(plan.getEndDate())
                    .status("ACTIVE")
                    .build());
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
            if (StringUtils.isBlank(assessor.getRole()) || StringUtils.isBlank(assessor.getUserId())) {
                throw new CustomException(AssessmentConstants.ASSESSMENT_ASSESSOR_ROLE_REQUIRED,
                        "Both ENUMERATOR and FIELD_POC assessors with userId are required");
            }
            roles.add(assessor.getRole());
        }
        if (!roles.contains(AssessmentConstants.ROLE_ENUMERATOR)
                || !roles.contains(AssessmentConstants.ROLE_FIELD_POC)) {
            throw new CustomException(AssessmentConstants.ASSESSMENT_ASSESSOR_ROLE_REQUIRED,
                    "Both ENUMERATOR and FIELD_POC assessors are required");
        }
    }

    private Employee getEmployeeByUserId(RequestInfo requestInfo, String userId) {
        if (StringUtils.isBlank(userId)) {
            return null;
        }
        try {
            String url = configuration.getHrmsHost() + configuration.getHrmsSearchUrl()
                    + "?tenantId=in&uuids=" + userId;
            Map<String, Object> payload = new HashMap<>();
            payload.put("RequestInfo", requestInfo);
            EmployeeResponse employeeResponse = serviceRequestRepository.fetchResult(
                    new StringBuilder(url), payload, new TypeReference<EmployeeResponse>() {});
            if (employeeResponse == null || employeeResponse.getEmployees() == null
                    || employeeResponse.getEmployees().isEmpty()) {
                log.warn("HRMS returned no employee for userId {}", userId);
                return null;
            }
            return employeeResponse.getEmployees().get(0);
        } catch (Exception e) {
            log.warn("HRMS lookup failed for userId {}: {}", userId, e.getMessage());
            return null;
        }
    }
}
