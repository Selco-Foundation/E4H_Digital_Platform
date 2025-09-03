package org.egov.field_planner.repository;

import lombok.extern.slf4j.Slf4j;
import org.egov.common.data.query.builder.SelectQueryBuilder;
import org.egov.common.data.repository.GenericRepository;
import org.egov.common.models.project.*;
import org.egov.common.producer.Producer;
import org.egov.field_planner.repository.querybuilder.FieldPlannerQueryBuilder;
import org.egov.field_planner.repository.rowmapper.*;
import org.egov.field_planner.web.models.FieldPlan;
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
                planRowMapper, Optional.of("project"));
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

    

}