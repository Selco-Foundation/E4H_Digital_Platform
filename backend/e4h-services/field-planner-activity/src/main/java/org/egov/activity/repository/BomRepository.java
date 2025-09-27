package org.egov.activity.repository;

import lombok.extern.slf4j.Slf4j;
import org.egov.activity.repository.querybuilder.BomQueryBuilder;
import org.egov.activity.repository.querybuilder.DocumentQueryBuilder;
import org.egov.activity.repository.rowmapper.BomRowMapper;
import org.egov.activity.repository.rowmapper.DocumentRowMapper;
import org.egov.activity.web.models.*;
import org.egov.common.data.query.builder.SelectQueryBuilder;
import org.egov.common.data.repository.GenericRepository;
import org.egov.common.models.core.URLParams;
import org.egov.common.models.project.Project;
import org.egov.common.producer.Producer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Repository
@Slf4j
public class BomRepository extends GenericRepository<BillOfMaterial> {

    private final BomQueryBuilder queryBuilder;

    private final DocumentQueryBuilder documentQueryBuilder;
    private final JdbcTemplate jdbcTemplate;
    private final BomRowMapper bomRowMapper;

    private final DocumentRowMapper documentRowMapper;


    @Autowired
    public BomRepository(Producer producer, NamedParameterJdbcTemplate namedParameterJdbcTemplate,
                         RedisTemplate<String, Object> redisTemplate, BomRowMapper bomRowMapper,
                         SelectQueryBuilder selectQueryBuilder, DocumentQueryBuilder documentQueryBuilder,
                         JdbcTemplate jdbcTemplate, BomQueryBuilder queryBuilder, DocumentRowMapper documentRowMapper) {
        super(producer, namedParameterJdbcTemplate, redisTemplate, selectQueryBuilder,
                bomRowMapper, Optional.of("bom"));
        this.queryBuilder = queryBuilder;
        this.jdbcTemplate = jdbcTemplate;
        this.bomRowMapper = bomRowMapper;
        this.documentRowMapper = documentRowMapper;
        this.documentQueryBuilder = documentQueryBuilder;
    }

    public List<BillOfMaterial> getBillOfMaterials(BomSearchRequest request, Integer limit, Integer offset, String tenantId, Boolean includeDeleted, Long lastChangedSince) {
        //Fetch FieldPlans based on search criteria
        List<Object> preparedStmtList = new ArrayList<>();
        BomSearchCriteria criteria = request.getCriteria();
        criteria.setCountQuery(false);
        URLParams urlParams = URLParams.builder().limit(limit).offset(offset).tenantId(tenantId).includeDeleted(includeDeleted).lastChangedSince(lastChangedSince).build();

        String query = queryBuilder.getBOMSearchQuery(criteria, urlParams, preparedStmtList);
        List<BillOfMaterial> billOfMaterials = jdbcTemplate.query(query, bomRowMapper, preparedStmtList.toArray());
        List<Document> documents = new ArrayList<>();
        if(billOfMaterials !=null && !billOfMaterials.isEmpty()){
            Set<String> bomIds = billOfMaterials.stream().map(BillOfMaterial::getId).collect(Collectors.toSet());
            //Fetch documents based on Project Ids
            documents = getDocumentsBasedOnBomIds(bomIds);
            if (documents != null && documents.size() > 0) {
                for (BillOfMaterial bom : billOfMaterials) {
                    log.info("Adding Documents to bom " + bom.getId());
                    addDocumentToBOM(bom, documents);
                }
            }
        }

        log.info("Fetched project list based on given search criteria");
        return billOfMaterials;
    }

    public Integer getBillOfMatrialsCount(BomSearchRequest request, String tenantId, Long lastChangedSince, Boolean includeDeleted) {
        List<Object> preparedStatement = new ArrayList<>();
        String query = queryBuilder.getSearchCountQueryString(request, tenantId, lastChangedSince, includeDeleted, preparedStatement);

        if (query == null)
            return 0;

        Integer count = jdbcTemplate.queryForObject(query, preparedStatement.toArray(), Integer.class);
        log.info("Total FieldPlans count is : " + count);
        return count;
    }

    /* Fetch documents based on Project Ids */
    private List<Document> getDocumentsBasedOnBomIds(Set<String> bomIds) {
        List<Object> preparedStmtListDocument = new ArrayList<>();
        String queryDocument = documentQueryBuilder.getDocumentSearchQuery(bomIds, preparedStmtListDocument);
        List<Document> documents = jdbcTemplate.query(queryDocument, documentRowMapper, preparedStmtListDocument.toArray());
        log.info("Fetched documents based on project Ids");
        return documents;
    }

    private void addDocumentToBOM(BillOfMaterial bom, List<Document> documents) {
        bom.setDocuments(new ArrayList<>());
        for (Document document : documents) {
            if (document.getBomId().equals(bom.getId())
                    && (document.getStatus() == null || document.getStatus() != null && !document.getStatus().equals("INACTIVE"))
                    && bom.getDocuments().stream().noneMatch(t -> t.getId().equals(document.getId()))) {
                bom.getDocuments().add(document);
            }
        }
    }
}