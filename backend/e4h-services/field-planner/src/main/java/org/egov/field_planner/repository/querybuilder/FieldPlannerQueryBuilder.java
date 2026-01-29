package org.egov.field_planner.repository.querybuilder;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.egov.common.models.core.URLParams;
import org.egov.field_planner.config.FieldPlannerConfiguration;
import org.egov.field_planner.web.models.FieldPlan;
import org.egov.field_planner.web.models.FieldPlanSearchCriteria;
import org.egov.field_planner.web.models.FieldPlanSearchRequest;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import static org.egov.field_planner.util.FieldPlannerConstants.DOT;

@Component
@Slf4j
@RequiredArgsConstructor
public class FieldPlannerQueryBuilder {

//    private static final String FETCH_FIELDPLAN_NAME = "SELECT tenant_id, name FROM field_plans";

    private static final String FETCH_FIELDPLAN_QUERY = "SELECT fp.id as fieldPlanId, fp.tenant_id as fp_tenantId, fp.name as fp_name, fp.project_id as fp_projectId, fp.health_facility_number as fp_healthFacilityNumber, " +
            "fp.geography_scope as fp_geographyScope, fp.selected_activities as fp_selectedActivities, fp.status as fp_status, fp.start_date as fp_startDate, fp.end_date as fp_endDate, " +
            "fp.additional_details as fp_additionalDetails, fp.isdeleted as fp_isDeleted, fp.created_by as fp_createdBy, fp.last_modified_by as fp_lastModifiedBy, fp.created_time as fp_createdTime, " +
            "fp.last_modified_time as fp_lastModifiedTime, prj.id as projectId, prj.tenantId as project_tenantId, prj.projectNumber as project_projectNumber, prj.name as project_name, prj.projectType as project_projectType, " +
            "prj.projectTypeId as project_projectTypeId, prj.projectSubType as project_projectSubtype, " +
            "prj.department as project_department, prj.description as project_description, prj.referenceId as project_referenceId, prj.startDate as project_startDate, prj.endDate as project_endDate, " +
            "prj.isTaskEnabled as project_isTaskEnabled, prj.parent as project_parent, prj.projectHierarchy as project_projectHierarchy, prj.natureOfWork as project_natureOfWork, prj.additionalDetails as project_additionalDetails, " +
            "prj.isDeleted as project_isDeleted, prj.rowVersion as project_rowVersion, prj.createdBy as project_createdBy, prj.lastModifiedBy as project_lastModifiedBy, prj.createdTime as project_createdTime, " +
            "prj.lastModifiedTime as project_lastModifiedTime " +
            " " +
            "from field_plans fp LEFT JOIN project prj ON prj.id = fp.project_id ";
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
        log.trace("Entering addClause method for tenant: {}", tenantId);
        if (StringUtils.isNotBlank(tenantId)) {
            addClauseIfRequired(preparedStmtList, queryBuilder);
            if (!tenantId.contains(DOT)) {
                log.debug("State level tenant: {}", tenantId);
                queryBuilder.append(" fp.tenant_id like ? ");
                preparedStmtList.add(tenantId + '%');
            } else {
                log.debug("City level tenant: {}", tenantId);
                queryBuilder.append(" fp.tenant_id=? ");
                preparedStmtList.add(tenantId);
            }
        }
        log.trace("Exiting addClause method");
    }

    public String getHighestFielPlanNameQuery(FieldPlan fieldPlan, List<Object> preparedStmtList) {
        log.trace("Entering getHighestFielPlanNameQuery method");
        log.debug("Building query for highest field plan name, tenant: {}", fieldPlan.getTenantId());
        
        StringBuilder queryBuilder = new StringBuilder(FETCH_FIELDPLAN_QUERY);
        if (StringUtils.isNotBlank(fieldPlan.getName())) {
            addClauseIfRequired(preparedStmtList, queryBuilder);
            queryBuilder.append(" LOWER(fp.name) LIKE ? ");
            preparedStmtList.add(fieldPlan.getName().toLowerCase() + "%");
            log.debug("Added name filter to query");
        }
        if (StringUtils.isNotBlank(fieldPlan.getTenantId())) {
            addClauseIfRequired(preparedStmtList, queryBuilder);
            log.debug("State level tenant");
            queryBuilder.append(" tenant_id like ? ");
            preparedStmtList.add(fieldPlan.getTenantId() + '%');
        }
        queryBuilder.append("ORDER BY created_time DESC LIMIT 1;");

        log.trace("Exiting getHighestFielPlanNameQuery method");
        return queryBuilder.toString();
    }

    public String getFieldPlanSearchQuery(FieldPlanSearchCriteria criteria, URLParams urlParams, List<Object> preparedStmtList) {
        log.trace("Entering getFieldPlanSearchQuery method");
        log.debug("Building field plan search query, isCountQuery: {}", criteria.isCountQuery());
        
        //This uses a ternary operator to choose between FIELDPLANS_COUNT_QUERY or FETCH_FIELDPLAN_QUERY based on the value of isCountQuery.
        String query = criteria.isCountQuery() ? FIELDPLAN_COUNT_QUERY : FETCH_FIELDPLAN_QUERY;
        StringBuilder queryBuilder = new StringBuilder(query);

        addClause(criteria.getTenantId(), preparedStmtList, queryBuilder);
        extracted(urlParams.getLastChangedSince(), preparedStmtList, criteria, queryBuilder);

//        if (criteria.getFromDate() != null && criteria.getFromDate() != 0) {
//            addClauseIfRequired(criteria.getPreparedStmtList(), queryBuilder);
//            queryBuilder.append(" fp.created_time >= ? ");
//            criteria.getPreparedStmtList().add(criteria.getFromDate());
//        }
//
//        if (criteria.getToDate() != null && criteria.getToDate() != 0) {
//            addClauseIfRequired(criteria.getPreparedStmtList(), queryBuilder);
//            queryBuilder.append(" fp.created_time <= ? ");
//            criteria.getPreparedStmtList().add(criteria.getToDate());
//        }

        //Add clause if includeDeleted is true in request parameter
        addIsDeletedCondition(preparedStmtList, queryBuilder, urlParams.getIncludeDeleted());

        if (criteria.isCountQuery()) {
            log.debug("Returning count query");
            log.trace("Exiting getFieldPlanSearchQuery method");
            return queryBuilder.toString();
        }

        //Wrap constructed SQL query with where criteria in pagination query
        log.debug("Wrapping query with pagination, limit: {}, offset: {}", urlParams.getLimit(), urlParams.getOffset());
        log.trace("Exiting getFieldPlanSearchQuery method");
        return addPaginationWrapper(queryBuilder.toString(), preparedStmtList, urlParams.getLimit(), urlParams.getOffset());
    }

    private void extracted(Long lastChangedSince, List<Object> preparedStmtList, FieldPlanSearchCriteria fieldPlan, StringBuilder queryBuilder) {
        log.trace("Entering extracted method for query building");
        if (!CollectionUtils.isEmpty(fieldPlan.getIds())) {
            addClauseIfRequired(preparedStmtList, queryBuilder);
            queryBuilder.append(" fp.id IN (").append(createQuery(fieldPlan.getIds())).append(")");
            preparedStmtList.addAll(fieldPlan.getIds());
        }

        if (!CollectionUtils.isEmpty(fieldPlan.getProjectId())) {
            addClauseIfRequired(preparedStmtList, queryBuilder);
            queryBuilder.append(" fp.project_id IN (").append(createQuery(fieldPlan.getProjectId())).append(")");
            preparedStmtList.addAll(fieldPlan.getProjectId());
        }

        // Check if workflowStatuses filter is provided
        if (!CollectionUtils.isEmpty(fieldPlan.getStatuses())) {
            addClauseIfRequired(preparedStmtList, queryBuilder);
            queryBuilder.append(" fp.status IN (");
            String placeholders = fieldPlan.getStatuses().stream().map(ws -> "?").collect(Collectors.joining(", "));
            queryBuilder.append(placeholders).append(") ");
            preparedStmtList.addAll(fieldPlan.getStatuses());
        }

        if (fieldPlan.getFromDate() != null && fieldPlan.getFromDate() != 0) {
            addClauseIfRequired(preparedStmtList, queryBuilder);
            queryBuilder.append(" fp.start_date >= ? ");
            preparedStmtList.add(fieldPlan.getFromDate());
        }

        if (fieldPlan.getToDate() != null && fieldPlan.getToDate() != 0) {
            addClauseIfRequired(preparedStmtList, queryBuilder);
            queryBuilder.append(" fp.end_date <= ? ");
            preparedStmtList.add(fieldPlan.getToDate());
        }

        if (lastChangedSince != null && lastChangedSince != 0) {
            addClauseIfRequired(preparedStmtList, queryBuilder);
            queryBuilder.append(" ( fp.last_modified_by >= ? )");
            preparedStmtList.add(lastChangedSince);
        }
        log.trace("Exiting extracted method");
    }

    private void addIsDeletedCondition(List<Object> preparedStmtList, StringBuilder queryBuilder, Boolean includeDeleted) {
        log.trace("Entering addIsDeletedCondition method, includeDeleted: {}", includeDeleted);
        if (!includeDeleted) {
            addClauseIfRequired(preparedStmtList, queryBuilder);
            queryBuilder.append(" fp.isdeleted = false ");
            log.debug("Added isDeleted condition to query");
        }
        log.trace("Exiting addIsDeletedCondition method");
    }

    private String addPaginationWrapper(String query, List<Object> preparedStmtList, Integer limitParam, Integer offsetParam) {
        log.trace("Entering addPaginationWrapper method");
        log.debug("Adding pagination wrapper, limitParam: {}, offsetParam: {}", limitParam, offsetParam);
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
        log.debug("Pagination wrapper added, final limit: {}, offset: {}", limit, offset);
        log.trace("Exiting addPaginationWrapper method");
        return finalQuery;
    }

    /* Returns query to get total projects count based on project search params */
    public String getSearchCountQueryString(FieldPlanSearchRequest request, String tenantId, Long lastChangedSince, Boolean includeDeleted, List<Object> preparedStatement) {
        log.trace("Entering getSearchCountQueryString method");
        log.debug("Building count query for tenant: {}", tenantId);
        FieldPlanSearchCriteria criteria = request.getFieldPlan();
        criteria.setCountQuery(true);
        URLParams urlParams = URLParams.builder().tenantId(tenantId).includeDeleted(includeDeleted).lastChangedSince(lastChangedSince).build();
        String result = getFieldPlanSearchQuery(criteria, urlParams, preparedStatement);
        log.trace("Exiting getSearchCountQueryString method");
        return result;
    }

    private String createQuery(Collection<String> ids) {
        log.trace("Entering createQuery method for {} IDs", ids.size());
        StringBuilder builder = new StringBuilder();
        int length = ids.size();
        for (int i = 0; i < length; i++) {
            builder.append(" ? ");
            if (i != length - 1) builder.append(",");
        }
        log.trace("Exiting createQuery method");
        return builder.toString();
    }

}
