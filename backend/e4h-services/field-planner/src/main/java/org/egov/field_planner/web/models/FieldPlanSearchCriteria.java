package org.egov.field_planner.web.models;

import lombok.Builder;
import lombok.Data;
import org.egov.common.models.project.Project;

import java.util.List;

/**
 * Encapsulates all parameters for building a project search query.
 */
@Data
@Builder
public class FieldPlanSearchCriteria {

    /**
     * The list of FieldPlan objects to filter on.
     */
    private List<FieldPlan> fieldPlans;

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
     * Whether to include deleted projects.
     */
    private Boolean includeDeleted;

    /**
     * Only include FieldPlan created on or after this timestamp.
     */
    private Long createdFrom;

    /**
     * Only include projects modified since this timestamp.
     */
    private Long lastChangedSince;

    /**
     * Only include FieldPlan created on or before this timestamp.
     */
    private Long createdTo;

    /**
     * Prepared‐statement parameter list (will be populated as query is built).
     */
    private List<Object> preparedStmtList;

    /**
     * If true, build a COUNT(*) query; otherwise build a fetch query.
     */
    private boolean isCountQuery;
}
