package org.egov.activity.repository.querybuilder;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.egov.activity.config.ActivityConfiguration;
import org.egov.activity.web.models.ActivityFacilitySearchCriteria;
import org.egov.activity.web.models.ActivityFacilitySearchRequest;
import org.egov.activity.web.models.ActivitySearchCriteria;
import org.egov.common.models.core.URLParams;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.Collection;
import java.util.List;

import static org.egov.activity.util.ActivityConstants.*;

@Component
@Slf4j
@RequiredArgsConstructor
public class ActivityQueryBuilder {

    private static final String FETCH_ACTIVITY_DATA_NAME = "SELECT act.id, act.name, act.code, act.sequence_order, act.tenant_id FROM activities act ";

    private static final String FETCH_ACTIVITY_QUERY = "SELECT fa.id as fa_facilityActivityId, fa.tenant_id as fa_tenantId, fa.facility_id as fa_facilityId, fa.activity_id as fa_activityId, " +
            "fa.field_plan_id as fa_fieldPlanId, fa.status as fa_status, fa.conditions_met as fa_conditionsMet, fa.assigned_user as fa_assignedUser, " +
            "fa.additional_details as fa_additionalDetails, fa.scheduled_at as fa_scheduledAt, fa.activated_at as fa_activatedAt, fa.completed_at as fa_completedAt, fa.created_time as fa_createdTime, " +
            "fa.last_modified_time as fa_lastModifiedTime, fac.id AS facilityId, fac.tenant_id AS fac_tenantId, fac.facility_category AS fac_facilityCategory, fac.facility_type AS fac_facilityType, " +
            "fac.facility_subtype AS fac_facilitySubtype, fac.facility_name AS fac_facilityName, fac.facility_ownership AS fac_facilityOwnership, fac.facility_region AS fac_facilityRegion, " +
            "fac.addressid, fac.facility_details AS fac_facilityDetails, fac.wf_status AS fac_status, fac.is_active AS fac_isActive, fac.additional_details AS fac_additionalDetails, fac.created_by AS fac_createdBy, " +
            "fac.created_at, fac.updated_by, fac.updated_at, fac.boundary_code AS fac_boundaryCode, ac.name AS activity_type, " +
            "COALESCE(afu.users, ARRAY[]::text[]) AS fa_linkedUsers " +
            " " +
            "from facility_activities fa LEFT JOIN public.facility AS fac ON fa.facility_id = fac.id LEFT JOIN public.activities AS ac ON fa.activity_id = ac.id " +
            "LEFT JOIN (SELECT activityfacilityid, array_agg(DISTINCT userid) AS users FROM activity_facility_users WHERE isdeleted = false GROUP BY activityfacilityid) afu ON afu.activityfacilityid = fa.id";

    private static final String STATUS_COUNT_QUERY = "SELECT status, COUNT(*) AS occurrences " +
            "FROM facility_activities fa where fa.status is not null AND fa.isdeleted = false ";
    private static final String ACTIVITY_COUNT_QUERY = "SELECT COUNT(*) FROM facility_activities fa LEFT JOIN public.facility AS fac ON fa.facility_id = fac.id LEFT JOIN public.activities AS ac ON fa.activity_id = ac.id";

    private static final String PAGINATION_WRAPPER_TEMPLATE = "SELECT * FROM " +
            "(SELECT *, DENSE_RANK() OVER (ORDER BY fa_lastModifiedTime %s , fa_facilityactivityid) offset_ FROM " +
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
                queryBuilder.append(" fa.tenant_id like ? ");
                preparedStmtList.add(tenantId + '%');
            } else {
                log.info("City level tenant");
                queryBuilder.append(" fa.tenant_id=? ");
                preparedStmtList.add(tenantId);
            }
        }
    }

    public String getActivityFacilitySearchQuery(ActivityFacilitySearchRequest request, URLParams urlParams, List<Object> preparedStmtList) {
        return getActivityFacilitySearchQuery(request, urlParams, preparedStmtList, true);
    }

    /**
     * @param scopeToAssignedUser false for internal re-reads - fetching a row the service is about
     *                            to update, or checking one exists. Those were never permission
     *                            checks (workflow-v2 authorises the transition), so scoping them
     *                            turns a missing assignment row into a misleading "not found" on
     *                            an action the caller is entitled to perform.
     */
    public String getActivityFacilitySearchQuery(ActivityFacilitySearchRequest request, URLParams urlParams, List<Object> preparedStmtList, boolean scopeToAssignedUser) {
        //This uses a ternary operator to choose between FIELDPLANS_COUNT_QUERY or FETCH_FIELDPLAN_QUERY based on the value of isCountQuery.
        ActivityFacilitySearchCriteria criteria = request.getCriteria();
        String query = criteria.isCountQuery() ? ACTIVITY_COUNT_QUERY : FETCH_ACTIVITY_QUERY;
        StringBuilder queryBuilder = new StringBuilder(query);

        // Get user info
        var userInfo = request.getRequestInfo().getUserInfo();
        String userUuid = userInfo.getUuid();
        boolean isProjectManager = false;
        boolean isFacilityAdmin = false;
        if (userInfo.getRoles() != null) {
            isProjectManager = userInfo.getRoles().stream().anyMatch(role -> PROJECT_MANAGER.equalsIgnoreCase(role.getCode()));
            isFacilityAdmin = userInfo.getRoles().stream().anyMatch(role -> FACILITY_ADMIN.equalsIgnoreCase(role.getCode()));
            // INSTALLATION_REPORT_APPROVER_QC_TEAM used to be exempt here too. It is not: a
            // reviewer is reached by uuid assignment, exactly like field staff and supervisors,
            // so they scope the same way.
        }

        // The assignment scope is one EXISTS predicate in extracted(), not a join here. A join
        // needed its own copy of the "is this caller privileged, and do we know who they are"
        // test, and the two copies had already drifted: this one lacked the isNotBlank(userUuid)
        // guard the predicate had, so a caller with no uuid got an unfiltered inner join - every
        // row, multiplied once per staffed user.

        addClause(criteria.getTenantId(), preparedStmtList, queryBuilder);

        boolean applyAssignmentScope = scopeToAssignedUser && !isProjectManager && !isFacilityAdmin;
        extracted(urlParams.getLastChangedSince(), preparedStmtList, criteria, queryBuilder, userUuid, applyAssignmentScope);

        // Add clause if includeDeleted is true in request parameter
        addIsDeletedCondition(preparedStmtList, queryBuilder, urlParams.getIncludeDeleted());

        if (criteria.isCountQuery()) {
            return queryBuilder.toString();
        }

        //Wrap constructed SQL query with where criteria in pagination query
        return addPaginationWrapper(queryBuilder.toString(), preparedStmtList, urlParams.getLimit(), urlParams.getOffset(), criteria.getSortDirection());
    }

    private void extracted(Long lastChangedSince, List<Object> preparedStmtList, ActivityFacilitySearchCriteria activityFacility, StringBuilder queryBuilder, String userUuid, boolean applyAssignmentScope) {

        if (!CollectionUtils.isEmpty(activityFacility.getIds())) {
            addClauseIfRequired(preparedStmtList, queryBuilder);
            queryBuilder.append(" fa.id IN (").append(createQuery(activityFacility.getIds())).append(")");
            preparedStmtList.addAll(activityFacility.getIds());
        }

        if (!CollectionUtils.isEmpty(activityFacility.getFieldPlanId())) {
            addClauseIfRequired(preparedStmtList, queryBuilder);
            queryBuilder.append(" fa.field_plan_id IN (").append(createQuery(activityFacility.getFieldPlanId())).append(")");
            preparedStmtList.addAll(activityFacility.getFieldPlanId());
        }

        if (!CollectionUtils.isEmpty(activityFacility.getActivityId())) {
            addClauseIfRequired(preparedStmtList, queryBuilder);
            queryBuilder.append(" fa.activity_id IN (").append(createQuery(activityFacility.getActivityId())).append(")");
            preparedStmtList.addAll(activityFacility.getActivityId());
        }

        if (!CollectionUtils.isEmpty(activityFacility.getActivityCodes())) {
            addClauseIfRequired(preparedStmtList, queryBuilder);
            queryBuilder.append(" ac.code IN (").append(createQuery(activityFacility.getActivityCodes())).append(")");
            preparedStmtList.addAll(activityFacility.getActivityCodes());
        }

        if (!CollectionUtils.isEmpty(activityFacility.getFacilityId())) {
            addClauseIfRequired(preparedStmtList, queryBuilder);
            queryBuilder.append(" fa.facility_id IN (").append(createQuery(activityFacility.getFacilityId())).append(")");
            preparedStmtList.addAll(activityFacility.getFacilityId());
        }

        if (!CollectionUtils.isEmpty(activityFacility.getStatuses())) {
            addClauseIfRequired(preparedStmtList, queryBuilder);
            queryBuilder.append(" fa.status IN (").append(createQuery(activityFacility.getStatuses())).append(")");
            preparedStmtList.addAll(activityFacility.getStatuses());
        }

        if (StringUtils.isNotBlank(activityFacility.getAssignedUserId())) {
            addClauseIfRequired(preparedStmtList, queryBuilder);
            queryBuilder.append(" fa.assigned_user =? ");
            preparedStmtList.add(activityFacility.getAssignedUserId());
        }

        // Check if facility name is provided
        if (StringUtils.isNotBlank(activityFacility.getFacilityName())) {
            addClauseIfRequired(preparedStmtList, queryBuilder);
            queryBuilder.append(" LOWER(fac.facility_name) LIKE ? ");
            preparedStmtList.add("%" + activityFacility.getFacilityName().toLowerCase() + "%");
        }

        if (!CollectionUtils.isEmpty(activityFacility.getBoundaryCodes())) {
            addClauseIfRequired(preparedStmtList, queryBuilder);
            queryBuilder.append(" fac.boundary_code IN (").append(createQuery(activityFacility.getBoundaryCodes())).append(")");
            preparedStmtList.addAll(activityFacility.getBoundaryCodes());
        }

        // Everyone except a project manager or facility admin sees only what they are assigned to.
        //
        // EXISTS rather than a join: it cannot multiply rows, so the fetch query needs no DISTINCT
        // and ACTIVITY_COUNT_QUERY - a bare COUNT(*) assembled by a different path - stays correct
        // without a matching edit.
        if (applyAssignmentScope && StringUtils.isNotBlank(userUuid)) {
            addClauseIfRequired(preparedStmtList, queryBuilder);
            queryBuilder.append(" EXISTS (SELECT 1 FROM activity_facility_users fu " +
                    "WHERE fu.activityfacilityid = fa.id AND fu.isdeleted = false AND fu.userid = ?) ");
            preparedStmtList.add(userUuid);
        }

        if (lastChangedSince != null && lastChangedSince != 0) {
            addClauseIfRequired(preparedStmtList, queryBuilder);
            queryBuilder.append(" ( fa.last_modified_time >= ? )");
            preparedStmtList.add(lastChangedSince);
        }
    }

    public String getActivityDataList(ActivitySearchCriteria criteria, List<Object> preparedStmtList) {
        StringBuilder queryBuilder = new StringBuilder(FETCH_ACTIVITY_DATA_NAME);
        if (!CollectionUtils.isEmpty(criteria.getIds())) {
            addClauseIfRequired(preparedStmtList, queryBuilder);
            queryBuilder.append(" act.id IN (").append(createQuery(criteria.getIds())).append(")");
            preparedStmtList.addAll(criteria.getIds());
        }

        if (!CollectionUtils.isEmpty(criteria.getCode())) {
            addClauseIfRequired(preparedStmtList, queryBuilder);
            queryBuilder.append(" act.code IN (").append(createQuery(criteria.getCode())).append(")");
            preparedStmtList.addAll(criteria.getCode());
        }

        return queryBuilder.toString();
    }

    private void addIsDeletedCondition(List<Object> preparedStmtList, StringBuilder queryBuilder, Boolean includeDeleted) {
        if (!includeDeleted) {
            addClauseIfRequired(preparedStmtList, queryBuilder);
            queryBuilder.append(" fa.isdeleted = false ");
        }
    }

    private String addPaginationWrapper(String query, List<Object> preparedStmtList, Integer limitParam, Integer offsetParam, String sortDirection) {
        int limit = config.getDefaultLimit();
        int offset = config.getDefaultOffset();
        String direction = "ASC".equalsIgnoreCase(sortDirection) ? "ASC" : "DESC";
        String finalQuery = String.format(PAGINATION_WRAPPER_TEMPLATE, direction).replace("{}", query);

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

    public String getStatusFacilitiesOccurence(String fieldPlanId, List<Object> preparedStmtList) {
        StringBuilder queryBuilder = new StringBuilder(STATUS_COUNT_QUERY);
        if (fieldPlanId != null && !fieldPlanId.isEmpty()) {
            queryBuilder.append(" AND fa.field_plan_id =? ");
            preparedStmtList.add(fieldPlanId);
        }
        queryBuilder.append("GROUP BY status ORDER BY occurrences DESC;");

        return queryBuilder.toString();
    }

    /* Returns query to get total projects count based on project search params */
    public String getSearchCountQueryString(ActivityFacilitySearchRequest activityFacilities, String tenantId, Long lastChangedSince, Boolean includeDeleted, List<Object> preparedStatement) {
        ActivityFacilitySearchCriteria criteria = activityFacilities.getCriteria();
        criteria.setCountQuery(true);
        URLParams urlParams = URLParams.builder().tenantId(tenantId).includeDeleted(includeDeleted).lastChangedSince(lastChangedSince).build();
        return getActivityFacilitySearchQuery(activityFacilities, urlParams, preparedStatement);
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
