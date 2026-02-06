package digit.repository.querybuilder;

import digit.config.ApplicationProperties;
import digit.util.QueryUtil;
import digit.web.models.BoundarySearchCriteria;
import digit.web.models.BoundaryTypeHierarchyDefinition;
import digit.web.models.BoundaryTypeHierarchySearchCriteria;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.ObjectUtils;
import java.util.List;

@Component
@Slf4j
public class BoundaryHierarchyTypeQueryBuilder {

    private ApplicationProperties config;

    public BoundaryHierarchyTypeQueryBuilder(ApplicationProperties config) {
        this.config = config;
    }

    private static String BOUNDARY_HIERARCHY_TYPE_BASE_SEARCH_QUERY = "SELECT id, tenantid, hierarchytype, boundaryhierarchy, createdtime, lastmodifiedtime, createdby, lastmodifiedby" +
            " FROM boundary_hierarchy ";

    private static String ORDER_BY_CLAUSE = " order by createdtime desc ";

    private static String BOUNDARY_HIERARCHY_TYPE_COUNT_QUERY = "SELECT count(*) FROM boundary_hierarchy ";

    public String getBoundaryHierarchyTypeSearchQuery(BoundaryTypeHierarchySearchCriteria boundaryTypeHierarchySearchCriteria, List<Object> preparedStmtList) {
        log.trace("getBoundaryHierarchyTypeSearchQuery method invoked");
        log.debug("Building boundary hierarchy type search query, tenantId={}, hierarchyType={}", 
                boundaryTypeHierarchySearchCriteria.getTenantId(),
                boundaryTypeHierarchySearchCriteria.getHierarchyType());
        String query = buildQuery(boundaryTypeHierarchySearchCriteria, preparedStmtList, BOUNDARY_HIERARCHY_TYPE_BASE_SEARCH_QUERY);
        query = QueryUtil.addOrderByClause(query, ORDER_BY_CLAUSE);
        query = getPaginatedQuery(query , boundaryTypeHierarchySearchCriteria , preparedStmtList);
        log.debug("Boundary hierarchy type search query built, parameters count={}", preparedStmtList.size());
        return query;
    }

    public String getBoundaryHierarchyTypeCountQuery(BoundaryTypeHierarchySearchCriteria boundaryTypeHierarchySearchCriteria, List<Object> preparedStmtList) {
        log.trace("getBoundaryHierarchyTypeCountQuery method invoked");
        log.debug("Building boundary hierarchy type count query, tenantId={}, hierarchyType={}", 
                boundaryTypeHierarchySearchCriteria.getTenantId(),
                boundaryTypeHierarchySearchCriteria.getHierarchyType());
        String query = buildQuery(boundaryTypeHierarchySearchCriteria, preparedStmtList, BOUNDARY_HIERARCHY_TYPE_COUNT_QUERY);
        log.debug("Boundary hierarchy type count query built, parameters count={}", preparedStmtList.size());
        return query;
    }
    private String buildQuery(BoundaryTypeHierarchySearchCriteria boundaryTypeHierarchySearchCriteria, List<Object> preparedStmtList, String Query) {
        log.trace("buildQuery method invoked");
        StringBuilder builder = new StringBuilder(Query);

        if (!ObjectUtils.isEmpty(boundaryTypeHierarchySearchCriteria.getTenantId())) {
            QueryUtil.addClauseIfRequired(builder, preparedStmtList);
            builder.append(" tenantid = ? ");
            preparedStmtList.add(boundaryTypeHierarchySearchCriteria.getTenantId());
        }

        if (!ObjectUtils.isEmpty(boundaryTypeHierarchySearchCriteria.getHierarchyType())) {
            QueryUtil.addClauseIfRequired(builder, preparedStmtList);
            builder.append(" hierarchytype = ? ");
            preparedStmtList.add(boundaryTypeHierarchySearchCriteria.getHierarchyType());
        }

        return builder.toString();
    }

    /**
     * Method to add pagination to the query
     * @param query
     * @param boundaryTypeHierarchySearchCriteria
     * @param preparedStmtList
     * @return
     */
    private String getPaginatedQuery(String query, BoundaryTypeHierarchySearchCriteria boundaryTypeHierarchySearchCriteria , List<Object> preparedStmtList) {
        log.trace("getPaginatedQuery method invoked");
        Integer offset = ObjectUtils.isEmpty(boundaryTypeHierarchySearchCriteria.getOffset()) ? config.getDefaultOffset() : boundaryTypeHierarchySearchCriteria.getOffset();
        Integer limit = ObjectUtils.isEmpty(boundaryTypeHierarchySearchCriteria.getLimit()) ? config.getDefaultLimit() : (boundaryTypeHierarchySearchCriteria.getLimit() > config.getMaxDefaultLimit() ? config.getMaxDefaultLimit() : boundaryTypeHierarchySearchCriteria.getLimit());
        log.debug("Adding pagination to query, offset={}, limit={}", offset, limit);
        
        StringBuilder paginatedQuery = new StringBuilder(query);

        // Append offset
        paginatedQuery.append(" OFFSET ? ");
        preparedStmtList.add(offset);

        // Append limit
        paginatedQuery.append(" LIMIT ? ");
        preparedStmtList.add(limit);

        log.debug("Pagination added to query");
        return paginatedQuery.toString();
    }
}
