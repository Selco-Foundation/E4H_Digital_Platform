package org.egov.field_planner.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.egov.common.contract.request.RequestInfo;
import org.egov.field_planner.config.FieldPlannerConfiguration;
import org.egov.field_planner.util.AssessmentConstants;
import org.egov.field_planner.web.models.ActivityAssignment;
import org.egov.field_planner.web.models.ActivityAssignmentSearchCriteria;
import org.egov.field_planner.web.models.ActivityAssignmentSearchRequest;
import org.egov.field_planner.web.models.ActivityAssignmentResponse;
import org.egov.field_planner.web.models.PlanFacility;
import org.egov.tracer.model.CustomException;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class AssessmentAssessorService {

    private final FieldPlannerConfiguration configuration;
    private final ServiceRequestRepository serviceRequestRepository;

    @Qualifier("objectMapper")
    private final ObjectMapper mapper;

    public void authorizeAssessor(RequestInfo requestInfo, PlanFacility facility, String requiredRole,
                                   String tenantId) {
        String userId = requestInfo.getUserInfo().getUuid();
        String effectiveTenant = tenantId != null ? tenantId : "in";
        List<ActivityAssignment> assignments = fetchAssignments(requestInfo, facility.getAssessmentPlanId(),
                effectiveTenant);
        boolean authorized = assignments.stream()
                .filter(a -> userId.equals(a.getAssignedTo()))
                .anyMatch(a -> requiredRole.equals(getRoleCode(a)));
        if (!authorized) {
            throw new CustomException(AssessmentConstants.ASSESSMENT_UNAUTHORIZED_ASSESSOR,
                    "User is not authorized as " + requiredRole + " on this plan");
        }
    }

    public List<String> getAssignedPlanIds(RequestInfo requestInfo, String tenantId, String requiredRole) {
        String userId = requestInfo.getUserInfo().getUuid();
        List<ActivityAssignment> assignments = fetchAssignmentsByUser(requestInfo, tenantId, userId);
        return assignments.stream()
                .filter(a -> requiredRole.equals(getRoleCode(a)))
                .map(ActivityAssignment::getFieldPlanId)
                .distinct()
                .collect(Collectors.toList());
    }

    private List<ActivityAssignment> fetchAssignments(RequestInfo requestInfo, String planId, String tenantId) {
        try {
            ActivityAssignmentSearchCriteria criteria = ActivityAssignmentSearchCriteria.builder()
                    .fieldPlanId(List.of(planId))
                    .tenantId(tenantId)
                    .build();
            ActivityAssignmentSearchRequest searchRequest = ActivityAssignmentSearchRequest.builder()
                    .criteria(criteria)
                    .requestInfo(requestInfo)
                    .build();
            String url = configuration.getFieldPlanActivityServiceHost()
                    + configuration.getFieldPlanActivitySearchUrl()
                    + "?tenantId=" + (tenantId != null ? tenantId : "in") + "&offset=0&limit=100";
            Object response = serviceRequestRepository.fetchResult(new StringBuilder(url), searchRequest);
            ActivityAssignmentResponse assignmentResponse = mapper.convertValue(response, ActivityAssignmentResponse.class);
            if (assignmentResponse == null || assignmentResponse.getActivityAssignment() == null) {
                return Collections.emptyList();
            }
            return assignmentResponse.getActivityAssignment();
        } catch (Exception e) {
            log.warn("Failed to fetch activity assignments for plan {}", planId, e);
            return Collections.emptyList();
        }
    }

    private List<ActivityAssignment> fetchAssignmentsByUser(RequestInfo requestInfo, String tenantId, String userId) {
        try {
            ActivityAssignmentSearchCriteria criteria = ActivityAssignmentSearchCriteria.builder()
                    .assignedTo(userId)
                    .tenantId(tenantId)
                    .build();
            ActivityAssignmentSearchRequest searchRequest = ActivityAssignmentSearchRequest.builder()
                    .criteria(criteria)
                    .requestInfo(requestInfo)
                    .build();
            String url = configuration.getFieldPlanActivityServiceHost()
                    + configuration.getFieldPlanActivitySearchUrl()
                    + "?tenantId=" + tenantId + "&offset=0&limit=500";
            Object response = serviceRequestRepository.fetchResult(new StringBuilder(url), searchRequest);
            ActivityAssignmentResponse assignmentResponse = mapper.convertValue(response, ActivityAssignmentResponse.class);
            if (assignmentResponse == null || assignmentResponse.getActivityAssignment() == null) {
                return Collections.emptyList();
            }
            return assignmentResponse.getActivityAssignment();
        } catch (Exception e) {
            log.warn("Failed to fetch activity assignments for user {}", userId, e);
            return Collections.emptyList();
        }
    }

    private String getRoleCode(ActivityAssignment assignment) {
        Map<String, Object> role = assignment.getRole();
        return role != null && role.get("code") != null ? role.get("code").toString() : null;
    }
}
