package org.egov.activity.repository.querybuilder;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.egov.activity.config.ActivityConfiguration;
import org.egov.activity.web.models.*;
import org.egov.common.models.core.URLParams;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.Collection;
import java.util.List;

import static org.egov.activity.util.ActivityConstants.DOT;
import static org.egov.activity.util.ActivityConstants.PROJECT_MANAGER;

@Component
@Slf4j
@RequiredArgsConstructor
public class ActivityAssignmentQueryBuilder {

    private static final String FETCH_ACTIVITY_FIELD_PLAN = "SELECT aa.id AS aa_activityAssignmentId, aa.tenant_id AS aa_tenantId, aa.field_plan_id AS aa_fieldPlanId, aa.activity_id AS aa_activityId, " +
            "aa.assigned_to AS aa_assignedTo, aa.assigned_by AS aa_assignedBy, aa.status AS aa_status, " +
            "aa.created_time AS aa_createdTime, aa.last_modified_time AS aa_lastModifiedTime, aa.additional_details AS aa_additionalDetails, aa.start_date AS aa_startDate, aa.end_date AS aa_endDate, " +
            "aa.role AS aa_role, aa.emailsent AS aa_emailSent, aa.isdeleted AS aa_isdeleted, aa.poc_number AS aa_pocNumber, fp.id AS field_plan_id_fp, fp.tenant_id AS fp_tenant_id, fp.name AS fp_name, fp.project_id AS fp_project_id, " +
            "fp.health_facility_number AS fp_health_facility_number, fp.geography_scope AS fp_geography_scope, fp.selected_activities AS fp_selected_activities, fp.created_by AS fp_created_by, " +
            "fp.status AS fp_status, fp.isdeleted AS fp_isdeleted, fp.last_modified_by AS fp_last_modified_by, fp.created_time AS fp_created_time, " +
            "fp.last_modified_time AS fp_last_modified_time, fp.additional_details AS fp_additional_details, fp.start_date AS fp_start_date, fp.end_date AS fp_end_date " +
            "FROM public.activity_assignments AS aa LEFT JOIN public.field_plans AS fp ON aa.field_plan_id = fp.id";

    private static final String ACTIVITY_FIELD_PLAN_COUNT_QUERY = "SELECT COUNT(*) FROM public.activity_assignments aa LEFT JOIN field_plans fp ON aa.field_plan_id = fp.id ";

    private final String paginationWrapper = "SELECT * FROM " +
            "(SELECT *, DENSE_RANK() OVER (ORDER BY aa_lastModifiedTime DESC , aa_activityAssignmentId) offset_ FROM " +
            "({})" +
            " result) result_offset " +
            "WHERE offset_ > ? AND offset_ <= ?";

    private final ActivityConfiguration config;

    /* Add WHERE clause before first condition, ADD and for subsequent conditions. Do not add AND before any condition and after "(" */
    private static void addClauseIfRequired(List<Object> values, StringBuilder queryString) {
        if (values.isEmpty())
            queryString.append(" WHERE ");
        else if (queryString.toString().lastIndexOf("(") != (queryString.toString().trim().length() - 1)) {
            queryString.append(" AND");
        }
    }

    private static void addClause(String tenantId, List<Object> preparedStmtList, StringBuilder queryBuilder) {
        if (StringUtils.isNotBlank(tenantId)) {
            addClauseIfRequired(preparedStmtList, queryBuilder);
            if (!tenantId.contains(DOT)) {
                log.info("State level tenant");
                queryBuilder.append(" aa.tenant_id like ? ");
                preparedStmtList.add(tenantId + '%');
            } else {
                log.info("City level tenant");
                queryBuilder.append(" aa.tenant_id=? ");
                preparedStmtList.add(tenantId);
            }
        }
    }

    public String getActivityAssignmentSearchQuery(ActivityAssignmentSearchRequest request, URLParams urlParams, List<Object> preparedStmtList) {
        //This uses a ternary operator to choose between FIELDPLANS_COUNT_QUERY or FETCH_FIELDPLAN_QUERY based on the value of isCountQuery.
        String query = request.getCriteria().isCountQuery() ? ACTIVITY_FIELD_PLAN_COUNT_QUERY : FETCH_ACTIVITY_FIELD_PLAN;
        StringBuilder queryBuilder = new StringBuilder(query);
        ActivityAssignmentSearchCriteria criteria = request.getCriteria();
        boolean isProjectManager = isUserProjectManager(request);
        String userUuid = request.getRequestInfo().getUserInfo().getUuid();

        addClause(criteria.getTenantId(), preparedStmtList, queryBuilder);

        extracted(urlParams.getLastChangedSince(), preparedStmtList, criteria, queryBuilder, userUuid, isProjectManager);

        //Add clause if includeDeleted is true in request parameter
        addIsDeletedCondition(preparedStmtList, queryBuilder, urlParams.getIncludeDeleted());

        if (criteria.isCountQuery()) {
            return queryBuilder.toString();
        }

        //Wrap constructed SQL query with where criteria in pagination query
        return addPaginationWrapper(queryBuilder.toString(), preparedStmtList, urlParams.getLimit(), urlParams.getOffset());
    }

    private boolean isUserProjectManager(ActivityAssignmentSearchRequest request) {
        var userInfo = request.getRequestInfo().getUserInfo();
        if (userInfo.getRoles() == null) {
            return false;
        }
        return userInfo.getRoles().stream().anyMatch(role -> PROJECT_MANAGER.equalsIgnoreCase(role.getCode()));
    }

    private void extracted(Long lastChangedSince, List<Object> preparedStmtList, ActivityAssignmentSearchCriteria activityAssignment, StringBuilder queryBuilder, String userUuid, boolean isProjectManager) {
        addIdBasedFilters(preparedStmtList, activityAssignment, queryBuilder);
        addRoleAndAssigneeFilters(preparedStmtList, activityAssignment, queryBuilder, userUuid, isProjectManager);
        addTimeAndFieldPlanFilters(lastChangedSince, preparedStmtList, activityAssignment, queryBuilder);
    }

    private void addIdBasedFilters(List<Object> preparedStmtList, ActivityAssignmentSearchCriteria activityAssignment, StringBuilder queryBuilder) {
        if (!CollectionUtils.isEmpty(activityAssignment.getIds())) {
            addClauseIfRequired(preparedStmtList, queryBuilder);
            queryBuilder.append(" aa.id IN (").append(createQuery(activityAssignment.getIds())).append(")");
            preparedStmtList.addAll(activityAssignment.getIds());
        }

        if (!CollectionUtils.isEmpty(activityAssignment.getFieldPlanId())) {
            addClauseIfRequired(preparedStmtList, queryBuilder);
            queryBuilder.append(" aa.field_plan_id IN (").append(createQuery(activityAssignment.getFieldPlanId())).append(")");
            preparedStmtList.addAll(activityAssignment.getFieldPlanId());
        }

        if (!CollectionUtils.isEmpty(activityAssignment.getActivityId())) {
            addClauseIfRequired(preparedStmtList, queryBuilder);
            queryBuilder.append(" aa.activity_id IN (").append(createQuery(activityAssignment.getActivityId())).append(")");
            preparedStmtList.addAll(activityAssignment.getActivityId());
        }
    }

    private void addRoleAndAssigneeFilters(List<Object> preparedStmtList, ActivityAssignmentSearchCriteria activityAssignment, StringBuilder queryBuilder, String userUuid, boolean isProjectManager) {
        if (!CollectionUtils.isEmpty(activityAssignment.getRoles())) {
            addClauseIfRequired(preparedStmtList, queryBuilder);
            queryBuilder.append(" aa.role ->> 'code' IN (").append(createQuery(activityAssignment.getRoles())).append(")");
            preparedStmtList.addAll(activityAssignment.getRoles());
        }

        if (StringUtils.isNotBlank(activityAssignment.getAssignedTo())) {
            addClauseIfRequired(preparedStmtList, queryBuilder);
            queryBuilder.append(" aa.assigned_to =? ");
            preparedStmtList.add(activityAssignment.getAssignedTo());
        }
        // Check if not project manager role
        if (!isProjectManager && StringUtils.isNotBlank(userUuid)) {
            addClauseIfRequired(preparedStmtList, queryBuilder);
            queryBuilder.append(" aa.assigned_to = ? ");
            preparedStmtList.add(userUuid);
        }
    }

    private void addTimeAndFieldPlanFilters(Long lastChangedSince, List<Object> preparedStmtList, ActivityAssignmentSearchCriteria activityAssignment, StringBuilder queryBuilder) {
        // Check if fp name is provided
        if (StringUtils.isNotBlank(activityAssignment.getFieldPlanCode())) {
            addClauseIfRequired(preparedStmtList, queryBuilder);
            queryBuilder.append(" LOWER(fp.name) LIKE ? ");
            preparedStmtList.add("%" + activityAssignment.getFieldPlanCode().toLowerCase() + "%");
        }
        if (lastChangedSince != null && lastChangedSince != 0) {
            addClauseIfRequired(preparedStmtList, queryBuilder);
            queryBuilder.append(" ( aa.last_modified_time >= ? )");
            preparedStmtList.add(lastChangedSince);
        }
    }

    private void addIsDeletedCondition(List<Object> preparedStmtList, StringBuilder queryBuilder, Boolean includeDeleted) {
        if (!includeDeleted) {
            addClauseIfRequired(preparedStmtList, queryBuilder);
            queryBuilder.append(" aa.isdeleted = false ");
        }
    }

    private String addPaginationWrapper(String query, List<Object> preparedStmtList, Integer limitParam, Integer offsetParam) {
        int limit = config.getDefaultLimit();
        int offset = config.getDefaultOffset();
        String finalQuery = paginationWrapper.replace("{}", query);

        if (limitParam != null) {
            if (limitParam <= config.getMaxLimit())
                limit = limitParam;
            else
                limit = config.getMaxLimit();
        }

        if (offsetParam != null)
            offset = offsetParam;

        preparedStmtList.add(offset);
        preparedStmtList.add(limit + offset);

        return finalQuery;
    }

    /* Returns query to get total projects count based on project search params */
    public String getSearchCountQueryString(ActivityAssignmentSearchRequest request, String tenantId, Long lastChangedSince, Boolean includeDeleted, List<Object> preparedStatement) {
        ActivityAssignmentSearchCriteria criteria = request.getCriteria();
        criteria.setCountQuery(true);
        URLParams urlParams = URLParams.builder().tenantId(tenantId).includeDeleted(includeDeleted).lastChangedSince(lastChangedSince).build();
        return getActivityAssignmentSearchQuery(request, urlParams, preparedStatement);
    }

    private String createQuery(Collection<String> ids) {
        StringBuilder builder = new StringBuilder();
        int length = ids.size();
        for (int i = 0; i < length; i++) {
            builder.append(" ? ");
            if (i != length - 1) builder.append(",");
        }
        return builder.toString();
    }

}
