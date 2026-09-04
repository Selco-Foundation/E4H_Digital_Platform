package org.egov.amc.repository.querybuilder;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.egov.amc.config.AMCServiceConfiguration;
import org.egov.amc.web.models.AmcPlanSearchCriteria;
import org.egov.amc.web.models.AmcPlanSearchRequest;
import org.egov.common.models.core.URLParams;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import static org.egov.amc.util.AmcConstants.DOT;

@Component
@Slf4j
@RequiredArgsConstructor
public class AmcPlanQueryBuilder {

    private static final String FETCH_AMC_PLAN_QUERY = "SELECT ap.id AS ap_id, ap.tenant_id AS ap_tenant_id, ap.name AS ap_name, " +
            "ap.project_id AS ap_project_id, ap.health_facility_number AS ap_health_facility_number, " +
            "ap.start_date AS ap_start_date, ap.end_date AS ap_end_date, ap.geography_scope AS ap_geography_scope, " +
            "ap.selected_activities AS ap_selected_activities, ap.status AS ap_status, ap.isdeleted AS ap_isdeleted, " +
            "ap.additional_details AS ap_additional_details, ap.created_by AS ap_created_by, ap.created_time AS ap_created_time, " +
            "ap.last_modified_by AS ap_last_modified_by, ap.last_modified_time AS ap_last_modified_time " +
            "FROM amc_plans AS ap ";
    private static final String AMC_PLAN_COUNT_QUERY = "SELECT COUNT(ap.id) FROM amc_plans AS ap ";

    private final String paginationWrapper = "SELECT * FROM " +
            "(SELECT *, DENSE_RANK() OVER (ORDER BY ap_last_modified_time DESC , ap_id) offset_ FROM " +
            "({})" +
            " result) result_offset " +
            "WHERE offset_ > ? AND offset_ <= ?";

    private final AMCServiceConfiguration config;

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
                queryBuilder.append(" ap.tenant_id like ? ");
                preparedStmtList.add(tenantId + '%');
            } else {
                queryBuilder.append(" ap.tenant_id=? ");
                preparedStmtList.add(tenantId);
            }
        }
    }

    public String getAmcPlanSearchQuery(AmcPlanSearchCriteria criteria, URLParams urlParams, List<Object> preparedStmtList) {
        log.trace("Entering getAmcPlanSearchQuery method, isCountQuery: {}", criteria.isCountQuery());
        String query = criteria.isCountQuery() ? AMC_PLAN_COUNT_QUERY : FETCH_AMC_PLAN_QUERY;
        StringBuilder queryBuilder = new StringBuilder(query);

        addClause(criteria.getTenantId(), preparedStmtList, queryBuilder);
        addFilters(preparedStmtList, criteria, queryBuilder, urlParams.getLastChangedSince());
        addIsDeletedCondition(queryBuilder, preparedStmtList, urlParams.getIncludeDeleted());

        if (criteria.isCountQuery()) {
            return queryBuilder.toString();
        }

        return addPaginationWrapper(queryBuilder.toString(), preparedStmtList, urlParams.getLimit(), urlParams.getOffset());
    }

    private void addFilters(List<Object> preparedStmtList, AmcPlanSearchCriteria criteria, StringBuilder queryBuilder, Long lastChangedSince) {
        if (!CollectionUtils.isEmpty(criteria.getIds())) {
            addClauseIfRequired(preparedStmtList, queryBuilder);
            queryBuilder.append(" ap.id IN (").append(createQuery(criteria.getIds())).append(")");
            preparedStmtList.addAll(criteria.getIds());
        }

        if (!CollectionUtils.isEmpty(criteria.getProjectIds())) {
            addClauseIfRequired(preparedStmtList, queryBuilder);
            queryBuilder.append(" ap.project_id IN (").append(createQuery(criteria.getProjectIds())).append(")");
            preparedStmtList.addAll(criteria.getProjectIds());
        }

        if (!CollectionUtils.isEmpty(criteria.getStatuses())) {
            addClauseIfRequired(preparedStmtList, queryBuilder);
            queryBuilder.append(" ap.status IN (");
            String placeholders = criteria.getStatuses().stream().map(ws -> "?").collect(Collectors.joining(", "));
            queryBuilder.append(placeholders).append(") ");
            preparedStmtList.addAll(criteria.getStatuses());
        }

        if (lastChangedSince != null && lastChangedSince != 0) {
            addClauseIfRequired(preparedStmtList, queryBuilder);
            queryBuilder.append(" ( ap.last_modified_time >= ? )");
            preparedStmtList.add(lastChangedSince);
        }
    }

    /** Hides soft-deleted plans unless includeDeleted is explicitly requested. */
    private void addIsDeletedCondition(StringBuilder queryBuilder, List<Object> preparedStmtList, Boolean includeDeleted) {
        if (Boolean.TRUE.equals(includeDeleted)) {
            return;
        }
        addClauseIfRequired(preparedStmtList, queryBuilder);
        queryBuilder.append(" (ap.isdeleted IS NOT TRUE) ");
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

    public String getSearchCountQueryString(AmcPlanSearchRequest request, String tenantId, Long lastChangedSince, Boolean includeDeleted, List<Object> preparedStatement) {
        AmcPlanSearchCriteria criteria = request.getSearchCriteria();
        criteria.setCountQuery(true);
        URLParams urlParams = URLParams.builder().tenantId(tenantId).includeDeleted(includeDeleted).lastChangedSince(lastChangedSince).build();
        return getAmcPlanSearchQuery(criteria, urlParams, preparedStatement);
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
