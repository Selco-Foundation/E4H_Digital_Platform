package org.egov.amc.repository;

import lombok.extern.slf4j.Slf4j;
import org.egov.amc.repository.querybuilder.ScheduledVisitQueryBuilder;
import org.egov.amc.repository.rowmapper.ScheduledVisitMapper;
import org.egov.amc.web.models.ScheduledVisit;
import org.egov.amc.web.models.ScheduledVisitSearchCriteria;
import org.egov.amc.web.models.ScheduledVisitSearchRequest;
import org.egov.common.data.query.builder.SelectQueryBuilder;
import org.egov.common.data.repository.GenericRepository;
import org.egov.common.models.core.URLParams;
import org.egov.common.producer.Producer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Repository
public class ScheduledVisitRepository extends GenericRepository<ScheduledVisit> {

    private final ScheduledVisitQueryBuilder queryBuilder;

    private final ScheduledVisitMapper scheduledVisitRowMapper;

    private final JdbcTemplate jdbcTemplate;

    @Autowired
    public ScheduledVisitRepository(Producer producer, NamedParameterJdbcTemplate namedParameterJdbcTemplate,
                                    RedisTemplate<String, Object> redisTemplate,
                                    SelectQueryBuilder selectQueryBuilder, ScheduledVisitMapper scheduledVisitMapper,
                                    ScheduledVisitQueryBuilder queryBuilder,
                                    JdbcTemplate jdbcTemplate) {
        super(producer, namedParameterJdbcTemplate, redisTemplate, selectQueryBuilder,
                scheduledVisitMapper, Optional.of("scheduled_visits"));
        this.queryBuilder = queryBuilder;
        this.jdbcTemplate = jdbcTemplate;
        this.scheduledVisitRowMapper = scheduledVisitMapper;
    }

    public List<ScheduledVisit> getScheduledVisit(ScheduledVisitSearchRequest request, Integer limit, Integer offset, String tenantId, Boolean includeDeleted, Long lastChangedSince) {
        log.trace("Entering getScheduledVisit method, tenantId: {}, limit: {}, offset: {}", tenantId, limit, offset);
        //Fetch ScheduledVisit based on search criteria
        List<Object> preparedStmtList = new ArrayList<>();
        ScheduledVisitSearchCriteria criteria = request.getSearchCriteria();
        criteria.setCountQuery(false);
        URLParams urlParams = URLParams.builder().limit(limit).offset(offset).tenantId(tenantId).includeDeleted(includeDeleted).lastChangedSince(lastChangedSince).build();

        String query = queryBuilder.getScheduledVisitSearchQuery(request, urlParams, preparedStmtList);
        log.debug("Executing scheduled visit search query for tenantId: {}", tenantId);
        List<ScheduledVisit> scheduledVisitList = jdbcTemplate.query(query, scheduledVisitRowMapper, preparedStmtList.toArray());

        log.info("Fetched {} scheduled visit(s) based on search criteria", scheduledVisitList.size());
        return scheduledVisitList;
    }

    public Integer getScheduledVisitCount(ScheduledVisitSearchRequest request, String tenantId, Long lastChangedSince, Boolean includeDeleted) {
        log.trace("Entering getScheduledVisitCount method, tenantId: {}", tenantId);
        List<Object> preparedStatement = new ArrayList<>();
        String query = queryBuilder.getSearchCountQueryString(request, tenantId, lastChangedSince, includeDeleted, preparedStatement);

        if (query == null) {
            log.debug("Count query is null, returning 0");
            return 0;
        }

        log.debug("Executing scheduled visit count query for tenantId: {}", tenantId);
        Integer count = jdbcTemplate.queryForObject(query, preparedStatement.toArray(), Integer.class);
        log.info("Total scheduled visit count: {}", count);
        return count;
    }

    /**
     * visitId -> every active assignee uuid on that visit, oldest assignment first.
     *
     * <p>Deliberately separate from {@link #getScheduledVisit}: that search scopes non-PROJECT_MANAGER
     * callers with {@code sva.assigned_user = <caller>}, and because the same {@code sva} alias feeds
     * the {@code jsonb_agg} that builds the assignments column, the visits it returns list only the
     * caller's own assignment. That is the intended API behaviour and is left alone - but the search
     * index needs the real, complete roster (the AMC field staff is usually not the caller), so the
     * index path reads it straight from the table with no caller scoping.
     *
     * <p>Ordered by created_time so "first field staff wins" in the mapped-vendor enrichment is stable
     * across reruns rather than dependent on whatever order the rows happen to come back in.
     */
    public Map<String, List<String>> getActiveAssigneeUuidsByVisitIds(List<String> visitIds) {
        if (CollectionUtils.isEmpty(visitIds)) {
            return Collections.emptyMap();
        }
        String placeholders = visitIds.stream().map(id -> "?").collect(Collectors.joining(", "));
        String query = "SELECT scheduled_visit_id, assigned_user FROM scheduled_visit_assignments"
                + " WHERE is_active = true AND assigned_user IS NOT NULL"
                + " AND scheduled_visit_id IN (" + placeholders + ")"
                + " ORDER BY scheduled_visit_id, created_time";

        Map<String, List<String>> assigneesByVisitId = new HashMap<>();
        jdbcTemplate.query(query, visitIds.toArray(), rs -> {
            assigneesByVisitId
                    .computeIfAbsent(rs.getString("scheduled_visit_id"), k -> new ArrayList<>())
                    .add(rs.getString("assigned_user"));
        });
        log.debug("Resolved active assignees for {} of {} visit(s) for indexing",
                assigneesByVisitId.size(), visitIds.size());
        return assigneesByVisitId;
    }

}