package org.egov.infra.mdms.service;

import lombok.Builder;
import lombok.extern.slf4j.Slf4j;
import org.egov.common.utils.MultiStateInstanceUtil;
import org.egov.infra.mdms.config.ApplicationConfig;
import org.egov.infra.mdms.model.*;
import org.egov.infra.mdms.repository.SchemaDefinitionRepository;
import org.egov.infra.mdms.service.enrichment.SchemaDefinitionEnricher;
import org.egov.infra.mdms.service.validator.SchemaDefinitionValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;

@Service
@Builder
@Slf4j
public class SchemaDefinitionService {

    private SchemaDefinitionRepository schemaDefinitionRepository;
    private ApplicationConfig applicationConfig;
    private SchemaDefinitionEnricher schemaDefinitionEnricher;
    private SchemaDefinitionValidator schemaDefinitionValidator;
    private MultiStateInstanceUtil multiStateInstanceUtil;

    @Autowired
    public SchemaDefinitionService(SchemaDefinitionRepository schemaDefinitionRepository, ApplicationConfig applicationConfig,
                                   SchemaDefinitionEnricher schemaDefinitionEnricher, SchemaDefinitionValidator schemaDefinitionValidator, MultiStateInstanceUtil multiStateInstanceUtil){
        this.schemaDefinitionRepository = schemaDefinitionRepository;
        this.applicationConfig = applicationConfig;
        this.schemaDefinitionEnricher = schemaDefinitionEnricher;
        this.schemaDefinitionValidator = schemaDefinitionValidator;
        this.multiStateInstanceUtil = multiStateInstanceUtil;
    }

    /**
     * This method processes requests for schema definition creation.
     * @param schemaDefinitionRequest
     * @return
     */
    public List<SchemaDefinition> create(SchemaDefinitionRequest schemaDefinitionRequest) {
        log.trace("SchemaDefinitionService.create: method invoked");
        // Set incoming tenantId as state level tenantId as schema is always created at state level
        String tenantId = schemaDefinitionRequest.getSchemaDefinition().getTenantId();
        String code = schemaDefinitionRequest.getSchemaDefinition().getCode();
        log.info("Processing schema definition create request for tenant: {}, code: {}", tenantId, code);
        
        String stateLevelTenantId = multiStateInstanceUtil.getStateLevelTenant(tenantId);
        log.debug("Converting tenantId: {} to state level tenantId: {}", tenantId, stateLevelTenantId);
        schemaDefinitionRequest.getSchemaDefinition().setTenantId(stateLevelTenantId);

        // Validate schema create request
        log.debug("Validating schema definition create request");
        schemaDefinitionValidator.validateCreateRequest(schemaDefinitionRequest);

        // Enrich schema create request
        log.debug("Enriching schema definition create request");
        schemaDefinitionEnricher.enrichCreateRequest(schemaDefinitionRequest);

        // Invoke repository method to emit schema creation event
        log.debug("Publishing schema definition create request to Kafka");
        schemaDefinitionRepository.create(schemaDefinitionRequest);
        log.info("Schema definition create request processed successfully for tenant: {}, code: {}", tenantId, code);

        return Arrays.asList(schemaDefinitionRequest.getSchemaDefinition());
    }

    /**
     * This method processes the requests for schema definition search.
     * @param schemaDefSearchRequest
     * @return
     */
    public List<SchemaDefinition> search(SchemaDefSearchRequest schemaDefSearchRequest) {
        log.trace("SchemaDefinitionService.search: method invoked");
        // Set incoming tenantId as state level tenantId as schema is created at state level
        String tenantId = schemaDefSearchRequest.getSchemaDefCriteria().getTenantId();
        log.info("Processing schema definition search request for tenant: {}", tenantId);
        
        String stateLevelTenantId = multiStateInstanceUtil.getStateLevelTenant(tenantId);
        log.debug("Converting tenantId: {} to state level tenantId: {}", tenantId, stateLevelTenantId);
        schemaDefSearchRequest.getSchemaDefCriteria().setTenantId(stateLevelTenantId);

        // Fetch schema definitions based on the given criteria
        log.debug("Fetching schema definitions from repository");
        List<SchemaDefinition> schemaDefinitions = schemaDefinitionRepository.search(schemaDefSearchRequest.getSchemaDefCriteria());
        log.debug("Repository returned schema definitions count: {}", schemaDefinitions != null ? schemaDefinitions.size() : 0);
        log.info("Schema definition search request processed successfully");

        return schemaDefinitions;
    }

}