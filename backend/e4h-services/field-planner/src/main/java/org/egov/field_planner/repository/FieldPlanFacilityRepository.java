package org.egov.field_planner.repository;

import lombok.extern.slf4j.Slf4j;
import org.egov.common.data.query.builder.SelectQueryBuilder;
import org.egov.common.data.repository.GenericRepository;
import org.egov.common.models.project.ProjectFacility;
import org.egov.common.producer.Producer;
import org.egov.field_planner.repository.rowmapper.FieldPlanFacilityRowMapper;
import org.egov.field_planner.web.models.FieldPlan;
import org.egov.field_planner.web.models.FieldPlanFacility;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
@Slf4j
public class FieldPlanFacilityRepository extends GenericRepository<FieldPlanFacility> {
    @Autowired
    public FieldPlanFacilityRepository(Producer producer, NamedParameterJdbcTemplate namedParameterJdbcTemplate,
                                       RedisTemplate<String, Object> redisTemplate,
                                       SelectQueryBuilder selectQueryBuilder, FieldPlanFacilityRowMapper fieldPlanFacilityRowMapper) {
        super(producer, namedParameterJdbcTemplate, redisTemplate, selectQueryBuilder,
                fieldPlanFacilityRowMapper, Optional.of("fieldPlan_facility"));
    }
}