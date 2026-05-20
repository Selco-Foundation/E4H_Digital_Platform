package org.egov.project.repository;

import lombok.RequiredArgsConstructor;
import org.egov.project.web.models.FacilityProjectMapping;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * LLD: single SQL join for projectId/projectName by facility IDs (no field-planner calls).
 */
@Repository
@RequiredArgsConstructor
public class Co2ProjectLookupRepository {

    private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    private static final String SQL = """
            SELECT pf.facilityid AS facility_id,
                   p.id AS project_id,
                   p.name AS project_name
            FROM project_facility pf
            INNER JOIN project p ON p.id = pf.projectid AND p.tenantid = pf.tenantid
            WHERE pf.tenantid = :tenantId
              AND pf.facilityid IN (:facilityIds)
              AND (pf.isdeleted IS NULL OR pf.isdeleted = false)
              AND (p.isdeleted IS NULL OR p.isdeleted = false)
            """;

    public List<FacilityProjectMapping> fetchProjectsByFacilities(String tenantId, List<String> facilityIds) {
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("tenantId", tenantId)
                .addValue("facilityIds", facilityIds);
        return namedParameterJdbcTemplate.query(SQL, params, (rs, rowNum) -> FacilityProjectMapping.builder()
                .facilityId(rs.getString("facility_id"))
                .projectId(rs.getString("project_id"))
                .projectName(rs.getString("project_name"))
                .build());
    }
}
