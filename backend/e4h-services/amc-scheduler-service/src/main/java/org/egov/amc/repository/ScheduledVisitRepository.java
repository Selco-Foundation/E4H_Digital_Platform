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

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

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

    /**
     * Fetches the facility's latest non-DRAFT, non-deleted visit, or empty if the facility has none
     * (no AMC configured yet, or every visit still a draft).
     */
    public Optional<ScheduledVisit> getLatestNonDraftVisitByFacility(String tenantId, String facilityId) {
        log.trace("Entering getLatestNonDraftVisitByFacility method, tenantId: {}, facilityId: {}", tenantId, facilityId);
        List<Object> preparedStmtList = new ArrayList<>();
        String query = queryBuilder.getLatestNonDraftVisitByFacilityQuery(tenantId, facilityId, preparedStmtList);
        List<ScheduledVisit> visits = jdbcTemplate.query(query, scheduledVisitRowMapper, preparedStmtList.toArray());
        return visits.isEmpty() ? Optional.empty() : Optional.of(visits.get(0));
    }

    /**
     * Merges {@code totalTickets} into the visit's additional_details, leaving every other key intact.
     *
     * <p>Written with a targeted UPDATE rather than pushed through egov-persister on purpose: the
     * update-scheduled-visit persister config rewrites the whole row and re-upserts assignments, which
     * would churn audit history on every ticket raised for the facility.
     *
     * @return number of rows updated (0 if the visit disappeared between lookup and write)
     */
    public int updateTotalTicketsOnVisit(String visitId, Integer totalTickets) {
        log.trace("Entering updateTotalTicketsOnVisit method, visitId: {}, totalTickets: {}", visitId, totalTickets);
        String query = "UPDATE scheduled_visits SET additional_details = " +
                "COALESCE(additional_details, '{}'::jsonb) || jsonb_build_object('totalTickets', ?::int) " +
                "WHERE id = ?";
        int updated = jdbcTemplate.update(query, totalTickets, visitId);
        log.debug("Stamped totalTickets={} on {} scheduled visit row(s) for visitId={}", totalTickets, updated, visitId);
        return updated;
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
    

}