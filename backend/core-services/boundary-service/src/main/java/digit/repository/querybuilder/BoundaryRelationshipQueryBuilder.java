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
        String query = buildQuery(boundaryRelationshipSearchCriteria, preparedStmtList);
        query += ORDER_BY_CLAUSE;
        return query;
    }

    private String buildQuery(BoundaryRelationshipSearchCriteria boundaryRelationshipSearchCriteria, List<Object> preparedStmtList) {
        log.debug("Starting to build boundary relationship query with criteria: {}", boundaryRelationshipSearchCriteria);
        StringBuilder builder = new StringBuilder(BOUNDARY_RELATIONSHIP_BASE_SEARCH_QUERY);

        if (!ObjectUtils.isEmpty(boundaryRelationshipSearchCriteria.getTenantId())) {
            QueryUtil.addClauseIfRequired(builder, preparedStmtList);
            builder.append(" tenantid = ? ");
            preparedStmtList.add(boundaryRelationshipSearchCriteria.getTenantId());
            log.debug("Added tenantId filter: {}", boundaryRelationshipSearchCriteria.getTenantId());
        }

        if (!ObjectUtils.isEmpty(boundaryRelationshipSearchCriteria.getHierarchyType())) {
            QueryUtil.addClauseIfRequired(builder, preparedStmtList);
            builder.append(" hierarchytype = ? ");
            preparedStmtList.add(boundaryRelationshipSearchCriteria.getHierarchyType());
            log.debug("Added hierarchyType filter: {}", boundaryRelationshipSearchCriteria.getHierarchyType());
        }

        if(!ObjectUtils.isEmpty(boundaryRelationshipSearchCriteria.getParent())) {
            QueryUtil.addClauseIfRequired(builder, preparedStmtList);
            builder.append(" parent = ? ");
            preparedStmtList.add(boundaryRelationshipSearchCriteria.getParent());
            log.debug("Added parent filter: {}", boundaryRelationshipSearchCriteria.getParent());
        }

        if(!boundaryRelationshipSearchCriteria.getIsSearchForRootNode()) {
            if (!ObjectUtils.isEmpty(boundaryRelationshipSearchCriteria.getBoundaryType())) {
                QueryUtil.addClauseIfRequired(builder, preparedStmtList);
                builder.append(" boundarytype = ? ");
                preparedStmtList.add(boundaryRelationshipSearchCriteria.getBoundaryType());
                log.debug("Added boundaryType filter: {}", boundaryRelationshipSearchCriteria.getBoundaryType());
            }

            if (!CollectionUtils.isEmpty(boundaryRelationshipSearchCriteria.getCodes())) {
                QueryUtil.addClauseIfRequired(builder, preparedStmtList);
                builder.append(" code IN ( ").append(QueryUtil.createQuery(boundaryRelationshipSearchCriteria.getCodes().size())).append(" )");
                QueryUtil.addToPreparedStatement(preparedStmtList, new HashSet<>(boundaryRelationshipSearchCriteria.getCodes()));
                log.debug("Added codes filter: {}", boundaryRelationshipSearchCriteria.getCodes());
            }
        }

        if(boundaryRelationshipSearchCriteria.getIsSearchForRootNode()) {
            QueryUtil.addClauseIfRequired(builder, preparedStmtList);
            builder.append(" parent IS NULL ");
            log.debug("Added condition for root node (parent IS NULL)");
        }

        if(!CollectionUtils.isEmpty(boundaryRelationshipSearchCriteria.getCurrentBoundaryCodes())) {
            QueryUtil.addClauseIfRequired(builder, preparedStmtList);
            builder.append(" ARRAY [ ").append(QueryUtil.createQuery(boundaryRelationshipSearchCriteria.getCurrentBoundaryCodes().size())).append(" ]").append("::text[] ");
            builder.append(" && string_to_array(ancestralmaterializedpath, '|') ");
            QueryUtil.addToPreparedStatement(preparedStmtList, new HashSet<>(boundaryRelationshipSearchCriteria.getCurrentBoundaryCodes()));
            log.debug("Added currentBoundaryCodes filter: {}", boundaryRelationshipSearchCriteria.getCurrentBoundaryCodes());
        }

        log.debug("Final built query: {}", builder.toString());
        log.debug("Prepared statement values: {}", preparedStmtList);
        return builder.toString();
    }

}
