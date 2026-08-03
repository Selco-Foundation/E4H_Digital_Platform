package org.egov.field_planner.repository;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.egov.field_planner.repository.querybuilder.AssessmentQueryBuilder;
import org.egov.field_planner.repository.rowmapper.AssessmentPlanRowMapper;
import org.egov.field_planner.util.AssessmentConstants;
import org.egov.field_planner.web.models.AssessmentPlan;
import org.egov.field_planner.web.models.AssessmentPlanMetrics;
import org.egov.field_planner.web.models.AssessmentPlanSearchCriteria;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Slf4j
@Repository
@RequiredArgsConstructor
public class AssessmentPlanRepository {

    private final JdbcTemplate jdbcTemplate;
    private final AssessmentQueryBuilder queryBuilder;
    private final AssessmentPlanRowMapper planRowMapper;
    private final ObjectMapper objectMapper;

    public void insertPlan(AssessmentPlan plan, String userId) {
        long now = System.currentTimeMillis();
        String geographyScope = toJson(Map.of("state", plan.getState() != null ? plan.getState() : ""));
        String selectedActivities = toJson(List.of(Map.of("code", AssessmentConstants.ACTIVITY_CODE_ASSESSMENT)));

        jdbcTemplate.update(
                """
                INSERT INTO field_plans (
                    id, tenant_id, name, project_id, health_facility_number, start_date, end_date,
                    geography_scope, selected_activities, status, plan_type, created_by, last_modified_by,
                    created_time, last_modified_time, additional_details, isdeleted
                ) VALUES (?, ?, ?, ?, ?, ?, ?, ?::jsonb, ?::jsonb, ?, ?, ?, ?, ?, ?, '{}'::jsonb, false)
                """,
                plan.getId(),
                plan.getTenantId(),
                plan.getName(),
                plan.getProjectId(),
                plan.getHealthFacilityCount() != null ? plan.getHealthFacilityCount() : 0,
                plan.getStartDate(),
                plan.getEndDate(),
                geographyScope,
                selectedActivities,
                plan.getStatus(),
                AssessmentConstants.PLAN_TYPE_ASSESSMENT,
                userId,
                userId,
                now,
                now
        );
    }

    public void updatePlan(AssessmentPlan plan, String userId) {
        long now = System.currentTimeMillis();
        String geographyScope = plan.getState() != null
                ? toJson(Map.of("state", plan.getState()))
                : null;

        if (geographyScope != null) {
            jdbcTemplate.update(
                    """
                    UPDATE field_plans SET name = ?, start_date = ?, end_date = ?, geography_scope = ?::jsonb,
                    last_modified_by = ?, last_modified_time = ? WHERE id = ? AND plan_type = 'ASSESSMENT'
                    """,
                    plan.getName(),
                    plan.getStartDate(),
                    plan.getEndDate(),
                    geographyScope,
                    userId,
                    now,
                    plan.getId()
            );
        } else {
            jdbcTemplate.update(
                    """
                    UPDATE field_plans SET name = ?, start_date = ?, end_date = ?,
                    last_modified_by = ?, last_modified_time = ? WHERE id = ? AND plan_type = 'ASSESSMENT'
                    """,
                    plan.getName(),
                    plan.getStartDate(),
                    plan.getEndDate(),
                    userId,
                    now,
                    plan.getId()
            );
        }
    }

    public void updatePlanStatus(String planId, String status, String userId) {
        jdbcTemplate.update(
                "UPDATE field_plans SET status = ?, last_modified_by = ?, last_modified_time = ? WHERE id = ? AND plan_type = 'ASSESSMENT'",
                status,
                userId,
                System.currentTimeMillis(),
                planId
        );
    }

    public void updateHealthFacilityCount(String planId, int count) {
        jdbcTemplate.update(
                "UPDATE field_plans SET health_facility_number = ?, last_modified_time = ? WHERE id = ?",
                count,
                System.currentTimeMillis(),
                planId
        );
    }

    public Optional<AssessmentPlan> findById(String planId) {
        List<Object> params = new ArrayList<>();
        String query = queryBuilder.getAssessmentPlanSearchQuery(params, null, null, List.of(planId));
        List<AssessmentPlan> plans = jdbcTemplate.query(query, planRowMapper, params.toArray());
        return plans.isEmpty() ? Optional.empty() : Optional.of(plans.get(0));
    }

    public List<AssessmentPlan> search(AssessmentPlanSearchCriteria criteria, int limit, int offset) {
        List<Object> params = new ArrayList<>();
        String baseQuery = queryBuilder.getAssessmentPlanSearchQuery(
                params,
                criteria.getTenantId(),
                criteria.getProjectId(),
                criteria.getIds()
        );
        String paginated = baseQuery + " LIMIT ? OFFSET ?";
        params.add(limit);
        params.add(offset);
        return jdbcTemplate.query(paginated, planRowMapper, params.toArray());
    }

    public int count(AssessmentPlanSearchCriteria criteria) {
        List<Object> params = new ArrayList<>();
        String query = queryBuilder.getAssessmentPlanCountQuery(
                params,
                criteria.getTenantId(),
                criteria.getProjectId(),
                criteria.getIds()
        );
        Integer count = jdbcTemplate.queryForObject(query, Integer.class, params.toArray());
        return count != null ? count : 0;
    }

    public boolean existsByName(String tenantId, String projectId, String name) {
        Integer count = jdbcTemplate.queryForObject(
                """
                SELECT COUNT(*) FROM field_plans
                WHERE tenant_id LIKE ? AND project_id = ? AND LOWER(name) = LOWER(?)
                AND plan_type = 'ASSESSMENT' AND COALESCE(isdeleted, false) = false
                """,
                Integer.class,
                tenantId + '%',
                projectId,
                name
        );
        return count != null && count > 0;
    }

    public AssessmentPlanMetrics getMetrics(String planId) {
        return jdbcTemplate.queryForObject(
                queryBuilder.getPlanMetricsQuery(),
                (rs, rowNum) -> AssessmentPlanMetrics.builder()
                        .remoteAssessmentTotal(rs.getLong("total"))
                        .remoteAssessmentDone(rs.getLong("remote_done"))
                        .onSiteAssessmentDone(rs.getLong("field_done"))
                        .onSiteAssessmentAssigned(rs.getLong("field_assigned"))
                        .eligible(rs.getLong("eligible"))
                        .notEligible(rs.getLong("not_eligible"))
                        .resultPending(rs.getLong("result_pending"))
                        .build(),
                planId,
                AssessmentConstants.ASSESSMENT_ACTIVITY_ID
        );
    }

    public int countFacilitiesOnPlan(String planId) {
        Integer count = jdbcTemplate.queryForObject(
                queryBuilder.getPlanFacilityCountByPlanQuery(),
                Integer.class,
                planId,
                AssessmentConstants.ASSESSMENT_ACTIVITY_ID
        );
        return count != null ? count : 0;
    }

    private String toJson(Object value) {
        try {
            return objectMapper.writeValueAsString(value);
        } catch (Exception e) {
            throw new IllegalStateException("Failed to serialize JSON", e);
        }
    }
}
