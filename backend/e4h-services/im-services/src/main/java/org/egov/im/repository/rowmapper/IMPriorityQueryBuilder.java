package org.egov.im.repository.rowmapper;

import org.egov.im.web.models.IMPrioritySearchCriteria;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public class IMPriorityQueryBuilder {

    private static final String BASE_QUERY =
            "SELECT priority FROM im_services_priority WHERE tenantId = ? ";

    public String getSearchQuery(IMPrioritySearchCriteria criteria, List<Object> preparedStmtList) {
        StringBuilder query = new StringBuilder(BASE_QUERY);

        preparedStmtList.add(criteria.getTenantId());

        query.append(" AND (incidentType = ? OR incidentType IS NULL) ");
        preparedStmtList.add(criteria.getIncidentType());

        query.append(" AND (incidentSubType = ? OR incidentSubType IS NULL) ");
        preparedStmtList.add(criteria.getIncidentSubType());

        query.append(" AND (systemFunctional = ? OR systemFunctional IS NULL) ");
        preparedStmtList.add(criteria.getSystemFunctional());

        return query.toString();
    }
}
