package org.selco.e4h.repository.querybuilder;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Slf4j
@RequiredArgsConstructor
public class IncidentQueryBuilder {

    private static final String STATUS_COUNT_QUERY =
            "SELECT " +
                    "    tenantid, " +
                    "    COUNT(*) AS total_occurrences, " +
                    "    SUM(CASE WHEN applicationstatus IN (" +
                    "        'PENDINGFORASSIGNMENT', " +
                    "        'PENDING_ASSIGNMENT_SPARE_PART_NEEDED', " +
                    "        'PENDING_ASSIGNMENT_OUT_OF_WARRANTY', " +
                    "        'PENDING_RESOLUTION_SPARE_PART_NEEDED', " +
                    "        'PENDING_RESOLUTION_OUT_OF_WARRANTY', " +
                    "        'PENDINGRESOLUTION') " +
                    "    THEN 1 ELSE 0 END) AS total_open_occurrences, " +
                    "    SUM(CASE WHEN applicationstatus IN (" +
                    "        'RESOLVED', " +
                    "        'CLOSEDAFTERRESOLUTION', " +
                    "        'REJECTED', " +
                    "        'CLOSEDAFTERREJECTION') " +
                    "    THEN 1 ELSE 0 END) AS total_close_occurrences " +
                    "FROM public.eg_incident_v2 ";

    private static final String SYSTEM_FUNCTIONAL_STATUS =
            "SELECT id, systemfunctional " +
                    "FROM public.eg_incident_v2 " +
                    "WHERE applicationstatus IN ( " +
                    "  'PENDINGFORASSIGNMENT', " +
                    "  'PENDING_ASSIGNMENT_SPARE_PART_NEEDED', " +
                    "  'PENDING_ASSIGNMENT_OUT_OF_WARRANTY', " +
                    "  'PENDING_RESOLUTION_SPARE_PART_NEEDED', " +
                    "  'PENDING_RESOLUTION_OUT_OF_WARRANTY', " +
                    "  'PENDINGRESOLUTION' " +
                    ")";

    public String getStatusIncidentOccurence(String tenantId, List<Object> preparedStmtList) {
        StringBuilder queryBuilder = new StringBuilder(STATUS_COUNT_QUERY);
        if (tenantId != null && !tenantId.isEmpty()) {
            queryBuilder.append(" WHERE tenantid =? ");
            preparedStmtList.add(tenantId);
        }
        queryBuilder.append("GROUP BY tenantid;");

        return queryBuilder.toString();
    }

    public String getStatusSystemFunctionalIncident(String tenantId, List<Object> preparedStmtList) {
        StringBuilder queryBuilder = new StringBuilder(SYSTEM_FUNCTIONAL_STATUS);
        if (tenantId != null && !tenantId.isEmpty()) {
            queryBuilder.append(" AND tenantid =? ");
            preparedStmtList.add(tenantId);
        }

        return queryBuilder.toString();
    }
}
