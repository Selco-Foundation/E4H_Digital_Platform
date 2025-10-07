package org.egov.field_planner.repository;

import lombok.extern.slf4j.Slf4j;
import org.egov.common.data.query.builder.SelectQueryBuilder;
import org.egov.common.data.repository.GenericRepository;
import org.egov.common.models.core.URLParams;
import org.egov.common.producer.Producer;
import org.egov.field_planner.repository.querybuilder.FieldPlannerQueryBuilder;
import org.egov.field_planner.repository.rowmapper.*;
import org.egov.field_planner.web.models.FieldPlan;
import org.egov.field_planner.web.models.FieldPlanRequest;
import org.egov.field_planner.web.models.FieldPlanSearchCriteria;
import org.egov.field_planner.web.models.FieldPlanSearchRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.*;

@Slf4j
@Repository
public class FieldPlannerRepository extends GenericRepository<FieldPlan> {

    private final FieldPlannerQueryBuilder queryBuilder;

    private final FieldPlanRowMapper fieldPlanRowMapper;

    private final JdbcTemplate jdbcTemplate;

    @Autowired
    public FieldPlannerRepository(Producer producer, NamedParameterJdbcTemplate namedParameterJdbcTemplate,
                                  RedisTemplate<String, Object> redisTemplate, PlanRowMapper planRowMapper,
                                  SelectQueryBuilder selectQueryBuilder, FieldPlanRowMapper fieldPlanRowMapper,
                                  FieldPlannerQueryBuilder queryBuilder,
                                  JdbcTemplate jdbcTemplate) {
        super(producer, namedParameterJdbcTemplate, redisTemplate, selectQueryBuilder,
                planRowMapper, Optional.of("field_plans"));
        this.queryBuilder = queryBuilder;
        this.jdbcTemplate = jdbcTemplate;
        this.fieldPlanRowMapper = fieldPlanRowMapper;
    }

    public List<FieldPlan> getHighestFielPlanName(FieldPlan fieldPlan) {
        List<Object> preparedStmtList = new ArrayList<>();
        String query = queryBuilder.getHighestFielPlanNameQuery(fieldPlan, preparedStmtList);
        List<FieldPlan> fieldPlans = jdbcTemplate.query(query, fieldPlanRowMapper, preparedStmtList.toArray());
        log.info("Fetched latest fieldPlan name based on given Parent Ids");
        return fieldPlans;
    }

    public List<FieldPlan> getFieldPlans(FieldPlanSearchRequest request, Integer limit, Integer offset, String tenantId, Boolean includeDeleted, Long lastChangedSince, Long createdFrom, Long createdTo) {
        //Fetch FieldPlans based on search criteria
        List<Object> preparedStmtList = new ArrayList<>();
        FieldPlanSearchCriteria criteria = request.getFieldPlan();
        criteria.setCountQuery(false);
        URLParams urlParams = URLParams.builder().limit(limit).offset(offset).tenantId(tenantId).includeDeleted(includeDeleted).lastChangedSince(lastChangedSince).build();

        String query = queryBuilder.getFieldPlanSearchQuery(criteria, urlParams, preparedStmtList);
        List<FieldPlan> fieldPlanList = jdbcTemplate.query(query, fieldPlanRowMapper, preparedStmtList.toArray());

        log.info("Fetched project list based on given search criteria");
        return fieldPlanList;
    }

    public Integer getFieldPlanCount(FieldPlanSearchRequest request, String tenantId, Long lastChangedSince, Boolean includeDeleted) {
        List<Object> preparedStatement = new ArrayList<>();
        String query = queryBuilder.getSearchCountQueryString(request, tenantId, lastChangedSince, includeDeleted, preparedStatement);

        if (query == null)
            return 0;

        Integer count = jdbcTemplate.queryForObject(query, preparedStatement.toArray(), Integer.class);
        log.info("Total FieldPlans count is : " + count);
        return count;
    }
    

}