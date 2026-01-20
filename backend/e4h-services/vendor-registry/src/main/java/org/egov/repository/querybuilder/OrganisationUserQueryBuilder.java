package org.egov.repository.querybuilder;

import lombok.extern.slf4j.Slf4j;
import org.egov.common.models.core.URLParams;
import org.egov.config.Configuration;
import org.egov.web.models.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.Collection;
import java.util.List;

@Component
@Slf4j
public class OrganisationUserQueryBuilder {

    private final Configuration config;

    private static final String FETCH_ORGANISATION_USER_QUERY = "SELECT ou.id as id, ou.tenantid as tenantId, " +
            "ou.organizationid as organizationId, ou.userid as userId, ou.additionaldetails as additionalDetails, " +
            "ou.createdby as createdBy, ou.lastmodifiedby as lastModifiedBy, " +
            "ou.createdtime as createdTime, ou.lastmodifiedtime as lastModifiedTime, ou.isdeleted as isDeleted " +
            "FROM eg_org_user ou";

    private static final String PAGINATION_WRAPPER = "SELECT * FROM " +
            "(SELECT *, DENSE_RANK() OVER (ORDER BY lastModifiedTime DESC , id) offset_ FROM " +
            "({})" +
            " result) result_offset " +
            "WHERE offset_ > ? AND offset_ <= ?";

    private static final String ORGANISATIONS_USERS_COUNT_QUERY = "SELECT COUNT(*) from eg_org_user ou ";

    private static final String COUNT_WRAPPER = "SELECT COUNT(*) FROM ({INTERNAL_QUERY}) as count";

    @Autowired
    public OrganisationUserQueryBuilder(Configuration config) {
        this.config = config;
    }

    public String getOrganisationUserSearchQuery(OrgUserSearchRequest orgSearchRequest, URLParams urlParams, List<Object> preparedStmtList, Boolean isCountQuery) {
        String query = Boolean.TRUE.equals(isCountQuery) ? ORGANISATIONS_USERS_COUNT_QUERY : FETCH_ORGANISATION_USER_QUERY;
        StringBuilder queryBuilder = new StringBuilder(query);
        OrgUserSearchCriteria searchCriteria = orgSearchRequest.getCriteria();

        if (!CollectionUtils.isEmpty(searchCriteria.getId())) {
            addClauseIfRequired(preparedStmtList, queryBuilder);
            queryBuilder.append(" ou.id IN (").append(createQuery(searchCriteria.getId())).append(")");
            preparedStmtList.addAll(searchCriteria.getId());
        }

        if (!CollectionUtils.isEmpty(searchCriteria.getUserId())) {
            addClauseIfRequired(preparedStmtList, queryBuilder);
            queryBuilder.append(" ou.userid IN (").append(createQuery(searchCriteria.getUserId())).append(")");
            preparedStmtList.addAll(searchCriteria.getUserId());
        }

        if (!CollectionUtils.isEmpty(searchCriteria.getOrganizationId())) {
            addClauseIfRequired(preparedStmtList, queryBuilder);
            queryBuilder.append(" ou.organizationid IN (").append(createQuery(searchCriteria.getOrganizationId())).append(")");
            preparedStmtList.addAll(searchCriteria.getOrganizationId());
        }

        //Add clause if includeDeleted is true in request parameter
        addIsDeletedCondition(preparedStmtList, queryBuilder, urlParams.getIncludeDeleted());

        if (Boolean.TRUE.equals(isCountQuery)) {
            return queryBuilder.toString();
        }

        Pagination pagination = Pagination.builder().limit(Double.valueOf(urlParams.getLimit()+"")).offset(Double.valueOf(urlParams.getOffset()+"")).build();
        addOrderByClause(queryBuilder, pagination);
        return addPaginationWrapper(queryBuilder.toString(), preparedStmtList, pagination);
    }

    private void addIsDeletedCondition(List<Object> preparedStmtList, StringBuilder queryBuilder, Boolean includeDeleted) {
        if (!includeDeleted) {
            addClauseIfRequired(preparedStmtList, queryBuilder);
            queryBuilder.append(" ou.isdeleted = false ");
        }
    }

    private static void addClauseIfRequired(List<Object> values, StringBuilder queryString) {
        if (values.isEmpty())
            queryString.append(" WHERE ");
        else {
            queryString.append(" AND");
        }
    }

    private void addToPreparedStatement(List<Object> preparedStmtList, Collection<String> ids) {
        preparedStmtList.addAll(ids);
    }

    private String  createQuery(Collection<String> ids) {
        StringBuilder builder = new StringBuilder();
        int length = ids.size();
        for (int i = 0; i < length; i++) {
            builder.append(" ? ");
            if (i != length - 1) builder.append(",");
        }
        return builder.toString();
    }

    private void addOrderByClause(StringBuilder queryBuilder, Pagination pagination) {
        log.info("OrganisationQueryBuilder::getOrganisationQuery");
        //default
        if (pagination == null || pagination.getSortBy() == null) {
            queryBuilder.append(" ORDER BY ou.createdtime ");
        } else {
            switch (pagination.getSortBy()) {
                case "name":
                    queryBuilder.append(" ORDER BY ou.name ");
                    break;
                case "type":
                    queryBuilder.append(" ORDER BY ou.type ");
                    break;
                default:
                    queryBuilder.append(" ORDER BY ou.createdtime ");
                    break;
            }
        }

        if (pagination != null && pagination.getOrder() == "ASC")
            queryBuilder.append(" ASC ");
        else queryBuilder.append(" DESC ");
    }

    private String addPaginationWrapper(String query, List<Object> preparedStmtList, Pagination pagination) {
        log.info("OrganisationQueryBuilder::addPaginationWrapper");
        double limit = config.getDefaultLimit();
        double offset = config.getDefaultOffset();
        String finalQuery = PAGINATION_WRAPPER.replace("{}", query);

        if (pagination != null && pagination.getLimit() != null) {
            if (pagination.getLimit() <= config.getMaxLimit())
                limit = pagination.getLimit();
            else
                limit = config.getMaxLimit();
        }

        if (pagination != null && pagination.getOffset() != null)
            offset = pagination.getOffset();

        preparedStmtList.add(offset);
        preparedStmtList.add(limit + offset);

        return finalQuery;
    }

//    public String getSearchCountQueryString(OrgSearchRequest orgSearchRequest, Set<String> orgIdsFromIdentifierAndBoundarySearch, List<Object> preparedStmtList) {
//        log.info("OrganisationSearchQueryBuilder::getSearchCountQueryString");
//        String query = getOrganisationSearchQuery(orgSearchRequest, orgIdsFromIdentifierAndBoundarySearch, preparedStmtList, true);
//        if (query != null)
//            return COUNT_WRAPPER.replace("{INTERNAL_QUERY}", query);
//        else
//            return query;
//    }
}
