package facility.service;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class FacilityQueryDao {

    private final JdbcTemplate jdbcTemplate;

    public boolean existsByHfrIdOrNinId(String hfrId, String ninId, String tenantId) {
        StringBuilder sql = new StringBuilder("SELECT EXISTS (SELECT 1 FROM facility WHERE tenant_id = ?");
        List<Object> params = new ArrayList<>();
        params.add(tenantId);

        if (hfrId != null && !hfrId.isBlank()) {
            sql.append(" AND facility_details ->> 'hfr_id' = ?");
            params.add(hfrId);
        }

        if (ninId != null && !ninId.isBlank()) {
            sql.append(hfrId != null && !hfrId.isBlank() ? " OR" : " AND");
            sql.append(" facility_details ->> 'nin_id' = ?");
            params.add(ninId);
        }

        sql.append(")");

        Boolean exists = jdbcTemplate.queryForObject(
                sql.toString(),
                params.toArray(),
                Boolean.class
        );

        return Boolean.TRUE.equals(exists);
    }


    public boolean existsByFacilityNameAndBoundary(String tenantId, String facilityName, String boundaryCode) {
        String sql = """
        SELECT EXISTS (
            SELECT 1 FROM facility
            WHERE tenant_id = ? AND facility_name = ? AND boundary_code = ?
        )
    """;

        Boolean exists = jdbcTemplate.queryForObject(
                sql,
                new Object[]{tenantId, facilityName, boundaryCode},
                Boolean.class
        );

        return Boolean.TRUE.equals(exists);
    }


}
