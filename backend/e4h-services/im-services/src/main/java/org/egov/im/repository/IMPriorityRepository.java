package org.egov.im.repository;

import lombok.extern.slf4j.Slf4j;
import org.egov.im.repository.rowmapper.IMPriorityRowMapper;
import org.egov.im.repository.rowmapper.IMPriorityQueryBuilder;
import org.egov.im.web.models.IMPrioritySearchCriteria;
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

    @Autowired
    public IMPriorityRepository(JdbcTemplate jdbcTemplate,
                                IMPriorityQueryBuilder queryBuilder,
                                IMPriorityRowMapper rowMapper) {
        this.jdbcTemplate = jdbcTemplate;
        this.queryBuilder = queryBuilder;
        this.rowMapper = rowMapper;
    }

    public Priority getPriority(IMPrioritySearchCriteria criteria) {
        List<Object> preparedStmtList = new ArrayList<>();
        String query = queryBuilder.getSearchQuery(criteria, preparedStmtList);
        log.info("Executing IMPriority Query: {} | Params: {}", query, preparedStmtList);
        List<Priority> priorityList = jdbcTemplate.query(query, rowMapper, preparedStmtList.toArray());
        return  getMaxPriority(priorityList);
    }

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
