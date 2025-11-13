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
        log.debug("Building boundary hierarchy type search query with criteria: {}", boundaryTypeHierarchySearchCriteria);
        String query = buildQuery(boundaryTypeHierarchySearchCriteria, preparedStmtList, BOUNDARY_HIERARCHY_TYPE_BASE_SEARCH_QUERY);
        query = QueryUtil.addOrderByClause(query, ORDER_BY_CLAUSE);
        query = getPaginatedQuery(query , boundaryTypeHierarchySearchCriteria , preparedStmtList);
        log.debug("Final boundary hierarchy search query: {}", query);
        log.debug("Prepared statement values: {}", preparedStmtList);
        return query;
    }

    public String getBoundaryHierarchyTypeCountQuery(BoundaryTypeHierarchySearchCriteria boundaryTypeHierarchySearchCriteria, List<Object> preparedStmtList) {
        log.debug("Building boundary hierarchy type count query with criteria: {}", boundaryTypeHierarchySearchCriteria);
        String query = buildQuery(boundaryTypeHierarchySearchCriteria, preparedStmtList, BOUNDARY_HIERARCHY_TYPE_COUNT_QUERY);
        log.debug("Final boundary hierarchy count query: {}", query);
        log.debug("Prepared statement values: {}", preparedStmtList);
        return query;
    }
    private String buildQuery(BoundaryTypeHierarchySearchCriteria boundaryTypeHierarchySearchCriteria, List<Object> preparedStmtList, String Query) {
        StringBuilder builder = new StringBuilder(Query);

        if (!ObjectUtils.isEmpty(boundaryTypeHierarchySearchCriteria.getTenantId())) {
            log.trace("Adding tenantId filter: {}", boundaryTypeHierarchySearchCriteria.getTenantId());
            QueryUtil.addClauseIfRequired(builder, preparedStmtList);
            builder.append(" tenantid = ? ");
            preparedStmtList.add(boundaryTypeHierarchySearchCriteria.getTenantId());
        }

        if (!ObjectUtils.isEmpty(boundaryTypeHierarchySearchCriteria.getHierarchyType())) {
            log.trace("Adding hierarchyType filter: {}", boundaryTypeHierarchySearchCriteria.getHierarchyType());
            QueryUtil.addClauseIfRequired(builder, preparedStmtList);
            builder.append(" hierarchytype = ? ");
            preparedStmtList.add(boundaryTypeHierarchySearchCriteria.getHierarchyType());
        }

        log.debug("Built intermediate query: {}", builder);
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
        log.debug("Applying pagination to boundary hierarchy query: {}", query);
        StringBuilder paginatedQuery = new StringBuilder(query);

        // Append offset
        paginatedQuery.append(" OFFSET ? ");
        preparedStmtList.add(ObjectUtils.isEmpty(boundaryTypeHierarchySearchCriteria.getOffset()) ? config.getDefaultOffset() : boundaryTypeHierarchySearchCriteria.getOffset());

        // Append limit
        paginatedQuery.append(" LIMIT ? ");
        preparedStmtList.add(ObjectUtils.isEmpty(boundaryTypeHierarchySearchCriteria.getLimit()) ? config.getDefaultLimit() : (boundaryTypeHierarchySearchCriteria.getLimit() > config.getMaxDefaultLimit() ? config.getMaxDefaultLimit() : boundaryTypeHierarchySearchCriteria.getLimit()) );

        log.debug("Final paginated query: {}", paginatedQuery);
        return paginatedQuery.toString();
    }
}
