package digit.repository.querybuilder;

import digit.util.QueryUtil;
import digit.web.models.BoundaryRelationshipSearchCriteria;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;
import java.util.HashSet;
import java.util.List;

@Component
@Slf4j
public class BoundaryRelationshipQueryBuilder {

    private static String BOUNDARY_RELATIONSHIP_BASE_SEARCH_QUERY = "SELECT id, tenantid, code, hierarchytype, boundarytype, parent, ancestralmaterializedpath, createdtime, createdby, lastmodifiedtime, lastmodifiedby" +
            " FROM boundary_relationship ";

    private static String ORDER_BY_CLAUSE = " order by createdtime desc ";

    public String getBoundaryRelationshipSearchQuery(BoundaryRelationshipSearchCriteria boundaryRelationshipSearchCriteria, List<Object> preparedStmtList) {
        log.trace("getBoundaryRelationshipSearchQuery method invoked");
        log.debug("Building boundary relationship search query, tenantId={}, hierarchyType={}", 
                boundaryRelationshipSearchCriteria.getTenantId(),
                boundaryRelationshipSearchCriteria.getHierarchyType());
        String query = buildQuery(boundaryRelationshipSearchCriteria, preparedStmtList);
        query += ORDER_BY_CLAUSE;
        log.debug("Boundary relationship search query built, parameters count={}", preparedStmtList.size());
        return query;
    }

    private String buildQuery(BoundaryRelationshipSearchCriteria boundaryRelationshipSearchCriteria, List<Object> preparedStmtList) {
        log.trace("buildQuery method invoked");
        StringBuilder builder = new StringBuilder(BOUNDARY_RELATIONSHIP_BASE_SEARCH_QUERY);

        addTenantClause(boundaryRelationshipSearchCriteria, preparedStmtList, builder);
        addHierarchyTypeClause(boundaryRelationshipSearchCriteria, preparedStmtList, builder);
        addParentClause(boundaryRelationshipSearchCriteria, preparedStmtList, builder);
        addParentCodesClause(boundaryRelationshipSearchCriteria, preparedStmtList, builder);
        addBoundaryTypeAndCodesClause(boundaryRelationshipSearchCriteria, preparedStmtList, builder);
        addRootNodeClause(boundaryRelationshipSearchCriteria, preparedStmtList, builder);
        addCurrentBoundaryCodesClause(boundaryRelationshipSearchCriteria, preparedStmtList, builder);

        return builder.toString();
    }

    /**
     * Adds tenant id condition: "tenantid = ?"
     */
    private void addTenantClause(BoundaryRelationshipSearchCriteria criteria,
                                 List<Object> preparedStmtList,
                                 StringBuilder builder) {
        if (ObjectUtils.isEmpty(criteria.getTenantId())) {
            return;
        }

        QueryUtil.addClauseIfRequired(builder, preparedStmtList);
        builder.append(" tenantid = ? ");
        preparedStmtList.add(criteria.getTenantId());
    }

    /**
     * Adds hierarchy type condition: "hierarchytype = ?"
     */
    private void addHierarchyTypeClause(BoundaryRelationshipSearchCriteria criteria,
                                        List<Object> preparedStmtList,
                                        StringBuilder builder) {
        if (ObjectUtils.isEmpty(criteria.getHierarchyType())) {
            return;
        }

        QueryUtil.addClauseIfRequired(builder, preparedStmtList);
        builder.append(" hierarchytype = ? ");
        preparedStmtList.add(criteria.getHierarchyType());
    }

    /**
     * Adds parent condition: "parent = ?"
     */
    private void addParentClause(BoundaryRelationshipSearchCriteria criteria,
                                 List<Object> preparedStmtList,
                                 StringBuilder builder) {
        if (ObjectUtils.isEmpty(criteria.getParent())) {
            return;
        }

        QueryUtil.addClauseIfRequired(builder, preparedStmtList);
        builder.append(" parent = ? ");
        preparedStmtList.add(criteria.getParent());
    }

    /**
     * Adds parent codes condition: "parent IN (?, ?, ...)"
     */
    private void addParentCodesClause(BoundaryRelationshipSearchCriteria criteria,
                                      List<Object> preparedStmtList,
                                      StringBuilder builder) {
        if (CollectionUtils.isEmpty(criteria.getParentCodes())) {
            return;
        }

        QueryUtil.addClauseIfRequired(builder, preparedStmtList);
        builder.append(" parent IN ( ")
                .append(QueryUtil.createQuery(criteria.getParentCodes().size()))
                .append(" )");
        QueryUtil.addToPreparedStatement(preparedStmtList, new HashSet<>(criteria.getParentCodes()));
    }

    /**
     * Adds boundary type and codes conditions when not searching for root node.
     */
    private void addBoundaryTypeAndCodesClause(BoundaryRelationshipSearchCriteria criteria,
                                               List<Object> preparedStmtList,
                                               StringBuilder builder) {
        if (criteria.getIsSearchForRootNode()) {
            return;
        }

        if (!ObjectUtils.isEmpty(criteria.getBoundaryType())) {
            QueryUtil.addClauseIfRequired(builder, preparedStmtList);
            builder.append(" boundarytype = ? ");
            preparedStmtList.add(criteria.getBoundaryType());
        }

        if (!CollectionUtils.isEmpty(criteria.getCodes())) {
            QueryUtil.addClauseIfRequired(builder, preparedStmtList);
            builder.append(" code IN ( ")
                    .append(QueryUtil.createQuery(criteria.getCodes().size()))
                    .append(" )");
            QueryUtil.addToPreparedStatement(preparedStmtList, new HashSet<>(criteria.getCodes()));
        }
    }

    /**
     * Adds root node condition: "parent IS NULL" when searching for root node.
     */
    private void addRootNodeClause(BoundaryRelationshipSearchCriteria criteria,
                                   List<Object> preparedStmtList,
                                   StringBuilder builder) {
        if (!criteria.getIsSearchForRootNode()) {
            return;
        }

        QueryUtil.addClauseIfRequired(builder, preparedStmtList);
        builder.append(" parent IS NULL ");
    }

    /**
     * Adds current boundary codes overlap condition using ancestralmaterializedpath.
     */
    private void addCurrentBoundaryCodesClause(BoundaryRelationshipSearchCriteria criteria,
                                               List<Object> preparedStmtList,
                                               StringBuilder builder) {
        if (CollectionUtils.isEmpty(criteria.getCurrentBoundaryCodes())) {
            return;
        }

        QueryUtil.addClauseIfRequired(builder, preparedStmtList);
        builder.append(" ARRAY [ ")
                .append(QueryUtil.createQuery(criteria.getCurrentBoundaryCodes().size()))
                .append(" ]")
                .append("::text[] ");
        builder.append(" && string_to_array(ancestralmaterializedpath, '|') ");
        QueryUtil.addToPreparedStatement(preparedStmtList, new HashSet<>(criteria.getCurrentBoundaryCodes()));
    }

}
