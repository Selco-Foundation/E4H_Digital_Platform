package org.egov.field_planner.repository.querybuilder;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.egov.field_planner.util.AssessmentConstants;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.stream.Collectors;

@Component
@Slf4j
public class AssessmentQueryBuilder {

    private static final String ASSESSMENT_ACTIVITY_JOIN =
            " INNER JOIN activities act ON act.id = fa.activity_id "
                    + "AND act.code = ? AND act.is_active = true ";

    public static final String ASSESSMENT_PLAN_SELECT =
            "SELECT fp.id, fp.tenant_id, fp.name, fp.project_id, fp.health_facility_number, "
                    + "fp.start_date, fp.end_date, fp.geography_scope, fp.selected_activities, fp.status, "
                    + "fp.plan_type, fp.additional_details, fp.created_by, fp.last_modified_by, "
                    + "fp.created_time, fp.last_modified_time "
                    + "FROM field_plans fp WHERE fp.plan_type = 'ASSESSMENT' ";

    public static final String ASSESSMENT_PLAN_COUNT =
            "SELECT COUNT(*) FROM field_plans fp WHERE fp.plan_type = 'ASSESSMENT' ";

    public static final String ASSESSMENT_FACILITY_SELECT =
            "SELECT fa.id, fa.tenant_id, fa.facility_id, fa.field_plan_id, fa.activity_id, "
                    + "fa.phone_status, fa.field_status, fa.overall_status, fa.assessment_completion_status, "
                    + "fa.installation_field_plan_id, fa.field_plan_facility_id, fa.additional_details, "
                    + "fa.last_modified_time, fp.project_id, fp.status AS plan_status "
                    + "FROM facility_activities fa "
                    + ASSESSMENT_ACTIVITY_JOIN
                    + "JOIN field_plans fp ON fp.id = fa.field_plan_id "
                    + "WHERE fp.plan_type = 'ASSESSMENT' ";

    public String getAssessmentPlanSearchQuery(List<Object> params, String tenantId, String projectId, List<String> ids) {
        StringBuilder query = new StringBuilder(ASSESSMENT_PLAN_SELECT);
        appendTenantFilter(params, query, tenantId);
        if (StringUtils.isNotBlank(projectId)) {
            query.append(" AND fp.project_id = ? ");
            params.add(projectId);
        }
        if (!CollectionUtils.isEmpty(ids)) {
            query.append(" AND fp.id IN (").append(String.join(",", ids.stream().map(id -> "?").toList())).append(") ");
            params.addAll(ids);
        }
        query.append(" AND COALESCE(fp.isdeleted, false) = false ");
        query.append(" ORDER BY fp.last_modified_time DESC ");
        return query.toString();
    }

    public String getAssessmentPlanCountQuery(List<Object> params, String tenantId, String projectId, List<String> ids) {
        StringBuilder query = new StringBuilder(ASSESSMENT_PLAN_COUNT);
        appendTenantFilter(params, query, tenantId);
        if (StringUtils.isNotBlank(projectId)) {
            query.append(" AND fp.project_id = ? ");
            params.add(projectId);
        }
        if (!CollectionUtils.isEmpty(ids)) {
            query.append(" AND fp.id IN (").append(String.join(",", ids.stream().map(id -> "?").toList())).append(") ");
            params.addAll(ids);
        }
        query.append(" AND COALESCE(fp.isdeleted, false) = false ");
        return query.toString();
    }

    private void appendTenantFilter(List<Object> params, StringBuilder query, String tenantId) {
        if (StringUtils.isNotBlank(tenantId)) {
            if (!tenantId.contains(".")) {
                query.append(" AND fp.tenant_id LIKE ? ");
                params.add(tenantId + '%');
            } else {
                query.append(" AND fp.tenant_id = ? ");
                params.add(tenantId);
            }
        }
    }

    public String getAssessmentFacilitiesByPlanQuery(List<Object> params, String planId) {
        StringBuilder query = new StringBuilder(ASSESSMENT_FACILITY_SELECT);
        params.add(AssessmentConstants.ACTIVITY_CODE_ASSESSMENT);
        query.append(" AND fa.field_plan_id = ? ");
        params.add(planId);
        return query.toString();
    }

    public String getAssessmentFacilitiesByFacilityIdsQuery(List<Object> params, List<String> facilityIds) {
        StringBuilder query = new StringBuilder(ASSESSMENT_FACILITY_SELECT);
        params.add(AssessmentConstants.ACTIVITY_CODE_ASSESSMENT);
        query.append(" AND fa.facility_id IN (")
                .append(String.join(",", facilityIds.stream().map(id -> "?").toList()))
                .append(") ");
        params.addAll(facilityIds);
        return query.toString();
    }

    public String getPlanFacilityCountByPlanQuery(List<Object> params, String planId) {
        params.add(AssessmentConstants.ACTIVITY_CODE_ASSESSMENT);
        params.add(planId);
        return """
                SELECT COUNT(*) FROM facility_activities fa
                INNER JOIN activities act ON act.id = fa.activity_id
                    AND act.code = ? AND act.is_active = true
                WHERE fa.field_plan_id = ?
                """;
    }

    public String getPlanMetricsQuery(List<Object> params, String planId) {
        params.add(AssessmentConstants.ACTIVITY_CODE_ASSESSMENT);
        params.add(planId);
        return """
                SELECT
                    COUNT(*) AS total,
                    COUNT(*) FILTER (WHERE phone_status IN ('QUALIFIED', 'NOT_QUALIFIED', 'PENDING_NO_ANSWER', 'PENDING_WRONG_NUMBER')) AS remote_done,
                    COUNT(*) FILTER (WHERE field_status IN ('QUALIFIED', 'NOT_QUALIFIED')) AS field_done,
                    COUNT(*) FILTER (WHERE field_status IS NOT NULL AND field_status <> 'PENDING') AS field_assigned,
                    COUNT(*) FILTER (WHERE overall_status = 'ELIGIBLE') AS eligible,
                    COUNT(*) FILTER (WHERE overall_status = 'NOT_ELIGIBLE') AS not_eligible,
                    COUNT(*) FILTER (WHERE overall_status = 'PENDING') AS result_pending
                FROM facility_activities fa
                INNER JOIN activities act ON act.id = fa.activity_id
                    AND act.code = ? AND act.is_active = true
                WHERE fa.field_plan_id = ?
                """;
    }

    public String getPendingOverallCountQuery(List<Object> params, String facilityId) {
        params.add(AssessmentConstants.ACTIVITY_CODE_ASSESSMENT);
        params.add(facilityId);
        return """
                SELECT COUNT(*) FROM facility_activities fa
                INNER JOIN activities act ON act.id = fa.activity_id
                    AND act.code = ? AND act.is_active = true
                JOIN field_plans fp ON fp.id = fa.field_plan_id
                WHERE fa.facility_id = ?
                AND fa.overall_status = 'PENDING' AND fp.plan_type = 'ASSESSMENT'
                """;
    }

    public String getNonClosedSourcePlansQuery(List<Object> params, String facilityId, String targetPlanId) {
        params.add(AssessmentConstants.ACTIVITY_CODE_ASSESSMENT);
        params.add(facilityId);
        params.add(targetPlanId);
        return """
                SELECT DISTINCT fp.id, fp.name, fp.status
                FROM facility_activities fa
                INNER JOIN activities act ON act.id = fa.activity_id
                    AND act.code = ? AND act.is_active = true
                JOIN field_plans fp ON fp.id = fa.field_plan_id
                WHERE fa.facility_id = ?
                AND fp.plan_type = 'ASSESSMENT' AND fp.status <> 'CLOSED'
                AND fa.field_plan_id <> ?
                """;
    }

    public String getSameProjectEligibleActiveQuery(List<Object> params, String facilityId,
                                                     String projectId, String targetPlanId) {
        params.add(AssessmentConstants.ACTIVITY_CODE_ASSESSMENT);
        params.add(facilityId);
        params.add(projectId);
        params.add(targetPlanId);
        return """
                SELECT fa.id FROM facility_activities fa
                INNER JOIN activities act ON act.id = fa.activity_id
                    AND act.code = ? AND act.is_active = true
                JOIN field_plans fp ON fp.id = fa.field_plan_id
                WHERE fa.facility_id = ?
                AND fp.project_id = ? AND fp.plan_type = 'ASSESSMENT'
                AND fa.overall_status = 'ELIGIBLE'
                AND fa.assessment_completion_status = 'ELIGIBLE'
                AND fa.field_plan_id <> ?
                LIMIT 1
                """;
    }

    public String getExistingOnPlanQuery(List<Object> params, String planId, String facilityId) {
        params.add(AssessmentConstants.ACTIVITY_CODE_ASSESSMENT);
        params.add(planId);
        params.add(facilityId);
        return """
                SELECT fa.id FROM facility_activities fa
                INNER JOIN activities act ON act.id = fa.activity_id
                    AND act.code = ? AND act.is_active = true
                WHERE fa.field_plan_id = ? AND fa.facility_id = ?
                LIMIT 1
                """;
    }

    public String getPlanFacilityByIdQuery(List<Object> params, String planFacilityId) {
        params.add(AssessmentConstants.ACTIVITY_CODE_ASSESSMENT);
        params.add(planFacilityId);
        return ASSESSMENT_FACILITY_SELECT + " AND fa.id = ? ";
    }

    public String getPlanFacilitySearchQuery(List<Object> params, String planId,
                                              org.egov.field_planner.web.models.PlanFacilityFilters filters) {
        StringBuilder query = new StringBuilder(ASSESSMENT_FACILITY_SELECT);
        params.add(AssessmentConstants.ACTIVITY_CODE_ASSESSMENT);
        query.append(" AND fa.field_plan_id = ? ");
        params.add(planId);
        appendFacilityFilters(params, query, filters);
        query.append(" ORDER BY fa.last_modified_time DESC ");
        return query.toString();
    }

    public String getPlanFacilityCountQuery(List<Object> params, String planId,
                                             org.egov.field_planner.web.models.PlanFacilityFilters filters) {
        StringBuilder query = new StringBuilder(
                """
                SELECT COUNT(*) FROM facility_activities fa
                INNER JOIN activities act ON act.id = fa.activity_id
                    AND act.code = ? AND act.is_active = true
                WHERE fa.field_plan_id = ?
                """);
        params.add(AssessmentConstants.ACTIVITY_CODE_ASSESSMENT);
        params.add(planId);
        appendFacilityFilters(params, query, filters);
        return query.toString();
    }

    public String getSubmissionQueueQuery(List<Object> params, List<String> planIds, String assessmentPhase) {
        StringBuilder query = new StringBuilder(ASSESSMENT_FACILITY_SELECT);
        params.add(AssessmentConstants.ACTIVITY_CODE_ASSESSMENT);
        query.append(" AND fa.field_plan_id IN (")
                .append(String.join(",", planIds.stream().map(id -> "?").toList()))
                .append(") ");
        params.addAll(planIds);
        if ("PHONE".equals(assessmentPhase)) {
            query.append(" AND fa.phone_status IN ('PENDING', 'PENDING_NO_ANSWER', 'PENDING_WRONG_NUMBER') ");
        } else {
            query.append(" AND fa.field_status = 'PENDING' ");
        }
        query.append(" ORDER BY fa.last_modified_time ASC ");
        return query.toString();
    }

    public String getEligibleFacilitiesQuery(List<Object> params, String projectId,
                                              List<String> assessmentPlanIds) {
        StringBuilder query = new StringBuilder(
                """
                SELECT fa.id, fa.tenant_id, fa.facility_id, fa.field_plan_id, fa.activity_id,
                       fa.phone_status, fa.field_status, fa.overall_status, fa.assessment_completion_status,
                       fa.installation_field_plan_id, fa.field_plan_facility_id, fa.additional_details,
                       fa.last_modified_time, fp.project_id AS project_id, fp.status AS plan_status, fp.name AS plan_name
                FROM facility_activities fa
                INNER JOIN activities act ON act.id = fa.activity_id
                    AND act.code = ? AND act.is_active = true
                JOIN field_plans fp ON fp.id = fa.field_plan_id
                WHERE fp.plan_type = 'ASSESSMENT'
                AND fp.project_id = ? AND fp.status = 'CLOSED'
                AND fa.assessment_completion_status = 'ELIGIBLE'
                AND fa.installation_field_plan_id IS NULL
                """);
        params.add(AssessmentConstants.ACTIVITY_CODE_ASSESSMENT);
        params.add(projectId);
        if (!CollectionUtils.isEmpty(assessmentPlanIds)) {
            query.append(" AND fa.field_plan_id IN (")
                    .append(String.join(",", assessmentPlanIds.stream().map(id -> "?").toList()))
                    .append(") ");
            params.addAll(assessmentPlanIds);
        }
        query.append(" ORDER BY fp.name ASC, fa.last_modified_time ASC ");
        return query.toString();
    }

    private void appendFacilityFilters(List<Object> params, StringBuilder query,
                                       org.egov.field_planner.web.models.PlanFacilityFilters filters) {
        if (filters == null) {
            return;
        }
        appendColumnInFilter(params, query, "fa.phone_status", filters.getResolvedPhoneStatuses());
        appendFieldStatusFilter(params, query, filters);
        appendColumnInFilter(params, query, "fa.overall_status", filters.getResolvedOverallStatuses());
        appendJsonFieldInFilter(params, query, "district", filters.getResolvedDistricts());
        appendJsonFieldInFilter(params, query, "block", filters.getResolvedBlocks());
        appendJsonFieldInFilter(params, query, "facilityCategory", filters.getResolvedFacilityCategories());
        appendJsonFieldInFilter(params, query, "facilityType", filters.getResolvedFacilityTypes());
    }

    private void appendFieldStatusFilter(List<Object> params, StringBuilder query,
                                         org.egov.field_planner.web.models.PlanFacilityFilters filters) {
        boolean includeNotInitiated = filters.includesNotInitiatedFieldStatus();
        List<String> concreteStatuses = filters.getResolvedConcreteFieldStatuses();
        if (!includeNotInitiated && concreteStatuses.isEmpty()) {
            return;
        }
        if (includeNotInitiated && concreteStatuses.isEmpty()) {
            query.append(" AND fa.field_status IS NULL ");
            return;
        }
        if (includeNotInitiated) {
            query.append(" AND (fa.field_status IS NULL OR fa.field_status IN (")
                    .append(placeholders(concreteStatuses.size()))
                    .append(")) ");
            params.addAll(concreteStatuses);
            return;
        }
        appendColumnInFilter(params, query, "fa.field_status", concreteStatuses);
    }

    private void appendColumnInFilter(List<Object> params, StringBuilder query, String column, List<String> values) {
        if (CollectionUtils.isEmpty(values)) {
            return;
        }
        if (values.size() == 1) {
            query.append(" AND ").append(column).append(" = ? ");
            params.add(values.get(0));
            return;
        }
        query.append(" AND ").append(column).append(" IN (")
                .append(placeholders(values.size()))
                .append(") ");
        params.addAll(values);
    }

    private void appendJsonFieldInFilter(List<Object> params, StringBuilder query, String jsonKey, List<String> values) {
        if (CollectionUtils.isEmpty(values)) {
            return;
        }
        String expression = "fa.additional_details ->> '" + jsonKey + "'";
        if (values.size() == 1) {
            query.append(" AND ").append(expression).append(" = ? ");
            params.add(values.get(0));
            return;
        }
        query.append(" AND ").append(expression).append(" IN (")
                .append(placeholders(values.size()))
                .append(") ");
        params.addAll(values);
    }

    private String placeholders(int count) {
        return java.util.stream.IntStream.range(0, count)
                .mapToObj(i -> "?")
                .collect(Collectors.joining(","));
    }
}
