package org.egov.im.repository;

import lombok.extern.slf4j.Slf4j;
import org.egov.im.repository.rowmapper.IMPriorityRowMapper;
import org.egov.im.repository.rowmapper.IMPriorityQueryBuilder;
import org.egov.im.web.models.PrioritySearchCriteria;
import org.egov.im.web.models.Priority;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Repository
public class IMPriorityRepository {

    private final JdbcTemplate jdbcTemplate;
    private final IMPriorityQueryBuilder queryBuilder;
    private final IMPriorityRowMapper rowMapper;

    // Constructor-based dependency injection
    public IMPriorityRepository(JdbcTemplate jdbcTemplate,
                                IMPriorityQueryBuilder queryBuilder,
                                IMPriorityRowMapper rowMapper) {
        this.jdbcTemplate = jdbcTemplate;
        this.queryBuilder = queryBuilder;
        this.rowMapper = rowMapper;
    }
    /**
     * Fetch priorities based on (incidentType + incidentSubType) for a tenant
     */
    public List<Priority> getPrioritiesByTypeAndSubtype(String tenantId, String incidentType, String incidentSubType) {
        if (tenantId == null || incidentType == null || incidentSubType == null) return new ArrayList<>();

        List<Object> preparedStmtList = new ArrayList<>();
        preparedStmtList.add(tenantId); // always first

        PrioritySearchCriteria criteria = new PrioritySearchCriteria();
        criteria.setIncidentType(incidentType);
        criteria.setIncidentSubType(incidentSubType);

        String query = queryBuilder.getSearchQuery(criteria, preparedStmtList, "typeAndSubtype");
        return jdbcTemplate.query(query, rowMapper, preparedStmtList.toArray());
    }

    /**
     * Fetch priorities based on systemFunctional for a tenant
     */
    public List<Priority> getPrioritiesBySystemFunctional(String tenantId, String systemFunctional) {
        if (tenantId == null || systemFunctional == null) return new ArrayList<>();

        List<Object> preparedStmtList = new ArrayList<>();
        preparedStmtList.add(tenantId); // always first

        PrioritySearchCriteria criteria = new PrioritySearchCriteria();
        criteria.setSystemFunctional(systemFunctional);

        String query = queryBuilder.getSearchQuery(criteria, preparedStmtList, "systemFunctional");
        return jdbcTemplate.query(query, rowMapper, preparedStmtList.toArray());
    }

    /**
     * Utility to get max priority from a list
     */
    public Priority getMaxPriority(List<Priority> priorities) {
        return priorities.stream()
                .max((p1, p2) -> Integer.compare(getPriorityRank(p1), getPriorityRank(p2)))
                .orElse(null);
    }

    private int getPriorityRank(Priority priority) {
        if (priority == null) return 0;
        switch (priority) {
            case HIGH: return 3;
            case MEDIUM: return 2;
            case LOW: return 1;
            default: return 0;
        }
    }
}
