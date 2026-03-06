package org.egov.amc.repository.querybuilder;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.egov.amc.config.AMCServiceConfiguration;
import org.egov.amc.web.models.AmcConfigurationSearchCriteria;
import org.egov.amc.web.models.AmcConfigurationSearchRequest;
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
public class AmcConfigurationQueryBuilder {

    private static final String FETCH_AMC_CONFIGURATION_QUERY = "SELECT ac.id AS amc_id, ac.tenant_id AS amc_tenant_id, ac.vendor_id as amc_vendor_id, ac.facility_id as amc_facility_id, ac.project_id as amc_project_id, ac.asset_types as amc_asset_types, ac.duration_months as amc_duration_months, " +
            "ac.visit_frequency_months as amc_visit_frequency_months, ac.configuration_start_date as amc_configuration_start_date, ac.configuration_end_date as amc_configuration_end_date, ac.status AS amc_status, ac.additional_details AS amc_additional_details, ac.created_by AS amc_created_by, " +
            "ac.created_time AS amc_created_time, ac.last_modified_by AS amc_last_modified_by, ac.last_modified_time AS amc_last_modified_time, " +
            "f.id as facility_id, f.facility_name, f.facility_type, f.facility_category, f.facility_subtype, f.facility_ownership, f.facility_region, " +
            "f.facility_details, f.boundary_code, f.is_active AS facility_is_active, " +
            "p.id as project_id, p.name AS project_name, p.description AS project_description, p.projectnumber AS project_number, p.projectsubtype AS project_subtype, " +
            "p.projecttype AS project_type, p.startdate AS project_start_date, p.enddate AS project_end_date, p.status AS project_status, p.additionalDetails as project_additionalDetails, " +
            "COALESCE(" +
            "        jsonb_agg( " +
            "            jsonb_build_object( " +
            "                'id', aca.id, " +
            "                'tenantId', aca.tenant_id, " +
            "                'amcConfigurationId', aca.amc_configuration_id, " +
            "                'assignedUser', aca.assigned_user, " +
            "                'isActive', aca.is_active, " +
            "                'createdBy', aca.created_by, " +
            "                'createdTime', aca.created_time, " +
            "                'lastModifiedBy', aca.last_modified_by, " +
            "                'lastModifiedTime', aca.last_modified_time " +
            "            ) " +
            "        ) FILTER (WHERE aca.id IS NOT NULL), " +
            "        '[]'::jsonb " +
            "    ) AS assignments " +
            " " +
            "FROM amc_configuration AS ac LEFT JOIN facility AS f ON ac.facility_id = f.id LEFT JOIN project AS p ON ac.project_id = p.id LEFT JOIN amc_configuration_assignments aca ON ac.id = aca.amc_configuration_id ";
    private static final String AMC_CONFIGURATION_COUNT_QUERY = "SELECT COUNT(distinct(ac.id)) FROM amc_configuration AS ac LEFT JOIN facility AS f ON ac.facility_id = f.id LEFT JOIN project AS p ON ac.project_id = p.id LEFT JOIN amc_configuration_assignments aca ON ac.id = aca.amc_configuration_id ";

    private final String paginationWrapper = "SELECT * FROM " +
            "(SELECT *, DENSE_RANK() OVER (ORDER BY amc_last_modified_time DESC , amc_id) offset_ FROM " +
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
                log.info("State level tenant");
                queryBuilder.append(" ac.tenant_id like ? ");
                preparedStmtList.add(tenantId + '%');
            } else {
                log.info("City level tenant");
                queryBuilder.append(" ac.tenant_id=? ");
                preparedStmtList.add(tenantId);
            }
        }
    }

    public String getAmcConfigurationSearchQuery(AmcConfigurationSearchCriteria criteria, URLParams urlParams, List<Object> preparedStmtList) {
        //This uses a ternary operator to choose between FIELDPLANS_COUNT_QUERY or FETCH_FIELDPLAN_QUERY based on the value of isCountQuery.
        String query = criteria.isCountQuery() ? AMC_CONFIGURATION_COUNT_QUERY : FETCH_AMC_CONFIGURATION_QUERY;
        StringBuilder queryBuilder = new StringBuilder(query);

        addClause(criteria.getTenantId(), preparedStmtList, queryBuilder);
        extracted(urlParams.getLastChangedSince(), preparedStmtList, criteria, queryBuilder);

        //Add clause if includeDeleted is true in request parameter
//        addIsDeletedCondition(preparedStmtList, queryBuilder, urlParams.getIncludeDeleted());

        if (criteria.isCountQuery()) {
            return queryBuilder.toString();
        }

        String groupBy = " GROUP BY \n" +
                "    ac.id, ac.tenant_id, ac.vendor_id, ac.facility_id, ac.project_id,\n" +
                "    ac.asset_types, ac.duration_months, ac.visit_frequency_months,\n" +
                "    ac.configuration_start_date, ac.configuration_end_date, ac.status,\n" +
                "    ac.additional_details, ac.created_by, ac.created_time,\n" +
                "    ac.last_modified_by, ac.last_modified_time,\n" +
                "    f.id, f.facility_name, f.facility_type, f.facility_category,\n" +
                "    f.facility_subtype, f.facility_ownership, f.facility_region,\n" +
                "    f.facility_details, f.boundary_code, f.is_active,\n" +
                "    p.id, p.name, p.description, p.projectnumber,\n" +
                "    p.projectsubtype, p.projecttype, p.startdate,\n" +
                "    p.enddate, p.status, p.additionalDetails";

        //Add clause if includeDeleted is true in request parameter
//        addIsDeletedCondition(preparedStmtList, queryBuilder, urlParams.getIncludeDeleted());

        queryBuilder.append(groupBy);

        //Wrap constructed SQL query with where criteria in pagination query
        return addPaginationWrapper(queryBuilder.toString(), preparedStmtList, urlParams.getLimit(), urlParams.getOffset());
    }

    private void extracted(Long lastChangedSince, List<Object> preparedStmtList, AmcConfigurationSearchCriteria criteria, StringBuilder queryBuilder) {

        if (!CollectionUtils.isEmpty(criteria.getIds())) {
            addClauseIfRequired(preparedStmtList, queryBuilder);
            queryBuilder.append(" ac.id IN (").append(createQuery(criteria.getIds())).append(")");
            preparedStmtList.addAll(criteria.getIds());
        }

        if (!CollectionUtils.isEmpty(criteria.getProjectIds())) {
            addClauseIfRequired(preparedStmtList, queryBuilder);
            queryBuilder.append(" ac.project_id IN (").append(createQuery(criteria.getProjectIds())).append(")");
            preparedStmtList.addAll(criteria.getProjectIds());
        }

        if (!CollectionUtils.isEmpty(criteria.getFacilityIds())) {
            addClauseIfRequired(preparedStmtList, queryBuilder);
            queryBuilder.append(" ac.facility_id IN (").append(createQuery(criteria.getFacilityIds())).append(")");
            preparedStmtList.addAll(criteria.getFacilityIds());
        }

        if (!CollectionUtils.isEmpty(criteria.getVendorIds())) {
            addClauseIfRequired(preparedStmtList, queryBuilder);
            queryBuilder.append(" ac.vendor_id IN (").append(createQuery(criteria.getVendorIds())).append(")");
            preparedStmtList.addAll(criteria.getVendorIds());
        }

        // Check if workflowStatuses filter is provided
        if (!CollectionUtils.isEmpty(criteria.getStatuses())) {
            addClauseIfRequired(preparedStmtList, queryBuilder);
            queryBuilder.append(" ac.status IN (");
            String placeholders = criteria.getStatuses().stream().map(ws -> "?").collect(Collectors.joining(", "));
            queryBuilder.append(placeholders).append(") ");
            preparedStmtList.addAll(criteria.getStatuses());
        }

        if (criteria.getConfigurationStartDate() != null && criteria.getConfigurationStartDate() != 0) {
            addClauseIfRequired(preparedStmtList, queryBuilder);
            queryBuilder.append(" ac.configuration_start_date >= ? ");
            preparedStmtList.add(criteria.getConfigurationStartDate());
        }

        if (criteria.getConfigurationEndDate() != null && criteria.getConfigurationEndDate() != 0) {
            addClauseIfRequired(preparedStmtList, queryBuilder);
            queryBuilder.append(" ac.configuration_end_date <= ? ");
            preparedStmtList.add(criteria.getConfigurationEndDate());
        }

        if (!CollectionUtils.isEmpty(criteria.getAssignedUsers())) {
            addClauseIfRequired(preparedStmtList, queryBuilder);
            queryBuilder.append(" aca.assigned_user IN (").append(createQuery(criteria.getAssignedUsers())).append(")");
            preparedStmtList.addAll(criteria.getAssignedUsers());
        }

        if (lastChangedSince != null && lastChangedSince != 0) {
            addClauseIfRequired(preparedStmtList, queryBuilder);
            queryBuilder.append(" ( ac.last_modified_time >= ? )");
            preparedStmtList.add(lastChangedSince);
        }
    }

//    private void addIsDeletedCondition(List<Object> preparedStmtList, StringBuilder queryBuilder, Boolean includeDeleted) {
//        if (!includeDeleted) {
//            addClauseIfRequired(preparedStmtList, queryBuilder);
//            queryBuilder.append(" fp.isdeleted = false ");
//        }
//    }

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
    public String getSearchCountQueryString(AmcConfigurationSearchRequest request, String tenantId, Long lastChangedSince, Boolean includeDeleted, List<Object> preparedStatement) {
        AmcConfigurationSearchCriteria criteria = request.getSearchCriteria();
        criteria.setCountQuery(true);
        URLParams urlParams = URLParams.builder().tenantId(tenantId).includeDeleted(includeDeleted).lastChangedSince(lastChangedSince).build();
        return getAmcConfigurationSearchQuery(criteria, urlParams, preparedStatement);
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
