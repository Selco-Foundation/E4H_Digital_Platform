package org.selco.e4h.repository.querybuilder;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Slf4j
@RequiredArgsConstructor
public class IncidentQueryBuilder {

    private static final String CLOSED_STATUSES =
            "'RESOLVED', 'CLOSEDAFTERRESOLUTION', 'REJECTED', 'CLOSEDAFTERREJECTION'";

    private static final String STATUS_COUNT_QUERY =
            "SELECT " +
                    "    boundarycode, " +
                    "    COUNT(*) AS total_occurrences, " +
                    "    SUM(CASE WHEN applicationstatus NOT IN (" +
                    "        " + CLOSED_STATUSES + ") " +
                    "    THEN 1 ELSE 0 END) AS total_open_occurrences, " +
                    "    SUM(CASE WHEN applicationstatus IN (" +
                    "        " + CLOSED_STATUSES + ") " +
                    "    THEN 1 ELSE 0 END) AS total_close_occurrences " +
                    "FROM public.eg_incident_v2 ";

    private static final String SYSTEM_FUNCTIONAL_STATUS =
            "SELECT id, systemfunctional " +
                    "FROM public.eg_incident_v2 " +
                    "WHERE applicationstatus NOT IN (" +
                    "  " + CLOSED_STATUSES +
                    ")";

    public String getStatusIncidentOccurence(String boundaryCode, List<Object> preparedStmtList) {
        StringBuilder queryBuilder = new StringBuilder(STATUS_COUNT_QUERY);
        if (boundaryCode != null && !boundaryCode.isEmpty()) {
            queryBuilder.append(" WHERE boundarycode =? ");
            preparedStmtList.add(boundaryCode);
        }
        queryBuilder.append("GROUP BY boundarycode;");

        return queryBuilder.toString();
    }

    /**
     * Oldest still-open ticket that reports the system as non-functional. This is the point in time
     * the facility stopped being functional: every later non-functional ticket is a symptom of an
     * outage that had already started, so the minimum {@code createdtime} is the one that matters.
     *
     * <p>Deliberately scoped to the same open/non-functional set as {@link #SYSTEM_FUNCTIONAL_STATUS}
     * so the timestamp can never contradict the {@code solarPanelStatus} derived alongside it -
     * {@code MIN} over an empty set yields {@code NULL}, which is exactly the value a functional
     * facility must publish.
     */
    private static final String OLDEST_OPEN_NON_FUNCTIONAL_CREATED_TIME =
            "SELECT MIN(createdtime) " +
                    "FROM public.eg_incident_v2 " +
                    "WHERE systemfunctional = 'NON_FUNCTIONAL' " +
                    "  AND applicationstatus NOT IN (" +
                    "  " + CLOSED_STATUSES +
                    ")";

    public String getStatusSystemFunctionalIncident(String boundaryCode, List<Object> preparedStmtList) {
        StringBuilder queryBuilder = new StringBuilder(SYSTEM_FUNCTIONAL_STATUS);
        if (boundaryCode != null && !boundaryCode.isEmpty()) {
            queryBuilder.append(" AND boundarycode =? ");
            preparedStmtList.add(boundaryCode);
        }

        return queryBuilder.toString();
    }

    public String getOldestOpenNonFunctionalCreatedTime(String boundaryCode, List<Object> preparedStmtList) {
        StringBuilder queryBuilder = new StringBuilder(OLDEST_OPEN_NON_FUNCTIONAL_CREATED_TIME);
        if (boundaryCode != null && !boundaryCode.isEmpty()) {
            queryBuilder.append(" AND boundarycode =? ");
            preparedStmtList.add(boundaryCode);
        }

        return queryBuilder.toString();
    }
}
