package org.egov.field_planner.repository.querybuilder;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.egov.common.models.core.ProjectSearchURLParams;
import org.egov.common.models.project.Project;
import org.egov.common.models.project.ProjectSearch;
import org.egov.common.models.project.ProjectSearchRequest;
import org.egov.field_planner.config.FieldPlannerConfiguration;
import org.egov.field_planner.web.models.FieldPlan;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import static org.egov.field_planner.util.FieldPlannerConstants.DOT;
import static org.egov.field_planner.util.FieldPlannerConstants.PROJECT_MANAGER;

@Component
@Slf4j
@RequiredArgsConstructor
public class FieldPlannerQueryBuilder {

    private static final String FETCCH_FIELDPLAN_NAME = "SELECT tenant_id, name FROM field_plans";

    private final FieldPlannerConfiguration config;

    /* Add WHERE clause before first condition, ADD and for subsequent conditions. Do not add AND before any condition and after "(" */
    private static void addClauseIfRequired(List<Object> values, StringBuilder queryString) {
        if (values.isEmpty())
            queryString.append(" WHERE ");
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

    public String getHighestFielPlanNameQuery(FieldPlan fieldPlan, List<Object> preparedStmtList) {
        StringBuilder queryBuilder = new StringBuilder(FETCCH_FIELDPLAN_NAME);
        if (StringUtils.isNotBlank(fieldPlan.getName())) {
            addClauseIfRequired(preparedStmtList, queryBuilder);
            queryBuilder.append(" LOWER(name) LIKE ? ");
            preparedStmtList.add(fieldPlan.getName().toLowerCase() + "%");
        }
        if (StringUtils.isNotBlank(fieldPlan.getTenantId())) {
            addClauseIfRequired(preparedStmtList, queryBuilder);
            log.info("State level tenant");
            queryBuilder.append(" tenant_id like ? ");
            preparedStmtList.add(fieldPlan.getTenantId() + '%');
        }
        queryBuilder.append("ORDER BY created_time DESC LIMIT 1;");

        return queryBuilder.toString();
    }
}
