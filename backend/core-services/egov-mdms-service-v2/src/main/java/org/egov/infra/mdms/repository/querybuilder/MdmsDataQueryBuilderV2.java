package org.egov.infra.mdms.repository.querybuilder;

import org.egov.infra.mdms.config.ApplicationConfig;
import org.egov.infra.mdms.model.MdmsCriteriaV2;
import org.egov.infra.mdms.utils.QueryUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;
import lombok.extern.slf4j.Slf4j;

import java.util.*;

@Component
@Slf4j
public class MdmsDataQueryBuilderV2 {

    @Autowired
    private ApplicationConfig config;

    private static final String SEARCH_MDMS_DATA_QUERY = "SELECT data.id, data.tenantid, data.uniqueidentifier, data.schemacode, data.data, data.isactive, data.createdby, data.lastmodifiedby, data.createdtime, data.lastmodifiedtime" +
            " FROM eg_mdms_data data ";

    private static final String MDMS_DATA_QUERY_ORDER_BY_CLAUSE = " order by data.createdtime desc ";

    /**
     * Method to handle request for fetching MDMS data search query
     * @param mdmsCriteriaV2
     * @param preparedStmtList
     * @return
     */
    public String getMdmsDataSearchQuery(MdmsCriteriaV2 mdmsCriteriaV2, List<Object> preparedStmtList) {
        log.trace("MdmsDataQueryBuilderV2.getMdmsDataSearchQuery: method invoked");
        String tenantId = mdmsCriteriaV2 != null ? mdmsCriteriaV2.getTenantId() : "null";
        log.debug("Building MDMS v2 data search query for tenant: {}", tenantId);
        
        String query = buildQuery(mdmsCriteriaV2, preparedStmtList);
        query = QueryUtil.addOrderByClause(query, MDMS_DATA_QUERY_ORDER_BY_CLAUSE);
        query = getPaginatedQuery(query, mdmsCriteriaV2, preparedStmtList);
        log.debug("MDMS v2 data search query built with {} parameters", preparedStmtList != null ? preparedStmtList.size() : 0);
        return query;
    }

    /**
     * Method to build query dynamically based on the criteria passed to the method
     * @param mdmsCriteriaV2
     * @param preparedStmtList
     * @return
     */
    private String buildQuery(MdmsCriteriaV2 mdmsCriteriaV2, List<Object> preparedStmtList) {
        log.trace("MdmsDataQueryBuilderV2.buildQuery: method invoked");
        StringBuilder builder = new StringBuilder(SEARCH_MDMS_DATA_QUERY);
        Map<String, String> schemaCodeFilterMap = mdmsCriteriaV2.getSchemaCodeFilterMap();
        
        int clauseCount = 0;
        if (!Objects.isNull(mdmsCriteriaV2.getTenantId())) {
            QueryUtil.addClauseIfRequired(builder, preparedStmtList);
            builder.append(" data.tenantid = ? ");
            preparedStmtList.add(mdmsCriteriaV2.getTenantId());
            clauseCount++;
        }
        if (!Objects.isNull(mdmsCriteriaV2.getIds())) {
            QueryUtil.addClauseIfRequired(builder, preparedStmtList);
            builder.append(" data.id IN ( ").append(QueryUtil.createQuery(mdmsCriteriaV2.getIds().size())).append(" )");
            QueryUtil.addToPreparedStatement(preparedStmtList, mdmsCriteriaV2.getIds());
            clauseCount++;
            log.debug("Added ID filter with {} IDs", mdmsCriteriaV2.getIds().size());
        }
        if (!Objects.isNull(mdmsCriteriaV2.getUniqueIdentifiers())) {
            QueryUtil.addClauseIfRequired(builder, preparedStmtList);
            builder.append(" data.uniqueidentifier IN ( ").append(QueryUtil.createQuery(mdmsCriteriaV2.getUniqueIdentifiers().size())).append(" )");
            QueryUtil.addToPreparedStatement(preparedStmtList, mdmsCriteriaV2.getUniqueIdentifiers());
            clauseCount++;
            log.debug("Added unique identifier filter with {} identifiers", mdmsCriteriaV2.getUniqueIdentifiers().size());
        }
        if (!Objects.isNull(mdmsCriteriaV2.getSchemaCodeFilterMap())) {
            QueryUtil.addClauseIfRequired(builder, preparedStmtList);
            builder.append(" data.schemacode IN ( ").append(QueryUtil.createQuery(schemaCodeFilterMap.keySet().size())).append(" )");
            QueryUtil.addToPreparedStatement(preparedStmtList, schemaCodeFilterMap.keySet());
            clauseCount++;
            log.debug("Added schema code filter with {} schema codes", schemaCodeFilterMap.keySet().size());
        }
        if(!Objects.isNull(mdmsCriteriaV2.getSchemaCode())){
            QueryUtil.addClauseIfRequired(builder, preparedStmtList);
            builder.append(" data.schemacode = ? ");
            preparedStmtList.add(mdmsCriteriaV2.getSchemaCode());
            clauseCount++;
        }
        if(!CollectionUtils.isEmpty(mdmsCriteriaV2.getFilterMap())){
            QueryUtil.addClauseIfRequired(builder, preparedStmtList);
            builder.append(" data.data @> CAST( ? AS jsonb )");
            String partialQueryJsonString = QueryUtil.preparePartialJsonStringFromFilterMap(mdmsCriteriaV2.getFilterMap());
            preparedStmtList.add(partialQueryJsonString);
            clauseCount++;
            log.debug("Added JSON filter with {} filter criteria", mdmsCriteriaV2.getFilterMap().size());
        }
        if(!CollectionUtils.isEmpty(mdmsCriteriaV2.getUniqueIdentifiersForRefVerification())){
            QueryUtil.addClauseIfRequired(builder, preparedStmtList);
            builder.append(" data.uniqueidentifier IN ( ").append(QueryUtil.createQuery(mdmsCriteriaV2.getUniqueIdentifiersForRefVerification().size())).append(" )");
            QueryUtil.addToPreparedStatement(preparedStmtList, mdmsCriteriaV2.getUniqueIdentifiersForRefVerification());
            clauseCount++;
            log.debug("Added reference verification filter with {} identifiers", mdmsCriteriaV2.getUniqueIdentifiersForRefVerification().size());
        }
        if(!Objects.isNull(mdmsCriteriaV2.getIsActive())) {
            QueryUtil.addClauseIfRequired(builder, preparedStmtList);
            builder.append(" data.isactive = ? ");
            preparedStmtList.add(mdmsCriteriaV2.getIsActive());
            clauseCount++;
        }
        log.debug("Query built with {} WHERE clauses", clauseCount);
        return builder.toString();
    }

    private String getPaginatedQuery(String query, MdmsCriteriaV2 mdmsCriteriaV2, List<Object> preparedStmtList) {
        log.trace("MdmsDataQueryBuilderV2.getPaginatedQuery: method invoked");
        StringBuilder paginatedQuery = new StringBuilder(query);

        // Append offset
        Integer offset = ObjectUtils.isEmpty(mdmsCriteriaV2.getOffset()) ? config.getDefaultOffset() : mdmsCriteriaV2.getOffset();
        paginatedQuery.append(" OFFSET ? ");
        preparedStmtList.add(offset);

        // Append limit
        Integer limit = ObjectUtils.isEmpty(mdmsCriteriaV2.getLimit()) ? config.getDefaultLimit() : mdmsCriteriaV2.getLimit();
        paginatedQuery.append(" LIMIT ? ");
        preparedStmtList.add(limit);

        log.debug("Added pagination with offset: {}, limit: {}", offset, limit);
        return paginatedQuery.toString();
    }

}