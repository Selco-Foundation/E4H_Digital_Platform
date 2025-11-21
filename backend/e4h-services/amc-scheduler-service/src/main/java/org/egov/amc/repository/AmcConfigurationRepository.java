package org.egov.amc.repository;

import lombok.extern.slf4j.Slf4j;
import org.egov.amc.repository.querybuilder.AmcConfigurationQueryBuilder;
import org.egov.amc.repository.rowmapper.AmcConfigurationMapper;
import org.egov.amc.web.models.AmcConfiguration;
import org.egov.amc.web.models.AmcConfigurationSearchCriteria;
import org.egov.amc.web.models.AmcConfigurationSearchRequest;
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
public class AmcConfigurationRepository extends GenericRepository<AmcConfiguration> {

    private final AmcConfigurationQueryBuilder queryBuilder;

    private final AmcConfigurationMapper amcConfigurationRowMapper;

    private final JdbcTemplate jdbcTemplate;

    @Autowired
    public AmcConfigurationRepository(Producer producer, NamedParameterJdbcTemplate namedParameterJdbcTemplate,
                                      RedisTemplate<String, Object> redisTemplate,
                                      SelectQueryBuilder selectQueryBuilder, AmcConfigurationMapper amcConfigurationMapper,
                                      AmcConfigurationQueryBuilder queryBuilder,
                                      JdbcTemplate jdbcTemplate) {
        super(producer, namedParameterJdbcTemplate, redisTemplate, selectQueryBuilder,
                amcConfigurationMapper, Optional.of("amc_configuration"));
        this.queryBuilder = queryBuilder;
        this.jdbcTemplate = jdbcTemplate;
        this.amcConfigurationRowMapper = amcConfigurationMapper;
    }

    public List<AmcConfiguration> getAmcConfiguration(AmcConfigurationSearchRequest request, Integer limit, Integer offset, String tenantId, Boolean includeDeleted, Long lastChangedSince) {
        //Fetch assetAMC based on search criteria
        List<Object> preparedStmtList = new ArrayList<>();
        AmcConfigurationSearchCriteria criteria = request.getSearchCriteria();
        criteria.setCountQuery(false);
        URLParams urlParams = URLParams.builder().limit(limit).offset(offset).tenantId(tenantId).includeDeleted(includeDeleted).lastChangedSince(lastChangedSince).build();

        String query = queryBuilder.getAmcConfigurationSearchQuery(criteria, urlParams, preparedStmtList);
        List<AmcConfiguration> fieldPlanList = jdbcTemplate.query(query, amcConfigurationRowMapper, preparedStmtList.toArray());

        log.info("Fetched project list based on given search criteria");
        return fieldPlanList;
    }

    public Integer getAmcConfigurationCount(AmcConfigurationSearchRequest request, String tenantId, Long lastChangedSince, Boolean includeDeleted) {
        List<Object> preparedStatement = new ArrayList<>();
        String query = queryBuilder.getSearchCountQueryString(request, tenantId, lastChangedSince, includeDeleted, preparedStatement);

        if (query == null)
            return 0;

        Integer count = jdbcTemplate.queryForObject(query, preparedStatement.toArray(), Integer.class);
        log.info("Total assetAMC count is : " + count);
        return count;
    }
    

}