package org.egov.activity.repository;

import lombok.extern.slf4j.Slf4j;
import org.egov.activity.repository.querybuilder.ActivityAssignmentQueryBuilder;
import org.egov.activity.repository.rowmapper.ActivityAssignmentRowMapper;
import org.egov.activity.web.models.*;
import org.egov.common.data.query.builder.SelectQueryBuilder;
import org.egov.common.data.repository.GenericRepository;
import org.egov.common.models.core.URLParams;
import org.egov.common.producer.Producer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Repository
@Slf4j
public class ActivityAssignmentRepository extends GenericRepository<ActivityAssignment> {

    private final ActivityAssignmentQueryBuilder queryBuilder;
    private final JdbcTemplate jdbcTemplate;
    private final ActivityAssignmentRowMapper activityRowMapper;

    @Autowired
    public ActivityAssignmentRepository(Producer producer, NamedParameterJdbcTemplate namedParameterJdbcTemplate,
                                        RedisTemplate<String, Object> redisTemplate, ActivityAssignmentRowMapper activityRowMapper,
                                        SelectQueryBuilder selectQueryBuilder,
                                        JdbcTemplate jdbcTemplate, ActivityAssignmentQueryBuilder queryBuilder) {
        super(producer, namedParameterJdbcTemplate, redisTemplate, selectQueryBuilder,
                activityRowMapper, Optional.of("activity_assignments"));
        this.queryBuilder = queryBuilder;
        this.jdbcTemplate = jdbcTemplate;
        this.activityRowMapper = activityRowMapper;
    }

    /**
     * Role codes a user still holds on a plan, ignoring the assignments being changed right now.
     *
     * Used before revoking a user's per-facility rows: one person can be both supervisor and
     * reviewer on the same plan, and dropping one of those must not take away the visibility the
     * other still needs.
     *
     * Deliberately not {@code getActivitiesAssignment}: its query builder scopes every caller who
     * is not a project manager to their own {@code assigned_to}, so the answer would depend on who
     * asked rather than on what is stored.
     */
    public List<String> findActiveRoleCodesForPlanUser(String fieldPlanId, String assignedTo, String tenantId,
                                                       Collection<String> excludeAssignmentIds) {
        StringBuilder query = new StringBuilder(
                "SELECT role ->> 'code' FROM activity_assignments " +
                        "WHERE isdeleted = false AND field_plan_id = ? AND assigned_to = ?");
        List<Object> params = new ArrayList<>();
        params.add(fieldPlanId);
        params.add(assignedTo);
        if (tenantId != null && !tenantId.isBlank()) {
            query.append(" AND tenant_id like ?");
            params.add(tenantId + "%");
        }
        if (excludeAssignmentIds != null && !excludeAssignmentIds.isEmpty()) {
            query.append(" AND id NOT IN (")
                    .append(String.join(",", Collections.nCopies(excludeAssignmentIds.size(), "?")))
                    .append(")");
            params.addAll(excludeAssignmentIds);
        }
        return jdbcTemplate.queryForList(query.toString(), String.class, params.toArray());
    }

    public List<ActivityAssignment> getActivitiesAssignment(ActivityAssignmentSearchRequest request, Integer limit, Integer offset, String tenantId, Boolean includeDeleted, Long lastChangedSince) {
        //Fetch Installation Plans based on search criteria
        List<Object> preparedStmtList = new ArrayList<>();
        ActivityAssignmentSearchCriteria criteria = request.getCriteria();
        criteria.setCountQuery(false);
        URLParams urlParams = URLParams.builder().limit(limit).offset(offset).tenantId(tenantId).includeDeleted(includeDeleted).lastChangedSince(lastChangedSince).build();

        String query = queryBuilder.getActivityAssignmentSearchQuery(request, urlParams, preparedStmtList);
        List<ActivityAssignment> activityAssignments = jdbcTemplate.query(query, activityRowMapper, preparedStmtList.toArray());

        log.info("Fetched activity assignments list based on given search criteria");
        return activityAssignments;
    }

    public Integer getActivitiesCount(ActivityAssignmentSearchRequest request, String tenantId, Long lastChangedSince, Boolean includeDeleted) {
        List<Object> preparedStatement = new ArrayList<>();
        String query = queryBuilder.getSearchCountQueryString(request, tenantId, lastChangedSince, includeDeleted, preparedStatement);

        if (query == null)
            return 0;

        Integer count = jdbcTemplate.queryForObject(query, preparedStatement.toArray(), Integer.class);
        log.info("Total ActivityAssignments count is : " + count);
        return count;
    }

    public Map<String, String> getFirstPocNumbersByFieldPlanIds(List<String> fieldPlanIds) {
        if (fieldPlanIds == null || fieldPlanIds.isEmpty()) {
            return Collections.emptyMap();
        }

        String placeholders = fieldPlanIds.stream().map(id -> "?").collect(Collectors.joining(", "));
        String query = "SELECT DISTINCT ON (field_plan_id) field_plan_id, poc_number "
                + "FROM activity_assignments "
                + "WHERE field_plan_id IN (" + placeholders + ") AND COALESCE(isdeleted, false) = false "
                + "ORDER BY field_plan_id, created_time ASC NULLS LAST";

        Map<String, String> pocNumbersByFieldPlanId = new HashMap<>();
        jdbcTemplate.query(query, rs -> {
            pocNumbersByFieldPlanId.put(rs.getString("field_plan_id"), rs.getString("poc_number"));
        }, fieldPlanIds.toArray());

        return pocNumbersByFieldPlanId;
    }

    public Map<String, String> getAssignedToByFieldPlanIdsAndRole(List<String> fieldPlanIds, String role) {
        if (fieldPlanIds == null || fieldPlanIds.isEmpty() || role == null || role.isBlank()) {
            return Collections.emptyMap();
        }

        String placeholders = fieldPlanIds.stream().map(id -> "?").collect(Collectors.joining(", "));
        String query = "SELECT DISTINCT ON (field_plan_id) field_plan_id, assigned_to "
                + "FROM activity_assignments "
                + "WHERE field_plan_id IN (" + placeholders + ") "
                + "AND role ->> 'code' = ? "
                + "AND COALESCE(isdeleted, false) = false "
                + "ORDER BY field_plan_id, created_time ASC NULLS LAST";

        List<Object> params = new ArrayList<>(fieldPlanIds);
        params.add(role);

        Map<String, String> assignedToByFieldPlanId = new HashMap<>();
        jdbcTemplate.query(query, rs -> {
            assignedToByFieldPlanId.put(rs.getString("field_plan_id"), rs.getString("assigned_to"));
        }, params.toArray());

        return assignedToByFieldPlanId;
    }
}