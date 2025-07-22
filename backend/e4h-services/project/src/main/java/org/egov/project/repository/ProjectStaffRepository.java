package org.egov.project.repository;

import lombok.extern.slf4j.Slf4j;
import org.egov.common.data.query.builder.SelectQueryBuilder;
import org.egov.common.data.repository.GenericRepository;
import org.egov.common.models.project.ProjectStaff;
import org.egov.common.producer.Producer;
import org.egov.project.repository.rowmapper.ProjectStaffRowMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;


@Repository
@Slf4j
public class ProjectStaffRepository extends GenericRepository<ProjectStaff> {

    @Autowired
    public ProjectStaffRepository(Producer producer, NamedParameterJdbcTemplate namedParameterJdbcTemplate,
                                  RedisTemplate<String, Object> redisTemplate,
                                  SelectQueryBuilder selectQueryBuilder, ProjectStaffRowMapper projectStaffRowMapper) {
        super(producer, namedParameterJdbcTemplate, redisTemplate, selectQueryBuilder,
                projectStaffRowMapper, Optional.of("project_staff"));
    }

    public List<String> findProjectIdsByUserId(String userId) {
        String sql = "SELECT projectid FROM project_staff WHERE staffid = :userId AND isdeleted = false";
        Map<String, Object> params = new HashMap<>();
        params.put("userId", userId);
        return namedParameterJdbcTemplate.queryForList(sql, params, String.class);
    }

}

