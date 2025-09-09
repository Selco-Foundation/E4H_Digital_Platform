package org.egov.field_planner.repository.querybuilder;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.egov.common.models.project.Project;
import org.egov.field_planner.config.FieldPlannerConfiguration;
import org.egov.field_planner.web.models.FieldPlan;
import org.egov.field_planner.web.models.FieldPlanSearchCriteria;
import org.springframework.stereotype.Component;

import java.util.List;

import static org.egov.field_planner.util.FieldPlannerConstants.DOT;

@Component
@Slf4j
@RequiredArgsConstructor
public class FieldPlannerQueryBuilder {

//    private static final String FETCH_FIELDPLAN_NAME = "SELECT tenant_id, name FROM field_plans";

    private static final String FETCH_FIELDPLAN_QUERY = "SELECT fp.id as fieldPlanId, fp.tenant_id as fp_tenantId, fp.name as fp_name, fp.project_id as fp_projectId, fp.health_facility_number as fp_healthFacilityNumber, " +
            "fp.geography_scope as fp_geographyScope, fp.selected_activities as fp_selectedActivities, fp.status as fp_status, fp.start_date as fp_startDate, fp.end_date as fp_endDate, " +
            "fp.additional_details as fp_additionalDetails, fp.isdeleted as fp_isDeleted, fp.created_by as fp_createdBy, fp.last_modified_by as fp_lastModifiedBy, fp.created_time as fp_createdTime, " +
            "fp.last_modified_time as fp_lastModifiedTime " +
            " " +
            "from field_plans fp ";
    private static final String FIELDPLAN_COUNT_QUERY = "SELECT COUNT(*) FROM field_plans fp ";

    private final String paginationWrapper = "SELECT * FROM " +
            "(SELECT *, DENSE_RANK() OVER (ORDER BY fp_lastModifiedTime DESC , fieldPlanId) offset_ FROM " +
            "({})" +
            " result) result_offset " +
            "WHERE offset_ > ? AND offset_ <= ?";

    private final FieldPlannerConfiguration config;

    /* Add WHERE clause before first condition, ADD and for subsequent conditions. Do not add AND before any condition and after "(" */
    private static void addClauseIfRequired(List<Object> values, StringBuilder queryString) {
        if (values.isEmpty())
            queryString.append(" WHERE ");
        else if (queryString.toString().lastIndexOf("(") != (queryString.toString().trim().length() - 1)) {
            queryString.append(" AND");
        }
    }

    /* Add conditional clause */
    private static void addConditionalClause(List<Object> values, StringBuilder queryString) {
        if (values.isEmpty())
            queryString.append(" WHERE ");
        else {
            queryString.append(" OR ");
        }
    }

    private static void addClause(String tenantId, List<Object> preparedStmtList, StringBuilder queryBuilder) {
        if (StringUtils.isNotBlank(tenantId)) {
            addClauseIfRequired(preparedStmtList, queryBuilder);
            if (!tenantId.contains(DOT)) {
                log.info("State level tenant");
                queryBuilder.append(" fp.tenant_id like ? ");
                preparedStmtList.add(tenantId + '%');
            } else {
                log.info("City level tenant");
                queryBuilder.append(" fp.tenant_id=? ");
                preparedStmtList.add(tenantId);
            }
        }
    }

    public String getHighestFielPlanNameQuery(FieldPlan fieldPlan, List<Object> preparedStmtList) {
        StringBuilder queryBuilder = new StringBuilder(FETCH_FIELDPLAN_QUERY);
        if (StringUtils.isNotBlank(fieldPlan.getName())) {
            addClauseIfRequired(preparedStmtList, queryBuilder);
            queryBuilder.append(" LOWER(name) LIKE ? ");
            preparedStmtList.add(fieldPlan.getName().toLowerCase() + "%");
        }
        if (StringUtils.isNotBlank(fieldPlan.getTenantId())) {
            addClauseIfRequired(preparedStmtList, queryBuilder);
            log.info("State level tenant");
            queryBuilder.append(" tenant_id like ? ");
            preparedStmtList.add(fieldPlan.getTenantId() + '%');
        }
        queryBuilder.append("ORDER BY created_time DESC LIMIT 1;");

        return queryBuilder.toString();
    }

    public String getFieldPlanSearchQuery(FieldPlanSearchCriteria criteria) {
        //This uses a ternary operator to choose between FIELDPLANS_COUNT_QUERY or FETCH_FIELDPLAN_QUERY based on the value of isCountQuery.
        String query = criteria.isCountQuery() ? FIELDPLAN_COUNT_QUERY : FETCH_FIELDPLAN_QUERY;
        StringBuilder queryBuilder = new StringBuilder(query);

        Integer count = criteria.getFieldPlans().size();

        for (FieldPlan fieldPlan : criteria.getFieldPlans()) {

            addClause(criteria.getTenantId(), criteria.getPreparedStmtList(), queryBuilder);

            /*
             * If isAncestorProjectId is set to true, Then either id equals to project id or projectHierarchy
             *  should have id of the project
             */
            extracted(criteria.getLastChangedSince(), criteria.getPreparedStmtList(), fieldPlan, queryBuilder);

            if (criteria.getCreatedFrom() != null && criteria.getCreatedFrom() != 0) {
                addClauseIfRequired(criteria.getPreparedStmtList(), queryBuilder);
                queryBuilder.append(" fp.created_time >= ? ");
                criteria.getPreparedStmtList().add(criteria.getCreatedFrom());
            }

            if (criteria.getCreatedTo() != null && criteria.getCreatedTo() != 0) {
                addClauseIfRequired(criteria.getPreparedStmtList(), queryBuilder);
                queryBuilder.append(" fp.created_time <= ? ");
                criteria.getPreparedStmtList().add(criteria.getCreatedTo());
            }

            //Add clause if includeDeleted is true in request parameter
            addIsDeletedCondition(criteria.getPreparedStmtList(), queryBuilder, criteria.getIncludeDeleted());

//            queryBuilder.append(" )");
            count--;
            addORClause(count, queryBuilder);
        }

        if (criteria.isCountQuery()) {
            return queryBuilder.toString();
        }

        //Wrap constructed SQL query with where criteria in pagination query
        return addPaginationWrapper(queryBuilder.toString(), criteria.getPreparedStmtList(), criteria.getLimit(), criteria.getOffset());
    }

    private static void extracted(Long lastChangedSince, List<Object> preparedStmtList, FieldPlan fieldPlan, StringBuilder queryBuilder) {

        if (StringUtils.isNotBlank(fieldPlan.getName())) {
            addClauseIfRequired(preparedStmtList, queryBuilder);
            queryBuilder.append(" fp.name LIKE ? ");
            preparedStmtList.add('%' + fieldPlan.getName() + '%');
        }

        if (fieldPlan.getHealthFacilityNumber() != 0) {
            addClauseIfRequired(preparedStmtList, queryBuilder);
            queryBuilder.append(" fp.health_facility_number=? ");
            preparedStmtList.add(fieldPlan.getHealthFacilityNumber());
        }

        if (StringUtils.isNotBlank(fieldPlan.getProjectId())) {
            addClauseIfRequired(preparedStmtList, queryBuilder);
            queryBuilder.append(" fp.project_id =? ");
            preparedStmtList.add(fieldPlan.getProjectId());
        }

//        if (project.getAddress() != null && StringUtils.isNotBlank(project.getAddress().getBoundary())) {
//            addClauseIfRequired(preparedStmtList, queryBuilder);
//            queryBuilder.append(" addr.boundary=? ");
//            preparedStmtList.add(project.getAddress().getBoundary());
//        }

        if (fieldPlan.getStartDate() != null && fieldPlan.getStartDate() != 0) {
            addClauseIfRequired(preparedStmtList, queryBuilder);
            queryBuilder.append(" fp.start_date >= ? ");
            preparedStmtList.add(fieldPlan.getStartDate());
        }

        if (fieldPlan.getEndDate() != null && fieldPlan.getEndDate() != 0) {
            addClauseIfRequired(preparedStmtList, queryBuilder);
            queryBuilder.append(" fp.end_date <= ? ");
            preparedStmtList.add(fieldPlan.getEndDate());
        }

        if (lastChangedSince != null && lastChangedSince != 0) {
            addClauseIfRequired(preparedStmtList, queryBuilder);
            queryBuilder.append(" ( fp.last_modified_by >= ? )");
            preparedStmtList.add(lastChangedSince);
        }
    }

    private void addIsDeletedCondition(List<Object> preparedStmtList, StringBuilder queryBuilder, Boolean includeDeleted) {
        if (!includeDeleted) {
            addClauseIfRequired(preparedStmtList, queryBuilder);
            queryBuilder.append(" fp.isdeleted = false ");
        }
    }

    private void addORClause(Integer count, StringBuilder queryBuilder) {
        if (count > 0) {
            queryBuilder.append(" OR ( ");
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
    public String getSearchCountQueryString(List<FieldPlan> fieldPlans, String tenantId, Long lastChangedSince, Boolean includeDeleted, Long createdFrom, Long createdTo, List<Object> preparedStatement) {
        FieldPlanSearchCriteria criteria = FieldPlanSearchCriteria.builder()
                .fieldPlans(fieldPlans)
                .limit(config.getMaxLimit())
                .offset(config.getDefaultOffset())
                .tenantId(tenantId)
                .includeDeleted(includeDeleted)
                .createdFrom(createdFrom)
                .createdTo(createdTo)
                .lastChangedSince(lastChangedSince)
                .preparedStmtList(preparedStatement)
                .isCountQuery(true)
                .build();

        return getFieldPlanSearchQuery(criteria);
    }

}
