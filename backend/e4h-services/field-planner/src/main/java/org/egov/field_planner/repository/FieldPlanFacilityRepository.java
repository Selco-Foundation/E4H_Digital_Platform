package org.egov.field_planner.repository;

import lombok.extern.slf4j.Slf4j;
import org.egov.common.data.query.builder.SelectQueryBuilder;
import org.egov.common.data.repository.GenericRepository;
import org.egov.common.producer.Producer;
import org.egov.field_planner.repository.rowmapper.FieldPlanFacilityRowMapper;
import org.egov.field_planner.web.models.FieldPlanFacility;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.Map;
import java.util.Optional;

@Repository
@Slf4j
public class FieldPlanFacilityRepository extends GenericRepository<FieldPlanFacility> {

    private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    @Autowired
    public FieldPlanFacilityRepository(Producer producer, NamedParameterJdbcTemplate namedParameterJdbcTemplate,
                                       RedisTemplate<String, Object> redisTemplate,
                                       SelectQueryBuilder selectQueryBuilder,
                                       FieldPlanFacilityRowMapper fieldPlanFacilityRowMapper) {
        super(producer, namedParameterJdbcTemplate, redisTemplate, selectQueryBuilder,
                fieldPlanFacilityRowMapper, Optional.of("field_plan_facilities"));
        this.namedParameterJdbcTemplate = namedParameterJdbcTemplate;
    }

    public Optional<String> findProjectIdByFieldPlanId(String fieldPlanId) {
        var rows = namedParameterJdbcTemplate.queryForList(
                "SELECT project_id FROM field_plans WHERE id = :id AND isdeleted = false LIMIT 1",
                Map.of("id", fieldPlanId));
        if (rows.isEmpty() || rows.get(0).get("project_id") == null) {
            return Optional.empty();
        }
        return Optional.of(rows.get(0).get("project_id").toString());
    }

    public void linkAssessmentSource(String fieldPlanFacilityId, String sourcePlanFacilityId,
                                     String assessmentPlanId, String userId) {
        long now = System.currentTimeMillis();
        namedParameterJdbcTemplate.update(
                """
                UPDATE field_plan_facilities
                SET source_plan_facility_id = :sourcePlanFacilityId,
                    additional_details = jsonb_set(
                        COALESCE(additional_details, '{}'::jsonb),
                        '{assessmentSource}',
                        to_jsonb(json_build_object(
                            'assessmentPlanId', :assessmentPlanId,
                            'planFacilityId', :sourcePlanFacilityId
                        )),
                        true
                    ),
                    last_modified_by = :userId,
                    lastmodifiedtime = :now
                WHERE id = :fieldPlanFacilityId
                """,
                Map.of(
                        "sourcePlanFacilityId", sourcePlanFacilityId,
                        "assessmentPlanId", assessmentPlanId,
                        "fieldPlanFacilityId", fieldPlanFacilityId,
                        "userId", userId,
                        "now", now
                )
        );
    }
}
