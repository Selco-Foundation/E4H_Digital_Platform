package org.egov.field_planner.repository;

import lombok.extern.slf4j.Slf4j;
import org.egov.common.data.query.builder.SelectQueryBuilder;
import org.egov.common.data.repository.GenericRepository;
import org.egov.common.models.core.URLParams;
import org.egov.common.producer.Producer;
import org.egov.field_planner.repository.querybuilder.FieldPlanTemplateQueryBuilder;
import org.egov.field_planner.repository.rowmapper.FieldPlanTemplateRowMapper;
import org.egov.field_planner.web.models.FieldPlanTemplate;
import org.egov.field_planner.web.models.FieldPlanTemplateSearchCriteria;
import org.egov.field_planner.web.models.FieldPlanTemplateSearchRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Repository
@Slf4j
public class FieldPlanTemplateRepository extends GenericRepository<FieldPlanTemplate> {

    private final FieldPlanTemplateQueryBuilder queryBuilder;
    private final JdbcTemplate jdbcTemplate;
    private final FieldPlanTemplateRowMapper rowMapper;

    @Autowired
    public FieldPlanTemplateRepository(
            Producer producer,
            NamedParameterJdbcTemplate namedParameterJdbcTemplate,
            RedisTemplate<String, Object> redisTemplate,
            SelectQueryBuilder selectQueryBuilder,
            FieldPlanTemplateRowMapper rowMapper,
            FieldPlanTemplateQueryBuilder queryBuilder,
            JdbcTemplate jdbcTemplate) {
        super(producer, namedParameterJdbcTemplate, redisTemplate, selectQueryBuilder, rowMapper, Optional.of("field_plan_template"));
        this.queryBuilder = queryBuilder;
        this.jdbcTemplate = jdbcTemplate;
        this.rowMapper = rowMapper;
    }

    public List<FieldPlanTemplate> getFieldPlanTemplates(
            FieldPlanTemplateSearchRequest request,
            Integer limit,
            Integer offset,
            String tenantId,
            Long lastChangedSince) {
        List<Object> preparedStmtList = new ArrayList<>();
        FieldPlanTemplateSearchCriteria criteria = request.getCriteria();
        criteria.setCountQuery(false);
        criteria.setTenantId(tenantId);
        URLParams urlParams = URLParams.builder()
                .limit(limit)
                .offset(offset)
                .tenantId(tenantId)
                .lastChangedSince(lastChangedSince)
                .build();

        String query = queryBuilder.getTemplateSearchQuery(criteria, urlParams, preparedStmtList);
        return jdbcTemplate.query(query, rowMapper, preparedStmtList.toArray());
    }

    public Integer getFieldPlanTemplateCount(
            FieldPlanTemplateSearchRequest request,
            String tenantId,
            Long lastChangedSince) {
        List<Object> preparedStatement = new ArrayList<>();
        String query = queryBuilder.getSearchCountQueryString(request, tenantId, lastChangedSince, preparedStatement);
        if (query == null) {
            return 0;
        }
        return jdbcTemplate.queryForObject(query, preparedStatement.toArray(), Integer.class);
    }
}
