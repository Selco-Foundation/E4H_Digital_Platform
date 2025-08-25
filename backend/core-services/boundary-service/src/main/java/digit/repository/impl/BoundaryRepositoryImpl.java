package digit.repository.impl;


import com.fasterxml.jackson.databind.ObjectMapper;
import digit.config.ApplicationProperties;
import digit.kafka.Producer;
import digit.repository.BoundaryRepository;
import digit.repository.querybuilder.BoundaryEntityQueryBuilder;
import digit.repository.rowmapper.BoundaryEntityRowMapper;
import digit.web.models.Boundary;
import digit.web.models.BoundaryRequest;
import digit.web.models.BoundarySearchCriteria;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.web.client.RestTemplate;

import java.util.*;
import java.util.stream.Collectors;

@Repository
@Slf4j
public class BoundaryRepositoryImpl implements BoundaryRepository {

    private final ObjectMapper mapper;

    private final RestTemplate restTemplate;

    private final JdbcTemplate jdbcTemplate;

    private final BoundaryEntityRowMapper boundaryEntityRowMapper;

    private final BoundaryEntityQueryBuilder boundaryEntityQueryBuilder;

    private final Producer producer;

    private final ApplicationProperties applicationProperties;

    public BoundaryRepositoryImpl(ObjectMapper mapper , RestTemplate restTemplate , JdbcTemplate jdbcTemplate , BoundaryEntityRowMapper boundaryEntityRowMapper
            , BoundaryEntityQueryBuilder boundaryEntityQueryBuilder , Producer producer , ApplicationProperties applicationProperties) {
        this.mapper = mapper;
        this.restTemplate = restTemplate;
        this.jdbcTemplate = jdbcTemplate;
        this.boundaryEntityRowMapper = boundaryEntityRowMapper;
        this.boundaryEntityQueryBuilder = boundaryEntityQueryBuilder;
        this.producer = producer;
        this.applicationProperties = applicationProperties;
    }

    /**
     * This method implements boundary entity repository interface. In this implementation
     * it pushes the request to kafka for persister to pick it up and perform insert.
     * @param boundaryRequest
     */
    @Override
    public void create(BoundaryRequest boundaryRequest) {
        log.debug("Creating boundary");
        producer.push(applicationProperties.getCreateBoundaryTopic() , boundaryRequest);
        log.info("Pushed boundary create request to Kafka topic: {}", applicationProperties.getCreateBoundaryTopic());
    }

    /**
     * This method is used to search for boundary entity
     * @param boundarySearchCriteria
     * @return
     */
    @Override
    public List<Boundary> search(BoundarySearchCriteria boundarySearchCriteria) {
        log.debug("Searching boundaries with criteria: {}", boundarySearchCriteria);

        List<Object> preparedStmtList = new ArrayList<>();

        String query = boundaryEntityQueryBuilder.getBoundaryDataSearchQuery(boundarySearchCriteria , preparedStmtList);
        log.debug("Generated boundary search query: {}", query);
        log.debug("Prepared statement values: {}", preparedStmtList);

        List<Boundary> boundaryList = jdbcTemplate.query(query , preparedStmtList.toArray() , boundaryEntityRowMapper);
        log.info("Search completed. Found {} boundary records.", boundaryList.size());

        return boundaryList;
    }

    /**
     * This method implements boundary type hierarchy repository interface. In this implementation
     * it pushes the request to kafka for persister to pick it up and perform update.
     * @param boundaryRequest
     */
    @Override
    public void update(BoundaryRequest boundaryRequest) {
        log.debug("Updating boundary with request: {}", boundaryRequest);
        producer.push(applicationProperties.getUpdateBoundaryTopic() , boundaryRequest);
        log.info("Pushed boundary update request to Kafka topic: {}", applicationProperties.getUpdateBoundaryTopic());
    }

    /**
     * This method returns the set of codes for a given tenantId
     * @param tenantId
     * @return
     */
    public Set<String> getCodeListByTenantId(String tenantId) {
        log.debug("Fetching boundary codes for tenantId: {}", tenantId);
        // create a boundary search criteria object with the given tenantId
        BoundarySearchCriteria boundarySearchCriteria = new BoundarySearchCriteria();
        boundarySearchCriteria.setTenantId(tenantId);

        // get all the boundary entities for the given tenantId from the database
        List<Boundary> boundaryList = search(boundarySearchCriteria);

        // return the set of codes from the boundary entities
        return boundaryList.stream().map(Boundary::getCode).collect(Collectors.toSet());

    }


}