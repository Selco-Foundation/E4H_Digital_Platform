package org.egov.field_planner.repository;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.egov.field_planner.repository.querybuilder.AssessmentQueryBuilder;
import org.egov.field_planner.repository.rowmapper.AssessmentFacilityRowMapper;
import org.egov.field_planner.repository.rowmapper.AssessmentQueueFacilityRowMapper;
import org.egov.field_planner.repository.rowmapper.EligibleFacilityRowMapper;
import org.egov.field_planner.util.AssessmentConstants;
import org.egov.tracer.model.CustomException;
import org.egov.field_planner.web.models.EligibleFacility;
import org.egov.field_planner.web.models.PlanFacility;
import org.egov.field_planner.web.models.PlanFacilityFilters;
import org.egov.field_planner.web.models.PlanFacilityIncludeItem;
import org.egov.field_planner.web.models.SubmissionQueueFilters;
import org.egov.field_planner.web.models.SubmissionQueueSort;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Repository
@RequiredArgsConstructor
public class AssessmentFacilityRepository {

    private final JdbcTemplate jdbcTemplate;
    private final AssessmentQueryBuilder queryBuilder;
    private final AssessmentFacilityRowMapper facilityRowMapper;
    private final AssessmentQueueFacilityRowMapper queueFacilityRowMapper;
    private final EligibleFacilityRowMapper eligibleFacilityRowMapper;
    private final ObjectMapper objectMapper;

    public PlanFacility insertFacility(String planId, String tenantId, String facilityId,
                                         PlanFacilityIncludeItem metadata, String userId) {
        String id = UUID.randomUUID().toString();
        long now = System.currentTimeMillis();
        String activityId = resolveAssessmentActivityId(tenantId);
        Map<String, Object> additionalDetails = buildAdditionalDetails(metadata);
        additionalDetails = org.egov.field_planner.util.AssessmentAdditionalDetailsHelper.appendAuditEvent(
                additionalDetails,
                org.egov.field_planner.web.models.AssessmentAuditEvent.builder()
                        .event(AssessmentConstants.AUDIT_INCLUDED)
                        .timestamp(now)
                        .actor(userId)
                        .assessmentPlanId(planId)
                        .build()
        );
        String additionalDetailsJson = toJson(additionalDetails);

        jdbcTemplate.update(
                """
                INSERT INTO facility_activities (
                    id, tenant_id, facility_id, activity_id, field_plan_id, status,
                    phone_status, field_status, overall_status, assessment_completion_status,
                    scheduled_at, activated_at, created_time, last_modified_time, additional_details
                ) VALUES (?, ?, ?, ?, ?, 'SCHEDULED', ?, ?, ?, ?, ?, ?, ?, ?, ?::jsonb)
                """,
                id,
                tenantId,
                facilityId,
                activityId,
                planId,
                AssessmentConstants.PHONE_PENDING,
                null,
                AssessmentConstants.OVERALL_PENDING,
                AssessmentConstants.COMPLETION_ENROLLED,
                now,
                now,
                now,
                now,
                additionalDetailsJson
        );

        return findById(id).orElseThrow();
    }

    public void deleteById(String planFacilityId) {
        jdbcTemplate.update("DELETE FROM facility_activities WHERE id = ?", planFacilityId);
    }

    public Optional<PlanFacility> findById(String planFacilityId) {
        List<Object> params = new ArrayList<>();
        String query = queryBuilder.getPlanFacilityByIdQuery(params, planFacilityId);
        List<PlanFacility> facilities = jdbcTemplate.query(query, facilityRowMapper, params.toArray());
        return facilities.isEmpty() ? Optional.empty() : Optional.of(facilities.get(0));
    }

    public List<PlanFacility> searchByPlan(String planId, PlanFacilityFilters filters, int limit, int offset) {
        List<Object> params = new ArrayList<>();
        String query = queryBuilder.getPlanFacilitySearchQuery(params, planId, filters);
        query += " LIMIT ? OFFSET ? ";
        params.add(limit);
        params.add(offset);
        return jdbcTemplate.query(query, facilityRowMapper, params.toArray());
    }

    public int countByPlan(String planId, PlanFacilityFilters filters) {
        List<Object> params = new ArrayList<>();
        String query = queryBuilder.getPlanFacilityCountQuery(params, planId, filters);
        Integer count = jdbcTemplate.queryForObject(query, Integer.class, params.toArray());
        return count != null ? count : 0;
    }

    public List<PlanFacility> findQueueFacilities(List<String> planIds, String assessmentPhase,
                                                  SubmissionQueueFilters filters, SubmissionQueueSort sort,
                                                  int limit, int offset) {
        if (planIds == null || planIds.isEmpty()) {
            return List.of();
        }
        List<Object> params = new ArrayList<>();
        String query = queryBuilder.getSubmissionQueueQuery(params, planIds, assessmentPhase, filters, sort);
        query += " LIMIT ? OFFSET ? ";
        params.add(limit);
        params.add(offset);
        return jdbcTemplate.query(query, queueFacilityRowMapper, params.toArray());
    }

    public int countQueueFacilities(List<String> planIds, String assessmentPhase, SubmissionQueueFilters filters) {
        if (planIds == null || planIds.isEmpty()) {
            return 0;
        }
        List<Object> params = new ArrayList<>();
        String query = queryBuilder.getSubmissionQueueCountQuery(params, planIds, assessmentPhase, filters);
        Integer count = jdbcTemplate.queryForObject(query, Integer.class, params.toArray());
        return count != null ? count : 0;
    }

    public List<EligibleFacility> findEligibleFacilities(String projectId, List<String> assessmentPlanIds) {
        List<Object> params = new ArrayList<>();
        String query = queryBuilder.getEligibleFacilitiesQuery(params, projectId, assessmentPlanIds);
        return jdbcTemplate.query(query, eligibleFacilityRowMapper, params.toArray());
    }

    public void updateFacilityStatuses(String planFacilityId, String phoneStatus, String fieldStatus,
                                        String overallStatus, String completionStatus,
                                        Map<String, Object> additionalDetails, String userId) {
        long now = System.currentTimeMillis();
        jdbcTemplate.update(
                """
                UPDATE facility_activities
                SET phone_status = COALESCE(?, phone_status),
                    field_status = ?,
                    overall_status = COALESCE(?, overall_status),
                    assessment_completion_status = COALESCE(?, assessment_completion_status),
                    additional_details = ?::jsonb,
                    last_modified_by = ?,
                    last_modified_time = ?
                WHERE id = ?
                """,
                phoneStatus,
                fieldStatus,
                overallStatus,
                completionStatus,
                toJson(additionalDetails),
                userId,
                now,
                planFacilityId
        );
    }

    public void applyHandoff(String planFacilityId, String installationFieldPlanId,
                              String fieldPlanFacilityId, Map<String, Object> additionalDetails, String userId) {
        long now = System.currentTimeMillis();
        jdbcTemplate.update(
                """
                UPDATE facility_activities
                SET assessment_completion_status = ?,
                    installation_field_plan_id = ?,
                    field_plan_facility_id = ?,
                    additional_details = ?::jsonb,
                    last_modified_by = ?,
                    last_modified_time = ?
                WHERE id = ?
                """,
                AssessmentConstants.COMPLETION_MOVED_TO_FIELD_PLAN,
                installationFieldPlanId,
                fieldPlanFacilityId,
                toJson(additionalDetails),
                userId,
                now,
                planFacilityId
        );
    }

    public List<PlanFacility> findByPlanId(String planId) {
        List<Object> params = new ArrayList<>();
        String query = queryBuilder.getAssessmentFacilitiesByPlanQuery(params, planId);
        return jdbcTemplate.query(query, facilityRowMapper, params.toArray());
    }

    public Optional<String> findExistingOnPlan(String planId, String facilityId) {
        List<Object> params = new ArrayList<>();
        List<String> ids = jdbcTemplate.query(
                queryBuilder.getExistingOnPlanQuery(params, planId, facilityId),
                (rs, rowNum) -> rs.getString("id"),
                params.toArray()
        );
        return ids.isEmpty() ? Optional.empty() : Optional.of(ids.get(0));
    }

    public int countPendingOverallForFacility(String facilityId) {
        List<Object> params = new ArrayList<>();
        Integer count = jdbcTemplate.queryForObject(
                queryBuilder.getPendingOverallCountQuery(params, facilityId),
                Integer.class,
                params.toArray()
        );
        return count != null ? count : 0;
    }

    public List<Map<String, Object>> findNonClosedSourcePlans(String facilityId, String targetPlanId) {
        List<Object> params = new ArrayList<>();
        return jdbcTemplate.queryForList(
                queryBuilder.getNonClosedSourcePlansQuery(params, facilityId, targetPlanId),
                params.toArray()
        );
    }

    public boolean hasSameProjectEligibleActive(String facilityId, String projectId, String targetPlanId) {
        List<Object> params = new ArrayList<>();
        List<String> ids = jdbcTemplate.query(
                queryBuilder.getSameProjectEligibleActiveQuery(params, facilityId, projectId, targetPlanId),
                (rs, rowNum) -> rs.getString("id"),
                params.toArray()
        );
        return !ids.isEmpty();
    }

    private String resolveAssessmentActivityId(String tenantId) {
        String resolvedTenantId = tenantId.contains(".") ? tenantId.substring(0, tenantId.indexOf('.')) : tenantId;
        List<String> ids = jdbcTemplate.query(
                """
                SELECT id FROM activities
                WHERE tenant_id = ? AND code = ? AND is_active = true
                LIMIT 1
                """,
                (rs, rowNum) -> rs.getString("id"),
                resolvedTenantId,
                AssessmentConstants.ACTIVITY_CODE_ASSESSMENT
        );
        if (ids.isEmpty()) {
            throw new CustomException(
                    AssessmentConstants.ASSESSMENT_ACTIVITY_NOT_FOUND,
                    "Activity not found for tenant " + tenantId + " and code "
                            + AssessmentConstants.ACTIVITY_CODE_ASSESSMENT);
        }
        return ids.get(0);
    }

    private Map<String, Object> buildAdditionalDetails(PlanFacilityIncludeItem metadata) {
        if (metadata == null) {
            return Map.of();
        }
        // facilityCategory, facilityType, district, and block are stored as codes (not display names).
        java.util.HashMap<String, Object> details = new java.util.HashMap<>();
        putIfPresent(details, "facilityName", metadata.getFacilityName());
        putIfPresent(details, "facilityCategory", metadata.getFacilityCategory());
        putIfPresent(details, "facilityType", metadata.getFacilityType());
        putIfPresent(details, "district", metadata.getDistrict());
        putIfPresent(details, "block", metadata.getBlock());
        return details;
    }

    private void putIfPresent(java.util.HashMap<String, Object> details, String key, String value) {
        if (value != null) {
            details.put(key, value);
        }
    }

    private String toJson(Object value) {
        try {
            return objectMapper.writeValueAsString(value);
        } catch (Exception e) {
            throw new IllegalStateException("Failed to serialize JSON", e);
        }
    }
}
