package org.egov.amc.repository.querybuilder;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.egov.amc.config.AMCServiceConfiguration;
import org.egov.amc.web.models.ScheduledVisitSearchCriteria;
import org.egov.amc.web.models.ScheduledVisitSearchRequest;
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
public class ScheduledVisitQueryBuilder {

    private static final String FETCH_SCHEDULED_VISIT_QUERY = "SELECT sv.id AS sv_visit_id, sv.tenant_id AS sv_tenant_id, sv.amc_configuration_id AS sv_amc_configuration_id, sv.facility_id AS sv_facility_id, " +
            "sv.project_id AS sv_project_id, sv.visit_number AS sv_visit_number, sv.scheduled_date AS sv_scheduled_date, sv.actual_visit_date AS sv_actual_visit_date, sv.last_scheduled_visit_date AS sv_last_scheduled_visit_date," +
            " sv.status AS sv_status, sv.visit_report AS sv_visit_report, sv.created_by AS sv_created_by, sv.created_time AS sv_created_time, sv.last_modified_by AS sv_last_modified_by, sv.last_modified_time AS sv_last_modified_time, " +
            "ac.id AS amc_id, ac.tenant_id AS amc_tenant_id, ac.vendor_id as amc_vendor_id, ac.facility_id as amc_facility_id, ac.project_id as amc_project_id, ac.asset_types as amc_asset_types, ac.duration_months as amc_duration_months, " +
            "ac.visit_frequency_months as amc_visit_frequency_months, ac.configuration_start_date as amc_configuration_start_date, ac.configuration_end_date as amc_configuration_end_date, ac.status AS amc_status, ac.additional_details AS amc_additional_details, ac.created_by AS amc_created_by," +
            "ac.created_time AS amc_created_time, ac.last_modified_by AS amc_last_modified_by, ac.last_modified_time AS amc_last_modified_time, " +
            "f.id as facility_id, f.facility_name, f.facility_type, f.facility_category, f.facility_subtype, f.facility_ownership, f.facility_region, f.facility_details, f.boundary_code, f.is_active AS facility_is_active, "+
            "COALESCE(" +
            "        jsonb_agg( " +
            "            jsonb_build_object( " +
            "                'id', sva.id, " +
            "                'tenantId', sva.tenant_id, " +
            "                'scheduledVisitId', sva.scheduled_visit_id, " +
            "                'assignedUser', sva.assigned_user, " +
            "                'isActive', sva.is_active, " +
            "                'createdBy', sva.created_by, " +
            "                'createdTime', sva.created_time, " +
            "                'lastModifiedBy', sva.last_modified_by, " +
            "                'lastModifiedTime', sva.last_modified_time " +
            "            ) " +
            "        ) FILTER (WHERE sva.id IS NOT NULL), " +
            "        '[]'::jsonb " +
            "    ) AS assignments " +
            " " +
            "FROM scheduled_visits sv LEFT JOIN amc_configuration ac ON sv.amc_configuration_id = ac.id LEFT JOIN facility f ON sv.facility_id = f.id LEFT JOIN scheduled_visit_assignments sva ON sv.id = sva.scheduled_visit_id ";
    private static final String SCHEDULED_VISIT_COUNT_QUERY = "SELECT COUNT(*) FROM scheduled_visits sv LEFT JOIN amc_configuration ac ON sv.amc_configuration_id = ac.id LEFT JOIN facility f ON sv.facility_id = f.id ";

    private final String paginationWrapper = "SELECT * FROM " +
            "(SELECT *, DENSE_RANK() OVER (ORDER BY sv_last_modified_time DESC , sv_visit_id) offset_ FROM " +
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
                queryBuilder.append(" sv.tenant_id like ? ");
                preparedStmtList.add(tenantId + '%');
            } else {
                log.debug("Adding city level tenant clause for tenantId: {}", tenantId);
                queryBuilder.append(" sv.tenant_id=? ");
                preparedStmtList.add(tenantId);
            }
        }
    }

    public String getScheduledVisitSearchQuery(ScheduledVisitSearchCriteria criteria, URLParams urlParams, List<Object> preparedStmtList) {
        log.trace("Entering getScheduledVisitSearchQuery method, isCountQuery: {}", criteria.isCountQuery());
        //This uses a ternary operator to choose between SCHEDULED_VISIT_COUNT_QUERY or FETCH_FIELDPLAN_QUERY based on the value of isCountQuery.
        String query = criteria.isCountQuery() ? SCHEDULED_VISIT_COUNT_QUERY : FETCH_SCHEDULED_VISIT_QUERY;
        StringBuilder queryBuilder = new StringBuilder(query);
        log.debug("Building scheduled visit search query, tenantId: {}", criteria.getTenantId());

        addClause(criteria.getTenantId(), preparedStmtList, queryBuilder);
        extracted(urlParams.getLastChangedSince(), preparedStmtList, criteria, queryBuilder);

        if (criteria.isCountQuery()) {
            return queryBuilder.toString();
        }

        String groupBy = " GROUP BY sv.id, sv.tenant_id, sv.amc_configuration_id, sv.facility_id,sv.project_id,  " +
                "    sv.visit_number, sv.scheduled_date, sv.actual_visit_date, sv.status, sv.last_scheduled_visit_date, " +
                "    sv.visit_report, sv.created_by, sv.created_time, sv.last_modified_by, sv.last_modified_time, " +
                "\n" +
                "    ac.id, ac.tenant_id, ac.vendor_id, ac.facility_id, ac.project_id, " +
                "    ac.asset_types, ac.duration_months, ac.visit_frequency_months, " +
                "    ac.configuration_start_date, ac.configuration_end_date, ac.status, " +
                "    ac.additional_details, ac.created_by, ac.created_time, ac.last_modified_by, ac.last_modified_time," +
                "    f.id, f.facility_name, f.facility_type, f.facility_category, " +
                "    f.facility_subtype, f.facility_ownership, f.facility_region, " +
                "    f.facility_details, f.boundary_code, f.is_active";

        //Add clause if includeDeleted is true in request parameter
//        addIsDeletedCondition(preparedStmtList, queryBuilder, urlParams.getIncludeDeleted());

        queryBuilder.append(groupBy);

        //Wrap constructed SQL query with where criteria in pagination query
        return addPaginationWrapper(queryBuilder.toString(), preparedStmtList, urlParams.getLimit(), urlParams.getOffset());
    }

    private void extracted(Long lastChangedSince, List<Object> preparedStmtList, ScheduledVisitSearchCriteria criteria, StringBuilder queryBuilder) {

        if (!CollectionUtils.isEmpty(criteria.getIds())) {
            addClauseIfRequired(preparedStmtList, queryBuilder);
            queryBuilder.append(" sv.id IN (").append(createQuery(criteria.getIds())).append(")");
            preparedStmtList.addAll(criteria.getIds());
        }

        if (!CollectionUtils.isEmpty(criteria.getAmcConfigurationIds())) {
            addClauseIfRequired(preparedStmtList, queryBuilder);
            queryBuilder.append(" sv.amc_configuration_id IN (").append(createQuery(criteria.getAmcConfigurationIds())).append(")");
            preparedStmtList.addAll(criteria.getAmcConfigurationIds());
        }

        if (!CollectionUtils.isEmpty(criteria.getFacilityIds())) {
            addClauseIfRequired(preparedStmtList, queryBuilder);
            queryBuilder.append(" sv.facility_id IN (").append(createQuery(criteria.getFacilityIds())).append(")");
            preparedStmtList.addAll(criteria.getFacilityIds());
        }

        if (!CollectionUtils.isEmpty(criteria.getProjectsIds())) {
            addClauseIfRequired(preparedStmtList, queryBuilder);
            queryBuilder.append(" sv.project_id IN (").append(createQuery(criteria.getProjectsIds())).append(")");
            preparedStmtList.addAll(criteria.getProjectsIds());
        }

        // Check if workflowStatuses filter is provided
        if (!CollectionUtils.isEmpty(criteria.getStatuses())) {
            addClauseIfRequired(preparedStmtList, queryBuilder);
            queryBuilder.append(" sv.status IN (");
            String placeholders = criteria.getStatuses().stream().map(ws -> "?").collect(Collectors.joining(", "));
            queryBuilder.append(placeholders).append(") ");
            preparedStmtList.addAll(criteria.getStatuses());
        }

        if (!CollectionUtils.isEmpty(criteria.getVisitNumbers())) {
            List<String> stringList = criteria.getVisitNumbers().stream()
                    .map(String::valueOf)
                    .toList();
            addClauseIfRequired(preparedStmtList, queryBuilder);
            queryBuilder.append(" sv.visit_number IN (").append(createQuery(stringList)).append(")");
            preparedStmtList.addAll(criteria.getVisitNumbers());
        }

        if (criteria.getScheduledDateFrom() != null && criteria.getScheduledDateFrom() != 0) {
            addClauseIfRequired(preparedStmtList, queryBuilder);
            queryBuilder.append(" sv.scheduled_date >= ? ");
            preparedStmtList.add(criteria.getScheduledDateFrom());
        }

        if (criteria.getScheduledDateTo() != null && criteria.getScheduledDateTo() != 0) {
            addClauseIfRequired(preparedStmtList, queryBuilder);
            queryBuilder.append(" sv.scheduled_date <= ? ");
            preparedStmtList.add(criteria.getScheduledDateTo());
        }

        if (criteria.getActualDateFrom() != null && criteria.getActualDateFrom() != 0) {
            addClauseIfRequired(preparedStmtList, queryBuilder);
            queryBuilder.append(" sv.actual_visit_date >= ? ");
            preparedStmtList.add(criteria.getActualDateFrom());
        }

        if (criteria.getActualDateTo() != null && criteria.getActualDateTo() != 0) {
            addClauseIfRequired(preparedStmtList, queryBuilder);
            queryBuilder.append(" sv.actual_visit_date <= ? ");
            preparedStmtList.add(criteria.getActualDateTo());
        }

        if (!CollectionUtils.isEmpty(criteria.getAssignedUsers())) {
            addClauseIfRequired(preparedStmtList, queryBuilder);
            queryBuilder.append(" sva.assigned_user IN (").append(createQuery(criteria.getAssignedUsers())).append(")");
            preparedStmtList.addAll(criteria.getAssignedUsers());
        }

        if (lastChangedSince != null && lastChangedSince != 0) {
            addClauseIfRequired(preparedStmtList, queryBuilder);
            queryBuilder.append(" ( aa.last_modified_time >= ? )");
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
    public String getSearchCountQueryString(ScheduledVisitSearchRequest request, String tenantId, Long lastChangedSince, Boolean includeDeleted, List<Object> preparedStatement) {
        ScheduledVisitSearchCriteria criteria = request.getSearchCriteria();
        criteria.setCountQuery(true);
        URLParams urlParams = URLParams.builder().tenantId(tenantId).includeDeleted(includeDeleted).lastChangedSince(lastChangedSince).build();
        return getScheduledVisitSearchQuery(criteria, urlParams, preparedStatement);
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
