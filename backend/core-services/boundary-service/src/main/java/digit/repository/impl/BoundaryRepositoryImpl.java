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
        log.info("Creating boundary entities directly in database");
        
        String insertQuery = "INSERT INTO boundary (id, tenantId, code, geometry, additionalDetails, " +
                "createdBy, lastModifiedBy, createdTime, lastModifiedTime) " +
                "VALUES (?, ?, ?, ?::jsonb, ?::jsonb, ?, ?, ?, ?)";
        
        try {
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
                            log.error("Error setting parameters for boundary insert: {}", e.getMessage(), e);
                            throw new RuntimeException("Error preparing boundary insert statement", e);
                        }
                    }
            );
            
            log.info("Successfully created {} boundary entities", boundaryRequest.getBoundary().size());
        } catch (Exception e) {
            log.error("Error creating boundary entities: {}", e.getMessage(), e);
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

        List<Object> preparedStmtList = new ArrayList<>();

        String query = boundaryEntityQueryBuilder.getBoundaryDataSearchQuery(boundarySearchCriteria , preparedStmtList);

        List<Boundary> boundaryList = jdbcTemplate.query(query , preparedStmtList.toArray() , boundaryEntityRowMapper);

        return boundaryList;
    }

    /**
     * This method implements boundary type hierarchy repository interface. In this implementation
     * it pushes the request to kafka for persister to pick it up and perform update.
     * @param boundaryRequest
     */
    @Override
    public void update(BoundaryRequest boundaryRequest) {
        producer.push(applicationProperties.getUpdateBoundaryTopic() , boundaryRequest);
    }

    /**
     * This method returns the set of codes for a given tenantId
     * @param tenantId
     * @return
     */
    public Set<String> getCodeListByTenantId(String tenantId) {

        // create a boundary search criteria object with the given tenantId
        BoundarySearchCriteria boundarySearchCriteria = new BoundarySearchCriteria();
        boundarySearchCriteria.setTenantId(tenantId);

        // get all the boundary entities for the given tenantId from the database
        List<Boundary> boundaryList = search(boundarySearchCriteria);

        // return the set of codes from the boundary entities
        return boundaryList.stream().map(Boundary::getCode).collect(Collectors.toSet());

    }

    /**
     * Helper method to convert JsonNode to String for JSONB database fields
     * @param jsonNode
     * @return JSON string representation or null if jsonNode is null
     */
    private String jsonNodeToString(JsonNode jsonNode) {
        if (jsonNode == null) {
            return null;
        }
        try {
            return mapper.writeValueAsString(jsonNode);
        } catch (JsonProcessingException e) {
            log.error("Error converting JsonNode to String: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to convert JsonNode to String", e);
        }
    }

}