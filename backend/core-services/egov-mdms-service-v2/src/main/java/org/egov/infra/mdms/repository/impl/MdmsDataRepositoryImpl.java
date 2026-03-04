package org.egov.infra.mdms.repository.impl;

import lombok.extern.slf4j.Slf4j;
import net.minidev.json.JSONArray;
import org.egov.infra.mdms.config.ApplicationConfig;
import org.egov.infra.mdms.model.Mdms;
import org.egov.infra.mdms.model.MdmsCriteria;
import org.egov.infra.mdms.model.MdmsCriteriaV2;
import org.egov.infra.mdms.model.MdmsRequest;
import org.egov.infra.mdms.producer.Producer;
import org.egov.infra.mdms.repository.MdmsDataRepository;
import org.egov.infra.mdms.repository.querybuilder.MdmsDataQueryBuilder;
import org.egov.infra.mdms.repository.querybuilder.MdmsDataQueryBuilderV2;
import org.egov.infra.mdms.repository.rowmapper.MdmsDataRowMapper;
import org.egov.infra.mdms.repository.rowmapper.MdmsDataRowMapperV2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Repository
@Slf4j
public class MdmsDataRepositoryImpl implements MdmsDataRepository {

    private Producer producer;
    private JdbcTemplate jdbcTemplate;
    private ApplicationConfig applicationConfig;
    private MdmsDataQueryBuilder mdmsDataQueryBuilder;
    private MdmsDataQueryBuilderV2 mdmsDataQueryBuilderV2;
    private MdmsDataRowMapperV2 mdmsDataRowMapperV2;
    private MdmsDataRowMapper mdmsDataRowMapper;

    @Autowired
    public MdmsDataRepositoryImpl(Producer producer, JdbcTemplate jdbcTemplate,
                                  ApplicationConfig applicationConfig, MdmsDataQueryBuilder mdmsDataQueryBuilder,
                                  MdmsDataRowMapperV2 mdmsDataRowMapperV2,
                                  MdmsDataQueryBuilderV2 mdmsDataQueryBuilderV2,
                                  MdmsDataRowMapper mdmsDataRowMapper) {
        this.producer = producer;
        this.jdbcTemplate = jdbcTemplate;
        this.applicationConfig = applicationConfig;
        this.mdmsDataQueryBuilder = mdmsDataQueryBuilder;
        this.mdmsDataRowMapper = mdmsDataRowMapper;
        this.mdmsDataRowMapperV2 = mdmsDataRowMapperV2;
        this.mdmsDataQueryBuilderV2 = mdmsDataQueryBuilderV2;
    }

    /**
     * @param mdmsRequest
     */
    @Override
    public void create(MdmsRequest mdmsRequest) {
        log.trace("MdmsDataRepositoryImpl.create: method invoked");
        String tenantId = mdmsRequest.getMdms() != null ? mdmsRequest.getMdms().getTenantId() : "null";
        String schemaCode = mdmsRequest.getMdms() != null ? mdmsRequest.getMdms().getSchemaCode() : "null";
        log.info("Publishing MDMS create request to Kafka for tenant: {}, schemaCode: {}", tenantId, schemaCode);
        
        try {
            producer.push(applicationConfig.getSaveMdmsDataTopicName(), mdmsRequest);
            log.debug("MDMS create request published successfully to topic: {}", applicationConfig.getSaveMdmsDataTopicName());
        } catch (Exception e) {
            log.error("Error publishing MDMS create request to Kafka for tenant: {}, schemaCode: {}", tenantId, schemaCode, e);
            throw e;
        }
    }

    /**
     * @param mdmsRequest
     */
    @Override
    public void update(MdmsRequest mdmsRequest) {
        log.trace("MdmsDataRepositoryImpl.update: method invoked");
        String tenantId = mdmsRequest.getMdms() != null ? mdmsRequest.getMdms().getTenantId() : "null";
        String schemaCode = mdmsRequest.getMdms() != null ? mdmsRequest.getMdms().getSchemaCode() : "null";
        String id = mdmsRequest.getMdms() != null ? mdmsRequest.getMdms().getId() : "null";
        log.info("Publishing MDMS update request to Kafka for tenant: {}, schemaCode: {}, id: {}", tenantId, schemaCode, id);
        
        try {
            producer.push(applicationConfig.getUpdateMdmsDataTopicName(), mdmsRequest);
            log.debug("MDMS update request published successfully to topic: {}", applicationConfig.getUpdateMdmsDataTopicName());
        } catch (Exception e) {
            log.error("Error publishing MDMS update request to Kafka for tenant: {}, schemaCode: {}, id: {}", tenantId, schemaCode, id, e);
            throw e;
        }
    }

    /**
     * @param mdmsCriteriaV2
     * @return
     */
    @Override
    public List<Mdms> searchV2(MdmsCriteriaV2 mdmsCriteriaV2) {
        log.trace("MdmsDataRepositoryImpl.searchV2: method invoked");
        String tenantId = mdmsCriteriaV2 != null ? mdmsCriteriaV2.getTenantId() : "null";
        String schemaCode = mdmsCriteriaV2 != null ? mdmsCriteriaV2.getSchemaCode() : "null";
        log.info("Searching MDMS data from database for tenant: {}, schemaCode: {}", tenantId, schemaCode);
        
        List<Object> preparedStmtList = new ArrayList<>();
        String query = mdmsDataQueryBuilderV2.getMdmsDataSearchQuery(mdmsCriteriaV2, preparedStmtList);
        log.debug("Generated MDMS data search query with {} parameters", preparedStmtList.size());
        
        try {
            List<Mdms> result = jdbcTemplate.query(query, preparedStmtList.toArray(), mdmsDataRowMapperV2);
            log.debug("MDMS data search completed, records found: {}", result != null ? result.size() : 0);
            return result;
        } catch (Exception e) {
            log.error("Error searching MDMS data from database for tenant: {}, schemaCode: {}", tenantId, schemaCode, e);
            throw e;
        }
    }

    /**
     * @param mdmsCriteria
     * @return
     */
    @Override
    public Map<String, Map<String, JSONArray>> search(MdmsCriteria mdmsCriteria) {
        log.trace("MdmsDataRepositoryImpl.search: method invoked");
        String tenantId = mdmsCriteria != null ? mdmsCriteria.getTenantId() : "null";
        log.info("Searching MDMS v1 data from database for tenant: {}", tenantId);
        
        List<Object> preparedStmtList = new ArrayList<>();
        String query = mdmsDataQueryBuilder.getMdmsDataSearchQuery(mdmsCriteria, preparedStmtList);
        log.debug("Generated MDMS v1 data search query with {} parameters", preparedStmtList.size());
        
        try {
            Map<String, Map<String, JSONArray>> result = jdbcTemplate.query(query, preparedStmtList.toArray(), mdmsDataRowMapper);
            log.debug("MDMS v1 data search completed, tenant count: {}", result != null ? result.size() : 0);
            return result;
        } catch (Exception e) {
            log.error("Error searching MDMS v1 data from database for tenant: {}", tenantId, e);
            throw e;
        }
    }
}