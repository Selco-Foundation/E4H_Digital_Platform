package digit.repository.impl;

import digit.config.ApplicationProperties;
import digit.kafka.Producer;
import digit.repository.BoundaryRelationshipRepository;
import digit.repository.querybuilder.BoundaryRelationshipQueryBuilder;
import digit.repository.rowmapper.BoundaryRelationshipRowMapper;
import digit.web.models.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;

@Repository
@Slf4j
public class BoundaryRelationshipRepositoryImpl implements BoundaryRelationshipRepository {

    private Producer producer;

    private JdbcTemplate jdbcTemplate;

    private BoundaryRelationshipQueryBuilder boundaryRelationshipQueryBuilder;

    private BoundaryRelationshipRowMapper boundaryRelationshipRowMapper;

    private ApplicationProperties applicationProperties;

    public BoundaryRelationshipRepositoryImpl(Producer producer, JdbcTemplate jdbcTemplate,
                                              BoundaryRelationshipQueryBuilder boundaryRelationshipQueryBuilder, BoundaryRelationshipRowMapper boundaryRelationshipRowMapper, ApplicationProperties applicationProperties) {
        this.producer = producer;
        this.jdbcTemplate = jdbcTemplate;
        this.boundaryRelationshipQueryBuilder = boundaryRelationshipQueryBuilder;
        this.boundaryRelationshipRowMapper = boundaryRelationshipRowMapper;
        this.applicationProperties = applicationProperties;
    }

    /**
     * This method implements boundary relationship interface. In this implementation
     * it pushes the request to kafka for persister to pick it up and perform create.
     * @param boundaryRelationshipRequest
     */
    @Override
    public void create(BoundaryRelationshipRequest boundaryRelationshipRequest) {
        log.trace("create method invoked");
        String code = boundaryRelationshipRequest.getBoundaryRelationship() != null
                ? boundaryRelationshipRequest.getBoundaryRelationship().getCode() : null;
        log.debug("Creating boundary relationship, code={}", code);

        // Transform boundary relationship request
        log.debug("Converting boundary relationship request to DTO");
        BoundaryRelationshipRequestDTO boundaryRelationshipRequestDTO = convertContractPOJOToDTO(boundaryRelationshipRequest);

        // Push to event bus for creating asynchronously
        log.debug("Publishing boundary relationship create request to Kafka, topic={}",
                applicationProperties.getCreateBoundaryRelationshipTopic());
        producer.push(applicationProperties.getCreateBoundaryRelationshipTopic(), boundaryRelationshipRequestDTO);
        log.debug("Boundary relationship create request published to Kafka successfully");
    }

    /**
     * This method implements boundary relationship interface's update method. In this implementation
     * it pushes the request to kafka for persister to pick it up and perform update.
     * @param boundaryRelationshipRequestDTO
     */
    @Override
    public void update(BoundaryRelationshipRequestDTO boundaryRelationshipRequestDTO) {
        log.trace("update method invoked");
        String code = boundaryRelationshipRequestDTO.getBoundaryRelationshipDTO() != null
                ? boundaryRelationshipRequestDTO.getBoundaryRelationshipDTO().getCode() : null;
        int updateCount = boundaryRelationshipRequestDTO.getBoundaryRelationshipDTOList() != null
                ? boundaryRelationshipRequestDTO.getBoundaryRelationshipDTOList().size() : 0;
        log.debug("Updating boundary relationship, code={}, total nodes to update={}", code, updateCount);

        // Push to event bus for updating asynchronously
        log.debug("Publishing boundary relationship update request to Kafka, topic={}",
                applicationProperties.getUpdateBoundaryRelationshipTopic());
        producer.push(applicationProperties.getUpdateBoundaryRelationshipTopic(), boundaryRelationshipRequestDTO);
        log.debug("Boundary relationship update request published to Kafka successfully");
    }

    /**
     * This method implements boundary relationship repository interface. In this implementation
     * it creates query to search data in PostgreSQL database and returns the search response back
     * to the caller.
     * @param boundaryRelationshipSearchCriteria
     * @return
     */
    @Override
    public List<BoundaryRelationshipDTO> search(BoundaryRelationshipSearchCriteria boundaryRelationshipSearchCriteria) {
        log.trace("search method invoked");
        log.debug("Searching boundary relationships, tenantId={}, hierarchyType={}",
                boundaryRelationshipSearchCriteria.getTenantId(),
                boundaryRelationshipSearchCriteria.getHierarchyType());

        // Declare prepared statement list
        List<Object> preparedStmtList = new ArrayList<>();

        // Get query for searching boundary relationship
        String query = boundaryRelationshipQueryBuilder.getBoundaryRelationshipSearchQuery(boundaryRelationshipSearchCriteria, preparedStmtList, false);
        log.debug("Executing boundary relationship search query");

        // Return search response based on provided search criteria
        List<BoundaryRelationshipDTO> results = jdbcTemplate.query(query, preparedStmtList.toArray(), boundaryRelationshipRowMapper);
        log.debug("Boundary relationship search query executed, found {} relationships", results.size());
        return results;
    }

    public Integer getBoundaryCount(BoundaryRelationshipSearchCriteria boundaryRelationshipSearchCriteria) {
        List<Object> preparedStatement = new ArrayList<>();
        String query = boundaryRelationshipQueryBuilder.getBoundaryRelationshipSearchQuery(boundaryRelationshipSearchCriteria, preparedStatement, true);
        if (query == null)
            return 0;

        Integer count = jdbcTemplate.queryForObject(query, preparedStatement.toArray(), Integer.class);
        log.info("Total boundary count is : " + count);
        return count;
    }

    /**
     * Helper method to convert boundary relationship POJOs into boundary relationship DTOs
     * @param contractBean
     * @return
     */
    private BoundaryRelationshipRequestDTO convertContractPOJOToDTO(BoundaryRelationshipRequest contractBean) {
        log.trace("convertContractPOJOToDTO method invoked");
        String code = contractBean.getBoundaryRelationship() != null
                ? contractBean.getBoundaryRelationship().getCode() : null;
        log.debug("Converting boundary relationship POJO to DTO, code={}", code);

        // Declare boundary relationship request DTO
        BoundaryRelationshipRequestDTO boundaryRelationshipRequestDTO = new BoundaryRelationshipRequestDTO();

        // Copy boundary relationship properties
        BoundaryRelationshipDTO boundaryRelationshipDTO = new BoundaryRelationshipDTO();
        BeanUtils.copyProperties(contractBean.getBoundaryRelationship(), boundaryRelationshipDTO);
        BeanUtils.copyProperties(contractBean, boundaryRelationshipRequestDTO);

        // Enrich ancestral materialized path
        boundaryRelationshipDTO.setAncestralMaterializedPath(contractBean.getBoundaryRelationship().getAncestralMaterializedPath());

        // Enrich boundary relationship DTO in request
        boundaryRelationshipRequestDTO.setBoundaryRelationshipDTO(boundaryRelationshipDTO);

        log.debug("Successfully converted boundary relationship POJO to DTO");
        return boundaryRelationshipRequestDTO;
    }
}
