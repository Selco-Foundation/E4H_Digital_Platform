package org.egov.amc.repository;

import lombok.extern.slf4j.Slf4j;
import org.egov.amc.repository.querybuilder.AmcPlanQueryBuilder;
import org.egov.amc.repository.rowmapper.AmcPlanMapper;
import org.egov.amc.web.models.AmcPlan;
import org.egov.amc.web.models.AmcPlanSearchCriteria;
import org.egov.amc.web.models.AmcPlanSearchRequest;
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
public class AmcPlanRepository extends GenericRepository<AmcPlan> {

    private final AmcPlanQueryBuilder queryBuilder;

    private final AmcPlanMapper amcPlanRowMapper;

    private final JdbcTemplate jdbcTemplate;

    @Autowired
    public AmcPlanRepository(Producer producer, NamedParameterJdbcTemplate namedParameterJdbcTemplate,
                              RedisTemplate<String, Object> redisTemplate,
                              SelectQueryBuilder selectQueryBuilder, AmcPlanMapper amcPlanMapper,
                              AmcPlanQueryBuilder queryBuilder,
                              JdbcTemplate jdbcTemplate) {
        super(producer, namedParameterJdbcTemplate, redisTemplate, selectQueryBuilder,
                amcPlanMapper, Optional.of("amc_plans"));
        this.queryBuilder = queryBuilder;
        this.jdbcTemplate = jdbcTemplate;
        this.amcPlanRowMapper = amcPlanMapper;
    }

    public List<AmcPlan> getAmcPlan(AmcPlanSearchRequest request, Integer limit, Integer offset, String tenantId, Boolean includeDeleted, Long lastChangedSince) {
        log.trace("Entering getAmcPlan method, tenantId: {}, limit: {}, offset: {}", tenantId, limit, offset);
        List<Object> preparedStmtList = new ArrayList<>();
        AmcPlanSearchCriteria criteria = request.getSearchCriteria();
        criteria.setCountQuery(false);
        URLParams urlParams = URLParams.builder().limit(limit).offset(offset).tenantId(tenantId).includeDeleted(includeDeleted).lastChangedSince(lastChangedSince).build();

        String query = queryBuilder.getAmcPlanSearchQuery(criteria, urlParams, preparedStmtList);
        List<AmcPlan> amcPlanList = jdbcTemplate.query(query, amcPlanRowMapper, preparedStmtList.toArray());

        log.info("Fetched {} AMC plan(s) based on search criteria", amcPlanList.size());
        return amcPlanList;
    }

    public Integer getAmcPlanCount(AmcPlanSearchRequest request, String tenantId, Long lastChangedSince, Boolean includeDeleted) {
        log.trace("Entering getAmcPlanCount method, tenantId: {}", tenantId);
        List<Object> preparedStatement = new ArrayList<>();
        String query = queryBuilder.getSearchCountQueryString(request, tenantId, lastChangedSince, includeDeleted, preparedStatement);

        if (query == null) {
            return 0;
        }

        Integer count = jdbcTemplate.queryForObject(query, preparedStatement.toArray(), Integer.class);
        log.info("Total AMC plan count: {}", count);
        return count;
    }

}
