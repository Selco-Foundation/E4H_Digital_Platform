package org.egov.infra.mdms.repository.querybuilder;

import org.egov.infra.mdms.model.MdmsCriteria;
import org.egov.infra.mdms.utils.QueryUtil;
import org.springframework.stereotype.Component;
import lombok.extern.slf4j.Slf4j;

import java.util.*;

@Component
@Slf4j
public class MdmsDataQueryBuilder {

    private static String SEARCH_MDMS_DATA_QUERY = "SELECT data.tenantid, data.uniqueidentifier, data.schemacode, data.data, data.isactive, data.createdby, data.lastmodifiedby, data.createdtime, data.lastmodifiedtime" +
            " FROM eg_mdms_data data ";

    private static final String MDMS_DATA_QUERY_ORDER_BY_CLAUSE = " order by data.createdtime desc ";

    /**
     * Method to handle request for fetching MDMS data search query
     * @param mdmsCriteria
     * @param preparedStmtList
     * @return
     */
    public String getMdmsDataSearchQuery(MdmsCriteria mdmsCriteria, List<Object> preparedStmtList) {
        log.trace("MdmsDataQueryBuilder.getMdmsDataSearchQuery: method invoked");
        String tenantId = mdmsCriteria != null ? mdmsCriteria.getTenantId() : "null";
        log.debug("Building MDMS data search query for tenant: {}", tenantId);
        
        String query = buildQuery(mdmsCriteria, preparedStmtList);
        query = QueryUtil.addOrderByClause(query, MDMS_DATA_QUERY_ORDER_BY_CLAUSE);
        log.debug("MDMS data search query built with {} parameters", preparedStmtList != null ? preparedStmtList.size() : 0);
        return query;
    }

    /**
     * Method to build query dynamically based on the criteria passed to the method
     * @param mdmsCriteria
     * @param preparedStmtList
     * @return
     */
    private String buildQuery(MdmsCriteria mdmsCriteria, List<Object> preparedStmtList) {
        log.trace("MdmsDataQueryBuilder.buildQuery: method invoked");
        StringBuilder builder = new StringBuilder(SEARCH_MDMS_DATA_QUERY);
        Map<String, String> schemaCodeFilterMap = mdmsCriteria.getSchemaCodeFilterMap();
        
        int clauseCount = 0;
        if (!Objects.isNull(mdmsCriteria.getTenantId())) {
            QueryUtil.addClauseIfRequired(builder, preparedStmtList);
            builder.append(" data.tenantid LIKE ? ");
            preparedStmtList.add(mdmsCriteria.getTenantId() + "%");
            clauseCount++;
        }
        if (!Objects.isNull(mdmsCriteria.getIds())) {
            QueryUtil.addClauseIfRequired(builder, preparedStmtList);
            builder.append(" data.id IN ( ").append(QueryUtil.createQuery(mdmsCriteria.getIds().size())).append(" )");
            QueryUtil.addToPreparedStatement(preparedStmtList, mdmsCriteria.getIds());
            clauseCount++;
            log.debug("Added ID filter with {} IDs", mdmsCriteria.getIds().size());
        }
        if (!Objects.isNull(mdmsCriteria.getUniqueIdentifier())) {
            QueryUtil.addClauseIfRequired(builder, preparedStmtList);
            builder.append(" data.uniqueidentifier = ? ");
            preparedStmtList.add(mdmsCriteria.getUniqueIdentifier());
            clauseCount++;
        }
        if (!Objects.isNull(mdmsCriteria.getSchemaCodeFilterMap())) {
            QueryUtil.addClauseIfRequired(builder, preparedStmtList);
            builder.append(" data.schemacode IN ( ").append(QueryUtil.createQuery(schemaCodeFilterMap.keySet().size())).append(" )");
            QueryUtil.addToPreparedStatement(preparedStmtList, schemaCodeFilterMap.keySet());
            clauseCount++;
            log.debug("Added schema code filter with {} schema codes", schemaCodeFilterMap.keySet().size());
        }
        if(!Objects.isNull(mdmsCriteria.getIsActive())) {
            QueryUtil.addClauseIfRequired(builder, preparedStmtList);
            builder.append(" data.isactive = ? ");
            preparedStmtList.add(mdmsCriteria.getIsActive());
            clauseCount++;
        }
        log.debug("Query built with {} WHERE clauses", clauseCount);
        return builder.toString();
    }

}