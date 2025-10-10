package org.egov.activity.repository.querybuilder;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.egov.activity.config.ActivityConfiguration;
import org.egov.activity.web.models.ActivityAssignmentSearchCriteria;
import org.egov.activity.web.models.ActivityAssignmentSearchRequest;
import org.egov.activity.web.models.ActivityFacilitySearchCriteria;
import org.egov.activity.web.models.ActivityFacilitySearchRequest;
import org.egov.common.models.core.URLParams;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.Collection;
import java.util.List;

import static org.egov.activity.util.ActivityConstants.DOT;

@Component
@Slf4j
@RequiredArgsConstructor
public class ActivityAssignmentQueryBuilder {

    private static final String FETCH_ACTIVITY_QUERY = "SELECT aa.id as aa_activityAssignmentId, aa.tenant_id as aa_tenantId, aa.activity_id as aa_activityId, aa.poc_number as aa_pocNumber, " +
            "aa.field_plan_id as aa_fieldPlanId, aa.status as aa_status, aa.assigned_to as aa_assignedTo, aa.assigned_by as aa_assignedBy, aa.emailsent as aa_emailSent, aa.isdeleted as aa_isdeleted,  " +
            "aa.additional_details as aa_additionalDetails, aa.start_date as aa_startDate, aa.end_date as aa_endDate, aa.role as aa_role, aa.created_time as aa_createdTime, " +
            "aa.last_modified_time as aa_lastModifiedTime " +
            " " +
            "from activity_assignments aa ";
    private static final String ACTIVITY_COUNT_QUERY = "SELECT COUNT(*) FROM activity_assignments aa ";

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

    public String getActivityAssignmentSearchQuery(ActivityAssignmentSearchCriteria criteria, URLParams urlParams, List<Object> preparedStmtList) {
        //This uses a ternary operator to choose between FIELDPLANS_COUNT_QUERY or FETCH_FIELDPLAN_QUERY based on the value of isCountQuery.
        String query = criteria.isCountQuery() ? ACTIVITY_COUNT_QUERY : FETCH_ACTIVITY_QUERY;
        StringBuilder queryBuilder = new StringBuilder(query);

        addClause(criteria.getTenantId(), preparedStmtList, queryBuilder);

        extracted(urlParams.getLastChangedSince(), preparedStmtList, criteria, queryBuilder);

        //Add clause if includeDeleted is true in request parameter
        addIsDeletedCondition(preparedStmtList, queryBuilder, urlParams.getIncludeDeleted());

        if (criteria.isCountQuery()) {
            return queryBuilder.toString();
        }

        //Wrap constructed SQL query with where criteria in pagination query
        return addPaginationWrapper(queryBuilder.toString(), preparedStmtList, urlParams.getLimit(), urlParams.getOffset());
    }

    private void extracted(Long lastChangedSince, List<Object> preparedStmtList, ActivityAssignmentSearchCriteria activityAssignment, StringBuilder queryBuilder) {

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

        if (StringUtils.isNotBlank(activityAssignment.getAssignedTo())) {
            addClauseIfRequired(preparedStmtList, queryBuilder);
            queryBuilder.append(" aa.assigned_to =? ");
            preparedStmtList.add(activityAssignment.getAssignedTo());
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
        return getActivityAssignmentSearchQuery(criteria, urlParams, preparedStatement);
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
