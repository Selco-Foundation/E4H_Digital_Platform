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
     * Creation time of the oldest still-open non-functional incident - i.e. when the facility went
     * non-functional. Every later non-functional ticket is a symptom of an outage that had already
     * started, so the minimum is the one that matters.
     *
     * <p>Scoped to the same open/non-functional set as {@link #OPEN_NON_FUNCTIONAL_COUNT} so the two
     * can never disagree: {@code MIN} over an empty set yields {@code NULL}, exactly the value a
     * functional facility must publish.
     */
    private static final String OLDEST_OPEN_NON_FUNCTIONAL_CREATED_TIME =
            "SELECT MIN(createdtime) FROM public.eg_incident_v2 " +
                    "WHERE boundarycode = ? " +
                    "  AND systemfunctional = ? " +
                    "  AND applicationstatus NOT IN " +
                    "      ('RESOLVED', 'CLOSEDAFTERRESOLUTION', 'REJECTED', 'CLOSEDAFTERREJECTION')";

    /**
     * A facility's solar panel state as derived from its incidents.
     *
     * @param status              {@code FUNCTIONAL} or {@code NON_FUNCTIONAL}
     * @param nonFunctionalSince  epoch millis the facility went non-functional, or {@code null} when
     *                            it is functional. Returned together with {@code status} so callers
     *                            cannot pair a stale timestamp with a fresh status.
     */
    public record SolarPanelState(String status, Long nonFunctionalSince) {

        public boolean isNonFunctional() {
            return NON_FUNCTIONAL.equals(status);
        }
    }

    /**
     * Computes the solar panel status for the given facility boundary code.
     *
     * @param boundaryCode the facility's boundary code (matches {@code eg_incident_v2.boundarycode})
     * @return {@code NON_FUNCTIONAL} when any open incident is non-functional, otherwise {@code FUNCTIONAL}.
     *         Falls back to {@code FUNCTIONAL} when the boundary code is blank or the lookup fails.
     */
    public String resolveSolarPanelStatus(String boundaryCode) {
        return resolveSolarPanelState(boundaryCode).status();
    }

    /**
     * Computes the solar panel status <em>and</em> the time the facility went non-functional.
     *
     * @return a {@link SolarPanelState}; falls back to {@code FUNCTIONAL} with a null timestamp when
     *         the boundary code is blank or the lookup fails.
     */
    public SolarPanelState resolveSolarPanelState(String boundaryCode) {
        if (boundaryCode == null || boundaryCode.isBlank()) {
            return new SolarPanelState(FUNCTIONAL, null);
        }
        try {
            Integer openNonFunctional = jdbcTemplate.queryForObject(
                    OPEN_NON_FUNCTIONAL_COUNT,
                    new Object[]{boundaryCode, NON_FUNCTIONAL},
                    Integer.class);
            boolean hasNonFunctional = openNonFunctional != null && openNonFunctional > 0;
            if (!hasNonFunctional) {
                return new SolarPanelState(FUNCTIONAL, null);
            }
            Long nonFunctionalSince = jdbcTemplate.queryForObject(
                    OLDEST_OPEN_NON_FUNCTIONAL_CREATED_TIME,
                    new Object[]{boundaryCode, NON_FUNCTIONAL},
                    Long.class);
            return new SolarPanelState(NON_FUNCTIONAL, nonFunctionalSince);
        } catch (Exception e) {
            log.warn("Unable to derive solar panel status from incidents for boundaryCode={}; defaulting to {}: {}",
                    boundaryCode, FUNCTIONAL, e.getMessage());
            return new SolarPanelState(FUNCTIONAL, null);
        }
    }
}
