package org.egov.amc.repository;

import lombok.extern.slf4j.Slf4j;
import org.egov.amc.repository.querybuilder.AssetAmcQueryBuilder;
import org.egov.amc.repository.rowmapper.AssetAmcMapper;
import org.egov.amc.web.models.AssetAmc;
import org.egov.amc.web.models.AssetAmcSearchCriteria;
import org.egov.amc.web.models.AssetAmcSearchRequest;
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
public class AssetAmcRepository extends GenericRepository<AssetAmc> {

    private final AssetAmcQueryBuilder queryBuilder;

    private final AssetAmcMapper assetAmcRowMapper;

    private final JdbcTemplate jdbcTemplate;

    @Autowired
    public AssetAmcRepository(Producer producer, NamedParameterJdbcTemplate namedParameterJdbcTemplate,
                              RedisTemplate<String, Object> redisTemplate,
                              SelectQueryBuilder selectQueryBuilder, AssetAmcMapper assetAmcMapper,
                              AssetAmcQueryBuilder queryBuilder,
                              JdbcTemplate jdbcTemplate) {
        super(producer, namedParameterJdbcTemplate, redisTemplate, selectQueryBuilder,
                assetAmcMapper, Optional.of("asset_amc"));
        this.queryBuilder = queryBuilder;
        this.jdbcTemplate = jdbcTemplate;
        this.assetAmcRowMapper = assetAmcMapper;
    }

    public List<AssetAmc> getAssetAmc(AssetAmcSearchRequest request, Integer limit, Integer offset, String tenantId, Boolean includeDeleted, Long lastChangedSince) {
        log.trace("Entering getAssetAmc method, tenantId: {}, limit: {}, offset: {}", tenantId, limit, offset);
        //Fetch assetAmc based on search criteria
        List<Object> preparedStmtList = new ArrayList<>();
        AssetAmcSearchCriteria criteria = request.getSearchCriteria();
        criteria.setCountQuery(false);
        URLParams urlParams = URLParams.builder().limit(limit).offset(offset).tenantId(tenantId).includeDeleted(includeDeleted).lastChangedSince(lastChangedSince).build();

        String query = queryBuilder.getAssetAmcSearchQuery(criteria, urlParams, preparedStmtList);
        log.debug("Executing asset AMC search query for tenantId: {}", tenantId);
        List<AssetAmc> assetAmcList = jdbcTemplate.query(query, assetAmcRowMapper, preparedStmtList.toArray());

        log.info("Fetched {} asset AMC record(s) based on search criteria", assetAmcList.size());
        return assetAmcList;
    }

    public Integer getAssetAmcCount(AssetAmcSearchRequest request, String tenantId, Long lastChangedSince, Boolean includeDeleted) {
        log.trace("Entering getAssetAmcCount method, tenantId: {}", tenantId);
        List<Object> preparedStatement = new ArrayList<>();
        String query = queryBuilder.getSearchCountQueryString(request, tenantId, lastChangedSince, includeDeleted, preparedStatement);

        if (query == null) {
            log.debug("Count query is null, returning 0");
            return 0;
        }

        log.debug("Executing asset AMC count query for tenantId: {}", tenantId);
        Integer count = jdbcTemplate.queryForObject(query, preparedStatement.toArray(), Integer.class);
        log.info("Total asset AMC count: {}", count);
        return count;
    }
    

}