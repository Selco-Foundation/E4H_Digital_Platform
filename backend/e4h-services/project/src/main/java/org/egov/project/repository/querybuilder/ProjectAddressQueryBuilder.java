package org.egov.project.repository.querybuilder;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.egov.common.models.core.ProjectSearchURLParams;
import org.egov.common.models.project.Project;
import org.egov.common.models.project.ProjectSearch;
import org.egov.common.models.project.ProjectSearchRequest;
import org.egov.project.config.ProjectConfiguration;
import org.egov.project.web.models.ProjectSearchCriteria;
import org.egov.project.web.models.ProjectSortCriteria;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import static org.egov.project.util.ProjectConstants.DOT;
import static org.egov.project.util.ProjectConstants.PROJECT_MANAGER;

@Component
@Slf4j
@RequiredArgsConstructor
public class ProjectAddressQueryBuilder {

    private static final String FETCH_PROJECT_ADDRESS_QUERY = "SELECT prj.id as projectId, prj.tenantId as project_tenantId, prj.projectNumber as project_projectNumber, prj.name as project_name, prj.projectType as project_projectType, prj.projectTypeId as project_projectTypeId, prj.projectSubType as project_projectSubtype, " +
            " prj.department as project_department, prj.description as project_description, prj.referenceId as project_referenceId, prj.startDate as project_startDate, prj.endDate as project_endDate, " +
            "prj.isTaskEnabled as project_isTaskEnabled, prj.parent as project_parent, prj.projectHierarchy as project_projectHierarchy, prj.natureOfWork as project_natureOfWork, prj.additionalDetails as project_additionalDetails, prj.isDeleted as project_isDeleted, prj.rowVersion as project_rowVersion, " +
            " prj.createdBy as project_createdBy, prj.lastModifiedBy as project_lastModifiedBy, prj.createdTime as project_createdTime, prj.lastModifiedTime as project_lastModifiedTime, " +
            "addr.id as addressId, addr.tenantId as address_tenantId, addr.projectId as address_projectId, addr.doorNo as address_doorNo, addr.latitude as address_latitude, addr.longitude as address_longitude, addr.locationAccuracy as address_locationAccuracy, " +
            " addr.type as address_type, addr.addressLine1 as address_addressLine1, addr.addressLine2 as address_addressLine2, addr.landmark as address_landmark, addr.city as address_city, addr.pinCode as address_pinCode, " +
            " addr.buildingName as address_buildingName, addr.street as address_street, addr.boundaryType as address_boundaryType, addr.boundary as address_boundary " +
            " " +
            "from project prj " +
            "left join project_address addr " +
            "on prj.id = addr.projectId ";
    private static final String PROJECTS_COUNT_QUERY = "SELECT COUNT(*) FROM project prj " +
            "left join project_address addr " +
            "on prj.id = addr.projectId ";
    private final String paginationWrapper = "SELECT * FROM " +
            "(SELECT *, DENSE_RANK() OVER (ORDER BY project_lastModifiedTime DESC , projectId) offset_ FROM " +
            "({})" +
            " result) result_offset " +
            "WHERE offset_ > ? AND offset_ <= ?";

    private static final String STATUS_COUNT_QUERY = "SELECT status, COUNT(*) AS occurrences " +
            "FROM project prj where prj.status is not null and prj.projecttype = 'Facility' ";

    private final ProjectConfiguration config;

    /* Add WHERE clause before first condition, ADD and for subsequent conditions. Do not add AND before any condition and after "(" */
    private static void addClauseIfRequired(List<Object> values, StringBuilder queryString) {
        if (values.isEmpty())
            queryString.append(" WHERE ( ");
        else if (queryString.toString().lastIndexOf("(") != (queryString.toString().trim().length() - 1)) {
            queryString.append(" AND");
        }
    }

    /* Add conditional clause */
    private static void addConditionalClause(List<Object> values, StringBuilder queryString) {
        if (values.isEmpty())
            queryString.append(" WHERE ");
        else {
            queryString.append(" OR ");
        }
    }

    private static void extracted(Long lastChangedSince, boolean isAncestorProjectId, List<Object> preparedStmtList, Project project, StringBuilder queryBuilder) {
        checkAncestorProject(isAncestorProjectId, preparedStmtList, project, queryBuilder);

        if (StringUtils.isNotBlank(project.getProjectNumber())) {
            addClauseIfRequired(preparedStmtList, queryBuilder);
            queryBuilder.append(" prj.projectNumber =? ");
            preparedStmtList.add(project.getProjectNumber());
        }

        if (StringUtils.isNotBlank(project.getName())) {
            addClauseIfRequired(preparedStmtList, queryBuilder);
            queryBuilder.append(" prj.name LIKE ? ");
            preparedStmtList.add('%' + project.getName() + '%');
        }

        if (StringUtils.isNotBlank(project.getProjectType())) {
            addClauseIfRequired(preparedStmtList, queryBuilder);
            queryBuilder.append(" prj.projectType=? ");
            preparedStmtList.add(project.getProjectType());
        }

        if (StringUtils.isNotBlank(project.getReferenceID())) {
            addClauseIfRequired(preparedStmtList, queryBuilder);
            queryBuilder.append(" prj.referenceId =? ");
            preparedStmtList.add(project.getReferenceID());
        }

        if (StringUtils.isNotBlank(project.getParent())) {
            addClauseIfRequired(preparedStmtList, queryBuilder);
            queryBuilder.append(" prj.parent =? ");
            preparedStmtList.add(project.getParent());
        }

        if (project.getAddress() != null && StringUtils.isNotBlank(project.getAddress().getBoundary())) {
            addClauseIfRequired(preparedStmtList, queryBuilder);
            queryBuilder.append(" addr.boundary=? ");
            preparedStmtList.add(project.getAddress().getBoundary());
        }

        if (StringUtils.isNotBlank(project.getProjectSubType())) {
            addClauseIfRequired(preparedStmtList, queryBuilder);
            queryBuilder.append(" prj.projectSubtype=? ");
            preparedStmtList.add(project.getProjectSubType());
        }

        if (project.getStartDate() != null && project.getStartDate() != 0) {
            addClauseIfRequired(preparedStmtList, queryBuilder);
            queryBuilder.append(" prj.startDate >= ? ");
            preparedStmtList.add(project.getStartDate());
        }

        if (project.getEndDate() != null && project.getEndDate() != 0) {
            addClauseIfRequired(preparedStmtList, queryBuilder);
            queryBuilder.append(" prj.endDate <= ? ");
            preparedStmtList.add(project.getEndDate());
        }

        if (lastChangedSince != null && lastChangedSince != 0) {
            addClauseIfRequired(preparedStmtList, queryBuilder);
            queryBuilder.append(" ( prj.lastModifiedTime >= ? )");
            preparedStmtList.add(lastChangedSince);
        }
    }

    private static void checkAncestorProject(boolean isAncestorProjectId, List<Object> preparedStmtList, Project project, StringBuilder queryBuilder) {
        if (isAncestorProjectId && StringUtils.isNotBlank(project.getId())) {
            addClauseIfRequired(preparedStmtList, queryBuilder);
            queryBuilder.append(" ( prj.projectHierarchy LIKE ? OR prj.id =? ) ");
            preparedStmtList.add('%' + project.getId() + '%');
            preparedStmtList.add(project.getId());
        } else if (StringUtils.isNotBlank(project.getId())) {
            addClauseIfRequired(preparedStmtList, queryBuilder);
            queryBuilder.append(" prj.id =? ");
            preparedStmtList.add(project.getId());
        }
    }

    private static void addClause(String tenantId, List<Object> preparedStmtList, StringBuilder queryBuilder) {
        if (StringUtils.isNotBlank(tenantId)) {
            addClauseIfRequired(preparedStmtList, queryBuilder);
            if (!tenantId.contains(DOT)) {
                log.info("State level tenant");
                queryBuilder.append(" prj.tenantId like ? ");
                preparedStmtList.add(tenantId + '%');
            } else {
                log.info("City level tenant");
                queryBuilder.append(" prj.tenantId=? ");
                preparedStmtList.add(tenantId);
            }
        }
    }

    public String getProjectSearchQuery(ProjectSearchCriteria criteria) {
        //This uses a ternary operator to choose between PROJECTS_COUNT_QUERY or FETCH_PROJECT_ADDRESS_QUERY based on the value of isCountQuery.
        String query = criteria.isCountQuery() ? PROJECTS_COUNT_QUERY : FETCH_PROJECT_ADDRESS_QUERY;
        StringBuilder queryBuilder = new StringBuilder(query);

        Integer count = criteria.getProjects().size();

        for (Project project : criteria.getProjects()) {

            addClause(criteria.getTenantId(), criteria.getPreparedStmtList(), queryBuilder);

            /*
             * If isAncestorProjectId is set to true, Then either id equals to project id or projectHierarchy
             *  should have id of the project
             */
            extracted(criteria.getLastChangedSince(), criteria.isAncestorProjectId(), criteria.getPreparedStmtList(), project, queryBuilder);

            if (criteria.getCreatedFrom() != null && criteria.getCreatedFrom() != 0) {
                addClauseIfRequired(criteria.getPreparedStmtList(), queryBuilder);
                queryBuilder.append(" prj.createdTime >= ? ");
                criteria.getPreparedStmtList().add(criteria.getCreatedFrom());
            }

            if (criteria.getCreatedTo() != null && criteria.getCreatedTo() != 0) {
                addClauseIfRequired(criteria.getPreparedStmtList(), queryBuilder);
                queryBuilder.append(" prj.createdTime <= ? ");
                criteria.getPreparedStmtList().add(criteria.getCreatedTo());
            }

            //Add clause if includeDeleted is true in request parameter
            addIsDeletedCondition(criteria.getPreparedStmtList(), queryBuilder, criteria.getIncludeDeleted());

            queryBuilder.append(" )");
            count--;
            addORClause(count, queryBuilder);
        }

        if (criteria.isCountQuery()) {
            return queryBuilder.toString();
        }

        //Wrap constructed SQL query with where criteria in pagination query
        return addPaginationWrapper(queryBuilder.toString(), criteria.getPreparedStmtList(), criteria.getLimit(), criteria.getOffset());
    }

    public String getStatusProjectOccurence(String parentProjectId, List<Object> preparedStmtList) {
        StringBuilder queryBuilder = new StringBuilder(STATUS_COUNT_QUERY);
        if (parentProjectId != null && !parentProjectId.isEmpty()) {
            queryBuilder.append(" AND prj.parent =? ");
            preparedStmtList.add(parentProjectId);
        }
        queryBuilder.append("GROUP BY status ORDER BY occurrences DESC;");

        return queryBuilder.toString();
    }

    /**
     * Constructs the SQL query string for searching projects based on the given parameters.
     *
     * @param projectSearchRequest    The search criteria provided in the request body.
     * @param urlParams        The search criteria provided as URL parameters.
     * @param preparedStmtList The list to which prepared statement parameters will be added.
     * @param isCountQuery     Boolean flag indicating if the query is for counting records.
     * @return The constructed SQL query string.
     */
    public String getProjectSearchQuery(@NotNull @Valid ProjectSearchRequest projectSearchRequest,
                                        ProjectSearchURLParams urlParams,
                                        List<Object> preparedStmtList,
                                        Boolean isCountQuery,
                                        List<String> workflowStatuses)
    {
        // Use a ternary operator to select between PROJECTS_COUNT_QUERY and FETCH_PROJECT_ADDRESS_QUERY based on isCountQuery flag.
        String query = isCountQuery ? PROJECTS_COUNT_QUERY : FETCH_PROJECT_ADDRESS_QUERY;
        StringBuilder queryBuilder = new StringBuilder(query);

        // Get user info
        var userInfo = projectSearchRequest.getRequestInfo().getUserInfo();
        String userUuid = userInfo.getUuid();
        boolean isProjectManager = false;
        if (userInfo.getRoles() != null) {
            isProjectManager = userInfo.getRoles().stream().anyMatch(role -> PROJECT_MANAGER.equalsIgnoreCase(role.getCode()));
        }

        if (!isProjectManager) {
            queryBuilder.append("JOIN project_staff ps ON ps.projectid = prj.id ");
        }

        ProjectSearch projectSearch = projectSearchRequest.getProject();
        // Check if tenant ID is provided in URL parameters
        addClause(urlParams.getTenantId(), preparedStmtList, queryBuilder);

        // Check if project IDs are provided
        addClauseOnProjects(projectSearch, preparedStmtList, queryBuilder, userUuid, isProjectManager);

        // Check if boundary code is provided
        if (projectSearch.getBoundaryCode() != null && StringUtils.isNotBlank(projectSearch.getBoundaryCode())) {
            addClauseIfRequired(preparedStmtList, queryBuilder);
            List<String> boundaryCodes = Arrays.stream(projectSearch.getBoundaryCode().split(","))
                    .map(String::trim)
                    .filter(StringUtils::isNotBlank)
                    .distinct()
                    .collect(Collectors.toList());
            queryBuilder.append(" addr.boundary IN (").append(createQuery(boundaryCodes)).append(")");
            addToPreparedStatement(preparedStmtList, boundaryCodes);
        }

        // Check if sub-project type ID is provided
        if (StringUtils.isNotBlank(projectSearch.getSubProjectTypeId())) {
            addClauseIfRequired(preparedStmtList, queryBuilder);
            queryBuilder.append(" prj.projectSubtype=? ");
            preparedStmtList.add(projectSearch.getSubProjectTypeId());
        }

        // Check if start date is provided
        if (projectSearch.getStartDate() != null && projectSearch.getStartDate() != 0) {
            addClauseIfRequired(preparedStmtList, queryBuilder);
            queryBuilder.append(" prj.startDate >= ? ");
            preparedStmtList.add(projectSearch.getStartDate());
        }

        // Check if end date is provided
        if (projectSearch.getEndDate() != null && projectSearch.getEndDate() != 0) {
            addClauseIfRequired(preparedStmtList, queryBuilder);
            queryBuilder.append(" prj.endDate <= ? ");
            preparedStmtList.add(projectSearch.getEndDate());
        }

        // Check if lastChangedSince is provided
        if (urlParams.getLastChangedSince() != null && urlParams.getLastChangedSince() != 0) {
            addClauseIfRequired(preparedStmtList, queryBuilder);
            queryBuilder.append(" ( prj.lastModifiedTime >= ? )");
            preparedStmtList.add(urlParams.getLastChangedSince());
        }

        // Check if parent is provided
        if (StringUtils.isNotBlank(projectSearch.getParent())) {
            addClauseIfRequired(preparedStmtList, queryBuilder);
            queryBuilder.append(" prj.parent =? ");
            preparedStmtList.add(projectSearch.getParent());
        }

        // Check if createdFrom date is provided
        if (urlParams.getCreatedFrom() != null && urlParams.getCreatedFrom() != 0) {
            addClauseIfRequired(preparedStmtList, queryBuilder);
            queryBuilder.append(" prj.createdTime >= ? ");
            preparedStmtList.add(urlParams.getCreatedFrom());
        }

        // Check if createdTo date is provided
        if (urlParams.getCreatedTo() != null && urlParams.getCreatedTo() != 0) {
            addClauseIfRequired(preparedStmtList, queryBuilder);
            queryBuilder.append(" prj.createdTime <= ? ");
            preparedStmtList.add(urlParams.getCreatedTo());
        }

        // Add clause if includeDeleted is true in request parameter
        addIsDeletedCondition(preparedStmtList, queryBuilder, urlParams.getIncludeDeleted());

        // Check if workflowStatuses filter is provided
        if (workflowStatuses != null && !workflowStatuses.isEmpty()) {
            addClauseIfRequired(preparedStmtList, queryBuilder);
            queryBuilder.append(" prj.status IN (");
            String placeholders = workflowStatuses.stream().map(ws -> "?").collect(Collectors.joining(", "));
            queryBuilder.append(placeholders).append(") ");
            preparedStmtList.addAll(workflowStatuses);
        }

        // Close the query with a closing bracket
        queryBuilder.append(" )");

        // Return query if it's a count query
        if (isCountQuery) {
            return queryBuilder.toString();
        }

        // Wrap constructed SQL query with pagination criteria
        return addPaginationWrapper(queryBuilder.toString(), preparedStmtList, urlParams.getLimit(), urlParams.getOffset());
    }

    private void addClauseOnProjects(ProjectSearch projectSearch, List<Object> preparedStmtList,
                                     StringBuilder queryBuilder, String userUuid, boolean isProjectManager) {
        if (!CollectionUtils.isEmpty(projectSearch.getId())) {
            addClauseIfRequired(preparedStmtList, queryBuilder);
            queryBuilder.append(" prj.id IN (").append(createQuery(projectSearch.getId())).append(")");
            addToPreparedStatement(preparedStmtList, projectSearch.getId());
        }

        // Check if reference ID is provided
        if (StringUtils.isNotBlank(projectSearch.getReferenceId())) {
            addClauseIfRequired(preparedStmtList, queryBuilder);
            queryBuilder.append(" prj.referenceId =? ");
            preparedStmtList.add(projectSearch.getReferenceId());
        }

        // Check if project name is provided
        if (StringUtils.isNotBlank(projectSearch.getName())) {
            addClauseIfRequired(preparedStmtList, queryBuilder);
            queryBuilder.append(" LOWER(prj.name) LIKE ? ");
            preparedStmtList.add("%" + projectSearch.getName().toLowerCase() + "%");
        }

        // Check if project type ID is provided
        if (StringUtils.isNotBlank(projectSearch.getProjectTypeId())) {
            addClauseIfRequired(preparedStmtList, queryBuilder);
            queryBuilder.append(" prj.projectType=? ");
            preparedStmtList.add(projectSearch.getProjectTypeId());
        }

        // Check if not project manager role
        if (!isProjectManager && StringUtils.isNotBlank(userUuid)) {
            addClauseIfRequired(preparedStmtList, queryBuilder);
            queryBuilder.append(" ps.staffid = ? ");
            preparedStmtList.add(userUuid);
        }
    }

    /* Constructs project search query based on Project Ids */
    public String getProjectSearchQueryBasedOnIds(List<String> projectIds, List<Object> preparedStmtList) {
        StringBuilder queryBuilder = new StringBuilder(FETCH_PROJECT_ADDRESS_QUERY);

        if (projectIds != null && !projectIds.isEmpty()) {
            addConditionalClause(preparedStmtList, queryBuilder);
            queryBuilder.append(" prj.id IN (").append(createQuery(projectIds)).append(")");
            addToPreparedStatement(preparedStmtList, projectIds);
        }

        return queryBuilder.toString();
    }

    private void addIsDeletedCondition(List<Object> preparedStmtList, StringBuilder queryBuilder, Boolean includeDeleted) {
        if (!includeDeleted) {
            addClauseIfRequired(preparedStmtList, queryBuilder);
            queryBuilder.append(" prj.isDeleted = false ");
        }
    }

    private void addORClause(Integer count, StringBuilder queryBuilder) {
        if (count > 0) {
            queryBuilder.append(" OR ( ");
        }
    }

    /* Wrap constructed SQL query with where criteria in pagination query */
    private String addPaginationWrapper(String query, List<Object> preparedStmtList, Integer limitParam, Integer offsetParam) {
        int limit = config.getDefaultLimit();
        int offset = config.getDefaultOffset();
        String finalQuery = paginationWrapper.replace("{}", query);

        if (limitParam != null) {
            if (limitParam <= config.getMaxLimit())
                limit = limitParam;
            else
                limit = config.getMaxLimit();
        }

        if (offsetParam != null)
            offset = offsetParam;

        preparedStmtList.add(offset);
        preparedStmtList.add(limit + offset);

        return finalQuery;
    }

    private String createQuery(Collection<String> ids) {
        StringBuilder builder = new StringBuilder();
        int length = ids.size();
        for (int i = 0; i < length; i++) {
            builder.append(" ? ");
            if (i != length - 1) builder.append(",");
        }
        return builder.toString();
    }

    private void addToPreparedStatement(List<Object> preparedStmtList, Collection<String> ids) {
        preparedStmtList.addAll(ids);
    }

    /* Returns query to search for projects where project_hierarchy contains project Ids */
    public String getProjectDescendantsSearchQueryBasedOnIds(List<String> projectIds, List<Object> preparedStmtListDescendants) {
        StringBuilder queryBuilder = new StringBuilder(FETCH_PROJECT_ADDRESS_QUERY);
        for (String projectId : projectIds) {
            addConditionalClause(preparedStmtListDescendants, queryBuilder);
            queryBuilder.append(" ( prj.projectHierarchy LIKE ? )");
            preparedStmtListDescendants.add('%' + projectId + '%');
        }

        return queryBuilder.toString();
    }

    /* Returns query to get total projects count based on project search params */
    public String getSearchCountQueryString(List<Project> projects, String tenantId, Long lastChangedSince, Boolean includeDeleted, Long createdFrom, Long createdTo, boolean isAncestorProjectId, List<Object> preparedStatement) {
        ProjectSearchCriteria criteria = ProjectSearchCriteria.builder()
                .projects(projects)
                .limit(config.getMaxLimit())
                .offset(config.getDefaultOffset())
                .tenantId(tenantId)
                .lastChangedSince(lastChangedSince)
                .includeDeleted(includeDeleted)
                .createdFrom(createdFrom)
                .createdTo(createdTo)
                .isAncestorProjectId(isAncestorProjectId)
                .preparedStmtList(preparedStatement)
                .isCountQuery(true)
                .build();

        return getProjectSearchQuery(criteria);
    }

    /* Returns query to get total projects count based on project search params */
    public String getSearchCountQueryString(ProjectSearchRequest projectSearchRequest,
                                            ProjectSearchURLParams urlParams,
                                            List<Object> preparedStatement,
                                            List<String> workflowStatuses) {
        return getProjectSearchQuery(projectSearchRequest, urlParams, preparedStatement, Boolean.TRUE, workflowStatuses);
    }

    public String getProjectSearchAndSortQuery(ProjectSearchRequest projectSearchRequest, ProjectSearchURLParams urlParams, List<Object> preparedStmtList, Boolean isCountQuery, List<String> workflowStatuses, ProjectSortCriteria sortParam) {
        String query = getProjectSearchQuery(projectSearchRequest, urlParams, preparedStmtList, isCountQuery, workflowStatuses);
        // Adding sort criteria
        String sortField = null;
        if (sortParam != null && sortParam.getSortBy() != null) {
            String userSortField = sortParam.getSortBy();
            sortField = userSortField.startsWith("project_") ? userSortField : "project_" + userSortField;
        }
        // Determine sort order (default DESC if invalid or null)
        ProjectSortCriteria.SortDirection sortDirection = (sortParam != null && sortParam.getSortDirection() != null)
                ? sortParam.getSortDirection()
                : ProjectSortCriteria.SortDirection.DESC;
        // Default sorting field
        String defaultSortField = "project_lastModifiedTime";
        String defaultSortOrder = ProjectSortCriteria.SortDirection.DESC.name();
        if(projectSearchRequest.getProject() !=null && projectSearchRequest.getProject().getProjectTypeId() !=null
                && projectSearchRequest.getProject().getProjectTypeId().equals("Facility"))
            sortField = "project_name";

        if (sortField != null) {
            query += " ORDER BY " + sortField + " " + sortDirection;
        } else {
            query += " ORDER BY " + defaultSortField + " " + defaultSortOrder;
        }
        return query;
    }
}
