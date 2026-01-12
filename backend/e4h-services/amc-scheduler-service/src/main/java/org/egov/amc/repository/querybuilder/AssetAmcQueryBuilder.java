package org.egov.amc.repository.querybuilder;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.egov.amc.config.AMCServiceConfiguration;
import org.egov.amc.web.models.AssetAmcSearchCriteria;
import org.egov.amc.web.models.AssetAmcSearchRequest;
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
public class AssetAmcQueryBuilder {

    private static final String FETCH_ASSET_AMC_QUERY = "SELECT aa.id AS asset_amc_id, aa.tenant_id AS asset_amc_tenant_id, aa.asset_id AS asset_amc_asset_id, aa.amc_configuration_id AS asset_amc_configuration_id, aa.amc_start_date, aa.amc_end_date, " +
            "aa.status AS asset_amc_status, aa.is_legacy_asset, aa.additional_details AS asset_amc_additional_details, aa.created_by AS asset_amc_created_by, aa.created_time AS asset_amc_created_time, " +
            "aa.last_modified_by AS asset_amc_last_modified_by, aa.last_modified_time AS asset_amc_last_modified_time, a.asset_id AS asset_id, a.tenant_id AS asset_tenant_id, a.system AS asset_system, " +
            "a.activity_facility_id AS asset_activity_facility_id, a.is_operational AS asset_is_operational, " +
            "a.facility_id AS asset_facility_id, a.asset_type_id AS asset_type_id, a.serial_number, a.model_number, a.brand_id, a.asset_details, a.warranty_start_date, a.warranty_duration, a.warranty_end_date, " +
            "a.wf_status AS asset_wf_status, a.is_active AS asset_is_active, a.additional_details AS asset_additional_details, ac.id AS amc_id, ac.tenant_id AS amc_tenant_id, ac.vendor_id AS amc_vendor_id, " +
            "ac.facility_id AS amc_facility_id, ac.project_id AS amc_project_id, ac.asset_types AS amc_asset_types, ac.duration_months AS amc_duration_months, ac.visit_frequency_months AS amc_visit_frequency_months, " +
            "ac.configuration_start_date AS amc_configuration_start_date, ac.configuration_end_date AS amc_configuration_end_date, ac.status AS amc_status, ac.additional_details AS amc_additional_details, ac.created_by AS amc_created_by, ac.created_time AS amc_created_time, " +
            "ac.last_modified_by AS amc_last_modified_by, ac.last_modified_time AS amc_last_modified_time " +
            " " +
            "FROM asset_amc aa LEFT JOIN asset a ON aa.asset_id = a.asset_id LEFT JOIN amc_configuration ac ON aa.amc_configuration_id = ac.id ";
    private static final String ASSET_AMC_COUNT_QUERY = "SELECT COUNT(*) FROM asset_amc aa LEFT JOIN asset a ON aa.asset_id = a.asset_id LEFT JOIN amc_configuration ac ON aa.amc_configuration_id = ac.id ";

    private final String paginationWrapper = "SELECT * FROM " +
            "(SELECT *, DENSE_RANK() OVER (ORDER BY asset_amc_last_modified_time DESC , asset_amc_id) offset_ FROM " +
            "({})" +
            " result) result_offset " +
            "WHERE offset_ > ? AND offset_ <= ?";

    private final AMCServiceConfiguration config;

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
                log.debug("Adding state level tenant clause for tenantId: {}", tenantId);
                queryBuilder.append(" aa.tenant_id like ? ");
                preparedStmtList.add(tenantId + '%');
            } else {
                log.debug("Adding city level tenant clause for tenantId: {}", tenantId);
                queryBuilder.append(" aa.tenant_id=? ");
                preparedStmtList.add(tenantId);
            }
        }
    }

    public String getAssetAmcSearchQuery(AssetAmcSearchCriteria criteria, URLParams urlParams, List<Object> preparedStmtList) {
        log.trace("Entering getAssetAmcSearchQuery method, isCountQuery: {}", criteria.isCountQuery());
        //This uses a ternary operator to choose between FIELDPLANS_COUNT_QUERY or FETCH_FIELDPLAN_QUERY based on the value of isCountQuery.
        String query = criteria.isCountQuery() ? ASSET_AMC_COUNT_QUERY : FETCH_ASSET_AMC_QUERY;
        StringBuilder queryBuilder = new StringBuilder(query);
        log.debug("Building asset AMC search query, tenantId: {}", criteria.getTenantId());

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

    private void extracted(Long lastChangedSince, List<Object> preparedStmtList, AssetAmcSearchCriteria criteria, StringBuilder queryBuilder) {

        if (!CollectionUtils.isEmpty(criteria.getIds())) {
            addClauseIfRequired(preparedStmtList, queryBuilder);
            queryBuilder.append(" aa.id IN (").append(createQuery(criteria.getIds())).append(")");
            preparedStmtList.addAll(criteria.getIds());
        }

        if (!CollectionUtils.isEmpty(criteria.getAssetIds())) {
            addClauseIfRequired(preparedStmtList, queryBuilder);
            queryBuilder.append(" aa.asset_id IN (").append(createQuery(criteria.getAssetIds())).append(")");
            preparedStmtList.addAll(criteria.getAssetIds());
        }

        if (!CollectionUtils.isEmpty(criteria.getAmcConfigurationIds())) {
            addClauseIfRequired(preparedStmtList, queryBuilder);
            queryBuilder.append(" aa.amc_configuration_id IN (").append(createQuery(criteria.getAmcConfigurationIds())).append(")");
            preparedStmtList.addAll(criteria.getAmcConfigurationIds());
        }

        // Check if workflowStatuses filter is provided
        if (!CollectionUtils.isEmpty(criteria.getStatuses())) {
            addClauseIfRequired(preparedStmtList, queryBuilder);
            queryBuilder.append(" aa.status IN (");
            String placeholders = criteria.getStatuses().stream().map(ws -> "?").collect(Collectors.joining(", "));
            queryBuilder.append(placeholders).append(") ");
            preparedStmtList.addAll(criteria.getStatuses());
        }

        if (criteria.getStartDateFrom() != null && criteria.getStartDateFrom() != 0) {
            addClauseIfRequired(preparedStmtList, queryBuilder);
            queryBuilder.append(" aa.amc_start_date >= ? ");
            preparedStmtList.add(criteria.getStartDateFrom());
        }

        if (criteria.getEndDateTo() != null && criteria.getEndDateTo() != 0) {
            addClauseIfRequired(preparedStmtList, queryBuilder);
            queryBuilder.append(" aa.amc_end_date <= ? ");
            preparedStmtList.add(criteria.getEndDateTo());
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
            queryBuilder.append(" fp.isdeleted = false ");
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
    public String getSearchCountQueryString(AssetAmcSearchRequest request, String tenantId, Long lastChangedSince, Boolean includeDeleted, List<Object> preparedStatement) {
        AssetAmcSearchCriteria criteria = request.getSearchCriteria();
        criteria.setCountQuery(true);
        URLParams urlParams = URLParams.builder().tenantId(tenantId).includeDeleted(includeDeleted).lastChangedSince(lastChangedSince).build();
        return getAssetAmcSearchQuery(criteria, urlParams, preparedStatement);
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
