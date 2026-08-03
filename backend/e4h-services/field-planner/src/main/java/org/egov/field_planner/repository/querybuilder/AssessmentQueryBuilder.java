package org.egov.field_planner.repository.querybuilder;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.List;

@Component
@Slf4j
public class AssessmentQueryBuilder {

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
                    + "JOIN field_plans fp ON fp.id = fa.field_plan_id "
                    + "WHERE fa.activity_id = ? ";

    private static void addClauseIfRequired(List<Object> values, StringBuilder query) {
        if (values.isEmpty()) {
            query.append(" AND ");
        } else {
            query.append(" AND ");
        }
    }

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
        params.add(AssessmentConstantsHolder.ACTIVITY_ID);
        query.append(" AND fa.field_plan_id = ? ");
        params.add(planId);
        return query.toString();
    }

    public String getAssessmentFacilitiesByFacilityIdsQuery(List<Object> params, List<String> facilityIds) {
        StringBuilder query = new StringBuilder(ASSESSMENT_FACILITY_SELECT);
        params.add(AssessmentConstantsHolder.ACTIVITY_ID);
        query.append(" AND fa.facility_id IN (")
                .append(String.join(",", facilityIds.stream().map(id -> "?").toList()))
                .append(") ");
        params.addAll(facilityIds);
        return query.toString();
    }

    public String getPlanFacilityCountByPlanQuery() {
        return "SELECT COUNT(*) FROM facility_activities WHERE field_plan_id = ? AND activity_id = ?";
    }

    public String getPlanMetricsQuery() {
        return """
                SELECT
                    COUNT(*) AS total,
                    COUNT(*) FILTER (WHERE phone_status IN ('QUALIFIED', 'NOT_QUALIFIED', 'PENDING_NO_ANSWER', 'PENDING_WRONG_NUMBER')) AS remote_done,
                    COUNT(*) FILTER (WHERE field_status IN ('QUALIFIED', 'NOT_QUALIFIED')) AS field_done,
                    COUNT(*) FILTER (WHERE field_status IS NOT NULL AND field_status <> 'PENDING') AS field_assigned,
                    COUNT(*) FILTER (WHERE overall_status = 'ELIGIBLE') AS eligible,
                    COUNT(*) FILTER (WHERE overall_status = 'NOT_ELIGIBLE') AS not_eligible,
                    COUNT(*) FILTER (WHERE overall_status = 'PENDING') AS result_pending
                FROM facility_activities
                WHERE field_plan_id = ? AND activity_id = ?
                """;
    }

    public String getPendingOverallCountQuery() {
        return """
                SELECT COUNT(*) FROM facility_activities fa
                JOIN field_plans fp ON fp.id = fa.field_plan_id
                WHERE fa.facility_id = ? AND fa.activity_id = ?
                AND fa.overall_status = 'PENDING' AND fp.plan_type = 'ASSESSMENT'
                """;
    }

    public String getNonClosedSourcePlansQuery() {
        return """
                SELECT DISTINCT fp.id, fp.name, fp.status
                FROM facility_activities fa
                JOIN field_plans fp ON fp.id = fa.field_plan_id
                WHERE fa.facility_id = ? AND fa.activity_id = ?
                AND fp.plan_type = 'ASSESSMENT' AND fp.status <> 'CLOSED'
                AND fa.field_plan_id <> ?
                """;
    }

    public String getSameProjectEligibleActiveQuery() {
        return """
                SELECT fa.id FROM facility_activities fa
                JOIN field_plans fp ON fp.id = fa.field_plan_id
                WHERE fa.facility_id = ? AND fa.activity_id = ?
                AND fp.project_id = ? AND fp.plan_type = 'ASSESSMENT'
                AND fa.overall_status = 'ELIGIBLE'
                AND fa.assessment_completion_status = 'ELIGIBLE'
                AND fa.field_plan_id <> ?
                LIMIT 1
                """;
    }

    public String getExistingOnPlanQuery() {
        return """
                SELECT id FROM facility_activities
                WHERE field_plan_id = ? AND facility_id = ? AND activity_id = ?
                LIMIT 1
                """;
    }

    public String getPlanFacilityByIdQuery() {
        return ASSESSMENT_FACILITY_SELECT + " AND fa.id = ? ";
    }

    public String getPlanFacilitySearchQuery(List<Object> params, String planId,
                                              org.egov.field_planner.web.models.PlanFacilityFilters filters) {
        StringBuilder query = new StringBuilder(ASSESSMENT_FACILITY_SELECT);
        params.add(AssessmentConstantsHolder.ACTIVITY_ID);
        query.append(" AND fa.field_plan_id = ? ");
        params.add(planId);
        appendFacilityFilters(params, query, filters);
        query.append(" ORDER BY fa.last_modified_time DESC ");
        return query.toString();
    }

    public String getPlanFacilityCountQuery(List<Object> params, String planId,
                                             org.egov.field_planner.web.models.PlanFacilityFilters filters) {
        StringBuilder query = new StringBuilder(
                "SELECT COUNT(*) FROM facility_activities fa WHERE fa.activity_id = ? AND fa.field_plan_id = ? ");
        params.add(AssessmentConstantsHolder.ACTIVITY_ID);
        params.add(planId);
        appendFacilityFilters(params, query, filters);
        return query.toString();
    }

    public String getSubmissionQueueQuery(List<Object> params, List<String> planIds, String assessmentPhase) {
        StringBuilder query = new StringBuilder(ASSESSMENT_FACILITY_SELECT);
        params.add(AssessmentConstantsHolder.ACTIVITY_ID);
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
                JOIN field_plans fp ON fp.id = fa.field_plan_id
                WHERE fa.activity_id = ? AND fp.plan_type = 'ASSESSMENT'
                AND fp.project_id = ? AND fp.status = 'CLOSED'
                AND fa.assessment_completion_status = 'ELIGIBLE'
                AND fa.installation_field_plan_id IS NULL
                """);
        params.add(AssessmentConstantsHolder.ACTIVITY_ID);
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
        if (StringUtils.isNotBlank(filters.getPhoneStatus())) {
            query.append(" AND fa.phone_status = ? ");
            params.add(filters.getPhoneStatus());
        }
        if (StringUtils.isNotBlank(filters.getFieldStatus())) {
            if ("NULL".equalsIgnoreCase(filters.getFieldStatus())) {
                query.append(" AND fa.field_status IS NULL ");
            } else {
                query.append(" AND fa.field_status = ? ");
                params.add(filters.getFieldStatus());
            }
        }
        if (StringUtils.isNotBlank(filters.getOverallStatus())) {
            query.append(" AND fa.overall_status = ? ");
            params.add(filters.getOverallStatus());
        }
        if (StringUtils.isNotBlank(filters.getDistrict())) {
            query.append(" AND fa.additional_details ->> 'district' = ? ");
            params.add(filters.getDistrict());
        }
        if (StringUtils.isNotBlank(filters.getFacilityCategory())) {
            query.append(" AND fa.additional_details ->> 'facilityCategory' = ? ");
            params.add(filters.getFacilityCategory());
        }
        if (StringUtils.isNotBlank(filters.getFacilityType())) {
            query.append(" AND fa.additional_details ->> 'facilityType' = ? ");
            params.add(filters.getFacilityType());
        }
    }

    /** Holds activity id to avoid circular dependency on constants in static SQL helpers. */
    static final class AssessmentConstantsHolder {
        static final String ACTIVITY_ID = "00000000-0000-4000-8000-000000000001";
    }
}
