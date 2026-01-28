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

    /**
     * Checks whether any facility exists with the given HFR ID or NIN ID in a specific tenant.
     * This ensures uniqueness of external identifiers.
     *
     * @param hfrId     HFR (Health Facility Registry) ID to check
     * @param ninId     NIN (National Identification Number) ID to check
     * @param tenantId  Tenant under which the search is scoped
     * @return true if a facility with given HFR ID or NIN ID exists
     */
    public boolean existsByHfrIdOrNinId(String hfrId, String ninId, String tenantId) {
        StringBuilder sql = new StringBuilder("SELECT EXISTS (SELECT 1 FROM facility WHERE tenant_id = ?");
        List<Object> params = new ArrayList<>();
        params.add(tenantId);

        // Dynamically add condition for hfrId if provided
        if (hfrId != null && !hfrId.isBlank()) {
            sql.append(" AND hfr_id = ?");
            params.add(hfrId);
        }

        // Add condition for ninId, with proper SQL handling depending on hfrId presence
        if (ninId != null && !ninId.isBlank()) {
            sql.append(hfrId != null && !hfrId.isBlank() ? " OR" : " AND");
            sql.append(" nin_id = ?");
            params.add(ninId);
        }

        sql.append(")");

        Boolean exists = jdbcTemplate.queryForObject(
                sql.toString(),
                params.toArray(),
                Boolean.class
        );

        // Use safe Boolean comparison to avoid null-related bugs
        return Boolean.TRUE.equals(exists);
    }

    /**
     * Checks whether a facility exists with the same name and boundary code
     * in a given tenant. This enforces uniqueness constraint on name + boundary.
     *
     * @param tenantId      Tenant ID to scope the query
     * @param facilityName  Facility name to match
     * @param boundaryCode  Boundary code to match
     * @return true if such a facility exists
     */
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
