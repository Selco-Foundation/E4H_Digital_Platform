package org.egov.activity.repository;

import lombok.extern.slf4j.Slf4j;
import org.egov.activity.repository.querybuilder.ActivityAssignmentQueryBuilder;
import org.egov.activity.repository.rowmapper.ActivityAssignmentRowMapper;
import org.egov.activity.repository.rowmapper.ActivityFacilityUserRowMapper;
import org.egov.activity.web.models.ActivityAssignment;
import org.egov.activity.web.models.ActivityAssignmentSearchCriteria;
import org.egov.activity.web.models.ActivityAssignmentSearchRequest;
import org.egov.activity.web.models.ActivityFacilityUser;
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
public class ActivityFacilityUserRepository extends GenericRepository<ActivityFacilityUser> {

    @Autowired
    public ActivityFacilityUserRepository(Producer producer, NamedParameterJdbcTemplate namedParameterJdbcTemplate,
                                          RedisTemplate<String, Object> redisTemplate, ActivityFacilityUserRowMapper activityRowMapper,
                                          SelectQueryBuilder selectQueryBuilder) {
        super(producer, namedParameterJdbcTemplate, redisTemplate, selectQueryBuilder,
                activityRowMapper, Optional.of("activity_facility_users"));
    }
}