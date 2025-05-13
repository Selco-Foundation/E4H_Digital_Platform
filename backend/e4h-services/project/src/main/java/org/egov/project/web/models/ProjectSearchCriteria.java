package org.egov.project.web.models;

import lombok.Builder;
import lombok.Data;
import org.egov.common.models.project.Project;

import java.util.List;

/**
 * Encapsulates all parameters for building a project search query.
 */
@Data
@Builder
public class ProjectSearchCriteria {

    /**
     * The list of Project objects to filter on.
     */
    private List<Project> projects;

    /**
     * Maximum number of records to return (pagination).
     */
    private Integer limit;

    /**
     * Offset into the result set (pagination).
     */
    private Integer offset;

    /**
     * Tenant identifier (state or city level).
     */
    private String tenantId;

    /**
     * Only include projects modified since this timestamp.
     */
    private Long lastChangedSince;

    /**
     * Whether to include deleted projects.
     */
    private Boolean includeDeleted;

    /**
     * Only include projects created on or after this timestamp.
     */
    private Long createdFrom;

    /**
     * Only include projects created on or before this timestamp.
     */
    private Long createdTo;

    /**
     * If true, treat each project ID as an ancestor and match on hierarchy or exact ID.
     */
    private boolean isAncestorProjectId;

    /**
     * Prepared‐statement parameter list (will be populated as query is built).
     */
    private List<Object> preparedStmtList;

    /**
     * If true, build a COUNT(*) query; otherwise build a fetch query.
     */
    private boolean isCountQuery;
}
