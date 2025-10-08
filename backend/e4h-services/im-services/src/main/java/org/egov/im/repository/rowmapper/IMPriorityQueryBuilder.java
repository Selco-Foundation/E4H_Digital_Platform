package org.egov.im.repository.rowmapper;

import org.egov.im.web.models.PrioritySearchCriteria;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

import java.util.List;

@Repository
public class IMPriorityQueryBuilder {

    private static final String BASE_QUERY =
            "SELECT priority" +
                    "FROM {schema}.im_services_priority ";


    public String getSearchQuery(PrioritySearchCriteria criteria, List<Object> preparedStmtList, String queryType) {
        StringBuilder query = new StringBuilder(BASE_QUERY);
        query.append(" WHERE tenantId = ? "); // tenantId is always required

        if ("typeAndSubtype".equals(queryType)) {
            if (!StringUtils.isEmpty(criteria.getIncidentType()) && !StringUtils.isEmpty(criteria.getIncidentSubType())) {
                query.append(" AND incidentType = ? AND incidentSubType = ? ");
            }
        } else if ("systemFunctional".equals(queryType)) {
            if (!StringUtils.isEmpty(criteria.getSystemFunctional())) {
                query.append(" AND systemFunctional = ? ");
            }
        }
        return query.toString();
    }
}
