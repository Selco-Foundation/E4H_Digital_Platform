package org.egov.infra.mdms.repository.querybuilder;

import org.egov.infra.mdms.config.ApplicationConfig;
import org.egov.infra.mdms.model.SchemaDefCriteria;
import org.egov.infra.mdms.utils.QueryUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.ObjectUtils;
import lombok.extern.slf4j.Slf4j;

import java.util.*;

@Component
@Slf4j
public class SchemaDefinitionQueryBuilder {

    @Autowired
    private ApplicationConfig config;

    private static final String SEARCH_SCHEMA_DEF_QUERY = "SELECT schema.id,schema.tenantid, schema.code, schema.description, schema.definition, schema.isactive, " +
            "schema.createdby, schema.lastmodifiedby, schema.createdtime, schema.lastmodifiedtime FROM " +
            "eg_mdms_schema_definition schema ";

    private static final String SEARCH_SCHEMA_DEF_ORDER_BY_CLAUSE = " order by schema.createdtime desc ";

    /**
     * Method to handle request for fetching schema search query
     * @param schemaDefCriteria
     * @param preparedStmtList
     * @return
     */
    public String getSchemaSearchQuery(SchemaDefCriteria schemaDefCriteria, List<Object> preparedStmtList) {
        log.trace("SchemaDefinitionQueryBuilder.getSchemaSearchQuery: method invoked");
        String tenantId = schemaDefCriteria != null ? schemaDefCriteria.getTenantId() : "null";
        log.debug("Building schema definition search query for tenant: {}", tenantId);
        
        String query = buildQuery(schemaDefCriteria, preparedStmtList);
        query = QueryUtil.addOrderByClause(query, SEARCH_SCHEMA_DEF_ORDER_BY_CLAUSE);
        query = getPaginatedQuery(query, schemaDefCriteria, preparedStmtList);
        log.debug("Schema definition search query built with {} parameters", preparedStmtList != null ? preparedStmtList.size() : 0);
        return query;
    }

    /**
     * Method to build query dynamically based on the criteria passed to the method
     * @param schemaDefCriteria
     * @param preparedStmtList
     * @return
     */
    private String buildQuery(SchemaDefCriteria schemaDefCriteria, List<Object> preparedStmtList) {
        log.trace("SchemaDefinitionQueryBuilder.buildQuery: method invoked");
        StringBuilder builder = new StringBuilder(SchemaDefinitionQueryBuilder.SEARCH_SCHEMA_DEF_QUERY);

        int clauseCount = 0;
        if (!Objects.isNull(schemaDefCriteria.getTenantId())) {
            QueryUtil.addClauseIfRequired(builder, preparedStmtList);
            builder.append(" schema.tenantid = ? ");
            preparedStmtList.add(schemaDefCriteria.getTenantId());
            clauseCount++;
        }
        if (!Objects.isNull(schemaDefCriteria.getCodes())) {
            QueryUtil.addClauseIfRequired(builder, preparedStmtList);
            builder.append(" schema.code IN ( ").append(QueryUtil.createQuery(schemaDefCriteria.getCodes().size())).append(" )");
            QueryUtil.addToPreparedStatement(preparedStmtList, new HashSet<>(schemaDefCriteria.getCodes()));
            clauseCount++;
            log.debug("Added schema code filter with {} codes", schemaDefCriteria.getCodes().size());
        }

        log.debug("Query built with {} WHERE clauses", clauseCount);
        return builder.toString();
    }

    private String getPaginatedQuery(String query, SchemaDefCriteria schemaDefCriteria, List<Object> preparedStmtList) {
        log.trace("SchemaDefinitionQueryBuilder.getPaginatedQuery: method invoked");
        StringBuilder paginatedQuery = new StringBuilder(query);

        // Append offset
        Integer offset = ObjectUtils.isEmpty(schemaDefCriteria.getOffset()) ? config.getDefaultOffset() : schemaDefCriteria.getOffset();
        paginatedQuery.append(" OFFSET ? ");
        preparedStmtList.add(offset);

        // Append limit
        Integer limit = ObjectUtils.isEmpty(schemaDefCriteria.getLimit()) ? config.getDefaultLimit() : schemaDefCriteria.getLimit();
        paginatedQuery.append(" LIMIT ? ");
        preparedStmtList.add(limit);

        log.debug("Added pagination with offset: {}, limit: {}", offset, limit);
        return paginatedQuery.toString();
    }

}