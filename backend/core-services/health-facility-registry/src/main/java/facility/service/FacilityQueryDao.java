package facility.service;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class FacilityQueryDao {

    private final JdbcTemplate jdbcTemplate;

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

    /**
     * Finds facilities in a tenant whose HFR ID, NIN ID or POC username matches any of the given
     * values. Every value is matched against all three columns, because each of them can become
     * the POC's HRMS username and HRMS does not allow two employees with the same username.
     *
     * @param tenantId          Tenant ID to scope the query
     * @param identifiers       Trimmed, non-blank values to look for
     * @param excludeFacilityId Facility id to exclude from the check, or null for create
     * @return matching facilities, with only id, name, HFR ID, NIN ID and POC username populated
     */
    public List<IdentifierOwner> findFacilitiesByIdentifiers(
            String tenantId, Collection<String> identifiers, String excludeFacilityId
    ) {
        if (identifiers == null || identifiers.isEmpty()) {
            return List.of();
        }
        String placeholders = String.join(",", Collections.nCopies(identifiers.size(), "?"));
        StringBuilder sql = new StringBuilder("""
        SELECT id, facility_name, TRIM(hfr_id) AS hfr_id, TRIM(nin_id) AS nin_id,
               TRIM(facility_poc_username) AS facility_poc_username
        FROM facility
        WHERE tenant_id = ?
        """);
        sql.append(" AND (TRIM(hfr_id) IN (").append(placeholders).append(")")
                .append(" OR TRIM(nin_id) IN (").append(placeholders).append(")")
                .append(" OR TRIM(facility_poc_username) IN (").append(placeholders).append("))");

        List<Object> params = new ArrayList<>();
        params.add(tenantId);
        params.addAll(identifiers);
        params.addAll(identifiers);
        params.addAll(identifiers);

        if (excludeFacilityId != null && !excludeFacilityId.isBlank()) {
            sql.append(" AND id <> ?");
            params.add(excludeFacilityId);
        }

        return jdbcTemplate.query(sql.toString(), (rs, rowNum) -> new IdentifierOwner(
                rs.getString("id"),
                rs.getString("facility_name"),
                rs.getString("hfr_id"),
                rs.getString("nin_id"),
                rs.getString("facility_poc_username")
        ), params.toArray());
    }

    /** The identifier columns of an existing facility, as returned by {@link #findFacilitiesByIdentifiers}. */
    public record IdentifierOwner(String facilityId, String facilityName, String hfrId, String ninId,
                                  String facilityPocUsername) {
    }
}
