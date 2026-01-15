package digit.repository.impl;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.core.JsonProcessingException;
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

import java.sql.PreparedStatement;
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
     * it directly inserts boundary entities into the database using batch operation.
     * @param boundaryRequest
     */
    @Override
    public void create(BoundaryRequest boundaryRequest) {
        log.trace("create method invoked");
        int boundaryCount = boundaryRequest.getBoundary() != null ? boundaryRequest.getBoundary().size() : 0;
        log.info("Creating boundary entities directly in database, boundary count={}", boundaryCount);
        
        String insertQuery = "INSERT INTO boundary (id, tenantId, code, geometry, additionalDetails, " +
                "createdBy, lastModifiedBy, createdTime, lastModifiedTime) " +
                "VALUES (?, ?, ?, ?::jsonb, ?::jsonb, ?, ?, ?, ?)";
        
        try {
            log.debug("Executing batch insert query for {} boundaries", boundaryCount);
            jdbcTemplate.batchUpdate(
                    insertQuery, boundaryRequest.getBoundary(),
                    boundaryRequest.getBoundary().size(),
                    (PreparedStatement ps, Boundary boundary) -> {
                        try {
                            int index = 1;
                            ps.setString(index++, boundary.getId());
                            ps.setString(index++, boundary.getTenantId());
                            ps.setString(index++, boundary.getCode());
                            ps.setString(index++, jsonNodeToString(boundary.getGeometry()));
                            ps.setString(index++, jsonNodeToString(boundary.getAdditionalDetails()));
                            ps.setString(index++, boundary.getAuditDetails().getCreatedBy());
                            ps.setString(index++, boundary.getAuditDetails().getLastModifiedBy());
                            ps.setLong(index++, boundary.getAuditDetails().getCreatedTime());
                            ps.setLong(index, boundary.getAuditDetails().getLastModifiedTime());
                        } catch (Exception e) {
                            log.error("Error setting parameters for boundary insert, code={}: {}", 
                                    boundary.getCode(), e.getMessage(), e);
                            throw new RuntimeException("Error preparing boundary insert statement", e);
                        }
                    }
            );
            
            log.info("Successfully created {} boundary entities", boundaryCount);
        } catch (Exception e) {
            log.error("Error creating boundary entities, boundary count={}: {}", boundaryCount, e.getMessage(), e);
            throw new RuntimeException("Failed to create boundary entities", e);
        }
    }

    /**
     * This method is used to search for boundary entity
     * @param boundarySearchCriteria
     * @return
     */
    @Override
    public List<Boundary> search(BoundarySearchCriteria boundarySearchCriteria) {
        log.trace("search method invoked");
        log.debug("Searching boundaries, tenantId={}, codes count={}", 
                boundarySearchCriteria.getTenantId(),
                boundarySearchCriteria.getCodes() != null ? boundarySearchCriteria.getCodes().size() : 0);

        List<Object> preparedStmtList = new ArrayList<>();

        String query = boundaryEntityQueryBuilder.getBoundaryDataSearchQuery(boundarySearchCriteria , preparedStmtList);
        log.debug("Executing boundary search query");

        List<Boundary> boundaryList = jdbcTemplate.query(query , preparedStmtList.toArray() , boundaryEntityRowMapper);
        log.debug("Boundary search query executed, found {} boundaries", boundaryList.size());

        return boundaryList;
    }

    /**
     * This method implements boundary type hierarchy repository interface. In this implementation
     * it pushes the request to kafka for persister to pick it up and perform update.
     * @param boundaryRequest
     */
    @Override
    public void update(BoundaryRequest boundaryRequest) {
        log.trace("update method invoked");
        int boundaryCount = boundaryRequest.getBoundary() != null ? boundaryRequest.getBoundary().size() : 0;
        log.debug("Publishing boundary update request to Kafka, boundary count={}, topic={}", 
                boundaryCount, applicationProperties.getUpdateBoundaryTopic());
        producer.push(applicationProperties.getUpdateBoundaryTopic() , boundaryRequest);
        log.debug("Boundary update request published to Kafka successfully");
    }

    /**
     * This method returns the set of codes for a given tenantId
     * @param tenantId
     * @return
     */
    public Set<String> getCodeListByTenantId(String tenantId) {
        log.trace("getCodeListByTenantId method invoked, tenantId={}", tenantId);

        // create a boundary search criteria object with the given tenantId
        BoundarySearchCriteria boundarySearchCriteria = new BoundarySearchCriteria();
        boundarySearchCriteria.setTenantId(tenantId);

        // get all the boundary entities for the given tenantId from the database
        List<Boundary> boundaryList = search(boundarySearchCriteria);
        log.debug("Retrieved {} boundaries for tenantId={}", boundaryList.size(), tenantId);

        // return the set of codes from the boundary entities
        Set<String> codes = boundaryList.stream().map(Boundary::getCode).collect(Collectors.toSet());
        log.debug("Extracted {} unique codes for tenantId={}", codes.size(), tenantId);
        return codes;

    }

    /**
     * Helper method to convert JsonNode to String for JSONB database fields
     * @param jsonNode
     * @return JSON string representation or null if jsonNode is null
     */
    private String jsonNodeToString(JsonNode jsonNode) {
        log.trace("jsonNodeToString method invoked");
        if (jsonNode == null) {
            log.debug("JsonNode is null, returning null");
            return null;
        }
        try {
            String result = mapper.writeValueAsString(jsonNode);
            log.debug("Successfully converted JsonNode to String, length={}", result.length());
            return result;
        } catch (JsonProcessingException e) {
            log.error("Error converting JsonNode to String: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to convert JsonNode to String", e);
        }
    }

}