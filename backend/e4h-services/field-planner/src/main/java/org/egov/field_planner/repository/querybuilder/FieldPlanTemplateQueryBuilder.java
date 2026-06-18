package org.egov.field_planner.repository.querybuilder;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.egov.common.models.core.URLParams;
import org.egov.field_planner.config.FieldPlannerConfiguration;
import org.egov.field_planner.web.models.FieldPlanTemplateSearchCriteria;
import org.egov.field_planner.web.models.FieldPlanTemplateSearchRequest;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.Collection;
import java.util.List;

import static org.egov.field_planner.util.FieldPlannerConstants.DOT;

@Component
@Slf4j
@RequiredArgsConstructor
public class FieldPlanTemplateQueryBuilder {

    private static final String FETCH_TEMPLATE_QUERY =
            "SELECT fpt.id AS fpt_id, fpt.tenant_id AS fpt_tenantId, fpt.field_plan_id AS fpt_fieldPlanId, "
                    + "fpt.system_type AS fpt_systemType, fpt.total_capacity AS fpt_totalCapacity, "
                    + "fpt.template_data AS fpt_templateData, fpt.created_by AS fpt_createdBy, "
                    + "fpt.last_modified_by AS fpt_lastModifiedBy, fpt.created_time AS fpt_createdTime, "
                    + "fpt.last_modified_time AS fpt_lastModifiedTime "
                    + "FROM field_plan_template fpt ";

    private static final String TEMPLATE_COUNT_QUERY = "SELECT COUNT(*) FROM field_plan_template fpt ";

    private final String paginationWrapper = "SELECT * FROM "
            + "(SELECT *, DENSE_RANK() OVER (ORDER BY fpt_lastModifiedTime DESC, fpt_id) offset_ FROM "
            + "({})"
            + " result) result_offset "
            + "WHERE offset_ > ? AND offset_ <= ?";

    private final FieldPlannerConfiguration config;

    private static void addClauseIfRequired(List<Object> values, StringBuilder queryString) {
        if (values.isEmpty()) {
            queryString.append(" WHERE ");
        } else if (queryString.toString().lastIndexOf("(") != (queryString.toString().trim().length() - 1)) {
            queryString.append(" AND");
        }
    }

    private static void addClause(String tenantId, List<Object> preparedStmtList, StringBuilder queryBuilder) {
        if (StringUtils.isNotBlank(tenantId)) {
            addClauseIfRequired(preparedStmtList, queryBuilder);
            if (!tenantId.contains(DOT)) {
                queryBuilder.append(" fpt.tenant_id LIKE ? ");
                preparedStmtList.add(tenantId + '%');
            } else {
                queryBuilder.append(" fpt.tenant_id = ? ");
                preparedStmtList.add(tenantId);
            }
        }
    }

    public String getTemplateSearchQuery(FieldPlanTemplateSearchCriteria criteria, URLParams urlParams, List<Object> preparedStmtList) {
        String query = criteria.isCountQuery() ? TEMPLATE_COUNT_QUERY : FETCH_TEMPLATE_QUERY;
        StringBuilder queryBuilder = new StringBuilder(query);

        addClause(criteria.getTenantId(), preparedStmtList, queryBuilder);
        addSearchFilters(preparedStmtList, criteria, queryBuilder, urlParams.getLastChangedSince());

        if (criteria.isCountQuery()) {
            return queryBuilder.toString();
        }

        return addPaginationWrapper(queryBuilder.toString(), preparedStmtList, urlParams.getLimit(), urlParams.getOffset());
    }

    public String getSearchCountQueryString(
            FieldPlanTemplateSearchRequest request,
            String tenantId,
            Long lastChangedSince,
            List<Object> preparedStatement) {
        FieldPlanTemplateSearchCriteria criteria = request.getCriteria();
        criteria.setCountQuery(true);
        criteria.setTenantId(tenantId);
        URLParams urlParams = URLParams.builder().lastChangedSince(lastChangedSince).build();
        return getTemplateSearchQuery(criteria, urlParams, preparedStatement);
    }

    private void addSearchFilters(
            List<Object> preparedStmtList,
            FieldPlanTemplateSearchCriteria criteria,
            StringBuilder queryBuilder,
            Long lastChangedSince) {

        if (!CollectionUtils.isEmpty(criteria.getIds())) {
            addClauseIfRequired(preparedStmtList, queryBuilder);
            queryBuilder.append(" fpt.id IN (").append(createQuery(criteria.getIds())).append(")");
            preparedStmtList.addAll(criteria.getIds());
        }

        if (!CollectionUtils.isEmpty(criteria.getFieldPlanId())) {
            addClauseIfRequired(preparedStmtList, queryBuilder);
            queryBuilder.append(" fpt.field_plan_id IN (").append(createQuery(criteria.getFieldPlanId())).append(")");
            preparedStmtList.addAll(criteria.getFieldPlanId());
        }

        if (!CollectionUtils.isEmpty(criteria.getSystemType())) {
            addClauseIfRequired(preparedStmtList, queryBuilder);
            queryBuilder.append(" fpt.system_type IN (").append(createQuery(criteria.getSystemType())).append(")");
            preparedStmtList.addAll(criteria.getSystemType());
        }

        if (!CollectionUtils.isEmpty(criteria.getTotalCapacity())) {
            addClauseIfRequired(preparedStmtList, queryBuilder);
            queryBuilder.append(" fpt.total_capacity IN (").append(createQuery(criteria.getTotalCapacity())).append(")");
            preparedStmtList.addAll(criteria.getTotalCapacity());
        }

        if (lastChangedSince != null) {
            addClauseIfRequired(preparedStmtList, queryBuilder);
            queryBuilder.append(" fpt.last_modified_time >= ? ");
            preparedStmtList.add(lastChangedSince);
        }
    }

    private String addPaginationWrapper(String query, List<Object> preparedStmtList, Integer limit, Integer offset) {
        int effectiveLimit = limit == null ? config.getDefaultLimit() : limit;
        int effectiveOffset = offset == null ? config.getDefaultOffset() : offset;
        preparedStmtList.add(effectiveOffset);
        preparedStmtList.add(effectiveOffset + effectiveLimit);
        return paginationWrapper.replace("{}", query);
    }

    private String createQuery(Collection<String> ids) {
        return String.join(",", ids.stream().map(id -> "?").toList());
    }
}
