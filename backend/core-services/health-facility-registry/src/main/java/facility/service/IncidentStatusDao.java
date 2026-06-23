package facility.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

/**
 * Reads incident state to derive a facility's solar panel status.
 *
 * <p>Mirrors the rule used by the im-services-analytics Kafka listener
 * ({@code org.selco.e4h.service.IncidentService}): a facility is NON_FUNCTIONAL when at least one
 * OPEN incident for its boundary code is NON_FUNCTIONAL, otherwise FUNCTIONAL.
 */
@Repository
@RequiredArgsConstructor
@Slf4j
public class IncidentStatusDao {

    private final JdbcTemplate jdbcTemplate;

    static final String FUNCTIONAL = "FUNCTIONAL";
    static final String NON_FUNCTIONAL = "NON_FUNCTIONAL";

    /**
     * Open == not in the closed set. Kept in sync with im-services-analytics
     * {@code IncidentQueryBuilder.CLOSED_STATUSES}.
     */
    private static final String OPEN_NON_FUNCTIONAL_COUNT =
            "SELECT COUNT(*) FROM public.eg_incident_v2 " +
                    "WHERE boundarycode = ? " +
                    "  AND systemfunctional = ? " +
                    "  AND applicationstatus NOT IN " +
                    "      ('RESOLVED', 'CLOSEDAFTERRESOLUTION', 'REJECTED', 'CLOSEDAFTERREJECTION')";

    /**
     * Computes the solar panel status for the given facility boundary code.
     *
     * @param boundaryCode the facility's boundary code (matches {@code eg_incident_v2.boundarycode})
     * @return {@code NON_FUNCTIONAL} when any open incident is non-functional, otherwise {@code FUNCTIONAL}.
     *         Falls back to {@code FUNCTIONAL} when the boundary code is blank or the lookup fails.
     */
    public String resolveSolarPanelStatus(String boundaryCode) {
        if (boundaryCode == null || boundaryCode.isBlank()) {
            return FUNCTIONAL;
        }
        try {
            Integer openNonFunctional = jdbcTemplate.queryForObject(
                    OPEN_NON_FUNCTIONAL_COUNT,
                    new Object[]{boundaryCode, NON_FUNCTIONAL},
                    Integer.class);
            boolean hasNonFunctional = openNonFunctional != null && openNonFunctional > 0;
            return hasNonFunctional ? NON_FUNCTIONAL : FUNCTIONAL;
        } catch (Exception e) {
            log.warn("Unable to derive solar panel status from incidents for boundaryCode={}; defaulting to {}: {}",
                    boundaryCode, FUNCTIONAL, e.getMessage());
            return FUNCTIONAL;
        }
    }
}
