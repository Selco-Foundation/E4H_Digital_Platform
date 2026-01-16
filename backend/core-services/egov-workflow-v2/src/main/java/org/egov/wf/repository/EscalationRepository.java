package org.egov.wf.repository;


import org.egov.wf.repository.querybuilder.EscalationQueryBuilder;
import org.egov.wf.web.models.EscalationSearchCriteria;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.SingleColumnRowMapper;
import org.springframework.stereotype.Component;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

@Component
@Slf4j
public class EscalationRepository {



    private JdbcTemplate jdbcTemplate;

    private EscalationQueryBuilder queryBuilder;

    @Autowired
    public EscalationRepository(JdbcTemplate jdbcTemplate, EscalationQueryBuilder queryBuilder) {
        this.jdbcTemplate = jdbcTemplate;
        this.queryBuilder = queryBuilder;
    }


    /**
     * Fetches uuids that haas to be escalated
     * @param criteria
     * @return
     */
    public List<String> getBusinessIds(EscalationSearchCriteria criteria){
        log.trace("Entering getBusinessIds method");
        log.info("Fetching business IDs for escalation - tenantId: {}, businessService: {}", 
                criteria.getTenantId(), criteria.getBusinessService());

        List<Object> preparedStmtList = new ArrayList<>();
        String query = queryBuilder.getEscalationQuery(criteria, preparedStmtList);
        log.debug("Escalation query: {} with params: {}", query, preparedStmtList);
        
        List<String> businessIds = jdbcTemplate.query(query, preparedStmtList.toArray(),  new SingleColumnRowMapper<>(String.class));
        log.info("Retrieved {} business ID(s) for escalation", businessIds != null ? businessIds.size() : 0);
        log.trace("Exiting getBusinessIds method");
        return  businessIds;

    }


}
