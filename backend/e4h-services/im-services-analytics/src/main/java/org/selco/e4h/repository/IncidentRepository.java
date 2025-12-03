package org.selco.e4h.repository;

import lombok.extern.slf4j.Slf4j;
import org.selco.e4h.repository.querybuilder.IncidentQueryBuilder;
import org.selco.e4h.repository.rowmapper.IncidentStatusRowMapper;
import org.selco.e4h.repository.rowmapper.IncidentSystemFunctionalRowMapper;
import org.selco.e4h.web.models.IncidentStatusAgregation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.*;

@Slf4j
@Repository
public class IncidentRepository {

    private final IncidentQueryBuilder queryBuilder;
    private final IncidentStatusRowMapper incidentStatusRowMapper;

    private final IncidentSystemFunctionalRowMapper incidentSystemFunctionalRowMapper;
    private final JdbcTemplate jdbcTemplate;

    @Autowired
    public IncidentRepository(IncidentQueryBuilder queryBuilder, JdbcTemplate jdbcTemplate, IncidentStatusRowMapper incidentStatusRowMapper, IncidentSystemFunctionalRowMapper incidentSystemFunctionalRowMapper) {
        this.queryBuilder = queryBuilder;
        this.jdbcTemplate = jdbcTemplate;
        this.incidentStatusRowMapper = incidentStatusRowMapper;
        this.incidentSystemFunctionalRowMapper = incidentSystemFunctionalRowMapper;
    }

    public List<IncidentStatusAgregation> getStatusIncidentsAgregation(String boundaryCode) {
        List<Object> preparedStmtList = new ArrayList<>();
        String query = queryBuilder.getStatusIncidentOccurence(boundaryCode, preparedStmtList);
        List<IncidentStatusAgregation> statusAgregations = jdbcTemplate.query(query, incidentStatusRowMapper, preparedStmtList.toArray());
        log.info("Fetched incident status agregation list based on given tenant Id");
        return statusAgregations;
    }

    public List<IncidentStatusAgregation> getStatusSystemFunctional(String boundaryCode) {
        List<Object> preparedStmtList = new ArrayList<>();
        String query = queryBuilder.getStatusSystemFunctionalIncident(boundaryCode, preparedStmtList);
        List<IncidentStatusAgregation> systemFunctionalList = jdbcTemplate.query(query, incidentSystemFunctionalRowMapper, preparedStmtList.toArray());
        log.info("Fetched system functional list based on given tenant Id");
        return systemFunctionalList;
    }
}