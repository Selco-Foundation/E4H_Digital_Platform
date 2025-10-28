package org.egov.activity.repository;

import lombok.extern.slf4j.Slf4j;
import org.egov.activity.repository.querybuilder.ActivityQueryBuilder;
import org.egov.activity.repository.rowmapper.ActivityDataRowMapper;
import org.egov.activity.repository.rowmapper.FacilityStatusRowMapper;
import org.egov.activity.web.models.*;
import org.egov.common.data.query.builder.SelectQueryBuilder;
import org.egov.common.data.repository.GenericRepository;
import org.egov.common.models.core.URLParams;
import org.egov.common.producer.Producer;
import org.egov.activity.repository.rowmapper.ActivityRowMapper;
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
public class ActivityFacilityRepository extends GenericRepository<ActivityFacility> {

    private final ActivityQueryBuilder queryBuilder;
    private final JdbcTemplate jdbcTemplate;
    private final ActivityRowMapper activityRowMapper;
    private final ActivityDataRowMapper activityDataRowMapper;
    private final FacilityStatusRowMapper facilityStatusRowMapper;

    @Autowired
    public ActivityFacilityRepository(Producer producer, NamedParameterJdbcTemplate namedParameterJdbcTemplate,
                                      RedisTemplate<String, Object> redisTemplate, ActivityRowMapper activityRowMapper,
                                      SelectQueryBuilder selectQueryBuilder, ActivityRowMapper fieldPlanFacilityRowMapper,
                                      JdbcTemplate jdbcTemplate, ActivityQueryBuilder queryBuilder, ActivityDataRowMapper activityDataRowMapper, FacilityStatusRowMapper facilityStatusRowMapper) {
        super(producer, namedParameterJdbcTemplate, redisTemplate, selectQueryBuilder,
                fieldPlanFacilityRowMapper, Optional.of("facility_activities"));
        this.queryBuilder = queryBuilder;
        this.jdbcTemplate = jdbcTemplate;
        this.activityRowMapper = activityRowMapper;
        this.activityDataRowMapper = activityDataRowMapper;
        this.facilityStatusRowMapper = facilityStatusRowMapper;
    }

    public List<ActivityFacility> getActivitiesFacility(ActivityFacilitySearchRequest request, Integer limit, Integer offset, String tenantId, Boolean includeDeleted, Long lastChangedSince) {
        //Fetch Facility based on search criteria
        List<Object> preparedStmtList = new ArrayList<>();
        ActivityFacilitySearchCriteria criteria = request.getCriteria();
        criteria.setCountQuery(false);
        URLParams urlParams = URLParams.builder().limit(limit).offset(offset).tenantId(tenantId).includeDeleted(includeDeleted).lastChangedSince(lastChangedSince).build();

        String query = queryBuilder.getActivityFacilitySearchQuery(criteria, urlParams, preparedStmtList);
        List<ActivityFacility> activityFacilities = jdbcTemplate.query(query, activityRowMapper, preparedStmtList.toArray());

        log.info("Fetched project list based on given search criteria");
        return activityFacilities;
    }

    public Activity getActivityObject(ActivitySearchCriteria criteria) {
        List<Object> preparedStmtList = new ArrayList<>();
        String query = queryBuilder.getActivityDataList(criteria, preparedStmtList);
        List<Activity> activities = jdbcTemplate.query(query, activityDataRowMapper, preparedStmtList.toArray());
        log.info("Fetched project status agregation list based on given Parent Ids");
        if (activities !=null && !activities.isEmpty())
            return activities.get(0);
        return null;
    }

    public Integer getActivitiesFacilityCount(ActivityFacilitySearchRequest request, String tenantId, Long lastChangedSince, Boolean includeDeleted) {
        List<Object> preparedStatement = new ArrayList<>();
        String query = queryBuilder.getSearchCountQueryString(request, tenantId, lastChangedSince, includeDeleted, preparedStatement);

        if (query == null)
            return 0;

        Integer count = jdbcTemplate.queryForObject(query, preparedStatement.toArray(), Integer.class);
        log.info("Total FieldPlans count is : " + count);
        return count;
    }

    public List<FacilityStatusAgregation> getStatusFacilitiesAgregation(String fieldPlanId) {
        List<Object> preparedStmtList = new ArrayList<>();
        String query = queryBuilder.getStatusFacilitiesOccurence(fieldPlanId, preparedStmtList);
        List<FacilityStatusAgregation> statusAgregations = jdbcTemplate.query(query, facilityStatusRowMapper, preparedStmtList.toArray());
        log.info("Fetched facility status agregation list based on given Parent Ids");
        return statusAgregations;
    }
}