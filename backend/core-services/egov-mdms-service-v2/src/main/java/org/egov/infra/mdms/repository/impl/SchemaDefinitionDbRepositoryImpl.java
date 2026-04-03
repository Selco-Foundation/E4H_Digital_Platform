package org.egov.infra.mdms.repository.impl;

import lombok.extern.slf4j.Slf4j;
import org.egov.infra.mdms.config.ApplicationConfig;
import org.egov.infra.mdms.model.SchemaDefCriteria;
import org.egov.infra.mdms.model.SchemaDefinition;
import org.egov.infra.mdms.model.SchemaDefinitionRequest;
import org.egov.infra.mdms.producer.Producer;
import org.egov.infra.mdms.repository.SchemaDefinitionRepository;
import org.egov.infra.mdms.repository.querybuilder.SchemaDefinitionQueryBuilder;
import org.egov.infra.mdms.repository.rowmapper.SchemaDefinitionRowMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import java.util.ArrayList;
import java.util.List;

@Repository
@Slf4j
public class SchemaDefinitionDbRepositoryImpl implements SchemaDefinitionRepository {

    private Producer producer;

    private JdbcTemplate jdbcTemplate;

    private ApplicationConfig applicationConfig;

    private SchemaDefinitionQueryBuilder schemaDefinitionQueryBuilder;

    private SchemaDefinitionRowMapper rowMapper;

    @Autowired
    public SchemaDefinitionDbRepositoryImpl(Producer producer, JdbcTemplate jdbcTemplate,
                                            ApplicationConfig applicationConfig, SchemaDefinitionRowMapper rowMapper, SchemaDefinitionQueryBuilder schemaDefinitionQueryBuilder){
        this.producer = producer;
        this.jdbcTemplate = jdbcTemplate;
        this.applicationConfig = applicationConfig;
        this.rowMapper = rowMapper;
        this.schemaDefinitionQueryBuilder = schemaDefinitionQueryBuilder;
    }


    /**
     * This method emits schema definition create request on kafka for async persistence
     * @param schemaDefinitionRequest
     */
    @Override
    public void create(SchemaDefinitionRequest schemaDefinitionRequest) {
        log.trace("SchemaDefinitionDbRepositoryImpl.create: method invoked");
        String tenantId = schemaDefinitionRequest.getSchemaDefinition() != null ? schemaDefinitionRequest.getSchemaDefinition().getTenantId() : "null";
        String code = schemaDefinitionRequest.getSchemaDefinition() != null ? schemaDefinitionRequest.getSchemaDefinition().getCode() : "null";
        log.info("Publishing schema definition create request to Kafka for tenant: {}, code: {}", tenantId, code);
        
        try {
            producer.push(applicationConfig.getSaveSchemaDefinitionTopicName(), schemaDefinitionRequest);
            log.debug("Schema definition create request published successfully to topic: {}", applicationConfig.getSaveSchemaDefinitionTopicName());
        } catch (Exception e) {
            log.error("Error publishing schema definition create request to Kafka for tenant: {}, code: {}", tenantId, code, e);
            throw e;
        }
    }

    /**
     * This method queries the database and returns schema definition search response based on
     * the provided criteria.
     * @param schemaDefCriteria
     */
    @Override
    public List<SchemaDefinition> search(SchemaDefCriteria schemaDefCriteria) {
        log.trace("SchemaDefinitionDbRepositoryImpl.search: method invoked");
        String tenantId = schemaDefCriteria != null ? schemaDefCriteria.getTenantId() : "null";
        log.info("Searching schema definitions from database for tenant: {}", tenantId);
        
        List<Object> preparedStatementList = new ArrayList<>();

        // Invoke query builder to generate query based on the provided criteria
        String query = schemaDefinitionQueryBuilder.getSchemaSearchQuery(schemaDefCriteria, preparedStatementList);
        log.debug("Generated schema definition search query with {} parameters", preparedStatementList.size());

        // Query the database to fetch schema definitions
        try {
            List<SchemaDefinition> result = jdbcTemplate.query(query, preparedStatementList.toArray(), rowMapper);
            log.debug("Schema definition search completed, records found: {}", result != null ? result.size() : 0);
            return result;
        } catch (Exception e) {
            log.error("Error searching schema definitions from database for tenant: {}", tenantId, e);
            throw e;
        }
    }

    /**
     * Skeleton method for update as update API has not been implemented
     * @param schemaDefinitionRequest
     */
    @Override
    public void update(SchemaDefinitionRequest schemaDefinitionRequest) {
    }

}