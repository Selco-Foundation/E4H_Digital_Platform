package digit.repository.impl;

import digit.config.ApplicationProperties;
import digit.kafka.Producer;
import digit.repository.BoundaryHierarchyRepository;
import digit.repository.querybuilder.BoundaryHierarchyTypeQueryBuilder;
import digit.repository.rowmapper.BoundaryHierarchyTypeRowMapper;
import digit.web.models.BoundaryTypeHierarchyDefinition;
import digit.web.models.BoundaryTypeHierarchyRequest;
import digit.web.models.BoundaryTypeHierarchySearchCriteria;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Repository
public class BoundaryHierarchyRepositoryImpl implements BoundaryHierarchyRepository {

    private Producer producer;

    private BoundaryHierarchyTypeQueryBuilder boundaryHierarchyTypeQueryBuilder;

    private JdbcTemplate jdbcTemplate;

    private BoundaryHierarchyTypeRowMapper boundaryHierarchyTypeRowMapper;

    private ApplicationProperties applicationProperties;

    public BoundaryHierarchyRepositoryImpl(Producer producer, BoundaryHierarchyTypeQueryBuilder boundaryHierarchyTypeQueryBuilder,
                                           JdbcTemplate jdbcTemplate, BoundaryHierarchyTypeRowMapper boundaryHierarchyTypeRowMapper, ApplicationProperties applicationProperties) {
        this.producer = producer;
        this.boundaryHierarchyTypeQueryBuilder = boundaryHierarchyTypeQueryBuilder;
        this.jdbcTemplate = jdbcTemplate;
        this.boundaryHierarchyTypeRowMapper = boundaryHierarchyTypeRowMapper;
        this.applicationProperties = applicationProperties;
    }

    /**
     * This method implements boundary type hierarchy repository interface. In this implementation
     * it pushes the request to kafka for persister to pick it up and perform insert.
     * @param boundaryTypeHierarchyRequest
     */
    @Override
    public void create(BoundaryTypeHierarchyRequest boundaryTypeHierarchyRequest) {
        log.trace("create method invoked");
        String tenantId = boundaryTypeHierarchyRequest.getBoundaryHierarchy() != null 
                ? boundaryTypeHierarchyRequest.getBoundaryHierarchy().getTenantId() : null;
        String hierarchyType = boundaryTypeHierarchyRequest.getBoundaryHierarchy() != null 
                ? boundaryTypeHierarchyRequest.getBoundaryHierarchy().getHierarchyType() : null;
        log.debug("Creating boundary hierarchy, tenantId={}, hierarchyType={}, topic={}", 
                tenantId, hierarchyType, applicationProperties.getCreateBoundaryHierarchyTopic());
        producer.push(applicationProperties.getCreateBoundaryHierarchyTopic(), boundaryTypeHierarchyRequest);
        log.debug("Boundary hierarchy create request published to Kafka successfully");
    }

    /**
     * This method implements boundary type hierarchy repository interface. In this implementation
     * it pushes the request to kafka for persister to pick it up and perform update.
     * @param boundaryTypeHierarchyRequest
     */
    @Override
    public void update(BoundaryTypeHierarchyRequest boundaryTypeHierarchyRequest) {
        log.trace("update method invoked");
        String tenantId = boundaryTypeHierarchyRequest.getBoundaryHierarchy() != null 
                ? boundaryTypeHierarchyRequest.getBoundaryHierarchy().getTenantId() : null;
        String hierarchyType = boundaryTypeHierarchyRequest.getBoundaryHierarchy() != null 
                ? boundaryTypeHierarchyRequest.getBoundaryHierarchy().getHierarchyType() : null;
        log.debug("Updating boundary hierarchy, tenantId={}, hierarchyType={}, topic={}", 
                tenantId, hierarchyType, applicationProperties.getUpdateBoundaryHierarchyTopic());
        producer.push(applicationProperties.getUpdateBoundaryHierarchyTopic(), boundaryTypeHierarchyRequest);
        log.debug("Boundary hierarchy update request published to Kafka successfully");
    }

    /**
     * This method implements boundary type hierarchy repository interface. In this implementation
     * it creates query to search data in PostgreSQL database and returns the search response back
     * to the caller.
     * @param boundaryTypeHierarchySearchCriteria
     * @return
     */
    @Override
    public List<BoundaryTypeHierarchyDefinition> search(BoundaryTypeHierarchySearchCriteria boundaryTypeHierarchySearchCriteria) {
        log.trace("search method invoked");
        log.debug("Searching boundary hierarchy definitions, tenantId={}, hierarchyType={}", 
                boundaryTypeHierarchySearchCriteria.getTenantId(),
                boundaryTypeHierarchySearchCriteria.getHierarchyType());
        
        List<Object> preparedStmtList = new ArrayList<>();
        String query = boundaryHierarchyTypeQueryBuilder.getBoundaryHierarchyTypeSearchQuery(boundaryTypeHierarchySearchCriteria, preparedStmtList);
        log.debug("Executing boundary hierarchy definition search query");
        
        List<BoundaryTypeHierarchyDefinition> results = jdbcTemplate.query(query, preparedStmtList.toArray(), boundaryHierarchyTypeRowMapper);
        log.debug("Boundary hierarchy definition search query executed, found {} definitions", results.size());
        return results;
    }

}