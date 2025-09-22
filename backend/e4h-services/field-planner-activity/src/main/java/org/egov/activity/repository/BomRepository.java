package org.egov.activity.repository;

import lombok.extern.slf4j.Slf4j;
import org.egov.activity.repository.querybuilder.BomQueryBuilder;
import org.egov.activity.repository.rowmapper.BomRowMapper;
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
import java.util.List;
import java.util.Optional;

@Repository
@Slf4j
public class BomRepository extends GenericRepository<BillOfMaterial> {

    private final BomQueryBuilder queryBuilder;
    private final JdbcTemplate jdbcTemplate;
    private final BomRowMapper bomRowMapper;


    @Autowired
    public BomRepository(Producer producer, NamedParameterJdbcTemplate namedParameterJdbcTemplate,
                         RedisTemplate<String, Object> redisTemplate, BomRowMapper bomRowMapper,
                         SelectQueryBuilder selectQueryBuilder,
                         JdbcTemplate jdbcTemplate, BomQueryBuilder queryBuilder) {
        super(producer, namedParameterJdbcTemplate, redisTemplate, selectQueryBuilder,
                bomRowMapper, Optional.of("bom"));
        this.queryBuilder = queryBuilder;
        this.jdbcTemplate = jdbcTemplate;
        this.bomRowMapper = bomRowMapper;
    }

    public List<BillOfMaterial> getBillOfMaterials(BomSearchRequest request, Integer limit, Integer offset, String tenantId, Boolean includeDeleted, Long lastChangedSince) {
        //Fetch FieldPlans based on search criteria
        List<Object> preparedStmtList = new ArrayList<>();
        BomSearchCriteria criteria = request.getCriteria();
        criteria.setCountQuery(false);
        URLParams urlParams = URLParams.builder().limit(limit).offset(offset).tenantId(tenantId).includeDeleted(includeDeleted).lastChangedSince(lastChangedSince).build();

        String query = queryBuilder.getBOMSearchQuery(criteria, urlParams, preparedStmtList);
        List<BillOfMaterial> billOfMaterials = jdbcTemplate.query(query, bomRowMapper, preparedStmtList.toArray());

        log.info("Fetched project list based on given search criteria");
        return billOfMaterials;
    }

    public Integer getBillOfMatrialsCount(BomSearchRequest request, String tenantId, Long lastChangedSince, Boolean includeDeleted) {
        List<Object> preparedStatement = new ArrayList<>();
        String query = queryBuilder.getSearchCountQueryString(request, tenantId, lastChangedSince, includeDeleted, preparedStatement);

        if (query == null)
            return 0;

        Integer count = jdbcTemplate.queryForObject(query, preparedStatement.toArray(), Integer.class);
        log.info("Total FieldPlans count is : " + count);
        return count;
    }
}