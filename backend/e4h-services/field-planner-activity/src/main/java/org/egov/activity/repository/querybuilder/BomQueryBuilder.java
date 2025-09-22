package org.egov.activity.repository.querybuilder;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.egov.activity.config.ActivityConfiguration;
import org.egov.activity.web.models.ActivityFacilitySearchCriteria;
import org.egov.activity.web.models.ActivityFacilitySearchRequest;
import org.egov.activity.web.models.BomSearchCriteria;
import org.egov.activity.web.models.BomSearchRequest;
import org.egov.common.models.core.URLParams;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.Collection;
import java.util.List;

import static org.egov.activity.util.ActivityConstants.DOT;

@Component
@Slf4j
@RequiredArgsConstructor
public class BomQueryBuilder {

    private static final String FETCH_BOM_QUERY = "SELECT bom.id as bom_bomId, bom.tenant_id as bom_tenantId, bom.facility_id as bom_facilityId, " +
            "bom.name as bom_name, bom.data as bom_data, bom.is_active as bom_isActive, bom.assign_user as bom_assignedUser, " +
            "bom.additional_details as bom_additionalDetails, bom.created_time as bom_createdTime, " +
            "bom.last_modified_time as bom_lastModifiedTime " +
            " " +
            "from bom bom ";
    private static final String BOM_COUNT_QUERY = "SELECT COUNT(*) FROM bom bom ";

    private final String paginationWrapper = "SELECT * FROM " +
            "(SELECT *, DENSE_RANK() OVER (ORDER BY bom_lastModifiedTime DESC , bom_bomId) offset_ FROM " +
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
                queryBuilder.append(" bom.tenant_id like ? ");
                preparedStmtList.add(tenantId + '%');
            } else {
                log.info("City level tenant");
                queryBuilder.append(" bom.tenant_id=? ");
                preparedStmtList.add(tenantId);
            }
        }
    }

    public String getBOMSearchQuery(BomSearchCriteria criteria, URLParams urlParams, List<Object> preparedStmtList) {
        //This uses a ternary operator to choose between FIELDPLANS_COUNT_QUERY or FETCH_FIELDPLAN_QUERY based on the value of isCountQuery.
        String query = criteria.isCountQuery() ? BOM_COUNT_QUERY : FETCH_BOM_QUERY;
        StringBuilder queryBuilder = new StringBuilder(query);

        addClause(criteria.getTenantId(), preparedStmtList, queryBuilder);

        extracted(urlParams.getLastChangedSince(), preparedStmtList, criteria, queryBuilder);

        //Add clause if includeDeleted is true in request parameter
//        addIsDeletedCondition(preparedStmtList, queryBuilder, urlParams.getIncludeDeleted());

        if (criteria.isCountQuery()) {
            return queryBuilder.toString();
        }

        //Wrap constructed SQL query with where criteria in pagination query
        return addPaginationWrapper(queryBuilder.toString(), preparedStmtList, urlParams.getLimit(), urlParams.getOffset());
    }

    private void extracted(Long lastChangedSince, List<Object> preparedStmtList, BomSearchCriteria criteria, StringBuilder queryBuilder) {

        if (!CollectionUtils.isEmpty(criteria.getIds())) {
            addClauseIfRequired(preparedStmtList, queryBuilder);
            queryBuilder.append(" bom.id IN (").append(createQuery(criteria.getIds())).append(")");
            preparedStmtList.addAll(criteria.getIds());
        }

        if (!CollectionUtils.isEmpty(criteria.getFacilityId())) {
            addClauseIfRequired(preparedStmtList, queryBuilder);
            queryBuilder.append(" bom.facility_id IN (").append(createQuery(criteria.getFacilityId())).append(")");
            preparedStmtList.addAll(criteria.getFacilityId());
        }

        if (!CollectionUtils.isEmpty(criteria.getName())) {
            addClauseIfRequired(preparedStmtList, queryBuilder);
            queryBuilder.append(" bom.name IN (").append(createQuery(criteria.getName())).append(")");
            preparedStmtList.addAll(criteria.getName());
        }

        if (StringUtils.isNotBlank(criteria.getAssignUser())) {
            addClauseIfRequired(preparedStmtList, queryBuilder);
            queryBuilder.append(" bom.assign_user =? ");
            preparedStmtList.add(criteria.getAssignUser());
        }

        if (lastChangedSince != null && lastChangedSince != 0) {
            addClauseIfRequired(preparedStmtList, queryBuilder);
            queryBuilder.append(" ( bom.last_modified_time >= ? )");
            preparedStmtList.add(lastChangedSince);
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
    public String getSearchCountQueryString(BomSearchRequest request, String tenantId, Long lastChangedSince, Boolean includeDeleted, List<Object> preparedStatement) {
        BomSearchCriteria criteria = request.getCriteria();
        criteria.setCountQuery(true);
        URLParams urlParams = URLParams.builder().tenantId(tenantId).includeDeleted(includeDeleted).lastChangedSince(lastChangedSince).build();
        return getBOMSearchQuery(criteria, urlParams, preparedStatement);
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
